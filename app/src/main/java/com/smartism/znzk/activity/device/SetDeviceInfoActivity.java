package com.smartism.znzk.activity.device;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.camera.CameraListActivity;
import com.smartism.znzk.activity.scene.CustomSceneActivity;
import com.smartism.znzk.activity.scene.LinkageSceneActivity;
import com.smartism.znzk.activity.scene.TimingSceneActivity;
import com.smartism.znzk.adapter.scene.SceneAdapter;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.FoundInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.MyGridView;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SetDeviceInfoActivity extends ActivityParentActivity implements View.OnClickListener, View.OnFocusChangeListener {
    private RelativeLayout deviceinfo_history, deviceinfo_camera, rl_userList;
    private ImageView mBack;
    private ImageView scene_save;
    private TextView mTv_title;
    private TextView mMenu_tv;
    private TextView mDevice_type;
    private TextView mTv_diy_scene;
    private EditText mDevice_addr;
    private EditText mDevice_name;
    private MyGridView security_list;
    private SceneAdapter sceneAdapter;
    private List<FoundInfo> sceneList = new ArrayList<FoundInfo>();
    String name = "";
    String where = "";
    private DeviceInfo deviceInfo;
    private String num = "";

    private String result;
    private FoundInfo resultStr;

    private boolean isMainList;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Intent intent = new Intent();
            switch (msg.what) {
                case 1:  //初始化加载完成
                    JSONObject resultBack = (JSONObject) msg.obj;
                    if (resultBack.get("name") != null) {
                        mDevice_name.setText(resultBack.getString("name"));
                    }
                    if (resultBack.get("where") != null) {
                        mDevice_addr.setText(resultBack.getString("where"));
                    }
                    if (resultBack.get("number") != null) {
                        num = resultBack.getString("number");
                    }
                    cancelInProgress();
                    break;
                case 10: // 修改完成
                    cancelInProgress();
                    scene_save.setVisibility(View.GONE);
                    mDevice_name.clearFocus();
                    mDevice_addr.clearFocus();
                    intent = new Intent(SecurityInfoActivity.FRUSH_DEVICE_INFO);
                    deviceInfo.setName(name);
                    deviceInfo.setWhere(where);
                    intent.putExtra("device", deviceInfo);
                    sendBroadcast(intent);
                    Toast.makeText(SetDeviceInfoActivity.this, getString(R.string.device_set_tip_success), Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    List<FoundInfo> sceneInfos = (List<FoundInfo>) msg.obj;
                    sceneList.clear();
                    sceneList.addAll(sceneInfos);
                    sceneAdapter.notifyDataSetChanged();
                    break;
                case 7:
                    if (result == null || "".equals(result)) {
                        cancelInProgress();
                        Toast.makeText(SetDeviceInfoActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    resultStr = JSON.parseObject(result, FoundInfo.class);
                    resultStr.setTip(1);

                    intent.putExtra("result", resultStr);
                    switch (resultStr.getType()) {
                        case 0:
                            intent.setClass(getApplicationContext(), CustomSceneActivity.class);
                            break;
                        case 1:
                            intent.setClass(getApplicationContext(), TimingSceneActivity.class);
                            break;
                        case 2:
                            intent.setClass(getApplicationContext(), LinkageSceneActivity.class);
                            break;
                    }
                    intent.putExtra("edit", false);
                    startActivity(intent);
                    break;

            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_device_info);
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        isMainList = getIntent().getBooleanExtra("isMainList", false);
        initView();
        initDate();
        initEvent();
        initListView();
    }

    private void initEvent() {
        deviceinfo_history.setOnClickListener(this);
        deviceinfo_camera.setOnClickListener(this);
        scene_save.setOnClickListener(this);
        rl_userList.setOnClickListener(this);
        mDevice_addr.setOnFocusChangeListener(this);
        mDevice_name.setOnFocusChangeListener(this);
    }

    private void initDate() {
        mDevice_type.setText(deviceInfo.getType());
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new InitDeviceInfoThread());
    }

    public void initListView() {
        sceneAdapter = new SceneAdapter(sceneList, this);
        security_list.setAdapter(sceneAdapter);
        JavaThreadPool.getInstance().excute(new ScenesLoad());
        security_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FoundScene(sceneList.get(position).getId());

            }
        });
    }

    private void initView() {
        security_list = (MyGridView) findViewById(R.id.security_list);
        deviceinfo_camera = (RelativeLayout) findViewById(R.id.deviceinfo_camera);
        deviceinfo_history = (RelativeLayout) findViewById(R.id.deviceinfo_history);
        mBack = (ImageView) findViewById(R.id.back);
        scene_save = (ImageView) findViewById(R.id.scene_save);
        mTv_title = (TextView) findViewById(R.id.tv_title);
        mMenu_tv = (TextView) findViewById(R.id.menu_tv);
        mDevice_name = (EditText) findViewById(R.id.device_name);
        mDevice_type = (TextView) findViewById(R.id.device_type);
        mDevice_addr = (EditText) findViewById(R.id.device_addr);
        mTv_diy_scene = (TextView) findViewById(R.id.tv_diy_scene);
        rl_userList = (RelativeLayout) findViewById(R.id.rl_userList);
        if (deviceInfo.getCa().equals(DeviceInfo.CaMenu.djkzq.value())) {
            rl_userList.setVisibility(View.VISIBLE);
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.REFRESH_DEVICES_LIST);
        registerReceiver(receiver, filter);

        if (isMainList) {
            deviceinfo_camera.setVisibility(View.GONE);
            security_list.setVisibility(View.GONE);
            findViewById(R.id.scene_list_title).setVisibility(View.GONE);
        }
    }

    public void finishActivity() {
        Contact mContact = (Contact) getIntent().getSerializableExtra("contact");
        String action = getIntent().getStringExtra("action");
        if (mContact != null) {
            if (action != null && !"".equals(action)) {
                Intent intent = new Intent();
                intent.putExtra("device", deviceInfo);
                intent.putExtra("contact", mContact);
                intent.setAction(action);
                startActivity(intent);
            }
        }
        finish();
    }

    public void back(View view) {
        finishActivity();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            finishActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_userList:
                Intent userIntent = new Intent();
                userIntent.putExtra("device", deviceInfo);
                userIntent.setClass(this, PerminssonTransActivity.class);
                startActivity(userIntent);
                break;
            case R.id.deviceinfo_history:
                Intent intent = new Intent();
                intent.putExtra("device", deviceInfo);
                intent.setClass(this, DeviceCommandHistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.deviceinfo_camera:
                Intent bintent = new Intent();
                bintent.setClass(getApplicationContext(), CameraListActivity.class);
                bintent.putExtra("device", deviceInfo);
                startActivity(bintent);
                break;
            case R.id.scene_save:
                save();
                break;
        }
    }

    private void save() {
        showInProgress(getString(R.string.device_set_tip_inupdate), false, true);
        JavaThreadPool.getInstance().excute(new UpdateInfoThread());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Actions.REFRESH_DEVICES_LIST.equals(intent.getAction())) {
                if (deviceInfo == null) return;
                DeviceInfo deviceInfo1 = null;
                try {
                    deviceInfo1 = DatabaseOperator.getInstance().queryDeviceInfo(deviceInfo.getId());
                    if (deviceInfo1 != null)
                        deviceInfo = deviceInfo1;
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
    };

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.device_name:
            case R.id.device_addr:
                if (hasFocus) {
                    scene_save.setVisibility(View.VISIBLE);
                } else {
                    mDevice_name.requestFocus();
                    mDevice_addr.requestFocus();
                }
                break;
        }
    }


    public void FoundScene(final long cid) {
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("id", cid);
                result = HttpRequestUtils
                        .requestoOkHttpPost( server + "/jdm/s3/scenes/get", pJsonObject, SetDeviceInfoActivity.this);
                if ("-3".equals(result)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(SetDeviceInfoActivity.this, getString(R.string.device_not_getdata),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (result != null && result.length() > 3) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            mHandler.sendEmptyMessage(7);
                        }
                    });
                }
            }
        });

    }

    class UpdateInfoThread implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", deviceInfo.getId());
            name = mDevice_name.getText().toString();
            where = mDevice_addr.getText().toString();
            if (!mDevice_name.getText().toString().equals("")) {
                object.put("name", name);
            }
            if (!mDevice_addr.getText().toString().equals("")) {
                object.put("where", where);
            }
            String secuirt = num;
            object.put("number", secuirt);

            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/uinfo", object, SetDeviceInfoActivity.this);
            if ("-3".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(SetDeviceInfoActivity.this, getString(R.string.device_set_tip_nodevice), Toast.LENGTH_LONG).show();
                    }
                });
            } else if (!StringUtils.isEmpty(result)) {
                JSONObject resultBack = null;
                resultBack = JSON.parseObject(result);
                try {
//                    resultBack = JSON.parseObject(SecurityUtil.decryptHexStringToString(result, DataCenterSharedPreferences.Constant.KEY_HTTP));
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                }
                if (resultBack == null) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(SetDeviceInfoActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                updateDBDate(resultBack);

                mHandler.sendEmptyMessage(10);
            }
        }

        private void updateDBDate(JSONObject resultBack) {
            if (DeviceInfo.ControlTypeMenu.zhuji.value().equals(deviceInfo.getControlType())) {
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
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", deviceInfo.getId());
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/info", object, SetDeviceInfoActivity.this);
            if ("-3".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(SetDeviceInfoActivity.this, getString(R.string.device_set_tip_nodevice), Toast.LENGTH_LONG).show();
                    }
                });
            } else if (!StringUtils.isEmpty(result) && result.length() > 4) {
                JSONObject resultBack = null;
                resultBack = JSON.parseObject(result);
//                try {
//                    resultBack = JSON.parseObject(SecurityUtil.decryptHexStringToString(result, DataCenterSharedPreferences.Constant.KEY_HTTP));
//                } catch (Exception e) {
//                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
//                    return;
//                }
                if (resultBack == null) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(SetDeviceInfoActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                Message m = mHandler.obtainMessage(1);
                m.obj = resultBack;
                mHandler.sendMessage(m);
            }
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            Log.e("TAG_2_3", "intent:" + intent.getAction());
        }
    };


    class ScenesLoad implements Runnable {

        public ScenesLoad() {
        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", deviceInfo.getId());
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/scenes/qlscenes", object, SetDeviceInfoActivity.this);
            List<FoundInfo> sceneInfos = new ArrayList<FoundInfo>();
            if (!StringUtils.isEmpty(result) && result.startsWith("[")) {
                JSONArray ll = null;
                try {
                    ll = JSON.parseArray(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                }
                if (ll == null) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(SetDeviceInfoActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                for (int j = 0; j < ll.size(); j++) {
                    JSONObject jsonObject = (JSONObject) ll.get(j);
                    FoundInfo resultStr = JSON.parseObject(jsonObject.toString(), FoundInfo.class);
                    sceneInfos.add(resultStr);
                }
            }
            Message m = mHandler.obtainMessage(2);
            m.obj = sceneInfos;
            mHandler.sendMessage(m);
        }
    }
}
