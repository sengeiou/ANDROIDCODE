package com.smartism.znzk.activity.common;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
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
import com.smartism.znzk.widget.NumberPickerView;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;
import com.smartism.znzk.zhicheng.tasks.LoadCommandsInfo;

import java.util.ArrayList;
import java.util.List;

/*
   致利德轮播方式设置
* */
public class ZhujiSettingCallModeActivity extends MZBaseActivity implements AdapterView.OnItemClickListener, LoadCommandsInfo.ILoadCommands, HttpAsyncTask.IHttpResultView {

    ListView modeDiaplayList ;
    ArrayAdapter mAdapter ;
    List<String> mData = new ArrayList<>();
    long zhujiId ;
    private NumberPickerView mNumberPickerView  ;
    private final int DISPLAY_COUNT = 9 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置标题
        setTitle(getResources().getString(R.string.zscm_call_mode_title));
        if(savedInstanceState==null){
            zhujiId = getIntent().getLongExtra("zhuji_id",0);
        }else{
            zhujiId = savedInstanceState.getLong("zhuji_id");
        }
        initData();
        mNumberPickerView = findViewById(R.id.call_phone_picker);
        modeDiaplayList = findViewById(R.id.modeDiaplayList);
        mAdapter = new ArrayAdapter(this,R.layout.custom_single_chioce_layout,mData);
        modeDiaplayList.setAdapter(mAdapter);
        modeDiaplayList.setOnItemClickListener(this);

        String[] displayValue = new String[DISPLAY_COUNT];
        for(int i=0;i<displayValue.length;i++){
            displayValue[i]=String.valueOf(i+1);
        }
        //必须设置NumberPickerView要显示的数组值
        mNumberPickerView.setDisplayedValues(displayValue);
        mNumberPickerView.setMinValue(0);
        mNumberPickerView.setMaxValue(DISPLAY_COUNT-1);
        new LoadCommandsInfo(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,zhujiId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("zhuji_id",zhujiId);
        super.onSaveInstanceState(outState);
    }

    private void initData(){
        String[] values = getResources().getStringArray(R.array.zscm_modes);
        for(int i=0;i<values.length;i++){
            mData.add(values[i]);
        }
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_zhuji_setting_call_mode_layout;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        lastPosition = currentPosition ; //保存前一个位置，防止设置失败
        currentPosition = position ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.zc_save_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.save_item_menu:
                if(currentPosition==-1){
                    return true;
                }
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", zhujiId);
                JSONArray array = new JSONArray();
                JSONObject object = new JSONObject();
                object.put("vkey", "156");
                String value = "";
                if(mData.get(currentPosition).equals(getResources().getString(R.string.zscm_call_only_number))){
                    value= value+"00";
                }else if(mData.get(currentPosition).equals(getResources().getString(R.string.zscm_call_all_number))){
                    value  =value +"01";
                }
                value =value+"0"+mNumberPickerView.getDisplayedValues()[mNumberPickerView.getValue()];
                object.put("value",Integer.parseInt(value,16));
                array.add(object);
                pJsonObject.put("vkeys", array);
                new HttpAsyncTask(ZhujiSettingCallModeActivity.this,HttpAsyncTask.Zhuji_SET_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pJsonObject);
                return true ;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    int currentPosition  = - 1;
    int lastPosition = - 1;
    @Override
    public void loadCommands(List<CommandInfo> lists) {
        String currentPositionStr = getResources().getString(R.string.zscm_call_all_number);
        String currentCallTimes = "3";
        if(lists!=null&&lists.size()>0){
            for(CommandInfo commandInfo :lists){
                if(commandInfo.getCtype().equals("156")){
                    //转换成16进制
                    String command = Integer.toHexString(Integer.parseInt(commandInfo.getCommand()));
                    while(command.length()<4){
                        command = "0"+command;
                    }
                    if(!TextUtils.isEmpty(command)){
                        String callMethod = command.substring(0,command.length()/2);
                        currentCallTimes = command.substring(command.length()/2,command.length());
                        if(callMethod.equals("00")){
                            currentPositionStr = getResources().getString(R.string.zscm_call_only_number);
                        }else if(callMethod.equals("01")){
                            currentPositionStr = getResources().getString(R.string.zscm_call_all_number);
                        }
                        break ;
                    }
                }
            }
        }
        //设置值
        int index = mData.indexOf(currentPositionStr);
        currentPosition = index ;
        modeDiaplayList.performItemClick(null,index,index);

        if((Integer.parseInt(currentCallTimes)-1)>mNumberPickerView.getMaxValue()){
            currentCallTimes ="3" ;
        }
        mNumberPickerView.setValue(Integer.parseInt(currentCallTimes)-1);
    }

    //Http结果处理
    @Override
    public void setResult(int flag, String result) {
        if(flag==HttpAsyncTask.Zhuji_SET_URL_FLAG){
            if ("-3".equals(result)) {
                modeDiaplayList.performItemClick(null,lastPosition,lastPosition);
                Toast.makeText(ZhujiSettingCallModeActivity.this, getString(R.string.net_error_nodata),
                        Toast.LENGTH_LONG).show();
            } else if ("-5".equals(result)) {
                modeDiaplayList.performItemClick(null,lastPosition,lastPosition);
                Toast.makeText(ZhujiSettingCallModeActivity.this, getString(R.string.device_not_getdata),
                        Toast.LENGTH_LONG).show();
            } else if ("0".equals(result)) {
                //设置成功，结束当前页面
                Toast.makeText(ZhujiSettingCallModeActivity.this, getString(R.string.success),
                        Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();
            }else if("-4".equals(result)){
                modeDiaplayList.performItemClick(null,lastPosition,lastPosition);
                ToastTools.short_Toast(this,getResources().getString(R.string.activity_zhuji_not));
            }
        }
    }

    @Override
    public void success(String message) {

    }
}
