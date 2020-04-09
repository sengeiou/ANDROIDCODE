package com.smartism.znzk.xiongmai.activities;


import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.lib.SDKCONST;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.view.SwitchButton.SwitchButton;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunError;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunLog;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunSupport;
import com.smartism.znzk.xiongmai.lib.funsdk.support.OnFunDeviceOptListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.CloudStorage;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.RecordParam;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.RecordParamEx;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.SimplifyEncode;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevType;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;
import com.smartism.znzk.xiongmai.lib.funsdk.support.utils.MyUtils;
import com.smartism.znzk.xiongmai.lib.sdk.struct.H264_DVR_FILE_DATA;
import com.smartism.znzk.xiongmai.utils.DeviceConfigType;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;

import java.util.ArrayList;
import java.util.List;

import static com.smartism.znzk.xiongmai.activities.XMSettingActivity.contains;

//
public class XMDeviceSetupRecord extends MZBaseActivity implements View.OnClickListener, OnFunDeviceOptListener, SeekBar.OnSeekBarChangeListener {

    private TextView mTextPreRecord = null;
    private SeekBar mBarPreRecord = null;
    private TextView mTextRecordLength = null;
    private SeekBar mBarRecordLength = null;
    private SwitchButton mRecordAudio = null;
    private FunDevice mFunDevice = null;
    private TextView mRecordMethodTv ;
    BottomSheetDialog mBottomSheetDialog ;
    private View mQingXiDuParent,mRecordMethodParent;
    private TextView mQingXiDuTv;

    /**
     * 本界面需要获取到的设备配置信息
     */
    private final String[] DEV_CONFIGS_FOR_CAMARA = {
            // 获取参数:SimplifyEncode -> audioEable
            SimplifyEncode.CONFIG_NAME,

            // 获取参数:RecordParam
            RecordParam.CONFIG_NAME,

          RecordParamEx.CONFIG_NAME,
            // 获取参数:CloudStorage
            // CloudStorage.CONFIG_NAME
    };

    private final String[] DEV_CONFIGS_FOR_CHANNELS = {
            // 获取参数:RecordParam
            RecordParam.CONFIG_NAME,
    };

    private String[] DEV_CONFIGS = DEV_CONFIGS_FOR_CHANNELS;

    // 设置配置信息的时候,由于有多个,通过下面的列表来判断是否所有的配置都设置完成了
    private List<String> mSettingConfigs = new ArrayList<String>();

    private String mDeviceSn  ;
    private View mBossGroup ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置标题
        setTitle(getResources().getString(R.string.video_set));
        if(savedInstanceState==null){
            mDeviceSn = getIntent().getStringExtra("sn");
        }else{
            mDeviceSn= savedInstanceState.getString("sn");
        }
        mRecordMethodParent = findViewById(R.id.record_method_parent);
        mRecordMethodTv = findViewById(R.id.record_method_tv);
        mBossGroup  = findViewById(R.id.boss_group);
        mQingXiDuTv = findViewById(R.id.qingxidu_tv);
        mQingXiDuParent = findViewById(R.id.qingxidu_parent);
        mTextPreRecord = (TextView)findViewById(R.id.setupRecordPreValue);
        mBarPreRecord = (SeekBar)findViewById(R.id.setupRecordPreSeekbar);
        mBarPreRecord.setOnSeekBarChangeListener(this);
        mTextRecordLength = (TextView)findViewById(R.id.setupRecordLengthValue);
        mBarRecordLength = (SeekBar)findViewById(R.id.setupRecordLengthSeekbar);
        mBarRecordLength.setOnSeekBarChangeListener(this);
        mRecordAudio  = findViewById(R.id.setupRecordAudioBtn);
        mQingXiDuParent.setOnClickListener(this);
        mRecordMethodParent.setOnClickListener(this);

        FunDevice funDevice = FunSupport.getInstance().findDeviceBySn(mDeviceSn);
        if ( null == funDevice ) {
            finish();
            return;
        }
        mFunDevice  = funDevice ;
        //判断设备是否支持关闭音频设置
        if (mFunDevice.channel!=null&&mFunDevice.channel.nChnCount == 1) {
            DEV_CONFIGS = DEV_CONFIGS_FOR_CAMARA;
            findViewById(R.id.close_audio_parent).setVisibility(View.VISIBLE);
        }
        // 注册设备操作监听
        FunSupport.getInstance().registerOnFunDeviceOptListener(this);
        //初始化清晰度设置选项
        initBottomSheetDialog();

        //初始化录像方式设置选项
        initRecordMethodBottom();

        // 获取配置信息
        tryGetRecordConfig();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("sn",mDeviceSn);
        super.onSaveInstanceState(outState);
    }

    ListView listView ;
    List<String> valuesOff = new ArrayList<>();
    void initBottomSheetDialog(){
        //当前设备的画质，六个等级  对应1/2/3/4/5/6  ->很差/较差/一般/好/很好/最好   按顺序写
        String[] value = getResources().getStringArray(R.array.xmdss_qingxidudefinition_values);
        for(int i=0;i<value.length;i++){
            valuesOff.add(value[i]);
        }
//        valuesOff.add("很差");
//        valuesOff.add("较差");
//        valuesOff.add("一般");
//        valuesOff.add("好");
//        valuesOff.add("很好");
//        valuesOff.add("最好");
        mBottomSheetDialog = new BottomSheetDialog(this);
        View view = (View) getLayoutInflater().inflate(R.layout.zhuji_setting_offline,null);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getResources().getDisplayMetrics().heightPixels/2);
        view.setLayoutParams(lp);
        listView = view.findViewById(R.id.bottom_lv);
        listView.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_single_choice,valuesOff));
        mBottomSheetDialog.setContentView(view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //发送指令
                mBottomSheetDialog.dismiss();
                //如果用户选中的清晰度和当前的一致，则不进行更改，直接返回
                if(valuesOff.get(position).equals(mQingXiDuTv.getText().toString())){
                    return ;
                }else{
                    mQingXiDuTv.setText(valuesOff.get(position));
                }
            }
        });
        //把BottomSheetDialog的背景透明，以便显示出我们的背景
        ViewGroup viewGroup = (ViewGroup) view.getParent();
        viewGroup.setBackgroundColor(Color.parseColor("#00000000"));
    }

    BottomSheetDialog mRecordMethodDialog ;
    ListView mRecordListView ;
    List<String> mRecordList = new ArrayList<>();
    private void initRecordMethodBottom(){
        String[] value = getResources().getStringArray(R.array.xmdss_recordmethod_values);
        for(int i=0;i<value.length;i++){
            mRecordList.add(value[i]);
        }
//        mRecordList.add("从不录像");
//        mRecordList.add("始终录像");
//        mRecordList.add("联动录像");

        mRecordMethodDialog = new BottomSheetDialog(this);
        View view = (View) getLayoutInflater().inflate(R.layout.zhuji_setting_offline,null);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getResources().getDisplayMetrics().heightPixels/4);
        view.setLayoutParams(lp);
        mRecordListView = view.findViewById(R.id.bottom_lv);
        mRecordListView.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_single_choice,mRecordList));
        mRecordMethodDialog.setContentView(view);
        mRecordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mRecordMethodDialog.dismiss();
                if(mRecordList.get(position).equals(mRecordMethodTv.getText().toString())){
                    return ;
                }else{
                    mRecordMethodTv.setText(mRecordList.get(position));
                }
            }
        });

        //把BottomSheetDialog的背景透明
        ViewGroup viewGroup = (ViewGroup) view.getParent();
        viewGroup.setBackgroundColor(Color.parseColor("#00000000"));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.zc_save_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_item_menu:
                //保存设置
                trySaveRecordConfig();
                return true ;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    private void trySaveRecordConfig() {
        if(mBossGroup.getVisibility()==View.GONE){
            return ;
        }
        boolean beSimplifyEncodeChanged = false;
        SimplifyEncode simplifyEncode = (SimplifyEncode)mFunDevice.getConfig(SimplifyEncode.CONFIG_NAME);
        if ( null != simplifyEncode ) {
            //录像音频
            if ( simplifyEncode.mainFormat.AudioEnable
                    != mRecordAudio.isChecked() ){
                simplifyEncode.mainFormat.AudioEnable = mRecordAudio.isChecked();
                beSimplifyEncodeChanged = true;
            }
            // 清晰度
            if (simplifyEncode.mainFormat.video.Quality != (valuesOff.indexOf(mQingXiDuTv.getText().toString())+1)) {
                simplifyEncode.mainFormat.video.Quality =valuesOff.indexOf(mQingXiDuTv.getText().toString())+1;
                beSimplifyEncodeChanged = true;
            }
        }

        boolean beRecordParamChanged = false;
        RecordParam recordParam = (RecordParam)mFunDevice.getConfig(RecordParam.CONFIG_NAME);
        if ( null != recordParam ) {
            //预录
            if (mBarPreRecord.getProgress() != recordParam.getPreRecordTime()) {
                recordParam.setPreRecordTime(mBarPreRecord.getProgress());
                beRecordParamChanged = true;
            }
            //录像段时长
            if (mBarRecordLength.getProgress() != recordParam.getPacketLength()) {
                recordParam.setPacketLength((mBarRecordLength.getProgress() + 1));
                beRecordParamChanged = true;
            }
            int mode = getNewIntRecordMode(mRecordMethodTv.getText().toString());

            recordParam.recordMode = getStringRecordMode(mode == 1 ? 2 : mode);
            // 如果是联动配置的话，把普通录像去掉
            for (int i = 0; i < SDKCONST.NET_N_WEEKS; ++i) {
                recordParam.mask[i][0] = MyUtils.getHexFromInt(mode == 2 ? 6 : 7);
            }
            beRecordParamChanged = true;

        }
        if ( beSimplifyEncodeChanged
                || beRecordParamChanged) {
            showProgress("");
            // 保存SimplifyEncode
            if ( beSimplifyEncodeChanged ) {
                synchronized (mSettingConfigs) {
                    mSettingConfigs.add(simplifyEncode.getConfigName());
                }
                FunSupport.getInstance().requestDeviceSetConfig(mFunDevice, simplifyEncode);
            }
            if ( beRecordParamChanged ) {
                synchronized (mSettingConfigs) {
                    mSettingConfigs.add(recordParam.getConfigName());
                }
                //配置保存
                FunSupport.getInstance().requestDeviceSetConfig(mFunDevice, recordParam);
            }
        } else {
            ToastTools.short_Toast(this,getString(R.string.xmdss_peizhi_no_change));
        }
    }


    private int getNewIntRecordMode(String mode){
        if(mode.equals(mRecordList.get(0))){
            return 0 ;
        }else if(mode.equals(mRecordList.get(1))){
            return 1 ;
        }else{
            return 2 ;
        }
    }
    //用于设置录像的方式，3种 1表示始终录像，0表示从不录像，2表示联动录像
    private String getStringRecordMode(int i) {
        if (i == 0) {
            return "ClosedRecord";
        } else if (i == 1) {
            return "ManualRecord";
        } else {
            return "ConfigRecord";
        }
    }


    private void tryGetRecordConfig() {
        if ( null != mFunDevice ) {
            showProgress("");
            for ( String configName : DEV_CONFIGS ) {
                // 删除老的配置信息
                mFunDevice.invalidConfig(configName);
                if(mFunDevice.getDevType() == FunDevType.EE_DEV_SMALLEYE || configName != CloudStorage.CONFIG_NAME){
                    // 重新搜索新的配置信息
                    if (contains(DeviceConfigType.DeviceConfigCommon, configName)) {
                        FunSupport.getInstance().requestDeviceConfig(mFunDevice,
                                configName);
                    }else if (contains(DeviceConfigType.DeviceConfigByChannel, configName)) {
                        FunSupport.getInstance().requestDeviceConfig(mFunDevice, configName, mFunDevice.CurrChannel);
                    }
                }
            }
        }
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_xmdevice_setup_record_layout;
    }


    @Override
    protected void onDestroy() {
        // 注销监听
        FunSupport.getInstance().removeOnFunDeviceOptListener(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.qingxidu_parent:
                if(!mBottomSheetDialog.isShowing()){
                    int index = valuesOff.indexOf(mQingXiDuTv.getText().toString());
                    listView.performItemClick(listView,index,index);//设置默认的选中项
                    mBottomSheetDialog.show();
                }
                break ;
            case R.id.record_method_parent:
                if(!mRecordMethodDialog.isShowing()){
                    int index = mRecordList.indexOf(mRecordMethodTv.getText().toString());
                    mRecordListView.performItemClick(mRecordListView,index,index);//设置默认的选中项
                    mRecordMethodDialog.show();
                }
                break;
        }
    }


    private boolean isCurrentUsefulConfig(String configName) {
        for ( int i = 0; i < DEV_CONFIGS.length; i ++ ) {
            if ( DEV_CONFIGS[i].equals(configName) ) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否所有需要的配置都获取到了
     */
    private boolean isAllConfigGetted() {
        for ( String configName : DEV_CONFIGS ) {
            if ( null == mFunDevice.getConfig(configName) ) {
                if(mFunDevice.getDevType() == FunDevType.EE_DEV_SMALLEYE || configName != CloudStorage.CONFIG_NAME)
                {
                    return false;
                }
            }
        }
        return true;
    }

    //判断当前录像的方式   1表示始终录像，0表示从不录像，2表示联动录像
    private int getIntRecordMode(String s) {
        if (s.equals("ClosedRecord")) {
            return 0;
        } else if (s.equals("ManualRecord")) {
            return 1;
        } else {
            return 2;
        }
    }

    //获取到设备的配置，同步View状态
    private void refreshRecordConfig() {
        SimplifyEncode simplifyEncode = (SimplifyEncode)mFunDevice.getConfig(SimplifyEncode.CONFIG_NAME);
        if ( null != simplifyEncode ) {
            int quality = simplifyEncode.mainFormat.video.Quality ; //当前设备的画质，六个等级  对应1/2/3/4/5/6  ->很差/较差/一般/好/很好/最好
            mQingXiDuTv.setText(valuesOff.get(quality-1));
            mRecordAudio.setCheckedImmediatelyNoEvent(simplifyEncode.mainFormat.AudioEnable);
        }
        RecordParam recordParam = (RecordParam)mFunDevice.getConfig(RecordParam.CONFIG_NAME);
        if ( null != recordParam ) {
            //设置当前设备的状态信息
            mTextPreRecord.setText(getResources().getQuantityString(R.plurals.plurals_second,recordParam.getPreRecordTime(),recordParam.getPreRecordTime()));
            mBarPreRecord.setProgress(recordParam.getPreRecordTime());
            mTextRecordLength.setText(getResources().getQuantityString(R.plurals.plurals_minute,recordParam.getPacketLength(),recordParam.getPacketLength()));
            mBarRecordLength.setProgress(recordParam.getPacketLength()-1);


            String realMode = "" ;
            if(getIntRecordMode(recordParam.getRecordMode()) == 2){
                boolean bNoramlRecord = MyUtils.getIntFromHex(recordParam.mask[0][0]) == 7;
                FunLog.i("setup record", "TTT--->" + recordParam.recordMode + "bNoramlRecord"
                        + (bNoramlRecord ? 1 : 2));

                //1 - >ManualRecord  2->ConfigRecord  0->ClosedRecord
             //   realMode =getStringRecordMode(bNoramlRecord?1:2);
                realMode = mRecordList.get(bNoramlRecord?1:2);
            }
            else{
                int mode = getIntRecordMode(recordParam.getRecordMode());
              //  realMode = getStringRecordMode(mode);
                realMode = mRecordList.get(mode);
            }
            mRecordMethodTv.setText(realMode);
            System.out.println("当前的录像模式:"+realMode);
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
        //获取配置成功回调
        if ( null != mFunDevice
                && funDevice.getId() == mFunDevice.getId()
                && isCurrentUsefulConfig(configName) ) {
            if ( isAllConfigGetted() ) {
               hideProgress();
            }

            refreshRecordConfig();
        }
    }

    @Override
    public void onDeviceGetConfigFailed(FunDevice funDevice, Integer errCode) {
        hideProgress();
        //获取配置失败回调
  //      ToastTools.short_Toast(this,FunError.getErrorStr(errCode));
    }

    //设置设备成功回调
    @Override
    public void onDeviceSetConfigSuccess(FunDevice funDevice, String configName) {
        if (null!=mFunDevice&& funDevice.getId()==mFunDevice.getId()) {
            synchronized (mSettingConfigs) {
                if (mSettingConfigs.contains(configName)) {
                    mSettingConfigs.remove(configName);
                }
                if (mSettingConfigs.size() == 0) {
                    ToastTools.short_Toast(this,getString(R.string.activity_editscene_modify_success));
                    hideProgress();
                }
            }
        }
    }

    //设置设备失败的回调
    @Override
    public void onDeviceSetConfigFailed(FunDevice funDevice, String configName, Integer errCode) {
        ToastTools.short_Toast(this,FunError.getErrorStr(errCode));
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


    //SeekBar改变的时间回调处理
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //注意点，api26以下SeekBar最小值默认是0，不可通过设置setMin方式来改变
        if(seekBar.equals(mBarPreRecord)){
            mTextPreRecord.setText(getResources().getQuantityString(R.plurals.plurals_second,progress,progress));
        }
        else if(seekBar.equals(mBarRecordLength)){
            progress++;
            mTextRecordLength.setText(getResources().getQuantityString(R.plurals.plurals_minute,progress,progress));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
