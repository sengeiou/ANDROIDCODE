package com.smartism.znzk.zhicheng.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonParseException;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.xiongmai.fragment.XMFragment;
import com.smartism.znzk.zhicheng.models.ARCModel;
import com.smartism.znzk.zhicheng.tasks.GeneralHttpTask;
import com.smartism.znzk.zhicheng.tasks.HttpAsyncTask;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.smartism.znzk.zhicheng.tasks.GeneralHttpTask.GET_BRAND_KEY_EVENT_URL;
import static com.smartism.znzk.zhicheng.tasks.GeneralHttpTask.GET_BRAND_KEY_LIST_URL;
import static com.smartism.znzk.zhicheng.tasks.HttpAsyncTask.IR_REMOTE_DELETE_URL_FLAG;

/*
 * 说明:当我们通过GET_BRAND_KEY_EVENT_URL不传入keyid时，获取的当前遥控器在辉联后台保存的状态，当传入keyid时，获取的是具体的按键码值，此时
 * 后台保存的遥控器状态也会更新成当前获取的具体按键的码值。
 *
 * author mz
 * */
public class AirConditioningActivity extends MZBaseActivity implements View.OnClickListener, GeneralHttpTask.ILoadARKeysImpl, HttpAsyncTask.IHttpResultView {

    private ImageView img_wind_direc, indication_btn, img_shut_btn, add_temprature_img, sub_temprature_img;
    private ImageView img_type_speed, imag_speed_speed, auto_case_tv;
    private View qiehuan_view;
    long deviceId;
    ARCModel mModel;
    int operateFlag = -1;  //1获取当前空调的各个按键值状态,2不知，3不知道
    TextView temper_tv; //显示温度的
    TextView auto_tv; //显示当前模式
    TextView wind_direc_auto_tv; //显示当前的风向
    TextView wind_speed_auto_tv; //显示当前风速
    TextView tv_remote_count; //显示当前牌子下遥控器的数量
    Map<Integer, String> keys = new HashMap<>();
    boolean stateSuccess = false; //判断状态是否请求成功
    final int SEND_TIMEOUT = 99;
    boolean is_modelist = false; //标识是否是从ZCModeList过来的
    String bipc;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_TIMEOUT:
                    hideProgress();//隐藏进度条
                    ToastTools.short_Toast(AirConditioningActivity.this, getResources().getString(R.string.request_timeout));
                    break;
            }
        }
    };

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Actions.ACCETP_ONEDEVICE_MESSAGE)) {
                String device_id = intent.getStringExtra("device_id");
                //不是关于这个设备的不处理
                if (device_id == null || Long.parseLong(device_id) != deviceId) {
                    return;
                }
                String data = (String) intent.getSerializableExtra("device_info");
                mHandler.removeMessages(SEND_TIMEOUT);
                hideProgress();
                JSONObject object = JSONObject.parseObject(data);
                if (object.getString("dt") != null && object.getString("dt").equals("0")) {
                    ToastTools.short_Toast(AirConditioningActivity.this, getResources().getString(R.string.rq_control_sendsuccess));
                    if (!is_modelist || kfids == null) {
                        return;
                    }
                    if (mNeedDisplaySelect) {
                        new AlertView(getString(R.string.hwzf_device_response_tip),
                                null,
                                getString(R.string.no),
                                new String[]{getString(R.string.yes)}, null,
                                mContext, AlertView.Style.Alert,
                                new OnItemClickListener() {
                                    @Override
                                    public void onItemClick(Object o, final int position) {
                                        if (position == -1) {
                                            //下一个空调型号
                                            nextRemoteControl(null);
                                        } else {
                                            //点击了是,进行保存
                                            selectRemoteControl(null);
                                        }
                                    }
                                }).show();
                    }

                }
            } else if (intent.getAction().equals(Actions.SHOW_SERVER_MESSAGE)) {
                //返送指令失败返回
                mHandler.removeMessages(SEND_TIMEOUT);
                hideProgress();
                //返回指令操作失败
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(intent.getStringExtra("message"));
                } catch (Exception e) {
                    Log.w("DevicesList", "获取服务器返回消息，转换为json对象失败，用原始值处理");
                }
                if (resultJson != null) {
                    switch (resultJson.getIntValue("Code")) {
                        case 4:
                            Toast.makeText(AirConditioningActivity.this, getString(R.string.tips_4), Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            Toast.makeText(AirConditioningActivity.this, getString(R.string.tips_5), Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(AirConditioningActivity.this, getString(R.string.tips_6), Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(AirConditioningActivity.this, getString(R.string.tips_7), Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            Toast.makeText(AirConditioningActivity.this, getString(R.string.tips_8), Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            Toast.makeText(AirConditioningActivity.this, "Unknown Info", Toast.LENGTH_SHORT).show();
                            break;
                    }
                } else {
                    Toast.makeText(AirConditioningActivity.this, intent.getStringExtra("message"), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }
    };
    List<ARCModel> kfids;
    FrameLayout display_camera_parent; //显示绑定给摄像头
    ImageView display_air_img;
    XMFragment mXMFragment;
    CameraInfo mCameraInfo;
    private boolean mNeedDisplaySelect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mNeedDisplaySelect = getIntent().getBooleanExtra("need_display_select", false);
            deviceId = getIntent().getLongExtra("did", 0);
            mModel = getIntent().getParcelableExtra("content_info");
            is_modelist = getIntent().getBooleanExtra("is_modelist", false);
            if (is_modelist) {
                kfids = getIntent().getParcelableArrayListExtra("kfids");
            } else {
                bipc = getIntent().getStringExtra("bipc");
            }
        } else {
            mNeedDisplaySelect = savedInstanceState.getBoolean("need_display_select", false);
            deviceId = savedInstanceState.getLong("did");
            mModel = savedInstanceState.getParcelable("content_info");
            is_modelist = savedInstanceState.getBoolean("is_modelist", false);
            if (is_modelist) {
                kfids = savedInstanceState.getParcelableArrayList("kfids");
            } else {
                bipc = savedInstanceState.getString("bipc");
            }
        }
        initView();
        setTitle(mModel.getRcName());
        requestData();
        //初始化支持的按键操作,这个不能采用国际化，需要与会连指控后台保持一致
        keys.put(R.id.img_shut_btn, "电源");
        keys.put(R.id.img_type_speed, "运作模式");
        keys.put(R.id.add_temprature_img, "温度+");
        keys.put(R.id.sub_temprature_img, "温度-");
        keys.put(R.id.imag_speed_speed, "风速");
        keys.put(R.id.img_wind_direc, "风向");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        intentFilter.addAction(Actions.SHOW_SERVER_MESSAGE);
        registerReceiver(mReceiver, intentFilter);

        initRemoteCount();

        if (bipc != null) {
            JavaThreadPool.getInstance().excute(new Runnable() {
                @Override
                public void run() {
                    DeviceInfo deviceInfo = DatabaseOperator.getInstance().queryDeviceInfo(deviceId);
                    ZhujiInfo zhujiInfo = DatabaseOperator.getInstance().queryDeviceZhuJiInfo(deviceInfo.getZj_id());
                    if (!zhujiInfo.isAdmin()) {
                        return;
                    }
                    List<ZhujiInfo> zhujiInfos = DatabaseOperator.getInstance().queryAllZhuJiInfos();
                    for (ZhujiInfo temp : zhujiInfos) {
                        if (temp.getCa().equals("sst") && temp.getCameraInfo().getC().equals(CameraInfo.CEnum.xiongmai.value())
                                && deviceInfo.getBipc().equals(String.valueOf(temp.getCameraInfo().getIpcid()))) {
                            mCameraInfo = temp.getCameraInfo();
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mXMFragment = XMFragment.newInstance(mCameraInfo);
                                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                                    fragmentTransaction.add(R.id.display_camera_parent, mXMFragment);
                                    fragmentTransaction.commit();
                                    display_camera_parent.setVisibility(View.VISIBLE);
                                }
                            });
                            break;
                        }
                    }
                }
            });
        }
    }

    int currentRemoteControl = 0;

    void initRemoteCount() {
        if (!is_modelist || kfids == null) {
            return;
        }
        qiehuan_view.setVisibility(View.VISIBLE);
        //设置数量是否显示
        if (mNeedDisplaySelect) {
            tv_remote_count.setVisibility(View.VISIBLE);
        }
        for (int i = 0; i < kfids.size(); i++) {
            if (mModel.getKfId().equals(kfids.get(i).getKfId())) {
                currentRemoteControl = i;
                break;
            }
        }
        currentRemoteControl++;
        tv_remote_count.setText(String.valueOf(currentRemoteControl) + "/" + kfids.size());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!is_modelist) {
            getMenuInflater().inflate(R.menu.zc_air_delete_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //删除当前空调遥控器
            case R.id.air_delete:
                new AlertView(getString(R.string.deviceslist_server_leftmenu_deltitle),
                        getString(R.string.deviceslist_server_leftmenu_delmessage),
                        getString(R.string.deviceslist_server_leftmenu_delcancel),
                        new String[]{getString(R.string.deviceslist_server_leftmenu_delbutton)}, null,
                        mContext, AlertView.Style.Alert,
                        new OnItemClickListener() {
                            @Override
                            public void onItemClick(Object o, final int position) {
                                if (position != -1) {
                                    JSONObject jsonObject = new JSONObject();
                                    jsonObject.put("did", deviceId);
                                    jsonObject.put("id", mModel.getLocalServereid());
                                    new HttpAsyncTask(AirConditioningActivity.this, IR_REMOTE_DELETE_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,jsonObject);
                                }
                            }
                        }).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            //溢出所有的Handler消息
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mReceiver != null) {
            //解绑注册
            unregisterReceiver(mReceiver);
        }
    }

    GeneralHttpTask mTask;

    void requestData() {
        //获取当前遥控器支持的按钮操作
        mTask = new GeneralHttpTask(this, GET_BRAND_KEY_LIST_URL);
        //请求数据
        Map<String, String> msp = new HashMap<>();
        msp.put("mac", ZCIRRemoteList.CURRENT_IR_MAC_VALUE);
        msp.put("kfid", mModel.getKfId());
        operateFlag = 2;
        mTask.execute(msp);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("need_display_select", mNeedDisplaySelect);
        outState.putLong("did", deviceId);
        outState.putParcelable("content_info", mModel);
        if (is_modelist) {
            outState.putParcelableArrayList("kfids", (ArrayList<? extends Parcelable>) kfids);
        } else {
            outState.putString("bipc", bipc);
        }
        outState.putBoolean("is_modelist", is_modelist);
        super.onSaveInstanceState(outState);
    }

    //当前活动显示的布局
    @Override
    public int setLayoutId() {
        return R.layout.air_conditioning_activity_layout;
    }

    private void initView() {
        img_wind_direc = findViewById(R.id.img_wind_direc);//风向
        imag_speed_speed = findViewById(R.id.imag_speed_speed);//风速
        img_type_speed = findViewById(R.id.img_type_speed);//模式
        img_shut_btn = findViewById(R.id.img_shut_btn);//电源
        add_temprature_img = findViewById(R.id.add_temprature_img);//温度加
        sub_temprature_img = findViewById(R.id.sub_temprature_img);//温度减
        temper_tv = findViewById(R.id.temper_tv);
        auto_tv = findViewById(R.id.auto_tv);
        wind_direc_auto_tv = findViewById(R.id.wind_direc_auto_tv);
        wind_speed_auto_tv = findViewById(R.id.wind_speed_auto_tv);
        indication_btn = findViewById(R.id.indication_btn);
        auto_case_tv = findViewById(R.id.auto_case_tv);
        qiehuan_view = findViewById(R.id.qiehuan_parent);
        tv_remote_count = findViewById(R.id.tv_remote_count);
        display_camera_parent = findViewById(R.id.display_camera_parent);
        display_air_img = findViewById(R.id.display_air_img);

        //事件
        img_type_speed.setOnClickListener(this);
        img_wind_direc.setOnClickListener(this);
        img_shut_btn.setOnClickListener(this);
        imag_speed_speed.setOnClickListener(this);
        add_temprature_img.setOnClickListener(this);
        sub_temprature_img.setOnClickListener(this);
    }

    int keyid = -1;

    //发送控制
    @Override
    public void onClick(View v) {
        if (!stateSuccess) {
            ToastTools.short_Toast(this, getResources().getString(R.string.hwzf_get_remote_fail));
            return;
        } else if (!isOpen && v.getId() != R.id.img_shut_btn) {
            ToastTools.short_Toast(this, getResources().getString(R.string.hwzf_close_power_tips));
            return;
        }
        operateFlag = 3;
        Map<String, String> msp = new HashMap<>();
        msp.put("mac", ZCIRRemoteList.CURRENT_IR_MAC_VALUE);
        msp.put("kfid", mModel.getKfId());
        switch (v.getId()) {
            //模式
            case R.id.img_type_speed:
                keyid = ids.indexOf(keys.get(R.id.img_type_speed));
                break;
            //风向
            case R.id.img_wind_direc:
                keyid = ids.indexOf(keys.get(R.id.img_wind_direc));
                break;
            //电源
            case R.id.img_shut_btn:
                keyid = ids.indexOf(keys.get(R.id.img_shut_btn));
                break;
            //风速
            case R.id.imag_speed_speed:
                keyid = ids.indexOf(keys.get(R.id.imag_speed_speed));
                break;
            //温度加
            case R.id.add_temprature_img:
                keyid = ids.indexOf(keys.get(R.id.add_temprature_img));
                break;
            //温度减
            case R.id.sub_temprature_img:
                keyid = ids.indexOf(keys.get(R.id.sub_temprature_img));
                break;
        }
        //申请按键数据
        msp.put("keyid", keyid + "");
        new GeneralHttpTask(this, GET_BRAND_KEY_EVENT_URL).execute(msp);
    }


    //上一个遥控器
    public void lastRemoteControl(View v) {
        if (kfids == null || currentRemoteControl == 1) {
            return;
        }
        int temp = currentRemoteControl - 1;
        operateFlag = 5;
        getRemoteStatusRequest(temp);
    }

    //下一个遥控器
    public void nextRemoteControl(View v) {
        if (kfids == null || currentRemoteControl == kfids.size()) {
            if (kfids.size() > 0 && currentRemoteControl == kfids.size()) {
                ToastTools.short_Toast(getApplicationContext(), getString(R.string.hwzf_last_type_tip));
            }
            return;
        }
        int temp = currentRemoteControl - 1;//小心数组越界
        operateFlag = 6;
        getRemoteStatusRequest(temp);
    }

    //请求index型号的按键状态
    void getRemoteStatusRequest(int index) {
        Map<String, String> msp = new HashMap<>();
        msp.put("mac", ZCIRRemoteList.CURRENT_IR_MAC_VALUE);
        msp.put("kfid", kfids.get(index).getKfId());
        new GeneralHttpTask(this, GET_BRAND_KEY_EVENT_URL).execute(msp);
    }

    //选择，将型号与主机进行绑定
    public void selectRemoteControl(View v) {
        //绑定设备
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("did", String.valueOf(deviceId));
        jsonObject.put("codeId", mModel.getKfId());
        jsonObject.put("source", "hlzk");
        jsonObject.put("tname", "kt");
        jsonObject.put("bname", mModel.getParentName());
        jsonObject.put("type", mModel.getRcName());
        jsonObject.put("zip", 0);
        jsonObject.put("v", 1);
        new HttpAsyncTask(this, HttpAsyncTask.IR_REMOTE_ADD_URL_FLAG).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,jsonObject);
    }

    //请求的回调结果
    @Override
    public void getRequestResult(String results) {
        if (TextUtils.isEmpty(results)) {
            error(getResources().getString(R.string.request_timeout));
        } else {
            JSONObject jsonObject;
            try {
                jsonObject = JSONObject.parseObject(results);
            } catch (JsonParseException e) {
                Log.e("AirConditioningActivity", "JSON数据错误");
                error(getResources().getString(R.string.hwzf_server_data_error));
                return;
            }
            if (operateFlag == 1) {
                stateSuccess = true;
                initViewState(jsonObject);
            } else if (operateFlag == 2) {
                //获取每一个键的id
                getButtonId(jsonObject);
                //请求空调支持的按键成功后，获取当前遥控器的状态
                //请求数据
                Map<String, String> msp = new HashMap<>();
                msp.put("mac", ZCIRRemoteList.CURRENT_IR_MAC_VALUE);
                msp.put("kfid", mModel.getKfId());
                operateFlag = 1;
                new GeneralHttpTask(this, GET_BRAND_KEY_EVENT_URL).execute(msp);
            } else if (operateFlag == 3) {
                //发送给红外设备,之前更新当前遥控器状态
                initViewState(jsonObject);
                //发送给红外设备
                sendCommandToIR(jsonObject);

            } else if (operateFlag == 5) {
                currentRemoteControl--;
                initViewState(jsonObject);
            } else if (operateFlag == 6) {
                currentRemoteControl++;
                initViewState(jsonObject);
            }
        }
    }

    private void sendCommandToIR(JSONObject jsonObject) {
        //发送数据至红外设备
        String irData = jsonObject.getString("irdata");//码值
        JSONObject object = new JSONObject();
        object.put("eid", String.valueOf(0));
        object.put("source", "hlzk");
        object.put("code", irData);
        object.put("name", ids.get(keyid));
        showProgress("");//显示进度条
        mHandler.sendEmptyMessageDelayed(SEND_TIMEOUT, 15 * 1000);//15秒后超时
        SyncMessage message1 = new SyncMessage();
        message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
        message1.setDeviceid(deviceId);// 红外转发器的ID
        try {
            message1.setSyncBytes(object.toJSONString().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        SyncMessageContainer.getInstance().produceSendMessage(message1);
    }

    //获取按键id
    List<String> ids = new ArrayList<>();

    void getButtonId(JSONObject jsonObject) {
        JSONArray array = jsonObject.getJSONArray("keylist");
        for (int i = 0; i < array.size(); i++) {
            ids.add(array.getString(i));
        }
    }

    boolean isOpen = false;

    //获取到按键值，初始化View的状态
    private void initViewState(JSONObject result) {

        if (operateFlag == 5 || operateFlag == 6) {
            tv_remote_count.setText(String.valueOf(currentRemoteControl) + "/" + kfids.size());
            mModel = kfids.get(currentRemoteControl - 1);
            setTitle(mModel.getRcName());
        }

        temper_tv.setText(result.getString("ctemp") + "℃");//温度
        auto_tv.setText(result.getString("cmode"));//模式
        wind_direc_auto_tv.setText(result.getString("cwinddir"));
        wind_speed_auto_tv.setText(result.getString("cwind"));
        //电源
        String temp = result.getString("conoff");
        if (temp.equals("开")) {
            isOpen = true;
//            indication_btn.setBackgroundColor(getResources().getColor(R.color.lime));
            indication_btn.setImageResource(R.drawable.power_status_open);
        } else {
            isOpen = false;
//            indication_btn.setBackgroundColor(getResources().getColor(R.color.colorGray));
            indication_btn.setImageResource(R.drawable.power_status_close);
        }

        if (auto_tv.getText().toString().equals("除湿")) {
            auto_case_tv.setImageResource(R.drawable.ic_icon_air_chushi);
        } else if (auto_tv.getText().toString().equals("制冷")) {
            auto_case_tv.setImageResource(R.drawable.ic_icon_iar_zhileng);
        } else if (auto_tv.getText().toString().equals("自动")) {
            auto_case_tv.setImageResource(R.drawable.ic_icon_air_zidong);
        } else if (auto_tv.getText().toString().equals("制热")) {
            auto_case_tv.setImageResource(R.drawable.ic_icon_air_zhire);
        } else if (auto_tv.getText().toString().equals("送风")) {
            auto_case_tv.setImageResource(R.drawable.ic_icon_air_songfeng);
        }
    }

    @Override
    public void setResult(int flag, String result) {
        if (flag == HttpAsyncTask.IR_REMOTE_ADD_URL_FLAG) {
            if (result.equals("0")) {
                //请求成功
                Intent intent = new Intent();
                intent.setClass(this, DeviceMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        } else if (flag == IR_REMOTE_DELETE_URL_FLAG && result.equals("0")) {
            finish();
        }
    }
}
