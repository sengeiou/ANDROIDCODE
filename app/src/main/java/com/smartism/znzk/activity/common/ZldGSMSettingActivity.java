package com.smartism.znzk.activity.common;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.DeviceSetGSMPhoneActivity;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.view.SwitchButton.SwitchButton;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

import java.util.*;

import static com.smartism.znzk.activity.alert.ChooseAudioSettingMode.SEND_RESULT_EXTRAS;
import static com.smartism.znzk.zhicheng.tasks.HttpAsyncTask.Zhuji_GSM_INIT_STATUS_FALG;
import static com.smartism.znzk.zhicheng.tasks.HttpAsyncTask.Zhuji_GSM_PHONE_LIST_FLAG;
import static com.smartism.znzk.zhicheng.tasks.HttpAsyncTask.Zhuji_GSM_SETTING_STATUE_FLAG;
import static com.smartism.znzk.zhicheng.tasks.HttpAsyncTask.Zhuji_SET_URL_FLAG;

/*
* 致利德GSM报警号码展示
* */
public class ZldGSMSettingActivity extends MZBaseActivity implements HttpAsyncTask.IHttpResultView {

    private  ListView mGsmListView ;
    private TextView mTipTv ;
    private SwitchButton mGsmStatusSwitch;//GSM是否开启状态开关
    private final int TYPE = 0 ; //0 GSM号码
    private final int MAX_NUMBER = 12;//支持的最大号码数
    private long mZhujiId = -1 ;//主机id
    private MyTelAdapter mAdapter ;
    private Map<Integer, DeviceSetGSMPhoneActivity.TelInfo> mTelInfos = new HashMap<>();//保存号码实体
    private ZhujiInfo mZhujiInfo ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            mZhujiId = getIntent().getLongExtra("zhuji_id",-1);
        }else{
            mZhujiId = savedInstanceState.getLong("zhuji_id");
        }
        //设置标题
        setTitle(getString(R.string.devices_list_menu_dialog_gsmsetting));
        initView();
    //    requestInitGsmStatu();
        new LoadZhujiAndDeviceTask().queryZhujiInfoByZhuji(mZhujiId, new LoadZhujiAndDeviceTask.ILoadResult<ZhujiInfo>() {
            @Override
            public void loadResult(ZhujiInfo result) {
                mZhujiInfo = result ;
                if(!mZhujiInfo.isAdmin()){
                    mGsmStatusSwitch.setEnabled(false);
                    mTipTv.setVisibility(View.VISIBLE);
                }else{
                    mTipTv.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestGetGSMList();
    }

    private void initView(){
        mTipTv = findViewById(R.id.tip_tv);
        mGsmListView = findViewById(R.id.gsm_list_view);
        mGsmStatusSwitch = findViewById(R.id.btn_sms_status);
        mAdapter = new MyTelAdapter(getResources().getStringArray(R.array.zld_gsmphone_type));
        mGsmListView.setAdapter(mAdapter);

        mGsmStatusSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                requestSettingGsmStatus(isChecked);
            }
        });

        mGsmListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mZhujiInfo!=null&&mZhujiInfo.isAdmin()){
                    Intent intent  = new Intent();
                    intent.setClass(getApplicationContext(),ZldGSMPhoneSettingActivity.class);
                    intent.putExtra("group",String.valueOf(position));
                    intent.putExtra("phone",mTelInfos.get(position)==null?"":mTelInfos.get(position).getTel());
                    intent.putExtra("zhuji_id",mZhujiId);
                    intent.putExtra("id",mTelInfos.get(position)==null?-1:mTelInfos.get(position).getId());
                    startActivity(intent);
                }

            }
        });

        //初始化mGsmStatusSwitch状态
        new LoadZhujiAndDeviceTask().queryAllCommandInfo(mZhujiId, new LoadZhujiAndDeviceTask.ILoadResult<List<CommandInfo>>() {
            @Override
            public void loadResult(List<CommandInfo> result) {
                if(result!=null){
                    for(CommandInfo commandInfo : result){
                        if("165".equals(commandInfo.getCtype())){
                            mGsmStatusSwitch.setCheckedImmediatelyNoEvent("0".equals(commandInfo.getCommand())?false:true);
                            break ;
                        }
                    }
                }
            }
        });
    }

    //初始化开关的gsm状态
    private void requestInitGsmStatu(){
        JSONObject object = new JSONObject();
        object.put("did",mZhujiId);
        object.put("type", TYPE);
        new HttpAsyncTask(this,Zhuji_GSM_INIT_STATUS_FALG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,object);
    }

    //设置是否启gsm状态
    private void requestSettingGsmStatus(boolean status){
        JSONObject pJsonObject = new JSONObject();
        pJsonObject.put("did", mZhujiId);
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("vkey", "165");
        object.put("value",status?"1":"0");
        array.add(object);
        pJsonObject.put("vkeys", array);
        new HttpAsyncTask(this, Zhuji_SET_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pJsonObject);
    }

    //获取gsm号码
    private  void requestGetGSMList(){
        JSONObject object = new JSONObject();
        object.put("did",mZhujiId);
        object.put("type", TYPE);
        new HttpAsyncTask(this,Zhuji_GSM_PHONE_LIST_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,object);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("zhuji_id",mZhujiId);
        super.onSaveInstanceState(outState);
    }

    //显示的布局
    @Override
    public int setLayoutId() {
        return R.layout.activity_zld_gsmsetting_layout;
    }

    //请求接口回调
    @Override
    public void setResult(int flag, String result) {
        if(flag==Zhuji_SET_URL_FLAG){
            //设置启用布撤防短信推送状态，结果返回
            boolean success = true ;
            if ("-3".equals(result)) {
                success = false ;
                ToastUtil.shortMessage(getString(R.string.net_error_nodata));
            } else if ("-5".equals(result)) {
                success = false ;
                ToastUtil.shortMessage(getString(R.string.device_not_getdata));
            } else if ("0".equals(result)) {
                ToastUtil.shortMessage(getString(R.string.success));
            }else if("-4".equals(result)){
                success = false ;
                ToastUtil.shortMessage(getString(R.string.activity_zhuji_not));
            }

            if(!success){
                //复原
                mGsmStatusSwitch.setCheckedImmediatelyNoEvent(!mGsmStatusSwitch.isChecked());
            }
        }else if(flag==Zhuji_GSM_PHONE_LIST_FLAG){
            //获取主机gsm号码返回结果
            JSONArray resultBack = null;
            mTelInfos.clear();
            if(!TextUtils.isEmpty(result)&&result.length()>4){
                try {
                    resultBack = JSON.parseArray(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(resultBack!=null){
                    for (int i = 0; i < resultBack.size(); i++) {
                        JSONObject obj = resultBack.getJSONObject(i);
                        DeviceSetGSMPhoneActivity.TelInfo telInfo = new DeviceSetGSMPhoneActivity.TelInfo();
                        telInfo.setId(obj.getLongValue("id"));
                        telInfo.setTel(obj.getString("tel"));
                        telInfo.setCountry(obj.getString("contry"));
                        telInfo.setTelId(obj.getIntValue("telId"));
                        mTelInfos.put(telInfo.getTelId(),telInfo);
                    }
                }
            }
            for(int i=0;i<MAX_NUMBER;i++){
                DeviceSetGSMPhoneActivity.TelInfo telInfo = mTelInfos.get(i);
                if(telInfo==null){
                    DeviceSetGSMPhoneActivity.TelInfo temp = new DeviceSetGSMPhoneActivity.TelInfo();
                    temp.setId(-1);
                    temp.setTel("");
                    mTelInfos.put(i,temp);
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void success(String message) {

    }

    private class MyTelAdapter extends BaseAdapter{

        String[] mGropNames;
        int mGroupIndex = 0 ;
        public MyTelAdapter(String[] groupNames){
            this.mGropNames = groupNames ;
        }
        @Override
        public int getCount() {
            return mTelInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return mTelInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder =null;
            if(convertView==null){
                View view  =  getLayoutInflater().inflate(R.layout.child_with_right_arrow_layout,parent,false);
                viewHolder = new ViewHolder();
                viewHolder.mGroupDisplayTv = view.findViewById(R.id.tv_call_mode);
                viewHolder.mPhoneDisplayTv = view.findViewById(R.id.display_tv);
                viewHolder.mDividerViewStub = view.findViewById(R.id.first_dividing_line);
                convertView = view;
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }
            if((position+1)!=mTelInfos.size()&&(position+1)%6==0){
                viewHolder.mDividerViewStub.setVisibility(View.VISIBLE);
                viewHolder.mDividerViewStub.setText(mGropNames[mGroupIndex]);
                mGroupIndex++;
                if(mGroupIndex==mGropNames.length){
                    mGroupIndex=0;
                }
            }else{
                viewHolder.mDividerViewStub.setVisibility(View.GONE);
            }
            viewHolder.mGroupDisplayTv.setText(getString(R.string.zld_gsm_group_name,position+1));
            //获取到就设置
            if(mTelInfos.get(position)!=null){
                viewHolder.mPhoneDisplayTv.setText(mTelInfos.get(position).getTel());
            }
            return convertView;
        }

        class ViewHolder{
            TextView mGroupDisplayTv ,mPhoneDisplayTv ;
            TextView mDividerViewStub;
        }
    }

}
