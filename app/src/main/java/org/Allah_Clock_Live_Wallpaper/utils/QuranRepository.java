package org.Allah_Clock_Live_Wallpaper.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.model.QuranJuz;
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
 * <p>The app bundles the official Uthmanic text of the fawazahmed0/quran-api repository
 * (branch 1, edition <i>ara-quranuthmanihaf</i> — the fully vowel-marked Uthmanic text after the
 * Hafs reading, sourced from the King Fahd Quran Complex) as a read-only asset,
 * {@code assets/quran.json}, together with the repository's companion metadata
 * ({@code assets/quran_info.json}), which carries the 114 surah names, the revelation type and —
 * for every single ayah — its Madani page (1..604), juz (1..30) and position on the printed page.
 * The reader therefore opens without a connection, keeps one verified text source, and never
 * changes the source text while formatting it for display.</p>
 *
 * <p>Ayah text stays identical to the asset. The display layer appends an
 * {@link AyahNumberSpan}; presentation markers never enter search or persistence.</p>
 *
 * <p>Both files are byte-identical to the upstream repository: {@code tools/verify_resources.py}
 * pins the git blob SHAs, and {@code docs/QURAN_TEXT_ATTRIBUTION.md} records the source and the
 * license terms.</p>
 */
public final class QuranRepository {

    public static final int SURAH_COUNT = 114;
    public static final int AYAH_COUNT = 6236;
    public static final int PAGE_COUNT = 604;
    public static final int JUZ_COUNT = 30;

    private static final String ASSET_TEXT = "quran.json";
    private static final String ASSET_INFO = "quran_info.json";

    private static final Pattern REFERENCE = Pattern.compile(
            "^\\s*(\\d{1,3})\\s*[:：]\\s*(\\d{1,3})\\s*$");
    private static volatile QuranRepository instance;

    private final List<QuranSurah> surahs = new ArrayList<>();
    private final Map<Integer, QuranSurah> surahsByNumber = new HashMap<>();
    private final Map<Integer, List<QuranAyah>> ayahsBySurah = new HashMap<>();
    private final List<QuranAyah> allAyahs = new ArrayList<>();
    private final Map<String, Integer> globalAyahIndexes = new HashMap<>();
    private final Map<Integer, int[]> pageRanges = new HashMap<>();
    private final List<QuranJuz> juzs = new ArrayList<>();

    private QuranRepository(@NonNull Context context) throws IOException {
        List<TextAyah> texts = readTexts(context);
        Info info = readInfo(context);
        build(texts, info);
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

    // ══════════════════════════ parsing the two bundled assets ══════════════════════════

    @NonNull
    private static List<TextAyah> readTexts(@NonNull Context context) throws IOException {
        String body = readAsset(context, ASSET_TEXT);
        TextFile file;
        try {
            file = new Gson().fromJson(body, TextFile.class);
        } catch (RuntimeException | OutOfMemoryError e) {
            // A corrupt asset must surface as the IOException the app already translates,
            // never as a raw Gson exception or an OOM crash in production.
            Log.e("CRITICAL_DEBUG", "Gson failed to parse " + ASSET_TEXT, e);
            throw new IOException("The bundled Quran text could not be parsed", e);
        }
        if (file == null || file.quran == null) {
            throw new IOException("The bundled Quran text has no verses");
        }
        return file.quran;
    }

    @NonNull
    private static Info readInfo(@NonNull Context context) throws IOException {
        String body = readAsset(context, ASSET_INFO);
        Info info;
        try {
            info = new Gson().fromJson(body, Info.class);
        } catch (RuntimeException | OutOfMemoryError e) {
            Log.e("CRITICAL_DEBUG", "Gson failed to parse " + ASSET_INFO, e);
            throw new IOException("The bundled Quran metadata could not be parsed", e);
        }
        if (info == null) {
            throw new IOException("The bundled Quran metadata is missing");
        }
        return info;
    }

    /** Reads one bundled asset completely (no API 24+ stream helpers: minSdk is 23). */
    @NonNull
    private static String readAsset(@NonNull Context context, @NonNull String name)
            throws IOException {
        try (InputStream input = context.getAssets().open(name);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(input, StandardCharsets.UTF_8), 1 << 16)) {
            StringBuilder out = new StringBuilder(1 << 20);
            char[] buffer = new char[1 << 14];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                out.append(buffer, 0, read);
            }
            return out.toString();
        }
    }

    private void build(@NonNull List<TextAyah> texts, @NonNull Info info) throws IOException {
        if (info.chapters == null || info.chapters.size() != SURAH_COUNT) {
            throw new IOException("Quran metadata must describe 114 surahs");
        }
        for (int surahIndex = 0; surahIndex < SURAH_COUNT; surahIndex++) {
            Chapter chapter = info.chapters.get(surahIndex);
            int number = surahIndex + 1;
            if (chapter.chapter != number || chapter.verses == null
                    || chapter.verses.size() != expectedAyahCount(number)
                    || chapter.verses.size() == 0) {
                throw new IOException("Invalid Quran surah metadata for surah " + number);
            }
            String revelation = chapter.revelation == null ? "" : chapter.revelation;
            if (!revelation.equalsIgnoreCase("Mecca") && !revelation.equalsIgnoreCase("Madina")) {
                throw new IOException("Invalid Quran revelation type for surah " + number);
            }
            QuranSurah surah = new QuranSurah(number, expectedAyahCount(number),
                    QuranText.displaySurahName(chapter.arabicname),
                    chapter.name == null ? "" : chapter.name,
                    chapter.englishname == null ? "" : chapter.englishname,
                    revelation.equalsIgnoreCase("Mecca"));
            surahs.add(surah);
            surahsByNumber.put(number, surah);
            List<QuranAyah> ayahs = new ArrayList<>(surah.getAyahCount());
            ayahsBySurah.put(number, ayahs);
        }

        // The text file is ordered surah by surah, and the verse number restarts at one in
        // every surah: the sequence must hold inside each surah and the surahs must be 1..114.
        int expectedVerse = 0;
        int lastChapter = 1;
        for (TextAyah row : texts) {
            if (row.chapter < 1 || row.chapter > SURAH_COUNT) {
                throw new IOException("Quran text has an invalid surah " + row.chapter);
            }
            if (row.chapter != lastChapter) {
                lastChapter = row.chapter;
                expectedVerse = 0;
            }
            expectedVerse++;
            if (row.verse != expectedVerse) {
                throw new IOException("Quran ayah sequence breaks at "
                        + row.chapter + ":" + row.verse);
            }
        }
        if (texts.size() != AYAH_COUNT) {
            throw new IOException("The Quran text must contain 6236 ayahs");
        }

        int cursor = 0;
        int sajdaAyahs = 0;
        for (int surahIndex = 0; surahIndex < SURAH_COUNT; surahIndex++) {
            Chapter chapter = info.chapters.get(surahIndex);
            List<QuranAyah> ayahs = ayahsBySurah.get(chapter.chapter);
            for (int ayahIndex = 0; ayahIndex < chapter.verses.size(); ayahIndex++) {
                Verse verseMeta = chapter.verses.get(ayahIndex);
                TextAyah text = texts.get(cursor++);
                if (text.chapter != chapter.chapter || text.verse != verseMeta.verse
                        || text.text == null || text.text.isEmpty()) {
                    throw new IOException("Quran text and metadata disagree at "
                            + chapter.chapter + ":" + verseMeta.verse);
                }
                if (verseMeta.page < 1 || verseMeta.page > PAGE_COUNT
                        || verseMeta.juz < 1 || verseMeta.juz > JUZ_COUNT
                        || verseMeta.line < 1) {
                    throw new IOException("Quran navigation is out of range at "
                            + chapter.chapter + ":" + verseMeta.verse);
                }
                QuranAyah ayah = new QuranAyah(chapter.chapter, verseMeta.verse,
                        text.text,
                        normalizeForSearch(text.text), verseMeta.page, verseMeta.juz,
                        verseMeta.line);
                ayahs.add(ayah);
                globalAyahIndexes.put(ayah.getKey(), allAyahs.size());
                allAyahs.add(ayah);
                if (verseMeta.hasSajda()) {
                    sajdaAyahs++;
                }
            }
        }
        if (cursor != texts.size()) {
            throw new IOException("Quran metadata and text cover different ayah counts");
        }
        if (info.sajdas == null || info.sajdas.count != sajdaAyahs) {
            // The inline per-verse flags and the summary sajdas block must agree, or the
            // bundled metadata is not the reviewed upstream file.
            throw new IOException("Quran metadata must count " + sajdaAyahs
                    + " sajda ayahs, found "
                    + (info.sajdas == null ? "none" : info.sajdas.count));
        }

        if (info.pages != null && info.pages.references != null) {
            for (PageRef ref : info.pages.references) {
                pageRanges.put(ref.page, new int[]{ref.start.chapter, ref.start.verse});
            }
        }
        if (info.juzs != null && info.juzs.references != null) {
            for (JuzRef ref : info.juzs.references) {
                QuranAyah first = getAyah(ref.start.chapter, ref.start.verse);
                if (first == null || first.getMushafPage() < 1) {
                    throw new IOException("Invalid Quran juz metadata for juz " + ref.juz);
                }
                juzs.add(new QuranJuz(ref.juz, first.getMushafPage(), first));
            }
        }
    }

    private void validate() throws IOException {
        if (surahs.size() != SURAH_COUNT) {
            throw new IOException("Quran index must contain 114 surahs");
        }
        if (allAyahs.size() != AYAH_COUNT) {
            throw new IOException("The Quran text must contain 6236 ayahs");
        }
        if (pageRanges.size() != PAGE_COUNT) {
            throw new IOException("Quran metadata must cover all 604 Madani pages");
        }
        if (juzs.size() != JUZ_COUNT) {
            throw new IOException("Quran metadata must contain the 30 juzs");
        }
    }

    /** The canonical ayah count of each surah, independent of the bundled files. */
    private static int expectedAyahCount(int surahNumber) {
        int[] counts = {
                7, 286, 200, 176, 120, 165, 206, 75, 129, 109,
                123, 111, 43, 52, 99, 128, 111, 110, 98, 135,
                112, 78, 118, 64, 77, 227, 93, 88, 69, 60,
                34, 30, 73, 54, 45, 83, 182, 88, 75, 85,
                54, 53, 89, 59, 37, 35, 38, 29, 18, 45,
                60, 49, 62, 55, 78, 96, 29, 22, 24, 13,
                14, 11, 11, 18, 12, 12, 30, 52, 52, 44,
                28, 28, 20, 56, 40, 31, 50, 40, 46, 42,
                29, 19, 36, 25, 22, 17, 19, 26, 30, 20,
                15, 21, 11, 8, 8, 19, 5, 8, 8, 11,
                11, 8, 3, 9, 5, 4, 7, 3, 6, 3,
                5, 4, 5, 6
        };
        if (surahNumber < 1 || surahNumber > counts.length) {
            throw new IllegalStateException("unknown surah number " + surahNumber);
        }
        return counts[surahNumber - 1];
    }

    // ══════════════════════════════════════════ access ══════════════════════════════════════════

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

    /** True when {@code pageNumber} is one of the 604 Madani pages. */
    public boolean hasPage(int pageNumber) {
        return pageRanges.containsKey(pageNumber);
    }

    @Nullable
    public QuranAyah getFirstAyahOfPage(int pageNumber) {
        int[] start = pageRanges.get(pageNumber);
        return start == null ? null : getAyah(start[0], start[1]);
    }

    /** The canonical page number (1..604) of a valid ayah, or -1 when unknown. */
    public int getPageForAyah(int surahNumber, int ayahNumber) {
        QuranAyah ayah = getAyah(surahNumber, ayahNumber);
        return ayah == null ? -1 : ayah.getMushafPage();
    }

    /** A read-only list of the ayahs printed on one Madani page. */
    @NonNull
    public List<QuranAyah> getAyahsForPage(int pageNumber) {
        QuranAyah first = getFirstAyahOfPage(pageNumber);
        if (first == null) {
            return Collections.emptyList();
        }
        List<QuranAyah> result = new ArrayList<>();
        int page = first.getMushafPage();
        for (QuranAyah ayah : allAyahs) {
            if (ayah.getMushafPage() != page) {
                continue;
            }
            result.add(ayah);
        }
        return Collections.unmodifiableList(result);
    }

    @Nullable
    public QuranJuz getJuz(int juzNumber) {
        if (juzNumber < 1 || juzNumber > juzs.size()) {
            return null;
        }
        return juzs.get(juzNumber - 1);
    }

    /** The traditional juz (1..30) containing a valid ayah, or -1 when unknown. */
    public int getJuzForAyah(int surahNumber, int ayahNumber) {
        QuranAyah ayah = getAyah(surahNumber, ayahNumber);
        return ayah == null ? -1 : ayah.getJuz();
    }

    /** The juz in which the first ayah of this page falls. */
    public int getJuzForPage(int pageNumber) {
        QuranAyah first = getFirstAyahOfPage(pageNumber);
        return first == null ? -1 : first.getJuz();
    }

    /** The juz that opens with this page (the header's juz when a juz starts mid-page). */
    @Nullable
    public QuranJuz getJuzStartingOnPage(int pageNumber) {
        for (QuranJuz juz : juzs) {
            if (juz.getPageNumber() == pageNumber) {
                return juz;
            }
        }
        return null;
    }

    // ══════════════════════════════════════════ search ══════════════════════════════════════════

    /**
     * Searches local surah names and all locally bundled ayahs. Arabic diacritics and common
     * alef/ya variants are ignored for matching only; the displayed Quran text remains exactly
     * the original Uthmanic source text.
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
     * Uthmanic text uses a superscript alef in words that readers commonly type with a regular
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

    // ══════════════════════════ Gson shapes of the two assets ══════════════════════════

    /** assets/quran.json — the edition text file. */
    private static final class TextFile {
        List<TextAyah> quran;
    }

    private static final class TextAyah {
        int chapter;
        int verse;
        String text;
    }

    /** assets/quran_info.json — the repository's companion metadata. */
    private static final class Info {
        CountOnly verses;
        List<Chapter> chapters;
        CountOnly sajdas;
        CountRef<PageRef> pages;
        CountRef<JuzRef> juzs;
    }

    private static final class CountOnly {
        int count;
    }

    private static final class CountRef<T> {
        int count;
        List<T> references;
    }

    private static final class Chapter {
        int chapter;
        String name;
        String englishname;
        String arabicname;
        String revelation;
        List<Verse> verses;
    }

    private static final class Verse {
        int verse;
        int line;
        int juz;
        int manzil;
        int page;
        int ruku;
        int maqra;

        /**
         * Polymorphic in assets/quran_info.json: {@code false} on the 6221 ayahs without a
         * prostration and an object {@code {"no":n,"recommended":b,"obligatory":b}} on the
         * 15 ayahs of prostration (the first is 7:206), so it must stay a {@link JsonElement}.
         */
        JsonElement sajda;

        /** True exactly for the ayahs the metadata flags as a prostration ayah. */
        boolean hasSajda() {
            if (sajda == null || sajda.isJsonNull()) {
                return false;
            }
            if (sajda.isJsonPrimitive()) {
                return sajda.getAsBoolean();
            }
            return sajda.isJsonObject();
        }
    }

    private static final class PageRef {
        int page;
        Ref start;
        Ref end;
    }

    private static final class JuzRef {
        int juz;
        Ref start;
        Ref end;
    }

    private static final class Ref {
        int chapter;
        int verse;
    }
}
