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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.DeviceTimingListActivity;
import com.smartism.znzk.activity.device.share.ShareDevicesActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.NetworkUtils;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;

import java.util.List;

/**
 * WIFI排插
 */
public class PlatoonActivity extends ActivityParentActivity implements View.OnClickListener {

    public static final String FRUSH_DEVICE_INFO = MainApplication.app.getPackageName() + "FRUSH_DEVICE_INFO";
    private DeviceInfo deviceInfo;
    private final int dHandler_timeout = 0, getdHandler_loadAllcommend = 2;
    boolean turn;


    private LinearLayout ll_power_on,ll_power_off, ll_timing, ll_info;

    private ImageView iv_power, iv_kg_status, iv_gz_status,iv_share;
    private TextView tv_power, tv_title, tv_gz_status, tv_kg_status;

    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_djkzq);
        mContext = this;
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        initView();
        initEvent();
        initData();
    }

    private void initData() {
        tv_title.setText(deviceInfo.getName());
        refreshDeviceStatus();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }


    private void changeDefence(boolean flag) {
        cancelInProgress();
        turn = flag;
//        iv_power.setImageResource(flag ? R.drawable.zhzj_power_on : R.drawable.zhzj_power_off);
//        tv_power.setText(flag ? getString(R.string.security_device_open) : getString(R.string.security_device_close));
        iv_kg_status.setImageResource(flag ? R.drawable.zhzj_power_on_state : R.drawable.zhzj_power_off_state);
        tv_kg_status.setText(flag ? getString(R.string.security_device_open) : getString(R.string.security_device_close));
    }

    private void initEvent() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        intentFilter.addAction(Actions.SHOW_SERVER_MESSAGE);
        intentFilter.addAction(FRUSH_DEVICE_INFO);
        registerReceiver(mReceiver, intentFilter);
    }


    private void initView() {
        ll_power_on = (LinearLayout) findViewById(R.id.ll_power_on);
        ll_power_off = (LinearLayout) findViewById(R.id.ll_power_off);
        ll_info = (LinearLayout) findViewById(R.id.ll_info);
        ll_timing = (LinearLayout) findViewById(R.id.ll_timing);


        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_power = (TextView) findViewById(R.id.tv_power);
        tv_gz_status = (TextView) findViewById(R.id.tv_gz_status);
        tv_kg_status = (TextView) findViewById(R.id.tv_kg_status);


        ll_power_on.setOnClickListener(this);
        ll_power_off.setOnClickListener(this);
        ll_info.setOnClickListener(this);
        ll_timing.setOnClickListener(this);


        iv_power = (ImageView) findViewById(R.id.iv_power);
        iv_kg_status = (ImageView) findViewById(R.id.iv_kg_status);
        iv_gz_status = (ImageView) findViewById(R.id.iv_gz_status);
        iv_share = (ImageView) findViewById(R.id.iv_share);
        iv_share.setOnClickListener(this);


    }

    public void back(View view) {
        finish();
    }

    @Override
    public void onClick(View v) {
        Intent definedIntent = new Intent();
        switch (v.getId()) {
            case R.id.iv_share:
                definedIntent.putExtra("pattern", "status_forver");
                definedIntent.putExtra("shareid", deviceInfo.getId());
                definedIntent.setClass(getApplicationContext(), ShareDevicesActivity.class);
                startActivity(definedIntent);
                break;
            case R.id.ll_power_on:
                if (!NetworkUtils.CheckNetwork(mContext)){
                    Toast.makeText(mContext, getString(R.string.net_error_nonet), Toast.LENGTH_LONG).show();
                    return;
                }
                sendCommod(true);
                break;
            case R.id.ll_power_off:
                if (!NetworkUtils.CheckNetwork(mContext)){
                    Toast.makeText(mContext, getString(R.string.net_error_nonet), Toast.LENGTH_LONG).show();
                    return;
                }
//                sendCommod(turn);
                sendCommod(false);
                break;
            case R.id.ll_info:
                Intent setintent = new Intent();
                setintent.setClass(this, SetDeviceInfoActivity.class);
                setintent.putExtra("isMainList", true);
                setintent.putExtra("device", deviceInfo);
                startActivity(setintent);
                break;
            case R.id.ll_timing:
                definedIntent.setClass(this, DeviceTimingListActivity.class);
                definedIntent.putExtra("device", deviceInfo);
                startActivity(definedIntent);

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
                message.setSyncBytes(new byte[]{0x01});
                operationSort = "1";
            } else {
                // 关操作
                message.setSyncBytes(new byte[]{0x00});
                operationSort = "0";
            }

            // 点击后显示进度条
            showInProgress(getString(R.string.operationing), false, false);
            defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 8 * 1000);
            SyncMessageContainer.getInstance().produceSendMessage(message);
        }
    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case dHandler_timeout:
                    cancelInProgress();
                    break;
                case getdHandler_loadAllcommend:
                    List<CommandInfo> commandInfos = (List<CommandInfo>) msg.obj;
                    if (commandInfos != null && !commandInfos.isEmpty()) {
                        for (CommandInfo command :
                                commandInfos) {
                            if ("95".equals(command.getCtype())) {//开关状态 0关闭 1打开
                                changeDefence("0".equals(command.getCommand()) ? false : true);
                            } else if ("96".equals(command.getCtype())) {//异常状态 1异常
                                tv_gz_status.setText("0".equals(command.getCommand()) ? getString(R.string.qwq_battry_normal)
                                        : getString(R.string.zss_item_exception));
                                iv_gz_status.setImageResource("0".equals(command.getCommand()) ? R.drawable.zhzj_normal :
                                        R.drawable.zhzj_abnormal);
                            }
//                            else if ("0".equals(command.getCtype())){
//                                changeDefence("关".equals(command.getCommand()) ? false : true);
//                            }
                        }
                    }
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    private void refreshDeviceStatus() {
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                Message m = defaultHandler.obtainMessage(getdHandler_loadAllcommend);
                m.obj = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());
                defaultHandler.sendMessage(m);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(FRUSH_DEVICE_INFO)) {
                DeviceInfo info = (DeviceInfo) intent.getSerializableExtra("device");
                if (info != null) {
                    deviceInfo = info;
                }
                initData();
            } else if (Actions.ACCETP_ONEDEVICE_MESSAGE.equals(intent.getAction())) { // 某一个设备的推送广播
                if (intent.getStringExtra("zhuji_id") != null) {//主机收到的数据
                    if (deviceInfo != null && intent.getStringExtra("zhuji_id") != null && deviceInfo.getId() == Long.parseLong(intent.getStringExtra("zhuji_id"))) {
                        String data = (String) intent.getSerializableExtra("zhuji_info");
                        if (data != null) {
                            try {
                                JSONObject object = JSONObject.parseObject(data);
                                if (operationSort != null && operationSort.equals(object.getString("sort")) && progressIsShowing()) {
                                    Toast.makeText(mContext, getString(R.string.rq_control_sendsuccess),
                                            Toast.LENGTH_SHORT).show();
                                    cancelInProgress();
                                    defaultHandler.removeMessages(dHandler_timeout);
                                }
                            } catch (Exception ex) {
                                //防止json无数据崩溃
                            }
                        }
                    }
                    refreshDeviceStatus();
                } else if (intent.getStringExtra("device_id") != null) {
                    String old_refulsh_device_id = intent.getStringExtra("device_id");
                    if (deviceInfo != null && intent.getStringExtra("device_id") != null && deviceInfo.getId() == Long.parseLong(intent.getStringExtra("device_id"))) {
                        String data = (String) intent.getSerializableExtra("device_info");
                        if (data != null) {
                            try {
                                JSONObject object = JSONObject.parseObject(data);
                                if (operationSort != null && operationSort.equals(object.getString("sort")) && progressIsShowing()) {
                                    Toast.makeText(mContext, getString(R.string.rq_control_sendsuccess),
                                            Toast.LENGTH_SHORT).show();
                                    cancelInProgress();
                                    defaultHandler.removeMessages(dHandler_timeout);
                                }
                            } catch (Exception ex) {
                                //防止json无数据崩溃
                            }
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
        }
    };
}

