package com.smartism.znzk.view;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.DeviceInfoActivity;
import com.smartism.znzk.adapter.ExperAdapter;
import com.smartism.znzk.adapter.ZhujiAdapter;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * 显示菜单的poputwindow
 *
 * @author 王建
 *         2015年9月30日
 */
public class HeaterMenuPopupWindow extends PopupWindow {

    private Context context;
    private View mMenuView;
    private View popShare, popEditFluid,popCreateGroup,popWifiDiagnosis;

    public HeaterMenuPopupWindow(final Activity context, OnClickListener itemsOnClick) {
        super(context);
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.activity_heater_menu, null);
        popShare =  mMenuView.findViewById(R.id.tv_share);
        popEditFluid = mMenuView.findViewById(R.id.tv_edit_fluid);
        popCreateGroup = mMenuView.findViewById(R.id.tv_create_group);
        popWifiDiagnosis = mMenuView.findViewById(R.id.tv_wifi_diagnosis);

        popShare.setOnClickListener(itemsOnClick);
        popEditFluid.setOnClickListener(itemsOnClick);
        popCreateGroup.setOnClickListener(itemsOnClick);
        popWifiDiagnosis.setOnClickListener(itemsOnClick);

        int h = context.getWindowManager().getDefaultDisplay().getHeight();
        int w = context.getWindowManager().getDefaultDisplay().getWidth();

        //设置按钮监听
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(w * 1 / 2);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.Right_menu_animation);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.pop_layout2).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = context.getWindow().getAttributes();
                params.alpha = 1f;
                context.getWindow().setAttributes(params);
            }
        });

    }
}
