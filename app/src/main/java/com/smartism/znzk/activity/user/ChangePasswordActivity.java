package com.smartism.znzk.activity.user;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.WeakRefHandler;

public class ChangePasswordActivity extends ActivityParentActivity {
	private LinearLayout changepw_old;
	private EditText password_old,password_new,password_confirm;
	private View view;
	private int phone;
	private Handler.Callback mCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			if (msg.what == 10) { //修改成功
				Toast.makeText(ChangePasswordActivity.this, getString(R.string.activity_changepassword_success), Toast.LENGTH_LONG).show();
				logout();
			}
			return false;
		}
	};
	private Handler defHandler = new WeakRefHandler(mCallback);
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_changepassword);
		//启动activity时不自动弹出软键盘
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		phone = dcsp.getInt(Constant.LOGIN_PHONESMS, 0);
		view = findViewById(R.id.changepw_old_view);
        password_old = (EditText) findViewById(R.id.password_old);
        password_new = (EditText) findViewById(R.id.password_new);
        password_confirm = (EditText) findViewById(R.id.password_confirm);
		password_new.setTransformationMethod(PasswordTransformationMethod.getInstance());
		password_confirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
		changepw_old = (LinearLayout) findViewById(R.id.ll_changepw_old);
		if (phone == 1) {
			changepw_old.setVisibility(View.GONE);
			view.setVisibility(View.GONE);
			password_old.setText("123456");
		}else{
			changepw_old.setVisibility(View.VISIBLE);
			view.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * 提交修改密码
	 * @param v
	 */
	public void reset(View v){
		showInProgress(getString(R.string.loading), false, true);
		final String o = password_old.getText().toString();
		final String n = password_new.getText().toString();
		final String c = password_confirm.getText().toString();

		if ( "".equals(n) || "".equals(c)) {
			Toast.makeText(ChangePasswordActivity.this, getString(R.string.activity_changepassword_error_empty), Toast.LENGTH_LONG).show();
			cancelInProgress();
			return;
		}
		if (!n.equals(c)) {
			cancelInProgress();
			Toast.makeText(ChangePasswordActivity.this, getString(R.string.activity_changepassword_error_confirm), Toast.LENGTH_LONG).show();
			return;
		}
		JavaThreadPool.getInstance().excute(new Runnable() {
			
			@Override
			public void run() {
				String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
				String oldPW = "";
				if (phone == 1) {
					oldPW = dcsp.getString(Constant.LOGIN_PWD,"");
				}else{
					oldPW = SecurityUtil.MD5(o);
				}
				JSONObject pJsonObject = new JSONObject();
//				pJsonObject.put("uid",dcsp.getLong(Constant.LOGIN_APPID, 0));
				pJsonObject.put("op", oldPW);
				pJsonObject.put("np", SecurityUtil.MD5(n));
				String result = HttpRequestUtils.requestoOkHttpPost(server+"/jdm/s3/u/cp",pJsonObject,ChangePasswordActivity.this);
//				String result = HttpRequestUtils.requestHttpServer(server+"/jdm/service/changepassword?v="+URLEncoder.encode(SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP),"UTF-8"),ChangePasswordActivity.this,defHandler);
				if("0".equals(result)){
					defHandler.sendEmptyMessage(10);
				}if("-3".equals(result)){
					defHandler.post(new Runnable() {
						
						@Override
						public void run() {
							cancelInProgress();
							Toast.makeText(ChangePasswordActivity.this, getString(R.string.activity_changepassword_error_oldpass), Toast.LENGTH_LONG).show();
						}
					});
				}
			}
		});
	}
	public void back(View v){
		finish();
	}
}
