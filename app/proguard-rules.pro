# ProGuard / R8 rules for org.Allah_Clock_Live_Wallpaper
#
# Note on the blanket rule at the bottom: the app shipped with "keep everything" and that
# is kept on purpose so the R8 upgrade cannot change runtime behaviour. Once you have run
# a full regression pass you can replace it with the targeted rules listed in the comment
# to shrink the APK considerably.

-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Gson (wallpapernew.json -> model classes) ──────────────────────────────
-keep class org.Allah_Clock_Live_Wallpaper.model.** { *; }
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn sun.misc.**

# ── Referenced by name from AndroidManifest.xml / layout XML ───────────────
-keep class org.Allah_Clock_Live_Wallpaper.AppClass { *; }
-keep class org.Allah_Clock_Live_Wallpaper.LiveClockWallpaper { *; }
-keep class org.Allah_Clock_Live_Wallpaper.CustomWallpaper { *; }
-keep class org.Allah_Clock_Live_Wallpaper.activity.** { *; }
-keep class org.Allah_Clock_Live_Wallpaper.viewUtils.** { *; }

# ── Google Mobile Ads + UMP ship their own consumer rules ─────────────────
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.android.ump.**

# ── OkDownload / OkHttp / Okio ────────────────────────────────────────────
-keep class com.liulishuo.okdownload.** { *; }
-dontwarn com.liulishuo.okdownload.**
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn org.slf4j.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ── QuadFlask colour picker ───────────────────────────────────────────────
-keep class com.flask.colorpicker.** { *; }
-dontwarn com.flask.colorpicker.**

# ── Glide ships its own consumer rules ────────────────────────────────────
-dontwarn com.bumptech.glide.**

-ignorewarnings

# Safety net carried over from the previous release. Replace with the targeted
# rules above (they are already present) to let R8 actually shrink the code:
#   -keep class * { public private *; }
-keep class * {
    public private *;
}
