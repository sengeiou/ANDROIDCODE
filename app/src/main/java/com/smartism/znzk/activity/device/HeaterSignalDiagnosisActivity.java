package com.smartism.znzk.activity.device;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSONObject;
import com.hjq.toast.ToastUtils;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.communication.connector.SyncClientAWSMQTTConnector;
import com.smartism.znzk.domain.HeaterShadowInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.WeakRefHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 检测设备wifi信号强度
 */
public class HeaterSignalDiagnosisActivity extends ActivityParentActivity {
    private static final int WHAT_TIME_OUT = 1;
    private static final int WHAT_TIME_OUT_TIME = 5000,WHAT_TIME_OUT_TOTAL = 3;
    private List<Integer> signalList;
    private ImageView loading1,loading2,loading3;
    private ImageView progress2,progressLine1;
    private ImageView progress3,progressLine2,diagnosisLogo;
    private Button btnGot;
    private LinearLayout layoutPregress,layoutPregressText;

    private String initMac;
    //总超时次数
    private int totalTimeOut;


    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case WHAT_TIME_OUT:
                    totalTimeOut ++;
                    if (totalTimeOut <= WHAT_TIME_OUT_TOTAL) {
                        JSONObject status = new JSONObject();
                        status.put(HeaterShadowInfo.getSignalStrength, 1);
                        SyncClientAWSMQTTConnector.getInstance().setDevicesStatus(initMac, "", status);
                        mHandler.sendEmptyMessageDelayed(WHAT_TIME_OUT,WHAT_TIME_OUT_TIME);
                    }else{
                        finish();
                        ToastUtils.show("Time out");
                    }
                    break;
            }
            return false;
        }
    };
    public Handler mHandler = new WeakRefHandler(mCallback);

    private android.content.BroadcastReceiver receiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Actions.CONNECTION_FAILED.equals(intent.getAction())) {
                ToastUtil.longMessage("You are offline");
            } else if (Actions.CONNECTION_ING.equals(intent.getAction())) {
                ToastUtil.longMessage("connecting");
            } else if (Actions.MQTT_GET_ACCEPTED.equals(intent.getAction())) { //获取到设备信息
                try{
                    cancelInProgress();
                    JSONObject param = JSONObject.parseObject(intent.getStringExtra(Actions.MQTT_GET_ACCEPTED_DATA_JSON));
                    JSONObject state = param.getJSONObject("state");
                    if (state.containsKey("reported") && state.getJSONObject("reported").containsKey("schedule")) {
                    }
                }catch (Exception ex){
                    ToastUtil.longMessage("Init failed!");
                    finish();
                }
            } else if (Actions.MQTT_GET_REJECTED.equals(intent.getAction())) { //获取信息被拒绝
                ToastUtil.longMessage("Init failed!");
                finish();
            } else if (Actions.MQTT_UPDATE_ACCEPTED.equals(intent.getAction())) { //收到更新信息
                try{
                    JSONObject param = JSONObject.parseObject(intent.getStringExtra(Actions.MQTT_UPDATE_ACCEPTED_DATA_JSON));
                    JSONObject state = param.getJSONObject("state");
                    if (state.containsKey("reported") && state.getJSONObject("reported").containsKey("signalStrength")) {
                        mHandler.removeMessages(WHAT_TIME_OUT);
                        signalList.add(state.getJSONObject("reported").getIntValue("signalStrength"));
                        if (signalList.size() == 1) {
                            stopShowLoading(loading1);
                            startShowLoading(loading2);
                            progressLine1.setImageResource(R.drawable.icon_diagnosis_line_h);
                            progress2.setImageResource(R.drawable.icon_diagnosis_progress2_h);
                        }
                        if (signalList.size() < 5) {
                            mHandler.postDelayed(() -> {
                                JSONObject status = new JSONObject();
                                status.put(HeaterShadowInfo.getSignalStrength, 1);
                                SyncClientAWSMQTTConnector.getInstance().setDevicesStatus(initMac, "", status);
                                mHandler.sendEmptyMessageDelayed(WHAT_TIME_OUT,WHAT_TIME_OUT_TIME);
                            },2000);
                        }
                        if (signalList.size() >= 5){
                            stopShowLoading(loading2);
                            startShowLoading(loading3);
                            progressLine2.setImageResource(R.drawable.icon_diagnosis_line_h);
                            progress3.setImageResource(R.drawable.icon_diagnosis_progress3_h);
                            mHandler.postDelayed(() -> {
                                int total = 0;
                                for (Integer i : signalList){
                                    total += i;
                                }
                                btnGot.setVisibility(View.VISIBLE);
                                if (total/signalList.size() >= -55){
                                    diagnosisLogo.setImageResource(R.drawable.icon_diagnosis_good);
                                }else if(total/signalList.size() < -55 && total/signalList.size() >= -88){
                                    diagnosisLogo.setImageResource(R.drawable.icon_diagnosis_normal);
                                }else{
                                    diagnosisLogo.setImageResource(R.drawable.icon_diagnosis_low);
                                }
                                layoutPregress.setVisibility(View.GONE);
                                layoutPregressText.setVisibility(View.GONE);
                            },2000);
                        }
                    }
                }catch (Exception ex){
                    ToastUtil.longMessage("Init failed!");
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heater_signal_disgnosis);
        initView();
        initRegisterReceiver();
        initData();
    }

    /**
     * 注册广播
     */
    private void initRegisterReceiver() {
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Actions.CONNECTION_FAILED);
        receiverFilter.addAction(Actions.CONNECTION_ING);
        receiverFilter.addAction(Actions.MQTT_GET_ACCEPTED);
        receiverFilter.addAction(Actions.MQTT_GET_REJECTED);
        receiverFilter.addAction(Actions.MQTT_UPDATE_ACCEPTED);
        mContext.registerReceiver(receiver, receiverFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private void initView() {
        loading1 = (ImageView) findViewById(R.id.loading_diagnosis1);
        loading2 = (ImageView) findViewById(R.id.loading_diagnosis2);
        loading3 = (ImageView) findViewById(R.id.loading_diagnosis3);
        progress2 = (ImageView) findViewById(R.id.icon_progress_2);
        progressLine1 = (ImageView) findViewById(R.id.icon_progress_line1);
        progress3 = (ImageView) findViewById(R.id.icon_progress_3);
        progressLine2 = (ImageView) findViewById(R.id.icon_progress_line2);
        diagnosisLogo = (ImageView) findViewById(R.id.image_diagnosis_logo);
        btnGot = (Button) findViewById(R.id.match_btn);
        layoutPregress = (LinearLayout) findViewById(R.id.layout_icon_pregress);
        layoutPregressText = (LinearLayout) findViewById(R.id.layout_icon_pregress_text);
    }

    public void back(View v) {
        finish();
    }

    private void initData() {
        initMac = getIntent().getStringExtra("mac");
        signalList = new ArrayList<>();
        JSONObject status = new JSONObject();
        status.put(HeaterShadowInfo.getSignalStrength,1);
        SyncClientAWSMQTTConnector.getInstance().setDevicesStatus(initMac,"",status);
        mHandler.sendEmptyMessageDelayed(WHAT_TIME_OUT,WHAT_TIME_OUT_TIME);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (loading1.getVisibility() == View.VISIBLE) {
            Animation imgloading_animation = AnimationUtils.loadAnimation(HeaterSignalDiagnosisActivity.this,
                    R.anim.loading_revolve);
            imgloading_animation.setInterpolator(new LinearInterpolator());
            loading1.startAnimation(imgloading_animation);
        }
    }

    public void stopShowLoading(ImageView imageView) {
        imageView.setVisibility(View.GONE);
        imageView.clearAnimation();
    }

    public void startShowLoading(ImageView imageView) {
        if (imageView != null && imageView.getVisibility() != View.VISIBLE) {
            imageView.setVisibility(View.VISIBLE);
            Animation imgloading_animation = AnimationUtils.loadAnimation(HeaterSignalDiagnosisActivity.this,
                    R.anim.loading_revolve);
            imgloading_animation.setInterpolator(new LinearInterpolator());
            imageView.startAnimation(imgloading_animation);
        }
    }
}