package com.smartism.znzk.activity.device;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hjq.toast.ToastUtils;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.scene.ChooseTimesActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.connector.SyncClientAWSMQTTConnector;
import com.smartism.znzk.domain.HeaterScheduleInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.ToastUtil;

import org.jaaksi.pickerview.dialog.DefaultPickerDialog;
import org.jaaksi.pickerview.picker.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 添加定时
 */
public class AddHeaterScheduleActivity extends ActivityParentActivity implements TimePicker.OnTimeSelectListener, View.OnClickListener {
    private RelativeLayout custom_scene_times;
    private TextView scene_times;
    private List<HeaterScheduleInfo> scheduleList;
    private HeaterScheduleInfo schedule;
    private LinearLayout layoutTimePickerON,layoutTimePickerOFF;
    private TimePicker mTimePickerON,mTimePickerOFF;
    private String initMac;

    private String[] times;
    private String timesCode = "";

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
                    if (state.containsKey("reported") && state.getJSONObject("reported").containsKey("schedule")) {
                        ToastUtils.show("Add success");
                        finish();
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
        setContentView(R.layout.activity_add_heater_schedule);
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
        mTimePickerON = new TimePicker.Builder(mContext,TimePicker.TYPE_HOUR|TimePicker.TYPE_MINUTE|TimePicker.TYPE_12_HOUR,(TimePicker picker, Date date) -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(date.getTime());
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY,hour);
                    calendar.set(Calendar.MINUTE,minute);
                    schedule.setTime(calendar.getTimeInMillis()/1000);
                    mTimePickerOFF.onConfirm();
                })
                .setRangDate(1577808000000L, 4102416000000L)
                // 设置 Formatter
                .setFormatter(new TimePicker.DefaultFormatter() {
                    @Override
                    public CharSequence format(TimePicker picker, int type, int position, long value) {
                        if (type == TimePicker.TYPE_12_HOUR) {
                            return value == 0 ? "AM" : "PM";
                        } else if (type == TimePicker.TYPE_HOUR) {
                            if (picker.hasType(TimePicker.TYPE_12_HOUR)) {
                                if (value == 0) {
                                    return "12";
                                }
                            }
                            return String.format(Locale.ENGLISH,"%2d", value);
                        } else if (type == TimePicker.TYPE_MINUTE) {
                            return String.format(Locale.ENGLISH,"%2d", value);
                        }

                        return super.format(picker, type, position, value);
                    }
                })
                .create();
        DefaultPickerDialog dialog = (DefaultPickerDialog) mTimePickerON.dialog();
        FrameLayout frameLayout = (FrameLayout) dialog.getBtnCancel().getParent();
        frameLayout.setVisibility(View.GONE);
        mTimePickerOFF = new TimePicker.Builder(mContext,TimePicker.TYPE_HOUR|TimePicker.TYPE_MINUTE|TimePicker.TYPE_12_HOUR,(TimePicker picker, Date date) -> {

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(date.getTime());
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.HOUR_OF_DAY,hour);
                    calendar.set(Calendar.MINUTE,minute);
                    int duration = (int) (calendar.getTimeInMillis()/1000 - schedule.getTime());
                    if (duration <= 0){
                        ToastUtil.longMessage("OFF time must be later than ON");
                        cancelInProgress();
                        return;
                    }
                    schedule.setDuration(duration);
                    schedule.setState(1);

                    scheduleList.add(schedule);
                    JSONObject listSchedule = new JSONObject();
                    listSchedule.put("schedule",scheduleList);
                    SyncClientAWSMQTTConnector.getInstance().setDevicesStatus(initMac,"",listSchedule);
                })
                .setRangDate(1577808000000L, 4102416000000L)
//                .setInterceptor(new BasePicker.Interceptor() {
//                    @Override public void intercept(PickerView pickerView, LinearLayout.LayoutParams params) {
//                        pickerView.setVisibleItemCount(5);
//                        // 将年月设置为循环的
//                        int type = (int) pickerView.getTag();
//                        if (type == TimePicker.TYPE_HOUR || type == TimePicker.TYPE_MINUTE) {
//                            pickerView.setIsCirculation(true);
//                        }
//                    }
//                })
                // 设置 Formatter
                .setFormatter(new TimePicker.DefaultFormatter() {
                    @Override
                    public CharSequence format(TimePicker picker, int type, int position, long value) {
                        if (type == TimePicker.TYPE_12_HOUR) {
                            return value == 0 ? "AM" : "PM";
                        } else if (type == TimePicker.TYPE_HOUR) {
                            if (picker.hasType(TimePicker.TYPE_12_HOUR)) {
                                if (value == 0) {
                                    return "12";
                                }
                            }
                            return String.format(Locale.ENGLISH,"%2d", value);
                        } else if (type == TimePicker.TYPE_MINUTE) {
                            return String.format(Locale.ENGLISH,"%2d", value);
                        }

                        return super.format(picker, type, position, value);
                    }
                })
                .create();

        dialog = (DefaultPickerDialog) mTimePickerOFF.dialog();
        frameLayout = (FrameLayout) dialog.getBtnCancel().getParent();
        frameLayout.setVisibility(View.GONE);
        layoutTimePickerON = findViewById(R.id.layout_time_picker_ON);
        layoutTimePickerOFF = findViewById(R.id.layout_time_picker_OFF);
//        LinearLayout parent = mTimePickerON.view();
//        View child = parent.getRootView();
//        parent.removeAllViews();
        layoutTimePickerON.addView(mTimePickerON.view().getRootView());
        layoutTimePickerOFF.addView(mTimePickerOFF.view().getRootView());

        custom_scene_times = (RelativeLayout) findViewById(R.id.custom_scene_times);
        custom_scene_times.setOnClickListener(this);
        scene_times = (TextView) findViewById(R.id.scene_times);
    }

    public void back(View v) {
        finish();
    }

    @Override
    public void onTimeSelect(TimePicker picker, Date date) {
        Log.i(MainApplication.TAG,"选择器选择时间"+date.toString());
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.custom_scene_times:
                intent.setClass(this, ChooseTimesActivity.class);
                intent.putExtra("times", timesCode);
                startActivityForResult(intent, 10);
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && data != null) {
            switch (resultCode) {
                case 20:
                    //选择重复次数
                    timesCode = data.getStringExtra("times");
                    initCycle(timesCode);
                    break;
            }
        }
    }

    private void initData() {
        initMac = getIntent().getStringExtra("mac");
        scheduleList = (List<HeaterScheduleInfo>) getIntent().getSerializableExtra("schedules");
        schedule = new HeaterScheduleInfo();
        if (!CollectionsUtils.isEmpty(scheduleList)){
            if (scheduleList.size() >= 4){
                ToastUtil.longMessage("Has reached the maximum");
                finish();
            }
            schedule.setId(getNoExistId(scheduleList,1));
        }else{
            schedule.setId(1);
        }
        times = new String[]{
                getString(R.string.monday),
                getString(R.string.tuesday),
                getString(R.string.wednesday),
                getString(R.string.thursday),
                getString(R.string.friday),
                getString(R.string.saturday),
                getString(R.string.sunday),
                getString(R.string.everyday)};
    }

    private int getNoExistId(List<HeaterScheduleInfo> list,int id){
        boolean isHave = false;
        for (HeaterScheduleInfo info : list){
            if (info.getId() == id){
                isHave = true;
            }
        }
        if (isHave){
            return getNoExistId(list,++id);
        }
        return id;
    }

    //根据传回的code解析出重复的日期
    public void initCycle(String cycle) {
        if ("1111111".equalsIgnoreCase(cycle)){
            scene_times.setText(times[7]);
        }else {
            char[] codes = cycle.toCharArray();
            StringBuffer sb = new StringBuffer();
            if (codes != null && codes.length > 1) {
                for (int i = 0; i < codes.length; i++) {
                    if (String.valueOf(codes[i]).equals("1")) {
                        sb.append(times[i]);
                        sb.append(",");
                    }
                }
                sb.deleteCharAt(sb.length() - 1);
                scene_times.setText(sb.toString());
            }
        }
        schedule.setCycle(Integer.parseInt(cycle,2));
    }

    /**
     * 保存按钮
     * @param v
     */
    public void save(View v){
        if (schedule.getCycle() == 0){
            ToastUtils.show("Repeat must be selected");
            return;
        }
        showInProgress();
        mTimePickerON.onConfirm();
    }
}