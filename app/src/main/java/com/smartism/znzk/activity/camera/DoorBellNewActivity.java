package com.smartism.znzk.activity.camera;

import android.app.Activity;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.camera.P2PConnect;
import com.smartism.znzk.camera.SettingListener;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.widget.NormalDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DoorBellNewActivity extends Activity implements View.OnClickListener{
    Context mContext;
    TextView monitor_btn, ignore_btn, shield_btn ,alarmIdName;
    int alarm_type, group, item;
    LinearLayout layout_area_chanel;
    TextView area_text, chanel_text;
    LinearLayout alarm_input, alarm_dialog;
    TextView alarm_go;
    EditText mPassword;
    boolean isAlarm, isSupport, hasContact = false, isRegFilter = false;
    NormalDialog dialog;
    TextView tv_info, tv_type;
    String contactId = "";
    boolean isOpendoor;
    RelativeLayout rlAlarmDefault, rlAlarmMotion, rlAlarmDoorbell;
    TextView alarm_id_text, alarm_type_text;
    ImageView alarm_img, alarm_bell, alarm_left_right,alarm_close,alarm_open;
    private Dialog Pwddialog;
    private TextView tvDefenceArea;
    private int TIME_OUT = 20 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_newdoorbell);
        KeyguardManager km =
                (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        mContext = this;
        getExtralIntent(getIntent());
        initCameraList();
    }

    private void getExtralIntent(Intent intent) {
        String s = getIntent().getStringExtra("contactId");
        if (s.equals(contactId)) {
            contactId = s;
        } else {
            contactId = s;
            initComponent();
            excuteTimeOutTimer();
            regFilter();
        }

    }

    // 超时计时器
    public void excuteTimeOutTimer() {
        timeOutTimer = new Timer();
        TimerTask mTask = new TimerTask() {
            @Override
            public void run() {
                // 弹出一个对话框
                if (!viewed) {
                    Message message = new Message();
                    message.what = USER_HASNOTVIEWED;
                    mHandler.sendMessage(message);
                }
            }
        };
        timeOutTimer.schedule(mTask, TIME_OUT);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("onDestroy","--------------------");
        isAlarm = false;
        if (isRegFilter = true) {
            mContext.unregisterReceiver(br);
        }
        if (timeOutTimer != null) {
            timeOutTimer.cancel();
        }
         SettingListener.setAlarm(false);
         P2PConnect.setDoorbell(false);
    }
    public void initComponent() {
        alarm_close = (ImageView) findViewById(R.id.iv_alarm_close);
        alarm_open = (ImageView) findViewById(R.id.iv_alarm_open);
        alarm_close.setOnClickListener(this);
        alarm_open.setOnClickListener(this);
        rlAlarmDefault = (RelativeLayout) findViewById(R.id.rl_anim_alarm);
        rlAlarmMotion = (RelativeLayout) findViewById(R.id.rl_anim_motion);
        rlAlarmDoorbell = (RelativeLayout) findViewById(R.id.rl_anim_doorbell);
        alarm_type_text = (TextView) findViewById(R.id.tv_alarm_type);

        alarm_img = (ImageView) findViewById(R.id.iv_alarm_anim);
        alarm_bell = (ImageView) findViewById(R.id.iv_alarm_bell);
        alarm_left_right = (ImageView) findViewById(R.id.iv_doorbell_left_right);

        alarm_id_text = (TextView) findViewById(R.id.tv_alarm_device_id);
        alarm_id_text.setText(String.format(alarm_id_text.getText().toString(),
                contactId));
        alarmIdName = (TextView) findViewById(R.id.tv_info);
        Contact contact = FList.getInstance().isContact(contactId);
        if (null != contact) {
            alarmIdName.setText(contact.contactName);
        } else {
            alarmIdName.setText(null);
        }
        tvDefenceArea = (TextView) findViewById(R.id.tv_defence_area);
        tvDefenceArea.setVisibility(View.GONE);
        showDoorbellAlarm();
    }

    void showDoorbellAlarm() {
        rlAlarmDefault.setVisibility(View.GONE);
        rlAlarmMotion.setVisibility(View.GONE);
        rlAlarmDoorbell.setVisibility(View.VISIBLE);
        final AnimationDrawable anim = (AnimationDrawable) alarm_bell
                .getDrawable();
        final AnimationDrawable anim1 = (AnimationDrawable) alarm_left_right
                .getDrawable();
        ViewTreeObserver.OnPreDrawListener opdl = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                anim.start();
                anim1.start();
                return true;
            }

        };
        setAlarmType(R.string.app_id);
        alarm_bell.getViewTreeObserver().addOnPreDrawListener(opdl);
        alarm_left_right.getViewTreeObserver().addOnPreDrawListener(opdl);
    }

    void setAlarmType(int stringId) {
        String str = getResources().getString(stringId);
        alarm_type_text.setText(R.string.alarm_type);
        // alarm_type_text.setText(String.format(alarm_type_text.getText()
        // .toString(), str));
    }


    public void regFilter() {
        isRegFilter = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.P2P.RET_CUSTOM_CMD_DISCONNECT);
        registerReceiver(br, filter);
    }

    BroadcastReceiver br = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(
                    Constants.P2P.RET_CUSTOM_CMD_DISCONNECT)) {
                finish();
            }

        }
    };
    @Override
    protected void onResume() {
        super.onResume();
//        loadMusicAndVibrate();
         SettingListener.setAlarm(true);
         P2PConnect.setDoorbell(true);
    }
    Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            Contact contact = (Contact) msg.obj;
            Intent monitor = new Intent();
            monitor.setClass(DoorBellNewActivity.this, ApMonitorActivity.class);
            monitor.putExtra("flag", true);
            monitor.putExtra("contact", contact);
            monitor.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
            startActivity(monitor);
            finish();
            return false;
        }
    });
    boolean viewed = false;
    Timer timeOutTimer;
    public static final int USER_HASNOTVIEWED = 3;
    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case USER_HASNOTVIEWED:
                    finish();
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_alarm_close:

                break;
            case R.id.iv_alarm_open:
                isOpendoor = false;
                Contact c = FList.getInstance().isContact(
                        String.valueOf(contactId));
               final Contact contact = c;
                if (null != contact) {
                    hasContact = true;
                    P2PConnect.vReject("");
                    new Thread() {
                        public void run() {
                            while (true) {
                                if (P2PConnect.getCurrent_state() == P2PConnect.P2P_STATE_NONE) {
                                    Message msg = new Message();
                                    String[] data = new String[] {
                                            contact.contactId,
                                            contact.contactPassword };
                                    msg.obj = contact;
                                    handler.sendMessage(msg);
                                    break;
                                }
                                Utils.sleepThread(500);
                            }
                        }
                    }.start();
                }
                break;
        }
    }
    //从服务器上拉数据，更新本地摄像头
    public void initCameraList() {
        DeviceInfo device = null;
//        ZhujiInfo zhuji = DatabaseOperator.getInstance(mContext.getApplicationContext()).queryDeviceZhuJiInfo(
//                DataCenterSharedPreferences.getInstance(mContext, DataCenterSharedPreferences.Constant.CONFIG)
//                        .getString(DataCenterSharedPreferences.Constant.APP_MASTERID, ""));
        //替换
        ZhujiInfo zhuji = DatabaseOperator.getInstance(getApplicationContext()).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());

        //StartSearchDevice(); 这个好像是搜索局域网内的在线摄像头。不需要
        List<DeviceInfo> dInfos = DatabaseOperator.getInstance(mContext).queryAllDeviceInfos(zhuji.getId());
        if (dInfos != null && !dInfos.isEmpty()) {
            for (DeviceInfo deviceInfo : dInfos) {
                if (deviceInfo.getCak().equals("surveillance")) {
                    device = deviceInfo;
                    break;
                }
            }
        }
        List<CameraInfo> camera = new ArrayList<>();
        if (device != null && device.getCak().equals("surveillance")) {
            camera = (List<CameraInfo>) JSON.parseArray(device.getIpc(), CameraInfo.class);
        }
        if (camera != null && camera.size() > 0) {
            FList.getInstance().list().clear();
            Log.e("点击", camera.toString() + ":" + zhuji.getId());
            for (int i = 0; i < camera.size(); i++) {
                Contact c = new Contact();
                c.contactId = camera.get(i).getId();
                c.contactName = camera.get(i).getN();
                c.contactPassword = camera.get(i).getP();
                c.userPassword = camera.get(i).getOriginalP();
                if (camera.get(i).getC().equals("jiwei")) {
                    FList.getInstance().insert(c);
                    FList.getInstance().updateLocalDeviceFlag(c.contactId, Constants.DeviceFlag.ALREADY_SET_PASSWORD);
                    FList.getInstance().updateLocalDeviceWithLocalFriends();
                }
            }
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}
