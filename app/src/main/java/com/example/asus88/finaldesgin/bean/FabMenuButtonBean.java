package com.example.asus88.finaldesgin.bean;

import android.view.View;

/**
 * Created by asus88 on 2017/2/18.
 */

public class FabMenuButtonBean {
    private String text;
    private int backgroundId;
    private View.OnClickListener mOnClickListener;

    public FabMenuButtonBean(String text, int backgroundId) {
        this.text = text;
        this.backgroundId = backgroundId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getBackgroundId() {
        return backgroundId;
    }

    public void setBackgroundId(int backgroundId) {
        this.backgroundId = backgroundId;
    }

    public View.OnClickListener getOnClickListener() {
        return mOnClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }
}
