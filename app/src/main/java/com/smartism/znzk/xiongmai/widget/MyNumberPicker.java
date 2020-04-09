package com.smartism.znzk.xiongmai.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.lang.reflect.Field;

class MyNumberPicker extends NumberPicker {

    public MyNumberPicker(Context context) {
        super(context);
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);//设置EditText不可以编辑
    }

    public MyNumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);//设置EditText不可以编辑

    }

    public void setDeviderColor(int color) {
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    //设置分割线的颜色值
                    pf.set(this,new ColorDrawable(color));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        invalidate();
    }


    @Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateView(child);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateView(child);
    }

    private void updateView(View view) {
        if (view instanceof EditText) {
            ((EditText) view).setTextColor(Color.BLACK);
           ((EditText) view).setTextSize(getContext().getResources().getDisplayMetrics().scaledDensity*5);
        }
    }
}
