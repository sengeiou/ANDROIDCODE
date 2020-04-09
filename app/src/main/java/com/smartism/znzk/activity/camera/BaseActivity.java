package com.smartism.znzk.activity.camera;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.p2p.core.BaseCoreActivity;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.view.LockActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.umeng.analytics.MobclickAgent;


public abstract class BaseActivity extends BaseCoreActivity {
	protected boolean showLock = true;
	public ProgressDialog mProgressDialog = null;
	protected int resultCode = 0; //返回时，如果需要带值回父窗体，这里带
	protected Intent resultData = null;//带值回去，这个暂未实现
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
		super.onCreate(savedInstanceState, persistentState);
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
				mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						// mProgressDialog = null; //当连续关闭打开时会导致第二个为null 而退出不了
					}
				});
				mProgressDialog.setMessage(text);
				mProgressDialog.setIndeterminate(bIndeterminate);
				mProgressDialog.setCancelable(bCancelable);
			}
			if (!mProgressDialog.isShowing()) {
				mProgressDialog.show();
			}
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
				// mProgressBar = null;
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mProgressDialog != null) {
			if (mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			mProgressDialog = null;
		}
	}

	@Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        showLook();
    }

    @Override
	protected void onGoBack() {
		// TODO Auto-generated method stub
		//MainApplication.app.showNotification();
	}

	@Override
	protected void onGoFront() {
		// TODO Auto-generated method stub
		//MainApplication.app.hideNotification();
	}

	@Override
	protected void onExit() {
		// TODO Auto-generated method stub
		//MainApplication.app.hideNotification();
	}

	public abstract int getActivityInfo();

	//隐藏键盘
	@Override
	public boolean onKeyDown ( int keyCode, KeyEvent event){
		if (keyCode == KeyEvent.ACTION_UP) {
			if(getWindow().getAttributes().softInputMode== WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED)
			{
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				// 隐藏软键盘
				imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	/**
	 * todo 展示手势密码
	 * 展示手势密码
	 */
	//// TODO: 16/9/2 展示手势密码
	public void showLook() {
		if (!MainApplication.app.getAppGlobalConfig().isSupportGestures() || !showLock) return;
//        KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//        boolean flag = mKeyguardManager.isKeyguardLocked();
		//判断是否登陆
		DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(BaseActivity.this, DataCenterSharedPreferences.Constant.CONFIG);
		if (dcsp.getBoolean(DataCenterSharedPreferences.Constant.IS_LOGIN, false)) {
			//手势密码功能开关打开      并且已经设置好密码
			if (dcsp.getBoolean(DataCenterSharedPreferences.Constant.IS_APP_GENSTURE, false) && (!TextUtils.isEmpty(dcsp.getString(DataCenterSharedPreferences.Constant.CODE_GENSTURE, "")))) {
				//判断在页面可见情况下是否锁屏
				if (dcsp.getBoolean(DataCenterSharedPreferences.Constant.IS_LOOKS, false)) {
//                    if (!flag){
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
	public void setResultNew(int resultCode, Intent data) {
		super.setResult(resultCode, data);
		this.resultCode = resultCode;
		this.resultData = data;
	}

	@Override
	public void finish() {
		super.finish();
		if (getIntent().getStringExtra("back") != null) {
			Intent intent = MainApplication.app.getBackNeedStartActivtyMap().get(getIntent().getStringExtra("back"));
			if (intent != null) {
				intent.putExtra("resultCode", resultCode);
				super.startActivity(intent);
				overridePendingTransition(R.anim.activity_in_alpha, R.anim.activity_out_alpha);
			}
		}
	}
}
