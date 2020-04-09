package com.smartism.znzk.activity.yaokan;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.GetAndDecodeMapString;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.SecurityUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;

import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class YKRemoteTypeAirActivity extends ActivityParentActivity {

    private static final int QUERYDEVICEINFO = 1;
    private static final int NEW_DEVICE_CODE = 2;
    private static final int SEND_CODE = 3;
    private TextView tv_tempreture;
    private LinearLayout ll_tempreture;
    private RelativeLayout rl_main_bg, ctrl_top_layout;
    private int tempreture = 26;
    private long did;
    // 存储空调的模式：
    private ArrayList<String> modeList_cn = new ArrayList<String>();
    private ArrayList<String> modeList_en = new ArrayList<String>();

    private CheckBox cb_power_status;
    private ImageView iv_power;
    private String ctrlId;
    private String eid;
    // 空调的模式 温度 开关状态
    private String ctrl_mode;
    private String ctrl_tempr;
    private static final int requestCode = 100;

    private String[] cmd_mode = {"ar", "ah", "aa", "ad", "aw"};//ad抽湿 aw送风 aa自动 ar制冷 ah制热
    private HashMap<String, String> codeHashMap;

    private ImageView iv_ar, iv_ad, iv_aw, iv_ah, iv_aa;

    private TextView tv_ar, tv_ad, tv_aw, tv_ah, tv_aa,   hor_tv,ver_tv;
//    private int[] iv_id = {R.id.iv_ar, R.id.iv_ad, R.id.iv_aw, R.id.iv_ah, R.id.iv_aa};

    private DeviceInfo deviceInfo;

//    private int temp = 0;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case QUERYDEVICEINFO:
                    String mode = (String) msg.obj;
                    handleData(mode);
                    if (mHandler.hasMessages(SEND_CODE)) {
                        mHandler.removeMessages(SEND_CODE);
                    }
                    if (progressIsShowing()) {
                        cancelInProgress();
                        Toast.makeText(YKRemoteTypeAirActivity.this, getString(R.string.rq_control_sendsuccess), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case NEW_DEVICE_CODE:
                    String result = (String) msg.obj;
                    cancelInProgress();
                    JSONObject objInfo = JSONObject.parseObject(result);
                    ncode = objInfo.getString("ncode");
                    nonoff = objInfo.getString("nonoff");
//                    if (TextUtils.isEmpty(nonoff)) {
//                        return;
//                    }
                    if (TextUtils.isEmpty(nonoff)) {
                        nonoff = "on";//未请求到默认设置开
                    }

                    if (nonoff != null && nonoff.equals("on")) {
                        if (TextUtils.isEmpty(ncode)) {
                            iv_power.setImageResource(R.drawable.kt_power_on);
                            power_flag = true;
                            cb_power_status.setChecked(true);
                            tv_tempreture.setText(26 + "");
                            ctrl_mode = "ar";
                            initBg(ctrl_mode);
                        } else {
                            ll_tempreture.setVisibility(View.VISIBLE);
                            if (ncode.length() > 2) {
                                ctrl_mode = ncode.substring(0, 2);
                                tempreture = Integer.parseInt(ncode.substring(2));
                            } else {
                                ctrl_mode = ncode;
                                tempreture = 18;
                            }
                            initBg(ctrl_mode);
                            iv_power.setImageResource(R.drawable.kt_power_on);
                            power_flag = true;
                            cb_power_status.setChecked(true);
                            tv_tempreture.setText(tempreture + "");
                        }

                    } else if (nonoff != null && nonoff.equals("off")) {

                    } else {
                        Toast.makeText(mContext, getString(R.string.net_error_servererror), Toast.LENGTH_SHORT).show();
                    }
//                    else if (ncode != null && nonoff != null) {
//                        if (nonoff.equals("on")) {
//                            ll_tempreture.setVisibility(View.VISIBLE);
//                            if (ncode.length() > 2) {
//                                ctrl_mode = ncode.substring(0, 2);
//                                tempreture = Integer.parseInt(ncode.substring(2));
//                            } else {
//                                ctrl_mode = ncode;
//                                tempreture = 18;
//                            }
//                            initBg(ctrl_mode);
//                            cb_button.setChecked(true);
//                            cb_power_status.setChecked(true);
//                            tv_tempreture.setText(tempreture + "");
//                        }
//                    }
                    break;
                case SEND_CODE:
                    if (progressIsShowing()) {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    private String masterId;
    private TextView tv_type;
    private String type, brand;
    private String code;
    private String ncode;
    private String nonoff;
    private boolean power_flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_type_air);
        initView();
        initData();
        if (getIntent().getStringExtra("ctrlId") == null) {
            Intent mIntent = new Intent();
            mIntent.setClass(YKRemoteTypeAirActivity.this, YKDownLoadCodeActivity.class);
            mIntent.putExtra("did", did);
            mIntent.putExtra("masterId", masterId);
//            startActivityForResult(mIntent, requestCode);
            startActivity(mIntent);
            return;
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initView();
        initData();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Actions.ACCETP_ONEDEVICE_MESSAGE)) {

                String result = (String) intent.getSerializableExtra("device_id");
                if (result != null && result.equals(String.valueOf(did))) {

                    JavaThreadPool.getInstance().excute(new QueryDeviceInfo(Long.parseLong(result)));
                }
            }
        }
    };

    private void handleData(String result) {

        ctrl_mode = result;
        if (result.equals("on")) {
            ll_tempreture.setVisibility(View.VISIBLE);
            ctrl_mode = "ar";
            tempreture = 26;
            cb_power_status.setChecked(true);
            iv_power.setImageResource(R.drawable.kt_power_on);
            power_flag = true;
            JSONObject obj_ar = new JSONObject();
            obj_ar.put("code", codeHashMap.get("ar" + tempreture));
            obj_ar.put("name", "ar" + tempreture);
            sendCmd(obj_ar.toString());
        } else if (result.equals("off")) {
            initBg("-1");
            ll_tempreture.setVisibility(View.GONE);
            cb_power_status.setChecked(false);
            iv_power.setImageResource(R.drawable.kt_power_off);
            power_flag = false;
        } else if (result.equals("aa")) {
            tempreture = 18;
        } else if (result.equals("ad")) {
            tempreture = 18;
        } else if (result.equals("aw")) {
            tempreture = 18;
        } else if (result.contains("ar")) {
            ctrl_mode = "ar";
            tempreture = Integer.parseInt(result.substring(2, result.length()));
        } else if (result.contains("ah")) {
            ctrl_mode = "ah";
            tempreture = Integer.parseInt(result.substring(2, result.length()));
        }
        tv_tempreture.setText(tempreture + "");
        initBg(ctrl_mode);

    }

    public void getCommand() {
        showInProgress(getString(R.string.ongoing), false, true);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                try {
                    code = getYaokanCode(Long.parseLong(eid), did, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!TextUtils.isEmpty(code)) {
                    Message msg = Message.obtain();
                    msg.what = NEW_DEVICE_CODE;
                    msg.obj = code;
                    mHandler.sendMessage(msg);
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.net_error_requestfailed), Toast.LENGTH_SHORT).show();
                        }
                    });
                }


            }
        });
    }

    private String getYaokanCode(long type, long did, Boolean b) {
        String result = null;
        JSONObject object = new JSONObject();
        object.put("did", did);
        object.put("c", b);
        String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");

        object.put("id", type);
        result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/infr/get", object, YKRemoteTypeAirActivity.this);


        return result;
    }

    private void initData() {
        //Bundle[{did=3999, ctrlId=20160602153805}] true
        Bundle mBundel = this.getIntent().getExtras();
        did = mBundel.getLong("did");
        ctrlId = mBundel.getString("ctrlId");
        masterId = mBundel.getString("masterId");
        type = getIntent().getStringExtra("type");
        brand = getIntent().getStringExtra("brand");
        eid = String.valueOf(getIntent().getLongExtra("eid", -1));
        tv_type.setText(brand + " " + type);
        getCommand();
        // 获取本地存储此遥控器的码库
        String mapString = Util.readYKCodeFromFile(brand+type);
        codeHashMap = new GetAndDecodeMapString().getMap(mapString);
        LinkedList<String> mList = new LinkedList<String>(codeHashMap.keySet());


        for (String str : mList
                ) {
            if (str.contains(cmd_mode[0])) {
                iv_ar.setEnabled(true);
            }
            if (str.contains(cmd_mode[1])) {
                iv_ah.setEnabled(true);
            }
            if (str.equals(cmd_mode[2])) {
                iv_aa.setEnabled(true);
            }
            if (str.equals(cmd_mode[3])) {
                iv_ad.setEnabled(true);
            }
            if (str.equals(cmd_mode[4])) {
                iv_aw.setEnabled(true);
            }
        }

        // 初始化背景颜色 空调当前模式下对应的Icon背景显示为绿色 其他为白色背景
        initBg(ctrl_mode);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        registerReceiver(receiver, filter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private void initView() {

        rl_main_bg = (RelativeLayout) findViewById(R.id.rl_main_bg);
        rl_main_bg.setBackgroundResource(R.color.yk_main_bg);
        tv_type = (TextView) findViewById(R.id.tv_type);
        ctrl_top_layout = (RelativeLayout) findViewById(R.id.ctrl_top_layout);
        ctrl_top_layout.setBackgroundResource(R.color.yk_main_bg);
        iv_ar = (ImageView) findViewById(R.id.iv_ar);
        iv_ad = (ImageView) findViewById(R.id.iv_ad);
        iv_aw = (ImageView) findViewById(R.id.iv_aw);
        iv_ah = (ImageView) findViewById(R.id.iv_ah);
        iv_aa = (ImageView) findViewById(R.id.iv_aa);

        tv_ar = (TextView) findViewById(R.id.tv_ar);
        tv_ad = (TextView) findViewById(R.id.tv_ad);
        tv_aw = (TextView) findViewById(R.id.tv_aw);
        tv_ah = (TextView) findViewById(R.id.tv_ah);
        tv_aa = (TextView) findViewById(R.id.tv_aa);
        hor_tv = (TextView) findViewById(R.id.hor_tv);
        ver_tv = (TextView) findViewById(R.id.ver_tv);

        tv_ar.setSelected(true);
        tv_ad.setSelected(true);
        tv_aw.setSelected(true);
        tv_ah.setSelected(true);
        tv_aa.setSelected(true);
        hor_tv.setSelected(true);
        ver_tv.setSelected(true);

        iv_ar.setEnabled(false);
        iv_ah.setEnabled(false);
        iv_ad.setEnabled(false);
        iv_aw.setEnabled(false);
        iv_aa.setEnabled(false);

        ll_tempreture = (LinearLayout) findViewById(R.id.ll_tempreture);


        tv_tempreture = (TextView) findViewById(R.id.tv_tempreture);
        iv_power = (ImageView) findViewById(R.id.iv_power);
        cb_power_status = (CheckBox) findViewById(R.id.cb_power_status);


        // 有指令的显示彩色 无指令的显示灰色
        iv_ar.setImageResource(R.drawable.bg_yaokan_air_ar);
        iv_ah.setImageResource(R.drawable.bg_yaokan_air_ah);
        iv_ad.setImageResource(R.drawable.bg_yaokan_air_ad);
        iv_aw.setImageResource(R.drawable.bg_yaokan_air_aw);
        iv_aa.setImageResource(R.drawable.bg_yaokan_air_aa);

        iv_power.setImageResource(R.drawable.kt_power_off);

    }

    @SuppressLint("NewApi")
    public void onClick(View v) {
        switch (v.getId()) {
            // 模式
            case R.id.iv_aa:
                if (!power_flag) {
                    Toast.makeText(getApplicationContext(), getString(R.string.hwzf_on_off), Toast.LENGTH_SHORT).show();
                    return;
                }
                ctrl_mode = "aa";
                JSONObject obj_aa = new JSONObject();
                obj_aa.put("code", codeHashMap.get("aa"));
                obj_aa.put("name", "aa");
                obj_aa.put("eid", eid);
                sendCmd(obj_aa.toString());
                break;
            case R.id.iv_ar:
                if (!power_flag) {
                    Toast.makeText(getApplicationContext(),  getString(R.string.hwzf_on_off), Toast.LENGTH_SHORT).show();
                    return;
                }
                ctrl_mode = "ar";
//                tempreture = dcsp.getInt(Constant.YAOKAN_LOCAL_TEMPRATURE, 26);
                JSONObject obj_ar = new JSONObject();
                obj_ar.put("code", codeHashMap.get("ar" + tempreture));
                obj_ar.put("name", "ar" + tempreture);
                obj_ar.put("eid", eid);
                sendCmd(obj_ar.toString());

                break;
            case R.id.iv_ah:
                if (!power_flag) {
                    Toast.makeText(getApplicationContext(),  getString(R.string.hwzf_on_off), Toast.LENGTH_SHORT).show();
                    return;
                }
                ctrl_mode = "ah";
//                tempreture = dcsp.getInt(Constant.YAOKAN_LOCAL_TEMPRATURE, 26);
                JSONObject obj_ah = new JSONObject();
                obj_ah.put("code", codeHashMap.get("ah" + tempreture));
                obj_ah.put("name", "ah" + tempreture);
                obj_ah.put("eid", eid);
                sendCmd(obj_ah.toString());
                break;
            case R.id.iv_ad:
                if (!power_flag) {
                    Toast.makeText(getApplicationContext(),  getString(R.string.hwzf_on_off), Toast.LENGTH_SHORT).show();
                    return;
                }
                ctrl_mode = "ad";
                JSONObject obj_ad = new JSONObject();
                obj_ad.put("code", codeHashMap.get("ad"));
                obj_ad.put("name", "ad");
                obj_ad.put("eid", eid);
                sendCmd(obj_ad.toString());

                break;

            case R.id.iv_aw:
                if (!power_flag) {
                    Toast.makeText(getApplicationContext(),  getString(R.string.hwzf_on_off), Toast.LENGTH_SHORT).show();
                    return;
                }

                ctrl_mode = "aw";
                JSONObject obj_aw = new JSONObject();
                obj_aw.put("code", codeHashMap.get("aw"));
                obj_aw.put("name", "aw");
                obj_aw.put("eid", eid);
                sendCmd(obj_aw.toString());

                break;
            // case R.id.tv_air_type:
            // if (!cb_button.isChecked()) {
            // Toast.makeText(getApplicationContext(), "请打开开关键", 0).show();
            // return;
            // }
            // mode++;
            // tv_mode.setText(modeList_cn.get(mode %
            // modeList_cn.size()).toString());
            // ctrl_mode = modeList_cn.get(mode % modeList_cn.size()).toString();
            // Log.e("aaa", getCmd());
            // sendCmd(getCmd());
            // dcsp.putString(Constant.YAOKAN_REMOTE_STATUS + ctrlId, ctrl_mode +
            // "#" + tempreture+"" + "#" + ctrl_switch)
            // .commit();
            // break;
            case R.id.btn_up:
                // 升温
                if (!power_flag) {
                    Toast.makeText(getApplicationContext(),  getString(R.string.hwzf_on_off), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ctrl_mode.equals(cmd_mode[2]) || ctrl_mode.equals(cmd_mode[3]) || ctrl_mode.equals(cmd_mode[4])) {
                    Toast.makeText(YKRemoteTypeAirActivity.this, getString(R.string.hwzf_mode_not_supply), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (tempreture == 30) {
                    tempreture = 30;
                } else {
                    tempreture++;
                }
//                tv_tempreture.setText(tempreture + "");
                ctrl_tempr = tempreture + "";
                JSONObject obj_up = new JSONObject();
                obj_up.put("code", codeHashMap.get(ctrl_mode + tempreture));
                obj_up.put("name", ctrl_mode + tempreture);
                obj_up.put("eid", eid);

                sendCmd(obj_up.toString());
                break;
            case R.id.btn_down:
                // 降温
                if (!power_flag) {
                    Toast.makeText(getApplicationContext(),  getString(R.string.hwzf_on_off), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (ctrl_mode.equals(cmd_mode[2]) || ctrl_mode.equals(cmd_mode[3]) || ctrl_mode.equals(cmd_mode[4])) {
                    Toast.makeText(YKRemoteTypeAirActivity.this, getString(R.string.hwzf_mode_not_supply), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (tempreture == 16) {
                    tempreture = 16;
                } else {
                    tempreture--;
                }
                ctrl_tempr = tempreture + "";
                JSONObject obj_down = new JSONObject();
                obj_down.put("code", codeHashMap.get(ctrl_mode + tempreture));
                obj_down.put("name", ctrl_mode + tempreture);
                obj_down.put("eid", eid);
                sendCmd(obj_down.toString());

                break;

            case R.id.iv_power:
                // 开关
//                showInProgress(getString(R.string.loading), false, true);
                JSONObject obj = new JSONObject();
                if (!power_flag) {
                    obj.put("code", codeHashMap.get("on"));
                    obj.put("eid", eid);
                    obj.put("name", "on");
                    sendCmd(obj.toJSONString());

                } else {
//                    initBg("-1");
                    obj.put("code", codeHashMap.get("off"));
                    obj.put("eid", eid);
                    obj.put("name", "off");
                    sendCmd(obj.toJSONString());
                }

                break;
            case R.id.back:
                Intent intent = new Intent();
                intent.putExtra("result", false);
                setResult(RESULT_OK, intent);
                finish();
            default:
                break;
        }
    }

    public void sendCmd(String cmd) {
        if (StringUtils.isEmpty(cmd)) {
            Log.e(TAG, "红外遥控操作 ： 发送参数为空！！！");
            return;
        }
        // 发送关闭指令
        showInProgress(getString(R.string.loading), false, true);
        SyncMessage message1 = new SyncMessage();
        message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
        message1.setDeviceid(did);// 红外转发器的ID
        try {
            message1.setSyncBytes(cmd.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mHandler.sendEmptyMessageDelayed(SEND_CODE,8 *1000);
        SyncMessageContainer.getInstance().produceSendMessage(message1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (!YKDetailActivity.flag) {
//            finish();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (TextUtils.isEmpty(status)) {
//            tv_tempreture.setText("26");
//            cb_button.setChecked(false);
//            cb_power_status.setChecked(false);
//        } else {
//            String[] arr = status.split("#");
//            ctrl_mode = arr[0];
//            ctrl_tempr = arr[1];
//            if (!StringUtils.equals("null", ctrl_tempr)) {
//                tempreture = Integer.parseInt(ctrl_tempr);
//            }
//            tv_tempreture.setText(StringUtils.equals("null", arr[1]) ? "26" : tempreture + "");
//            cb_button.setChecked(arr[2].equals("on"));
//            cb_power_status.setChecked(arr[2].equals("on"));
//        }
//        initView();
    }


    // 初始化背景
    @SuppressLint("NewApi")
    private void initBg(String ctrl_mode) {
        if (ctrl_mode != null) {
            iv_ar.setBackground(ctrl_mode.contentEquals("ar")
                    ? ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_yaokan_air_blue)
                    : ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_yaokan_air_white));
            iv_ah.setBackground(ctrl_mode.contentEquals("ah")
                    ? ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_yaokan_air_blue)
                    : ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_yaokan_air_white));
            iv_ad.setBackground(ctrl_mode.contentEquals("ad")
                    ? ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_yaokan_air_blue)
                    : ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_yaokan_air_white));
            iv_aw.setBackground(ctrl_mode.contentEquals("aw")
                    ? ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_yaokan_air_blue)
                    : ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_yaokan_air_white));
            iv_aa.setBackground(ctrl_mode.contentEquals("aa")
                    ? ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_yaokan_air_blue)
                    : ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_yaokan_air_white));
        } else {
            iv_ar.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_yaokan_air_white));
            iv_ah.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_yaokan_air_white));
            iv_ad.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_yaokan_air_white));
            iv_aw.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_yaokan_air_white));
            iv_aa.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_yaokan_air_white));
        }
    }

    private class QueryDeviceInfo implements Runnable {
        private long id;

        public QueryDeviceInfo(long result) {
            this.id = result;
        }

        @Override
        public void run() {

            deviceInfo = DatabaseOperator.getInstance(YKRemoteTypeAirActivity.this).queryDeviceInfo(id);
            Message msg = mHandler.obtainMessage(QUERYDEVICEINFO);
            if (deviceInfo != null) {
                msg.obj = deviceInfo.getLastCommand();
                mHandler.sendMessage(msg);
            }
        }
    }
}
