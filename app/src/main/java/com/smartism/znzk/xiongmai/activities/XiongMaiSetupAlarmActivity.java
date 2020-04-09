package com.smartism.znzk.xiongmai.activities;


import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.Group;
import android.support.design.widget.BottomSheetDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.lib.DevSDK;
import com.lib.FunSDK;
import com.smartism.znzk.R;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.view.SwitchButton.SwitchButton;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunError;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunSupport;
import com.smartism.znzk.xiongmai.lib.funsdk.support.OnFunDeviceOptListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.DetectBlind;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.DetectMotion;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;
import com.smartism.znzk.xiongmai.lib.sdk.struct.H264_DVR_FILE_DATA;
import com.smartism.znzk.xiongmai.utils.DeviceConfigType;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;

import java.util.ArrayList;
import java.util.List;


import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import static com.smartism.znzk.util.DataCenterSharedPreferences.Constant.ALARM_PUSH_STATUS;
import static com.smartism.znzk.xiongmai.activities.XMSettingActivity.contains;
import static com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevType.EE_DEV_IDR;

public class XiongMaiSetupAlarmActivity extends MZBaseActivity implements OnFunDeviceOptListener {

    private SwitchButton mBtnSwitchMotion = null;
    private SwitchButton mBtnSwitchMotionRecord = null;
    private SwitchButton mBtnSwitchMotionCapture = null;
    private SwitchButton mBtnSwitchMotionPushMsg = null;
    private LinearLayout layoutMotionDetectionAlarmLevel = null;
    private TextView mAlarmSensitivityTv;
    private SwitchButton mBtnSwitchBlock = null;
    private SwitchButton mBtnSwitchBlockRecord = null;
    private SwitchButton mBtnSwitchBlockCapture = null;
    private SwitchButton mBtnSwitchBlockPushMsg = null;
    private SwitchButton mBtnDoorbellPushMsg;

    private boolean mSaveDoorbellFlag = true; //默认值打开

    FunDevice mFunDevice;
    private String mDeviceSn;

    private String[] DEV_CONFIGS = null; //保存要获取的配置
    /**
     * 本界面需要获取到的设备配置信息(监控类)
     */
    private final String[] DEV_CONFIGS_FOR_CAMERA = {
            // 移动侦测
            DetectMotion.CONFIG_NAME,

            // 视频遮掉
            DetectBlind.CONFIG_NAME,

    };

    private final String[] DEV_CONFIGS_FOR_CHANNELS = {
            //移动侦测
            DetectMotion.CONFIG_NAME,

            //视频遮挡
            DetectBlind.CONFIG_NAME,
    };
    // 设置配置信息的时候,由于有多个,通过下面的列表来判断是否所有的配置都设置完成了
    private List<String> mSettingConfigs = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mDeviceSn = getIntent().getStringExtra("sn");
        } else {
            mDeviceSn = savedInstanceState.getString("sn");
        }

        setTitle(getString(R.string.xmsaa_title));
        FunDevice funDevice = FunSupport.getInstance().findDeviceBySn(mDeviceSn);
        if (null == funDevice) {
            finish();
            return;
        }
        mFunDevice = funDevice;
        // 监控类的设备报警
        DEV_CONFIGS = DEV_CONFIGS_FOR_CHANNELS;

        // 注册设备操作监听
        FunSupport.getInstance().registerOnFunDeviceOptListener(this);

        // 获取报警配置信息
        tryGetAlarmConfig();

        //设备订阅报警功能
        FunSupport.getInstance().mpsLinkDevice(mFunDevice);


        initView();
        initEvent();
        initRecordMethodBottom();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("sn", mDeviceSn);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销监听
        FunSupport.getInstance().removeOnFunDeviceOptListener(this);
        //保存报警消息订阅设置到本地
        DataCenterSharedPreferences.getInstance(getApplicationContext()
                , DataCenterSharedPreferences.Constant.XM_CONFIG).putBoolean(mDeviceSn + ALARM_PUSH_STATUS, mSaveDoorbellFlag).commit();
    }

    private void tryGetAlarmConfig() {
        if (null != mFunDevice) {
            showProgress("");

            for (String configName : DEV_CONFIGS) {

                // 删除老的配置信息
                mFunDevice.invalidConfig(configName);

                // 重新搜索新的配置信息
                if (contains(DeviceConfigType.DeviceConfigCommon, configName)) {
                    FunSupport.getInstance().requestDeviceConfig(mFunDevice,
                            configName);
                } else if (contains(DeviceConfigType.DeviceConfigByChannel, configName)) {
                    FunSupport.getInstance().requestDeviceConfig(mFunDevice, configName, mFunDevice.CurrChannel);
                }
            }
        }
    }


    private void initView() {
        //移动侦测
        mBtnSwitchMotion = findViewById(R.id.btnSwitchMotionDetection);
        mBtnSwitchMotionRecord = findViewById(R.id.btnSwitchMotionDetectionAlarmRecord);
        mBtnSwitchMotionCapture = findViewById(R.id.btnSwitchMotionDetectionAlarmCapture);
        mBtnSwitchMotionPushMsg = findViewById(R.id.btnSwitchMotionDetectionAlarmPushMsg);
        layoutMotionDetectionAlarmLevel = findViewById(R.id.layoutMotionDetectionAlarmLevel);
        mAlarmSensitivityTv = findViewById(R.id.titleMotionDetectionAlarmLevelTip);

        //视频遮挡
        mBtnSwitchBlock = findViewById(R.id.btnSwitchVideoBlock);
        mBtnSwitchBlockRecord = findViewById(R.id.btnSwitchVideoBlockAlarmRecord);
        mBtnSwitchBlockCapture = findViewById(R.id.btnSwitchVideoBlockAlarmCapture);
        mBtnSwitchBlockPushMsg = findViewById(R.id.btnSwitchVideoBlockAlarmPushMsg);

        //门铃
        mBtnDoorbellPushMsg = findViewById(R.id.btnSwitchDoorbell);
    }


    private void initEvent() {
        layoutMotionDetectionAlarmLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mAlarmSeitivityDialog.isShowing()) {
                    int index = mAlarmSensitivityData.indexOf(mAlarmSensitivityTv.getText().toString());
                    mAlarmSensitivityList.performItemClick(null, index, index);
                    mAlarmSeitivityDialog.show();
                }
            }
        });

        mBtnDoorbellPushMsg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    //取消订阅
                    mBtnSwitchBlockPushMsg.setCheckedImmediately(false);
                    mBtnSwitchMotionPushMsg.setCheckedImmediately(false);
                }
            }
        });

        mBtnSwitchMotionPushMsg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked&&!mBtnDoorbellPushMsg.isChecked()){
                    //总开关没有打开
                    mBtnDoorbellPushMsg.setCheckedImmediately(true);
                }
            }
        });

        mBtnSwitchBlockPushMsg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked&&!mBtnDoorbellPushMsg.isChecked()){
                    //总开关没有打开
                    mBtnDoorbellPushMsg.setCheckedImmediately(true);
                }
            }
        });

        mSaveDoorbellFlag = DataCenterSharedPreferences.getInstance(getApplicationContext()
                , DataCenterSharedPreferences.Constant.XM_CONFIG).getBoolean(mDeviceSn + ALARM_PUSH_STATUS, true);
        mBtnDoorbellPushMsg.setCheckedImmediately(mSaveDoorbellFlag);//设置开关
        //初始化报警消息订阅
        if(mSaveDoorbellFlag){
            FunSupport.getInstance().mpsLinkDevice(mFunDevice);
        }else{
            FunSupport.getInstance().mpsUnLinkDevice(mFunDevice.getDevSn());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.zc_save_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_item_menu:
                //保存设置
                //    trySaveAlarmConfig();
                tryNewSaveAlarmConfig();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    //这里应用做成Fragment的
    BottomSheetDialog mAlarmSeitivityDialog;
    ListView mAlarmSensitivityList;
    List<String> mAlarmSensitivityData = new ArrayList<>();

    private void initRecordMethodBottom() {
        mAlarmSensitivityData.clear();
        mAlarmSensitivityData.add(getString(R.string.xmsaa_alarm_level_dowm));
        mAlarmSensitivityData.add(getString(R.string.xmsaa_alarm_level_middle));
        mAlarmSensitivityData.add(getString(R.string.xmsaa_alarm_level_high));

        mAlarmSeitivityDialog = new BottomSheetDialog(this);
        View view = (View) getLayoutInflater().inflate(R.layout.zhuji_setting_offline, null);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDisplayMetrics().heightPixels / 4);
        view.setLayoutParams(lp);
        mAlarmSensitivityList = view.findViewById(R.id.bottom_lv);
        mAlarmSensitivityList.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, mAlarmSensitivityData));
        mAlarmSeitivityDialog.setContentView(view);
        mAlarmSensitivityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mAlarmSeitivityDialog.dismiss();
                if (mAlarmSensitivityData.get(position).equals(mAlarmSensitivityTv.getText().toString())) {
                    return;
                } else {
                    mAlarmSensitivityTv.setText(mAlarmSensitivityData.get(position));
                }
            }
        });

        //把BottomSheetDialog的背景透明
        ViewGroup viewGroup = (ViewGroup) view.getParent();
        viewGroup.setBackgroundColor(Color.parseColor("#00000000"));

    }

    private void tryNewSaveAlarmConfig() {
        // 监控类
        boolean beMotionChanged = false;
        boolean beBlockChanged = false;

        DetectMotion detectMotion = (DetectMotion) mFunDevice.getConfig(DetectMotion.CONFIG_NAME);
        if (null != detectMotion) {

            detectMotion.Enable = mBtnSwitchMotion.isChecked();
            beMotionChanged = true;


            detectMotion.event.RecordEnable = mBtnSwitchMotionRecord.isChecked();
            detectMotion.event.RecordMask = DevSDK.SetSelectHex(
                    detectMotion.event.RecordMask, mFunDevice.CurrChannel,
                    detectMotion.event.RecordEnable);
            beMotionChanged = true;


            detectMotion.event.SnapEnable = mBtnSwitchMotionCapture.isChecked();
            detectMotion.event.SnapShotMask = DevSDK.SetSelectHex(
                    detectMotion.event.SnapShotMask, mFunDevice.CurrChannel,
                    detectMotion.event.SnapEnable);
            beMotionChanged = true;

            detectMotion.event.MessageEnable = mBtnSwitchMotionPushMsg.isChecked();
            beMotionChanged = true;


            int index = mAlarmSensitivityData.indexOf(mAlarmSensitivityTv.getText().toString());

            detectMotion.Level = changeLevelToDetect(index);
            beMotionChanged = true;

        }

        DetectBlind detectBlind = (DetectBlind) mFunDevice.getConfig(DetectBlind.CONFIG_NAME);
        if (null != detectBlind) {
            detectBlind.Enable = mBtnSwitchBlock.isChecked();
            beBlockChanged = true;

            detectBlind.event.RecordEnable = mBtnSwitchBlockRecord.isChecked();
            beBlockChanged = true;

            detectBlind.event.SnapEnable = mBtnSwitchBlockCapture.isChecked();
            beBlockChanged = true;

            detectBlind.event.MessageEnable = mBtnSwitchBlockPushMsg.isChecked();
            beBlockChanged = true;
        }

        mSettingConfigs.clear();

        if (beBlockChanged || beMotionChanged || (mBtnDoorbellPushMsg.isChecked() != mSaveDoorbellFlag)) {
            showProgress("");
            //订阅消息
            if (mBtnDoorbellPushMsg.isChecked()) {
                //设备订阅报警功能
                FunSupport.getInstance().mpsLinkDevice(mFunDevice);
            } else {
                //取消订阅
                FunSupport.getInstance().mpsUnLinkDevice(mFunDevice.devSn);
            }
            mSaveDoorbellFlag = mBtnDoorbellPushMsg.isChecked();
            if (beMotionChanged) {
                synchronized (mSettingConfigs) {
                    mSettingConfigs.add(detectMotion.getConfigName());
                }
                FunSupport.getInstance().requestDeviceSetConfig(mFunDevice, detectMotion);
            }
            if (beBlockChanged) {
                synchronized (mSettingConfigs) {
                    mSettingConfigs.add(detectBlind.getConfigName());
                }
                FunSupport.getInstance().requestDeviceSetConfig(mFunDevice, detectBlind);
            }
        } else {
            //配置没有变化，提示用户
            ToastUtil.shortMessage(getString(R.string.xmdss_peizhi_no_change));
        }
    }

    //保存
    private void trySaveAlarmConfig() {

        // 监控类
        boolean beMotionChanged = false;
        boolean beBlockChanged = false;

        DetectMotion detectMotion = (DetectMotion) mFunDevice.getConfig(DetectMotion.CONFIG_NAME);
        if (null != detectMotion) {
            if (mBtnSwitchMotion.isChecked() != detectMotion.Enable) {
                detectMotion.Enable = mBtnSwitchMotion.isChecked();
                beMotionChanged = true;
            }

            if (mBtnSwitchMotionRecord.isChecked() != detectMotion.event.RecordEnable) {
                detectMotion.event.RecordEnable = mBtnSwitchMotionRecord.isChecked();
                detectMotion.event.RecordMask = DevSDK.SetSelectHex(
                        detectMotion.event.RecordMask, mFunDevice.CurrChannel,
                        detectMotion.event.RecordEnable);
                beMotionChanged = true;
            }

            if (mBtnSwitchMotionCapture.isChecked() != detectMotion.event.SnapEnable) {
                detectMotion.event.SnapEnable = mBtnSwitchMotionCapture.isChecked();
                detectMotion.event.SnapShotMask = DevSDK.SetSelectHex(
                        detectMotion.event.SnapShotMask, mFunDevice.CurrChannel,
                        detectMotion.event.SnapEnable);
                beMotionChanged = true;
            }

            if (mBtnSwitchMotionPushMsg.isChecked() != detectMotion.event.MessageEnable) {
                detectMotion.event.MessageEnable = mBtnSwitchMotionPushMsg.isChecked();
                beMotionChanged = true;
            }

            int index = mAlarmSensitivityData.indexOf(mAlarmSensitivityTv.getText().toString());
            if (index != changeLevelToUI(detectMotion.Level)) {
                detectMotion.Level = changeLevelToDetect(index);
                beMotionChanged = true;
            }
        }

        DetectBlind detectBlind = (DetectBlind) mFunDevice.getConfig(DetectBlind.CONFIG_NAME);
        if (null != detectBlind) {
            if (mBtnSwitchBlock.isChecked() != detectBlind.Enable) {
                detectBlind.Enable = mBtnSwitchBlock.isChecked();
                beBlockChanged = true;
            }

            if (mBtnSwitchBlockRecord.isChecked() != detectBlind.event.RecordEnable) {
                detectBlind.event.RecordEnable = mBtnSwitchBlockRecord.isChecked();
                beBlockChanged = true;
            }

            if (mBtnSwitchBlockCapture.isChecked() != detectBlind.event.SnapEnable) {
                detectBlind.event.SnapEnable = mBtnSwitchBlockCapture.isChecked();
                beBlockChanged = true;
            }

            if (mBtnSwitchBlockPushMsg.isChecked() != detectBlind.event.MessageEnable) {
                detectBlind.event.MessageEnable = mBtnSwitchBlockPushMsg.isChecked();
                beBlockChanged = true;
            }
        }

        mSettingConfigs.clear();

        if (beBlockChanged || beMotionChanged || (mBtnDoorbellPushMsg.isChecked() != mSaveDoorbellFlag)) {
            showProgress("");
            //订阅消息
            if (mBtnDoorbellPushMsg.isChecked()) {
                //设备订阅报警功能
                FunSupport.getInstance().mpsLinkDevice(mFunDevice);
            } else {
                //取消订阅
                FunSupport.getInstance().mpsUnLinkDevice(mFunDevice.devSn);
            }

            if (beMotionChanged) {
                synchronized (mSettingConfigs) {
                    mSettingConfigs.add(detectMotion.getConfigName());
                }

                FunSupport.getInstance().requestDeviceSetConfig(mFunDevice, detectMotion);
            }

            if (beBlockChanged) {

                synchronized (mSettingConfigs) {
                    mSettingConfigs.add(detectBlind.getConfigName());
                }

                FunSupport.getInstance().requestDeviceSetConfig(mFunDevice, detectBlind);
            }

        } else {
            //配置没有变化，提示用户
            ToastUtil.shortMessage(getString(R.string.xmdss_peizhi_no_change));
        }
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_xiong_mai_setup_alarm_layout;
    }


    private boolean isCurrentUsefulConfig(String configName) {
        for (int i = 0; i < DEV_CONFIGS.length; i++) {
            if (DEV_CONFIGS[i].equals(configName)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 判断是否所有需要的配置都获取到了
     *
     * @return
     */
    private boolean isAllConfigGetted() {
        for (String configName : DEV_CONFIGS) {
            if (null == mFunDevice.getConfig(configName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Level报警灵敏度转换,界面低级/中级/高级(0,1,2),需要和实际级别做一个转换
     *
     * @param level
     * @return
     */
    private int changeLevelToUI(int level) {
        int uiLevel = (level == 0 ? 1 : (level % 2 + level / 2)) - 1;
        return Math.max(0, uiLevel);
    }

    private int changeLevelToDetect(int uiLevel) {
        return (uiLevel + 1) * 2;
    }

    private void refreshAlarmConfig() {
        // 监控类
        // 移动侦测
        DetectMotion detectMotion = (DetectMotion) mFunDevice.getConfig(DetectMotion.CONFIG_NAME);
        if (null != detectMotion) {
            mBtnSwitchMotion.setCheckedImmediatelyNoEvent(detectMotion.Enable);
            mBtnSwitchMotionRecord.setCheckedImmediatelyNoEvent(detectMotion.event.RecordEnable);
            mBtnSwitchMotionCapture.setCheckedImmediatelyNoEvent(detectMotion.event.SnapEnable);
            mBtnSwitchMotionPushMsg.setCheckedImmediatelyNoEvent(detectMotion.event.MessageEnable);
            mAlarmSensitivityTv.setText(mAlarmSensitivityData.get(changeLevelToUI(detectMotion.Level)));
        }

        // 视频遮挡
        DetectBlind detectBlind = (DetectBlind) mFunDevice.getConfig(DetectBlind.CONFIG_NAME);
        if (null != detectBlind) {
            mBtnSwitchBlock.setCheckedImmediatelyNoEvent(detectBlind.Enable);
            mBtnSwitchBlockRecord.setCheckedImmediatelyNoEvent(detectBlind.event.RecordEnable);
            mBtnSwitchBlockCapture.setCheckedImmediatelyNoEvent(detectBlind.event.SnapEnable);
            mBtnSwitchBlockPushMsg.setCheckedImmediatelyNoEvent(detectBlind.event.MessageEnable);
        }
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
        if (null != mFunDevice
                && funDevice.getId() == mFunDevice.getId()
                && isCurrentUsefulConfig(configName)) {
            if (isAllConfigGetted()) {
                hideProgress();
            }

            refreshAlarmConfig();
        }
    }

    @Override
    public void onDeviceGetConfigFailed(FunDevice funDevice, Integer errCode) {
        hideProgress();
    }

    @Override
    public void onDeviceSetConfigSuccess(FunDevice funDevice, String configName) {
        if (null != mFunDevice
                && funDevice.getId() == mFunDevice.getId()) {
            synchronized (mSettingConfigs) {
                if (mSettingConfigs.contains(configName)) {
                    mSettingConfigs.remove(configName);
                }

                if (mSettingConfigs.size() == 0) {
                    // 所有的设置修改都已经完成
                    hideProgress();
                }
            }

            //更新View状态
            refreshAlarmConfig();
            ToastUtil.shortMessage(getString(R.string.activity_editscene_modify_success));
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
