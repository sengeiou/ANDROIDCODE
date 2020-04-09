package com.smartism.znzk.activity.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.DataManager;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.Account;
import com.smartism.znzk.fragment.ContactFrag;
import com.smartism.znzk.global.AccountPersist;
import com.smartism.znzk.global.AppConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.global.NpcCommon.NETWORK_TYPE;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.util.camera.WifiUtils;
import com.smartism.znzk.widget.NormalDialog;

public class MainActivity extends BaseActivity implements OnClickListener {
    public Context mContext;
    private ImageView dials_img, contact_img, recent_img, settings_img, discover_img;
    private RelativeLayout contact, dials, recent, settings, discover;
    private ImageView mAddCamere;
    private ImageView iv_back;
    boolean isRegFilter = false;
    private int currFrag = 0;
    private TextView tv_contact, tv_message, tv_image, tv_more;

    private String[] fragTags = new String[]{"contactFrag", "keyboardFrag", "nearlyTellFrag", "utilsFrag",
            "settingFrag", "apcontactFrag"};
    private ContactFrag contactFrag;
    public static boolean isConnectApWifi = false;
    private NormalDialog dialog;
    public int i;
    private String number;
    private DeviceInfo device;
    private ZhujiInfo zhuji;

    @Override
    protected void onCreate(Bundle arg0) {
//        P2PHandler.getInstance().p2pInit(this, new P2PListener(), new SettingListener());
        super.onCreate(arg0);
        mContext = this;
        device = (DeviceInfo) getIntent().getSerializableExtra("device");
        if (device!=null){
            zhuji = DatabaseOperator.getInstance().queryDeviceZhuJiInfo(device.getZj_id());
        }
        number = getIntent().getStringExtra("v380");
        i = getIntent().getIntExtra("int", 0);
        if (i == 1) {
            setTheme(android.R.style.Theme_NoDisplay);
        } else {
//            setTheme(android.R.style.Theme_Holo_NoActionBar);
        }

        if (number != null && number.equals("v380")) {
            initComponent();
            if (null == contactFrag) {
                contactFrag = new ContactFrag();
                contactFrag.setNumber(number);
            }
            replaceFragment(R.id.fragContainer, contactFrag, fragTags[0]);
            changeIconShow();
        } else {
            isConnectApWifi = getIntent().getBooleanExtra("isConnectApWifi", false);
            isConnectWifi(isConnectApWifi);
        }

    }

    private void isConnectWifi(boolean is) {
        initComponent();
        new FList();
        NpcCommon.verifyNetwork(mContext);
        regFilter();
        connect();
        currFrag = 0;
        if (null == contactFrag) {
            contactFrag = new ContactFrag();
        }
        replaceFragment(R.id.fragContainer, contactFrag, fragTags[0]);
        changeIconShow();
        if (i == 1) {
//            finish();
        }
    }

    private ChangeMode Change = new ChangeMode() {

        @Override
        public void OnChangeMode() {
            String wifiName = WifiUtils.getInstance().getConnectWifiName();
            WifiUtils.getInstance().DisConnectWifi(wifiName);
            // WifiUtils.getInstance().SetWiFiEnAble(false);
            Intent canel = new Intent();
            canel.setAction(Constants.Action.ACTION_SWITCH_USER);
            mContext.sendBroadcast(canel);
        }
    };

    public interface ChangeMode {
        void OnChangeMode();
    }

    private ChangeMode change;

    public void setOnChangeMode(ChangeMode Change) {
        this.Change = Change;
    }

    /**
     * 设置页面初始化
     */
    public void initComponent() {
        setContentView(R.layout.activity_main_camera);
        mAddCamere = (ImageView) findViewById(R.id.button_add);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        dials = (RelativeLayout) findViewById(R.id.icon_keyboard);
        dials_img = (ImageView) findViewById(R.id.icon_keyboard_img);
        contact = (RelativeLayout) findViewById(R.id.icon_contact);
        contact_img = (ImageView) findViewById(R.id.icon_contact_img);
        recent = (RelativeLayout) findViewById(R.id.icon_nearlytell);
        recent_img = (ImageView) findViewById(R.id.icon_nearlytell_img);
        settings = (RelativeLayout) findViewById(R.id.icon_setting);
        settings_img = (ImageView) findViewById(R.id.icon_setting_img);
        discover = (RelativeLayout) findViewById(R.id.icon_discover);
        discover_img = (ImageView) findViewById(R.id.icon_discover_img);
        tv_contact = (TextView) findViewById(R.id.tv_contact);
        tv_message = (TextView) findViewById(R.id.tv_message);
        tv_image = (TextView) findViewById(R.id.tv_image);
        tv_more = (TextView) findViewById(R.id.tv_more);

        dials.setOnClickListener(this);
        contact.setOnClickListener(this);
        recent.setOnClickListener(this);
        settings.setOnClickListener(this);
        discover.setOnClickListener(this);
        iv_back.setOnClickListener(this);
        mAddCamere.setOnClickListener(this);
    }

    public void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.Action.ACTION_NETWORK_CHANGE);
        filter.addAction(Constants.Action.ACTION_SWITCH_USER);
        filter.addAction(Constants.Action.ACTION_EXIT);
        filter.addAction(Constants.Action.RECEIVE_MSG);
        filter.addAction(Constants.Action.RECEIVE_SYS_MSG);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);
        filter.addAction(Constants.Action.ACTION_UPDATE);
        filter.addAction(Constants.Action.SESSION_ID_ERROR);
        filter.addAction(Constants.Action.EXITE_AP_MODE);
        filter.addAction(Constants.Action.ACTIVITY_FINISH);
        filter.addAction(Constants.Action.ACTIVITY_ADDCAMERA);
        filter.addAction(Constants.Action.ACTIVITY_OPENCAMERA);
        filter.addAction("DISCONNECT");

        this.registerReceiver(mReceiver, filter);
        isRegFilter = true;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            Log.e("mReceiver", intent.getAction());
            // TODO Auto-generated method stub
            if (intent.getAction().equals(Constants.Action.ACTION_NETWORK_CHANGE)) {
                boolean isNetConnect = false;
                ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetInfo != null) {
                    if (activeNetInfo.isConnected()) {
                        isNetConnect = true;
                        T.showShort(mContext, getString(R.string.message_net_connect) + activeNetInfo.getTypeName());
                        WifiManager wifimanager = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);
                        if (wifimanager == null) {
                            return;
                        }
                        WifiInfo wifiinfo = wifimanager.getConnectionInfo();
                        if (wifiinfo == null) {
                            return;
                        }
                        if (wifiinfo.getSSID().length() > 0) {
                            String wifiName = Utils.getWifiName(wifiinfo.getSSID());
                            if (wifiName.startsWith(AppConfig.Relese.APTAG)) {
                                String id = wifiName.substring(AppConfig.Relese.APTAG.length());
                                // APList.getInstance().gainDeviceMode(id);
                                // FList.getInstance().gainDeviceMode(id);
                                FList.getInstance().setIsConnectApWifi(id, true);
                            } else {
                                FList.getInstance().setAllApUnLink();
                            }
                        }
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//                        Intent intentNew = new Intent();
//                        intentNew.setAction(Constants.Action.NET_WORK_TYPE_CHANGE);
//                        mContext.sendBroadcast(intentNew);
                        WifiUtils.getInstance().isApDevice();
                    } else {
                        T.showShort(mContext, getString(R.string.network_error) + " " + activeNetInfo.getTypeName());
                    }

                    if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        NpcCommon.mNetWorkType = NETWORK_TYPE.NETWORK_WIFI;
                    } else {
                        NpcCommon.mNetWorkType = NETWORK_TYPE.NETWORK_2GOR3G;
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }

                NpcCommon.setNetWorkState(isNetConnect);

                // Intent intentNew = new Intent();
                // intentNew.setAction(Constants.Action.NET_WORK_TYPE_CHANGE);
                // mContext.sendBroadcast(intentNew);
            } else if (intent.getAction().equals(Constants.Action.ACTION_SWITCH_USER)) {
                Account account = AccountPersist.getInstance().getActiveAccountInfo(mContext);
//				if (!account.three_number.equals("0517401")) {
//					new ExitTask(account).execute();
//				}
                AccountPersist.getInstance().setActiveAccount(mContext, new Account());
                NpcCommon.mThreeNum = "";
                Intent i = new Intent(MainApplication.MAIN_SERVICE_START);
                stopService(i);
                dialog = new NormalDialog(mContext);
                dialog.showLoadingDialog2();
            } else if (intent.getAction().equals(Constants.Action.SESSION_ID_ERROR)) {
                Account account = AccountPersist.getInstance().getActiveAccountInfo(mContext);
                AccountPersist.getInstance().setActiveAccount(mContext, new Account());
                Intent i = new Intent(MainApplication.MAIN_SERVICE_START);
                stopService(i);
                finish();
            } else if (intent.getAction().equals(Constants.Action.ACTION_EXIT)) {
                finish();
            } else if (intent.getAction().equals(Intent.ACTION_LOCALE_CHANGED)) {

            } else if (intent.getAction().equals(Constants.Action.RECEIVE_MSG)) {
                int result = intent.getIntExtra("result", -1);
                String msgFlag = intent.getStringExtra("msgFlag");

                if (result == Constants.P2P_SET.ACK_RESULT.ACK_SUCCESS) {
                    DataManager.updateMessageStateByFlag(mContext, msgFlag, Constants.MessageType.SEND_SUCCESS);
                } else {
                    DataManager.updateMessageStateByFlag(mContext, msgFlag, Constants.MessageType.SEND_FAULT);
                }

            } else if (intent.getAction().equals(Constants.Action.RECEIVE_SYS_MSG)) {

            } else if (intent.getAction().equals(Constants.Action.SETTING_WIFI_SUCCESS)) {
                currFrag = 0;
                if (null == contactFrag) {
                    contactFrag = new ContactFrag();
                }
                replaceFragment(R.id.fragContainer, contactFrag, fragTags[0]);
                changeIconShow();
            } else if (intent.getAction().equals(Constants.Action.EXITE_AP_MODE)) {
                finish();
            } else if (intent.getAction().equals("DISCONNECT")) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                isConnectApWifi = false;
                isConnectWifi(isConnectApWifi);
            } else if (intent.getAction().equals(Constants.Action.ACTIVITY_FINISH)) {
                finish();
            }
        }

    };

    private void connect() {
        Intent service = new Intent(MainApplication.MAIN_SERVICE_START);
        service.setPackage(getPackageName());
        startService(service);
        if (AppConfig.DeBug.isWrightAllLog) {
            Intent log = new Intent(MainApplication.LOGCAT);
            log.setPackage(getPackageName());
            startService(log);
        }
    }

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch (view.getId()) {
            case R.id.button_add:
                Intent radar_add = new Intent();
                if (MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_JUJIANG)) {
                    radar_add.setClass(mContext.getApplicationContext(), RadarAddActivity.class);
                } else {
                    radar_add.setClass(mContext.getApplicationContext(), RadarAddActivity.class);
                }
                radar_add.putExtra("int", 3);
                radar_add.putExtra("isCameraList", 1);
                startActivity(radar_add);
                break;
            case R.id.icon_keyboard:
                break;
            case R.id.icon_nearlytell:
                break;

            case R.id.icon_setting:

                break;
            case R.id.icon_discover:

                break;
            case R.id.iv_back:
                finish();
                break;
        }

    }

    public void changeIconShow() {
        switch (currFrag) {
            case 0:
                contact_img.setImageResource(R.drawable.contact_p);
                dials_img.setImageResource(R.drawable.keyboard);
                recent_img.setImageResource(R.drawable.recent);
                settings_img.setImageResource(R.drawable.setting);
                discover_img.setImageResource(R.drawable.toolbox);
                tv_contact.setTextColor(getResources().getColor(R.color.color_local_device_bar));
                tv_message.setTextColor(getResources().getColor(R.color.white));
                tv_image.setTextColor(getResources().getColor(R.color.white));
                tv_more.setTextColor(getResources().getColor(R.color.white));
                contact.setSelected(true);
                dials.setSelected(false);
                recent.setSelected(false);
                settings.setSelected(false);
                discover.setSelected(false);
                break;
            case 1:
                contact_img.setImageResource(R.drawable.contact);
                dials_img.setImageResource(R.drawable.keyboard_p);
                recent_img.setImageResource(R.drawable.recent);
                settings_img.setImageResource(R.drawable.setting);
                discover_img.setImageResource(R.drawable.toolbox);
                tv_contact.setTextColor(getResources().getColor(R.color.white));
                tv_message.setTextColor(getResources().getColor(R.color.color_local_device_bar));
                tv_image.setTextColor(getResources().getColor(R.color.white));
                tv_more.setTextColor(getResources().getColor(R.color.white));
                contact.setSelected(false);
                dials.setSelected(true);
                recent.setSelected(false);
                settings.setSelected(false);
                discover.setSelected(false);
                break;
            case 2:
                contact_img.setImageResource(R.drawable.contact);
                dials_img.setImageResource(R.drawable.keyboard);
                recent_img.setImageResource(R.drawable.recent_p);
                settings_img.setImageResource(R.drawable.setting);
                discover_img.setImageResource(R.drawable.toolbox);
                contact.setSelected(false);
                dials.setSelected(false);
                recent.setSelected(true);
                settings.setSelected(false);
                discover.setSelected(false);
                break;
            case 3:
                contact_img.setImageResource(R.drawable.contact);
                dials_img.setImageResource(R.drawable.keyboard);
                recent_img.setImageResource(R.drawable.recent);
                settings_img.setImageResource(R.drawable.setting_p);
                discover_img.setImageResource(R.drawable.toolbox);
                tv_contact.setTextColor(getResources().getColor(R.color.white));
                tv_message.setTextColor(getResources().getColor(R.color.white));
                tv_image.setTextColor(getResources().getColor(R.color.white));
                tv_more.setTextColor(getResources().getColor(R.color.color_local_device_bar));
                contact.setSelected(false);
                dials.setSelected(false);
                recent.setSelected(false);
                settings.setSelected(true);
                discover.setSelected(false);
                break;
            case 4:
                contact_img.setImageResource(R.drawable.contact);
                dials_img.setImageResource(R.drawable.keyboard);
                recent_img.setImageResource(R.drawable.recent);
                settings_img.setImageResource(R.drawable.setting);
                discover_img.setImageResource(R.drawable.toolbox_p);
                tv_contact.setTextColor(getResources().getColor(R.color.white));
                tv_message.setTextColor(getResources().getColor(R.color.white));
                tv_image.setTextColor(getResources().getColor(R.color.color_local_device_bar));
                tv_more.setTextColor(getResources().getColor(R.color.white));
                contact.setSelected(false);
                dials.setSelected(false);
                recent.setSelected(false);
                settings.setSelected(false);
                discover.setSelected(true);
                break;
        }
    }

    public void replaceFragment(int container, Fragment fragment, String tag) {
        try {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            // transaction.setCustomAnimations(android.R.anim.fade_in,
            // android.R.anim.fade_out);
            transaction.replace(R.id.fragContainer, fragment, tag);
            transaction.commit();
            manager.executePendingTransactions();
        } catch (Exception e) {
            e.printStackTrace();
//			Log.e("my", "replaceFrag error--main");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (contactFrag != null) {
            contactFrag.dismissDialog();
            contactFrag.removeRunable();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isRegFilter) {
            isRegFilter = false;
            this.unregisterReceiver(mReceiver);
        }
        if (contactFrag != null && contactFrag.mAdapter != null)
            SyncMessageContainer.getInstance().produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (contactFrag != null) {
            contactFrag.removeRunable();
        }
        Intent exit = new Intent();
        exit.setAction(Constants.Action.ACTION_EXIT);
        exit.putExtra("isfinish", true);
        mContext.sendBroadcast(exit);
    }


//	class GetAccountInfoTask extends AsyncTask {
//
//		public GetAccountInfoTask() {
//
//		}
//
//		@Override
//		protected Object doInBackground(Object... params) {
//			// TODO Auto-generated method stub
//			Utils.sleepThread(1000);
//			Account account = AccountPersist.getInstance().getActiveAccountInfo(mContext);
//			return NetManager.getInstance(mContext).getAccountInfo(NpcCommon.mThreeNum, account.sessionId);
//		}
//
//		@Override
//		protected void onPostExecute(Object object) {
//			// TODO Auto-generated method stub
//			GetAccountInfoResult result = NetManager.createGetAccountInfoResult((org.json.JSONObject) object);
//			switch (Integer.parseInt(result.error_code)) {
//			case NetManager.SESSION_ID_ERROR:
//				Intent i = new Intent();
//				i.setAction(Constants.Action.SESSION_ID_ERROR);
//				MainApplication.app.sendBroadcast(i);
//				break;
//			case NetManager.CONNECT_CHANGE:
//				new GetAccountInfoTask().execute();
//				return;
//			case NetManager.GET_ACCOUNT_SUCCESS:
//				try {
//					String email = result.email;
//					String phone = result.phone;
//					Account account = AccountPersist.getInstance().getActiveAccountInfo(mContext);
//					if (null == account) {
//						account = new Account();
//					}
//					account.email = email;
//					account.phone = phone;
//					AccountPersist.getInstance().setActiveAccount(mContext, account);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				break;
//			default:
//				break;
//			}
//		}
//
//	}

//	class ExitTask extends AsyncTask {
//		Account account;
//
//		public ExitTask(Account account) {
//			this.account = account;
//		}
//
//		@Override
//		protected Object doInBackground(Object... params) {
//			// TODO Auto-generated method stub
//			return NetManager.getInstance(mContext).exit_application(account.three_number, account.sessionId);
//		}
//
//		@Override
//		protected void onPostExecute(Object object) {
//			// TODO Auto-generated method stub
//			int result = (Integer) object;
//			switch (result) {
//			case NetManager.CONNECT_CHANGE:
//				new ExitTask(account).execute();
//				return;
//			default:
//
//				break;
//			}
//		}
//	}

//	class LoginTask extends AsyncTask {
//		Account account = AccountPersist.getInstance().getActiveAccountInfo(mContext);
//
//		public LoginTask() {
//
//		}
//		@Override
//		protected Object doInBackground(Object... params) {
//			// TODO Auto-generated method stub
//			Utils.sleepThread(1000);
//			return NetManager.getInstance(mContext).getDeviceList(account.email, account.sessionId);
//		}
//
//		@Override
//		protected void onPostExecute(Object object) {
//			// TODO Auto-generated method stub
//			GetDeviceListResult result = NetManager.createGetDeviceListResult((JSONObject) object);
//			switch (Integer.parseInt(result.error_code)) {
//			case NetManager.SESSION_ID_ERROR:
//				/*
//				 * Intent i = new Intent();
//				 * i.setAction(Constants.Action.SESSION_ID_ERROR);
//				 * MainApplication.app.sendBroadcast(i);
//				 */
//				Log.e("错误", "SESSION_ID_ERROR");
//				break;
//			case NetManager.CONNECT_CHANGE:
//				new LoginTask().execute();
//				return;
//			case NetManager.GET_DEVICE_LIST_EMPTY:
//
//				break;
//			case NetManager.GET_DEVICE_LIST_SUCCESS:
//				Log.e("获取", result.contactIds.toString());
//			}
//		}
//
//	}

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
*/
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);

    }

    @Override
    public int getActivityInfo() {
        // TODO Auto-generated method stub
        return Constants.ActivityInfo.ACTIVITY_MAINACTIVITY;
    }

    @Override
    protected int onPreFinshByLoginAnother() {
        return 0;
    }

    public DeviceInfo getDevice() {
        return device;
    }

    public ZhujiInfo getZhuji() {
        return zhuji;
    }
}
