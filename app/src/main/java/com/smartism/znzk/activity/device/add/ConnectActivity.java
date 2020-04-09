package com.smartism.znzk.activity.device.add;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.amplify.generated.graphql.CreateCtrUserDeviceRelationsMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.baidu.location.BDLocation;
import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.awsClient.AWSClients;
import com.smartism.znzk.communication.connector.SyncClientAWSMQTTConnector;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.domain.HeaterShadowInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.udputil.UDPReceiver;
import com.smartism.znzk.udputil.UDPSend;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.BaiduLBSUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.EspWifiAdminSimple;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.NetworkUtils;
import com.smartism.znzk.util.StringUtils;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.MyRoundProcess;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nonnull;

import type.CreateCtrUserDeviceRelationsInput;

import static com.smartism.znzk.activity.device.add.GSMDistributionNetworkActivity.SMS_DISTRIBUTION_FLAG;


/**
 * 2017.4.14 create by wxb
 * 8266smartlink 连接等待界面
 */
public class ConnectActivity extends BaseActivty implements View.OnClickListener, OnItemClickListener, HttpAsyncTask.IHttpResultView{

    public static final String DEBUG_TAG = "ConnectActivity";
    private EspWifiAdminSimple mWifiAdmin;
    private ImageView iv_back;

    private int secondleft = 0;

    private Timer timer;
    private boolean isReceiveLink = false;
    private MyRoundProcess roundProcess, serach_process;
    private boolean isFailure = false;
    private LinearLayout ll_serach,ll_connect_parent;
    private TextView tv_info, tv_serach;
    private int mPeiwangFlag = -1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        mPeiwangFlag = getIntent().getIntExtra("peiwang_flag",-1);
        isMainList = getIntent().getBooleanExtra("isMainList", false);
        mWifiAdmin = new EspWifiAdminSimple(this);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        ll_connect_parent = findViewById(R.id.ll_connect_parent);
        roundProcess = (MyRoundProcess) findViewById(R.id.round_process);
        serach_process = (MyRoundProcess) findViewById(R.id.serach_process);
        ll_serach = (LinearLayout) findViewById(R.id.ll_serach);
        tv_serach = (TextView) findViewById(R.id.tv_serach);
        tv_info = (TextView) findViewById(R.id.tv_info);
        btn_serach_retry = (Button) findViewById(R.id.btn_serach_retry);
        ll_fail = (LinearLayout) findViewById(R.id.ll_fail);
        btn_serach_retry.setOnClickListener(this);
        ll_serach.setVisibility(View.GONE);
        iv_back.setOnClickListener(this);
        initRegisterReceiver();
        esptouchAsyncTask3 = new EsptouchAsyncTask3();
        if(mPeiwangFlag!=SMS_DISTRIBUTION_FLAG){
            esptouchAsyncTask3.execute(getIntent().getStringExtra("ssid"), mWifiAdmin.getWifiConnectedBssid(), getIntent().getStringExtra("password"), "1");
        }else{
            //短信配网搜索
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ll_connect_parent.setVisibility(View.GONE);
                    ll_serach.setVisibility(View.VISIBLE);
                    isReceiveLink = true ;
                    search_count = 0;
                    serach_process.setProgress(0);
                    handler.sendEmptyMessage(handler_key.CONFIG_FINISH.ordinal());
                }
            });
        }
        if(Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            BaiduLBSUtils.location(this);
        }

        mDMAIntent = new Intent();
        mDMAIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mDMAIntent.setClass(getApplicationContext(), DeviceMainActivity.class);
        mDMAIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
    Intent mDMAIntent ;
    BDLocation bdlocation ;

    public enum handler_key {
        //the tick time
        TICK_TIME,
        // the result ok
        CONFIG_SUCCESS,
        // the result false
        CONFIG_FALSE,

        CONFIG_FINISH,

        SERACH_TIME,

        SERACH_FALSE,

        SERACH_FINISH,

        SEARCHING,
    }

    private boolean isSerachingSuccess;
    private Button btn_serach_retry;
    private LinearLayout ll_fail;
    private static final int AGAIN_REQUEUST_FALG = 0X56;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            handler_key key = handler_key.values()[msg.what];
            switch (key) {
                case TICK_TIME:
                    secondleft++;
                    if (isReceiveLink) {
                        secondleft = 601;
                    }
                    if (secondleft > 600) {
                        if (isReceiveLink) {
//                            handler.sendEmptyMessage(handler_key.CONFIG_SUCCESS.ordinal());
                            handler.sendEmptyMessage(handler_key.CONFIG_FINISH.ordinal());
                        } else {
                            handler.sendEmptyMessage(handler_key.CONFIG_FALSE.ordinal());
                        }
                    } else {
                        roundProcess.setProgress(secondleft * 100 / 600);
                    }

                    break;
                case SERACH_TIME:
                    secondleft++;
                    if (isSerachingSuccess) {
                        secondleft = 601;
                    }
                    if (secondleft > 600) {
                        if (isSerachingSuccess) {
//                            handler.sendEmptyMessage(handler_key.SERACH_FINISH.ordinal());
                        } else {
                            handler.sendEmptyMessage(handler_key.SERACH_FALSE.ordinal());
                        }
                    } else {
                        serach_process.setProgress(secondleft * 100 / 600);
                    }
                    break;
                case CONFIG_SUCCESS:
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
//                            dialog.dismiss();
                            handler.sendEmptyMessage(handler_key.CONFIG_FINISH.ordinal());
                        }
                    }, 1000);

                    break;
                case CONFIG_FALSE:
                    if (isFailure) return true;
                    isFailure = true;
//                    openTimer(1);
                    timer.cancel();
                    timer = null;
                    secondleft = 0;
                    Intent intent = new Intent(ConnectActivity.this, AddZhujiFailureActivity.class);
                    intent.putExtra(HeaterShadowInfo.type, getIntent().getStringExtra(HeaterShadowInfo.type));
                    startActivityForResult(intent, 5);
                    break;
                case SERACH_FALSE:
                    if (isFailure) return true;
                    isFailure = true;
                    timer.cancel();
                    secondleft = 0;
                    handler.sendEmptyMessage(handler_key.CONFIG_FINISH.ordinal());
                    break;
                case SERACH_FINISH:
                    break;
                case CONFIG_FINISH:
//                    finish 前面的程序
//                    setResult(11);
                    tv_info.setText(getString(R.string.hiflying_smartlinker_completed));
                    roundProcess.setProgress(100);
                    handler.removeMessages(handler_key.TICK_TIME.ordinal());
                    if(timer!=null){
                        timer.cancel();
                    }
                    if (NetworkUtils.CheckNetworkIsWifi(getApplicationContext())) {
                        ll_serach.setVisibility(View.VISIBLE);
//                        serach_process.setProgress(secondleft * 100 / 600);
                        openTimer(2);
                        handler.sendEmptyMessageDelayed(handler_key.SEARCHING.ordinal(), search_time);
                    } else {
                        new AlertView(getString(R.string.activity_weight_notice), getString(R.string.deviceslist_server_search_nowifi), null,
                                new String[]{getString(R.string.sure)}, null, ConnectActivity.this, AlertView.Style.Alert,
                                new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(Object o, int position) {
                                        if (android.os.Build.VERSION.SDK_INT > 10) {
                                            // 3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
                                            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                                        } else {
                                            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                        }
                                    }
                                }).show();
                    }
                    break;
                case SEARCHING:
                    ConnectActivity.this.sendBroadcast(new Intent(Actions.SEND_SEARCHZHUJICOMMAND));
                    handler.sendEmptyMessageDelayed(handler_key.SEARCHING.ordinal(), search_time);
                    break;
            }
            return false;
        }
    };
    private Handler handler = new WeakRefHandler(mCallback);

    private InputMethodManager imm;
    private EditText etName;
    private AlertView mAlertViewExt;
    private String sqr = "";

    private void closeKeyboard() {
        //关闭软键盘
        imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
        //恢复位置
        mAlertViewExt.setMarginBottom(0);
    }

    @Override
    public void onItemClick(Object o, int position) {
        closeKeyboard();
        //判断是否是拓展窗口View，而且点击的是非取消按钮
        if (o == mAlertViewExt && position != AlertView.CANCELPOSITION) {
            String name = etName.getText().toString();
            if (name.isEmpty()) {
                Toast.makeText(this, getString(R.string.activity_add_zhuji_idzhu_empty), Toast.LENGTH_SHORT).show();
            }
            if (mAlertViewExt != null && mAlertViewExt.isShowing()) {
                mAlertViewExt.dismissImmediately();
            }
            return;
        } else {
            if (mAlertViewExt != null && mAlertViewExt.isShowing()) {
                mAlertViewExt.dismissImmediately();
            }
        }
    }

    private boolean tips_isshowing, isMainList;
    private int three_notice = 0 ;
    private boolean mSearchSuccess = false ;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Actions.MQTT_GET_ACCEPTED.equals(intent.getAction())) { //获取到设备信息
                try{
                    JSONObject param = JSONObject.parseObject(intent.getStringExtra(Actions.MQTT_GET_ACCEPTED_DATA_JSON));
                    JSONObject state = param.getJSONObject("state");
                    if (state.containsKey("reported")) {
                        acceptConnectedStateAndAdd(state.getJSONObject("reported"));
                    }
                }catch (Exception ex){
                }
            } else if (Actions.MQTT_UPDATE_ACCEPTED.equals(intent.getAction())) { //收到更新信息
                try{
                    JSONObject param = JSONObject.parseObject(intent.getStringExtra(Actions.MQTT_UPDATE_ACCEPTED_DATA_JSON));
                    JSONObject state = param.getJSONObject("state");
                    if (state.containsKey("reported")) {
                        acceptConnectedStateAndAdd(state.getJSONObject("reported"));
                    }
                }catch (Exception ex){
                }
            }else if (Actions.SEND_SEARCHZHUJICOMMAND.equals(intent.getAction())) {
                search_count++;
                if (search_count >= search_cmax) {
                    mSearchSuccess = true ;
                    dismissProgressDialogSearchZhuji();
                    serach_process.cancelAnimate();
                    timer.cancel();
                    ll_fail.setVisibility(View.VISIBLE);
//					Toast.makeText(ConnectActivity.this, getString(R.string.activity_add_zhuji_nozhu_again), Toast.LENGTH_LONG).show();
//                    new AlertView(getString(R.string.tips), getString(R.string.activity_add_zhuji_idzhu_nofondinwifi), getString(R.string.cancel), new String[]{getString(R.string.sure)}, null, ConnectActivity.this, AlertView.Style.Alert.Alert, new OnItemClickListener() {
//
//                        @Override
//                        public void onItemClick(Object o, int position) {
//                            if (position != -1) {
////                                if (isMainList) {
////                                    addDeviceBySerial();
////                                } else {
////                                }
//                                addZhujiBySerial();
//                            }
//                        }
//                    }).show();
                    return;
                }
                SyncMessage syncMessage = new SyncMessage(SyncMessage.CommandMenu.rq_szhuji);
                try {
                     bdlocation = getJdmApplication().getLocation() ;
                    if (bdlocation != null) {
                        JSONObject pJsonObject = new JSONObject();
                        pJsonObject.put("country", bdlocation.getCountry());
                        pJsonObject.put("province", bdlocation.getProvince());
                        pJsonObject.put("city", bdlocation.getCity());
                        pJsonObject.put("district", bdlocation.getDistrict());
                        pJsonObject.put("street", bdlocation.getStreet());
                        pJsonObject.put("addr", bdlocation.getAddrStr());
                        pJsonObject.put("lng", bdlocation.getLongitude());
                        pJsonObject.put("lat", bdlocation.getLatitude());
                        syncMessage.setSyncBytes(pJsonObject.toJSONString().getBytes("UTF-8"));
                    }
                }catch (UnsupportedEncodingException ex){
                    LogUtil.e(ConnectActivity.this,"ConnectActivity","unsupport UTF-8");
                }
                SyncMessageContainer.getInstance().produceSendMessage(syncMessage);
            } else if (Actions.SEARCH_ZHUJI_RESPONSE.equals(intent.getAction())&&!mSearchSuccess) { //搜索主机返回
                Log.d(DEBUG_TAG,"搜索广播");
                if (intent.getIntExtra("data", 1000000) == 0) { //完成主机搜索
                    Log.d(DEBUG_TAG,"完成主机搜索");
                    dismissProgressDialogSearchZhuji();
                    mSearchSuccess = true ;
                    isSerachingSuccess = true;
                    timer.cancel();
                    tv_serach.setText(getString(R.string.activity_add_zhuji_havezhu_addsuccess));
//                    finish();
                    final Intent in = new Intent();
                    in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    in.setClass(ConnectActivity.this, DeviceMainActivity.class);
                    if (Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                        Intent intent1 = new Intent(Actions.ADD_NEW_ZHUJI);
                        intent1.putExtra("masterId", String.valueOf(intent.getStringExtra("masterId")));
                        ConnectActivity.this.sendBroadcast(intent1);
                    }
                    if(Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())&&(bdlocation==null||
                            !BaiduLBSUtils.judgeLocationSucess(bdlocation.getLocType()))){
                        AlertView temp = new AlertView(getString(R.string.activity_add_zhuji_wangduoduo_tip_first)
                                + String.valueOf(intent.getStringExtra("masterId")) + getString(R.string.activity_add_zhuji_wangduoduo_tip_middle), null,
                                getString(R.string.pickerview_submit), null, null, ConnectActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                            @Override
                            public void onItemClick(Object o, int position) {
                                startActivity(in);
                            }
                        });
                        temp.setCancelable(false);
                        temp.show();
                    }else{
                        startActivity(in);
                    }
              //      startActivity(in);
                    Toast.makeText(ConnectActivity.this, getString(R.string.activity_add_zhuji_havezhu_addsuccess),
                            Toast.LENGTH_LONG).show();
                } else if (intent.getIntExtra("data", 1000000) == 1) {
                    Log.d(DEBUG_TAG,"需要授权");
                    three_notice++;
                    //需要授权
                    if(three_notice>=4){
                        dismissProgressDialogSearchZhuji();
                    }
                    mSearchSuccess = true ;

                    String name = intent.getStringExtra("value");
                    int count = name != null ? name.split(",").length : 0;
                    String message = "";
                    if (count > 1) {
                        message += getString(R.string.activity_add_zhuji_nozhu_type_p_more);
                        message += "\"";
                        message += intent.getStringExtra("value");
                        message += "\"";
                        message += getString(R.string.activity_add_zhuji_nozhu_type_f_more);
                    } else {
                        message += getString(R.string.activity_add_zhuji_nozhu_type_p_one);
                        message += "\"";
                        message += intent.getStringExtra("value");
                        message += "\"";
                        message += getString(R.string.activity_add_zhuji_nozhu_type_f_one);
                    }
                    if (!tips_isshowing&&three_notice>3) {
                        timer.cancel();
                        tips_isshowing = true;
                        new AlertView(getString(R.string.activity_add_zhuji_nozhu_typetitle),
                                message,
                                null, new String[]{getString(R.string.sure)}, null, ConnectActivity.this, AlertView.Style.Alert, new OnItemClickListener() {

                            @Override
                            public void onItemClick(Object o, int position) {
                                tips_isshowing = false;
                                three_notice = 0 ;
                            }
                        }).show();
                    }
                } else {
                    //无操作 未搜索到主机
                    Log.d(DEBUG_TAG,"TCP没有搜索到主机");
                }
            }
        }
    };

    private void dismissProgressDialogSearchZhuji() {
        handler.removeMessages(handler_key.SEARCHING.ordinal());
        handler.removeMessages(handler_key.SERACH_TIME.ordinal());
        if (serach_process != null) serach_process.setProgress(100);
    }

    private int search_count = 0;  //搜索次数
    private int search_cmax = 10;  //搜索最大次数
    private int search_time = 5000;  //搜索重发间隔 单位ms


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        handler.removeCallbacksAndMessages(null);
        handler = null;
    }

    //配网倒计时，100ms为一个刻度，配网时间为1min.
    private void openTimer(final int type) {
        secondleft = 0;
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (type == 1) {
                    handler.sendEmptyMessage(handler_key.TICK_TIME.ordinal());
                } else {
                    handler.sendEmptyMessage(handler_key.SERACH_TIME.ordinal());
                }
            }
        }, 1000, 100);

    }

    EsptouchAsyncTask3 esptouchAsyncTask3;

    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {

        private ProgressDialog mProgressDialog;

        private IEsptouchTask mEsptouchTask;
        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            openTimer(1);
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                // !!!NOTICE
                String apSsid = mWifiAdmin.getWifiConnectedSsidAscii(params[0]);
                String apBssid = params[1];
                String apPassword = params[2];
                String taskResultCountStr = params[3];
                taskResultCount = Integer.parseInt(taskResultCountStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, ConnectActivity.this);
                mEsptouchTask.setEsptouchListener(myListener);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            if (isCancelled())
                return;
            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                int count = 0;
                final int maxDisplayCount = 5;
                if (firstResult.isSuc()) {
                } else {
                    handler.sendEmptyMessage(handler_key.CONFIG_FALSE.ordinal());
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (esptouchAsyncTask3 != null && esptouchAsyncTask3.getStatus() == AsyncTask.Status.RUNNING) {
            esptouchAsyncTask3.cancel(true);
        }
//        setResult(11);
    }

    private IEsptouchListener myListener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            onEsptoucResultAddedPerform(result);
        }
    };

    private String mMasterId ;
    private Runnable mRequestRunnable ;
    private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String text = getString(R.string.hiflying_smartlinker_completed);
                Toast.makeText(ConnectActivity.this, text, Toast.LENGTH_LONG).show();
                isReceiveLink = true;
                //开启UDP接收模式
                new UDPReceiver().receive(7770, new UDPReceiver.OnUDPReceiverListener<byte[]>() {
                    @Override
                    public void receiveResult(byte[] result, InetSocketAddress inetSocketAddress) {
                        //收到数据回调，主线程
                        String udpPackage = new String(result);
                        Log.i(TAG,"收到7770端口的UDP包，内容为："+udpPackage);
                        String[] pra = udpPackage.split(":");
                        if ("+MAC".equalsIgnoreCase(pra[0])){
                            if (StringUtils.isEmpty(mMasterId)) {
                                mMasterId = pra[1];

                                SyncClientAWSMQTTConnector.getInstance().registerDevicesShadowTopic(mMasterId);
                                SyncClientAWSMQTTConnector.getInstance().getDevicesStatus(mMasterId);
                            }
                        }
                        //发送UDP广播20s
                        new UDPSend().sendString(inetSocketAddress.getAddress().getHostAddress(),inetSocketAddress.getPort(),pra[0]+":OK",20);
                    }
                });
//                JavaThreadPool.getInstance().excute(new UDPClient(ConnectActivity.this));
            }

        });

    }

    private void acceptConnectedStateAndAdd(JSONObject reported){
        if (reported.containsKey("connected") && "connected".equalsIgnoreCase(reported.getString("connected"))) {
            requestData(mMasterId);
        }
    }

    /**
     * 注册广播
     */
    private void initRegisterReceiver() {
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Actions.SEARCH_ZHUJI_RESPONSE);
        receiverFilter.addAction(Actions.CONNECTIVITY_CHANGE);
        receiverFilter.addAction(Actions.SEND_SEARCHZHUJICOMMAND);
        receiverFilter.addAction(Actions.MQTT_GET_ACCEPTED);
        receiverFilter.addAction(Actions.MQTT_UPDATE_ACCEPTED);
        registerReceiver(receiver, receiverFilter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_serach_retry:
                btn_serach_retry.setEnabled(false);
                search_count = 0;
                serach_process.setProgress(0);
                handler.sendEmptyMessage(handler_key.CONFIG_FINISH.ordinal());
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }


    /**
     * 此client引用activity时，采用的是弱引用，有利于内存释放
     *
     * @author wangjian
     */
    class UDPClient implements Runnable {
        DatagramSocket client = null;
        WeakReference<ConnectActivity> context = null;
        SocketAddress addr = null;
        String serverAddr = "";

        public UDPClient(ConnectActivity context) {
            try {
                String server = context.getDcsp().getString(DataCenterSharedPreferences.Constant.SYNC_DATA_SERVERS, "139.196.38.110:7777");
                serverAddr = server.substring(0, server.indexOf(":"));
                //新建一个DatagramSocket
                client = new DatagramSocket();
                client.setSoTimeout(5 * 1000);
                this.context = new WeakReference<ConnectActivity>(context);
                ConnectActivity activity = this.context.get();
                if (activity != null) {
                    String ip = Util.getWIFILocalIpAdress(activity);
                    addr = new InetSocketAddress(InetAddress.getByName(ip.substring(0, ip.lastIndexOf(".")) + ".255"), 5000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                //发送设置
                String recvStr = sendCommand(client, "AT+SOCKSET:1:id=1,mode=2,host=" + serverAddr + ",port=7777,xopt=3\n", true);
                int count = 0;
                while ((recvStr == null || "".equals(recvStr) || !"AT+Success".equals(recvStr.substring(0, 10))) && count < 5) {
                    count++;
                    //发送设置
                    recvStr = sendCommand(client, "AT+SOCKSET:1:id=1,mode=2,host=" + serverAddr + ",port=7777,xopt=3\n", true);
                }
                if (count >= 5) {
                    handler.sendEmptyMessage(handler_key.CONFIG_SUCCESS.ordinal());
                    return;
                }
                final ConnectActivity activity = context.get();
                if (activity != null) {
                    handler.post(new Runnable() {
                        public void run() {
                            activity.setResult(11);
                            Toast.makeText(activity, activity.getString(R.string.hiflying_smartlinker_completed),
                                    Toast.LENGTH_SHORT).show();
//                            activity.mWaitingDialog.dismiss();
                            activity.finish();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String sendCommand(DatagramSocket client, String command, boolean receive) {
            byte[] sendBuf = command.getBytes();
            DatagramPacket sendPacket = null;
            try {
                sendPacket = new DatagramPacket(sendBuf, sendBuf.length, addr);
                client.send(sendPacket);
                if (receive) {
                    //接受服务端传来的消息
                    byte[] recvBuf = new byte[500];
                    DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
                    client.receive(recvPacket);
                    String recvStr = new String(recvPacket.getData(), 0,
                            recvPacket.getLength());
                    Log.e("AddWifi8711:", recvStr);
                    return recvStr;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("8711:", "", e);
            }

            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 5 && resultCode == 10) {
            ll_serach.setVisibility(View.GONE);
            isFailure = false;
            secondleft = 0;
            roundProcess.setProgress(secondleft * 100 / 600);
            esptouchAsyncTask3 = new EsptouchAsyncTask3();
            esptouchAsyncTask3.execute(getIntent().getStringExtra("ssid"), mWifiAdmin.getWifiConnectedBssid(), getIntent().getStringExtra("password"), "1");
        } else if (requestCode == 5 && resultCode == 8) {
//            setResult(9);
            finish();
        }
    }

    @Override
    public void setResult(int flag, String result) {
        if(flag==HttpAsyncTask.Zhuji_FIND_URL_FLAG){
            if("0".equals(result)){
                SyncMessageContainer.getInstance().produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                ToastUtil.shortMessage(getString(R.string.add_success));
                mSearchSuccess = true ; //搜索到了
                mHandler.removeCallbacks(mRequestRunnable);
                dismissProgressDialogSearchZhuji();
                timer.cancel();
                finish();
                startActivity(mDMAIntent);
            }else if(result.contains("-3")){
                mHandler.removeCallbacks(mRequestRunnable);
                mSearchSuccess = true ; //搜索到了
                dismissProgressDialogSearchZhuji();
                timer.cancel();
                String personName=null;
                try {
                    personName  = URLEncoder.encode(result.substring(2),"utf-8");
                } catch (UnsupportedEncodingException e) {
                    personName = "解析异常";
                    e.printStackTrace();
                }
                //需要授权
                new AlertView(getString(R.string.remind_msg),
                        getString(R.string.add_zhuji_by_ap_permission_tip,personName),
                        null,
                        new String[]{getString(R.string.confirm)}, null,
                        this, AlertView.Style.Alert,
                        new OnItemClickListener() {
                            @Override
                            public void onItemClick(Object o, final int position) {
                                if(position!=-1){
                                    finish();
                                    startActivity(mDMAIntent);
                                }
                            }
                        }).show();
            }else{
                mHandler.removeCallbacks(mRequestRunnable);//防止请求过多
                if(!mSearchSuccess){
                    //没有搜索到请求数据,
                    mHandler.postDelayed(mRequestRunnable,2000);
                }

            }
        }
    }

//    HttpAsyncTask mTask ;
    private  void requestData(String masterId){
        //通过Http查询主机
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("key",masterId);
//        jsonObject.put("init",true);
//        mTask = new HttpAsyncTask(this,HttpAsyncTask.Zhuji_FIND_URL_FLAG);
//        mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,jsonObject);
        if (!StringUtils.isEmpty(masterId) && !StringUtils.isEmpty(getIntent().getStringExtra(HeaterShadowInfo.type))) {
            String name = "";
            if (ZhujiInfo.CtrDeviceType.AIRCARE_SINGLE_REFILL.equalsIgnoreCase(getIntent().getStringExtra(HeaterShadowInfo.type))){
                name = "Air Care";
            }else{
                name = "Pest Control";
            }
            CreateCtrUserDeviceRelationsInput createCtrUserDeviceRelationsInput = CreateCtrUserDeviceRelationsInput.builder()
                    .uid(AWSMobileClient.getInstance().getUsername())
                    .mac(masterId)
                    .name(name)
                    .type(getIntent().getStringExtra(HeaterShadowInfo.type))
                    .build();

            AWSClients.getInstance().getmAWSAppSyncClient().mutate(CreateCtrUserDeviceRelationsMutation.builder().input(createCtrUserDeviceRelationsInput).build())
                    .enqueue(mutationCallback);
        }
    }

    private GraphQLCall.Callback<CreateCtrUserDeviceRelationsMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateCtrUserDeviceRelationsMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateCtrUserDeviceRelationsMutation.Data> response) {
            Log.i(TAG,"Results GraphQL Add:" + JSON.toJSONString(response));

            runOnUiThread(() ->{
                ToastUtil.shortMessage(getString(R.string.add_success));
                mSearchSuccess = true ; //搜索到了
                mHandler.removeCallbacks(mRequestRunnable);
                dismissProgressDialogSearchZhuji();
                timer.cancel();
                finish();
                startActivity(mDMAIntent);
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error","GraphQL Add Exception", e);
        }
    };

    @Override
    public void showProgress(String text) {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void error(String message) {

    }

    @Override
    public void success(String message) {

    }
}

