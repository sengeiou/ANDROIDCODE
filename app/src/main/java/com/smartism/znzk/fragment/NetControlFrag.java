package com.smartism.znzk.fragment;

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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.smartism.znzk.R;
import com.smartism.znzk.adapter.camera.WifiAdapter;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.thread.DelayThread;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.view.MyListView;
import com.smartism.znzk.widget.MyInputDialog;
import com.smartism.znzk.widget.NormalDialog;

public class NetControlFrag extends BaseFragment implements OnClickListener {
	private Context mContext;
	private Contact contact;
	private boolean isRegFilter = false;
	private RelativeLayout dialog_input_mask;
	private RelativeLayout net_type_bar, list_wifi_bar;
	private LinearLayout net_type_radio, list_wifi_content;
	private ProgressBar progressBar_net_type, progressBar_list_wifi;
	private RadioButton radio_one, radio_two;

	private WifiAdapter mAdapter;
	private MyListView list;
	private int last_net_type;
	private int last_modify_net_type;
	private int last_modify_wifi_type;
	private String last_modify_wifi_name;
	private String last_modify_wifi_password;
	private NormalDialog dialog_loading;
	private MyInputDialog dialog_input;
	private String idOrIp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	public NetControlFrag() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mContext = getActivity();
		contact = (Contact) getArguments().getSerializable("contact");
		idOrIp=contact.contactId;
		if(contact.ipadressAddress!=null){
			String mark=contact.ipadressAddress.getHostAddress();
			String ip=mark.substring(mark.lastIndexOf(".")+1,mark.length());
			if(!ip.equals("")&&ip!=null){
				idOrIp=ip;
			}
		}
		View view = inflater.inflate(R.layout.fragment_net_control, container,
				false);
		initComponent(view);
		regFilter();

		showProgress_net_type();

		P2PHandler.getInstance()
				.getNpcSettings(idOrIp, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
		return view;
	}

	public void initComponent(View view) {
		dialog_input_mask = (RelativeLayout) view
				.findViewById(R.id.dialog_input_mask);

		net_type_bar = (RelativeLayout) view.findViewById(R.id.net_type_bar);
		list_wifi_bar = (RelativeLayout) view.findViewById(R.id.list_wifi_bar);

		net_type_radio = (LinearLayout) view.findViewById(R.id.net_type_radio);
		list_wifi_content = (LinearLayout) view
				.findViewById(R.id.list_wifi_content);

		progressBar_net_type = (ProgressBar) view
				.findViewById(R.id.progressBar_net_type);
		progressBar_list_wifi = (ProgressBar) view
				.findViewById(R.id.progressBar_list_wifi);

		list = (MyListView) view.findViewById(R.id.list_wifi);
		mAdapter = new WifiAdapter(mContext, this);
		list.setAdapter(mAdapter);

		radio_one = (RadioButton) view.findViewById(R.id.radio_one);
		radio_two = (RadioButton) view.findViewById(R.id.radio_two);
		radio_one.setOnClickListener(this);
		radio_two.setOnClickListener(this);
	}

	public void regFilter() {
		IntentFilter filter = new IntentFilter();

		filter.addAction(Constants.Action.CLOSE_INPUT_DIALOG);

		filter.addAction(Constants.P2P.ACK_RET_GET_NPC_SETTINGS);

		filter.addAction(Constants.P2P.ACK_RET_SET_NET_TYPE);
		filter.addAction(Constants.P2P.RET_SET_NET_TYPE);
		filter.addAction(Constants.P2P.RET_GET_NET_TYPE);

		filter.addAction(Constants.P2P.ACK_RET_SET_WIFI);
		filter.addAction(Constants.P2P.ACK_RET_GET_WIFI);
		filter.addAction(Constants.P2P.RET_SET_WIFI);
		filter.addAction(Constants.P2P.RET_GET_WIFI);

		mContext.registerReceiver(mReceiver, filter);
		isRegFilter = true;
	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals(Constants.Action.CLOSE_INPUT_DIALOG)) {
				if (null != dialog_input) {
					dialog_input.hide(dialog_input_mask);
				}
			} else if (intent.getAction()
					.equals(Constants.P2P.RET_GET_NET_TYPE)) {
				int type = intent.getIntExtra("type", -1);
				if (type == Constants.P2P_SET.NET_TYPE_SET.NET_TYPE_WIRED) {
					last_net_type = Constants.P2P_SET.NET_TYPE_SET.NET_TYPE_WIRED;
					radio_one.setChecked(true);
					if (contact.contactType != P2PValue.DeviceType.NPC) {
						showProgressWiFiList();
						P2PHandler.getInstance().getWifiList(idOrIp,
								contact.contactPassword,MainApplication.GWELL_LOCALAREAIP);
					} else {
						hideWiFiList();
					}

				} else if (type == Constants.P2P_SET.NET_TYPE_SET.NET_TYPE_WIFI) {
					last_net_type = Constants.P2P_SET.NET_TYPE_SET.NET_TYPE_WIFI;
					radio_two.setChecked(true);
					showProgressWiFiList();
					P2PHandler.getInstance().getWifiList(idOrIp,
							contact.contactPassword,MainApplication.GWELL_LOCALAREAIP);
				}
				showNetType();
				setRadioEnable(true);
			} else if (intent.getAction()
					.equals(Constants.P2P.RET_SET_NET_TYPE)) {
				int result = intent.getIntExtra("result", -1);
				if (result == Constants.P2P_SET.NET_TYPE_SET.SETTING_SUCCESS) {
					last_net_type = last_modify_net_type;
					if (last_modify_net_type == Constants.P2P_SET.NET_TYPE_SET.NET_TYPE_WIFI) {
						showProgressWiFiList();
						P2PHandler.getInstance().getWifiList(idOrIp,
								contact.contactPassword,MainApplication.GWELL_LOCALAREAIP);
						radio_two.setChecked(true);
					} else {
						hideWiFiList();
						radio_one.setChecked(true);
					}
					T.showShort(mContext, R.string.device_set_tip_success);
				} else {
					if (last_net_type == Constants.P2P_SET.NET_TYPE_SET.NET_TYPE_WIFI) {
						showProgressWiFiList();
						radio_two.setChecked(true);
					} else {
						hideWiFiList();
						radio_one.setChecked(true);
					}
					T.showShort(mContext, R.string.operator_error);
				}
				showNetType();
				setRadioEnable(true);
			} else if (intent.getAction().equals(Constants.P2P.RET_GET_WIFI)) {
				int iCurrentId = intent.getIntExtra("iCurrentId", -1);
				int iCount = intent.getIntExtra("iCount", 0);
				int[] iType = intent.getIntArrayExtra("iType");
				int[] iStrength = intent.getIntArrayExtra("iStrength");
				String[] names = intent.getStringArrayExtra("names");
				mAdapter.updateData(iCurrentId, iCount, iType, iStrength, names);
				showWiFiList();
				list.setSelection(iCurrentId);
			} else if (intent.getAction().equals(Constants.P2P.RET_SET_WIFI)) {
				int result = intent.getIntExtra("result", -1);
				if (result == Constants.P2P_SET.WIFI_SET.SETTING_SUCCESS) {

				} else if (result == 20) {
					T.showShort(mContext, R.string.wifi_pwd_format_error);
				} else {
					T.showShort(mContext, R.string.operator_error);
				}

			} else if (intent.getAction().equals(
					Constants.P2P.ACK_RET_GET_NPC_SETTINGS)) {
				int result = intent.getIntExtra("result", -1);
				if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
					Intent i = new Intent();
					i.setAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
					mContext.sendBroadcast(i);
				} else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
					Log.e("my", "net error resend:get npc settings");
					P2PHandler.getInstance().getNpcSettings(idOrIp,
							contact.contactPassword,MainApplication.GWELL_LOCALAREAIP);
				}
			} else if (intent.getAction().equals(
					Constants.P2P.ACK_RET_SET_NET_TYPE)) {
				int result = intent.getIntExtra("result", -1);
				if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
					Intent i = new Intent();
					i.setAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
					mContext.sendBroadcast(i);
				} else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
					Log.e("my", "net error resend:set npc settings net type");
					if (null != dialog_loading && dialog_loading.isShowing()) {
						P2PHandler.getInstance().setNetType(idOrIp,
								contact.contactPassword, last_modify_net_type,MainApplication.GWELL_LOCALAREAIP);
					}
				} else if (result == Constants.P2P_SET.ACK_RESULT.ACK_SUCCESS) {
					if (null != dialog_loading) {
						dialog_loading.dismiss();
					}
					hideWiFiList();
					showProgress_net_type();
					P2PHandler.getInstance().getNpcSettings(idOrIp,
							contact.contactPassword,MainApplication.GWELL_LOCALAREAIP);
					setRadioEnable(true);
				}
			} else if (intent.getAction()
					.equals(Constants.P2P.ACK_RET_GET_WIFI)) {
				int result = intent.getIntExtra("result", -1);
				if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
					Intent i = new Intent();
					i.setAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
					mContext.sendBroadcast(i);
				} else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
					Log.e("my", "net error resend:get wifi list");
					P2PHandler.getInstance().getWifiList(idOrIp,
							contact.contactPassword,MainApplication.GWELL_LOCALAREAIP);
				}
			} else if (intent.getAction()
					.equals(Constants.P2P.ACK_RET_SET_WIFI)) {
				int result = intent.getIntExtra("result", -1);
				if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
					Intent i = new Intent();
					i.setAction(Constants.Action.CONTROL_SETTING_PWD_ERROR);
					mContext.sendBroadcast(i);
				} else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
					Log.e("my", "net error resend:set wifi");
					if (null != dialog_loading && dialog_loading.isShowing()) {
						P2PHandler.getInstance().setWifi(idOrIp,
								contact.contactPassword, last_modify_wifi_type,
								last_modify_wifi_name,
								last_modify_wifi_password,MainApplication.GWELL_LOCALAREAIP);
					}

				} else if (result == Constants.P2P_SET.ACK_RESULT.ACK_SUCCESS) {
					if (null != dialog_loading) {
						dialog_loading.dismiss();
					}
					hideWiFiList();
					showProgress_net_type();
					P2PHandler.getInstance().getNpcSettings(idOrIp,
							contact.contactPassword,MainApplication.GWELL_LOCALAREAIP);
				}
			}

		}
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
			case R.id.radio_one:
				changeNetType(Constants.P2P_SET.NET_TYPE_SET.NET_TYPE_WIRED);
				break;
			case R.id.radio_two:
				changeNetType(Constants.P2P_SET.NET_TYPE_SET.NET_TYPE_WIFI);
				break;
		}
	}

	public void changeNetType(final int type) {
		final NormalDialog dialog = new NormalDialog(mContext, mContext
				.getResources().getString(R.string.warning), mContext
				.getResources().getString(R.string.modify_net_warning),
				mContext.getResources().getString(R.string.change), mContext
				.getResources().getString(R.string.cancel));
		switch (last_net_type) {
			case Constants.P2P_SET.NET_TYPE_SET.NET_TYPE_WIFI:
				dialog.setOnButtonCancelListener(new NormalDialog.OnButtonCancelListener() {

					@Override
					public void onClick() {
						// TODO Auto-generated method stub
						radio_two.setChecked(true);
						dialog.dismiss();
					}
				});
				break;
			case Constants.P2P_SET.NET_TYPE_SET.NET_TYPE_WIRED:
				dialog.setOnButtonCancelListener(new NormalDialog.OnButtonCancelListener() {

					@Override
					public void onClick() {
						// TODO Auto-generated method stub
						radio_one.setChecked(true);
						dialog.dismiss();
					}
				});
				break;
		}
		dialog.setOnButtonOkListener(new NormalDialog.OnButtonOkListener() {

			@Override
			public void onClick() {
				// TODO Auto-generated method stub
				// progressBar_net_type.setVisibility(RelativeLayout.VISIBLE);
				if (null == dialog_loading) {
					dialog_loading = new NormalDialog(mContext, mContext
							.getResources().getString(R.string.verification),
							"", "", "");
					dialog_loading.setStyle(NormalDialog.DIALOG_STYLE_LOADING);
				}
				dialog_loading.showDialog();
				new DelayThread(
						Constants.SettingConfig.SETTING_CLICK_TIME_DELAY,
						new DelayThread.OnRunListener() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								last_modify_net_type = type;
								P2PHandler.getInstance().setNetType(idOrIp,
										contact.contactPassword, type,MainApplication.GWELL_LOCALAREAIP);
							}
						}).start();
				setRadioEnable(false);

			}
		});

		dialog.showNormalDialog();
		dialog.setCanceledOnTouchOutside(false);
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

	public void setRadioEnable(boolean bool) {
		if (bool) {
			radio_one.setEnabled(true);
			radio_two.setEnabled(true);
		} else {
			radio_one.setEnabled(false);
			radio_two.setEnabled(false);
		}
	}

	public void showProgress_net_type() {
		progressBar_net_type.setVisibility(RelativeLayout.VISIBLE);
		net_type_radio.setVisibility(RelativeLayout.GONE);
	}

	public void showNetType() {
		progressBar_net_type.setVisibility(RelativeLayout.GONE);
		net_type_radio.setVisibility(RelativeLayout.VISIBLE);
	}

	public void hideWiFiList() {
		list_wifi_bar.setVisibility(RelativeLayout.GONE);
		list_wifi_content.setVisibility(RelativeLayout.GONE);
	}

	public void showProgressWiFiList() {
		list_wifi_bar.setVisibility(RelativeLayout.VISIBLE);
		progressBar_list_wifi.setVisibility(RelativeLayout.VISIBLE);
		list_wifi_content.setVisibility(RelativeLayout.GONE);
	}

	public void showWiFiList() {
		list_wifi_bar.setVisibility(RelativeLayout.VISIBLE);
		progressBar_list_wifi.setVisibility(RelativeLayout.GONE);
		list_wifi_content.setVisibility(RelativeLayout.VISIBLE);
	}

	public void showModfyWifi(final int type, final String name) {
		Log.e("wifiname", "wifiname" + name + "  " + name.length());
		dialog_input = new MyInputDialog(mContext);
		dialog_input.setTitle(mContext.getResources().getString(
				R.string.change_wifi)
				+ "(" + name + ")");
		dialog_input.setBtn1_str(mContext.getResources().getString(
				R.string.sure));
		dialog_input.setBtn2_str(mContext.getResources().getString(
				R.string.cancel));
		dialog_input
				.setOnButtonOkListener(new MyInputDialog.OnButtonOkListener() {

					@Override
					public void onClick() {
						// TODO Auto-generated method stub
						String password = dialog_input.getInput1Text();
						if (type != 0) {
							if ("".equals(password.trim())) {
								T.showShort(mContext, R.string.input_wifi_pwd);
								return;
							}
						}
						dialog_input.hide(dialog_input_mask);
						if (null == dialog_loading) {
							dialog_loading = new NormalDialog(mContext,
									mContext.getResources().getString(
											R.string.verification), "", "", "");
							dialog_loading
									.setStyle(NormalDialog.DIALOG_STYLE_LOADING);
						}
						dialog_loading.showDialog();
						last_modify_wifi_type = type;
						last_modify_wifi_name = name;
						last_modify_wifi_password = password;
						if (type == 0) {
							P2PHandler.getInstance().setWifi(idOrIp,
									contact.contactPassword, type, name, "0",MainApplication.GWELL_LOCALAREAIP);
						} else {
							P2PHandler.getInstance().setWifi(idOrIp,
									contact.contactPassword, type, name,
									password,MainApplication.GWELL_LOCALAREAIP);
						}
					}
				});
		dialog_input.show(dialog_input_mask);
		dialog_input.setInput1HintText(R.string.input_wifi_pwd);
	}

	public boolean IsInputDialogShowing() {
		if (null != dialog_input) {
			return dialog_input.isShowing();
		} else {
			return false;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Intent it = new Intent();
		it.setAction(Constants.Action.CONTROL_BACK);
		mContext.sendBroadcast(it);
	}
}
