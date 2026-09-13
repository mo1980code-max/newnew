package org.Allah_Clock_Live_Wallpaper.model;

import com.google.gson.annotations.SerializedName;

/**
 * One dhikr entry of {@code res/raw/athkar.json}.
 *
 * <p>The content carries its own two languages (Arabic text plus English translation and
 * transliteration) on purpose: a dhikr is content, not UI chrome, so it must not live in
 * {@code strings.xml} where the app-language switch would replace it.</p>
 */
public final class AthkarItem {

    public static final String TYPE_MORNING = "morning";
    public static final String TYPE_EVENING = "evening";

    @SerializedName("id")
    private int id;

    @SerializedName("type")
    private String type;

    @SerializedName("repeat")
    private int repeat;

    @SerializedName("arabic_text")
    private String arabicText;

    @SerializedName("english_text")
    private String englishText;

    @SerializedName("transliteration")
    private String transliteration;

    @SerializedName("reference_ar")
    private String referenceAr;

    @SerializedName("reference_en")
    private String referenceEn;

    @SerializedName("virtue_ar")
    private String virtueAr;

    @SerializedName("virtue_en")
    private String virtueEn;

    public int getId() {
        return id;
    }

    public String getType() {
        return type == null ? "" : type;
    }

    /** How many times this dhikr should be repeated. Always at least 1. */
    public int getRepeat() {
        return repeat < 1 ? 1 : repeat;
    }

    public String getArabicText() {
        return arabicText == null ? "" : arabicText;
    }

    public String getEnglishText() {
        return englishText == null ? "" : englishText;
    }

    public String getTransliteration() {
        return transliteration == null ? "" : transliteration;
    }

    /** Source line, in the language matching the app UI. Empty when the source lists none. */
    public String getReference(boolean arabicUi) {
        String value = arabicUi ? referenceAr : referenceEn;
        return value == null ? "" : value;
    }

    /**
     * The reward note printed under the dhikr ("من قالها حين يصبح ..."), in the UI language.
     * Many athkar carry none, so callers must hide the row when this returns an empty string.
     */
    public String getVirtue(boolean arabicUi) {
        String value = arabicUi ? virtueAr : virtueEn;
        return value == null ? "" : value.trim();
    }
}
