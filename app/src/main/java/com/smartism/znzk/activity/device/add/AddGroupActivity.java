package com.smartism.znzk.activity.device.add;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amazonaws.amplify.generated.graphql.CreateCtrGroupDevicesMutation;
import com.amazonaws.amplify.generated.graphql.CreateCtrUserDeviceShareMutation;
import com.amazonaws.amplify.generated.graphql.CreateCtrUserGroupMutation;
import com.amazonaws.amplify.generated.graphql.DeleteCtrUserDeviceRelationsMutation;
import com.amazonaws.amplify.generated.graphql.ListCtrUserDeviceRelationsQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.adapter.DeviceExpandableAdapter;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.awsClient.AWSClients;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
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
import type.CreateCtrUserDeviceShareInput;
import type.CreateCtrUserGroupInput;
import type.TableCtrUserDeviceRelationsFilterInput;
import type.TableStringFilterInput;

public class AddGroupActivity extends ActivityParentActivity {
    private final int dHandler_loadsuccess = 1;
    private EditText name;
    private GroupAdapter mAdapter;
    private List<ZhujiInfo> zhujiList;
    private ListView listView;
    private String initMac;

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
        setContentView(R.layout.activity_add_group);
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
        initMac = getIntent().getStringExtra("mac");
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
            Toast.makeText(AddGroupActivity.this, getText(R.string.activity_group_add_name_empty), Toast.LENGTH_SHORT).show();
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
                    type = ZhujiInfo.CtrDeviceType.INSECTICIDE_SINGLE_GROUP;
                }
            }
        }
        if (!isSelect) {
            cancelInProgress();
            Toast.makeText(AddGroupActivity.this, getText(R.string.activity_group_add_devict_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        CreateCtrUserGroupInput createCtrUserGroupInput = CreateCtrUserGroupInput.builder()
                .uid(AWSMobileClient.getInstance().getUsername())
                .name(gname)
                .type(type)
                .build();

        AWSClients.getInstance().getmAWSAppSyncClient().mutate(CreateCtrUserGroupMutation.builder().input(createCtrUserGroupInput).build())
                .enqueue(mutationGroupCallback);

    }

    private GraphQLCall.Callback<CreateCtrUserGroupMutation.Data> mutationGroupCallback = new GraphQLCall.Callback<CreateCtrUserGroupMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateCtrUserGroupMutation.Data> response) {
            if (!response.hasErrors()){
                if (response.data()!=null && response.data().createCtrUserGroup()!=null) {
                    String gid = response.data().createCtrUserGroup().id();

                    List<CreateCtrGroupDevicesInput> createCtrGroupDevicesInputList = new ArrayList<>(zhujiList.size());
                    for (ZhujiInfo zj : zhujiList) {
                        if (zj.isChecked()) {
                            createCtrGroupDevicesInputList.add(CreateCtrGroupDevicesInput.builder()
                                    .id(UUID.randomUUID().toString())
                                    .gid(gid)
                                    .mac(zj.getMac())
                                    .build());
                        }
                    }

                    AWSClients.getInstance().getmAWSAppSyncClient().mutate(CreateCtrGroupDevicesMutation.builder().input(createCtrGroupDevicesInputList).build())
                            .enqueue(mutationGroupDevicesCallback);
                }
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
        RelativeLayout itemLayout;
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
    private void initEvent(final GroupInfoView viewCache,final int i){
        viewCache.itemLayout.setOnClickListener((View v)-> {
            zhujiList.get(i).setChecked(!zhujiList.get(i).isChecked());
            mAdapter.notifyDataSetChanged();
        });
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
                convertView = View.inflate(AddGroupActivity.this, R.layout.activity_add_group_devices_list_item, null);
                spImg.itemLayout = (RelativeLayout) convertView.findViewById(R.id.device_item_layout);
                spImg.ioc = (ImageView) convertView.findViewById(R.id.device_logo);
                spImg.checked = (ImageView) convertView.findViewById(R.id.checked);
                spImg.name = (TextView) convertView.findViewById(R.id.device_name);
                spImg.type = (TextView) convertView.findViewById(R.id.device_type);
                convertView.setTag(spImg);
            } else {
                spImg = (GroupInfoView) convertView.getTag();
            }
            setShowInfo(spImg, position);
            initEvent(spImg,position);
            return convertView;
        }
    }
}