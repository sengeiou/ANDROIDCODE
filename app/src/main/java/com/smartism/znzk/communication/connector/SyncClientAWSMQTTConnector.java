package com.smartism.znzk.communication.connector;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPolicyRequest;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SyncClientAWSMQTTConnector {
    private static final String TAG = MainApplication.TAG;
    public static final String TOPIC_PATH_PROFIX = "";//不加前缀，只用mac
    private static volatile SyncClientAWSMQTTConnector _instance;
    /***IoT相关参数***/
    public static final String IOT_ENDPOINT = "a1r48tmsgwd00p-ats.iot.us-east-1.amazonaws.com";
    //策略名称
    private static final String IOT_POLICE_NAME = "CTR-Policy";
    //服务器地区
    private static final String IOT_REGION = "us-east-1";
    /**IoT相关参数 end***/
    private boolean awsIotMqttIsConnected = false;
    private AWSIotMqttManager mqttManager;
    private AWSIotClient mIotAndroidClient;
    private Context context = null;

    public static SyncClientAWSMQTTConnector getInstance() {
        if (_instance == null) {
            synchronized (SyncClientAWSMQTTConnector.class) {
                if (_instance == null) {
                    _instance = new SyncClientAWSMQTTConnector();
                }
            }
        }
        return _instance;
    }

    public synchronized void connect(Context context) {
//        if (isConnected()) {
//            disconnect();
//            ctx = null;
//        }

        this.context = context;

        context.sendBroadcast(new Intent(Actions.CONNECTION_ING));

        JavaThreadPool.getInstance().excute(() -> {
            LogUtil.i(TAG, "init ...connection properties........");

            // Initialize the AWSIotMqttManager with the configuration
            if (mqttManager == null) {
                getInstance().context.sendBroadcast(new Intent(Actions.CONNECTION_ING));
                mqttManager = new AWSIotMqttManager(
                        UUID.randomUUID().toString(),
                        IOT_ENDPOINT);
                mqttManager.setKeepAlive(30);//心跳间隔
                mqttManager.setAutoReconnect(true);//设置自动重连
                mqttManager.setMaxAutoReconnectAttempts(Integer.MAX_VALUE);//自动重连次数
                mqttManager.setAutoResubscribe(true);//重新连接 重新自动订阅

                AttachPolicyRequest attachPolicyReq = new AttachPolicyRequest();
                attachPolicyReq.setPolicyName(IOT_POLICE_NAME); // name of your IOT AWS policy
                attachPolicyReq.setTarget(AWSMobileClient.getInstance().getIdentityId());
                mIotAndroidClient = new AWSIotClient(AWSMobileClient.getInstance());
                mIotAndroidClient.setRegion(Region.getRegion(IOT_REGION)); // name of your IoT Region such as "us-east-1"
                mIotAndroidClient.attachPolicy(attachPolicyReq);
            }

            try {
                getInstance().context.sendBroadcast(new Intent(Actions.CONNECTION_ING));
                mqttManager.connect(AWSMobileClient.getInstance(), new AWSIotMqttClientStatusCallback() {

                    @Override
                    public void onStatusChanged(final AWSIotMqttClientStatus status, final Throwable throwable) {
                        Log.d(TAG, "Connection Status: " + String.valueOf(status),throwable);
                        switch (status){
                            case Connected:
                                awsIotMqttIsConnected = true;
                                context.sendBroadcast(new Intent(Actions.CONNECTION_SUCCESS)); // 连接成功
                                break;
                            default:
                                awsIotMqttIsConnected = false;
                                getInstance().context.sendBroadcast(new Intent(Actions.CONNECTION_ING));
                                break;
                        }
                    }
                });
            } catch (final Exception e) {
                Log.e(TAG, "Connection error: ", e);
            }
        });
    }

    public synchronized void disconnect() {
        LogUtil.i(TAG, "-------------------disconnect begin ----------------------");

        if (mqttManager!=null) {
//            JavaThreadPool.getInstance().excute(new Runnable() {
//                @Override
//                public void run() {
                    try {
                        mqttManager.disconnect();
                    } catch (Exception e) {
                        Log.e(TAG, "关掉MQTT连接异常", e);
                    }
//                }
//            });
        }
        LogUtil.i(TAG, "-------------------disconnect end ----------------------");
    }

    public boolean isConnected() {
        return (mqttManager != null && awsIotMqttIsConnected);
    }

    /**
     * 注册设备的影子主题
     * @param mac
     */
    public void registerDevicesShadowTopic(String mac){
        List<String> macs = new ArrayList<>(1);
        macs.add(mac);
        registerDevicesShadowTopic(macs,"");
    }
    /**
     * 注册设备的影子主题
     * @param macs
     */
    private void registerDevicesShadowTopic(List<String> macs,String child){
        for (String mac: macs) {
            try {
                //accepted主题，当获取设备状态时，发送向主题get中发布一条空消息，则会通过这个主题返回设备详情
                mqttManager.subscribeToTopic("$aws/things/"+TOPIC_PATH_PROFIX+mac+child+"/shadow/get/accepted", AWSIotMqttQos.QOS1,
                        (final String topic, final byte[] data) -> {
                            try {
                                String message = new String(data, "UTF-8");
                                Log.d(TAG, "Message received: " + message);
                                Intent intent = new Intent(Actions.MQTT_GET_ACCEPTED);
                                intent.putExtra(Actions.MQTT_GET_ACCEPTED_DATA_JSON,message);
                                intent.putExtra(Actions.MQTT_TOPIC_THINGNAME,topic.substring(12,topic.indexOf("/shadow")));
                                context.sendBroadcast(intent);
                            } catch (UnsupportedEncodingException e) {
                                Log.e(TAG, "Message encoding error: ", e);
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Subscription accepted error: ", e);
            }
            try {
                ///get/rejected主题，当获取设备状态失败时，这个主题返回错误信息
                mqttManager.subscribeToTopic("$aws/things/"+TOPIC_PATH_PROFIX+mac+child+"/shadow/get/rejected", AWSIotMqttQos.QOS1,
                        (final String topic, final byte[] data) -> {
                            try {
                                String message = new String(data, "UTF-8");
                                Log.e(TAG, "Message received: " + message);
                                JSONObject m = JSONObject.parseObject(message);
                                if (m.getIntValue("code") != 404) {
                                    //404影子主题不存在，忽略它
                                    Intent intent = new Intent(Actions.MQTT_GET_REJECTED);
                                    intent.putExtra(Actions.MQTT_GET_REJECTED_DATA_JSON, message);
                                    intent.putExtra(Actions.MQTT_TOPIC_THINGNAME, topic.substring(12, topic.indexOf("/shadow")));
                                    context.sendBroadcast(intent);
                                }
                            } catch (UnsupportedEncodingException e) {
                                Log.e(TAG, "Message encoding error: ", e);
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Subscription rejected error: ", e);
            }
            try {
                //更新accepted主题，当设备信息变更时，通过此主题返回
                mqttManager.subscribeToTopic("$aws/things/"+TOPIC_PATH_PROFIX+mac+child+"/shadow/update/accepted", AWSIotMqttQos.QOS1,
                        (final String topic, final byte[] data) -> {
                            try {
                                String message = new String(data, "UTF-8");
                                Log.d(TAG, "Message received: " + message);
                                Intent intent = new Intent(Actions.MQTT_UPDATE_ACCEPTED);
                                intent.putExtra(Actions.MQTT_UPDATE_ACCEPTED_DATA_JSON,message);
                                intent.putExtra(Actions.MQTT_TOPIC_THINGNAME,topic.substring(12,topic.indexOf("/shadow")));
                                context.sendBroadcast(intent);
                            } catch (UnsupportedEncodingException e) {
                                Log.e(TAG, "Message encoding error: ", e);
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Subscription update accepted error: ", e);
            }
//            try {
//                //更新accepted主题，当设备信息变更时，通过此主题返回
//                mqttManager.subscribeToTopic("$aws/things/"+TOPIC_PATH_PROFIX+mac+child+"/shadow/update/rejected", AWSIotMqttQos.QOS1,
//                        (final String topic, final byte[] data) -> {
//                            try {
//                                String message = new String(data, "UTF-8");
//                                Log.d(TAG, "Message received: " + message);
//                            } catch (UnsupportedEncodingException e) {
//                                Log.e(TAG, "Message encoding error: ", e);
//                            }
//                        });
//            } catch (Exception e) {
//                Log.e(TAG, "Subscription update accepted error: ", e);
//            }
        }
    }

    /**
     * 获取设备的最新状态
     * @param macs
     */
    private void getDevicesStatus(List<String> macs,String child){
        for (String mac: macs) {
            if (isConnected()) {
                try {
                    mqttManager.publishString("", "$aws/things/"+TOPIC_PATH_PROFIX+mac+child+"/shadow/get", AWSIotMqttQos.QOS1);
                } catch (AmazonClientException ace){
                    if ("Client is disconnected or not yet connected.".equalsIgnoreCase(ace.getMessage())){
                        Log.e(TAG, "Publish error: 未连接！！");
                        getInstance().context.sendBroadcast(new Intent(Actions.CONNECTION_ING));
                    }
                }catch (Exception e) {
                    Log.e(TAG, "Publish error: ", e);
                }
            }
        }
    }
    public void getDevicesStatus(String mac){
        List<String> macs = new ArrayList<>(1);
        macs.add(mac);
        getDevicesStatus(macs,"");
    }

    /**
     * 设置设备的状态
     * @param mac
     * @param child
     * @param status
     */
    public void setDevicesStatus(String mac,String child,JSONObject status){
        if (isConnected()) {
            try {
                Log.i(TAG,"发送新消息到MyCTR/"+TOPIC_PATH_PROFIX+mac+child+",内容为:"+status.toJSONString());
                mqttManager.publishString(status.toJSONString(), "MyCTR/"+TOPIC_PATH_PROFIX+mac+child, AWSIotMqttQos.QOS0);
            } catch (Exception e) {
                Log.e(TAG, "Publish error: ", e);
            }
        }
    }
}
