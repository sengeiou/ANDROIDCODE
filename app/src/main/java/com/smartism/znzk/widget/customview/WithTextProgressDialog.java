package com.smartism.znzk.widget.customview;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartism.znzk.R;

//带文本的自定义进度条
public class WithTextProgressDialog extends Dialog {


    private View mProgressView ;
    private TextView mTextDisplay ;
    private int width,height ;
    public WithTextProgressDialog(Context context) {
        super(context);
        mProgressView   = getLayoutInflater().inflate(R.layout.layout_dialog_view,null);
        width = context.getResources().getDisplayMetrics().widthPixels/2;
        height = width/2;
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        mProgressView.setLayoutParams(lp);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor("#7f000000"));
        drawable.setCornerRadius(context.getResources().getDimension(R.dimen.dp_8));
        mProgressView.setBackgroundDrawable(drawable);

        mTextDisplay = mProgressView.findViewById(R.id.progress_tv);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE) ;//去掉标题栏
        setContentView(mProgressView);//设置子View
        Window window = getWindow() ;
        WindowManager.LayoutParams lp  = window.getAttributes();
        lp.width = width ;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT ;
        lp.gravity= Gravity.CENTER ;
        lp.alpha= 1f ;
        lp.dimAmount = 0f ;
        getWindow().setAttributes(lp);
        getWindow().setBackgroundDrawable(null);
        setCancelable(true);
    }

    public void setText(String text){
        mTextDisplay.setText(text);
        mTextDisplay.postInvalidate();
    }
}
