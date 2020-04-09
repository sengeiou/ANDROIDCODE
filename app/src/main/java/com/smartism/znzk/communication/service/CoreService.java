package com.smartism.znzk.communication.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPolicyRequest;
import com.baidu.location.BDLocation;
import com.libhttp.entity.LoginResult;
import com.libhttp.subscribers.SubscriberListener;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PSpecial.HttpErrorCode;
import com.p2p.core.P2PSpecial.HttpSend;
import com.smartism.znzk.activity.user.LoginActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.camera.P2PConnect;
import com.smartism.znzk.camera.P2PListener;
import com.smartism.znzk.camera.SettingListener;
import com.smartism.znzk.communication.connector.SyncClientAWSMQTTConnector;
import com.smartism.znzk.communication.connector.SyncClientNettyConnector;
import com.smartism.znzk.communication.data.SyncSendMessageHandler;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.camera.Account;
import com.smartism.znzk.global.AccountPersist;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.thread.MainThread;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.NetworkUtils;
import com.smartism.znzk.util.NotificationUtil;
import com.smartism.znzk.util.StringUtils;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import me.tatarka.support.job.JobInfo;
import me.tatarka.support.job.JobScheduler;
import me.tatarka.support.os.PersistableBundle;

/**
 * @author wj
 *         coreService 后台运行的核心service
 */
public class CoreService extends Service {
    private static final String TAG = MainApplication.TAG;

    /**
     * 天气相关 OpenWeather
     * units=metric 摄氏度
     * units=imperial  华氏度
     */
    String weatherOpenWeather = "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=65d1d6a80460021801e63e867e384d18&units=metric";
    /**
     * 紫外线相关 OpenWeather
     * 获取当前的紫外线强度
     */
    String weatherOpenWeather_uv = "https://api.openweathermap.org/data/2.5/uvi?lat=%f&lon=%f&appid=65d1d6a80460021801e63e867e384d18";
    /**
     * 获取pm2.5值 10KM范围内的点，取平均值，parameter[]=pm10&parameter[]=pm25 ，带参数会不回数据
     */
    String weatherOpenaq_pm25 = "https://api.openaq.org/v1/latest?coordinates=%f,%f&radius=10000";

    private final int keepalive_time = 30000; //心跳包执行间隔
    private long keepalive_ltime = 0;//最后一次发送心跳包的时间
    private long last_excute_keepalive_method = 0;//最后一次调用sendCommandToKeepAlive方法的时间
    /**
     * 本地配置信息*
     */
    private DataCenterSharedPreferences dcsp;
    /**
     * 广播接收器*
     */
    protected BroadcastReceiver broadcastReceiver;
    protected BroadcastReceiver broadcastReceiver2;
//    private BroadcastReceiver connectionState;
    /**
     * 全局定时器
     */
    //闹钟服务好像不准,改为 handler定时发送
//    protected AlarmManager alarmManager;
    /**
     * 检查网络连接的intent用于发送广播
     */
//    protected PendingIntent checkNetIntent;
    protected Context mContext = this;
    private int pendingRequestCode = 0;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) { //服务器连接操作完成并且定时器已经启动
                broadcastReceiver = new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (Actions.CONNECTIVITY_CHANGE.equals(intent.getAction())) {// 网络状态切换广播 每一次注册都会触发一次
                            LogUtil.i(TAG, "CoreService -- Actions.CONNECTIVITY_CHANGE -- 网络变更");
                            defHandler.removeMessages(2);//需要先删除再发送，不然会导致非常多的心跳检测
                            defHandler.sendEmptyMessage(2);//有网络变更，立马发送一个心跳检测下
                        } else if (Actions.CONNECTIVITY_KEPLIVE.equals(intent.getAction())) { //网络kepalive
                            try {
                                if (NetworkUtils.CheckNetwork(mContext)) {
                                    if (!SyncClientAWSMQTTConnector.getInstance().isConnected()) {
                                        LogUtil.i(TAG, "检查到连接已经断开，开始连接！");
                                        SyncClientAWSMQTTConnector.getInstance().connect(mContext);
                                    }
                                } else {
                                    LogUtil.i(TAG, "连接检查定时器 : 当前无网络");
                                    SyncClientAWSMQTTConnector.getInstance().disconnect();
                                    mContext.sendBroadcast(new Intent(Actions.CONNECTION_NONET));
                                }
                            } catch (Exception e) {
                                Log.e("CoreService", "连接检查异常", e);
                                mContext.sendBroadcast(new Intent(Actions.CONNECTION_FAILED));
                            }
                        }
                    }
                };
                registerReceiver(broadcastReceiver, setIntentFilter());
            } else if (msg.what == 2) { //需要发送一个心跳广播
                CoreService.this.sendBroadcast(new Intent(Actions.CONNECTIVITY_KEPLIVE));
                defHandler.sendEmptyMessageDelayed(2, keepalive_time);
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.i(TAG, "Core Service onCreate ");
        dcsp = DataCenterSharedPreferences.getInstance(this, Constant.CONFIG);
//		alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        // 初始化接收服务器发送过来的消息接收线程 开启一个线程，从一个队里里面获取服务器推送过来的消息。
// 		SyncReceiveMessageHandler.getInstance(getApplicationContext())
// 				.start();
        // 初始化向服务器发送消息的线程
        SyncSendMessageHandler.getInstance(mContext).start();
        SyncClientAWSMQTTConnector.getInstance().connect(mContext);

        if (defHandler != null) {
//            defHandler.sendEmptyMessageDelayed(2, 10000); //10秒发送一次广播
            defHandler.sendEmptyMessage(1); //连接定时器启动完成，可以注册网络广播接收了
        }
        initBroadcastReceiver2();
        NotificationUtil.showOngoingTips(this);
        startJobScheduler();
        if (!StringUtils.isEmpty(MainApplication.app.getAppGlobalConfig().getAPPID())) {
            P2PHandler.getInstance().p2pInit(mContext, new P2PListener(), new SettingListener());// 初始化P2P的监听
        }
    }

    private void initBroadcastReceiver2() {
        broadcastReceiver2 = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (Actions.SET_BROADCAST_CHANGEONGOING.equals(intent.getAction())) {
                    if (intent.getBooleanExtra("ischecked", false)) {
                        NotificationUtil.cancelNotification(context, Constant.NOTIFICATIONID_ONGO);
                    } else {
                        NotificationUtil.showOngoingTips(context);
                    }
                } else if (Actions.DEVICE_STATUS_NORMAL.equals(intent.getAction())) {
//                    JavaThreadPool.getInstance().excute(new updateDeviceStatus(intent.getLongExtra("devicdid", 0)));
                } else if (Actions.APP_KICKOFF.equals(intent.getAction())) {   //踢下线
                    LogUtil.i(TAG, "被踢下线了，处理踢下线逻辑。");
                    Util.clearLoginInfo(getApplicationContext(), dcsp);
                    stopSelf();
//					ActivityManager a = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//					if(context.getPackageName().equals(a.getRunningTasks(1).get(0).topActivity.getPackageName())){
                    //跳转到登录页面，并且需要清空activity栈
                    int code = intent.getIntExtra("kickofftype", 0);
                    Intent loginIntent = new Intent();
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    loginIntent.setClass(getApplicationContext(), LoginActivity.class);
                    loginIntent.putExtra("iskickoff", true);
                    loginIntent.putExtra("msg", String.valueOf(code));
                    startActivity(loginIntent);
//					}
                } else if (Actions.APP_KICKOFF_SESSIONFAILURE.equals(intent.getAction())) {   //踢下线
                    LogUtil.i(TAG, "被踢下线了，会话失效");
                    Util.clearLoginInfo(getApplicationContext(), dcsp);
                    stopSelf();
//					ActivityManager a = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//					if(context.getPackageName().equals(a.getRunningTasks(1).get(0).topActivity.getPackageName())){
                    //跳转到登录页面，并且需要清空activity栈
                    Intent loginIntent = new Intent();
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    loginIntent.setClass(getApplicationContext(), LoginActivity.class);
                    loginIntent.putExtra("iskickoff", true);
                    loginIntent.putExtra("msg", "sessionfailure");
                    startActivity(loginIntent);
//					}
                } else if (Actions.APP_KICKOFF_OUTOFDAY.equals(intent.getAction())) {   //踢下线 已过期
                    LogUtil.i(TAG, "被踢下线了，登录已过期");
                    Util.clearLoginInfo(getApplicationContext(), dcsp);
                    stopSelf();
//					ActivityManager a = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//					if(context.getPackageName().equals(a.getRunningTasks(1).get(0).topActivity.getPackageName())){
                    //跳转到登录页面，并且需要清空activity栈
                    Intent loginIntent = new Intent();
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    loginIntent.setClass(getApplicationContext(), LoginActivity.class);
                    loginIntent.putExtra("iskickoff", true);
                    loginIntent.putExtra("msg", "outofday");
                    startActivity(loginIntent);
//					}
                } else if (Actions.APP_RECONNECTION.equals(intent.getAction())) { //重连服务器
                    // 连接服务器
                    SyncClientNettyConnector.getInstance().disconnect();
                    if (defHandler != null) {
                        defHandler.removeMessages(2);//需要先删除再发送，不然会导致非常多的心跳检测
                        defHandler.sendEmptyMessage(2);//有网络变更，立马发送一个心跳检测下
                    }
                } else if (Actions.WEATHER_GET.equals(intent.getAction())) { //刷新天气
                    refreshWeather(MainApplication.app.getLocation());
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.SET_BROADCAST_CHANGEONGOING);
        filter.addAction(Actions.DEVICE_STATUS_NORMAL);
        filter.addAction(Actions.APP_KICKOFF);//踢下线
        filter.addAction(Actions.APP_KICKOFF_SESSIONFAILURE);//密码被修改踢下线
        filter.addAction(Actions.APP_KICKOFF_OUTOFDAY);//登录已过期踢下线
        filter.addAction(Actions.APP_RECONNECTION);//重连服务器
        filter.addAction(Actions.WEATHER_GET);//刷新天气
        registerReceiver(broadcastReceiver2, filter);
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(fd, writer, args);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    public void refreshWeather(BDLocation location){
        if (location!=null){
            JavaThreadPool.getInstance().excute(()->{
                try {
                    String result = HttpRequestUtils.requestoOkHttpGet(String.format(weatherOpenWeather, location.getLatitude(), location.getLongitude()));
                    if (!StringUtils.isEmpty(result)) {
                        JSONObject weatherInfo = JSON.parseObject(result);
                        result = HttpRequestUtils.requestoOkHttpGet(String.format(weatherOpenWeather_uv,location.getLatitude(), location.getLongitude()));
                        JSONObject uvPar = JSON.parseObject(result);
                        /**
                         * {
                         *  "lat": 38.75,
                         *  "lon": 40.25,
                         *  "date_iso": "2017-06-23T12:00:00Z",
                         *  "date": 1498219200,
                         *  "value": 10.16  - 紫外线强度
                         * }
                         */
                        weatherInfo.put("uvindex",uvPar.getFloatValue("value"));
                        dcsp.putStringAndCommit(Constant.WEATHER_INFO, weatherInfo.toJSONString());
                        mContext.sendBroadcast(new Intent(Actions.WEATHER_GET_RESULT));

                        result = HttpRequestUtils.requestoOkHttpGet(String.format(weatherOpenaq_pm25,location.getLatitude(),location.getLongitude()));
                        /**
                         * {
                         *     "meta": {
                         *         "name": "openaq-api",
                         *         "license": "CC BY 4.0",
                         *         "website": "https://docs.openaq.org/",
                         *         "page": 1,
                         *         "limit": 100,
                         *         "found": 4
                         *     },
                         *     "results": [
                         *         {
                         *             "location": "湖南中医药大学",
                         *             "city": "长沙市",
                         *             "country": "CN",
                         *             "distance": 7749.792115410704,
                         *             "measurements": [
                         *                 {
                         *                     "parameter": "co",
                         *                     "value": 1000,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "no2",
                         *                     "value": 33,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "o3",
                         *                     "value": 126,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "so2",
                         *                     "value": 23,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "pm25",
                         *                     "value": 45,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "pm10",
                         *                     "value": 58,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 }
                         *             ],
                         *             "coordinates": {
                         *                 "latitude": 28.1308,
                         *                 "longitude": 112.8908
                         *             }
                         *         },
                         *         {
                         *             "location": "湖南师范大学",
                         *             "city": "长沙市",
                         *             "country": "CN",
                         *             "distance": 4019.4224443586163,
                         *             "measurements": [
                         *                 {
                         *                     "parameter": "pm25",
                         *                     "value": 52,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "pm10",
                         *                     "value": 59,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "so2",
                         *                     "value": 11,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "o3",
                         *                     "value": 128,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "no2",
                         *                     "value": 23,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "co",
                         *                     "value": 1200,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 }
                         *             ],
                         *             "coordinates": {
                         *                 "latitude": 28.19,
                         *                 "longitude": 112.9394
                         *             }
                         *         },
                         *         {
                         *             "location": "火车新站",
                         *             "city": "长沙市",
                         *             "country": "CN",
                         *             "distance": 9959.718839994177,
                         *             "measurements": [
                         *                 {
                         *                     "parameter": "no2",
                         *                     "value": 48,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "o3",
                         *                     "value": 120,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "co",
                         *                     "value": 1400,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "so2",
                         *                     "value": 16,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "pm25",
                         *                     "value": 138,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "pm10",
                         *                     "value": 80,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 }
                         *             ],
                         *             "coordinates": {
                         *                 "latitude": 28.1944,
                         *                 "longitude": 113.0014
                         *             }
                         *         },
                         *         {
                         *             "location": "高开区环保局",
                         *             "city": "长沙市",
                         *             "country": "CN",
                         *             "distance": 2448.1717201153315,
                         *             "measurements": [
                         *                 {
                         *                     "parameter": "no2",
                         *                     "value": 28,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "so2",
                         *                     "value": 21,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "pm25",
                         *                     "value": 46,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "pm10",
                         *                     "value": 62,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "o3",
                         *                     "value": 130,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 },
                         *                 {
                         *                     "parameter": "co",
                         *                     "value": 1500,
                         *                     "lastUpdated": "2020-03-19T03:00:00.000Z",
                         *                     "unit": "µg/m³",
                         *                     "sourceName": "ChinaAQIData",
                         *                     "averagingPeriod": {
                         *                         "value": 1,
                         *                         "unit": "hours"
                         *                     }
                         *                 }
                         *             ],
                         *             "coordinates": {
                         *                 "latitude": 28.2189,
                         *                 "longitude": 112.8872
                         *             }
                         *         }
                         *     ]
                         * }
                         */
                        if (!StringUtils.isEmpty(result)){
                            JSONObject pm25Json = JSON.parseObject(result);
                            if (pm25Json.containsKey("results") && pm25Json.getJSONArray("results").size() > 0){
                                JSONArray pm25List = pm25Json.getJSONArray("results");
                                int totalPm25 = 0,totalPm10 = 0;
                                for (int i = 0; i < pm25List.size(); i++) {
                                    JSONObject pm25City = pm25List.getJSONObject(i);
                                    if (pm25City.containsKey("measurements") && pm25City.getJSONArray("measurements").size() > 0){
                                        JSONArray array = pm25City.getJSONArray("measurements");
                                        for (int j = 0; j < array.size(); j++) {
                                            JSONObject pm = array.getJSONObject(j);
                                            if ("pm25".equalsIgnoreCase(pm.getString("parameter"))){
                                                totalPm25 += pm.getIntValue("value");
                                            }else if("pm10".equalsIgnoreCase(pm.getString("parameter"))){
                                                totalPm10 += pm.getIntValue("value");
                                            }
                                        }
                                    }
                                }
                                weatherInfo.put("pm25",totalPm25/pm25List.size());
                                weatherInfo.put("pm10",totalPm10/pm25List.size());
                                dcsp.putStringAndCommit(Constant.WEATHER_INFO, weatherInfo.toJSONString());
                                mContext.sendBroadcast(new Intent(Actions.WEATHER_GET_RESULT));
                            }
                        }
                    }
                }catch (Exception ex){
                    Log.e(TAG,"获取天气异常",ex);
                }
            });
        }
    }

    /**
     * 设置广播接收器IntentFilter
     *
     * @return
     */
    private IntentFilter setIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.CONNECTIVITY_CHANGE);
        filter.addAction(Actions.CONNECTIVITY_KEPLIVE);
        return filter;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        /*****************摄像头的服务***********/
        if (!StringUtils.isEmpty(MainApplication.app.getAppGlobalConfig().getAPPID())) {
            Account account = AccountPersist.getInstance().getActiveAccountInfo(this);
            try {
                if (account != null) {
                    P2PHandler.getInstance().p2pInit(mContext, new P2PListener(), new SettingListener());
                    boolean result = P2PHandler.getInstance().p2pConnect(account.three_number,account.sessionId,account.sessionId2, account.rCode1, account.rCode2,0);
                    if (result) {
                        new P2PConnect(getApplicationContext());
                        MainThread.getInstance(getApplicationContext()).go();
                    } else {
//                        LogUtil.e(getApplicationContext(),"摄像头初始化错误","调用p2pConnect失败，返回false,摄像头可能看不了");
                    }
                } else {
                    loginAndConnectionP2p();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        /***************摄像头服务********/
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtil.i(TAG, "onStartCommand,此方法会多次调用。。。");
        flags = Service.START_STICKY | Service.START_FLAG_RETRY;
        sendCommandToKeepAlive();
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 同步方法 此方法会触发心跳广播发送心跳，但是只能3秒发送一次
     */
    private synchronized void sendCommandToKeepAlive(){
        if (last_excute_keepalive_method + 3000 > System.currentTimeMillis()){
            return;
        }
        last_excute_keepalive_method = System.currentTimeMillis();
        CoreService.this.sendBroadcast(new Intent(Actions.CONNECTIVITY_KEPLIVE)); //这里多次调用会导致心跳同一时间发送过多，在未收到回应时，就已经判断离线了会断开重连。又为了能够更快连上，所以增加一个最后调用时间
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        LogUtil.i(TAG, TAG + " onDestroy......");
        /************摄像头资源释放**************/
        if (!StringUtils.isEmpty(MainApplication.app.getAppGlobalConfig().getAPPID())) {
            MainThread.getInstance(mContext).kill();
            new Thread() {
                public void run() {
                    P2PHandler.getInstance().p2pDisconnect();
                    Intent i = new Intent();
                    i.setAction("DISCONNECT");
                    mContext.sendBroadcast(i);
                }

                ;
            }.start();
        }
        /************摄像头资源释放结束************/
//        alarmManager.cancel(checkNetIntent);  //取消定时器
//        alarmManager = null;
        try {
            if (broadcastReceiver != null) {
                unregisterReceiver(broadcastReceiver);
            }
        } catch (Exception e) {
            Log.w(TAG, "coreservice解注册异常：", e);
        }
        try {
            if (broadcastReceiver2 != null) {
                unregisterReceiver(broadcastReceiver2);
            }
        } catch (Exception e) {
            Log.w(TAG, "coreservice解注册异常2：", e);
        }
        // 断开服务器连接
        SyncClientNettyConnector.getInstance().disconnect();
        NotificationUtil.cancelNotification(mContext, Constant.NOTIFICATIONID_ONGO);
        defHandler.removeCallbacksAndMessages(null);
        defHandler = null;
        super.onDestroy();
    }


    private void startJobScheduler() {
        // Get an instance of the JobScheduler, this will delegate to the system JobScheduler on api 21+
// and to a custom implementataion on older api levels.
        JobScheduler jobScheduler = JobScheduler.getInstance(mContext);

// Extras for your job.
        PersistableBundle extras = new PersistableBundle();
        extras.putString("key", "value");

// Construct a new job with your service and some constraints.
// See the javadoc for more detail.
        JobInfo job = new JobInfo.Builder(0 /*jobid*/, new ComponentName(mContext, KeepLiveService.class))
                .setMinimumLatency(5000) //最小执行时间
                .setOverrideDeadline(10000)//最大执行时间
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) //任意一个网络执行
                .setRequiresCharging(false) //设置设备是否只有在充电时执行
                .setExtras(extras)
                .build();

        jobScheduler.schedule(job);
    }

    private void loginAndConnectionP2p() {
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                String http =  server + "/jdm/s3/ipcs/P2PVerifyCode";
                String result = "";
                result = HttpRequestUtils.requestoOkHttpPost(http, null,dcsp);
                // -1参数为空 -2校验失败 -10服务器不存在
                if (result != null && result.length() > 10) {
                    JSONObject jsonObject = JSONObject.parseObject(result);
                    final String password = jsonObject.getString("p");
                    JSONObject JSONobj = jsonObject.getJSONObject("jiwei");
                    String contactId = JSONobj.getString("contactId");
                    String countryCode = JSONobj.getString("countryCode");
                    final String email = JSONobj.getString("email");
                    String error_code = JSONobj.getString("error_code");
                    String phone = JSONobj.getString("phone");
                    String rCode1 = JSONobj.getString("rCode1");
                    String rCode2 = JSONobj.getString("rCode2");
                    String sessionId = JSONobj.getString("sessionId");

                    defHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            HttpSend.getInstance().login(email, password, new SubscriberListener<LoginResult>() {
                                @Override
                                public void onStart() {

                                }

                                @Override
                                public void onNext(LoginResult loginResult) {
                                    Log.i(TAG, "jiwei p2p 登录结果："+ JSON.toJSONString(loginResult));
                                    switch (loginResult.getError_code()) {
                                        case "0":
                                            //code1与code2是p2p连接的鉴权码,只有在帐号异地登录或者服务器强制刷新(一般不会干这件事)时才会改变
                                            //所以可以将code1与code2保存起来,只需在下次登录时刷新即可
                                            saveAuthor(loginResult,password);
                                            break;
                                        case HttpErrorCode.ERROR_10902011:
                                            break;
                                        case HttpErrorCode.ERROR_10902003:
                                            break;
                                        default:
                                            //其它错误码需要用户自己实现
                                            break;
                                    }
                                }

                                @Override
                                public void onError(String error_code, Throwable throwable) {

                                }
                            });
                        }
                    });
                }else{
                    Log.i(TAG, "run: 获取摄像头账号失败！！返回内容为："+result);
                }

            }
        });
    }
    private void saveAuthor(LoginResult loginResult,String password){
        int code1 = Integer.parseInt(loginResult.getP2PVerifyCode1());
        int code2 = Integer.parseInt(loginResult.getP2PVerifyCode2());
        int sessionId =(int)Long.parseLong(loginResult.getSessionID());
        int sessionId2 =(int)Long.parseLong(loginResult.getSessionID2());
        Account account = new Account(loginResult.getUserID(), loginResult.getEmail(), loginResult.getPhoneNO(), sessionId,sessionId2, code1, code2, loginResult.getCountryCode(), password);
        AccountPersist.getInstance().setActiveAccount(mContext, account);
        NpcCommon.mThreeNum = AccountPersist.getInstance().getActiveAccountInfo(mContext).three_number;

        P2PHandler.getInstance().p2pInit(mContext, new P2PListener(), new SettingListener());
        boolean result = P2PHandler.getInstance().p2pConnect(account.three_number,account.sessionId,account.sessionId2, account.rCode1, account.rCode2,0);
        if (result) {
            new P2PConnect(getApplicationContext());
            MainThread.getInstance(getApplicationContext()).go();
        } else {
//            LogUtil.e(getApplicationContext(),"摄像头初始化错误","调用p2pConnect失败，返回false,摄像头可能看不了");
        }
    }
}