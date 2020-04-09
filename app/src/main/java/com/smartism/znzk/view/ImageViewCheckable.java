package com.smartism.znzk.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageView;

/**
 * Created by win7 on 2017/8/9.
 */


public class ImageViewCheckable extends ImageView implements Checkable {

    private boolean mChecked;


    private static final int[] CHECK_STATE_SET = {android.R.attr.state_checked};

    public ImageViewCheckable(Context context) {
        super(context);
    }

    public ImageViewCheckable(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageViewCheckable(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ImageViewCheckable(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();//刷新drawable状态
        }
    }

    /**
     * Imageview默认不支持state_checked状态，需要重写此方法
     *
     * @param extraSpace
     * @return
     */
    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECK_STATE_SET);
        }
        return drawableState;
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }
}
