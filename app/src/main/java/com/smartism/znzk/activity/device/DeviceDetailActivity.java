package com.smartism.znzk.activity.device;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.FragmentParentActivity;
import com.smartism.znzk.activity.camera.CameraBindAlarmGuideActivity;
import com.smartism.znzk.activity.common.NoticeCenterActivity;
import com.smartism.znzk.activity.common.ZhujiSettingActivity;
import com.smartism.znzk.activity.common.ZldGSMSettingActivity;
import com.smartism.znzk.activity.device.share.ShareDevicesActivity;
import com.smartism.znzk.activity.scene.SceneActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.OwenerInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.SwitchButton.SwitchButton;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.xiongmai.fragment.InputFragment;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class DeviceDetailActivity extends FragmentParentActivity implements View.OnClickListener
        , HttpAsyncTask.IHttpResultView,InputFragment.OnInputContentListener{
    private final int dHandler_loadsuccess = 2, dHandler_timeout = 4, dHandler_oweneractivity = 8, dHandlerWhat_serverupdatetimeout = 3;
    public List<DeviceInfo> deviceInfos;
    private ListView devices_listView;
    private DeviceAdapter deviceAdapter;
    private ZhujiInfo zhuji;
    private OwenerInfo owenerInf;
    private TextView tv_owner;
    private LinearLayout layout_owner, ll_zhuji_info, ll_alarm,ll_lv,ll_wifiymq_pwd;
//    private View dv_view,dv_view_top;
    private AlertView showAlert;
    private boolean isHaveFmq;
    private Context mContext;
    private TextView tv_version;
    private String newVersion = "";

    //nb烟感设备离线提醒设置
    private RelativeLayout rl_smoke_offline ;
    private SwitchButton switch_smoke_btn  ;

    // 显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            // .displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();

    private boolean flagClick;
    private BroadcastReceiver defaultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (Actions.ACCETP_ONEDEVICE_MESSAGE.equals(intent.getAction())) { // 某一个设备的推送广播
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    cancelInProgress();
                    defaultHandler.removeMessages(dHandler_timeout);
                }
                refreshData();
            } else if (Actions.SHOW_SERVER_MESSAGE.equals(intent.getAction())) { // 显示服务器信息
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    cancelInProgress();
                    defaultHandler.removeMessages(dHandler_timeout);
                }
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(intent.getStringExtra("message"));
                } catch (Exception e) {
                    Log.w("DevicesList", "获取服务器返回消息，转换为json对象失败，用原始值处理");
                }
                if (resultJson != null) {
                    switch (resultJson.getIntValue("Code")) {
                        case 4:
                            Toast.makeText(DeviceDetailActivity.this, getString(R.string.tips_4), Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            Toast.makeText(DeviceDetailActivity.this, getString(R.string.tips_5), Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(DeviceDetailActivity.this, getString(R.string.tips_6), Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(DeviceDetailActivity.this, getString(R.string.tips_7), Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            Toast.makeText(DeviceDetailActivity.this, getString(R.string.tips_8), Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            Toast.makeText(DeviceDetailActivity.this, "Unknown Info", Toast.LENGTH_SHORT).show();
                            break;
                    }

                } else {
                    Toast.makeText(DeviceDetailActivity.this, intent.getStringExtra("message"), Toast.LENGTH_SHORT)
                            .show();
                }
                refreshData();
            } else if (Actions.CONNECTION_FAILED_SENDFAILED.equals(intent.getAction())) { //服务器未连接
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    cancelInProgress();
                    defaultHandler.removeMessages(dHandler_timeout);
                }
                Toast.makeText(DeviceDetailActivity.this, getString(R.string.rq_control_sendfailed),
                        Toast.LENGTH_SHORT).show();
                refreshData();
            } else if (Actions.ZHUJI_CHECKUPDATE.equals(intent.getAction())) {

                cancelInProgress();
//                boolean to = defaultHandler.hasMessages(dHandler_timeout);
                defaultHandler.removeMessages(dHandler_timeout);
                if (SyncMessage.CodeMenu.rp_checkpudate_nonew.value() == intent.getIntExtra("data", 0)) {
//                    if (to) { // 当页面数据初始化完成会检测主机的固件版本，这个时候是不需要显示固件是最新提示的
                        String data_info = intent.getStringExtra("data_info");
                        if (data_info != null && !"".equals(data_info)) {
                            JSONObject object = JSON.parseObject(data_info);
                            if (flagClick)
                                Toast.makeText(mContext, getString(R.string.deviceslist_server_noupdatev, object.getString("ov")),
                                        Toast.LENGTH_SHORT).show();
                            String o = object.getString("ov") + " " + "(" + getString(R.string.frimware_dadate_new) + ")";
                            tv_version.setText(o);
                        } else {
                            if (flagClick)
                                Toast.makeText(mContext, getString(R.string.deviceslist_server_noupdatev),
                                        Toast.LENGTH_SHORT).show();
                            tv_version.setText("(" + getString(R.string.frimware_dadate_new) + ")");
                        }
//                    }

                } else if (SyncMessage.CodeMenu.rp_checkpudate_havenew.value() == intent.getIntExtra("data", 0)) {
                    String data_info = intent.getStringExtra("data_info");
                    if (data_info != null && !"".equals(data_info)) {
                        JSONObject object = JSON.parseObject(data_info);
                        data_info = getString(R.string.deviceslist_server_update_havenewv, object.getString("ov"), object.getString("nv"));
                        newVersion = object.getString("nv");
                        String o = object.getString("ov") + " " + "(" + getString(R.string.frimware_dadate_old) + ")";
                        tv_version.setText(o);
                    } else {
                        data_info = getString(R.string.deviceslist_server_update_havenew);
                        tv_version.setText("(" + getString(R.string.frimware_dadate_old) + ")");
                    }
                    if (flagClick) {
                        if (showAlert == null || !showAlert.isShowing()) {
                            showAlert = new AlertView(getString(R.string.deviceslist_server_update),
                                    data_info,
                                    getString(R.string.deviceslist_server_leftmenu_delcancel),
                                    new String[]{getString(R.string.deviceslist_server_update_button)}, null,
                                    mContext, AlertView.Style.Alert,
                                    new com.smartism.znzk.view.alertview.OnItemClickListener() {

                                        @Override
                                        public void onItemClick(Object o, int position) {
                                            if (position != -1) {
                                                showInProgress(getString(R.string.ongoing), false, false);
                                                defaultHandler.sendEmptyMessageDelayed(dHandlerWhat_serverupdatetimeout, 20000);
                                                SyncMessage message1 = new SyncMessage();
                                                message1.setCommand(SyncMessage.CommandMenu.rq_pudate.value());
                                                message1.setDeviceid(zhuji.getId());
                                                SyncMessageContainer.getInstance().produceSendMessage(message1);
                                            }
                                        }
                                    });
                            showAlert.show();
                        }
                        flagClick = false;
                    }
                }
            } else if (Actions.ZHUJI_UPDATE.equals(intent.getAction())) { // 主机更新
                System.out.println(
                        "max:" + intent.getIntExtra("max", 0) + "progress:" + intent.getIntExtra("progress", 0));
                defaultHandler.removeMessages(dHandlerWhat_serverupdatetimeout);
                if (SyncMessage.CodeMenu.rp_pupdate_into.value() == intent.getIntExtra("data", 0)) {
                    cancelInProgress();
                    showOrUpdateProgressBar(getString(R.string.deviceslist_server_updating), true, 1, 100);
                } else if (SyncMessage.CodeMenu.rp_pupdate_success.value() == intent.getIntExtra("data", 0)) {
                    cancelInProgress();
                    cancelInProgressBar();
                    String o = newVersion + " " + "(" + getString(R.string.frimware_dadate_new) + ")";
                    tv_version.setText(o);
                    Toast.makeText(mContext, getString(R.string.deviceslist_server_update_success),
                            Toast.LENGTH_LONG).show();
                } else if (SyncMessage.CodeMenu.rp_pupdate_progress.value() == intent.getIntExtra("data", 0)) {
                    showOrUpdateProgressBar(getString(R.string.deviceslist_server_updating), true,
                            intent.getIntExtra("progress", 0) + 1, intent.getIntExtra("max", 0));
                    if (intent.getIntExtra("progress", 0) + 1 == intent.getIntExtra("max", 0)
                            && intent.getIntExtra("progress", 0) != 0) {
                        showInProgress(getString(R.string.deviceslist_server_update_reboot), false, true);
                    }
                }
            }
        }
    };

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Intent intent = null;
            switch (msg.what) {
                case dHandlerWhat_serverupdatetimeout:
                    cancelInProgress();
                    Toast.makeText(getApplicationContext(), getString(R.string.deviceslist_server_updating_timeout),
                            Toast.LENGTH_SHORT).show();
                    break;
                case dHandler_oweneractivity:
                    owenerInf = (OwenerInfo) msg.obj;
                    cancelInProgress();
                    intent = new Intent(DeviceDetailActivity.this, ZhujiOwnerActivity.class);
                    intent.putExtra("zhuji", zhuji);
                    intent.putExtra("owenerInf", owenerInf);
                    startActivity(intent);
                    break;
                case 1:
                    owenerInf = (OwenerInfo) msg.obj;
                    cancelInProgress();
                    intent = new Intent(DeviceDetailActivity.this, ZhujiOwnerActivity.class);
                    intent.putExtra("zhuji", zhuji);
                    intent.putExtra("owenerInf", owenerInf);
                    startActivity(intent);
                    break;
                case dHandler_loadsuccess: // 加载完成
                    deviceInfos = (List<DeviceInfo>) msg.obj;
//                    if (deviceInfos == null || deviceInfos.isEmpty()) {
//                        dv_view.setVisibility(View.GONE);
//                        dv_view_top.setVisibility(View.GONE);
//                    } else {
//                        dv_view.setVisibility(View.VISIBLE);
//                        dv_view_top.setVisibility(View.VISIBLE);
//                    }
                    //智利的版本不显示主机蜂鸣器
                    if(Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                        for(DeviceInfo deviceInfo :deviceInfos){
                            if(deviceInfo.getCa().equals(DeviceInfo.CaMenu.zhujifmq.value())){
                                deviceInfos.remove(deviceInfo);
                            }
                        }
                    }else if(Actions.VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                        String temp = zhuji.getRolek() ;
                        if(!(zhuji.isAdmin()||zhuji.getRolek().equals("lock_num_partner"))){
                            //不是管理、主賬戶、愛人，移除主机蜂鸣器
                            for(DeviceInfo deviceInfo :deviceInfos){
                                if(deviceInfo.getCa().equals(DeviceInfo.CaMenu.zhujifmq.value())){
                                    deviceInfos.remove(deviceInfo);
                                }
                            }
                        }
                    }
                    deviceAdapter = new DeviceAdapter(DeviceDetailActivity.this);
//                    setListViewHeightBasedOnChildren(devices_listView);
                    devices_listView.setAdapter(deviceAdapter);
                    if (!CollectionsUtils.isEmpty(deviceInfos))
                        isHaveFmq = true;
                    break;
                case dHandler_timeout:
                    cancelInProgress();
                    Toast.makeText(getApplicationContext(), getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    private DeviceInfo deviceInfo;
    private ImageView iv_history, iv_share;
    private SwitchButton btn_toogle_online_status, btn_toogle_offline_status;
    private TextView tv_alarm_time;

    private int alarmType = -1;
    private boolean isAdmin;
    private LinearLayout ll_sence, ll_security, ll_check_version, ll_setting, ll_users, ll_disarming, ll_camera, ll_notice_center;
    private TextView mZhujiInfoTv,mZhujiOwner ,mZhujiTitleTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices_detail);
        Intent intent = getIntent();
        deviceInfo = (DeviceInfo) intent.getSerializableExtra("device");
        zhuji = DatabaseOperator.getInstance(this).queryDeviceZhuJiInfo(deviceInfo.getZj_id());
        mContext = this;
        initViews();
        getData();
        tv_owner.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showInProgress(getString(R.string.loading), false, true);
                JavaThreadPool.getInstance().excute(new LoadingOwener(zhuji.getId(), dHandler_oweneractivity));
            }
        });



        if(Actions.VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            ll_camera.setVisibility(View.GONE);
            if(zhuji!=null){
               if(zhuji.isAdmin()||zhuji.getRolek().equals("lock_num_partner")){
                   ll_check_version.setVisibility(View.VISIBLE);
                   ll_users.setVisibility(View.VISIBLE);
               }else{
                   ll_check_version.setVisibility(View.GONE);
                   ll_users.setVisibility(View.GONE);
               }
            }
        }else if(Actions.VersionType.CHANNEL_ZHZJ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            if(zhuji.getCa()!=null&&(zhuji.getCa().equals(DeviceInfo.CaMenu.nbyg.value()))
                    ||DeviceInfo.CaMenu.nbrqbjq.value().equals(zhuji.getCa())){
                mZhujiInfoTv = findViewById(R.id.tv_zhuji_info);
                mZhujiOwner = findViewById(R.id.tv_zhuji_owenr);
                mZhujiTitleTv = findViewById(R.id.tv_title);
                ll_sence.setVisibility(View.GONE);
                ll_security.setVisibility(View.GONE);
                ll_setting.setVisibility(View.GONE);
                ll_disarming.setVisibility(View.GONE);
                ll_camera.setVisibility(View.GONE);
                ll_notice_center.setVisibility(View.GONE);
                ll_lv.setVisibility(View.GONE);
                iv_history.setVisibility(View.GONE);

                mZhujiTitleTv.setText(getString(R.string.deviceinfo_activity_singleproduct_title,getString(R.string.deviceinfo_activity_singleproduct_nbyg)));
                mZhujiInfoTv.setText(getString(R.string.deviceinfo_activity_singleproduct_info,getString(R.string.deviceinfo_activity_singleproduct_nbyg)));
                mZhujiOwner.setText(getString(R.string.deviceinfo_activity_singleproduct_ower,getString(R.string.deviceinfo_activity_singleproduct_nbyg)));

                //nb烟感离线提醒
                if(DeviceInfo.CaMenu.nbyg.value().equals(zhuji.getCa())){
                    initSmokeView();
                }

            }else if(zhuji.getCa()!=null&&zhuji.getCa().equals(DeviceInfo.CaMenu.wifirqbjq.value())){
                ll_sence.setVisibility(View.GONE);
                ll_security.setVisibility(View.GONE);
       //         ll_check_version.setVisibility(View.GONE);
       //         ll_setting.setVisibility(View.GONE);
                ll_disarming.setVisibility(View.GONE);
                ll_camera.setVisibility(View.GONE);
                ll_notice_center.setVisibility(View.GONE);
                ll_alarm.setVisibility(View.GONE);
                ll_lv.setVisibility(View.GONE);
                iv_history.setVisibility(View.GONE);
            }
        }else if(Actions.VersionType.CHANNEL_YIXUNGE.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            settingWifiymq();
            iv_history.setVisibility(View.GONE);
        }
    }

    //wifi烟雾器设置
    private void settingWifiymq(){
        if(zhuji.getCa()!=null&&zhuji.getCa().equals(DeviceInfo.CaMenu.wifiymq.value())){
            ll_sence.setVisibility(View.GONE);
            ll_security.setVisibility(View.GONE);
            ll_disarming.setVisibility(View.GONE);
            if(zhuji.isAdmin()){
                ll_wifiymq_pwd.setVisibility(View.VISIBLE);
                ll_wifiymq_pwd.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        InputFragment.getInstance(null,null).show(getSupportFragmentManager(),"input_fragment");
                    }
                });
            }
        }
    }

    //nb烟感离线提醒设置
    private void initSmokeView(){
        rl_smoke_offline.setVisibility(View.VISIBLE);
        switch_smoke_btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int time = -1;
                if(isChecked){
                    //时间1分钟
                    time = 60 ;
                }else{
                    //关闭
                    time = -1 ;
                }
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", zhuji.getId());
                JSONArray array = new JSONArray();
                JSONObject object = new JSONObject();
                object.put("vkey", "alert_offline");
                object.put("value",String.valueOf(time));
                array.add(object);
                pJsonObject.put("vkeys", array);
                new HttpAsyncTask(DeviceDetailActivity.this,
                        HttpAsyncTask.Zhuji_SET_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pJsonObject);
            }
        });

        //同步switchbutton状态
        new LoadZhujiAndDeviceTask().queryAllCommandInfo(zhuji.getId(), new LoadZhujiAndDeviceTask.ILoadResult<List<CommandInfo>>() {
            @Override
            public void loadResult(List<CommandInfo> result) {
                switch_smoke_btn.setCheckedImmediatelyNoEvent(false);//默认关闭
                if(result==null){
                    return ;
                }
                for(CommandInfo info:result) {
                    if (info.getCtype().equals("alert_offline")) {
                        int value = Integer.parseInt(info.getCommand());
                        if (value < 0) {
                            switch_smoke_btn.setCheckedImmediatelyNoEvent(false);
                        } else {
                            switch_smoke_btn.setCheckedImmediatelyNoEvent(true);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initRegisterReceiver();
        if (zhuji != null && !zhuji.isOnline()) {
            if(DeviceInfo.CaMenu.nbyg.value().equals(zhuji.getCa())){
                tv_version.setText(getString(R.string.activity_nbyg_offine));
            }else {
                tv_version.setText(getString(R.string.activity_zhuji_not));
            }
        } else {
            flagClick = false;
//          defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 8 * 1000);
            SyncMessageContainer.getInstance().sendMessageToServer(SyncMessage.CommandMenu.rq_checkpudate, SyncMessage.CodeMenu.zero,
                    zhuji.getId(), null);
        }
//            Toast.makeText(mContext, getString(R.string.update_zhuji_gujian), Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (defaultReceiver != null) {
            unregisterReceiver(defaultReceiver);
        }
    }

    private void initViews() {
        rl_smoke_offline = findViewById(R.id.rl_smoke_device_offline_tip);
        switch_smoke_btn  = findViewById(R.id.smoke_offline_tip_switchbtn);
        ll_wifiymq_pwd = findViewById(R.id.ll_wifiymq_pwd);
        ll_security = (LinearLayout) findViewById(R.id.ll_security);
        ll_disarming = (LinearLayout) findViewById(R.id.ll_disarming);
        ll_sence = (LinearLayout) findViewById(R.id.ll_sence);
        ll_check_version = (LinearLayout) findViewById(R.id.ll_check_version);
        ll_setting = (LinearLayout) findViewById(R.id.ll_setting);
        ll_users = (LinearLayout) findViewById(R.id.ll_users);
        ll_camera = (LinearLayout) findViewById(R.id.ll_camera);
        ll_notice_center = (LinearLayout) findViewById(R.id.ll_notice_center);
        ll_sence.setOnClickListener(this);
        ll_check_version.setOnClickListener(this);
        ll_security.setOnClickListener(this);
        ll_setting.setOnClickListener(this);
        ll_users.setOnClickListener(this);
        ll_disarming.setOnClickListener(this);
        ll_notice_center.setOnClickListener(this);
        ll_camera.setOnClickListener(this);


        if (zhuji != null && zhuji.isAdmin()) {
            if (zhuji.getAc() > 0)
                ll_notice_center.setVisibility(View.VISIBLE);
        }

        if (zhuji != null && MainApplication.app.getAppGlobalConfig().isShowSecurity()) {
            ll_security.setVisibility(View.VISIBLE);
        }else{
            ll_security.setVisibility(View.GONE);
        }
        if (zhuji != null && MainApplication.app.getAppGlobalConfig().isShowScene()) {
            ll_sence.setVisibility(View.VISIBLE);
        }else{
            ll_sence.setVisibility(View.GONE);
        }
        iv_history = (ImageView) findViewById(R.id.iv_history);
        iv_share = (ImageView) findViewById(R.id.iv_share);
        iv_share.setOnClickListener(this);
        iv_history.setOnClickListener(this);
        tv_alarm_time = (TextView) findViewById(R.id.tv_alarm_time);
        tv_version = (TextView) findViewById(R.id.tv_version);
//        ll_his = (LinearLayout) findViewById(R.id.ll_his);
//        ll_share = (LinearLayout) findViewById(R.id.ll_share);
//        ll_his.setOnClickListener(this);
//        ll_share.setOnClickListener(this);
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

        tv_owner = (TextView) findViewById(R.id.tv_zhuji_owenr);
//        iv_circle = (CircleImageView) findViewById(R.id.iv_circle);
//        if (Actions.VersionType.CHANNEL_ZHZJ.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
        layout_owner = (LinearLayout) findViewById(R.id.layout_zhuji_owner);
        if (zhuji != null && zhuji.isAdmin() && !Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                && !Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
            //主账户才显示主机所有者 中山不显示主机所有者
            layout_owner.setVisibility(View.VISIBLE);
        }
        if (zhuji != null && zhuji.isAdmin()) {
            //主账户才显示分享主机
            iv_share.setVisibility(View.VISIBLE);
        }
//        }
        devices_listView = (ListView) findViewById(R.id.devices_listView);
        ll_lv = (LinearLayout) findViewById(R.id.ll_lv);
//        if (!"".equals(dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_LOGO, ""))) {
//            ImageLoader.getInstance().displayImage(dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_LOGO, ""), iv_circle,
//                    options_userlogo);
//        } else {
//            iv_circle.setImageResource(R.drawable.h0);
//        }
//        if (Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
        if (!zhuji.isLa()) {
            ll_lv.setVisibility(View.VISIBLE);
        }
        ll_zhuji_info = (LinearLayout) findViewById(R.id.ll_zhuji_info);
        ll_zhuji_info.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceDetailActivity.this, ZhujiInfoActivity.class);
                intent.putExtra("device", deviceInfo);
                intent.putExtra("owenerInf", owenerInf);
                startActivity(intent);
            }
        });
        ll_alarm = (LinearLayout) findViewById(R.id.ll_alarm);

        //设备主机对象判断是否是主账户（后面需求可能改成非管理员也可以显示报警电话这里的判断是否是主账户需要去掉）
        if ((MainApplication.app.getAppGlobalConfig().isShowCallAlarm()
            || MainApplication.app.getAppGlobalConfig().isShowSmsAlarm()) && zhuji.getGsm() == 0 && zhuji.isAdmin()) {
            ll_alarm.setVisibility(View.VISIBLE);
            alarmType = 1;//报警电话
            isAdmin = true;
        }
        if (zhuji.getGsm() == 1&&zhuji.isAdmin()){
            ll_alarm.setVisibility(View.VISIBLE);
            alarmType = 0;//Gsm电话
            isAdmin = true;
        }
        ll_alarm.setOnClickListener(this);


        if (Actions.VersionType.CHANNEL_UCTECH.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
            ll_sence.setVisibility(View.GONE);
            ll_security.setVisibility(View.GONE);
            ll_disarming.setVisibility(View.GONE);
            ll_check_version.setVisibility(View.GONE);
            iv_history.setVisibility(View.GONE);
            ll_camera.setVisibility(View.GONE);
        }else if(Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            if(zhuji.getCa().equals(DeviceInfo.CaMenu.bohaoqi.value())){
                ll_security.setVisibility(View.GONE);
                ll_disarming.setVisibility(View.GONE);
                ll_sence.setVisibility(View.GONE);
            }else if(zhuji.getCa().equals(DeviceInfo.CaMenu.zhuji.value())){
                ll_alarm.setVisibility(View.GONE);
            }
        }else if(Actions.VersionType.CHANNEL_HUALI.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            ll_disarming.setVisibility(View.GONE);
            ll_camera.setVisibility(View.GONE);
        }


        if (Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                ||Actions.VersionType.CHANNEL_ZHICHENG.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                ||Actions.VersionType.CHANNEL_ZHZJ.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                ||Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                ||Actions.VersionType.CHANNEL_WOFEE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                ||Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                ||Actions.VersionType.CHANNEL_HZYCZN.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                ||Actions.VersionType.CHANNEL_JAOLH.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                ||Actions.VersionType.CHANNEL_ANBABAOQUAN.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                ||Actions.VersionType.CHANNEL_DITAIXING.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
            ll_camera.setVisibility(View.GONE);
        }
    }

    private void setOnOffline(final String key, final boolean isChecked) {
        showInProgress(getString(R.string.loading), false, true);
        defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 10 * 1000);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", deviceInfo.getId());
                JSONArray array = new JSONArray();
                JSONObject object = new JSONObject();
                object.put("vkey", key);
                object.put("value", isChecked == true ? "2" : "0");
                array.add(object);
                pJsonObject.put("vkeys", array);

                String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/u/p/set", pJsonObject, DeviceDetailActivity.this);
                if ("-3".equals(result)) {
                    if (defaultHandler.hasMessages(dHandler_timeout)) {
                        defaultHandler.removeMessages(dHandler_timeout);
                    }
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(DeviceDetailActivity.this, getString(R.string.net_error_nodata),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-5".equals(result)) {
                    if (defaultHandler.hasMessages(dHandler_timeout)) {
                        defaultHandler.removeMessages(dHandler_timeout);
                    }
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(DeviceDetailActivity.this, getString(R.string.device_not_getdata),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("0".equals(result)) {
                    if (defaultHandler.hasMessages(dHandler_timeout)) {
                        defaultHandler.removeMessages(dHandler_timeout);
                    }
                    defaultHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(DeviceDetailActivity.this, getString(R.string.success),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
        });
    }

    String result = null;

    private void getData() {
        JavaThreadPool.getInstance().excute(new loadAllDevicesInfo(dHandler_loadsuccess));

    }

    public void back(View v) {
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 100:
                if (resultCode == 10) {
                    String time = data.getStringExtra("time");
                    tag = Integer.parseInt(time);
                    if (tag == 30) {
                        tv_alarm_time.setText(getString(R.string.wsd_accept_time_30_default));
                    } else {
                        tv_alarm_time.setText(tag + getString(R.string.wsd_accept_time_min));
                    }
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.ll_notice_center:
                intent.setClass(this, NoticeCenterActivity.class);
                intent.putExtra("zhuji_id", zhuji.getId());
                startActivity(intent);
                break;
            case R.id.ll_camera:
                if (zhuji != null && !zhuji.isOnline()) {
                    Toast.makeText(mContext, getString(R.string.update_zhuji_gujian), Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.putExtra("zhuji", zhuji);
                intent.setClass(this, CameraBindAlarmGuideActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_disarming:
                intent.putExtra("zhuji_id", zhuji.getId());
                intent.setClass(this, SecurityDisarmingActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_alarm:
                if(Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                        &&zhuji.getCa().equals(DeviceInfo.CaMenu.bohaoqi.value())){
                    intent.setClass(mContext, ZldGSMSettingActivity.class);
                    intent.putExtra("zhuji_id",deviceInfo.getZj_id());
                }else{
                     intent.setClass(mContext, DeviceSetGSMPhoneActivity.class);
                     intent.putExtra("device", deviceInfo);
                     intent.putExtra("type", alarmType);
                     intent.putExtra("isAdmin", isAdmin);
                }
                startActivity(intent);
                break;
            case R.id.ll_users:
                intent.putExtra("device", deviceInfo);
                intent.setClass(this, PerminssonTransActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_setting:
                intent.putExtra("zhuji_id", zhuji.getId());
                intent.putExtra("isshow", isHaveFmq);
                intent.setClass(this, ZhujiSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_check_version:
                if (zhuji != null && !zhuji.isOnline()) {
                    Toast.makeText(mContext, getString(R.string.update_zhuji_gujian), Toast.LENGTH_SHORT).show();
                    return;
                }
                flagClick = true;
                SyncMessageContainer.getInstance().sendMessageToServer(SyncMessage.CommandMenu.rq_checkpudate, SyncMessage.CodeMenu.zero,
                        zhuji.getId(), null);
                showInProgress(getString(R.string.loading), false, false);
                defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 8 * 1000);
                break;
            case R.id.ll_security:
                intent.putExtra("zhuji_id", zhuji.getId());
                intent.setClass(this, SecurityActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_sence:
                if (!zhuji.isOnline()) {
                    Toast.makeText(this, getString(R.string.deviceslist_zhuji_offline), Toast.LENGTH_SHORT).show();
                    break;
                }
                intent.setClass(getApplicationContext(), SceneActivity.class);
                startActivity(intent);
                break;
//            case R.id.ll_his:
//                intent.setClass(this, DeviceInfoActivity.class);
//                intent.putExtra("device", deviceInfo);
//                startActivity(intent);
//                break;
//            case R.id.ll_share:
//                intent.putExtra("pattern", "status_forver");
//                intent.setClass(getApplicationContext(), ShareDevicesActivity.class);
//                startActivity(intent);
//                break;
            case R.id.iv_history:
                intent.setClass(this, DeviceInfoActivity.class);
                intent.putExtra("device", deviceInfo);
                startActivity(intent);
                break;
            case R.id.iv_share:
                intent.putExtra("pattern", "status_forver");
                intent.setClass(getApplicationContext(), ShareDevicesActivity.class);
                intent.putExtra("shareid",deviceInfo!=null?deviceInfo.getId():0);
                startActivity(intent);
                break;
        }
    }


    private void initRegisterReceiver() {
        // 注册广播
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        receiverFilter.addAction(Actions.SHOW_SERVER_MESSAGE);
        receiverFilter.addAction(Actions.CONNECTION_FAILED_SENDFAILED);
        receiverFilter.addAction(Actions.ZHUJI_CHECKUPDATE);
        receiverFilter.addAction(Actions.ZHUJI_UPDATE);
        registerReceiver(defaultReceiver, receiverFilter);
    }

    @Override
    public void setResult(int flag, String result) {
        if(flag == HttpAsyncTask.Zhuji_SET_URL_FLAG) {
            if ("-3".equals(result)) {
                ToastUtil.longMessage(getString(R.string.net_error_nodata));
                //设置失败，还原
                if(switch_smoke_btn.getVisibility()==View.VISIBLE){
                    switch_smoke_btn.setCheckedImmediatelyNoEvent(!switch_smoke_btn.isChecked());
                }
            } else if ("-5".equals(result)) {
                //设置失败，还原
                if(switch_smoke_btn.getVisibility()==View.VISIBLE){
                    switch_smoke_btn.setCheckedImmediatelyNoEvent(!switch_smoke_btn.isChecked());
                }
                ToastUtil.longMessage(getString(R.string.device_not_getdata));
            } else if ("0".equals(result)) {
                ToastUtil.longMessage(getString(R.string.success));
            }
        }
    }

    @Override
    public void showProgress(String text) {
        showInProgress("",true,false);
    }

    @Override
    public void hideProgress() {
        cancelInProgress();
    }

    @Override
    public void error(String message) {

    }

    @Override
    public void success(String message) {

    }

    //密码输入对话框回调
    @Override
    public void onInputContent(String content, boolean confirm) {
        if(!confirm){
            //点击取消按钮
            return ;
        }
        if(content.length()<6||!TextUtils.isDigitsOnly(content)){
            //请输入6位数字密码
            ToastUtil.shortMessage(getString(R.string.abbq_update_cld_pwd_length));
            return ;
        }
        JSONObject pJsonObject = new JSONObject();
        pJsonObject.put("did", zhuji.getId());
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("vkey", "pwd_control");
        object.put("value",content);
        array.add(object);
        pJsonObject.put("vkeys", array);
        new HttpAsyncTask(DeviceDetailActivity.this,
                HttpAsyncTask.Zhuji_SET_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pJsonObject);
    }


    class loadAllDevicesInfo implements Runnable {
        private int what;

        public loadAllDevicesInfo() {
        }

        public loadAllDevicesInfo(int what) {
            this.what = what;
        }

        @Override
        public void run() {
            List<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();
            Cursor cursor = DatabaseOperator.getInstance(DeviceDetailActivity.this).getReadableDatabase().rawQuery(
                    "select * from DEVICE_STATUSINFO where zj_id = ? ",
                    new String[]{String.valueOf(zhuji.getId())});
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    DeviceInfo deviceInfo = DatabaseOperator.getInstance(DeviceDetailActivity.this).buildDeviceInfo(cursor);
                    if (DeviceInfo.CaMenu.zhujifmq.value().equals(deviceInfo.getCa())) {
                        deviceList.add(deviceInfo);
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            Message m = defaultHandler.obtainMessage(this.what);
            m.obj = deviceList;
            defaultHandler.sendMessage(m);
        }
    }

    private void refreshData() {
        JavaThreadPool.getInstance().excute(new loadAllDevicesInfo(dHandler_loadsuccess));
    }


    @SuppressLint("NewApi")
    class DeviceAdapter extends BaseAdapter {
        /**
         * 视图内部类
         *
         * @author Administrator
         */
        class DeviceInfoView {
            SwitchButton switchButton;
            ImageView ioc;
            TextView name;
        }

        LayoutInflater layoutInflater;

        public DeviceAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return deviceInfos.size();
        }

        @Override
        public Object getItem(int arg0) {
            return deviceInfos.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        /**
         * 返回一个view视图，填充gridview的item
         */
        @SuppressLint("NewApi")
        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            DeviceInfoView viewCache = null;
            if (view == null) {
                viewCache = new DeviceInfoView();
                view = layoutInflater.inflate(R.layout.activity_devices_detail_list_item, null);
                viewCache.ioc = (ImageView) view.findViewById(R.id.device_logo);
                viewCache.name = (TextView) view.findViewById(R.id.device_name);
                viewCache.switchButton = (SwitchButton) view.findViewById(R.id.c_switchButton);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
            initButtonEvent(viewCache, i);
            setDeviceLogoAndName(viewCache, i);
            return view;
        }

        /**
         * 设置设备logo图片和名称
         *
         * @param
         */
        private void setDeviceLogoAndName(DeviceInfoView viewCache, int i) {
            // 设置图片
            ImageLoader.getInstance().displayImage(dcsp.getString(Constant.HTTP_DATA_SERVERS, "")
                    + "/devicelogo/" + deviceInfos.get(i).getLogo(), viewCache.ioc, options, new ImageLoadingBar());
            viewCache.name.setText(deviceInfos.get(i).getName());
        }


        /**
         * 初始化按钮点击事件
         *
         * @param viewCache
         */
        private void initButtonEvent(final DeviceInfoView viewCache, final int i) {

            viewCache.switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                    showInProgress(getString(R.string.operationing), false, true);
//                    getWindow().setLocalFocus(false, false);
                    if (Util.isFastClick()) {
                        Toast.makeText(mContext, getString(R.string.activity_devices_commandhistory_tip), Toast.LENGTH_SHORT).show();
                    } else {
                        SyncMessage message = new SyncMessage();
                        message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                        message.setDeviceid(deviceInfos.get(i).getId());

                        if (arg1) { // 开关操作
                            // 这里的操作写死，需要在服务器的配置页面中配置此按钮对应的指令是什么
                            // 开操作
                            Log.e("aaa", "发送通知===开指令");
                            message.setSyncBytes(new byte[]{0x01});
                        } else {
                            // 关操作
                            Log.e("aaa", "发送通知===关指令");
                            message.setSyncBytes(new byte[]{0x00});
                        }

                        // 点击后显示进度条
                        defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 8000);
                        SyncMessageContainer.getInstance().produceSendMessage(message);
                    }
                }
            });
            viewCache.switchButton.setCheckedImmediatelyNoEvent(deviceInfos.get(i).getAcceptMessage() == 0 ? false : true);
        }
    }

    class LoadingOwener implements Runnable {
        private long did;
        private int what;

        public LoadingOwener(long did, int what) {
            this.did = did;
            this.what = what;
        }

        @Override
        public void run() {
            JSONObject o = new JSONObject();
            o.put("id", did);
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            String result = HttpRequestUtils
                    .requestoOkHttpPost(server + "/jdm/s3/dzj/get", o, DeviceDetailActivity.this);
            // -1参数为空 -2校验失败 -10服务器不存在
            if (result != null && result.length() > 4) {

                try {
//                    {"createTime":1487759700000,"dId":1046241555382272,"id":56862237192617984,"masterId":"FF020F4376","operateIs":false,"updateTime":1487759700000,"userAddress":"广东","userAreaId":1,"userCityId":36,"userCountyId":37,
//                            "userName":"微微","userPhone":"15677408825","userTel":"8980337","valid":true}
                    JSONObject JSONobj = JSONObject.parseObject(result);
                    OwenerInfo owenerInfo = new OwenerInfo();
                    owenerInfo.setId(Long.parseLong(JSONobj.getString("dId")));
                    owenerInfo.setMasterId(JSONobj.getString("masterId"));
                    if (JSONobj.getString("serviceTime") != null) {
                        owenerInfo.setServiceTime(Long.parseLong(JSONobj.getString("serviceTime")));
                    }
                    owenerInfo.setUserAddress(JSONobj.getString("userAddress"));
                    owenerInfo.setUserAreaInfo(JSONobj.getString("userAreaInfo"));
                    owenerInfo.setUserAreaId(Long.parseLong(JSONobj.getString("userAreaId")));
                    owenerInfo.setUserCityId(Long.parseLong(JSONobj.getString("userCityId")));
                    owenerInfo.setUserCountyId(Long.parseLong(JSONobj.getString("userCountyId")));
                    owenerInfo.setUserName(JSONobj.getString("userName"));
                    owenerInfo.setUserPhone(JSONobj.getString("userPhone"));
                    owenerInfo.setUserTel(JSONobj.getString("userTel"));
                    owenerInfo.setUserStreetId(JSONobj.getLong("userStreetId"));
                    owenerInfo.setUserCommunityId(JSONobj.getLong("userCommunityId"));
                    Message message = new Message();
                    message.obj = owenerInfo;
                    message.what = this.what;
                    defaultHandler.sendMessage(message);
                } catch (JSONException e) {
                    Log.e(TAG, "登录IPC异常：", e);
                }
            } else {
                defaultHandler.sendEmptyMessage(this.what);
            }

        }
    }

    private int tag = 30;

    private class GetZhujiAlarmTime implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("did", zhuji.getId());
            JSONArray array = new JSONArray();
            JSONObject object = new JSONObject();
            object.put("vkey", CommandInfo.CommandTypeEnum.setZhujiAlarmTime.value());
//                JSONObject object1 = new JSONObject();
//                object1.put("vkey", "alertlevel_offline");
            array.add(object);
//                array.add(object1);
            pJsonObject.put("vkeys", array);

            final String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/p/list", pJsonObject, DeviceDetailActivity.this);
            if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
//                            Toast.makeText(WSDTimeSetActivity.this, getString(R.string.net_error_nodata),
//                                    Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-5".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
//                            Toast.makeText(WSDTimeSetActivity.this, getString(R.string.device_not_getdata),
//                                    Toast.LENGTH_LONG).show();
                    }
                });
            } else if (!StringUtils.isEmpty(result)) {
                final JSONArray array1 = JSONArray.parseArray(result);
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (array1 != null) {
                            for (int i = 0; i < array1.size(); i++) {
                                JSONObject jsonObject = array1.getJSONObject(i);

                                if (jsonObject.getString("key").equals(CommandInfo.CommandTypeEnum.setZhujiAlarmTime.value())) {
                                    if (jsonObject.containsKey("value")) {
                                        String results = jsonObject.getString("value");
                                        try {
                                            int a = Integer.parseInt(results);
                                            tag = a;
                                            if (tag == 30) {
                                                tv_alarm_time.setText(getString(R.string.wsd_accept_time_30_default));
                                            } else {
                                                tv_alarm_time.setText(tag + getString(R.string.wsd_accept_time_min));
                                            }
                                            Log.d("wsdj_time", a + "");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        tag = 30;
                                        tv_alarm_time.setText(getString(R.string.wsd_accept_time_30_default));
                                    }
                                }

                            }
                        }
                    }
                });

            } else {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        tag = 30;
                    }
                });
            }
        }
    }
}
