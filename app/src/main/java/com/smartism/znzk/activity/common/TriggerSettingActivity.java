package com.smartism.znzk.activity.common;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;
import com.smartism.znzk.zhicheng.tasks.LoadCommandsInfo;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

import java.util.ArrayList;
import java.util.List;

import static com.smartism.znzk.activity.alert.ChooseAudioSettingMode.SEND_RESULT_EXTRAS;

public class TriggerSettingActivity extends MZBaseActivity implements AdapterView.OnItemClickListener, LoadCommandsInfo.ILoadCommands, HttpAsyncTask.IHttpResultView {


    ListView modeDiaplayList ;
    ArrayAdapter mAdapter ;
    List<String> mData = new ArrayList<>();
    //传过来的设备ID，可以是主机ID也可以是设备ID
    long deviceId ;
    private int mCurrentFlag = -1 ; //标识是触发电平设置(0)还是触发模式设置(1),布防电平设置(2)
    private DeviceInfo mDeviceInfo ;
    private ZhujiInfo mZhuji ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            mCurrentFlag = getIntent().getIntExtra("flags",-1);
            deviceId = getIntent().getLongExtra("device_id",0);
        }else{
            mCurrentFlag = savedInstanceState.getInt("flags");
            deviceId = savedInstanceState.getLong("device_id");
        }

        //设置标题
        if(mCurrentFlag==0){
            setTitle(getResources().getString(R.string.triggers_title));
        }else if(mCurrentFlag==1){
            setTitle(getResources().getString(R.string.triggers_edge_title));
        }else if(mCurrentFlag==2){
            setTitle(getResources().getString(R.string.triggers_defen_title));
        }

        initData();
        modeDiaplayList = findViewById(R.id.modeDiaplayList);
        mAdapter = new ArrayAdapter(this,R.layout.custom_single_chioce_layout,mData);
        modeDiaplayList.setAdapter(mAdapter);
        modeDiaplayList.setOnItemClickListener(this);

        initDevicesInfo();
    }

    private void initDevicesInfo(){
        new LoadZhujiAndDeviceTask().queryDeviceInfoByDevice(deviceId, new LoadZhujiAndDeviceTask.ILoadResult<DeviceInfo>() {
            @Override
            public void loadResult(DeviceInfo result) {
                mDeviceInfo = result;
                initZhujiInfo();
            }
        });
    }

    private void initZhujiInfo(){
        new LoadZhujiAndDeviceTask().queryZhujiInfoByZhuji(mDeviceInfo!=null?mDeviceInfo.getZj_id():deviceId, new LoadZhujiAndDeviceTask.ILoadResult<ZhujiInfo>() {
            @Override
            public void loadResult(ZhujiInfo result) {
                mZhuji = result;
                if (mDeviceInfo == null){
                    mDeviceInfo = Util.getZhujiDevice(mZhuji);
                }
                initDeviceCommandList();
            }
        });
    }

    private void initDeviceCommandList(){
        new LoadZhujiAndDeviceTask().queryAllCommandInfo(deviceId, new LoadZhujiAndDeviceTask.ILoadResult<List<CommandInfo>>() {
            //主线程调用
            @Override
            public void loadResult(List<CommandInfo> result) {
                initValue(result);
            }
        });
    }

    private void initValue(List<CommandInfo> lists){
        String currentPositionStr = "";
        if(lists!=null&&lists.size()>0){
            for(CommandInfo commandInfo :lists){
                if(mCurrentFlag==0){
                    //触发电平
                    if(CommandInfo.CommandTypeEnum.dSetDpTypeChufa.value().equalsIgnoreCase(commandInfo.getCtype())){
                        String command = commandInfo.getCommand();
                        if(command.equals("00")){
                            currentPositionStr = getResources().getString(R.string.triggers_low_level);
                        }else if(command.equals("01")){
                            currentPositionStr = getResources().getString(R.string.triggers_high_lever);
                        }
                        break ;
                    }
                }else if(mCurrentFlag==1){
                    //触发模式
                    if(CommandInfo.CommandTypeEnum.dSetChufaSignal.value().equalsIgnoreCase(commandInfo.getCtype())){
                        String command = commandInfo.getCommand();
                        if(command.equals("0")){
                            currentPositionStr = getString(R.string.triggers_edge_edge);
                        }else if(command.equals("1")){
                            currentPositionStr = getString(R.string.triggers_edge_level);
                        }
                        break ;
                    }

                }else if(mCurrentFlag==2){
                    //布防电平设置
                    if(CommandInfo.CommandTypeEnum.dSetArmingDianPing.value().equalsIgnoreCase(commandInfo.getCtype())){
                        String command = commandInfo.getCommand();
                        if("1".equals(command)){
                            currentPositionStr = getResources().getString(R.string.triggers_high_level_defen);
                        }else if("0".equals(command)){
                            currentPositionStr = getResources().getString(R.string.triggers_low_level_defen);
                        }
                        break ;
                    }
                }

            }
        }
        int index = 0 ;
        //判断是否需要设置默认值
        if(mCurrentFlag==0) {
            //触发电平
            if(TextUtils.isEmpty(currentPositionStr)){
                if ("FF12".equals(mZhuji.getMasterid().substring(0,4))) {
                    if (mDeviceInfo.getSlaveId().contains("3")) {
                        //防区3的默认值
                        currentPositionStr = getResources().getString(R.string.triggers_low_level);
                    } else if (mDeviceInfo.getSlaveId().contains("1") || mDeviceInfo.getSlaveId().contains("2")) {
                        //防区1，2,默认值
                        currentPositionStr = getResources().getString(R.string.triggers_high_lever);
                    }
                }else{
                    currentPositionStr = getResources().getString(R.string.triggers_high_lever);
                }
            }
        }else if(mCurrentFlag==1){
            //触发模式
            currentPositionStr = TextUtils.isEmpty(currentPositionStr)?getString(R.string.triggers_edge_level):currentPositionStr;
        }else if(mCurrentFlag==2){
            //布防电平默认值
            currentPositionStr = TextUtils.isEmpty(currentPositionStr)?getString(R.string.triggers_high_level_defen):currentPositionStr;
        }

        index = mData.indexOf(currentPositionStr);
        currentPosition = index ;
        modeDiaplayList.performItemClick(null,index,index);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("device_id",deviceId);
        outState.putInt("flags",mCurrentFlag);
        super.onSaveInstanceState(outState);
    }

    private void initData(){
        String[] values = null ;
        if(mCurrentFlag==0){
            values = getResources().getStringArray(R.array.triggers_lists);
        }else if(mCurrentFlag==1){
            values = getResources().getStringArray(R.array.triggers_edge_lists);
        }else if(mCurrentFlag==2){
            values = getResources().getStringArray(R.array.triggers_defen_lists);
        }

        for(int i=0;i<values.length;i++){
            mData.add(values[i]);
        }
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_trigger_setting_layout;
    }

    int currentPosition  = -1 ;
    int lastPosition = - 1;
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(view==null||currentPosition==position){
            //如果当前已经选中，则不做处设置请求。
            return ;
        }
        lastPosition = currentPosition ; //保存前一个位置，防止设置失败
        currentPosition = position ;
        if(mCurrentFlag==0){
            levelRequest(position);
        }else if(mCurrentFlag==1){
            //触发模式请求
            edgeRequest(position);
        }else if(mCurrentFlag==2){
            defenRequest(position);
        }

    }

    private void defenRequest(int position){
        JSONObject pJsonObject = new JSONObject();
        pJsonObject.put("did", deviceId);
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("vkey", "162");
        if(mData.get(position).equals(getResources().getString(R.string.triggers_high_level_defen))){
            object.put("value","1");
        }else if(mData.get(position).equals(getResources().getString(R.string.triggers_low_level_defen))){
            object.put("value","0");
        }
        array.add(object);
        pJsonObject.put("vkeys", array);
        sendRequest(pJsonObject);
    }

    //触发电平请求
    private void levelRequest(int position){
        JSONObject pJsonObject = new JSONObject();
        pJsonObject.put("did", deviceId);
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("vkey", "155");
        if(mData.get(position).equals(getResources().getString(R.string.triggers_low_level))){
            object.put("value","00");
        }else if(mData.get(position).equals(getResources().getString(R.string.triggers_high_lever))){
            object.put("value","01");
        }
        array.add(object);
        pJsonObject.put("vkeys", array);
        sendRequest(pJsonObject);
    }

    //触发模式请求
    private void edgeRequest(int position){
        JSONObject pJsonObject = new JSONObject();
        pJsonObject.put("did", deviceId);
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("vkey", "161");
        if(mData.get(position).equals(getString(R.string.triggers_edge_edge))){
            object.put("value","0");
        }else if(mData.get(position).equals(getString(R.string.triggers_edge_level))){
            object.put("value","1");
        }
        array.add(object);
        pJsonObject.put("vkeys", array);
        sendRequest(pJsonObject);
    }

    private void sendRequest(JSONObject pJsonObject){
        new HttpAsyncTask(TriggerSettingActivity.this,HttpAsyncTask.Zhuji_SET_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pJsonObject);
    }

    @Override
    public void loadCommands(List<CommandInfo> lists) {
        String currentPositionStr = "";
        if(lists!=null&&lists.size()>0){
            for(CommandInfo commandInfo :lists){
                if(mCurrentFlag==0){
                    //触发电平
                    if(commandInfo.getCtype().equals("155")){
                        String command = commandInfo.getCommand();
                        if(command.equals("00")){
                            currentPositionStr = getResources().getString(R.string.triggers_low_level);
                        }else if(command.equals("01")){
                            currentPositionStr = getResources().getString(R.string.triggers_high_lever);
                        }
                        break ;
                    }
                }else if(mCurrentFlag==1){
                    //触发模式
                    if(commandInfo.getCtype().equals("161")){
                        String command = commandInfo.getCommand();
                        if(command.equals("0")){
                            currentPositionStr = getString(R.string.triggers_edge_edge);
                        }else if(command.equals("1")){
                            currentPositionStr = getString(R.string.triggers_edge_level);
                        }
                        break ;
                    }

                }

            }
        }
        int index = 0 ;
        //判断是否需要设置默认值
        if(mCurrentFlag==0) {
            //触发电平
            if(TextUtils.isEmpty(currentPositionStr)){
                //默认低电平触发
                currentPositionStr = getResources().getString(R.string.triggers_low_level);

            }
        }else if(mCurrentFlag==1){
            //触发模式
            currentPositionStr = TextUtils.isEmpty(currentPositionStr)?getString(R.string.triggers_edge_level):currentPositionStr;
        }

        index = mData.indexOf(currentPositionStr);
        currentPosition = index ;
        modeDiaplayList.performItemClick(null,index,index);
    }

    @Override
    public void setResult(int flag, String result) {
        if(flag==HttpAsyncTask.Zhuji_SET_URL_FLAG){
            if ("-3".equals(result)) {
                modeDiaplayList.performItemClick(null,lastPosition,lastPosition);
                Toast.makeText(TriggerSettingActivity.this, getString(R.string.net_error_nodata),
                        Toast.LENGTH_LONG).show();
            } else if ("-5".equals(result)) {
                modeDiaplayList.performItemClick(null,lastPosition,lastPosition);
                Toast.makeText(TriggerSettingActivity.this, getString(R.string.device_not_getdata),
                        Toast.LENGTH_LONG).show();
            } else if ("0".equals(result)) {
                Toast.makeText(TriggerSettingActivity.this, getString(R.string.success),
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra(SEND_RESULT_EXTRAS,mData.get(currentPosition));
                setResult(RESULT_OK,intent);
                finish();
            }else if("-4".equals(result)){
                modeDiaplayList.performItemClick(null,lastPosition,lastPosition);
                ToastTools.short_Toast(this,getResources().getString(R.string.activity_zhuji_not));
            }
        }
    }

    @Override
    public void success(String message) {}
}
