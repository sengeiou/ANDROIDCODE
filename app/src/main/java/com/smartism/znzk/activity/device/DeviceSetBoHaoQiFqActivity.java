package com.smartism.znzk.activity.device;

import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.alert.NoCloseAlarmActivity;
import com.smartism.znzk.activity.common.TriggerSettingActivity;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceInfo.ControlTypeMenu;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.ContainsEmojiEditText;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;
import com.smartism.znzk.zhicheng.tasks.LoadCommandsInfo;

import org.apache.commons.lang.StringUtils;

import java.util.List;

import static com.smartism.znzk.activity.alert.ChooseAudioSettingMode.SEND_RESULT_EXTRAS;

/**
 * 设置 - 拨号器子设备 防区 的设置
 *
 * 实现方式和ios一样，将名称直接放在这一层，去下层编辑其他属性。
 *
 */
public class DeviceSetBoHaoQiFqActivity extends ActivityParentActivity implements LoadCommandsInfo.ILoadCommands, View.OnClickListener,HttpAsyncTask.IHttpResultView {
    private final int REQUEST_ALARM_TYPE =0X68,REQUEST_ALARM_SIGNAL = 0X69;
    private TextView  editAlarmType,editAlarmSignal,save;
    private LinearLayout layoutAlarmType,layoutAlarmSignal;
    private ContainsEmojiEditText name;
    private DeviceInfo operationDevice;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:  //初始化加载完成
                    JSONObject resultBack = (JSONObject) msg.obj;
                    if (resultBack.get("name") != null) {
                        name.setText(resultBack.getString("name"));
                    }
                    cancelInProgress();
                    break;
                case 10: // 修改完成
                    cancelInProgress();
                    sendBroadcast(new Intent(Actions.REFRESH_DEVICES_LIST));
                    Toast.makeText(DeviceSetBoHaoQiFqActivity.this, getString(R.string.device_set_tip_success), Toast.LENGTH_LONG).show();
                    //请求刷新列表
                    SyncMessageContainer.getInstance()
                            .produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                    finish();
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    /* (non-Javadoc)
     * @see com.znwx.jiadianmao.activity.ActivityParentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_set_bohaoqi_fq);
        operationDevice = (DeviceInfo) getIntent().getSerializableExtra("device");
        name = (ContainsEmojiEditText) findViewById(R.id.set_name_edit);
        editAlarmType = (TextView) findViewById(R.id.tv_alarm_type);
        editAlarmSignal = (TextView) findViewById(R.id.tv_alarm_signal);
        layoutAlarmType = (LinearLayout) findViewById(R.id.set_bohaoqi_type_layout);
        layoutAlarmType.setOnClickListener(this);
        layoutAlarmSignal = (LinearLayout) findViewById(R.id.set_bohaoqi_signal_layout);
        layoutAlarmSignal.setOnClickListener(this);
        save = (TextView) findViewById(R.id.save);
        save.setOnClickListener(this);


        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new InitDeviceInfoThread());
        new LoadCommandsInfo(this).execute(operationDevice.getId());
    }

    public void back(View v) {
        finish();
    }

    public void subToUpdate() {
        if(TextUtils.isEmpty(name.getText().toString())){
            ToastUtil.shortMessage(getString(R.string.zhuji_owner_msg_name));
            return ;
        }
        showInProgress(getString(R.string.device_set_tip_inupdate), false, true);
        JavaThreadPool.getInstance().excute(new UpdateInfoThread());
    }

    public void subMoreSetUpdate(){
        JSONObject pJsonObject = new JSONObject();
        pJsonObject.put("did", operationDevice.getId());
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("vkey", CommandInfo.CommandTypeEnum.dSetDpTypeChufa.value());
        if (getString(R.string.triggers_high_lever).equalsIgnoreCase(editAlarmType.getText().toString())){
            object.put("value",1);
        }else{
            object.put("value",0);
        }
        array.add(object);
        object = new JSONObject();
        object.put("vkey", CommandInfo.CommandTypeEnum.dSetChufaSignal.value());
        if (getString(R.string.triggers_edge_level).equalsIgnoreCase(editAlarmType.getText().toString())){
            object.put("value",1);
        }else{
            object.put("value",0);
        }
        array.add(object);
        pJsonObject.put("vkeys", array);
        new HttpAsyncTask(DeviceSetBoHaoQiFqActivity.this,HttpAsyncTask.Zhuji_SET_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pJsonObject);
    }

    protected void onDestroy() {
        defaultHandler.removeCallbacksAndMessages(null);
        defaultHandler = null;
        super.onDestroy();
    }

    @Override
    public void loadCommands(List<CommandInfo> lists) {
        String currentTypeStr = getString(R.string.triggers_high_lever),currentSignalStr = getString(R.string.triggers_edge_level);
        //第三防区默认为低电平也就是持续信号
        if ("0001003".equalsIgnoreCase(operationDevice.getSlaveId())){
            currentTypeStr = getString(R.string.triggers_low_level);
        }
        if(lists!=null&&lists.size()>0){
            for(CommandInfo commandInfo :lists){
                if(CommandInfo.CommandTypeEnum.dSetDpTypeChufa.value().equalsIgnoreCase(commandInfo.getCtype())){
                    if(Integer.parseInt(commandInfo.getCommand()) == 0){
                        currentTypeStr = getString(R.string.triggers_low_level);
                    }else{
                        currentTypeStr = getString(R.string.triggers_high_lever);
                    }
                }else if(CommandInfo.CommandTypeEnum.dSetChufaSignal.value().equalsIgnoreCase(commandInfo.getCtype())){
                    if(Integer.parseInt(commandInfo.getCommand()) == 0){
                        currentSignalStr = getString(R.string.triggers_edge_edge);
                    }else{
                        currentSignalStr = getString(R.string.triggers_edge_level);
                    }
                }
            }
        }
        editAlarmSignal.setText(currentSignalStr);
        editAlarmType.setText(currentTypeStr);
    }

    @Override
    public void showProgress(String text) {

    }

    @Override
    public void hideProgress() {

    }

    @Override
    public void error(String message) {

    }

    @Override
    public void success(String message) {

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.save:
                subToUpdate();
                break;
            case R.id.set_bohaoqi_type_layout:
                intent.setClass(getApplicationContext(), TriggerSettingActivity.class);
                intent.putExtra("device_id",operationDevice.getId());
                intent.putExtra("flags",0);//触发电平设置
                startActivityForResult(intent,REQUEST_ALARM_TYPE);
                break;
            case R.id.set_bohaoqi_signal_layout:
                intent.setClass(getApplicationContext(), TriggerSettingActivity.class);
                intent.putExtra("device_id",operationDevice.getId());
                intent.putExtra("flags",1);//触发模式设置
                startActivityForResult(intent,REQUEST_ALARM_SIGNAL);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_ALARM_TYPE:
                //为了防止数据没能及时刷新，采用这种方式
                if(resultCode==RESULT_OK){
                    editAlarmType.setText(data.getStringExtra(SEND_RESULT_EXTRAS));
                }
                break ;
            case REQUEST_ALARM_SIGNAL:
                //为了防止数据没能及时刷新，采用这种方式
                if(resultCode==RESULT_OK){
                    editAlarmSignal.setText(data.getStringExtra(SEND_RESULT_EXTRAS));
                }
                break ;
        }
    }

    @Override
    public void setResult(int flag, String result) {
        if(flag==HttpAsyncTask.Zhuji_SET_URL_FLAG){
            if ("-3".equals(result)) {
                cancelInProgress();
                ToastUtil.shortMessage(getResources().getString(R.string.net_error_nodata));
            } else if ("-5".equals(result)) {
                cancelInProgress();
                ToastUtil.shortMessage(getResources().getString(R.string.device_not_getdata));
            } else if ("0".equals(result)) {
//                JavaThreadPool.getInstance().excute(new UpdateInfoThread());
            }else if("-4".equals(result)){
                cancelInProgress();
                ToastUtil.shortMessage(getResources().getString(R.string.activity_zhuji_not));
            }
        }
    }

    class UpdateInfoThread implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", operationDevice.getId());
            if (!name.getText().toString().equals("")) {
                object.put("name", name.getText().toString());
            }

            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/uinfo", object, DeviceSetBoHaoQiFqActivity.this);
            if ("-3".equals(result)) {
                defaultHandler.post(()-> {
                    cancelInProgress();
                    Toast.makeText(DeviceSetBoHaoQiFqActivity.this, getString(R.string.device_set_tip_nodevice), Toast.LENGTH_LONG).show();
                });
            } else if (!StringUtils.isEmpty(result)) {
                JSONObject resultBack = null;
                try {
                    resultBack = JSON.parseObject(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                }
                if (resultBack == null) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(DeviceSetBoHaoQiFqActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                updateDBDate(resultBack);

                defaultHandler.sendEmptyMessage(10);
            }
        }

        private void updateDBDate(JSONObject resultBack) {
            if (ControlTypeMenu.zhuji.value().equals(operationDevice.getControlType())) {
                ContentValues values = new ContentValues();
                if (resultBack.get("deviceName") != null) {
                    values.put("name", resultBack.getString("deviceName"));
                } else {
                    values.put("name", "");
                }
                if (resultBack.get("deviceWhere") != null) {
                    values.put("dwhere", resultBack.getString("deviceWhere"));
                } else {
                    values.put("dwhere", "");
                }
                try {
                    DatabaseOperator.getInstance().getWritableDatabase().update("ZHUJI_STATUSINFO", values, "id = ?", new String[]{String.valueOf(resultBack.getLongValue("deviceId"))});
                } catch (Exception e) {
                    Log.e(TAG, "获取数据库失败");
                }
                if (StringUtils.isEmpty(values.getAsString("name"))){ //名字为空，需要发一下107刷新出默认的名字
                    SyncMessageContainer.getInstance()
                            .produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                }
            } else {
                ContentValues values = new ContentValues();
                if (resultBack.get("deviceName") != null) {
                    values.put("device_name", resultBack.getString("deviceName"));
                } else {
                    values.put("device_name", "");
                }
                if (resultBack.get("deviceWhere") != null) {
                    values.put("device_where", resultBack.getString("deviceWhere"));
                } else {
                    values.put("device_where", "");
                }
                try {
                    DatabaseOperator.getInstance().getWritableDatabase().update("DEVICE_STATUSINFO", values, "id = ?", new String[]{String.valueOf(resultBack.getLongValue("deviceId"))});
                } catch (Exception e) {
                    Log.e(TAG, "获取数据库失败");
                }
            }
        }
    }

    class InitDeviceInfoThread implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", operationDevice.getId());
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/info", object, DeviceSetBoHaoQiFqActivity.this);
            if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceSetBoHaoQiFqActivity.this, getString(R.string.device_set_tip_nodevice), Toast.LENGTH_LONG).show();
                    }
                });
            } else if (!StringUtils.isEmpty(result) && result.length() > 4) {
                JSONObject resultBack = null;
                try {
                    resultBack = JSON.parseObject(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                    return;
                }
                if (resultBack == null) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(DeviceSetBoHaoQiFqActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                Message m = defaultHandler.obtainMessage(1);
                m.obj = resultBack;
                defaultHandler.sendMessage(m);
            }else{
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                    }
                });
            }
        }
    }
}
