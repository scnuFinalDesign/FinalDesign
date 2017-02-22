package com.example.asus88.finaldesgin.util;

import android.net.wifi.WifiManager;

import java.lang.reflect.Method;

/**
 * Created by asus88 on 2017/2/22.
 */

public class WifiUitl {
    /**
     * @return WIFI_AP_STATE_DISABLED     10
     * #WIFI_AP_STATE_DISABLING   11
     * #WIFI_AP_STATE_ENABLING    12
     * #WIFI_AP_STATE_ENABLED     13
     * #WIFI_AP_STATE_FAILED      14
     */
    public static int getWifiApState(WifiManager manager) {
        try {
            Method method = manager.getClass().getMethod("getWifiApState");
            int state = (int) method.invoke(manager);
            return state;
        } catch (Exception e) {
            return 10;
        }
    }
}
