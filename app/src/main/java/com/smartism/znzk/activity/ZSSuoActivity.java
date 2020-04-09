package com.smartism.znzk.activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by win7 on 2016/12/27.
 */

public class ZSSuoActivity extends FragmentParentActivity implements View.OnClickListener, AdapterView.OnItemClickListener {
    private static final long DELAYTIME = 12 * 1000;
    private GridView mGridView;
    private MyGridAdapter mGridAdapter;
    //    private int[] menu = {R.drawable.zss_sf_bg, R.drawable.zss_cf_bg, R.drawable.zss_lsjl_bg, R.drawable.zss_ksbh_bg};
    private int[] menu = {R.drawable.zss_lsjl_bg, R.drawable.zss_ksbh_bg, R.drawable.zss_auto};
    //    private String[] titles = {"电压正常", "禁止开锁", "允许开锁", "设防", "撤防", "历史记录"};
    private String[] titles;
    private ArrayList<Map<String, Integer>> mDatas;

    private TextView tv_jiixn, d_where, d_type, d_name, tv_title;
    private SwipeRefreshLayout refreshLayout;
    private Context mContext;
    private ImageView iv_annima, iv_zss_suo;
    private DeviceInfo deviceInfo;
    private String deviceId, zhujiId;
    private boolean suoflag;

    private ImageView iv_dianya, iv_conn_status;
    private TextView tv_dianya, tv_conn_status;

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
                                iv_zss_suo.setImageResource(R.drawable.icon_zss_close);
                            } else if (commandInfos.get(i).getCommand().equals("48")) {
                                iv_zss_suo.setImageResource(R.drawable.icon_zss_open);
                            }
                            if (suoModeFlag) {
                                iv_zss_suo.setImageResource(R.drawable.icon_zss_close);
                            }
                            iv_zss_suo.setEnabled(true);
                            iv_annima.setEnabled(true);
                        } else if (commandInfos.get(i).getCtype().equals("51")) { //禁止允许开锁状态
                            if (commandInfos.get(i).getCommand().equals("11")) {
                                suoflag = false;
                            } else if (commandInfos.get(i).getCommand().equals("12")) {
                                suoflag = true;
                            }
                        }
                    }
                    mGridAdapter.notifyDataSetChanged();
                }
                cancelInProgress();
            } else if (msg.what == 3) {
                iv_zss_suo.setImageResource(R.drawable.icon_zss_close);
                iv_zss_suo.setEnabled(true);
                iv_annima.setEnabled(true);
            } else if (msg.what == 10) {
//                if (conn_flag) {
//                    iv_conn_status.setImageResource(R.drawable.icon_connectio_exception);
//                    tv_conn_status.setText(getString(R.string.zss_item_exception));
//                }
                cancelInProgress();
                handler.removeMessages(10);
                if (operatingAnim != null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    iv_annima.clearAnimation();
                    iv_annima.setVisibility(View.GONE);
                    iv_annima.setEnabled(true);
                    iv_zss_suo.setEnabled(true);
                }

            } else if (msg.what == 11) {
                handler.removeMessages(11);
                if (refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(false);
                }
            }
            return false;
        }
    };
    private Handler handler = new WeakRefHandler(mCallback);

    private Animation operatingAnim;
    private boolean suoModeFlag = true;

    private int mode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zssuo_primary);
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
                iv_conn_status.setImageResource(R.drawable.icon_normal_connection);
                tv_conn_status.setText(getString(R.string.zss_blow_normal));
            } else {
                iv_conn_status.setImageResource(R.drawable.icon_connectio_exception);
                tv_conn_status.setText(getString(R.string.zss_item_exception));
            }
        }
        d_type.setText(deviceInfo.getType());
        d_name.setText(deviceInfo.getName());
        tv_title.setText(deviceInfo.getName());
        d_where.setText(deviceInfo.getWhere());

        tv_dianya.setText(deviceInfo.isLowb() ? getString(R.string.zss_blow_blow) : getString(R.string.zss_blow_normal));
        iv_dianya.setImageResource(deviceInfo.isLowb() ? R.drawable.icon_abnormal_voltage : R.drawable.icon_normal_voltage);
        mDatas = new ArrayList<>();
        HashMap<String, Integer> map = new HashMap<>();
        for (int i = 0; i < titles.length; i++) {
            map.put(titles[i], menu[i]);
            mDatas.add(map);
        }
        mGridAdapter = new MyGridAdapter();
        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnItemClickListener(this);
        iv_zss_suo.setOnClickListener(this);
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
        titles = new String[]{getString(R.string.zss_main_history), getString(R.string.zss_item_open_number), getString(R.string.zss_auto)};
        d_where = (TextView) findViewById(R.id.d_where);
        d_type = (TextView) findViewById(R.id.d_type);
        d_name = (TextView) findViewById(R.id.d_name);
        tv_title = (TextView) findViewById(R.id.tv_home);
        mGridView = (GridView) findViewById(R.id.myGrid);
        mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        iv_zss_suo = (ImageView) findViewById(R.id.iv_zss_suo);
        iv_annima = (ImageView) findViewById(R.id.iv_an);
        tv_jiixn = (TextView) findViewById(R.id.tv_jixin);
        iv_dianya = (ImageView) findViewById(R.id.iv_dianya);
        iv_conn_status = (ImageView) findViewById(R.id.iv_conn_status);
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
                        iv_conn_status.setImageResource(R.drawable.icon_normal_connection);
                        tv_conn_status.setText(getString(R.string.zss_blow_normal));
                    } else {
                        iv_conn_status.setImageResource(R.drawable.icon_connectio_exception);
                        tv_conn_status.setText(getString(R.string.zss_item_exception));
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
                                if (handler.hasMessages(10)) {
                                    handler.removeMessages(10);
                                }
                                if (object.containsKey("deviceCommand")) {
                                    if (object.getString("deviceCommand").equals("48")) {
                                        iv_zss_suo.setImageResource(R.drawable.icon_zss_open);
                                        if (suoModeFlag == true) {
                                            handler.sendEmptyMessageDelayed(3, 5000);//5秒之后 锁自动关闭
                                        } else {
                                            iv_zss_suo.setEnabled(true);
                                            iv_annima.setEnabled(true);
                                        }
                                    } else if (object.getString("deviceCommand").equals("49")) {
                                        iv_zss_suo.setImageResource(R.drawable.icon_zss_close);
                                        iv_zss_suo.setEnabled(true);
                                        iv_annima.setEnabled(true);
                                    }
                                }
                                if (refreshLayout.isRefreshing()) {
                                    refreshLayout.setRefreshing(false);
                                }
                                mGridAdapter.notifyDataSetChanged();
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
                            mGridAdapter.notifyDataSetChanged();
                        }
                    } else {
                        deviceInfo = DatabaseOperator.getInstance(mContext).queryDeviceInfo(deviceInfo.getId());
                        if (handler.hasMessages(10)) {
                            handler.removeMessages(10);
                        }
                        if (deviceInfo != null) {
                            cancelInProgress();
                            tv_dianya.setText(deviceInfo.isLowb() ? getString(R.string.zss_blow_blow) : getString(R.string.zss_blow_normal));
                            iv_dianya.setImageResource(deviceInfo.isLowb() ? R.drawable.icon_abnormal_voltage : R.drawable.icon_normal_voltage);
                            mGridAdapter.notifyDataSetChanged();
                        }
                    }
                } else if (deviceId != null && deviceId.equals(String.valueOf(zhuji.getId()))) {
                    if (zhuji.isOnline()) {
                        iv_conn_status.setImageResource(R.drawable.icon_normal_connection);
                        tv_conn_status.setText(getString(R.string.zss_blow_normal));
                    } else {
                        iv_conn_status.setImageResource(R.drawable.icon_connectio_exception);
                        tv_conn_status.setText(getString(R.string.zss_item_exception));
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
                        }
                    }
                    mGridAdapter.notifyDataSetChanged();
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
                            Toast.makeText(ZSSuoActivity.this, getString(R.string.tips_4), Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            Toast.makeText(ZSSuoActivity.this, getString(R.string.tips_5), Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(ZSSuoActivity.this, getString(R.string.tips_6), Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(ZSSuoActivity.this, getString(R.string.tips_7), Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            Toast.makeText(ZSSuoActivity.this, getString(R.string.tips_8), Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            Toast.makeText(ZSSuoActivity.this, "Unknown Info", Toast.LENGTH_SHORT).show();
                            break;
                    }

                } else {
                    Toast.makeText(ZSSuoActivity.this, intent.getStringExtra("message"), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }
    };

    private void refreshData() {
        JavaThreadPool.getInstance().excute(new RefreshDeviceCommand());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


//        if (position == 1) {
//            handlerData(new byte[]{0x04}, SyncMessage.CommandMenu.rq_control.value());
//        } else if (position == 2) {
//            handlerData(new byte[]{0x05}, SyncMessage.CommandMenu.rq_control.value());
//        } else
//        if (position == 0) {
//            handlerData(new byte[]{0x03}, SyncMessage.CommandMenu.rq_controlRemind.value());
//        } else if (position == 1) {
//            handlerData(new byte[]{0x00}, SyncMessage.CommandMenu.rq_controlRemind.value());
//        } else
        if (position == 2) {
            showInProgress(getString(R.string.loading), false, true);
            JavaThreadPool.getInstance().excute(new SetSuoMode());
        } else if (position == 0) {
            Intent intent = new Intent(this, DeviceCommandHistoryActivity.class);
            intent.putExtra("device", deviceInfo);
            startActivity(intent);
        } else if (position == 1) {
            Intent intent = new Intent(this, ZssOpenCloseHistoryActivity.class);
            intent.putExtra("device", deviceInfo);
            startActivity(intent);
        }
    }

    private void handlerData(byte[] data, int type) {

        SyncMessage message = new SyncMessage();

        message.setDeviceid(deviceInfo.getId());
        showInProgress(getString(R.string.loading), false, true);
        message.setCommand(type);
        message.setSyncBytes(data);
        SyncMessageContainer.getInstance().produceSendMessage(message);
        handler.sendEmptyMessageDelayed(10, DELAYTIME);
    }

    class MyGridAdapter extends BaseAdapter {
//        @Override
//        public boolean isEnabled(int position) {
//            if (position == 0) {
//                return false;
//            }
//            return super.isEnabled(position);
//        }

        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(mContext, R.layout.zss_primary_grid, null);
                holder.iv = (ImageView) convertView.findViewById(R.id.zss_image);

                holder.tv = (TextView) convertView.findViewById(R.id.zss_tv);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.iv.setBackgroundResource(menu[position]);
            holder.tv.setText(titles[position]);
            setTitle(holder, position);
            return convertView;
        }

        private void setTitle(ViewHolder holder, int position) {
            if (position == 2) {
                holder.tv.setText(suoModeFlag ? getString(R.string.zss_auto) : getString(R.string.zss_sd));
                holder.iv.setImageResource(suoModeFlag ? R.drawable.zss_auto : R.drawable.zss_sd);
            }
//            if (position == 0) {
//                holder.tv.setText(deviceInfo.isLowb() ? getString(R.string.zss_blow_blow) : getString(R.string.zss_blow_normal));
//                holder.iv.setImageResource(deviceInfo.isLowb() ? R.drawable.zss_dianya_blow : R.drawable.zss_dianya_normal);
//            }
//            else if (position == 1) {
//                if (!suoflag) {
//                    holder.iv.setBackgroundResource(R.drawable.zss_jzks_over);
//                }
//            } else if (position == 2) {
//                if (suoflag) {
//                    holder.iv.setBackgroundResource(R.drawable.zss_yxks_over);
//                }
//            }
//            else
//            if (position == 0) {
//                if (deviceInfo.getAcceptMessage() == 3) {
//                    holder.iv.setBackgroundResource(R.drawable.icon_fortification_mode);
//                }
//            } else if (position == 1) {
//                if (deviceInfo.getAcceptMessage() == 0) {
//                    holder.iv.setBackgroundResource(R.drawable.icon_disarm_mode);
//                }
//            }
        }

    }

    class ViewHolder {
        public ImageView iv;
        public TextView tv;
    }

    public void back(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_zss_suo:
                iv_annima.setVisibility(View.VISIBLE);
                operatingAnim = AnimationUtils.loadAnimation(mContext, R.anim.tip);
                LinearInterpolator lin = new LinearInterpolator();
                operatingAnim.setInterpolator(lin);
                if (operatingAnim != null) {
                    iv_annima.startAnimation(operatingAnim);
                }
                iv_annima.setEnabled(false);
                iv_zss_suo.setEnabled(false);
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
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/p/set", object, ZSSuoActivity.this);

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
                        mGridAdapter.notifyDataSetChanged();
                        Toast.makeText(ZSSuoActivity.this, getString(R.string.success),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(ZSSuoActivity.this, getString(R.string.net_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
