package com.example.asus88.finaldesgin.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.asus88.finaldesgin.MyApplication;

/**
 * Created by asus88 on 2017/3/23.
 */

public class SharePUtil {
    public static void save(String name, String key, String value) {
        SharedPreferences sp = MyApplication.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String read(String name, String key) {
        SharedPreferences sp = MyApplication.getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        return sp.getString(key, null);
    }
}
