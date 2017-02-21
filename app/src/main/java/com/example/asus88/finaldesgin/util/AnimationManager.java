package com.example.asus88.finaldesgin.util;

import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.view.View;

import java.util.ArrayList;


/**
 * Created by Weiping Huang at 03:27 on 16/7/26
 * For Personal Open Source
 * Contact me at 2584541288@qq.com or nightonke@outlook.com
 * For more projects: https://github.com/Nightonke
 *
 */

public class AnimationManager {

    public static ObjectAnimator animate(Object target,
                                         String property,
                                         long delay,
                                         long duration,
                                         TimeInterpolator interpolator,
                                         AnimatorListenerAdapter listenerAdapter,
                                         float... values) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, property, values);
        animator.setStartDelay(delay);
        animator.setDuration(duration);
        if (interpolator != null) animator.setInterpolator(interpolator);
        if (listenerAdapter != null) animator.addListener(listenerAdapter);
        animator.start();
        return animator;
    }

    public static ObjectAnimator animate(Object target,
                                         String property,
                                         long delay,
                                         long duration,
                                         TimeInterpolator interpolator,
                                         float... values) {
        return animate(
                target,
                property,
                delay,
                duration,
                interpolator,
                null,
                values);
    }

    public static void animate(String property,
                               long delay,
                               long duration,
                               float[] values,
                               TimeInterpolator interpolator,
                               ArrayList<View> targets) {
        for (Object target : targets) {
            animate(
                    target,
                    property,
                    delay,
                    duration,
                    interpolator,
                    null,
                    values);
        }
    }


    public static ObjectAnimator animate(
            Object target,
            String property,
            long delay,
            long duration,
            TypeEvaluator evaluator,
            int... values) {
        return animate(target, property, delay, duration, evaluator, null, values);
    }

    public static ObjectAnimator animate(
            Object target,
            String property,
            long delay,
            long duration,
            TypeEvaluator evaluator,
            AnimatorListenerAdapter listenerAdapter,
            int... values) {
        ObjectAnimator animator = ObjectAnimator.ofInt(target, property, values);
        animator.setStartDelay(delay);
        animator.setDuration(duration);
        animator.setEvaluator(evaluator);
        if (listenerAdapter != null) animator.addListener(listenerAdapter);
        animator.start();
        return animator;
    }


    private static AnimationManager ourInstance = new AnimationManager();

    public static AnimationManager getInstance() {
        return ourInstance;
    }

    private AnimationManager() {
    }
}