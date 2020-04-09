package com.smartism.znzk.activity.user;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.HostedUIOptions;
import com.amazonaws.mobile.client.IdentityProvider;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.UserStateListener;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobileconnectors.cognitoauth.Auth;
import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotConfirmedException;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.hjq.toast.ToastUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.DisclaimerActivity;
import com.smartism.znzk.activity.common.EnvironmentCheckToolActivity;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.update.UpdateAgent;
import com.smartism.znzk.view.DialogView;
import com.smartism.znzk.view.alertview.AlertView;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends ActivityParentActivity implements View.OnClickListener {
    private static final int MSG_AUTH_CANCEL = 2;
    private static final int MSG_AUTH_ERROR = 3;
    private static final int MSG_AUTH_COMPLETE = 4;
    private static final int MSG_TONETWORK_TESTPAGE = 1000;
    private EditText user, pass;
    private CheckBox rememberPwd;
    // private ImageView loginlogo;
    private String errorInfo;
    private TextView register,forgot;
    private LinearLayout qq, weixin, facebook, twitter,googleplus, showthirdLogin;
    private CheckBox check_pass;
    private LinearLayout ll_pass;
    private TextView forget_passwd;
    private RelativeLayout rl_gcode;
    private ImageView loginLogo;
    //免责声明
    private LinearLayout disclaimerSelect;
    private TextView disclaimerLook, disclaimerText;
    private ImageView disclaimerIcon;
    private boolean isAgreeDisclaimer;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_TONETWORK_TESTPAGE:
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), EnvironmentCheckToolActivity.class);
                    startActivity(intent);
                    break;
//				登陆成功
                case 10:
                    //清空雄迈摄像头配置
                    DataCenterSharedPreferences.getInstance(getApplicationContext()
                            , DataCenterSharedPreferences.Constant.XM_CONFIG).getEditor().clear().commit();

                    DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(LoginActivity.this,
                            Constant.CONFIG);
                    dcsp.putBoolean(Constant.IS_LOGIN, true).commit();
                    cancelInProgress();
                    // 登陆成功界面不需要加锁
                    dcsp.putBoolean(Constant.IS_LOOKS, false).commit();
                    // MainApplication.LockS=false;
                    intent = new Intent();

                    String rson = (String) msg.obj;
                    JSONObject object = JSON.parseObject(rson);

                    String mobile = null;
                    if (object.getString("mobile") != null) {
                        mobile = object.getString("mobile");
                    }


                    boolean isNoticeAddMobile = false;
                    long currentTime = System.currentTimeMillis();
                    long lastTinme = dcsp.getLong(Constant.NOTICE_ADD_MOBILE, 0);
                    long lId = dcsp.getLong(Constant.LOGIN_APPID + Constant.NOTICE_ADD_MOBILE, 0);
                    Log.d(TAG, "loginId: " + lId);
                    int days = (int) ((currentTime - lastTinme) / (1000 * 3600 * 24));
                    long loginId = object.getLongValue("id");
//                    //1.mobile必须为空 2.本地存储的时间为0 3.同一用户7天外4.切换用户
//                    if (MainApplication.app.getAppGlobalConfig().isPhone() && TextUtils.isEmpty(mobile)) {
//                        if (lastTinme == 0 || lId == 0) {
//                            isNoticeAddMobile = true;
//                        } else if (lId != 0 && lId != loginId) {
//                            isNoticeAddMobile = true;
//                        } else if (lastTinme != 0 && days > 7) {
//                            isNoticeAddMobile = true;
//                        }
//                    }


                    if (isNoticeAddMobile) {
                        isNoticeAddMobile = false;
                        intent.setClass(LoginActivity.this, BindPhoneActivity.class);
                        intent.putExtra("bindType", -1);//首页添加手机号
                    } else {
                        intent.setClass(LoginActivity.this, DeviceMainActivity.class);
                    }

                    if (isNoticeAddMobile)
                        dcsp.putLong(Constant.NOTICE_ADD_MOBILE, currentTime).commit();

                    dcsp.putLong(Constant.LOGIN_APPID + Constant.NOTICE_ADD_MOBILE, loginId).commit();
//                    intent.putExtra("isNoticeAddMobile", isNoticeAddMobile);
                    startActivity(intent);
                    finish();
                    break;
                case MSG_AUTH_CANCEL:
                    //取消授权
                    cancelInProgress();
                    Toast.makeText(mContext, R.string.auth_cancel, Toast.LENGTH_SHORT).show();

                    break;
                case MSG_AUTH_ERROR:
                    //授权失败
                    cancelInProgress();
                    Toast.makeText(mContext, R.string.auth_error, Toast.LENGTH_SHORT).show();

                    break;
                case MSG_AUTH_COMPLETE:
                    //授权成功
                    cancelInProgress();
                    showInProgress(getString(R.string.auth_complete));
                    Object[] objs = (Object[]) msg.obj;
                    final String platform = (String) objs[0];
//                    final PlatformDb platformDb = (PlatformDb) objs[1];
//                    if (platformDb != null) {
////                        setHttps(platformDb);
//                        JavaThreadPool.getInstance().excute(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences
//                                        .getInstance(LoginActivity.this, Constant.CONFIG);
//                                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
//                                JSONObject object = new JSONObject();
//                                if (platform.equals(QQ.NAME)) {
//                                    object.put("p", 1);
//                                } else if (platform.equals(Wechat.NAME)) {
//                                    object.put("p", 3);
//                                } else if (platform.equals(Facebook.NAME)) {
//                                    object.put("p", 4);
//                                } else if (platform.equals(Twitter.NAME)) {
//                                    object.put("p", 5);
//                                } else if (platform.equals(GooglePlus.NAME)){
//                                    object.put("p", 6);
//                                }
//                                object.put("openid", platformDb.getUserId());
//                                object.put("unionid", platformDb.get("unionid"));
//                                object.put("name", platformDb.getUserName());
//                                object.put("logo", platformDb.getUserIcon());
//                                object.put("type", "android");
//                                object.put("lang",
//                                        Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry());
//                                object.put("tz", DateUtil.getCurrentTimeZone());
//
//                                String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/u/loginft", object, LoginActivity.this);
////                                    result = HttpRequestUtils
////                                            .requestHttpServer(
////                                                     server + "/jdm/service/apploginft?v="
////                                                            + URLEncoder.encode(SecurityUtil.crypt(object.toJSONString(),
////                                                            Constant.KEY_HTTP), "UTF-8"),
////                                                    LoginActivity.this, defHandler);
//                                // -1参数为空,-5服务器错误
//                                if (result != null && result.length() > 2) {
//                                    JSONObject rson = null;
//                                    try {
//                                        rson = JSONObject
//                                                .parseObject(result);
//                                    } catch (Exception e) {
//                                        LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
//                                    }
//                                    if (rson == null) {
//                                        defHandler.post(new Runnable() {
//
//                                            @Override
//                                            public void run() {
//                                                cancelInProgress();
//                                                Toast.makeText(LoginActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
//                                            }
//                                        });
//                                        return;
//                                    }
//                                    dcsp.putString(Constant.LOGIN_ACCOUNT, rson.getString("account"))
//                                            .putString(Constant.LOGIN_PWD, rson.getString("pass"))
//                                            .putLong(Constant.LOGIN_APPID, rson.getLongValue("id"))
//                                            .putString(Constant.LOGIN_CODE, rson.getString("code"))
//                                            .putString(Constant.LOGIN_LOGO, rson.getString("logo"))
//                                            .commit();
//                                    AppUserInfo userInfo = new AppUserInfo();
//                                    userInfo.setId(rson.getLongValue("id"));
//                                    userInfo.setAccount(rson.getString("account"));
//                                    userInfo.setLogo(rson.getString("logo"));
//                                    userInfo.setEmail(rson.getString("email"));
//                                    userInfo.setMobile(rson.getString("mobile"));
//                                    userInfo.setCode(rson.getString("code"));
//                                    userInfo.setRole(rson.getString("role"));
//                                    DatabaseOperator.getInstance().insertOrUpdateUserInfo(userInfo);
//                                    dcsp.putBoolean(Constant.IS_TURN, true).commit();//调用登录接口设置一次
//                                    if (rson.get("name") != null) {
//                                        dcsp.putString(Constant.LOGIN_APPNAME, rson.getString("name")).commit();
//                                    }
//                                    if (rson.get("role") != null) {
//                                        dcsp.putString(Constant.LOGIN_ROLE, rson.getString("role")).commit();
//                                    } else {
//                                        dcsp.putString(Constant.LOGIN_ROLE, Constant.ROLE_NORMAL).commit();
//                                    }
//                                    Message msg = Message.obtain();
//                                    msg.what = 10;
//                                    msg.obj = rson.toJSONString();
//                                    defHandler.sendMessage(msg);
//
//                                } else if ("-5".equals(result)) {
//                                    defHandler.post(new Runnable() {
//
//                                        @Override
//                                        public void run() {
//                                            cancelInProgress();
//                                            Toast.makeText(LoginActivity.this, getString(R.string.net_error_servererror),
//                                                    Toast.LENGTH_LONG).show();
//                                        }
//                                    });
//                                }
//                            }
//                        });
//                    } else {
//                        Toast.makeText(LoginActivity.this, getString(R.string.login_thrid_error_failed), Toast.LENGTH_SHORT)
//                                .show();
//                        Log.d("TestData", "发生错误：");
//                    }
                    break;
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);
    private boolean isShowPassword;
    private ImageView gcode_icon;
    private TextView gcode_aname;

//    public void setHttps(PlatformDb platformDb) {
//        String l = Locale.getDefault().getLanguage() + "-"
//                + Locale.getDefault().getCountry();
//        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
//                .hostnameVerifier(new HostnameVerifier() {
//                    @Override
//                    public boolean verify(String hostname, SSLSession session) {
//                        return false;
//                    }
//                })
//                .retryOnConnectionFailure(false)
//                .build();
//        OkHttpUtils.initClient(okHttpClient);
//
//        String url = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + platformDb.getToken() + "&openid=" + platformDb.getUserId() + "&lang=" + l;
//        OkHttpUtils.get().url(url).build().execute(new StringCallback() {
//            @Override
//            public void onError(Call call, Exception e, int id) {
//                Toast.makeText(mContext, "get FAil" + call.toString(), Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onResponse(String response, int id) {
//                Toast.makeText(mContext, "get success" + response.toString(), Toast.LENGTH_LONG).show();
//            }
//        });
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 启动activity时不自动弹出软键盘
        setContentView(R.layout.activity_zhzj_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        loginLogo = (ImageView) findViewById(R.id.login_logo);
        loginLogo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){//按下
                    defHandler.sendEmptyMessageDelayed(MSG_TONETWORK_TESTPAGE,10000);
                }else if(event.getAction() == MotionEvent.ACTION_UP
                        || event.getAction() == MotionEvent.ACTION_CANCEL
                        || event.getAction() == MotionEvent.ACTION_OUTSIDE){
                    defHandler.removeMessages(MSG_TONETWORK_TESTPAGE);
                }
                return true;
            }
        });

        //免则声明
        isAgreeDisclaimer = dcsp.getBoolean(Constant.START_APP_FIRST, false);
        disclaimerSelect = (LinearLayout) findViewById(R.id.layout_disclaimer);
        disclaimerText = (TextView) findViewById(R.id.login_disclaimer);
        disclaimerLook = (TextView) findViewById(R.id.login_disclaimer_text);
        disclaimerIcon = (ImageView) findViewById(R.id.login_disclaimer_select);
        disclaimerLook.setOnClickListener(this);
        disclaimerText.setOnClickListener(this);
        disclaimerIcon.setOnClickListener(this);
        changeDisclaimer(isAgreeDisclaimer);
        disclaimerText.setText(Html.fromHtml(getResources().getString(R.string.activity_privacy_policy)));


        user = (EditText) findViewById(R.id.login_user_edit);
        check_pass = (CheckBox) findViewById(R.id.check_pass);
        register = (TextView) findViewById(R.id.login_to_register);
        forgot = (TextView) findViewById(R.id.login_to_forgot);
        ll_pass = (LinearLayout) findViewById(R.id.ll_pass);
        rl_gcode = (RelativeLayout) findViewById(R.id.rl_gcode);
        rl_gcode.setOnClickListener(this);
        if (!MainApplication.app.getAppGlobalConfig().isPhone()) {
            rl_gcode.setVisibility(View.GONE);
            findViewById(R.id.rl_gcode_line).setVisibility(View.GONE);
            user.setHint(getString(R.string.login_user_hit_nophone));
        }
        forget_passwd = (TextView) findViewById(R.id.forget_passwd);
        forget_passwd.setOnClickListener(this);
        ll_pass.setOnClickListener(this);
        register.setOnClickListener(this);
        forgot.setOnClickListener(this);
        DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(LoginActivity.this, Constant.CONFIG);
        user.setText(dcsp.getString(Constant.LOGIN_ACCOUNT_ORIGINAL, ""));
        pass = (EditText) findViewById(R.id.login_passwd_edit);
        pass.setTypeface(Typeface.DEFAULT);
        pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
        if (dcsp.getBoolean(Constant.LOGIN_PWD_REMEMBER, false)) {
            pass.setText(SecurityUtil.decrypt(dcsp.getString(Constant.LOGIN_PWD_ORIGINAL, ""), Constant.KEY_TCP));
        }
        //设置光标位置
        user.setSelection(user.getText().length());
        pass.setSelection(pass.getText().length());
//        pass.setInputType(InputType.TYPE_CLASS_TEXT
//                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        gcode_icon = (ImageView) findViewById(R.id.gcode_icon);
        gcode_aname = (TextView) findViewById(R.id.gcode_aname);
        String gcodeInfo = dcsp.getString(Constant.LOCALE_GCODE,"");
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
        check_pass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String psw = pass.getText().toString();

                if (isChecked) {
                    pass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                pass.setSelection(psw.length());
            }
        });
        rememberPwd = (CheckBox) findViewById(R.id.login_passwd_remember);
        rememberPwd.setChecked(dcsp.getBoolean(Constant.LOGIN_PWD_REMEMBER, false));
        if (getIntent() != null && getIntent().getBooleanExtra("iskickoff", false)) {
            if ("1".equals(getIntent().getStringExtra("msg"))) {
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_kickoff_errorpassword_tip),
                        Toast.LENGTH_LONG).show();
            } else if ("sessionfailure".equals(getIntent().getStringExtra("msg"))) { // 踢下线 密码被修改
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_kickoff_sessionfailure_tip),
                        Toast.LENGTH_LONG).show();
            } else if ("outofday".equals(getIntent().getStringExtra("msg"))) { //踢下线 登录已过期
                Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_kickoff_outofday_tip),
                        Toast.LENGTH_LONG).show();
            } else {
                new AlertView(getString(R.string.login_kickoff_tip), getString(R.string.login_kickoff_msg), null,
                        new String[]{getString(R.string.sure)}, null, LoginActivity.this, AlertView.Style.Alert,
                        null).show();
            }
        }
//        if (MainApplication.app.getAppGlobalConfig().isPhone()) {
//            smsLogin.setVisibility(View.VISIBLE);
//        } else {
//            smsLogin.setVisibility(View.GONE);
//        }
        // if
        // (VersionType.CHANNEL_UCTECH.equals(getJdmApplication().getVersionType()))
        // {
        // loginlogo = (ImageView) findViewById(R.id.login_logo);
        // loginlogo.setImageResource(R.drawable.ic_launcher_uctech);
        // }
        //吉凯达有自己的layout文件，不需要判断是否启用三方登录。
        showthirdLogin = (LinearLayout) findViewById(R.id.show_thrid_login);
        if (MainApplication.app.getAppGlobalConfig().isShowThirdLogin()) {
            showthirdLogin.setVisibility(View.VISIBLE);
            qq = (LinearLayout) findViewById(R.id.login_qq);
            qq.setOnClickListener(this);
            if (!MainApplication.app.getAppGlobalConfig().isShowQQ()) {
                qq.setVisibility(View.GONE);
            }
            weixin = (LinearLayout) findViewById(R.id.login_wechat);
            weixin.setOnClickListener(this);
            if (!MainApplication.app.getAppGlobalConfig().isShowWeiXin()) {
                weixin.setVisibility(View.GONE);
            }
            facebook = (LinearLayout) findViewById(R.id.login_facebook);
            facebook.setOnClickListener(this);
            if (!MainApplication.app.getAppGlobalConfig().isShowFaceBook()) {
                facebook.setVisibility(View.GONE);
            }
            twitter = (LinearLayout) findViewById(R.id.login_twitter);
            twitter.setOnClickListener(this);
            if (!MainApplication.app.getAppGlobalConfig().isShowTwitter()) {
                twitter.setVisibility(View.GONE);
            }
            googleplus = (LinearLayout) findViewById(R.id.login_google);
            googleplus.setOnClickListener(this);
            if (!MainApplication.app.getAppGlobalConfig().isShowGoogle()) {
                googleplus.setVisibility(View.GONE);
            }
        } else {
            showthirdLogin.setVisibility(View.GONE);
        }
        if (!LogUtil.isDebug && MainApplication.app.getAppGlobalConfig().isAutomaticUpdates()) { // 只有正式环境才让更新
            UpdateAgent.update(this);
        }
//        ShareSDK.initSDK(this);
//        Review.MD5Review(this,"com.smartism.jujiang","3da71ed8be6dc300477a75bc679b0952");

//        String pack = this.getPackageName();
//        Log.d("package", pack);
        initAwsUserStateListener();
    }

    public void to_register(View v) {


    }

    private void initAwsUserStateListener(){
        AWSMobileClient.getInstance().addUserStateListener(new UserStateListener() {
            @Override
            public void onUserStateChanged(UserStateDetails userStateDetails) {
                switch (userStateDetails.getUserState()){
                    case GUEST:
                        Log.i(TAG, "user is in guest mode");
                        break;
                    case SIGNED_OUT:
                        Log.i(TAG, "user is signed out");
                        break;
                    case SIGNED_IN:
                        Log.i(TAG, "user is signed in");
                        ToastUtils.show("Sign-in done.");
                        runOnUiThread(() -> {
                            cancelInProgress();
                        });
                        Intent intent = new Intent();
                        intent.setClass(getApplication(), DeviceMainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case SIGNED_OUT_USER_POOLS_TOKENS_INVALID:
                        Log.i(TAG, "need to login again");
                        break;
                    case SIGNED_OUT_FEDERATED_TOKENS_INVALID:
                        Log.i(TAG, "user logged in via federation, but currently needs new tokens");
                        break;
                    default:
                        Log.e(TAG, "unsupported");
                }
            }
        });
    }

    public void changeDisclaimer(boolean agree) {
        if (agree) {
            disclaimerIcon.setImageResource(R.drawable.zhzj_date_xuanzhong);
        } else {
            disclaimerIcon.setImageResource(R.drawable.zhzj_date_moren);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (Util.isChinaSimCard(mContext) || Actions.VersionType.CHANNEL_QYJUNHONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
//            rl_gcode.setVisibility(View.GONE);
//        } else {
//            rl_gcode.setVisibility(View.VISIBLE);
//        }
        Util.clearLoginInfo(getApplicationContext(), dcsp);//打开登录页再清除一下登录信息 防止未清空的情况
        Intent activityIntent = getIntent();
        if (activityIntent.getData() != null &&
                "myapp".equals(activityIntent.getData().getScheme())) {
            showInProgress();
            AWSMobileClient.getInstance().handleAuthResponse(activityIntent);
        }
    }

    private String country = "0086";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                // 注册返回结果
                if (resultCode == 111) {
                    Toast.makeText(LoginActivity.this, getString(R.string.login_tip_registersuccess), Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case 11:
                if (resultCode == 11) {
                    ImageLoader.getInstance().displayImage(data.getStringExtra("icon"), gcode_icon);
                    gcode_aname.setText(data.getStringExtra("aname"));
                    country = data.getStringExtra("country");
                }
                break;
            default:
                break;
        }
    }

    public void back(View v) {
        finish();
    }

    public void retrieve_pwd(View v) {
        forget_passwd.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        forget_passwd.getPaint().setAntiAlias(true);
        if (MainApplication.app.getAppGlobalConfig().isPhone()) {
            DialogView dialogView = new DialogView(LoginActivity.this, true,
                    getString(R.string.login_reset_pass_mode),
                    getString(R.string.login_reset_pass_messages),
                    getString(R.string.register_reset_pass_email),
                    null,
                    getString(R.string.login_reset_pass_phone),
                    new DialogView.DialogViewItemListener() {
                        @Override
                        public void onItemListener(DialogView dialogView, View view, int index) {
                            Intent intent = new Intent();
                            switch (index) {
                                case 0:
                                    intent.setClass(getApplicationContext(), ResetPasswordActivity.class);
                                    startActivity(intent);
                                    break;
                                case 1:
                                    break;
                                case 2:
                                    intent.setClass(LoginActivity.this, RegisterActivity.class);
                                    intent.putExtra("isEmail", -1);
                                    intent.putExtra("flag", 3);
                                    startActivity(intent);

                                    break;
                            }
                        }

                    });
            dialogView.show();
        } else {
            Intent intent = new Intent();
//            intent.setClass(getApplicationContext(), ResetPasswordActivity.class);
            intent.setClass(getApplicationContext(), RegisterActivity.class);
            intent.putExtra("flag", 3);
            startActivity(intent);
        }
//

    }


    public boolean isNumeric(String str) {
        if (str.length() > 6) { //账号长度大于6才可能是手机号
            Pattern pattern = Pattern.compile("[0-9]*");
            Matcher isNum = pattern.matcher(str);
            if (!isNum.matches()) {
                return false;
            }
        }else{
            return false;
        }
        return true;
    }

    /**
     * 开始登陆
     *
     * @param v
     */
    public void login(View v) {
        if (!isAgreeDisclaimer) {
            ToastUtil.longMessage(getString(R.string.activity_disclaimer_mustagree));
    //        Toast.makeText(this, getString(R.string.activity_disclaimer_mustagree), Toast.LENGTH_SHORT).show();
            return;
        }
//        测试北京锁
//        boolean isok = true;
//        startActivity(new Intent(this, BeijingSuoActivity.class));
//        if (isok) return;
        showInProgress(getString(R.string.login_tip_logining), false, true);
        String account = user.getText().toString();
        if ("".equals(account)) {
            defHandler.post(new Runnable() {

                @Override
                public void run() {
                    cancelInProgress();
                    new AlertView(getString(R.string.zhzj_login_fail_title), getString(R.string.login_tip_account_empty), null,
                            new String[]{getString(R.string.sure)}, null, LoginActivity.this, AlertView.Style.Alert,
                            null).show();
//                    Toast.makeText(LoginActivity.this, getString(R.string.login_tip_account_empty), Toast.LENGTH_LONG)
//                            .show();
                }
            });
            return;
        }
        if (account.contains("@")) {
            //账号为邮箱
            String acc = account.substring(0, account.lastIndexOf("@"));
            String yu = account.substring(account.lastIndexOf("@"));
            if (yu != null) {
                yu = yu.toLowerCase();
                yu = yu.replaceAll("。", ".");
            }
            account = acc + yu;
        }
        if (Util.checkPhoneNumber(account)) {
        }

        if (!isNumeric(account) || ((Actions.VersionType.CHANNEL_AIERFUDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                || Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())) && account.startsWith("8"))) {//如果是村数字的话就是号码
            country = "";
        }

        final String a = country + account;
        final String aaa = account;
        if ("".equals(pass.getText().toString())) {
            defHandler.post(new Runnable() {

                @Override
                public void run() {
                    cancelInProgress();
                    new AlertView(getString(R.string.zhzj_login_fail_title), getString(R.string.login_tip_password_empty), null,
                            new String[]{getString(R.string.sure)}, null, LoginActivity.this, AlertView.Style.Alert,
                            null).show();
//                    Toast.makeText(LoginActivity.this, getString(R.string.login_tip_password_empty), Toast.LENGTH_LONG)
//                            .show();
                }
            });
            return;
        }
//        final String p = SecurityUtil.MD5(pass.getText().toString());
        final String p = pass.getText().toString();
        if (rememberPwd.isChecked()) {
            dcsp.putBoolean(Constant.LOGIN_PWD_REMEMBER, true).commit();
            dcsp.putString(Constant.LOGIN_PWD_ORIGINAL, SecurityUtil.crypt(pass.getText().toString(), Constant.KEY_TCP))
                    .commit();
        } else {
            dcsp.putBoolean(Constant.LOGIN_PWD_REMEMBER, false).commit();
            dcsp.putString(Constant.LOGIN_PWD_ORIGINAL, "").commit();
        }


        AWSMobileClient.getInstance().signIn(account, p, null, new Callback<SignInResult>() {
            @Override
            public void onResult(final SignInResult signInResult) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Sign-in callback state: " + signInResult.getSignInState());
                    switch (signInResult.getSignInState()) {
                        case DONE:
                            ToastUtils.show("Sign-in done.");
                            Intent intent = new Intent();
                            intent.setClass(LoginActivity.this,DeviceMainActivity.class);
                            startActivity(intent);
                            break;
                        case SMS_MFA:
                            ToastUtil.longMessage("Please confirm sign-in with SMS.");
                            break;
                        case NEW_PASSWORD_REQUIRED:
                            ToastUtil.longMessage("Please confirm sign-in with new password.");
                            break;
                        default:
                            ToastUtil.longMessage("Unsupported sign-in confirmation: " + signInResult.getSignInState());
                            break;
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Sign-in error", e);
                if (e instanceof UserNotConfirmedException){
                    runOnUiThread(() -> {
                        cancelInProgress();
                        ToastUtil.longMessage("User is not confirmed.");
                    });
                }else if(e instanceof NotAuthorizedException){
                    runOnUiThread(() -> {
                        cancelInProgress();
                        ToastUtil.longMessage("Incorrect username or password.");
                    });
                }else if(e instanceof UserNotFoundException){
                    runOnUiThread(() -> {
                        cancelInProgress();
                        ToastUtil.longMessage("User does not exist.");
                    });
                }else{
                    runOnUiThread(() -> {
                        cancelInProgress();
                        ToastUtil.longMessage(e.getMessage());
                    });
                }
            }
        });

//        JavaThreadPool.getInstance().excute(new Runnable() {
//
//            @Override
//            public void run() {
//                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
//                JSONObject pJsonObject = new JSONObject();
//                pJsonObject.put("a", a);
//                pJsonObject.put("p", p);
//                pJsonObject.put("tz", DateUtil.getCurrentTimeZone());
//                pJsonObject.put("lang",
//                        Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry());
//                String result = HttpRequestUtils
//                        .requestoOkHttpPost(server + "/jdm/s3/u/login", pJsonObject, LoginActivity.this);
////                String result = HttpRequestUtils
////                        .requestHttpServer(
////                                 server + "/jdm/service/applogin?v="
////                                        + URLEncoder.encode(
////                                        SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP)),
////                                LoginActivity.this, defHandler);
//                // -1用户名为空，-2密码为空，-3用户不存在，-4密码不正确,-5服务器错误
//                if (result != null && result.length() > 10) {
//                    JSONObject rson = null;
//                    try {
//                        rson = JSONObject.parseObject(result);
//                    } catch (Exception e) {
//                        LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
//                    }
//                    if (rson == null) {
//                        defHandler.post(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                cancelInProgress();
//                                Toast.makeText(LoginActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
//                            }
//                        });
//                        return;
//                    }
//                    MiPushClient.setAlias(LoginActivity.this, String.valueOf(rson.getLongValue("id")), null);
//
//                    dcsp.putString(Constant.LOGIN_ACCOUNT, rson.getString("account"))
//                            .putString(Constant.LOGIN_ACCOUNT_ORIGINAL, aaa)
//                            .putString(Constant.LOGIN_CODE, rson.getString("code"))
//                            .putString(Constant.LOGIN_PWD, p)
//                            .putLong(Constant.LOGIN_APPID, rson.getLongValue("id"))
//                            .putString(Constant.LOGIN_LOGO, (rson.containsKey("logo") ? rson.getString("logo") : ""))
//                            .putInt(DataCenterSharedPreferences.Constant.LOGIN_PHONESMS, 0)
//                            .commit();
//                    AppUserInfo userInfo = new AppUserInfo();
//                    userInfo.setId(rson.getLongValue("id"));
//                    userInfo.setAccount(rson.getString("account"));
//                    userInfo.setLogo(rson.getString("logo"));
//                    userInfo.setEmail(rson.getString("email"));
//                    userInfo.setMobile(rson.getString("mobile"));
//                    userInfo.setCode(rson.getString("code"));
//                    userInfo.setRole(rson.getString("role"));
//                    DatabaseOperator.getInstance().insertOrUpdateUserInfo(userInfo);
//                    dcsp.putBoolean(Constant.IS_TURN, true).commit();//调用登录接口设置一次
//                    if (rson.get("name") != null) {
//                        dcsp.putString(Constant.LOGIN_APPNAME, rson.getString("name")).commit();
//                    }
//                    if (rson.get("role") != null) {
//                        dcsp.putString(Constant.LOGIN_ROLE, rson.getString("role")).commit();
//                    } else {
//                        dcsp.putString(Constant.LOGIN_ROLE, Constant.ROLE_NORMAL).commit();
//                    }
//                    Message msg = Message.obtain();
//                    msg.what = 10;
//                    msg.obj = rson.toJSONString();
//                    defHandler.sendMessage(msg);
//                } else if ("-8".equals(result)) {
//                    defHandler.post(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            cancelInProgress();
//                            Toast.makeText(LoginActivity.this, getString(R.string.login_request_account_empty),
//                                    Toast.LENGTH_LONG).show();
//                        }
//                    });
//                } else if ("-9".equals(result)) {
//                    defHandler.post(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            cancelInProgress();
//                            Toast.makeText(LoginActivity.this, getString(R.string.login_request_password_empty),
//                                    Toast.LENGTH_LONG).show();
//                        }
//                    });
//                } else if ("-3".equals(result)) {
//                    defHandler.post(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            cancelInProgress();
//                            Toast.makeText(LoginActivity.this, getString(R.string.login_request_no_user),
//                                    Toast.LENGTH_LONG).show();
//                        }
//                    });
//                } else if ("-4".equals(result)) {
//                    defHandler.post(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            cancelInProgress();
//                            Toast.makeText(LoginActivity.this, getString(R.string.login_request_password_error),
//                                    Toast.LENGTH_LONG).show();
//                        }
//                    });
//                } else if ("-5".equals(result)) {
//                    defHandler.post(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            cancelInProgress();
//                            Toast.makeText(LoginActivity.this, getString(R.string.net_error_servererror),
//                                    Toast.LENGTH_LONG).show();
//                        }
//                    });
//                }
//            }
//        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_disclaimer:
                Intent disintent = new Intent();
                disintent.setClass(LoginActivity.this, DisclaimerActivity.class);
                startActivity(disintent);
                break;
            case R.id.login_disclaimer_select:
                isAgreeDisclaimer = !isAgreeDisclaimer;
                dcsp.putBoolean(Constant.START_APP_FIRST, isAgreeDisclaimer).commit();
                changeDisclaimer(isAgreeDisclaimer);
                break;
            case R.id.login_disclaimer_text:
                isAgreeDisclaimer = !isAgreeDisclaimer;
                dcsp.putBoolean(Constant.START_APP_FIRST, isAgreeDisclaimer).commit();
                changeDisclaimer(isAgreeDisclaimer);
                break;
            case R.id.rl_gcode://选择国家
                Intent intent2 = new Intent();
                intent2.setClass(LoginActivity.this, GCodeListActivity.class);
                startActivityForResult(intent2, 11);
                break;
            case R.id.forget_passwd:
                forget_passwd.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
                forget_passwd.getPaint().setAntiAlias(true);
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), RegisterActivity.class);
                intent.putExtra("flag", 3);
                startActivity(intent);
                break;
            case R.id.login_qq:
                if (!isAgreeDisclaimer) {
                    ToastUtil.longMessage(getString(R.string.activity_disclaimer_mustagree));
                //    Toast.makeText(this, getString(R.string.activity_disclaimer_mustagree), Toast.LENGTH_SHORT).show();
                    return;
                }
//                Platform qq = getPlatform(QQ.NAME);
//                authorize(qq);
                break;
            case R.id.login_wechat:
                if (!isAgreeDisclaimer) {
                    ToastUtil.longMessage(getString(R.string.activity_disclaimer_mustagree));
                 //   Toast.makeText(this, getString(R.string.activity_disclaimer_mustagree), Toast.LENGTH_SHORT).show();
                    return;
                }
//                Platform wechat = getPlatform(Wechat.NAME);
//                authorize(wechat);
                break;
            case R.id.login_facebook:
                if (!isAgreeDisclaimer) {
                    ToastUtil.longMessage(getString(R.string.activity_disclaimer_mustagree));
                   // Toast.makeText(this, getString(R.string.activity_disclaimer_mustagree), Toast.LENGTH_SHORT).show();
                    return;
                }
//                Platform facebook = getPlatform(Facebook.NAME);
//                authorize(facebook);
                break;
            case R.id.login_twitter:
                if (!isAgreeDisclaimer){
                    ToastUtil.longMessage(getString(R.string.activity_disclaimer_mustagree));
              //      Toast.makeText(this, getString(R.string.activity_disclaimer_mustagree), Toast.LENGTH_SHORT).show();
                    return;
                }
//                Platform twitter = getPlatform(Twitter.NAME);
//                authorize(twitter);
                break;
            case R.id.login_google:
                if (!isAgreeDisclaimer){
                    ToastUtil.longMessage(getString(R.string.activity_disclaimer_mustagree));
              //      Toast.makeText(this, getString(R.string.activity_disclaimer_mustagree), Toast.LENGTH_SHORT).show();
                    return;
                }

                AWSMobileClient.getInstance().showSignIn(
                        LoginActivity.this,
                        SignInUIOptions.builder()
                                .hostedUIOptions(HostedUIOptions.builder().scopes("openid","email").identityProvider("Google").build())
                                .build(),
                        new Callback<UserStateDetails>() {
                            @Override
                            public void onResult(UserStateDetails result) {
                                Log.d(TAG, "google login result: " + result.getUserState());
                                cancelInProgress();
                                Intent intent = new Intent();
                                switch (result.getUserState()){
                                    case SIGNED_IN:
                                        intent.setClass(getApplication(), DeviceMainActivity.class);
                                        break;
                                }
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "onError: ", e);
                            }
                        }
                );

//                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                        .requestIdToken(getString(R.string.default_web_client_id))
//                        .requestEmail()
//                        .build();
//
//                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
//
//                googleSignInButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//                        startActivityForResult(signInIntent, RC_SIGN_IN);
//                    }
//                });

//                Platform google = getPlatform(GooglePlus.NAME);
//                authorize(google);
                break;
            case R.id.login_to_register:
                Intent intent1 = new Intent();
                intent1.setClass(getApplicationContext(), RegisterActivity.class);
                intent1.putExtra("flag", 1);
                startActivity(intent1);
                break;
            case R.id.login_to_forgot:
                intent1 = new Intent();
                intent1.setClass(getApplicationContext(), RegisterActivity.class);
                intent1.putExtra("flag", 3);
                startActivity(intent1);
                break;
            default:
                break;
        }
    }

    // 点击空白区域隐藏软键盘
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View v = getCurrentFocus();
        if ((v != null && (v instanceof EditText))) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDestroy() {
        if (defHandler!=null){
            defHandler.removeMessages(MSG_TONETWORK_TESTPAGE);
        }
        super.onDestroy();
    }
}
