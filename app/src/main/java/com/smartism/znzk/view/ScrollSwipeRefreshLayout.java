package com.smartism.znzk.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

/**
 * Created by win7 on 2017/2/7.
 */

public class ScrollSwipeRefreshLayout extends SwipeRefreshLayout {


    public ViewGroup viewGroup;

    public ScrollSwipeRefreshLayout(Context context) {
        super(context);
    }

    public ScrollSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewGroup getViewGroup() {
        return viewGroup;
    }

    public void setViewGroup(ViewGroup viewGroup) {
        this.viewGroup = viewGroup;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (null != viewGroup) {
            if (viewGroup.getScrollY()!=0){
                return false;
            }else {
                return super.onTouchEvent(ev);
            }

//            if (viewGroup.getScrollY() > 1) {
//                //直接截断时间传播
//                return false;
//            } else {
//                return super.onTouchEvent(ev);
//            }
        }
        return super.onTouchEvent(ev);
    }
}
