package com.smartism.znzk.activity.camera;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.p2p.core.P2PView;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentMonitorActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.camera.P2PConnect;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.global.AppConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.view.P2PSquareLayout;
import com.smartism.znzk.widget.HeaderView;
import com.smartism.znzk.widget.MyInputPassDialog;
import com.smartism.znzk.widget.MyInputPassDialog.OnCustomDialogListener;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * 技威的单独监控视频播放的activity
 *
 * @author 2016年08月09日 update by 王建
 */
public class PlayBaseActivity extends ActivityParentMonitorActivity implements
        OnClickListener {
    private boolean isFirstCreate = true;//标示是不是首次创建
    protected View p2pLayoutView; //播放摄像头的view
    public TextView users;
    P2PSquareLayout camera_layout;
    RelativeLayout control_bottom;
    LinearLayout control_top;
    Button choose_video_format;
    TextView video_mode_hd, video_mode_sd, video_mode_ld;
    ImageView close_voice, send_voice, screenshot, btn_play, voice_state;
    LinearLayout layout_voice_state;
    RelativeLayout r_p2pview;
    protected Contact mContact;
    int callType = 3;
    public Context mContext;
    boolean isReject = false;
    boolean isRegFilter = false;
    private int ScrrenOrientation;
    int window_width, window_height;
    String callId, password;
    int connectType;
    private int defenceState = -1;//布防状态
    boolean mIsCloseVoice = false;
    int mCurrentVolume, mMaxVolume;
    public AudioManager mAudioManager;
    boolean isSurpportOpenDoor = false;
    boolean isShowVideo = false;
    boolean isSpeak = false; //是否在对讲
    int current_video_mode;
    int screenWidth;
    int screenHeigh;
    // 刷新监控部分
    private RelativeLayout rlPrgTxError;
    private TextView txError, tx_wait_for_connect;
    private Button btnRefrash;
    private ProgressBar progressBar;
    private HeaderView ivHeader;
    private String[] ipcList;
    int number;
    int currentNumber = 0;
    private static final int UPTATE_INTERVAL_TIME = 70;
    private static final int SPEED_SHRESHOLD = 2000;
    private boolean isReceveHeader = false;
    boolean isPermission = true;
    private View vLineHD;
    private boolean connectSenconde = false;
    int pushAlarmType;
    boolean isCustomCmdAlarm = false;
    private String idOrIp;
    private boolean isShowCamera = true;
    protected boolean isPlay = false;//标示是否进入过播放

    public void setShowCamera(boolean showCamera) {
        isShowCamera = showCamera;
    }

    public void setIvHeader(int rec) {
        if (ivHeader != null) {
            ivHeader.setBackgroundResource(rec);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mContact = (Contact) getIntent().getSerializableExtra("contact");
        isShowCamera = getIntent().getBooleanExtra("showcamera",true);
    }

    private void initCameraEvent() {
        btn_play.setOnClickListener(this);
        rlPrgTxError.setOnClickListener(this);
        btnRefrash.setOnClickListener(this);
        choose_video_format.setOnClickListener(this);
        close_voice.setOnClickListener(this);
        send_voice.setOnClickListener(this);
        screenshot.setOnClickListener(this);
        video_mode_hd.setOnClickListener(this);
        video_mode_sd.setOnClickListener(this);
        video_mode_ld.setOnClickListener(this);

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
        send_voice.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                int time = 0;
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
    }

    private void initCameraView() {
        users = (TextView) findViewById(R.id.users);
        camera_layout = (P2PSquareLayout) findViewById(R.id.camera_layout);
        camera_layout.setVisibility(View.VISIBLE);
        pView = (P2PView) findViewById(R.id.p2pview);
        pView.setVisibility(View.VISIBLE);
        P2PView.type = 0;
        btn_play = (ImageView) findViewById(R.id.btn_play);
        control_bottom = (RelativeLayout) findViewById(R.id.control_bottom);
        control_top = (LinearLayout) findViewById(R.id.control_top);
        video_mode_hd = (TextView) findViewById(R.id.video_mode_hd);
        video_mode_sd = (TextView) findViewById(R.id.video_mode_sd);
        video_mode_ld = (TextView) findViewById(R.id.video_mode_ld);

        vLineHD = findViewById(R.id.v_line_hd);

        choose_video_format = (Button) findViewById(R.id.choose_video_format);
        close_voice = (ImageView) findViewById(R.id.close_voice);
        send_voice = (ImageView) findViewById(R.id.send_voice);
        layout_voice_state = (LinearLayout) findViewById(R.id.layout_voice_state);
        screenshot = (ImageView) findViewById(R.id.screenshot);
        r_p2pview = (RelativeLayout) findViewById(R.id.r_p2pview);
        voice_state = (ImageView) findViewById(R.id.voice_state);

        rlPrgTxError = (RelativeLayout) findViewById(R.id.rl_prgError);
        txError = (TextView) findViewById(R.id.tx_monitor_error);
        btnRefrash = (Button) findViewById(R.id.btn_refrash);
        progressBar = (ProgressBar) findViewById(R.id.prg_monitor);
        tx_wait_for_connect = (TextView) findViewById(R.id.tx_wait_for_connect);
        ivHeader = (HeaderView) findViewById(R.id.hv_header);
    }

    private void start() {
        if (mContact == null) {
            //提示
            return;
        }
        isPlay = true;
        btn_play.setVisibility(View.GONE);
        tx_wait_for_connect.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
//        if (mContact.contactType == P2PValue.DeviceType.IPC) {
//            setIsLand(false);//默认设置非全屏
//        } else {
//            setIsLand(true);
//        }
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

        updateVideoModeText(current_video_mode);
        password = P2PHandler.getInstance().EntryPassword(mContact.getContactPassword());
//        P2PHandler.getInstance().p2pInit(mContext, new P2PListener(), new SettingListener());// 设置在监控
        getScreenWithHeigh();

        callDevice();
//        initcComponent();
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
        P2PHandler.getInstance().getNpcSettings(mContact.contactId, mContact.getContactPassword(), MainApplication.GWELL_LOCALAREAIP);
    }

    private void initcComponent() {
        initCameraView();
        initCameraEvent();
        regP2pFilter();
        // 更新头像
        updateHeaderImage();
    }

    /**
     * 子类如果需要修改初始化显示的第一张图片，请重写此方法
     */
    public void updateHeaderImage(){
        setHeaderImage();
    }

    public void initSpeark(int deviceType, boolean isOpenDor) {
        if (deviceType != P2PValue.DeviceType.DOORBELL && !isOpenDor) {
            send_voice.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
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
        } else if (deviceType == P2PValue.DeviceType.DOORBELL && !isOpenDor) {
            isFirstMute = false;
            send_voice.setOnTouchListener(null);
            send_voice.setOnClickListener(this);
        } else if (isOpenDor) {
            send_voice.setOnTouchListener(null);
            control_bottom.setVisibility(View.VISIBLE);
            // 开始监控时没有声音，暂时这样
            send_voice.setOnClickListener(this);
            isFirstMute = true;
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

    private void regP2pFilter() {
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
    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            Log.e("BroadcastReceiver_1111", intent.getAction());

            if (intent.getAction().equals(
                    Constants.P2P.P2P_MONITOR_NUMBER_CHANGE)) {
                int number = intent.getIntExtra("number", -1);
                if (number != -1) {
                    users.setText(mContext.getResources().getString(
                            R.string.monitor_number)
                            + " " + P2PConnect.getNumber());
                }
            }else if (intent.getAction().equals(Constants.P2P.RET_GET_RECORD_PLAN_TIME)) {
                planTime = intent.getStringExtra("time");

            } else if (intent.getAction().equals(Constants.P2P.RET_SET_RECORD_TYPE)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Constants.P2P_SET.RECORD_TYPE_SET.SETTING_SUCCESS) {
                    currentdType = result;
                }
            } else if (intent.getAction().equals(Constants.P2P.RET_SET_REMOTE_RECORD)) {
                state = intent.getIntExtra("result", -1);
                P2PHandler.getInstance().getNpcSettings(mContact.contactId, mContact.getContactPassword(), MainApplication.GWELL_LOCALAREAIP);
//                updateRecord(state,true);
            } else if (intent.getAction().equals(Constants.P2P.P2P_READY)) {
                P2PHandler.getInstance().getDefenceStates(callId, password, MainApplication.GWELL_LOCALAREAIP);
                P2PConnect.setPlayingContact(mContact);
                isReceveHeader = false;
                pView.halfScreen();
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
                finish();
            } else if (intent.getAction().equals(Constants.P2P.P2P_REJECT)) {
                String error = intent.getStringExtra("error");
                int code = intent.getIntExtra("code", 9);
                showError(error, code);
            } else if (intent.getAction().equals(
                    Constants.P2P.RET_GET_REMOTE_DEFENCE)) {
                String ids = intent.getStringExtra("contactId");
                if (!ids.equals("") && ids.equals(callId)) {
                    defenceState = intent.getIntExtra("state", 0);
                }
            } else if (intent.getAction().equals(
                    Constants.P2P.RET_SET_REMOTE_DEFENCE)) {
                int result = intent.getIntExtra("state", -1);
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                String error = intent.getStringExtra("error");
                showError(error, 0);
            } else if (intent.getAction().equals(
                    Constants.Action.MONITOR_NEWDEVICEALARMING)) {
                finish();
            } else if (intent.getAction().equals(Constants.P2P.RET_P2PDISPLAY)) {
                Log.e("monitor", "RET_P2PDISPLAY");
                connectSenconde = true;
                if (!isReceveHeader) {
                    hindRlProTxError();
                    pView.updateScreenOrientation();
                    isReceveHeader = true;
                }
            } else if (intent.getAction().equals(
                    Constants.P2P.DELETE_BINDALARM_ID)) {
                int result = intent.getIntExtra("deleteResult", 1);
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
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
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
        } else if (connectType == Constants.ConnectType.P2PCONNECT) {
            callType = 1;
            String ipAdress = FList.getInstance().getCompleteIPAddress(
                    mContact.contactId);
            P2PHandler.getInstance().call(NpcCommon.mThreeNum, password, true,
                    Constants.P2P_TYPE.P2P_TYPE_MONITOR, callId, ipAdress,
                    push_mesg, AppConfig.VideoMode, mContact.contactId, MainApplication.GWELL_LOCALAREAIP);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
        } else if (mode == P2PValue.VideoMode.VIDEO_MODE_SD) {
            video_mode_hd.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            video_mode_sd.setTextColor(mContext.getResources().getColor(
                    R.color.blue));
            video_mode_ld.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            choose_video_format.setText(R.string.video_mode_sd);
        } else if (mode == P2PValue.VideoMode.VIDEO_MODE_LD) {
            video_mode_hd.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            video_mode_sd.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            video_mode_ld.setTextColor(mContext.getResources().getColor(
                    R.color.blue));
            choose_video_format.setText(R.string.video_mode_ld);
        }
    }


    @Override
    protected void onCaptureScreenResult(boolean isSuccess, int prePoint) {
        if (isSuccess) {
            T.showShort(mContext, R.string.capture_success);
        } else {
            T.showShort(mContext, R.string.capture_failed);
        }

    }

    @Override
    protected void onVideoPTS(long videoPTS) {

    }

//    @Override
//    public int getActivityInfo() {
//        return Constants.ActivityInfo.ACTIVITY_APMONITORACTIVITY;
//    }

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

        Intent refreshContans = new Intent();
        refreshContans.setAction(Constants.Action.REFRESH_CONTANTS);
        mContext.sendBroadcast(refreshContans);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            mCurrentVolume++;
            if (mCurrentVolume > mMaxVolume) {
                mCurrentVolume = mMaxVolume;
            }

            if (mCurrentVolume != 0) {
                mIsCloseVoice = false;
                if (close_voice!=null) {
                    close_voice.setBackgroundResource(R.drawable.m_voice_on);
                }
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
                if (close_voice!=null) {
                    close_voice.setBackgroundResource(R.drawable.m_voice_off);
                }
            }

            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:
                start();
                break;
            case R.id.close_voice:
            case R.id.iv_vioce:
                if (mIsCloseVoice) {
                    mIsCloseVoice = false;
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
            case R.id.choose_video_format:
                changevideoformat();
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
        finish();
    }

    // 设置成对话状态
    private void speak() {
        hideVideoFormat();
        layout_voice_state.setVisibility(RelativeLayout.VISIBLE);
        send_voice.setBackgroundResource(R.drawable.ic_send_audio_p);
        setMute(false);
        isSpeak = true;
    }

    private void noSpeak() {
        send_voice.setBackgroundResource(R.drawable.ic_send_audio);
        setMute(true);
        isSpeak = false;
        cameraHandler.postDelayed(mrunnable, 500);
    }

    private boolean isFirstMute = true;
    Runnable mrunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (isFirstMute) {
                send_voice.performClick();
                isFirstMute = false;
            }
        }
    };

    public void stopSpeak() {
        send_voice.setBackgroundResource(R.drawable.ic_send_audio);
        layout_voice_state.setVisibility(RelativeLayout.GONE);
        setMute(true);
        isSpeak = false;
    }

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

    protected long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (!isPlay) {
                finish();
            } else {
                if ((System.currentTimeMillis() - exitTime) > 2000) {
                    T.showShort(mContext, R.string.press_again_monitor);
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                }
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
        if (control_bottom.getVisibility() == RelativeLayout.VISIBLE) {
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
                            cameraHandler.sendMessage(msg);
                            break;
                        }
                        Utils.sleepThread(500);
                    }
                }
            }.start();

        }
    };
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (passworddialog != null && passworddialog.isShowing()) {
                        passworddialog.dismiss();
                    }
                    String[] data = (String[]) msg.obj;
                    P2PHandler.getInstance().reject();
                    switchConnect();
                    callId = data[0];
                    password = data[1];
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
                    callDevice();
                    frushLayout(P2PValue.DeviceType.IPC);
                    break;
                case 2:
                    if (isShowCamera){
                        initcComponent();
                    }
                    break;
                case 3:
                    break;
            }
            return false;
        }
    };
    private Handler cameraHandler = new WeakRefHandler(mCallback);

//    @Override
//    public void onHomePressed() {
//        super.onHomePressed();
//        reject();
//    }

    public void getScreenWithHeigh() {
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeigh = dm.heightPixels;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstCreate){//首次进入设置非首次创建
            isFirstCreate = false;
        }else{//非首次创建时，重启自己,在有摄像头的情况下,并且是不会显示手势密码的时候
            if (mContact!=null && !isShowLook()){
                finish();
                startActivity(getIntent());
                overridePendingTransition(R.anim.activity_in_alpha,R.anim.activity_out_alpha);
            }
        }
    }

    /*
     * 初始化P2pview
     */
    private void initp2pView() {
        initP2PView(7, P2PView.LAYOUTTYPE_TOGGEDER);
        WindowManager manager = getWindowManager();
        window_width = manager.getDefaultDisplay().getWidth();
        window_height = manager.getDefaultDisplay().getHeight();
        this.initScaleView(this, window_width, window_height);
        setMute(true);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    /**
     * 请求数据子线程
     */
    public class BindingCameraLoad implements Runnable {
        private long uid;
        private String code;
        private String bIpc;

        public BindingCameraLoad(String bIpc) {
            this.bIpc = bIpc;
        }

        @Override
        public void run() {
            if (mContact != null){
                cameraHandler.sendEmptyMessage(2);
                return;
            }
            CameraInfo c = null;
            ZhujiInfo zhuji;
//            String masterID = dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, "");
            //替换
            String masterID = ZhujiListFragment.getMasterId();
            zhuji = DatabaseOperator.getInstance(mContext)
                    .queryDeviceZhuJiInfo(masterID);
            if (zhuji == null)
                return;
            List<CameraInfo> cameraInfos = DatabaseOperator.getInstance(mContext.getApplicationContext()).queryAllCameras(zhuji);
            if (!cameraInfos.isEmpty()) {
                for (CameraInfo cs : cameraInfos) {
                    if (cs.getIpcid() == Long.parseLong(bIpc)) {
                        c = cs;
                        break;
                    }
                }
            }
            if (c != null) {
                Contact contact = new Contact();
                contact.contactId = c.getId();
                contact.contactName = c.getN();
                contact.contactPassword = c.getP();
                contact.userPassword = c.getOriginalP();
                try {
                    contact.ipadressAddress = InetAddress.getByName("192.168.1.1");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                mContact = contact;
                cameraHandler.sendEmptyMessage(2);
            }
        }
    }

//    public void finishActivity() {
//        Contact mContact = (Contact) getIntent().getSerializableExtra("bcontact");
//        DeviceInfo into = (DeviceInfo) getIntent().getSerializableExtra("group");
//        DeviceInfo device = (DeviceInfo) getIntent().getSerializableExtra("device");
//        String action = getIntent().getStringExtra("action");
//        if (action != null && !"".equals(action)) {
//            Intent intent = new Intent();
//            intent.putExtra("contact", mContact);
//            if (into != null) {
//                //群组信息不为空的时候
//                intent.putExtra("device", into);
//            } else {
//                intent.putExtra("device", device);
//            }
//            intent.setAction(action);
//            startActivity(intent);
//        }
//        finish();
//    }
}
