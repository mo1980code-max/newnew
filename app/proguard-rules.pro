# ProGuard / R8 rules for org.Allah_Clock_Live_Wallpaper
#
# The release build runs R8 against the *optimize* platform file
# (proguard-android-optimize.txt, see app/build.gradle), so this file has to keep exactly what
# reflection and the platform reach for - and nothing else:
#
#   * Gson reads and writes the model classes by field name (TinyDB, AthkarRepository);
#   * classes named from AndroidManifest.xml and from layout XML are kept explicitly, even
#     though the platform rules already cover Activities/Views: being explicit costs nothing and
#     survives a change of default file;
#   * every dependency that ships consumer rules keeps them (Ads, UMP, Glide, AndroidX), and the
#     one library that does not (the colour picker) is kept below.
#
# History: up to v1.1 this file ended with a blanket
#     -keep class * { public private *; }
# which kept every class of every dependency and made minifyEnabled / shrinkResources almost a
# no-op (that is the long-standing release-build finding). UPGRADE_NOTES section 22 removed it.
# To go back to the old behaviour in a hurry, uncomment that single line at the bottom - it is
# the only rule that is deliberately absent.

-keepattributes Signature
-keepattributes InnerClasses,EnclosingMethod
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Gson (TinyDB persists the selected Clocks model; AthkarRepository parses athkar.json) ──
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
-keep class org.Allah_Clock_Live_Wallpaper.service.** { *; }
-keep class org.Allah_Clock_Live_Wallpaper.widget.** { *; }
-keep class org.Allah_Clock_Live_Wallpaper.viewUtils.** { *; }

# ── Google Mobile Ads + UMP ship their own consumer rules ─────────────────
-dontwarn com.google.android.gms.ads.**
-dontwarn com.google.android.ump.**

# ── Glide ships its own consumer rules ────────────────────────────────────
-dontwarn com.bumptech.glide.**

# ── QuadFlask colour picker (used by EditorActivity) ─────────────────────
-keep class com.flask.colorpicker.** { *; }
-dontwarn com.flask.colorpicker.**

# Failure-proofing for the one thing R8 cannot know: a closed-source dependency referencing an
# optional class that is not on the classpath reports it as a "missing class" error. Without
# this line the release build would stop on a dependency's optional code path; with it, the
# build finishes and the R8 report in build/outputs/mapping/release/ lists what was skipped.
# Remove it once that report has been reviewed - `python3 tools/verify_java_symbols.py` is the
# in-repo guard for the errors that matter (the app's own symbols).
-ignorewarnings

# Emergency rollback (all of the targeted rules above are still applied):
#   -keep class * { public private *; }
