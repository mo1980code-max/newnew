package org.Allah_Clock_Live_Wallpaper.utils;

import android.content.Context;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.model.QuranJuz;
import org.Allah_Clock_Live_Wallpaper.model.QuranPage;
import org.Allah_Clock_Live_Wallpaper.model.QuranSearchResult;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Offline source of truth for the Quran reader.
 *
 * <p>The Uthmani text is deliberately shipped as a read-only raw asset rather than fetched
 * from a network API. The reader therefore opens without a connection, keeps a single
 * verified text source, and never changes the source text while formatting it for display.
 * The original Tanzil license is bundled alongside the text in {@code res/raw}.</p>
 */
public final class QuranRepository {

    public static final int SURAH_COUNT = 114;
    public static final int AYAH_COUNT = 6236;
    public static final int PAGE_COUNT = 604;
    public static final int JUZ_COUNT = 30;

    private static final Pattern REFERENCE = Pattern.compile(
            "^\\s*(\\d{1,3})\\s*[:：]\\s*(\\d{1,3})\\s*$");
    private static volatile QuranRepository instance;

    private final List<QuranSurah> surahs = new ArrayList<>();
    private final Map<Integer, QuranSurah> surahsByNumber = new HashMap<>();
    private final Map<Integer, List<QuranAyah>> ayahsBySurah = new HashMap<>();
    private final List<QuranAyah> allAyahs = new ArrayList<>();
    private final Map<String, Integer> globalAyahIndexes = new HashMap<>();
    private final List<QuranPage> pages = new ArrayList<>();
    private final Map<String, Integer> pageByAyah = new HashMap<>();
    private final List<QuranJuz> juzs = new ArrayList<>();
    private final Map<String, Integer> juzByAyah = new HashMap<>();

    private QuranRepository(@NonNull Context context) throws IOException {
        Resources resources = context.getResources();
        loadSurahs(resources);
        loadAyahs(resources);
        loadPages(resources);
        loadJuzs(resources);
        validate();
    }

    /**
     * Loads once per process. Call this from a worker thread the first time because parsing
     * the complete bundled text is intentionally done off the UI thread.
     */
    @NonNull
    public static QuranRepository get(@NonNull Context context) throws IOException {
        QuranRepository local = instance;
        if (local != null) {
            return local;
        }
        synchronized (QuranRepository.class) {
            local = instance;
            if (local == null) {
                local = new QuranRepository(context.getApplicationContext());
                instance = local;
            }
            return local;
        }
    }

    private void loadSurahs(@NonNull Resources resources) throws IOException {
        try (InputStream input = resources.openRawResource(R.raw.quran_surahs);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] fields = line.split("\\|", -1);
                if (fields.length != 7) {
                    throw new IOException("Invalid Quran surah metadata row");
                }
                int number = positiveInt(fields[0], "surah number");
                int ayahCount = positiveInt(fields[2], "ayah count");
                if (number != surahs.size() + 1 || fields[3].trim().isEmpty()
                        || fields[4].trim().isEmpty()) {
                    throw new IOException("Invalid Quran surah metadata order");
                }
                boolean meccan;
                if ("Meccan".equals(fields[6])) {
                    meccan = true;
                } else if ("Medinan".equals(fields[6])) {
                    meccan = false;
                } else {
                    throw new IOException("Invalid Quran revelation type");
                }
                QuranSurah surah = new QuranSurah(number, ayahCount, fields[3], fields[4],
                        fields[5], meccan);
                surahs.add(surah);
                surahsByNumber.put(number, surah);
                ayahsBySurah.put(number, new ArrayList<>());
            }
        }
    }

    private void loadAyahs(@NonNull Resources resources) throws IOException {
        try (InputStream input = resources.openRawResource(R.raw.quran_uthmani);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // The upstream text carries its mandatory attribution as trailing # comments.
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int first = line.indexOf('|');
                int second = first < 0 ? -1 : line.indexOf('|', first + 1);
                if (first <= 0 || second <= first + 1 || second == line.length() - 1) {
                    throw new IOException("Invalid Quran text row");
                }
                int surahNumber = positiveInt(line.substring(0, first), "ayah surah number");
                int ayahNumber = positiveInt(line.substring(first + 1, second), "ayah number");
                QuranSurah surah = surahsByNumber.get(surahNumber);
                List<QuranAyah> ayahs = ayahsBySurah.get(surahNumber);
                if (surah == null || ayahs == null || ayahNumber != ayahs.size() + 1
                        || ayahNumber > surah.getAyahCount()) {
                    throw new IOException("Unexpected Quran ayah order");
                }
                String text = line.substring(second + 1);
                QuranAyah ayah = new QuranAyah(surahNumber, ayahNumber, text,
                        normalizeForSearch(text));
                ayahs.add(ayah);
                globalAyahIndexes.put(ayah.getKey(), allAyahs.size());
                allAyahs.add(ayah);
            }
        }
    }

    /**
     * Reads the 604 traditional Madani-page starts. Tanzil puts a [115, 1] sentinel after its
     * JavaScript list; the generated TSV intentionally excludes that one-past-the-end marker.
     */
    private void loadPages(@NonNull Resources resources) throws IOException {
        List<Integer> startIndexes = new ArrayList<>();
        try (InputStream input = resources.openRawResource(R.raw.quran_pages);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] fields = line.split("\\|", -1);
                if (fields.length != 3) {
                    throw new IOException("Invalid Quran page metadata row");
                }
                int page = positiveInt(fields[0], "page number");
                int surah = positiveInt(fields[1], "page surah number");
                int ayah = positiveInt(fields[2], "page ayah number");
                Integer index = globalAyahIndexes.get(surah + ":" + ayah);
                if (page != startIndexes.size() + 1 || index == null
                        || (!startIndexes.isEmpty() && index <= startIndexes.get(startIndexes.size() - 1))) {
                    throw new IOException("Invalid Quran page metadata order");
                }
                startIndexes.add(index);
            }
        }
        if (startIndexes.size() != PAGE_COUNT || startIndexes.get(0) != 0) {
            throw new IOException("Quran page metadata must contain 604 ordered pages");
        }
        for (int pageIndex = 0; pageIndex < startIndexes.size(); pageIndex++) {
            int start = startIndexes.get(pageIndex);
            int end = pageIndex + 1 < startIndexes.size()
                    ? startIndexes.get(pageIndex + 1) - 1 : allAyahs.size() - 1;
            if (end < start) {
                throw new IOException("Empty Quran page metadata range");
            }
            QuranPage page = new QuranPage(pageIndex + 1, start, end,
                    allAyahs.get(start), allAyahs.get(end));
            pages.add(page);
            for (int ayahIndex = start; ayahIndex <= end; ayahIndex++) {
                pageByAyah.put(allAyahs.get(ayahIndex).getKey(), page.getNumber());
            }
        }
    }

    /** Reads the thirty traditional juz starts used for fast local navigation. */
    private void loadJuzs(@NonNull Resources resources) throws IOException {
        List<Integer> startIndexes = new ArrayList<>();
        try (InputStream input = resources.openRawResource(R.raw.quran_juz);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] fields = line.split("\\|", -1);
                if (fields.length != 3) {
                    throw new IOException("Invalid Quran juz metadata row");
                }
                int juz = positiveInt(fields[0], "juz number");
                int surah = positiveInt(fields[1], "juz surah number");
                int ayah = positiveInt(fields[2], "juz ayah number");
                Integer index = globalAyahIndexes.get(surah + ":" + ayah);
                if (juz != startIndexes.size() + 1 || index == null
                        || (!startIndexes.isEmpty() && index <= startIndexes.get(startIndexes.size() - 1))) {
                    throw new IOException("Invalid Quran juz metadata order");
                }
                startIndexes.add(index);
            }
        }
        if (startIndexes.size() != JUZ_COUNT || startIndexes.get(0) != 0) {
            throw new IOException("Quran juz metadata must contain 30 ordered juzs");
        }
        for (int juzIndex = 0; juzIndex < startIndexes.size(); juzIndex++) {
            int start = startIndexes.get(juzIndex);
            int end = juzIndex + 1 < startIndexes.size()
                    ? startIndexes.get(juzIndex + 1) - 1 : allAyahs.size() - 1;
            QuranAyah first = allAyahs.get(start);
            Integer pageNumber = pageByAyah.get(first.getKey());
            if (end < start || pageNumber == null) {
                throw new IOException("Invalid Quran juz metadata range");
            }
            QuranJuz juz = new QuranJuz(juzIndex + 1, pageNumber, first);
            juzs.add(juz);
            for (int ayahIndex = start; ayahIndex <= end; ayahIndex++) {
                juzByAyah.put(allAyahs.get(ayahIndex).getKey(), juz.getNumber());
            }
        }
    }

    private void validate() throws IOException {
        if (surahs.size() != SURAH_COUNT) {
            throw new IOException("Quran index must contain 114 surahs");
        }
        int total = 0;
        for (QuranSurah surah : surahs) {
            List<QuranAyah> list = ayahsBySurah.get(surah.getNumber());
            if (list == null || list.size() != surah.getAyahCount()) {
                throw new IOException("Unexpected ayah count in Quran data");
            }
            total += list.size();
            ayahsBySurah.put(surah.getNumber(), Collections.unmodifiableList(list));
        }
        if (total != AYAH_COUNT) {
            throw new IOException("Quran text must contain 6236 ayahs");
        }
        if (pages.size() != PAGE_COUNT || pageByAyah.size() != total) {
            throw new IOException("Quran page metadata does not cover every ayah");
        }
        if (juzs.size() != JUZ_COUNT || juzByAyah.size() != total) {
            throw new IOException("Quran juz metadata does not cover every ayah");
        }
    }

    private static int positiveInt(@NonNull String value, @NonNull String label)
            throws IOException {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new NumberFormatException();
            }
            return parsed;
        } catch (NumberFormatException error) {
            throw new IOException("Invalid " + label, error);
        }
    }

    @NonNull
    public List<QuranSurah> getSurahs() {
        return Collections.unmodifiableList(surahs);
    }

    @Nullable
    public QuranSurah getSurah(int number) {
        return surahsByNumber.get(number);
    }

    @NonNull
    public List<QuranAyah> getAyahs(int surahNumber) {
        List<QuranAyah> result = ayahsBySurah.get(surahNumber);
        return result == null ? Collections.emptyList() : result;
    }

    @Nullable
    public QuranAyah getAyah(int surahNumber, int ayahNumber) {
        List<QuranAyah> ayahs = ayahsBySurah.get(surahNumber);
        if (ayahs == null || ayahNumber < 1 || ayahNumber > ayahs.size()) {
            return null;
        }
        return ayahs.get(ayahNumber - 1);
    }

    @Nullable
    public QuranPage getPage(int pageNumber) {
        if (pageNumber < 1 || pageNumber > pages.size()) {
            return null;
        }
        return pages.get(pageNumber - 1);
    }

    /** Returns the canonical Madani-page number for a valid ayah, or -1 when unknown. */
    public int getPageForAyah(int surahNumber, int ayahNumber) {
        Integer page = pageByAyah.get(surahNumber + ":" + ayahNumber);
        return page == null ? -1 : page;
    }

    /** A read-only, compact list of the ayahs falling within one canonical page boundary. */
    @NonNull
    public List<QuranAyah> getAyahsForPage(int pageNumber) {
        QuranPage page = getPage(pageNumber);
        if (page == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(allAyahs.subList(
                page.getStartIndex(), page.getEndIndex() + 1)));
    }

    @Nullable
    public QuranJuz getJuz(int juzNumber) {
        if (juzNumber < 1 || juzNumber > juzs.size()) {
            return null;
        }
        return juzs.get(juzNumber - 1);
    }

    /** Returns the traditional juz containing a valid ayah, or -1 when unknown. */
    public int getJuzForAyah(int surahNumber, int ayahNumber) {
        Integer juz = juzByAyah.get(surahNumber + ":" + ayahNumber);
        return juz == null ? -1 : juz;
    }

    /** Returns the juz in which the first ayah of this page falls. */
    public int getJuzForPage(int pageNumber) {
        QuranPage page = getPage(pageNumber);
        return page == null ? -1 : getJuzForAyah(page.getFirstAyah().getSurahNumber(),
                page.getFirstAyah().getAyahNumber());
    }

    /**
     * Searches local surah names and all locally bundled ayahs. Arabic diacritics and common
     * alef/ya variants are ignored for matching only; the displayed Quran text remains exactly
     * the original Uthmani source text.
     */
    @NonNull
    public SearchResults search(@Nullable String rawQuery, int visibleLimit) {
        String query = normalizeForSearch(rawQuery);
        if (query.isEmpty()) {
            return new SearchResults(Collections.emptyList(), 0);
        }

        int limit = Math.max(1, visibleLimit);
        Matcher reference = rawQuery == null ? null : REFERENCE.matcher(normalizeForSearch(rawQuery));
        if (reference != null && reference.matches()) {
            try {
                QuranAyah ayah = getAyah(Integer.parseInt(reference.group(1)),
                        Integer.parseInt(reference.group(2)));
                if (ayah != null) {
                    QuranSurah surah = getSurah(ayah.getSurahNumber());
                    if (surah != null) {
                        return new SearchResults(Collections.singletonList(
                                QuranSearchResult.forAyah(surah, ayah)), 1);
                    }
                }
            } catch (NumberFormatException ignored) {
                // Fall through to the normal textual search.
            }
        }

        List<QuranSearchResult> visible = new ArrayList<>();
        int total = 0;

        for (QuranSurah surah : surahs) {
            if (matchesSurah(surah, query)) {
                total++;
                if (visible.size() < limit) {
                    visible.add(QuranSearchResult.forSurah(surah));
                }
            }
        }
        for (QuranSurah surah : surahs) {
            for (QuranAyah ayah : getAyahs(surah.getNumber())) {
                if (fuzzyContains(ayah.getNormalizedSearchText(), query)) {
                    total++;
                    if (visible.size() < limit) {
                        visible.add(QuranSearchResult.forAyah(surah, ayah));
                    }
                }
            }
        }
        return new SearchResults(visible, total);
    }

    private static boolean matchesSurah(@NonNull QuranSurah surah, @NonNull String query) {
        return fuzzyContains(normalizeForSearch(surah.getArabicName()), query)
                || fuzzyContains(normalizeForSearch(surah.getTransliteration()), query)
                || fuzzyContains(normalizeForSearch(surah.getEnglishMeaning()), query)
                || String.valueOf(surah.getNumber()).equals(query);
    }

    /**
     * Uthmani text uses a superscript alef in words that readers commonly type with a regular
     * alef (for example الصراط), while other words conventionally omit that visual alef
     * (for example الرحمن). Direct matching is tried first; an alef-insensitive fallback
     * makes both familiar spellings find the same original, untouched text.
     */
    private static boolean fuzzyContains(@NonNull String normalizedText,
                                         @NonNull String normalizedQuery) {
        if (normalizedText.contains(normalizedQuery)) {
            return true;
        }
        String compactQuery = withoutAlef(normalizedQuery);
        return !compactQuery.isEmpty()
                && withoutAlef(normalizedText).contains(compactQuery);
    }

    @NonNull
    private static String withoutAlef(@NonNull String value) {
        return value.replace("ا", "");
    }

    /** Public for deterministic tests and for the Quran search behavior documentation. */
    @NonNull
    public static String normalizeForSearch(@Nullable String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        String decomposed = Normalizer.normalize(value, Normalizer.Form.NFD);
        StringBuilder out = new StringBuilder(decomposed.length());
        for (int offset = 0; offset < decomposed.length();) {
            int codePoint = decomposed.codePointAt(offset);
            offset += Character.charCount(codePoint);
            int category = Character.getType(codePoint);
            if (category == Character.NON_SPACING_MARK
                    || category == Character.COMBINING_SPACING_MARK
                    || category == Character.ENCLOSING_MARK
                    || codePoint == 0x0640) { // tatweel
                continue;
            }
            if (codePoint >= 0x0660 && codePoint <= 0x0669) { // Arabic-Indic digits
                out.append((char) ('0' + codePoint - 0x0660));
                continue;
            }
            if (codePoint >= 0x06F0 && codePoint <= 0x06F9) { // Eastern Arabic-Indic digits
                out.append((char) ('0' + codePoint - 0x06F0));
                continue;
            }
            switch (codePoint) {
                case 0x0622: // آ
                case 0x0623: // أ
                case 0x0625: // إ
                case 0x0671: // ٱ
                    out.append('ا');
                    break;
                case 0x0624: // ؤ
                    out.append('و');
                    break;
                case 0x0626: // ئ
                case 0x0649: // ى
                    out.append('ي');
                    break;
                case 0x0629: // ة
                    out.append('ه');
                    break;
                default:
                    out.appendCodePoint(codePoint);
                    break;
            }
        }
        return out.toString().toLowerCase(Locale.ROOT).trim();
    }

    /** Search result payload plus the count before the reader-friendly display limit. */
    public static final class SearchResults {
        @NonNull
        private final List<QuranSearchResult> items;
        private final int totalMatches;

        private SearchResults(@NonNull List<QuranSearchResult> items, int totalMatches) {
            this.items = Collections.unmodifiableList(new ArrayList<>(items));
            this.totalMatches = totalMatches;
        }

        @NonNull
        public List<QuranSearchResult> getItems() {
            return items;
        }

        public int getTotalMatches() {
            return totalMatches;
        }

        public boolean isTruncated() {
            return totalMatches > items.size();
        }
    }
}
