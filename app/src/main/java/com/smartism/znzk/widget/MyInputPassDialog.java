package com.smartism.znzk.widget;

import com.p2p.core.P2PHandler;
import com.smartism.znzk.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class MyInputPassDialog extends Dialog implements OnClickListener {
	private String title;
	private String contactID;
	private TextView txTitle, btnText1, btnText2;
	private EditText etPass;
	private OnCustomDialogListener listener;

	public MyInputPassDialog(Context context, String title, String contactid,
			OnCustomDialogListener listener) {
		super(context, R.style.CustomnewInputDialog);
		this.title = title;
		this.contactID = contactid;
		this.listener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_myinputpass);
		initUI();
		initData();
	}

	private void initUI() {
		txTitle = (TextView) findViewById(R.id.title_text);
		etPass = (EditText) findViewById(R.id.input1);
		btnText1 = (TextView) findViewById(R.id.button1_text);
		btnText2 = (TextView) findViewById(R.id.button2_text);
		btnText1.setOnClickListener(this);
		btnText2.setOnClickListener(this);
	}

	private void initData() {
		txTitle.setText(title + contactID);
	}

	public interface OnCustomDialogListener {
		public void check(String password, String id);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1_text:
			final String password = etPass.getText().toString();
			listener.check(password, contactID);
			break;
		case R.id.button2_text:
			this.dismiss();
			break;

		default:
			break;
		}

	}

}
