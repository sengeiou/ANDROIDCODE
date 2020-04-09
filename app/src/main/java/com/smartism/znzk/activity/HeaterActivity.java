package com.smartism.znzk.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.amplify.generated.graphql.ListCtrAromaLiquidsQuery;
import com.amazonaws.amplify.generated.graphql.ListCtrGroupDevicesQuery;
import com.amazonaws.amplify.generated.graphql.ListCtrUserGroupsQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.hjq.toast.ToastUtils;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.HeaterSignalDiagnosisActivity;
import com.smartism.znzk.activity.device.add.AddGroupActivity;
import com.smartism.znzk.activity.device.share.ShareDevicesActivity;
import com.smartism.znzk.awsClient.AWSClients;
import com.smartism.znzk.communication.connector.SyncClientAWSMQTTConnector;
import com.smartism.znzk.domain.HeaterShadowInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DateUtil;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.HeaterMenuPopupWindow;
import com.smartism.znzk.view.zbarscan.ScanCaptureActivity;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.Nonnull;

import type.TableCtrAromaLiquidFilterInput;
import type.TableCtrGroupDevicesFilterInput;
import type.TableCtrUserGroupFilterInput;
import type.TableStringFilterInput;


/**
 * Created by wangjian on 2020年03月05日.
 * 熏香机设备信息
 */

public class HeaterActivity extends FragmentParentActivity implements View.OnClickListener {
    private static final int WHAT_TIME_OUT = 1,REQUEST_CODE_EDIT_FLUID = 666;

    public HeaterMenuPopupWindow menuWindow; // 右上角菜单

    private HeaterWickFragment wickFragment1,wickFragment2;
    private TabLayout tabLayout;
    private FragmentManager fragmentManager;
    private TextView title;
    private String[] titles;

    /**
     * 保存熏香机的信息
     */
    private JSONObject heaterInfo;
    /**
     * 是否是分组
     */
    private boolean isGroup = false;
    /**
     * 分组下的设备信息
     */
    private List<ListCtrGroupDevicesQuery.Item> groupDevices;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }
    };
    private Handler handler = new WeakRefHandler(mCallback);

    public Handler getHandler() {
        return handler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heater);
        if (heaterInfo == null){
            heaterInfo = new JSONObject();
            heaterInfo.put(HeaterShadowInfo.mac,getIntent().getStringExtra(HeaterShadowInfo.mac));
            heaterInfo.put(HeaterShadowInfo.name,getIntent().getStringExtra(HeaterShadowInfo.name));
            heaterInfo.put(HeaterShadowInfo.relationId,getIntent().getStringExtra(HeaterShadowInfo.relationId));
            isGroup = getIntent().getBooleanExtra("isGroup",false);
        }
        initView();
        initEvent();
        initRegisterReceiver();
        initData();
    }

    /**
     * 显示选中Fragment
     *
     * @param tag Fragment别名
     */
    private void selectFragment(int tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        switch (tag) {
            case 0:
                transaction.show(wickFragment1);
                transaction.hide(wickFragment2);
                break;
            case 1:
                transaction.hide(wickFragment1);
                transaction.show(wickFragment2);
                break;

        }
        transaction.commitAllowingStateLoss();
    }

    /**
     * 设置默认tab样式
     *
     * @param tab
     */

    private void setTab(TabLayout.Tab tab) {
        View view = tab.getCustomView();
        TextView tvTabTitle = (TextView) view.findViewById(R.id.tvTabTitle);
        tvTabTitle.setTextColor(getResources().getColor(R.color.main_color));
    }

    /**
     * 设置未选中Tab样式
     *
     * @param tab 目标Tab
     */
    private void unselectTab(TabLayout.Tab tab) {
        View view = tab.getCustomView();
        TextView tvTabTitle = (TextView) view.findViewById(R.id.tvTabTitle);
        tvTabTitle.setTextColor(getResources().getColor(R.color.black));
    }

    private void initTab() {
        //初始化第一个tab tag不能为空
        TabLayout.Tab tab = tabLayout.newTab().setTag(0).setCustomView(getTabView(0));
        tabLayout.addTab(tab);
        setTab(tab);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (wickFragment1 == null){
            wickFragment1 = new HeaterWickFragment();
            Bundle bundle = new Bundle();
            bundle.putString(HeaterShadowInfo.wickId,"1");
            bundle.putString(HeaterShadowInfo.mac,heaterInfo.getString(HeaterShadowInfo.mac));
            wickFragment1.setArguments(bundle);
            transaction.add(R.id.frame, wickFragment1);
        }
        //初始化第二个tab
        tab = tabLayout.newTab().setTag(1).setCustomView(getTabView(1));
        tabLayout.addTab(tab);
        if (wickFragment2 == null) {
            wickFragment2 = new HeaterWickFragment();
            Bundle bundle = new Bundle();
            bundle.putString(HeaterShadowInfo.wickId,"2");
            bundle.putString(HeaterShadowInfo.mac,heaterInfo.getString(HeaterShadowInfo.mac));
            wickFragment2.setArguments(bundle);
            transaction.add(R.id.frame, wickFragment2);
            tabLayout.setVisibility(View.VISIBLE);
        }
        transaction.commitAllowingStateLoss();
        selectFragment(0);
        if (isGroup){
            tabLayout.setVisibility(View.GONE);
        }
    }

    private View getTabView(int index) {
        View view = View.inflate(HeaterActivity.this, R.layout.layout_tab_wick, null);
        TextView tv = (TextView) view.findViewById(R.id.tvTabTitle);
        tv.setText(titles[index]);
        return view;
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

    private void initEvent() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setTab(tab);
                selectFragment((int) tab.getTag());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                unselectTab(tab);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        if (!isGroup) {
            SyncClientAWSMQTTConnector.getInstance().registerDevicesShadowTopic(heaterInfo.getString(HeaterShadowInfo.mac));
        }
    }

    private void initData() {
        if (!isGroup) {
            refreshData();
        }else{
            refreshGroupData();
        }
    }

    private void initView() {
        menuWindow = new HeaterMenuPopupWindow(this, this);
        tabLayout = (TabLayout) findViewById(R.id.tab);
        title = (TextView) findViewById(R.id.title);
        title.setText(heaterInfo.getString("name"));
        fragmentManager = getSupportFragmentManager();
        titles = new String[]{"LEFT","RIGHT"};
        initTab();
    }

    private android.content.BroadcastReceiver receiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Actions.CONNECTION_FAILED.equals(intent.getAction())) {
                ToastUtil.longMessage("You are offline");
            } else if (Actions.CONNECTION_ING.equals(intent.getAction())) {
                ToastUtil.longMessage("connecting");
            } else if (Actions.MQTT_GET_ACCEPTED.equals(intent.getAction())) { //获取到设备信息
                try{
                    /**
                     * {
                     *   "state": {
                     *     "desired": {
                     *       "color": "yello",
                     *       "power": "on"
                     *     },
                     *     "reported": {
                     *       "color": "yello",
                     *       "power": "off",
                     *       "id": "2",
                     *       "connected": true
                     *     },
                     *     "delta": {
                     *       "power": "on"
                     *     }
                     *   },
                     *   "metadata": {
                     *     "desired": {
                     *       "color": {
                     *         "timestamp": 1583309531
                     *       },
                     *       "power": {
                     *         "timestamp": 1583309531
                     *       }
                     *     },
                     *     "reported": {
                     *       "color": {
                     *         "timestamp": 1583310441
                     *       },
                     *       "power": {
                     *         "timestamp": 1583310441
                     *       },
                     *       "id": {
                     *         "timestamp": 1583310441
                     *       },
                     *       "connected": {
                     *         "timestamp": 1583310441
                     *       }
                     *     }
                     *   },
                     *   "version": 9,
                     *   "timestamp": 1583400138
                     * }
                     */
                    cancelInProgress();
                    JSONObject param = JSONObject.parseObject(intent.getStringExtra(Actions.MQTT_GET_ACCEPTED_DATA_JSON));
                    JSONObject state = param.getJSONObject("state");
                    if (state.containsKey("reported")) {
                        JSONObject reported = state.getJSONObject("reported");
                        String macInfo = intent.getStringExtra(Actions.MQTT_TOPIC_THINGNAME);
                        refreshFragmentPage(reported, macInfo);
                        checkAndSyncTimeZone(reported, macInfo);
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
                    if (state.containsKey("reported")) {
                        JSONObject reported =  state.getJSONObject("reported");
                        String macInfo = intent.getStringExtra(Actions.MQTT_TOPIC_THINGNAME);
                        refreshFragmentPage(reported, macInfo);
                    }
                }catch (Exception ex){
                    ToastUtil.longMessage("Init failed!");
                    finish();
                }
            }
        }
    };

    private void checkAndSyncTimeZone(JSONObject reported,String macInfo){
        if (!DateUtil.getCurrentTimeZone().equalsIgnoreCase(reported.getString("timeZone"))){
            //更新时区
            JSONObject status = new JSONObject();
            status.put(HeaterShadowInfo.setTimeZone,DateUtil.getCurrentTimeZone());
            SyncClientAWSMQTTConnector.getInstance().setDevicesStatus(macInfo,"",status);
        }
    }
    private void refreshFragmentPage(JSONObject reported,String macInfo){
        if (!StringUtils.isEmpty(macInfo) && macInfo.equalsIgnoreCase(heaterInfo.getString(HeaterShadowInfo.mac))) {
            heaterInfo.putAll(reported);

            if (reported.containsKey("heaterStrip")){
                JSONArray heaterStrip = reported.getJSONArray("heaterStrip");
                if (heaterStrip!=null && heaterStrip.size() > 0){
                    for (int i = 0; i < heaterStrip.size(); i++) {
                        if (heaterStrip.getJSONObject(i).getIntValue(HeaterShadowInfo.wickId) == 1){
                            reported.putAll(heaterStrip.getJSONObject(i));
                            if (wickFragment1 != null) {
                                wickFragment1.refreshPage(reported);
                            }
                        }
                        if (heaterStrip.getJSONObject(i).getIntValue(HeaterShadowInfo.wickId) == 2){
                            reported.putAll(heaterStrip.getJSONObject(i));
                            if (wickFragment2 != null) {
                                wickFragment2.refreshPage(reported);
                            }
                        }
                    }
                }
                if (heaterStrip!=null && heaterStrip.size() == 1){
                    tabLayout.setVisibility(View.GONE);
                }
            }else{
                //共有属性的刷新，例如环境温度
                if (wickFragment1 != null) {
                    wickFragment1.refreshPage(reported);
                }
                if (wickFragment2 != null) {
                    wickFragment2.refreshPage(reported);
                }
            }
        }
    }

    private void refreshData() {
        showInProgress(getString(R.string.ongoing));
        SyncClientAWSMQTTConnector.getInstance().getDevicesStatus(heaterInfo.getString(HeaterShadowInfo.mac));
    }

    private void refreshGroupData(){
        showInProgress(getString(R.string.ongoing));
        AWSClients.getInstance().getmAWSAppSyncClient().query(ListCtrGroupDevicesQuery.builder()
                .filter(TableCtrGroupDevicesFilterInput.builder()
                        .gid(TableStringFilterInput.builder().eq(heaterInfo.getString(HeaterShadowInfo.relationId)).build())
                        .build())
                .build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryGroupDeviceCallback);
    }

    private GraphQLCall.Callback<ListCtrGroupDevicesQuery.Data> queryGroupDeviceCallback = new GraphQLCall.Callback<ListCtrGroupDevicesQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListCtrGroupDevicesQuery.Data> response) {
            if (response.data()!=null && response.data().listCtrGroupDevices()!=null && response.data().listCtrGroupDevices().items() != null){
                groupDevices = response.data().listCtrGroupDevices().items();
                runOnUiThread(()->{
                    JSONObject attr = new JSONObject();
                    attr.put("isGroup",true);
                    JSONArray array = new JSONArray();
                    for (ListCtrGroupDevicesQuery.Item item : groupDevices){
                        array.add(item.mac());
                    }
                    attr.put("groupDevices",array);
                    if (wickFragment1 != null) {
                        wickFragment1.refreshPage(attr);
                    }
                    if (wickFragment2 != null) {
                        wickFragment2.refreshPage(attr);
                    }
                    cancelInProgress();
                });
            }else{
                runOnUiThread(()->{
                    ToastUtils.show("Init failed");
                    finish();
                });
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
        }
    };

    /**
     * 返回按钮
     * @param v
     */
    public void back(View v) {
        finish();
    }

    /**
     * 显示菜单
     * @param v
     */
    public void showMenu(View v) {
        menuWindow.showAtLocation(v, Gravity.TOP | Gravity.RIGHT, 0,
                Util.dip2px(getApplicationContext(), 55) + Util.getStatusBarHeight(this));
    }



    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            //菜单按钮 - 分享
            case R.id.tv_share:
                menuWindow.dismiss();
                intent.setClass(getApplicationContext(), ShareDevicesActivity.class);
                intent.putExtra("sharekey",heaterInfo.getString(HeaterShadowInfo.mac));
                startActivity(intent);
                break;
            //菜单按钮 - 编辑熏香液
            case R.id.tv_edit_fluid:
                menuWindow.dismiss();
                intent.setClass(mContext, ScanCaptureActivity.class);
                startActivityForResult(intent,REQUEST_CODE_EDIT_FLUID);
                break;
            //菜单按钮 - 创建分组
            case R.id.tv_create_group:
                menuWindow.dismiss();
                intent.setClass(mContext, AddGroupActivity.class);
                intent.putExtra("mac",heaterInfo.getString(HeaterShadowInfo.mac));
                startActivity(intent);
                break;
            //菜单按钮 - wifi诊断
            case R.id.tv_wifi_diagnosis:
                menuWindow.dismiss();
                intent.setClass(mContext, HeaterSignalDiagnosisActivity.class);
                intent.putExtra("mac",heaterInfo.getString(HeaterShadowInfo.mac));
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //返回请求编辑熏香液的扫码结果
        if (requestCode == REQUEST_CODE_EDIT_FLUID && resultCode == DataCenterSharedPreferences.Constant.CAPUTRE_ADDRESULT){
            //判断是不是熏香液
            if ("ctrfluid".equalsIgnoreCase(data.getStringExtra("pattern"))){
                AWSClients.getInstance().getmAWSAppSyncClient().query(ListCtrAromaLiquidsQuery.builder()
                        .filter(TableCtrAromaLiquidFilterInput.builder()
                                .type(TableStringFilterInput.builder().eq(data.getStringExtra("value")).build())
                                .build())
                        .build())
                        .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                        .enqueue(queryRelatioinsCallback);
            }else{
                ToastUtil.longMessage("Please scan the QR code on the aromatherapy liquid");
            }
        }
    }

    private GraphQLCall.Callback<ListCtrAromaLiquidsQuery.Data> queryRelatioinsCallback = new GraphQLCall.Callback<ListCtrAromaLiquidsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListCtrAromaLiquidsQuery.Data> response) {
            if (!response.hasErrors()){
                if (response.data()!=null && response.data().listCtrAromaLiquids() != null && !CollectionsUtils.isEmpty(response.data().listCtrAromaLiquids().items())){
                    Log.i("Results", response.data().listCtrAromaLiquids().items().toString());
                    String wickId = "";
                    if (!wickFragment1.isHidden()){
                        wickId = "1";
                    }else{
                        wickId = "2";
                    }
                    ListCtrAromaLiquidsQuery.Item item = response.data().listCtrAromaLiquids().items().get(0);
                    JSONObject status = new JSONObject();
                    status.put(HeaterShadowInfo.wickTempSet,item.temperature());
                    SyncClientAWSMQTTConnector.getInstance().setDevicesStatus(heaterInfo.getString(HeaterShadowInfo.mac),wickId,status);
                }else{
                    runOnUiThread(() -> {
                        ToastUtil.longMessage("Unknown type and add failed!");
                    });
                }
            }else{
                runOnUiThread(() -> {
                    ToastUtil.longMessage("Please try again!");
                });
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
            runOnUiThread(() -> {
                ToastUtil.longMessage("Please try again!");
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * 返回设备是否已连接,组时不用判断是否在线。
     * @return
     */
    public boolean isConnected(){
        return HeaterShadowInfo.connected.equalsIgnoreCase(heaterInfo.getString(HeaterShadowInfo.connected)) || isGroup;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public HeaterWickFragment getWickFragment1() {
        return wickFragment1;
    }

    public HeaterWickFragment getWickFragment2() {
        return wickFragment2;
    }
}
