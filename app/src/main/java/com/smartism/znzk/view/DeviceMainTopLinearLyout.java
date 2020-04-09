package com.smartism.znzk.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * Created by win7 on 2017/8/2.
 */

public class DeviceMainTopLinearLyout extends LinearLayout {
    private SwipeRefreshLayout mRefreshLayout;

    private boolean isShowSence;

    public boolean isShowSence() {
        return isShowSence;
    }

    public void setShowSence(boolean showSence) {
        isShowSence = showSence;
    }

    public SwipeRefreshLayout getmRefreshLayout() {
        return mRefreshLayout;
    }

    public void setmRefreshLayout(SwipeRefreshLayout mRefreshLayout) {
        this.mRefreshLayout = mRefreshLayout;
    }

    public DeviceMainTopLinearLyout(Context context) {
        super(context);
    }

    public DeviceMainTopLinearLyout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DeviceMainTopLinearLyout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DeviceMainTopLinearLyout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mRefreshLayout != null) {
            mRefreshLayout.setEnabled(true);
        }
        return super.onInterceptTouchEvent(ev);
    }
}
