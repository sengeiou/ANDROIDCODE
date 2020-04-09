package com.smartism.znzk.activity.camera;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.macrovideo.sdk.custom.DeviceInfo;
import com.p2p.core.P2PValue;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.APContact;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.db.camera.DataManager;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.global.VList;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.MaxLengthWatcher;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.ImageUtils;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.widget.NormalDialog;

public class ModifyContactActivity extends BaseActivity implements OnClickListener {
	private static final int RESULT_GETIMG_FROM_CAMERA = 0x11;
	private static final int RESULT_GETIMG_FROM_GALLERY = 0x12;
	private static final int RESULT_CUT_IMAGE = 0x13;
	private TextView mSave;
	private ImageView mBack;
	Context mContext;
	EditText contactName, contactPwd;
	LinearLayout layout_device_pwd;
	TextView contactId;
	private String device;
	Contact mModifyContact;
	NormalDialog dialog;
	RelativeLayout modify_header;
	TextView error_account1;
	private Bitmap tempHead;
	private DeviceInfo info;
	private ZhujiInfo zhuji;
	private String result;

	private Handler.Callback mCallback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					Log.e("添加", "添加成功");
					FList.getInstance().update(mModifyContact);
					Intent refreshContans = new Intent();
//				refreshContans.setAction(Constants.Action.REFRESH_CONTANTS);
					refreshContans.putExtra("contact", mModifyContact);
					mContext.sendBroadcast(refreshContans);
					T.showShort(mContext, R.string.device_set_tip_success);

					//发送广播到mainadapter中更新修改后的名字
					Intent adapterintent = new Intent();
					adapterintent.setAction(Constants.Action.ADAPTER_CHANGE_TITLE);
					adapterintent.putExtra("title", mModifyContact.contactName);
					adapterintent.putExtra("contactId", mModifyContact.contactId);
					mContext.sendBroadcast(adapterintent);
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
		setContentView(R.layout.activity_modify_contact);
		mModifyContact = (Contact) getIntent().getSerializableExtra("contact");
		device = getIntent().getStringExtra("device");
		if (getIntent().getIntExtra("deviceid", 0) != 0) {
			info = VList.getInstance().findById(getIntent().getIntExtra("deviceid", 0));
		}
		mContext = this;
//		zhuji = DatabaseOperator.getInstance(getApplicationContext()).queryDeviceZhuJiInfo(
//				DataCenterSharedPreferences.getInstance(mContext, Constant.CONFIG)
//						.getString(Constant.APP_MASTERID, ""));
		//替换
		zhuji = DatabaseOperator.getInstance(getApplicationContext()).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());

		initCompent();
		Log.e("phone_ipdress", "ipdress=" + Utils.getPhoneIpdress());
	}

	public void initCompent() {
		contactId = (TextView) findViewById(R.id.contactId);
		contactName = (EditText) findViewById(R.id.contactName);
		//限制字符串的长度
		contactName.addTextChangedListener(new MaxLengthWatcher(10,contactName));
		contactPwd = (EditText) findViewById(R.id.contactPwd);
		// contactPwd.setTransformationMethod(PasswordTransformationMethod
		// .getInstance());
		layout_device_pwd = (LinearLayout) findViewById(R.id.layout_device_pwd);
		mBack = (ImageView) findViewById(R.id.back_btn);
		mSave = (TextView) findViewById(R.id.save);
		modify_header = (RelativeLayout) findViewById(R.id.modify_header);
		error_account1 = (TextView) findViewById(R.id.error_account1);
		/*
		 * if (mModifyContact.contactType != P2PValue.DeviceType.PHONE) {
		 * layout_device_pwd.setVisibility(RelativeLayout.VISIBLE); } else {
		 * layout_device_pwd.setVisibility(RelativeLayout.GONE); }
		 */
		if (mModifyContact != null) {
			contactId.setText(mModifyContact.contactId);
			contactName.setText(mModifyContact.contactName);
			if (mModifyContact.mode == P2PValue.DeviceMode.AP_MODE) {
				error_account1.setText(R.string.device_wifi_pwd);
				contactPwd.setHint(R.string.input_device_wifi_pwd);
				APContact apContact = DataManager.findAPContactByActiveUserAndContactId(mContext, NpcCommon.mThreeNum,
						mModifyContact.contactId);
				if (apContact != null) {
					contactPwd.setText(apContact.Pwd);
				}
			} else {
				contactPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
				error_account1.setText(R.string.contact_pwd);
				contactPwd.setText(mModifyContact.userPassword);
				contactPwd.setHint(R.string.input_contact_pwd);
			}
		} else if (info != null) {
			contactId.setText(info.getnDevID() + "");
			contactName.setText(info.getStrName());
		}
		modify_header.setOnClickListener(this);
		mBack.setOnClickListener(this);
		mSave.setOnClickListener(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == RESULT_GETIMG_FROM_CAMERA) {
			try {
				Bundle extras = data.getExtras();
				tempHead = (Bitmap) extras.get("data");
				Log.e("my", tempHead.getWidth() + ":" + tempHead.getHeight());
				ImageUtils.saveImg(tempHead, Constants.Image.USER_HEADER_PATH,
						Constants.Image.USER_HEADER_TEMP_FILE_NAME);

				Intent cutImage = new Intent(mContext, CutImageActivity.class);
				cutImage.putExtra("contact", mModifyContact);
				startActivityForResult(cutImage, RESULT_CUT_IMAGE);
			} catch (NullPointerException e) {
				Log.e("my", "用户终止..");
			}
		} else if (requestCode == RESULT_GETIMG_FROM_GALLERY) {

			try {
				Uri uri = data.getData();
				tempHead = ImageUtils.getBitmap(ImageUtils.getAbsPath(mContext, uri),
						Constants.USER_HEADER_WIDTH_HEIGHT, Constants.USER_HEADER_WIDTH_HEIGHT);
				ImageUtils.saveImg(tempHead, Constants.Image.USER_HEADER_PATH,
						Constants.Image.USER_HEADER_TEMP_FILE_NAME);

				Intent cutImage = new Intent(mContext, CutImageActivity.class);
				cutImage.putExtra("contact", mModifyContact);
				startActivityForResult(cutImage, RESULT_CUT_IMAGE);

			} catch (NullPointerException e) {
				Log.e("my", "用户终止..");
			}
		} else if (requestCode == RESULT_CUT_IMAGE) {
			Log.e("my", resultCode + "");
			try {
				if (resultCode == 1) {
					Intent refreshContans = new Intent();
					refreshContans.setAction(Constants.Action.REFRESH_CONTANTS);
					refreshContans.putExtra("contact", mModifyContact);
					mContext.sendBroadcast(refreshContans);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void destroyTempHead() {
		if (tempHead != null && !tempHead.isRecycled()) {
			tempHead.recycle();
			tempHead = null;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back_btn:
			finish();
			break;
		case R.id.save:
			if (mModifyContact != null) {
				if (mModifyContact.mode == P2PValue.DeviceMode.AP_MODE) {
					saveWifiName();
				} else {
					save();
				}
			} else if (info != null) {
				Intent in = new Intent();
				in.setAction(Constants.Action.REFRESH_DATA);
				sendBroadcast(in);
				finish();

			}

			break;
		case R.id.modify_header:

			break;
		default:
			break;
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			finish();
		}
		return  super.onKeyDown(keyCode, event);
	}
	void save() {
		String input_name = contactName.getText().toString();
		// String input_pwd = contactPwd.getText().toString();

		/*
		 * if (input_name != null && input_name.trim().equals("")) {
		 * T.showShort(mContext, R.string.input_contact_name); return; }
		 * 
		 * if (mModifyContact.contactType != P2PValue.DeviceType.PHONE) { if
		 * (input_pwd != null && input_pwd.trim().equals("")) {
		 * T.showShort(mContext, R.string.input_contact_pwd); return; }
		 * if(input_pwd.charAt(0) == '0'|| input_pwd.length() > 30){
		 * T.showShort(mContext, R.string.device_password_invalid); return; } }
		 */
		mModifyContact.contactName = input_name;
		// mModifyContact.userPassword=input_pwd;
		// String pwd=P2PHandler.getInstance().EntryPassword(input_pwd);
		// mModifyContact.contactPassword =pwd;
		mModifyContact.defenceState = Constants.DefenceState.DEFENCE_STATE_LOADING;
		addDevice();
	}

	public void saveWifiName() {
		String input_name = contactName.getText().toString();
		String input_pwd = contactPwd.getText().toString();
		if (input_name != null && input_name.trim().equals("")) {
			T.showShort(mContext, R.string.input_contact_name);
			return;
		}

		if (mModifyContact.contactType != P2PValue.DeviceType.PHONE) {
			if (input_pwd != null && input_pwd.trim().equals("")) {
				T.showShort(mContext, R.string.input_device_wifi_pwd);
				return;
			}
			if (input_pwd.length() < 8) {
				// 密码必须8位
				T.showShort(mContext, R.string.wifi_pwd_error);
				return;
			}
			mModifyContact.contactName = input_name;
			mModifyContact.wifiPassword = input_pwd;
			APContact apContact = changeContactToAPContact(mModifyContact);
			// 连接成功页面需要跳转并且保存数据库
			if (!DataManager.isAPContactExist(mContext, apContact.activeUser, apContact.contactId)) {
				// 保存
				Log.e("dxsTest", "保存-->" + apContact.activeUser + "mModifyContact.wifiPassword-->" + apContact.Pwd);
				DataManager.insertAPContact(mContext, apContact);
			} else {
				// 更新
				Log.e("dxsTest", "更新-->" + apContact.activeUser + "mModifyContact.wifiPassword-->" + apContact.Pwd);
				DataManager.updateAPContact(mContext, apContact);
			}
			Intent refreshContans = new Intent();
			refreshContans.setAction(Constants.Action.REFRESH_CONTANTS);
			refreshContans.putExtra("contact", mModifyContact);
			mContext.sendBroadcast(refreshContans);
			T.showShort(mContext, R.string.device_set_tip_success);
			finish();
		}
	}

	private APContact changeContactToAPContact(Contact contact) {
		if (NpcCommon.mThreeNum == null) {
			NpcCommon.mThreeNum = "0517401";
		}
		APContact ap = DataManager.findAPContactByActiveUserAndContactId(mContext, NpcCommon.mThreeNum,
				contact.contactId);
		if (ap != null) {
			ap.Pwd = contact.wifiPassword;
			return ap;
		} else {
			return new APContact(contact.contactId, contact.contactName, contact.contactName, contact.wifiPassword,
					NpcCommon.mThreeNum);
		}
	}

	private void addDevice() {
		final long did = zhuji.getId();
		final String c = "jiwei";
		final String id = mModifyContact.contactId;
		final String n = mModifyContact.contactName;
		final String p = mModifyContact.contactPassword;
		JavaThreadPool.getInstance().excute(new Runnable() {

			@Override
			public void run() {
				DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(ModifyContactActivity.this,
						Constant.CONFIG);
				String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
				JSONObject pJsonObject = new JSONObject();
				pJsonObject.put("did", did);
				pJsonObject.put("c", c);
				pJsonObject.put("id", id);
				pJsonObject.put("n", n);
				pJsonObject.put("p", p);
				String http =  server + "/jdm/s3/ipcs/add";
				String 	result = HttpRequestUtils.requestoOkHttpPost(http, pJsonObject,dcsp);
				// -1参数为空 -2校验失败 -10服务器不存在
				 if ("0".equals(result)) {
					defaultHandler.sendEmptyMessage(1);
				}else{
					 defaultHandler.post(new Runnable() {
						 @Override
						 public void run() {
							 T.showShort(mContext, R.string.addfailed);
						 }
					 });
				 }
			}
		});
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		destroyTempHead();
	}

	@Override
	public int getActivityInfo() {
		// TODO Auto-generated method stub
		return Constants.ActivityInfo.ACTIVITY_MODIFYCONTACTACTIVITY;
	}

	@Override
	protected int onPreFinshByLoginAnother() {
		return 0;
	}
}
