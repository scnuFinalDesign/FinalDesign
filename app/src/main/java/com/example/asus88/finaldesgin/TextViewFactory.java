package com.example.asus88.finaldesgin;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.asus88.finaldesgin.bean.FabMenuButtonBean;
import com.example.asus88.finaldesgin.util.DimenUtil;

/**
 * Created by asus88 on 2017/2/9.
 */

public class TextViewFactory {
    public static TextView createTextView(Context context, FabMenuButtonBean bean) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(DimenUtil.getRealWidth(context, 768, 600),
                DimenUtil.getRealHeight(context, 1280, 120));
        TextView textView = new TextView(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.setElevation(20);
        }
        textView.setText(bean.getText());
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(16);
        textView.setBackground(context.getResources().getDrawable(bean.getBackgroundId()));
        textView.setLayoutParams(layoutParams);
        textView.setOnClickListener(bean.getOnClickListener());
        return textView;
    }
}
