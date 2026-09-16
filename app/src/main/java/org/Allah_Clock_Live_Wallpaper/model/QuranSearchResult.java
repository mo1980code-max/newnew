package org.Allah_Clock_Live_Wallpaper.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** A hit in either the Quran surah index or the locally bundled verse text. */
public final class QuranSearchResult {

    public static final int TYPE_SURAH = 1;
    public static final int TYPE_AYAH = 2;

    private final int type;
    @NonNull
    private final QuranSurah surah;
    @Nullable
    private final QuranAyah ayah;

    private QuranSearchResult(int type, @NonNull QuranSurah surah, @Nullable QuranAyah ayah) {
        this.type = type;
        this.surah = surah;
        this.ayah = ayah;
    }

    @NonNull
    public static QuranSearchResult forSurah(@NonNull QuranSurah surah) {
        return new QuranSearchResult(TYPE_SURAH, surah, null);
    }

    @NonNull
    public static QuranSearchResult forAyah(@NonNull QuranSurah surah, @NonNull QuranAyah ayah) {
        return new QuranSearchResult(TYPE_AYAH, surah, ayah);
    }

    public int getType() {
        return type;
    }

    @NonNull
    public QuranSurah getSurah() {
        return surah;
    }

    @Nullable
    public QuranAyah getAyah() {
        return ayah;
    }
}
