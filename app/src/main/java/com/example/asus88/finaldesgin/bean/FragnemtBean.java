package com.example.asus88.finaldesgin.bean;

import android.support.v4.app.Fragment;

/**
 * Created by asus88 on 2017/2/28.
 */

public class FragnemtBean {
    private Fragment mFragment;
    private String name;

    public FragnemtBean(Fragment fragment, String name) {
        mFragment = fragment;
        this.name = name;
    }

    public Fragment getFragment() {
        return mFragment;
    }

    public void setFragment(Fragment fragment) {
        mFragment = fragment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
