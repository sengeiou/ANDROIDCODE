package com.smartism.znzk.activity.alert;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.basic.G;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.alert.NoCloseAlarmActivity;
import com.smartism.znzk.activity.SmartMedicine.MedicineSetTimeActivity;
import com.smartism.znzk.activity.camera.CameraListActivity;
import com.smartism.znzk.activity.common.JdqWorkTimeSettingActivity;
import com.smartism.znzk.activity.common.TriggerSettingActivity;
import com.smartism.znzk.activity.common.ZoneAttributeSettingActivity;
import com.smartism.znzk.activity.device.DeviceSetActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.view.CheckSwitchButton;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.zhicheng.tasks.LoadCommandsInfo;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

import java.util.List;

public class ChooseAudioSettingMode extends ActivityParentActivity implements View.OnClickListener, LoadCommandsInfo.ILoadCommands {

    private final int REQUEST_TRIGGER =0X68,REQUEST_ZONE=0x65,REQUEST_CODE_JIAYU = 0X34;
    public static final String SEND_RESULT_EXTRAS = "result";
    public static final int TIME_CODE = 100;
    public static final int TIME_DELAY = 10 * 1000;
    private LinearLayout rl_setting_devices_audio, rl_setting_message_audio, rl_device_bipc, rl_device_setting,ll_receivepush,
            rl_pl_device_med,rl_pl_device_lowbattery,rl_no_close_gate_alarm;
    private Intent mIntent;
    public static String devId;
    public static String mode;
    private DeviceInfo operationDevice;
    private boolean isGroup = false;
    private boolean isShowPush;
    private ZhujiInfo zhujiInfo ;
    private CheckSwitchButton check_receivepush;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TIME_CODE:
                    cancelInProgress();
                    Toast.makeText(mContext, getString(R.string.time_out), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_audio_setting_mode);
        operationDevice = (DeviceInfo) getIntent().getSerializableExtra("device");
        isGroup = getIntent().getBooleanExtra("isgroup", false);
        zhujiInfo = DatabaseOperator.getInstance().queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
        initView();
        initValue();
        initEvent();
//		Util.showToast(this, "" + (operationDevice==null));

        //宏才隐藏
        if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            rl_pl_device_lowbattery.setVisibility(View.GONE);
        }
        if(!DeviceInfo.ControlTypeMenu.group.value().equals(operationDevice.getControlType())){
            //子账户不能绑定摄像头
            JavaThreadPool.getInstance().excute(new Runnable() {
                @Override
                public void run() {
                    final ZhujiInfo zhujiInfo = DatabaseOperator.getInstance().queryDeviceZhuJiInfo(operationDevice.getZj_id());
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(!zhujiInfo.isAdmin()){
                                rl_device_bipc.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            });
        }else{
            rl_device_bipc.setVisibility(View.GONE);
        }
        //智力得继电器和防区设置
        initZhiliDeViews();

        //润龙是否接收锁端通知
        initRunLongZhinengsuo();

        //佳玉
        initYuJia();
    }

    private void initRunLongZhinengsuo(){
        if(!Actions.VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            return ;
        }
        rl_pl_device_lowbattery.setVisibility(View.GONE);
        if (operationDevice.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())) {
            if (zhujiInfo != null && zhujiInfo.isAdmin() || (zhujiInfo.getRolek().equals("lock_num_admin") ||
                    zhujiInfo.getRolek().equals("lock_num_partner") || zhujiInfo.getRolek().equals("lock_num_old"))) {
                ll_receivepush.setVisibility(View.VISIBLE);
                isShowPush = true;
            }
        }
        if (!isShowPush) {
            return;
        }
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject object = new JSONObject();
                object.put("did", operationDevice.getId());
                JSONArray array = new JSONArray();
                JSONObject o = new JSONObject();
                o.put("vkey", "alertlevel_unlock");
                array.add(o);
                object.put("vkeys", array);
                String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/u/p/list", object, ChooseAudioSettingMode.this);
//                [{"key":"alertlevel_unlock"}]
                if ("-3".equals(result)) {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ChooseAudioSettingMode.this, getString(R.string.activity_editscene_s_erro),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-4".equals(result)) {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ChooseAudioSettingMode.this, getString(R.string.operator_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-5".equals(result)) {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ChooseAudioSettingMode.this, getString(R.string.net_error_nopermission),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (result != null) {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }

                    final JSONArray array1 = JSON.parseArray(result);

                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            if (array1 != null) {
                                String keyalue = array1.getJSONObject(0).getString("key");
                                String value = array1.getJSONObject(0).getString("value");
                                if (keyalue.equals("alertlevel_unlock")) {
                                    if (value == null || !value.equals("0")) {
                                        check_receivepush.setCheckedNotListener(true);
                                    } else {
                                        check_receivepush.setCheckedNotListener(false);
                                    }
                                }
                            }
                        }
                    });
                } else {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ChooseAudioSettingMode.this, getString(R.string.net_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }


    private LinearLayout rl_pl_jidianqitime_setting,rl_pl_fangqu_setting,rl_pl_edge_setting;
    private TextView current_jidiantitime_tv,current_fangqu_tv,current_edge_tv;
    private void initZhiliDeViews(){
        rl_pl_jidianqitime_setting = findViewById(R.id.rl_pl_jidianqitime_setting);
        current_jidiantitime_tv = findViewById(R.id.current_jidiantitime_tv);
        rl_pl_jidianqitime_setting.setOnClickListener(this);
        rl_pl_fangqu_setting = findViewById(R.id.rl_pl_fangqu_setting);
        current_fangqu_tv = findViewById(R.id.current_fangqu_tv);
        rl_pl_fangqu_setting.setOnClickListener(this);
        rl_pl_edge_setting = findViewById(R.id.rl_pl_edge_setting);
        current_edge_tv = findViewById(R.id.current_edge_tv);
        rl_pl_edge_setting.setOnClickListener(this);

        if (operationDevice.getNt() == 1 && "1".equalsIgnoreCase(zhujiInfo.getSetInfos().get(ZhujiInfo.GNSetNameMenu.supportLevel.value()))) {
            rl_pl_fangqu_setting.setVisibility(View.VISIBLE);
        }

        if(!Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            return ;
        }

        //设置继电器设置和防区设置的可见性
        if(operationDevice!=null&&operationDevice.getCa().equals(DeviceInfo.CaMenu.jdq.value())){
            rl_pl_jidianqitime_setting.setVisibility(View.VISIBLE);
        }else if(operationDevice!=null&&operationDevice.getCa().equals("fq")){
            //致利德的都是有线防区 - 第二款会不一样
            rl_pl_fangqu_setting.setVisibility(View.VISIBLE);
            rl_pl_edge_setting.setVisibility(View.VISIBLE);
        }
        new LoadCommandsInfo(this).execute(operationDevice.getId());
    }



    private LinearLayout rl_pl_fangqu_attribute;
    private TextView current_fangqu_tv_attribute ;
    //佳玉防区属性设置
    private  void initYuJia(){
        if(operationDevice!=null&&operationDevice.getCa().equals("fq")&&ZhujiListFragment.getMasterId().contains("FF20")){
            rl_pl_fangqu_attribute = findViewById(R.id.rl_pl_fangqu_attribute);
            current_fangqu_tv_attribute = findViewById(R.id.current_fangqu_tv_attribute);

            rl_pl_fangqu_attribute.setOnClickListener(this);
            rl_pl_fangqu_attribute.setVisibility(View.VISIBLE);

            //获取
            new LoadZhujiAndDeviceTask().queryAllCommandInfo(operationDevice.getId(), new LoadZhujiAndDeviceTask.ILoadResult<List<CommandInfo>>() {
                @Override
                public void loadResult(List<CommandInfo> result) {
                    if(result!=null&&result.size()>0){
                        for(CommandInfo commandInfo:result){
                            if(commandInfo.getCtype().equals("138")){
                                int  command = Integer.parseInt(commandInfo.getCommand());
                                int resId = -1 ;
                                switch (command){
                                    case 1:
                                        resId = R.string.jiayu_zone_attribute_home;
                                        break ;
                                    case 2:
                                        resId = R.string.jiayu_zone_attribute_mistake;
                                        break ;
                                    case 3:
                                        resId = R.string.jiayu_zone_attribute_24;
                                        break ;
                                    case 4:
                                        resId = R.string.jiayu_zone_attribute_open;
                                        break ;
                                    case 5:
                                        resId = R.string.jiayu_zone_attribute_close;
                                        break ;
                                    case 6:
                                        resId = R.string.jiayu_zone_attribute_door;
                                        break ;
                                }
                                if(resId!=-1){
                                    current_fangqu_tv_attribute.setText(getResources().getString(resId));
                                }
                                break ;

                            }
                        }
                    }
                }
            });
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        //移除所有的消息和任务,防止引持有activity对象而导致内存泄漏
        mHandler.removeCallbacksAndMessages(null);
    }

    private void initView() {
        rl_setting_devices_audio = (LinearLayout) findViewById(R.id.rl_pl_setting_devices_audio);
        rl_setting_message_audio = (LinearLayout) findViewById(R.id.rl_pl_setting_message_audio);
        rl_no_close_gate_alarm = findViewById(R.id.rl_pl_device_alarm_dnc);
        rl_device_setting = (LinearLayout) findViewById(R.id.rl_pl_device_setting);
        ll_receivepush = (LinearLayout) findViewById(R.id.ll_receivepush);
        check_receivepush = (CheckSwitchButton) findViewById(R.id.check_receivepush);
        rl_device_bipc = (LinearLayout) findViewById(R.id.rl_pl_device_bipc);
        rl_pl_device_med = (LinearLayout) findViewById(R.id.rl_pl_device_med);
        rl_pl_device_lowbattery = (LinearLayout) findViewById(R.id.rl_pl_device_lowbattery);
        if (operationDevice.isLowb()){
            rl_pl_device_lowbattery.setVisibility(View.VISIBLE);
        }
//        if ("znyx".equals(operationDevice.getCa())){
//            rl_pl_device_med.setVisibility(View.VISIBLE);
//        }
        TextView tv = (TextView) findViewById(R.id.tv_pl_device_setting);
        if(DeviceInfo.CaMenu.nbyg.value().equals(operationDevice.getCa())){
            tv.setText(getResources().getString(R.string.devices_list_menu_nb_setting));
        }else if (DeviceInfo.ControlTypeMenu.zhuji.value().equals(operationDevice.getControlType())) {
            tv.setText(getResources().getString(R.string.devices_list_menu_dialog_zhujisetting));
        }else if (DeviceInfo.ControlTypeMenu.group.value().equals(operationDevice.getControlType())) {
            tv.setText(getResources().getString(R.string.devices_list_menu_dialog_groupsetting));
        }
//        if (DeviceInfo.CakMenu.zhuji.value().equals(operationDevice.getCak())){
//		rl_device_bipc.setVisibility(View.GONE);
//        }
        if (MainApplication.app.getAppGlobalConfig().isAudioMenu())
            initAudioMenu();

        //是否支持绑定摄像头,并且不支持摄像头绑定的包括 摄像头，主机，温湿度计，健康设备，红外转发器，群组
        if (MainApplication.app.getAppGlobalConfig().isBipcn()) {
            if (!("sst".equals(operationDevice.getCa())
                    || DeviceInfo.ControlTypeMenu.zhuji.value().equals(operationDevice.getControlType())
                    || DeviceInfo.CakMenu.detection.value().equals(operationDevice.getCak())
                    || DeviceInfo.CakMenu.health.value().equals(operationDevice.getCak())
                    || DeviceInfo.CaMenu.wenshiduji.value().equals(operationDevice.getCa())
                    || DeviceInfo.CaMenu.wenduji.value().equals(operationDevice.getCa())
                    || "hwzf".equals(operationDevice.getCa())
                    || isGroup
                     )) {
                rl_device_bipc.setVisibility(View.VISIBLE);
            }
            //专有迎宾器cak为数据采集，ca为迎宾器 需要支持
            if (DeviceInfo.CakMenu.detection.value().equals(operationDevice.getCak()) && DeviceInfo.CaMenu.ybq.value().equals(operationDevice.getCa())){
                rl_device_bipc.setVisibility(View.VISIBLE);
            }
        }

        if(Actions.VersionType.CHANNEL_ZHICHENG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            if(operationDevice.getCa()!=null&&operationDevice.getCa().equals("hwzf")){
                rl_device_bipc.setVisibility(View.VISIBLE);
            }
        }else if(Actions.VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){

        }

        //判断是否是门磁
        if(operationDevice.getCa().equals(DeviceInfo.CaMenu.menci.value())){
            //显示未关门报警
            //显示未关门报警
            JavaThreadPool.getInstance().excute(new Runnable() {
                @Override
                public void run() {
                    JSONObject object = new JSONObject();
                    object.put("did",operationDevice.getId());
                    String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS,"");
                    String result = HttpRequestUtils.requestoOkHttpPost(server+"/jdm/s3/d/dcomms",object,ChooseAudioSettingMode.this);

                    JSONArray jsonArray = JSONArray.parseArray(result);
                    if(jsonArray.size()==0){
                        return ;
                    }

                    for(int i=0;i<jsonArray.size();i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        int temp = jsonObject.getIntValue("s");
                        if(temp==9){
                            JSONObject mBossJsonObject = new JSONObject();
                            mBossJsonObject.put("did",operationDevice.getId());
                            JSONArray mListJsonArray = new JSONArray();
                            JSONObject littleObject = new JSONObject();
                            littleObject.put("vkey","alarm_dnc");
                            mListJsonArray.add(littleObject);
                            mBossJsonObject.put("vkeys",mListJsonArray);
                            //请求未关门报警时长

                            final String timeResult = HttpRequestUtils.requestoOkHttpPost(server+"/jdm/s3/d/p/list",mBossJsonObject,ChooseAudioSettingMode.this);

                            //表示设备属于可以进行未关门报警设置
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    //这样不太好，容易内存泄漏,不该了，以后注意
                                    JSONArray times = JSONArray.parseArray(timeResult);
                                    if(times.size()==0){
                                        return ;
                                    }
                                    JSONObject tempObject = times.getJSONObject(0);
                                    int value = tempObject.getIntValue("value");
                                    TextView tips_alarm_dnc = findViewById(R.id.tips_alarm_dnc);
                                    if(value==0){
                                        tips_alarm_dnc.setText(getResources().getString(R.string.no_colse_alarm_close));
                                    }else{
                                        tips_alarm_dnc.setText(value/60+getResources().getString(R.string.no_close_minute));
                                    }
                                    rl_no_close_gate_alarm.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }

                }
            });
        }
    }
    private void initValue() {
        devId = operationDevice.getId() + "";
    }

    private void setAcceptPush(final boolean isChecked) {
        mHandler.sendEmptyMessageDelayed(TIME_CODE, TIME_DELAY);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject object = new JSONObject();
                object.put("did", operationDevice.getId());
                JSONArray array = new JSONArray();
                JSONObject o = new JSONObject();
                o.put("vkey", "alertlevel_unlock");
                o.put("value", isChecked ? "2" : "0");
                array.add(o);
                object.put("vkeys", array);
                String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/u/p/set", object, ChooseAudioSettingMode.this);

                if ("0".equals(result)) {
                    if (mHandler.hasMessages(TIME_CODE)) {
                        mHandler.removeMessages(TIME_CODE);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
//                            cancelInProgress();
                            Toast.makeText(ChooseAudioSettingMode.this, getString(R.string.success),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-3".equals(result)) {
                    if (mHandler.hasMessages(TIME_CODE)) {
                        mHandler.removeMessages(TIME_CODE);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ChooseAudioSettingMode.this, getString(R.string.activity_editscene_s_erro),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-4".equals(result)) {
                    if (mHandler.hasMessages(TIME_CODE)) {
                        mHandler.removeMessages(TIME_CODE);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ChooseAudioSettingMode.this, getString(R.string.operator_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-5".equals(result)) {
                    if (mHandler.hasMessages(TIME_CODE)) {
                        mHandler.removeMessages(TIME_CODE);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ChooseAudioSettingMode.this, getString(R.string.net_error_nopermission),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    if (mHandler.hasMessages(TIME_CODE)) {
                        mHandler.removeMessages(TIME_CODE);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ChooseAudioSettingMode.this, getString(R.string.net_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 需要此功能的版本需要调用此方法
     */
    private void initAudioMenu() {
        if (operationDevice.getCak() != null
                && operationDevice.getCak().contains(DeviceInfo.CakMenu.security.value())
                && operationDevice.getAcceptMessage() > 0) {
            rl_setting_devices_audio.setVisibility(View.VISIBLE);
            rl_setting_message_audio.setVisibility(View.VISIBLE);
        } else {
            rl_setting_devices_audio.setVisibility(View.GONE);
            rl_setting_message_audio.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case NoCloseAlarmActivity.REQUEST_CODE:
                if(resultCode==RESULT_CANCELED){
                    return ;
                }
                String resultTime = data.getStringExtra("resultTime");
                TextView textView = rl_no_close_gate_alarm.findViewById(R.id.tips_alarm_dnc);
                textView.setText(resultTime);
                break;
            case REQUEST_TRIGGER:
                //为了防止数据没能及时刷新，采用这种方式
                if(resultCode==RESULT_OK){
                    current_fangqu_tv.setText(data.getStringExtra(SEND_RESULT_EXTRAS));
                }

                break ;
            case REQUEST_ZONE:
                //为了防止数据没能及时刷新，采用这种方式
                if(resultCode==RESULT_OK){
                    int value = Integer.parseInt(data.getStringExtra(SEND_RESULT_EXTRAS));
                    current_jidiantitime_tv.setText(getResources().getQuantityString(R.plurals.plurals_second,value,value));
                }
                break;
            case REQUEST_CODE_JIAYU:
                //为了防止数据没能及时刷新，采用这种方式
                if(resultCode==RESULT_OK){
                    current_fangqu_tv_attribute.setText(data.getStringExtra(SEND_RESULT_EXTRAS));
                }
                break ;
        }
    }

    private void initEvent() {
        check_receivepush.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setAcceptPush(isChecked);
            }
        });
        mIntent = new Intent();

        //初始化未关门报警点击事件
        rl_no_close_gate_alarm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView temp=  view.findViewById(R.id.tips_alarm_dnc);
                mIntent.setClass(getApplicationContext(), NoCloseAlarmActivity.class);
                mIntent.putExtra("device",operationDevice);
                mIntent.putExtra("defaultTime",temp.getText());
                startActivityForResult(mIntent,NoCloseAlarmActivity.REQUEST_CODE);
            }
        });


        rl_pl_device_med.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent.setClass(getApplicationContext(), MedicineSetTimeActivity.class);
                mIntent.putExtra("device", operationDevice);
                startActivity(mIntent);
            }
        });
        rl_setting_devices_audio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent.putExtra("mode", "device");
                //Log.e("aaa", "choose chu qu"+devId);
                mIntent.putExtra("devId", devId);
                mIntent.setClass(getApplicationContext(), AudioSettingActivity.class);
                startActivity(mIntent);
            }
        });

        rl_setting_message_audio.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mIntent.putExtra("mode", "message");
//				Log.e("aaa", "choose chu qu" + devId);
                mIntent.putExtra("devId", devId);
                mIntent.setClass(getApplicationContext(), AudioSettingActivity.class);
                startActivity(mIntent);
            }
        });

        rl_device_setting.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), DeviceSetActivity.class);
                intent.putExtra("device", operationDevice);
                startActivity(intent);

            }
        });
        rl_device_bipc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), CameraListActivity.class);
                intent.putExtra("device", operationDevice);
                startActivity(intent);
            }
        });
        rl_pl_device_lowbattery.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertView(getString(R.string.tips),
                        getString(R.string.activity_device_set_lowbattery_msg),
                        getString(R.string.cancel),
                        new String[]{getString(R.string.activity_device_set_lowbattery_btn)}, null,
                        mContext, AlertView.Style.Alert,
                        new com.smartism.znzk.view.alertview.OnItemClickListener() {

                            @Override
                            public void onItemClick(Object o, int position) {
                                if (position != -1) {
                                    showInProgress(getString(R.string.ongoing));
                                    //设置为正常电量
                                    JavaThreadPool.getInstance().excute(new Runnable() {
                                        @Override
                                        public void run() {
                                            String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                                            JSONObject pJsonObject = new JSONObject();
                                            pJsonObject.put("id", operationDevice.getId());
                                            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/clb", pJsonObject, mContext);
                                            if ("0".equals(result)) {
                                                DeviceInfo deviceInfo = DatabaseOperator.getInstance().queryDeviceInfo(operationDevice.getId());
                                                deviceInfo.setLowb(false);
                                                DatabaseOperator.getInstance().insertOrUpdateDeviceInfo(deviceInfo);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mContext.cancelInProgress();
                                                        Toast.makeText(mContext, R.string.success,Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            } else if ("-3".equals(result)) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mContext.cancelInProgress();
                                                        Toast.makeText(mContext, R.string.update_failed,Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            } else {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mContext.cancelInProgress();
                                                        Toast.makeText(mContext, R.string.update_failed,Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        }).show();
            }
        });
    }

    public void back(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.putExtra("device_id",operationDevice.getId());
        switch (v.getId()){
            case R.id.rl_pl_fangqu_setting:
                intent.setClass(getApplicationContext(), TriggerSettingActivity.class);
                intent.putExtra("flags",0);//触发电平设置
                startActivityForResult(intent,REQUEST_TRIGGER);
                break ;
            case R.id.rl_pl_jidianqitime_setting:
                intent.setClass(getApplicationContext(), JdqWorkTimeSettingActivity.class);
                startActivityForResult(intent,REQUEST_ZONE);
                break ;
            case R.id.rl_pl_edge_setting:
                intent.setClass(getApplicationContext(), TriggerSettingActivity.class);
                intent.putExtra("flags",1);//触发模式设置
                startActivityForResult(intent,REQUEST_TRIGGER);
                break ;
            case R.id.rl_pl_fangqu_attribute:
                //佳玉防区属性
                intent.setClass(getApplicationContext(), ZoneAttributeSettingActivity.class);
                startActivityForResult(intent,REQUEST_CODE_JIAYU);
                break ;
        }
    }

    @Override
    public void loadCommands(List<CommandInfo> lists) {
        if(Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            String currentJdqStr = "";
            String currentFangquStr="";
            String currentEdgeStr="";
            if(lists!=null&&lists.size()>0){
                for(CommandInfo commandInfo :lists){
                    if(commandInfo.getCtype().equals("155")){
                        String command = commandInfo.getCommand();
                        if(command.equals("00")){
                            currentFangquStr = getResources().getString(R.string.triggers_low_level);
                        }else if(command.equals("01")){
                            currentFangquStr = getResources().getString(R.string.triggers_high_lever);
                        }
                    }else if(commandInfo.getCtype().equals("154")){
                        //工作时长
                        currentJdqStr = commandInfo.getCommand() ;
                    }else if(commandInfo.getCtype().equals("161")){
                        String command = commandInfo.getCommand();
                        if(command.equals("0")){
                            currentEdgeStr = getString(R.string.triggers_edge_edge);
                        }else if(command.equals("1")){
                            currentEdgeStr = getString(R.string.triggers_edge_level);
                        }
                    }
                }

            }
            //设置继电器设置和防区设置的可见性
            if(operationDevice!=null&&operationDevice.getCa().equals(DeviceInfo.CaMenu.jdq.value())){
                if(TextUtils.isEmpty(currentJdqStr)){
                    //默认值
                    currentJdqStr = "3";
                }
                current_jidiantitime_tv.setText(getResources().getQuantityString(R.plurals.plurals_second,Integer.parseInt(currentJdqStr),Integer.parseInt(currentJdqStr)));
            }else if(operationDevice!=null&&operationDevice.getCa().equals("fq")){
                if(TextUtils.isEmpty(currentFangquStr)){
                    if(operationDevice.getSlaveId().contains("3")){
                        //防区3的默认值
                        currentFangquStr = getResources().getString(R.string.triggers_low_level);
                    }else if(operationDevice.getSlaveId().contains("1")||operationDevice.getSlaveId().contains("2")){
                        //防区1，2,默认值
                        currentFangquStr = getResources().getString(R.string.triggers_high_lever);
                    }

                }
                current_fangqu_tv.setText(currentFangquStr);
                current_edge_tv.setText(TextUtils.isEmpty(currentEdgeStr)?getString(R.string.triggers_edge_level):currentEdgeStr);
            }
        }

    }

    @Override
    public void showProgress(String text) {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void error(String message) {

    }

    @Override
    public void success(String message) {

    }
}
