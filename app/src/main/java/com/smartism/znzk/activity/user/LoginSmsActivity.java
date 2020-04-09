package com.smartism.znzk.activity.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.AppUserInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DateUtil;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.alertview.AlertView;

import java.util.Locale;


public class LoginSmsActivity extends ActivityParentActivity implements View.OnClickListener{
    private EditText user,pass,pass_confirm,name,mobile,email,code;
    private TextView text;
    private Button sendCodeBtn,next;
    private String codeback;
    private int waitTime = 60;
    private int isEmail = -1;
    private long starttime;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 10) { //注册成功
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(LoginSmsActivity.this,
                        DataCenterSharedPreferences.Constant.CONFIG);
                dcsp.putBoolean(DataCenterSharedPreferences.Constant.IS_LOGIN, true).commit();
                cancelInProgress();
                // 登陆成功界面不需要加锁
                dcsp.putBoolean(DataCenterSharedPreferences.Constant.IS_LOOKS, false).commit();
                // MainApplication.LockS=false;
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), DeviceMainActivity.class);
                startActivity(intent);
//                    startActivity(new Intent(getApplicationContext(), HuaweiPushActivity.class));
                finish();
            }else if(msg.what == 11){
                sendCodeBtn.setText(getString(R.string.register_emailcodesend));
                waitTime = 60;
                changeSendCodeBtn(email.getText().toString());
            }else if(msg.what == 12){
                waitTime--;
                sendCodeBtn.setText(getString(R.string.register_emailcodewait)+"("+waitTime+")");
                sendCodeBtn.setEnabled(false);
                if (waitTime <=1) {
                    defHandler.sendEmptyMessageDelayed(11, 1000);
                }else{
                    defHandler.sendEmptyMessageDelayed(12, 1000);
                }
            }else if (msg.what == 13) {
                Intent intent = new Intent();
                intent.setClass(LoginSmsActivity.this, UserMessageRegisterActivity.class);
                intent.putExtra("email", email.getText().toString());
                intent.putExtra("code", codeback);
                startActivityForResult(intent, 1);
            }else if(msg.what == 14){
                if(getWindow().getAttributes().softInputMode== WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED)
                {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    // 隐藏软键盘
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                }
                cancelInProgress();
                new AlertView(getString(R.string.tips), getString(R.string.register_phone_codesendsuccess), null,
                        new String[]{getString(R.string.sure)}, null, LoginSmsActivity.this, AlertView.Style.Alert, null).show();
                starttime = System.currentTimeMillis();
                dcsp.putLong(DataCenterSharedPreferences.Constant.CODE_START_TIME,starttime).commit();
                defHandler.sendEmptyMessageDelayed(12, 1000);
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_sms);
        //启动activity时不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //user = (EditText) findViewById(R.id.register_user_edit);
        //pass = (EditText) findViewById(R.id.register_passwd_edit);
        //pass_confirm = (EditText) findViewById(R.id.register_passwd_edit_confirm);
        //name = (EditText) findViewById(R.id.register_name_edit);
        //mobile = (EditText) findViewById(R.id.register_mobile_edit);
        isEmail = getIntent().getIntExtra("isEmail",-1);
        starttime = dcsp.getLong(DataCenterSharedPreferences.Constant.CODE_START_TIME,0);
        int time = 60 - (int)((System.currentTimeMillis() - starttime)/1000);
        if (starttime > 0&& time > 0){
            waitTime = time;
            if (waitTime>0){
                starttime = System.currentTimeMillis();
                defHandler.sendEmptyMessageDelayed(12, 1000);
            }
        }
        email = (EditText) findViewById(R.id.loginsms_phone_edit);
        email.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (waitTime<60){
                    return;
                }
                changeSendCodeBtn(s.toString());


            }
        });
        code = (EditText) findViewById(R.id.loginsms_phone_code);
        sendCodeBtn = (Button) findViewById(R.id.loginsms_code_btn);
        next = (Button) findViewById(R.id.loginsms_next_btn);
        sendCodeBtn.setOnClickListener(this);

    }

    private void changeSendCodeBtn(String s) {
        if (isEmail==-1){
            if (Util.checkPhoneNumber(s.toString())) {
                sendCodeBtn.setEnabled(true);
                next.setEnabled(true);
            }else{
                sendCodeBtn.setEnabled(false);
                next.setEnabled(false);
            }
        }else {
            if (s.length() > 0) {
                sendCodeBtn.setEnabled(true);
                next.setEnabled(true);
            }else{
                sendCodeBtn.setEnabled(false);
                next.setEnabled(false);
            }
        }
    }

    public void back(View v){
        finish();
    }
    public void next(View v){
        toLogin();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                //注册返回结果
                if(resultCode == 111){
                    defHandler.sendEmptyMessage(10);
                }
                break;

            default:
                break;
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginsms_code_btn: //发送验证码
                sendCodeToPhone();
                break;

            default:
                break;
        }
    }
    /**
     * 发送验证码
     * @param
     */
    public void sendCodeToPhone(){
        final String account = email.getText().toString();
        if (!Util.checkPhoneNumber(account)){
            Toast.makeText(LoginSmsActivity.this, getString(R.string.register_tip_phone_error), Toast.LENGTH_LONG).show();
            return;
        }
        showInProgress(getString(R.string.submiting), false, true);
        final String country = "0086";
        final String language = Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry();

        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("mobile",account);
                pJsonObject.put("conntry",country);
                pJsonObject.put("lang",language);
                pJsonObject.put("t",2);
                String result = HttpRequestUtils.requestoOkHttpPost(server+"/jdm/s3/sms/sendcode",pJsonObject,LoginSmsActivity.this);
//				String result = HttpRequestUtils.requestHttpServer(server+"/jdm/service/sendregcode?v="+URLEncoder.encode(SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP)),RegisterActivity.this,defHandler);
                if("0".equals(result)){
                    defHandler.sendEmptyMessage(14);
                }else if("-3".equals(result)){
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(LoginSmsActivity.this, getString(R.string.register_tip_phone_error), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else if("-4".equals(result)){
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(LoginSmsActivity.this, getString(R.string.register_tip_phone_isin), Toast.LENGTH_LONG).show();
                        }
                    });
                }else if("-5".equals(result)){
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(LoginSmsActivity.this, getString(R.string.register_tip_phone_send_filde), Toast.LENGTH_LONG).show();
                        }
                    });
                }else if("-6".equals(result)){
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(LoginSmsActivity.this, getString(R.string.net_error_programs), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    /**
     * 登录
     * @param
     */
    public void toLogin(){
        final String account = email.getText().toString();
        if (!Util.checkPhoneNumber(account)){
            Toast.makeText(LoginSmsActivity.this, getString(R.string.register_tip_phone_error), Toast.LENGTH_LONG).show();
            return;
        }
        showInProgress(getString(R.string.submiting), false, true);
        final String country = "0086";
        final String c = code.getText().toString();
        final String language = Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry();

        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("mobile",account);
                pJsonObject.put("conntry",country);
                pJsonObject.put("lang",language);
                pJsonObject.put("type", "android");
                pJsonObject.put("code",c);
                pJsonObject.put("tz", DateUtil.getCurrentTimeZone());
                String result = HttpRequestUtils.requestoOkHttpPost(server+"/jdm/s3/u/loginwsms",pJsonObject,LoginSmsActivity.this);
//				String result = HttpRequestUtils.requestHttpServer(server+"/jdm/service/sendregcode?v="+URLEncoder.encode(SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP)),RegisterActivity.this,defHandler);
                Log.e("wxb",result+"");
                if (result != null && result.length() > 10) {
                    Log.e("wxb",result+"");
                    JSONObject rson = null;
                    try {
                        rson = JSONObject.parseObject(result);
                    } catch (Exception e) {
                        LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                    }

                    if (rson == null) {
                        defHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                cancelInProgress();
                                Toast.makeText(LoginSmsActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
                            }
                        });
                        return;
                    }
                    dcsp.putString(DataCenterSharedPreferences.Constant.LOGIN_ACCOUNT, rson.getString("account"))
                            .putString(DataCenterSharedPreferences.Constant.LOGIN_CODE, rson.getString("code"))
                            .putString(DataCenterSharedPreferences.Constant.LOGIN_PWD, rson.getString("pass"))
                            .putLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, rson.getLongValue("id"))
                            .putInt(DataCenterSharedPreferences.Constant.LOGIN_PHONESMS, 1)
                            .commit();
                    AppUserInfo userInfo = new AppUserInfo();
                    userInfo.setId(rson.getLongValue("id"));
                    userInfo.setAccount(rson.getString("account"));
                    userInfo.setLogo(rson.getString("logo"));
                    userInfo.setEmail(rson.getString("email"));
                    userInfo.setMobile(rson.getString("mobile"));
                    userInfo.setCode(rson.getString("code"));
                    userInfo.setRole(rson.getString("role"));
                    DatabaseOperator.getInstance().insertOrUpdateUserInfo(userInfo);
                    if (rson.get("name") != null) {
                        dcsp.putString(DataCenterSharedPreferences.Constant.LOGIN_APPNAME, rson.getString("name")).commit();
                    }
                    if (rson.get("role") != null) {
                        dcsp.putString(DataCenterSharedPreferences.Constant.LOGIN_ROLE, rson.getString("role")).commit();
                    } else {
                        dcsp.putString(DataCenterSharedPreferences.Constant.LOGIN_ROLE, DataCenterSharedPreferences.Constant.ROLE_NORMAL).commit();
                    }
                    dcsp.remove(DataCenterSharedPreferences.Constant.LOGIN_LOGO).commit();
                    defHandler.sendEmptyMessage(10);
                } else if ("-3".equals(result)){
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(LoginSmsActivity.this, getString(R.string.activity_phone_number_formaterror), Toast.LENGTH_LONG).show();
                        }
                    });
                }else if("-4".equals(result)){
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(LoginSmsActivity.this, getString(R.string.activity_phone_verificationcode_error), Toast.LENGTH_LONG).show();
                        }
                    });
                }else if("-5".equals(result)){
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(LoginSmsActivity.this, getString(R.string.activity_phone_verificationcode_outtime), Toast.LENGTH_LONG).show();
                        }
                    });
                }else if("-7".equals(result)){
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(LoginSmsActivity.this, getString(R.string.activity_phone_parameter_error), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
}
