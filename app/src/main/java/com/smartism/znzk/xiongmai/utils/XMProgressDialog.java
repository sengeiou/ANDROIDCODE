package com.smartism.znzk.xiongmai.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartism.znzk.R;

public class XMProgressDialog extends Dialog {

    private Context mContext ;
    public XMProgressDialog(@NonNull Context context) {
        super(context);
        mContext = context ;
    }

    private String mMessage ;
    private TextView mContent ;
    //这个方法在Dialog首次显示的时候才会调用
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.xm_progress_dialog);
        mContent= findViewById(R.id.juhua_content);
        LinearLayout v = findViewById(R.id.xm_content_parent);
        int width  = mContext.getResources().getDisplayMetrics().widthPixels/4;
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.BLACK);
        drawable.setAlpha(127);
        drawable.setCornerRadius(mContext.getResources().getDimension(R.dimen.dp_12));
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.JELLY_BEAN_MR1){
            v.setBackgroundDrawable(drawable);
        }else{
            v.setBackground(drawable);
        }
        //Window的宽高默认是适应内容
        Window window = getWindow() ;
        window.setBackgroundDrawable(new ColorDrawable(0));
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.width = width ;
        wlp.height = width ;
        window.setAttributes(wlp);
       // setCancelable(false);

        if(!TextUtils.isEmpty(mMessage)){
            mContent.setVisibility(View.VISIBLE);
            mContent.setText(mMessage);
        }
    }

    public void setMesage(String mesage){
        this.mMessage = mesage ;
        if(mContent!=null) {
            if(!TextUtils.isEmpty(mMessage)){
                mContent.setVisibility(View.VISIBLE);
                mContent.setText(mMessage);
            }else{
                mContent.setVisibility(View.GONE);
            }
        }

    }

    public void showDialog(){
        if(!isShowing()){
            show();
        }
    }

    public void dismissDialog(){
        if(isShowing()){
            dismiss();
        }
    }
}
