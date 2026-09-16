package org.Allah_Clock_Live_Wallpaper.utils;

/**
 * Live-wallpaper frame rate policy ("FPS limiter").
 *
 * <p>Two modes, chosen by the user in the editor options dialog:</p>
 * <ul>
 *   <li><b>Power saver (default)</b> — the analog second hand ticks once per second and the
 *       whole wallpaper is redrawn at 1 Hz. This is exactly what shipped before the option
 *       existed, so existing users see zero change.</li>
 *   <li><b>Smooth</b> — the second hand sweeps continuously at ~30 fps. Much prettier, and
 *       roughly thirty times the redraw work, which is why it is opt-in.</li>
 * </ul>
 *
 * <p>Digital and smart clocks never need more than 1 Hz: only the minute changes, so the
 * smooth mode deliberately does not affect them.</p>
 */
public final class FrameRate {

    /** ~30 fps: the ceiling for a battery-friendly continuous sweep. */
    public static final int SMOOTH_FRAME_MS = 33;

    /** 1 fps: one redraw per second, the power-saver rate. */
    public static final int TICK_FRAME_MS = 1000;

    private FrameRate() {
    }

    /** {@code true} = ticking second hand (default), {@code false} = continuous sweep. */
    public static boolean isPowerSaver(TinyDB tinyDB) {
        return tinyDB.getBoolean("powerSaver", true);
    }

    /** Delay for the live wallpaper's redraw loop, given the active clock type. */
    public static int liveClockDelayMs(TinyDB tinyDB, int clockType) {
        if (clockType == 0 && !isPowerSaver(tinyDB)) {
            return SMOOTH_FRAME_MS;
        }
        return TICK_FRAME_MS;
    }
}
