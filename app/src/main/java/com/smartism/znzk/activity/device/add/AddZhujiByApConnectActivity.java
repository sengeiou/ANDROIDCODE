package com.smartism.znzk.activity.device.add;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.amplify.generated.graphql.CreateCtrUserDeviceRelationsMutation;
import com.amazonaws.amplify.generated.graphql.ListCtrUserDeviceRelationsQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.awsClient.AWSClients;
import com.smartism.znzk.communication.connector.SyncClientAWSMQTTConnector;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.domain.HeaterShadowInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.BaiduLBSUtils;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.StringUtils;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.view.MyRoundProcess;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.annotation.Nonnull;

import type.CreateCtrUserDeviceRelationsInput;
import type.TableCtrUserDeviceRelationsFilterInput;
import type.TableStringFilterInput;

/**
 * AP配网的最后一步，添加设备
 */
public class AddZhujiByApConnectActivity extends MZBaseActivity implements HttpAsyncTask.IHttpResultView{

    private static final int SEND_PROGERSS_ADD = 0X83 ;
    private static final int SEARCH_PROGRESS_ADD = 0X84 ;
    private static final int SEARCH_TIMEOUT = 0X64 ;
    private static final int SEND_OUTTIME = 0X63 ;

    private MyRoundProcess myRoundProcess,mSearchRoundProgress;
    private LinearLayout mSearchZhujiParent ;
    private TextView tv_info ,mSearchTipTv;
    private String mac;
    private int currentProgress = 0 ;
    private boolean send_timeout = false;

    HttpAsyncTask mTask ;

    Handler mHandler  = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SEARCH_PROGRESS_ADD:
                    if(currentProgress>=99){
                        mHandler.removeMessages(SEARCH_PROGRESS_ADD);
                        mHandler.sendEmptyMessage(SEARCH_TIMEOUT);
                        return ;
                    }
                    currentProgress= currentProgress+2 ;
                    mSearchRoundProgress.setProgress(currentProgress);
                    mHandler.sendEmptyMessageDelayed(SEARCH_PROGRESS_ADD,1000);
                    break ;
                case SEARCH_TIMEOUT:
                    mHandler.removeMessages(SEARCH_PROGRESS_ADD);
                    send_timeout = true ;
                    currentProgress = 99 ;
                    mSearchRoundProgress.setProgress(currentProgress);
                    new AlertView(getString(R.string.remind_msg),
                            getString(R.string.add_zhuji_by_ap_searchfailed_msg),
                            getString(R.string.cancel),
                            new String[]{getString(R.string.add_zhuji_by_ap_search_again)}, null,
                            mContext, AlertView.Style.Alert,
                            new OnItemClickListener() {
                                @Override
                                public void onItemClick(Object o, final int position) {
                                    if(position!=-1){
                                        initSearch();
                                    }else{
                                        startActivity(intent);
                                    }
                                }
                            }).show();
                    break ;
                case SEND_PROGERSS_ADD:
                    if(mSendProgress>100){
                        mHandler.removeMessages(SEND_PROGERSS_ADD);
                        return   ;
                    }
                    mSendProgress+=4 ;
                    myRoundProcess.setProgress(mSendProgress);
                    mHandler.sendEmptyMessageDelayed(SEND_PROGERSS_ADD,1000);
                    break ;
                case SEND_OUTTIME:
//                    if(mUseUdpTask!=null){
//                        mUseUdpTask.cancel(true);//取消任务，直接中断线程
//                    }
                    mHandler.removeMessages(SEND_PROGERSS_ADD);
                    mSendProgress = 99 ;
                    myRoundProcess.setProgress(mSendProgress);
                    //弹出提示框
                    new AlertView(getString(R.string.remind_msg),
                           getString(R.string.add_zhuji_by_ap_send_wifiinfo_failed),
                            null,
                            new String[]{getString(R.string.ready_guide_msg13)}, null,
                            mContext, AlertView.Style.Alert,
                            new OnItemClickListener() {
                                @Override
                                public void onItemClick(Object o, final int position) {
                                    if(position!=-1){
                                        Intent intent = new Intent();
                                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        if (Actions.VersionType.CHANNEL_QYJUNHONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                                                ||Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                                            intent.setClass(getApplicationContext(), AddZhujiActivity.class);
                                        } else {
                                            intent.setClass(getApplicationContext(), AddZhujiWayChooseActivity.class);
                                        }
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            }).setCancelable(false).show();
                    break ;
            }
        }
    };



    private int mNetId =-1 ;//Wifi id
    private String mWifiPassword  ; //wifi密码
    private String mSsid ; //wifi ssid
//    private UseUDPSendAndReceive mUseUdpTask ;
//    private String dest_ip = "192.168.4.1";
    private String apSetUrl = "http://192.168.4.1/post";
    private int dest_post = 5000;
    private int mSendProgress = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState==null){
            mSsid = getIntent().getStringExtra("ssid");
            mNetId = getIntent().getIntExtra("net_id",-1);
            mWifiPassword = getIntent().getStringExtra("password");
        }else{
            mSsid = savedInstanceState.getString("ssid");
            mNetId = savedInstanceState.getInt("net_id");
            mWifiPassword = savedInstanceState.getString("password");
        }
        initRegisterReceiver();
        initViewAndData();

        myRoundProcess.post(new Runnable() {
            @Override
            public void run() {
//                if(mUseUdpTask!=null){
//                    mUseUdpTask.cancel(true);
//                }
                myRoundProcess.setProgress(0f);
                mHandler.sendEmptyMessageDelayed(SEND_OUTTIME,25*1000);
                mHandler.sendEmptyMessageDelayed(SEND_PROGERSS_ADD,1000);
                // UDP 配置方式
//                mUseUdpTask = new UseUDPSendAndReceive(AddZhujiByApConnectActivity.this,dest_ip,dest_post);
//                StringBuilder sendData = new StringBuilder("AT+CWJAP=");
//                sendData.append("\"")
//                        .append(mSsid)
//                        .append("\"")
//                        .append(",")
//                        .append("\"")
//                        .append(mWifiPassword)
//                        .append("\"");
//                mUseUdpTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,sendData.toString());

                JavaThreadPool.getInstance().excute(() -> {
                    try {
                        String result = requestHttpServer(new URL(apSetUrl),"ssid="+mSsid+"&password="+mWifiPassword);
                        if (!StringUtils.isEmpty(result)) {
                            myRoundProcess.post(() -> {
                                receiveResult(JSONObject.parseObject(result));
                            });
                        }else{
                            ToastUtil.shortMessage(getString(R.string.set_wifi_failed));
                        }
                    }catch (Exception ne){
                        ne.printStackTrace();
                    }
                });
            }
        });
    }

    /**
     *
     * 用HttpURLConnection这个基础的方法来请求，密码如果带了特殊字符会被urlEncode转码，所以密码就错了
     *
     * @param url
     * @return
     * @throws Exception
     */
    public static String requestHttpServer(URL url,String body) throws Exception {
        StringBuilder sb = new StringBuilder();
        InputStream in = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10 * 1000);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.getOutputStream().write(body.getBytes());
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String len = null;
                while ((len = reader.readLine()) != null) {
                    sb.append(len);
                }
                reader.close();
            }
        } catch (IOException e) {
            Log.e(HttpRequestUtils.class.getName(), "发送http - get 请求IO错误(网络不给力):", e);
            throw e;
        } catch (Exception e) {
            Log.e(HttpRequestUtils.class.getName(), "发送http - get 请求异常:", e);
            throw e;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

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
                try {
                    JSONObject param = JSONObject.parseObject(intent.getStringExtra(Actions.MQTT_UPDATE_ACCEPTED_DATA_JSON));
                    JSONObject state = param.getJSONObject("state");
                    if (state.containsKey("reported")) {
                        acceptConnectedStateAndAdd(state.getJSONObject("reported"));
                    }
                } catch (Exception ex) {
                }
            }
        }
    };

    private void acceptConnectedStateAndAdd(JSONObject reported){
        if (reported.containsKey("connected") && "connected".equalsIgnoreCase(reported.getString("connected"))) {
            requestData();
        }
    }

    /**
     * 注册广播
     */
    private void initRegisterReceiver() {
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Actions.MQTT_GET_ACCEPTED);
        receiverFilter.addAction(Actions.MQTT_UPDATE_ACCEPTED);
        mContext.registerReceiver(receiver, receiverFilter);
    }


    private void initViewAndData(){

        myRoundProcess = findViewById(R.id.send_wifi_round_process); //发送Wifi信息进度
        tv_info = findViewById(R.id.sendwifi_tv_info); //发送Wifi信息提示文字
        mSearchRoundProgress = findViewById(R.id.search_round_process);
        mSearchTipTv = findViewById(R.id.search_tv_info);
        mSearchZhujiParent = findViewById(R.id.search_zhuji_parent);

        //初始化View的文字
        tv_info.setText(getString(R.string.add_zhuji_by_ap_send_wifiinfo_todevice));
        setTitle(getString(R.string.connect));//设置标题

        intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setClass(getApplicationContext(), DeviceMainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }


    Intent intent ;


    void initSearch(){
        mSearchRoundProgress.setProgress(0f);
        currentProgress = 0 ;
        send_timeout = false ;
        mHandler.sendEmptyMessage(SEARCH_PROGRESS_ADD);
        mHandler.sendEmptyMessageDelayed(100*1000,SEARCH_TIMEOUT);

        SyncClientAWSMQTTConnector.getInstance().registerDevicesShadowTopic(mac);
        SyncClientAWSMQTTConnector.getInstance().getDevicesStatus(mac);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    private  void requestData(){
        //可能网络还没有及时连接上,我们延迟发送请求任务
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("key",master_id);
//        jsonObject.put("init",true);
//        mTask = new HttpAsyncTask(AddZhujiByApConnectActivity.this,HttpAsyncTask.Zhuji_FIND_URL_FLAG);
//        mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,jsonObject);
        if (!StringUtils.isEmpty(mac) && !StringUtils.isEmpty(getIntent().getStringExtra(HeaterShadowInfo.type))) {

            AWSClients.getInstance().getmAWSAppSyncClient().query(ListCtrUserDeviceRelationsQuery.builder()
                    .filter(TableCtrUserDeviceRelationsFilterInput.builder()
                            .uid(TableStringFilterInput.builder().eq(AWSMobileClient.getInstance().getUsername()).build())
                            .build())
                    .build())
                    .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                    .enqueue(queryRelatioinsCallback);


            String name = "";
            if (ZhujiInfo.CtrDeviceType.AIRCARE_SINGLE_REFILL.equalsIgnoreCase(getIntent().getStringExtra(HeaterShadowInfo.type))){
                name = "Air Care";
            }else{
                name = "Pest Control";
            }
            CreateCtrUserDeviceRelationsInput createCtrUserDeviceRelationsInput = CreateCtrUserDeviceRelationsInput.builder()
                    .uid(AWSMobileClient.getInstance().getUsername())
                    .mac(mac)
                    .name(name)
                    .type(getIntent().getStringExtra(HeaterShadowInfo.type))
                    .build();

            AWSClients.getInstance().getmAWSAppSyncClient().mutate(CreateCtrUserDeviceRelationsMutation.builder().input(createCtrUserDeviceRelationsInput).build())
                    .enqueue(mutationCallback);
        }
    }

    private GraphQLCall.Callback<ListCtrUserDeviceRelationsQuery.Data> queryRelatioinsCallback = new GraphQLCall.Callback<ListCtrUserDeviceRelationsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListCtrUserDeviceRelationsQuery.Data> response) {
            Log.i("Results", response.data().listCtrUserDeviceRelations().items().toString());
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };

    private GraphQLCall.Callback<CreateCtrUserDeviceRelationsMutation.Data> mutationCallback = new GraphQLCall.Callback<CreateCtrUserDeviceRelationsMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateCtrUserDeviceRelationsMutation.Data> response) {
            Log.i(TAG,"Results GraphQL Add:" + JSON.toJSONString(response));
            SyncMessageContainer.getInstance().produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
            currentProgress = 100 ;
            runOnUiThread(() -> {
                mSearchRoundProgress.setProgress(currentProgress);
                ToastUtil.shortMessage(getString(R.string.add_success));
                finish();
                startActivity(intent);
            });
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", e.toString());
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("ssid",mSsid);
        outState.putInt("net_id",mNetId);
        outState.putString("password",mWifiPassword);
        super.onSaveInstanceState(outState);
    }

    //显示退出菜单项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.exit_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.exit_menu_item:
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (Actions.VersionType.CHANNEL_QYJUNHONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                        ||Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                    intent.setClass(getApplicationContext(), AddZhujiActivity.class);
                } else {
                    intent.setClass(getApplicationContext(), AddZhujiWayChooseActivity.class);
                }
                startActivity(intent);
                finish();
                return true ;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_add_zhuji_by_ap ;
    }

    @Override
    public void setResult(int flag, String result) {
        if(flag==HttpAsyncTask.Zhuji_FIND_URL_FLAG){
            if("0".equals(result)){
                SyncMessageContainer.getInstance().produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                currentProgress = 100 ;
                mSearchRoundProgress.setProgress(currentProgress);
                ToastUtil.shortMessage(getString(R.string.add_success));
                finish();
                startActivity(intent);
            }else if(result.contains("-3")){
                currentProgress = 100 ;
                mSearchRoundProgress.setProgress(currentProgress);
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
                                    startActivity(intent);
                                }
                            }
                        }).show();
            }
//            else{
//                mHandler.removeCallbacks(mRquestRunnable);//防止过多请求
//                if(!send_timeout){
//                    mHandler.postDelayed(mRquestRunnable,5000);
//                }
//            }
        }
    }

    //不显示进度条，进行重写
    @Override
    public void hideProgress() {

    }

    @Override
    public void showProgress(String text) {

    }

    @Override
    public void success(String message) {

    }

    public void receiveResult(JSONObject result) {
        mHandler.removeMessages(SEND_OUTTIME);
        if(result!=null){
            //配置成功
            mac = result.getString("mid"); //设备mac地址
            mHandler.removeMessages(SEND_PROGERSS_ADD);
            tv_info.setText(getString(R.string.add_zhuji_by_ap_get_wifiinfo));
            mSendProgress = 100 ;
            myRoundProcess.setProgress(mSendProgress);
            mSearchZhujiParent.setVisibility(View.VISIBLE); //显示搜索进度条
            mSearchTipTv.setText(getString(R.string.add_zhuji_by_ap_searchtip,mac));
            //连接网络，主要是防止用户对Wifi设置为不自动连接
            if(mNetId>=0) {
                WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                manager.enableNetwork(mNetId, true);//连接设备上次的连接过的Wifi
            }
            //5秒钟后开始搜索
            myRoundProcess.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initSearch();
                }
            },5*1000);

        }else{
            //设置失败
            ToastTools.short_Toast(this,getString(R.string.activity_editscene_set_falid));
        }
    }
    // UDP 方式回应
//    @Override
//    public void receiveResult(String result) {
//        mHandler.removeMessages(SEND_OUTTIME);
//        if(result!=null&&result.contains("CWJAP:OK")){
//            //配置成功
//            master_id = result.split(":")[2]; //主机序列号
//            mHandler.removeMessages(SEND_PROGERSS_ADD);
//            tv_info.setText(getString(R.string.add_zhuji_by_ap_get_wifiinfo));
//            mSendProgress = 100 ;
//            myRoundProcess.setProgress(mSendProgress);
//            mSearchZhujiParent.setVisibility(View.VISIBLE); //显示搜索进度条
//            mSearchTipTv.setText(getString(R.string.add_zhuji_by_ap_searchtip,master_id));
//            //连接网络，主要是防止用户对Wifi设置为不自动连接
//            if(mNetId>=0) {
//                WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//                manager.enableNetwork(mNetId, true);//连接设备上次的连接过的Wifi
//            }
//            //3秒钟后开始搜索
//            myRoundProcess.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    initSearch();
//                }
//            },3*1000);
//
//        }else{
//            //设置失败
//            ToastTools.short_Toast(this,getString(R.string.activity_editscene_set_falid));
//        }
//    }
}
