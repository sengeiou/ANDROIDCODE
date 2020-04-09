package com.smartism.znzk.activity.common;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

import java.util.ArrayList;
import java.util.List;

import static com.smartism.znzk.activity.alert.ChooseAudioSettingMode.SEND_RESULT_EXTRAS;

public class ArmyDelayTimeSettingActivity extends MZBaseActivity implements HttpAsyncTask.IHttpResultView{

    private ListView mListView ;
    private ArrayAdapter mAdapter ;
    private List<String> mData ;
    private int mCurrentPosition  = -1 ;
    private int mLastPosition = - 1;
    private long mDeviceId;
    private int mFlags = -1 ; //0表示布防延迟时长 1表示报警延迟时长
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState==null){
            mDeviceId = getIntent().getLongExtra("device_id",-1);
            mFlags = getIntent().getIntExtra("flags",-1);
        }else{
            mDeviceId = savedInstanceState.getLong("device_id");
            mFlags = savedInstanceState.getInt("flags",-1);
        }
        if(mFlags==0){
            setTitle(getResources().getString(R.string.jiayu_adsa_title));
        }else if(mFlags==1){
            setTitle(getResources().getString(R.string.jiayu_adsa_alarm_title));
        }

        bindView();
        bindEvent();
        bindData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("device_id",mDeviceId);
        outState.putInt("flags",mFlags);
        super.onSaveInstanceState(outState);
    }

    private void bindData(){
        mData = new ArrayList<>();
        String[] temp  = getResources().getStringArray(R.array.jiayu_adsa_array);
        for(int i=0;i<temp.length;i++){
            mData.add(temp[i]);
        }
        mAdapter = new ArrayAdapter(this,R.layout.custom_single_chioce_layout,mData);
        mListView.setAdapter(mAdapter);

        //获取
        new LoadZhujiAndDeviceTask().queryAllCommandInfo(mDeviceId, new LoadZhujiAndDeviceTask.ILoadResult<List<CommandInfo>>() {
            @Override
            public void loadResult(List<CommandInfo> result) {
                int postiion  = 0;
                if(result!=null&&result.size()>0){
                    for(CommandInfo commandInfo:result){
                        if(mFlags==0){
                            if(commandInfo.getCtype().equals("142")){
                                try {
                                    int command = Integer.parseInt(commandInfo.getCommand());
                                    postiion = getPositionByCommand(command);
                                }catch (NumberFormatException e){
                                    postiion = 0;
                                }
                                break ;
                            }
                        }else if(mFlags==1){
                            if(commandInfo.getCtype().equals("141")){
                                try {
                                    int command = Integer.parseInt(commandInfo.getCommand());
                                    postiion = getPositionByCommand(command);
                                }catch (NumberFormatException e){
                                    postiion = 0;
                                }
                                break ;
                            }
                        }

                    }
                }
                mCurrentPosition = postiion;
                mListView.performItemClick(null,postiion,postiion);
            }
        });
    }

    private void bindView(){
        mListView = findViewById(R.id.list_view);
    }

    public static int getPositionByCommand(int command){
        switch (command){
            case 0:
                return 0;
            case 5:
                return 1;
            case 10:
                return 2;
            case 15:
                return 3;
            case 20:
                return 4;
            case 30:
                return 5;
            case 45:
                return 6;
            case 60:
                return 7;
            case 120:
                return 8;
            case 180:
                return 9;
            case 300:
                return 10;
            case 480:
                return 11;
            case 600:
                return 12 ;
        }

        return 0 ;
    }

    public static int getTimeByPosition(int position){
        switch (position){
            case 0:
                return 0;
            case 1:
                return 5;
            case 2:
                return 10;
            case 3:
                return 15;
            case 4:
                return 20;
            case 5:
                return 30;
            case 6:
                return 45;
            case 7:
                return 60;
            case 8:
                return 120;
            case 9:
                return 180;
            case 10:
                return 300;
            case 11:
                return 480;
            case 12:
                return 600 ;
        }
        return 0 ;
    }

    private void bindEvent(){
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(view==null||mCurrentPosition==position){
                    //如果当前已经选中，则不做处设置请求。
                    return ;
                }
                mLastPosition = mCurrentPosition ; //保存前一个位置，防止设置失败
                mCurrentPosition = position ;
                requestData(position);
            }
        });
    }

    private void requestData(int position){
        JSONObject pJsonObject = new JSONObject();
        pJsonObject.put("did", mDeviceId);
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        if(mFlags==0){
            object.put("vkey", "142");
        }else if(mFlags==1){
            object.put("vkey", "141");
        }

        object.put("value",getTimeByPosition(position));
        array.add(object);
        pJsonObject.put("vkeys", array);
        //请求数据
        new HttpAsyncTask(ArmyDelayTimeSettingActivity.this,HttpAsyncTask.Zhuji_SET_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pJsonObject);
    }


    @Override
    public void setResult(int flag, String result) {
        if(flag==HttpAsyncTask.Zhuji_SET_URL_FLAG){
            if ("-3".equals(result)) {
                mListView.performItemClick(null,mLastPosition,mLastPosition);
                Toast.makeText(ArmyDelayTimeSettingActivity.this, getString(R.string.net_error_nodata),
                        Toast.LENGTH_LONG).show();
            } else if ("-5".equals(result)) {
                mListView.performItemClick(null,mLastPosition,mLastPosition);
                Toast.makeText(ArmyDelayTimeSettingActivity.this, getString(R.string.device_not_getdata),
                        Toast.LENGTH_LONG).show();
            } else if ("0".equals(result)) {
                Toast.makeText(ArmyDelayTimeSettingActivity.this, getString(R.string.success),
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra(SEND_RESULT_EXTRAS,mData.get(mCurrentPosition));
                setResult(RESULT_OK,intent);
                finish();
            }else if("-4".equals(result)){
                mListView.performItemClick(null,mLastPosition,mLastPosition);
                ToastTools.short_Toast(this,getResources().getString(R.string.activity_zhuji_not));
            }
        }
    }

    @Override
    public void success(String message) {

    }

    @Override
    public void error(String message) {

    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_army_delay_time_setting_layout;
    }
}
