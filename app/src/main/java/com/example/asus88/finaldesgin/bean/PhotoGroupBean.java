package com.example.asus88.finaldesgin.bean;

import java.util.List;

/**
 * Created by asus88 on 2017/2/4.
 */

public class PhotoGroupBean {
    private List<PhotoBean> photoPath;
    private String path;
    private String name;
    private boolean isSelected;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public List<PhotoBean> getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(List<PhotoBean> photoPath) {
        this.photoPath = photoPath;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        return this.path.equals(((PhotoGroupBean) o).path);
    }
}
