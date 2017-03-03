package com.example.asus88.finaldesgin.bean;

import com.example.asus88.finaldesgin.connection.Dev;

/**
 * Created by asus88 on 2017/2/27.
 */

public abstract class SendTakBean {
    public int getLayoutId() {
        return this.initLayoutId();
    }

    private Dev dev;

    public Dev getDev() {
        return dev;
    }

    public void setDev(Dev dev) {
        this.dev = dev;
    }

    /**
     * 该条目的布局id
     *
     * @return 布局id
     */
    protected abstract int initLayoutId();
}
