package org.Allah_Clock_Live_Wallpaper.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.WallpaperCategory;
import org.Allah_Clock_Live_Wallpaper.model.WallpaperItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The single source of truth for every wallpaper the app offers.
 *
 * <p><b>Every image ships inside the APK</b> ({@code res/drawable-nodpi}). There is
 * deliberately no remote catalogue: no third-party image host can disappear overnight,
 * serve unexpected content or raise a copyright claim, and the whole wallpaper section
 * keeps working with the network switched off.</p>
 *
 * <p>To add a wallpaper: drop a jpg into {@code res/drawable-nodpi} and add its
 * {@code R.drawable} id to the matching array below. To add a category: add a title in
 * {@code strings.xml} and one {@code category(...)} line in {@link #build()}.</p>
 */
public final class WallpaperCatalog {

    private WallpaperCatalog() {
    }

    /** Makkah — the Kaaba and Masjid al-Haram. */
    private static final int[] KAABA = {
            R.drawable.wp_kaaba_1, R.drawable.wp_kaaba_2, R.drawable.wp_kaaba_3,
            R.drawable.wp_kaaba_4, R.drawable.wp_kaaba_5,
    };

    /** Madinah — the Prophet's Mosque. */
    private static final int[] MADINA = {
            R.drawable.wp_madina_1, R.drawable.wp_madina_2, R.drawable.wp_madina_3,
            R.drawable.wp_madina_4, R.drawable.wp_madina_5,
    };

    /** Jerusalem — Al-Aqsa and the Dome of the Rock. */
    private static final int[] AQSA = {
            R.drawable.wp_aqsa_1, R.drawable.wp_aqsa_2, R.drawable.wp_aqsa_3,
            R.drawable.wp_aqsa_4, R.drawable.wp_aqsa_5,
    };

    /** Mosques, minarets and lantern-lit courtyards. */
    private static final int[] MOSQUES = {
            R.drawable.wp_mosque_1, R.drawable.wp_mosque_2, R.drawable.wp_mosque_3,
            R.drawable.wp_mosque_4, R.drawable.wp_mosque_5,
    };

    /** Gradient / abstract backgrounds that shipped with the first releases. */
    private static final int[] CLASSIC = {
            R.drawable.bg2, R.drawable.wp_min_1, R.drawable.bg, R.drawable.bg1,
            R.drawable.bg3, R.drawable.bg4, R.drawable.bg5,
    };

    private static final List<WallpaperCategory> CATEGORIES =
            Collections.unmodifiableList(build());

    private static List<WallpaperCategory> build() {
        List<WallpaperCategory> list = new ArrayList<>();
        list.add(category(R.string.cat_kaaba, R.drawable.wp_kaaba_1, KAABA));
        list.add(category(R.string.cat_madina, R.drawable.wp_madina_1, MADINA));
        list.add(category(R.string.cat_aqsa, R.drawable.wp_aqsa_1, AQSA));
        list.add(category(R.string.cat_mosques, R.drawable.wp_mosque_1, MOSQUES));
        list.add(category(R.string.cat_classic, R.drawable.bg2, CLASSIC));
        return list;
    }

    private static WallpaperCategory category(int titleRes, int coverRes, int[] items) {
        List<WallpaperItem> wallpapers = new ArrayList<>(items.length);
        for (int res : items) {
            wallpapers.add(new WallpaperItem(res));
        }
        return new WallpaperCategory(titleRes, coverRes, wallpapers);
    }

    /** All categories, in display order. Never null, never empty. */
    @NonNull
    public static List<WallpaperCategory> getCategories() {
        return CATEGORIES;
    }

    /** @return the category at {@code index}, or {@code null} when out of range. */
    @Nullable
    public static WallpaperCategory getCategory(int index) {
        if (index < 0 || index >= CATEGORIES.size()) {
            return null;
        }
        return CATEGORIES.get(index);
    }
}
