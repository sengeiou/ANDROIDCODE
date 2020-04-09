package com.smartism.znzk.xiongmai.activities;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.lib.EPTZCMD;
import com.lib.FunSDK;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.FragmentParentActivity;
import com.smartism.znzk.activity.device.IPCZhujiDetailActivity;
import com.smartism.znzk.adapter.recycleradapter.BaseRecyslerAdapter;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.camera.adapter.ImageListAdapter;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.widget.HeaderView;
import com.smartism.znzk.widget.NormalDialog;
import com.smartism.znzk.xiongmai.fragment.InputFragment;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunSupport;
import com.smartism.znzk.xiongmai.lib.funsdk.support.OnFunDeviceListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.OnFunDeviceOptListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.OPTimeQuery;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.OPTimeSetting;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevStatus;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevType;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunLoginType;
import com.smartism.znzk.xiongmai.lib.funsdk.support.widget.FunVideoView;
import com.smartism.znzk.xiongmai.lib.sdk.struct.H264_DVR_FILE_DATA;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

import java.io.File;
import java.io.FilenameFilter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static com.smartism.znzk.util.DataCenterSharedPreferences.Constant.ALARM_PUSH_STATUS;
import static com.smartism.znzk.util.DataCenterSharedPreferences.Constant.SECURITY_SETTING_PWD;
import static com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevType.EE_DEV_NORMAL_MONITOR;

/*
 * 摄像头播放活动 --雄迈
 * */
public class XiongMaiDisplayCameraActivity extends FragmentParentActivity implements View.OnClickListener
        ,OnFunDeviceListener,OnFunDeviceOptListener, BaseRecyslerAdapter.RecyclerItemLongClickListener
        ,BaseRecyslerAdapter.RecyclerItemClickListener, InputFragment.OnInputContentListener {

    final int MESSAGE_PLAY_SUCCESS=0x345;
    private  final int LEFT=19,RIGHT=95,UP=12,DOWM=21;//标识是左滑还是右滑上滑啥的
    final String TAG = getClass().getSimpleName();
    String mCurrentDevSN = "6d6fdc93edf62bda";//测试用的
    TextView mTileTextView ;
    FunDevice mFunDevice;
    FunVideoView mFunVideoView;
    FrameLayout mParentFunVideo;
    ImageView playBtn,backBtn; //播放按钮,返回按钮
    ImageView screenBtn;//截图按钮
    ImageView  mRecordImageView;//录制视频控件
    ImageView mPortraitLocalFilesButton;//打开录像文件
    private ImageView mLookLocalPicIv ; //查看截图文件
    LinearLayout mProgressParentLinear;//进度条父布局
    HeaderView mHeaderView ; //一家人看视频照片
    RecyclerView mRecyclerView ;//显示图片
    ImageView closeVoiceShu ,closeVoiceHeng; //竖屏静音按钮,横屏静音按钮
    boolean isCloseVoice = false;//标识声明是否关闭
    SeekBar mSoundSeekBar;//设置声音大小进度条
    TextView voiceValue;//显示当前声音大小
    ImageView mPortraitSpeak ,mLandscapeSpeak; //竖屏状态以及横屏下说话
    LinearLayout mLinearVoiceState ; //说话声音的提示效果,通过帧动画来表现动画效果
    ImageView mImageViewVoiceState;//帧动画表现提示效果
    private List<RecyclerItemBean> pictrues = null;
    ImageListAdapter mAdapter;
    boolean isPlayMedia = false;
    RelativeLayout mNavigationShuPing,mNavigationHengPing; //竖屏与竖屏下的导航条
    LinearLayout mVedioModeLinearLayout ;//清晰度切换，只有高清
    TextView users ; //显示观看人数
    boolean isPortraitShow = false ;//标识竖屏下的导航栏是否显示
    boolean isLandscapeShow =false ;//标识横屏下的导航栏是否显示
    boolean isRecording = false ;  //记录是否正在录像
    int hTalker = -1; //语音操作句柄
    ImageView tv_moreinfo ;
    ImageView tv_setting ; //设置
    boolean isMengLingCamera = false;

    //横竖屏切换按钮
    ImageView mFullScreenPlay,mHalfScreenPlay;//全屏播放以及半屏播放
    RelativeLayout mRoundMenuRelative,mTitleLayoutRelative;//底部圆形导航栏以及标题栏
    ImageView mLandscapeCloseBtn,mLandscapeScreenHot;//横屏下的关闭按钮、截图按钮

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_PLAY_SUCCESS:
                    if(mFunVideoView.isPlaying()){
                        isPlayMedia = true;
                        mHandler.removeMessages(MESSAGE_PLAY_SUCCESS);//移除以前发送的任务
                        ToastTools.short_Toast(mContext,getResources().getString(R.string.camera_play));
                        syncCameraTime();//同步摄像头时间
                        mProgressParentLinear.setVisibility(View.GONE);
                    }else{
                        mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY_SUCCESS,1000);
                    }
                    break;
                case 2:
                    refreshImageList();//通知数据改变 这里好像没有生效
                    break;
                case 1:
                    mHandler.removeMessages(1);
                    if(!mFunVideoView.isPlaying()){
                        break;
                    }
                    //停止摄像头转动,不太好
                    if(msg.arg1==LEFT){
                        FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.PAN_LEFT,true,mFunDevice.CurrChannel);
                    }else if(msg.arg1==RIGHT){
                        FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.PAN_RIGHT,true,mFunDevice.CurrChannel);
                    }else if(msg.arg1==UP){
                        FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.TILT_UP,true,mFunDevice.CurrChannel);
                    }else if(msg.arg1 == DOWM){
                        FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.TILT_DOWN,true,mFunDevice.CurrChannel);
                    }
                    break;
                case 3:
                    long currentTime = System.currentTimeMillis();
                    mHandler.removeMessages(3);
                    //仅作为关闭,必须满足显示了3秒之后关闭
                    Log.v(TAG,"显示时间:"+(currentTime-mClickLastTime));
                    if((isPortraitShow||isLandscapeShow)&&(currentTime-mClickLastTime)>3000){
                        closeOrDisplayNavigation();
                    }
                    break;
            }
            return true;
        }
    });
    DeviceInfo operationDevice ;
    Contact mContact ;

    //摄像头
    private CameraInfo mCameraInfo  ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xiongmai_play);
       /*
        //测试用的
        mFunDevice  = new FunDevice();
        mFunDevice.devSn= mCurrentDevSN;
        */

       if(savedInstanceState==null){
           mContact = (Contact) getIntent().getSerializableExtra("contact");
           operationDevice = (DeviceInfo) getIntent().getSerializableExtra("deviceInfo");
       }else{
           mContact  = (Contact) savedInstanceState.getSerializable("contact");
           operationDevice = (DeviceInfo) savedInstanceState.getSerializable("deviceInfo");
       }


        mFunDevice  = new FunDevice();
        mFunDevice.devSn=mContact.getContactId();
        mFunDevice.devType = EE_DEV_NORMAL_MONITOR ; //默认正常监控设备
        mCurrentDevSN = mContact.getContactId();
        initComponent();
        // 设置登录方式为本地登录
        FunSupport.getInstance().setLoginType(FunLoginType.LOGIN_BY_LOCAL);

        getCameraPwd();//获取摄像头密码原始

        //对门铃进行唤醒
        FunSDK.DevWakeUp(FunSupport.getInstance().getHandler(),mFunDevice.devSn,mFunDevice.getId());
    }

    private void getCameraPwd(){
        showInProgress("");
     new LoadZhujiAndDeviceTask().queryZhujiInfoByZhuji(operationDevice.getZj_id(), new LoadZhujiAndDeviceTask.ILoadResult<ZhujiInfo>() {
         @Override
         public void loadResult(ZhujiInfo result) {
             cancelInProgress();
             mCameraInfo = result.getCameraInfo() ;
         }
     });
    }

    //判断是否相同，true为相同
    private boolean  judgeCameraPwd(String remotePwd){
      String password =   DataCenterSharedPreferences.getInstance(getApplicationContext()
                , DataCenterSharedPreferences.Constant.XM_CONFIG).getString(mCurrentDevSN + SECURITY_SETTING_PWD,"");
      if(TextUtils.isEmpty(password)||!remotePwd.equals(password)){
          return false ;
      }

      return true ;
    }

    View mCurrentClickView ;
    private void showInputPasswordDialog(View view){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction() ;
        fragmentTransaction.setCustomAnimations(R.anim.fade_in_center,R.anim.fade_out_center);
        InputFragment.getInstance(getString(R.string.inputpassword),getString(R.string.inputpassword)).show(fragmentTransaction,"input_dialog");
        mCurrentClickView = view ;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("contact",mContact);
        outState.putSerializable("deviceInfo",operationDevice);
        super.onSaveInstanceState(outState);
    }

    //同步摄像头时间
    private void syncCameraTime(){
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        String sysTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(cal.getTime());
        OPTimeSetting devtimeInfo = (OPTimeSetting)mFunDevice.checkConfig(OPTimeSetting.CONFIG_NAME);
        devtimeInfo.setmSysTime(sysTime);
        FunSupport.getInstance().requestDeviceSetConfig(mFunDevice,devtimeInfo);
    }


    private void initComponent(){
        MyFrameLayout myFrameLayout = new MyFrameLayout(this);
        mFunVideoView  =myFrameLayout.getmFunVideoView();
        mParentFunVideo = findViewById(R.id.mGLSurfaceView);
        mParentFunVideo.addView(myFrameLayout);


        tv_moreinfo = findViewById(R.id.tv_moreinfo);
        tv_setting = findViewById(R.id.tv_setting);//设置
        tv_setting.setOnClickListener(this);
        tv_setting.setVisibility(View.VISIBLE);//显示设置按钮

        users = findViewById(R.id.users);
        //  users.setText(getString(R.string.monitor_number) + P2PConnect.getNumber());
        playBtn = findViewById(R.id.play_media_img);
        mTileTextView = findViewById(R.id.tv_name);
        mProgressParentLinear = findViewById(R.id.parentProgress_linear);
        mHeaderView  = findViewById(R.id.hv_header);
        backBtn = findViewById(R.id.back_btn);
        screenBtn= findViewById(R.id.iv_screenshot);
        closeVoiceShu = findViewById(R.id.iv_vioce);
        closeVoiceHeng = findViewById(R.id.close_voice);
        mSoundSeekBar = findViewById(R.id.seek_voice);
        voiceValue = findViewById(R.id.voice_persent);
        mRecyclerView=findViewById(R.id.horizon_listview);//列表
        mNavigationShuPing = findViewById(R.id.l_control);
        mNavigationHengPing = findViewById(R.id.control_bottom);
        mVedioModeLinearLayout = findViewById(R.id.control_top);//清晰度切换的父View
        mRecordImageView = findViewById(R.id.iv_recode);//录制视频控件
        mLookLocalPicIv = findViewById(R.id.iv_defence);
        mPortraitLocalFilesButton = findViewById(R.id.iv_file);//打开录像文件
        mPortraitSpeak = findViewById(R.id.iv_speak);//底部说话按钮
        mLandscapeSpeak = findViewById(R.id.send_voice);//横屏状态下的说话按钮
        mLinearVoiceState = findViewById(R.id.layout_voice_state);
        mImageViewVoiceState = findViewById(R.id.voice_state);//声音提示效果控件
        mFullScreenPlay = findViewById(R.id.iv_full_screen);//全屏播放控件
        mHalfScreenPlay = findViewById(R.id.iv_half_screen);//切换半屏
        mRoundMenuRelative = findViewById(R.id.rl_control);//底部圆形菜单
        mTitleLayoutRelative = findViewById(R.id.layout_title);//标题栏部分
        mLandscapeCloseBtn = findViewById(R.id.hungup);//横屏下的关闭按钮
        mLandscapeScreenHot = findViewById(R.id.screenshot);//横屏状态下的截图

        //设置标题
        Contact contact  = (Contact) getIntent().getSerializableExtra("contact");
        if(contact!=null){
            mTileTextView.setText(contact.getContactName());
        }else{
            mTileTextView.setText("Camera");
        }


        //截图显示列表
        initImageList();//初始化pictures
        mRecyclerView=findViewById(R.id.horizon_listview);//列表
        mAdapter = new ImageListAdapter(pictrues);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setRecyclerItemClickListener(this);
        mAdapter.setRecyclerItemLongClickListener(this);

        //创建默认线性LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        //设置布局管理器
        mRecyclerView.setLayoutManager(layoutManager);
        //设置adapter
        //设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mHandler.sendEmptyMessage(2);//显示本地截图

        //监听播放失败与否
        mFunVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                //播放失败回调
                Log.v(TAG,"播放失败信息码:"+extra);
                mProgressParentLinear.setVisibility(View.GONE);
                isPlayMedia = false;
                mHandler.removeMessages(MESSAGE_PLAY_SUCCESS);//移除消息
                return false;
            }
        });

        tv_moreinfo.setOnClickListener(this);
        closeVoiceHeng.setOnClickListener(this);
        closeVoiceShu.setOnClickListener(this);
        screenBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        playBtn.setOnClickListener(this);
        mRecordImageView.setOnClickListener(this);
        mPortraitLocalFilesButton.setOnClickListener(this);
        mFullScreenPlay.setOnClickListener(this);
        mHalfScreenPlay.setOnClickListener(this);
        mLandscapeCloseBtn.setOnClickListener(this);
        mLandscapeScreenHot.setOnClickListener(this);
        mLookLocalPicIv.setOnClickListener(this);

        //声音拖动条监听器
        //竖屏状态下设置声音的SeekBar
        mSoundSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                float value = seekBar.getProgress();
                int temp  = (int) ((value/seekBar.getMax())*100);//因为转换为整型时，会舍弃小数部分
                voiceValue.setText(temp+"%");
                int result = FunSDK.MediaSetSound(mFunVideoView.getmPlayerHandler(),seekBar.getProgress(),mFunDevice.getId());
                if(temp==0){
                    if(!isCloseVoice){
                        //表示声音是打开的
                        closeVoice();
                    }
                }else{
                    if(isCloseVoice){
                        //表示声音是静音的
                        closeVoice();
                    }
                }
            }
        });

        //提示说话事件处理器
        View.OnTouchListener mListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                AnimationDrawable drawable = (AnimationDrawable) mImageViewVoiceState.getDrawable();
                if(view.getId()== R.id.send_voice){
                    mHandler.removeMessages(3);
                }
                if(mFunVideoView.isPlaying()){
                    //进行权限处理
                    if(ContextCompat.checkSelfPermission(mContext,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
                        requestPermissionForAudioRecoder(REQUEST_PERMISSION_CODE_AUDIO, Manifest.permission.RECORD_AUDIO);
                        return false;
                    }
                    switch (motionEvent.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            if(mLinearVoiceState.getVisibility()==View.GONE){
                                mLinearVoiceState.setVisibility(View.VISIBLE);//设置为可见

                                if(!drawable.isRunning()){
                                    drawable.start();//运行动画
                                }
                                //开启对讲声音
                                hTalker = FunSDK.DevStarTalk(FunSupport.getInstance().getHandler()
                                        ,mFunDevice.getDevSn(),mFunDevice.getId(),0,0);//开始说话
                                FunSDK.MediaSetSound(mFunVideoView.getmPlayerHandler(),0,mFunDevice.getId());//关闭声音
                                XiongMaiAudioRecordThread.startRecord(mFunDevice);//开始录制
                                if(!isCloseVoice){
                                    //表示当前没有静音
                                    closeVoice();//静音状态
                                }
                            }
                            return true ;
                        case MotionEvent.ACTION_UP:
                            //关闭麦克风
                            if(mLinearVoiceState.getVisibility()==View.VISIBLE){
                                if(drawable.isRunning()){
                                    drawable.stop();
                                }
                                mLinearVoiceState.setVisibility(View.GONE);
                                //关闭对讲声音
                                XiongMaiAudioRecordThread.stopRecord();//结束录制
                                FunSDK.DevStopTalk(hTalker);//结束对讲说话
                                FunSDK.MediaSetSound(mFunVideoView.getmPlayerHandler()
                                        ,mSoundSeekBar.getProgress(),mFunDevice.getId());//恢复声音大小
                                if(isCloseVoice&&(mSoundSeekBar.getProgress()!=0)){
                                    //当前处于静音状态
                                    closeVoice();//恢复不静音状态
                                }
                            }
                            return true ;
                    }
                }
                return false ;
            }
        };
        mPortraitSpeak.setOnTouchListener(mListener);
        mLandscapeSpeak.setOnTouchListener(mListener);
    }

    @Override
    public void onBackPressed() {
        //处理在横屏状态下回到竖屏
        Configuration configuration = getResources().getConfiguration();
        if(configuration.orientation==Configuration.ORIENTATION_LANDSCAPE){
            //横屏，此时切换到竖屏
            //隐藏横屏导航
            mNavigationHengPing.setVisibility(View.GONE);
            //设置显示标志为false
            isLandscapeShow = false;
            //显示出列表，隐藏底部圆形菜单,标题栏
            mRoundMenuRelative.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mTitleLayoutRelative.setVisibility(View.VISIBLE);

            //显示竖屏导航
            mNavigationShuPing.setVisibility(View.VISIBLE);
            //设置标志为true
            isPortraitShow = true;
            Log.v(TAG,"切换至竖屏");
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//并不是这个Configuration.ORIENTATION_LANDSCAPE
        }else{
            super.onBackPressed();
        }
    }

    //权限处理结果回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_PERMISSION_CODE_AUDIO:
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, getResources().getString(R.string.device_not_permission), Toast.LENGTH_SHORT);
                }
                break;
            case REQUEST_PERMISSION_CODE_EXTERNAL_STORAGE:
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, getResources().getString(R.string.device_not_permission), Toast.LENGTH_SHORT);
                }else{
                    mHandler.sendEmptyMessage(2);
                }
                break;
        }
    }

    // 设备登录
    private void requestDeviceStatus() {
        FunSupport.getInstance().requestDeviceStatus(mFunDevice.getDevType(), mFunDevice.devSn);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopMedia();
        // 注销设备事件监听
        FunSupport.getInstance().removeOnFunDeviceListener(this);
        FunSupport.getInstance().removeOnFunDeviceOptListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 注册设备操作回调
        FunSupport.getInstance().registerOnFunDeviceOptListener(this);
        // 监听设备类事件
        FunSupport.getInstance().registerOnFunDeviceListener(this);

        refreshImageList();

        if(isPlayMedia){
            playRealMedia();
        }
    }


    //停止播放
    private void stopMedia() {
        if (null != mFunVideoView) {
            mFunVideoView.stopPlayback();
            mFunVideoView.stopRecordVideo();
        }
    }
    //开始播放
    private void playRealMedia() {
        mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY_SUCCESS,1000);//这里采用handle来实现播放成功的监听
        if (mFunDevice.isRemote) {
            mFunVideoView.setRealDevice(mFunDevice.getDevSn(), mFunDevice.CurrChannel);
        } else {
            String deviceIp = FunSupport.getInstance().getDeviceWifiManager().getGatewayIp();
            mFunVideoView.setRealDevice(deviceIp, mFunDevice.CurrChannel);
        }
        // 打开声音
        FunSDK.MediaSetSound(mFunVideoView.getmPlayerHandler(),mSoundSeekBar.getProgress(),mFunDevice.getId());
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMedia();
        if(isMengLingCamera){
            //使门铃进入休眠
            FunSDK.DevSleep(FunSupport.getInstance().getHandler(),mFunDevice.devSn,mFunDevice.getId());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play_media_img:
                if(!judgeCameraPwd(mCameraInfo.getOriginalP())){
                    showInputPasswordDialog(v);
                    return ;
                }
                v.setVisibility(View.GONE);
                mHeaderView.setVisibility(View.GONE);
                mProgressParentLinear.setVisibility(View.VISIBLE);
                requestDeviceStatus();//查询设备成功之后会播放
                ToastTools.long_Toast(this,getResources().getString(R.string.poor_network_tips));
                break;
            case R.id.hungup:
            case R.id.back_btn:
                //返回按钮
                finish();
                break;
            case R.id.screenshot:
            case R.id.iv_screenshot:
                if(mFunVideoView.isPlaying()){
                    screenCapture();//截图
                }
                break;
            case R.id.close_voice:
            case R.id.iv_vioce:
                closeVoice();//静音
                break;
            case R.id.iv_recode:
                if(mFunVideoView.isPlaying()){
                    recordCamera(); //录制视频
                }else{
                    ToastTools.short_Toast(mContext,getResources().getString(R.string.please_play_camera));
                }
                break;
            case R.id.iv_file:
                openLocalFile();//打开录像文件
                break;
            case R.id.iv_full_screen:
                //隐藏竖屏导航
                mNavigationShuPing.setVisibility(View.GONE);
                //设置为没有显示
                isPortraitShow = false;//否则需要会出现点击两次才显示竖屏导航栏的现象，仔细体会
                //隐藏列表，隐藏底部圆形菜单,标题栏
                mRoundMenuRelative.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.GONE);
                mTitleLayoutRelative.setVisibility(View.GONE);
                //显示横屏导航
                mNavigationHengPing.setVisibility(View.VISIBLE);
                isLandscapeShow = true ; //显示了横屏导航标志
                //横屏显示
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//并不是这个Configuration.ORIENTATION_LANDSCAPE
                break;
            case R.id.iv_half_screen:
                //切换半屏播放
                //隐藏横屏导航
                mNavigationHengPing.setVisibility(View.GONE);
                //设置显示标志为false
                isLandscapeShow = false;
                //显示出列表，隐藏底部圆形菜单,标题栏
                mRoundMenuRelative.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mTitleLayoutRelative.setVisibility(View.VISIBLE);

                //显示竖屏导航
                mNavigationShuPing.setVisibility(View.VISIBLE);
                //设置标志为true
                isPortraitShow = true;
                //竖屏显示
                Log.v(TAG,"切换至竖屏");
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//并不是这个Configuration.ORIENTATION_LANDSCAPE
                break;
            case R.id.tv_moreinfo:
                Intent intent = new Intent();
                intent.setClass(mContext.getApplicationContext(), IPCZhujiDetailActivity.class);
                intent.putExtra("device",operationDevice);
                startActivity(intent);
                break;
            case R.id.tv_setting:
                if(!judgeCameraPwd(mCameraInfo.getOriginalP())){
                    showInputPasswordDialog(v);
                    return  ;
                }
                Intent settingIntent = new Intent();
                settingIntent.setClass(this,XMSettingActivity.class);
                settingIntent.putExtra("camera_info",mCameraInfo);
                settingIntent.putExtra("sn",mFunDevice.getDevSn());
                settingIntent.putExtra("device_id",operationDevice.getId());
                startActivity(settingIntent);
                break ;
            case R.id.iv_defence:
                intent = new Intent();
                intent.setClass(this, XiongMaiSetupAlarmActivity.class);
                intent.putExtra("sn", mFunDevice.getDevSn());
                startActivity(intent);
                break ;
        }
    }

    //打开显示录像文件Activity
    private void openLocalFile(){
        //传过去设备的序列号
        Intent intent = new Intent(this,XMLocalRecordFilesActivity.class);
        intent.putExtra("sn",mFunDevice.getDevSn());
        startActivity(intent);
    }

    //录制视频
    private void recordCamera(){
        AnimationDrawable drawable= null;
        if(!isRecording){
            //表明没有正在录制视频,开启
            mRecordImageView.setImageResource(R.drawable.recor_icon_anim);
            drawable  = (AnimationDrawable) mRecordImageView.getDrawable();
            if(!drawable.isRunning()){
                drawable.start();
            }
            String fileName = getFilePath(".mp4",LOCAL_MEDIA);
            StringBuffer picFileName = new StringBuffer();
            String[] temp = fileName.split("\\.");//视频的界面的截图文件
            for(int i=0;i<temp.length-1;i++){
                picFileName.append(temp[i]);
                if(0<=i&&i<=1){
                    picFileName.append(".");
                }
            }
            picFileName.append(".jpg");
            //在这里在进行一个截图，作为视频的封面
            FunSDK.MediaSnapImage(mFunVideoView.getmPlayerHandler(),picFileName.toString(),mFunDevice.getId());
            int result = FunSDK.MediaStartRecord(mFunVideoView.getmPlayerHandler(),fileName,mFunDevice.getId());
            if(result>=0){
                Toast.makeText(this,getResources().getString(R.string.start_record),Toast.LENGTH_SHORT).show();
                isRecording = true ; //正在录制
            }else{
                Toast.makeText(this,getResources().getString(R.string.activity_editscene_set_falid)
                        ,Toast.LENGTH_SHORT).show();
            }
        }else{
            //正在录制视频
            if(drawable!=null&&drawable.isRunning()){
                drawable.stop();//停止帧动画
            }
            mRecordImageView.setImageResource(R.drawable.zhzj_sxt_luxiang);
            int result = FunSDK.MediaStopRecord(mFunVideoView.getmPlayerHandler(),mFunDevice.getId());//停止录制
            if(result>=0){
                isRecording= false;
                Toast.makeText(this,getResources().getString(R.string.activity_editscene_modify_success)
                        ,Toast.LENGTH_SHORT).show();
            }
        }
    }

    //声音恢复之后，得把x去掉
    private void closeVoice(){
        if(isPlayMedia){
            if(!isCloseVoice){
                //关闭声音
                isCloseVoice = true;
                closeVoiceShu.setImageResource(R.drawable.zhzj_sxt_jingyin);
                closeVoiceHeng.setBackgroundResource(R.drawable.m_voice_off);
                mFunVideoView.setMediaSound(false);
            }else{
                //恢复声音
                isCloseVoice = false;
                closeVoiceShu.setImageResource(R.drawable.zhzj_sxt_shengyin);
                closeVoiceHeng.setBackgroundResource(R.drawable.m_voice_on);
                FunSDK.MediaSetSound(mFunVideoView.getmPlayerHandler(),mSoundSeekBar.getProgress(),mFunDevice.getId());
            }

        }else{
            Log.v(TAG,"大哥还没播放呢，别点了没用的");
        }
    }

    final int REQUEST_PERMISSION_CODE_AUDIO = 0X98;//申请录制视频权限请求码
    final int REQUEST_PERMISSION_CODE_EXTERNAL_STORAGE = 0X99;//申请储存权限请求码
    private void requestPermissionForAudioRecoder(int request_code,String...permissionName){
        if(permissionName==null){
            throw new IllegalStateException("不能为Null");
        }
        ActivityCompat.requestPermissions(this,permissionName,request_code);//进行权限申请
    }

    final int LOCAL_MEDIA = 0X89 ; //本地录制视频标识
    final int LOCAL_PICTURE = 0X88; //本地截图标识
    //创建图片文件路径
    private String getFilePath(final String houZhuiMing,final int type){
        //截图和录像是属于某一个摄像头的，显示匹配当前设备的截图和录像文件
        String filePath = Environment.getExternalStorageDirectory().toString()+
                File.separator+getPackageName()+File.separator+"xiongmaitempimg"+File.separator+mFunDevice.getDevSn();
        File picFile = null ;
        if(type==LOCAL_MEDIA){
            picFile = new File(filePath+File.separator+"local_media");
        }else if(type==LOCAL_PICTURE){
            picFile = new File(filePath+File.separator+"local_picture");
        }

        if(!picFile.exists()){
            boolean bool = picFile.mkdirs();
            if(bool){
                Log.v(TAG,"截图目录创建成功");
            }else{
                Log.v(TAG,"截图目录创建失败");
            }
        }
        return picFile.toString()+File.separator+System.currentTimeMillis()+houZhuiMing;
    }


    //截取摄像头画面
    private boolean screenCapture(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            requestPermissionForAudioRecoder(REQUEST_PERMISSION_CODE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            Log.v(TAG,"截图保存失败");
            return false;
        }
        String filePath = getFilePath(".jpg",LOCAL_PICTURE);
        Log.v(TAG,"filePath:"+filePath);
        int result =  FunSDK.MediaSnapImage(mFunVideoView.getmPlayerHandler(),filePath,mFunDevice.getId());//截图咯
        if(result==0){
            Toast.makeText(this,getResources().getString(R.string.capture_success),Toast.LENGTH_SHORT).show();
            mHandler.sendEmptyMessageDelayed(2,300);
            return true;//成功
        }
        Toast.makeText(this,getResources().getString(R.string.capture_failed), Toast.LENGTH_SHORT).show();
        return false ;//失败
    }

    private void refreshImageList(){
        initImageList();
        mAdapter.notifyDataSetChanged();
    }

    private void initImageList(){
        ArrayList<File> lastTimeFile = new ArrayList<>();
        if(pictrues==null){
            pictrues = new ArrayList<>();
        }
        pictrues.clear();//清除里面的内容
        //获取截图目录下的文件
        List<String> tempList= getImageFiles();
        if(tempList.size()>0){
            //按最后修改的时间进行排序,前提是用户没对图片进行修改
            Collections.sort(tempList, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    File file1 = new File(o1);
                    File file2 = new File(o2);
                    return file1.lastModified()>file2.lastModified()?-1:
                            file1.lastModified()==file2.lastModified()?0:1;
                }
            });
            for(String path :tempList){
                pictrues.add(new RecyclerItemBean(path,0));
            }

        }
    }




    //获取截图目录下的文件名
    private List<String> getImageFiles(){
        //进行权限申请
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            requestPermissionForAudioRecoder(REQUEST_PERMISSION_CODE_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        List<String> list = new ArrayList<>();
        File picFile = new File(Environment.getExternalStorageDirectory().toString()+File.separator+getPackageName()
                +File.separator+"xiongmaitempimg"+File.separator+mFunDevice.getDevSn()+File.separator+"local_picture");
        if(!picFile.exists()){
            return  list;
        }
        //获取该目录下的文件名，注意仅仅是文件名
        String[] temp = picFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                //s表示当前目录下的文件名
                if(s.endsWith(".jpg")){
                    return true;
                }
                return false ;
            }
        });
        if(temp!=null){
            //加上父目录路径
            for(int i=0;i<temp.length;i++){
                temp[i]=picFile.toString()+File.separator+temp[i];
                list.add(temp[i]);
            }
        }

        return list;
    }


    //列表项长按事件处理器
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
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                    pictrues.remove(position);
                    mAdapter.notifyItemRemoved(position);
                }
            }
        });
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
        dialog.showDialog();
        return true;
    }

    //列表项点击事件处理器
    @Override
    public void onRecycleItemClick(View view, int position) {
        Intent intent = new Intent();
        intent.setClass(this, XiongMaiImageSeeActivity.class);
        intent.putExtra("paths", (Serializable) pictrues);
        intent.putExtra("position",position);
        startActivity(intent);
    }


    public void closeOrDisplayNavigation(){
        //显示竖屏或者横屏下的导航栏
        Configuration configuration = getResources().getConfiguration();
        int orientation = configuration.orientation;
        if(orientation==Configuration.ORIENTATION_PORTRAIT){
            if(!isPortraitShow){
                mNavigationShuPing.setVisibility(View.VISIBLE);
                isPortraitShow  = true;
            }else{
                mNavigationShuPing.setVisibility(View.GONE);
                isPortraitShow = false;

            }
        }else if(orientation==Configuration.ORIENTATION_LANDSCAPE){
            if(!isLandscapeShow){
                mNavigationHengPing.setVisibility(View.VISIBLE);
                isLandscapeShow = true;
            }else{
                mNavigationHengPing.setVisibility(View.GONE);
                isLandscapeShow = false ;
            }
        }
    }




    private void loginDevice() {
        FunSupport.getInstance().requestDeviceLogin(mFunDevice);
    }

    @Override
    public void onDeviceListChanged() {

    }

    @Override
    public void onDeviceStatusChanged(FunDevice funDevice) {
        // 设备状态变化,如果是当前登录的设备查询之后是在线的,打开设备操作界面
        if (mFunDevice.devSn.equals(funDevice.getDevSn())) {
            Log.v(TAG,"查询状态成功");
            if (funDevice.devStatus == FunDevStatus.STATUS_ONLINE) {
                funDevice.devSn = mFunDevice.devSn ;
                mFunDevice = funDevice;
                // 如果设备在线,获取设备信息
                if ((funDevice.devType == null || funDevice.devType == FunDevType.EE_DEV_UNKNOWN)) {
                    funDevice.devType = EE_DEV_NORMAL_MONITOR;//监控设备
                }

                //保存密码
                // 启动/打开设备操作界面
                if (null != mFunDevice) {
                    // 传入用户名/密码
                    if (mFunDevice.loginName==null||mFunDevice.loginName.length() == 0) {
                        // 用户名默认是:admin
                        mFunDevice.loginName = "admin";
                    }
                    if(isMengLingCamera){
                        //设置设备的类型为门铃
                        mFunDevice.devType = FunDevType.EE_DEV_IDR;
                        //门铃直接播放
                        playRealMedia();
                    }else{
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
                ToastTools.short_Toast(this, getResources().getString(R.string.camera_off));
                //恢复布局
                mProgressParentLinear.setVisibility(View.GONE);
                mHeaderView.setVisibility(View.VISIBLE);
                playBtn.setVisibility(View.VISIBLE);
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


    //OnFunDeviceOptListener方法
    @Override
    public void onDeviceLoginSuccess(FunDevice funDevice) {
        if (null != mFunDevice && null != funDevice) {
            if (mFunDevice.getId() == funDevice.getId()) {
                playRealMedia();//登入成功之后直接播放
            }
        }
    }

    //门铃唤醒成功
    @Override
    public void onDoorBellWakeUp() {
        Log.d(TAG,"门铃唤醒成功");
        isMengLingCamera = true ;
    }

    @Override
    public void onDeviceLoginFailed(FunDevice funDevice, Integer errCode) {
        //登入失败
        Log.v(TAG,"登入失败");
        ToastTools.short_Toast(this,getResources().getString(R.string.zhzj_login_fail_title));
        mProgressParentLinear.setVisibility(View.GONE);
        mHeaderView.setVisibility(View.VISIBLE);
        playBtn.setVisibility(View.VISIBLE);

    }

    @Override
    public void onDeviceGetConfigSuccess(FunDevice funDevice, String configName, int nSeq) {

    }

    @Override
    public void onDeviceGetConfigFailed(FunDevice funDevice, Integer errCode) {

    }

    //设置设备成功回调
    @Override
    public void onDeviceSetConfigSuccess(FunDevice funDevice, String configName) {
        if ( OPTimeSetting.CONFIG_NAME.equals(configName) ) {
            // 重新获取时间
            FunSupport.getInstance().requestDeviceCmdGeneral(
                    mFunDevice, new OPTimeQuery());
        }
    }

    //设置设备失败回调
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

    long mClickLastTime = 0l;

    //密码输入回调事件
    @Override
    public void onInputContent(String content, boolean confirm) {
        if(confirm){
            if(content.equals(mCameraInfo.getOriginalP())){
                //密码输入正确,保存到本地
                DataCenterSharedPreferences.getInstance(getApplicationContext()
                        , DataCenterSharedPreferences.Constant.XM_CONFIG).putString(mCurrentDevSN + SECURITY_SETTING_PWD, content).commit();
                if(mCurrentClickView!=null){
                    //密码正确后执行点击操作
                    mCurrentClickView.performClick();
                }
            }else{
                ToastUtil.shortMessage(getString(R.string.password_error));
            }

        }
    }

    class MyFrameLayout extends  FrameLayout{

        public FunVideoView getmFunVideoView() {
            return mFunVideoView;
        }

        FunVideoView mFunVideoView ;
        TextView mTextAttention ;
        public MyFrameLayout(@NonNull Context context) {
            super(context);
            initView();
        }

        public MyFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            initView();
        }

        private void initView(){
            mFunVideoView =new FunVideoView(getContext());
            mFunVideoView.setLayoutParams(generateDefaultLayoutParams());
            setLayoutParams(generateDefaultLayoutParams());
            addView(mFunVideoView);
        }

        float downX,downY,upX,upY;//保存按下和抬起的坐标
        long lastTime = 0L;//计时用的
        int touchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();//默认的最小滑动距离
        /*
         * 需要注意ACTION_DOWN类型、ACTION_UP以及其它类型的触摸事件都是采用同一个
         * MotionEvent事件来封装，这一点需要注意。
         * */
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            switch (ev.getAction()){
                case MotionEvent.ACTION_DOWN:
                    downX = ev.getX();
                    downY = ev.getY();
                    lastTime = System.currentTimeMillis();
                    Log.v(TAG,"DOWN事件");
                    break;
                case MotionEvent.ACTION_UP:
                    Log.v(TAG,"UP事件");
                    upX = ev.getX();
                    upY = ev.getY();
                    long currentTime =0L;
                    currentTime = System.currentTimeMillis();
                    long result = currentTime - lastTime;
                    Log.v(TAG,"currentTime-lastTime:"+result);
                    //抬起的时间和按下的时间小于500毫秒时，转动摄像头
                    if(result<500) {
                        Message message = Message.obtain();
                        message.what = 1;
                        //大于默认的最小滑动距离时，进行摄像头转动
                        if ((upX - downX > touchSlop) || (upY - downY > touchSlop)) {
                            if (upY > downY) {
                                //大致下滑
                                double angleDown = Math.abs(Math.atan((upX - downX) / (upY - downY)));
                                if (angleDown > 4 / Math.PI) {
                                    if (upX > downX) {
                                        Log.v(TAG, "右下滑");
                                        FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.PAN_RIGHT, false, mFunDevice.CurrChannel);
                                        message.arg1 = RIGHT;
                                    } else {
                                        Log.v(TAG, "左下滑");
                                        FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.PAN_LEFT, false, mFunDevice.CurrChannel);
                                        message.arg1 = LEFT;
                                    }
                                } else {
                                    Log.v(TAG, "正宗下滑");
                                    FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.TILT_DOWN, false, mFunDevice.CurrChannel);
                                    message.arg1 = DOWM;
                                }
                            } else {
                                //大致上滑
                                double angleUp = Math.abs(Math.atan((upY - downY) / (upX - downX)));
                                if (angleUp < 4 / Math.PI) {
                                    //右上滑或者左上滑
                                    if (upX > downX) {
                                        //右上滑
                                        Log.v(TAG, "右上滑");
                                        message.arg1 = RIGHT;
                                        FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.PAN_RIGHT, false, mFunDevice.CurrChannel);
                                    } else {
                                        //左上滑
                                        Log.v(TAG, "左上滑");
                                        FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.PAN_LEFT, false, mFunDevice.CurrChannel);
                                        message.arg1 = LEFT;
                                    }
                                } else {
                                    //正宗上滑
                                    Log.v(TAG, "正宗上滑");
                                    FunSupport.getInstance().requestDevicePTZControl(mFunDevice, EPTZCMD.TILT_UP, false, mFunDevice.CurrChannel);
                                    message.arg1 = UP;
                                }
                            }
                            mHandler.sendMessageDelayed(message, 200);
                        } else {
                            //当作是点击
                            //显示竖屏或者横屏下的导航栏
                            mClickLastTime = System.currentTimeMillis();
                            closeOrDisplayNavigation();
                            mHandler.sendEmptyMessageDelayed(3,4000);//四秒之后，自动收起导航栏
                        }
                    }
                    break;
            }
            return super.onInterceptTouchEvent(ev);
        }
    }
}
