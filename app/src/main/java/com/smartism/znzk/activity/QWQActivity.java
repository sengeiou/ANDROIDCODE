package com.smartism.znzk.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.DeviceCommandHistoryActivity;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceTimerInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.CountDownTimerUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.view.CheckSwitchButton;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;

import org.apache.commons.lang.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by win7 on 2016/12/9.
 */

public class QWQActivity extends ActivityParentActivity implements View.OnClickListener {
    private static final int LASTTIME = 11;
    private Button btn_oneminute, btn_add;
    private CheckSwitchButton switch_morn;
    private TextView tv_morn;
    private Context mContext;
    private DeviceInfo deviceInfo;
    private String deviceId;
    private ImageView iv;
    private ListView mListView;
    private List<DeviceTimerInfo> timerInfos;
    private MyAdapter adapter;
    private boolean status = false;
    private CountDownTimerUtils timer;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    cancelInProgress();
                    timerInfos = (List<DeviceTimerInfo>) msg.obj;
                    if (timerInfos != null && timerInfos.size() > 0) {
                        adapter.notifyDataSetChanged();
                        switch_morn.setChecked((timerInfos.get(0).getStatus() == 0) ? false : true);
                        status = true;
                    }
//                    Log.e("场景", infos.toString() + "....");

                    break;
                case 10:
                    cancelInProgress();
                    T.showShort(mContext, R.string.timeout);
                    break;
                case LASTTIME:
                    cancelInProgress();
                    List<CommandInfo> infos = (List<CommandInfo>) msg.obj;
                    if (infos != null && infos.size() > 0) {
                        for (CommandInfo info : infos) {
                            if (info.getCtype()  .equals(CommandInfo.CommandTypeEnum.commandsendtime.value())) {
                                long time = Long.parseLong(info.getCommand());
                                String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(time));
                                String date1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(System.currentTimeMillis());

                                defTime = System.currentTimeMillis() - time;
                                if (defTime < 60 * 1000) {
                                    defTime = 60 * 1000 - defTime;
                                    timer.setCountdownInterval(1000);
                                    timer.setMillisInFuture(defTime);
                                    timer.start();
                                }
                            } else if (info.getCtype().equals(CommandInfo.CommandTypeEnum.liquidMargin.value())) {
                                if (lastTime > 0) {//还在喷洒一分钟 不执行
                                    return true;
                                }
                                if (info.getCommand().equals("1")) {
                                    btn_oneminute.setEnabled(false);
                                } else if (info.getCommand().equals("0")) {
                                    btn_oneminute.setEnabled(true);
                                }

                            }
                        }
                    } else {
                        btn_oneminute.setEnabled(true);
                    }
                    break;
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);
    private int itemPosition = -1;
    private QwqMenuPopupWindow popupWindow;
    private LinearLayout linearLayout;
    private long defTime = 60 * 1000;
    private long lastTime = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_qwq);
        mContext = this;
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        initView();
        initData();
    }

    private void initData() {

        tv_morn.setOnClickListener(this);
        btn_oneminute.setOnClickListener(this);
        iv.setOnClickListener(this);
        switch_morn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (status) {
                    if (isChecked) {
                        enable(1, deviceInfo.getId());
                    } else {
                        enable(0, deviceInfo.getId());
                    }
                } else {
                    status = true;
                }

            }
        });


        timer = new CountDownTimerUtils(defTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
//                btn_oneminute.setText((millisUntilFinished / 1000) + "s");
                lastTime = millisUntilFinished / 1000;
                btn_oneminute.setEnabled(false);
            }

            @Override
            public void onFinish() {
                btn_oneminute.setText(getString(R.string.qwq_one_minute));
                btn_oneminute.setEnabled(true);
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        filter.addAction(Actions.REFRESH_DEVICES_LIST);
        filter.addAction(Actions.SHOW_SERVER_MESSAGE);
        registerReceiver(receiver, filter);
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new GetCommandTime());
    }

    public void enable(int s, final long scenceId) {
        final int status = s;
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(QWQActivity.this,
                        DataCenterSharedPreferences.Constant.CONFIG);
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", scenceId);
                pJsonObject.put("s", status);

                final String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/dtc/status", pJsonObject, QWQActivity.this);
                if ("0".equals(result)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            if (status == 1) {
                                Toast.makeText(QWQActivity.this, getString(R.string.activity_editscene_enable),
                                        Toast.LENGTH_LONG).show();
                                switch_morn.setChecked(true);

                            } else {
                                Toast.makeText(QWQActivity.this, getString(R.string.activity_editscene_disable),
                                        Toast.LENGTH_LONG).show();
                                switch_morn.setChecked(false);
                            }

                        }
                    });
                } else if ("-3".equals(result)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(QWQActivity.this, getString(R.string.activity_editscene_s_erro),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Actions.ACCETP_ONEDEVICE_MESSAGE.equals(intent.getAction())) { // 某一个设备的推送广播
                deviceId = intent.getStringExtra("device_id");
                if (deviceId != null && deviceId.equals(String.valueOf(deviceInfo.getId()))) {
                    String data = (String) intent.getSerializableExtra("device_info");
                    if (data != null) {
                        JSONObject object1 = JSONObject.parseObject(data);
                        String dt = object1.getString("dt");
                        if (dt != null) {
                            if (dt.equals("48") && lastTime > 1) {
                                return;
                            }
                            if (dt.equals("48") && object1.getString("deviceCommand").equals("1")) {
                                btn_oneminute.setEnabled(false);
                            } else if (dt.equals("48") && object1.getString("deviceCommand").equals("0")) {
                                btn_oneminute.setEnabled(true);
                            }
                        } else if (object1.containsKey("sort") && object1.getString("sort").equals("2")) {
                            if (mHandler.hasMessages(10)) {
                                mHandler.removeMessages(10);
                            }
                            long uid = dcsp.getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
                            if (object1.getString("send").equals(String.valueOf(uid))) {
                                Toast.makeText(QWQActivity.this, getString(R.string.rq_control_sendsuccess), Toast.LENGTH_SHORT).show();
                                timer.start();
                            } else {
                                List<CommandInfo> infos = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());
                                if (infos != null && infos.size() > 0) {
                                    for (CommandInfo info : infos) {
                                        if (info.getCtype() .equals(CommandInfo.CommandTypeEnum.commandsendtime.value())) {
                                            long time = Long.parseLong(info.getCommand());
                                        String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(time));
                                        String date1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(System.currentTimeMillis());
                                            defTime = System.currentTimeMillis() - time;
                                            if (defTime < 60 * 1000) {
                                                defTime = 60 * 1000 - defTime;
                                                timer.setCountdownInterval(1000);
                                                timer.setMillisInFuture(defTime);
                                                timer.start();
                                            }
                                        }
                                    }
                                }
                            }


                        } else if (object1.containsKey("sort")) {
                            if (mHandler.hasMessages(10)) {
                                mHandler.removeMessages(10);
                            }
                            Toast.makeText(QWQActivity.this, getString(R.string.rq_control_sendsuccess), Toast.LENGTH_SHORT).show();
                        }
                    }
                    if (progressIsShowing()) {
                        cancelInProgress();
                    }
                } else if (intent.getAction().equals(Actions.SHOW_SERVER_MESSAGE)) {
                    //返回指令操作失败
                    mHandler.removeMessages(10);
                    cancelInProgress();
                    JSONObject resultJson = null;
                    try {
                        resultJson = JSON.parseObject(intent.getStringExtra("message"));
                    } catch (Exception e) {
                        Log.w("DevicesList", "获取服务器返回消息，转换为json对象失败，用原始值处理");
                    }
                    if (resultJson != null) {
                        switch (resultJson.getIntValue("Code")) {
                            case 4:
                                Toast.makeText(QWQActivity.this, getString(R.string.tips_4), Toast.LENGTH_SHORT).show();
                                break;
                            case 5:
                                Toast.makeText(QWQActivity.this, getString(R.string.tips_5), Toast.LENGTH_SHORT).show();
                                break;
                            case 6:
                                Toast.makeText(QWQActivity.this, getString(R.string.tips_6), Toast.LENGTH_SHORT).show();
                                break;
                            case 7:
                                Toast.makeText(QWQActivity.this, getString(R.string.tips_7), Toast.LENGTH_SHORT).show();
                                break;
                            case 8:
                                Toast.makeText(QWQActivity.this, getString(R.string.tips_8), Toast.LENGTH_SHORT).show();
                                break;

                            default:
                                Toast.makeText(QWQActivity.this, "Unknown Info", Toast.LENGTH_SHORT).show();
                                break;
                        }

                    } else {
                        Toast.makeText(QWQActivity.this, intent.getStringExtra("message"), Toast.LENGTH_SHORT)
                                .show();

                    }
                } else if (Actions.REFRESH_DEVICES_LIST.equals(intent.getAction())) {
                    List<CommandInfo> infos = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());
                }

            }
        }

    };

    private void initView() {
        linearLayout = (LinearLayout) findViewById(R.id.ll_layout);
        btn_oneminute = (Button) findViewById(R.id.onnminitue);

        tv_morn = (TextView) findViewById(R.id.text_morn);
        switch_morn = (CheckSwitchButton) findViewById(R.id.switch_morn);
        iv = (ImageView) findViewById(R.id.iv_history);
        btn_add = (Button) findViewById(R.id.btn_add);
        btn_add.setOnClickListener(this);
        adapter = new MyAdapter();
        timerInfos = new ArrayList<DeviceTimerInfo>();
        mListView = (ListView) findViewById(R.id.lv_clock);
        mListView.setAdapter(adapter);
        popupWindow = new QwqMenuPopupWindow(this, this);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                itemPosition = position;
                popupWindow.updateDeviceMenu(mContext);
                popupWindow.showAtLocation(linearLayout, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
                return true;
            }
        });
    }

    public class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return timerInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return timerInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int arg0, View view, ViewGroup parent) {
            // TODO Auto-generated method stub
            MyHolder myHolder = null;
            if (view == null) {
                myHolder = new MyHolder();
                view = LayoutInflater.from(QWQActivity.this).inflate(R.layout.activity_qwq_item, null);
                myHolder.tv = (TextView) view.findViewById(R.id.tv_tel);
                myHolder.editBtn = (Button) view.findViewById(R.id.btn_update);
                myHolder.deleteBtn = (Button) view
                        .findViewById(R.id.btn_delete);
                view.setTag(myHolder);
            } else {
                myHolder = (MyHolder) view.getTag();
            }
            int time, hour, min;
            time = timerInfos.get(arg0).getTime();
            hour = time / 60;
            min = time - hour * 60;

            String hour1 = (hour < 10) ? "0" + hour : String.valueOf(hour);
            String min1 = (min < 10) ? "0" + min : String.valueOf(min);

            myHolder.tv.setText(hour1 + ":" + min1);
            return view;
        }

        class MyHolder {
            public TextView tv;
            public Button editBtn, deleteBtn;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new SceneLoad());
    }

    public void back(View v) {
        finish();
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.text_morn:
                break;
            case R.id.onnminitue:
                showInProgress(getString(R.string.loading), false, true);
                SyncMessage message = new SyncMessage();
                message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                message.setDeviceid(deviceInfo.getId());
                message.setSyncBytes(new byte[]{0x02});
                SyncMessageContainer.getInstance().produceSendMessage(message);
                mHandler.sendEmptyMessageDelayed(10, 8 * 1000);
                break;
            case R.id.iv_history:
                Intent intent1 = new Intent();
                intent1.setClass(this, DeviceCommandHistoryActivity.class);
                intent1.putExtra("device", deviceInfo);
                startActivity(intent1);
                break;
            case R.id.btn_add:
                Intent intent = new Intent();
                intent.putExtra("device", deviceInfo);
                intent.putExtra("status", switch_morn.isChecked());
                intent.setClass(this, QWQNoticeActicity.class);
                startActivity(intent);
                break;
            case R.id.btn_setdevice:
                popupWindow.dismiss();
                Intent intent2 = new Intent(this, QWQNoticeActicity.class);
                intent2.putExtra("device", deviceInfo);
                intent2.putExtra("timerInfo", timerInfos.get(itemPosition));
                startActivity(intent2);
                break;
            case R.id.btn_deldevice:
                popupWindow.dismiss();
                new AlertView(getString(R.string.deviceslist_server_leftmenu_deltitle),
                        getString(R.string.qwq_clock),
                        getString(R.string.deviceslist_server_leftmenu_delcancel),
                        new String[]{getString(R.string.deviceslist_server_leftmenu_delbutton)}, null,
                        mContext, AlertView.Style.Alert,
                        new OnItemClickListener() {

                            @Override
                            public void onItemClick(Object o, final int position) {
                                if (position != -1) {
                                    showInProgress(getString(R.string.deviceslist_server_leftmenu_deltips), false, true);
                                    JavaThreadPool.getInstance().excute(new Runnable() {

                                        @Override
                                        public void run() {

                                            String server = dcsp.getString(
                                                    DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                                            JSONObject object = new JSONObject();
                                            object.put("id", timerInfos.get(itemPosition).getId());
                                            server = server + "/jdm/s3/dtc/del";
                                            String result = HttpRequestUtils.requestoOkHttpPost( server, object, QWQActivity.this);
                                            // -1参数为空，0删除成功
                                            if (result != null && result.equals("0")) {
                                                mHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        cancelInProgress();
                                                        Toast.makeText(QWQActivity.this, getString(R.string.device_del_success), Toast.LENGTH_SHORT).show();
                                                        timerInfos.remove(itemPosition);
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                });

                                            }
                                        }
                                    });
                                }
                            }
                        }).show();
                break;
        }

    }

    public class QwqMenuPopupWindow extends PopupWindow {

        private View mMenuView;
        private Button btn_deldevice, btn_setdevice;

        public QwqMenuPopupWindow(Context context, View.OnClickListener itemsOnClick) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mMenuView = inflater.inflate(R.layout.zss_item_menu, null);
            btn_deldevice = (Button) mMenuView.findViewById(R.id.btn_deldevice);
            btn_setdevice = (Button) mMenuView.findViewById(R.id.btn_setdevice);

            btn_deldevice.setOnClickListener(itemsOnClick);
            btn_setdevice.setOnClickListener(itemsOnClick);
            //设置SelectPicPopupWindow的View
            this.setContentView(mMenuView);
            //设置SelectPicPopupWindow弹出窗体的宽
            this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            //设置SelectPicPopupWindow弹出窗体的高
            this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            //设置SelectPicPopupWindow弹出窗体可点击
            this.setFocusable(true);
            //设置SelectPicPopupWindow弹出窗体动画效果
            this.setAnimationStyle(R.style.Devices_list_menu_Animation);
            //实例化一个ColorDrawable颜色为半透明
            ColorDrawable dw = new ColorDrawable(0x00000000);
            //设置SelectPicPopupWindow弹出窗体的背景
            this.setBackgroundDrawable(dw);
            //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
            mMenuView.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {

                    int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                    int y = (int) event.getY();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (y < height) {
                            dismiss();
                        }
                    }
                    return true;
                }
            });

        }


        public void updateDeviceMenu(Context context) {
            btn_setdevice.setText(context.getResources().getString(R.string.check));
            btn_deldevice.setText(context.getResources().getString(R.string.zss_item_del));
        }

    }

    class SceneLoad implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("id", deviceInfo.getId());
            String result = HttpRequestUtils
                    .requestoOkHttpPost(server + "/jdm/s3/dtc/all", pJsonObject, QWQActivity.this);
            if (!StringUtils.isEmpty(result) && result.length() > 2) {
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
                            Toast.makeText(QWQActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                Log.e(TAG, ll.toString());

                timerInfos = JSON.parseArray(ll.toJSONString(), DeviceTimerInfo.class);
            }
            Message m = mHandler.obtainMessage(1);
            m.obj = timerInfos;
            mHandler.sendMessage(m);
        }
    }

    private class GetCommandTime implements Runnable {
        @Override
        public void run() {
            List<CommandInfo> infos = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());

            Message message = Message.obtain();
            message.obj = infos;
            message.what = LASTTIME;
            mHandler.sendMessage(message);
        }
    }
}
