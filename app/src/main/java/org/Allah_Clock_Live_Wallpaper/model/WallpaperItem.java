package org.Allah_Clock_Live_Wallpaper.model;

/**
 * One wallpaper offered by the app.
 *
 * <p>A wallpaper is simply a drawable resource that ships inside the APK — there is no
 * remote image host any more, so nothing can disappear, be replaced by someone else's
 * content or raise a copyright claim.</p>
 */
public final class WallpaperItem {

    private final int drawableRes;

    public WallpaperItem(int drawableRes) {
        this.drawableRes = drawableRes;
    }

    /** {@code R.drawable.*} id of the bundled image. Never 0. */
    public int getDrawableRes() {
        return this.drawableRes;
    }
}
