package com.smartism.znzk.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.WeakRefHandler;

import org.apache.commons.lang.StringUtils;

import java.util.Locale;

public class UserMessageRegisterActivity extends ActivityParentActivity implements OnClickListener{
	private Button sure;
	private int isEmail;
	private int flag = 1;

	private Handler.Callback mCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			if (msg.what == 10) { //注册成功
				setResult(111);
				if (flag==1){
					Toast.makeText(UserMessageRegisterActivity.this, getString(R.string.login_tip_registersuccess), Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(UserMessageRegisterActivity.this, getString(R.string.login_reset_pass_succesmessage), Toast.LENGTH_SHORT).show();
				}

				finish();
			}
			return false;
		}
	};
	private Handler defHandler = new WeakRefHandler(mCallback);
	private EditText pass,pass_confirm;
	Intent intent = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_message_register);
		intent = getIntent();
		isEmail = intent.getIntExtra("isEmail",-1);
		flag = intent.getIntExtra("flag",1);
		sure = (Button) findViewById(R.id.register_detemine_btn);
		sure.setOnClickListener(this);
		pass = (EditText) findViewById(R.id.pass_edit);
		pass_confirm = 	(EditText) findViewById(R.id.pass_confirm_edit);
		pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
		pass_confirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
	}
	public void determine(View v){
		showInProgress(getString(R.string.submit_ing), false, true);
		//final String a = user.getText().toString();
		if (pass.getText().toString().length()<6){
			defHandler.post(new Runnable() {

				@Override
				public void run() {
					cancelInProgress();
					Toast.makeText(UserMessageRegisterActivity.this, getString(R.string.register_tip_password_length), Toast.LENGTH_LONG).show();
				}
			});
			return;
		}
		if (!pass.getText().toString().equals(pass_confirm.getText().toString())) {
			defHandler.post(new Runnable() {

				@Override
				public void run() {
					cancelInProgress();
					Toast.makeText(UserMessageRegisterActivity.this, getString(R.string.register_tip_password_confirm), Toast.LENGTH_LONG).show();
				}
			});
			return;
		}
		final String p = pass.getText().toString();
		final String email = intent.getStringExtra("email");
		final String code = intent.getStringExtra("code");
		/*final String n = name.getText().toString();
		final String m = mobile.getText().toString();d
		final String e = email.getText().toString();*/
		final String l = Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry();
		JavaThreadPool.getInstance().excute(new Runnable() {
			
			@Override
			public void run() {
				String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
				JSONObject pJsonObject = new JSONObject();
				String http = "";
				if (flag==1) {
					if (!StringUtils.isEmpty(p)) {
						pJsonObject.put("password", p);
					}
					pJsonObject.put("type", "android");
					pJsonObject.put("code", code);
					pJsonObject.put("lang", l);
					pJsonObject.put("istc", MainApplication.app.getAppGlobalConfig().isTc());

					if (isEmail == -1) {
						pJsonObject.put("conntry", "0086");
						pJsonObject.put("mobile", email);
						http = "/jdm/s3/u/regbysms";
					} else {
						pJsonObject.put("email", email);
						http = "/jdm/s3/u/reg";
					}
				}else{
					if (!StringUtils.isEmpty(p)) {
						pJsonObject.put("np", SecurityUtil.MD5(p));
					}
					pJsonObject.put("code", code);
					pJsonObject.put("conntry", "0086");
					pJsonObject.put("mobile", email);
					http = "/jdm/s3/u/rpsms";
				}
				Log.e("wxb", "p:"+p+"-"+"type:"+"android"+"-"+"mp:"+getJdmVersionPrefix()+"-"+"code:"+code+"-"+"lang:"+l+"-"+"istc:"+dcsp.getBoolean("isTC", false)+"-"+"mobile:"+email+"-");
				final String result = HttpRequestUtils.requestoOkHttpPost(server+http,pJsonObject,UserMessageRegisterActivity.this);
//				final String result = HttpRequestUtils.requestHttpServer(server+"/jdm/service/reg?v="+URLEncoder.encode(SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP)),UserMessageRegisterActivity.this,defHandler);
				//-1参数为空  -2邮箱为空 -3邮箱格式不正确  -4邮箱已经存在 -5邮件发送失败
				if("-3".equals(result)){
					defHandler.post(new Runnable() {
						
						@Override
						public void run() {
							cancelInProgress();
							if (flag==1){
								Toast.makeText(UserMessageRegisterActivity.this, getString(R.string.register_tip_email_erro), Toast.LENGTH_LONG).show();
							}else {
								Toast.makeText(UserMessageRegisterActivity.this, getString(R.string.activity_phone_number_formaterror), Toast.LENGTH_LONG).show();
							}
						}
					});
				}
				else if("-4".equals(result)){
					defHandler.post(new Runnable() {
						
						@Override
						public void run() {
							cancelInProgress();
							if (flag==1){
								Toast.makeText(UserMessageRegisterActivity.this, getString(R.string.register_tip_email_isin), Toast.LENGTH_LONG).show();
							}else {
								Toast.makeText(UserMessageRegisterActivity.this, getString(R.string.register_tip_phone_isin), Toast.LENGTH_LONG).show();
							}

						}
					});
				}
				else if("-5".equals(result)){
					defHandler.post(new Runnable() {
						
						@Override
						public void run() {
							cancelInProgress();
							if (flag==1){
								Toast.makeText(UserMessageRegisterActivity.this, getString(R.string.register_tip_email_send_filde), Toast.LENGTH_LONG).show();
							}else {
								Toast.makeText(UserMessageRegisterActivity.this, getString(R.string.register_tip_phone_send_filde), Toast.LENGTH_LONG).show();
							}
						}
					});
				}else if("-6".equals(result)){
					defHandler.post(new Runnable() {
						
						@Override
						public void run() {
							cancelInProgress();
							if (flag==1){
								Toast.makeText(UserMessageRegisterActivity.this, getString(R.string.register_codeerror), Toast.LENGTH_LONG).show();
							}else {
								Toast.makeText(UserMessageRegisterActivity.this, getString(R.string.activity_phone_verificationcode_error), Toast.LENGTH_LONG).show();
							}
						}
					});
				}else if(!StringUtils.isEmpty(result) && result.length() > 3){
					defHandler.sendEmptyMessage(10);
				}else if ("0".equals(result)){
					defHandler.sendEmptyMessage(10);
				}

			}
		});
	}
	public void back(View v){
		finish();
	}
	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.register_detemine_btn:
			determine(arg0);
			break;

		default:
			break;
		}
	}

}
