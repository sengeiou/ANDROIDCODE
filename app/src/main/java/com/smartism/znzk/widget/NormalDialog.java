package com.smartism.znzk.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.p2p.core.utils.MyUtils;
import com.smartism.znzk.R;
import com.smartism.znzk.adapter.camera.SelectorDialogAdapter;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.Utils;

import java.util.Timer;
import java.util.TimerTask;

public class NormalDialog {
	Context context;
	String[] list_data = new String[] {};
	String title_str, content_str, btn1_str, btn2_str,btn3_str;
	AlertDialog dialog;
	private int loadingMark;
	private OnButtonOkListener onButtonOkListener;
	private OnButtonCancelListener onButtonCancelListener;
	private OnCancelListener onCancelListener;
	private DialogInterface.OnKeyListener onKeyListener;
	private OnCustomCancelListner customCancelListner;
	private OnDismissListener onDismissListener = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {
			if (mTimer != null) {
				mTimer.cancel();
			}
		}
	};
	private OnItemClickListener onItemClickListener;
	private int style = 999;

	public static final int DIALOG_STYLE_NORMAL = 1;
	public static final int DIALOG_STYLE_LOADING = 2;
	public static final int DIALOG_STYLE_UPDATE = 3;
	public static final int DIALOG_STYLE_DOWNLOAD = 4;
	public static final int DIALOG_STYLE_PROMPT = 5;

	public NormalDialog(Context context, String title, String content,
						String btn1, String btn2) {
		this.context = context;
		this.title_str = title;
		this.content_str = content;
		this.btn1_str = btn1;
		this.btn2_str = btn2;
	}

	public NormalDialog(Context context) {
		this.context = context;
		this.title_str = "";
		this.content_str = "";
		this.btn1_str = "";
		this.btn2_str = "";
	}

	public void showDialog() {
		switch (style) {
			case DIALOG_STYLE_NORMAL:
				showNormalDialog();
				break;
			case DIALOG_STYLE_PROMPT:
				showPromptDialog();
				break;
			case DIALOG_STYLE_LOADING:
				showLoadingDialog();
				break;
			default:
				showNormalDialog();
				break;
		}
	}

	public void showLoadingDialog() {
		Log.i("摄像头", "dialog1");
		View view = LayoutInflater.from(context).inflate(
				R.layout.dialog_loading, null);

		TextView title = (TextView) view.findViewById(R.id.title_text);
		title.setText(title_str);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		dialog = builder.create();
		dialog.show();
		dialog.setContentView(view);
		/*FrameLayout.LayoutParams layout = (LayoutParams) view.getLayoutParams();
		layout.width = (int) context.getResources().getDimension(
				R.dimen.Loading_dialog2_width);
		view.setLayoutParams(layout);*/
		dialog.setOnCancelListener(onCancelListener);
		dialog.setCanceledOnTouchOutside(false);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		window.setGravity(Gravity.CENTER);
		//显示的坐标
		/*lp.x = context.get;
		lp.y = 50;*/
		lp.width= context.getResources().getDimensionPixelOffset(R.dimen.Loading_dialog_width);
		dialog.setOnKeyListener(onKeyListener);
		dialog.setOnCancelListener(onCancelListener);
		dialog.setOnDismissListener(onDismissListener);
		window.setAttributes(lp);
		window.setWindowAnimations(R.style.dialog_normal);
	}

	public void setOnKeyListener(DialogInterface.OnKeyListener onKeyListener) {
		this.onKeyListener = onKeyListener;
	}

	public void showAboutDialog() {
		Log.i("摄像头", "dialog2");
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_about,
				null);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (null != dialog) {
					dialog.dismiss();
				}
			}

		});
		TextView txVersion = (TextView) view.findViewById(R.id.tv_about);
		txVersion.setText(MyUtils.getVersion(context));
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		dialog = builder.create();
		dialog.show();
		dialog.setContentView(view);
		LayoutParams layout = (LayoutParams) view.getLayoutParams();
		layout.width = (int) context.getResources().getDimension(
				R.dimen.about_dialog_width);
		view.setLayoutParams(layout);
		dialog.setOnCancelListener(onCancelListener);
		dialog.setCanceledOnTouchOutside(true);
		Window window = dialog.getWindow();
		window.setWindowAnimations(R.style.dialog_normal);
	}

	public void showDeviceInfoDialog(String curversion, String uBootVersion,
									 String kernelVersion, String rootfsVersion) {
		Log.i("摄像头", "dialog3");
		View view = LayoutInflater.from(context).inflate(
				R.layout.dialog_device_info, null);
		TextView text_curversion = (TextView) view
				.findViewById(R.id.text_curversion);
		TextView text_uBootVersion = (TextView) view
				.findViewById(R.id.text_uBootVersion);
		TextView text_kernelVersion = (TextView) view
				.findViewById(R.id.text_kernelVersion);
		TextView text_rootfsVersion = (TextView) view
				.findViewById(R.id.text_rootfsVersion);
		text_curversion.setText(curversion);
		text_uBootVersion.setText(uBootVersion);
		text_kernelVersion.setText(kernelVersion);
		text_rootfsVersion.setText(rootfsVersion);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (null != dialog) {
					dialog.dismiss();
				}
			}

		});
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		dialog = builder.create();
		dialog.show();
		dialog.setContentView(view);
		dialog.setOnCancelListener(onCancelListener);
		dialog.setCanceledOnTouchOutside(true);
		Window window = dialog.getWindow();
		window.setWindowAnimations(R.style.dialog_normal);
	}

	public void showLoadingDialog2() {
		View view = LayoutInflater.from(context).inflate(
				R.layout.dialog_loading2, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		dialog = builder.create();
		dialog.show();
		dialog.setContentView(view);
		/*FrameLayout.LayoutParams layout = (LayoutParams) view.getLayoutParams();
		layout.width = (int) context.getResources().getDimension(
				R.dimen.Loading_dialog2_width);
		view.setLayoutParams(layout);*/
		dialog.setOnCancelListener(onCancelListener);
		dialog.setCanceledOnTouchOutside(false);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		window.setGravity(Gravity.CENTER);
		//显示的坐标
		/*lp.x = context.get;
		lp.y = 50;*/
		window.setAttributes(lp);
		window.setWindowAnimations(R.style.dialog_normal);
	}

	public void showLoadingDialog3() {
		Log.i("摄像头", "dialog5");
		View view = LayoutInflater.from(context).inflate(
				R.layout.dialog_loading3, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		dialog = builder.create();
		dialog.show();
		dialog.setContentView(view);
		/*FrameLayout.LayoutParams layout = (LayoutParams) view.getLayoutParams();
		layout.width = (int) context.getResources().getDimension(
				R.dimen.Loading_dialog2_width);
		view.setLayoutParams(layout);*/
		dialog.setOnCancelListener(onCancelListener);
		dialog.setCanceledOnTouchOutside(false);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		window.setGravity(Gravity.CENTER);

		//显示的坐标
		/*lp.x = context.get;
		lp.y = 50;*/
		lp.width= context.getResources().getDimensionPixelOffset(R.dimen.Loading_dialog2_widths);
		lp.height = context.getResources().getDimensionPixelOffset(R.dimen.loading_dialog2_content_heights);
		window.setAttributes(lp);
		window.setWindowAnimations(R.style.dialog_normal);
	}

	public void showNormalDialog() {
		Log.i("摄像头", "dialog6");
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_normal, null);
		TextView title = (TextView) view.findViewById(R.id.title_text);
		TextView content = (TextView) view.findViewById(R.id.content_text);
		TextView button1 = (TextView) view.findViewById(R.id.button1_text);
		TextView button2 = (TextView) view.findViewById(R.id.button2_text);
		title.setText(title_str);
		content.setText(content_str);
		button1.setText(btn1_str);
		button2.setText(btn2_str);
		button1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != dialog) {
					dialog.dismiss();
				}
				onButtonOkListener.onClick();
			}
		});
		button2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null == onButtonCancelListener) {
					if (null != dialog) {
						dialog.cancel();
					}
				} else {
					onButtonCancelListener.onClick();
				}
			}
		});
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		dialog = builder.create();
		if (dialog.isShowing()) {
			dialog.dismiss();
		}
		dialog.show();
		dialog.setContentView(view);
		/*FrameLayout.LayoutParams layout = (LayoutParams) view.getLayoutParams();
		layout.width = (int) context.getResources().getDimension(
				R.dimen.normal_dialog_width);

		view.setLayoutParams(layout);*/
		dialog.setCanceledOnTouchOutside(true);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		window.setGravity(Gravity.CENTER);

//		int width = context.getResources().getDimensionPixelOffset(R.dimen.normal_dialog_width);
//		int height = context.getResources().getDimensionPixelOffset(R.dimen.slidingmenu_offset);
		//dialog的大小
//		lp.width = width;
//		lp.height = height;
//		window.setAttributes(lp);
		window.setWindowAnimations(R.style.dialog_normal);
	}

	public void showSelectorDialog() {
		Log.i("摄像头", "dialog7");
		View view = LayoutInflater.from(context).inflate(
				R.layout.dialog_selector, null);
		TextView title = (TextView) view.findViewById(R.id.title_text);
		title.setText(title_str);

		ListView content = (ListView) view.findViewById(R.id.content_text);

		SelectorDialogAdapter adapter = new SelectorDialogAdapter(context,
				list_data);
		content.setAdapter(adapter);
		content.setOnItemClickListener(onItemClickListener);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		dialog = builder.create();
		dialog.show();
		dialog.setContentView(view);

		int itemHeight = (int) context.getResources().getDimension(
				R.dimen.selector_dialog_item_height);
		int margin = (int) context.getResources().getDimension(
				R.dimen.selector_dialog_margin);
		int separatorHeight = (int) context.getResources().getDimension(
				R.dimen.selector_dialog_separator_height);

		LayoutParams layout = (LayoutParams) view.getLayoutParams();
		layout.width = (int) context.getResources().getDimension(
				R.dimen.selector_dialog_width);
		layout.height = itemHeight * list_data.length + margin * 2
				+ (list_data.length - 1) * separatorHeight;
		view.setLayoutParams(layout);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setCancelable(true);
		Window window = dialog.getWindow();
		window.setWindowAnimations(R.style.dialog_normal);
	}

	public String getContentStr(){
		return content_str;
	}

	public void setContentStr(int id){
		String str=Utils.getStringForId(id);
		setContentStr(str);
	}


	public void setContentStr(String contantStr){
		content_str=contantStr;
	}


	public String getbtnStr1(){
		return btn1_str;
	}

	public void setbtnStr1(int id){
		String str=Utils.getStringForId(id);
		setbtnStr1(str);
	}


	public void setbtnStr1(String btn1_st){
		btn1_str=btn1_st;
	}

	public String getbtnStr2(){
		return btn2_str;
	}

	public void setbtnStr2(int id){
		String str=Utils.getStringForId(id);
		setbtnStr2(str);
	}


	public void setbtnStr2(String btn2_st){
		btn2_str=btn2_st;
	}

	public String getbtnStr3(){
		return btn3_str;
	}

	public void setbtnStr3(int id){
		String str=Utils.getStringForId(id);
		setbtnStr3(str);
	}


	public void setbtnStr3(String btn3_st){
		btn3_str=btn3_st;
	}

	public void showPromptDialog() {
		Log.i("摄像头", "dialog8");
		View view = LayoutInflater.from(context).inflate(
				R.layout.dialog_prompt, null);
		TextView content = (TextView) view.findViewById(R.id.content_text);
		TextView title = (TextView) view.findViewById(R.id.title_text);
		TextView button2 = (TextView) view.findViewById(R.id.button2_text);
		content.setText(content_str);
		title.setText(title_str);
		button2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null == onButtonCancelListener) {
					if (null != dialog) {
						dialog.dismiss();
					}
				} else {
					onButtonCancelListener.onClick();
				}
			}
		});
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		dialog = builder.create();
		dialog.show();
		dialog.setContentView(view);
		LayoutParams layout = (LayoutParams) view.getLayoutParams();
		layout.width = (int) context.getResources().getDimension(
				R.dimen.normal_dialog_width);

		view.setLayoutParams(layout);
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(true);
		Window window = dialog.getWindow();
		window.setWindowAnimations(R.style.dialog_normal);
	}

	public void showPromoptDiaglog() {
		Log.i("摄像头", "dialog9");
		View view = LayoutInflater.from(context).inflate(
				R.layout.dialog_promopt_box2, null);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (null != dialog) {
					dialog.dismiss();
				}
			}

		});
		Button bt1 = (Button) view.findViewById(R.id.bt_determine);
		bt1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (null != dialog) {
					dialog.dismiss();
				}
			}
		});
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		dialog = builder.create();
		dialog.show();
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(true);
		LayoutParams layout = (LayoutParams) view.getLayoutParams();
		layout.width = (int) context.getResources().getDimension(
				R.dimen.dialog_promopt_width);
		layout.height = (int) context.getResources().getDimension(
				R.dimen.dialog_promopt_height);
		view.setLayoutParams(layout);
		Window window = dialog.getWindow();
		window.setWindowAnimations(R.style.dialog_normal);
	}

	public void showWaitConnectionDialog() {
		Log.i("摄像头", "dialog10");
		View view = LayoutInflater.from(context).inflate(
				R.layout.dialog_wait_connection, null);
		// view.setOnClickListener(new OnClickListener(){
		//
		// @Override
		// public void onClick(View arg0) {
		// // TODO Auto-generated method stub
		// if (null!=dialog) {
		// dialog.dismiss();
		// }
		// }
		//
		// });
		ImageView anim_load = (ImageView) view.findViewById(R.id.anim_wait);
		AnimationDrawable animationdrawable = (AnimationDrawable) anim_load
				.getDrawable();
		animationdrawable.start();
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		dialog = builder.create();
		dialog.show();
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(false);
		LayoutParams layout = (LayoutParams) view.getLayoutParams();
		layout.width = (int) context.getResources().getDimension(
				R.dimen.dialog_remind_width);
		layout.height = (int) context.getResources().getDimension(
				R.dimen.dialog_reming_height);
		view.setLayoutParams(layout);
		Window window = dialog.getWindow();
		window.setWindowAnimations(R.style.dialog_normal);
	}

	public void showQRcodehelp() {
		View view = LayoutInflater.from(context).inflate(R.layout.dialog_help,
				null);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (null != dialog) {
					dialog.dismiss();
				}
			}

		});
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		dialog = builder.create();
		dialog.show();
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(true);
		LayoutParams layout = (LayoutParams) view.getLayoutParams();
		layout.width = (int) context.getResources().getDimension(
				R.dimen.dialog_remind_width);
		layout.height = (int) context.getResources().getDimension(
				R.dimen.dialog_reming_height);
		view.setLayoutParams(layout);
		Window window = dialog.getWindow();
		window.setWindowAnimations(R.style.dialog_normal);
	}

	public void successDialog() {
		View view = LayoutInflater.from(context).inflate(
				R.layout.dialog_success, null);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (null != dialog) {
					dialog.dismiss();
				}
			}

		});
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		dialog = builder.create();
		dialog.show();
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(true);
		LayoutParams layout = (LayoutParams) view.getLayoutParams();
		layout.width = (int) context.getResources().getDimension(
				R.dimen.dialog_success_width);
		layout.height = (int) context.getResources().getDimension(
				R.dimen.dialog_success_height);
		view.setLayoutParams(layout);
		Window window = dialog.getWindow();
		window.setWindowAnimations(R.style.dialog_normal);
	}

	public void faildDialog() {
		View view = LayoutInflater.from(context).inflate(
				R.layout.dialog_prompt_box1, null);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (null != dialog) {
					dialog.dismiss();
				}
			}

		});
		Button bt1 = (Button) view.findViewById(R.id.bt_determine);
		bt1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (dialog != null) {
					dialog.dismiss();
				}

			}
		});
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		dialog = builder.create();
		dialog.show();
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(true);
		LayoutParams layout = (LayoutParams) view.getLayoutParams();
		layout.width = (int) context.getResources().getDimension(
				R.dimen.dialog_promopt_width);
		layout.height = (int) context.getResources().getDimension(
				R.dimen.dialog_promopt_height);
		view.setLayoutParams(layout);
		Window window = dialog.getWindow();
		window.setWindowAnimations(R.style.dialog_normal);
	}

	public void showConnectFail() {
		View view = LayoutInflater.from(context).inflate(
				R.layout.dialog_connect_failed, null);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// if (null!=dialog) {
				// dialog.dismiss();
				// }
			}

		});
		Button try_again = (Button) view.findViewById(R.id.try_again);
		Button use_qrecode = (Button) view.findViewById(R.id.try_qrecode);
		try_again.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (null != dialog) {
					dialog.dismiss();
				}
			}
		});
		use_qrecode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// 跳转到二维码添加
				/*if (null != dialog) {
					dialog.dismiss();
					onButtonCancelListener.onClick();
				}
				context.startActivity(new Intent(context, QRcodeActivity.class));*/
			}
		});
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		dialog = builder.create();
		dialog.show();
		dialog.setContentView(view);
		dialog.setCanceledOnTouchOutside(false);
//		LayoutParams layout = (LayoutParams) view.getLayoutParams();
//		layout.width = (int) context.getResources().getDimension(
//				R.dimen.dialog_remind_width);
//		layout.height = (int) context.getResources().getDimension(
//				R.dimen.dialog_reming_height);
//		view.setLayoutParams(layout);
		Window window = dialog.getWindow();
		window.setWindowAnimations(R.style.dialog_normal);

	}

	/**
	 * 报警弹窗
	 * @param surrportDelete 是否支持解绑
	 * @param id 报警ID
	 */
	public void showAlarmDialog(final boolean surrportDelete,final String id){
		View view = LayoutInflater.from(context).inflate(
				R.layout.dialog_alarm, null);
		TextView content = (TextView) view.findViewById(R.id.content_text);
		TextView button1 = (TextView) view.findViewById(R.id.button1_text);
		TextView button2 = (TextView) view.findViewById(R.id.button2_text);
		TextView btnDelete = (TextView) view.findViewById(R.id.button3_text);
		ImageView iv=(ImageView) view.findViewById(R.id.iv_line_shu);
		content.setText(content_str);
		button1.setText(btn1_str);
		button2.setText(btn2_str);
		if(surrportDelete){
			btnDelete.setVisibility(View.VISIBLE);
			iv.setVisibility(View.VISIBLE);
			btnDelete.setText(btn3_str);
		}else{
			btnDelete.setVisibility(View.GONE);
			iv.setVisibility(View.GONE);
		}

		button1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(AlarmClickListner!=null){
					AlarmClickListner.onOkClick(id, surrportDelete, dialog);
				}else{
					if (null != dialog) {
						dialog.cancel();
					}
				}
			}
		});
		button2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(AlarmClickListner!=null){
					AlarmClickListner.onCancelClick(id, surrportDelete, dialog);
				}else{
					if (null != dialog) {
						dialog.cancel();
					}
				}
			}
		});
		btnDelete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(AlarmClickListner!=null){
					AlarmClickListner.onDeleteClick(id, surrportDelete, dialog);
				}else{
					if (null != dialog) {
						dialog.cancel();
					}
				}

			}
		});
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		dialog = builder.create();
		if (dialog.isShowing()) {
			dialog.dismiss();
		}
		dialog.show();
		dialog.setContentView(view);
		LayoutParams layout = (LayoutParams) view.getLayoutParams();
		layout.width = (int) context.getResources().getDimension(
				R.dimen.normal_dialog_width);
		view.setLayoutParams(layout);
		dialog.setCanceledOnTouchOutside(true);
		Window window = dialog.getWindow();
		window.setWindowAnimations(R.style.dialog_normal);
	}

	public void setTitle(String title) {
		this.title_str = title;
	}

	public void setTitle(int id) {
		this.title_str = context.getResources().getString(id);
	}

	public void setListData(String[] data) {
		this.list_data = data;
	}

	public void setCanceledOnTouchOutside(boolean bool) {
		dialog.setCanceledOnTouchOutside(bool);
	}

	public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	public void setCancelable(boolean bool) {
		dialog.setCancelable(bool);
	}

	public void cancel() {
		dialog.cancel();
	}

	public void dismiss() {
		if (dialog != null) {
			dialog.dismiss();
		}
	}

	public boolean isShowing() {
		return dialog.isShowing();
	}

	public void setBtnListener(TextView btn1, TextView btn2) {

	}

	public void setStyle(int style) {
		this.style = style;
	}

	public interface OnButtonOkListener {
		public void onClick();
	}

	public interface OnButtonCancelListener {
		public void onClick();
	}

	public void setOnButtonOkListener(OnButtonOkListener onButtonOkListener) {
		this.onButtonOkListener = onButtonOkListener;
	}

	public void setOnButtonCancelListener(
			OnButtonCancelListener onButtonCancelListener) {
		this.onButtonCancelListener = onButtonCancelListener;
	}

	public void setOnCancelListener(OnCancelListener onCancelListener) {
		this.onCancelListener = onCancelListener;
		Log.i("dxsSMTP", "setlistener");
	}

	// 设置对话框超时
	private TimerTask reAddTask;
	private Timer mTimer;

	private Handler.Callback mCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			if(dialog!=null&&dialog.isShowing()){
				dialog.dismiss();
			}
			TimeOutListner.onTimeOut();
			return true;
		}
	};
	private Handler timeOutHandler = new WeakRefHandler(mCallback);


	public void setTimeOut(long delay) {
		mTimer = new Timer();
		reAddTask = new TimerTask() {
			@Override
			public void run() {
				timeOutHandler.sendEmptyMessage(0);
			}
		};
		mTimer.schedule(reAddTask, delay);
	}

	private OnNormalDialogTimeOutListner TimeOutListner;

	public interface OnNormalDialogTimeOutListner {
		public void onTimeOut();
	}

	public void setOnNormalDialogTimeOutListner(
			OnNormalDialogTimeOutListner TimeOutListner) {
		this.TimeOutListner = TimeOutListner;
	}

	// 取消自定义监听
	public interface OnCustomCancelListner {
		void onCancle(int mark);
	}

	public void setOnCustomCancelListner(
			OnCustomCancelListner customCancelListner) {
		this.customCancelListner = customCancelListner;
	}

	public void setLoadingMark(int loadingMark) {
		this.loadingMark = loadingMark;
	}
	/**
	 * 报警弹窗
	 * @author Administrator
	 *
	 */
	public interface OnAlarmClickListner{
		void onCancelClick(String alarmID, boolean isSurportDelete, Dialog dialog);
		void onOkClick(String alarmID, boolean isSurportDelete, Dialog dialog);
		void onDeleteClick(String alarmID, boolean isSurportDelete, Dialog dialog);
	}
	private OnAlarmClickListner AlarmClickListner;
	public void setOnAlarmClickListner(
			OnAlarmClickListner Listner) {
		this.AlarmClickListner = Listner;
	}

}
