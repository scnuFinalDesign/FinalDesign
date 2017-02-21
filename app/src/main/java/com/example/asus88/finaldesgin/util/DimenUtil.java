package com.example.asus88.finaldesgin.util;

import android.content.Context;

/**
 * Created by asus88 on 2017/1/14.
 */

public class DimenUtil {
    public static int dpTopx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int getDeviceHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getDeviceWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 根据设计稿 返回真实高度
     *
     * @param context
     * @param designHeight 设计稿屏幕高度
     * @param viewHeight   view height 设计稿 控件高度
     * @return
     */
    public static int getRealHeight(Context context, int designHeight, int viewHeight) {
        return (int) ((float) viewHeight * getDeviceHeight(context)) /designHeight;
    }

    /**
     * 根据设计稿 返回真实宽度
     *
     * @param context
     * @param designWidth
     * @param viewWidth
     * @return
     */
    public static int getRealWidth(Context context, int designWidth, int viewWidth) {
        return (int) (((float) viewWidth * getDeviceWidth(context)) / designWidth);
    }

    /**
     * 根据真实高度返回设计中高度
     *
     * @param context
     * @param designHeight
     * @param viewHeight
     * @return
     */
    public static int getDesignHeight(Context context, int designHeight, int viewHeight) {
        return (int) (((float) viewHeight * designHeight) / getDeviceHeight(context));
    }

    /**
     * 根据真实宽度返回真实宽度
     *
     * @param context
     * @param designWidth
     * @param viewWidth
     * @return
     */
    public static int getDesignWidth(Context context, int designWidth, int viewWidth) {
        return (int) (((float) viewWidth * designWidth) / getDeviceWidth(context));
    }
}
