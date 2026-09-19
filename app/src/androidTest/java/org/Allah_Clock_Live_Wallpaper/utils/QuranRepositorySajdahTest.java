package org.Allah_Clock_Live_Wallpaper.utils;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.Allah_Clock_Live_Wallpaper.model.QuranAyah;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * The fifteen ayahs of prostration, read back out of the bundled metadata the way the reader
 * reads them.
 *
 * <p>Run on an Android device/emulator: {@code ./gradlew connectedDebugAndroidTest}. The
 * repository parses both bundled assets, so this is the test that proves the mihrab the reader
 * draws stands on the upstream data and not on a list kept in the app.</p>
 */
@RunWith(AndroidJUnit4.class)
public class QuranRepositorySajdahTest {

    private QuranRepository repository() throws IOException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        return QuranRepository.get(context);
    }

    @Test
    public void loadsTheFifteenProstrationsInMushafOrder() throws IOException {
        List<QuranAyah> sajdahs = repository().getSajdahAyahs();
        assertEquals(QuranRepository.SAJDAH_COUNT, sajdahs.size());
        assertEquals(7, sajdahs.get(0).getSurahNumber());
        assertEquals(206, sajdahs.get(0).getAyahNumber());
        assertEquals(96, sajdahs.get(14).getSurahNumber());
        assertEquals(19, sajdahs.get(14).getAyahNumber());
        for (int index = 0; index < sajdahs.size(); index++) {
            QuranAyah ayah = sajdahs.get(index);
            assertEquals("the metadata numbers them 1..15", index + 1, ayah.getSajdahNumber());
            assertTrue(ayah.isSajdahAyah());
            assertTrue(ayah.getMushafPage() >= 1);
            assertTrue(ayah.getMushafPage() <= QuranRepository.PAGE_COUNT);
        }
    }

    @Test
    public void flagsFourObligatoryAndElevenRecommended() throws IOException {
        int obligatory = 0;
        for (QuranAyah ayah : repository().getSajdahAyahs()) {
            if (ayah.isSajdahObligatory()) {
                obligatory++;
            }
        }
        assertEquals(4, obligatory);
    }

    @Test
    public void namesAtMostOneProstrationPerPageAndNoneForTheRest() throws IOException {
        QuranRepository loaded = repository();
        Set<Integer> pages = new HashSet<>();
        for (QuranAyah ayah : loaded.getSajdahAyahs()) {
            QuranAyah onPage = loaded.getSajdahOnPage(ayah.getMushafPage());
            assertNotNull(onPage);
            assertEquals(ayah.getSurahNumber(), onPage.getSurahNumber());
            assertEquals(ayah.getAyahNumber(), onPage.getAyahNumber());
            assertTrue("two prostrations share page " + ayah.getMushafPage(),
                    pages.add(ayah.getMushafPage()));
        }
        // Al-Fatiha's single page and the last page of the Mushaf carry none.
        assertNull(loaded.getSajdahOnPage(1));
        assertNull(loaded.getSajdahOnPage(QuranRepository.PAGE_COUNT));
        // And an ayah that is not one of the fifteen says so on the model itself.
        QuranAyah first = loaded.getAyah(1, 1);
        assertNotNull(first);
        assertFalse(first.isSajdahAyah());
        assertEquals(QuranAyah.NO_SAJDAH, first.getSajdahNumber());
    }
}
