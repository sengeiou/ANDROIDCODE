package com.smartism.znzk.activity.user;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;

public class ResetPasswordActivity extends ActivityParentActivity implements View.OnClickListener {
    private EditText account;
    private Button reset_btn;
    private LinearLayout ll_vertify_code, ll_password, ll_confirm;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 10) { //注册成功
                Toast.makeText(ResetPasswordActivity.this, getString(R.string.reset_password_success), Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_resetpassword);
        setContentView(R.layout.activity_register);
        //启动activity时不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        account = (EditText) findViewById(R.id.register_email_edit);
        ll_vertify_code = (LinearLayout) findViewById(R.id.ll_vertify_code);
        ll_vertify_code.setVisibility(View.GONE);
        ll_password = (LinearLayout) findViewById(R.id.ll_password);
        ll_password.setVisibility(View.GONE);
        ll_confirm = (LinearLayout) findViewById(R.id.ll_confirm);
        ll_confirm.setVisibility(View.GONE);
        View view = findViewById(R.id.view);
        view.setVisibility(View.GONE);

        TextView title = (TextView) findViewById(R.id.regiter_title);
        title.setText(getString(R.string.resetpassword_zhaohuititle));

        reset_btn = (Button)  findViewById(R.id.register_next_btn);
        reset_btn.setOnClickListener(this);
        reset_btn.setEnabled(true);
        reset_btn.setText(getString(R.string.resetpassword_zhaohuibutton));
    }

    /**
     * 提交重置密码
     *
     * @param v
     */
    public void next(View v) {
        showInProgress(getString(R.string.reset_password_submiting), false, true);
        String a = account.getText().toString();
        if ("".equals(a)) {
            Toast.makeText(ResetPasswordActivity.this, getString(R.string.reset_password_noaccount), Toast.LENGTH_LONG).show();
            cancelInProgress();
            return;
        }
        if (a.contains("@")) {
            //邮箱格式化转换
            String acc = a.substring(0, a.lastIndexOf("@"));
            String yu = a.substring(a.lastIndexOf("@"));
            if (yu != null) {
                yu = yu.toLowerCase();
                yu = yu.replaceAll("。", ".");
            }
            a = acc + yu;
        }
        final String e = a;
        account.setText(e);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("a", e);
                String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/u/rp", pJsonObject, ResetPasswordActivity.this);
//				String result = HttpRequestUtils.requestHttpServer(server+"/jdm/service/resetpassword?v="+URLEncoder.encode(SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP),"UTF-8"),ResetPasswordActivity.this,defHandler);
                //-1账号不能为空
                if ("0".equals(result)) {
                    defHandler.sendEmptyMessage(10);
                }
                if ("-6".equals(result)) {
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ResetPasswordActivity.this, getString(R.string.reset_password_nosetemail), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-3".equals(result)) {
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ResetPasswordActivity.this, getString(R.string.reset_password_sendemailfailed), Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-5".equals(result)) {
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ResetPasswordActivity.this, getString(R.string.reset_password_noaccounthave), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    public void back(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.register_next_btn:
                showInProgress(getString(R.string.reset_password_submiting), false, true);
                String a = account.getText().toString();
                if ("".equals(a)) {
                    Toast.makeText(ResetPasswordActivity.this, getString(R.string.reset_password_noaccount), Toast.LENGTH_LONG).show();
                    cancelInProgress();
                    return;
                }
                if (a.contains("@")) {
                    //邮箱格式化转换
                    String acc = a.substring(0, a.lastIndexOf("@"));
                    String yu = a.substring(a.lastIndexOf("@"));
                    if (yu != null) {
                        yu = yu.toLowerCase();
                        yu = yu.replaceAll("。", ".");
                    }
                    a = acc + yu;
                }
                final String e = a;
                account.setText(e);
                JavaThreadPool.getInstance().excute(new Runnable() {

                    @Override
                    public void run() {
                        String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                        JSONObject pJsonObject = new JSONObject();
                        pJsonObject.put("a", e);
                        String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/u/rp", pJsonObject, ResetPasswordActivity.this);
//				String result = HttpRequestUtils.requestHttpServer(server+"/jdm/service/resetpassword?v="+URLEncoder.encode(SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP),"UTF-8"),ResetPasswordActivity.this,defHandler);
                        //-1账号不能为空
                        if ("0".equals(result)) {
                            defHandler.sendEmptyMessage(10);
                        }
                        if ("-6".equals(result)) {
                            defHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(ResetPasswordActivity.this, getString(R.string.reset_password_nosetemail), Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-3".equals(result)) {
                            defHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(ResetPasswordActivity.this, getString(R.string.reset_password_sendemailfailed), Toast.LENGTH_LONG).show();
                                }
                            });
                        } else if ("-5".equals(result)) {
                            defHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast.makeText(ResetPasswordActivity.this, getString(R.string.reset_password_noaccounthave), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
                break;
        }
    }
}
