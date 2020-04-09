package com.smartism.znzk.activity.yaokan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.GroupInfoActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.YKDeviceInfo;
import com.smartism.znzk.domain.yankan.DeviceTypeResult;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LanguageUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.yaokan.YkanIRInterface;
import com.smartism.znzk.util.yaokan.YkanIRInterfaceImpl;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 添加页面
 */
public class YKDownLoadCodeActivity extends ActivityParentActivity implements View.OnClickListener {

    private static final int NODATA = 11;
    private static final int CODEDATA = 12;
    private static final int DEVICE_POWER_OK = 13;
    private static final int SEND_CODE = 14;
    private static final int NEW_DEVICE_CODE = 15;
    private YkanIRInterface ykanInterface;
    private String deviceId = "";
    private Button bt_getDeviceType;
    private long did;
    private Context mContext;

    private List<YKDeviceInfo> testDatas;
    YKDeviceInfo info;
    private RecyclerView recyclerView;
    RecyclerView.LayoutManager manager;
    JSONArray array;
    String jsonString;
    private MyAdapter adapter;
    private String code = null;
    private boolean flag = true;
    private String result;
    private YKDeviceInfo ykInfo;
    private String powerCode;
    private String powerCodeoff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_list);
        mContext = this;
        initView();
        initData();
    }

    private void initData() {
        ykInfo = new YKDeviceInfo();
        testDatas = new ArrayList<>();
//        deviceId = getIntent().getStringExtra("masterId");
//        deviceId = dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, "");
        //替换
        deviceId = ZhujiListFragment.getMasterId();
        MainApplication.app.getAppGlobalConfig().setYaoKanDeviceId(deviceId);
        ykanInterface = new YkanIRInterfaceImpl();
        did = getIntent().getLongExtra("did", 0);
        manager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        showInProgress(getString(R.string.ongoing), false, true);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                ykanInterface.registerDevice(mHandler);
            }
        });

        // 添加遥控器
        bt_getDeviceType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInProgress(getString(R.string.ongoing), false, true);
                JavaThreadPool.getInstance().excute(new Runnable() {
                    public void run() {
                        ykanInterface.getDeviceType(mHandler);
                    }
                });
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.REFRESH_DEVICES_LIST);
        filter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        filter.addAction(Actions.FINISH_YK_EXIT);
        registerReceiver(receiver, filter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Actions.REFRESH_DEVICES_LIST)) {
                getDeviceList();
            } else if (intent.getAction().equals(Actions.ACCETP_ONEDEVICE_MESSAGE)) {
                String deviceId = (String) intent.getSerializableExtra("device_id");
                String data = (String) intent.getSerializableExtra("device_info");
                if (deviceId != null && deviceId.equals(String.valueOf(did))) {
                    if (mHandler.hasMessages(SEND_CODE)) {
                        mHandler.removeMessages(SEND_CODE);
                    }
                    if (data != null) {
                        JSONObject object1 = JSONObject.parseObject(data);
                        long uid = dcsp.getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
                        if (object1.getString("send").equals(String.valueOf(uid))) {
                            Toast.makeText(YKDownLoadCodeActivity.this, getString(R.string.rq_control_sendsuccess), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            } else if (intent.getAction().equals(Actions.FINISH_YK_EXIT)) {
                did = intent.getLongExtra("did", 0);
            }
        }
    };

    private void initView() {
        linearLayout = (LinearLayout) findViewById(R.id.ll_layout);
        bt_getDeviceType = (Button) findViewById(R.id.bt_getDeviceType);
        recyclerView = (RecyclerView) findViewById(R.id.recyleview);
        popupWindow = new YKMenuPopupWindow(this, this);

    }


    private String tname;
    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case YkanIRInterfaceImpl.NET_SUCCEES_GETDEVICETYPE:
                    cancelInProgress();
                    DeviceTypeResult deviceTypeResult = (DeviceTypeResult) msg.obj;
                    if (deviceTypeResult != null) {
                        Intent intent = new Intent();
                        intent.putExtra("deviceTypeResult", deviceTypeResult);
                        intent.putExtra("did", did);
                        intent.setClass(getApplicationContext(), YKGetDeviceTypeActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.net_error_ioerror, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case YkanIRInterfaceImpl.NET_SUCCEES_REGIS:
                    cancelInProgress();
                    if (msg.arg1 == -1) {
                        Log.e("YKRegis:", "success");
                    } else {
//                        String result = (String) msg.obj;
                    }
                    break;
                case NODATA:
                    testDatas.clear();
                    YKDeviceInfo info1 = new YKDeviceInfo();
                    info1.setImageId(R.drawable.yk_download_add_white);
                    testDatas.add(info1);
                    adapter = new MyAdapter(testDatas, mContext);
                    recyclerView.setAdapter(adapter);
                    break;
                case DEVICE_POWER_OK:
                    cancelInProgress();
                    String data = (String) msg.obj;
                    if (data != null && !data.isEmpty()) {
                        JSONObject obj = new JSONObject();
                        if (ykInfo.getName().equals("空调")) {
                            obj.put("name", ykInfo.getStatus() == 0 ? "off" : "on");
                            obj.put("eid", ykInfo.getEid());
                        } else {
                            obj.put("name", "power");
                        }
                        obj.put("code", data);
//                        showInProgress(getString(R.string.loading), false, true);
                        SyncMessage message1 = new SyncMessage();
                        message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
                        message1.setDeviceid(did);// 红外转发器的ID
                        try {
                            message1.setSyncBytes(obj.toJSONString().getBytes("UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        SyncMessageContainer.getInstance().produceSendMessage(message1);
                        mHandler.sendEmptyMessageDelayed(SEND_CODE, 8 * 1000);
                    }
                    break;
                case SEND_CODE:
                    if (progressIsShowing()) {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case NEW_DEVICE_CODE:
                    String result = (String) msg.obj;
                    Intent intent = new Intent();

                    cancelInProgress();
                    JSONObject objInfo = JSONObject.parseObject(result);
                    String rcCode = objInfo.getString("rcCode");
                    if (info.getName().equals("空调")) {
                        intent.setClass(mContext, YKRemoteTypeAirActivity.class);
                        intent.putExtra("eid", info.getEid());
                    } else if (info.getName().equals("风扇")) {
                        intent.setClass(mContext, YKElectricFanMainActivity.class);
                    } else if (info.getName().equals("电视机")) {
                        intent.setClass(mContext, YKTVMainActivity.class);
                    } else if (info.getName().equals("电视机顶盒")) {
                        intent.setClass(mContext, YKTVBoxActivity.class);
                    }
                    Util.saveYKCodeToFile(rcCode,info.getBrand()+info.getType());
                    intent.putExtra("did", did);
                    intent.putExtra("ctrlId", info.getCodeId());
                    intent.putExtra("type", info.getType());
                    intent.putExtra("brand", info.getBrand());
//                    if ((ncode != null) && (nonoff != null)) {
//                        intent.putExtra("ncode", ncode);
//                        intent.putExtra("nonoff", nonoff);
//                    }
                    // 服务器保存的最新指令
                    startActivity(intent);
                    break;
                case CODEDATA:
                    jsonString = (String) msg.obj;
                    if (jsonString != null && !jsonString.isEmpty()) {
                        testDatas.clear();
                        testDatas = new ArrayList<>();
                        array = JSON.parseArray(jsonString);
                        if (array != null && !array.isEmpty())
                            for (int i = 0; i < array.size(); i++) {
                                YKDeviceInfo info = new YKDeviceInfo();
                                JSONObject jsonObject = array.getJSONObject(i);
                                info.setBrand(jsonObject.getString("bname"));
                                info.setType(jsonObject.getString("type"));
                                info.setCodeId(jsonObject.getString("codeId"));
                                info.setName(jsonObject.getString("tname"));
                                info.setEid(jsonObject.getLongValue("id"));
                                if (info.getName().equals("风扇")) {
                                    info.setImageId(R.drawable.icon_yk_fan);
                                } else if (info.getName().equals("空调")) {
                                    info.setStatus(0);
                                    if (jsonObject.containsKey("nonoff")) {
                                        info.setStatus(jsonObject.getString("nonoff").equals("on") ? 0 : 1);
                                    }
                                    info.setImageId(R.drawable.yaokan_ctrl_d_air);
                                } else if (info.getName().equals("电视机")) {
                                    info.setImageId(R.drawable.icon_yk_tv);
                                } else if (info.getName().equals("电视机顶盒")) {
                                    info.setImageId(R.drawable.icon_yk_tvbox);
                                }
                                testDatas.add(info);
                            }
                        YKDeviceInfo info = new YKDeviceInfo();
                        info.setImageId(R.drawable.yk_download_add_white);
                        testDatas.add(info);
                        adapter = new MyAdapter(testDatas, mContext);
                        recyclerView.setAdapter(adapter);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    private int itemPosition = -1;
    private YKMenuPopupWindow popupWindow;
    private LinearLayout linearLayout;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_setdevice:
                popupWindow.dismiss();
                break;
            case R.id.btn_deldevice:
                popupWindow.dismiss();
                new AlertView(getString(R.string.deviceslist_server_leftmenu_deltitle),
                        getString(R.string.deviceslist_server_leftmenu_delmessage),
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
                                            object.put("id", ykInfo.getEid());
                                            object.put("did", did);
                                            server = server + "/jdm/s3/infr/del";
                                            String result = HttpRequestUtils.requestoOkHttpPost( server, object, YKDownLoadCodeActivity.this);
                                            // -1参数为空，0删除成功
                                            if (result != null && result.equals("0")) {
                                                mHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        cancelInProgress();
                                                        Toast.makeText(YKDownLoadCodeActivity.this, getString(R.string.device_del_success), Toast.LENGTH_SHORT).show();
                                                        testDatas.remove(itemPosition);
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

    public class YKMenuPopupWindow extends PopupWindow {

        private View mMenuView;
        private Button btn_deldevice, btn_setdevice;

        public YKMenuPopupWindow(Context context, View.OnClickListener itemsOnClick) {
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
            btn_setdevice.setText(context.getResources().getString(R.string.cancel_panel));
            btn_deldevice.setText(context.getResources().getString(R.string.zss_item_del));
        }

    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder> {

        private List<YKDeviceInfo> mDatas;
        private LayoutInflater inflater;

        public MyAdapter(List<YKDeviceInfo> datas, Context context) {
            this.mDatas = datas;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.item_ykdevice_list, null);
            MyHolder holder = new MyHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(MyHolder holder, final int position) {

            if (position == mDatas.size() - 1) {
                holder.tv_type.setText(getString(R.string.hwzf_download_title_small));
                holder.tv_brand.setText(getString(R.string.hwzf_download_title));
                holder.tv_brand.setSelected(true);
                holder.tv_type.setSelected(true);
                holder.iv_power.setVisibility(View.GONE);
                holder.imageView.setImageResource(mDatas.get(position).getImageId());
            } else {
                holder.imageView.setImageResource(mDatas.get(position).getImageId());

                holder.tv_brand.setText(mDatas.get(position).getBrand());

                holder.tv_type.setText(mDatas.get(position).getType());
                if (!LanguageUtil.isZh(YKDownLoadCodeActivity.this)) {
                    if (mDatas.get(position).getType().equals("空调")) {
                        holder.tv_type.setText("Air conditioning");
                    } else if (mDatas.get(position).getType().equals("风扇")) {
                        holder.tv_type.setText("Fan");
                    } else if (mDatas.get(position).getType().equals("电视机")) {
                        holder.tv_type.setText("TV");
                    } else if (mDatas.get(position).getType().equals("电视机顶盒")) {
                        holder.tv_type.setText("Set-top box");
                    }
                }
            }
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (position == mDatas.size() - 1) {
                        showInProgress(getString(R.string.ongoing), false, true);
                        JavaThreadPool.getInstance().excute(new Runnable() {
                            public void run() {
                                ykanInterface.getDeviceType(mHandler);
                            }
                        });
                    } else {
                        displayInfo(position);
                    }
                }
            });
            holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (position == mDatas.size() - 1) {
                        return true;
                    }
                    ykInfo = mDatas.get(position);
                    itemPosition = position;
                    popupWindow.updateDeviceMenu(mContext);
                    popupWindow.showAtLocation(linearLayout, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
                    return false;
                }
            });
            holder.iv_power.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ykInfo = mDatas.get(position);
                    getItemPower(ykInfo);
                }
            });
        }


        @Override
        public int getItemCount() {
            return mDatas.size();
        }

        public class MyHolder extends RecyclerView.ViewHolder {
            public ImageView imageView, iv_power;
            public TextView tv_brand;
            public TextView tv_type;
            public View view;

            public MyHolder(View itemView) {
                super(itemView);
                view = itemView;
                imageView = (ImageView) itemView.findViewById(R.id.iv_type);
                iv_power = (ImageView) itemView.findViewById(R.id.iv_power);
                tv_brand = (TextView) itemView.findViewById(R.id.tv_brand);
                tv_type = (TextView) itemView.findViewById(R.id.tv_type);
            }
        }
    }

    private void displayInfo(int position) {
        final Intent intent = new Intent();
        info = testDatas.get(position);

        if (!TextUtils.isEmpty(Util.readYKCodeFromFile(info.getBrand()+info.getType()))) {
//            if (info.getName().equals("空调")) {
//
//                intent.setClass(mContext, YKRemoteTypeAirActivity.class);
//                intent.putExtra("eid", info.getEid());
//            } else if (info.getName().equals("风扇")) {
//
//                intent.setClass(mContext, YKElectricFanMainActivity.class);
//
//            } else if (info.getName().equals("电视机")) {
//
//                intent.setClass(mContext, YKTVMainActivity.class);
//
//            } else if (info.getName().equals("电视机顶盒")) {
//                intent.setClass(mContext, YKTVBoxActivity.class);
//            }
            if (info.getName().equals("空调")) {

                intent.setClass(mContext, YKRemoteTypeAirActivity.class);
                intent.putExtra("eid", info.getEid());
            } else if (info.getName().equals("风扇")) {

                intent.setClass(mContext, YKElectricFanMainActivity.class);

            } else if (info.getName().equals("电视机")) {

                intent.setClass(mContext, YKTVMainActivity.class);

            } else if (info.getName().equals("电视机顶盒")) {
                intent.setClass(mContext, YKTVBoxActivity.class);
            }
            intent.putExtra("did", did);
            intent.putExtra("ctrlId", info.getCodeId());
            intent.putExtra("type", info.getType());
            intent.putExtra("brand", info.getBrand());
            startActivity(intent);
        } else {
            showInProgress(getString(R.string.ongoing), false, true);
            JavaThreadPool.getInstance().excute(new Runnable() {
                @Override
                public void run() {
                    try {
                        code = getYaokanCode(info.getEid(), did, true);
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
                                Toast.makeText(mContext, getString(R.string.net_error_requestfailed), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }


                }
            });
        }


    }

    public void getItemPower(final YKDeviceInfo ykInfo) {
        String result = Util.readYKCodeFromFile(ykInfo.getBrand()+ykInfo.getType());
        if (!TextUtils.isEmpty(result)) {
            JSONArray array = JSON.parseArray(result);
            JSONObject o;
            JSONObject o2;

            for (int i = 0; i < array.size(); i++) {
                o = array.getJSONObject(i);
                if (o.containsKey("power")) {
                    if (ykInfo.getName().equals("风扇")) {
                        powerCode = o.getString("power");
                    } else {
                        powerCode = o.getJSONObject("power").getString("src");
                    }
                } else if (o.containsKey("on") && ykInfo.getStatus() == 1) {
                    powerCode = o.getString("on");
                    break;
                } else if (o.containsKey("off") && ykInfo.getStatus() == 0) {
                    powerCode = o.getString("off");
                    break;
                }
            }
            Message msg = Message.obtain();
            msg.what = DEVICE_POWER_OK;
            msg.obj = powerCode;
            mHandler.sendMessage(msg);
        } else {
            showInProgress(getString(R.string.ongoing), false, true);
            JavaThreadPool.getInstance().excute(new Runnable() {
                @Override
                public void run() {
                    String result = getYaokanCode(ykInfo.getEid(), did, true);
                    if (!TextUtils.isEmpty(result) && result.length() > 5) {
                        // 如果下载成功 保存数据
                        JSONObject objInfo = JSONObject.parseObject(result);
                        String rcCode = objInfo.getString("rcCode");
                        Util.saveYKCodeToFile(rcCode,ykInfo.getBrand()+ykInfo.getType());
                        JSONArray array = JSON.parseArray(rcCode);
                        JSONObject o;
                        JSONObject o2;
                        for (int i = 0; i < array.size(); i++) {
                            o = array.getJSONObject(i);
                            if (o.containsKey("power")) {
                                if (ykInfo.getName().equals("风扇")) {
                                    powerCode = o.getString("power");
                                    break;
                                } else {
                                    powerCode = o.getJSONObject("power").getString("src");
                                    break;
                                }
                            } else if (o.containsKey("on") && ykInfo.getStatus() == 1) {
                                powerCode = o.getString("on");
                                break;
                            } else if (o.containsKey("off") && ykInfo.getStatus() == 0) {
                                powerCode = o.getString("off");
                                break;
                            }
                        }

//                        for (Object o1 : array) {
//                            o = (JSONObject) o1;
//                            if (o.containsKey("power")) {
//                                if (ykInfo.getName().equals("风扇")) {
//                                    powerCode = o.getString("power");
//                                } else {
//                                    powerCode = o.getJSONObject("power").getString("src");
//                                }
//                            } else if (o.containsKey("on")) {
//                                if (ykInfo.getStatus() == 1) {
//                                    powerCode = o.getString("off");
//                                } else {
//                                    powerCode = o.getString("on");
//                                }
//                            }
//                        }
                        Message msg = Message.obtain();
                        msg.what = DEVICE_POWER_OK;
                        msg.obj = powerCode;
                        mHandler.sendMessage(msg);

                    } else {
                        cancelInProgress();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(mContext, getString(R.string.net_error_requestfailed), Toast.LENGTH_SHORT).show();
                            }
                        });

                        return;
                    }

                }
            });
        }
    }

    private String getYaokanCode(long type, long did, Boolean b) {
        String result = null;
        JSONObject object = new JSONObject();
        object.put("did", did);
        object.put("c", b);
        String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
        if (type != 0) {
            object.put("id", type);
            result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/infr/get", object, YKDownLoadCodeActivity.this);
        } else {
            result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/infr/list", object, YKDownLoadCodeActivity.this);
        }

        return result;
    }

    public void back(View v) {
//        toGroupInfoActivity();
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        getDeviceList();
    }

    private void getDeviceList() {

        JavaThreadPool.getInstance().excute(new Runnable() {
            private String codeId;
            String resultCode = null;

            public void run() {
                String result = getYaokanCode(0, did, false);
                if (result == null || TextUtils.isEmpty(result)) {
//                    mHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(mContext, getString(R.string.net_error_servererror), Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//                    });
                    return;
                } else if (result.contentEquals("-3")) {
                    mHandler.sendEmptyMessage(NODATA);
                    return;
                } else if (result.length() > 4) {
                    try {
                        resultCode = result;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Message message = Message.obtain();
                    message.obj = resultCode;
                    message.what = CODEDATA;
                    mHandler.sendMessage(message);
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(YKDownLoadCodeActivity.this, getString(R.string.net_error), Toast.LENGTH_SHORT).show();
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
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
//            toGroupInfoActivity();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

//    public void toGroupInfoActivity() {
//        Contact contact = (Contact) getIntent().getSerializableExtra("camera");
//        DeviceInfo groupDevice = (DeviceInfo) getIntent().getSerializableExtra("group");
//        if (groupDevice != null) {
//            Intent intent = new Intent();
//            intent.setClass(getApplication(), GroupInfoActivity.class);
//            intent.putExtra("device", groupDevice);
//            intent.putExtra("contact", contact);
//            intent.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
//            startActivity(intent);
//        }
//    }

}
