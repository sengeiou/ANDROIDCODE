package com.smartism.znzk.activity.yaokan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.GetAndDecodeMapString;
import com.smartism.znzk.util.LanguageUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by win7 on 2017/3/1.
 */

public class YKElectricFanMainActivity extends ActivityParentActivity implements View.OnClickListener {

    private static final int SEND_CODE = 1;
    private Context mContext;
    private long did;
    private String ctrlId;
    private HashMap codeData;
    private TextView tv_type;
    private Button btn_yaotou, btn_speedtype, btn_clock, btn_back;
    private ImageView iv_speed, iv_power;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_CODE:
                    if (progressIsShowing()) {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    private String type, brand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ykfan_main);
        mContext = this;
        initView();
        initData();
    }

    private void initData() {
        Bundle mBundel = this.getIntent().getExtras();
        did = mBundel.getLong("did");
        ctrlId = mBundel.getString("ctrlId");
        type = getIntent().getStringExtra("type");
        brand = getIntent().getStringExtra("brand");
        tv_type.setText(brand + " " + type);
        // 获取本地存储此遥控器的码库
        String mapString = Util.readYKCodeFromFile(brand+type);
        codeData = new GetAndDecodeMapString().getMap(mapString);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String deviceId = (String) intent.getSerializableExtra("device_id");
            if (intent.getAction().equals(Actions.ACCETP_ONEDEVICE_MESSAGE)) {
                if (deviceId != null && deviceId.equals(String.valueOf(did))) {
                    if (mHandler.hasMessages(SEND_CODE)) {
                        mHandler.removeMessages(SEND_CODE);
                    }
                    if (progressIsShowing()) {
                        cancelInProgress();
                        Toast.makeText(YKElectricFanMainActivity.this, getString(R.string.rq_control_sendsuccess), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
    };

    private void initView() {
        btn_yaotou = (Button) findViewById(R.id.btn_yaotou);
        btn_speedtype = (Button) findViewById(R.id.btn_speedtype);
        btn_clock = (Button) findViewById(R.id.btn_clock);
        iv_power = (ImageView) findViewById(R.id.iv_power);
        iv_speed = (ImageView) findViewById(R.id.iv_fengsu);
        if (!LanguageUtil.isZh(this)) {
            iv_power.setImageResource(R.drawable.fan_on);
            iv_speed.setImageResource(R.drawable.fan_speed);
        }
        btn_back = (Button) findViewById(R.id.btn_back);

        tv_type = (TextView) findViewById(R.id.tv_type);

        btn_yaotou.setOnClickListener(this);
        btn_speedtype.setOnClickListener(this);
        btn_clock.setOnClickListener(this);
        iv_power.setOnClickListener(this);
        iv_speed.setOnClickListener(this);
        btn_back.setOnClickListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        registerReceiver(receiver, filter);
    }

    public void sendCommand(String controlName) {

        JSONObject object = new JSONObject();
        object.put("name", controlName);
        object.put("code", codeData.get(controlName));
        if (codeData.get(controlName) == null) {
            Toast.makeText(YKElectricFanMainActivity.this, getString(R.string.hwzf_mode_not_supply), Toast.LENGTH_SHORT).show();
            return;
        }
        showInProgress(getString(R.string.loading), false, true);
        SyncMessage message1 = new SyncMessage();
        message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
        message1.setDeviceid(did);// 红外转发器的ID
        try {
            message1.setSyncBytes(object.toJSONString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        SyncMessageContainer.getInstance().produceSendMessage(message1);
        mHandler.sendEmptyMessageDelayed(SEND_CODE, 8 * 1000);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_yaotou:
                sendCommand("oscillation");
                break;
            case R.id.btn_speedtype:
                sendCommand("mode");
                break;
            case R.id.btn_clock:
                sendCommand("timer");
                break;
            case R.id.iv_power:
                if (codeData.get("poweroff") != null) {
                    sendCommand("poweroff");
                } else {
                    sendCommand("power");
                }
                break;
            case R.id.iv_fengsu:
                if (codeData.get("fanspeed") != null) {
                    sendCommand("fanspeed");
                } else {
                    sendCommand("power");
                }
                break;
            case R.id.btn_back:
                finish();
                break;
        }

    }
}
