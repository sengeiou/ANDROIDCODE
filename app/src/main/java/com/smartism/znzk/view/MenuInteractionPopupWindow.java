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
import android.widget.PopupWindow;

import com.smartism.znzk.R;

/**
 * 显示互动模块菜单的poputwindow
 * @author 王建
 * 2016年12月03日
 *
 */
public class MenuInteractionPopupWindow extends PopupWindow {

	private Context context;

	private View mMenuView;
	private View pop_myfaq,pop_myidea,pop_addfaq,pop_addidea,pop_addwjob,pop_myyugou;

	public MenuInteractionPopupWindow(Activity context, OnClickListener itemsOnClick) {
		super(context);
		this.context = context;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.activity_devices_interaction_rightmenu, null);
		pop_addfaq = mMenuView.findViewById(R.id.pop_newfaq);
		pop_addidea = mMenuView.findViewById(R.id.pop_newidea);
		pop_myyugou = mMenuView.findViewById(R.id.pop_myyugou);
		pop_addwjob = mMenuView.findViewById(R.id.pop_newwjob);
		pop_myfaq = mMenuView.findViewById(R.id.pop_myfaq);
		pop_myidea=mMenuView.findViewById(R.id.pop_myidea);

		pop_addfaq.setOnClickListener(itemsOnClick);
		pop_addidea.setOnClickListener(itemsOnClick);
		pop_myyugou.setOnClickListener(itemsOnClick);
		pop_addwjob.setOnClickListener(itemsOnClick);
		pop_myfaq.setOnClickListener(itemsOnClick);
		pop_myidea.setOnClickListener(itemsOnClick);
		int h = context.getWindowManager().getDefaultDisplay().getHeight();
		int w = context.getWindowManager().getDefaultDisplay().getWidth();
		//设置按钮监听
		//设置SelectPicPopupWindow的View
		this.setContentView(mMenuView);
		//设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth(w/2+50);
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

				int height = mMenuView.findViewById(R.id.pop_layout3).getTop();
				int y=(int) event.getY();
				if(event.getAction()==MotionEvent.ACTION_UP){
					if(y<height){
						dismiss();
					}
				}
				return true;
			}
		});

	}

	/**
	 * 根据不同的内容显示不同的菜单 index 0 faq， 1点子  2工单
	 * @param index
     */
	public void update(int index){
        pop_addfaq.setVisibility(View.GONE);
        pop_addidea.setVisibility(View.GONE);
        pop_myyugou.setVisibility(View.GONE);
        pop_addwjob.setVisibility(View.GONE);
        pop_myfaq.setVisibility(View.GONE);
        pop_myidea.setVisibility(View.GONE);
        switch (index) {
            case 0:
                pop_myfaq.setVisibility(View.VISIBLE);
                pop_addfaq.setVisibility(View.VISIBLE);
                break;
            case 1:
                pop_addidea.setVisibility(View.VISIBLE);
				pop_myyugou.setVisibility(View.VISIBLE);
                pop_myidea.setVisibility(View.VISIBLE);
                break;
            case 2:
                pop_addwjob.setVisibility(View.VISIBLE);
                break;
        }
	}
}
