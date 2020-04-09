package com.smartism.znzk.activity.device;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.user.GCodeListActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.alertview.AlertView;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceUpdatetGSMPhoneActivity extends ActivityParentActivity implements View.OnClickListener {
    private final int mHandleWhat_15 = 15;
    private long telDBId;
    private String telNumber;
    private int typeWay;
    private EditText phone;
    private DeviceInfo operationDevice;
    private ImageView addBtn;
    private LinearLayout mLayout;
    private int type;
    private TextView txt_gsmnumber, title;
    private ImageView back_btn;
    private LinearLayout gcode_ly, ll_code,ll_typeway;
    private ImageView gcode_icon;
    private TextView gcode_aname;
    private String country = "0086";
    private static final int TIME_OUT = 100;
    private TimerTask timerTask;
    private Timer timer;
    private int waitTime = 60;
    private TextView sendCodeBtn,sendVoiceCodeBtn;
    private long starttime;
    private EditText code;
    private Context mContext;
    private CheckBox gsmTypewaySms,gsmTypewayCall;
    private Map<String,String> zhujiSetInfos = new HashMap<>();//主机设置信息

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 10: // 修改完成
                    cancelInProgress();
                    sendBroadcast(new Intent(Actions.REFRESH_DEVICES_LIST));
                    Toast.makeText(DeviceUpdatetGSMPhoneActivity.this, getString(R.string.device_set_tip_success),
                            Toast.LENGTH_LONG).show();
                    finish();
                    break;
                case 12:
                    waitTime--;
                    sendCodeBtn.setText(getString(R.string.register_emailcodewait) + "(" + waitTime + ")");
                    sendVoiceCodeBtn.setText(getString(R.string.register_emailcodewait_voice) + "(" + waitTime + ")");

                    if (waitTime <= 1) {
                        starttime = 0;
                        defaultHandler.sendEmptyMessageDelayed(11, 1000);
                    } else {
                        defaultHandler.sendEmptyMessageDelayed(12, 1000);
                    }
                    break;
                case 11:
                    sendCodeBtn.setText(getString(R.string.register_emailcodesend));
                    sendVoiceCodeBtn.setText(getString(R.string.register_voicecodesend));
                    waitTime = 60;

                    if (Util.isMobileNO(phone.getText().toString())) {
                        sendCodeBtn.setEnabled(true);
                        sendVoiceCodeBtn.setEnabled(true);
                    } else {
                        sendCodeBtn.setEnabled(false);
                        sendVoiceCodeBtn.setEnabled(false);
                    }
                    break;
                case 14:
                    cancelInProgress();
                    if (defaultHandler.hasMessages(TIME_OUT)) {
                        defaultHandler.removeMessages(TIME_OUT);
                    }
                    /*
                    //和ios保持一致，toast一下就行了
                    new AlertView(getString(R.string.tips), getString(R.string.register_phone_codesendsuccess), null, new String[]{getString(R.string.sure)}, null, mContext, AlertView.Style.Alert, null).show();
                     */
                    Toast toast = Toast.makeText(mContext,"",Toast.LENGTH_LONG);
                    toast.setText(R.string.register_codesendtip);
                    toast.show();
                    starttime = System.currentTimeMillis();
                    dcsp.putLong(DataCenterSharedPreferences.Constant.CODE_START_TIME, starttime).commit();
                    sendCodeBtn.setEnabled(false);
                    sendVoiceCodeBtn.setEnabled(false);
                    defaultHandler.sendEmptyMessage(12);
                    break;
                case mHandleWhat_15://数据库加载完成
                    if ("1".equalsIgnoreCase(zhujiSetInfos.get(ZhujiInfo.GNSetNameMenu.supportGSMTypeWay.value())) && type == 0){
                        ll_typeway.setVisibility(View.VISIBLE);
                    }
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_device_add_gsm);
        phone = (EditText) findViewById(R.id.setgsm_phone_edit);
        code = (EditText) findViewById(R.id.register_email_code);
        txt_gsmnumber = (TextView) findViewById(R.id.txt_gsmnumber);
        ll_typeway = (LinearLayout) findViewById(R.id.ll_typeway);
        gsmTypewaySms = (CheckBox) findViewById(R.id.gsm_typeway_sms);
        gsmTypewayCall = (CheckBox) findViewById(R.id.gsm_typeway_call);
        sendCodeBtn = (TextView) findViewById(R.id.register_code_btn);
        sendCodeBtn.setOnClickListener(this);
        sendVoiceCodeBtn = (TextView) findViewById(R.id.register_voicecode_btn);
        sendVoiceCodeBtn.setOnClickListener(this);
        title = (TextView) findViewById(R.id.title);
        back_btn = (ImageView) findViewById(R.id.back_btn);
        telDBId = getIntent().getLongExtra("id", 0);
        telNumber = getIntent().getStringExtra("tel");
        type = getIntent().getIntExtra("type", 0);
        country = getIntent().getStringExtra("country");
        typeWay = getIntent().getIntExtra("typeWay",0);
        if ((typeWay&1) != 0){
            gsmTypewaySms.setChecked(true);
        }
        if ((typeWay&2) != 0){
            gsmTypewayCall.setChecked(true);
        }
        starttime = dcsp.getLong(DataCenterSharedPreferences.Constant.CODE_START_TIME, 0);
        ll_code = (LinearLayout) findViewById(R.id.ll_code);
        int time = 60 - (int) ((System.currentTimeMillis() - starttime) / 1000);
        if (starttime > 0 && time > 0) {
            waitTime = time;
            if (waitTime > 0) {
                starttime = System.currentTimeMillis();
                defaultHandler.sendEmptyMessageDelayed(12, 1000);
            }
        }
        if (country == null) {
            country = "0086";
        }
        if (MainApplication.app.getAppGlobalConfig().isSuportVoice() && type == 1){
            findViewById(R.id.voice_layout).setVisibility(View.VISIBLE);
        }
        if (type == 1) {//电话
            title.setText(getString(R.string.activity_device_setgsm_phone_tip));
            txt_gsmnumber.setText(getString(R.string.activity_device_setgsm_phonenumber));
            phone.setHint(getString(R.string.activity_device_setgsm_phone_tip));
        } else {
            ll_code.setVisibility(View.GONE);
        }
        if (telNumber != null) {
            phone.setText(telNumber);
        }
        addBtn = (ImageView) findViewById(R.id.addTel);
        addBtn.setVisibility(View.GONE);
        mLayout = (LinearLayout) findViewById(R.id.setgsm_edit_layout);
        mLayout.setVisibility(View.VISIBLE);
        gcode_ly = (LinearLayout) findViewById(R.id.gcode_ly);

        gcode_icon = (ImageView) findViewById(R.id.gcode_icon);
        gcode_aname = (TextView) findViewById(R.id.gcode_aname);
        String gcodeInfo = dcsp.getString(DataCenterSharedPreferences.Constant.LOCALE_GCODE,"");
        if (!StringUtils.isEmpty(gcodeInfo)){
            try{
                JSONObject object = JSON.parseObject(gcodeInfo);
                ImageLoader.getInstance().displayImage(object.getString("icon"), gcode_icon);
                gcode_aname.setText(object.getString("aname"));
                country = object.getString("country");
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        gcode_ly.setOnClickListener(this);
        operationDevice = (DeviceInfo) getIntent().getSerializableExtra("device");
        if (operationDevice == null) {
            Toast.makeText(DeviceUpdatetGSMPhoneActivity.this, getString(R.string.device_set_tip_nopro), Toast.LENGTH_LONG)
                    .show();
            finish();
        }

        zhujiInfo = DatabaseOperator.getInstance(this).queryDeviceZhuJiInfo(operationDevice.getId());
        initCountryCode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        JavaThreadPool.getInstance().excute(new InitDeviceSetThread());
    }

    private ZhujiInfo zhujiInfo;

    public void sendCodeToPhone(final int t,final boolean voice) {
        if (waitTime < 60)
            return;
        final String account = phone.getText().toString();
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(mContext, getString(R.string.register_tip_phone_empty), Toast.LENGTH_SHORT).show();
            return;
        }
/*        if ("0086".equals(country) && !Util.isMobileNO(account)) {
            Toast.makeText(mContext, getString(R.string.register_tip_phone_error), Toast.LENGTH_LONG).show();
            return;
        }*/
        defaultHandler.sendEmptyMessageDelayed(TIME_OUT, 10 * 1000);
        showInProgress(getString(R.string.submiting), false, true);
        final String language = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("mobile", account);
                pJsonObject.put("conntry", country);
                pJsonObject.put("lang", language);
                pJsonObject.put("voice", voice);
                pJsonObject.put("t", t);
                if (t == 6) {
                    pJsonObject.put("did", zhujiInfo.getId());
                    pJsonObject.put("type", 1);
                }
                String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/sms/sendcode", pJsonObject, DeviceUpdatetGSMPhoneActivity.this);
                if ("0".equals(result)) {
                    defaultHandler.sendEmptyMessage(14);
                } else if ("-3".equals(result)) {
                    if (defaultHandler.hasMessages(TIME_OUT)) {
                        defaultHandler.removeMessages(TIME_OUT);
                    }
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();

                            Toast.makeText(mContext, getString(R.string.register_tip_phone_error), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-4".equals(result)) {
                    if (defaultHandler.hasMessages(TIME_OUT)) {
                        defaultHandler.removeMessages(TIME_OUT);
                    }
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.register_tip_phone_isin), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-5".equals(result)) {
                    if (defaultHandler.hasMessages(TIME_OUT)) {
                        defaultHandler.removeMessages(TIME_OUT);
                    }
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.register_tip_phone_send_filde), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-6".equals(result)) {
                    if (defaultHandler.hasMessages(TIME_OUT)) {
                        defaultHandler.removeMessages(TIME_OUT);
                    }
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.register_operate_many), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-7".equals(result)) {
                    if (defaultHandler.hasMessages(TIME_OUT)) {
                        defaultHandler.removeMessages(TIME_OUT);
                    }
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.register_operate_limit), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-8".equals(result)) {
                    if (defaultHandler.hasMessages(TIME_OUT)) {
                        defaultHandler.removeMessages(TIME_OUT);
                    }
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.activity_phone_number_isexit), Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    if (defaultHandler.hasMessages(TIME_OUT)) {
                        defaultHandler.removeMessages(TIME_OUT);
                    }
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.net_error_servererror), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }


    public void back(View v) {
        finish();
    }

    public void subToUpdate(View v) {
        if (TextUtils.isEmpty(phone.getText().toString().trim())) {
            Toast.makeText(mContext, getString(R.string.activity_device_setcall_phone_notice), Toast.LENGTH_SHORT).show();
            return;
        }
        if (type != 0 && TextUtils.isEmpty(code.getText().toString().trim())) {
            Toast.makeText(mContext, getString(R.string.input_vf_code), Toast.LENGTH_SHORT).show();
            return;
        }
        showInProgress(getString(R.string.device_set_tip_inupdate), false, true);
        JavaThreadPool.getInstance().excute(new UpdateInfoThread());
    }

    protected void onDestroy() {
        defaultHandler.removeCallbacksAndMessages(null);
        defaultHandler = null;
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gcode_ly://选择国家
                Intent intent = new Intent();
                intent.setClass(mContext.getApplicationContext(), GCodeListActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.register_code_btn:
                if (zhujiInfo == null)
                    return;
                sendCodeToPhone(6,false);
                break;
            case R.id.register_voicecode_btn:
                if (zhujiInfo == null)
                    return;
                sendCodeToPhone(6,true);
                break;
            default:
                break;
        }
    }

    private void initCountryCode() {
        if (!"0086".equals(country)) {
            showInProgress(getString(R.string.submiting), false, true);
            JavaThreadPool.getInstance().excute(new Runnable() {
                @Override
                public void run() {
                    String server = dcsp.getString(
                            DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                    JSONObject object = new JSONObject();
                    object.put("country", country);
                    String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/sms/gcodeinfo", object, DeviceUpdatetGSMPhoneActivity.this);
                    if (!StringUtils.isEmpty(result) && result.length() > 4) {
                        try {
                            final JSONObject o = JSON.parseObject(result);
                            defaultHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    cancelInProgress();
                                    ImageLoader.getInstance().displayImage(o.getString("icon"), gcode_icon);
                                    gcode_aname.setText(o.getString("aname"));
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        defaultHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                cancelInProgress();
                                Toast.makeText(mContext, getString(R.string.net_error_requestfailed), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }

    ;

    class UpdateInfoThread implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", operationDevice.getId());
            if (!phone.getText().toString().equals("")) {
                object.put("tel", phone.getText().toString());
            }
            object.put("contry", country);
            String url = "";
            Log.d(TAG, "id:" + telDBId);
            if (telDBId != 0) {
                object.put("id", telDBId);
                url = "update";
            } else {
                url = "add";
                object.put("type", type);
            }
            if (type == 1) {
                object.put("code", code.getText().toString().trim());
            }
            int typeway = 0b00;
            if (gsmTypewaySms.isChecked()){
                typeway += 0b01;
            }
            if (gsmTypewayCall.isChecked()){
                typeway += 0b10;
            }
            object.put("typeWay",typeway);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/gsmtel/" + url, object, DeviceUpdatetGSMPhoneActivity.this);
            if ("0".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceUpdatetGSMPhoneActivity.this, getString(R.string.success), Toast.LENGTH_LONG)
                                .show();
                        finish();
                    }
                });
            } else if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceUpdatetGSMPhoneActivity.this, getString(R.string.activity_device_setgsm_errorgly),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-4".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceUpdatetGSMPhoneActivity.this, getString(R.string.net_error_operationfailed),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-5".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceUpdatetGSMPhoneActivity.this, getString(R.string.activity_device_setgsm_notgsm),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-7".equals(result)) {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceUpdatetGSMPhoneActivity.this, getString(R.string.activity_phone_verificationcode_error), Toast.LENGTH_SHORT).show();
                    }
                });
            } else if ("-8".equals(result)) {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceUpdatetGSMPhoneActivity.this, getString(R.string.activity_phone_number_isexit), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceUpdatetGSMPhoneActivity.this, getString(R.string.net_error),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 11) {
            ImageLoader.getInstance().displayImage(data.getStringExtra("icon"), gcode_icon);
            gcode_aname.setText(data.getStringExtra("aname"));
            country = data.getStringExtra("country");
        }
    }

    class InitDeviceSetThread implements Runnable {
        @Override
        public void run() {
            zhujiSetInfos = DatabaseOperator.getInstance().queryZhujiSets(operationDevice.getId());
            defaultHandler.sendEmptyMessage(mHandleWhat_15);
        }
    }

}
