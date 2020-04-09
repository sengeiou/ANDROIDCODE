package com.smartism.znzk.activity.device.add;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.DeviceMainFragment;
import com.smartism.znzk.activity.common.HongCaiSettingActivity;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.global.AppConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.util.camera.WifiUtils;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

import java.util.ArrayList;
import java.util.List;

import static com.smartism.znzk.zhicheng.tasks.HttpAsyncTask.Zhuji_WIRED_ADD_URL_FLAG;

//有线防区添加页面
public class AddWiredDeviceActivity extends MZBaseActivity implements AdapterView.OnItemClickListener,HttpAsyncTask.IHttpResultView {

    private EditText mNameEdit,mLocationEdit ;
    private ListView mListView ;
    private ArrayAdapter mAdapter ;


    private ZhujiInfo mZhuji ;
    private String mTypeId ;
    private List<String> mData = new ArrayList<>();
    private List<String> mDataSlaveId = new ArrayList<>();
    private String mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            mName = getIntent().getStringExtra("name");
            mZhuji = (ZhujiInfo) getIntent().getSerializableExtra("zhuji");
            mTypeId = getIntent().getStringExtra("type_id");
        }else{
            mZhuji = (ZhujiInfo) getIntent().getSerializableExtra("zhuji");
            mName = savedInstanceState.getString("name");
            savedInstanceState.getString("type_id");
        }
        setTitle(mName);
        initViews();
        queryDeviceInfos();
        initRegisterReceiver();
    }

    private void initData(){
        mDataSlaveId.clear();
        if (null == mZhuji.getSetInfos().get(ZhujiInfo.GNSetNameMenu.cableCount.value())
            || "4".equals(mZhuji.getSetInfos().get(ZhujiInfo.GNSetNameMenu.cableCount.value()))) {
            mDataSlaveId.add("F0000001");
            mDataSlaveId.add("F0000002");
            mDataSlaveId.add("F0000003");
            mDataSlaveId.add("F0000004");
        }else{
            try {
                int total = Integer.parseInt(mZhuji.getSetInfos().get(ZhujiInfo.GNSetNameMenu.cableCount.value()));
                for (int j = 0; j< total; j++){
                    mDataSlaveId.add(Integer.toHexString(0xF0000001 + j).toUpperCase());
                }
            }catch (Exception ex){
                Log.i(TAG, "initData: 错误了",ex);
            }
        }
        for(int i=0;i<mDataSlaveId.size();i++){
            mData.add(getString(R.string.adwa_fangqu_name,i+1));
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("name",mName);
        outState.putSerializable("zhuji",mZhuji);
        outState.putString("type_id",mTypeId);
        super.onSaveInstanceState(outState);
    }

    private BroadcastReceiver defaultReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (Actions.REFRESH_DEVICES_LIST.equals(intent.getAction())) { // 数据刷新完成广播
                queryDeviceInfos();
            }
        }
    };

    /**
     * 注册广播 onpuse 中 需要解注册，意思是当页面跳走了不再接受广播
     */
    private void initRegisterReceiver() {
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Actions.REFRESH_DEVICES_LIST);
        mContext.registerReceiver(defaultReceiver, receiverFilter);
    }

    @Override
    protected void onDestroy() {
        mContext.unregisterReceiver(defaultReceiver);
        super.onDestroy();
    }

    private void initViews(){
        mNameEdit = findViewById(R.id.device_name_tv);
        mLocationEdit = findViewById(R.id.device_location_tv);
        mListView = findViewById(R.id.list_view);

        mAdapter = new ArrayAdapter(this,R.layout.custom_single_chioce_layout,mData);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setEmptyView(findViewById(R.id.list_emppty_view));//没有可添加防区时的提示View

//        mNameEdit.setText(mName);
//        mNameEdit.setSelection(mName.length());
    }

    //查询主机下所有的设备
    private void queryDeviceInfos(){
        new LoadZhujiAndDeviceTask().queryDeviceInfosByZhuji(mZhuji.getId(), new LoadZhujiAndDeviceTask.ILoadResult<List<DeviceInfo>>() {
            @Override
            public void loadResult(List<DeviceInfo> result) {
                initData();
                if(result!=null&&result.size()>0){
                    for(int i=0;i< result.size();i++){
                        DeviceInfo temp = result.get(i);
                        int index  = mDataSlaveId.indexOf(temp.getSlaveId());
                        if(index!=-1){
                            mData.remove(index);
                            mData.add(index,"");//置"空"
                        }
                    }
                }
                //移除空字符元素
                for(int i=0;i<mData.size();i++){
                    if(mData.get(i).equals("")){
                        mData.remove(i);
                        mDataSlaveId.remove(i);
                        i--;
                    }
                }
                mListView.performItemClick(null,0,0);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_add_wired_device_layout;
    }

    //添加按钮点击事件
    public void saveEvent(View view) {
        if(mData.size()==0){
            return ;
        }
        JSONObject pJsonObject = new JSONObject();
        pJsonObject.put("did", mZhuji.getId());
        pJsonObject.put("typeid",mTypeId);
        pJsonObject.put("slaveid",mDataSlaveId.get(currentPostion));
        pJsonObject.put("name",mNameEdit.getText().toString());
        pJsonObject.put("where",mLocationEdit.getText().toString());

        new HttpAsyncTask(this,Zhuji_WIRED_ADD_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pJsonObject);
    }

    @Override
    public void setResult(int flag, String result) {
        if (flag == Zhuji_WIRED_ADD_URL_FLAG) {
            if ("0".equals(result)) {
                //设置成功，结束当前页面
                Toast.makeText(AddWiredDeviceActivity.this, getString(R.string.success),
                        Toast.LENGTH_LONG).show();
                SyncMessageContainer.getInstance().produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));//完成修改，发送服务器刷新指令
                //结束当前活动 有线防区添加，不跳转  暂时屏蔽
//                NavUtils.navigateUpFromSameTask(this);
            }else if("-3".equals(result)){
                Toast.makeText(AddWiredDeviceActivity.this, getString(R.string.zjnothave),
                        Toast.LENGTH_LONG).show();
            }else{
                ToastTools.short_Toast(getApplicationContext(),getString(R.string.operator_error));
            }
        }
    }

    int currentPostion = 0;
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(view==null){
            return ;
        }
        currentPostion = position ;
    }

    @Override
    public void error(String message) {

    }
}
