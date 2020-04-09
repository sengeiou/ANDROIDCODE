package com.smartism.znzk.camera.widgh;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by Administrator on 2017/5/10.
 */

public class  RelativeHight extends RelativeLayout {

    public RelativeHight(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RelativeHight(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RelativeHight(Context context) {
        super(context);
    }


    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));

//        int childWidthSize = childHeightSize*720/400;
//        int childHeightSize = getMeasuredHeight();
        int childWidthSize = getMeasuredWidth();
        int childHeightSize = childWidthSize*400/720;
        heightMeasureSpec = MeasureSpec.makeMeasureSpec((childHeightSize), MeasureSpec.EXACTLY);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

}
