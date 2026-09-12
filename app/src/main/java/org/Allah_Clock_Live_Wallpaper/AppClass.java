package org.Allah_Clock_Live_Wallpaper;

import android.app.Application;

import org.Allah_Clock_Live_Wallpaper.ads.PremiumManager;

/**
 * Process-wide entry point.
 *
 * <p>The ads SDK is deliberately <b>not</b> initialised here: the UMP consent flow has to
 * run first and that needs an Activity, so it happens in {@code MainActivity}.</p>
 */
public class AppClass extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Application.onCreate runs exactly once per process, which is what makes the
        // rewarded premium unlock a "session" unlock.
        PremiumManager.resetForNewSession();
    }
}
