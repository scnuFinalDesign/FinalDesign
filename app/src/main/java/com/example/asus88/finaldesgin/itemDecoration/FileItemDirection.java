package com.example.asus88.finaldesgin.itemDecoration;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.asus88.finaldesgin.util.DimenUtil;

/**
 * Created by asus88 on 2017/1/18.
 */

public class FileItemDirection extends RecyclerView.ItemDecoration {
    private Drawable mDivider;
    private int mMargin;      //图像左右的间距
    private int mTopMargin;   //上下间距
    private static int[] ATTRS = new int[]{android.R.attr.listDivider};

    public FileItemDirection(Context context, int margin) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
        mMargin = DimenUtil.dpTopx(context, margin);
        mTopMargin=DimenUtil.dpTopx(context,5);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        drawHorizontal(c, parent);
    }

    private void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop()+mTopMargin;
        final int bottom = parent.getHeight() - parent.getPaddingBottom()-mTopMargin;
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + params.rightMargin + mMargin;
            final int right = left + mDivider.getIntrinsicWidth();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.set(mMargin, 0, mDivider.getIntrinsicWidth() + mMargin, 0);
    }
}
