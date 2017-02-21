package com.example.asus88.finaldesgin.util;

import android.util.Log;

/**
 * Created by asus88 on 2016/12/21.
 */

public class LogUtil {
    private static boolean isShowLog = true;

    public static void logd(String TAG, String content) {
        if (!isShowLog)
            return;
        Log.d(TAG, content);
    }
}
