package com.smartism.znzk.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.ImageView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.activity.user.LoginActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.global.LogService;
import com.smartism.znzk.hipermission.Permissions;
import com.smartism.znzk.util.Actions.VersionType;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.DateUtil;
import com.smartism.znzk.util.NotificationUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;

/*
* 无法通过配置文件shoWelcome控制是否显示启动页,启动页占用内存过大，会耗时
* */
public class MainActivity extends ActivityParentActivity {
    // 服务器IP列表
//	List<String> ips = new ArrayList<String>();

    private ImageView layout;
    boolean isFirst;//安装完app后进入显示web

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
//                    if (VersionType.CHANNEL_JUJIANG.equals(MainApplication.app.getAppGlobalConfig().getVersion()) && isFirst) {
//                        //安装的时候进入协议，同意以后不在显示（现在按钮没有只能定时进去）
//                        main_webview.setVisibility(View.VISIBLE);
//                        layout.setVisibility(View.GONE);
//                        isFirst = false;
//                    } else {
//                        //跳转
//                        nextActivity();
//                    }
                    nextActivity();
                    break;

                default:
                    break;
            }
            return false;
        }
    };
    private Handler dHandler = new WeakRefHandler(mCallback);

    /**
     * 跳转到下一个界面
     */
    public void nextActivity() {
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {

                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i(TAG, "init aws get userstate onResult: " + userStateDetails.getUserState());
                        Intent intent = new Intent();
                        switch (userStateDetails.getUserState()){
                            case SIGNED_IN:
                                intent.setClass(getApplication(), DeviceMainActivity.class);
                                break;
                            default:
                                intent.setClass(getApplication(), LoginActivity.class);
                                break;
                        }
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Initialization error.", e);
                    }
                }
        );
    }

    @JavascriptInterface
    public void start() {
        //声明免责协议按钮调用
        isFirst = false;
        dcsp.putBoolean(Constant.START_APP_FIRST, isFirst).commit();
        nextActivity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        layout = (ImageView) findViewById(R.id.main_bg);
         createNotificationChannel();
        if (MainApplication.app.getAppGlobalConfig().isStartLogService()) {
            //启动日志管理服务  Log stored in SDcard, the path is:/storage/emulated/0/com.smartism.jujiangxhouse-/log/
            startService(new Intent(this, LogService.class));
        }
        //读写数据库耗时，占用正常逻辑，会导致闪动
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean isShowWle = false;//用于一天只显示一次欢迎页
                if (VersionType.CHANNEL_JUJIANG.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
//            isFirst = dcsp.getBoolean(Constant.START_APP_FIRST, true);
                    String time = dcsp.getString(Constant.START_APP_TIME, "0");
                    isShowWle = DateUtil.formatUnixTime(System.currentTimeMillis(), "yyyy-MM-dd").equals(time);
                }
                dcsp.putBoolean(Constant.IS_TURN, true).commit();
                if (isShowWle) {
                    nextActivity();
                } else {
                    initPermission();
                }
            }
        },500);
        Log.i("main", "mainactivity onCreate end: " + System.currentTimeMillis());
    }

    private void initPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean needPermission = false;
            for (int i = 0; i< Permissions.ALLNEEDPERMISSIONS.length; i++){
                int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Permissions.ALLNEEDPERMISSIONS[i]);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    needPermission = true;
                    // We don't have permission so prompt the user
                    ActivityCompat.requestPermissions(MainActivity.this,Permissions.ALLNEEDPERMISSIONS,Permissions.REQUEST_EXTERNAL_STORAGE);
                    break;
                }
            }
            if (!needPermission){
                nextActivity();
            }
        }else{
            nextActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Permissions.REQUEST_EXTERNAL_STORAGE && (permissions.length >= Permissions.ALLNEEDPERMISSIONS.length || permissions.length == 0)){
            Log.e("权限", "onRequestPermissionsResult: 授权结束");
            if (permissions.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.WRITE_CONTACTS) && grantResults[i] == 0) {
                        if (MainApplication.app.getAppGlobalConfig().isShowCallAlarm()) {
                            int previous = dcsp.getInt(Constant.ALARM_VERSIONCODE,0);
                            final int current  = dcsp.getInt(Constant.APP_VERSIONCODE,0);
                            if(current>previous){
                                Util.addContacts(getApplicationContext());
                                dcsp.putInt(Constant.ALARM_VERSIONCODE,current).commit();
                            }
                        }
                    }
                }
            }
            nextActivity();
        }
    }

    /**
     * 适配8.0以上的通知栏适配 创建渠道(渠道为一组行为)
     */
    //TargetApi 和 RequiresApi 只是消除警告，崩溃还是存在
    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = null;
            channel = new NotificationChannel(NotificationUtil.CHANNEL_ONGOING_ID, mContext.getString(R.string.notification_ongoing), NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false); //是否在久按桌面图标时显示此渠道的通知
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);//设置在锁屏界面上不显示这条通知 好像没效果
            notificationManager.createNotificationChannel(channel);

            channel = new NotificationChannel(NotificationUtil.CHANNEL_NOTIFICATION_ID, mContext.getString(R.string.notification_default), NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);////是否在桌面icon右上角展示小红点
            channel.setLightColor(Color.RED); //小红点颜色
            channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);//设置在锁屏界面上显示这条通知
            notificationManager.createNotificationChannel(channel);
        }
    }
}
