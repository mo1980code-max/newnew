package com.clock.livewallpaper.utils;

import com.clock.livewallpaper.R;
import com.clock.livewallpaper.model.Clocks;
import com.clock.livewallpaper.model.SmartClocks;
import com.clock.livewallpaper.model.TextClocks;
import java.util.ArrayList;



public class GetClocks {
    public ArrayList<Clocks> getClocks() {
        ArrayList<Clocks> arrayList = new ArrayList<>();
        arrayList.add(new Clocks(R.drawable.clock_bg_1, R.drawable.clock_hour_1, R.drawable.clock_minte_1, R.drawable.clock_second_2, "#593524", "#593524"));
        arrayList.add(new Clocks(R.drawable.clock_bg_2, R.drawable.clock_hour_2, R.drawable.clock_minte_2, R.drawable.clock_second_1, "#A87E70", "#A87E70"));
        arrayList.add(new Clocks(R.drawable.clock_bg_3, R.drawable.clock_hour_3, R.drawable.clock_minte_3, R.drawable.clock_second_3, "#9E6761", "#9E6761"));
        arrayList.add(new Clocks(R.drawable.clock_bg_4, R.drawable.clock_hour_4, R.drawable.clock_minte_4, R.drawable.clock_second_4, "#464D63", "#464D63"));
        arrayList.add(new Clocks(R.drawable.clock_bg_5, R.drawable.clock_hour_5, R.drawable.clock_minte_5, R.drawable.clock_second_5, "#8E493A", "#8E493A"));
        arrayList.add(new Clocks(R.drawable.clock_bg_6, R.drawable.clock_hour_6, R.drawable.clock_minte_6, R.drawable.clock_second_6, "#362325", "#362325"));
        arrayList.add(new Clocks(R.drawable.clock_bg_7, R.drawable.clock_hour_7, R.drawable.clock_minte_7, R.drawable.clock_second_7, "#54574C", "#54574C"));
        arrayList.add(new Clocks(R.drawable.clock_bg_8, R.drawable.clock_hour_8, R.drawable.clock_minte_8, R.drawable.clock_second_8, "#A57739", "#A57739"));
        arrayList.add(new Clocks(R.drawable.clock_bg_9, R.drawable.clock_hour_9, R.drawable.clock_minte_9, R.drawable.clock_second_9, "#C9D4DA", "#C9D4DA"));
        arrayList.add(new Clocks(R.drawable.clock_bg_10, R.drawable.clock_hour_10, R.drawable.clock_minte_10, R.drawable.clock_second_10, "#4F98DB", "#4F98DB"));
        arrayList.add(new Clocks(R.drawable.clock_bg_11, R.drawable.clock_hour_11, R.drawable.clock_minte_11, R.drawable.clock_second_11, "#E8BBD1", "#E8BBD1"));
        arrayList.add(new Clocks(R.drawable.clock_bg_12, R.drawable.clock_hour_12, R.drawable.clock_minte_12, R.drawable.clock_second_12, "#C9F3A7", "#C9F3A7"));
        arrayList.add(new Clocks(R.drawable.clock_bg_13, R.drawable.clock_hour_13, R.drawable.clock_minte_13, R.drawable.clock_second_13, "#CDB8E3", "#CDB8E3"));
        arrayList.add(new Clocks(R.drawable.clock_bg_14, R.drawable.clock_hour_14, R.drawable.clock_minte_14, R.drawable.clock_second_14, "#F0DDF8", "#F0DDF8"));
        return arrayList;
    }

    public ArrayList<TextClocks> getTextClocks() {
        ArrayList<TextClocks> arrayList = new ArrayList<>();
        arrayList.add(new TextClocks(R.drawable.digital_thumb_1, "#000000"));
        arrayList.add(new TextClocks(R.drawable.digital_thumb_2, "#000000"));
        arrayList.add(new TextClocks(R.drawable.digital_thumb_3, "#000000"));
        arrayList.add(new TextClocks(R.drawable.digital_thumb_4, "#000000"));
        arrayList.add(new TextClocks(R.drawable.digital_thumb_5, "#000000"));
        arrayList.add(new TextClocks(R.drawable.digital_thumb_6, "#000000"));
        arrayList.add(new TextClocks(R.drawable.digital_thumb_7, "#000000"));
        arrayList.add(new TextClocks(R.drawable.digital_thumb_8, "#000000"));
        arrayList.add(new TextClocks(R.drawable.digital_thumb_9, "#000000"));
        arrayList.add(new TextClocks(R.drawable.digital_thumb_10, "#000000"));
        return arrayList;
    }

    public ArrayList<SmartClocks> getSmartClocks() {
        ArrayList<SmartClocks> arrayList = new ArrayList<>();
        arrayList.add(new SmartClocks(R.drawable.smart_thumb_1, R.drawable.bg3));
        arrayList.add(new SmartClocks(R.drawable.smart_thumb_10, R.drawable.bg2));
        arrayList.add(new SmartClocks(R.drawable.smart_thumb_9, R.drawable.bg1));
        arrayList.add(new SmartClocks(R.drawable.smart_thumb_8, R.drawable.bg4));
        arrayList.add(new SmartClocks(R.drawable.smart_thumb_7, R.drawable.bg5));
        arrayList.add(new SmartClocks(R.drawable.smart_thumb_6, R.drawable.bg1));
        arrayList.add(new SmartClocks(R.drawable.smart_thumb_5, R.drawable.pattern_gradient_00));
        arrayList.add(new SmartClocks(R.drawable.smart_thumb_11, R.drawable.pattern_gradient_04));
        arrayList.add(new SmartClocks(R.drawable.smart_thumb_4, R.drawable.pattern_gradient_06));
        arrayList.add(new SmartClocks(R.drawable.smart_thumb_3, R.drawable.pattern_gradient_17));
        arrayList.add(new SmartClocks(R.drawable.smart_thumb_2, R.drawable.pattern_gradient_01));
        return arrayList;
    }
}
