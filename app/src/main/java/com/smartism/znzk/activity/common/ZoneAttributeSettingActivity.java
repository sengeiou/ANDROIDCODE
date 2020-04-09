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

//佳玉防区属性设置
public class ZoneAttributeSettingActivity extends MZBaseActivity  implements HttpAsyncTask.IHttpResultView {


    private ListView mListView ;
    private ArrayAdapter mAdapter ;
    private List<String> mData ;
    private int mCurrentPosition  = -1 ;
    private int mLastPosition = - 1;
    private long mDeviceId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.jiayu_zone_attribute_title));
        if(savedInstanceState==null){
            mDeviceId = getIntent().getLongExtra("device_id",-1);
        }else{
            mDeviceId = savedInstanceState.getLong("device_id");
        }
        bindView();
        bindEvent();
        bindData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("device_id",mDeviceId);
        super.onSaveInstanceState(outState);
    }

    private void bindData(){
        mData = new ArrayList<>();
        String[] temp  = getResources().getStringArray(R.array.jiayu_zone_setting_array);
        for(int i=0;i<temp.length;i++){
            mData.add(temp[i]);
        }
        mAdapter = new ArrayAdapter(this,R.layout.custom_single_chioce_layout,mData);
        mListView.setAdapter(mAdapter);

        //获取
        new LoadZhujiAndDeviceTask().queryAllCommandInfo(mDeviceId, new LoadZhujiAndDeviceTask.ILoadResult<List<CommandInfo>>() {
            @Override
            public void loadResult(List<CommandInfo> result) {
                int postiion  = 4;
                if(result!=null&&result.size()>0){
                    for(CommandInfo commandInfo:result){
                        if(commandInfo.getCtype().equals("138")){
                            try {
                                int command = Integer.parseInt(commandInfo.getCommand());
                                postiion = command ;
                            }catch (NumberFormatException e){
                                postiion = 4;
                            }
                            break ;
                        }
                    }
                }
                mCurrentPosition = postiion-1;
                mListView.performItemClick(null,postiion-1,postiion-1);
            }
        });
    }

    private void bindView(){
        mListView = findViewById(R.id.list_view);
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
        object.put("vkey", "138");
        object.put("value",String.valueOf((position+1)));
        array.add(object);
        pJsonObject.put("vkeys", array);
        //请求数据
        new HttpAsyncTask(ZoneAttributeSettingActivity.this,HttpAsyncTask.Zhuji_SET_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pJsonObject);
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_zone_attribute_setting_layout;
    }

    @Override
    public void setResult(int flag, String result) {
        if(flag==HttpAsyncTask.Zhuji_SET_URL_FLAG){
            if ("-3".equals(result)) {
                mListView.performItemClick(null,mLastPosition,mLastPosition);
                Toast.makeText(ZoneAttributeSettingActivity.this, getString(R.string.net_error_nodata),
                        Toast.LENGTH_LONG).show();
            } else if ("-5".equals(result)) {
                mListView.performItemClick(null,mLastPosition,mLastPosition);
                Toast.makeText(ZoneAttributeSettingActivity.this, getString(R.string.device_not_getdata),
                        Toast.LENGTH_LONG).show();
            } else if ("0".equals(result)) {
                Toast.makeText(ZoneAttributeSettingActivity.this, getString(R.string.success),
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
}
