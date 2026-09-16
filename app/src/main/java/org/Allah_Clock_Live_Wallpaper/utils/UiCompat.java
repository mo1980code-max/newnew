package org.Allah_Clock_Live_Wallpaper.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.Allah_Clock_Live_Wallpaper.R;

/**
 * Small helpers that replace APIs which are deprecated or ignored once the app targets
 * Android 15/16 (API 35/36): edge-to-edge windows, the immersive system-bar flags and
 * {@code ProgressDialog}.
 */
public final class UiCompat {

    private UiCompat() {
    }

    public static int dp(Context context, float value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics()));
    }

    /** Walks up any wrappers until the hosting Activity is found. */
    @Nullable
    public static Activity findActivity(@Nullable Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    /**
     * Opt into edge-to-edge and keep the whole screen clear of the status / navigation
     * bars. Used by the list screens, where the background is a flat colour, so padding
     * the content root looks exactly like the old non-edge-to-edge layout.
     *
     * <p>From targetSdk 35 onwards edge-to-edge is enforced by the platform; doing it
     * explicitly keeps API 23-34 devices looking the same.</p>
     */
    public static void applyEdgeToEdge(@NonNull Activity activity) {
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.setBackgroundDrawableResource(R.color.textColor);

        View content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }
        ViewCompat.setOnApplyWindowInsetsListener(content, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(content);

        // Light status-bar icons would disappear on the white background.
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, content);
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
    }

    /**
     * Hides both system bars and brings them back with a swipe. Replaces the removed
     * {@code View.setSystemUiVisibility()} / {@code FLAG_FULLSCREEN} combination.
     */
    public static void applyImmersive(@NonNull Activity activity) {
        Window window = activity.getWindow();
        WindowCompat.setDecorFitsSystemWindows(window, false);
        View content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, content);
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        controller.hide(WindowInsetsCompat.Type.systemBars());
    }

    /**
     * Non-blocking "please wait" dialog. Replaces the deprecated {@code ProgressDialog}.
     * The returned object is safe to dismiss from any state and is never leaked: always
     * pair it with {@link #dismissSafely(Dialog)}.
     */
    @Nullable
    public static Dialog showLoading(@NonNull Activity activity, @NonNull String message) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return null;
        }
        Context context = activity;

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        int pad = dp(context, 20);
        root.setPadding(pad, pad, pad, pad);

        ProgressBar progress = new ProgressBar(context);
        root.addView(progress, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView text = new TextView(context);
        text.setText(message);
        text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        text.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textParams.leftMargin = dp(context, 16);
        textParams.setMarginStart(dp(context, 16));
        root.addView(text, textParams);

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(root);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        return dialog;
    }

    public static void dismissSafely(@Nullable Dialog dialog) {
        if (dialog == null) {
            return;
        }
        try {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (IllegalArgumentException ignored) {
            // window already gone
        }
    }
}
