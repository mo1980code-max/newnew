package com.clock.livewallpaper.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;



public class TinyDB {
    private String DEFAULT_APP_IMAGEDATA_DIRECTORY;
    private String lastImagePath = "";
    private SharedPreferences preferences;

    public TinyDB(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }





    public int getInt(String str) {
        return this.preferences.getInt(str, 0);
    }

    public int getInt(String str, int i) {
        return this.preferences.getInt(str, i);
    }



    public float getFloat(String str, float f) {
        return this.preferences.getFloat(str, f);
    }



    public String getString(String str) {
        return this.preferences.getString(str, "");
    }


    public boolean getBoolean(String str) {
        return this.preferences.getBoolean(str, false);
    }


    public Object getObject(String str, Class<?> cls) {
        return getObject(str, cls, new Gson());
    }


    public Object getObject(String str, Class<?> cls, Gson gson) {
        Object fromJson = gson.fromJson(getString(str), (Class<Object>) cls);
        return fromJson == null ? new GetClocks().getClocks().get(0) : fromJson;
    }


    public void putInt(String str, int i) {
        checkForNullKey(str);
        this.preferences.edit().putInt(str, i).apply();
    }



    public void putFloat(String str, float f) {
        checkForNullKey(str);
        this.preferences.edit().putFloat(str, f).apply();
    }



    public void putString(String str, String str2) {
        checkForNullKey(str);
        checkForNullValue(str2);
        this.preferences.edit().putString(str, str2).apply();
    }


    public void putBoolean(String str, boolean z) {
        checkForNullKey(str);
        this.preferences.edit().putBoolean(str, z).apply();
    }



    public void putObject(String str, Object obj) {
        putObject(str, obj, new Gson());
    }



    public void putObject(String str, Object obj, Gson gson) {
        checkForNullKey(str);
        putString(str, gson.toJson(obj));
    }




    public void checkForNullKey(String str) {
        str.getClass();
    }

    public void checkForNullValue(String str) {
        str.getClass();
    }
}
