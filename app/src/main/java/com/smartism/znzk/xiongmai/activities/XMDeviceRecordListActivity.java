package com.smartism.znzk.xiongmai.activities;


import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.constraint.Group;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.lib.FunSDK;
import com.lib.SDKCONST;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.util.StringUtils;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.xiongmai.adapter.DeviceCameraPicAdapter;
import com.smartism.znzk.xiongmai.adapter.DeviceCameraRecordAdapter;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunError;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunSupport;
import com.smartism.znzk.xiongmai.lib.funsdk.support.OnFunDeviceOptListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.OnFunDeviceRecordListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.OPCompressPic;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevRecordFile;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunFileData;
import com.smartism.znzk.xiongmai.lib.funsdk.support.widget.FunVideoView;
import com.smartism.znzk.xiongmai.lib.sdk.struct.H264_DVR_FILE_DATA;
import com.smartism.znzk.xiongmai.lib.sdk.struct.H264_DVR_FINDINFO;
import com.smartism.znzk.xiongmai.lib.sdk.struct.SDK_SearchByTime;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.smartism.znzk.xiongmai.lib.funsdk.support.FunError.EE_NO_RECORD_FILE_CURRENT;

public class XMDeviceRecordListActivity extends MZBaseActivity implements OnFunDeviceRecordListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener
, RadioGroup.OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener, OnFunDeviceOptListener, View.OnClickListener {

    private FunDevice mFunDevice = null;
    private Calendar calendar;
    private boolean byFile = true;
    String device_sn ;
    private int MaxProgress;

    private DeviceCameraRecordAdapter mRecordByTimeAdapter;
    private DeviceCameraPicAdapter mRecordByFileAdapter;
    private ListView mRecordList = null;
    private RadioGroup rgWayToGetVideo = null;
    private FunVideoView mVideoView = null;
    private LinearLayout mLayoutProgress = null;
    private TextView mTextCurrTime = null;
    private TextView mTextDuration = null;
    private SeekBar mSeekBar = null;
    private ImageView mBtnPlay,mBtnSound,mBtnSnap,mBtnRecord;

    //是否在播放声音
    private boolean mIsPlaySound = true;

    //是否在录像
    private boolean mIsRecording = false;

    private final int MESSAGE_REFRESH_PROGRESS = 0x100;
    private final int MESSAGE_SEEK_PROGRESS = 0x101;
    private final int MESSAGE_SET_IMAGE = 0x102;

    private View mBossGroup ;
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MESSAGE_REFRESH_PROGRESS:
                    refreshProgress();
                    resetProgressInterval();
                break;
                case MESSAGE_SEEK_PROGRESS:
                    seekRecordVideo(msg.arg1);
                break;
                case MESSAGE_SET_IMAGE:
                    if (mRecordByFileAdapter != null) {
                        mRecordByFileAdapter.setBitmapTempPath((String) msg.obj);
                    }
                break;
            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            device_sn = getIntent().getStringExtra("sn");
        }else{
            device_sn= savedInstanceState.getString("sn");
        }
        FunDevice funDevice = FunSupport.getInstance().findDeviceBySn(device_sn);
        if ( null == funDevice ) {
            finish();
            return;
        }
        mFunDevice  = funDevice ;
        calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        setTitle(sdf.format(calendar.getTime()));
        mBossGroup = findViewById(R.id.boss_group);
        mRecordList = (ListView) findViewById(R.id.lv_records);
        mRecordList.setOnItemClickListener(this);
        mRecordList.setOnItemLongClickListener(this);
        rgWayToGetVideo = (RadioGroup) findViewById(R.id.rg_way_to_get_video);
        rgWayToGetVideo.setOnCheckedChangeListener(this);

        mLayoutProgress = findViewById(R.id.videoProgressArea);
        mTextCurrTime = (TextView)findViewById(R.id.videoProgressCurrentTime);
        mTextDuration = (TextView)findViewById(R.id.videoProgressDurationTime);
        mSeekBar = (SeekBar)findViewById(R.id.videoProgressSeekBar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mVideoView = (FunVideoView)findViewById(R.id.funRecVideoView);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnErrorListener(this);

        mBtnPlay = (ImageView) findViewById(R.id.video_control_play);
        mBtnPlay.setOnClickListener(this);
        mBtnRecord = (ImageView) findViewById(R.id.video_control_record);
        mBtnRecord.setOnClickListener(this);
        mBtnSnap = (ImageView) findViewById(R.id.video_control_snapimage);
        mBtnSnap.setOnClickListener(this);
        mBtnSound = (ImageView) findViewById(R.id.video_control_sound);
        mBtnSound.setOnClickListener(this);

        // 1. 注册录像文件搜索结果监听 - 在搜索完成后以回调的方式返回
        FunSupport.getInstance().registerOnFunDeviceRecordListener(this);
        FunSupport.getInstance().registerOnFunDeviceOptListener(this);
        //执行按文件搜索
        ((RadioButton)findViewById(R.id.rb_by_file)).setChecked(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.calendar_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.calendar_item:
                new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        calendar.set(year, monthOfYear, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        setTitle(sdf.format(calendar.getTime()));
                        onSearchFile();
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
                return true ;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void seekRecordVideo(int progress) {
        if ( null != mVideoView ) {
            int seekPos = mVideoView.getStartTime()+progress;
            if (byFile) {
                int seekposbyfile = (progress*100)/MaxProgress;
                mVideoView.seekbyfile(seekposbyfile);
            }else{
                mVideoView.seek(seekPos);
            }
        }
    }

    @Override
    protected void onDestroy() {
        // 停止视频播放
        if ( null != mVideoView ) {
            mVideoView.stopPlayback();
        }
        // 5. 退出注销监听
        FunSupport.getInstance().removeOnFunDeviceRecordListener(this);
        FunSupport.getInstance().removeOnFunDeviceOptListener(this);
        if ( null != mHandler ) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        super.onDestroy();
    }

    private void refreshProgress() {
        int posTm = mVideoView.getPosition();
        if ( posTm > 0 ) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
            mTextCurrTime.setText(sdf.format(new Date((long)posTm*1000)));
            mSeekBar.setProgress(posTm - mVideoView.getStartTime());
        }
    }

    private void resetProgressInterval() {
        if ( null != mHandler ) {
            mHandler.removeMessages(MESSAGE_REFRESH_PROGRESS);
            mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH_PROGRESS, 500);
        }
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_xmdevice_record_list_layout ;
    }


    private int MasktoInt(int channel){
        int MaskofChannel = 0;
        MaskofChannel = (1 << channel) | MaskofChannel;
        return MaskofChannel;
    }

    private void onSearchFile() {
        showProgress("");

        int time[] = { calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DATE) };
        if (byFile) {
            H264_DVR_FINDINFO info = new H264_DVR_FINDINFO();
            info.st_1_nFileType = SDKCONST.FileType.SDK_RECORD_ALL;
            info.st_2_startTime.st_0_dwYear = time[0];
            info.st_2_startTime.st_1_dwMonth = time[1];
            info.st_2_startTime.st_2_dwDay = time[2];
            info.st_2_startTime.st_3_dwHour = 0;
            info.st_2_startTime.st_4_dwMinute = 0;
            info.st_2_startTime.st_5_dwSecond = 0;
            info.st_3_endTime.st_0_dwYear = time[0];
            info.st_3_endTime.st_1_dwMonth = time[1];
            info.st_3_endTime.st_2_dwDay = time[2];
            info.st_3_endTime.st_3_dwHour = 23;
            info.st_3_endTime.st_4_dwMinute = 59;
            info.st_3_endTime.st_5_dwSecond = 59;
            info.st_0_nChannelN0 = mFunDevice.CurrChannel;
            FunSupport.getInstance().requestDeviceFileList(mFunDevice, info);
        } else {
            SDK_SearchByTime search_info = new SDK_SearchByTime();
            search_info.st_6_nHighStreamType = 0;
            search_info.st_7_nLowStreamType = 0;
            search_info.st_1_nLowChannel = MasktoInt(mFunDevice.CurrChannel);
            search_info.st_2_nFileType = 0;
            search_info.st_3_stBeginTime.st_0_year = time[0];
            search_info.st_3_stBeginTime.st_1_month = time[1];
            search_info.st_3_stBeginTime.st_2_day = time[2];
            search_info.st_3_stBeginTime.st_4_hour = 0;
            search_info.st_3_stBeginTime.st_5_minute = 0;
            search_info.st_3_stBeginTime.st_6_second = 0;
            search_info.st_4_stEndTime.st_0_year = time[0];
            search_info.st_4_stEndTime.st_1_month = time[1];
            search_info.st_4_stEndTime.st_2_day = time[2];
            search_info.st_4_stEndTime.st_4_hour = 23;
            search_info.st_4_stEndTime.st_5_minute = 59;
            search_info.st_4_stEndTime.st_6_second = 59;
            FunSupport.getInstance().requestDeviceFileListByTime(mFunDevice, search_info);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.video_control_play:
                if (mVideoView.isPlaying()){
                    mBtnPlay.setImageResource(R.drawable.zhzj_sxt_bofang);
                    mVideoView.pause();
                }else{
                    mBtnPlay.setImageResource(R.drawable.zhzj_sxt_zanting);
                    mVideoView.resume();
                }
                break;
            case R.id.video_control_record:
                recordCamera();
                break;
            case R.id.video_control_snapimage:
//                if (mVideoView.isPlaying()) {
                screenCapture();
//                }
                break;
            case R.id.video_control_sound:
                if (mIsPlaySound && mVideoView.isPlaying()){
                    mIsPlaySound = false;
                    mVideoView.setMediaSound(false);
                    mBtnSound.setImageResource(R.drawable.zhzj_sxt_jingyin);
                }else if(mVideoView.isPlaying()){
                    mIsPlaySound = true;
                    mVideoView.setMediaSound(true);
                    mBtnSound.setImageResource(R.drawable.zhzj_sxt_shengyin);
                }
                break;
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

    //截取摄像头画面
    private boolean screenCapture(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            requestPermissionForAudioRecoder(REQUEST_PERMISSION_CODE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            Log.v(TAG,"截图保存失败");
            return false;
        }
        String filePath = getFilePath(".jpg",LOCAL_PICTURE);
        Log.v(TAG,"filePath:"+filePath);
        int result =  FunSDK.MediaSnapImage(mVideoView.getmPlayerHandler(),filePath,mFunDevice.getId());//截图咯
        if(result==0){
            Toast.makeText(this,getResources().getString(R.string.capture_success),Toast.LENGTH_SHORT).show();
            mHandler.sendEmptyMessageDelayed(2,300);
            return true;//成功
        }
        Toast.makeText(this,getResources().getString(R.string.capture_failed), Toast.LENGTH_SHORT).show();
        return false ;//失败
    }

    //录制视频
    private void recordCamera(){
        AnimationDrawable drawable= null;
        if(!mIsRecording){
            //表明没有正在录制视频,开启
            mBtnRecord.setImageResource(R.drawable.recor_icon_anim);
            drawable  = (AnimationDrawable) mBtnRecord.getDrawable();
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
            FunSDK.MediaSnapImage(mVideoView.getmPlayerHandler(),picFileName.toString(),mFunDevice.getId());
            int result = FunSDK.MediaStartRecord(mVideoView.getmPlayerHandler(),fileName,mFunDevice.getId());
            if(result>=0){
                Toast.makeText(this,getResources().getString(R.string.start_record),Toast.LENGTH_SHORT).show();
                mIsRecording = true ; //正在录制
            }else{
                Toast.makeText(this,getResources().getString(R.string.activity_editscene_set_falid)
                        ,Toast.LENGTH_SHORT).show();
            }
        }else{
            //正在录制视频
            if(drawable!=null&&drawable.isRunning()){
                drawable.stop();//停止帧动画
            }
            mBtnRecord.setImageResource(R.drawable.zhzj_sxt_luxiang);
            int result = FunSDK.MediaStopRecord(mVideoView.getmPlayerHandler(),mFunDevice.getId());//停止录制
            if(result>=0){
                mIsRecording= false;
                Toast.makeText(this,getResources().getString(R.string.activity_editscene_modify_success)
                        ,Toast.LENGTH_SHORT).show();
            }
        }
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

    private void playRecordVideoByTime(FunDevRecordFile recordFile) {
        mVideoView.stopPlayback();
        showProgress("");
        int[] time = { calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0 };
        // 只给定起始时间播放
//		int absTime = FunSDK.ToTimeType(time) + recordFile.recStartTime;
//    	mVideoView.playRecordByTime(mFunDevice.getDevSn(), absTime);
        // 给定起始时间和结束时间播放
        int fromTime = FunSDK.ToTimeType(time) + recordFile.recStartTime;
        int toTime = FunSDK.ToTimeType(time) + recordFile.recEndTime;
        mVideoView.playRecordByTime(mFunDevice.getDevSn(), fromTime, toTime, mFunDevice.CurrChannel);
        //打开声音
        mVideoView.setMediaSound(true);
    }

    @Override
    public void onRequestRecordListSuccess(List<FunDevRecordFile> files) {
        if (files == null || files.size() == 0) {
            ToastTools.short_Toast(this,getString(R.string.xmdrs_video_empty));
        }
        // 3. 在回调中处理录像列表结果 - onRequestRecordListSuccess()
        // 显示录像文件列表
        mRecordByTimeAdapter = new DeviceCameraRecordAdapter(this, files);
        mRecordList.setAdapter(mRecordByTimeAdapter);
        hideProgress();
        // 如果录像存在,默认开始播放第一段录像
        if ( files.size() > 0 ) {
            mRecordByTimeAdapter.setPlayingIndex(0);
            playRecordVideoByTime(files.get(0));
        }
    }

    private void playRecordVideoByFile(FunFileData recordFile) {
        mVideoView.stopPlayback();
        showProgress("");
        mVideoView.playRecordByFile(mFunDevice.getDevSn(), recordFile.getFileData(), mFunDevice.CurrChannel);
        mVideoView.setMediaSound(true);
    }

    @Override
    public void onRequestRecordListFailed(Integer errCode) {
        hideProgress();
        if(errCode==EE_NO_RECORD_FILE_CURRENT){
            ToastTools.short_Toast(this,getResources().getString(R.string.xmdrs_video_empty));
        }else{
            ToastTools.short_Toast(this,getResources().getString(R.string.xmdrs_get_failed,""));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (byFile) {
            if (null != mRecordByFileAdapter) {
                FunFileData recordFile = mRecordByFileAdapter.getRecordFile(position);
                if (null != recordFile) {
                    mRecordByFileAdapter.setPlayingIndex(position);
                    playRecordVideoByFile(recordFile);
                }
            }
        } else {
            if ( null != mRecordByTimeAdapter) {
                FunDevRecordFile recordFile = mRecordByTimeAdapter.getRecordFile(position);
                if (null != recordFile) {
                    mRecordByTimeAdapter.setPlayingIndex(position);
                    playRecordVideoByTime(recordFile);
                }
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_by_file:
                byFile = true;
                break;
            case R.id.rb_by_time:
                byFile = false;
                break;
        }
        mVideoView.stopPlayback();
        onSearchFile();
    }

    private void refreshPlayInfo() {
        int startTm = mVideoView.getStartTime();
        int endTm = mVideoView.getEndTime();
        MaxProgress = endTm-startTm;
        if (startTm > 0 && endTm > startTm) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
            mTextCurrTime.setText(sdf.format(new Date((long) startTm * 1000)));
            mTextDuration.setText(sdf.format(new Date((long) endTm * 1000)));
            mSeekBar.setMax(endTm - startTm);
            mSeekBar.setProgress(0);
            mLayoutProgress.setVisibility(View.VISIBLE);
            resetProgressInterval();
        } else {
            mLayoutProgress.setVisibility(View.GONE);
            cleanProgressInterval();
        }
    }

    private void cleanProgressInterval() {
        if ( null != mHandler ) {
            mHandler.removeMessages(MESSAGE_REFRESH_PROGRESS);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if ( fromUser ) {
            if ( null != mHandler ) {
                mHandler.removeMessages(MESSAGE_SEEK_PROGRESS);
                Message msg = new Message();
                msg.what = MESSAGE_SEEK_PROGRESS;
                msg.arg1 = progress;
                mHandler.sendMessageDelayed(msg, 300);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // 播放失败
        ToastTools.short_Toast(this,getResources().getString(R.string.xmdrs_play_failed,FunError.getErrorStr(extra)));
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        hideProgress();
        refreshPlayInfo();
        String path = mVideoView.captureImage(null);
        Message message = Message.obtain();
        message.what = MESSAGE_SET_IMAGE;
        message.obj = path;
        mHandler.sendMessageDelayed(message, 200);
    }

    @Override
    public void onDeviceLoginSuccess(FunDevice funDevice) {

    }

    @Override
    public void onDoorBellWakeUp() {

    }

    @Override
    public void onDeviceLoginFailed(FunDevice funDevice, Integer errCode) {

    }

    @Override
    public void onDeviceGetConfigSuccess(FunDevice funDevice, String configName, int nSeq) {
        onSearchFile();
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

        List<FunFileData> files = new ArrayList<FunFileData>();

        if (null != funDevice
                && null != mFunDevice
                && funDevice.getId() == mFunDevice.getId()) {

            for (H264_DVR_FILE_DATA data : datas) {
                FunFileData funFileData = new FunFileData(data, new OPCompressPic());
                files.add(funFileData);
            }

            if (files.size() == 0) {
                ToastTools.short_Toast(this,getString(R.string.xmdrs_video_empty));
            } else {
                mRecordByFileAdapter = new DeviceCameraPicAdapter(this, mRecordList, mFunDevice, files);
                mRecordList.setAdapter(mRecordByFileAdapter);
                if (mRecordByFileAdapter != null) {
                    mRecordByFileAdapter.release();
                }
            }

            // 如果录像存在,默认开始播放第一段录像
            if ( files.size() > 0 ) {
                mBossGroup.setVisibility(View.VISIBLE);
                playRecordVideoByFile(files.get(0));
                mRecordByFileAdapter.setPlayingIndex(0);
            }
        }
        hideProgress();
    }

    @Override
    public void onDeviceFileListGetFailed(FunDevice funDevice) {

    }
}
