package com.example.asus88.finaldesgin.itemDecoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.asus88.finaldesgin.util.DimenUtil;

/**
 * Created by asus88 on 2017/2/11.
 */

public class LineItemDecoration extends RecyclerView.ItemDecoration {
    private static final String TAG = "LineItemDecoration";
    private Drawable mDivier;
    private int marginLeft, marginRight;

    public LineItemDecoration(Context context, int marginLeft, int marginRight, int drawableId) {
        this.marginLeft = DimenUtil.getRealWidth(context, 1280, marginLeft);
        this.marginRight = DimenUtil.getRealWidth(context, 1280, marginRight);
        mDivier = context.getResources().getDrawable(drawableId);
        Log.d(TAG, "LineItemDecoration: "+this.marginLeft+"   "+this.marginRight+"  "+DimenUtil.getDeviceWidth(context));
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        drawVertical(c, parent);
    }

    private void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft() + marginLeft;
        final int right = parent.getWidth() - parent.getPaddingRight() - marginRight;
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivier.getIntrinsicHeight();
            mDivier.setBounds(left, top, right, bottom);
            mDivier.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(0, 0, 0, mDivier.getIntrinsicHeight());
    }
}
