package com.smartism.znzk.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.user.GCodeListActivity;
import com.smartism.znzk.activity.user.RegisterActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.StringUtils;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.alertview.AlertView;

import java.util.Locale;

/**
 * Created by win7 on 2017/7/20.
 */

public class PhoneRegisterFragment extends Fragment implements View.OnClickListener {
    private static final long DELAY_TIME = 12 * 1000;
    private EditText email, code;
    private Button next;
    private TextView sendCodeBtn,sendVoiceCodeBtn;
    //    private String codeback;//邮箱验证码
    private int waitTime = 60;
    private long starttime;
    public int isEmail = -1;
    private EditText pass_edit, pass_confirm_edit;
    private static final int TIME_OUT = 100;
    private RegisterActivity mContext;
    private int flag = 1;  //是请求验证码的 “t” ：1、是注册请求，2、登录请求 3、找回密码请求
    private LinearLayout gcode_ly;
    private ImageView gcode_icon;
    private TextView gcode_aname;
    private String country = "0086";
    private LinearLayout ll_confirm,ll_password;
    private InputMethodManager imm = null;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 10) { //注册成功
                if (defHandler.hasMessages(TIME_OUT)) {
                    defHandler.removeMessages(TIME_OUT);
                }
//                mContext.setResult(111);
                if (flag == 1) {
                    Toast.makeText(mContext, getString(R.string.login_tip_registersuccess), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, getString(R.string.login_reset_pass_succesmessage), Toast.LENGTH_SHORT).show();
                }

                mContext.finish();
//                finish();
            } else if (msg.what == 11) {
                if (defHandler.hasMessages(TIME_OUT)) {
                    defHandler.removeMessages(TIME_OUT);
                }
                if (isAdded())
                    sendCodeBtn.setText(getString(R.string.register_emailcodesend));
                sendVoiceCodeBtn.setText(getString(R.string.register_voicecodesend));
                waitTime = 60;
                changeSendCodeBtn(email.getText().toString());
            } else if (msg.what == 12) {
                if (defHandler.hasMessages(TIME_OUT)) {
                    defHandler.removeMessages(TIME_OUT);
                }
                if (isAdded()) {
                    waitTime--;
                    sendCodeBtn.setText(getString(R.string.register_emailcodewait) + "(" + waitTime + ")");
                    sendCodeBtn.setEnabled(false);
                    sendVoiceCodeBtn.setText(getString(R.string.register_emailcodewait_voice) + "(" + waitTime + ")");
                    sendVoiceCodeBtn.setEnabled(false);
                }

                if (waitTime <= 1) {
                    starttime = 0;

                    defHandler.sendEmptyMessageDelayed(11, 1000);
                } else {
                    defHandler.sendEmptyMessageDelayed(12, 1000);
                }
            } else if (msg.what == 13) {
            } else if (msg.what == 14) {
                if (defHandler.hasMessages(TIME_OUT)) {
                    defHandler.removeMessages(TIME_OUT);
                }
                mContext.cancelInProgress();
                ll_confirm.setVisibility(View.VISIBLE);
                ll_password.setVisibility(View.VISIBLE);
                if (isEmail == -1) {
                    new AlertView(getString(R.string.tips), getString(R.string.register_phone_codesendsuccess), null, new String[]{getString(R.string.sure)}, null, mContext, AlertView.Style.Alert, null).show();
                } else {
                    new AlertView(getString(R.string.tips), getString(R.string.register_codesendsuccess), null, new String[]{getString(R.string.sure)}, null, mContext, AlertView.Style.Alert, null).show();
                }
                starttime = System.currentTimeMillis();
                mContext.dcsp.putLong(Constant.CODE_START_TIME, starttime).commit();
                defHandler.sendEmptyMessageDelayed(12, 1000);
            } else if (msg.what == TIME_OUT) {
                mContext.cancelInProgress();
                Toast.makeText(mContext, getString(R.string.time_out), Toast.LENGTH_LONG).show();
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_regis_phone, container, false);
        mContext = (RegisterActivity) getActivity();
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        email = (EditText) view.findViewById(R.id.register_email_edit);
        code = (EditText) view.findViewById(R.id.register_email_code);
        pass_edit = (EditText) view.findViewById(R.id.pass_edit);
        pass_confirm_edit = (EditText) view.findViewById(R.id.pass_confirm_edit);
        pass_edit.setTransformationMethod(PasswordTransformationMethod.getInstance());
        pass_confirm_edit.setTransformationMethod(PasswordTransformationMethod.getInstance());

        sendCodeBtn = (TextView) view.findViewById(R.id.register_code_btn);
        sendVoiceCodeBtn = (TextView) view.findViewById(R.id.register_voicecode_btn);
        next = (Button) view.findViewById(R.id.register_next_btn);
        gcode_ly = (LinearLayout) view.findViewById(R.id.gcode_ly);
        gcode_icon = (ImageView) view.findViewById(R.id.gcode_icon);
        gcode_aname = (TextView) view.findViewById(R.id.gcode_aname);
        ll_password = (LinearLayout) view.findViewById(R.id.ll_password);
        ll_confirm = (LinearLayout) view.findViewById(R.id.ll_confirm);
        if (MainApplication.app.getAppGlobalConfig().isSuportVoice()){
            view.findViewById(R.id.voice_layout).setVisibility(View.VISIBLE);
        }
        String gcodeInfo = mContext.getDcsp().getString(Constant.LOCALE_GCODE,"");
        if (!org.apache.commons.lang.StringUtils.isEmpty(gcodeInfo)){
            try{
                JSONObject object = JSON.parseObject(gcodeInfo);
                ImageLoader.getInstance().displayImage(object.getString("icon"), gcode_icon);
                gcode_aname.setText(object.getString("aname"));
                country = object.getString("country");
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
//        if (!Util.isChinaSimCard(mContext)){
//        }
        gcode_ly.setOnClickListener(this);
        email.setInputType(InputType.TYPE_CLASS_PHONE); //设置输入类型
        initData();
    }

    private void initData() {
        sendCodeBtn.setOnClickListener(this);
        sendVoiceCodeBtn.setOnClickListener(this);
        next.setOnClickListener(this);
        starttime = mContext.dcsp.getLong(Constant.CODE_START_TIME, 0);
        Log.e("wxb", starttime + "starttime");
        int time = 60 - (int) ((System.currentTimeMillis() - starttime) / 1000);
        if (starttime > 0 && time > 0) {
            waitTime = time;
//            Log.e("wxb", waitTime + "waitTime");
            if (waitTime > 0) {
                starttime = System.currentTimeMillis();
                defHandler.sendEmptyMessageDelayed(12, 1000);
            }
        }

        flag = mContext.getIntent().getIntExtra("flag", 1);
        if (flag != 1) {
            next.setText(getString(R.string.submit));
        }
        email.addTextChangedListener(new TextWatcher() {

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
                    sendCodeBtn.setEnabled(true);
                    sendVoiceCodeBtn.setEnabled(true);
                    next.setEnabled(true);
                }

            }
        });

        if (isEmail == -1) {
            email.setHint(getResources().getString(R.string.register_phonenumber));
        }
    }

    public void changeSendCodeBtn(String s) {
        if (Util.isMobileNO(s)) {
            sendCodeBtn.setEnabled(true);
            sendVoiceCodeBtn.setEnabled(true);
            next.setEnabled(true);
        } else {
            sendCodeBtn.setEnabled(false);
            sendVoiceCodeBtn.setEnabled(false);
        }
    }


    /**
     * 发送验证码
     *
     * @param
     */
    public void sendCodeToPhone(final int t,final boolean voice) {
        final String account = email.getText().toString();
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(mContext, getString(R.string.register_tip_phone_empty), Toast.LENGTH_SHORT).show();
            return;
        }
/*        if ("0086".equals(country) && !Util.isMobileNO(account)) {
            Toast.makeText(mContext, getString(R.string.register_tip_phone_error), Toast.LENGTH_LONG).show();
            return;
        }*/
        defHandler.sendEmptyMessageDelayed(TIME_OUT, DELAY_TIME);
        mContext.showInProgress(getString(R.string.submiting), false, true);
        final String language = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = mContext.dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("mobile", account);
                pJsonObject.put("conntry", country);
                pJsonObject.put("lang", language);
                pJsonObject.put("voice", voice);
                pJsonObject.put("t", t);
                String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/sms/sendcode", pJsonObject, mContext);
                if ("0".equals(result)) {
                    defHandler.sendEmptyMessage(14);
                } else if ("-3".equals(result)) {
                    if (defHandler.hasMessages(TIME_OUT)) {
                        defHandler.removeMessages(TIME_OUT);
                    }
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mContext.cancelInProgress();

                            Toast.makeText(mContext, getString(R.string.register_tip_phone_error), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-4".equals(result)) {
                    if (defHandler.hasMessages(TIME_OUT)) {
                        defHandler.removeMessages(TIME_OUT);
                    }
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            if (t==3 || t == 2){
                                Toast.makeText(mContext, getString(R.string.register_tip_phone_not), Toast.LENGTH_LONG).show();
                            }else if (t ==1){
                                Toast.makeText(mContext, getString(R.string.register_tip_phone_isin), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else if ("-5".equals(result)) {
                    if (defHandler.hasMessages(TIME_OUT)) {
                        defHandler.removeMessages(TIME_OUT);
                    }
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.register_tip_phone_send_filde), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-6".equals(result)) {
                    if (defHandler.hasMessages(TIME_OUT)) {
                        defHandler.removeMessages(TIME_OUT);
                    }
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.register_operate_many), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-7".equals(result)) {
                    if (defHandler.hasMessages(TIME_OUT)) {
                        defHandler.removeMessages(TIME_OUT);
                    }
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.register_operate_limit), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }


    /**
     * //     * 验证验证码
     * //     *
     * //     * @param
     * //
     */
    public void phoneCode() {
        final String account = email.getText().toString();
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(mContext, getString(R.string.register_tip_phone_empty), Toast.LENGTH_SHORT).show();
            return;
        }

/*        if ("0086".equals(country) &&!Util.isMobileNO(account)) {
            Toast.makeText(mContext, getString(R.string.register_tip_phone_error), Toast.LENGTH_LONG).show();
            return;
        }*/
        final String c = code.getText().toString();


        if (TextUtils.isEmpty(c)) {
            Toast.makeText(mContext, getString(R.string.login_regis_code), Toast.LENGTH_LONG).show();
            return;
        }

        if (pass_edit.getText().toString().length() < 6) {
            defHandler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(mContext, getString(R.string.register_tip_password_length), Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        if (!pass_edit.getText().toString().equals(pass_confirm_edit.getText().toString())) {
            defHandler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(mContext, getString(R.string.register_tip_password_confirm), Toast.LENGTH_LONG).show();
                }
            });
            return;
        }
        final String p = pass_edit.getText().toString();
        final String l = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
        mContext.showInProgress(getString(R.string.submiting), false, true);
        defHandler.sendEmptyMessageDelayed(TIME_OUT, DELAY_TIME);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = mContext.dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                String http = "";
                JSONObject pJsonObject = new JSONObject();
//                pJsonObject.put("conntry", "0086");
//                pJsonObject.put("mobile", account);
//                pJsonObject.put("code", c);
                if (flag == 1) {
                    if (!StringUtils.isEmpty(p)) {
                        pJsonObject.put("password", p);
                    }
                    pJsonObject.put("type", "android");
                    pJsonObject.put("code", c);
                    pJsonObject.put("lang", l);
                    pJsonObject.put("istc", MainApplication.app.getAppGlobalConfig().isTc());
                    pJsonObject.put("conntry", country);
                    pJsonObject.put("mobile", email.getText().toString());
                    http = "/jdm/s3/u/regbysms";
                } else {
                    if (!StringUtils.isEmpty(p)) {
                        pJsonObject.put("np", SecurityUtil.MD5(p));
                    }
                    pJsonObject.put("code", c);
                    pJsonObject.put("conntry", country);
                    pJsonObject.put("mobile", email.getText().toString());
                    http = "/jdm/s3/u/rpsms";
                }

                final String result = HttpRequestUtils.requestoOkHttpPost( server + http, pJsonObject, mContext);
                //-1参数为空  -2邮箱为空 -3邮箱格式不正确  -4邮箱已经存在 -5邮件发送失败
                if ("-3".equals(result)) {
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {

                            mContext.cancelInProgress();
                            if (defHandler.hasMessages(TIME_OUT)) {
                                defHandler.removeMessages(TIME_OUT);
                            }
                            if (flag == 1) {
                                Toast.makeText(mContext, getString(R.string.register_tip_email_erro), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(mContext, getString(R.string.activity_phone_number_formaterror), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else if ("-4".equals(result)) {
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            if (defHandler.hasMessages(TIME_OUT)) {
                                defHandler.removeMessages(TIME_OUT);
                            }
                            if (flag == 1) {
                                Toast.makeText(mContext, getString(R.string.register_tip_email_isin), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(mContext, getString(R.string.register_tip_phone_isin), Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                } else if ("-5".equals(result)) {
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            if (defHandler.hasMessages(TIME_OUT)) {
                                defHandler.removeMessages(TIME_OUT);
                            }
                            if (flag == 1) {
                                Toast.makeText(mContext, getString(R.string.register_tip_email_send_filde), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(mContext, getString(R.string.register_tip_phone_send_filde), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else if ("-6".equals(result)) {
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            if (defHandler.hasMessages(TIME_OUT)) {
                                defHandler.removeMessages(TIME_OUT);
                            }
                            mContext.cancelInProgress();
                            if (flag == 1) {
                                Toast.makeText(mContext, getString(R.string.register_codeerror), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(mContext, getString(R.string.activity_phone_verificationcode_error), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else if (!StringUtils.isEmpty(result) && result.length() > 3) {
                    defHandler.sendEmptyMessage(10);
                } else if ("0".equals(result)) {
                    defHandler.sendEmptyMessage(10);
                } else {
                    if (defHandler.hasMessages(TIME_OUT)) {
                        defHandler.removeMessages(TIME_OUT);
                    }
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            mContext.cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.net_error_weizhi), Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_code_btn: //发送验证码
                // 隐藏软键盘
                imm.hideSoftInputFromWindow(mContext.getWindow().getDecorView().getWindowToken(), 0);
                sendCodeToPhone(flag,false);
                break;
            case R.id.register_voicecode_btn: //发送语音验证码
                // 隐藏软键盘
                imm.hideSoftInputFromWindow(mContext.getWindow().getDecorView().getWindowToken(), 0);
                sendCodeToPhone(flag,true);
                break;
            case R.id.register_next_btn:
                phoneCode();
                break;
            case R.id.gcode_ly://选择国家
                Intent intent = new Intent();
                intent.setClass(mContext.getApplicationContext(), GCodeListActivity.class);
                startActivityForResult(intent,1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 11){
            ImageLoader.getInstance().displayImage(data.getStringExtra("icon"),gcode_icon);
            gcode_aname.setText(data.getStringExtra("aname"));
            country = data.getStringExtra("country");
        }
    }
}
