package com.smartism.znzk.activity.common;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;
import com.smartism.znzk.zhicheng.tasks.LoadCommandsInfo;

import java.util.List;

import static com.smartism.znzk.activity.alert.ChooseAudioSettingMode.SEND_RESULT_EXTRAS;

public class JdqWorkTimeSettingActivity extends MZBaseActivity implements LoadCommandsInfo.ILoadCommands,HttpAsyncTask.IHttpResultView{

    private EditText input_time;
    private long device_id ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.jwtsa_work_time_title));
        if(savedInstanceState==null){
            device_id = getIntent().getLongExtra("device_id",0);
        }else{
            device_id = savedInstanceState.getLong("device_id");
        }
        input_time = findViewById(R.id.input_time);
        new LoadCommandsInfo(this).execute(device_id);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong("device_id",device_id);
        super.onSaveInstanceState(outState);
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_jdq_work_time_setting_layout;
    }

    public void saveEvent(View v){
        String value = input_time.getText().toString();
        if(TextUtils.isEmpty(value)){
            ToastTools.short_Toast(this,getString(R.string.jieaolihua_time_not_empty));
            return ;
        }
        if(Integer.parseInt(value)>6500){
            ToastTools.short_Toast(this,getString(R.string.jwtsa_work_must_delow_value,6500));
            return ;
        }
        JSONObject pJsonObject = new JSONObject();
        pJsonObject.put("did", device_id);
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("vkey", "154");
        object.put("value",value);
        array.add(object);
        pJsonObject.put("vkeys", array);
        new HttpAsyncTask(JdqWorkTimeSettingActivity.this,HttpAsyncTask.Zhuji_SET_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pJsonObject);
    }

    @Override
    public void loadCommands(List<CommandInfo> lists) {
        String currentJdqStr = "";
        if(lists!=null&&lists.size()>0){
            for(CommandInfo commandInfo :lists){
               if(commandInfo.getCtype().equals("154")){
                    //工作时长
                    currentJdqStr = commandInfo.getCommand() ;
                    break ;
                }
            }
        }
        if(TextUtils.isEmpty(currentJdqStr)){
            //默认值
            currentJdqStr = "3";
        }
        input_time.setText(currentJdqStr);
        input_time.setSelection(currentJdqStr.length());
    }

    @Override
    public void setResult(int flag, String result) {
        if(flag==HttpAsyncTask.Zhuji_SET_URL_FLAG){
            if ("-3".equals(result)) {
                Toast.makeText(JdqWorkTimeSettingActivity.this, getString(R.string.net_error_nodata),
                        Toast.LENGTH_LONG).show();
            } else if ("-5".equals(result)) {
                Toast.makeText(JdqWorkTimeSettingActivity.this, getString(R.string.device_not_getdata),
                        Toast.LENGTH_LONG).show();
            } else if ("0".equals(result)) {
                Toast.makeText(JdqWorkTimeSettingActivity.this, getString(R.string.success),
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.putExtra(SEND_RESULT_EXTRAS,input_time.getText().toString());
                setResult(RESULT_OK,intent);
                finish();
            }else if("-4".equals(result)){
                ToastTools.short_Toast(this,getResources().getString(R.string.activity_zhuji_not));
            }
        }
    }

    @Override
    public void success(String message) {

    }
}
