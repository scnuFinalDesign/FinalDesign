package com.example.asus88.finaldesgin.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by asus88 on 2016/12/22.
 */

public class PhotoBean implements Parcelable{
    private String path;
    private int selected;  //0 未选 1已选

    public PhotoBean() {
    }

    public PhotoBean(String path, int selected) {
        this.path = path;
        this.selected = selected;
    }

    public static final Creator<PhotoBean> CREATOR = new Creator<PhotoBean>() {
        @Override
        public PhotoBean createFromParcel(Parcel in) {
            return new PhotoBean(in.readString(),in.readInt());
        }

        @Override
        public PhotoBean[] newArray(int size) {
            return new PhotoBean[size];
        }
    };

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getSelected() {
        return selected;
    }

    public void setSelected(int selected) {
        this.selected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeInt(selected);
    }
}
