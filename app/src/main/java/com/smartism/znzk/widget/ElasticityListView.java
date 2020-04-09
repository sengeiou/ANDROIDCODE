package com.smartism.znzk.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ListView;

/*
* 弹性滑动ListView
* */
public class ElasticityListView extends ListView {

    private int mMaxOverScrollY ;
    public ElasticityListView(Context context) {
        super(context);
        initData(context);
    }

    public ElasticityListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initData(context);
    }

    private  void initData(Context context){
        mMaxOverScrollY = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,80f,context.getResources().getDisplayMetrics());
        setVerticalScrollBarEnabled(false);
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, mMaxOverScrollY, isTouchEvent);
    }
}
