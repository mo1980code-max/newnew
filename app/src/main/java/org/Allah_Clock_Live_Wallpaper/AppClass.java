package org.Allah_Clock_Live_Wallpaper;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import org.Allah_Clock_Live_Wallpaper.activity.MainActivity;
import org.Allah_Clock_Live_Wallpaper.ads.AppOpenAdController;
import org.Allah_Clock_Live_Wallpaper.ads.PremiumManager;

/**
 * Process-wide entry point.
 *
 * <p>The ads SDK is deliberately <b>not</b> initialised here: the UMP consent flow has to
 * run first and that needs an Activity, so it happens in {@code MainActivity}.</p>
 *
 * <p>This class also tracks foreground/background transitions with plain
 * {@link ActivityLifecycleCallbacks} (no extra dependency) so app-open ads fire only when
 * the user genuinely returns to the app, and only on the home screen.</p>
 */
public class AppClass extends Application {

    private static int startedActivities;
    private static boolean returnedToForeground;

    /** Consumes the "user just brought the app back" flag. Safe from any thread. */
    public static boolean consumeReturnToForeground() {
        boolean value = returnedToForeground;
        returnedToForeground = false;
        return value;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Application.onCreate runs exactly once per process, which is what makes the
        // rewarded premium unlock a "session" unlock.
        PremiumManager.resetForNewSession();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityStarted(Activity activity) {
                if (startedActivities == 0) {
                    returnedToForeground = true;
                }
                startedActivities++;
            }

            @Override
            public void onActivityStopped(Activity activity) {
                startedActivities = Math.max(0, startedActivities - 1);
            }

            @Override
            public void onActivityResumed(Activity activity) {
                // Consume on every resume so a return to a sub-screen never banks the
                // flag for a later internal navigation; only the home screen shows it.
                if (consumeReturnToForeground() && activity instanceof MainActivity) {
                    AppOpenAdController.tryShow(activity);
                }
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityPaused(Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
            }
        });
    }
}
