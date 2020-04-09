package com.smartism.znzk.view;


import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.smartism.znzk.R;

/**
 * 主机搜索提示
 * @author 王建
 * 2015年9月30日
 */
public class DeviceSearchPopupWindow extends PopupWindow {

	private View mView;
	private TextView dl_search_zj_tips;
	private ImageView dl_search_zj_bg,dl_search_zj_btn;
	private Button dl_search_zj_setbtn;


	public DeviceSearchPopupWindow(final Activity context,OnClickListener itemsOnClick) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mView = inflater.inflate(R.layout.activity_devices_list_searchzhuji, null);
		dl_search_zj_tips = (TextView) mView.findViewById(R.id.dl_searchzj_tips);
		dl_search_zj_bg = (ImageView) mView.findViewById(R.id.dl_searchzj_bg);
		dl_search_zj_btn = (ImageView) mView.findViewById(R.id.dl_searchzj_btn);
		dl_search_zj_setbtn = (Button) mView.findViewById(R.id.dl_searchzj_set);
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
	}
	public TextView getDl_search_zj_tips() {
		return dl_search_zj_tips;
	}
	
	public ImageView getDl_search_zj_bg() {
		return dl_search_zj_bg;
	}
	
	public ImageView getDl_search_zj_btn() {
		return dl_search_zj_btn;
	}
	public Button getDl_search_zj_setbtn() {
		return dl_search_zj_setbtn;
	}
	
}
