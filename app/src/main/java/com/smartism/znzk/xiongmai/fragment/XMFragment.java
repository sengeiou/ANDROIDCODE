package com.smartism.znzk.xiongmai.fragment;


import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lib.FunSDK;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.smartlock.LockMainActivity;
import com.smartism.znzk.activity.smartlock.WifiLockMainActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.widget.HeaderView;
import com.smartism.znzk.xiongmai.activities.XiongMaiAudioRecordThread;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunDevicePassword;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunSupport;
import com.smartism.znzk.xiongmai.lib.funsdk.support.OnFunDeviceListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.OnFunDeviceOptListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.OPTimeSetting;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevStatus;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevType;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunLoginType;
import com.smartism.znzk.xiongmai.lib.funsdk.support.widget.FunVideoView;
import com.smartism.znzk.xiongmai.lib.sdk.struct.H264_DVR_FILE_DATA;
import com.smartism.znzk.xiongmai.widget.XMFramLayout;
import com.umeng.commonsdk.debug.W;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static com.smartism.znzk.util.DataCenterSharedPreferences.Constant.SECURITY_SETTING_PWD;


/*
 * 实现雄迈摄像头播放，方便插入别的Activity中
 * */

public class XMFragment extends Fragment implements View.OnClickListener, OnFunDeviceListener,OnFunDeviceOptListener, View.OnTouchListener{


    final String TAG = getClass().getSimpleName();
    final int MESSAGE_PLAY_SUCCESS = 0x44;
    private static final String XIONGMAI_CAMERAINFO= "xm_camerainfo";
    private static final String START_RECORD = "start_record";
    FunVideoView mFunVideoView;
    FunDevice mFunDevice;
    FrameLayout mXiongMaiParent;
    ImageView mPlayBtn; //播放视频按钮
    ProgressBar mProgressBar; //显示的进度条
    HeaderView mHeaderView;
    Button choose_video_format;//清晰度改变按钮
    ImageView screenshot, send_voice, close_voice; //截图按钮,发送声音 ,静音
    LinearLayout l_control_bottom, layout_voice_state; //底部导航条,说话声音提示
    TextView users;
    ImageView voice_state;
    public View.OnTouchListener mSpearkListener; //说话声音事件处理器
    boolean isPlayMedia = false;
    boolean isCloseVoice = false;
    int hTalker = -1;
    //主要用于处理点击雄迈的事件
    Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_PLAY_SUCCESS:
                    //播放是否成功
                    Log.v(TAG, "是否正在播放:" + mFunVideoView.isPlaying());
                    if (mFunVideoView.isPlaying()) {
                        isPlayMedia = true;
                        mHandler.removeMessages(MESSAGE_PLAY_SUCCESS);//移除以前发送的任务
                        syncCameraTime();
                        mProgressBar.setVisibility(View.GONE);
                        if (autoRecord) {
                            //开启自动录制
                            recordCamera();
                        }
                    } else {
                        mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY_SUCCESS, 1000);
                    }
                    break;
                case 99:
                    mHandler.removeMessages(99);
                    //隐藏导航条
                    if (l_control_bottom.getVisibility() == View.GONE) {
                        l_control_bottom.setVisibility(View.VISIBLE);
                    } else {
                        l_control_bottom.setVisibility(View.GONE);
                    }
                    break;
            }
            return true;
        }
    };
    Handler mHandler = new Handler(mCallback);
    private boolean autoRecord = false; //开启自动录制,默认不开启
    private CameraInfo mCameraInfo ;

    /*
     * 当活动由于异常的程序行为而被销毁时，
     * Activity会调用默认的无参的构造器创建Fragment对象
     * */
    public XMFragment() {
        mFunDevice = new FunDevice();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
             mCameraInfo = (CameraInfo) bundle.getSerializable(XIONGMAI_CAMERAINFO);
            autoRecord = bundle.getBoolean(START_RECORD, false);
            mFunDevice.devSn = mCameraInfo.getId();
            mFunDevice.devType = FunDevType.EE_DEV_NORMAL_MONITOR;//默认监控设备
            // 设置登录方式为本地登录
            FunSupport.getInstance().setLoginType(FunLoginType.LOGIN_BY_LOCAL);
            // 注册设备操作回调
            FunSupport.getInstance().registerOnFunDeviceOptListener(this);
            // 监听设备类事件
            FunSupport.getInstance().registerOnFunDeviceListener(this);
        }
        //对门铃进行唤醒
        FunSDK.DevWakeUp(FunSupport.getInstance().getHandler(), mFunDevice.devSn, mFunDevice.getId());
    }

    //同步摄像头时间
    private void syncCameraTime() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        String sysTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(cal.getTime());
        OPTimeSetting devtimeInfo = (OPTimeSetting) mFunDevice.checkConfig(OPTimeSetting.CONFIG_NAME);
        devtimeInfo.setmSysTime(sysTime);
        FunSupport.getInstance().requestDeviceSetConfig(mFunDevice, devtimeInfo);
    }


    // 设备登录
    private void requestDeviceStatus() {
        FunSupport.getInstance().requestDeviceStatus(mFunDevice.getDevType(), mFunDevice.devSn);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_base_camera, container, false);
        v.setVisibility(View.VISIBLE);
        View p2pView = v.findViewById(R.id.p2pview);
        mXiongMaiParent = v.findViewById(R.id.xmFragmentXiongMai);//雄迈父View
        mProgressBar = v.findViewById(R.id.prg_monitor);
        mPlayBtn = v.findViewById(R.id.btn_play);
        mHeaderView = v.findViewById(R.id.hv_header);
        choose_video_format = v.findViewById(R.id.choose_video_format);
        close_voice = v.findViewById(R.id.close_voice);
        screenshot = v.findViewById(R.id.screenshot);
        send_voice = v.findViewById(R.id.send_voice);
        l_control_bottom = v.findViewById(R.id.l_control_bottom);
        voice_state = v.findViewById(R.id.voice_state);
        layout_voice_state = v.findViewById(R.id.layout_voice_state);
        users = v.findViewById(R.id.users);
        //  users.setText(getString(R.string.monitor_number) + P2PConnect.getNumber());
        choose_video_format.setText(R.string.video_mode_hd);
        //事件绑定
        mPlayBtn.setOnClickListener(this);
        close_voice.setOnClickListener(this);
        send_voice.setOnClickListener(this);
        screenshot.setOnClickListener(this);

        //显示图片
        mHeaderView.setImageResource(R.drawable.header_icon);

        //添加雄迈播放
        XMFramLayout xmFramLayout = new XMFramLayout(getActivity(), mFunDevice, mHandler);
        mXiongMaiParent.addView(xmFramLayout);
        mFunVideoView = xmFramLayout.getmFunVideoView();
        //监听播放失败与否
        mFunVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                //播放失败回调
                Log.v(TAG, "播放失败信息码:" + extra);
                mProgressBar.setVisibility(View.GONE);
                mPlayBtn.setVisibility(View.VISIBLE);
                mHeaderView.setVisibility(View.VISIBLE);
                isPlayMedia = false;
                mHandler.removeMessages(MESSAGE_PLAY_SUCCESS);//移除消息
                return false;
            }
        });
        p2pView.setVisibility(View.GONE);//隐藏技威
        //密码输入Dialog初始化
        initInputDialog();
        //自动播放功能实现
        if (autoRecord) {
            if(!judgeCameraPwd(mCameraInfo.getOriginalP())){
                //需要输入密码
                showInputPasswordDialog();
            }else{
                mXiongMaiParent.post(new Runnable() {
                    @Override
                    public void run() {
                        if (!mFunVideoView.isPlaying()) {
                            //播放
                            mProgressBar.setVisibility(View.VISIBLE);//显示进度条
                            mHeaderView.setVisibility(View.GONE);//隐藏图片
                            mPlayBtn.setVisibility(View.GONE);
                            requestDeviceStatus();//查询设备状态
                            ToastUtil.longMessage(getString(R.string.poor_network_tips));
                        }
                    }
                });
            }
        }


        send_voice.setOnTouchListener(this);
        return v;
    }


    //判断是否相同，true为相同
    private boolean  judgeCameraPwd(String remotePwd){
        String password =   DataCenterSharedPreferences.getInstance(MainApplication.app
                ,DataCenterSharedPreferences.Constant.XM_CONFIG).getString(mFunDevice.getDevSn() + SECURITY_SETTING_PWD,"");
        if(TextUtils.isEmpty(password)||!remotePwd.equals(password)){
            return false ;
        }

        return true ;
    }

    AlertDialog mInputDialog ;
    private void initInputDialog(){
        View view = getLayoutInflater().inflate(R.layout.fragment_input_password_layout,null);
        final EditText mEditText = view.findViewById(R.id.new_password_edit);
        Button cancelBtn = view.findViewById(R.id.cancel_btn);
        Button confirmBtn  = view.findViewById(R.id.confirm_btn);
        View.OnClickListener  onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputDialog.dismiss();
                if(v.getId()==R.id.confirm_btn){
                    if(mEditText.getText().toString().equals(mCameraInfo.getOriginalP())){
                        //密码输入正确,保存到本地
                        DataCenterSharedPreferences.getInstance(MainApplication.app
                                , DataCenterSharedPreferences.Constant.XM_CONFIG).putString(mCameraInfo.getId() + SECURITY_SETTING_PWD, mEditText.getText().toString()).commit();
                        mPlayBtn.performClick();
                    }else{
                        ToastUtil.shortMessage(getString(R.string.pw_incrrect));
                    }
                }
            }
        };
        cancelBtn.setOnClickListener(onClickListener);
        confirmBtn.setOnClickListener(onClickListener);
        mInputDialog = new AlertDialog.Builder(getContext()).setView(view)
                .setCancelable(true)
                .create();
    }

    private void showInputPasswordDialog(){
        if(!mInputDialog.isShowing()){
            mInputDialog.show();
        }
    }


    boolean isRecording = false;

    //录制视频
    private void recordCamera() {
        AnimationDrawable drawable = null;
        if (!isRecording) {
            //表明没有正在录制视频,开启
          /*  mRecordImageView.setImageResource(R.drawable.recor_icon_anim);
            drawable  = (AnimationDrawable) mRecordImageView.getDrawable();
            if(!drawable.isRunning()){
                drawable.start();
            }*/
            String fileName = getFilePath(".mp4", LOCAL_MEDIA);
            StringBuffer picFileName = new StringBuffer();
            String[] temp = fileName.split("\\.");//视频的界面的截图文件
            for (int i = 0; i < temp.length - 1; i++) {
                picFileName.append(temp[i]);
                if (0 <= i && i <= 1) {
                    picFileName.append(".");
                }
            }
            picFileName.append(".jpg");
            //在这里在进行一个截图，作为视频的封面
            FunSDK.MediaSnapImage(mFunVideoView.getmPlayerHandler(), picFileName.toString(), mFunDevice.getId());
            int result = FunSDK.MediaStartRecord(mFunVideoView.getmPlayerHandler(), fileName, mFunDevice.getId());
            if (result >= 0) {
                Toast.makeText(getActivity(), getResources().getString(R.string.start_record), Toast.LENGTH_SHORT).show();
                isRecording = true; //正在录制
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.end_record), Toast.LENGTH_SHORT).show();
            }
        } else {
            int result = FunSDK.MediaStopRecord(mFunVideoView.getmPlayerHandler(), mFunDevice.getId());//停止录制
            if (result >= 0) {
                isRecording = false;
            }
        }
    }


    final int LOCAL_PICTURE = 0X88; //本地截图标识
    final int LOCAL_MEDIA = 0X89; //本地录制视频标识

    //截取摄像头画面
    private boolean screenCapture() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionForAudioRecoder(REQUEST_PERMISSION_CODE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            Log.v(TAG, "截图保存失败");
            return false;
        }
        String filePath = getFilePath(".jpg", LOCAL_PICTURE);
        Log.v(TAG, "filePath:" + filePath);
        int result = FunSDK.MediaSnapImage(mFunVideoView.getmPlayerHandler(), filePath, mFunDevice.getId());//截图咯
        if (result == 0) {
            Toast.makeText(getActivity(), getResources().getString(R.string.capture_success), Toast.LENGTH_SHORT).show();
            mHandler.sendEmptyMessageDelayed(2, 300);
            return true;//成功
        }
        Toast.makeText(getActivity(), getResources().getString(R.string.capture_failed), Toast.LENGTH_SHORT).show();
        return false;//失败
    }

    //创建图片文件路径
    private String getFilePath(final String houZhuiMing, final int type) {
        //截图和录像是属于某一个摄像头的，显示匹配当前设备的截图和录像文件
        String filePath = Environment.getExternalStorageDirectory().toString() +
                File.separator + getActivity().getPackageName() + File.separator + "xiongmaitempimg" + File.separator + mFunDevice.getDevSn();
        File picFile = null;
        if (type == LOCAL_MEDIA) {
            picFile = new File(filePath + File.separator + "local_media");
        } else if (type == LOCAL_PICTURE) {
            picFile = new File(filePath + File.separator + "local_picture");
        }

        if (!picFile.exists()) {
            boolean bool = picFile.mkdirs();
            if (bool) {
                Log.v(TAG, "截图目录创建成功");
            } else {
                Log.v(TAG, "截图目录创建失败");
            }
        }
        return picFile.toString() + File.separator + System.currentTimeMillis() + houZhuiMing;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE_AUDIO:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.device_not_permission), Toast.LENGTH_SHORT);
                }
                break;
            case REQUEST_PERMISSION_CODE_EXTERNAL_STORAGE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.device_not_permission), Toast.LENGTH_SHORT);
                }
                break;
        }
    }

    final int REQUEST_PERMISSION_CODE_AUDIO = 0X93;//申请录制视频权限请求码
    final int REQUEST_PERMISSION_CODE_EXTERNAL_STORAGE = 0X90;//申请储存权限请求码

    private void requestPermissionForAudioRecoder(int request_code, String... permissionName) {
        if (permissionName == null) {
            throw new IllegalStateException("不能为Null");
        }
        ActivityCompat.requestPermissions(getActivity(), permissionName, request_code);//进行权限申请
    }


    //停止播放
    private void stopMedia() {
        if (null != mFunVideoView) {
            mFunVideoView.stopPlayback();
            mFunVideoView.stopRecordVideo();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopMedia();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isPlayMedia) {
            playRealMedia();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销设备事件监听
        FunSupport.getInstance().removeOnFunDeviceListener(this);
        FunSupport.getInstance().removeOnFunDeviceOptListener(this);
        stopMedia();
        if (isMengLingCamera) {
            //使门铃进入休眠
            FunSDK.DevSleep(FunSupport.getInstance().getHandler(), mFunDevice.devSn, mFunDevice.getId());
        }
        if (autoRecord) {
            //停止录制
            recordCamera();
        }
    }


    //声音恢复之后，得把x去掉
    private void closeVoice() {
        if (isPlayMedia) {
            if (!isCloseVoice) {
                //关闭声音
                isCloseVoice = true;
                //WifiMainActivity
                if (getActivity() instanceof WifiLockMainActivity) {
                    WifiLockMainActivity activity = (WifiLockMainActivity) getActivity();
                    activity.handleIVoice(true);
                } else if (getActivity() instanceof LockMainActivity) {
                    LockMainActivity wangguan = (LockMainActivity) getActivity();
                    wangguan.handleIVoice(true);
                }
                close_voice.setBackgroundResource(R.drawable.m_voice_off);
                mFunVideoView.setMediaSound(false);
            } else {
                //恢复声音
                isCloseVoice = false;
                //WifiMainActivity
                if (getActivity() instanceof WifiLockMainActivity) {
                    WifiLockMainActivity activity = (WifiLockMainActivity) getActivity();
                    activity.handleIVoice(false);
                } else if (getActivity() instanceof LockMainActivity) {
                    LockMainActivity wangguan = (LockMainActivity) getActivity();
                    wangguan.handleIVoice(false);
                }
                close_voice.setBackgroundResource(R.drawable.m_voice_on);
                FunSDK.MediaSetSound(mFunVideoView.getmPlayerHandler(), 50, mFunDevice.getId());
            }

        } else {
            Log.v(TAG, "大哥还没播放呢，别点了没用的");
            ToastTools.short_Toast(getActivity(), getResources().getString(R.string.please_play_camera));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:
                if(!judgeCameraPwd(mCameraInfo.getOriginalP())) {
                    //需要输入密码
                    showInputPasswordDialog();
                    return ;
                }
                    //播放
                mProgressBar.setVisibility(View.VISIBLE);//显示进度条
                mHeaderView.setVisibility(View.GONE);//隐藏图片
                v.setVisibility(View.GONE);
                requestDeviceStatus();//查询设备状态
                ToastTools.long_Toast(getActivity(), getResources().getString(R.string.poor_network_tips));
                break;
            case R.id.wifi_lock_iv_vioce:
            case R.id.close_voice:
                //静音
                closeVoice();
                break;
            case R.id.wifi_lock_iv_screenshot:
            case R.id.screenshot:
                //截屏\
                if (mFunVideoView.isPlaying()) {
                    screenCapture();//截图
                } else {
                    ToastTools.short_Toast(getActivity(), getResources().getString(R.string.please_play_camera));
                }
                break;
        }
    }

    //登入设备
    private void loginDevice() {
        FunSupport.getInstance().requestDeviceLogin(mFunDevice);
    }

    //开始播放
    private void playRealMedia() {
        mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY_SUCCESS, 1000);//这里采用handle来实现播放成功的监听
        if (mFunDevice.isRemote) {
            mFunVideoView.setRealDevice(mFunDevice.getDevSn(), mFunDevice.CurrChannel);
        } else {
            String deviceIp = FunSupport.getInstance().getDeviceWifiManager().getGatewayIp();
            mFunVideoView.setRealDevice(deviceIp, mFunDevice.CurrChannel);
        }
        // 打开声音
        FunSDK.MediaSetSound(mFunVideoView.getmPlayerHandler(), 50, mFunDevice.getId());
    }

    //形参为雄迈的序列号
    public static XMFragment newInstance(CameraInfo cameraInfo) {
        return newInstance(cameraInfo, false);
    }


    public static XMFragment newInstance(CameraInfo cameraInfo, boolean record) {
        if (TextUtils.isEmpty(cameraInfo.getId())) {
            throw new IllegalArgumentException("序列号有误,请检查!");
        }
        XMFragment xmFragment = new XMFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(XIONGMAI_CAMERAINFO, cameraInfo);
        bundle.putBoolean(START_RECORD, record);
        xmFragment.setArguments(bundle);
        return xmFragment;
    }

    //OnFunDeviceListener事件处理方法
    @Override
    public void onDeviceListChanged() {

    }

    @Override
    public void onDeviceStatusChanged(FunDevice funDevice) {
        // 设备状态变化,如果是当前登录的设备查询之后是在线的,打开设备操作界面
        if (null != mFunDevice.devSn && mFunDevice.devSn.equals(funDevice.getDevSn())) {
            Log.v(TAG, "查询状态成功");
            if (funDevice.devStatus == FunDevStatus.STATUS_ONLINE) {
                // 如果设备在线,获取设备信息
                if ((funDevice.devType == null || funDevice.devType == FunDevType.EE_DEV_UNKNOWN)) {
                    funDevice.devType = FunDevType.EE_DEV_NORMAL_MONITOR;
                }
                funDevice.devSn = mFunDevice.devSn;
                mFunDevice = funDevice;
                Log.v("SNLogin", "设备类型:" + funDevice.devType);
                //保存密码
                // 启动/打开设备操作界面
                if (null != mFunDevice) {
                    // 传入用户名/密码
                    //   mFunDevice.loginName = mEditDevLoginName.getText().toString().trim();
                    if (mFunDevice.loginName == null || mFunDevice.loginName.length() == 0) {
                        // 用户名默认是:admin
                        mFunDevice.loginName = "admin";
                    }
                    //  mFunDevice.loginPsw = mEditDevLoginPasswd.getText().toString().trim();
                    //Save the password to local file
                    FunDevicePassword.getInstance().saveDevicePassword(mFunDevice.getDevSn(), mFunDevice.loginPsw);
                    FunSDK.DevSetLocalPwd(mFunDevice.getDevSn(), "admin", mFunDevice.loginPsw);
                    if (isMengLingCamera) {
                        //设置设备的类型为门铃摄像头
                        mFunDevice.devType = FunDevType.EE_DEV_IDR;
                        //门铃直接播放
                        playRealMedia();
                    } else {
                        // 如果设备未登录,先登录设备
                        if (!mFunDevice.hasLogin() || !mFunDevice.hasConnected()) {
                            loginDevice();
                        } else {
                            playRealMedia();//已经登入就直接播放
                        }
                    }
                }
            } else {
                // 设备不在线
                ToastTools.short_Toast(getActivity(), getResources().getString(R.string.camera_off));
                //恢复布局
                mProgressBar.setVisibility(View.GONE);
                mHeaderView.setVisibility(View.VISIBLE);
                mPlayBtn.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDeviceAddedSuccess() {

    }

    @Override
    public void onDeviceAddedFailed(Integer errCode) {

    }

    @Override
    public void onDeviceRemovedSuccess() {

    }

    @Override
    public void onDeviceRemovedFailed(Integer errCode) {

    }

    @Override
    public void onAPDeviceListChanged() {

    }

    @Override
    public void onLanDeviceListChanged() {

    }

    //OnFunDeviceOptListener事件处理方法

    @Override
    public void onDeviceLoginSuccess(FunDevice funDevice) {
        Log.v(TAG, "设备登入成功:" + funDevice.getDevSn());
        if (null != mFunDevice && null != funDevice) {
            if (mFunDevice.getId() == funDevice.getId()) {
                playRealMedia();//登入成功之后直接播放
            }
        }
    }

    boolean isMengLingCamera = false;

    @Override
    public void onDoorBellWakeUp() {
        Log.d(TAG, "门铃唤醒成功");
        isMengLingCamera = true;
    }

    @Override
    public void onDeviceLoginFailed(FunDevice funDevice, Integer errCode) {
        //登入失败
        Log.v(TAG, "登入失败");
        ToastTools.short_Toast(getActivity(), getResources().getString(R.string.zhzj_login_fail_title));
        mProgressBar.setVisibility(View.GONE);
        mHeaderView.setVisibility(View.VISIBLE);
        mPlayBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDeviceGetConfigSuccess(FunDevice funDevice, String configName, int nSeq) {

    }

    @Override
    public void onDeviceGetConfigFailed(FunDevice funDevice, Integer errCode) {

    }

    @Override
    public void onDeviceSetConfigSuccess(FunDevice funDevice, String configName) {

    }

    @Override
    public void onDeviceSetConfigFailed(FunDevice funDevice, String configName, Integer errCode) {

    }

    @Override
    public void onDeviceChangeInfoSuccess(FunDevice funDevice) {

    }

    @Override
    public void onDeviceChangeInfoFailed(FunDevice funDevice, Integer errCode) {

    }

    @Override
    public void onDeviceOptionSuccess(FunDevice funDevice, String option) {

    }

    @Override
    public void onDeviceOptionFailed(FunDevice funDevice, String option, Integer errCode) {

    }

    @Override
    public void onDeviceFileListChanged(FunDevice funDevice) {

    }

    @Override
    public void onDeviceFileListChanged(FunDevice funDevice, H264_DVR_FILE_DATA[] datas) {

    }

    @Override
    public void onDeviceFileListGetFailed(FunDevice funDevice) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        AnimationDrawable drawable = (AnimationDrawable) voice_state.getDrawable();
        if (v.getId() == R.id.send_voice) {
            mHandler.removeMessages(3);
        }
        if (mFunVideoView.isPlaying()) {
            //进行权限处理
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionForAudioRecoder(REQUEST_PERMISSION_CODE_AUDIO, Manifest.permission.RECORD_AUDIO);
                return false;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (layout_voice_state.getVisibility() == View.GONE) {
                        layout_voice_state.setVisibility(View.VISIBLE);//设置为可见

                        if (!drawable.isRunning()) {
                            drawable.start();//运行动画
                        }
                        //开启对讲声音
                        hTalker = FunSDK.DevStarTalk(FunSupport.getInstance().getHandler()
                                , mFunDevice.getDevSn(), mFunDevice.getId(),0,0);//开始说话
                        Log.v(TAG, "说话句柄:" + hTalker);
                        FunSDK.MediaSetSound(mFunVideoView.getmPlayerHandler(), 0, mFunDevice.getId());//关闭声音
                        XiongMaiAudioRecordThread.startRecord(mFunDevice);//开始录制
                        if (!isCloseVoice) {
                            //表示当前没有静音
                            closeVoice();//静音状态
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    //关闭麦克风
                    if (layout_voice_state.getVisibility() == View.VISIBLE) {
                        if (drawable.isRunning()) {
                            drawable.stop();
                        }
                        layout_voice_state.setVisibility(View.GONE);
                        //关闭对讲声音
                        XiongMaiAudioRecordThread.stopRecord();//结束录制
                        FunSDK.DevStopTalk(hTalker);//结束对讲说话
                        FunSDK.MediaSetSound(mFunVideoView.getmPlayerHandler()
                                , 50, mFunDevice.getId());//恢复声音大小
                        if (isCloseVoice) {
                            //当前处于静音状态
                            closeVoice();//恢复不静音状态
                        }
                    }
                    return true;
            }
        } else {
            //避免重复弹出消息提示框
            if (event.getAction() == MotionEvent.ACTION_UP) {
                ToastTools.short_Toast(getActivity(), getResources().getString(R.string.please_play_camera));
            }
        }
        return false;
    }
}
