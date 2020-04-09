package com.smartism.znzk.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.results.ForgotPasswordResult;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;
import com.amazonaws.services.cognitoidentityprovider.model.UsernameExistsException;
import com.hjq.toast.ToastUtils;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.user.RegisterActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.StringUtils;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.alertview.AlertView;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.smartism.znzk.application.MainApplication.TAG;


/**
 * Created by win7 on 2017/7/20.
 */

public class EmailRegisterFragment extends Fragment implements View.OnClickListener {
    public static final int DELAY_MILLIS = 12 * 1000;
    private EditText email, code, userName, pass_edit,pass_edit2,phone_number;
    private Button next;
    private TextView sendCodeBtn;
    private LinearLayout llPassword,llPassword2,llPhone,llemail;
    private int waitTime = 60;
    private long starttime;
    private static final int TIME_OUT = 100;
    private RegisterActivity mContext;
    private InputMethodManager imm = null;
    private int flag = 1;  //是请求验证码的 “t” ：1、是注册请求，2、登录请求 3、找回密码请求
    private boolean isResend = false;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 10) { //注册成功
                if (defHandler.hasMessages(TIME_OUT)) {
                    defHandler.removeMessages(TIME_OUT);
                }
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
                }

                if (waitTime <= 1) {
                    starttime = 0;

                    defHandler.sendEmptyMessageDelayed(11, 1000);
                } else {
                    defHandler.sendEmptyMessageDelayed(12, 1000);
                }
            } else if (msg.what == 13) {
//				Intent intent = new Intent();
//				intent.setClass(mContext, UserMessageRegisterActivity.class);
//				intent.putExtra("email", email.getText().toString());
//				intent.putExtra("code", codeback);
//				intent.putExtra("isEmail", isEmail);
//				intent.putExtra("flag",flag);
//				startActivityForResult(intent, 1);
            } else if (msg.what == 14) {
                if (defHandler.hasMessages(TIME_OUT)) {
                    defHandler.removeMessages(TIME_OUT);
                }
                mContext.cancelInProgress();
                userName.setEnabled(false);
                pass_edit.setEnabled(false);
                phone_number.setEnabled(false);
                email.setEnabled(false);
//                if (isEmail == -1) {
//                    new AlertView(getString(R.string.tips), getString(R.string.register_phone_codesendsuccess), null, new String[]{getString(R.string.sure)}, null, mContext, AlertView.Style.Alert, null).show();
//                } else {
//                    new AlertView(getString(R.string.tips), getString(R.string.register_codesendsuccess), null, new String[]{getString(R.string.sure)}, null, mContext, AlertView.Style.Alert, null).show();
//                }
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
        View view = inflater.inflate(R.layout.fragment_regis_emai, container, false);
        mContext = (RegisterActivity) getActivity();
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        email = (EditText) view.findViewById(R.id.register_email_edit);
        userName = (EditText) view.findViewById(R.id.register_username_edit);
        code = (EditText) view.findViewById(R.id.register_email_code);
        pass_edit = (EditText) view.findViewById(R.id.pass_edit);
        pass_edit.setTransformationMethod(PasswordTransformationMethod.getInstance());
        pass_edit2 = (EditText) view.findViewById(R.id.pass_edit2);
        pass_edit2.setTransformationMethod(PasswordTransformationMethod.getInstance());
        phone_number = (EditText) view.findViewById(R.id.register_phone_edit);

        llPassword = (LinearLayout) view.findViewById(R.id.ll_password);
        llPassword2 = (LinearLayout) view.findViewById(R.id.ll_password2);
        llemail = (LinearLayout) view.findViewById(R.id.ll_email);
        llPhone = (LinearLayout) view.findViewById(R.id.ll_phone);

        sendCodeBtn = (TextView) view.findViewById(R.id.register_code_btn);
        next = (Button) view.findViewById(R.id.register_next_btn);
        initData();

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    private void initData() {
//        reset_password_email activity_thchart_reminder
//        tv_notice.setText(getString(R.string.activity_thchart_reminder) + getString(R.string.reset_password_email));
        sendCodeBtn.setOnClickListener(this);
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
            llPassword.setVisibility(View.GONE);
            llemail.setVisibility(View.GONE);
            llPhone.setVisibility(View.GONE);
        }
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
                if (s.length() > 0) {
                    sendCodeBtn.setEnabled(true);
                    next.setEnabled(true);
                }
//                changeSendCodeBtn(s.toString());

            }
        });
    }

    public void changeSendCodeBtn(String s) {
        if (s.length() > 0) {
            sendCodeBtn.setEnabled(true);
            next.setEnabled(true);
        } else {
            sendCodeBtn.setEnabled(false);
//                next.setEnabled(false);
        }
    }


    /**
     * 发送验证码
     *
     * @param
     */
    public void sendCodeToEmail() {
        String account = email.getText().toString();
        if (TextUtils.isEmpty(account)) {
            Toast.makeText(mContext, getString(R.string.register_tip_email_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(userName.getText().toString())) {
            ToastUtil.longMessage("Username cannot be empty");
            return;
        }

        if (TextUtils.isEmpty(pass_edit.getText().toString())) {
            ToastUtil.longMessage("Password cannot be empty");
            return;
        }

        if (TextUtils.isEmpty(phone_number.getText().toString())) {
            ToastUtil.longMessage("phone number cannot be empty");
            return;
        }

        if (pass_edit.getText().toString().length() < 8) {
            ToastUtil.longMessage("Password must have length greater than or equal to 8");
            return;
        }

        if (account.contains("@")) {
            //邮箱格式化转换
            String acc = account.substring(0, account.lastIndexOf("@"));
            String yu = account.substring(account.lastIndexOf("@"));
            if (yu != null) {
                yu = yu.toLowerCase();
                yu = yu.replaceAll("。", ".");
            }
            account = acc + yu;
        }
        final String e = account;
        email.setText(e);
        mContext.showInProgress(getString(R.string.submiting), false, true);
        defHandler.sendEmptyMessageDelayed(TIME_OUT, DELAY_MILLIS);
        final String username = userName.getText().toString();
        final String password = pass_edit.getText().toString();

        final Map<String, String> attributes = new HashMap<>();
        attributes.put("email", email.getText().toString());
        attributes.put("phone_number", phone_number.getText().toString());
//        attributes.put("phone_number", "+819080231230");
//        attributes.put("phone_number", "+85254420567");
        attributes.put("name", username);
        if (isResend) {
            AWSMobileClient.getInstance().resendSignUp(username, new Callback<SignUpResult>() {
                @Override
                public void onResult(SignUpResult signUpResult) {
                    Log.i("test", "A verification code has been sent via" +
                            signUpResult.getUserCodeDeliveryDetails().getDeliveryMedium()
                            + " at " +
                            signUpResult.getUserCodeDeliveryDetails().getDestination());
                    defHandler.sendEmptyMessage(14);
                    ToastUtil.longMessage("A verification code has been sent via " +
                                    signUpResult.getUserCodeDeliveryDetails().getDeliveryMedium()
                                    + " at " +
                                    signUpResult.getUserCodeDeliveryDetails().getDestination());
                }

                @Override
                public void onError(Exception e) {
                    Log.e("test", "error",e);
                    mContext.runOnUiThread(() -> {
                        ToastUtil.longMessage("error, please again");
                        mContext.cancelInProgress();
                    });
                }
            });
        }else {
            AWSMobileClient.getInstance().signUp(username, password, attributes, null, new Callback<SignUpResult>() {
                @Override
                public void onResult(final SignUpResult signUpResult) {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            isResend = true;
                            Log.d("test", "Sign-up callback state: " + signUpResult.getConfirmationState());
                            if (!signUpResult.getConfirmationState()) {
                                final UserCodeDeliveryDetails details = signUpResult.getUserCodeDeliveryDetails();
                                defHandler.sendEmptyMessage(14);
                                ToastUtil.longMessage("Confirm sign-up with: " + details.getDestination());
                            } else {
                                ToastUtil.longMessage("Sign-up done.");
                            }
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    Log.e("test", "Sign-up error", e);
                    defHandler.removeMessages(TIME_OUT);
                    if (e instanceof UsernameExistsException) {
                        mContext.runOnUiThread(() -> {
                            ToastUtil.longMessage("User already exists");
                            mContext.cancelInProgress();
                        });
                    } else {
                        mContext.runOnUiThread(() -> {
                            ToastUtil.longMessage("Parameter error");
                            mContext.cancelInProgress();
                        });
                    }
                }
            });
        }
    }
    /**
     * 发送验证码 找回密码
     *
     * @param
     */
    public void sendForgotCodeToUser() {
        if (TextUtils.isEmpty(userName.getText().toString())) {
            ToastUtil.longMessage("Username cannot be empty");
            return;
        }
        mContext.showInProgress(getString(R.string.submiting), false, true);
        defHandler.sendEmptyMessageDelayed(TIME_OUT, DELAY_MILLIS);
        AWSMobileClient.getInstance().forgotPassword(userName.getText().toString(), new Callback<ForgotPasswordResult>() {
            @Override
            public void onResult(ForgotPasswordResult result) {
                Log.d(TAG, "forgot password state: " + result.getState());
                mContext.runOnUiThread(()->{
                    switch (result.getState()) {
                        case CONFIRMATION_CODE:
                            llPassword2.setVisibility(View.VISIBLE);
                            defHandler.sendEmptyMessage(14);
                            ToastUtils.show("Confirmation code is sent to reset password");
                            break;
                        default:
                            Log.e(TAG, "un-supported forgot password state");
                            break;
                    }
                });

            }

            @Override
            public void onError(Exception e) {
                Log.e("test", "error",e);
                mContext.runOnUiThread(() -> {
                    ToastUtil.longMessage("error, please again");
                    mContext.cancelInProgress();
                });
            }
        });
    }



    /**
     * 验证验证码
     *
     * @param
     */
    public void emailCode() {

        final String e = userName.getText().toString();
        final String c = code.getText().toString();
        if (TextUtils.isEmpty(e)) {
            Toast.makeText(mContext, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(c)) {
            Toast.makeText(mContext, getString(R.string.login_regis_code), Toast.LENGTH_LONG).show();
            return;
        }

        defHandler.sendEmptyMessageDelayed(TIME_OUT, DELAY_MILLIS);
        mContext.showInProgress(getString(R.string.submiting), false, true);
        AWSMobileClient.getInstance().confirmSignUp(e, c, new Callback<SignUpResult>() {
            @Override
            public void onResult(final SignUpResult signUpResult) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("test", "Sign-up callback state: " + signUpResult.getConfirmationState());
                        if (!signUpResult.getConfirmationState()) {
                            final UserCodeDeliveryDetails details = signUpResult.getUserCodeDeliveryDetails();
                            ToastUtil.longMessage("Confirm sign-up with: " + details.getDestination());
                        } else {
//                            ToastUtil.longMessage("Sign-up done.");
                            defHandler.sendEmptyMessage(10);
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("test", "Confirm sign-up error", e);
            }
        });
    }
    /**
     * 验证验证码
     *
     * @param
     */
    public void forgotCode() {

        final String p = pass_edit2.getText().toString();
        final String c = code.getText().toString();
        if (TextUtils.isEmpty(p)) {
            Toast.makeText(mContext, "New password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(c)) {
            Toast.makeText(mContext, getString(R.string.login_regis_code), Toast.LENGTH_LONG).show();
            return;
        }

        defHandler.sendEmptyMessageDelayed(TIME_OUT, DELAY_MILLIS);
        mContext.showInProgress(getString(R.string.submiting), false, true);
        AWSMobileClient.getInstance().confirmForgotPassword(p, c, new Callback<ForgotPasswordResult>() {
            @Override
            public void onResult(final ForgotPasswordResult result) {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Sign-up callback state: " + result.getState());

                        switch (result.getState()) {
                            case DONE:
                                defHandler.sendEmptyMessage(10);
                                break;
                            default:
                                mContext.cancelInProgress();
                                ToastUtils.show("un-supported forgot password state");
                                Log.e(TAG, "un-supported forgot password state");
                                break;
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("test", "Confirm sign-up error", e);
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register_code_btn: //发送验证码
                // 隐藏软键盘
                imm.hideSoftInputFromWindow(mContext.getWindow().getDecorView().getWindowToken(), 0);
                if (isRegister()) {
                    sendCodeToEmail();
                }else{
                    sendForgotCodeToUser();
                }
                break;
            case R.id.register_next_btn: //注册
                if (isRegister()) {
                    emailCode();
                }else{
                    forgotCode();
                }
                break;
            default:
                break;
        }
    }

    private boolean isRegister(){
        return flag == 1;
    }
}
