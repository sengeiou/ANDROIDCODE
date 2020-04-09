package com.smartism.znzk.activity.developer;

import java.io.UnsupportedEncodingException;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.communication.protocol.SyncMessage.CodeMenu;
import com.smartism.znzk.communication.protocol.SyncMessage.CommandMenu;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DeveCommandShowActivity extends ActivityParentActivity implements OnClickListener{
	private Button accept,send;
	private TextView accept_t;
	private EditText send_e;
	private ZhujiInfo zhuji;
	int js;
	
	protected BroadcastReceiver broadcastReceiver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_deve_command_show);
		
		accept = (Button) findViewById(R.id.deve_accept);
		accept.setOnClickListener(this);
		send = (Button) findViewById(R.id.deve_send);
		send.setOnClickListener(this);
		accept_t = (TextView) findViewById(R.id.deve_command_s);
		send_e = (EditText) findViewById(R.id.deve_command_e);
		initBroadcastReceiver();
//		zhuji = DatabaseOperator.getInstance(this).queryDeviceZhuJiInfo(dcsp.getString(Constant.APP_MASTERID, ""));
		//替换
		zhuji = DatabaseOperator.getInstance(this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
	}
	@Override
	protected void onResume() {
		super.onResume();
		send_e.setText(dcsp.getString("tmp_send_text",""));
	}
	public void back(View v){
		finish();
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.deve_accept:
			if(js == 1){
				SyncMessageContainer.getInstance().sendMessageToServer(CommandMenu.rq_developertc,CodeMenu.rq_developertc_n, zhuji.getId(), null);
			}else{
				SyncMessageContainer.getInstance().sendMessageToServer(CommandMenu.rq_developertc,CodeMenu.rq_developertc_a, zhuji.getId(), null);
			}
			break;
		case R.id.deve_send:
			if(send_e.getText().length() != 28 && send_e.getText().length() != 16){
				Toast.makeText(this, "发送的码长度必须为28或者16", Toast.LENGTH_SHORT).show();
				return;
			}
			dcsp.putString("tmp_send_text", send_e.getText().toString()).commit();
			JSONObject o = new JSONObject();
			o.put("c", send_e.getText().toString());
			try {
				SyncMessageContainer.getInstance().sendMessageToServer(CommandMenu.rq_developertc,CodeMenu.rq_developertc_c, zhuji.getId(), o.toJSONString().getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				LogUtil.e(getApplicationContext(), "DEVE", "不支持UTF-8编码格式");
			}
			break;
		default:
			break;
		}
	}
	
	private void initBroadcastReceiver(){
    	broadcastReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				if (Actions.DEVE_TC.equals(intent.getAction())) {
					switch (intent.getIntExtra("code",2)) {
					case 0:
						accept.setText("开始接受并显示收到的指令");
						js = 0;
						break;
					case 1:
						js = 1;
						accept.setText("已经进入接收指令状态持续5分钟");
						break;
					case 2:
						String tt = intent.getStringExtra("c");
						StringBuffer buffer = new StringBuffer();
						buffer.append(getString(R.string.activity_deve_show_tid));
						for (int i = 1; i <= tt.length(); i++) {
							if (i%2==0) {
								buffer.append(tt.substring(i-2, i));
								buffer.append(" ");
								if (i==8) {
									buffer.append("\r\n");
									buffer.append(getString(R.string.activity_deve_show_did));
								}
								if (i==16) {
									buffer.append("\r\n");
									buffer.append(getString(R.string.activity_deve_show_datatype));
								}
								if (i==20) {
									buffer.append("\r\n");
									buffer.append(getString(R.string.activity_deve_show_data));
								}
							}
						}
						accept_t.setText(buffer.toString());
						break;
					default:
						break;
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.DEVE_TC);
		registerReceiver(broadcastReceiver, filter);
    }
	
}
