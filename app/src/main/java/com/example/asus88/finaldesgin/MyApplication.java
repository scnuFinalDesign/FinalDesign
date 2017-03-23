package com.example.asus88.finaldesgin;

import android.app.Application;
import android.content.Context;

/**
 * Created by asus88 on 2017/3/12.
 */

public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
       mContext=getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }
}
