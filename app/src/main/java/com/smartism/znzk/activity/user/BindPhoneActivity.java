package com.smartism.znzk.activity.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.alertview.AlertView;

import org.apache.commons.lang.StringUtils;

import java.util.Locale;


public class BindPhoneActivity extends ActivityParentActivity implements View.OnClickListener {
    private TextView tv_title, register_code_btn,register_voicecode_btn, tv_ignore;
    private EditText et_phone, et_code;
    private LinearLayout gcode_ly;

    private ImageView gcode_icon;
    private TextView gcode_aname;
    private String country = "0086";

    public static final int TIME_OUT = 100;
    private int waitTime = 60;
    private Context mContext;
    private long startTime;
    private Button btn_sure;
    private int bindType;
    private String number;

    private EditText pass_edit, pass_confirm_edit;
    private LinearLayout ll_password, ll_confirm;

    private boolean isShowPassword;//是否显示密码输入框

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_bind);
        mContext = this;
        //启动activity时不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        initView();
    }

    private void initView() {

        pass_edit = (EditText) findViewById(R.id.pass_edit);
        pass_confirm_edit = (EditText) findViewById(R.id.pass_confirm_edit);
        pass_edit.setTransformationMethod(PasswordTransformationMethod.getInstance());
        pass_confirm_edit.setTransformationMethod(PasswordTransformationMethod.getInstance());

        ll_password = (LinearLayout) findViewById(R.id.ll_password);
        ll_confirm = (LinearLayout) findViewById(R.id.ll_confirm);


        bindType = getIntent().getIntExtra("bindType", 0);
        number = getIntent().getStringExtra("number");
        isShowPassword = getIntent().getBooleanExtra("isShowPassword", false);


        if (bindType == -1)//首页添加手机号需要新增
            isShowPassword = true;

        if (isShowPassword) {
            ll_password.setVisibility(View.VISIBLE);
            ll_confirm.setVisibility(View.VISIBLE);
        }
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_ignore = (TextView) findViewById(R.id.tv_ignore);
        gcode_aname = (TextView) findViewById(R.id.gcode_aname);
        gcode_icon = (ImageView) findViewById(R.id.gcode_icon);
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
        et_phone = (EditText) findViewById(R.id.register_email_edit);
        et_code = (EditText) findViewById(R.id.register_email_code);
        register_code_btn = (TextView) findViewById(R.id.register_code_btn);
        register_code_btn.setOnClickListener(this);
        register_voicecode_btn = (TextView) findViewById(R.id.register_voicecode_btn);
        register_voicecode_btn.setOnClickListener(this);
        tv_ignore.setOnClickListener(this);
        gcode_ly = (LinearLayout) findViewById(R.id.gcode_ly);
        gcode_ly.setOnClickListener(this);
        btn_sure = (Button) findViewById(R.id.btn_sure);
        btn_sure.setOnClickListener(this);
        if (MainApplication.app.getAppGlobalConfig().isSuportVoice()){
            findViewById(R.id.voice_layout).setVisibility(View.VISIBLE);
        }
        //bind  4 移除手机绑定  5  手机号存在则是更换 不存在则是新增
        if (!TextUtils.isEmpty(number)) {
            et_phone.setText(number);
            tv_title.setText(bindType == 4 ? getString(R.string.userinfo_activity_remove_phone) : getString(R.string.userinfo_activity_update_phone));
        } else {
            if (bindType == -1)
                tv_ignore.setVisibility(View.VISIBLE);
            tv_title.setText(getString(R.string.userinfo_activity_phone_bind));
        }
        startTime = dcsp.getLong(DataCenterSharedPreferences.Constant.CODE_START_TIME, 0);
        int time = 60 - (int) ((System.currentTimeMillis() - startTime) / 1000);
        if (startTime > 0 && time > 0) {
            waitTime = time;
            if (waitTime > 0) {
                startTime = System.currentTimeMillis();
                mHandler.sendEmptyMessageDelayed(12, 1000);
            }
        }

        et_phone.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (waitTime < 60) {
                    return;
                }
                if (Util.isMobileNO(s.toString())) {
                    register_code_btn.setEnabled(true);
                    register_voicecode_btn.setEnabled(true);
                    btn_sure.setEnabled(true);
                }

            }
        });
    }

    public void back(View v) {
        if (bindType == -1)
            startDeviceMainActivity();
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_code_btn: //发送验证码 4为解除 5绑定或更换
                if (bindType == -1) {//首页验证
                    sendCodeToPhone(5,false);
                } else {
                    sendCodeToPhone(bindType,false);
                }
                break;
            case R.id.register_voicecode_btn: //发送验证码 4为解除 5绑定或更换
                if (bindType == -1) {//首页验证
                    sendCodeToPhone(5,true);
                } else {
                    sendCodeToPhone(bindType,true);
                }
                break;
            case R.id.gcode_ly://选择国家
                Intent intent = new Intent();
                intent.setClass(mContext.getApplicationContext(), GCodeListActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.btn_sure:
                sure();
                break;
            case R.id.tv_ignore:
                if (bindType == -1)
                    startDeviceMainActivity();
                finish();
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 11) {
            ImageLoader.getInstance().displayImage(data.getStringExtra("icon"), gcode_icon);
            gcode_aname.setText(data.getStringExtra("aname"));
            country = data.getStringExtra("country");
        }
    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    if (mHandler.hasMessages(TIME_OUT)) {
                        mHandler.removeMessages(TIME_OUT);
                    }
                    String numbers = "";
                    Intent intent = getIntent();
                    if (bindType == 5 || bindType == -1) {
                        numbers = (String) msg.obj;
                        if (TextUtils.isEmpty(number)) {
                            Toast.makeText(mContext, getString(R.string.activity_beijingmy_bindsuccess), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mContext, getString(R.string.userinfo_activity_account_update_bind), Toast.LENGTH_SHORT).show();
                        }
                    } else if (bindType == 4) {
                        Toast.makeText(mContext, getString(R.string.userinfo_activity_account_remove_bind), Toast.LENGTH_SHORT).show();
                    }
                    if (bindType == -1) {
                        startDeviceMainActivity();
                    } else {
                        intent.putExtra("number", numbers);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    break;
                case TIME_OUT:
                    cancelInProgress();
                    Toast.makeText(mContext, getString(R.string.time_out), Toast.LENGTH_SHORT).show();
                    break;
                case 14:
                    if (mHandler.hasMessages(TIME_OUT)) {
                        mHandler.removeMessages(TIME_OUT);
                    }
                    if (getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        // 隐藏软键盘
                        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                    }
                    cancelInProgress();
                    new AlertView(getString(R.string.tips), getString(R.string.register_phone_codesendsuccess), null, new String[]{getString(R.string.sure)}, null, mContext, AlertView.Style.Alert, null).show();
                    startTime = System.currentTimeMillis();
                    dcsp.putLong(DataCenterSharedPreferences.Constant.CODE_START_TIME, startTime).commit();
                    mHandler.sendEmptyMessageDelayed(12, 1000);
                    break;

                case 12:
                    if (mHandler.hasMessages(TIME_OUT)) {
                        mHandler.removeMessages(TIME_OUT);
                    }
                    waitTime--;
                    register_code_btn.setText(getString(R.string.register_emailcodewait) + "(" + waitTime + ")");
                    register_code_btn.setEnabled(false);
                    register_voicecode_btn.setText(getString(R.string.register_emailcodewait_voice) + "(" + waitTime + ")");
                    register_voicecode_btn.setEnabled(false);

                    if (waitTime <= 1) {
                        startTime = 0;

                        mHandler.sendEmptyMessageDelayed(11, 1000);
                    } else {
                        mHandler.sendEmptyMessageDelayed(12, 1000);
                    }
                    break;
                case 11:
                    if (mHandler.hasMessages(TIME_OUT)) {
                        mHandler.removeMessages(TIME_OUT);
                    }
                    register_code_btn.setText(getString(R.string.register_emailcodesend));
                    register_voicecode_btn.setText(getString(R.string.register_voicecodesend));
                    waitTime = 60;
                    changeSendCodeBtn(et_phone.getText().toString());
                    break;
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    public void changeSendCodeBtn(String s) {
        if (Util.isMobileNO(s)) {
            register_code_btn.setEnabled(true);
            register_voicecode_btn.setEnabled(true);
            btn_sure.setEnabled(true);
        } else {
            register_code_btn.setEnabled(false);
            register_voicecode_btn.setEnabled(false);
        }
    }

    /**
     * 发送验证码
     *
     * @param
     */
    public void sendCodeToPhone(final int t,final boolean voice) {
        final String account = et_phone.getText().toString();
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(mContext, getString(R.string.register_tip_phone_empty), Toast.LENGTH_SHORT).show();
            return;
        }
/*        if ("0086".equals(country) && !Util.isMobileNO(account)) {
            Toast.makeText(mContext, getString(R.string.register_tip_phone_error), Toast.LENGTH_LONG).show();
            return;
        }*/
        mHandler.sendEmptyMessageDelayed(TIME_OUT, 10 * 1000);
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
                String url = "";
                if (t == 4) {
                    url = "sendnormalcode";
                } else {
                    url = "sendcode";
                }
                String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/sms/" + url, pJsonObject, BindPhoneActivity.this);
                if ("0".equals(result)) {
                    mHandler.sendEmptyMessage(14);
                } else if ("-3".equals(result)) {
                    if (mHandler.hasMessages(TIME_OUT)) {
                        mHandler.removeMessages(TIME_OUT);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();

                            Toast.makeText(mContext, getString(R.string.register_tip_phone_error), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-4".equals(result)) {
                    if (mHandler.hasMessages(TIME_OUT)) {
                        mHandler.removeMessages(TIME_OUT);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.register_tip_phone_isin), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-5".equals(result)) {
                    if (mHandler.hasMessages(TIME_OUT)) {
                        mHandler.removeMessages(TIME_OUT);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.register_tip_phone_send_filde), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-6".equals(result)) {
                    if (mHandler.hasMessages(TIME_OUT)) {
                        mHandler.removeMessages(TIME_OUT);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.register_operate_many), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-7".equals(result)) {
                    if (mHandler.hasMessages(TIME_OUT)) {
                        mHandler.removeMessages(TIME_OUT);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.register_operate_limit), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }


    public void sure() {

        final String account = et_phone.getText().toString();


        if (bindType == 5) {
            if (TextUtils.isEmpty(account)) {
                Toast.makeText(mContext, getString(R.string.register_tip_phone_empty), Toast.LENGTH_SHORT).show();
                return;
            }
/*            if ("0086".equals(country) && !Util.isMobileNO(account)) {
                Toast.makeText(mContext, getString(R.string.register_tip_phone_error), Toast.LENGTH_LONG).show();
                return;
            }*/
        }

        final String c = et_code.getText().toString();


        if (TextUtils.isEmpty(c)) {
            Toast.makeText(mContext, getString(R.string.login_regis_code), Toast.LENGTH_LONG).show();
            return;
        }

        if (isShowPassword) {
            if (pass_edit.getText().toString().length() < 6) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(mContext, getString(R.string.register_tip_password_length), Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }
            if (!pass_edit.getText().toString().equals(pass_confirm_edit.getText().toString())) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(mContext, getString(R.string.register_tip_password_confirm), Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }
        }
        final String p = pass_edit.getText().toString();


        showInProgress(getString(R.string.submiting), false, true);
        mHandler.sendEmptyMessageDelayed(TIME_OUT, 10 * 1000);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                String http = "";
                JSONObject pJsonObject = new JSONObject();
                String path = "";
                if (bindType == 5 || bindType == -1) {
                    pJsonObject.put("mobile", account);
                    pJsonObject.put("conntry", country);
                    pJsonObject.put("pwd", isShowPassword ? p : "");
                    path = "umobile";
                } else {
                    path = "cmobile";
                }
                pJsonObject.put("code", c);
                String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/u/" + path, pJsonObject, BindPhoneActivity.this);
                //-1参数为空  -2邮箱为空 -3邮箱格式不正确  -4邮箱已经存在 -5邮件发送失败
                if ("-3".equals(result)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {

                            cancelInProgress();
                            if (mHandler.hasMessages(TIME_OUT)) {
                                mHandler.removeMessages(TIME_OUT);
                            }
                            Toast.makeText(mContext, getString(R.string.activity_phone_number_formaterror), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-4".equals(result)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            if (mHandler.hasMessages(TIME_OUT)) {
                                mHandler.removeMessages(TIME_OUT);
                            }
                            if (bindType != 5) {
                                Toast.makeText(mContext, getString(R.string.register_tip_empty), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(mContext, getString(R.string.register_tip_phone_empty), Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                } else if ("-6".equals(result)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            if (mHandler.hasMessages(TIME_OUT)) {
                                mHandler.removeMessages(TIME_OUT);
                            }
                            Toast.makeText(mContext, getString(R.string.login_tip_password_empty), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-5".equals(result)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            if (mHandler.hasMessages(TIME_OUT)) {
                                mHandler.removeMessages(TIME_OUT);
                            }
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.activity_phone_verificationcode_error), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("0".equals(result)) {
                    Message message = Message.obtain();
                    message.what = 10;
                    if (bindType == 5 || bindType == -1)
                        message.obj = account;
                    mHandler.sendMessage(message);
                } else {
                    if (mHandler.hasMessages(TIME_OUT)) {
                        mHandler.removeMessages(TIME_OUT);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.net_error_weizhi), Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }

    public void startDeviceMainActivity() {
        Intent in = new Intent();
        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        in.setClass(BindPhoneActivity.this, DeviceMainActivity.class);
        startActivity(in);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (bindType == -1) {//进入APP提醒绑定手机
                startDeviceMainActivity();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
