package com.example.asus88.finaldesgin;

import android.text.TextUtils;

import com.example.asus88.finaldesgin.bean.FileBean;

import java.util.Comparator;

/**
 * Created by asus88 on 2017/3/8.
 */

public class MyComparator implements Comparator {

    @Override
    public int compare(Object lhs, Object rhs) {
            String f1 = ((FileBean) lhs).getType();
            String f2 = ((FileBean) rhs).getType();
            if (TextUtils.isEmpty(f1))
                return -1;
            else if (TextUtils.isEmpty(f2)) return 1;
            else return f1.compareToIgnoreCase(f2);

    }
}
