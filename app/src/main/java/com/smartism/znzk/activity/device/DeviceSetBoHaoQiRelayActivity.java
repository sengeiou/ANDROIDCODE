package com.smartism.znzk.activity.device;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.common.JdqWorkTimeSettingActivity;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceInfo.ControlTypeMenu;
import com.smartism.znzk.domain.ZhujiInfo;
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

import java.util.ArrayList;
import java.util.List;

import static com.smartism.znzk.activity.alert.ChooseAudioSettingMode.SEND_RESULT_EXTRAS;

/**
 * 设置 - 拨号器子设备 继电器的设置
 */
public class DeviceSetBoHaoQiRelayActivity extends ActivityParentActivity implements LoadCommandsInfo.ILoadCommands, View.OnClickListener,HttpAsyncTask.IHttpResultView {
    private EditText  relayWorkTime;
    private ContainsEmojiEditText name;
    private TextView save;
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
                    Toast.makeText(DeviceSetBoHaoQiRelayActivity.this, getString(R.string.device_set_tip_success), Toast.LENGTH_LONG).show();
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
        setContentView(R.layout.activity_device_set_bohaoqi_relay);
        operationDevice = (DeviceInfo) getIntent().getSerializableExtra("device");
        name = (ContainsEmojiEditText) findViewById(R.id.set_name_edit);
        relayWorkTime = (EditText) findViewById(R.id.relay_working_time);
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
        String value = relayWorkTime.getText().toString();
        if(TextUtils.isEmpty(value)){
            ToastTools.short_Toast(this,getString(R.string.jieaolihua_time_not_empty));
            return ;
        }
        if(Integer.parseInt(value)>65000){
            ToastTools.short_Toast(this,getString(R.string.jwtsa_work_must_delow_value,65000));
            return ;
        }
        showInProgress(getString(R.string.device_set_tip_inupdate), false, true);
        JSONObject pJsonObject = new JSONObject();
        pJsonObject.put("did", operationDevice.getId());
        JSONArray array = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("vkey", CommandInfo.CommandTypeEnum.dSetJdqWorkTime.value());
        object.put("value",value);
        array.add(object);
        pJsonObject.put("vkeys", array);
        new HttpAsyncTask(DeviceSetBoHaoQiRelayActivity.this,HttpAsyncTask.Zhuji_SET_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,pJsonObject);
    }

    protected void onDestroy() {
        defaultHandler.removeCallbacksAndMessages(null);
        defaultHandler = null;
        super.onDestroy();
    }

    @Override
    public void loadCommands(List<CommandInfo> lists) {
        String currentJdqStr = "";
        if(lists!=null&&lists.size()>0){
            for(CommandInfo commandInfo :lists){
                if(CommandInfo.CommandTypeEnum.dSetJdqWorkTime.value().equalsIgnoreCase(commandInfo.getCtype())){
                    //工作时长
                    currentJdqStr = commandInfo.getCommand() ;
                    break ;
                }
            }
        }
        if(TextUtils.isEmpty(currentJdqStr)){
            //默认值
            currentJdqStr = "5";
        }
        relayWorkTime.setText(currentJdqStr);
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
        switch (v.getId()){
            case R.id.save:
                subToUpdate();
                break;
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
                JavaThreadPool.getInstance().excute(new UpdateInfoThread());
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

            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/uinfo", object, DeviceSetBoHaoQiRelayActivity.this);
            if ("-3".equals(result)) {
                defaultHandler.post(()-> {
                    cancelInProgress();
                    Toast.makeText(DeviceSetBoHaoQiRelayActivity.this, getString(R.string.device_set_tip_nodevice), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(DeviceSetBoHaoQiRelayActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
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
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/info", object, DeviceSetBoHaoQiRelayActivity.this);
            if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceSetBoHaoQiRelayActivity.this, getString(R.string.device_set_tip_nodevice), Toast.LENGTH_LONG).show();
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
                            Toast.makeText(DeviceSetBoHaoQiRelayActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
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
