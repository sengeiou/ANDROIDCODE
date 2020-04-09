package com.smartism.znzk.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by win7 on 2018/1/19.
 */

public class CustomRotateTextView extends TextView {
    public CustomRotateTextView(Context context) {
        super(context);
    }

    public CustomRotateTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRotateTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomRotateTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.rotate(90,getMeasuredWidth()/2,getMeasuredHeight()/2);
        super.onDraw(canvas);
    }
}
