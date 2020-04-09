package com.smartism.znzk.view;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.smartism.znzk.R;

/**
 * 主机工厂模式配对
 * @author 王建
 * 2015年9月30日
 */
public class DeviceFactoryPopupWindow extends PopupWindow {

	private View mView;
	private TextView factory_tips,f_1_on,f_1_off,f_2_on,f_2_off,f_3_on,f_3_off;

	public DeviceFactoryPopupWindow(final Activity context,OnClickListener itemsOnClick,final OnCancelBeforeListener cancel) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.activity_add_device_factory_tips, null);
		factory_tips = (TextView) mView.findViewById(R.id.factory_tips);
		f_1_on = (TextView) mView.findViewById(R.id.f_1_on);
		f_1_off = (TextView) mView.findViewById(R.id.f_1_off);
		f_2_on = (TextView) mView.findViewById(R.id.f_2_on);
		f_2_off = (TextView) mView.findViewById(R.id.f_2_off);
		f_3_on = (TextView) mView.findViewById(R.id.f_3_on);
		f_3_off = (TextView) mView.findViewById(R.id.f_3_off);
		//设置SelectPicPopupWindow的View
		this.setContentView(mView);
		//设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth(LayoutParams.MATCH_PARENT);
		//设置SelectPicPopupWindow弹出窗体的高
		this.setHeight(LayoutParams.WRAP_CONTENT);
		//设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		//设置SelectPicPopupWindow弹出窗体动画效果
		this.setAnimationStyle(R.style.Devices_list_menu_Animation);
		//实例化一个ColorDrawable颜色为白色
		//ColorDrawable dw = new ColorDrawable(0xb0000000);
		ColorDrawable dw = new ColorDrawable(0x00000000);
		//设置SelectPicPopupWindow弹出窗体的背景
		this.setBackgroundDrawable(dw);
		//mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
		mView.setOnTouchListener(new OnTouchListener() {
			
			public boolean onTouch(View v, MotionEvent event) {
				
				int height = mView.findViewById(R.id.pop_layout).getTop();
				int y=(int) event.getY();
				if(event.getAction()==MotionEvent.ACTION_UP){
					if(y<height){
						cancel.onCancelBefore();
					}
				}
				return true;
			}
		});
		mView.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
			        cancel.onCancelBefore();
					return true;
			    }
				return false;
			}
		});
	}
    public interface OnCancelBeforeListener {
        void onCancelBefore();
    }
	public TextView getF_1_on() {
		return f_1_on;
	}

	public void setF_1_on(TextView f_1_on) {
		this.f_1_on = f_1_on;
	}

	public TextView getF_1_off() {
		return f_1_off;
	}

	public void setF_1_off(TextView f_1_off) {
		this.f_1_off = f_1_off;
	}

	public TextView getF_2_on() {
		return f_2_on;
	}

	public void setF_2_on(TextView f_2_on) {
		this.f_2_on = f_2_on;
	}

	public TextView getF_2_off() {
		return f_2_off;
	}

	public void setF_2_off(TextView f_2_off) {
		this.f_2_off = f_2_off;
	}

	public TextView getF_3_on() {
		return f_3_on;
	}

	public void setF_3_on(TextView f_3_on) {
		this.f_3_on = f_3_on;
	}

	public TextView getF_3_off() {
		return f_3_off;
	}

	public void setF_3_off(TextView f_3_off) {
		this.f_3_off = f_3_off;
	}

	public TextView getFactory_tips() {
		return factory_tips;
	}

	public void setFactory_tips(TextView factory_tips) {
		this.factory_tips = factory_tips;
	}
	
}
