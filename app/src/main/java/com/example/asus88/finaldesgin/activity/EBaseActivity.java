package com.example.asus88.finaldesgin.activity;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.asus88.finaldesgin.R;
import com.example.asus88.finaldesgin.bean.FabMenuButtonBean;
import com.example.asus88.finaldesgin.connection.Dev;
import com.example.asus88.finaldesgin.connection.Manager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asus88 on 2017/3/11.
 */

public class EBaseActivity extends BaseActivity implements Manager.onDevMapChangeListener {

    public Manager conManager;
    public WifiManager mWifiManager;
    private FrameLayout background;
    private TextView[] fabButton;
    private int fabButtonSize;
    private List<FabMenuButtonBean> fabBtnList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        conManager = Manager.getManager();
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        conManager.setOnDevMapChangeListener(this);
        fabBtnList = new ArrayList<>();
        fabButton = new TextView[5];

    }

    @Override
    public void onDevNumChange(Dev dev, boolean isAdd) {
// link
    }

    @Override
    public void onTransferStateChange(Dev dev, boolean isEnabled) {
// 3个页面
    }

    @Override
    public void onNetWorkStateChange() {
//// TODO: 2017/3/11 3个页面都重写
    }

    @Override
    public void onCreateTransferFail(Dev dev) {
// link
    }

    public TextView[] getFabButton() {
        return fabButton;
    }

    public void setFabButton(TextView[] fabButton) {
        this.fabButton = fabButton;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        conManager.setOnDevMapChangeListener(null);
    }

    public void showBackground() {
        if (background == null) {
            ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
            background = new FrameLayout(this);
            background.setBackgroundColor(getResources().getColor(R.color.fab_menu_color));
            background.setLayoutParams(new FrameLayout.LayoutParams(rootView.getWidth(), rootView.getHeight()));
            background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                //    removeBtnFromBg();
                    hideBackground();
                }
            });
           // initFabButtonData();
            rootView.addView(background);
        }
        addBtnToBg();
        background.setVisibility(View.VISIBLE);
    }

    public void hideBackground() {
        background.setVisibility(View.GONE);
    }

    public void addBtnToBg() {
        for (int i = 5 - fabButtonSize; i < 5; i++) {
            background.addView(fabButton[i]);
        }
    }
}
