package com.example.asus88.finaldesgin.util;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.lang.reflect.Method;
import java.util.Random;

/**
 * Created by asus88 on 2017/2/22.
 */

public class WifiUtil {
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

    /**
     * 创建热点
     *
     * @param mWifiManager
     * @return
     */
    public static boolean createHotspot(WifiManager mWifiManager) {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
        try {
            WifiConfiguration con = new WifiConfiguration();
            con.SSID = Build.MODEL;
            con.preSharedKey = createPassWord();
            con.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class,
                    Boolean.TYPE);
            return (Boolean) method.invoke(mWifiManager, con, true);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 随机生成8位密码
     *
     * @return
     */
    private static String createPassWord() {
        Random mRandom = new Random();
        String passWord = ""; // 保存随机数
        int num;
        do {
            if (mRandom.nextInt() % 2 == 1) {
                num = Math.abs(mRandom.nextInt()) % 10 + 48;//0-9
            } else {
                num = Math.abs(mRandom.nextInt()) % 26 + 97;//a-z
            }
            char num1 = (char) num; // int转换char
            String dd = Character.toString(num1);
            passWord += dd;
        } while (passWord.length() < 8);// 设定长度，此处假定长度小于8
        return passWord;
    }
}
