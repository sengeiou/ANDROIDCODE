package com.smartism.znzk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 重写focused方法，一直返回true，跑马灯就可以一直跑了
 */


public class TextViewAutoHorizontalScroll extends TextView {

    public TextViewAutoHorizontalScroll(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public TextViewAutoHorizontalScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextViewAutoHorizontalScroll(Context context) {
        super(context);
    }

    @Override

    public boolean isFocused() {
        return true;
    }
}