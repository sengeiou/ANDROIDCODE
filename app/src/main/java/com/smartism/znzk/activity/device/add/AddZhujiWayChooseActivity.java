package com.smartism.znzk.activity.device.add;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.camera.RadarAddActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.domain.HeaterShadowInfo;
import com.smartism.znzk.util.Actions;
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
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.view.zbarscan.ScanCaptureActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 主机添加方式选择,优化改善
 * 此页面为wifi，2G，3G，4G，NbIot，lan 等自主联网设备的添加方式选择。
 * 2019年11月26日
 * @author 王建
 */
public class AddZhujiWayChooseActivity extends ActivityParentActivity implements OnClickListener, OnItemClickListener {
    private LinearLayout layout_smartlink,layout_ap,layout_scan,layout_search,layout_serial,layout_sms,layout_camere;
    private String key, pattern;//扫码返回的key和参数

    private InputMethodManager imm;//输入管理
    private AlertView mAlertViewExt,mAlartViewAuthorization;//对话框
    private EditText etName;//序列号输入框

    //主机搜索和提示
    private DeviceSearchPopupWindow searchPopupWindow;
    private int search_count = 0;  //搜索次数
    private final int search_cmax = 10;  //搜索最大次数
    private final int search_time = 5000;  //搜索重发间隔 单位ms

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
                    AddZhujiWayChooseActivity.this.sendBroadcast(new Intent(Actions.SEND_SEARCHZHUJICOMMAND));
                    defaultHandler.sendEmptyMessageDelayed(4, search_time);
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhuji_add_way_choose);
        initView();
        initReceiver();
        BaiduLBSUtils.location(this);
    }

    private void initView() {
        layout_smartlink = (LinearLayout) findViewById(R.id.rl_zhuji_add_smartlink);
        layout_ap = (LinearLayout) findViewById(R.id.rl_zhuji_add_ap);
        layout_scan = (LinearLayout) findViewById(R.id.rl_zhuji_add_scan);
        layout_search = (LinearLayout) findViewById(R.id.rl_zhuji_add_search);
        layout_serial = (LinearLayout) findViewById(R.id.rl_zhuji_add_serial);
        layout_sms = (LinearLayout) findViewById(R.id.rl_zhuji_add_sms);
        layout_camere = (LinearLayout) findViewById(R.id.rl_zhuji_add_camera);
        layout_smartlink.setOnClickListener(this);
        layout_ap.setOnClickListener(this);
        layout_scan.setOnClickListener(this);
        layout_search.setOnClickListener(this);
        layout_serial.setOnClickListener(this);
        layout_sms.setOnClickListener(this);
        layout_camere.setOnClickListener(this);
    }
    private void initReceiver(){
        // 注册广播
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Actions.SEARCH_ZHUJI_RESPONSE);
        receiverFilter.addAction(Actions.CONNECTIVITY_CHANGE);
        receiverFilter.addAction(Actions.SEND_SEARCHZHUJICOMMAND);
        registerReceiver(defaultReceiver, receiverFilter);
    }

    public void back(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.rl_zhuji_add_smartlink:
                intent.setClass(getApplicationContext(), Add8266WifiActivity.class);
                intent.putExtra("isMainList", false);
                intent.putExtra(HeaterShadowInfo.type, getIntent().getStringExtra(HeaterShadowInfo.type));
                startActivityForResult(intent, 5);
                break;
            case R.id.rl_zhuji_add_ap:
                intent.setClass(getApplicationContext(), AddZhujiByAPActivity.class);
                intent.putExtra("flags",0);
                intent.putExtra(HeaterShadowInfo.type, getIntent().getStringExtra(HeaterShadowInfo.type));
                startActivity(intent);
                break;
            case R.id.rl_zhuji_add_scan:
                intent.setClass(this,ScanCaptureActivity.class);
                intent.putExtra("isZhujiFragment", true);
                startActivityForResult(intent,3333);
                break;
            case R.id.rl_zhuji_add_search:
                showProgressDialogSearchZhuji();
                break;
            case R.id.rl_zhuji_add_serial:
                addDeviceBySerial();
                break;
            case R.id.rl_zhuji_add_sms:
                //短信配网
                intent.setClass(getApplicationContext(), GeneralCollectionWifiActivity.class);
                intent.putExtra("title",getString(R.string.zhuji_perwang_sms_title));
                intent.putExtra("need_exit",true);
                intent.putExtra("exit_activity", AddZhujiWayChooseActivity.class);
                intent.putExtra("next_activity", GSMDistributionNetworkActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_zhuji_add_camera:
                intent.setClass(mContext.getApplicationContext(), RadarAddActivity.class);
                //判断是否是主机列表Fragment
                intent.putExtra("isMainList", true);
                intent.putExtra("int", 3);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /**
     * 添加设备，通过序列号
     */
    private void addDeviceBySerial() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //拓展窗口
//        mAlertViewExt = new AlertView(getString(R.string.tips), getString(R.string.activity_add_zhuji_idzhu_msg), getString(R.string.cancel), null, new String[]{getString(R.string.compele)}, this, Style.Alert, this);
        mAlertViewExt = new AlertView(getString(R.string.activity_add_idzhu_msg), null, getString(R.string.cancel), null, new String[]{getString(R.string.compele)}, this, AlertView.Style.Alert, this);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3333 && resultCode == Constant.CAPUTRE_ADDRESULT) {
            key = data.getStringExtra("value");
            pattern = data.getStringExtra("pattern");
            if (key == null) {
                Toast.makeText(this, getString(R.string.net_error_programs), Toast.LENGTH_SHORT).show();
                return;
            }
            showInProgress(getString(R.string.activity_add_zhuji_havezhu_ongoing), false, false);
            JavaThreadPool.getInstance().excute(new AddZhujiWayChooseActivity.Matching());
        } else if (requestCode == 5 && resultCode == 8) {
            finish();
        }
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
    public void onItemClick(Object o, int position) {
        closeKeyboard();
        //判断是否是拓展窗口View，而且点击的是非取消按钮
        if (o == mAlertViewExt && position != AlertView.CANCELPOSITION) {
            String name = etName.getText().toString();
            if (name.isEmpty()) {
                ToastUtil.shortMessage(getString(R.string.activity_add_zhuji_idzhu_empty));
            }
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

    /**
     * 扫码后 添加设备
     */
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
                        .requestoOkHttpPost(
                                server + "/jdm/s3/d/mshare", pJsonObject,
                                AddZhujiWayChooseActivity.this);
                // 临时添加
            } else if (pattern.equals("status_temp")) {
                result = HttpRequestUtils
                        .requestoOkHttpPost(
                                server + "/jdm/s3/d/tmshare", pJsonObject,
                                AddZhujiWayChooseActivity.this);
            }
            if ("-3".equals(result)) {
                mHandler.post(() -> {
                        cancelInProgress();
                        Toast.makeText(AddZhujiWayChooseActivity.this, getString(R.string.activity_add_zhuji_havezhu_shixiao), Toast.LENGTH_LONG).show();
                });
            } else if ("0".equals(result)) {
                mHandler.post(() -> {
                    cancelInProgress();
                    //重新获取设备列表
                    SyncMessageContainer.getInstance()
                            .produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                    Toast.makeText(AddZhujiWayChooseActivity.this, getString(R.string.activity_add_zhuji_havezhu_addsuccess), Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        }
    }

    /**
     * 序列号添加设备
     */
    String sqr = "";
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

//            String result = HttpRequestUtils.requestHttpServer( server + "/jdm/service/find?v=" + URLEncoder.encode(SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP)), AddZhujiWayChooseActivity.this, defaultHandler);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/find", pJsonObject, AddZhujiWayChooseActivity.this);
            if (result != null && result.startsWith("-3")) {
                try {
                    sqr = URLDecoder.decode(result.substring(2), "UTF-8");
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
//                int count = sqr.split(",").length;
//                String message = "";
//                if (count > 1) {
//                    message += getString(R.string.activity_add_zhuji_nozhu_type_p_more);
//                    message += "\"";
//                    message += sqr;
//                    message += "\"";
//                    message += getString(R.string.activity_add_zhuji_nozhu_type_f_more);
//                } else {
//                    message += getString(R.string.activity_add_zhuji_nozhu_type_p_one);
//                    message += "\"";
//                    message += sqr;
//                    message += "\"";
//                    message += getString(R.string.activity_add_zhuji_nozhu_type_f_one);
//                }
//                sqr = message;
                try {
                    Thread.sleep(1000);//不暂停下 提示框出不来
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        if (mAlartViewAuthorization == null || !mAlartViewAuthorization.isShowing()) {
                            //需要授权
                            mAlartViewAuthorization = new AlertView(getString(R.string.activity_add_zhuji_nozhu_typetitle),
                                    getString(R.string.add_zhuji_by_ap_permission_tip, sqr),
                                    null,
                                    new String[]{getString(R.string.confirm)}, null,
                                    AddZhujiWayChooseActivity.this, AlertView.Style.Alert, (Object o, final int position) -> {
                                if (position != -1) {
                                    finish();
                                }
                            });
                            mAlartViewAuthorization.show();
                        }
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
                        if (Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                            Intent intent = new Intent(Actions.ADD_NEW_ZHUJI);
                            intent.putExtra("masterId", zhujiID);
                            AddZhujiWayChooseActivity.this.sendBroadcast(intent);
                        }
                        if(Actions.VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())&&(bdlocation==null||
                                !BaiduLBSUtils.judgeLocationSucess(bdlocation.getLocType()))){
                            AlertView temp = new AlertView(getString(R.string.activity_add_zhuji_wangduoduo_tip_first)
                                    + zhujiID + getString(R.string.activity_add_zhuji_wangduoduo_tip_middle), null,
                                    getString(R.string.pickerview_submit), null, null, AddZhujiWayChooseActivity.this, AlertView.Style.Alert, new OnItemClickListener() {
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
                        intent.setClass(getApplicationContext(), AddZhujiGuideActivity.class);
                        intent.putExtra("isMainList", false);
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

    private BroadcastReceiver defaultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (Actions.SEND_SEARCHZHUJICOMMAND.equals(intent.getAction())) { //定时器1S重新再发送刷新事件
                search_count++;
                if (search_count >= search_cmax) {
                    dismissProgressDialogSearchZhuji();
                    ToastUtil.longMessage(getString(R.string.activity_add_zhuji_nozhu_again));
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
                    LogUtil.e(AddZhujiWayChooseActivity.this,"AddZhuji","not support UTF-8");
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
                    if (mAlartViewAuthorization == null || !mAlartViewAuthorization.isShowing()) {
                        //需要授权
                        mAlartViewAuthorization = new AlertView(getString(R.string.activity_add_zhuji_nozhu_typetitle),
                                getString(R.string.add_zhuji_by_ap_permission_tip, name),
                                null,
                                new String[]{getString(R.string.confirm)}, null,
                                AddZhujiWayChooseActivity.this, AlertView.Style.Alert, (Object o, final int position) -> {
                            if (position != -1) {
                                finish();
                            }
                        });
                        mAlartViewAuthorization.show();
                    }
                } else {
                    //无操作 未搜索到主机
                }
            }
        }
    };

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
            searchPopupWindow.setOutsideTouchable(true);
            searchPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

                @Override
                public void onDismiss() {
                    cancelCheckZhuji();
                }
            });
            defaultHandler.sendEmptyMessageDelayed(4, search_time);


//			if (alarmManager==null) {
//				alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//				checkZhujiIntent = PendingIntent.getBroadcast(getApplicationContext(), pendingRequestCode++, new Intent(Actions.SEND_SEARCHZHUJICOMMAND), PendingIntent.FLAG_UPDATE_CURRENT);
//				alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, search_time, search_time, checkZhujiIntent);
//			}
        } else {
            searchPopupWindow.setOutsideTouchable(true);
            cancelCheckZhuji();
            searchPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

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
                    Intent intent = new Intent();
                    intent.setClassName("com.android.settings","com.android.settings.Settings$WifiSettingsActivity");
                    startActivity(intent);
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
            searchPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

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
        search_count = 0;
//		if (alarmManager!=null) {
//			alarmManager.cancel(checkZhujiIntent);
//			alarmManager = null;
//		}
    }
}
