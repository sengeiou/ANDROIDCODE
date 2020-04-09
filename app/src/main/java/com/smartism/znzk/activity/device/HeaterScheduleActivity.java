package com.smartism.znzk.activity.device;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.communication.connector.SyncClientAWSMQTTConnector;
import com.smartism.znzk.domain.HeaterScheduleInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.view.CheckSwitchButton;
import com.smartism.znzk.view.SwitchButton.SwitchButton;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static cn.bingoogolapple.photopicker.util.BGAPhotoPickerUtil.dp2px;

public class HeaterScheduleActivity extends ActivityParentActivity {
    private ScheduleAdapter mAdapter;
    private List<HeaterScheduleInfo> scheduleList;
    private SwipeMenuListView listView;
    private String initMac;

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
                        setPage(state.getJSONObject("reported").getJSONArray("schedule"));
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
                        setPage(state.getJSONObject("reported").getJSONArray("schedule"));
                    }
                }catch (Exception ex){
                    ToastUtil.longMessage("update failed!");
                }
                cancelInProgress();
            }
        }
    };

    private void setPage(JSONArray schedules){
        try{
            if (schedules==null || schedules.size() == 0){
                addSchedule(null);
            }
            scheduleList.clear();
            scheduleList.addAll(JSONObject.parseArray(schedules.toJSONString(), HeaterScheduleInfo.class));
            mAdapter.notifyDataSetChanged();
        }catch (Exception ex){
            ToastUtil.longMessage("Data error!");
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heater_schedule);
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
        scheduleList = new ArrayList<>();
        listView = (SwipeMenuListView) findViewById(R.id.devices_list);
        mAdapter = new ScheduleAdapter();
        listView.setAdapter(mAdapter);
        listView.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext());
                // set item background
//                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
//                        0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(dp2px(90));
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete_item);
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

        listView.setMenuCreator(creator);

        listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        // delete
                        deleteSchedule(scheduleList.get(position).getId());
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
    }

    private void initData() {
        initMac = getIntent().getStringExtra("mac");
        showInProgress();
        SyncClientAWSMQTTConnector.getInstance().getDevicesStatus(initMac);
    }

    public void deleteSchedule(int id){
        cancelInProgress();
        for (int i = 0; i < scheduleList.size(); i++) {
            if (scheduleList.get(i).getId() == id){
                scheduleList.get(i).setDelete(1);
                break;
            }
        }
        JSONObject list = new JSONObject();
        list.put("schedule", JSONArray.toJSON(scheduleList));
        SyncClientAWSMQTTConnector.getInstance().setDevicesStatus(initMac,"",list);
    }

    public void back(View v) {
        finish();
    }

    class GroupInfoView {
        TextView on, off,repeat;
        SwitchButton switchButton;
    }

    /**
     * 设置设备logo图片和名称
     *
     * @param i
     */
    private void setShowInfo(GroupInfoView viewCache, int i) {
        if (scheduleList.get(i).getState() == 1){
            viewCache.switchButton.setCheckedNoEvent(true);
        }else{
            viewCache.switchButton.setCheckedNoEvent(false);
        }

        if (scheduleList.get(i).getCycle() == 127){
            viewCache.repeat.setText("Everyday");
        }else{
            viewCache.repeat.setText("");
            if (Util.intBitIsTrue(scheduleList.get(i).getCycle(),1)){
                viewCache.repeat.setText("Mon.");
            }
            if (Util.intBitIsTrue(scheduleList.get(i).getCycle(),2)){
                viewCache.repeat.setText(viewCache.repeat.getText()+"Tue.");
            }
            if (Util.intBitIsTrue(scheduleList.get(i).getCycle(),3)){
                viewCache.repeat.setText(viewCache.repeat.getText()+"Wed.");
            }
            if (Util.intBitIsTrue(scheduleList.get(i).getCycle(),4)){
                viewCache.repeat.setText(viewCache.repeat.getText()+"Thu.");
            }
            if (Util.intBitIsTrue(scheduleList.get(i).getCycle(),5)){
                viewCache.repeat.setText(viewCache.repeat.getText()+"Fri.");
            }
            if (Util.intBitIsTrue(scheduleList.get(i).getCycle(),6)){
                viewCache.repeat.setText(viewCache.repeat.getText()+"Sat.");
            }
            if (Util.intBitIsTrue(scheduleList.get(i).getCycle(),7)){
                viewCache.repeat.setText(viewCache.repeat.getText()+"Sun.");
            }
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(scheduleList.get(i).getTime()*1000);
        viewCache.on.setText("ON  "+new SimpleDateFormat("hh:mm a").format(calendar.getTime()));
        calendar.add(Calendar.SECOND,scheduleList.get(i).getDuration());
        viewCache.off.setText("OFF  "+new SimpleDateFormat("hh:mm a").format(calendar.getTime()));
    }

    /**
     * 初始化switch事件
     * @param viewCache
     * @param i
     */
    private void initEvent(GroupInfoView viewCache, int i) {
        viewCache.switchButton.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if (isChecked){
                scheduleList.get(i).setState(1);
            }else{
                scheduleList.get(i).setState(0);
            }
            updateScheduleListToDevice();
        });
    }

    private void updateScheduleListToDevice(){
        JSONObject listSchedule = new JSONObject();
        listSchedule.put("schedule",scheduleList);
        SyncClientAWSMQTTConnector.getInstance().setDevicesStatus(initMac,"",listSchedule);
    }

    class ScheduleAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return scheduleList != null ? scheduleList.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return scheduleList != null ? scheduleList.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GroupInfoView spImg = new GroupInfoView();

            if (convertView == null) {
                convertView = View.inflate(HeaterScheduleActivity.this, R.layout.activity_schedule_list_item, null);
                spImg.on = (TextView) convertView.findViewById(R.id.text_on);
                spImg.off = (TextView) convertView.findViewById(R.id.text_off);
                spImg.repeat = (TextView) convertView.findViewById(R.id.text_repeat_schedule);
                spImg.switchButton = (SwitchButton) convertView.findViewById(R.id.btn_switch_state);
                convertView.setTag(spImg);
            } else {
                spImg = (GroupInfoView) convertView.getTag();
            }
            setShowInfo(spImg, position);
            initEvent(spImg,position);
            return convertView;
        }
    }

    public void addSchedule(View v){
        Intent intent = new Intent();
        intent.setClass(mContext, AddHeaterScheduleActivity.class);
        intent.putExtra("mac", initMac);
        intent.putExtra("schedules", (Serializable) scheduleList);
        startActivity(intent);
    }
}