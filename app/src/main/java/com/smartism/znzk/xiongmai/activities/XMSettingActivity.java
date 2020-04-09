package com.smartism.znzk.xiongmai.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.os.Handler;


import com.lib.FunSDK;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.camera.MainControlActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.xiongmai.fragment.XMSettingMainFragment;
import com.smartism.znzk.xiongmai.fragment.XMVideoSettingFragment;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunSupport;
import com.smartism.znzk.xiongmai.lib.funsdk.support.OnFunDeviceListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.OnFunDeviceOptListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.AVEncVideoWidget;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.CameraParam;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.CameraParamEx;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.FVideoOsdLogo;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.SimplifyEncode;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.SystemInfo;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevStatus;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;
import com.smartism.znzk.xiongmai.lib.sdk.struct.H264_DVR_FILE_DATA;
import com.smartism.znzk.xiongmai.utils.DeviceConfigType;
import com.smartism.znzk.xiongmai.utils.XMProgressDialog;

import java.util.Arrays;
import java.util.List;

public class XMSettingActivity extends FragmentActivity implements OnFunDeviceListener,OnFunDeviceOptListener
        , XMVideoSettingFragment.VideoSettingInterface,XMSettingMainFragment.XMSettingMainTiaoZhuang{


    private final static String TAG = "XMSettingActivity";
    private final static int LOAD_DEVICE_INFO_TIME_OUT = 0X89 ;

    public ProgressDialog mProgressDialog = null;
    XMProgressDialog xmProgressDialog ;
    TextView contactName ;
    Button viewDeviceVersionBtn ;
    CameraInfo  mCameraInfo ;
    FunDevice mFunDevice ;
    String deviceSn  ;
    SystemInfo mSystemInfo ;//保存设备信息
    CameraParam mCameraParam ;
    XMSettingMainFragment mSettingMain ;
    private long  mDeviceId ;

    private Handler mHandler = new Handler();
    private Runnable mLoadDeviceInfoTimeOutRunable = new Runnable() {
        @Override
        public void run() {
            ToastUtil.shortMessage(getResources().getString(R.string.xmdss_get_device_info_timeout));
            cancelInProgress();

            //退出
            finish();
        }
    };

    //需要获取的监控设备配置信息
    private final String[] DEV_CONFIGS_FOR_CAMERA = {
            // 获取参数:SimplifyEncode -> 清晰度
            SimplifyEncode.CONFIG_NAME,
            // 获取参数:FVideoOsdLogo
            FVideoOsdLogo.CONFIG_NAME,
            // 获取参数:CameraParam -> 图像上下翻转/图像左右翻转/背光补偿/降噪
            CameraParam.CONFIG_NAME,
            // 获取参数:CameraParamEx -> 电子防抖/测光模式/宽动态
            CameraParamEx.CONFIG_NAME,
            // OSD水印内容
            AVEncVideoWidget.CONFIG_NAME,
            //设备相关信息
            SystemInfo.CONFIG_NAME
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_main);


        if(savedInstanceState==null){
            mCameraInfo = (CameraInfo) getIntent().getSerializableExtra("camera_info");
            deviceSn = getIntent().getStringExtra("sn");
            mDeviceId =  getIntent().getLongExtra("device_id",0);


            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Bundle bundle = new Bundle();
            bundle.putString("sn",deviceSn);
            bundle.putSerializable("camera_info",mCameraInfo);
            bundle.putSerializable("device_id",mDeviceId);
            mSettingMain = XMSettingMainFragment.getInstance(bundle);
            transaction.add(R.id.fragContainer,mSettingMain);
            transaction.commit();
        }else{
            mCameraInfo = (CameraInfo) savedInstanceState.getSerializable("camera_info");
            deviceSn = savedInstanceState.getString("sn");
            mDeviceId = savedInstanceState.getLong("device_id");
        }

        mFunDevice = new FunDevice();
        mFunDevice.devSn = deviceSn ;
        mFunDevice.devStatus = FunDevStatus.STATUS_OFFLINE ;
        initView();

        showInProgress("",true,false);
        requestDeviceStatus();

        //初始化远程推送ip
        if(MainApplication.app.getAppGlobalConfig().isDebug()){
            FunSupport.getInstance().mpsInit("https://dev.smart-ism.com/jdm/tpush/xiongmai");
        }else{
            FunSupport.getInstance().mpsInit(DataCenterSharedPreferences.getInstance(XMSettingActivity.this,
                    DataCenterSharedPreferences.Constant.CONFIG).getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS,"") + "/jdm/tpush/xiongmai");
        }

        //超时
        mHandler.postDelayed(mLoadDeviceInfoTimeOutRunable,20*1000);//20秒超时

        //对门铃进行唤醒
        FunSDK.DevWakeUp(FunSupport.getInstance().getHandler(),mFunDevice.devSn,mFunDevice.getId());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("sn",deviceSn);
        outState.putLong("device_id",mDeviceId);
        outState.putSerializable("camera_info",mCameraInfo);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 注册设备操作回调
        FunSupport.getInstance().registerOnFunDeviceOptListener(this);
        // 监听设备类事件
        FunSupport.getInstance().registerOnFunDeviceListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FunSupport.getInstance().removeOnFunDeviceListener(this);
        FunSupport.getInstance().removeOnFunDeviceOptListener(this);
    }

    public void back(View v){
        if(getSupportFragmentManager().getBackStackEntryCount()>0){
            getSupportFragmentManager().popBackStack();
        }else{
            finish();
        }
    }

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

    public void cancelInProgress() {
        synchronized (this) {
            if (mProgressDialog != null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                mProgressDialog = null;
            }
        }
    }


    // 设备登录
    private void requestDeviceStatus() {
        FunSupport.getInstance().requestDeviceStatus(mFunDevice.getDevType(), mFunDevice.devSn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mHandler!=null){
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void initView(){
        contactName = findViewById(R.id.contactName);
        viewDeviceVersionBtn = findViewById(R.id.viewDeviceVersionBtn);
        viewDeviceVersionBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(mSystemInfo!=null){
                    popupWindowDeviceInfo(mSystemInfo);
                }else{
                    ToastTools.short_Toast(XMSettingActivity.this,getResources().getString(R.string.get_camera_info_fail));
                }
            }
        });
        contactName.setText(mCameraInfo.getN());//设置设备名
    }

    public static boolean contains(String[] stringArray, String source) {
        // 转换为list
        List<String> tempList = Arrays.asList(stringArray);
        // 利用list的包含方法,进行判断
        return tempList.contains(source);
    }

    //查询监控设备相关配置
    private void tryGetCameraConfig() {
        if (null != mFunDevice) {
            for (String configName : DEV_CONFIGS_FOR_CAMERA) {
                // 删除老的配置信息
                mFunDevice.invalidConfig(configName);
                //根据是否需要传通道号 重新搜索新的配置信息
                if (contains(DeviceConfigType.DeviceConfigCommon, configName)) {
                    FunSupport.getInstance().requestDeviceConfig(mFunDevice,
                            configName);
                }else if (contains(DeviceConfigType.DeviceConfigByChannel, configName)) {
                    FunSupport.getInstance().requestDeviceConfig(mFunDevice, configName, mFunDevice.CurrChannel);
                }

            }
        }
    }

    /**
     * 判断是否所有需要的配置都获取到了
     */
    private boolean isAllConfigGetted() {
        for (String configName : DEV_CONFIGS_FOR_CAMERA) {
            if (null == mFunDevice.getConfig(configName)) {
                return false;
            }
        }
        return true;
    }

    private String getConnectTypeStringId(int netConnectType) {
        if ( netConnectType == 0 ) {
            return "P2P";
        } else if ( netConnectType == 1 ) {
            return getResources().getString(R.string.xm_zhuangfamoshi);
        } else if ( netConnectType == 2 ) {
            return getResources().getString(R.string.xm_ip_zhilian);
        } else if ( netConnectType == 5) {
            return "RPS";
        }

        return getResources().getString(R.string.xm_unkonw);
    }

    PopupWindow mPopupWindow  = new PopupWindow();
    private void popupWindowDeviceInfo(SystemInfo systemInfo){

        mPopupWindow.setFocusable(true);
        mPopupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mPopupWindow.dismiss();
                return true ;
            }
        });
        mPopupWindow.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        mPopupWindow.setHeight(WindowManager.LayoutParams.MATCH_PARENT);

        View view = getLayoutInflater().inflate(R.layout.device_info_xm,null);
        TextView sn = view.findViewById(R.id.sn_textview);
        sn.setText(systemInfo.getSerialNo());
        TextView xh = view.findViewById(R.id.device_xinghao);
        xh.setText(systemInfo.getHardware());
        TextView yj = view.findViewById(R.id.device_yingjian);
        yj.setText(systemInfo.getHardwareVersion());
        TextView runtime = view.findViewById(R.id.device_runtime);
        runtime.setText(systemInfo.getDeviceRunTimeWithFormat());
        TextView ct = view.findViewById(R.id.device_connect_type);
        ct.setText(getConnectTypeStringId(mFunDevice.getNetConnectType()));
        TextView up = view.findViewById(R.id.device_updata);
        up.setText(systemInfo.getBuildTime());
        mPopupWindow.setContentView(view);
        mPopupWindow.showAtLocation(contactName, Gravity.CENTER,0,0);
    }


    @Override
    public void onDeviceListChanged() {

    }

    //查询设备状态
    @Override
    public void onDeviceStatusChanged(FunDevice funDevice) {
        Log.v(TAG,"查询状态成功");
        if (funDevice.devStatus == FunDevStatus.STATUS_ONLINE) {
            mFunDevice = funDevice;
            tryGetCameraConfig();
            //获取设备的通道信息，该信息影响着设备是否支持关闭音频设置
            FunSupport.getInstance().requestGetDevChnName(funDevice);
        } else {
            cancelInProgress();
            mHandler.removeCallbacks(mLoadDeviceInfoTimeOutRunable);
            ToastTools.short_Toast(this, getResources().getString(R.string.camera_off));
            finish(); //结束
            Log.d(TAG,"设备不在线");
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

    @Override
    public void onDeviceLoginSuccess(FunDevice funDevice) {

    }

    @Override
    public void onDoorBellWakeUp() {

    }

    @Override
    public void onDeviceLoginFailed(FunDevice funDevice, Integer errCode) {

    }

    //获取设备配置成功
    @Override
    public void onDeviceGetConfigSuccess(FunDevice funDevice, String configName, int nSeq) {
        if ( null != mFunDevice
                && funDevice.getId() == mFunDevice.getId()) {
            mFunDevice = funDevice ;
            if(isAllConfigGetted()){
                //查看所有的配置是否都获取到了,获取到了取消进度条
                cancelInProgress();
                mHandler.removeCallbacks(mLoadDeviceInfoTimeOutRunable);
                mSystemInfo = (SystemInfo) mFunDevice.getConfig(SystemInfo.CONFIG_NAME);
                mCameraParam = (CameraParam) mFunDevice.getConfig(CameraParam.CONFIG_NAME);
            }
        }
    }

    //获取配置失败
    @Override
    public void onDeviceGetConfigFailed(FunDevice funDevice, Integer errCode) {
        cancelInProgress();
        mHandler.removeCallbacks(mLoadDeviceInfoTimeOutRunable);
        ToastTools.short_Toast(this,getResources().getString(R.string.EE_DVR_ARSP_QUERY_ERROR));
        finish();//结束
    }

    //设置设备配置成功
    @Override
    public void onDeviceSetConfigSuccess(FunDevice funDevice, String configName) {
        if(funDevice!=null&&configName.equals(CameraParam.CONFIG_NAME)){
            cancelInProgress();
            ToastTools.short_Toast(this,getResources().getString(R.string.success));
        }
    }

    //设置设备配置失败
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

    //图像倒置功能实现
    @Override
    public boolean changeImageReverse(int flag, boolean isOpen) {
        showInProgress("",true,false);
        if(flag==TOP_BOTTOM_REVERSE){
            if(isOpen){
                //倒置上下,不正常显示
                mCameraParam.setPictureFlip(false);
            }else{
                //上下倒置，倒回来
                mCameraParam.setPictureFlip(true);
            }
        }else if(flag==RIGHT_LEFT_REVERSE){
            if(isOpen){
                //倒置左右,进行倒置
                mCameraParam.setPictureMirror(false);
            }else{
                //恢复正常状态
                mCameraParam.setPictureMirror(true);
            }
        }
        FunSupport.getInstance().requestDeviceSetConfig(mFunDevice,mCameraParam);
        return true;
    }

    //实现Fragment切换
    @Override
    public void changeFragment(int id) {
        switch (id){
            case R.id.video_control:
                //媒体设置
                if(mCameraParam==null){
                    ToastTools.short_Toast(this,getResources().getString(R.string.EE_CAMERA_XM_QUERY_ERROR));
                    return ;
                }
                Bundle bundle = new Bundle();
                bundle.putBoolean("isFlipTop",mCameraParam.getPictureFlip());
                bundle.putBoolean("isFlipRight",mCameraParam.getPictureMirror());
                XMVideoSettingFragment fragment = XMVideoSettingFragment.getInstance(bundle);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in_center,R.anim.fade_out_center,R.anim.fade_in_center,R.anim.fade_out_center);
                transaction.replace(R.id.fragContainer,fragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
        }
    }
}
