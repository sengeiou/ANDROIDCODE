package com.smartism.znzk.activity.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.scene.CustomSceneActivity;
import com.smartism.znzk.activity.scene.LinkageSceneActivity;
import com.smartism.znzk.activity.scene.TimingSceneActivity;
import com.smartism.znzk.adapter.scene.SceneAdapter;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.activity.camera.PlayBaseActivity;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.FoundInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.view.MyGridView;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SecurityInfoActivity extends PlayBaseActivity {
    public static final String FRUSH_DEVICE_INFO = MainApplication.app.getPackageName() + "FRUSH_DEVICE_INFO";
    private ImageView device_history, icon_cf, icon_info, imageView;
    private LinearLayout cf_layout, info_layout, list_layout, switch_layout;
    private TextView tv_cf, tv_info, info_name, device_msg, tv_title;
    private MyGridView security_list;
    private SceneAdapter sceneAdapter;
    private RadioGroup device_switch;
    private RadioButton switch_on, switch_off;
    private List<FoundInfo> sceneList = new ArrayList<FoundInfo>();
    private DeviceInfo deviceInfo;
    private int dr = 0;
    private boolean turn;
    private final int dHandler_timeout = 0, dHandler_scenes = 1, dHandler_scene = 10;
    private String result;
    private FoundInfo resultStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!com.smartism.znzk.util.StringUtils.isEmpty(MainApplication.app.getAppGlobalConfig().getAPPID())) {
            setContentView(R.layout.activity_security_info);
        }else{
            setContentView(R.layout.activity_security_info_nop2pview);
        }
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        mContact = (Contact) getIntent().getSerializableExtra("contact");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        intentFilter.addAction(Actions.SHOW_SERVER_MESSAGE);
        intentFilter.addAction(FRUSH_DEVICE_INFO);
        registerReceiver(mReceiver, intentFilter);
        initView();
        initEvent();
        initData();
        if (DeviceInfo.CaMenu.menci.value().equals(deviceInfo.getCa()) || DeviceInfo.CaMenu.yangan.value().equals(deviceInfo.getCa())) {//门磁、烟感
            initListView();
        }
    }

    private void initData() {
        if (DeviceInfo.CaMenu.menci.value().equals(deviceInfo.getCa()) || DeviceInfo.CaMenu.yangan.value().equals(deviceInfo.getCa())) {//门磁、烟感
            message = deviceInfo.getAcceptMessage() == 0;
            list_layout.setVisibility(View.VISIBLE);
            if (DeviceInfo.CaMenu.menci.value().equals(deviceInfo.getCa())) {
                imageView.setImageResource(R.drawable.zhzj_shebei_meici);
            } else {
                imageView.setImageResource(R.drawable.zhzj_shebei_tantou);
            }
            changeAcceptMessage();
        } else if (DeviceInfo.CaMenu.cazuo.value().equals(deviceInfo.getCa())) {//排插
            dr = deviceInfo.getDr();
            turn = (dr == 1);
            cf_layout.setVisibility(View.INVISIBLE);
            switch_layout.setVisibility(View.VISIBLE);
            tv_cf.setText(getString(R.string.security_ds));
            icon_cf.setImageResource(R.drawable.zhzj_shebei_dingshi);
            imageView.setImageResource(R.drawable.zhzj_shebei_paicha);
            changeDefence(turn);
//            if (turn){
//                switch_on.setChecked(true);
//            }else {
//                switch_off.setChecked(true);
//            }
        }
        if (deviceInfo.getSort() == 0) {
            operationSort = "0";
        } else {
            operationSort = "1";
        }
        info_name.setText(deviceInfo.getName());
        tv_title.setText(deviceInfo.getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    /**
     * 排插
     */
    private void changeDefence(boolean flag) {
        if (!flag) { //关
            turn = false;
            switch_off.setChecked(true);
            device_msg.setText(deviceInfo.getName() + getString(R.string.security_close));
        } else {  //开
            turn = true;
            switch_on.setChecked(true);
            device_msg.setText(deviceInfo.getName() + getString(R.string.security_open));
        }
    }

    private boolean message;

    /**
     * 设防、撤防更改
     */
    private void changeAcceptMessage() {
        if (deviceInfo.getAcceptMessage() == 0) { //撤防
            tv_cf.setText(getString(R.string.zss_main_chefang));
            icon_cf.setImageResource(R.drawable.zhzj_shebei_chefang);
            device_msg.setText(deviceInfo.getName() + getString(R.string.security_cf));
        } else {  //设防
            tv_cf.setText(getString(R.string.zss_main_shefang));
            icon_cf.setImageResource(R.drawable.zhzj_shebei_shefang);
            device_msg.setText(deviceInfo.getName() + getString(R.string.security_sf));
        }
    }

    private void initEvent() {
        device_history.setOnClickListener(this);
        cf_layout.setOnClickListener(this);
        info_layout.setOnClickListener(this);
        switch_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (turn) {
                    turn = false;
                    sendCommod(turn);
                }
            }
        });
        switch_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!turn) {
                    turn = true;
                    sendCommod(turn);
                }
            }
        });

    }


    private void initView() {
        device_history = (ImageView) findViewById(R.id.device_history);
        icon_cf = (ImageView) findViewById(R.id.icon_cf);
        icon_info = (ImageView) findViewById(R.id.icon_info);
        imageView = (ImageView) findViewById(R.id.imageView);

        cf_layout = (LinearLayout) findViewById(R.id.cf_layout);
        info_layout = (LinearLayout) findViewById(R.id.info_layout);
        list_layout = (LinearLayout) findViewById(R.id.list_layout);
        switch_layout = (LinearLayout) findViewById(R.id.switch_layout);

        tv_cf = (TextView) findViewById(R.id.tv_cf);
        tv_info = (TextView) findViewById(R.id.tv_info);
        info_name = (TextView) findViewById(R.id.info_name);
        device_msg = (TextView) findViewById(R.id.device_msg);
        tv_title = (TextView) findViewById(R.id.tv_title);
        device_switch = (RadioGroup) findViewById(R.id.device_switch);
        switch_off = (RadioButton) findViewById(R.id.switch_off);
        switch_on = (RadioButton) findViewById(R.id.switch_on);
        security_list = (MyGridView) findViewById(R.id.security_list);
    }

    public void initListView() {
        sceneAdapter = new SceneAdapter(sceneList, this);
        security_list.setAdapter(sceneAdapter);
        JavaThreadPool.getInstance().excute(new ScenesLoad());
        security_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FoundScene(sceneList.get(position).getId());
            }
        });
    }

    public void back(View view) {
        if (!isPlay) {
            finish();
        } else {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                T.showShort(mContext, R.string.press_again_monitor);
                exitTime = System.currentTimeMillis();
            } else {
                reject();
            }
        }

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.device_history:
                Intent intent = toNextActivity();
                intent.setClass(this, DeviceCommandHistoryActivity.class);
                startActivity(intent);
                if (isPlay) finish();
                break;
            case R.id.cf_layout:
                if (deviceInfo.isFa()){
                    Toast.makeText(mContext,getString(R.string.activity_securityInfo_ctivity_24_device),Toast.LENGTH_SHORT).show();
                    return;
                }
                sendCommod(deviceInfo.getAcceptMessage() == 0);
                break;
            case R.id.info_layout:
                Intent setintent = toNextActivity();
                setintent.setClass(this, SetDeviceInfoActivity.class);
                startActivity(setintent);
                if (isPlay) finish();
                break;
        }
    }

    String operationSort;

    public void sendCommod(boolean flag) {
        if (Util.isFastClick()) {
            Toast.makeText(mContext, getString(R.string.activity_devices_commandhistory_tip), Toast.LENGTH_SHORT).show();
        } else {
            SyncMessage message = new SyncMessage();
            message.setCommand(SyncMessage.CommandMenu.rq_control.value());
            message.setDeviceid(deviceInfo.getId());
            if (flag) { // 开操作
                // 这里的操作写死，需要在服务器的配置页面中配置此按钮对应的指令是什么
                // 开操作
                Log.e("aaa", "发送通知===开指令");
                if (DeviceInfo.CaMenu.menci.value().equals(deviceInfo.getCa()) || DeviceInfo.CaMenu.yangan.value().equals(deviceInfo.getCa())) {//门磁、烟感
                    message.setSyncBytes(new byte[]{0x02});
                } else {
                    message.setSyncBytes(new byte[]{0x01});
                }
                operationSort = "1";
            } else {
                // 关操作
                Log.e("aaa", "发送通知===关指令");
                message.setSyncBytes(new byte[]{0x00});
                operationSort = "0";
            }

            // 点击后显示进度条
            showInProgress(getString(R.string.operationing), false, true);
            defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 8 * 1000);
            SyncMessageContainer.getInstance().produceSendMessage(message);
        }
    }


    public Intent toNextActivity() {
        Intent intent = new Intent();
        intent.putExtra("device", deviceInfo);
        if (isPlay) intent.putExtra("contact", mContact);
        intent.putExtra("action", "com.android.activity.SECURITYINFO");
        return intent;
    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case dHandler_timeout:
                    cancelInProgress();
                    break;
                case dHandler_scenes:
                    List<FoundInfo> sceneInfos = (List<FoundInfo>) msg.obj;
                    sceneList.clear();
                    sceneList.addAll(sceneInfos);
                    for (FoundInfo sceneInfo : sceneList) {
                        Log.e("TAG_!!!", sceneInfo.toString());
                    }
                    sceneAdapter.notifyDataSetChanged();
                    break;
                case dHandler_scene:
                    if (result == null || "".equals(result)) {
                        cancelInProgress();
                        Toast.makeText(SecurityInfoActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    resultStr = JSON.parseObject(result, FoundInfo.class);
                    resultStr.setTip(1);
                    Intent intent = new Intent();
                    intent.putExtra("result", resultStr);
                    switch (resultStr.getType()) {
                        case 0:
                            intent.setClass(getApplicationContext(), CustomSceneActivity.class);
                            break;
                        case 1:
                            intent.setClass(getApplicationContext(), TimingSceneActivity.class);
                            break;
                        case 2:
                            intent.setClass(getApplicationContext(), LinkageSceneActivity.class);
                            break;
                    }
                    intent.putExtra("edit", false);
                    startActivity(intent);
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    public void FoundScene(final long cid) {
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("id", cid);
                result = HttpRequestUtils
                        .requestoOkHttpPost(server + "/jdm/s3/scenes/get", pJsonObject, SecurityInfoActivity.this);
                if ("-3".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(SecurityInfoActivity.this, getString(R.string.device_not_getdata),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (result != null && result.length() > 3) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            defaultHandler.sendEmptyMessage(dHandler_scene);
                        }
                    });
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    class ScenesLoad implements Runnable {

        public ScenesLoad() {
        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", deviceInfo.getId());
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/scenes/qlscenes", object, SecurityInfoActivity.this);
            List<FoundInfo> sceneInfos = new ArrayList<FoundInfo>();
            if (!StringUtils.isEmpty(result) && result.startsWith("[")) {
                JSONArray ll = null;
                try {
                    ll = JSON.parseArray(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                }
                if (ll == null) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(SecurityInfoActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                for (int j = 0; j < ll.size(); j++) {
                    JSONObject jsonObject = (JSONObject) ll.get(j);
                    FoundInfo resultStr = JSON.parseObject(jsonObject.toString(), FoundInfo.class);
                    sceneInfos.add(resultStr);
                }
            }
            Message m = defaultHandler.obtainMessage(dHandler_scenes);
            m.obj = sceneInfos;
            defaultHandler.sendMessage(m);
        }
    }


    private String deviceId;
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            String device_id = intent.getStringExtra("device_id");
            if (intent.getAction().equals(FRUSH_DEVICE_INFO)) {
                DeviceInfo info = (DeviceInfo) intent.getSerializableExtra("device");
                if (info != null) {
                    deviceInfo = info;
                }
                initData();
            } else if (Actions.ACCETP_ONEDEVICE_MESSAGE.equals(intent.getAction())) { // 某一个设备的推送广播
                if (mProgressDialog != null && mProgressDialog.isShowing())
                    cancelInProgress();
                deviceId = intent.getStringExtra("device_id");
                if (device_id == null || !device_id.equals(String.valueOf(deviceInfo.getId()))) {
                    return;
                }
                if (DeviceInfo.CaMenu.menci.value().equals(deviceInfo.getCa()) || DeviceInfo.CaMenu.yangan.value().equals(deviceInfo.getCa())) {//门磁、烟感
//                    message true 撤防 false设防
                    DeviceInfo deviceInfo1 = DatabaseOperator.getInstance(mContext).queryDeviceInfo(deviceInfo.getId());
                    if (deviceInfo1 != null) {
                        deviceInfo = deviceInfo1;
                    }
                    changeAcceptMessage();
                }
                String data = (String) intent.getSerializableExtra("device_info");
                if (data != null) {
                    try {
                        JSONObject object = JSONObject.parseObject(data);
                        if (object.getString("sort") != null) {
                            if (object.getString("sort").equals("0")) {
                                operationSort = "0";
                            } else if (object.getString("sort").equals("1")) {
                                operationSort = "1";
                            }
                            changeDefence(operationSort.equals("1"));
                        }
                        long uid = dcsp.getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
                        if (object.getString("send") != null && object.getString("send").equals(String.valueOf(uid))) {
                            cancelInProgress();
                            defaultHandler.removeMessages(dHandler_timeout);
                            Toast.makeText(mContext, getString(R.string.rq_control_sendsuccess), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, "excetipn:" + ex.toString());
                        //防止json无数据崩溃
                    }
                }
            } else if (Actions.SHOW_SERVER_MESSAGE.equals(intent.getAction())) { // 显示服务器信息
                cancelInProgress();
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(intent.getStringExtra("message"));
                } catch (Exception e) {
                    Log.w("DevicesList", "获取服务器返回消息，转换为json对象失败，用原始值处理");
                }
                if (resultJson != null) {
                    changeDefence(!turn);
                    switch (resultJson.getIntValue("Code")) {
                        case 4:
                            Toast.makeText(mContext, getString(R.string.tips_4), Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            Toast.makeText(mContext, getString(R.string.tips_5), Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(mContext, getString(R.string.tips_6), Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(mContext, getString(R.string.tips_7), Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            Toast.makeText(mContext, getString(R.string.tips_8), Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            Toast.makeText(mContext, "Unknown Info", Toast.LENGTH_SHORT).show();
                            break;
                    }

                } else {
                    Toast.makeText(mContext, intent.getStringExtra("message"), Toast.LENGTH_SHORT)
                            .show();

                }
            }


        }
    };
}
