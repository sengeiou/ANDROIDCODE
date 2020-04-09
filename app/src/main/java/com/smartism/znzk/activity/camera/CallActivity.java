package com.smartism.znzk.activity.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.p2p.core.BaseCallActivity;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PView;
import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.camera.P2PConnect;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.db.camera.DataManager;
import com.smartism.znzk.db.camera.NearlyTell;
import com.smartism.znzk.global.AppConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.widget.HeaderView;

public class CallActivity extends BaseCallActivity implements OnClickListener {
	// test svn
	Context mContext;
	TextView top_text, reject_text, title_text;
	RelativeLayout accept, reject, layout_accept;
	boolean isOutCall;
	ImageView call_anim;
	HeaderView header_img;
	String callId;
	String contactName;
	String ipFlag;
	int type;
	int contactType;
	String password;
	boolean isRegFilter = false;

	boolean isAccept = false;
	boolean isReject = false;
	boolean isSurpportOpenDoor=false;

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
				| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		mContext = this;
		setContentView(R.layout.activity_call);

		isOutCall = this.getIntent().getBooleanExtra("isOutCall", false);
		callId = this.getIntent().getStringExtra("callId");
		contactName = this.getIntent().getStringExtra("contactName");
		ipFlag = this.getIntent().getStringExtra("ipFlag");
		type = this.getIntent().getIntExtra("type", -1);
		password = this.getIntent().getStringExtra("password");
		contactType=this.getIntent().getIntExtra("contactType",-1);
		isSurpportOpenDoor=this.getIntent().getBooleanExtra("isSurpportOpenDoor", false);
		if (!Utils.hasDigit(callId)) {
			if (type == Constants.P2P_TYPE.P2P_TYPE_MONITOR) {
				T.showShort(mContext, R.string.tab_message);
			} else {
				T.showShort(mContext, R.string.message);
			}

			finish();
		} else {
			P2PConnect.setCurrent_state(P2PConnect.P2P_STATE_CALLING);
			P2PConnect.setCurrent_call_id(callId);
			initComponent();
			regFilter();
//			startWatcher();

			String push_mesg = NpcCommon.mThreeNum
					+ ":"
					+ mContext.getResources().getString(
							R.string.p2p_call_push_mesg);
			// if (!P2PHandler.getInstance().call(NpcCommon.mThreeNum, password,
			// isOutCall, type, callId, ipFlag, push_mesg)) {
			// finish();
			// }
			P2PHandler.getInstance().call(NpcCommon.mThreeNum, "0", true,
					Constants.P2P_TYPE.P2P_TYPE_MONITOR, "1", "1", push_mesg,
					AppConfig.VideoMode, callId, MainApplication.GWELL_LOCALAREAIP);
		}
	}

	@Override
	public void onHomePressed() {
		// TODO Auto-generated method stub
		super.onHomePressed();
		reject();
	}

//	private void startWatcher() {
//		mPhoneWatcher = new PhoneWatcher(mContext);
//		mPhoneWatcher
//				.setOnCommingCallListener(new PhoneWatcher.OnCommingCallListener() {
//
//					@Override
//					public void onCommingCall() {
//						// TODO Auto-generated method stub
//						reject();
//					}
//
//				});
//		mPhoneWatcher.startWatcher();
//	}

	public void initComponent() {
		top_text = (TextView) findViewById(R.id.top_text);
		accept = (RelativeLayout) findViewById(R.id.accept);
		layout_accept = (RelativeLayout) findViewById(R.id.layout_accept);
		reject = (RelativeLayout) findViewById(R.id.reject);
		reject_text = (TextView) findViewById(R.id.reject_text);
		title_text = (TextView) findViewById(R.id.title_text);
		call_anim = (ImageView) findViewById(R.id.call_anim);
		header_img = (HeaderView) findViewById(R.id.header_img);
		header_img.updateImage(callId, false);
		if (isOutCall) {
			reject_text.setText(R.string.hungup);
			layout_accept.setVisibility(RelativeLayout.GONE);
			if (type == Constants.P2P_TYPE.P2P_TYPE_MONITOR) {
				top_text.setText(mContext.getResources().getString(
						R.string.activity_zhujiowner_sure)
						+ "......");
				if (contactName != null && !contactName.equals("")) {
					title_text.setText(contactName);
				} else {
					title_text.setText(callId);
				}
//				call_anim.setImageResource(R.anim.monitor);
			} else {
				if (contactName != null && !contactName.equals("")) {
					title_text.setText(contactName);
				} else {
					title_text.setText(callId);
				}
				call_anim.setImageResource(R.drawable.zhzj_arrow);
				top_text.setText(mContext.getResources().getString(
						R.string.calling_to)
						+ "......");
			}

		} else {
			call_anim.setImageResource(R.drawable.zhzj_arrow);
			reject_text.setText(R.string.reject);
			layout_accept.setVisibility(RelativeLayout.VISIBLE);
			// top_text.setText(mContext.getResources().getString(
			// R.string.calling_from)
			// + " " + callId);
			
	      Contact contact= FList.getInstance().isContact(callId);
	      if(contact==null){
	    	  title_text.setText(callId);
	      }else{
	    	  title_text.setText(contact.contactName);
	      }
	       
		}

		final AnimationDrawable anim = (AnimationDrawable) call_anim
				.getDrawable();
		// anim.start();
		OnPreDrawListener opdl = new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				anim.start();
				return true;
			}

		};
		call_anim.getViewTreeObserver().addOnPreDrawListener(opdl);
		accept.setOnClickListener(this);
		reject.setOnClickListener(this);
	}

	public void regFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.P2P.P2P_ACCEPT);
		filter.addAction(Constants.P2P.P2P_READY);
		filter.addAction(Constants.P2P.P2P_REJECT);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		mContext.registerReceiver(mReceiver, filter);
		isRegFilter = true;
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals(Constants.P2P.P2P_ACCEPT)) {
				int[] type=intent.getIntArrayExtra("type");
				P2PView.type=type[0];
				P2PView.scale=type[1];
				P2PHandler.getInstance().openAudioAndStartPlaying(1);
			} else if (intent.getAction().equals(Constants.P2P.P2P_READY)) {
				Intent intentCall = new Intent();
				if (type == Constants.P2P_TYPE.P2P_TYPE_CALL) {
//					intentCall.setClass(mContext, VideoActivity.class);
				} else if (type == Constants.P2P_TYPE.P2P_TYPE_MONITOR) {
					intentCall.setClass(mContext, AppCompatActivity.class);
					intentCall.putExtra("contactType", contactType);
					intentCall.putExtra("callId", callId);
					intentCall.putExtra("password", password);
					intentCall.putExtra("isSurpportOpenDoor", isSurpportOpenDoor);
				}
				intentCall.putExtra("type", type);
				intentCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intentCall);
				finish();
			} else if (intent.getAction().equals(Constants.P2P.P2P_REJECT)) {
				reject();
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				reject();
			}
		}
	};

	@Override
	public void onBackPressed() {
		reject();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.accept:
			if (!isAccept) {
				isAccept = true;
				P2PHandler.getInstance().accept();
			}
			break;
		case R.id.reject:
			reject();
			break;
		}
	}

	public void reject() {
		if (!isReject) {
			isReject = true;
			P2PHandler.getInstance().finish();
//			if (!activity_stack
//					.containsKey(Constants.ActivityInfo.ACTIVITY_MAINACTIVITY)) {
//				Intent i = new Intent(CallActivity.this, MainActivity.class);
//				startActivity(i);
//			}
			finish();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (isRegFilter) {
			mContext.unregisterReceiver(mReceiver);
			isRegFilter = false;
		}
//
//		if (null != mPhoneWatcher) {
//			mPhoneWatcher.stopWatcher();
//		}
		insertNearly();
	}

	public void insertNearly() {
		NearlyTell nearlyTell = new NearlyTell();
		nearlyTell.activeUser = NpcCommon.mThreeNum;
		nearlyTell.tellId = callId;
		nearlyTell.tellTime = String.valueOf(System.currentTimeMillis());
		nearlyTell.tellState = type;
		if (isOutCall && isReject) {
			nearlyTell.tellState = NearlyTell.TELL_STATE_CALL_OUT_REJECT;
		} else if (isOutCall && !isReject) {
			nearlyTell.tellState = NearlyTell.TELL_STATE_CALL_OUT_ACCEPT;
		} else if (!isOutCall && isReject) {
			nearlyTell.tellState = NearlyTell.TELL_STATE_CALL_IN_REJECT;
		} else {
			nearlyTell.tellState = NearlyTell.TELL_STATE_CALL_IN_ACCEPT;
		}
		DataManager.insertNearlyTell(mContext, nearlyTell);
	}

	@Override
	public int getActivityInfo() {
		// TODO Auto-generated method stub
		return Constants.ActivityInfo.ACTIVITY_CALLACTIVITY;
	}

	@Override
	protected void onGoBack() {
		// TODO Auto-generated method stub
//		MainApplication.app.showNotification();
	}

	@Override
	protected void onGoFront() {
		// TODO Auto-generated method stub
//		MainApplication.app.hideNotification();
	}

	@Override
	protected void onExit() {
		// TODO Auto-generated method stub
//		MyApp.app.hideNotification();
	}

}
