package com.smartism.znzk.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.camera.MainActivity;
import com.smartism.znzk.activity.camera.MainControlActivity;
import com.smartism.znzk.activity.camera.RadarAddActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.adapter.camera.MainAdapter;
import com.smartism.znzk.adapter.camera.MainAdapter.onConnectListner;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.activity.camera.ApMonitorActivity;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.db.camera.DataManager;
import com.smartism.znzk.db.camera.SharedPreferencesManager;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.GroupInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.global.AppConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.thread.MainThread;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.util.camera.WifiUtils;
import com.smartism.znzk.widget.HeaderTextView;
import com.smartism.znzk.widget.NormalDialog;
import com.smartism.znzk.widget.NormalDialog.OnNormalDialogTimeOutListner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * 摄像头首页 内容
 *
 * @author 2016年08月08日 update 王建
 */
public class ContactFrag extends BaseFragment implements OnClickListener {

    public static final int CHANGE_REFRESHING_LABLE = 0x12;
    private Context mContext;
    private boolean isRegFilter = false;
    private boolean isDoorBellRegFilter = false;
    private MainActivity activity;
    private ListView mListView;
    public MainAdapter mAdapter;
    private SwipeRefreshLayout mPullRefreshListView;
    boolean refreshEnd = false;
    boolean isFirstRefresh = true;
    boolean isActive;
    boolean isCancelLoading;
    private LinearLayout net_work_status_bar;
    NormalDialog dialog;
    private Contact next_contact;
    private LinearLayout layout_add;
    private RelativeLayout layout_contact;
    private RelativeLayout radar_add, manually_add;
    int count1 = 0;

    public static boolean isHideAdd = true;
    private Animation animation_out, animation_in;
    private NormalDialog dialog_loading;
    private NormalDialog dialog_connect;
    private HeaderTextView v;
    private Contact apContact;
    private Contact nvrContact;
    public static boolean isShowMenu = false;
    private String number;

    public void setNumber(String number) {
        this.number = number;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    public ContactFrag() {
        super();
    }

    public void dismissDialog() {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
                dialog = null;
            }
        }
    }

    public void removeRunable() {
        if (mHandler != null) {
            mHandler.removeCallbacks(runnable);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container,
                false);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = getActivity();
        v = new HeaderTextView(mContext,
                Utils.getStringByResouceID(R.string.tv_add_device1),
                Utils.getStringByResouceID(R.string.tv_add_device2));
        initComponent(view);
        regFilter();
        initCameraList();

        if (isFirstRefresh) {
            isFirstRefresh = !isFirstRefresh;
            FList flist = FList.getInstance();
            flist.updateOnlineState();
            flist.searchLocalDevice();
        }
    }

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {

            mHandler.sendEmptyMessage(9);
        }
    };

    public void initComponent(View view) {
        net_work_status_bar = (LinearLayout) view
                .findViewById(R.id.net_status_bar_top);
        mPullRefreshListView = (SwipeRefreshLayout) view
                .findViewById(R.id.pull_refresh_list);
        layout_add = (LinearLayout) view.findViewById(R.id.layout_add);
        layout_contact = (RelativeLayout) view
                .findViewById(R.id.layout_contact);
        radar_add = (RelativeLayout) view.findViewById(R.id.radar_add);
        manually_add = (RelativeLayout) view.findViewById(R.id.manually_add);
        radar_add.setOnClickListener(this);
        manually_add.setOnClickListener(this);
        layout_contact.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (isHideAdd == false) {
                    hideAdd();
                }
                return false;
            }
        });
        mPullRefreshListView
                .setOnRefreshListener(new OnRefreshListener() {


                    @Override
                    public void onRefresh() {
                        mHandler.sendEmptyMessageDelayed(3, 8 * 1000);
                        new GetDataTask().execute();
                    }
                });
        mPullRefreshListView.setColorSchemeResources(R.color.green, R.color.green, R.color.green, R.color.green);
        mListView = (ListView) view.findViewById(R.id.lv_list);
        mAdapter = new MainAdapter(mContext);
        mAdapter.setOnSrttingListner(new onConnectListner() {
            @Override
            public void onNvrClick(Contact contact) {
                nvrContact = contact;
                Log.e("leleTest", "onNvrClick=" + nvrContact.contactType);
                P2PHandler.getInstance().getNvrIpcList(contact.contactId, contact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
                dialog_loading = new NormalDialog(mContext);
                dialog_loading.setTitle(getResources().getString(R.string.verification));
                dialog_loading.setStyle(NormalDialog.DIALOG_STYLE_LOADING);
                dialog_loading.showDialog();
            }
        });
        mListView.setAdapter(mAdapter);
        mListView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if (isHideAdd == false) {
                    hideAdd();
                }
                return false;
            }
        });

        List<Contact> contacts = DataManager.findContactByActiveUser(mContext,
                NpcCommon.mThreeNum);
        animation_out = AnimationUtils.loadAnimation(mContext,
                R.anim.scale_amplify);
        animation_in = AnimationUtils.loadAnimation(mContext,
                R.anim.scale_narrow);
    }


    private void addListHeader() {
        // 添加头部
        HeaderTextView header = new HeaderTextView(mContext, "", "");
        AbsListView.LayoutParams headerParams = new AbsListView.LayoutParams(
                LayoutParams.MATCH_PARENT, R.dimen.contact_item_margin);
        header.setLayoutParams(headerParams);
        mListView.addHeaderView(header, null, false);
    }

    public void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.Action.REFRESH_CONTANTS);
        filter.addAction(Constants.Action.GET_FRIENDS_STATE);
        filter.addAction(Constants.Action.LOCAL_DEVICE_SEARCH_END);
        filter.addAction(Constants.Action.ACTION_NETWORK_CHANGE);
        filter.addAction(Constants.P2P.ACK_RET_CHECK_PASSWORD);
        filter.addAction(Constants.P2P.RET_GET_REMOTE_DEFENCE);
        filter.addAction(Constants.Action.SETTING_WIFI_SUCCESS);
        filter.addAction(Constants.Action.ADD_CONTACT_SUCCESS);
        filter.addAction(Constants.Action.REFRESH_DATA);
        //filter.addAction(Constants.Action.DELETE_DEVICE_ALL);
        // 接收报警ID----------
        filter.addAction(Constants.P2P.RET_GET_BIND_ALARM_ID);
        filter.addAction(Constants.P2P.RET_SET_BIND_ALARM_ID);
        filter.addAction(Constants.P2P.ACK_RET_SET_BIND_ALARM_ID);
        filter.addAction(Constants.P2P.ACK_RET_GET_BIND_ALARM_ID);
        filter.addAction(Constants.Action.SEARCH_AP_DEVICE);
        filter.addAction(Constants.Action.ENTER_DEVICE_SETTING);
        filter.addAction(Constants.Action.CALL_DEVICE);
        filter.addAction(Constants.Action.NET_WORK_TYPE_CHANGE);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(Constants.P2P.ACK_GET_NVR_IPC_LIST);
        filter.addAction(Constants.P2P.RET_GET_NVR_IPC_LIST);
        filter.addAction(Constants.P2P.RET_SET_REMOTE_DEFENCE);
        filter.addAction(Actions.REFRESH_DEVICES_LIST);

        filter.addAction(Constants.Action.ADAPTER_CHANGE_TITLE);
        mContext.registerReceiver(mReceiver, filter);
        isRegFilter = true;
    }

    public void regDoorbellFilter() {
        IntentFilter filter = new IntentFilter();
        // 接收报警ID----------
        filter.addAction(Constants.P2P.RET_GET_BIND_ALARM_ID);
        filter.addAction(Constants.P2P.RET_SET_BIND_ALARM_ID);
        filter.addAction(Constants.P2P.ACK_RET_SET_BIND_ALARM_ID);
        filter.addAction(Constants.P2P.ACK_RET_GET_BIND_ALARM_ID);
        // 接收报警ID---------------
        mContext.registerReceiver(mDoorbellReceiver, filter);
        isDoorBellRegFilter = true;
    }

    BroadcastReceiver mDoorbellReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constants.P2P.RET_GET_BIND_ALARM_ID)) {
                String[] data = intent.getStringArrayExtra("data");
                String srcID = intent.getStringExtra("srcID");
                int max_count = intent.getIntExtra("max_count", 0);
                if (data.length >= max_count) {
                    if (!SharedPreferencesManager.getInstance()
                            .getIsDoorBellToast(mContext, srcID)) {
                        T.show(mContext, R.string.alarm_push_limit, 2000);
                        SharedPreferencesManager.getInstance()
                                .putIsDoorBellToast(srcID, true, mContext);
                    }
                } else {
                    // 处理绑定推送ID
                    mAdapter.setBindAlarmId(srcID, data);
                }
            } else if (intent.getAction().equals(
                    Constants.P2P.RET_SET_BIND_ALARM_ID)) {
                int result = intent.getIntExtra("result", -1);
                String srcID = intent.getStringExtra("srcID");
                if (result == Constants.P2P_SET.BIND_ALARM_ID_SET.SETTING_SUCCESS) {
                    // 设置成功重新获取列表
                    // mAdapter.getBindAlarmId(srcID);
                    mAdapter.setBindAlarmIdSuccess(srcID);
                } else {

                }
            } else if (intent.getAction().equals(
                    Constants.P2P.ACK_RET_SET_BIND_ALARM_ID)) {
                int result = intent.getIntExtra("result", -1);
                String srcID = intent.getStringExtra("srcID");
                if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {

                } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    Log.e("my", "net error resend:set alarm bind id");
                    // 设置时网络错误，重新设置
                    mAdapter.setBindAlarmId(srcID);
                }
            } else if (intent.getAction().equals(
                    Constants.P2P.ACK_RET_GET_BIND_ALARM_ID)) {
                int result = intent.getIntExtra("result", -1);
                String srcID = intent.getStringExtra("srcID");
                if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {

                } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    Log.e("my", "net error resend:get alarm bind id");
                    // 获得列表网络错误，重新获取
                    mAdapter.getBindAlarmId(srcID);
                }
            }
        }
    };


    //从服务器上拉数据，更新本地摄像头
    public void initCameraList() {
        DeviceInfo device = null;
        //StartSearchDevice(); 这个好像是搜索局域网内的在线摄像头。不需要
        List<DeviceInfo> dInfos = DatabaseOperator.getInstance(mContext).queryAllDeviceInfos(activity.getZhuji().getId());
        if (dInfos != null && !dInfos.isEmpty()) {
            for (DeviceInfo deviceInfo : dInfos) {
                if (deviceInfo.getCak().equals("surveillance")) {
                    device = deviceInfo;
                    break;
                }
            }
        }
        List<CameraInfo> camera = new ArrayList<>();
        if (device != null && device.getCak().equals("surveillance")) {
            camera = (List<CameraInfo>) JSON.parseArray(device.getIpc(), CameraInfo.class);
        }
        if (camera != null && camera.size() > 0) {
            FList.getInstance().list().clear();
            for (int i = 0; i < camera.size(); i++) {
                Contact c = new Contact();
                c.contactId = camera.get(i).getId();
                c.contactName = camera.get(i).getN();
                c.contactPassword = camera.get(i).getP();
                c.userPassword = camera.get(i).getOriginalP();
                if (camera.get(i).getC().equals("jiwei")) {
                    FList.getInstance().insert(c);
                    FList.getInstance().updateLocalDeviceFlag(c.contactId, Constants.DeviceFlag.ALREADY_SET_PASSWORD);
                    FList.getInstance().updateLocalDeviceWithLocalFriends();
                }
            }
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("BroadcastReceiver_1111", intent.getAction());
            if (Constants.Action.ADAPTER_CHANGE_TITLE.equals(intent.getAction())) {
                mAdapter.notifyDataSetChanged();
            } else if (Actions.REFRESH_DEVICES_LIST.equals(intent.getAction())) { // 数据刷新完成广播
                new GetDataTask().execute();
                mAdapter.notifyDataSetChanged();
            }else if (intent.getAction().equals(Constants.Action.REFRESH_DATA)) {
                mAdapter.notifyDataSetChanged();
            }else if (intent.getAction().equals(Constants.Action.REFRESH_CONTANTS)) {
                //摄像头广播
                initCameraList();
                FList flist = FList.getInstance();
                flist.updateOnlineState();
                mAdapter.notifyDataSetChanged();
            } else if (intent.getAction().equals(Constants.Action.GET_FRIENDS_STATE)) {
                mAdapter.notifyDataSetChanged();
                refreshEnd = true;
            } else if (intent.getAction().equals(Constants.Action.ACTION_NETWORK_CHANGE)) {
                ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = connectivityManager
                        .getActiveNetworkInfo();
                if (activeNetInfo != null) {
                    if (!activeNetInfo.isConnected()) {
                        T.showShort(mContext, getString(R.string.network_error)
                                + " " + activeNetInfo.getTypeName());

                        net_work_status_bar
                                .setVisibility(RelativeLayout.VISIBLE);
                    } else {
                        net_work_status_bar.setVisibility(RelativeLayout.GONE);
                    }
                } else {
                    T.showShort(mContext, R.string.network_error);
                    net_work_status_bar.setVisibility(RelativeLayout.VISIBLE);
                }

            } else if (intent.getAction().equals(Constants.P2P.ACK_RET_CHECK_PASSWORD)) {
                if (!isActive) {
                    return;
                }
                if (dialog == null) return;
                if (activity == null) return;
                dismissDialog();
                Intent control = new Intent();
                control.setClass(mContext, MainControlActivity.class);
                control.putExtra("contact", next_contact);
                control.putExtra("deviceInfo",activity.getDevice());
                control.putExtra("type", P2PValue.DeviceType.NPC);
                mContext.startActivity(control);

            } else if (intent.getAction().equals(Constants.P2P.RET_GET_REMOTE_DEFENCE)) {
                int state = intent.getIntExtra("state", -1);
                String contactId = intent.getStringExtra("contactId");
                Contact contact = FList.getInstance().isContact(contactId);

                if (state == Constants.DefenceState.DEFENCE_STATE_WARNING_NET) {
                    if (null != contact && contact.isClickGetDefenceState) {
                        T.showShort(mContext, R.string.net_error);
                    }
                } else if (state == Constants.DefenceState.DEFENCE_STATE_WARNING_PWD) {
                    if (null != contact && contact.isClickGetDefenceState) {
                        T.showShort(mContext, R.string.password_error);
                    }
                }

                if (null != contact && contact.isClickGetDefenceState) {
                    FList.getInstance().setIsClickGetDefenceState(contactId,
                            false);
                }

                mAdapter.notifyDataSetChanged();
            } else if (intent.getAction().equals(Constants.Action.SETTING_WIFI_SUCCESS)) {
                FList flist = FList.getInstance();
                flist.updateOnlineState();
                flist.searchLocalDevice();
            } else if (intent.getAction().equals(Constants.Action.DIAPPEAR_ADD)) {
                if (isHideAdd == false) {
                    hideAdd();
                }
            } else if (intent.getAction().equals(Constants.Action.ADD_CONTACT_SUCCESS)) {

            } else if (intent.getAction().equals(Constants.Action.DELETE_DEVICE_ALL)) {
                mPullRefreshListView.setRefreshing(false);


            } else if (intent.getAction().equals(Constants.Action.ENTER_DEVICE_SETTING)) {
                /**********************点击列表中设置按钮，从adapter中发出的广播*********************************/
                Contact contact = (Contact) intent.getSerializableExtra("contact");
                next_contact = contact;
                dialog = new NormalDialog(mContext);
                dialog.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface arg0) {
                        // TODO Auto-generated method stub
                        isCancelLoading = true;
                    }

                });
                dialog.showLoadingDialog2();
                dialog.setCanceledOnTouchOutside(false);

                isCancelLoading = false;
                P2PHandler.getInstance().checkPassword(contact.contactId,
                        contact.getContactPassword(), MainApplication.GWELL_LOCALAREAIP);
                count1++;
            } else if (intent.getAction().equals(Constants.Action.CALL_DEVICE)) {
            } else if (intent.getAction().equals(Constants.Action.NET_WORK_TYPE_CHANGE)) {
                Log.e("connect_failed", "NET_WORK_TYPE_CHANGE");
                String connect_name = WifiUtils.getInstance().getConnectWifiName();
                boolean isApDevice = WifiUtils.getInstance().isApDevice(connect_name);
                if (isApDevice) {
                    String deviceId = connect_name.substring(AppConfig.Relese.APTAG.length());
                    Contact contact = FList.getInstance().isContact(deviceId);
                    if (contact != null) {
                        contact.apModeState = Constants.APmodeState.LINK;
                        Intent it = new Intent();
                        it.setAction(Constants.Action.REFRESH_CONTANTS);
                        context.sendBroadcast(it);
                    }
                }
                if (dialog_connect != null && dialog_connect.isShowing()) {
                    dialog_connect.dismiss();
                } else {
                    return;
                }
                if (WifiUtils.getInstance().isConnectWifi(
                        apContact.getAPName())) {
                    Intent apMonitor = new Intent(mContext, ApMonitorActivity.class);
                    try {
                        apContact.ipadressAddress = InetAddress.getByName("192.168.1.1");
                    } catch (UnknownHostException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    apMonitor.putExtra("contact", apContact);
                    apMonitor.putExtra("connectType", Constants.ConnectType.RTSPCONNECT);
                    startActivity(apMonitor);
                    Intent exit = new Intent();
                    exit.setAction(Constants.Action.ACTION_EXIT);
                    exit.putExtra("isfinish",true);
                    mContext.sendBroadcast(exit);
                } else {
                    reConnectApModeWifi();
                }
            } else if (intent.getAction().equals(Constants.P2P.ACK_GET_NVR_IPC_LIST)) {
                Log.e("Camera_log", "ACK_GET_NVR_IPC_LIST");
                int state = intent.getIntExtra("state", -1);
                Log.e("nvr_list", "state=" + state);
                if (state == Constants.P2P_SET.ACK_RESULT.ACK_SUCCESS) {

                } else if (state == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                    if (dialog_loading != null && dialog_loading.isShowing()) {
                        dialog_loading.dismiss();
                    }
                    T.showShort(mContext, R.string.pw_incrrect);
                } else if (state == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    if (dialog_loading != null && dialog_loading.isShowing()) {
                        dialog_loading.dismiss();
                    }
                    T.showShort(mContext, R.string.net_error);
                }

            } else if (intent.getAction().equals(Constants.P2P.RET_GET_NVR_IPC_LIST)) {
                Log.e("Camera_log", "RET_GET_NVR_IPC_LIST");
                String contactId = intent.getStringExtra("contactId");
                String[] data = intent.getStringArrayExtra("data");
                int number = intent.getIntExtra("number", -1);
                String s = "";
                if (dialog_loading != null || dialog_loading.isShowing()) {
                    dialog_loading.dismiss();
                }
                for (String d : data) {
                    s = s + d + " ";
                }
                if (number > 0) {
                    Intent monitor = new Intent(mContext, ApMonitorActivity.class);
                    monitor.putExtra("contact", nvrContact);
                    Log.e("leleTest", "onNvrClick=" + nvrContact.contactType);
                    monitor.putExtra("ipcList", data);
                    monitor.putExtra("number", number);
                    monitor.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
                    mContext.startActivity(monitor);
                    Intent exit = new Intent();
                    exit.setAction(Constants.Action.ACTION_EXIT);
                    exit.putExtra("isfinish",true);
                    mContext.sendBroadcast(exit);
                } else {
                    T.showShort(mContext, R.string.no_ipc_list);
                }
            } else if (intent.getAction().equals(Constants.P2P.RET_SET_REMOTE_DEFENCE)) {

                String contactId = intent.getStringExtra("contactId");
                int state = intent.getIntExtra("state", -1);
                Contact cameraInfo = FList.getInstance().getDeviceInfo(contactId);
                if (state == 0) {
                    if (contactId.equals("1")) {
                        P2PHandler.getInstance().getDefenceStates(contactId, "0", MainApplication.GWELL_LOCALAREAIP);
                    }
                }
            }
        }
    };

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case CHANGE_REFRESHING_LABLE:
                    String lable = (String) msg.obj;
                    // mPullRefreshListView.setHeadLable(lable);
                    break;
                case 3:
                    mPullRefreshListView.setRefreshing(false);
                    if (isAdded())
                        T.show(getActivity(),getResources().getString(R.string.time_out), Toast.LENGTH_SHORT);
                case 9:
                    mAdapter.notifyDataSetChanged();
                    break;
                case 10:
                    new GetDataTask().execute();
                    break;
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    private class GetDataTask extends AsyncTask<Void, Void, String[]> {

        @Override
        protected String[] doInBackground(Void... params) {
            // Simulates a background job.
            Log.e("my", "doInBackground");
            FList flist = FList.getInstance();
            flist.searchLocalDevice();
            if (flist.size() == 0) {
                return null;
            }
            refreshEnd = false;
            flist.updateOnlineState();
            flist.getDefenceState();
            flist.getCheckUpdate();
            while (!refreshEnd) {
                Utils.sleepThread(1000);
            }

            Message msg = new Message();
            msg.what = CHANGE_REFRESHING_LABLE;
            msg.obj = mContext.getResources().getString(
                    R.string.pull_to_refresh_refreshing_success_label);
            mHandler.sendMessage(msg);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            mHandler.removeMessages(3);
            mPullRefreshListView.setRefreshing(false);
            super.onPostExecute(result);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add:
                mPullRefreshListView.setFocusable(false);
                mListView.setFocusable(false);
                Intent radar_add = new Intent();
                if (MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_JUJIANG)) {
                    radar_add.setClass(mContext.getApplicationContext(), RadarAddActivity.class);
                }else {
                    radar_add.setClass(mContext.getApplicationContext(), RadarAddActivity.class);
                }
                radar_add.putExtra("int", 3);
                radar_add.putExtra("isCameraList", 1);
                activity.startActivity(radar_add);

                break;
            case R.id.radar_add:
                break;
            case R.id.manually_add:
                break;
            case R.id.back_btn:
                removeRunable();

                Intent exit = new Intent();
                exit.setAction(Constants.Action.ACTION_EXIT);
                exit.putExtra("isfinish", true);
                mContext.sendBroadcast(exit);
            default:
                break;
        }
    }

    Runnable runnable = new Runnable() {

        @Override
        public void run() {
            dismissDialog();
//            T.showShort(mContext, R.string.time_out);
        }
    };

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        MainThread.setOpenThread(false);
        super.onPause();
        isActive = false;
        if (isDoorBellRegFilter) {
            isDoorBellRegFilter = false;
            mContext.unregisterReceiver(mDoorbellReceiver);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isDoorBellRegFilter) {
            regDoorbellFilter();
        }
        isActive = true;
        initCameraList();
        mHandler.sendEmptyMessage(10);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (isRegFilter) {
            mContext.unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void hideAdd() {
        layout_add.startAnimation(animation_in);
        layout_add.setVisibility(LinearLayout.GONE);
        isHideAdd = true;
    }

    public void showAdd() {
        layout_add.setVisibility(LinearLayout.VISIBLE);
        layout_add.startAnimation(animation_out);
        isHideAdd = false;
    }

    private void connectWifi(String wifiName, String wifiPwd) {
        if (wifiPwd.length() < 8) {
            // 密码必须8位
            T.showShort(mContext, R.string.wifi_pwd_error);
            return;
        }
        WifiUtils.getInstance().connectWifi(wifiName, wifiPwd,
                1);
        if (dialog_connect == null) {
            dialog_connect = new NormalDialog(mContext);
        }
        dialog_connect.setTitle(R.string.wait_connect);
        dialog_connect.showLoadingDialog();
        dialog_connect.setTimeOut(30 * 1000);
        dialog_connect.setOnNormalDialogTimeOutListner(new OnNormalDialogTimeOutListner() {

            @Override
            public void onTimeOut() {
                T.showShort(mContext, R.string.connect_wifi_timeout);
                reConnectApModeWifi();
            }
        });

    }

    public void reConnectApModeWifi() {
        try {
            apContact.ipadressAddress = InetAddress.getByName("192.168.1.1");
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*Intent modify=new Intent();
		modify.setClass(mContext, AddApDeviceActivity.class);
		modify.putExtra("isAPModeConnect", 0);
	    modify.putExtra("contact", apContact);
	    modify.putExtra("ipFlag","1");
	    modify.putExtra("isCreatePassword", false);
	    startActivity(modify);*/
        T.showShort(mContext, R.string.conn_fail);
    }

    private void refreshData() {
        JavaThreadPool.getInstance().excute(new loadAllDevicesInfo());
    }

    protected DataCenterSharedPreferences dcsp = null;
    int sortType = 0;

    class loadAllDevicesInfo implements Runnable {
        private int what;

        public loadAllDevicesInfo() {
            dcsp = DataCenterSharedPreferences.getInstance(getActivity(),
                    DataCenterSharedPreferences.Constant.CONFIG);
        }

        public loadAllDevicesInfo(int what) {
            this.what = what;
        }

        @Override
        public void run() {
//            ZhujiInfo zhuji = DatabaseOperator.getInstance(getActivity())
//                    .queryDeviceZhuJiInfo(dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, ""));
            //替换
            ZhujiInfo zhuji = DatabaseOperator.getInstance(getActivity()).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
            if (zhuji == null) {
                List<ZhujiInfo> tmpzhuji = DatabaseOperator.getInstance(getActivity()).queryAllZhuJiInfos();
                if (!tmpzhuji.isEmpty()) {
                    zhuji = tmpzhuji.get(0);
//                    dcsp.putString(DataCenterSharedPreferences.Constant.APP_MASTERID, zhuji.getMasterid()).commit();
                    ZhujiListFragment.setMasterId(zhuji.getMasterid());
                }
            }
            DeviceInfo shexiangtou = null;
            List<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();
            List<DeviceInfo> deviceList_close = new ArrayList<DeviceInfo>();
            if (zhuji != null) {
                if (dcsp.getBoolean(DataCenterSharedPreferences.Constant.SHOW_ZHUJI, true)) {
                    // 设置属性
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.setId(zhuji.getId());
                    deviceInfo.setName(zhuji.getName());
                    deviceInfo.setWhere(zhuji.getWhere());
                    deviceInfo.setStatus(zhuji.getUpdateStatus());
                    deviceInfo.setControlType(DeviceInfo.ControlTypeMenu.zhuji.value());
                    deviceInfo.setLogo(zhuji.getLogo());
                    deviceInfo.setGsm(zhuji.getGsm());
                    deviceInfo.setFlag(zhuji.isAdmin()); // 利用deviceInfo的flag存主机的是否admin信息
                    deviceList.add(deviceInfo); // 主机实例化一个对象来代替
                }
                List<GroupInfo> gInfos = DatabaseOperator.getInstance(getActivity())
                        .queryAllGroups(zhuji.getId());
                if (gInfos != null && !gInfos.isEmpty()) {
                    for (GroupInfo g : gInfos) {
                        DeviceInfo dInfo = new DeviceInfo();
                        dInfo.setId(g.getId());
                        dInfo.setName(g.getName());
                        dInfo.setBipc(g.getBipc());
                        dInfo.setLogo(g.getLogo());
                        dInfo.setControlType(DeviceInfo.ControlTypeMenu.group.value());
                        dInfo.setAcceptMessage(1);
                        deviceList.add(dInfo);
                    }
                }
                sortType = dcsp.getString(DataCenterSharedPreferences.Constant.SHOW_DLISTSORT, "zhineng").equals("zhineng") ? 0 : 1;
                String ordersql = "";
                if (sortType == 0) {
                    ordersql = "order by device_lasttime desc";
                } else {
                    ordersql = "order by sort desc";
                }
                int totalNr = 0;
                Cursor cursor = DatabaseOperator.getInstance(getActivity()).getReadableDatabase().rawQuery(
                        "select * from DEVICE_STATUSINFO where zj_id = ? " + ordersql,
                        new String[]{String.valueOf(zhuji.getId())});
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        DeviceInfo deviceInfo = DatabaseOperator.getInstance(getActivity())
                                .buildDeviceInfo(cursor);
                        if ("shexiangtou".equals(deviceInfo.getControlType())) {
                            shexiangtou = deviceInfo;
                            if (shexiangtou.getIpc() != null) {
                                JSONArray array = JSONArray.parseArray(shexiangtou.getIpc());

                                if (array == null) {
                                }
                            }
                            continue;
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
						/*ShortcutBadger.with(getActivity().getApplicationContext()).count(totalNr);
						deviceList.addAll(deviceList_close);*/
                    }

                }
            }
        }
    }

    private List<CameraInfo> getCameraList() {
        List<CameraInfo> camera = new ArrayList<>();
        DeviceInfo deviceInfo = null;
//        ZhujiInfo zhuji = DatabaseOperator.getInstance(getActivity()).queryDeviceZhuJiInfo(
//                DataCenterSharedPreferences.getInstance(getActivity(), DataCenterSharedPreferences.Constant.CONFIG)
//                        .getString(DataCenterSharedPreferences.Constant.APP_MASTERID, ""));
        ZhujiInfo zhuji = DatabaseOperator.getInstance(getActivity()).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
        //StartSearchDevice(); 这个好像是搜索局域网内的在线摄像头。不需要
        List<DeviceInfo> dInfos = DatabaseOperator.getInstance(getActivity()).queryAllDeviceInfos(zhuji.getId());
        if (dInfos != null && !dInfos.isEmpty()) {
            for (DeviceInfo device : dInfos) {
                if (device.getCak().equals("surveillance")) {
                    deviceInfo = device;
                    break;

                }
            }
        }

        if (deviceInfo != null && deviceInfo.getCak().equals("surveillance")) {
            List<CameraInfo> list = (List<CameraInfo>) JSON.parseArray(deviceInfo.getIpc(), CameraInfo.class);
            camera.addAll(list);
        }
        if (camera != null && camera.size() > 0) {
            FList.getInstance().list().clear();
            for (int i = 0; i < camera.size(); i++) {
                Contact c = new Contact();
                c.contactId = camera.get(i).getId();
                c.contactName = camera.get(i).getN();
                c.contactPassword = camera.get(i).getP();
                c.userPassword = camera.get(i).getOriginalP();
                if (camera.get(i).getC().equals("jiwei")) {
                    FList.getInstance().insert(c);
                    FList.getInstance().updateLocalDeviceFlag(c.contactId, Constants.DeviceFlag.ALREADY_SET_PASSWORD);
                    FList.getInstance().updateLocalDeviceWithLocalFriends();
                }
            }
        }
        mAdapter.notifyDataSetChanged();
        return camera;
    }
}