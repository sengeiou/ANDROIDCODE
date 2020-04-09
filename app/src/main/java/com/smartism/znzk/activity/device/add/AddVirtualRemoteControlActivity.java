package com.smartism.znzk.activity.device.add;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.pickerview.OptionsPickerView;
import com.smartism.znzk.view.pickerview.OptionsPickerView.OnOptionsSelectListener;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

/**
 * 添加虚拟遥控器
 * @author 王建 2016年03月01日
 *
 */
public class AddVirtualRemoteControlActivity extends ActivityParentActivity implements OnClickListener{
	private ZhujiInfo zhuji;
	private RelativeLayout key_layout,type_layout,pl_layout;
	private ArrayList<Integer> keys;
	private OptionsPickerView<Integer> keyOptions;
	private ArrayList<String> types;
	private OptionsPickerView<String> typeOptions;
	private ArrayList<String> pls;
	private OptionsPickerView<String> plOptions;
	private TextView t_pl,t_key,t_type;
	private int key;
	private String type,pl;

	private Handler.Callback mCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					cancelInProgress();
					Toast.makeText(AddVirtualRemoteControlActivity.this,getString(R.string.activity_add_vrc_success), Toast.LENGTH_LONG).show();
					finish();
					break;

				default:
					break;
			}
			return false;
		}
	};
	private Handler defaultHandler = new WeakRefHandler(mCallback);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_virtual_remote_control);
		initView();
		initOptions();
	}
	private void initView(){
		zhuji = (ZhujiInfo) getIntent().getSerializableExtra("zhuji");
		t_key = (TextView) findViewById(R.id.vrc_key);
		t_type = (TextView) findViewById(R.id.vrc_type);
		t_pl = (TextView) findViewById(R.id.vrc_pl);
		key_layout = (RelativeLayout) findViewById(R.id.vrc_key_layout);
		key_layout.setOnClickListener(this);
		type_layout = (RelativeLayout) findViewById(R.id.vrc_type_layout);
		type_layout.setOnClickListener(this);
		pl_layout = (RelativeLayout) findViewById(R.id.vrc_pl_layout);
		pl_layout.setOnClickListener(this);
		//遥控器频率
		if (!MainApplication.app.getAppGlobalConfig().isShowFrequency()) {
			pl_layout.setVisibility(View.GONE);
			key_layout.setVisibility(View.GONE);
		}
	}
	
	private void initOptions(){
		keyOptions = new OptionsPickerView<Integer>(this);
		keys = new ArrayList<Integer>();
		for (int i = 1; i <= 16; i++) {
			keys.add(i);
		}
		keyOptions.setTitle(getString(R.string.activity_add_vrc_key));
		keyOptions.setPicker(keys);
		keyOptions.setCyclic(false);
		keyOptions.setOnoptionsSelectListener(new OnOptionsSelectListener() {
			@Override
			public void onOptionsSelect(int options1, int option2, int options3) {
				key = keys.get(options1);
				t_key.setText(String.valueOf(key));
			}
		});
		
		typeOptions = new OptionsPickerView<String>(this);
		types = new ArrayList<String>();
		if (MainApplication.app.getAppGlobalConfig().isFourKeyTelecontrol()) {
			//巨将默认 315 4键遥控器
			pl = "315";
			key = 4;
			for (char i = 'A'; i <= 'J'; i++) {
				types.add(String.valueOf(i));
			}
		}else{
			types.add("1527");
		}
		typeOptions.setTitle(getString(R.string.activity_add_vrc_type));
		typeOptions.setPicker(types);
		typeOptions.setCyclic(false);
		typeOptions.setOnoptionsSelectListener(new OnOptionsSelectListener() {
			@Override
			public void onOptionsSelect(int options1, int option2, int options3) {
				type = types.get(options1);
				t_type.setText(type);
			}
		});
		
		plOptions = new OptionsPickerView<String>(this);
		pls = new ArrayList<String>();
		pls.add("315");
		pls.add("433");
		plOptions.setTitle(getString(R.string.activity_add_vrc_pl));
		plOptions.setPicker(pls);
		plOptions.setCyclic(false);
		plOptions.setOnoptionsSelectListener(new OnOptionsSelectListener() {
			@Override
			public void onOptionsSelect(int options1, int option2, int options3) {
				pl = pls.get(options1);
				t_pl.setText(pl);
			}
		});
	}
	/***返回按钮**/
	public void back(View v){
		finish();
	}
	@Override
	protected void onDestroy() {
		defaultHandler.removeCallbacksAndMessages(null);
		defaultHandler = null;
		super.onDestroy();
	}
	/**
	 * 创建按钮
	 * @param v
	 */
	public void create(View v){
		if (key <=0 || key > 16 || StringUtils.isEmpty(type) || StringUtils.isEmpty(pl)) {
			Toast.makeText(AddVirtualRemoteControlActivity.this,getString(R.string.net_error_programs), Toast.LENGTH_LONG).show();
			return;
		}
		showInProgress(getString(R.string.submit_ing), false, true);
		JavaThreadPool.getInstance().excute(new Runnable() {
			
			@Override
			public void run() {
				try {
					String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
					JSONObject object = new JSONObject();
					object.put("did", zhuji.getId());
					object.put("k", key);
					object.put("s", type);
					object.put("p", pl);
//					String result = HttpRequestUtils.requestHttpServer(server+"/jdm/service/evrc?v="+URLEncoder.encode(SecurityUtil.crypt(object.toJSONString(), Constant.KEY_HTTP), "UTF-8"),AddVirtualRemoteControlActivity.this,defaultHandler);
					String result=HttpRequestUtils.requestoOkHttpPost(  server + "/jdm/s3/d/evrc",object,AddVirtualRemoteControlActivity.this);

					if(result!=null && result.equals("0")){
						SyncMessageContainer.getInstance().produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
						defaultHandler.sendEmptyMessage(1);
					}
				} catch (Exception e) {
					Log.e(TAG, "异常",e);
				}
			}
		});
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.vrc_key_layout:
			keyOptions.show();
			break;
		case R.id.vrc_type_layout:
			typeOptions.show();
			break;
		case R.id.vrc_pl_layout:
			plOptions.show();
		default:
			break;
		}
	}
}
