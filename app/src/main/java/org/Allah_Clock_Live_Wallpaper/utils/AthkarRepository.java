package org.Allah_Clock_Live_Wallpaper.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.Allah_Clock_Live_Wallpaper.R;
import org.Allah_Clock_Live_Wallpaper.model.AthkarFile;
import org.Allah_Clock_Live_Wallpaper.model.AthkarItem;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Loads {@code res/raw/athkar.json} once per process and serves filtered slices of it.
 *
 * <p>Raw resource + Gson (already a dependency for TinyDB) instead of Room: the content is
 * read-only, ships in the APK and totals a few dozen rows, so a database would add weight
 * and migration surface for nothing.</p>
 */
public final class AthkarRepository {

    private static final String TAG = "AthkarRepository";

    private static List<AthkarItem> cache;

    private AthkarRepository() {
    }

    @NonNull
    public static synchronized List<AthkarItem> all(@NonNull Context context) {
        if (cache != null) {
            return cache;
        }
        List<AthkarItem> loaded = read(context);
        cache = Collections.unmodifiableList(loaded);
        return cache;
    }

    @NonNull
    public static List<AthkarItem> forWindow(@NonNull Context context, int window) {
        String type = window == PrayerWindow.MORNING
                ? AthkarItem.TYPE_MORNING
                : AthkarItem.TYPE_EVENING;
        List<AthkarItem> result = new ArrayList<>();
        for (AthkarItem item : all(context)) {
            if (type.equals(item.getType())) {
                result.add(item);
            }
        }
        return result;
    }

    @NonNull
    private static List<AthkarItem> read(Context context) {
        InputStream input = null;
        try {
            input = context.getResources().openRawResource(R.raw.athkar);
            ByteArrayOutputStream out = new ByteArrayOutputStream(65536);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            AthkarFile file = new Gson().fromJson(
                    new String(out.toByteArray(), StandardCharsets.UTF_8), AthkarFile.class);
            return file == null ? new ArrayList<AthkarItem>() : file.getAthkar();
        } catch (Throwable t) {
            Log.e(TAG, "could not read athkar.json", t);
            return new ArrayList<>();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Throwable ignored) {
                }
            }
        }
    }
}
