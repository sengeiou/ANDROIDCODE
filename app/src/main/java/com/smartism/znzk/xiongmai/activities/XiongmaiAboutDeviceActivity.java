package com.smartism.znzk.xiongmai.activities;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.lib.EDEV_JSON_ID;
import com.lib.EUIMSG;
import com.lib.FunSDK;
import com.lib.IFunSDKResult;
import com.lib.MsgContent;
import com.smartism.znzk.R;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunError;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunSupport;
import com.smartism.znzk.xiongmai.lib.funsdk.support.OnFunDeviceOptListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.JsonConfig;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.OPTimeQuery;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.OPTimeSetting;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.StatusNetInfo;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.SystemInfo;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;
import com.smartism.znzk.xiongmai.lib.funsdk.support.widget.TimeTextView;
import com.smartism.znzk.xiongmai.lib.sdk.bean.DefaultConfigBean;
import com.smartism.znzk.xiongmai.lib.sdk.bean.HandleConfigData;
import com.smartism.znzk.xiongmai.lib.sdk.struct.H264_DVR_FILE_DATA;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

//雄迈关于设备
public class XiongmaiAboutDeviceActivity extends MZBaseActivity implements View.OnClickListener , OnFunDeviceOptListener, IFunSDKResult {


    private TextView mTextDevSn = null;     //序列号
    private TextView mTextDevModel = null;  //设备版本
    private TextView mTextDevSWVer = null;  //软件版本
    private TextView mTextDevPubDate = null;    //设备发布时间
    private TextView mTextDevPubTime = null; //设备显示时间
    private TextView mTextDevNatCode = null; //网络模式
    private Button mBtnDefaltConfig = null;//恢复出厂设置


    private FunDevice mFunDevice ;
    private String mDeviceSn ;
    private DefaultConfigBean mdefault = null;
    private int mHandler;
    private AlertDialog mDefaultDialog ;
    private AlertDialog mSyncDialog ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            mDeviceSn = getIntent().getStringExtra("sn");
        }else{
            mDeviceSn= savedInstanceState.getString("sn");
        }
        setTitle(getString(R.string.xmada_about_device)); //标题
        mFunDevice = FunSupport.getInstance().findDeviceBySn(mDeviceSn);
        if ( null == mFunDevice ) {
            finish();
            return;
        }
        mdefault = new DefaultConfigBean();
        mHandler = FunSDK.RegUser(this);

        initView();
        initEvent();
        initDialog();


        // 注册设备操作监听
        FunSupport.getInstance().registerOnFunDeviceOptListener(this);
        //请求获取设备信息
        requestSystemInfo();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("sn",mDeviceSn);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除绑定
        FunSupport.getInstance().removeOnFunDeviceOptListener(this);
        FunSDK.UnRegUser(mHandler);
    }

    // 0: p2p 1:转发 2IP直连
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


    private void requestSystemInfo() {
        showProgress("");//显示进度条
        // 获取系统信息
        FunSupport.getInstance().requestDeviceConfig(
                mFunDevice, SystemInfo.CONFIG_NAME);

        // 获取时间
        FunSupport.getInstance().requestDeviceCmdGeneral(
                mFunDevice, new OPTimeQuery());
    }

    private void refreshSystemInfo() {
        if ( null != mFunDevice ) {
            SystemInfo systemInfo = (SystemInfo)mFunDevice.getConfig(SystemInfo.CONFIG_NAME);
            if ( null != systemInfo ) {
                // 序列号
                mTextDevSn.setText(systemInfo.getSerialNo());
                // 设备型号
                mTextDevModel.setText(systemInfo.getHardware());
                // 软件版本号
                mTextDevSWVer.setText(systemInfo.getSoftwareVersion());

                // 发布时间
                mTextDevPubDate.setText(systemInfo.getBuildTime());
                // 设备连接方式
                mTextDevNatCode.setText(getConnectTypeStringId(mFunDevice.getNetConnectType()));

            }

            OPTimeQuery showDevtimeQuery = (OPTimeQuery) mFunDevice
                    .getConfig(OPTimeQuery.CONFIG_NAME);
            if (null != showDevtimeQuery) {
                String mOPTimeQuery = showDevtimeQuery.getOPTimeQuery();
                mTextDevPubTime.setText(mOPTimeQuery);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date;
                try {
                    date = sdf.parse(mOPTimeQuery);
                    ((TimeTextView) mTextDevPubTime).setDevSysTime(date.getTime());
                    ((TimeTextView) mTextDevPubTime).onStartTimer();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initView(){
        mTextDevSn =findViewById(R.id.textDeviceSN);
        mTextDevModel = findViewById(R.id.textDeviceModel);
        mTextDevSWVer = findViewById(R.id.textDeviceSWVer);
        mTextDevPubDate = findViewById(R.id.textDevicePubDate);
        mTextDevPubTime=findViewById(R.id.textDevicePubTime);
        mTextDevPubTime.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mTextDevPubTime.getPaint().setAntiAlias(true);
        mTextDevPubTime.setClickable(true);
        mTextDevNatCode = findViewById(R.id.textDeviceNatCode);
        mBtnDefaltConfig = findViewById(R.id.defealtconfig);
    }

    private void initEvent(){
        mBtnDefaltConfig.setOnClickListener(this);
        mTextDevPubTime.setOnClickListener(this);
    }


    //恢复出厂设置对话框
    private void initDialog(){
        mDefaultDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.xmada_recovery))
                .setMessage(getString(R.string.xmada_recovery_tip))
                .setPositiveButton(getString(R.string.permission_ensure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgress("");//显示进度条
                        mdefault.setAllConfig(1);
                        FunSDK.DevSetConfigByJson(mHandler, mFunDevice.devSn, JsonConfig.OPERATION_DEFAULT_CONFIG,
                                HandleConfigData.getSendData(JsonConfig.OPERATION_DEFAULT_CONFIG, "0x1", mdefault), -1 , 20000, mFunDevice.getId());
                    }
                }).setNegativeButton(getString(R.string.permission_cancel),null)
                .setCancelable(true)
                .create();

        mSyncDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.xmada_sync_device_time))
                .setMessage(getString(R.string.xmada_sync_device_time_tip))
                .setPositiveButton(getString(R.string.permission_ensure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgress("");//显示进度条
                        syncCameraTime();
                    }
                })
                .setNegativeButton(getString(R.string.permission_cancel),null)
                .create();
    }
    @Override
    public int setLayoutId() {
        return R.layout.activity_xiongmai_about_device;
    }

    @Override
    public void onClick(View v) {
       switch (v.getId()){
           case R.id.defealtconfig:
               if(!mDefaultDialog.isShowing()){
                   mDefaultDialog.show();
               }
               break ;
           case R.id.textDevicePubTime:
               if(!mSyncDialog.isShowing()){
                   mSyncDialog.show();
               }
               break ;
       }
    }

    @Override
    public int OnFunSDKResult(Message message, MsgContent msgContent) {
        hideProgress();
        switch (message.what){
            case EUIMSG.DEV_SET_JSON: {
                if (message.arg1 < 0) {
                    ToastUtil.shortMessage(getString(R.string.xmada_recovery_failed));
                }else {
                    if (msgContent.str.equals(JsonConfig.OPERATION_DEFAULT_CONFIG)) {
                        JSONObject object = new JSONObject();
                        object.put("Action", "Reboot");
                        FunSDK.DevCmdGeneral(mHandler, mFunDevice.devSn, EDEV_JSON_ID.OPMACHINE, JsonConfig.OPERATION_MACHINE, 1024, 5000,
                                HandleConfigData.getSendData(JsonConfig.OPERATION_MACHINE, "0x1", object).getBytes(), -1, 0);
                        ToastUtil.shortMessage(getString(R.string.xmada_reboot_device));
                    }
                }
            }
            break;
        }
        return 0;
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
        if ( null != mFunDevice
                && funDevice.getId() == mFunDevice.getId()
                && ( SystemInfo.CONFIG_NAME.equals(configName)
                || StatusNetInfo.CONFIG_NAME.equals(configName)
                || OPTimeQuery.CONFIG_NAME.equals(configName)) ) {
            hideProgress();//隐藏
            refreshSystemInfo();
        }
    }

    @Override
    public void onDeviceGetConfigFailed(FunDevice funDevice, Integer errCode) {
        hideProgress();
        ToastUtil.shortMessage(FunError.getErrorStr(errCode));
    }

    @Override
    public void onDeviceSetConfigSuccess(FunDevice funDevice, String configName) {
        if ( OPTimeSetting.CONFIG_NAME.equals(configName) ) {
            hideProgress();
            // 重新获取时间
            FunSupport.getInstance().requestDeviceCmdGeneral(
                    mFunDevice, new OPTimeQuery());
            ToastUtil.shortMessage(getString(R.string.xmada_sync_time_success));
        }
    }

    @Override
    public void onDeviceSetConfigFailed(FunDevice funDevice, String configName, Integer errCode) {
        ToastUtil.shortMessage(FunError.getErrorStr(errCode));
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
}
