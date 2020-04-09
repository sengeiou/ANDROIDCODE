package com.smartism.znzk.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.user.LoginActivity;
import com.smartism.znzk.activity.view.LockActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.service.CoreService;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.view.ProgressCycleView;
import com.umeng.analytics.MobclickAgent;

public class FragmentParentActivity extends AppCompatActivity {
    protected final String TAG = MainApplication.TAG;
    public FragmentParentActivity mContext;
    public ProgressDialog mProgressDialog = null;
    protected AlertDialog mProgressBarDialog = null;
    public ProgressCycleView mProgressCycleView = null;
    public DataCenterSharedPreferences dcsp = null;
    public Animation imgloading_animation;
    //public LockScreenReceiver LockscreenReceiver;
    public ActivityManager activityManager;

    //    public int Pre_priority;
//    public int End_priority;
    protected boolean showLock = true;//是否显示手势密码 默认显示
    protected int resultCode = 0; //返回时，如果需要带值回父窗体，这里带
    protected Intent resultData = null;//带值回去，这个暂未实现

    private boolean isSee = true;//是否可见

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
//        Pre_priority=getPriority(getApplicationContext());
        startScreenBroadcastReceiver();
        dcsp = DataCenterSharedPreferences.getInstance(FragmentParentActivity.this,
                DataCenterSharedPreferences.Constant.CONFIG);
        imgloading_animation = AnimationUtils.loadAnimation(FragmentParentActivity.this, R.anim.loading_revolve);
        imgloading_animation.setInterpolator(new LinearInterpolator());

//		LockscreenReceiver = new LockScreenReceiver();
//		//创建一个IntentFilter对象，来封装事件类型
//		IntentFilter intentFilter = new IntentFilter();
//		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
//		//intentFilter.addAction(Intent.ACTION_SCREEN_ON);
//		intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
//		registerReceiver(LockscreenReceiver, intentFilter);
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
     * 显示进度条
     *
     * @param text
     */
    public void showInProgress(String text) {
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
                mProgressDialog.setIndeterminate(false);
                mProgressDialog.setCancelable(true);
            }
            if (!mProgressDialog.isShowing()) {
                mProgressDialog.show();
            }
        }
    }

    /**
     * 显示带进度的进度条
     *
     * @param text
     * @param max         false 未知进度的进度条， true 知道进度的进度条
     * @param bCancelable true 返回按钮可以退出 false 返回按钮不能退出
     */
    public void showOrUpdateProgressBar(String text, boolean bCancelable, int progress, int max) {
        synchronized (this) {
            if (mProgressBarDialog == null || mProgressCycleView == null || !mProgressBarDialog.isShowing()) {
                mProgressBarDialog = new AlertDialog.Builder(FragmentParentActivity.this).create();
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
     * 进度条是否是展示状态
     *
     * @return
     */
    public boolean progressIsShowing() {
        return mProgressDialog != null && mProgressDialog.isShowing();
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

    public DataCenterSharedPreferences getDcsp() {
        return dcsp;
    }


    @Override
    protected void onPause() {
        super.onPause();
        isSee = false;
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
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
        stopScreenStateUpdate();
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
        isSee = true;
        MobclickAgent.onResume(this);
        showLook();
    }

    //// TODO: 16/9/2 接收屏幕点亮广播
    BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                if (!dcsp.getBoolean(DataCenterSharedPreferences.Constant.IS_LOOKS, false)) {
                    dcsp.putBoolean(DataCenterSharedPreferences.Constant.IS_LOOKS, true).commit();
                    showLook();
                }
            }
        }
    };

    /**
     * 启动screen状态广播接收器
     */
    private void startScreenBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        mContext.registerReceiver(mScreenReceiver, filter);
    }

    /**
     * 停止screen状态更新
     */
    public void stopScreenStateUpdate() {

        mContext.unregisterReceiver(mScreenReceiver);
    }

    /**
     * todo 展示手势密码
     * 展示手势密码
     */
    //// TODO: 16/9/2 展示手势密码
    public void showLook() {
//        KeyguardManager mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//        boolean flag = mKeyguardManager.isKeyguardLocked();
        if (!MainApplication.app.getAppGlobalConfig().isSupportGestures() || !showLock) return;
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
                    overridePendingTransition(R.anim.activity_in_alpha, R.anim.activity_out_alpha);
//                    }
                }
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
//        End_priority = getPriority(getApplicationContext());
//        if (Pre_priority != End_priority) {
//            dcsp.putBoolean(DataCenterSharedPreferences.Constant.IS_LOOKS, true).commit();
//        }
    }

//    public int getPriority(Context context){
//        int priority=0;
//        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
//        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
//            if (appProcess.processName.equals(context.getPackageName())) {
//                priority = appProcess.importance;
//            }
//        }
//        return priority;
//    }

    private class ExitLogin implements Runnable {
        @Override
        public void run() {
            Util.clearLoginInfo(getApplicationContext(), dcsp); //清空用户登录信息
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            String result = HttpRequestUtils
                    .requestoOkHttpPost(server + "/jdm/s3/u/logout", null, mContext);
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

    public boolean isSee() {
        return isSee;
    }

    public void setSee(boolean see) {
        isSee = see;
    }
}