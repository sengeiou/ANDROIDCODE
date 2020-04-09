package com.smartism.znzk.activity.device;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.widget.NumberPickerView;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

import java.util.ArrayList;
import java.util.List;

public class NumberPickerSettingActivity extends MZBaseActivity implements HttpAsyncTask.IHttpResultView {


    private NumberPickerView mNumberPickerView ;
    private int mResId ;
    private String[] mData ;
    private List<String> mDataList = new ArrayList<>();
    private String mTitle = "" ;
    private long mDeviceId ;
    private int mCt ;
    private int defaultPosition = -1 ; //默认显示的位置
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            mResId = getIntent().getIntExtra("data",-1);
            mTitle = getIntent().getStringExtra("title");
            mDeviceId  = getIntent().getLongExtra("device_id",-1);
            mCt = getIntent().getIntExtra("ct",-1);
            defaultPosition = getIntent().getIntExtra("position",-1);
        }else{
            mResId = savedInstanceState.getInt("data");
            mTitle = savedInstanceState.getString("title");
            mDeviceId = savedInstanceState.getLong("device_id");
            mCt = savedInstanceState.getInt("ct");
            defaultPosition = savedInstanceState.getInt("position",-1);
        }
        setTitle(mTitle);
        bindView();
        bindEvent();
        bindData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("data",mResId);
        outState.putString("title",mTitle);
        outState.putLong("device_id",mDeviceId);
        outState.putInt("ct",mCt);
        outState.putInt("position",defaultPosition);
        super.onSaveInstanceState(outState);
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
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", mDeviceId);
                JSONArray array = new JSONArray();
                JSONObject object = new JSONObject();
                object.put("vkey", mCt);
                String futureValue = mNumberPickerView.getDisplayedValues()[mNumberPickerView.getValue()];
                String value = "";
                if(mCt==119){
                    value = futureValue.replaceAll("ppm","");
                }else if(mCt==152){
                    value = futureValue.replaceAll("℃","");
                    value = judgeLessThanZero(value)+handleTempToHex(value,6);
                }
                object.put("value",value);
                array.add(object);
                pJsonObject.put("vkeys", array);
                new HttpAsyncTask(this,HttpAsyncTask.Zhuji_SET_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pJsonObject);
                return true ;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    private void bindView(){
        mNumberPickerView = findViewById(R.id.number_picker_iew);
    }

    private void bindEvent(){

    }

    private void bindData(){
        mData = getResources().getStringArray(mResId);
        mNumberPickerView.setDisplayedValues(mData);
        mNumberPickerView.setMinValue(0);
        mNumberPickerView.setMaxValue(mData.length-1);
        if(defaultPosition!=-1){
            mNumberPickerView.setValue(defaultPosition);
        }else{
            mNumberPickerView.setValue(mData.length/2);
        }
        mDataList.clear();
        int length = mData!=null?mData.length:0;
        for(int i=0;i<length;i++){
            mDataList.add(mData[i]);
        }
        new LoadZhujiAndDeviceTask().queryAllCommandInfo(mDeviceId, new LoadZhujiAndDeviceTask.ILoadResult<List<CommandInfo>>() {
            @Override
            public void loadResult(List<CommandInfo> result) {
                if(result==null){
                    return ;
                }

                for(CommandInfo commandInfo:result){
                    if(commandInfo.getCtype().equals(String.valueOf(mCt))){
                        int index = 0 ;
                        if(mCt==152){
                            //使用的是十六进制
                            index = mDataList.indexOf(tempTransformHex(commandInfo.getCommand())+"℃");
                        }else{
                            index = mDataList.indexOf(commandInfo.getCommand()+"ppm");
                        }
                        if(mNumberPickerView.getMinValue()<=index&&index<=mNumberPickerView.getMaxValue()){
                            mNumberPickerView.setValue(index);
                        }
                        break ;
                    }
                }

            }
        });
    }


    @Override
    public int setLayoutId() {
        return R.layout.activity_number_picker_setting_layout;
    }

    @Override
    public void setResult(int flag, String result) {
        if(flag==HttpAsyncTask.Zhuji_SET_URL_FLAG){
            if ("-3".equals(result)) {
                Toast.makeText(this, getString(R.string.net_error_nodata),
                        Toast.LENGTH_LONG).show();
            } else if ("-5".equals(result)) {
                Toast.makeText(this, getString(R.string.device_not_getdata),
                        Toast.LENGTH_LONG).show();
            } else if ("0".equals(result)) {
                //设置成功，结束当前页面
                Toast.makeText(this, getString(R.string.success),
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra(String.valueOf(mCt),mData[mNumberPickerView.getValue()]);
                setResult(RESULT_OK,intent);
                finish();
            }else if("-4".equals(result)){
                ToastTools.short_Toast(this,getResources().getString(R.string.activity_zhuji_not));
            }
        }
    }


    //符号处理
    String judgeLessThanZero(String value){
        if(Integer.parseInt(value)<0){
            return "01";
        }else{
            return "00";
        }
    }
    //温度值转换为10进制
    String tempTransformHex(String hex){
        String fuHao = hex.substring(0,2);
        if(fuHao.equals("00")){
            //正
            return String.valueOf(Integer.parseInt(hex,16));//十六进制转十进制
        }else{
            //负号
            return "-"+Integer.valueOf(hex.substring(2,hex.length()));
        }

    }
    //温度值转16进制
    String handleTempToHex(String value,int length){
        String hex = Integer.toHexString(Integer.parseInt(value));
        if(hex.length()<length){
            while(hex.length()!=length){
                hex = "0"+hex;
            }
        }

        return hex ;
    }

    @Override
    public void success(String message) {

    }

    @Override
    public void error(String message) {

    }
}
