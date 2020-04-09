package com.smartism.znzk.activity.user;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.view.LockSetupActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GenstureInitActivity extends Activity {
	
	Button btn_initGensture;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initView();
		initEvent();
	}
	
	private void initView() {
		setContentView(R.layout.activity_init_gensture);
		btn_initGensture=(Button) findViewById(R.id.btn_initGensture);
		
	}

	private void initEvent() {
		btn_initGensture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),LockSetupActivity.class));
				finish();
			}
		});
	}
	
	public void back(View v){
		finish();
	}
}
