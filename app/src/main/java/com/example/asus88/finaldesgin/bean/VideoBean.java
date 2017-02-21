package com.example.asus88.finaldesgin.bean;

import android.graphics.Bitmap;

/**
 * Created by asus88 on 2016/12/22.
 */

public class VideoBean extends Bean {
    private String duration;
    private Bitmap bitmap;

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
