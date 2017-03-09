package com.example.asus88.finaldesgin.util;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Created by asus88 on 2016/12/29.
 */

public class TimeUtil {
    public static String ms2Time(String time) {
        long duration = Long.parseLong(time);
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT+0"));
        return format.format(duration);
    }

    public static String ms2Modify(Long duration) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return format.format(duration);
    }
}
