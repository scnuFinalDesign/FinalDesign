package com.example.asus88.finaldesgin.bean;

import android.graphics.drawable.Drawable;

/**
 * Created by asus88 on 2016/12/22.
 */

public class ApplicationBean extends Bean {
    private String packName;
    private Drawable icon;
    private String number;  //版本号

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
