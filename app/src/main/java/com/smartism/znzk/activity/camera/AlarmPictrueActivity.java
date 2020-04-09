package com.smartism.znzk.activity.camera;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.p2p.core.P2PHandler;
import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.camera.P2PConnect;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.WeakRefHandler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AlarmPictrueActivity extends BaseActivity implements
        OnClickListener {
    private Context mContext;
    private TextView device_id;
    private ImageView iv_sure, iv_cancel;
    private String id, password, name;
    protected DataCenterSharedPreferences dcsp = null;
    private Vibrator vibrator = null;
    //AudioManager mAudioMgr = null;
    MediaPlayer mMediaPlayer = null;
    private int time;
    Calendar cl = Calendar.getInstance();
    private Contact c;
    private Contact contact;

    @Override
    protected void onCreate(@Nullable Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_show_alarm);
        P2PConnect.setAlarm(true);
        mContext = this;
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        dcsp = DataCenterSharedPreferences.getInstance(mContext,
                DataCenterSharedPreferences.Constant.CONFIG);
        //判断屏幕是否锁屏
        KeyguardManager km =
                (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        mMediaPlayer = MediaPlayer.create(mContext,
                RingtoneManager.getActualDefaultRingtoneUri(mContext, RingtoneManager.TYPE_RINGTONE));
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(0.9f, 0.9f);
        }
        id = getIntent().getStringExtra("deviceid");

        initCameraList(id);
//        contact = FList.getInstance().getDeviceInfo(id);
        init();
        time = cl.get(Calendar.MINUTE);

    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (vibrator != null) {
                        vibrator.cancel();
                    }
                    if (mMediaPlayer != null) {
                        mMediaPlayer.stop();
                    }
                    break;

                default:
                    break;
            }
            return false;
        }
    };
    private Handler dHandler = new WeakRefHandler(mCallback);

    private void init() {

        vibrator.vibrate(new long[]{1000, 10, 100, 1000}, 0);
        //mAudioMgr.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT);
        if (mMediaPlayer != null) {
            mMediaPlayer.setLooping(false);
            mMediaPlayer.start();
        }
        device_id = (TextView) findViewById(R.id.device_id);
        if (contact != null && contact.contactName != null && contact.contactName != "") {
            device_id.setText(contact.contactName);
        } else {
            device_id.setText(id);
        }

        iv_sure = (ImageView) findViewById(R.id.iv_sure);
        iv_cancel = (ImageView) findViewById(R.id.iv_cancel);
        iv_sure.setOnClickListener(this);
        iv_cancel.setOnClickListener(this);
        dHandler.sendEmptyMessageDelayed(1, 18000);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (vibrator != null) {
            vibrator.cancel();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
        P2PConnect.setAlarm(false);
    }

    @Override
    public int getActivityInfo() {
        return Constants.ActivityInfo.ACTIVITY_ALRAM_WITH_PICTRUE;
    }

    @Override
    protected int onPreFinshByLoginAnother() {
        return 0;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_sure:
                c = contact;
                if (c != null && !"".equals(c)) {
                    P2PHandler.getInstance().setRemoteDefence(id, c.contactPassword,
                            Constants.P2P_SET.REMOTE_DEFENCE_SET.ALARM_SWITCH_OFF, MainApplication.GWELL_LOCALAREAIP);
                    FList.getInstance().setIsClickGetDefenceState(id, true);
                }
                finish();
                break;
            case R.id.iv_cancel:
                finish();
                break;
            default:
                break;
        }
    }

    //从服务器上拉数据，更新本地摄像头
    public void initCameraList(String cameraId) {
        DeviceInfo device = null;
        List<ZhujiInfo> zhujiInfos = DatabaseOperator.getInstance(mContext.getApplicationContext()).queryAllZhuJiInfos();

        List<CameraInfo> camera = new ArrayList<>();
        if (zhujiInfos != null) {
            for (ZhujiInfo info : zhujiInfos) {

                if (DeviceInfo.CakMenu.surveillance.value().equals(info.getCak())){
                    camera.add(info.getCameraInfo());
                }

                //获取主机下的设备，找到监控设备
                List<DeviceInfo> dInfos = DatabaseOperator.getInstance(mContext).queryAllDeviceInfos(info.getId());
                if (dInfos != null && !dInfos.isEmpty()) {
                    for (DeviceInfo deviceInfo : dInfos) {
                        if (deviceInfo.getCak().equals("surveillance")) {
                            device = deviceInfo;
                            break;
                        }
                    }
                }

                // 从服务器上获取到监控设备下的所有设备
                if (device != null && device.getCak().equals("surveillance")) {
                    List<CameraInfo> ca = new ArrayList<>();
                    ca = (List<CameraInfo>) JSON.parseArray(device.getIpc(), CameraInfo.class);
                    if (ca != null && !ca.isEmpty()) {
                        camera.addAll(ca);
                    }
                }
            }
        }
        //根据id找到报警设备（有多条相同设备信息时只去第一条 注意：一个监控设备可以被多个主机添加（名称不一样，但是密码一样））
        if (camera != null && !camera.isEmpty()) {
           for (CameraInfo cameraInfo : camera){
               if (cameraId.equals(cameraInfo.getId())){
                   contact = new Contact();
                   contact.contactId = cameraInfo.getId();
                   contact.contactName = cameraInfo.getN();
                   contact.contactPassword = cameraInfo.getP();
                   contact.userPassword = cameraInfo.getOriginalP();
                   return;
               }
           }
        }
    }
}
