package com.example.asus88.finaldesgin.myViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ProgressBar;

import com.example.asus88.finaldesgin.R;

/**
 * Created by asus88 on 2017/2/28.
 */

public class DigitalProgressBar extends ProgressBar {
    private static final String TAG = "DigitalProgressBar";

    private static final int DEFAULT_TEXT_SIZE = 30;
    private static final int DEFAULT_TEXT_COLOR = 0xFF000000;
    private static final int DEFAULT_UNREACHED_BAR_COLOR = 0xFF9b9898;
    private static final int DEFAULT_REACHED_BAR_COLOR = 0xFF07df97;
    private static final int DEFAULT_TEXT_OFFSET = 15;
    private static final int DEFAULT_BAR_HEIGHT = 3;

    private int textColor = DEFAULT_TEXT_COLOR;
    private int texOffset = dp2px(DEFAULT_TEXT_OFFSET);
    private int textSize = sp2px(DEFAULT_TEXT_SIZE);
    private int unreachedBarColor = DEFAULT_UNREACHED_BAR_COLOR;
    private int reachedBarColor = DEFAULT_REACHED_BAR_COLOR;
    private int barHeight = dp2px(DEFAULT_BAR_HEIGHT);
    private int mViewWidth;

    private Paint mPaint;

    public DigitalProgressBar(Context context) {
        this(context, null);
    }

    public DigitalProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DigitalProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.DigitalProgressBar);
        textColor = a.getColor(R.styleable.DigitalProgressBar_text_color, DEFAULT_TEXT_COLOR);
        textSize = (int) a.getDimension(R.styleable.DigitalProgressBar_text_size, DEFAULT_TEXT_SIZE);
        texOffset = (int) a.getDimension(R.styleable.DigitalProgressBar_text_offset, DEFAULT_TEXT_OFFSET);
        unreachedBarColor = a.getColor(R.styleable.DigitalProgressBar_unreached_color, DEFAULT_UNREACHED_BAR_COLOR);
        reachedBarColor = a.getColor(R.styleable.DigitalProgressBar_reached_color, DEFAULT_REACHED_BAR_COLOR);
        a.recycle();
        mPaint = new Paint();
        mPaint.setTextSize(textSize);
        mPaint.setColor(textColor);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = getWidth() - getPaddingRight() - getPaddingLeft();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(getPaddingLeft(), getHeight() / 2);
        float percentage = getProgress() * 1.0f / getMax();
        float reachBarWidth = mViewWidth * percentage;
        boolean isNeedUnReached = true;
        String text = getProgress() + "%";

        float textWidth = mPaint.measureText(text);
        float textHeight = (mPaint.descent() + mPaint.ascent()) / 2;

        if (reachBarWidth + textWidth > mViewWidth) {
            reachBarWidth = mViewWidth - textWidth;
            isNeedUnReached = false;
        }

        //已到达进度
        float textEndX = reachBarWidth - texOffset / 2;
        if (textEndX > 0) {
            mPaint.setColor(reachedBarColor);
            mPaint.setStrokeWidth(barHeight);
            canvas.drawLine(0, 0, textEndX, 0, mPaint);
        }

        mPaint.setColor(textColor);
        canvas.drawText(text, reachBarWidth, -textHeight, mPaint);

        // 未到达进度
        if (isNeedUnReached) {
            float start = reachBarWidth + textWidth + texOffset / 2;
            mPaint.setColor(unreachedBarColor);
            mPaint.setStrokeWidth(barHeight);
            canvas.drawLine(start, 0, mViewWidth, 0, mPaint);
        }
        canvas.restore();
    }

    protected int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

    /**
     * sp 2 px
     *
     * @param spVal
     * @return
     */
    protected int sp2px(int spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, getResources().getDisplayMetrics());

    }
}
