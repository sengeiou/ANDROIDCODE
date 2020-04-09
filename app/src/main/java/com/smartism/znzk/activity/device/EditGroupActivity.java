package com.smartism.znzk.activity.device;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.amplify.generated.graphql.CreateCtrGroupDevicesMutation;
import com.amazonaws.amplify.generated.graphql.CreateCtrUserGroupMutation;
import com.amazonaws.amplify.generated.graphql.ListCtrGroupDevicesQuery;
import com.amazonaws.amplify.generated.graphql.ListCtrUserDeviceRelationsQuery;
import com.amazonaws.amplify.generated.graphql.UpdateCtrUserGroupMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.gson.JsonObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.awsClient.AWSClients;
import com.smartism.znzk.domain.HeaterGroupInfo;
import com.smartism.znzk.domain.HeaterShadowInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import type.CreateCtrGroupDevicesInput;
import type.CreateCtrUserGroupInput;
import type.TableCtrGroupDevicesFilterInput;
import type.TableCtrUserDeviceRelationsFilterInput;
import type.TableStringFilterInput;
import type.UpdateCtrUserGroupInput;

public class EditGroupActivity extends ActivityParentActivity {
    private final int dHandler_loadsuccess = 1,dHandler_loadsuccess_groupdevices = 2;
    private EditText name;
    private GroupAdapter mAdapter;
    private GroupAdapter groupAdapter;
    private List<ZhujiInfo> zhujiList;
    private ListView listView;
    private String initMac;

    private JSONObject groupInfo;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case dHandler_loadsuccess:
                    zhujiList.clear();
                    zhujiList.addAll((Collection<? extends ZhujiInfo>) msg.obj);
                    mAdapter.notifyDataSetChanged();
                    cancelInProgress();
                    break;

                case dHandler_loadsuccess_groupdevices:
                    String gDevices = String.valueOf(msg.obj);
                    JSONArray gDevicesArray = JSON.parseArray(gDevices);
                    for (ZhujiInfo zj : zhujiList) {
                        for (int i = 0; i < gDevicesArray.size(); i++) {
                            JSONObject p = gDevicesArray.getJSONObject(i);
                            if (zj.getMac().equals(p.getString(HeaterGroupInfo.mac))){
                                zj.setChecked(true);
                            }

                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    cancelInProgress();
                    break;

                default:
                    break;
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);
        initView();
        initData();
    }

    private void initView() {
        zhujiList = new ArrayList<>();
        name = (EditText) findViewById(R.id.group_name);
        listView = (ListView) findViewById(R.id.devices_list);
        mAdapter = new GroupAdapter();
        listView.setAdapter(mAdapter);
    }

    private void initData() {
        groupInfo = JSON.parseObject(getIntent().getStringExtra("groupInfo"));
        initDevicesList();
    }

    /**
     * 保存按钮点击
     *
     * @param v
     */
    public void save(View v) {
        showInProgress(getString(R.string.submiting), false, true);
        final String gname = name.getEditableText().toString();
        if (StringUtils.isEmpty(gname)) {
            cancelInProgress();
            Toast.makeText(this, getText(R.string.activity_group_add_name_empty), Toast.LENGTH_SHORT).show();
            return;
        }
        boolean isSelect = false;
        String type = null;
        for (ZhujiInfo d : zhujiList) {
            if (d.isChecked()) {
                isSelect = true;
                if (d.getType().startsWith(ZhujiInfo.CtrDeviceType.AIRCARE)){
                    type = ZhujiInfo.CtrDeviceType.AIRCARE_SINGLE_GROUP;
                }else{
                    type = ZhujiInfo.CtrDeviceType.AIRCARE_SINGLE_GROUP;
                }
            }
        }
        if (!isSelect) {
            cancelInProgress();
            Toast.makeText(this, getText(R.string.activity_group_add_devict_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateCtrUserGroupInput updateCtrUserGroupInput = UpdateCtrUserGroupInput.builder()
                .id(groupInfo.getString(HeaterGroupInfo.id))
                .uid(AWSMobileClient.getInstance().getUsername())
                .name(gname)
                .type(groupInfo.getString(HeaterGroupInfo.type))
                .build();

        AWSClients.getInstance().getmAWSAppSyncClient().mutate(UpdateCtrUserGroupMutation.builder().input(updateCtrUserGroupInput).build())
                .enqueue(mutationUpdateGroupCallback);

    }

    private GraphQLCall.Callback<UpdateCtrUserGroupMutation.Data> mutationUpdateGroupCallback = new GraphQLCall.Callback<UpdateCtrUserGroupMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<UpdateCtrUserGroupMutation.Data> response) {
            if (!response.hasErrors()){

                List<CreateCtrGroupDevicesInput> createCtrGroupDevicesInputList = new ArrayList<>(zhujiList.size());
                for (ZhujiInfo zj:zhujiList){
                    if (zj.isChecked()){
                        createCtrGroupDevicesInputList.add(CreateCtrGroupDevicesInput.builder()
                                .id(UUID.randomUUID().toString())
                                .mac(zj.getMac())
                                .build());
                    }
                }

                AWSClients.getInstance().getmAWSAppSyncClient().mutate(CreateCtrGroupDevicesMutation.builder().input(createCtrGroupDevicesInputList).build())
                        .enqueue(mutationGroupDevicesCallback);
            }else{
                runOnUiThread(()->{
                    cancelInProgress();
                    ToastUtil.longMessage("Save failed and try again");
                });
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error","GraphQL add group Exception", e);
            runOnUiThread(()->{
                cancelInProgress();
                ToastUtil.longMessage("Save failed and try again");
            });
        }
    };

    private GraphQLCall.Callback<CreateCtrGroupDevicesMutation.Data> mutationGroupDevicesCallback = new GraphQLCall.Callback<CreateCtrGroupDevicesMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateCtrGroupDevicesMutation.Data> response) {
            if (!response.hasErrors()){
                runOnUiThread(()->{
                    ToastUtil.longMessage("Save success");
                    finish();
                });
            }else{
                runOnUiThread(()->{
                    cancelInProgress();
                    ToastUtil.longMessage("Save failed and try again");
                });
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error","GraphQL add group Exception", e);
            runOnUiThread(()->{
                cancelInProgress();
                ToastUtil.longMessage("Save failed and try again");
            });
        }
    };


    public void back(View v) {
        finish();
    }


    public void initGroupDevices() {
        showInProgress();
        AWSClients.getInstance().getmAWSAppSyncClient().query(ListCtrGroupDevicesQuery.builder()
                .filter(TableCtrGroupDevicesFilterInput.builder()
                        .gid(TableStringFilterInput.builder().eq(HeaterGroupInfo.id).build())
                        .build())
                .build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryGroupDevicesCallback);
    }

    private GraphQLCall.Callback<ListCtrGroupDevicesQuery.Data> queryGroupDevicesCallback = new GraphQLCall.Callback<ListCtrGroupDevicesQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListCtrGroupDevicesQuery.Data> response) {
            Log.i("Results", response.data().listCtrGroupDevices().items().toString());
            if (!response.hasErrors()) {
                Message message = defHandler.obtainMessage(dHandler_loadsuccess_groupdevices);
                message.obj = response.data().listCtrGroupDevices().items().toString();
                defHandler.sendMessage(message);
            }else{
                runOnUiThread(() ->{
                    ToastUtil.longMessage("Load group devices failed");
                    finish();
                });
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
            runOnUiThread(() ->{
                ToastUtil.longMessage("Load group devices failed");
                finish();
            });
        }
    };



    public void initDevicesList() {
        showInProgress();
        AWSClients.getInstance().getmAWSAppSyncClient().query(ListCtrUserDeviceRelationsQuery.builder()
                .filter(TableCtrUserDeviceRelationsFilterInput.builder()
                        .uid(TableStringFilterInput.builder().eq(AWSMobileClient.getInstance().getUsername()).build())
                        .build())
                .build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(queryRelatioinsCallback);
    }

    private GraphQLCall.Callback<ListCtrUserDeviceRelationsQuery.Data> queryRelatioinsCallback = new GraphQLCall.Callback<ListCtrUserDeviceRelationsQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListCtrUserDeviceRelationsQuery.Data> response) {
            Log.i("Results", response.data().listCtrUserDeviceRelations().items().toString());
            List<ZhujiInfo> zhujiInfoList = new ArrayList<>();
            List<ListCtrUserDeviceRelationsQuery.Item> allZhujiInfos = response.data().listCtrUserDeviceRelations().items();
            if (!CollectionsUtils.isEmpty(allZhujiInfos)) {
                String tempType = "";
                for (ListCtrUserDeviceRelationsQuery.Item info : allZhujiInfos) {
                    ZhujiInfo zj = Util.itemToZhujiInfo(info);
                    if (zj.getMac().equalsIgnoreCase(initMac)){
                        zj.setChecked(true);
                        tempType = zj.getType();
                    }
                }
                for (int i = 0; i < allZhujiInfos.size(); i++) {
                    ListCtrUserDeviceRelationsQuery.Item info = allZhujiInfos.get(i);
                    ZhujiInfo zj = Util.itemToZhujiInfo(info);
                    if (zj.getMac().equalsIgnoreCase(initMac)){
                        zj.setChecked(true);
                    }
                    if (zj.getType().startsWith(tempType.substring(0,tempType.indexOf("_")))) {
                        zhujiInfoList.add(zj);
                    }
                }
            }
            Message message = defHandler.obtainMessage(dHandler_loadsuccess);
            message.obj = zhujiInfoList;
            defHandler.sendMessage(message);
            initGroupDevices();
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("ERROR", e.toString());
            runOnUiThread(() ->{
                ToastUtil.longMessage("Load devices failed");
                finish();
            });
        }
    };

    class GroupInfoView {
        ImageView ioc, checked;
        TextView name, where, type;
    }

    /**
     * 设置设备logo图片和名称
     *
     * @param i
     */
    private void setShowInfo(GroupInfoView viewCache, int i) {
        // 设置图片
        viewCache.ioc.setImageResource(zhujiList.get(i).getLogoResource());
        viewCache.name.setText(zhujiList.get(i).getName());
        if (zhujiList.get(i).isChecked()) {
            viewCache.checked.setBackgroundResource(R.drawable.zhzj_date_xuanzhong);
        } else {
            viewCache.checked.setBackgroundResource(R.drawable.zhzj_date_moren);
        }
    }

    class GroupAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return zhujiList != null ? zhujiList.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return zhujiList != null ? zhujiList.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GroupInfoView spImg = new GroupInfoView();

            if (convertView == null) {
                convertView = View.inflate(EditGroupActivity.this, R.layout.activity_add_group_devices_list_item, null);
                spImg.ioc = (ImageView) convertView.findViewById(R.id.device_logo);
                spImg.checked = (ImageView) convertView.findViewById(R.id.checked);
                spImg.name = (TextView) convertView.findViewById(R.id.device_name);
                spImg.type = (TextView) convertView.findViewById(R.id.device_type);
                convertView.setTag(spImg);
            } else {
                spImg = (GroupInfoView) convertView.getTag();
            }
            setShowInfo(spImg, position);
            return convertView;
        }
    }
}