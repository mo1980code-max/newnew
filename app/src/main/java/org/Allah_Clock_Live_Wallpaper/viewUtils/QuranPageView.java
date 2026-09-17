package org.Allah_Clock_Live_Wallpaper.viewUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.Allah_Clock_Live_Wallpaper.utils.QuranPageBuilder;
import org.Allah_Clock_Live_Wallpaper.utils.QuranPaginator;
import org.Allah_Clock_Live_Wallpaper.utils.QuranTheme;

import java.util.Collections;
import java.util.List;

/**
 * Draws one page of the Mushaf: a slice of the built text, laid out by {@link QuranPaginator}
 * with exactly the parameters that were measured, then painted on the canvas.
 *
 * <p>Drawing the layout itself (instead of handing the text to a {@link android.widget.TextView})
 * is what guarantees that what the paginator measured is what the reader sees — a TextView would
 * lay the slice out again with its own padding and could push the last line off the page.</p>
 *
 * <p>Touch handling is deliberately minimal: a press that does not travel is a tap on the verse
 * underneath (the host toggles its bookmark) and every other gesture is left to the parent, so
 * the horizontal page turn of the enclosing {@code ViewPager2} keeps working.</p>
 */
public final class QuranPageView extends View {

    /** A tap on a verse, reported with its number so the host can save or clear it. */
    public interface Listener {
        void onVerseTapped(int ayahNumber);
    }

    @Nullable
    private StaticLayout layout;
    @Nullable
    private TextPaint paint;
    @Nullable
    private CharSequence text;
    @Nullable
    private List<QuranPageBuilder.Verse> verses = Collections.emptyList();
    @Nullable
    private Listener listener;
    private int startOffset;
    private int endOffset;
    private float lineSpacingExtra;
    private int measuredWidth;

    private float downX;
    private float downY;
    private int touchSlop;

    public QuranPageView(Context context) {
        this(context, null);
    }

    public QuranPageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * Installs one page slice. The text is shared between pages; only the slice offsets differ,
     * so a bookmark tint applied later to the shared text shows up on every page at once.
     */
    public void bind(@NonNull CharSequence text, int start, int end,
                     @NonNull List<QuranPageBuilder.Verse> verses, @NonNull TextPaint paint,
                     @NonNull QuranTheme theme, float lineSpacingExtra,
                     @Nullable Listener listener) {
        applySheet(theme);
        this.text = text;
        this.startOffset = Math.max(0, start);
        this.endOffset = Math.min(text.length(), Math.max(this.startOffset, end));
        this.verses = verses;
        this.paint = paint;
        this.lineSpacingExtra = lineSpacingExtra;
        this.listener = listener;
        this.measuredWidth = 0;
        this.layout = null;
        requestLayout();
        invalidate();
    }

    /** Re-paints after a theme or bookmark change; the layout is reused, so nothing re-flows. */
    public void refresh() {
        invalidate();
    }

    /**
     * The page sheet: a gold hairline frame with a second, inset hairline, in the palette that is
     * active right now. Built in code rather than shipped as a drawable because the reader's
     * night theme is an in-app switch, so the same view has to be able to change colour without
     * the screen being recreated.
     */
    public void applySheet(@NonNull QuranTheme theme) {
        GradientDrawable frame = new GradientDrawable();
        frame.setShape(GradientDrawable.RECTANGLE);
        frame.setColor(theme.surface);
        frame.setCornerRadius(dp(18f));
        frame.setStroke(dp(1f), theme.gold);

        GradientDrawable inner = new GradientDrawable();
        inner.setShape(GradientDrawable.RECTANGLE);
        inner.setColor(Color.TRANSPARENT);
        inner.setCornerRadius(dp(14f));
        inner.setStroke(dp(1f), theme.line);

        LayerDrawable sheet = new LayerDrawable(new Drawable[]{
                frame,
                new InsetDrawable(inner, dp(4f), dp(4f), dp(4f), dp(4f)),
        });
        setBackground(sheet);
    }

    private int dp(float value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics()));
    }

    public boolean hasContent() {
        return this.text != null && this.endOffset > this.startOffset;
    }

    /** The natural height of this page's text, so a host can tell whether it needs scrolling. */
    public int contentHeight() {
        return this.layout == null ? 0 : this.layout.getHeight();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int available = Math.max(1, width - getPaddingLeft() - getPaddingRight());
        if (this.layout == null || this.measuredWidth != available) {
            this.measuredWidth = available;
            this.layout = buildLayout(available);
        }
        int height = this.layout == null ? 0 : this.layout.getHeight();
        int desired = height + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width,
                resolveSize(desired, heightMeasureSpec));
    }

    @Nullable
    private StaticLayout buildLayout(int width) {
        if (this.text == null || this.paint == null || this.endOffset <= this.startOffset) {
            return null;
        }
        return QuranPaginator.build(this.text, this.startOffset, this.endOffset, this.paint,
                width, this.lineSpacingExtra);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (this.layout == null) {
            return;
        }
        canvas.save();
        canvas.translate(getPaddingLeft(), getPaddingTop());
        this.layout.draw(canvas);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                this.downX = event.getX();
                this.downY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                if (Math.abs(event.getX() - this.downX) <= this.touchSlop
                        && Math.abs(event.getY() - this.downY) <= this.touchSlop) {
                    onTap(event.getX(), event.getY());
                }
                return true;
            default:
                // Let the pager own every drag: a swipe must never be swallowed here.
                return super.onTouchEvent(event);
        }
    }

    /** Maps a tap to the verse under the finger and hands it to the host. */
    private void onTap(float x, float y) {
        if (this.layout == null || this.verses == null || this.listener == null) {
            return;
        }
        float localX = x - getPaddingLeft();
        float localY = y - getPaddingTop();
        if (localY < 0 || localY > this.layout.getHeight()) {
            return;
        }
        int line = this.layout.getLineForVertical((int) localY);
        if (localX < this.layout.getLineLeft(line) || localX > this.layout.getLineRight(line)) {
            return; // the finger landed on the page margin, not on a verse
        }
        int offset = this.startOffset + this.layout.getOffsetForHorizontal(line, localX);
        for (QuranPageBuilder.Verse verse : this.verses) {
            if (verse.contains(offset)) {
                this.listener.onVerseTapped(verse.ayahNumber);
                return;
            }
        }
    }
}
