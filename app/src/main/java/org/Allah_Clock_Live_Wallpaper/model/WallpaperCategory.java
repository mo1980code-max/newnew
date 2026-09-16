package org.Allah_Clock_Live_Wallpaper.model;

import java.util.Collections;
import java.util.List;

/**
 * A named group of bundled wallpapers.
 *
 * <p>The title is a string resource so categories localise like everything else, and the
 * cover is the drawable shown on the category card.</p>
 */
public final class WallpaperCategory {

    private final int titleRes;
    private final int coverRes;
    private final List<WallpaperItem> items;

    public WallpaperCategory(int titleRes, int coverRes, List<WallpaperItem> items) {
        this.titleRes = titleRes;
        this.coverRes = coverRes;
        this.items = items == null
                ? Collections.<WallpaperItem>emptyList()
                : Collections.unmodifiableList(items);
    }

    /** {@code R.string.*} id of the category title. */
    public int getTitleRes() {
        return this.titleRes;
    }

    /** {@code R.drawable.*} id of the image shown on the category card. */
    public int getCoverRes() {
        return this.coverRes;
    }

    /** Never null, never modified by callers. */
    public List<WallpaperItem> getItems() {
        return this.items;
    }
}
