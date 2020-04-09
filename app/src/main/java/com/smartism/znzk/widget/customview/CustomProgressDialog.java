package com.smartism.znzk.widget.customview;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import com.smartism.znzk.R;

public class CustomProgressDialog extends Dialog {

    CustomProgressView mProgressView ;
    int width ,height ;

    public CustomProgressDialog(@NonNull Context context) {
        super(context);
        mProgressView = new CustomProgressView(context);
         width = context.getResources().getDisplayMetrics().widthPixels/7;
         height = width ;
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(width,height);
        mProgressView.setLayoutParams(lp);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor("#7f000000"));
        drawable.setCornerRadius(context.getResources().getDimension(R.dimen.dp_8));
        mProgressView.setBackgroundDrawable(drawable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE) ;//去掉标题栏
        setContentView(mProgressView);//设置子View
        Window window = getWindow() ;
        WindowManager.LayoutParams lp  = window.getAttributes();
        lp.width = width ;
        lp.height = height ;
        lp.gravity= Gravity.CENTER ;
        lp.alpha= 1f ;
        lp.dimAmount = 0f ;
        getWindow().setAttributes(lp);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(true);
    }
}
