package com.example.asus88.finaldesgin.bean;

/**
 * Created by asus88 on 2017/2/27.
 */

public abstract class SendTakBean {
    public int getLayoutId() {
        return this.initLayoutId();
    }


    /**
     * 该条目的布局id
     *
     * @return 布局id
     */
    protected abstract int initLayoutId();
}
