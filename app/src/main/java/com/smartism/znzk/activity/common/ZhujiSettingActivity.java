package com.smartism.znzk.activity.common;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.BeiJingMaoYanActivity;
import com.smartism.znzk.activity.device.DeviceUpdatetGSMPhoneActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.activity.user.GenstureInitActivity;
import com.smartism.znzk.activity.user.GenstureSettingActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.SwitchButton.SwitchButton;

import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;
import com.smartism.znzk.zhicheng.tasks.LoadCommandsInfo;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

import org.apache.commons.lang.StringUtils;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.smartism.znzk.activity.alert.ChooseAudioSettingMode.SEND_RESULT_EXTRAS;
import static com.smartism.znzk.activity.common.ArmyDelayTimeSettingActivity.getPositionByCommand;


public class ZhujiSettingActivity extends ActivityParentActivity implements OnClickListener,LoadCommandsInfo.ILoadCommands,HttpAsyncTask.IHttpResultView {
    private final int mHandleWhat_15 = 15;
    public final int CM_REQUEST_CODE = 0X47,JIA_YU_REQUECT_CODE=0x43,JIA_YU_ALARM_REQUEST = 0X45;
    private TextView tv_gensture_status, tip_notice, tips_wsd_show, tv_alarm_time;
    private LinearLayout rl_setting_gensture, ll_set_wsd_time, ll_alarm_time,ll_zhuji_time_sync_parent,ll_zhuji_timezone_parent,ll_zhuji_lang_parent,ll_zhuji_wifi_parent,ll_zhuji_init_parent,ll_zhuji_army_parent;
    private long zhuji_id;
    private ZhujiInfo zhuji;
    private SwitchButton btn_toogle_online_status, btn_toogle_offline_status;
    private final int DHANDLER_TIMEOUT = 5;
    private final int DHANDLER_TIMEOUT_TIME = 10 * 1000;
    private boolean isshow;
    private ViewGroup zhuji_shangxiaxian_tip ;
    //本地指令数据
    private List<CommandInfo> commandInfos;
    //输入管理
    private InputMethodManager imm;
    //wifi设置的弹出框和输入框
    private AlertView mAlertViewExt;
    private EditText mAlertEdit_ssid,mAlertEdit_pwd;

    private Map<String,String> zhujiSetInfos = new HashMap<>();//主机设置信息,其实没必要，zhuji对象中已带


    //宏才版本
    LinearLayout hongcai_banben_parent,hongcai_alarm_setting,hongcai_alarm_clock_setting,hongcai_time_sync,ll_offline;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_zhuji);
        zhuji_id = getIntent().getLongExtra("zhuji_id", 0);
        isshow = getIntent().getBooleanExtra("isshow", false);
        zhuji = DatabaseOperator.getInstance(ZhujiSettingActivity.this).queryDeviceZhuJiInfo(zhuji_id);
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        tip_notice = (TextView) findViewById(R.id.tips_notice);
        tips_wsd_show = (TextView) findViewById(R.id.tips_wsd_show);
        rl_setting_gensture = (LinearLayout) findViewById(R.id.rl_setting_gensture);
        ll_set_wsd_time = (LinearLayout) findViewById(R.id.ll_set_wsd_time);
        ll_set_wsd_time.setOnClickListener(this);
        ll_zhuji_timezone_parent = (LinearLayout) findViewById(R.id.ll_zhuji_timezone_parent);
        ll_zhuji_timezone_parent.setOnClickListener(this);
        ll_zhuji_wifi_parent = (LinearLayout) findViewById(R.id.ll_zhuji_wifi_parent);
        ll_zhuji_wifi_parent.setOnClickListener(this);
        ll_zhuji_init_parent = (LinearLayout) findViewById(R.id.ll_zhuji_init_parent);
        ll_zhuji_init_parent.setOnClickListener(this);
        ll_zhuji_lang_parent = (LinearLayout) findViewById(R.id.ll_zhuji_lang_parent);
        ll_zhuji_lang_parent.setOnClickListener(this);
        tv_gensture_status = (TextView) findViewById(R.id.tv_gensture_status);
        ll_alarm_time = (LinearLayout) findViewById(R.id.ll_alarm_time);
        ll_alarm_time.setOnClickListener(this);
        zhuji_shangxiaxian_tip = findViewById(R.id.zhuji_shangxiaxian_tip);
        if (!MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_AIERFUDE)
                && !Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
            if (zhuji != null && zhuji.isAdmin()) {
                //智利的版本显示,报警时长设置
                if(Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                    ll_alarm_time.setVisibility(View.VISIBLE);
                }else {
                    if (isshow) {
                        ll_alarm_time.setVisibility(View.VISIBLE);
                    }
                }
                if (!MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_ZHILIDE)&&
                    !MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_HUALI)&&
                    !MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_YIXUNGE)) {
                    ll_set_wsd_time.setVisibility(View.VISIBLE);
                }
            }
        }
        tv_alarm_time = (TextView) findViewById(R.id.tv_alarm_time);

        // 跳转到手势密码打开关闭界面
        rl_setting_gensture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String patternString = dcsp.getString(Constant.CODE_GENSTURE, "");
                if (TextUtils.isEmpty(patternString)) {
                    startActivity(new Intent(getApplicationContext(), GenstureInitActivity.class));
                } else {
                    startActivity(new Intent(getApplicationContext(), GenstureSettingActivity.class));
                }
            }
        });

        initEvent();
        initData();

        //宏才版本,主机必须在线且是管理员才能够设置
        if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            ll_alarm_time.setVisibility(View.GONE);
            if(zhuji.isOnline()&&zhuji.isAdmin()){
                hongcai_banben_parent = findViewById(R.id.hongcai_banben_parent);
                hongcai_alarm_setting = findViewById(R.id.hongcai_alarm_setting);
                hongcai_alarm_clock_setting = findViewById(R.id.hongcai_alarm_clock_setting);
                hongcai_time_sync = findViewById(R.id.hongcai_time_sync);
                hongcai_banben_parent.setVisibility(View.VISIBLE);

                hongcai_alarm_clock_setting.setOnClickListener(this);
                hongcai_alarm_setting.setOnClickListener(this);
                hongcai_time_sync.setOnClickListener(this);
            }

        }else if(Actions.VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            ll_alarm_time.setVisibility(View.GONE);
        }else if(Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            zhuji_shangxiaxian_tip.setVisibility(View.VISIBLE);
            ll_offline.setVisibility(View.GONE);
        }

        //主机离线提醒设置
        zhujiOfflineinit();

        //致利德
        initZhiliDeSetting();

        //佳域
        jiayuSetting();

        //主机密码设置
        zhujiPwdSetting();

        //主机时间同步设置
        timeSyncSetting();
    }

    private void timeSyncSetting(){
        if(!(zhuji!=null&&zhuji.isAdmin())){
            return ;
        }
        if(ZhujiListFragment.getMasterId().contains("FF20")){
            ll_zhuji_time_sync_parent = findViewById(R.id.ll_zhuji_time_sync_parent);
            ll_zhuji_time_sync_parent.setOnClickListener(this);
            ll_zhuji_time_sync_parent.setVisibility(View.VISIBLE);
        }
    }

    //智力得拨号器设置
    private TextView call_mode_current_tv,level_current_tv;
    private LinearLayout ll_call_mode_parent,ll_level_parent;

    private void initZhiliDeSetting(){
        if(Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())&&zhuji.getCa().equals(DeviceInfo.CaMenu.bohaoqi.value())){
            ll_call_mode_parent = findViewById(R.id.ll_call_mode_parent);
            ll_level_parent = findViewById(R.id.ll_level_parent);
            call_mode_current_tv = findViewById(R.id.call_mode_current_tv);
            level_current_tv = findViewById(R.id.level_current_tv);
            ll_call_mode_parent.setVisibility(View.VISIBLE);
            ll_level_parent.setVisibility(View.VISIBLE);
            ll_call_mode_parent.setOnClickListener(this);
            ll_level_parent.setOnClickListener(this);
        }

    }

    //佳域
    private LinearLayout ll_jiayu_aralm_parent;
    private TextView jiayu_army_current_tv,jiayu_aralm_current_tv;
    private String[] mJiaYuData ;

    private void jiayuSetting(){
        ll_zhuji_army_parent = findViewById(R.id.ll_jiayu_army_parent);
        ll_zhuji_army_parent.setOnClickListener(this);
        jiayu_army_current_tv = findViewById(R.id.jiayu_army_current_tv);
        mJiaYuData = getResources().getStringArray(R.array.jiayu_adsa_array);
        ll_jiayu_aralm_parent = findViewById(R.id.ll_jiayu_aralm_parent);
        jiayu_aralm_current_tv = findViewById(R.id.jiayu_aralm_current_tv);
        ll_jiayu_aralm_parent.setOnClickListener(this);
        new LoadZhujiAndDeviceTask().queryAllCommandInfo(zhuji_id, new LoadZhujiAndDeviceTask.ILoadResult<List<CommandInfo>>() {
            @Override
            public void loadResult(List<CommandInfo> result) {
                int armypostiion = 0 ;
                int aralmposition = 0 ;
                if(result!=null&&result.size()>0){
                    for(CommandInfo commandInfo:result){
                        if(commandInfo.getCtype().equals("142")){
                            try {
                                int command = Integer.parseInt(commandInfo.getCommand());
                                armypostiion = getPositionByCommand(command);
                            }catch (NumberFormatException e){
                                armypostiion = 0;
                            }

                        }else if(commandInfo.getCtype().equals("141")){
                            try {
                                int command = Integer.parseInt(commandInfo.getCommand());
                                aralmposition = getPositionByCommand(command);
                            }catch (NumberFormatException e){
                                aralmposition = 0;
                            }
                        }
                    }
                }
                jiayu_army_current_tv.setText(mJiaYuData[armypostiion]);
                jiayu_aralm_current_tv.setText(mJiaYuData[aralmposition]);
            }
        });
    }
    private LinearLayout ll_zhuji_pwd_parent ;
    private void zhujiPwdSetting(){
        if(!(zhuji!=null&&zhuji.isAdmin())){
            return ;
        }
        ll_zhuji_pwd_parent = findViewById(R.id.ll_zhuji_pwd_parent);
        ll_zhuji_pwd_parent.setOnClickListener(this);
//        if(ZhujiListFragment.getMasterId().contains("FF20")){
//            ll_zhuji_pwd_parent.setVisibility(View.VISIBLE);
//        }
    }

    BottomSheetDialog mBottomDialog;
    TextView content_tv_offline ;
    ListView listView ;
    List<String> valuesOff = new ArrayList<>();
    int currentSelection ;
    void zhujiOfflineinit(){
        valuesOff.add(getString(R.string.hub_offline_tips_close));
        valuesOff.add(getString(R.string.hub_offline_tips_delay_three_minutes));
        valuesOff.add(getString(R.string.hub_offline_tips_delay_ten_minutes));
        valuesOff.add(getString(R.string.hub_offline_tips_delay_thirty_minutes));

        //从数据库加载命令
        new LoadCommandsInfo(this).execute(zhuji.getId());
        ll_offline = findViewById(R.id.ll_offline);
        View view = getLayoutInflater().inflate(R.layout.zhuji_setting_offline,null);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getResources().getDisplayMetrics().heightPixels/3);
        view.setLayoutParams(lp);
        listView = view.findViewById(R.id.bottom_lv);
        content_tv_offline = findViewById(R.id.tv_offline__content);
        listView.setAdapter(new ArrayAdapter(this,android.R.layout.simple_list_item_single_choice,valuesOff));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //发送指令
                mBottomDialog.dismiss();
                if(valuesOff.get(position).equals(content_tv_offline.getText().toString())){
                    return ;
                }
                int time = -1 ;
                switch (position){
                    case 0:
                        time = -1 ;
                        break ;
                    case 1:
                        time = 180;
                        break;
                    case 2:
                        time = 600;
                        break ;
                    case 3:
                        time = 1800;
                        break;
                }
                currentSelection = position ;
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", zhuji.getId());
                JSONArray array = new JSONArray();
                JSONObject object = new JSONObject();
                object.put("vkey", "alert_offline");
                object.put("value",String.valueOf(time));
                array.add(object);
                pJsonObject.put("vkeys", array);
                new HttpAsyncTask(ZhujiSettingActivity.this,HttpAsyncTask.Zhuji_SET_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pJsonObject);
            }
        });
        mBottomDialog = new BottomSheetDialog(this);
        mBottomDialog.setContentView(view);
        ll_offline.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(valuesOff.contains(content_tv_offline.getText().toString())){
                    int index = valuesOff.indexOf(content_tv_offline.getText().toString());
                    listView.performItemClick(listView,index,index);//设置默认选中项，在现实底部框时显示
                }
                mBottomDialog.show();
            }
        });
        ViewGroup viewGroup = (ViewGroup) view.getParent();
        viewGroup.setBackgroundColor(Color.parseColor("#00000000"));
    }


    private void initEvent() {

        btn_toogle_online_status = (SwitchButton) findViewById(R.id.btn_toogle_online_status);
        btn_toogle_offline_status = (SwitchButton) findViewById(R.id.btn_toogle_offline_status);

        btn_toogle_online_status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                setOnOffline("alertlevel_online", isChecked);
            }
        });

        btn_toogle_offline_status.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setOnOffline("alertlevel_offline", isChecked);
            }
        });
    }

    private void initData() {
        showInProgress(getString(R.string.loading), false, true);
        commandInfos = DatabaseOperator.getInstance(this).queryAllCommands(zhuji.getId());
        mHandler.sendEmptyMessageDelayed(DHANDLER_TIMEOUT, DHANDLER_TIMEOUT_TIME);
        JavaThreadPool.getInstance().excute(new GetZhujiAlarmTime());
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", zhuji.getId());
                JSONArray array = new JSONArray();

                JSONObject oo = new JSONObject();
                oo.put("vkey", "alertlevel_offline");
                JSONObject ooo = new JSONObject();
                ooo.put("vkey", "alertlevel_online");
                array.add(oo);
                array.add(ooo);
                pJsonObject.put("vkeys", array);
                final String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/u/p/list", pJsonObject, ZhujiSettingActivity.this);
                if ("-3".equals(result)) {
                    mHandler.removeMessages(DHANDLER_TIMEOUT);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                        }
                    });
                } else if ("-5".equals(result)) {
                    mHandler.removeMessages(DHANDLER_TIMEOUT);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                        }
                    });
                } else if (!StringUtils.isEmpty(result)) {
                    Log.e(TAG, "level_online：" + result.toString());
                    cancelInProgress();
                    mHandler.removeMessages(DHANDLER_TIMEOUT);
                    final JSONArray array1 = JSONArray.parseArray(result);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (array1 != null) {
                                for (int i = 0; i < array1.size(); i++) {
                                    JSONObject jsonObject = array1.getJSONObject(i);

                                    if (jsonObject.getString("key").equals("alertlevel_online")) {
                                        if (jsonObject.containsKey("value") && jsonObject.getString("value").equals("0")) {
                                            btn_toogle_online_status.setCheckedImmediatelyNoEvent(false);
                                        } else {
                                            btn_toogle_online_status.setCheckedImmediatelyNoEvent(true);
                                        }
                                    } else if (jsonObject.getString("key").equals("alertlevel_offline")) {
                                        if (jsonObject.containsKey("value") && jsonObject.getString("value").equals("0")) {
                                            btn_toogle_offline_status.setCheckedImmediatelyNoEvent(false);
                                        } else {
                                            btn_toogle_offline_status.setCheckedImmediatelyNoEvent(true);
                                        }
                                    }

                                }
                            }
                        }
                    });

                } else {
                    mHandler.removeMessages(DHANDLER_TIMEOUT);
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
//                            wsdTag = 60;
                        }
                    });
                }
            }
        });
    }


    private void setOnOffline(final String key, final boolean isChecked) {
        showInProgress(getString(R.string.loading), false, true);
        mHandler.sendEmptyMessageDelayed(DHANDLER_TIMEOUT, 10 * 1000);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", zhuji.getId());
                JSONArray array = new JSONArray();
                JSONObject object = new JSONObject();
                object.put("vkey", key);
                object.put("value", isChecked == true ? "2" : "0");
                array.add(object);
                pJsonObject.put("vkeys", array);

                String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/u/p/set", pJsonObject, ZhujiSettingActivity.this);
                if ("-3".equals(result)) {
                    if (mHandler.hasMessages(DHANDLER_TIMEOUT)) {
                        mHandler.removeMessages(DHANDLER_TIMEOUT);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ZhujiSettingActivity.this, getString(R.string.net_error_nodata),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-5".equals(result)) {
                    if (mHandler.hasMessages(DHANDLER_TIMEOUT)) {
                        mHandler.removeMessages(DHANDLER_TIMEOUT);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ZhujiSettingActivity.this, getString(R.string.device_not_getdata),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("0".equals(result)) {
                    if (mHandler.hasMessages(DHANDLER_TIMEOUT)) {
                        mHandler.removeMessages(DHANDLER_TIMEOUT);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ZhujiSettingActivity.this, getString(R.string.success),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
        });
    }

    //专注于更新页面
    @Override
    public void loadCommands(List<CommandInfo> lists) {
        if(lists==null||lists.size()==0){
            //设置默认值,不仅仅
            content_tv_offline.setText(getString(R.string.hub_offline_tips_close));
        }else{
            for(CommandInfo info:lists){
                if(info.getCtype().equals("alert_offline")){
                    int value = Integer.parseInt(info.getCommand());
                    if(value<0){
                        content_tv_offline.setText(getString(R.string.hub_offline_tips_close));
                    }else{
                        content_tv_offline.setText(getString(R.string.hub_offline_tips_delay)+(value/60)+getString(R.string.hub_offline_tips_delay_minutes));
                    }
                }else if(Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                    String command = info.getCommand();
                    if(info.getCtype().equals("156")){
                        if(!TextUtils.isEmpty(command)){
                            String callMethod = command.substring(0,command.length()/2);
                            if("00".equals(callMethod)){
                                call_mode_current_tv.setText(getResources().getString(R.string.zscm_call_only_number));
                            }else if("01".equals(callMethod)){
                                call_mode_current_tv.setText(getResources().getString(R.string.zscm_call_all_number));
                            }
                        }
                        if(TextUtils.isEmpty(call_mode_current_tv.getText())){
                            //默认值
                            call_mode_current_tv.setText(getResources().getString(R.string.zscm_call_all_number));
                        }
                    }else if(info.getCtype().equals("162")){
                        if("1".equals(command)){
                            level_current_tv.setText(getResources().getString(R.string.triggers_high_level_defen));
                        }else if("0".equals(command)){
                            level_current_tv.setText(getResources().getString(R.string.triggers_low_level_defen));
                        }
                        if(TextUtils.isEmpty(level_current_tv.getText())){
                            //默认值
                            level_current_tv.setText(getResources().getString(R.string.triggers_high_level_defen));
                        }
                    }

                }
            }
        }
    }

    @Override
    public void showProgress(String text) {
        showInProgress(text);
    }

    @Override
    public void hideProgress() {
        cancelInProgress();
    }

    @Override
    public void error(String message) {
        Log.d(getClass().getSimpleName(),"error");
    }

    @Override
    public void success(String message) {
        Log.d(getClass().getSimpleName(),"success");
    }

    //http请求结果回调,flag用于标识请求，好做相应的处理
    @Override
    public void setResult(int flag,String result) {
        if(flag == HttpAsyncTask.Zhuji_SET_URL_FLAG){
            if ("-3".equals(result)) {
                Toast.makeText(ZhujiSettingActivity.this, getString(R.string.net_error_nodata),
                                Toast.LENGTH_LONG).show();
            } else if ("-5".equals(result)) {
                Toast.makeText(ZhujiSettingActivity.this, getString(R.string.device_not_getdata),
                                Toast.LENGTH_LONG).show();
            } else if ("0".equals(result)) {
                Toast.makeText(ZhujiSettingActivity.this, getString(R.string.success),
                                Toast.LENGTH_LONG).show();
                content_tv_offline.setText(valuesOff.get(currentSelection));
            }
        }
    }


    private class GetZhujiAlarmTime implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("did", zhuji.getId());
            JSONArray array = new JSONArray();
            JSONObject object = new JSONObject();
            object.put("vkey", CommandInfo.CommandTypeEnum.setZhujiAlarmTime.value());
            JSONObject o = new JSONObject();
            o.put("vkey", "interval_wsdj");
            array.add(object);
            array.add(o);
//                array.add(object1);
            pJsonObject.put("vkeys", array);

            final String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/p/list", pJsonObject, ZhujiSettingActivity.this);
            if ("-3".equals(result)) {
                mHandler.removeMessages(DHANDLER_TIMEOUT);
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
//                            Toast.makeText(WSDTimeSetActivity.this, getString(R.string.net_error_nodata),
//                                    Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-5".equals(result)) {
                mHandler.removeMessages(DHANDLER_TIMEOUT);
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
//                            Toast.makeText(WSDTimeSetActivity.this, getString(R.string.device_not_getdata),
//                                    Toast.LENGTH_LONG).show();
                    }
                });
            } else if (!StringUtils.isEmpty(result)) {
                mHandler.removeMessages(DHANDLER_TIMEOUT);
                final JSONArray array1 = JSONArray.parseArray(result);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        if (array1 != null) {
                            for (int i = 0; i < array1.size(); i++) {
                                JSONObject jsonObject = array1.getJSONObject(i);

                                if (jsonObject.getString("key").equals("interval_wsdj")) {
                                    if (jsonObject.containsKey("value")) {
                                        String results = jsonObject.getString("value");
                                        try {
                                            int a = Integer.parseInt(results);
                                            wsdTag = a;
                                            tips_wsd_show.setText(wsdTag + getString(R.string.wsd_accept_time_min));
                                            Log.d("wsdj_time", a + "");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        wsdTag = 60;
                                        tips_wsd_show.setText(wsdTag + getString(R.string.wsd_accept_time_min));
                                    }
                                } else if (jsonObject.getString("key").equals(CommandInfo.CommandTypeEnum.setZhujiAlarmTime.value())) {
                                    if (jsonObject.containsKey("value")) {
                                        String results = jsonObject.getString("value");
                                        try {
                                            int a = Integer.parseInt(results);
                                            zhujiAlarmTag = a;
                                            if (zhujiAlarmTag == 5) {
                                                tv_alarm_time.setText(getString(R.string.wsd_accept_time_5_default));
                                            } else {
                                                tv_alarm_time.setText(zhujiAlarmTag + getString(R.string.wsd_accept_time_min));
                                            }
                                            Log.d("wsdj_time", a + "");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        zhujiAlarmTag = 5;
                                        tv_alarm_time.setText(getString(R.string.wsd_accept_time_5_default));
                                    }
                                }

                            }
                        }
                    }
                });

            } else {
                mHandler.removeMessages(DHANDLER_TIMEOUT);
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        zhujiAlarmTag = 5;
                        wsdTag = 60;
                    }
                });
            }
        }
    }


    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case DHANDLER_TIMEOUT:
                    cancelInProgress();
                    Toast.makeText(getApplicationContext(), getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    break;
                case mHandleWhat_15://数据库加载完成
                    if (zhuji.isAdmin()) {
                        if ("1".equalsIgnoreCase(zhujiSetInfos.get(ZhujiInfo.GNSetNameMenu.supportWifiSet.value()))) {
                            ll_zhuji_wifi_parent.setVisibility(View.VISIBLE);
                        }
                        if ("1".equalsIgnoreCase(zhujiSetInfos.get(ZhujiInfo.GNSetNameMenu.supportLangSet.value()))) {
                            ll_zhuji_lang_parent.setVisibility(View.VISIBLE);
                        }
                        if ("1".equalsIgnoreCase(zhujiSetInfos.get(ZhujiInfo.GNSetNameMenu.supportTimeZoneSet.value()))) {
                            ll_zhuji_timezone_parent.setVisibility(View.VISIBLE);
                        }
                    }
                    if ("1".equalsIgnoreCase(zhujiSetInfos.get(ZhujiInfo.GNSetNameMenu.supportRefreshInit.value()))) {
                        ll_zhuji_init_parent.setVisibility(View.VISIBLE);
                    }
                    if ("1".equalsIgnoreCase(zhujiSetInfos.get(ZhujiInfo.GNSetNameMenu.delayArming.value()))) {
                        ll_zhuji_army_parent.setVisibility(View.VISIBLE);
                    }
                    if ("1".equalsIgnoreCase(zhujiSetInfos.get(ZhujiInfo.GNSetNameMenu.delayAlarming.value()))) {
                        ll_jiayu_aralm_parent.setVisibility(View.VISIBLE);
                    }
                    if ("1".equalsIgnoreCase(zhujiSetInfos.get(ZhujiInfo.GNSetNameMenu.supportDevicePwd.value()))) {
                        ll_zhuji_pwd_parent.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.ll_set_wsd_time:
                intent.setClass(this, WSDTimeSetActivity.class);
                intent.putExtra("zhuji", zhuji);
                intent.putExtra("time", wsdTag);
                startActivityForResult(intent, 100);
                break;
            case R.id.ll_alarm_time:
                intent.setClass(this, ZhujiAlarmTimeSetActivity.class);
                intent.putExtra("zhuji", zhuji);
                intent.putExtra("time", zhujiAlarmTag);
                startActivityForResult(intent, 101);
                break;
            case R.id.hongcai_alarm_setting:
                //主机报警设定
                intent.setClass(this,HongCaiSettingActivity.class);
                intent.putExtra("whatsetting",1);
                intent.putExtra("zhuji_id",zhuji.getId());
                startActivity(intent);
                break;
            case R.id.hongcai_alarm_clock_setting:
                //主机闹钟设定
                intent.setClass(this,HongCaiSettingActivity.class);
                intent.putExtra("whatsetting",2);
                intent.putExtra("zhuji_id",zhuji.getId());
                startActivity(intent);
                break;
            case R.id.ll_zhuji_time_sync_parent:
            case R.id.hongcai_time_sync:
                //时间同步
                View view = getLayoutInflater().inflate(R.layout.unlock_notice_layout,hongcai_time_sync,false);
                TextView title_tv = view.findViewById(R.id.pwd_title_tv);
                TextView content_tv =view.findViewById(R.id.pwd_content_tv);
                TextView concel_tv = view.findViewById(R.id.pwd_cancel_btn);
                TextView confirm_tv = view.findViewById(R.id.pwd_confirm_btn);
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) content_tv.getLayoutParams();
                lp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                content_tv.setLayoutParams(lp);
                final AlertDialog dialog = new AlertDialog.Builder(this).setCancelable(true)
                        .setView(view).create();
                title_tv.setText(getString(R.string.hongcai_sync_time_title));
                title_tv.setVisibility(View.VISIBLE);
                concel_tv.setText(getResources().getString(R.string.permission_cancel));
                confirm_tv.setText(getResources().getString(R.string.pickerview_submit));
                confirm_tv.setTextColor(Color.BLACK);
                concel_tv.setTextColor(Color.BLACK);
                content_tv.setText(getString(R.string.hongcai_sync_time_tishi));
                View.OnClickListener listener = new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if(v.getId()==R.id.pwd_confirm_btn){
                            showInProgress(getString(R.string.please_wait));
                            JavaThreadPool.getInstance().excute(new Runnable() {
                                @Override
                                public void run() {
                                    String server = dcsp.getString(Constant.HTTP_DATA_SERVERS,"");
                                    SimpleDateFormat format  = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                                    String time = format.format(new Date());
                                    String[] temps = time.split("-");
                                    String year = Integer.toHexString(Integer.parseInt(temps[0]));
                                    String month = Integer.toHexString(Integer.parseInt(temps[1]));
                                    String day = Integer.toHexString(Integer.parseInt(temps[2]));
                                    String hour  = Integer.toHexString(Integer.parseInt(temps[3]));
                                    String minute = Integer.toHexString(Integer.parseInt(temps[4]));
                                    String second = Integer.toHexString(Integer.parseInt(temps[5]));
                                    String result = "";
                                    while(year.length()<4){
                                        year ="0"+year;
                                    }
                                    month = "0"+month;
                                    if(day.length()<2){
                                        day = "0"+day;
                                    }
                                    if(hour.length()<2){
                                        hour="0"+hour;
                                    }
                                    if(minute.length()<2){
                                        minute="0"+minute;
                                    }
                                    if(second.length()<2){
                                        second="0"+second;
                                    }
                                    result = year+month+day+hour+minute+second ;
                                    server = server+"/jdm/s3/sphctz/update";
                                    JSONObject object = new JSONObject();
                                    object.put("did",zhuji_id);
                                    object.put("setInfo",result);
                                    object.put("setKey","112");
                                    String temp = HttpRequestUtils.requestoOkHttpPost(server,object,mContext);
                                    if(temp.equals("0")){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                cancelInProgress();
                                                Toast toast = Toast.makeText(mContext,"",Toast.LENGTH_LONG);
                                                toast.setText(getString(R.string.deviceinfo_activity_success));
                                                toast.show();
                                            }
                                        });

                                    }else{
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                cancelInProgress();
                                                Toast toast = Toast.makeText(mContext,"",Toast.LENGTH_LONG);
                                                toast.setText(getString(R.string.activity_editscene_set_falid));
                                                toast.show();
                                            }
                                        });
                                    }

                                }
                            });
                        }
                    }
                };
                concel_tv.setOnClickListener(listener);
                confirm_tv.setOnClickListener(listener);
                dialog.show();
                break ;
            case R.id.ll_call_mode_parent:
                //拨号器模式选择
                intent.setClass(getApplicationContext(),ZhujiSettingCallModeActivity.class);
                intent.putExtra("zhuji_id",zhuji_id);
                startActivityForResult(intent,CM_REQUEST_CODE);
                break  ;

            case R.id.ll_level_parent:
                intent.setClass(getApplicationContext(),TriggerSettingActivity.class);
                intent.putExtra("device_id",zhuji_id);
                intent.putExtra("flags",2);
                startActivityForResult(intent,CM_REQUEST_CODE);
                break ;
            case R.id.ll_jiayu_army_parent:
                intent.setClass(getApplicationContext(),ArmyDelayTimeSettingActivity.class);
                intent.putExtra("device_id",zhuji_id);
                intent.putExtra("flags",0);
                startActivityForResult(intent,JIA_YU_REQUECT_CODE);
                break ;
            case R.id.ll_jiayu_aralm_parent:
                intent.setClass(getApplicationContext(),ArmyDelayTimeSettingActivity.class);
                intent.putExtra("device_id",zhuji_id);
                intent.putExtra("flags",1);
                startActivityForResult(intent,JIA_YU_ALARM_REQUEST);
                break ;
            case R.id.ll_zhuji_pwd_parent:
                intent.setClass(getApplicationContext(),ZhujiPasswordModifyActivity.class);
                intent.putExtra("device_id",zhuji_id);
                startActivity(intent);
                break ;
            case R.id.ll_zhuji_lang_parent:
                break;
            case R.id.ll_zhuji_timezone_parent:
                break;
            case R.id.ll_zhuji_wifi_parent:
                //拓展窗口
                mAlertViewExt = new AlertView(getString(R.string.activity_beijingmy_wifititle), null, getString(R.string.cancel), null, new String[]{getString(R.string.submit)}, this, AlertView.Style.Alert, new OnItemClickListener(){

                    @Override
                    public void onItemClick(Object o, int position) {
                        if (position != -1){
                            if ("".equals(mAlertEdit_ssid.getText().toString()) || "".equals(mAlertEdit_pwd.getText().toString())){
                                Toast.makeText(mContext,getString(R.string.activity_beijingmy_wifiinfoempty),Toast.LENGTH_SHORT).show();
                                return;
                            }
                            showInProgress(getString(R.string.operationing),false,true);
                            String[] keys = new String[2];
                            String[] values = new String[2];
                            keys[0] = String.valueOf(CommandInfo.CommandTypeEnum.setWifiSSID.value());
                            values[0] = mAlertEdit_ssid.getText().toString();
                            keys[1] = String.valueOf(CommandInfo.CommandTypeEnum.setWifiPassword.value());
                            values[1] = mAlertEdit_pwd.getText().toString();
                            JavaThreadPool.getInstance().excute(new PropertiesSet(0,keys,values));
                        }
                    }
                });
                ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.layout_alertview_alertext_form,null);
                mAlertEdit_ssid = (EditText) extView.findViewById(R.id.wifi_ssid);
                mAlertEdit_ssid.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean focus) {
                        //输入框出来则往上移动
                        boolean isOpen=imm.isActive();
                        mAlertViewExt.setMarginBottom(isOpen&&focus ? 120 :0);
                    }
                });
                mAlertEdit_pwd = (EditText) extView.findViewById(R.id.wifi_pwd);
                mAlertEdit_pwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean focus) {
                        //输入框出来则往上移动
                        boolean isOpen=imm.isActive();
                        mAlertViewExt.setMarginBottom(isOpen&&focus ? 120 :0);
                    }
                });
                for (CommandInfo comm : commandInfos) {
                    if (comm != null && comm.getCtype() .equals(CommandInfo.CommandTypeEnum.setWifiSSID.value()) ){
                        mAlertEdit_ssid.setText(comm.getCommand());
                    }else if (comm != null && comm.getCtype() .equals(CommandInfo.CommandTypeEnum.setWifiPassword.value()) ){
                        mAlertEdit_pwd.setText(comm.getCommand());
                    }
                }
                mAlertViewExt.addExtView(extView);
                mAlertViewExt.show();
                break;
            case R.id.ll_zhuji_init_parent:
                showInProgress(getString(R.string.please_wait));
                JavaThreadPool.getInstance().excute(new RefreshInitThread());
                break;
        }
    }

    private int wsdTag = 60;
    private int zhujiAlarmTag = 5;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 100:
                if (resultCode == 10) {
                    String time = data.getStringExtra("time");
                    wsdTag = Integer.parseInt(time);
                    tips_wsd_show.setText(time + getString(R.string.wsd_accept_time_min));
                }
                break;
            case 101:
                if (resultCode == 10) {
                    String time = data.getStringExtra("time");
                    zhujiAlarmTag = Integer.parseInt(time);
                    if (zhujiAlarmTag == 5) {
                        tv_alarm_time.setText(getString(R.string.wsd_accept_time_5_default));
                    } else {
                        tv_alarm_time.setText(zhujiAlarmTag + getString(R.string.wsd_accept_time_min));
                    }
                }
                break;
            case CM_REQUEST_CODE:
                if(resultCode==RESULT_OK){
                    new LoadCommandsInfo(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,zhuji_id);
                }
                break ;
            case JIA_YU_REQUECT_CODE:
                if(resultCode==RESULT_OK){
                    jiayu_army_current_tv.setText(data.getStringExtra(SEND_RESULT_EXTRAS));
                }
                break ;
            case JIA_YU_ALARM_REQUEST:
                if(resultCode==RESULT_OK){
                    jiayu_aralm_current_tv.setText(data.getStringExtra(SEND_RESULT_EXTRAS));
                }
                break ;
            default:
                break;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (rl_setting_gensture.getVisibility() == View.VISIBLE) {
            String patternString = dcsp.getString(Constant.CODE_GENSTURE, "");
            if (TextUtils.isEmpty(patternString)  /*patternString == null*/) {

                runOnUiThread(new Runnable() {
                    public void run() {
                        tv_gensture_status.setText(R.string.gesture_unInit);
                    }
                });
            } else {
                dcsp = DataCenterSharedPreferences.getInstance(ZhujiSettingActivity.this,
                        Constant.CONFIG);
                runOnUiThread(new Runnable() {
                    public void run() {

                        //tv_gensture_status.setText(dcsp.getBoolean(Constant.IS_APP_GENSTURE, false) ? (R。string。gensture_open) : (R。string。gensture_off));

                        if (dcsp.getBoolean(Constant.IS_APP_GENSTURE, false)) {
                            tv_gensture_status.setText(R.string.gensture_open);
                        } else {
                            tv_gensture_status.setText(R.string.gensture_off);
                        }
                    }
                });
            }

        }
        JavaThreadPool.getInstance().excute(new InitDeviceSetThread());
    }

    public void back(View v) {
        finish();
    }


    /**
     * what 为0 时直接在线程内部处理，非0才会回调出来
     */
    private class PropertiesSet implements Runnable {
        int what = 0;
        String[] keys,values;
        public PropertiesSet(int what,String[] keys,String[] values){
            this.what = what;
            this.keys = keys;
            this.values = values;
        }
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", zhuji.getId());
            JSONArray array = new JSONArray();
            if (keys!=null && keys.length > 0 && values!=null && keys.length == values.length){
                for (int i=0; i<keys.length;i++){
                    JSONObject o = new JSONObject();
                    o.put("vkey", keys[i]);
                    o.put("value",values[i]);
                    array.add(o);
                }
            }
            object.put("vkeys", array);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/p/set", object, ZhujiSettingActivity.this);

            if (result != null && result.equals("0")) {
                if (what != 0){
                    mHandler.sendMessage(mHandler.obtainMessage(what));
                }else{
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ZhujiSettingActivity.this, getString(R.string.success),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(ZhujiSettingActivity.this, getString(R.string.operator_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
    private class RefreshInitThread implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", zhuji.getId());
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/refreshInit", object, ZhujiSettingActivity.this);

            if (result != null && result.equals("0")) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(ZhujiSettingActivity.this, getString(R.string.success),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(ZhujiSettingActivity.this, getString(R.string.operator_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    class InitDeviceSetThread implements Runnable {
        @Override
        public void run() {
            zhujiSetInfos = DatabaseOperator.getInstance().queryZhujiSets(zhuji.getId());
            mHandler.sendEmptyMessage(mHandleWhat_15);
        }
    }

}
