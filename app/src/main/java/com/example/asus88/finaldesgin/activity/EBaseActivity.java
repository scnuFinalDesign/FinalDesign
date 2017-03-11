package com.example.asus88.finaldesgin.activity;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.example.asus88.finaldesgin.connection.Dev;
import com.example.asus88.finaldesgin.connection.Manager;

/**
 * Created by asus88 on 2017/3/11.
 */

public class EBaseActivity extends BaseActivity implements Manager.onDevMapChangeListener {
    public Manager conManager;
    public WifiManager mWifiManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        conManager = Manager.getManager();
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        conManager.setOnDevMapChangeListener(this);
    }

    @Override
    public void onDevNumChange(Dev dev, boolean isAdd) {

    }

    @Override
    public void onTransferStateChange(Dev dev, boolean isEnabled) {

    }

    @Override
    public void onNetWorkStateChange() {

    }

    @Override
    public void onCreateTransferFail(Dev dev) {

    }


}
