package com.example.asus88.finaldesgin.bean;

/**
 * Created by asus88 on 2017/1/14.
 */

public class FileBean extends Bean implements Comparable<FileBean> {
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    @Override
    public int compareTo(FileBean another) {
        return getName().compareToIgnoreCase(another.getName());
    }

    public boolean isDirectory() {
        if (this.type.equals("directory")) {
            return true;
        }
        return false;
    }
}
