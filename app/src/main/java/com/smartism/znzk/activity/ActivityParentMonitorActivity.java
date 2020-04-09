package com.smartism.znzk.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.camera.BaseMonitorActivity;
import com.smartism.znzk.activity.user.LoginActivity;
import com.smartism.znzk.activity.view.LockActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.camera.P2PConnect;
import com.smartism.znzk.communication.service.CoreService;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.ProgressCycleView;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

/*****
 * 为摄像头播放增加的一个基类
 * @author 王建  2016年08月24日
 *
 */
public class ActivityParentMonitorActivity extends BaseMonitorActivity {
	protected final String TAG = "jdm";
	public ActivityParentMonitorActivity mContext;
	public ProgressDialog mProgressDialog = null;
	protected AlertDialog mProgressBarDialog = null;
	public ProgressCycleView mProgressCycleView = null;
	protected DataCenterSharedPreferences dcsp = null;
	protected Animation imgloading_animation;
	//public LockScreenReceiver LockscreenReceiver;
	public ActivityManager activityManager;
	//    public int Pre_priority;
//    public int End_priority;
	protected boolean showLock = true;//是否显示手势密码
	private Intent startIntent = null;

	private Handler.Callback mCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			return false;
		}
	};
	public Handler mHandler = new WeakRefHandler(mCallback);

	protected MainApplication getJdmApplication() {
		return (MainApplication) getApplication();
	}

	protected String getJdmVersionType() {
		return getJdmApplication().getAppGlobalConfig().getVersion();
	}

	protected String getJdmVersionPrefix() {
		return getJdmApplication().getAppGlobalConfig().getVersionPrefix();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		startIntent = new Intent(getIntent());
		DeviceInfo deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
		DeviceInfo deviceInfo2 = (DeviceInfo) startIntent.getSerializableExtra("device");
//        Pre_priority = getPriority(getApplicationContext());
		dcsp = DataCenterSharedPreferences.getInstance(ActivityParentMonitorActivity.this,
				Constant.CONFIG);
		imgloading_animation = AnimationUtils.loadAnimation(ActivityParentMonitorActivity.this, R.anim.loading_revolve);
		imgloading_animation.setInterpolator(new LinearInterpolator());

//		LockscreenReceiver = new LockScreenReceiver();
//		//创建一个IntentFilter对象，来封装事件类型
//		IntentFilter intentFilter = new IntentFilter();
//		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
//		//intentFilter.addAction(Intent.ACTION_SCREEN_ON);
//		intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
//		registerReceiver(LockscreenReceiver, intentFilter);
	}

	/**
	 * 显示进度条
	 *
	 * @param text
	 * @param bIndeterminate false 未知进度的进度条， true 知道进度的进度条
	 * @param bCancelable    true 返回按钮可以退出 false 返回按钮不能退出
	 */
	public void showInProgress(String text, boolean bIndeterminate, boolean bCancelable) {
		synchronized (this) {
			if (mProgressDialog == null) {
				mProgressDialog = new ProgressDialog(this);
				mProgressDialog.setOnCancelListener(new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						// mProgressDialog = null; //当连续关闭打开时会导致第二个为null 而退出不了
					}
				});
				mProgressDialog.setMessage(text);
				mProgressDialog.setIndeterminate(bIndeterminate);
				mProgressDialog.setCancelable(bCancelable);
			}
			mProgressDialog.show();
		}
	}

	/**
	 * 显示进度条
	 *
	 * @param text
	 */
	public void showInProgress(String text) {
		showInProgress(text,false,true);
	}

	/**
	 * 显示带进度的进度条
	 *
	 * @param text
	 * @param bCancelable false 未知进度的进度条， true 知道进度的进度条
	 * @param bCancelable true 返回按钮可以退出 false 返回按钮不能退出
	 */
	public void showOrUpdateProgressBar(String text, boolean bCancelable, int progress, int max) {
		synchronized (this) {
			if (mProgressBarDialog == null || mProgressCycleView == null || !mProgressBarDialog.isShowing()) {
				mProgressBarDialog = new AlertDialog.Builder(ActivityParentMonitorActivity.this).create();
				View contentView = LayoutInflater.from(this).inflate(R.layout.activity_devices_list_uprogress, null);
				mProgressCycleView = (ProgressCycleView) contentView.findViewById(R.id.zj_update_probar);
				// mProgressBar = (ProgressBar)
				// contentView.findViewById(R.id.zj_update_progressBar);
				TextView title = (TextView) contentView.findViewById(R.id.progress_title);
				// mProgressTotal = (TextView)
				// contentView.findViewById(R.id.progress_total);
				// mProgressPro = (TextView)
				// contentView.findViewById(R.id.progress_key);
				title.setText(text);
				mProgressBarDialog.setTitle(text);
				mProgressBarDialog.setCancelable(bCancelable);
				mProgressBarDialog.show();
				mProgressBarDialog.setContentView(contentView);
			}
			// mProgressTotal.setText(String.valueOf(max));
			// mProgressBar.setMax(max);
			// mProgressPro.setText(String.valueOf(progress));
			// mProgressBar.setProgress(progress);
			mProgressCycleView.setMax(max);
			mProgressCycleView.setProgress(progress);
		}
	}

	/**
	 * 结束进度条
	 */
	public void cancelInProgress() {
		synchronized (this) {
			if (mProgressDialog != null) {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}
				mProgressDialog = null;
				mProgressCycleView = null;
				// mProgressBar = null;
			}
		}
	}

	/**
	 * 结束进度条
	 */
	public void cancelInProgressBar() {
		synchronized (this) {
			if (mProgressBarDialog != null) {
				mProgressBarDialog.dismiss();
				mProgressBarDialog = null;
			}
		}
	}

	/**
	 * 进度条是否是展示状态
	 *
	 * @return
	 */
	public boolean progressIsShowing() {
		return mProgressDialog != null && mProgressDialog.isShowing();
	}


	public DataCenterSharedPreferences getDcsp() {
		return dcsp;
	}

	public class ImageLoadingBar implements ImageLoadingListener {

		@Override
		public void onLoadingCancelled(String arg0, View arg1) {
			if (arg1 != null) {
				arg1.clearAnimation();
			}
		}

		@Override
		public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
			arg1.clearAnimation();
		}

		@Override
		public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
			arg1.clearAnimation();
		}

		@Override
		public void onLoadingStarted(String arg0, View arg1) {
			arg1.startAnimation(imgloading_animation);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		needBack = false;
		MobclickAgent.onPause(this);
	}

	@Override
	public void onDestroy() {
		if (mProgressDialog != null) {
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			mProgressDialog = null;
			mProgressCycleView = null;
		}
		if (mProgressBarDialog != null) {
			mProgressBarDialog.dismiss();
			mProgressBarDialog = null;
		}

		//unregisterReceiver(LockscreenReceiver);
		super.onDestroy();
	}

	public void logout() {
        JavaThreadPool.getInstance().excute(new ExitLogin());
		// 跳转到登录页面，并且需要清空activity栈
		Intent refreshContans = new Intent();
		refreshContans.setAction(Constants.Action.ACTIVITY_FINISH);
		sendBroadcast(refreshContans);
		Intent loginIntent = new Intent();
		loginIntent.setClass(this, CoreService.class);
		stopService(loginIntent);
		loginIntent.addFlags(
				Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		loginIntent.setClass(this, LoginActivity.class);
		startActivity(loginIntent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		//Log.e("aaa", "是否执行  resume");
		MobclickAgent.onResume(this);
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
		showLook();
//            }
//        },200); //延迟200毫秒的原因：7.0以上系统在解锁之后调用showLook方法时，isKeyguardLocked判断还是会为true这是解锁页面尚未完全销毁导致的，延迟一点点执行。
	}

	@Override
	public void onStop() {
		super.onStop();
//        End_priority = getPriority(getApplicationContext());
//        if (Pre_priority != End_priority) {
//            dcsp.putBoolean(Constant.IS_LOOKS, true).commit();
//        }
//		ActivityManager a = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		//5.0以上的手机这个条件不成立
//		if (!getPackageName().equals(a.getRunningTasks(1).get(0).topActivity.getPackageName())) {
//			dcsp.putBoolean(Constant.IS_LOOKS, true).commit();
//		}
	}


	//来电广播，关闭屏幕的广播 ，判断是否开启锁屏
//	public class LockScreenReceiver extends BroadcastReceiver {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			dcsp.putBoolean(Constant.IS_LOOKS, true).commit();
//		}
//	}

	public int getPriority(Context context) {
		int priority = 0;
		activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(context.getPackageName())) {
				priority = appProcess.importance;
			}
		}
		return priority;
	}

	/**
	 * 强制关闭软键盘
	 */
	public void hideKeyboard(Activity activity) {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
		if (imm.isActive() && activity.getCurrentFocus() != null) {
			if (activity.getCurrentFocus().getWindowToken() != null) {
				imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}
	}

	@Override
	protected void onP2PViewSingleTap() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onP2PViewFilling() {

	}

	@Override
	protected void turnCamera() {

	}

	@Override
	protected void onCaptureScreenResult(boolean isSuccess, int prePoint) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onVideoPTS(long videoPTS) {

	}

    @Override
    public int getActivityInfo() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected void onGoBack() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onGoFront() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onExit() {
        // TODO Auto-generated method stub

    }

	/**
	 * todo 展示手势密码
	 * 展示手势密码
	 */
	//// TODO: 16/9/2 展示手势密码
	public void showLook() {
//        KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//        boolean flag = mKeyguardManager.isKeyguardLocked();
		if (!showLock) return;
		//判断是否登陆
		if (dcsp.getBoolean(DataCenterSharedPreferences.Constant.IS_LOGIN, false)) {
			//手势密码功能开关打开      并且已经设置好密码
			if (dcsp.getBoolean(DataCenterSharedPreferences.Constant.IS_APP_GENSTURE, false) && (!TextUtils.isEmpty(dcsp.getString(DataCenterSharedPreferences.Constant.CODE_GENSTURE, "")))) {
				//判断在页面可见情况下是否锁屏
				if (dcsp.getBoolean(DataCenterSharedPreferences.Constant.IS_LOOKS, false)) {
//                    if (!flag) {
					Intent intent = new Intent();
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
					intent.setClass(this, LockActivity.class);
					startActivity(intent);
					overridePendingTransition(R.anim.activity_in_alpha,R.anim.activity_out_alpha);
//                    }
				}
			}
		}
	}

	/**
	 * 会不会显示手势密码
	 * @return true 会 false 不会
	 */
	public boolean isShowLook() {
		KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
		boolean flag = mKeyguardManager.isKeyguardLocked();
		if (!showLock) return false;
		//判断是否登陆
		if (dcsp.getBoolean(DataCenterSharedPreferences.Constant.IS_LOGIN, false)) {
			//手势密码功能开关打开      并且已经设置好密码
			if (dcsp.getBoolean(DataCenterSharedPreferences.Constant.IS_APP_GENSTURE, false) && (!TextUtils.isEmpty(dcsp.getString(DataCenterSharedPreferences.Constant.CODE_GENSTURE, "")))) {
				//判断在页面可见情况下是否锁屏
				if (dcsp.getBoolean(DataCenterSharedPreferences.Constant.IS_LOOKS, false)) {
					if (!flag) {
						return true;
					}
				}
			}
		}
		return false;
	}

	boolean startNewActivityToFinish = false; //此变量表示是否是新打开一个activity而结束自己的，如果是则不打开需要返回的页面。
	boolean needBack = true; //播放页面返回 默认是需要启用上一个播放页面的
	@Override
	public void startActivity(Intent intent) {
		intent.putExtra("back",this.getLocalClassName());
		MainApplication.app.getBackNeedStartActivtyMap().put(this.getLocalClassName(), startIntent);
		startNewActivityToFinish = true;
		super.startActivity(intent);
	}

	/**
	 * 摄像头的子类如果需要调用startActivityForResult方法来做一些事情，请使用此方法来调用
	 * 否则可能引起错误，因为此页面在不可见时会finish，所以需要由此类的上级页面进行重启，需要传递参数
	 * @param intent
	 * @param requestCode
	 */
	public void startActivityForResultNew(Intent intent, int requestCode) {
		intent.putExtra("back", this.getLocalClassName());
		startIntent.putExtra("requestCode", requestCode);
		MainApplication.app.getBackNeedStartActivtyMap().put(this.getLocalClassName(), startIntent);
		startNewActivityToFinish = true;
		super.startActivity(intent);
	}

	@Override
	public void finish() {
		P2PConnect.setPlayingContact(null);
		super.finish();
		if (getIntent().getStringExtra("back")!=null && !startNewActivityToFinish && needBack){
			Intent intent = MainApplication.app.getBackNeedStartActivtyMap().get(getIntent().getStringExtra("back"));
			if (intent!=null && !this.getLocalClassName().equals(intent.getComponent().getClassName())){//如果要重启自己则不通过
				super.startActivity(intent);
				getIntent().removeExtra("back");//有些页面安分会调用多次finish
				overridePendingTransition(R.anim.activity_in_alpha,R.anim.activity_out_alpha);
			}
		}
	}
    private class ExitLogin implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            String result = HttpRequestUtils
                    .requestoOkHttpPost(server + "/jdm/s3/u/logout", null, mContext);
            Util.clearLoginInfo(getApplicationContext(), dcsp); //清空用户登录信息
        }
    }
}
