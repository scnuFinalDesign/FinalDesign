package com.example.asus88.finaldesgin.util;

import android.animation.ObjectAnimator;

/**
 * Created by asus88 on 2017/3/21.
 */

public class AnimationUtil {
    public static ObjectAnimator createAnimator(Object target, String propertyName, float start, float end) {
        return ObjectAnimator.ofFloat(target, propertyName, start, end);
    }
}
