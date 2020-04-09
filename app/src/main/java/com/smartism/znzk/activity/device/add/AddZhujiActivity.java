package com.smartism.znzk.activity.device.add;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.AddZhujiBySelectMethodActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.domain.CategoryInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.Actions.VersionType;
import com.smartism.znzk.util.BaiduLBSUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.NetworkUtils;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.DeviceSearchPopupWindow;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.AlertView.Style;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.view.zbarscan.ScanCaptureActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@SuppressLint("NewApi")
public class AddZhujiActivity extends ActivityParentActivity implements OnClickListener, OnItemClickListener {
    ImageView displayZhuji_pic ;
    private RelativeLayout havezhuji_layout, nozhuji_layout, wifizhuji_layout, gprszhuji_layout;
    private TextView title, wifi_title;
    private EditText etName;
    private TextView mOtherTipTv; //其它配网方式
    //主机搜索和提示
    DeviceSearchPopupWindow searchPopupWindow;
    //	private AlarmManager alarmManager;
    private AlertView mAlertViewExt;
    //	private PendingIntent checkZhujiIntent;
    private String key;
    private boolean tips_isshowing = false;
    private int search_count = 0;  //搜索次数
    private int search_cmax = 10;  //搜索最大次数
    private int search_time = 5000;  //搜索重发间隔 单位ms
    private int pendingRequestCode = 0;
    private InputMethodManager imm;
    private String sqr = "";
    private String pattern; //分享类型


    private BroadcastReceiver defaultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (Actions.SEND_SEARCHZHUJICOMMAND.equals(intent.getAction())) { //定时器1S重新再发送刷新事件
                search_count++;
                if (search_count >= search_cmax) {
                    dismissProgressDialogSearchZhuji();
//					Toast.makeText(AddZhujiActivity.this, getString(R.string.activity_add_zhuji_nozhu_again), Toast.LENGTH_LONG).show();
//                    new AlertView(getString(R.string.tips), getString(R.string.activity_add_zhuji_idzhu_nofondinwifi), getString(R.string.cancel), new String[]{getString(R.string.sure)}, null, AddZhujiActivity.this, Style.Alert.Alert, new OnItemClickListener() {
//
//                        @Override
//                        public void onItemClick(Object o, int position) {
//                            if (position != -1) {
//                                if (isMainList) {
//                                    addDeviceBySerial();
//                                } else {
//                                    addZhujiBySerial();
//                                }
//                            }
//                        }
//                    }).show();
                    return;
                }
                SyncMessage syncMessage = new SyncMessage(SyncMessage.CommandMenu.rq_szhuji);
                try {
                    if (getJdmApplication().getLocation() != null) {
                        JSONObject pJsonObject = new JSONObject();
                        pJsonObject.put("country", getJdmApplication().getLocation().getCountry());
                        pJsonObject.put("province", getJdmApplication().getLocation().getProvince());
                        pJsonObject.put("city", getJdmApplication().getLocation().getCity());
                        pJsonObject.put("district", getJdmApplication().getLocation().getDistrict());
                        pJsonObject.put("street", getJdmApplication().getLocation().getStreet());
                        pJsonObject.put("addr", getJdmApplication().getLocation().getAddrStr());
                        pJsonObject.put("lng", getJdmApplication().getLocation().getLongitude());
                        pJsonObject.put("lat", getJdmApplication().getLocation().getLatitude());
                        syncMessage.setSyncBytes(pJsonObject.toJSONString().getBytes("UTF-8"));
                    }
                }catch (UnsupportedEncodingException ex){
                    LogUtil.e(AddZhujiActivity.this,"AddZhuji","unsupport UTF-8");
                }
                SyncMessageContainer.getInstance().produceSendMessage(syncMessage);
            } else if (Actions.CONNECTION_SUCCESS.equals(intent.getAction())) { //连接成功

            } else if (Actions.CONNECTION_FAILED.equals(intent.getAction())) { //连接失败

            } else if (Actions.CONNECTIVITY_CHANGE.equals(intent.getAction())) { //网络状态广播
                if (searchPopupWindow != null && searchPopupWindow.isShowing()) {
//                    showProgressDialogSearchZhuji();
                }
            } else if (Actions.SEARCH_ZHUJI_RESPONSE.equals(intent.getAction())) { //搜索主机返回
                if (intent.getIntExtra("data", 1000000) == 0) { //完成主机搜索
                    dismissProgressDialogSearchZhuji();
                    finish();
                    ToastUtil.longMessage(getString(R.string.activity_add_zhuji_havezhu_addsuccess));
                } else if (intent.getIntExtra("data", 1000000) == 1) { //需要授权
                    dismissProgressDialogSearchZhuji();
                    String name = intent.getStringExtra("value");
                    int count = name != null ? name.split(",").length : 0;
                    String message = "";
                    if (count > 1) {
                        message += getString(R.string.activity_add_zhuji_nozhu_type_p_more);
                        message += "\"";
                        message += intent.getStringExtra("value");
                        message += "\"";
                        message += getString(R.string.activity_add_zhuji_nozhu_type_f_more);
                    } else {
                        message += getString(R.string.activity_add_zhuji_nozhu_type_p_one);
                        message += "\"";
                        message += intent.getStringExtra("value");
                        message += "\"";
                        message += getString(R.string.activity_add_zhuji_nozhu_type_f_one);
                    }
//                    if (!tips_isshowing) {
//                        tips_isshowing = true;
//                        new AlertView(getString(R.string.activity_add_zhuji_nozhu_typetitle),
//                                message,
//                                null, new String[]{getString(R.string.sure)}, null, AddZhujiActivity.this, Style.Alert, new OnItemClickListener() {
//
//                            @Override
//                            public void onItemClick(Object o, int position) {
//                                tips_isshowing = false;
//                            }
//                        }).show();
//                    }
                } else {
                    //无操作 未搜索到主机
                }
            }
        }
    };

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            cancelInProgress();
            switch (msg.what) {
                case 2:
                    ToastUtil.longMessage(getString(R.string.add_shoudong_failed_chaoshi));
                    break;
                case 3:
                    ToastUtil.longMessage(getString(R.string.add_auto_tips_auto_over));
                    break;
                case 4:
                    AddZhujiActivity.this.sendBroadcast(new Intent(Actions.SEND_SEARCHZHUJICOMMAND));
                    defaultHandler.sendEmptyMessageDelayed(4, search_time);
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    boolean isMainList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhzj_add_zhuji);
        isMainList = false;
//        isMainList = getIntent().getBooleanExtra("isMainList", false);
        initView();
        if(VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            BaiduLBSUtils.location(this);
        }


    }


    private TextView tv_serial_add, tv_buy, other_add;
    private ImageView iv_menu_qrcode;
    private Button btn_add_lan, btn_add_wifi;

    private void initView() {

        displayZhuji_pic = findViewById(R.id.displayZhuji_pic);
        //宏才换图标
        if(VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            displayZhuji_pic.setImageResource(R.drawable.hongcai_add_zhuji_host);
        }


        other_add = (TextView) findViewById(R.id.other_add);
        tv_buy = (TextView) findViewById(R.id.tv_buy);
        tv_serial_add = (TextView) findViewById(R.id.tv_serial_add);
        iv_menu_qrcode = (ImageView) findViewById(R.id.iv_menu_qrcode);
        btn_add_lan = (Button) findViewById(R.id.btn_add_lan);
        btn_add_wifi = (Button) findViewById(R.id.btn_add_wifi);

        other_add.setOnClickListener(this);
        iv_menu_qrcode.setOnClickListener(this);
        tv_buy.setOnClickListener(this);
        tv_serial_add.setOnClickListener(this);
        btn_add_lan.setOnClickListener(this);
        btn_add_wifi.setOnClickListener(this);
        title = (TextView) findViewById(R.id.activity_add_zhuji_title);
        mOtherTipTv = findViewById(R.id.other_peiwang_tv);

        mOtherTipTv.setOnClickListener(this);
        havezhuji_layout = (RelativeLayout) findViewById(R.id.havezhuji_layout);
        havezhuji_layout.setOnClickListener(this);
        nozhuji_layout = (RelativeLayout) findViewById(R.id.nozhuji_layout);
        nozhuji_layout.setOnClickListener(this);
        wifizhuji_layout = (RelativeLayout) findViewById(R.id.wifizhuji_layout);
        wifizhuji_layout.setOnClickListener(this);
        gprszhuji_layout = (RelativeLayout) findViewById(R.id.gprszhuji_layout);
        gprszhuji_layout.setOnClickListener(this);
        if (!MainApplication.app.getAppGlobalConfig().isShowAddLanZJ())
            nozhuji_layout.setVisibility(View.GONE);
        if (!MainApplication.app.getAppGlobalConfig().isShowAddWifiZJ())
            wifizhuji_layout.setVisibility(View.GONE);
        if (VersionType.CHANNEL_ZHISHANG.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
            havezhuji_layout.setVisibility(View.GONE);
            nozhuji_layout.setVisibility(View.GONE);
            gprszhuji_layout.setVisibility(View.GONE);
            title.setText(getString(R.string.activity_add_zhuji_title_device));
            wifi_title = (TextView) findViewById(R.id.addzhu_wifi_1);
            wifi_title.setText(getString(R.string.activity_add_zhuji_title_wifi));
        }

        if(VersionType.CHANNEL_ZHZJ.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                ||VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            other_add.setVisibility(View.VISIBLE);
        }

        // 注册广播
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Actions.SEARCH_ZHUJI_RESPONSE);
        receiverFilter.addAction(Actions.CONNECTIVITY_CHANGE);
        receiverFilter.addAction(Actions.SEND_SEARCHZHUJICOMMAND);
        registerReceiver(defaultReceiver, receiverFilter);
        tv_serial_add.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线

        if (isMainList) {
            tv_serial_add.setText(R.string.add_devices_title);
            title.setText(R.string.add_devices_title);
            btn_add_wifi.setText(R.string.add_devices_new);
            btn_add_lan.setText(R.string.add_devices_wifi);
            ((TextView) findViewById(R.id.add_title1)).setText(R.string.add_device_message_title1);
            ((TextView) findViewById(R.id.add_title2)).setText(R.string.add_device_message_title2);
            ((TextView) findViewById(R.id.add_msg1)).setText(R.string.add_device_message1);
            ((TextView) findViewById(R.id.add_msg2)).setText(R.string.add_device_message2);
        }

        //设置可见性,开发智慧主机可见
//        if(MainApplication.app.getAppGlobalConfig().isDebug()&&MainApplication.app.getAppGlobalConfig().getVersion().equals(VersionType.CHANNEL_ZHZJ)){
            mOtherTipTv.setVisibility(View.VISIBLE);
//        }else{
//            mOtherTipTv.setVisibility(View.GONE);
//        }
    }

    public void back(View v) {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("扫码", requestCode + ":" + resultCode);
        if (requestCode == 3333 && resultCode == Constant.CAPUTRE_ADDRESULT) {
            key = data.getStringExtra("value");
            pattern = data.getStringExtra("pattern");
            if (key == null) {
                ToastUtil.longMessage(getString(R.string.net_error_programs));
                return;
            }
            showInProgress(getString(R.string.activity_add_zhuji_havezhu_ongoing), false, false);
            JavaThreadPool.getInstance().excute(new Matching());
        } else if (requestCode == 5 && resultCode == 11) {//完成了wifi的设置了
//            finishWifi = true;
//            showProgressDialogSearchZhuji();
        } else if (requestCode == 5 && resultCode == 8) {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.iv_menu_qrcode:
                intent.putExtra(Constant.CAPUTRE_REQUESTCOE, Constant.CAPUTRE_ADDZHUJI);
                intent.setClass(this, ScanCaptureActivity.class);
                startActivityForResult(intent, 3333);
                break;
            case R.id.tv_serial_add:
                if (isMainList) {
                    addDeviceBySerial();
                } else {
                    addZhujiBySerial();
                }
                break;
            case R.id.btn_add_lan:
//                showProgressDialogSearchZhuji();
                intent.putExtra(Constant.CAPUTRE_REQUESTCOE, Constant.CAPUTRE_ADDZHUJI);
                intent.setClass(this, ScanCaptureActivity.class);
                startActivityForResult(intent, 3333);
                break;
            case R.id.btn_add_wifi:
                intent = new Intent();
                //主机界面不显示引导页
                if (isMainList) {
                    intent.setClass(getApplicationContext(), Add8266WifiActivity.class);
                } else {
                    intent.setClass(getApplicationContext(), AddZhujiGuideActivity.class);
                }
                intent.putExtra("isMainList", isMainList);
                startActivityForResult(intent, 5);
                break;
            case R.id.havezhuji_layout:
                intent = new Intent();
                intent.putExtra(Constant.CAPUTRE_REQUESTCOE, Constant.CAPUTRE_ADDZHUJI);
                intent.setClass(this, ScanCaptureActivity.class);
                startActivityForResult(intent, 3333);
                break;
            case R.id.nozhuji_layout:
//                showProgressDialogSearchZhuji();
                break;
            case R.id.wifizhuji_layout:
                intent = new Intent();
                //intent.setClass(getApplicationContext(), Add8781WifiActivity.class);
                if (VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion()) ||
                        VersionType.CHANNEL_WOAIJIA.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                    intent.setClass(getApplicationContext(), AddZhujiGuideActivity.class);
                } else if (/*VersionType.CHANNEL_JUJIANG.equals(MainApplication.app.getAppGlobalConfig().getVersion()) ||*/
                        VersionType.CHANNEL_SHUNANJU.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                    intent.setClass(getApplicationContext(), Add8781WifiActivity.class);
                } else {
//                        intent.setClass(getApplicationContext(), AddDeviceWifiActivity.class);
                    intent.setClass(getApplicationContext(), AddZhujiGuideActivity.class);
                }
                startActivityForResult(intent, 5);
                break;
            case R.id.gprszhuji_layout:
                if (isMainList) {
                    addDeviceBySerial();
                } else {
                    addZhujiBySerial();
                }
                break;
            case R.id.other_add:
                intent = new Intent();
                intent.setClass(getApplicationContext(), AddDeviceChooseActivity.class);
                intent.putExtra(AddDeviceChooseActivity.NET_TYPE, CategoryInfo.NetTypeEnum.wifiOrLan.value());
                startActivityForResult(intent, 5);
                break;
            case R.id.other_peiwang_tv:
                intent = new Intent();
                intent.setClass(getApplicationContext(), AddZhujiBySelectMethodActivity.class);
                startActivity(intent);
                break ;
            default:
                break;
        }
    }

    /**
     * 添加主机，通过序列号
     */
    private void addZhujiBySerial() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //拓展窗口
//        mAlertViewExt = new AlertView(getString(R.string.tips), getString(R.string.activity_add_zhuji_idzhu_msg), getString(R.string.cancel), null, new String[]{getString(R.string.compele)}, this, Style.Alert, this);
        mAlertViewExt = new AlertView(getString(R.string.activity_add_zhuji_idzhu_msg), null, getString(R.string.cancel), null, new String[]{getString(R.string.compele)}, this, Style.Alert, this);
        ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_addzhuji_alertext_form, null);
        etName = (EditText) extView.findViewById(R.id.etName);
        etName.setHint(getString(R.string.activity_add_zhuji_idzhu_hit));
        etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                //输入框出来则往上移动
                boolean isOpen = imm.isActive();
                mAlertViewExt.setMarginBottom(isOpen && focus ? 120 : 0);
            }
        });
        mAlertViewExt.addExtView(extView);
        mAlertViewExt.show();
    }

    /**
     * 添加主机，通过序列号
     */
    private void addDeviceBySerial() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //拓展窗口
//        mAlertViewExt = new AlertView(getString(R.string.tips), getString(R.string.activity_add_zhuji_idzhu_msg), getString(R.string.cancel), null, new String[]{getString(R.string.compele)}, this, Style.Alert, this);
        mAlertViewExt = new AlertView(getString(R.string.activity_add_idzhu_msg), null, getString(R.string.cancel), null, new String[]{getString(R.string.compele)}, this, Style.Alert, this);
        ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_addzhuji_alertext_form, null);
        etName = (EditText) extView.findViewById(R.id.etName);
        etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean focus) {
                //输入框出来则往上移动
                boolean isOpen = imm.isActive();
                mAlertViewExt.setMarginBottom(isOpen && focus ? 120 : 0);
            }
        });
        mAlertViewExt.addExtView(extView);
        mAlertViewExt.show();
    }

    @Override
    protected void onDestroy() {
        if (searchPopupWindow != null) {
            searchPopupWindow.dismiss();
            searchPopupWindow = null;
        }
        unregisterReceiver(defaultReceiver);
        defaultHandler.removeCallbacksAndMessages(null);
        defaultHandler = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelCheckZhuji();
    }

    class Matching implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("uid", dcsp.getLong(Constant.LOGIN_APPID, 0));
            pJsonObject.put("code", dcsp.getString(Constant.LOGIN_CODE, ""));
            pJsonObject.put("key", key);
            String result = "";
            // 长久添加
            if (pattern.equals("status_forver")) {
                result = HttpRequestUtils
                        .requestoOkHttpPost(server + "/jdm/s3/d/mshare", pJsonObject,
                                AddZhujiActivity.this);
                // 临时添加
            } else if (pattern.equals("status_temp")) {
                result = HttpRequestUtils
                        .requestoOkHttpPost(server + "/jdm/s3/d/tmshare", pJsonObject,
                                AddZhujiActivity.this);
            }
            if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        ToastUtil.longMessage(getString(R.string.activity_add_zhuji_havezhu_shixiao));
                    }
                });
            } else if ("0".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        //重新获取设备列表
                        SyncMessageContainer.getInstance()
                                .produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                        finish();
                        ToastUtil.longMessage(getString(R.string.activity_add_zhuji_havezhu_addsuccess));
                    }
                });
            }
        }
    }

    /**
     * 搜索主机提示
     * 2、主机搜索框出现：已经连接上，无设备信息
     * 2.1搜索框出现中：发送搜索主机指令，检测wifi
     * 2.2主机搜索框消失：断开连接，获取到主机信息
     *
     * @param
     */
    private void showProgressDialogSearchZhuji() {
        //设备列表无数据，
        if (searchPopupWindow == null) {
            searchPopupWindow = new DeviceSearchPopupWindow(this, this);
        }
        if (NetworkUtils.CheckNetworkIsWifi(getApplicationContext())) {
            search_count = 0;
            searchPopupWindow.getDl_search_zj_setbtn().setVisibility(View.GONE);
            searchPopupWindow.getDl_search_zj_tips().setText(getString(R.string.deviceslist_server_search_ing));
            searchPopupWindow.getDl_search_zj_bg().setVisibility(View.VISIBLE);
            searchPopupWindow.getDl_search_zj_btn().setVisibility(View.VISIBLE);
            searchPopupWindow.getDl_search_zj_bg().setBackgroundResource(R.drawable.dl_search_zj_bg);
            ((AnimationDrawable) searchPopupWindow.getDl_search_zj_bg().getBackground()).start();
            searchPopupWindow.getDl_search_zj_btn().setBackgroundResource(R.drawable.dl_search_zj_refresh_btn);
            Animation rotateAnimation = new RotateAnimation(0, 359f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(1000);
            rotateAnimation.setRepeatCount(-1);
            rotateAnimation.setInterpolator(new LinearInterpolator());
            searchPopupWindow.getDl_search_zj_btn().startAnimation(rotateAnimation);
            searchPopupWindow.setOutsideTouchable(false);
            searchPopupWindow.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss() {
                    cancelCheckZhuji();
                }
            });
            //这里屏蔽定时器发送108
//            defaultHandler.sendEmptyMessageDelayed(4, search_time);


//			if (alarmManager==null) {
//				alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//				checkZhujiIntent = PendingIntent.getBroadcast(getApplicationContext(), pendingRequestCode++, new Intent(Actions.SEND_SEARCHZHUJICOMMAND), PendingIntent.FLAG_UPDATE_CURRENT);
//				alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, search_time, search_time, checkZhujiIntent);
//			}
        } else {
            searchPopupWindow.setOutsideTouchable(true);
            cancelCheckZhuji();
            searchPopupWindow.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss() {
                }
            });
            searchPopupWindow.getDl_search_zj_tips().setText(getString(R.string.deviceslist_server_search_nowifi));
            searchPopupWindow.getDl_search_zj_bg().setVisibility(View.GONE);
            searchPopupWindow.getDl_search_zj_btn().setVisibility(View.GONE);
            searchPopupWindow.getDl_search_zj_btn().clearAnimation();
            searchPopupWindow.getDl_search_zj_setbtn().setVisibility(View.VISIBLE);
            searchPopupWindow.getDl_search_zj_setbtn().setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (android.os.Build.VERSION.SDK_INT > 10) {
                        // 3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
                        startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                    } else {
                        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                    }
                }
            });
        }
        if (!searchPopupWindow.isShowing()) {
            searchPopupWindow.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
        }
    }

    /**
     * 取消主机搜索提示
     */
    private void dismissProgressDialogSearchZhuji() {

        cancelCheckZhuji();
        if (searchPopupWindow != null && searchPopupWindow.isShowing()) {
            searchPopupWindow.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss() {
                }
            });
            searchPopupWindow.dismiss();
            searchPopupWindow = null;
        }
    }

    public void cancelCheckZhuji() {
        defaultHandler.removeMessages(4);
//		if (alarmManager!=null) {
//			alarmManager.cancel(checkZhujiIntent);
//			alarmManager = null;
//		}
    }

    @Override
    public void onItemClick(Object o, int position) {
        closeKeyboard();
        //判断是否是拓展窗口View，而且点击的是非取消按钮
        if (o == mAlertViewExt && position != AlertView.CANCELPOSITION) {
            String name = etName.getText().toString();
            if (name.isEmpty()) {
                ToastUtil.shortMessage(getString(R.string.activity_add_zhuji_idzhu_empty));
            }
//            else if (!name.toUpperCase().startsWith("FF13") && name.length() != 10) {//FF13 艾礼安的主机 云云对接时，长度有14个长度
//                ToastUtil.shortMessage(getString(R.string.activity_add_zhuji_idzhu_leng));
//            }
            else {
                showInProgress(getString(R.string.activity_add_zhuji_idzhu_ongoing), false, true);
                JavaThreadPool.getInstance().excute(new RequestAddZhuji(name));
            }
            if (mAlertViewExt != null && mAlertViewExt.isShowing()) {
                mAlertViewExt.dismissImmediately();
            }
            return;
        } else {
            if (mAlertViewExt != null && mAlertViewExt.isShowing()) {
                mAlertViewExt.dismissImmediately();
            }
        }
    }

    private void closeKeyboard() {
        //关闭软键盘
        imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
        //恢复位置
        mAlertViewExt.setMarginBottom(0);
    }

    class RequestAddZhuji implements Runnable {
        private String zhujiID;

        public RequestAddZhuji(String key) {
            this.zhujiID = key;
        }

        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("key", zhujiID);

            final BDLocation bdlocation = getJdmApplication().getLocation() ;
            if (bdlocation!=null){
                pJsonObject.put("country", bdlocation.getCountry());
                pJsonObject.put("province", bdlocation.getProvince());
                pJsonObject.put("city", bdlocation.getCity());
                pJsonObject.put("district", bdlocation.getDistrict());
                pJsonObject.put("street", bdlocation.getStreet());
                pJsonObject.put("addr", bdlocation.getAddrStr());
                pJsonObject.put("lng", bdlocation.getLongitude());
                pJsonObject.put("lat", bdlocation.getLatitude());
            }

//            String result = HttpRequestUtils.requestHttpServer( server + "/jdm/service/find?v=" + URLEncoder.encode(SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP)), AddZhujiActivity.this, defaultHandler);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/find", pJsonObject, AddZhujiActivity.this);
            if (result != null && result.startsWith("-3")) {
                try {
                    sqr = URLDecoder.decode(result.substring(2), "UTF-8");
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
                int count = sqr.split(",").length;
                String message = "";
                if (count > 1) {
                    message += getString(R.string.activity_add_zhuji_nozhu_type_p_more);
                    message += "\"";
                    message += sqr;
                    message += "\"";
                    message += getString(R.string.activity_add_zhuji_nozhu_type_f_more);
                } else {
                    message += getString(R.string.activity_add_zhuji_nozhu_type_p_one);
                    message += "\"";
                    message += sqr;
                    message += "\"";
                    message += getString(R.string.activity_add_zhuji_nozhu_type_f_one);
                }
                sqr = message;
                try {
                    Thread.sleep(1000);//不暂停下 提示框出不来
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        new AlertView(getString(R.string.tips), sqr, null, new String[]{getString(R.string.sure)}, null, AddZhujiActivity.this, Style.Alert, new OnItemClickListener() {

                            @Override
                            public void onItemClick(Object o, int position) {
                            }
                        }).show();
                    }
                });
            } else if ("-4".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        ToastUtil.shortMessage(getString(R.string.activity_add_zhuji_idzhu_notfond));
                    }
                });
            } else if ("-5".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        ToastUtil.shortMessage(getString(R.string.activity_add_zhuji_idzhu_offline));
                    }
                });
            } else if ("0".equals(result)) {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        SyncMessageContainer.getInstance()
                                .produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                        if (VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                            Intent intent = new Intent(Actions.ADD_NEW_ZHUJI);
                            intent.putExtra("masterId", zhujiID);
                            AddZhujiActivity.this.sendBroadcast(intent);
                        }
                        if(VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())&&(bdlocation==null||
                                !BaiduLBSUtils.judgeLocationSucess(bdlocation.getLocType()))){
                            AlertView temp = new AlertView(getString(R.string.activity_add_zhuji_wangduoduo_tip_first)
                                    + zhujiID + getString(R.string.activity_add_zhuji_wangduoduo_tip_middle), null,
                                    getString(R.string.pickerview_submit), null, null, AddZhujiActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
                                @Override
                                public void onItemClick(Object o, int position) {
                                    finish();
                                }
                            });
                            temp.setCancelable(false);
                            temp.show();
                        }else{
                            finish();
                        }
                        ToastUtil.shortMessage(getString(R.string.activity_add_zhuji_havezhu_addsuccess));
                       // finish();
                    }
                });
            }else if("-6".equals(result)){
                //跳转配网界面
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Intent intent = new Intent();
                        //主机界面不显示引导页
                        if (isMainList) {
                            intent.setClass(getApplicationContext(), Add8266WifiActivity.class);
                        } else {
                            intent.setClass(getApplicationContext(), AddZhujiGuideActivity.class);
                        }
                        intent.putExtra("isMainList", isMainList);
                        startActivityForResult(intent, 5);
                    }
                });
            } else {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        ToastUtil.shortMessage(getString(R.string.net_error_operationfailed));
                    }
                });
            }
        }
    }
}
