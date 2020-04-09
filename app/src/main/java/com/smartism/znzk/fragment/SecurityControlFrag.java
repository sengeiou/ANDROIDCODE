package com.smartism.znzk.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.camera.MainControlActivity;
import com.smartism.znzk.activity.camera.ModifyNpcPasswordActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.global.Constants;

public class SecurityControlFrag extends BaseFragment implements
		OnClickListener {
	private Context mContext;
	private Contact contact;
	private boolean isRegFilter = false;
	RelativeLayout change_password, change_super_password, automatic_upgrade;
	ImageView img_automatic_upgrade, super_icon;
	ProgressBar progressBar_automatic_upgrade, visitorProgress;
	boolean isOpenAutomaticUpgrade;
	String visitorpwd = "0";
	String idOrIp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mContext =getActivity();
		contact = (Contact) getArguments().getSerializable("contact");
		if(contact!=null){
		idOrIp=contact.contactId;
		if(contact.ipadressAddress!=null){
			String mark=contact.ipadressAddress.getHostAddress();
			String ip=mark.substring(mark.lastIndexOf(".")+1,mark.length());
			if(!ip.equals("")&&ip!=null){
				idOrIp=ip;
				}	
			}
		}
		View view = inflater.inflate(R.layout.fragment_security_control,
				container, false);
		initComponent(view);
		regFilter();
		showProgressVisitorPwd();
		if(contact!=null){
		P2PHandler.getInstance()
				.getNpcSettings(idOrIp, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
		}
		return view;
	}

	private void showProgressVisitorPwd() {
		visitorProgress.setVisibility(RelativeLayout.VISIBLE);
		super_icon.setVisibility(RelativeLayout.GONE);
		change_super_password.setEnabled(false);
	}

	private void showVisitorPwd() {
		visitorProgress.setVisibility(RelativeLayout.GONE);
		super_icon.setVisibility(RelativeLayout.GONE);
		change_super_password.setEnabled(false);
	}

	public void initComponent(View view) {
		change_password = (RelativeLayout) view
				.findViewById(R.id.change_password);
		change_super_password = (RelativeLayout) view
				.findViewById(R.id.change_super_password);
		automatic_upgrade = (RelativeLayout) view
				.findViewById(R.id.automatic_upgrade);
		img_automatic_upgrade = (ImageView) view
				.findViewById(R.id.img_automatic_upgrade);
		progressBar_automatic_upgrade = (ProgressBar) view
				.findViewById(R.id.progressBar_automatic_upgrade);
		visitorProgress = (ProgressBar) view
				.findViewById(R.id.progressBar_visitor_password);
		super_icon = (ImageView) view.findViewById(R.id.super_icon);
		change_password.setOnClickListener(this);
		change_super_password.setOnClickListener(this);
		automatic_upgrade.setOnClickListener(this);
		if(contact!=null){
		if (contact.contactType == P2PValue.DeviceType.IPC||contact.contactType== P2PValue.DeviceType.DOORBELL) {
			change_super_password.setVisibility(RelativeLayout.GONE);
		}
		if (Integer.parseInt(contact.contactId) < 256) {
			change_super_password.setVisibility(RelativeLayout.GONE);
		}
		}
	}

	public void regFilter() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.Action.REFRESH_CONTANTS);
		filter.addAction(Constants.P2P.ACK_RET_SET_AUTOMATIC_UPGRADE);
		filter.addAction(Constants.P2P.RET_GET_VISTOR_PASSWORD);
		// filter.addAction(Constants.P2P.RET_GET_AUTOMATIC_UPGRAD);

		mContext.registerReceiver(mReceiver, filter);
		isRegFilter = true;
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals(Constants.Action.REFRESH_CONTANTS)) {
				Contact c = (Contact) intent.getSerializableExtra("contact");
				if (null != c) {
					contact = c;
				}
			} else if (intent.getAction().equals(Constants.P2P.RET_GET_AUTOMATIC_UPGRAD)) {
				int state = intent.getIntExtra("state", -1);
				if (state == Constants.P2P_SET.AUTOMATIC_UPGRADE.AUTOMATIC_UPGRADE_ON) {
					automatic_upgrade.setVisibility(RelativeLayout.VISIBLE);
					isOpenAutomaticUpgrade = false;
					img_automatic_upgrade
							.setBackgroundResource(R.drawable.zhzj_switch_on);
				} else if (state == Constants.P2P_SET.AUTOMATIC_UPGRADE.AUTOMATIC_UPGRADE_OFF) {
					automatic_upgrade.setVisibility(RelativeLayout.VISIBLE);
					isOpenAutomaticUpgrade = true;
					img_automatic_upgrade
							.setBackgroundResource(R.drawable.zhzj_switch_off);
				}
				showImg_automatic_upgrade();
			} else if (intent.getAction().equals(
					Constants.P2P.ACK_RET_SET_AUTOMATIC_UPGRADE)) {
				int state = intent.getIntExtra("state", -1);
				if (state == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
					if (isOpenAutomaticUpgrade == true) {
						P2PHandler.getInstance().setAutomaticUpgrade(idOrIp,
								contact.contactPassword, 1,MainApplication.GWELL_LOCALAREAIP);
					} else {
						P2PHandler.getInstance().setAutomaticUpgrade(idOrIp,
								contact.contactPassword, 0,MainApplication.GWELL_LOCALAREAIP);
					}
					showImg_automatic_upgrade();
				} else if (state == Constants.P2P_SET.ACK_RESULT.ACK_SUCCESS) {
					if (isOpenAutomaticUpgrade == true) {
						isOpenAutomaticUpgrade = false;
						img_automatic_upgrade
								.setBackgroundResource(R.drawable.zhzj_switch_on);
					} else {
						isOpenAutomaticUpgrade = true;
						img_automatic_upgrade
								.setBackgroundResource(R.drawable.zhzj_switch_off);
					}
					showImg_automatic_upgrade();
				}
			} else if (intent.getAction().equals(
					Constants.P2P.RET_GET_VISTOR_PASSWORD)) {
				showVisitorPwd();
				int visitorPwd = intent.getIntExtra("visitorpwd", -1);
				if (visitorPwd == -1) {
					isSeeVisitorPwd = false;
				} else {
					isSeeVisitorPwd = true;
				}
				if (visitorPwd <= 0) {
					visitorpwd = "";
				} else {
					visitorpwd = String.valueOf(visitorPwd);
				}
			}
		}
	};
	private boolean isSeeVisitorPwd = false;

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.change_password:
			Intent modify_password = new Intent(mContext,
					ModifyNpcPasswordActivity.class);
			modify_password.putExtra("contact", contact);
			modify_password.putExtra("deviceInfo",((MainControlActivity)getActivity()).getDeviceInfo());
			mContext.startActivity(modify_password);
			break;
		case R.id.automatic_upgrade:
			if (isOpenAutomaticUpgrade == true) {
				P2PHandler.getInstance().setAutomaticUpgrade(idOrIp,
						contact.contactPassword, 1,MainApplication.GWELL_LOCALAREAIP);
			} else {
				P2PHandler.getInstance().setAutomaticUpgrade(idOrIp,
						contact.contactPassword, 0,MainApplication.GWELL_LOCALAREAIP);
			}
			break;
		case R.id.change_super_password:
			/*Intent modify_visitor_password = new Intent(mContext,
					ModifyNpcVisitorPasswordActivity.class);
			modify_visitor_password.putExtra("visitorpwd", visitorpwd);
			modify_visitor_password
					.putExtra("isSeeVisitorPwd", isSeeVisitorPwd);
			modify_visitor_password.putExtra("contact", contact);
			startActivityForResult(modify_visitor_password, 2);*/
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("dxsactivityresult", "resultCode-->" + resultCode);
		if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
			visitorpwd = data.getStringExtra("visitorpwd");
		}
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		if (isRegFilter) {
			mContext.unregisterReceiver(mReceiver);
			isRegFilter = false;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Intent it = new Intent();
		it.setAction(Constants.Action.CONTROL_BACK);
		mContext.sendBroadcast(it);
	}

	public void showProgress_automatic_upgrade() {
		progressBar_automatic_upgrade.setVisibility(ProgressBar.VISIBLE);
		img_automatic_upgrade.setVisibility(ImageView.GONE);
	}

	public void showImg_automatic_upgrade() {
		progressBar_automatic_upgrade.setVisibility(ProgressBar.GONE);
		img_automatic_upgrade.setVisibility(ImageView.VISIBLE);
	}
}
