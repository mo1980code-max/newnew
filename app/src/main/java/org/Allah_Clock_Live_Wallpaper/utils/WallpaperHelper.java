package org.Allah_Clock_Live_Wallpaper.utils;

import android.app.WallpaperManager;
import android.content.Context;
import android.net.Uri;
import android.service.wallpaper.WallpaperInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Wallpaper related helpers that do not depend on any Android version specific storage
 * permission.
 */
public final class WallpaperHelper {

    private WallpaperHelper() {
    }

    /**
     * @return true when one of <i>this</i> app's live wallpaper services is the wallpaper
     * that is currently set on the device. Used to decide whether an "apply wallpaper"
     * action really succeeded before an interstitial is allowed to be shown.
     */
    public static boolean isOurLiveWallpaperSet(Context context) {
        try {
            WallpaperManager manager = WallpaperManager.getInstance(context.getApplicationContext());
            WallpaperInfo info = manager.getWallpaperInfo();
            return info != null
                    && info.getService() != null
                    && info.getService().getPackageName() != null
                    && context.getPackageName().equals(info.getService().getPackageName());
        } catch (Throwable ignored) {
            return false;
        }
    }

    /**
     * Copies a {@code content://} uri (gallery picker result) into app-private storage.
     *
     * <p>Storing the picked image this way removes two problems of the previous
     * implementation on Android 10+: the real file path of a MediaStore/SAF uri is not
     * readable any more under scoped storage, and the temporary uri permission granted to
     * the picking Activity is not visible to a WallpaperService.</p>
     *
     * @return true when the copy succeeded
     */
    public static boolean copyUriToFile(Context context, Uri uri, File destination) {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = context.getContentResolver().openInputStream(uri);
            if (input == null) {
                return false;
            }
            File parent = destination.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                return false;
            }
            output = new FileOutputStream(destination);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            return destination.length() > 0;
        } catch (Throwable ignored) {
            return false;
        } finally {
            closeQuietly(output);
            closeQuietly(input);
        }
    }

    private static void closeQuietly(java.io.Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Throwable ignored) {
        }
    }
}
