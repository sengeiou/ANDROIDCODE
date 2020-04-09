package com.smartism.znzk.activity.camera;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.p2p.core.P2PView;
import com.p2p.core.utils.DelayThread;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentMonitorActivity;
import com.smartism.znzk.activity.common.CLDTimeSetActivity;
import com.smartism.znzk.activity.device.IPCZhujiDetailActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.adapter.recycleradapter.BaseRecyslerAdapter;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.camera.P2PConnect;
import com.smartism.znzk.camera.adapter.ImageListAdapter;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.global.AppConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.widget.HeaderView;
import com.smartism.znzk.widget.MyInputPassDialog;
import com.smartism.znzk.widget.MyInputPassDialog.OnCustomDialogListener;
import com.smartism.znzk.widget.NormalDialog;
import com.smartism.znzk.widget.NormalDialog.OnAlarmClickListner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 技威的单独监控视频播放的activity
 *
 * @author 2016年08月09日 update by 王建
 */
public class ApMonitorActivity extends ActivityParentMonitorActivity implements
        OnClickListener, BaseRecyslerAdapter.RecyclerItemClickListener, BaseRecyslerAdapter.RecyclerItemLongClickListener {

    private final int dHandler_timeout = 100,dHandler_daojishi = 101;
    private RecyclerView horizon_listview;
    private ImageListAdapter mAdapter;
    private RelativeLayout layout_title, l_control;
    private ImageView iv_full_screen, iv_voice, iv_recode, iv_file, iv_speak,
            iv_defence, iv_screenshot, open_door;
    private RelativeLayout rl_control, control_bottom;
    private LinearLayout control_top;
    private Button choose_video_format, format_h;
    private ImageView back_btn;
    private TextView video_mode_hd, video_mode_sd, video_mode_ld;
    private ImageView close_voice, send_voice, iv_half_screen, hungup, screenshot,
            defence_state;
    private LinearLayout layout_voice_state;
    private TextView tv_name;
    private RelativeLayout r_p2pview;
    private ImageView voice_state;
    private LinearLayout l_device_list;
    private TextView tv_choosee_device;
    private Contact mContact;
    private int callType = 3;
    public Context mContext;
    private TextView users;
    boolean isReject = false;
    boolean isRegFilter = false;
    private int ScrrenOrientation;
    int window_width, window_height;
    private String callId, password;
    int connectType;
    private int defenceState = -1;//布防状态
    boolean mIsCloseVoice = false;
    public AudioManager mAudioManager;
    boolean isSurpportOpenDoor = false;
    boolean isShowVideo = false;
    boolean isSpeak = false; //是否在对讲
    int current_video_mode;
    int screenWidth;
    int screenHeigh;
    private String NewMessageDeviceID;
    // 刷新监控部分
    private RelativeLayout rlPrgTxError;
    private TextView txError, tx_wait_for_connect;
    private Button btnRefrash;
    private ProgressBar progressBar;
    private HeaderView ivHeader;
    private String alarm_id = "";
    private String[] ipcList;
    int number;
    int currentNumber = 0;
    boolean isShowDeviceList = false;
    List<TextView> devicelist = new ArrayList<TextView>();
    // 摇手机切换ipc
    Vibrator vibrator;
    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener sensorListener;
    ImageView tv_moreinfo, tv_setting;
    boolean isShake = true;
    private long lastUpdateTime;
    private float lastX;
    private float lastY;
    private float lastZ;
    private static final int UPTATE_INTERVAL_TIME = 70;
    private static final int SPEED_SHRESHOLD = 2000;
    private boolean isReceveHeader = false;
    boolean isPermission = true;
    private View vLineHD;
    private boolean connectSenconde = false;
    int pushAlarmType;
    boolean isCustomCmdAlarm = false;
    private int last_record;
    private String command;
    private String idOrIp;

    int mMaxVolume = 100;
    int mCurrentVolume = 0;

    private SeekBar seek_voice;
    private TextView voice_persent;
    private DeviceInfo deviceInfo;
    private boolean isCancelLoading;
    private ImageView iv_cld;
    private InputMethodManager imm;

    private AlertView controlConfrimAlert;//控制确认提示框
    private EditText etName; //催泪密码
    private AlertView mAlertViewExt;//催泪密码
    private AlertView mAnBaForceAlertTip ; //安霸禁用提示框

    public static String getCameraPath(Context context) {
        return Environment.getExternalStorageDirectory().getPath() + File.separator + context.getPackageName() + File.separator + "/";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apmonitor);
        mContext = this;
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        mContact = (Contact) getIntent().getSerializableExtra("contact");
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("deviceInfo");
//        String masterID = dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, "");
        //替换
        String masterID = ZhujiListFragment.getMasterId() ;
        zhuji = DatabaseOperator.getInstance(mContext)
                .queryDeviceZhuJiInfo(masterID);
        if (deviceInfo == null) {
            deviceInfo = Utils.getContactDevice(getApplicationContext(), mContact);//初始化摄像头属于的智慧猫设备信息
        }
        if (mContact.contactType == P2PValue.DeviceType.IPC) {
            setIsLand(false);
        } else {
            setIsLand(true);
        }
        idOrIp = mContact.contactId;
        if (mContact.ipadressAddress != null) {
            String mark = mContact.ipadressAddress.getHostAddress();
            String ip = mark.substring(mark.lastIndexOf(".") + 1, mark.length());
            if (!ip.equals("") && ip != null) {
                idOrIp = ip;
            }
        }
        ipcList = getIntent().getStringArrayExtra("ipcList");
        number = getIntent().getIntExtra("number", -1);
        connectType = getIntent().getIntExtra("connectType",
                Constants.ConnectType.P2PCONNECT);
        isSurpportOpenDoor = getIntent().getBooleanExtra("isSurpportOpenDoor", false);
        isCustomCmdAlarm = getIntent().getBooleanExtra("isCustomCmdAlarm", false);
        callId = mContact.contactId;
        if (number > 0) {
            callId = ipcList[0];
        }
        password = mContact.getContactPassword();


//        P2PHandler.getInstance().p2pInit(mContext, new P2PListener(), new SettingListener());// 设置在监控
        getScreenWithHeigh();
        regFilter();
        callDevice();
        initcComponent();
        readyCallDevice();
        initp2pView();
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        }
        mCurrentVolume = mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        mMaxVolume = mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        ScrrenOrientation = Configuration.ORIENTATION_PORTRAIT;
        vibrator = (Vibrator) mContext
                .getSystemService(mContext.VIBRATOR_SERVICE);

        P2PHandler.getInstance().getNpcSettings(mContact.contactId, mContact.getContactPassword(), MainApplication.GWELL_LOCALAREAIP);
        seek_voice.setMax(mMaxVolume);
        seek_voice.setProgress(mCurrentVolume);
        int persent = mCurrentVolume * 100 / mMaxVolume;
        voice_persent.setText(persent + "%");
        seek_voice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mCurrentVolume = seekBar.getProgress();
                seek_voice.setProgress(mCurrentVolume);
                int persent = mCurrentVolume * 100 / mMaxVolume;
                voice_persent.setText(persent + "%");
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mCurrentVolume, 0); //tempVolume:音量绝对值
            }
        });

        if(Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            mAnBaForceAlertTip = new AlertView(getString(R.string.remind_msg), getString(R.string.abbq_ges_notice_force_tip),
                    null,
                    new String[]{getString(R.string.ready_guide_msg13)}, null,
                    mContext, AlertView.Style.Alert,
                    new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, final int position) {
                            if(position!=-1){
                                Log.d(TAG,"点击我知道了");
                            }
                        }
                    });
            mAnBaForceAlertTip.setCancelable(false);
        }
    }
    //调节摄像头音量
    public void switchVideoVolume(final int toggle) {

        new DelayThread(Constants.SettingConfig.SETTING_CLICK_TIME_DELAY, new DelayThread.OnRunListener() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                P2PHandler.getInstance().setVideoVolume(mContact.contactId, mContact.getContactPassword(), toggle, MainApplication.GWELL_LOCALAREAIP);

            }
        }).start();
    }

    public String createCommand(String bCommandType, String bOption,
                                String SDCardCounts) {
        return bCommandType + bOption + SDCardCounts;
    }

    public void initcComponent() {
        iv_cld = (ImageView) findViewById(R.id.iv_cld);
        iv_cld.setOnClickListener(this);
        if (Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            if(deviceInfo.getId()==deviceInfo.getZj_id()){
                //摄像头作为主机,隐藏
                iv_cld.setVisibility(View.GONE);
            }else{
                //技威摄像头作为设备，显示.
                iv_cld.setVisibility(View.VISIBLE);
            }

        }
        users = (TextView) findViewById(R.id.users);
        users.setText(getString(R.string.monitor_number) + P2PConnect.getNumber());
        seek_voice = (SeekBar) findViewById(R.id.seek_voice);
        voice_persent = (TextView) findViewById(R.id.voice_persent);

        seek_voice.setMax(mMaxVolume);
        seek_voice.setProgress(mCurrentVolume);
        int persent = mCurrentVolume * 100 / mMaxVolume;
        voice_persent.setText(persent + "%");

        pView = (P2PView) findViewById(R.id.p2pview);
        // this.initP2PView(mContact.contactType);
        P2PView.type = 0;
        WindowManager manager = getWindowManager();
        window_width = manager.getDefaultDisplay().getWidth();
        window_height = manager.getDefaultDisplay().getHeight();
        this.initScaleView(this, window_width, window_height);
        LayoutInflater inflater = getLayoutInflater();
        rl_control = (RelativeLayout) findViewById(R.id.rl_control);
        iv_voice = (ImageView) findViewById(R.id.iv_vioce);
        iv_recode = (ImageView) findViewById(R.id.iv_recode);
        iv_file = (ImageView) findViewById(R.id.iv_file);

        iv_defence = (ImageView) findViewById(R.id.iv_defence);
        iv_speak = (ImageView) findViewById(R.id.iv_speak);
        iv_screenshot = (ImageView) findViewById(R.id.iv_screenshot);

        horizon_listview = (RecyclerView) findViewById(R.id.horizon_listview);
        layout_title = (RelativeLayout) findViewById(R.id.layout_title);
        iv_full_screen = (ImageView) findViewById(R.id.iv_full_screen);
        l_control = (RelativeLayout) findViewById(R.id.l_control);

        back_btn = (ImageView) findViewById(R.id.back_btn);
        control_bottom = (RelativeLayout) findViewById(R.id.control_bottom);
        control_top = (LinearLayout) findViewById(R.id.control_top);
        video_mode_hd = (TextView) findViewById(R.id.video_mode_hd);
        video_mode_sd = (TextView) findViewById(R.id.video_mode_sd);
        video_mode_ld = (TextView) findViewById(R.id.video_mode_ld);
        vLineHD = findViewById(R.id.v_line_hd);
        choose_video_format = (Button) findViewById(R.id.choose_video_format);
        format_h = (Button) findViewById(R.id.choose_video_format_h);

        close_voice = (ImageView) findViewById(R.id.close_voice);
        send_voice = (ImageView) findViewById(R.id.send_voice);
        layout_voice_state = (LinearLayout) findViewById(R.id.layout_voice_state);
        iv_half_screen = (ImageView) findViewById(R.id.iv_half_screen);
        hungup = (ImageView) findViewById(R.id.hungup);
        screenshot = (ImageView) findViewById(R.id.screenshot);
        defence_state = (ImageView) findViewById(R.id.defence_state);
        open_door = (ImageView) findViewById(R.id.open_door);
        tv_name = (TextView) findViewById(R.id.tv_name);
        r_p2pview = (RelativeLayout) findViewById(R.id.r_p2pview);
        voice_state = (ImageView) findViewById(R.id.voice_state);
        l_device_list = (LinearLayout) findViewById(R.id.l_device_list);
        tv_choosee_device = (TextView) findViewById(R.id.tv_choosee_device);
        tv_moreinfo = (ImageView) findViewById(R.id.tv_moreinfo);
        tv_setting = (ImageView) findViewById(R.id.tv_setting);
        if (deviceInfo != null && DeviceInfo.ControlTypeMenu.zhuji.value().equals(deviceInfo.getControlType())) {
            tv_moreinfo.setVisibility(View.VISIBLE);
            tv_setting.setVisibility(View.VISIBLE);
        }
        setControlButtomHeight(0);
        frushLayout(mContact.contactType);
        if (number > 1) {
            // tv_choosee_device.setVisibility(View.VISIBLE);
            sensorManager = (SensorManager) mContext
                    .getSystemService(mContext.SENSOR_SERVICE);
            if (sensorManager != null) {
                sensor = sensorManager
                        .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorListener = new SensorEventListener() {

                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        // TODO Auto-generated method stub
                        if (!isShake) {
                            // 现在检测时间
                            long currentUpdateTime = System.currentTimeMillis();
                            // 两次检测的时间间隔
                            long timeInterval = currentUpdateTime
                                    - lastUpdateTime;
                            if (timeInterval < UPTATE_INTERVAL_TIME)
                                return;
                            // 现在的时间变成last时间
                            lastUpdateTime = currentUpdateTime;

                            // 获得x,y,z坐标
                            float x = event.values[0];
                            float y = event.values[1];
                            float z = event.values[2];

                            // 获得x,y,z的变化值
                            float deltaX = x - lastX;
                            float deltaY = y - lastY;
                            float deltaZ = z - lastZ;

                            // 将现在的坐标变成last坐标
                            lastX = x;
                            lastY = y;
                            lastZ = z;
                            double speed = Math.sqrt(deltaX * deltaX + deltaY
                                    * deltaY + deltaZ * deltaZ)
                                    / timeInterval * 10000;
                            // 达到速度阀值，发出提示
                            if (speed >= SPEED_SHRESHOLD) {
                                isShake = true;
                                vibrator.vibrate(new long[]{500, 200, 500,
                                        200}, -1);
                                switchNext();
                            }
                        }
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                        // TODO Auto-generated method stub

                    }

                };
            }
            sensorManager.registerListener(sensorListener, sensor,
                    SensorManager.SENSOR_DELAY_GAME);
        } else {
            tv_choosee_device.setVisibility(View.GONE);
        }
        //截图列表
        showImageList();
        mAdapter = new ImageListAdapter(pictrues);
        mAdapter.setRecyclerItemClickListener(this);
        mAdapter.setRecyclerItemLongClickListener(this);
        horizon_listview.setAdapter(mAdapter);

        //创建默认线性LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        //设置布局管理器
        horizon_listview.setLayoutManager(layoutManager);
        //设置adapter
        //设置Item增加、移除动画
        horizon_listview.setItemAnimator(new DefaultItemAnimator());
        //添加分割线
        horizon_listview.setAdapter(mAdapter);

        // 刷新监控
        rlPrgTxError = (RelativeLayout) findViewById(R.id.rl_prgError);
        txError = (TextView) findViewById(R.id.tx_monitor_error);
        btnRefrash = (Button) findViewById(R.id.btn_refrash);
        progressBar = (ProgressBar) findViewById(R.id.prg_monitor);
        tx_wait_for_connect = (TextView) findViewById(R.id.tx_wait_for_connect);
        ivHeader = (HeaderView) findViewById(R.id.hv_header);
        rlPrgTxError.setOnClickListener(this);
        btnRefrash.setOnClickListener(this);
        // 更新头像
        setHeaderImage();
        iv_voice.setOnClickListener(this);
        iv_recode.setOnClickListener(this);
        iv_file.setOnClickListener(this);

        iv_full_screen.setOnClickListener(this);
        iv_defence.setOnClickListener(this);
        iv_screenshot.setOnClickListener(this);
        back_btn.setOnClickListener(this);
        choose_video_format.setOnClickListener(this);
        format_h.setOnClickListener(this);
        close_voice.setOnClickListener(this);
        send_voice.setOnClickListener(this);
        iv_half_screen.setOnClickListener(this);
        hungup.setOnClickListener(this);
        screenshot.setOnClickListener(this);
        video_mode_hd.setOnClickListener(this);
        video_mode_sd.setOnClickListener(this);
        video_mode_ld.setOnClickListener(this);
        defence_state.setOnClickListener(this);
        tv_choosee_device.setOnClickListener(this);
        tv_moreinfo.setOnClickListener(this);
        tv_setting.setOnClickListener(this);
        open_door.setOnClickListener(this);
        tv_name.setText(mContact.getContactName());
        final AnimationDrawable anim = (AnimationDrawable) voice_state
                .getDrawable();
        OnPreDrawListener opdl = new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                anim.start();
                return true;
            }

        };
        voice_state.getViewTreeObserver().addOnPreDrawListener(opdl);
        if (mContact.contactType == P2PValue.DeviceType.NPC) {
            current_video_mode = P2PValue.VideoMode.VIDEO_MODE_LD;
        } else {
            current_video_mode = P2PConnect.getMode();
        }
        if (isSurpportOpenDoor == true) {
            open_door.setVisibility(ImageView.VISIBLE);
        } else {
            open_door.setVisibility(ImageView.GONE);
        }

        updateVideoModeText(current_video_mode);
        if (mContact.contactType != P2PValue.DeviceType.DOORBELL
                && !isSurpportOpenDoor) {
            iv_speak.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent event) {
                    // TODO Auto-generated method stub
                    int time = 0;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:

                            hideVideoFormat();
                            layout_voice_state
                                    .setVisibility(RelativeLayout.VISIBLE);
                            // iv_speak.setBackgroundResource(R.drawable.portrait_speak_p);
                            // T.showShort(mContext, R.string.hold_talk);
                            setMute(false);
                            return true;
                        case MotionEvent.ACTION_UP:
                            layout_voice_state.setVisibility(RelativeLayout.GONE);
                            setMute(true);
                            return true;
                        case MotionEvent.ACTION_CANCEL:
                            layout_voice_state.setVisibility(RelativeLayout.GONE);
                            setMute(true);
                            return true;
                    }

                    return false;
                }
            });
            send_voice.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent event) {
                    int time = 0;
                    // TODO Auto-generated method stub
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            time++;
                            Log.e("时间：", event.getDownTime() + "");
                            hideVideoFormat();
                            layout_voice_state
                                    .setVisibility(RelativeLayout.VISIBLE);

                            send_voice
                                    .setBackgroundResource(R.drawable.ic_send_audio_p);
                            setMute(false);
                            return true;
                        case MotionEvent.ACTION_UP:
                            layout_voice_state.setVisibility(RelativeLayout.GONE);
                            send_voice
                                    .setBackgroundResource(R.drawable.ic_send_audio);
                            setMute(true);
                            return true;
                        case MotionEvent.ACTION_CANCEL:
                            layout_voice_state.setVisibility(RelativeLayout.GONE);
                            send_voice
                                    .setBackgroundResource(R.drawable.ic_send_audio);
                            setMute(true);
                            return true;
                    }
                    return false;
                }
            });
        } else if (mContact.contactType == P2PValue.DeviceType.DOORBELL
                && !isSurpportOpenDoor) {
            isFirstMute = false;
            iv_speak.setOnClickListener(this);
            send_voice.setOnClickListener(this);
        } else if (isSurpportOpenDoor) {
            Log.e("leleTest", "isSurpportOpenDoor=" + isSurpportOpenDoor);
            iv_speak.setOnClickListener(this);
            // 开始监控时没有声音，暂时这样
            send_voice.setOnClickListener(this);
            iv_speak.performClick();
            iv_speak.performClick();
            // speak();
            // speak();
            // send_voice.performClick();
            // speak();
        }
        initIpcDeviceList();
    }

    private void showImageList() {
        //截图显示
        if (pictrues == null)
            pictrues = new ArrayList<>();
        pictrues.clear();
        List<String> paths = Utils.getScreenShotImagePath(callId, 1);
        if (!paths.isEmpty()) {
            for (String path : paths) {
                pictrues.add(new RecyclerItemBean(path, 0));
            }
        }
    }

    public void initSpeark(int deviceType, boolean isOpenDor) {
        if (isOpenDor == true) {
            open_door.setVisibility(View.VISIBLE);
        } else {
            open_door.setVisibility(View.GONE);
        }
        if (deviceType != P2PValue.DeviceType.DOORBELL && !isOpenDor) {
            iv_speak.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent event) {
                    // TODO Auto-generated method stub
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.e("时间：", event.getEventTime() + "");
                            hideVideoFormat();
                            layout_voice_state
                                    .setVisibility(RelativeLayout.VISIBLE);

                            // iv_speak.setBackgroundResource(R.drawable.portrait_speak_p);
                            T.showShort(mContext, R.string.hold_talk);
                            hideVideoFormat();
                            setMute(false);
                            return true;
                        case MotionEvent.ACTION_UP:
                            layout_voice_state.setVisibility(RelativeLayout.GONE);
                        /*
                         * iv_speak
						 * .setBackgroundResource(R.drawable.portrait_speak);
						 */
                            setMute(true);
                            return true;
                        case MotionEvent.ACTION_CANCEL:
                            layout_voice_state.setVisibility(RelativeLayout.GONE);
                        /*
                         * iv_speak
						 * .setBackgroundResource(R.drawable.portrait_speak);
						 */
                            setMute(true);
                            return true;
                    }
                    return false;
                }
            });
            send_voice.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent event) {
                    // TODO Auto-generated method stub
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.e("时间：", event.getEventTime() + "");
                            hideVideoFormat();
                            layout_voice_state
                                    .setVisibility(RelativeLayout.VISIBLE);

                            send_voice
                                    .setBackgroundResource(R.drawable.icon_camere_speak_p);
                            setMute(false);
                            return true;
                        case MotionEvent.ACTION_UP:
                            layout_voice_state.setVisibility(RelativeLayout.GONE);
                            send_voice
                                    .setBackgroundResource(R.drawable.icon_camere_speak);
                            setMute(true);
                            return true;
                        case MotionEvent.ACTION_CANCEL:
                            layout_voice_state.setVisibility(RelativeLayout.GONE);
                            send_voice
                                    .setBackgroundResource(R.drawable.icon_camere_speak);
                            setMute(true);
                            return true;
                    }
                    return false;
                }
            });
        } else if (deviceType == P2PValue.DeviceType.DOORBELL && !isOpenDor) {
            isFirstMute = false;
            iv_speak.setOnTouchListener(null);
            send_voice.setOnTouchListener(null);
            iv_speak.setOnClickListener(this);
            send_voice.setOnClickListener(this);
        } else if (isOpenDor) {
            iv_speak.setOnTouchListener(null);
            send_voice.setOnTouchListener(null);
            control_bottom.setVisibility(View.VISIBLE);
            iv_speak.setOnClickListener(this);
            // 开始监控时没有声音，暂时这样
            send_voice.setOnClickListener(this);
            isFirstMute = true;
            iv_speak.performClick();
            iv_speak.performClick();
            // speak();
        }
    }

    private void setHeaderImage() {
        ivHeader.updateImage(callId, true, 1);
    }

    /**
     * 刷新IPC和NPC布局异同
     */
    private void frushLayout(int contactType) {
        if (contactType == P2PValue.DeviceType.IPC) {
            video_mode_hd.setVisibility(View.VISIBLE);
            vLineHD.setVisibility(View.VISIBLE);
        } else if (contactType == P2PValue.DeviceType.NPC) {
            video_mode_hd.setVisibility(View.GONE);
            vLineHD.setVisibility(View.GONE);
        }
    }

    public void regFilter() {
        if (!isRegFilter) {
            isRegFilter = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.P2P.P2P_ACCEPT);
            filter.addAction(Constants.P2P.P2P_READY);
            filter.addAction(Constants.P2P.P2P_REJECT);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Constants.P2P.ACK_RET_CHECK_PASSWORD);
            filter.addAction(Constants.P2P.RET_GET_REMOTE_DEFENCE);
            filter.addAction(Constants.P2P.RET_SET_REMOTE_DEFENCE);
            filter.addAction(Constants.P2P.P2P_RESOLUTION_CHANGE);
            filter.addAction(Constants.P2P.DELETE_BINDALARM_ID);
            filter.addAction(Constants.Action.MONITOR_NEWDEVICEALARMING);
            filter.addAction(Constants.P2P.RET_P2PDISPLAY);
            filter.addAction(Constants.P2P.ACK_GET_REMOTE_DEFENCE);
            filter.addAction(Constants.P2P.RET_GET_REMOTE_RECORD);
            filter.addAction(Constants.P2P.RET_GET_RECORD_TYPE);
            filter.addAction(Constants.P2P.RET_SET_RECORD_TYPE);
            filter.addAction(Constants.P2P.RET_SET_REMOTE_RECORD);
            filter.addAction(Constants.P2P.RET_GET_RECORD_PLAN_TIME);
            filter.addAction(Constants.P2P.P2P_MONITOR_NUMBER_CHANGE);
            filter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
            filter.addAction(Actions.CONTROL_BACK_MESSAGE);
            registerReceiver(mReceiver, filter);
        }
    }

    int currentdType = -1;
    int recordType = -1;
    int recordState = -1;
    boolean isFirstInitType = true;
    boolean isFirstInitState = true;
    String planTime = "";
    int state = -1;
    private ZhujiInfo zhuji;
    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            Log.e("BroadcastReceiver_1111", intent.getAction());
            if (Actions.ACCETP_ONEDEVICE_MESSAGE.equals(intent.getAction())) {//发射催泪弹返回
                if (intent.getStringExtra("zhuji_id") != null) {//主机收到的数据
                    String zhuji_id = intent.getStringExtra("zhuji_id");
                    if (zhuji != null && zhuji_id.equals(String.valueOf(zhuji.getId()))) {
                        String data = (String) intent.getSerializableExtra("zhuji_info");
                        if (data != null) {
                            try {
                                JSONObject object = JSONObject.parseObject(data);
                                if (object != null && "101".equals(object.getString("sort"))) {
                                    long uid = dcsp.getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
                                    if (object.getString("send").equals(String.valueOf(uid))) {
                                        cancelInProgress();
                                        handler.removeMessages(100);
                                        Toast.makeText(mContext, mContext.getString(R.string.cld_send_succ),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (Exception ex) {
                                //防止json无数据崩溃
                            }
                        }
                    }
                }
            }else if (Actions.CONTROL_BACK_MESSAGE.equals(intent.getAction())) { // 控制返回
                handler.removeMessages(100);
                if (intent.getIntExtra("code",0) == SyncMessage.CodeMenu.rp_control_needconfirm.value()) { //需要授权
                    final String keyStr = intent.getStringExtra("data_info");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            controlConfrimAlert = new AlertView(getString(R.string.warning), getString(R.string.abbq_ges_notice_warning), getString(R.string.cancel),
                                    new String[]{getString(R.string.sure) + "(20)"}, null, mContext, AlertView.Style.Alert,
                                    new OnItemClickListener() {

                                        @Override
                                        public void onItemClick(Object o, final int position) {
                                            if (position != -1) {
                                                //发送控制
                                                showInProgress(getString(R.string.operationing), false, true);
                                                SyncMessage message = new SyncMessage();
                                                message.setCommand(SyncMessage.CommandMenu.rq_controlConfirm.value());
                                                message.setDeviceid(zhuji.getId());
                                                // 操作 这里的操作写死，需要在服务器的配置页面中配置此按钮对应的指令是什么
                                                message.setSyncBytes(keyStr.getBytes());
                                                SyncMessageContainer.getInstance().produceSendMessage(message);
                                                handler.sendEmptyMessageDelayed(100, 8000);//8秒超时
                                            }
                                        }
                                    });
                            LinearLayout loAlertButtons = (LinearLayout) controlConfrimAlert.getContentContainer().findViewById(R.id.loAlertButtons);
                            TextView textView = (TextView) loAlertButtons.getChildAt(2).findViewById(R.id.tvAlert); //获取到按钮
                            textView.setTextColor(getResources().getColor(R.color.red));
                            controlConfrimAlert.show();
                            handler.sendMessageDelayed(handler.obtainMessage(dHandler_daojishi, 19, 0), 1000);//改变倒计时
                        }
                    },1000);
                }else if(intent.getIntExtra("code",0) == SyncMessage.CodeMenu.rp_control_verifyerror.value()){
                    //催泪弹发射，返回-7
                    //弹出提示框
                    cancelInProgress();//隐藏进度条
                    //ViewGroup频繁的remove会导致空指针异常.
                    if(mAnBaForceAlertTip!=null&&!mAnBaForceAlertTip.isShowing()){
                        layout_voice_state.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(mAnBaForceAlertTip!=null&&!mAnBaForceAlertTip.isShowing()){
                                    mAnBaForceAlertTip.show();
                                }
                            }
                        },500);
                    }


                }

            } else if (intent.getAction().equals(Constants.P2P.RET_GET_RECORD_PLAN_TIME)) {
                planTime = intent.getStringExtra("time");

            } else if (intent.getAction().equals(
                    Constants.P2P.P2P_MONITOR_NUMBER_CHANGE)) {
                int number = intent.getIntExtra("number", -1);
                if (number != -1) {
                    users.setText(mContext.getResources().getString(
                            R.string.monitor_number)
                            + " " + P2PConnect.getNumber());
                }
            } else if (intent.getAction().equals(Constants.P2P.RET_GET_REMOTE_RECORD)) {
                state = intent.getIntExtra("state", -1);
                if (isFirstInitState) {
                    recordState = state;
                    isFirstInitState = false;
                } else {
                    last_record = state;
                }
                updateRecord(state, true);
            } else if (intent.getAction().equals(Constants.P2P.RET_SET_RECORD_TYPE)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Constants.P2P_SET.RECORD_TYPE_SET.SETTING_SUCCESS) {
                    currentdType = result;
                }
            } else if (intent.getAction().equals(Constants.P2P.RET_SET_REMOTE_RECORD)) {
                state = intent.getIntExtra("result", -1);
                P2PHandler.getInstance().getNpcSettings(mContact.contactId, mContact.getContactPassword(), MainApplication.GWELL_LOCALAREAIP);
//                updateRecord(state,true);
            } else if (intent.getAction().equals(Constants.P2P.RET_GET_RECORD_TYPE)) {
                int type = intent.getIntExtra("type", -1);
                if (isFirstInitType) {
                    recordType = type;
                    isFirstInitType = false;
                    updateRecord(state, true);
                }
                currentdType = type;
            } else if (intent.getAction().equals(Constants.P2P.P2P_READY)) {
                Log.e("monitor", "P2P_READY" + "callId=" + callId);
                P2PHandler.getInstance().getDefenceStates(callId, password, MainApplication.GWELL_LOCALAREAIP);
                isReceveHeader = false;
                isShake = false;
                pView.sendStartBrod();
            } else if (intent.getAction().equals(Constants.P2P.P2P_ACCEPT)) {
                int[] type = intent.getIntArrayExtra("type");
                P2PView.type = type[0];
                P2PView.scale = type[1];
                int Heigh = 0;
                if (P2PView.type == 1 && P2PView.scale == 0) {
                    Heigh = screenWidth * 3 / 4;
                    setIsLand(true);
                } else {
                    Heigh = screenWidth * 9 / 16;
                    setIsLand(false);
                }
                if (ScrrenOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    LinearLayout.LayoutParams parames = new LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                    parames.height = Heigh;
                    r_p2pview.setLayoutParams(parames);
                }
            } else if (intent.getAction().equals(
                    Constants.P2P.ACK_RET_CHECK_PASSWORD)) {
//                finish();
                if (!isCancelLoading) {
                    Intent control = new Intent();
                    control.setClass(mContext, MainControlActivity.class);
                    control.putExtra("contact", mContact);
                    control.putExtra("type", P2PValue.DeviceType.NPC);
                    control.putExtra("deviceInfo", deviceInfo);
                    mContext.startActivity(control);
                }
            } else if (intent.getAction().equals(Constants.P2P.P2P_REJECT)) {
                String error = intent.getStringExtra("error");
                int code = intent.getIntExtra("code", 9);
                showError(error, code);
                isShake = false;
            } else if (intent.getAction().equals(
                    Constants.P2P.RET_GET_REMOTE_DEFENCE)) {
                String ids = intent.getStringExtra("contactId");
                if (!ids.equals("") && ids.equals(callId)) {
                    defenceState = intent.getIntExtra("state", -1);
                    changeDefence(defenceState);
                }
                defence_state.setVisibility(ImageView.VISIBLE);
            } else if (intent.getAction().equals(
                    Constants.P2P.RET_SET_REMOTE_DEFENCE)) {
                int result = intent.getIntExtra("state", -1);
                if (result == 0) {
                    if (defenceState == Constants.DefenceState.DEFENCE_STATE_ON) {
                        defenceState = Constants.DefenceState.DEFENCE_STATE_OFF;
                    } else {
                        defenceState = Constants.DefenceState.DEFENCE_STATE_ON;
                    }
                    changeDefence(defenceState);
                }
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                String error = intent.getStringExtra("error");
                showError(error, 0);
            } else if (intent.getAction().equals(
                    Constants.Action.MONITOR_NEWDEVICEALARMING)) {
                // 弹窗
                /*
                 * int MessageType = intent.getIntExtra("messagetype", 2); int
				 * type = intent.getIntExtra("alarm_type", 0); pushAlarmType =
				 * type; disconnectDooranerfa(); isCustomCmdAlarm =
				 * intent.getBooleanExtra("isCustomCmdAlarm", false); int group
				 * = intent.getIntExtra("group", -1); int item =
				 * intent.getIntExtra("item", -1); boolean isSupport =
				 * intent.getBooleanExtra("isSupport", false); boolean
				 * isSupportDelete = intent.getBooleanExtra( "isSupportDelete",
				 * false); if (MessageType == 1) { // 报警推送 NewMessageDeviceID =
				 * intent.getStringExtra("alarm_id"); if
				 * (alarm_id.equals(NewMessageDeviceID) && passworddialog !=
				 * null && passworddialog.isShowing()) { return; } else {
				 * alarm_id = NewMessageDeviceID; } } else { // 透传推送
				 * NewMessageDeviceID = intent.getStringExtra("contactId"); type
				 * = 13; Log.i("dxsmoniter_alarm", "透传推送" + NewMessageDeviceID);
				 * } String alarmtype = Utils.getAlarmType(type, isSupport,
				 * group, item); StringBuffer NewMassage = new StringBuffer(
				 * Utils.getStringByResouceID(R.string.tab_device))
				 * .append("：").append(
				 * Utils.getDeviceName(NewMessageDeviceID)); NewMassage
				 * .append("\n") .append(Utils
				 * .getStringByResouceID(R.string.allarm_type))
				 * .append(alarmtype); NewMessageDialog(NewMassage.toString(),
				 * NewMessageDeviceID, isSupportDelete);
				 */
                Log.e("警报", "跳转");
                finish();
                Intent alarm = new Intent();
                //alarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                alarm.setClass(mContext, AlarmPictrueActivity.class);
                alarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                alarm.putExtra("deviceid", callId);
                startActivity(alarm);
                Log.e("警报", "跳转2");

            } else if (intent.getAction().equals(Constants.P2P.RET_P2PDISPLAY)) {
                Log.e("monitor", "RET_P2PDISPLAY");
                connectSenconde = true;
                if (!isReceveHeader) {
                    hindRlProTxError();
                    pView.updateScreenOrientation();
                    // iv_full_screen.setVisibility(View.VISIBLE);
                    isReceveHeader = true;
                }
            } else if (intent.getAction().equals(
                    Constants.P2P.DELETE_BINDALARM_ID)) {
                int result = intent.getIntExtra("deleteResult", 1);
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (result == 0) {
                    // 删除成功
                    T.showShort(mContext, R.string.device_set_tip_success);
                } else if (result == -1) {
                    // 不支持
                    T.showShort(mContext, R.string.device_not_support);
                } else {
                    // 失败
                }
            } else if (intent.getAction().equals(
                    Constants.P2P.ACK_GET_REMOTE_DEFENCE)) {
                String contactId = intent.getStringExtra("contactId");
                int result = intent.getIntExtra("result", -1);
                if (contactId.equals(callId)) {
                    if (result == Constants.P2P_SET.ACK_RESULT.ACK_INSUFFICIENT_PERMISSIONS) {
                        isPermission = false;
                    }
                }

            }
        }
    };

    public void changeDefence(int defencestate) {
        if (defencestate == Constants.DefenceState.DEFENCE_STATE_ON) {
            iv_defence.setImageResource(R.drawable.zhzj_sxt_suoding);
            defence_state.setImageResource(R.drawable.deployment);
        } else {
            iv_defence.setImageResource(R.drawable.zhzj_sxt_jiesuo);
            defence_state.setImageResource(R.drawable.disarm);
        }
    }

    /**
     * 隐藏过度页
     */
    private void hindRlProTxError() {
        rlPrgTxError.setVisibility(View.GONE);
    }

    private void showRlProTxError() {
        ObjectAnimator anima = ObjectAnimator.ofFloat(rlPrgTxError, "alpha",
                0f, 1.0f);
        rlPrgTxError.setVisibility(View.VISIBLE);
        anima.setDuration(500).start();
        anima.addListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
    }

    // 设置布防
    public void setDefence() {
        if (!isPermission) {
            T.showShort(mContext, R.string.insufficient_permissions);
            return;
        }
        if (defenceState == Constants.DefenceState.DEFENCE_STATE_ON) {
            P2PHandler.getInstance().setRemoteDefence(mContact.getContactId(),
                    password,
                    Constants.P2P_SET.REMOTE_DEFENCE_SET.ALARM_SWITCH_OFF, MainApplication.GWELL_LOCALAREAIP);

        } else if (defenceState == Constants.DefenceState.DEFENCE_STATE_OFF) {
            P2PHandler.getInstance().setRemoteDefence(mContact.getContactId(),
                    password,
                    Constants.P2P_SET.REMOTE_DEFENCE_SET.ALARM_SWITCH_ON, MainApplication.GWELL_LOCALAREAIP);
            AlertDialog myAlertDialog = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.activity_weight_notice))
                    .setMessage(getString(R.string.camera_dafen_msg))
                    .setPositiveButton(getString(R.string.sure),
                            null).show();
        }

    }

    public void callDevice() {
        P2PConnect.setCurrent_state(P2PConnect.P2P_STATE_CALLING);
        P2PConnect.setCurrent_call_id(callId);

        String push_mesg = NpcCommon.mThreeNum
                + ":"
                + mContext.getResources()
                .getString(R.string.p2p_call_push_mesg);
        Log.e("dxsTest", "NpcCommon.mThreeNum-->" + NpcCommon.mThreeNum
                + "mContact.contactId-->" + mContact.contactId
                + "connectType-->" + connectType + "AppConfig.VideoMode-->"
                + AppConfig.VideoMode);
        if (connectType == Constants.ConnectType.RTSPCONNECT) {
            callType = 3;
            String ipAddress = "";
            String ipFlag = "";
            if (mContact.ipadressAddress != null) {
                ipAddress = mContact.ipadressAddress.getHostAddress();
                ipFlag = ipAddress.substring(ipAddress.lastIndexOf(".") + 1,
                        ipAddress.length());
            } else {

            }
            P2PHandler.getInstance().call(NpcCommon.mThreeNum, "0", true,
                    Constants.P2P_TYPE.P2P_TYPE_MONITOR, "1", "1", push_mesg,
                    AppConfig.VideoMode, mContact.contactId, MainApplication.GWELL_LOCALAREAIP);
            // P2PHandler.getInstance().RTSPConnect(NpcCommon.mThreeNum,
            // mContact.contactPassword, true, callType, mContact.contactId,
            // ipFlag, push_mesg, ipAddress,AppConfig.VideoMode,rtspHandler);
        } else if (connectType == Constants.ConnectType.P2PCONNECT) {
            callType = 1;
            String ipAdress = FList.getInstance().getCompleteIPAddress(
                    mContact.contactId);
            P2PHandler.getInstance().call(NpcCommon.mThreeNum, password, true,
                    Constants.P2P_TYPE.P2P_TYPE_MONITOR, callId, ipAdress,
                    push_mesg, AppConfig.VideoMode, mContact.contactId, MainApplication.GWELL_LOCALAREAIP);
        }
    }

    private void playReady() {
        Intent ready = new Intent();
        ready.setAction(Constants.P2P.P2P_READY);
        this.sendBroadcast(ready);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ScrrenOrientation = Configuration.ORIENTATION_LANDSCAPE;
            layout_title.setVisibility(View.GONE);
            l_control.setVisibility(View.GONE);
            rl_control.setVisibility(View.GONE);
            // 设置control_bottom的高度
            int height = (int) getResources().getDimension(
                    R.dimen.p2p_monitor_bar_height);
            setControlButtomHeight(height);
            control_bottom.setVisibility(View.VISIBLE);
            pView.fullScreen();
            isFullScreen = true;
            LinearLayout.LayoutParams parames = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            r_p2pview.setLayoutParams(parames);

        } else {
            ScrrenOrientation = Configuration.ORIENTATION_PORTRAIT;
            layout_title.setVisibility(View.VISIBLE);
            l_control.setVisibility(View.VISIBLE);
            rl_control.setVisibility(View.VISIBLE);
            format_h.setVisibility(View.VISIBLE);
            iv_full_screen.setVisibility(View.VISIBLE);
            setControlButtomHeight(0);
            control_bottom.setVisibility(View.GONE);
            control_top.setVisibility(View.GONE);
            if (isFullScreen) {
                isFullScreen = false;
                pView.halfScreen();
                Log.e("half", "half screen--");
            }
            if (P2PView.type == 1) {
                if (P2PView.scale == 0) {
                    int Heigh = screenWidth * 3 / 4;
                    LinearLayout.LayoutParams parames = new LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                    parames.height = Heigh;
                    r_p2pview.setLayoutParams(parames);
                } else {
                    int Heigh = screenWidth * 9 / 16;
                    LinearLayout.LayoutParams parames = new LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                    parames.height = Heigh;
                    r_p2pview.setLayoutParams(parames);
                }
            } else {
                if (mContact.contactType == P2PValue.DeviceType.NPC) {
                    int Heigh = screenWidth * 3 / 4;
                    LinearLayout.LayoutParams parames = new LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                    parames.height = Heigh;
                    r_p2pview.setLayoutParams(parames);
                } else {
                    int Heigh = screenWidth * 9 / 16;
                    LinearLayout.LayoutParams parames = new LayoutParams(
                            LayoutParams.MATCH_PARENT,
                            LayoutParams.WRAP_CONTENT);
                    parames.height = Heigh;
                    r_p2pview.setLayoutParams(parames);
                }
            }
            //转横竖屏截图列表不显示，现在这样解决，转竖屏后刷新列表

            showImageList();
            mAdapter.notifyDataSetChanged();
        }
    }

    public void updateVideoModeText(int mode) {
        if (mode == P2PValue.VideoMode.VIDEO_MODE_HD) {
            video_mode_hd.setTextColor(mContext.getResources().getColor(
                    R.color.blue));
            video_mode_sd.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            video_mode_ld.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            choose_video_format.setText(R.string.video_mode_hd);
            format_h.setText(R.string.video_mode_hd);
        } else if (mode == P2PValue.VideoMode.VIDEO_MODE_SD) {
            video_mode_hd.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            video_mode_sd.setTextColor(mContext.getResources().getColor(
                    R.color.blue));
            video_mode_ld.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            choose_video_format.setText(R.string.video_mode_sd);
            format_h.setText(R.string.video_mode_sd);
        } else if (mode == P2PValue.VideoMode.VIDEO_MODE_LD) {
            video_mode_hd.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            video_mode_sd.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            video_mode_ld.setTextColor(mContext.getResources().getColor(
                    R.color.blue));
            choose_video_format.setText(R.string.video_mode_ld);
            format_h.setText(R.string.video_mode_ld);
        }
    }

    @Override
    protected void onP2PViewSingleTap() {
        // TODO Auto-generated method stub
        changeControl();
    }

    @Override
    protected void onP2PViewFilling() {

    }

    private List<RecyclerItemBean> pictrues = null;

    @Override
    protected void onCaptureScreenResult(boolean isSuccess, int prePoint) {
        // TODO Auto-generated method stub
        if (isSuccess) {
            T.showShort(mContext, R.string.capture_success);
            if (ScrrenOrientation != Configuration.ORIENTATION_LANDSCAPE) {
                List<String> path = Utils.getScreenShotImagePath(callId, 1);
                if (path.size() <= 0) {
                    return;
                }
                Utils.saveImgToGallery(path.get(0));
                showImageList();
                mAdapter.notifyDataSetChanged();
            }
        } else {
            T.showShort(mContext, R.string.capture_failed);
        }

    }

    @Override
    protected void onVideoPTS(long videoPTS) {

    }

    @Override
    public int getActivityInfo() {
        // TODO Auto-generated method stub
        return Constants.ActivityInfo.ACTIVITY_APMONITORACTIVITY;
    }

    @Override
    protected void onGoBack() {
        // TODO Auto-generated method stub
        // MainApplication.app.showNotification();
    }

    @Override
    protected void onGoFront() {
        // TODO Auto-generated method stub
        // MainApplication.app.hideNotification();
    }

    @Override
    protected void onExit() {
        // TODO Auto-generated method stub
        // MainApplication.app.hideNotification();
    }

    @Override
    public void onBackPressed() {
        reject();
        finish();
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    mCurrentVolume, 0);
        }
        if (isRegFilter) {
            unregisterReceiver(mReceiver);
            isRegFilter = false;
        }
        if (sensorListener != null) {
            sensorManager.unregisterListener(sensorListener);
        }

        Intent refreshContans = new Intent();
        refreshContans.setAction(Constants.Action.REFRESH_CONTANTS);
        mContext.sendBroadcast(refreshContans);
        if (isRecording) {
            P2PHandler.getInstance().stopRecoding();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            mCurrentVolume++;
            if (mCurrentVolume > mMaxVolume) {
                mCurrentVolume = mMaxVolume;
            }

            if (mCurrentVolume != 0) {
                mIsCloseVoice = false;
                iv_voice.setImageResource(R.drawable.zhzj_sxt_shengyin);
                close_voice.setBackgroundResource(R.drawable.m_voice_on);
            }
            return false;
        } else if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mCurrentVolume--;
            if (mCurrentVolume < 0) {
                mCurrentVolume = 0;
            }

            if (mCurrentVolume == 0) {
                mIsCloseVoice = true;
                iv_voice.setImageResource(R.drawable.zhzj_sxt_jingyin);
                close_voice.setBackgroundResource(R.drawable.m_voice_off);
            }

            return false;
        }

        return super.dispatchKeyEvent(event);
    }

    AnimationDrawable animationDrawable;

    public void updateRecord(int state, boolean isSet) {
    }

    long currTime = 0;
    boolean isOnclick = false;


    public void sendCuiLeiCommand() {
        //判断是否设置催泪面积
        List<CommandInfo> commandInfos = DatabaseOperator.getInstance(mContext).queryCommandsByCT(zhuji.getId(),"108");
        boolean showTip = false;
        if (!CollectionsUtils.isEmpty(commandInfos)){
            CommandInfo commandInfo = commandInfos.get(0);
            if (Integer.parseInt(commandInfo.getCommand()) == 0){
                showTip = true;
            }
        }else{
            showTip = true;
        }
        if (showTip){
            new AlertView(getString(R.string.activity_weight_notice), zhuji.isAdmin() ? getString(R.string.abbq_ges_notice_nomianji) : getString(R.string.abbq_ges_notice_nomianjinoadmin), zhuji.isAdmin() ? getString(R.string.deviceslist_server_leftmenu_delcancel) : getString(R.string.sure),
                    zhuji.isAdmin() ? new String[]{getString(R.string.abbq_ges_notice_toset)} : null, null, mContext, AlertView.Style.Alert,
                    new OnItemClickListener() {

                        @Override
                        public void onItemClick(Object o, final int position) {
                            if (position != -1) {
                                Intent intent = new Intent(mContext, CLDTimeSetActivity.class);
                                intent.putExtra("zhuji",zhuji);
                                startActivity(intent);
                            }
                        }
                    }).show();
        }else{
            final List<CommandInfo> commandPwds = DatabaseOperator.getInstance(mContext).queryCommandsByCT(zhuji.getId(), "pwd_cl");
            if (CollectionsUtils.isEmpty(commandPwds)) {//未设置密码
                new AlertView(getString(R.string.activity_weight_notice), zhuji.isAdmin() ? getString(R.string.abbq_ges_pwd_nomianji) : getString(R.string.abbq_ges_pwd_nomianjinoadmin), getString(R.string.sure),
                        null, null, mContext, AlertView.Style.Alert, null).show();
            } else{
                mAlertViewExt = new AlertView(null, getString(R.string.abbq_cld_pwd_title), getString(R.string.cancel), null, new String[]{getString(R.string.sure)}, mContext, AlertView.Style.Alert, new OnItemClickListener() {
                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position != -1){ //确认密码
                            if (etName.getText().length() != 6){
                                Toast.makeText(mContext,R.string.abbq_update_cld_pwd_length,Toast.LENGTH_SHORT).show();
                            }else if (etName.getText().toString().equals(commandPwds.get(0).getCommand())) {
                                SyncMessage message1 = new SyncMessage();
                                message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
                                message1.setDeviceid(zhuji.getId());
                                // 操作
                                showInProgress(getString(R.string.cld_send_ing));
                                handler.sendEmptyMessageDelayed(dHandler_timeout, 10 * 1000);
                                message1.setSyncBytes(new byte[]{(byte) 101}); //主机固定101 发射催泪弹
                                SyncMessageContainer.getInstance().produceSendMessage(message1);
                            }else{
                                Toast.makeText(mContext,R.string.password_error,Toast.LENGTH_SHORT).show();
                            }
                        }
                        //关闭软键盘
                        imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
                        //恢复位置
                        mAlertViewExt.setMarginBottom(0);
                    }
                });
                ViewGroup extView = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.alert_password_form, null);
                etName = (EditText) extView.findViewById(R.id.etName);
                CheckBox cbLaws = (CheckBox) extView.findViewById(R.id.cbLaws);
                etName.setText("");
                etName.setHint(getString(R.string.abbq_cld_pwd_title));
                etName.setInputType(InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                cbLaws.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String psw = etName.getText().toString();

                        if (isChecked) {
                            etName.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                        } else {
                            etName.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                        }
                        etName.setSelectAllOnFocus(true);
                        etName.setSelection(psw.length());
                    }
                });
                etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean focus) {
                        //输入框出来则往上移动
                        boolean isOpen = imm.isActive();
                        mAlertViewExt.setMarginBottom(isOpen && focus ? 120 : 0);
                    }
                });
                mAlertViewExt.addExtView(extView);
                mAlertViewExt.show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_cld:
                sendCuiLeiCommand();
                break;
            case R.id.iv_full_screen:
                ScrrenOrientation = Configuration.ORIENTATION_LANDSCAPE;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case R.id.defence_state:
                setDefence();
                break;
            case R.id.iv_recode:
                //录像
                startMoniterRecoding();
                break;
            case R.id.iv_file:
                //录像文件
                Intent intent = new Intent();
                intent.putExtra("contact", mContact);
                intent.putExtra("flag", getIntent().getBooleanExtra("flag", false));
                intent.setClass(ApMonitorActivity.this, LocalRecordFilesActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.iv_defence:
                setDefence();
                break;
            case R.id.close_voice:
            case R.id.iv_vioce:
                Log.e("音量", "被点击了");
                if (mIsCloseVoice) {
                    mIsCloseVoice = false;
                    iv_voice.setImageResource(R.drawable.zhzj_sxt_shengyin);
                    close_voice.setBackgroundResource(R.drawable.m_voice_on);
                    if (mCurrentVolume == 0) {
                        mCurrentVolume = 1;
                    }
                    if (mAudioManager != null) {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                mCurrentVolume, 0);
                    }
                } else {
                    mIsCloseVoice = true;
                    iv_voice.setImageResource(R.drawable.zhzj_sxt_jingyin);
                    close_voice.setBackgroundResource(R.drawable.m_voice_off);
                    if (mAudioManager != null) {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
                                0);
                    }
                }
                break;
            case R.id.screenshot:
            case R.id.iv_screenshot:
                this.captureScreen(-1);
                break;
            case R.id.hungup:
            case R.id.back_btn:
                reject();
                finish();
                break;
            case R.id.choose_video_format_h:
                //不用break是为了直接执行下面的，操作一样的
            case R.id.choose_video_format:
                changevideoformat();
                break;
            case R.id.iv_half_screen:
                control_bottom.setVisibility(View.INVISIBLE);
                ScrrenOrientation = Configuration.ORIENTATION_PORTRAIT;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case R.id.video_mode_hd:
                if (current_video_mode != P2PValue.VideoMode.VIDEO_MODE_HD) {
                    current_video_mode = P2PValue.VideoMode.VIDEO_MODE_HD;
                    P2PHandler.getInstance().setVideoMode(
                            P2PValue.VideoMode.VIDEO_MODE_HD);
                    updateVideoModeText(current_video_mode);
                }
                hideVideoFormat();
                break;
            case R.id.video_mode_sd:
                if (current_video_mode != P2PValue.VideoMode.VIDEO_MODE_SD) {
                    current_video_mode = P2PValue.VideoMode.VIDEO_MODE_SD;
                    P2PHandler.getInstance().setVideoMode(
                            P2PValue.VideoMode.VIDEO_MODE_SD);
                    updateVideoModeText(current_video_mode);
                }
                hideVideoFormat();
                break;
            case R.id.video_mode_ld:
                if (current_video_mode != P2PValue.VideoMode.VIDEO_MODE_LD) {
                    current_video_mode = P2PValue.VideoMode.VIDEO_MODE_LD;
                    P2PHandler.getInstance().setVideoMode(
                            P2PValue.VideoMode.VIDEO_MODE_LD);
                    updateVideoModeText(current_video_mode);
                }
                hideVideoFormat();
                break;
            case R.id.rl_prgError:
            case R.id.btn_refrash:
                if (btnRefrash.getVisibility() == View.VISIBLE) {
                    hideError();
                    callDevice();
                }
                break;
            case R.id.iv_next:
                switchNext();
                break;
            case R.id.iv_last:
                switchLast();
                break;
            case R.id.tv_choosee_device:
                if (isShowDeviceList) {
                    l_device_list.setVisibility(View.GONE);
                    isShowDeviceList = false;
                } else {
                    l_device_list.setVisibility(View.VISIBLE);
                    isShowDeviceList = true;
                }
                break;
            case R.id.tv_moreinfo:
                intent = new Intent();
                intent.setClass(mContext.getApplicationContext(), IPCZhujiDetailActivity.class);
                intent.putExtra("device", deviceInfo);
                startActivity(intent);
                break;
            case R.id.tv_setting:
                dialog = new NormalDialog(mContext);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface arg0) {
                        isCancelLoading = true;
                    }
                });
                dialog.showLoadingDialog2();
                dialog.setCanceledOnTouchOutside(false);

                isCancelLoading = false;
                P2PHandler.getInstance().checkPassword(mContact.contactId,
                        mContact.getContactPassword(), MainApplication.GWELL_LOCALAREAIP);
                break;
            case R.id.open_door:
                openDor();
                break;
            case R.id.iv_speak:
            case R.id.send_voice:
                if (!isSpeak) {
                    speak();
                } else {
                    noSpeak();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        reject();
    }

    // 设置成对话状态
    private void speak() {
        hideVideoFormat();
        layout_voice_state.setVisibility(RelativeLayout.VISIBLE);
        send_voice.setBackgroundResource(R.drawable.icon_camere_speak_p);
        // iv_speak.setBackgroundResource(R.drawable.portrait_speak_p);
        setMute(false);
        isSpeak = true;
        Log.e("leleSpeak", "speak--" + isSpeak);
    }

    private void noSpeak() {
        send_voice.setBackgroundResource(R.drawable.icon_camere_speak);
        iv_speak.setBackgroundResource(R.drawable.portrait_disarm);
        // layout_voice_state.setVisibility(RelativeLayout.GONE);
        setMute(true);
        isSpeak = false;
        mHandler.postDelayed(mrunnable, 500);
        Log.e("leleSpeak", "no speak--" + isSpeak);
    }

    private boolean isFirstMute = true;
    Runnable mrunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (isFirstMute) {
                Log.e("leleSpeak", "mrunnable--");
                send_voice.performClick();
                isFirstMute = false;
            }
        }
    };

    public void stopSpeak() {
        send_voice.setBackgroundResource(R.drawable.icon_camere_speak);
        iv_speak.setBackgroundResource(R.drawable.portrait_disarm);
        layout_voice_state.setVisibility(RelativeLayout.GONE);
        setMute(true);
        isSpeak = false;
    }

    /**
     * 开门
     */
    private void openDor() {
        NormalDialog dialog = new NormalDialog(mContext, mContext
                .getResources().getString(R.string.open_door), mContext
                .getResources().getString(R.string.confirm_open_door), mContext
                .getResources().getString(R.string.yes), mContext
                .getResources().getString(R.string.no));
        dialog.setOnButtonOkListener(new NormalDialog.OnButtonOkListener() {

            @Override
            public void onClick() {
                if (isCustomCmdAlarm == true) {
                    String cmd = "IPC1anerfa:unlock";
                    P2PHandler.getInstance().sendCustomCmd(callId, password,
                            cmd, MainApplication.GWELL_LOCALAREAIP);
                } else {
                    P2PHandler.getInstance().setGPIO1_0(callId, password, MainApplication.GWELL_LOCALAREAIP);
                }
            }
        });
        dialog.showDialog();
    }


    public Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub.
            pView.updateScreenOrientation();
            return false;
        }
    });


    /**
     * 展示连接错误
     *
     * @param error
     */
    public void showError(String error, int code) {
        if (!connectSenconde && code != 9) {
            callDevice();
            connectSenconde = true;
            return;
        }
        progressBar.setVisibility(View.GONE);
        tx_wait_for_connect.setVisibility(View.GONE);
        txError.setVisibility(View.VISIBLE);
        btnRefrash.setVisibility(View.VISIBLE);
        txError.setText(error);
    }

    /**
     * 隐藏连接错误
     */
    private void hideError() {
        progressBar.setVisibility(View.VISIBLE);
        tx_wait_for_connect.setText(getResources().getString(
                R.string.waite_for_linke));
        tx_wait_for_connect.setVisibility(View.VISIBLE);
        txError.setVisibility(View.GONE);
        btnRefrash.setVisibility(View.GONE);
    }

    /**
     * 切换连接
     */
    private void switchConnect() {
        progressBar.setVisibility(View.VISIBLE);
        tx_wait_for_connect.setText(getResources().getString(
                R.string.switch_connect));
        tx_wait_for_connect.setVisibility(View.VISIBLE);
        txError.setVisibility(View.GONE);
        btnRefrash.setVisibility(View.GONE);
        // iv_full_screen.setVisibility(View.INVISIBLE);
        showRlProTxError();
    }

    public void reject() {
        if (!isReject) {
            isReject = true;
            P2PHandler.getInstance().finish();
            finish();
        }
    }

    public void readyCallDevice() {
        if (connectType == Constants.ConnectType.P2PCONNECT) {
            P2PHandler.getInstance().openAudioAndStartPlaying(1);
            P2PHandler.getInstance().getDefenceStates(callId, password, MainApplication.GWELL_LOCALAREAIP);
        } else {
            P2PHandler.getInstance().openAudioAndStartPlaying(1);
            callId = "1";
            password = "0";
            P2PHandler.getInstance().getDefenceStates(callId, password, MainApplication.GWELL_LOCALAREAIP);
        }

    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                T.showShort(mContext, R.string.press_again_monitor);
                exitTime = System.currentTimeMillis();
            } else {
                reject();
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void changevideoformat() {
        if (control_top.getVisibility() == RelativeLayout.VISIBLE) {
            Animation anim2 = AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_out);
            anim2.setDuration(100);
            control_top.startAnimation(anim2);
            control_top.setVisibility(RelativeLayout.GONE);
            isShowVideo = false;
        } else {
            Animation anim2 = AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_in);
            anim2.setDuration(100);
            control_top.setVisibility(RelativeLayout.VISIBLE);
            control_top.startAnimation(anim2);
            isShowVideo = true;
        }
    }

    public void hideVideoFormat() {
        if (control_top.getVisibility() == RelativeLayout.VISIBLE) {
            Animation anim2 = AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_out);
            anim2.setDuration(100);
            control_top.startAnimation(anim2);
            control_top.setVisibility(RelativeLayout.GONE);
            isShowVideo = false;
        }
    }

    public void changeControl() {
        if (isSpeak) {// 对讲过程中不可消失
            return;
        }
        if (ScrrenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            return;
        }
        Log.e("changeControl", "changeControl");
        if (control_bottom.getVisibility() == RelativeLayout.VISIBLE) {
            Log.e("changeControl", "changeControl--VISIBLE");
            Animation anim2 = AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_out);
            anim2.setDuration(100);
            control_bottom.startAnimation(anim2);
            anim2.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation arg0) {
                    // TODO Auto-generated method stub
                    hideVideoFormat();
                    choose_video_format.setClickable(false);
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    // TODO Auto-generated method stub
                    hideVideoFormat();
                    control_bottom.setVisibility(RelativeLayout.INVISIBLE);
                    choose_video_format
                            .setBackgroundResource(R.drawable.sd_backgroud);
                    choose_video_format.setClickable(true);
                }
            });

        } else {
            Log.e("changeControl", "changeControl--INVISIBLE");
            control_bottom.setVisibility(RelativeLayout.VISIBLE);
            control_bottom.bringToFront();
            Animation anim2 = AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_in);
            anim2.setDuration(100);
            control_bottom.startAnimation(anim2);
            anim2.setAnimationListener(new AnimationListener() {

                @Override
                public void onAnimationStart(Animation arg0) {
                    // TODO Auto-generated method stub
                    hideVideoFormat();
                    choose_video_format.setClickable(false);
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    // TODO Auto-generated method stub
                    hideVideoFormat();
                    choose_video_format.setClickable(true);
                }
            });
        }
    }

    /**
     * 新报警信息
     */
    NormalDialog dialog;
    String contactidTemp = "";

    private void NewMessageDialog(String Meassage, final String contacid,
                                  boolean isSurportdelete) {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
        dialog = new NormalDialog(mContext);
        dialog.setContentStr(Meassage);
        dialog.setbtnStr1(R.string.check);
        dialog.setbtnStr2(R.string.cancel);
        dialog.setbtnStr3(R.string.clear_bundealarmid);
        dialog.showAlarmDialog(isSurportdelete, contacid);
        dialog.setOnAlarmClickListner(AlarmClickListner);
        contactidTemp = contacid;
    }

    /**
     * 监控对话框单击回调
     */
    private OnAlarmClickListner AlarmClickListner = new OnAlarmClickListner() {

        @Override
        public void onOkClick(String alarmID, boolean isSurportDelete,
                              Dialog dialog) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            // 查看新监控--挂断当前监控，再次呼叫另一个监控
            seeMonitor(alarmID);
        }

        @Override
        public void onDeleteClick(String alarmID, boolean isSurportDelete,
                                  Dialog dialog) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            DeleteDevice(alarmID);
        }

        @Override
        public void onCancelClick(String alarmID, boolean isSurportDelete,
                                  Dialog dialog) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    };

    // 解绑确认弹框
    private void DeleteDevice(final String alarmId) {
        dialog = new NormalDialog(mContext, mContext.getResources().getString(
                R.string.clear_bundealarmid), mContext.getResources()
                .getString(R.string.clear_bundealarmid_tips), mContext
                .getResources().getString(R.string.sure), mContext
                .getResources().getString(R.string.cancel));
        dialog.setOnButtonOkListener(new NormalDialog.OnButtonOkListener() {

            @Override
            public void onClick() {
                P2PHandler.getInstance().DeleteDeviceAlarmId(
                        String.valueOf(alarmId), MainApplication.GWELL_LOCALAREAIP);
                dialog.dismiss();
                ShowLoading();
            }
        });
        dialog.showDialog();
    }

    private void ShowLoading() {
        dialog = new NormalDialog(mContext);
        dialog.showLoadingDialog();
    }

    private void seeMonitor(String contactId) {
        number = 1;
        final Contact contact = FList.getInstance().isContact(contactId);
        if (null != contact) {
            P2PHandler.getInstance().reject();
            switchConnect();
            changeDeviceListTextColor();
            callId = contact.contactId;
            password = contact.contactPassword;
            tv_name.setText(callId);
            if (isSpeak) {
                stopSpeak();
            }
            setHeaderImage();
            if (pushAlarmType == P2PValue.AlarmType.ALARM_TYPE_DOORBELL_PUSH) {
                initSpeark(contact.contactType, true);
            } else {
                initSpeark(contact.contactType, false);
            }
            connectDooranerfa();
            callDevice();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            frushLayout(P2PValue.DeviceType.IPC);
        } else {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Log.i("dxsmonitor", contactId);
            createPassDialog(contactId);
        }
    }

    private Dialog passworddialog;

    void createPassDialog(String id) {
        passworddialog = new MyInputPassDialog(mContext,
                Utils.getStringByResouceID(R.string.check), id, listener);
        passworddialog.show();
    }

    private OnCustomDialogListener listener = new OnCustomDialogListener() {

        @Override
        public void check(final String password, final String id) {
            if (password.trim().equals("")) {
                T.showShort(mContext, R.string.input_monitor_pwd);
                return;
            }

            if (password.length() > 30 || password.charAt(0) == '0') {
                T.showShort(mContext, R.string.device_password_invalid);
                return;
            }

            P2PConnect.vReject(9, "");
            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        if (P2PConnect.getCurrent_state() == P2PConnect.P2P_STATE_NONE) {
                            Message msg = new Message();
                            String pwd = P2PHandler.getInstance()
                                    .EntryPassword(password);
                            String[] data = new String[]{id, pwd,
                                    String.valueOf(pushAlarmType)};
                            msg.what = 1;
                            msg.obj = data;
                            handler.sendMessage(msg);
                            break;
                        }
                        Utils.sleepThread(500);
                    }
                }
            }.start();

        }
    };
    Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            // TODO Auto-generated method stub
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            if (msg.what == 0) {
                Contact contact = (Contact) msg.obj;
                Intent monitor = new Intent(mContext, ApMonitorActivity.class);
                monitor.putExtra("contact", contact);
                monitor.putExtra("connectType",
                        Constants.ConnectType.P2PCONNECT);
                startActivity(monitor);

            } else if (msg.what == 1) {
                if (passworddialog != null && passworddialog.isShowing()) {
                    passworddialog.dismiss();
                }
                String[] data = (String[]) msg.obj;
                P2PHandler.getInstance().reject();
                switchConnect();
                changeDeviceListTextColor();
                callId = data[0];
                password = data[1];
                tv_name.setText(callId);
                if (isSpeak) {
                    stopSpeak();
                }
                setHeaderImage();
                if (pushAlarmType == P2PValue.AlarmType.ALARM_TYPE_DOORBELL_PUSH) {
                    initSpeark(P2PValue.DeviceType.DOORBELL, true);
                    Log.e("leleMonitor", "switch doorbell push");
                } else {
                    initSpeark(P2PValue.DeviceType.IPC, false);
                    Log.e("leleMonitor", "switch---");
                }
                connectDooranerfa();
                callDevice();
                frushLayout(P2PValue.DeviceType.IPC);

            } else if (msg.what == dHandler_timeout) {
                cancelInProgress();
                Toast.makeText(mContext, getString(R.string.time_out), Toast.LENGTH_SHORT).show();
            } else if(msg.what == dHandler_daojishi){
                if (controlConfrimAlert!=null && controlConfrimAlert.isShowing()){
                    LinearLayout loAlertButtons = (LinearLayout) controlConfrimAlert.getContentContainer().findViewById(R.id.loAlertButtons);
                    TextView textView = (TextView) loAlertButtons.getChildAt(2).findViewById(R.id.tvAlert); //获取到按钮
                    textView.setText(getString(R.string.sure)+"("+msg.arg1+")");
                    if (msg.arg1 <= 0){
                        textView.setClickable(false);
                        textView.setTextColor(getResources().getColor(R.color.gray));
                    }else {
                        handler.sendMessageDelayed(handler.obtainMessage(dHandler_daojishi, msg.arg1 - 1, 0), 1000);
                    }
                }else{
                    handler.removeMessages(dHandler_daojishi);
                }
            }
            return false;
        }
    });
    boolean isRecording;

    public void startMoniterRecoding() {
        String pathName = "";
        if (isRecording) {

            iv_recode.setBackgroundResource(R.drawable.zhzj_sxt_luxiang);
            isRecording = false;
            P2PHandler.getInstance().stopRecoding();
            return;
        }
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ||
                    Environment.getExternalStorageState().equals(Environment.MEDIA_SHARED)) {
                String path = getCameraPath(this) + mContact.contactId;
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdirs();
                }
                long time = System.currentTimeMillis();
                pathName = file.getPath() + File.separator + System.currentTimeMillis() + ".mp4";
            } else {
                throw new NoSuchFieldException("sd卡");
            }
        } catch (NoSuchFieldException | NullPointerException e) {
            Toast.makeText(ApMonitorActivity.this, getResources().getString(R.string.activity_error_nosdcard), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        if (P2PHandler.getInstance().starRecoding(pathName)) {
            isRecording = true;
            iv_recode.setBackgroundResource(R.drawable.recor_icon_anim);
            animationDrawable = (AnimationDrawable) iv_recode.getBackground();
            animationDrawable.start();
            Toast.makeText(ApMonitorActivity.this, getResources().getString(R.string.activity_recording), Toast.LENGTH_SHORT).show();
        } else {
            //录像初始化失败
            Toast.makeText(ApMonitorActivity.this, getResources().getString(R.string.activity_error_initrecord), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onHomePressed() {
        // TODO Auto-generated method stub
        super.onHomePressed();
        reject();
        finish();
    }

    public void getScreenWithHeigh() {
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeigh = dm.heightPixels;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /*
     * 初始化P2pview
     */
    public void initp2pView() {
        initP2PView(7, P2PView.LAYOUTTYPE_TOGGEDER);
        WindowManager manager = getWindowManager();
        window_width = manager.getDefaultDisplay().getWidth();
        window_height = manager.getDefaultDisplay().getHeight();
        this.initScaleView(this, window_width, window_height);
        setMute(true);
    }

    public void initIpcDeviceList() {
        LinearLayout.LayoutParams p = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        p.height = dip2px(mContext, 40 * number);
        l_device_list.setLayoutParams(p);
        for (int i = 0; i < number; i++) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.item_device, null);
            final TextView tv_deviceId = (TextView) view
                    .findViewById(R.id.tv_deviceId);
            tv_deviceId.setText(ipcList[i]);
            if (i == 0) {
                tv_deviceId.setTextColor(getResources().getColor(R.color.blue));
            } else {
                tv_deviceId
                        .setTextColor(getResources().getColor(R.color.white));
            }
            devicelist.add(tv_deviceId);
            l_device_list.addView(view);
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    for (int i = 0; i < ipcList.length; i++) {
                        if (ipcList[i].equals(String.valueOf(Integer.parseInt(tv_deviceId.getText()
                                .toString())))){
                            currentNumber = i;
                            P2PHandler.getInstance().reject();
                            changeDeviceListTextColor();
                            callId = ipcList[currentNumber];
                            callDevice();
                        }
                    }
                }
            });
        }
    }

    public void changeDeviceListTextColor() {
        for (int i = 0; i < devicelist.size(); i++) {
            if (i == currentNumber) {
                devicelist.get(i).setTextColor(
                        getResources().getColor(R.color.blue));
                devicelist.get(i).setClickable(false);
            } else {
                devicelist.get(i).setTextColor(
                        getResources().getColor(R.color.white));
                devicelist.get(i).setClickable(true);
            }
        }

    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public void switchNext() {
        if (currentNumber < number - 1) {
            currentNumber = currentNumber + 1;
        } else {
            currentNumber = 0;
        }
        P2PHandler.getInstance().reject();
        switchConnect();
        changeDeviceListTextColor();
        callId = ipcList[currentNumber];
        tv_name.setText(callId);
        setHeaderImage();
    }

    public void switchLast() {
        if (currentNumber > 0) {
            currentNumber = currentNumber - 1;
        } else {
            currentNumber = number - 1;
        }
        P2PHandler.getInstance().reject();
        switchConnect();
        changeDeviceListTextColor();
        callId = ipcList[currentNumber];
        tv_name.setText(callId);
        setHeaderImage();
        callDevice();
    }

    public void connectDooranerfa() {
        if (isCustomCmdAlarm == true) {
            String cmd_connect = "IPC1anerfa:connect";
            P2PHandler.getInstance().sendCustomCmd(callId, password,
                    cmd_connect, MainApplication.GWELL_LOCALAREAIP);
        }
    }

    public void disconnectDooranerfa() {
        if (isCustomCmdAlarm == true) {
            String cmd_disconnect = "IPC1anerfa:disconnect";
            P2PHandler.getInstance().sendCustomCmd(callId, password,
                    cmd_disconnect, MainApplication.GWELL_LOCALAREAIP);
        }
    }

    public void setControlButtomHeight(int height) {
        LinearLayout.LayoutParams control_bottom_parames = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        control_bottom_parames.height = height;
        control_bottom.setLayoutParams(control_bottom_parames);
    }

    @Override
    public void onRecycleItemClick(View view, int position) {
        //点击事件
        String path = (String) pictrues.get(position).getT();
        Intent in = new Intent();
        in.setClass(mContext, ImageSeeActivity.class);
        in.putExtra("flag", getIntent().getBooleanExtra("flag", false));
        in.putExtra("startactivity", 1);
        in.putExtra("path", path);
        in.putExtra("number", 0);
        in.putExtra("contact", mContact);
        in.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
        startActivity(in);
        finish();
    }

    @Override
    public boolean onRecycleItemLongClick(View view, final int position) {
        //长按事件
        final String path = (String) pictrues.get(position).getT();
        NormalDialog dialog = new NormalDialog(this, getResources().getString(R.string.delete), getResources().getString(R.string.confirm_delete),
                getResources().getString(R.string.delete),
                getResources().getString(R.string.cancel));
        dialog.setOnButtonOkListener(new NormalDialog.OnButtonOkListener() {

            @Override
            public void onClick() {
                // TODO Auto-generated method stub
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                    pictrues.remove(position);
                    mAdapter.notifyItemRemoved(position);
                }
            }
        });
        dialog.showDialog();
        return true;
    }
}
