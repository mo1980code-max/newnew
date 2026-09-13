package org.Allah_Clock_Live_Wallpaper.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** Root of {@code res/raw/athkar.json}. */
public final class AthkarFile {

    @SerializedName("athkar")
    private List<AthkarItem> athkar;

    public List<AthkarItem> getAthkar() {
        return athkar == null ? new ArrayList<AthkarItem>() : athkar;
    }
}
