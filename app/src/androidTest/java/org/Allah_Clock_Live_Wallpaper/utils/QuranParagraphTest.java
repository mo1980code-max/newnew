package org.Allah_Clock_Live_Wallpaper.utils;

import android.content.Context;
import android.os.Build;
import android.text.Layout;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.adapter.QuranFlowAdapter;
import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.Allah_Clock_Live_Wallpaper.model.QuranSurah;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class QuranParagraphTest {
    private Context context() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void fatihaIsOneParagraphWithSevenInlineMarkersAndExactVerseOffsets() throws Exception {
        QuranRepository repository = QuranRepository.get(context());
        List<QuranAyah> ayahs = repository.getAyahs(1);
        QuranParagraph paragraph = new QuranParagraph(ayahs, 1f, AyahNumberSpan.PRIMARY_GREEN);
        assertFalse(paragraph.text.toString().contains("\n"));
        assertEquals(7, paragraph.text.getSpans(0, paragraph.text.length(), AyahNumberSpan.class).length);
        for (int i = 0; i < ayahs.size(); i++) {
            QuranAyah ayah = ayahs.get(i);
            int start = paragraph.start(i);
            int end = paragraph.end(i);
            assertEquals(ayah.getText() + "\u00A0\uFFFC",
                    paragraph.text.subSequence(start, end).toString());
            assertSame(ayah, paragraph.ayahAtOffset(start));
            assertSame(ayah, paragraph.ayahAtOffset(end - 1));
            assertEquals(start, paragraph.offsetOf(1, ayah.getAyahNumber()));
            if (i > 0) {
                assertEquals(paragraph.end(i - 1) + 1, start);
                assertEquals(' ', paragraph.text.charAt(start - 1));
            }
        }
        List<QuranFlowAdapter.Row> rows = new ArrayList<>();
        QuranFlowAdapter.appendSurahRows(rows, repository.getSurah(1), ayahs, false);
        assertEquals(2, rows.size()); // Heading + ONE TextView for all seven ayahs.
        assertEquals(QuranFlowAdapter.TYPE_SURAH, rows.get(0).type);
        assertEquals(7, rows.get(1).ayahs.size());
    }

    @Test
    public void fullMushafGroupsOnlyAtPageAndSurahBoundariesWithoutLosingAnyVerse() throws Exception {
        QuranRepository repository = QuranRepository.get(context());
        List<QuranFlowAdapter.Row> rows = new ArrayList<>();
        for (QuranSurah surah : repository.getSurahs()) {
            QuranFlowAdapter.appendSurahRows(rows, surah, repository.getAyahs(surah.getNumber()), true);
        }
        Set<String> keys = new HashSet<>();
        int pages = 0;
        int headings = 0;
        QuranAyah previous = null;
        for (QuranFlowAdapter.Row row : rows) {
            if (row.type == QuranFlowAdapter.TYPE_PAGE) {
                assertEquals(++pages, row.page);
            } else if (row.type == QuranFlowAdapter.TYPE_SURAH) {
                headings++;
            } else {
                assertFalse(row.ayahs.isEmpty());
                QuranAyah first = row.ayahs.get(0);
                if (previous != null) {
                    assertTrue(first.getMushafPage() != previous.getMushafPage()
                            || first.getSurahNumber() != previous.getSurahNumber());
                }
                for (QuranAyah ayah : row.ayahs) {
                    assertEquals(first.getSurahNumber(), ayah.getSurahNumber());
                    assertEquals(row.page, ayah.getMushafPage());
                    assertTrue(keys.add(ayah.getKey()));
                    previous = ayah;
                }
            }
        }
        assertEquals(604, pages);
        assertEquals(114, headings);
        assertEquals(6236, keys.size());
    }

    @Test
    public void wrappedMarkersStayWithLastWordOnItsLeft() {
        String verse = "قُلْ هُوَ ٱللَّهُ أَحَدٌ";
        List<QuranAyah> ayahs = Arrays.asList(
                new QuranAyah(112, 1, verse, "", 604, 30, 1),
                new QuranAyah(112, 2, verse, "", 604, 30, 2));
        for (float textSize : new float[]{24f, 40f}) {
            for (int width : new int[]{220, 320, 600}) {
                QuranParagraph paragraph = new QuranParagraph(ayahs, 1f, AyahNumberSpan.PRIMARY_GREEN);
                TextPaint paint = new TextPaint();
                paint.setTextSize(textSize);
                StaticLayout.Builder builder = StaticLayout.Builder.obtain(paragraph.text, 0,
                                paragraph.text.length(), paint, width)
                        .setTextDirection(TextDirectionHeuristics.RTL)
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                        .setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE)
                        .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    builder.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
                }
                StaticLayout layout = builder.build();
                for (int i = 0; i < ayahs.size(); i++) {
                    int marker = paragraph.end(i) - 1;
                    int lastWord = paragraph.start(i) + verse.lastIndexOf(' ') + 1;
                    assertEquals(layout.getLineForOffset(lastWord), layout.getLineForOffset(marker));
                    assertEquals(-1, layout.getParagraphDirection(layout.getLineForOffset(marker)));
                    assertTrue(layout.getPrimaryHorizontal(marker) < layout.getPrimaryHorizontal(lastWord));
                }
                // A wide paragraph must allow the next ayah to share its predecessor's line.
                if (width == 600 && textSize == 24f) {
                    assertEquals(layout.getLineForOffset(paragraph.end(0) - 1),
                            layout.getLineForOffset(paragraph.start(1)));
                }
            }
        }
    }

    @Test
    public void readerBindsRtlParagraphAndKeepsIndividualVerseClicks() throws Exception {
        QuranRepository repository = QuranRepository.get(context());
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            List<QuranFlowAdapter.Row> rows = new ArrayList<>();
            QuranFlowAdapter.appendSurahRows(rows, repository.getSurah(1), repository.getAyahs(1), false);
            QuranAyah[] tapped = new QuranAyah[1];
            QuranFlowAdapter adapter = new QuranFlowAdapter(rows, new QuranTheme(context(), false),
                    new QuranStore(context()), 24f, ayah -> tapped[0] = ayah);
            RecyclerView.ViewHolder holder = adapter.onCreateViewHolder(new FrameLayout(context()),
                    QuranFlowAdapter.TYPE_PARAGRAPH);
            adapter.onBindViewHolder(holder, 1);
            TextView text = holder.itemView.findViewById(R.id.quranAyahText);
            assertEquals(View.LAYOUT_DIRECTION_RTL, text.getLayoutDirection());
            assertEquals(View.TEXT_DIRECTION_RTL, text.getTextDirection());
            assertEquals(View.TEXT_ALIGNMENT_VIEW_START, text.getTextAlignment());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                assertEquals(Layout.JUSTIFICATION_MODE_INTER_WORD, text.getJustificationMode());
            }
            Spanned rendered = (Spanned) text.getText();
            ClickableSpan[] links = rendered.getSpans(0, rendered.length(), ClickableSpan.class);
            assertEquals(7, links.length);
            links[3].onClick(text);
            assertEquals(4, tapped[0].getAyahNumber());
            assertEquals(1, adapter.rowForAyah(1, 7));
        });
    }
}
