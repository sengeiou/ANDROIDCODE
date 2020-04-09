package com.smartism.znzk.activity.device;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

import java.util.List;

public class WiFiRqSettingActivity extends MZBaseActivity implements View.OnClickListener{

    private static final int SETTING_REQUEST_CODE = 0X66 ;
    private LinearLayout mNongduParent,mTempParent;
    private TextView mCurrentRqTv,mCurrentTempTv;
    private long mDeviceId ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            mDeviceId = getIntent().getLongExtra("device_id",-1);
        }else{
            mDeviceId = savedInstanceState.getLong("device_id",-1);
        }
        setTitle(getResources().getString(R.string.devices_list_menu_dialog_devicesetting));
        bindView();
        bindEvent();
        bindData();
    }

    private void bindData(){
        new LoadZhujiAndDeviceTask().queryAllCommandInfo(mDeviceId, new LoadZhujiAndDeviceTask.ILoadResult<List<CommandInfo>>() {
            @Override
            public void loadResult(List<CommandInfo> result) {
                if(result==null){
                    return ;
                }
                for(CommandInfo commandInfo:result){
                    if(commandInfo.getCtype().equals("119")){
                        mCurrentRqTv.setText(commandInfo.getCommand()+"ppm");
                    }else if(commandInfo.getCtype().equals("152")){
                        mCurrentTempTv.setText(tempTransformHex(commandInfo.getCommand())+"℃");
                    }
                }

            }
        });
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("device_id",mDeviceId);
        super.onSaveInstanceState(outState);
    }

    private void bindView(){
        mCurrentRqTv = findViewById(R.id.current_ranqi);
        mCurrentTempTv = findViewById(R.id.current_temp);
        mNongduParent = findViewById(R.id.ll_rangqinongdu_setting);
        mTempParent = findViewById(R.id.ll_temp_setting);
    }

    private void bindEvent(){
        mNongduParent.setOnClickListener(this);
        mTempParent.setOnClickListener(this);
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_wi_fi_rq_setting_layout;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.setClass(this,NumberPickerSettingActivity.class);
        intent.putExtra("device_id",mDeviceId);
        switch (v.getId()){
            case R.id.ll_rangqinongdu_setting:
                intent.putExtra("position",2);
                intent.putExtra("data",R.array.jingyuanxin_rqnongdu_array);
                intent.putExtra("title",getResources().getString(R.string.ranqinongdu_fazhi));
                intent.putExtra("ct",119);
                break ;
            case R.id.ll_temp_setting:
                intent.putExtra("position",14);
                intent.putExtra("data",R.array.jingyuanxin_wifirq_temp);
                intent.putExtra("title",getResources().getString(R.string.wendu_fazhi));
                intent.putExtra("ct",152);
                break ;
        }
        startActivityForResult(intent,SETTING_REQUEST_CODE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==SETTING_REQUEST_CODE&&RESULT_OK==resultCode){
            mCurrentTempTv.setText(data.getStringExtra("152")==null?mCurrentTempTv.getText():tempTransformHex(data.getStringExtra("152")));
            mCurrentRqTv.setText(data.getStringExtra("119")==null?mCurrentRqTv.getText():data.getStringExtra("119"));
        }
    }
}
