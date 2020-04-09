package com.smartism.znzk.activity.yaokan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.yankan.MatchRemoteControl;
import com.smartism.znzk.domain.yankan.MatchRemoteControlResult;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.GetAndDecodeMapString;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.yaokan.YkanIRInterface;
import com.smartism.znzk.util.yaokan.YkanIRInterfaceImpl;
import com.smartism.znzk.view.alertview.AlertView;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 型号Activity
 */
public class YKDetailActivity extends ActivityParentActivity implements View.OnClickListener {

    private static final int QUERYDEVICEINFO = 1;
    private TextView dev_version, title_tv;
    private int sort;
    private TextView tv_sort;
    private YkanIRInterface ykanInterface;
    private HashMap<String, Object> codeLib = new HashMap<>();
    private String tname;
    private String bname;
    private long did;
    private String jsonString;
    private ImageView iv_device;
    private ImageButton iv_power;
    private Button right_btn, left_btn, back, no_btn, yes_btn;

    List<MatchRemoteControl> rs = new ArrayList<MatchRemoteControl>();

    String result;

//    public static boolean flag;

    private MatchRemoteControlResult matchInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_remote_ctrl);

        initView();
        initData();
    }

    public void initData() {
        matchInfo = (MatchRemoteControlResult) getIntent().getSerializableExtra("matchInfo");
        rs = matchInfo.getRs();
        String index = getIntent().getStringExtra("index");
        for (int i = 0; i < rs.size(); i++) {
            if (rs.get(i).getRmodel().contentEquals(index)) {
                sort = i;
                break;
            }
        }

        ykanInterface = new YkanIRInterfaceImpl();
        tname = getIntent().getStringExtra("tname");
        bname = getIntent().getStringExtra("bname");
        did = getIntent().getLongExtra("did", 0);
        tv_sort.setText(((sort + 1) + "/" + rs.size() + getString(R.string.hwzf_fa)));
        dev_version.setText(rs.get(sort).getRmodel().toString());
        title_tv.setText(getString(R.string.hwzf_detail_dz) + " " + tname + getString(R.string.hwzf_detail_kg));

        if (tname.equals(getString(R.string.hwzf_fan_fan))) {
            iv_device.setBackgroundResource(R.drawable.icon_yk_fan);
            iv_power.setImageResource(R.drawable.yk_power);
        } else if (tname.equals(getString(R.string.hwzf_kt))) {
            iv_device.setBackgroundResource(R.drawable.yaokan_ctrl_d_air);
            iv_power.setImageResource(R.drawable.yk_power);
        } else if (tname.equals(getString(R.string.hwzf_tv_tv))) {
            iv_device.setBackgroundResource(R.drawable.icon_yk_tv);
            iv_power.setImageResource(R.drawable.yk_power);
        } else if (tname.equals(getString(R.string.hwzf_tvbox_tvbox))) {
            title_tv.setText(getString(R.string.hwzf_detail_dz) + " " + tname + getString(R.string.hwzf_detail_yl));
            iv_power.setImageResource(R.drawable.tv_plus);
            iv_device.setBackgroundResource(R.drawable.icon_yk_tvbox);
        }


        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        filter.addAction(Actions.FINISH_YK_EXIT);
        registerReceiver(receiver, filter);
    }

    public void initView() {
        left_btn = (Button) findViewById(R.id.left_btn);
        no_btn = (Button) findViewById(R.id.no_btn);
        yes_btn = (Button) findViewById(R.id.yes_btn);
        yes_btn.setOnClickListener(this);
        no_btn.setOnClickListener(this);
        back = (Button) findViewById(R.id.back);
        back.setOnClickListener(this);
        right_btn = (Button) findViewById(R.id.right_btn);
        dev_version = (TextView) findViewById(R.id.dev_version);
        tv_sort = (TextView) findViewById(R.id.tv_sort);
        title_tv = (TextView) findViewById(R.id.title_tv);
        iv_device = (ImageView) findViewById(R.id.iv_device);
        iv_power = (ImageButton) findViewById(R.id.iv_power);
        iv_power.setOnClickListener(this);
        left_btn.setOnClickListener(this);
        right_btn.setOnClickListener(this);

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Actions.ACCETP_ONEDEVICE_MESSAGE)) {
                String result = (String) intent.getSerializableExtra("device_id");
                if (result != null && !TextUtils.isEmpty(result) && result.equals(String.valueOf(did))) {
                    JavaThreadPool.getInstance().excute(new QueryDeviceInfo(Long.parseLong(result)));
                }
            } else if (intent.getAction().equals(Actions.FINISH_YK_EXIT)) {
                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case YkanIRInterfaceImpl.NET_SUCCEES_GETREMOTEDETAILS:
                    codeLib = (HashMap<String, Object>) msg.obj;
                    if (codeLib.containsKey("error")) {
                        cancelInProgress();
                        if (codeLib.get("error") != null) {
                            String error = (String) codeLib.get("error");
                            Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                        }
                    } else if (codeLib != null && codeLib.size() > 1) {
                        upLoad(null);
                    } else {
                        cancelInProgress();
                        if (mHandler.hasMessages(10)) {
                            mHandler.removeMessages(10);
                        }
                        Toast.makeText(getApplicationContext(), R.string.net_error_ioerror, Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    break;
                case QUERYDEVICEINFO:
                    String result = (String) msg.obj;

                    if (tname.equals(getString(R.string.hwzf_kt)) && result.equals("on")) {
                        no_btn.setVisibility(View.VISIBLE);
                        yes_btn.setVisibility(View.VISIBLE);
                        left_btn.setEnabled(false);
                        right_btn.setEnabled(false);
                    } else if ((tname.equals(getString(R.string.hwzf_fan_fan)) || tname.equals(getString(R.string.hwzf_tv_tv))) && result.equals("power")) {
                        no_btn.setVisibility(View.VISIBLE);
                        yes_btn.setVisibility(View.VISIBLE);
                        left_btn.setEnabled(false);
                        right_btn.setEnabled(false);
                    } else if (tname.equals(getString(R.string.hwzf_tvbox_tvbox)) && result.equals("vol+")) {
                        no_btn.setVisibility(View.VISIBLE);
                        yes_btn.setVisibility(View.VISIBLE);
                        left_btn.setEnabled(false);
                        right_btn.setEnabled(false);
                    }
                    break;
                case 10:
                    cancelInProgress();
                    Toast.makeText(YKDetailActivity.this, getString(R.string.time_out), Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    // 上传数据到服务器
    private void upLoad(final String codeString) {
        JavaThreadPool.getInstance().excute(new Runnable() {
            public void run() {
                JSONObject object = new JSONObject();
                object.put("did", did); // 3 要绑定的红外转发器设备id
                object.put("codeId", rs.get(sort).getRid()); // 遥控器ID
                object.put("type", rs.get(sort).getRmodel()); // 5 遥控码遥控器型号
                object.put("zip", 0); // 0不压缩 1压缩
                object.put("v", 1); //  红外遥控版本
                object.put("source", "yk"); // 8 ===来源：yk (遥看)
                String t = "空调";
                if (tname.equals(getString(R.string.hwzf_kt))) {
                    t = "空调";
                } else if (tname.equals(getString(R.string.hwzf_fan_fan))) {
                    t = "风扇";
                } else if (tname.equals(getString(R.string.hwzf_tv_tv))) {
                    t = "电视机";
                } else if (tname.equals(getString(R.string.hwzf_tvbox_tvbox))) {
                    t = "电视机顶盒";
                }
                object.put("tname", t); // 9 ====遥控器种类名称：电视机，空调等
                object.put("bname", bname); // 10 ====遥控器品牌
                int type = 0;
                if (t.equals("空调") || t.equals("风扇")) {
                    type = 1;
                }
                if (t.equals("电视机顶盒") || t.equals("电视机")) {
                    type = 2;
                }
                if (codeString==null){
                    jsonString = new GetAndDecodeMapString().getTvCode(codeLib, type);
                    object.put("rccode", jsonString);// 码的实际内容
                    Util.saveYKCodeToFile(jsonString,rs.get(sort).getName()+rs.get(sort).getRmodel());//保存到本地
                }else{
                    object.put("rccode", codeString);// 码的实际内容
                }
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                try {
                    result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/infr/add",object,false, YKDetailActivity.this);
                    // 如果上传成功后 本地保存
                    if (result != null && result.contentEquals("0")) {
                        cancelInProgress();
                        if (mHandler.hasMessages(10)) {
                            mHandler.removeMessages(10);
                        }
                        Intent intent = new Intent();
//                        String ctrlId = rs.get(sort).getRid();
//                        Bundle mBundel = new Bundle();
//                        mBundel.putString("ctrlId", ctrlId);
//                        mBundel.putString("type", dev_version.getText().toString());
//                        mBundel.putString("brand", bname);
//                        mBundel.putLong("did", did);
//                        mBundel.putString("masterId",dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, ""));
//                        mIntent.putExtras(mBundel);
                        intent.putExtra("did", did);
                        intent.setAction(Actions.FINISH_YK_EXIT);
                        mContext.sendBroadcast(intent);
//                        mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        mIntent.setClass(YKDetailActivity.this, YKDownLoadCodeActivity.class);
//                        startActivity(mIntent);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "上传异常");
                }


            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.left_btn:
                if (sort == 0) {
                    sort = rs.size() - 1;
                } else {
                    sort -= 1;
                }
                dev_version.setText(rs.get(sort).getRmodel());
                tv_sort.setText((sort + 1) + "/" + rs.size() + getString(R.string.hwzf_fa));
                break;
            case R.id.right_btn:
                if (sort == rs.size() - 1) {
                    sort = 0;
                } else {
                    sort += 1;
                }
                dev_version.setText(rs.get(sort).getRmodel());
                tv_sort.setText((sort + 1) + "/" + rs.size() + getString(R.string.hwzf_fa));
                break;
            case R.id.back:
                finish();
                break;
            case R.id.iv_power:
                String onOffCode = "";
                JSONObject obj = new JSONObject();
                HashMap mapPower = null;
                HashMap mapRcCommand;
                mapRcCommand = (HashMap) rs.get(sort).getRcCommand();
                if (tname.equals(getString(R.string.hwzf_kt))) {
                    obj.put("name", "on");
                    mapPower = (HashMap) mapRcCommand.get("on");
                } else if (tname.equals(getString(R.string.hwzf_fan_fan)) || tname.equals(getString(R.string.hwzf_tv_tv))) {
                    obj.put("name", "power");
                    mapPower = (HashMap) mapRcCommand.get("power");
                } else if (tname.equals(getString(R.string.hwzf_tvbox_tvbox))) {
                    obj.put("name", "vol+");
                    mapPower = (HashMap) mapRcCommand.get("vol+");
                }
                if (mapPower != null && mapPower.size() > 1) {
                    if (mapPower.containsKey("src")) {
                        onOffCode = (String) mapPower.get("src");
                        obj.put("code", onOffCode);
                    }
                }

                SyncMessage message1 = new SyncMessage();
                message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
                message1.setDeviceid(did);// 红外转发器的ID
                try {
                    message1.setSyncBytes(obj.toJSONString().getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                SyncMessageContainer.getInstance().produceSendMessage(message1);
                break;
            case R.id.no_btn:
                yes_btn.setVisibility(View.GONE);
                no_btn.setVisibility(View.GONE);
                left_btn.setEnabled(true);
                right_btn.setEnabled(true);
                if (sort == rs.size() - 1) {
                    sort = 0;
                } else {
                    sort += 1;
                }
                dev_version.setText(rs.get(sort).getRmodel());
                tv_sort.setText((sort + 1) + "/" + rs.size() + " " + getString(R.string.hwzf_fa));
                break;
            case R.id.yes_btn:
                new AlertView(getString(R.string.hwzf_detail_sure) + " " + tname + " " + getString(R.string.hwzf_detail_zt), tname + getString(R.string.hwzf_detail_bdhwzf), getString(R.string.cancel),
                        new String[]{getString(R.string.sure)}, null, YKDetailActivity.this, AlertView.Style.Alert,
                        new com.smartism.znzk.view.alertview.OnItemClickListener() {

                            @Override
                            public void onItemClick(Object o, int position) {
                                // 如果遥控器型号为空
                                if (TextUtils.isEmpty(rs.get(sort).getRmodel())) {
//                                    Toast.makeText(getApplicationContext(), "此型号的遥控器正在升级中...", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                if (position != -1) {
                                    showInProgress(getString(R.string.ongoing), false, true);
                                    String codeString = Util.readYKCodeFromFile(rs.get(sort).getName()+rs.get(sort).getRmodel());
                                    if (codeString!=null && codeString.length() > 10){//本地已经有了
                                        upLoad(codeString);
                                    }else{
                                        // 获取此遥控器对应的码库
                                        new Thread(new Runnable() {
                                            public void run() {
                                                ykanInterface.getRemoteDetailsHashMap(YKDetailActivity.this, rs.get(sort).getRid(), tname, mHandler);
                                                mHandler.sendEmptyMessageDelayed(10, 60 * 1000);
                                            }
                                        }).start();
                                    }
                                }
                            }
                        }).show();
                break;
        }
    }


    private class QueryDeviceInfo implements Runnable {
        private long id;

        public QueryDeviceInfo(long result) {
            this.id = result;
        }

        @Override
        public void run() {
            DeviceInfo deviceInfo = DatabaseOperator.getInstance(YKDetailActivity.this).queryDeviceInfo(id);
            Message msg = mHandler.obtainMessage(QUERYDEVICEINFO);
            msg.obj = deviceInfo.getLastCommand();
            mHandler.sendMessage(msg);
        }
    }
}
