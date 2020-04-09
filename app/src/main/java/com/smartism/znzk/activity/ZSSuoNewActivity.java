package com.smartism.znzk.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.DeviceCommandHistoryActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;

import java.util.List;


/**
 * Created by win7 on 2016/12/27.
 */

public class ZSSuoNewActivity extends FragmentParentActivity implements View.OnClickListener {
    private static final long DELAYTIME = 12 * 1000;
    private TextView tv_jiixn, d_where, d_type, d_name, tv_title;
    private SwipeRefreshLayout refreshLayout;
    private Context mContext;
    private ImageView iv_annima, iv_suo;
    private DeviceInfo deviceInfo;
    private String deviceId;
    private boolean suoflag;

    private TextView tv_dianya, tv_conn_status;
    private LinearLayout ll_his, ll_number, ll_lock;
    private ImageView iv_lock;
    private TextView tv_lock;
    private TextView tv_order, tv_ing;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 2) {
                List<CommandInfo> commandInfos = (List<CommandInfo>) msg.obj;
                if (commandInfos != null && commandInfos.size() > 0) {
                    cancelInProgress();

                    for (CommandInfo info : commandInfos) {
                        if (info.getCtype().equals("44")) {
                            if (info.getCommand().equals("0")) {
                                suoModeFlag = true;
                            } else {
                                suoModeFlag = false;
                            }
                        }
                    }
                    for (int i = 0; i < commandInfos.size(); i++) {
                        if (commandInfos.get(i).getCtype().equals("42")) { //机芯码
                            tv_jiixn.setText(commandInfos.get(i).getCommand());
                        } else if (commandInfos.get(i).getCtype().equals("50")) { //开关状态
                            handler.removeMessages(10);
                            if (commandInfos.get(i).getCommand().equals("49")) {
                                iv_suo.setImageResource(R.drawable.zhzj_close);
                                tv_order.setText("已关锁");
                                isOpen = false;
                            } else if (commandInfos.get(i).getCommand().equals("48")) {
                                iv_suo.setImageResource(R.drawable.zhzj_open);
                                tv_order.setText("已开锁");
                                isOpen = true;
                            }
                            if (suoModeFlag) {
                                iv_suo.setImageResource(R.drawable.zhzj_close);
                                tv_order.setText("已关锁");
                                isOpen = false;
                            }
                            iv_suo.setEnabled(true);
                            iv_annima.setEnabled(true);
                        } else if (commandInfos.get(i).getCtype().equals("51")) { //禁止允许开锁状态
                            if (commandInfos.get(i).getCommand().equals("11")) {
                                suoflag = false;
                            } else if (commandInfos.get(i).getCommand().equals("12")) {
                                suoflag = true;
                            }
                        }
                    }
                    tv_lock.setText(suoModeFlag ? getString(R.string.zss_auto) : getString(R.string.zss_sd));
                    iv_lock.setImageResource(suoModeFlag ? R.drawable.zhzj_lock_locking : R.drawable.zhzj_lock_manual);
                }
                cancelInProgress();
            } else if (msg.what == 3) {
                isOpen = false;
                iv_suo.setVisibility(View.VISIBLE);
                iv_suo.setImageResource(R.drawable.zhzj_close);
                iv_suo.setEnabled(true);
                iv_annima.setEnabled(true);
                tv_order.setText("已关锁");
            } else if (msg.what == 10) {
                cancelInProgress();
                handler.removeMessages(10);
                if (handler.hasMessages(12))
                    handler.removeMessages(12);
                if (operatingAnim != null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    iv_annima.clearAnimation();
                    iv_annima.setVisibility(View.GONE);
                    iv_annima.setEnabled(true);
                }
                iv_suo.setEnabled(true);
                tv_ing.setVisibility(View.GONE);
                iv_suo.setVisibility(View.VISIBLE);
                if (isOpen) {
                    tv_order.setText("已开锁");
                    iv_suo.setImageResource(R.drawable.zhzj_open);
                } else {
                    tv_order.setText("已关锁");
                    iv_suo.setImageResource(R.drawable.zhzj_close);
                }
            } else if (msg.what == 11) {
                handler.removeMessages(11);
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                }
            } else if (msg.what == 12) {
                if (isOpen) {
                    tv_ing.setText(textsClose[textIndex]);
                } else {
                    tv_ing.setText(texts[textIndex]);
                }
                textIndex++;
                if (textIndex == 4)
                    textIndex = 0;
                handler.sendEmptyMessageDelayed(12, 500);

            }
            return false;
        }
    };
    private Handler handler = new WeakRefHandler(mCallback);

    private Animation operatingAnim;
    private boolean suoModeFlag = true;

    private int mode = 0;
    private String[] texts, textsClose;
    private int textIndex = 0;

    private boolean isOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zssuo_new_primary);
        mContext = this;
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        initView();
        initEvent();
        initData();
    }

    private void initEvent() {
//        String masterID = dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, "");
        String masterID = ZhujiListFragment.getMasterId() ;
        if (masterID != null) {
            zhuji = DatabaseOperator.getInstance(mContext)
                    .queryDeviceZhuJiInfo(masterID);
            if (zhuji != null && zhuji.isOnline()) {
                tv_conn_status.setText(getString(R.string.zss_blow_normal));
                tv_conn_status.setTextColor(getResources().getColor(R.color.zss_text_black));
            } else {
                tv_conn_status.setText(getString(R.string.zss_item_exception));
                tv_conn_status.setTextColor(getResources().getColor(R.color.viewfinder_laser));
            }
        }
        d_type.setText(deviceInfo.getType());
        d_name.setText(deviceInfo.getName());
        tv_title.setText(deviceInfo.getName());
        d_where.setText(deviceInfo.getWhere());

        tv_dianya.setText(deviceInfo.isLowb() ? getString(R.string.zss_blow_blow) : getString(R.string.zss_blow_normal));
        tv_dianya.setTextColor(deviceInfo.isLowb() ?getResources().getColor(R.color.viewfinder_laser) : getResources().getColor(R.color.zss_text_black));
        iv_suo.setOnClickListener(this);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SyncMessage message = new SyncMessage();
                handler.sendEmptyMessageDelayed(11, DELAYTIME);
                message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                message.setDeviceid(deviceInfo.getId());
                message.setSyncBytes(new byte[]{0x01});
                SyncMessageContainer.getInstance().produceSendMessage(message);
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        filter.addAction(Actions.SHOW_SERVER_MESSAGE);
        filter.addAction(Actions.REFRESH_DEVICES_LIST);
        registerReceiver(receiver, filter);

    }

    private void initData() {
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new InitDeviceCommand());
    }

    private void initView() {
        texts = new String[]{"正在开锁", "正在开锁.", "正在开锁..", "正在开锁..."};
        textsClose = new String[]{"正在关锁", "正在关锁.", "正在关锁..", "正在关锁..."};
        tv_order = (TextView) findViewById(R.id.tv_order);
        tv_ing = (TextView) findViewById(R.id.tv_ing);
        ll_his = (LinearLayout) findViewById(R.id.ll_his);
        ll_number = (LinearLayout) findViewById(R.id.ll_number);
        ll_lock = (LinearLayout) findViewById(R.id.ll_lock);
        ll_his.setOnClickListener(this);
        ll_number.setOnClickListener(this);
        ll_lock.setOnClickListener(this);

        iv_lock = (ImageView) findViewById(R.id.iv_lock);
        tv_lock = (TextView) findViewById(R.id.tv_lock);

        d_where = (TextView) findViewById(R.id.d_where);
        d_type = (TextView) findViewById(R.id.d_type);
        d_name = (TextView) findViewById(R.id.d_name);
        tv_title = (TextView) findViewById(R.id.tv_home);
        iv_suo = (ImageView) findViewById(R.id.iv_suo);
        iv_annima = (ImageView) findViewById(R.id.iv_an);
        tv_jiixn = (TextView) findViewById(R.id.tv_jixin);
        tv_conn_status = (TextView) findViewById(R.id.tv_conn_status);
        tv_dianya = (TextView) findViewById(R.id.tv_dianya);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
    }

    private boolean conn_flag;
    private ZhujiInfo zhuji;
    private android.content.BroadcastReceiver receiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Actions.ACCETP_ONEDEVICE_MESSAGE.equals(intent.getAction())) { // 某一个设备的推送广播
                deviceId = intent.getStringExtra("device_id");
//                String masterID = dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, "");
                String masterID = ZhujiListFragment.getMasterId() ;
                if (masterID != null) {
                    zhuji = DatabaseOperator.getInstance(mContext)
                            .queryDeviceZhuJiInfo(masterID);
                    if (zhuji.isOnline()) {
                        tv_conn_status.setText(getString(R.string.zss_blow_normal));
                        tv_conn_status.setTextColor(getResources().getColor(R.color.zss_text_black));
                    } else {
                        tv_conn_status.setText(getString(R.string.zss_item_exception));
                        tv_conn_status.setTextColor(getResources().getColor(R.color.viewfinder_laser));
                    }
                }
                if (deviceId != null && deviceId.equals(String.valueOf(deviceInfo.getId()))) {
                    String data = (String) intent.getSerializableExtra("device_info");
                    if (data != null) {
//                        conn_flag = true;
                        JSONObject object = JSONObject.parseObject(data);
                        if (object.containsKey("dt")) {
                            if (object.getIntValue("dt") == 50) {
                                iv_annima.clearAnimation();
                                iv_annima.setVisibility(View.GONE);
                                if (object.containsKey("deviceCommand")) {
                                    if (object.getString("deviceCommand").equals("48")) {

                                        if (suoModeFlag == true) {
                                            if (handler.hasMessages(10)) {
                                                handler.removeMessages(10);
                                            }
                                            handler.sendEmptyMessageDelayed(3, 5000);//5秒之后 锁自动关闭
                                        } else {
                                            iv_suo.setEnabled(true);
                                            iv_annima.setEnabled(true);
                                            if (isOpen) {
                                                //手动闭锁模式下，关锁会先接收开锁指令，这里超时不取消
                                                return;
                                            } else {
                                                if (handler.hasMessages(10)) {
                                                    handler.removeMessages(10);
                                                }
                                            }
                                        }
                                        handler.removeMessages(12);
                                        iv_suo.setVisibility(View.VISIBLE);
                                        iv_suo.setImageResource(R.drawable.zhzj_open);
                                        tv_ing.setVisibility(View.GONE);
                                        tv_order.setText("已开锁");
                                        isOpen = true;
                                    } else if (object.getString("deviceCommand").equals("49")) {
                                        if (handler.hasMessages(10)) {
                                            handler.removeMessages(10);
                                        }
                                        iv_suo.setVisibility(View.VISIBLE);
                                        tv_ing.setVisibility(View.GONE);
                                        handler.removeMessages(12);
                                        tv_order.setText("已关锁");
                                        iv_suo.setImageResource(R.drawable.zhzj_close);
                                        iv_suo.setEnabled(true);
                                        iv_annima.setEnabled(true);
                                        isOpen = false;
                                    }
                                }
                                if (refreshLayout.isRefreshing()) {
                                    refreshLayout.setRefreshing(false);
                                }
                            } else if (object.getIntValue("dt") == 42) {
                                if (handler.hasMessages(10)) {
                                    handler.removeMessages(10);
                                }
                                tv_jiixn.setText(object.getString("deviceCommand"));
                            } else if (object.getIntValue("dt") == 44) {
                                if (handler.hasMessages(10)) {
                                    handler.removeMessages(10);
                                }
                                if (object.getString("deviceCommand").equals("0")) {
                                    suoModeFlag = true;
                                } else {
                                    suoModeFlag = false;
                                }
                            }
                            tv_lock.setText(suoModeFlag ? getString(R.string.zss_auto) : getString(R.string.zss_sd));
                            iv_lock.setImageResource(suoModeFlag ? R.drawable.zhzj_lock_locking : R.drawable.zhzj_lock_manual);
                        }
                    } else {
                        deviceInfo = DatabaseOperator.getInstance(mContext).queryDeviceInfo(deviceInfo.getId());
                        if (handler.hasMessages(10)) {
                            handler.removeMessages(10);
                        }
                        if (deviceInfo != null) {
                            cancelInProgress();
                            tv_dianya.setText(deviceInfo.isLowb() ? getString(R.string.zss_blow_blow) : getString(R.string.zss_blow_normal));
                            tv_dianya.setTextColor(deviceInfo.isLowb() ?getResources().getColor(R.color.viewfinder_laser) : getResources().getColor(R.color.zss_text_black));
                        }
                    }
                } else if (deviceId != null && deviceId.equals(String.valueOf(zhuji.getId()))) {
                    if (zhuji.isOnline()) {
                        tv_conn_status.setText(getString(R.string.zss_blow_normal));
                        tv_conn_status.setTextColor(getResources().getColor(R.color.zss_text_black));
                    } else {
                        tv_conn_status.setText(getString(R.string.zss_item_exception));
                        tv_conn_status.setTextColor(getResources().getColor(R.color.viewfinder_laser));
                    }
                }
            } else if (Actions.REFRESH_DEVICES_LIST.equals(intent.getAction())) {
                List<CommandInfo> infos = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());
                if (infos != null && infos.size() > 0) {
                    for (CommandInfo info : infos) {
                        if (info.getCtype().equals("44")) {
                            if (info.getCommand().equals("0")) {
                                suoModeFlag = true;
                            } else {
                                suoModeFlag = false;
                            }
                            tv_lock.setText(suoModeFlag ? getString(R.string.zss_auto) : getString(R.string.zss_sd));
                            iv_lock.setImageResource(suoModeFlag ? R.drawable.zhzj_lock_locking : R.drawable.zhzj_lock_manual);
                        }
                    }
                }
            } else if (Actions.SHOW_SERVER_MESSAGE.equals(intent.getAction())) { // 显示服务器信息
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    cancelInProgress();
                    handler.removeMessages(10);
                }
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(intent.getStringExtra("message"));
                } catch (Exception e) {
                    Log.w("DevicesList", "获取服务器返回消息，转换为json对象失败，用原始值处理");
                }
                if (resultJson != null) {
                    switch (resultJson.getIntValue("Code")) {
                        case 4:
                            Toast.makeText(ZSSuoNewActivity.this, getString(R.string.tips_4), Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            Toast.makeText(ZSSuoNewActivity.this, getString(R.string.tips_5), Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(ZSSuoNewActivity.this, getString(R.string.tips_6), Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(ZSSuoNewActivity.this, getString(R.string.tips_7), Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            Toast.makeText(ZSSuoNewActivity.this, getString(R.string.tips_8), Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            Toast.makeText(ZSSuoNewActivity.this, "Unknown Info", Toast.LENGTH_SHORT).show();
                            break;
                    }

                } else {
                    Toast.makeText(ZSSuoNewActivity.this, intent.getStringExtra("message"), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }
    };

    private void refreshData() {
        JavaThreadPool.getInstance().excute(new RefreshDeviceCommand());
    }


    public void back(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_his:
                Intent intent = new Intent(this, DeviceCommandHistoryActivity.class);
                intent.putExtra("device", deviceInfo);
                startActivity(intent);
                break;
            case R.id.ll_lock:
                showInProgress(getString(R.string.loading), false, true);
                JavaThreadPool.getInstance().excute(new SetSuoMode());
                break;
            case R.id.ll_number:
                Intent intent1 = new Intent(this, ZssOpenCloseHistoryActivity.class);
                intent1.putExtra("device", deviceInfo);
                startActivity(intent1);
                break;
            case R.id.iv_suo:
                iv_annima.setVisibility(View.VISIBLE);
                tv_ing.setVisibility(View.VISIBLE);
                handler.sendEmptyMessage(12);
                operatingAnim = AnimationUtils.loadAnimation(mContext, R.anim.tip);
                LinearInterpolator lin = new LinearInterpolator();
                operatingAnim.setInterpolator(lin);
                if (operatingAnim != null) {
                    iv_annima.startAnimation(operatingAnim);
                }
                tv_order.setText("");
                textIndex = 0;


//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//
//                            Thread.sleep(300);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
                iv_annima.setEnabled(false);
                iv_suo.setEnabled(false);
                iv_suo.setVisibility(View.GONE);
                SyncMessage message1 = new SyncMessage();
                message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
                message1.setDeviceid(deviceInfo.getId());
                message1.setSyncBytes(new byte[]{0x06});
                SyncMessageContainer.getInstance().produceSendMessage(message1);
                handler.sendEmptyMessageDelayed(10, DELAYTIME);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }


    private class InitDeviceCommand implements Runnable {
        @Override
        public void run() {
            List<CommandInfo> commandInfos = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());
            boolean havejxm = false;
            if (commandInfos != null && commandInfos.size() > 0) {
                for (CommandInfo c : commandInfos) {
                    if (c.getCtype().equals("42")) { // 机芯码
                        havejxm = true;
                    }
                }
            }
            if (!havejxm) { //无机芯码
                SyncMessage message = new SyncMessage();
                message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                message.setDeviceid(deviceInfo.getId());
                message.setSyncBytes(new byte[]{0x03});
                SyncMessageContainer.getInstance().produceSendMessage(message);
            }
            if (commandInfos != null) {
                Message mssage = handler.obtainMessage(2);
                mssage.obj = commandInfos;
                handler.sendMessage(mssage);
            }
        }
    }

    private class RefreshDeviceCommand implements Runnable {
        @Override
        public void run() {
            deviceInfo = DatabaseOperator.getInstance(mContext).queryDeviceInfo(deviceInfo.getId());
            List<CommandInfo> commandInfos = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());
            Message mssage = handler.obtainMessage(2);
            mssage.obj = commandInfos;
            handler.sendMessage(mssage);
        }
    }

    private class SetSuoMode implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", deviceInfo.getId());
            JSONArray array = new JSONArray();
            JSONObject o = new JSONObject();
            o.put("vkey", 44);
            //zd 0 sd 1
            if (suoModeFlag) {
                mode = 1;//设置 手动
                o.put("value", mode);
            } else {
                mode = 0;//设置 自动
                o.put("value", mode);
            }

            array.add(o);
            object.put("vkeys", array);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/p/set", object, ZSSuoNewActivity.this);

            if (result != null && result.equals("0")) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        if (mode == 1) {
                            suoModeFlag = false;
                        } else {
                            suoModeFlag = true;
                        }
                        tv_lock.setText(suoModeFlag ? getString(R.string.zss_auto) : getString(R.string.zss_sd));
                        iv_lock.setImageResource(suoModeFlag ? R.drawable.zhzj_lock_locking : R.drawable.zhzj_lock_manual);
                        Toast.makeText(ZSSuoNewActivity.this, getString(R.string.success),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(ZSSuoNewActivity.this, getString(R.string.net_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
