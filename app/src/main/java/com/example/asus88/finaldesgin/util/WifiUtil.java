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
     *@param mWifiManager
     *
     * @return
     */
    public static boolean createHotspot(WifiManager mWifiManager) {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
        try {
            WifiConfiguration configuration = new WifiConfiguration();
            configuration.SSID = Build.MODEL;
            configuration.preSharedKey = createPassWord();
            configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            Method method = mWifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class,
                    Boolean.TYPE);
            return (Boolean) method.invoke(mWifiManager, configuration, true);
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
        Random rd = new Random(); // 创建随机对象
        String passWord = ""; // 保存随机数
        int rdGet; // 取得随机数
        do {
            if (rd.nextInt() % 2 == 1) {
                rdGet = Math.abs(rd.nextInt()) % 10 + 48; // 产生48到57的随机数(0-9的键位值)
            } else {
                rdGet = Math.abs(rd.nextInt()) % 26 + 97; // 产生97到122的随机数(a-z的键位值)
            }
            char num1 = (char) rdGet; // int转换char
            String dd = Character.toString(num1);
            passWord += dd;
        } while (passWord.length() < 8);// 设定长度，此处假定长度小于8
        return passWord;
    }
}
