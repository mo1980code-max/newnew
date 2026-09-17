package org.Allah_Clock_Live_Wallpaper.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.viewUtils.QuranPageView;

/**
 * The usable text box of one Mushaf page, measured from the real item layout instead of being
 * guessed from constants.
 *
 * <p>The paginator and the page view must agree on the width a page is laid out in to the pixel:
 * if the paginator measured 900 px and the sheet then offered 880, the last line of every page
 * would be clipped. So the item is inflated once, measured against the live pager, and the text
 * box is read back out of it — change the item's padding or margins and both sides follow without
 * any arithmetic in two places.</p>
 */
public final class QuranPageMetrics {

    /** A safety margin, in dp, so a line that lands exactly on the edge is not cut in half. */
    private static final float SAFETY_DP = 2f;

    public final int textWidth;
    public final int textHeight;

    private QuranPageMetrics(int textWidth, int textHeight) {
        this.textWidth = textWidth;
        this.textHeight = textHeight;
    }

    /**
     * @param inflater  the host's layout inflater
     * @param pagerHost the host the pages are shown in; must already be laid out
     * @return the text box, or {@code null} while the host still has no size
     */
    public static QuranPageMetrics measure(@NonNull LayoutInflater inflater,
                                           @NonNull ViewGroup pagerHost, @NonNull android.content.Context context) {
        int hostWidth = pagerHost.getWidth();
        int hostHeight = pagerHost.getHeight();
        if (hostWidth <= 0 || hostHeight <= 0) {
            return null;
        }
        View item = inflater.inflate(R.layout.item_quran_screen, pagerHost, false);
        item.measure(View.MeasureSpec.makeMeasureSpec(hostWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(hostHeight, View.MeasureSpec.AT_MOST));
        QuranPageView page = item.findViewById(R.id.quranPageView);
        if (page == null) {
            return null;
        }
        int width = page.getMeasuredWidth() - page.getPaddingLeft() - page.getPaddingRight();
        int vertical = page.getPaddingTop() + page.getPaddingBottom();
        ViewGroup.LayoutParams params = page.getLayoutParams();
        if (params instanceof LinearLayout.LayoutParams) {
            vertical += ((LinearLayout.LayoutParams) params).topMargin
                    + ((LinearLayout.LayoutParams) params).bottomMargin;
        } else if (params instanceof ViewGroup.MarginLayoutParams) {
            vertical += ((ViewGroup.MarginLayoutParams) params).topMargin
                    + ((ViewGroup.MarginLayoutParams) params).bottomMargin;
        }
        int height = hostHeight - vertical - UiCompat.dp(context, SAFETY_DP);
        if (width <= 0 || height <= 0) {
            return null;
        }
        return new QuranPageMetrics(width, height);
    }
}
