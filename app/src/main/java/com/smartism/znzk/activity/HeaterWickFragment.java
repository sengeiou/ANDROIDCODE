package com.smartism.znzk.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.hjq.toast.ToastUtils;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.HeaterScheduleActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.connector.SyncClientAWSMQTTConnector;
import com.smartism.znzk.domain.HeaterScheduleCommandInfo;
import com.smartism.znzk.domain.HeaterScheduleInfo;
import com.smartism.znzk.domain.HeaterShadowInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.TemperatureUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.view.ProgressBar.CircleBarView;
import com.smartism.znzk.view.SwitchButton.SwitchButton;
import com.smartism.znzk.view.pickerview.TimePickerView;
import com.smartism.znzk.view.seekBar.OnSeekChangeListener;
import com.smartism.znzk.view.seekBar.SeekParams;
import com.smartism.znzk.view.seekBar.TickSeekBar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * Created by wangjian on 2020年03月05日.
 * 熏香机 加热芯信息
 */
public class HeaterWickFragment extends Fragment implements View.OnClickListener {
    private HeaterActivity mContext;
    private SwitchButton btnPower;
    private RadioButton radioBoost,radioTimer,radioSleep;
    private boolean radioChangeNeedEvent = true;
    private TextView textTimer,textAmbiTemp,textFluidLevel,textSchedule,textHeaterTmpProgress,textWickTmpProgress,textHeaterTmpProgressUnit,textWickTmpProgressUnit,
            textHeaterMin,textHeaterMax,textWickMin,textWickMax;
    private TickSeekBar barWickTempSet;
    private CircleBarView heaterTempShow,wickTempShow;
    private TimePickerView timePickerView;//时间选择器，选择timer时长

    /**
     * 加热丝信息
     */
    private JSONObject wickInfo;
    /**
     * 倒计时 线程
     */
    private Runnable timerCountdownRunnable;
    private Runnable scheduleCountdownRunnable;
    /**
     * 定时列表
     */
    private List<HeaterScheduleInfo> scheduleList;
    private List<HeaterScheduleCommandInfo> scheduleCommandList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = (HeaterActivity) context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_heater_wick, container, false);
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void initView(View view) {
        btnPower = (SwitchButton) view.findViewById(R.id.c_switchButton);
        radioBoost = (RadioButton) view.findViewById(R.id.radio_boost);
        radioTimer = (RadioButton) view.findViewById(R.id.radio_timer);
        radioSleep = (RadioButton) view.findViewById(R.id.radio_sleep);
        textTimer = (TextView) view.findViewById(R.id.text_timer);
        textTimer.setOnClickListener(this);
        textAmbiTemp = (TextView) view.findViewById(R.id.text_ambi);
        textFluidLevel = (TextView) view.findViewById(R.id.text_fluidlevel);
        textSchedule = (TextView) view.findViewById(R.id.text_schedule);
        textSchedule.setOnClickListener(this);
        heaterTempShow = (CircleBarView) view.findViewById(R.id.circle_heater_temp);
        textHeaterTmpProgressUnit = (TextView) view.findViewById(R.id.text_heater_progress_unit);
        textHeaterTmpProgress = (TextView) view.findViewById(R.id.text_heater_progress);
        heaterTempShow.setTextView(textHeaterTmpProgress);
        textHeaterMin = (TextView) view.findViewById(R.id.text_heater_min);
        textHeaterMax = (TextView) view.findViewById(R.id.text_heater_max);
        wickTempShow = (CircleBarView) view.findViewById(R.id.circle_wick_temp);
        textWickTmpProgressUnit = (TextView) view.findViewById(R.id.text_wick_progress_unit);
        textWickTmpProgress = (TextView) view.findViewById(R.id.text_wick_progress);
        wickTempShow.setTextView(textWickTmpProgress);
        textWickMin = (TextView) view.findViewById(R.id.text_wick_min);
        textWickMax = (TextView) view.findViewById(R.id.text_wick_max);

        barWickTempSet = (TickSeekBar) view.findViewById(R.id.bar_wick);

        /**
         * 时间控件
         */
        timePickerView = new TimePickerView(mContext, TimePickerView.Type.HOURS_MINS);
        timePickerView.setTime(new Date());
        timePickerView.setCyclic(true);
        timePickerView.setCancelable(true);
        // 时间选择后回调
        timePickerView.setOnTimeSelectListener((Date date) -> {
            textTimer.setText(getTime(date));
            wickInfo.put(HeaterShadowInfo.timer,System.currentTimeMillis() / 1000 + getTimeToDelaySecond(date));
            updateStatusToDevice();
        });

        radioBoost.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (radioChangeNeedEvent && isChecked) {
                if (mContext.isConnected()) {
                    wickInfo.put(HeaterShadowInfo.mode, 0);
                    wickInfo.put(HeaterShadowInfo.timer,System.currentTimeMillis() / 1000 + 3600);
                    updateStatusToDevice();
                }else{
                    ToastUtils.show("Device is offline");
                    refreshPage(null);
                }
            }
        });
        radioTimer.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (radioChangeNeedEvent && isChecked) {
                if (mContext.isConnected()) {
                    wickInfo.put(HeaterShadowInfo.wickPower,HeaterShadowInfo.wickPowerON);
                    wickInfo.put(HeaterShadowInfo.mode, 1);
                    wickInfo.put(HeaterShadowInfo.timer, System.currentTimeMillis() / 1000 + 3600);
                    updateStatusToDevice();
                }else{
                    ToastUtils.show("Device is offline");
                    refreshPage(null);
                }
            }
        });
        radioSleep.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (radioChangeNeedEvent && isChecked) {
                if (mContext.isConnected()) {
                    wickInfo.put(HeaterShadowInfo.wickPower,HeaterShadowInfo.wickPowerOFF);
                    wickInfo.put(HeaterShadowInfo.mode, 2);
                    wickInfo.put(HeaterShadowInfo.timer, System.currentTimeMillis() / 1000 + 3600);
                    updateStatusToDevice();
                }else{
                    ToastUtils.show("Device is offline");
                    refreshPage(null);
                }
            }
        });

        barWickTempSet.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                //滑动时也会触发
            }

            @Override
            public void onStartTrackingTouch(TickSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(TickSeekBar seekBar) {
                if (mContext.isConnected()) {
                    String tempUnit = mContext.getDcsp().getString(DataCenterSharedPreferences.Constant.SHOW_TEMPERATURE_UNIT, "ssd");
                    if (tempUnit.equals("ssd")) {
                        wickInfo.put(HeaterShadowInfo.wickTempSet, seekBar.getProgress());
                    } else {
                        wickInfo.put(HeaterShadowInfo.wickTempSet, (int) TemperatureUtil.fsdTossd(seekBar.getProgress()));
                    }
                    updateStatusToDevice();
                }else{
                    ToastUtils.show("Device is offline");
                    refreshPage(null);
                }
            }
        });


        btnPower.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (mContext.isConnected()) {
                if (isChecked){
                    wickInfo.put(HeaterShadowInfo.wickPower,HeaterShadowInfo.wickPowerON);
                }else{
                    wickInfo.put(HeaterShadowInfo.wickPower,HeaterShadowInfo.wickPowerOFF);
                }
                updateStatusToDevice();
            }else{
                ToastUtils.show("Device is offline");
                refreshPage(null);
            }
        });
        heaterTempShow.setOnAnimationListener(new CircleBarView.OnAnimationListener() {
            @Override
            public String howToChangeText(float interpolatedTime, float updateNum, float maxNum) {
                return String.valueOf(Math.round(interpolatedTime * updateNum));
            }

            @Override
            public void howTiChangeProgressColor(Paint paint, float interpolatedTime, float updateNum, float maxNum) {

            }
        });
        wickTempShow.setOnAnimationListener(new CircleBarView.OnAnimationListener() {
            @Override
            public String howToChangeText(float interpolatedTime, float updateNum, float maxNum) {
                return String.valueOf(Math.round(interpolatedTime * updateNum));
            }

            @Override
            public void howTiChangeProgressColor(Paint paint, float interpolatedTime, float updateNum, float maxNum) {

            }
        });
    }

    public JSONObject getWickStatus(){
        JSONObject status = new JSONObject();
        status.put(HeaterShadowInfo.wickId,wickInfo.getIntValue(HeaterShadowInfo.wickId));
        status.put(HeaterShadowInfo.wickPower,wickInfo.getString(HeaterShadowInfo.wickPower));
        status.put(HeaterShadowInfo.wickTempSet,wickInfo.getIntValue(HeaterShadowInfo.wickTempSet));
        status.put(HeaterShadowInfo.mode,wickInfo.getIntValue(HeaterShadowInfo.mode));
        status.put(HeaterShadowInfo.timer,wickInfo.getLongValue(HeaterShadowInfo.timer));
        return status;
    }

    public void updateStatusToDevice(){
        mContext.showInProgress(getString(R.string.ongoing));
        JSONArray wicks = new JSONArray();
        wicks.add(getWickStatus());
        if (ZhujiInfo.CtrDeviceType.AIRCARE_DOUBLE_REFILL.equalsIgnoreCase(wickInfo.getString(HeaterShadowInfo.type))){
            if (wickInfo.getIntValue(HeaterShadowInfo.wickId) == 1){
                wicks.add(mContext.getWickFragment2().getWickStatus());
            }else{
                wicks.add(mContext.getWickFragment1().getWickStatus());
            }
        }
        if (wickInfo.getBooleanValue("isGroup")){
            JSONArray array = wickInfo.getJSONArray("groupDevices");
            if (array!=null && array.size() > 0){
                JSONObject status = new JSONObject();
                status.put("heaterStrip", wicks);
                for (int i = 0; i < array.size(); i++) {
                    SyncClientAWSMQTTConnector.getInstance().setDevicesStatus(array.getString(i), "", status);
                }
            }
            mContext.cancelInProgress();
        }else {
            JSONObject status = new JSONObject();
            status.put("heaterStrip", wicks);
            SyncClientAWSMQTTConnector.getInstance().setDevicesStatus(wickInfo.getString(HeaterShadowInfo.mac), "", status);
        }
    }

    /**
     * 初始化数据，一般是默认数据
     */
    private void initData() {
        String tempUnit = mContext.getDcsp().getString(DataCenterSharedPreferences.Constant.SHOW_TEMPERATURE_UNIT, "ssd");
        heaterTempShow.setProgressNum(0,0);
        wickTempShow.setProgressNum(0,0);
        if (tempUnit.equals("ssd")) {
            textWickTmpProgressUnit.setText("℃");
            textHeaterTmpProgressUnit.setText("℃");
            barWickTempSet.setProgressUnit("℃");
            textHeaterMin.setText("0℃");
            textWickMin.setText("0℃");
            textHeaterMax.setText("160℃");
            textWickMax.setText("160℃");
            heaterTempShow.setMaxNum(160);
            wickTempShow.setMaxNum(160);
            barWickTempSet.setMax(160);
        }else{
            textWickTmpProgressUnit.setText("℉");
            textHeaterTmpProgressUnit.setText("℉");
            barWickTempSet.setProgressUnit("℉");
            textHeaterMin.setText("0℉");
            textWickMin.setText("0℉");
            textHeaterMax.setText("320℉");
            textWickMax.setText("320℉");
            heaterTempShow.setMaxNum(320);
            wickTempShow.setMaxNum(320);
            barWickTempSet.setMax(320);
        }
        if (wickInfo==null){
            wickInfo = new JSONObject();
            try {
                wickInfo.put(HeaterShadowInfo.wickId, getArguments().getString(HeaterShadowInfo.wickId));
                wickInfo.put(HeaterShadowInfo.mac, getArguments().getString(HeaterShadowInfo.mac));
            }catch (NullPointerException ex){
                Log.e(MainApplication.TAG,"WickFragment id is null!!");
            }
        }
    }

    /**
     * 由返回的数据更新页面显示
     * @param parameter
     */
    public void refreshPage(JSONObject parameter) {
        mContext.cancelInProgress();
        String tempUnit = mContext.getDcsp().getString(DataCenterSharedPreferences.Constant.SHOW_TEMPERATURE_UNIT, "ssd");
        if (parameter!=null) {
            if (wickInfo == null) {
                wickInfo = parameter;
            } else {
                wickInfo.putAll(parameter);
            }
        }else{
            if (wickInfo == null){
                ToastUtils.show("Data error on the page");
                return;
            }
        }

        if (HeaterShadowInfo.wickPowerON.equalsIgnoreCase(wickInfo.getString(HeaterShadowInfo.wickPower))){
            btnPower.setCheckedNoEvent(true);
        }else{
            btnPower.setCheckedNoEvent(false);
        }

        if (wickInfo.containsKey(HeaterShadowInfo.mode)){
            radioChangeNeedEvent = false;
            switch (wickInfo.getIntValue(HeaterShadowInfo.mode)){
                case 0:
                    radioBoost.setChecked(true);
                    radioTimer.setChecked(false);
                    radioSleep.setChecked(false);
                    break;
                case 1:
                    radioBoost.setChecked(false);
                    radioTimer.setChecked(true);
                    radioSleep.setChecked(false);
                    break;
                case 2:
                    radioBoost.setChecked(false);
                    radioTimer.setChecked(false);
                    radioSleep.setChecked(true);
                    break;
            }
            radioChangeNeedEvent = true;
        }

        if (wickInfo.containsKey(HeaterShadowInfo.ambientTemperature)){
            float ambiTemp = wickInfo.getFloatValue(HeaterShadowInfo.ambientTemperature);
            if (tempUnit.equals("ssd")) {
                textAmbiTemp.setText(String.valueOf(Math.round(ambiTemp)) + "℃");
            } else if (tempUnit.equals("hsd")) {
                textAmbiTemp.setText(String.valueOf(Math.round(TemperatureUtil.ssdToFsd(ambiTemp)))+"℉");
            }
        }
        if (wickInfo.containsKey(HeaterShadowInfo.heaterTemperature)){
            int heaterTemperature = wickInfo.getIntValue(HeaterShadowInfo.heaterTemperature);
            if (tempUnit.equals("ssd")) {
                heaterTempShow.setProgressNum(heaterTemperature,0);
            } else if (tempUnit.equals("hsd")) {
                heaterTempShow.setProgressNum((int) TemperatureUtil.ssdToFsd(heaterTemperature),0);
            }
        }
        if (wickInfo.containsKey(HeaterShadowInfo.wickTemperature)){
            int wickTemperature = wickInfo.getIntValue(HeaterShadowInfo.wickTemperature);
            if (tempUnit.equals("ssd")) {
                wickTempShow.setProgressNum(wickTemperature,0);
            } else if (tempUnit.equals("hsd")) {
                wickTempShow.setProgressNum((int) TemperatureUtil.ssdToFsd(wickTemperature),0);
            }
        }
        if (wickInfo.containsKey(HeaterShadowInfo.fluidLevel)){
            int fluidLevel = wickInfo.getIntValue(HeaterShadowInfo.fluidLevel);
            switch (fluidLevel){
                case 0:
                    textFluidLevel.setText("Replace Bottle");
                    break;
                default:
                    textFluidLevel.setText("Normal");
                    break;
            }
        }
        if (wickInfo.containsKey(HeaterShadowInfo.timer)){
            initTimerCountdown();
        }

        if (wickInfo.containsKey(HeaterShadowInfo.wickTempSet)){
            float wickTempSet = wickInfo.getFloatValue(HeaterShadowInfo.wickTempSet);
            if (tempUnit.equals("ssd")) {
                barWickTempSet.setProgress(wickTempSet);
            } else if (tempUnit.equals("hsd")) {
                barWickTempSet.setProgress((int) TemperatureUtil.ssdToFsd(wickTempSet));
            }
        }

        if (wickInfo.containsKey("schedule")){
            if (scheduleList == null){
                scheduleList = new ArrayList<>();
            }
            scheduleList.clear();
            scheduleList.addAll(JSONObject.parseArray(wickInfo.getJSONArray("schedule").toJSONString(), HeaterScheduleInfo.class));
            initScheduleCountdown();
        }
    }

    private void updateTextSchedule(long timer,int onOrOff){
        textSchedule.setText(String.format("%s:%s %s",String.valueOf(timer/3600<10?"0"+(timer/3600):timer/3600),String.valueOf(timer%3600/60<10?"0"+(timer%3600/60):timer%3600/60),onOrOff == 0 ? HeaterShadowInfo.wickPowerOFF : HeaterShadowInfo.wickPowerON));
    }

    private void updateTextTimer(long timer){
        textTimer.setText(String.format("%s:%s",String.valueOf(timer/3600<10?"0"+(timer/3600):timer/3600),String.valueOf(timer%3600/60<10?"0"+(timer%3600/60):timer%3600/60)));
    }

    private void initScheduleCountdown(){
        if (scheduleCommandList == null){
            scheduleCommandList = new ArrayList<>();
        }
        scheduleCommandList.clear();
        //将定时的所有事件都拿出来
        for (HeaterScheduleInfo schedule: scheduleList) {
            //当前定时是否有效
            if (schedule.getState() == 1){
                //当天是否是在循环周期内
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                int week = calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 7 : calendar.get(Calendar.DAY_OF_WEEK) - 1;
                if (Util.intBitIsTrue(schedule.getCycle(),week)){
                    updateTimeToNowDay(schedule);
                    HeaterScheduleCommandInfo cInfo = new HeaterScheduleCommandInfo();
                    cInfo.setOnOrOff(1);
                    cInfo.setTime(schedule.getTime());
                    scheduleCommandList.add(cInfo);

                    calendar.setTimeInMillis(schedule.getTime());
                    calendar.add(Calendar.SECOND,schedule.getDuration());
                    cInfo = new HeaterScheduleCommandInfo();
                    cInfo.setOnOrOff(0);
                    cInfo.setTime(calendar.getTimeInMillis());
                    scheduleCommandList.add(cInfo);
                }
                //第二天的定时
                week = week + 1 == 8 ? 1 : week + 1;
                if (Util.intBitIsTrue(schedule.getCycle(),week)){
                    calendar.setTimeInMillis(schedule.getTime());
                    calendar.add(Calendar.DAY_OF_YEAR,1);

                    HeaterScheduleCommandInfo cInfo = new HeaterScheduleCommandInfo();
                    cInfo.setOnOrOff(1);
                    cInfo.setTime(calendar.getTimeInMillis());
                    scheduleCommandList.add(cInfo);

                    cInfo = new HeaterScheduleCommandInfo();
                    cInfo.setOnOrOff(0);
                    calendar.add(Calendar.SECOND,schedule.getDuration());
                    cInfo.setTime(calendar.getTimeInMillis());
                    scheduleCommandList.add(cInfo);
                }
            }
        }
        if (!CollectionsUtils.isEmpty(scheduleCommandList)) {
            //小到大排序
            Collections.sort(scheduleCommandList, new Comparator<HeaterScheduleCommandInfo>() {
                @Override
                public int compare(HeaterScheduleCommandInfo o1, HeaterScheduleCommandInfo o2) {
                    return (int) (o1.getTime() - o2.getTime());
                }
            });
            HeaterScheduleCommandInfo target = null;
            for (HeaterScheduleCommandInfo command : scheduleCommandList) {
                if (System.currentTimeMillis() < command.getTime()){
                    target = command;
                    break;
                }
            }
            refreshScheduleCountdown(target);
        }else{
            removeScheduleCountdownRunnable();
            textSchedule.setText("No Schedule");
        }
    }

    /**
     * 将设定的时间戳改为当天的时间戳
     * @param scheduleInfo
     * @return
     */
    private void updateTimeToNowDay(HeaterScheduleInfo scheduleInfo){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(scheduleInfo.getTime()*1000);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY,hour);
        calendar.set(Calendar.MINUTE,minute);
        scheduleInfo.setTime(calendar.getTimeInMillis());
    }

    /**
     * 参数是未来要执行的第一个操作。
     * @param commandInfo
     */
    private void refreshScheduleCountdown(HeaterScheduleCommandInfo commandInfo){
        if (commandInfo == null){
            removeScheduleCountdownRunnable();
            textSchedule.setText("No Schedule");
            return;
        }
        long timer = commandInfo.getTime()/1000;
        long now = System.currentTimeMillis()/1000;
        //+59的原因是以分钟为单位的
        timer = timer - now + 59;

        if (timer > 60){
            removeScheduleCountdownRunnable();
            updateTextSchedule(timer,commandInfo.getOnOrOff());
            scheduleCountdownRunnable = () -> {
                long timerDelay = commandInfo.getTime()/1000;
                long nowDelay = System.currentTimeMillis() / 1000;
                timerDelay = timerDelay - nowDelay + 59;
                if (timerDelay > 0) {
                    Log.i(MainApplication.TAG,"定时任务 schedule - 》》》获取到倒计时时间：：："+timerDelay + ",thread id:"+Thread.currentThread().getId());
                    updateTextSchedule(timerDelay,commandInfo.getOnOrOff());
                    refreshScheduleCountdown(commandInfo);
                }else{
                    initScheduleCountdown();
                }
            };
            mContext.getHandler().postDelayed(scheduleCountdownRunnable,5000);
        }else{
            initScheduleCountdown();
        }
    }

    private void removeScheduleCountdownRunnable(){
        if (scheduleCountdownRunnable!=null) {
            Log.i(MainApplication.TAG,"remove runable"+scheduleCountdownRunnable.toString());
            mContext.getHandler().removeCallbacks(scheduleCountdownRunnable);
        }
    }

    private void removeTimerCountdownRunnable(){
        if (timerCountdownRunnable!=null) {
            mContext.getHandler().removeCallbacks(timerCountdownRunnable);
        }
    }

    private void initTimerCountdown(){
        int mode = wickInfo.getIntValue(HeaterShadowInfo.mode);
        long timer = wickInfo.getLongValue(HeaterShadowInfo.timer);
        long now = System.currentTimeMillis()/1000;
        //+59的原因是以分钟为单位的
        timer = timer - now + 59;

        if (timer > 60 && mode > 0){
            removeTimerCountdownRunnable();
            timerCountdownRunnable = () -> {
                    long timerDelay = wickInfo.getLongValue(HeaterShadowInfo.timer);
                    long nowDelay = System.currentTimeMillis() / 1000;
                    timerDelay = timerDelay - nowDelay + 59;
                    if (timerDelay > 0) {
                        Log.i(MainApplication.TAG,"定时任务 - 》》》获取到倒计时时间：：："+timerDelay);
                        updateTextTimer(timerDelay);
                        initTimerCountdown();
                    }else{
                        if (timerDelay < 0){
                            timerDelay = 60*60; //默认一小时
                        }
                        updateTextTimer(timerDelay);
                    }
            };
            mContext.getHandler().postDelayed(timerCountdownRunnable,5000);
        }else{
            if (mode == 0 || timer < 0) {
                timer = 3600;
            }
            removeTimerCountdownRunnable();
        }
        updateTextTimer(timer);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.text_schedule:
                intent.setClass(mContext, HeaterScheduleActivity.class);
                intent.putExtra("mac",wickInfo.getString(HeaterShadowInfo.mac));
                startActivity(intent);
                break;
            case R.id.text_timer:
                timePickerView.show();
                break;
        }
    }

    private String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }
    private int getTimeToDelaySecond(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)) * 60;
    }
}
