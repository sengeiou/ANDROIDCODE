package com.smartism.znzk.xiongmai.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import com.smartism.znzk.R;

import java.util.ArrayList;

public class PickerDialog extends Dialog {

    PickerParentLayout mLayout ;
    public PickerDialog(@NonNull Context context) {
        super(context);
        mLayout = new PickerParentLayout(getContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        mLayout.setLayoutParams(lp);
        mLayout.setTopBackground(new ColorDrawable(getContext().getResources().getColor(R.color.zhzj_default)));
        setContentView(mLayout);
        int width  = getContext().getResources().getDisplayMetrics().widthPixels;
        Window window = getWindow() ;
        window.setBackgroundDrawable(new ColorDrawable(0));
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        wlp.width = width ;
        wlp.height = width*2/3;
        window.setAttributes(wlp);
        setCancelable(true);
    }

    public void setDisplayValues(ArrayList<String[]> values,PickerParentLayout.OnClickListener listener){
        mLayout.setPickerDisplayValues(values);
        mLayout.setPickerClick(listener);
    }
    public void setTile(String title){
        mLayout.setTitle(title);
    }

    public void setConfirmContent(String content){
        mLayout.setConfirmTitle(content);
    }

    public void setConcelContent(String content){
        mLayout.setConcelTitle(content);
    }
}
