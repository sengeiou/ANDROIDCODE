package com.smartism.znzk.activity.device;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.p2p.core.P2PView;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentMonitorActivity;
import com.smartism.znzk.activity.SmartMedicine.SmartMedicineMainActivity;
import com.smartism.znzk.activity.alert.ChooseAudioSettingMode;
import com.smartism.znzk.activity.camera.AlarmPictrueActivity;
import com.smartism.znzk.activity.camera.MainActivity;
import com.smartism.znzk.activity.common.HongCaiTantouSettingActivity;
import com.smartism.znzk.activity.common.SettingActivity;
import com.smartism.znzk.activity.yaokan.YKDownLoadCodeActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.camera.P2PConnect;
import com.smartism.znzk.activity.camera.ApMonitorActivity;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessage.CodeMenu;
import com.smartism.znzk.communication.protocol.SyncMessage.CommandMenu;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceInfo.ControlTypeMenu;
import com.smartism.znzk.domain.DeviceKeys;
import com.smartism.znzk.domain.GroupInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.global.AppConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.Actions.VersionType;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.NotificationUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.util.camera.WifiUtils;
import com.smartism.znzk.view.BadgeView;
import com.smartism.znzk.view.CheckSwitchButton;
import com.smartism.znzk.view.DevicesMenuPopupWindow;
import com.smartism.znzk.view.MyGridView;
import com.smartism.znzk.view.SelectAddPopupWindow;
import com.smartism.znzk.widget.HeaderView;
import com.smartism.znzk.widget.MyInputPassDialog;
import com.smartism.znzk.widget.NormalDialog;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupInfoActivity extends ActivityParentMonitorActivity implements OnClickListener {
    // handler常量
    private final int dHandlerWhat_initsuccess = 1,
            dHandlerWhat_loadsuccess = 2,
            dHandlerWhat_serverupdatetimeout = 3,
            dHandler_timeout = 4,
            dHandler_timerc = 5,
            dHander_refresh = 6,
            dHandler_scenes = 7,
            dHandler_key_timeout = 8,
            dHandler_getiplist = 9,
            dHandlerWhat_deletesuccess = 10,
            dHandler_devicekeys = 11,
            dHandler_initContast = 12;
    private int index = 0;//长按时下标
    private ImageView back;
    private int sortType = 0; // 主页面排序类型 0为智能类型， 1为排序类型
    public GroupInfoActivity mContext;
    private ZhujiInfo zhuji;
    private DeviceInfo operationDevice;// 操作的对象，主机也会生成此对象并写入主机的属性
    private DeviceInfo groupDevice;
    private ListView devices_ListView;
    private TextView title;
    //宏才
    RelativeLayout inputNameParent ;
    EditText name_edit ;
    TextView group_edit_save ;
    // 长按菜单时间
    private DevicesMenuPopupWindow itemMenu;
    private DeviceAdapter deviceAdapter;
    public List<DeviceInfo> deviceInfos;
    private String old_refulsh_device_id = "";
    private boolean initSuccess = false;
    public static String currentScene = null;
    private int devicenum = 0;
    public NormalDialog dialog;
    // 自定义的弹出框类 右边菜单
    public SelectAddPopupWindow menuWindow; // 弹出框

    public static int key;//判断按键是震动 1、有声 2、无声0
    private DeviceInfo deviceInfo;
    private boolean isScrolling;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case dHandlerWhat_initsuccess: // 初始化加载数据完成
                    cancelInProgress();
                    deviceInfos.clear();
                    deviceInfos.addAll((List<DeviceInfo>) msg.obj);
                    LogUtil.i(TAG, "设备列表，初始化数据集合大小为:" + deviceInfos.size());
                    for (DeviceInfo d : deviceInfos) {
                        List<DeviceKeys> deviceKeyses = DatabaseOperator.getInstance(GroupInfoActivity.this).findDeviceKeysByDeviceId(d.getId());
                        if (deviceKeyses != null) map.put(d.getId(), deviceKeyses);
                    }
                    deviceAdapter.notifyDataSetChanged();
                    initSuccess = true;
                    defaultHandler.sendEmptyMessageDelayed(dHandler_timerc, 60000);
                    break;
                case dHandlerWhat_loadsuccess: // 加载完成0
                    cancelInProgress();
                    defaultHandler.removeMessages(dHandler_timeout);
                    deviceInfos.clear();
                    deviceInfos.addAll((List<DeviceInfo>) msg.obj);
                    if (deviceInfos.size()==0){
                        finish();
                    }else {
                        deviceAdapter.notifyDataSetChanged();
                    }
                    break;
                case dHandlerWhat_serverupdatetimeout:
                    cancelInProgress();
                    Toast.makeText(getApplicationContext(), getString(R.string.deviceslist_server_updating_timeout),
                            Toast.LENGTH_SHORT).show();
                    break;
                case dHandlerWhat_deletesuccess: // 删除成功
                    refreshData();
                    break;
                case dHandler_timeout:
                    cancelInProgress();
                    Toast.makeText(getApplicationContext(), getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    break;
                case dHandler_timerc: // 1分钟执行一次，检查一键上行设备是否正常。
                    for (DeviceInfo info : deviceInfos) {
                        if (ControlTypeMenu.shangxing_1.value().equals(info.getControlType())) {
                            if (!"".equals(info.getLastUpdateTime())) {
                                if (info.getLastUpdateTime() < System.currentTimeMillis() - 12 * 60 * 60000) { // 指令超过12小时没有持续的包下来则表示正常了
                                    info.setLastUpdateTime(0);
                                    info.setLastCommand(getString(R.string.deviceslist_server_item_normal));
                                }
                            }
                        }
                    }
                    defaultHandler.sendEmptyMessageDelayed(dHandler_timerc, 60000);
                    break;
                case dHander_refresh: // 刷新超时
                    Toast.makeText(getApplicationContext(), getString(R.string.timeout), Toast.LENGTH_SHORT).show();
                    break;
                case dHandler_scenes: // 场景操作完成
                    cancelInProgress();
                    break;

                case dHandler_initContast:
                    if (deviceIntent != null) {
                        Contact contact = (Contact) msg.obj;
                        List<CameraInfo> cameraInfos = getCameraList();
                        if (cameraInfos != null && cameraInfos.size() > 0) {
                            for (CameraInfo cameraInfo : cameraInfos) {
                                if (cameraInfo.getId().equals(contact.contactId)) {
                                    deviceIntent.putExtra("contact", contact);
                                }
                            }
                        }
                        deviceIntent.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
                    }
//                    unResterReceiver();
                    startActivity(deviceIntent);
                    reject();
                    break;
                case dHandler_devicekeys:
                    deviceAdapter.notifyDataSetChanged();
                    break;
                //摄像头部分
                case 9:// 获取数据成功
                    for (int i = 0; i < ipcList.length; i++) {
                        if (ipcList[i].equals(String.valueOf(msg.what))) {
                            currentNumber = i;
                            P2PHandler.getInstance().reject();
                            changeDeviceListTextColor();
                            callId = ipcList[currentNumber];
                            callDevice();
                            iv_last.setClickable(false);
                        }
                    }
                    break;
                case dHandler_key_timeout:
                    cancelInProgress();
                    T.showShort(mContext, R.string.timeout);
                    break;

                case dHandler_camera_0:
                    Log.e("dxswifi", "rtsp失败");
                    showError("connect error", 0);
                    P2PHandler.getInstance().reject();
                    break;
                case dHandler_camera_1:
                    Log.e("dxswifi", "rtsp成功");
                    rlPrgTxError.setVisibility(View.GONE);
                    P2PConnect.setCurrent_state(2);
                    playReady();
                    mContact.apModeState = Constants.APmodeState.LINK;
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);
    Intent deviceIntent = null;
    private BroadcastReceiver defaultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, final Intent intent) {
            if (intent.getAction().equals(
                    Constants.P2P.P2P_MONITOR_NUMBER_CHANGE)) {
                int number = intent.getIntExtra("number", -1);
                if (number != -1) {
                    users.setText(mContext.getResources().getString(
                            R.string.monitor_number)
                            + " " + P2PConnect.getNumber());
                }
            } else if (Actions.REFRESH_DEVICES_LIST.equals(intent.getAction())) { // 数据刷新完成广播
                cancelInProgress();
                refreshData();
            } else if (intent.getAction().equals(Constants.Action.ACTION_NETWORK_CHANGE)) {
                boolean isNetConnect = false;
                ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetInfo != null) {
                    if (activeNetInfo.isConnected()) {
                        isNetConnect = true;
                        WifiManager wifimanager = (WifiManager) mContext.getApplicationContext().getSystemService(mContext.WIFI_SERVICE);
                        if (wifimanager == null) {
                            return;
                        }
                        WifiInfo wifiinfo = wifimanager.getConnectionInfo();
                        if (wifiinfo == null) {
                            return;
                        }
                        if (wifiinfo.getSSID().length() > 0) {
                            String wifiName = Utils.getWifiName(wifiinfo.getSSID());
                            if (wifiName.startsWith(AppConfig.Relese.APTAG)) {
                                String id = wifiName.substring(AppConfig.Relese.APTAG.length());
                                FList.getInstance().setIsConnectApWifi(id, true);
                            } else {
                                FList.getInstance().setAllApUnLink();
                            }
                        }
//                        try {
//                            Thread.sleep(1000);
//                        } catch (InterruptedException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
//                        Intent intentNew = new Intent();
//                        intentNew.setAction(Constants.Action.NET_WORK_TYPE_CHANGE);
//                        mContext.sendBroadcast(intentNew);
                        WifiUtils.getInstance().isApDevice();
                    } else {
                        T.showShort(mContext, getString(R.string.network_error) + " " + activeNetInfo.getTypeName());
                    }

                    if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                        NpcCommon.mNetWorkType = NpcCommon.NETWORK_TYPE.NETWORK_WIFI;
                    } else {
                        NpcCommon.mNetWorkType = NpcCommon.NETWORK_TYPE.NETWORK_2GOR3G;
                    }
                } else {
                    Toast.makeText(mContext, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                }

                NpcCommon.setNetWorkState(isNetConnect);

            } else if (intent.getAction().equals(Actions.SHOW_SERVER_MESSAGE)) {
                defaultHandler.removeMessages(dHandler_key_timeout);
                //返回指令操作失败
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
                            Toast.makeText(GroupInfoActivity.this, getString(R.string.tips_4), Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            Toast.makeText(GroupInfoActivity.this, getString(R.string.tips_5), Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(GroupInfoActivity.this, getString(R.string.tips_6), Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(GroupInfoActivity.this, getString(R.string.tips_7), Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            Toast.makeText(GroupInfoActivity.this, getString(R.string.tips_8), Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            Toast.makeText(GroupInfoActivity.this, "Unknown Info", Toast.LENGTH_SHORT).show();
                            break;
                    }

                } else {
                    Toast.makeText(GroupInfoActivity.this, intent.getStringExtra("message"), Toast.LENGTH_SHORT)
                            .show();

                }
                defaultHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                    }
                }, 300);
            } else if (Actions.CONNECTION_FAILED_SENDFAILED.equals(intent.getAction())) { //服务器未连接
                defaultHandler.removeMessages(dHandler_key_timeout);
                //返回指令操作失败
                cancelInProgress();
                Toast.makeText(GroupInfoActivity.this, getString(R.string.rq_control_sendfailed),
                        Toast.LENGTH_SHORT).show();
            } else if (intent.getAction().equals(Actions.ACCETP_ONEDEVICE_MESSAGE)) {
                //返回指令操作成功
                defaultHandler.removeMessages(dHandler_key_timeout);

                if (progressIsShowing()) {
                    Toast.makeText(GroupInfoActivity.this, getString(R.string.rq_control_sendsuccess),
                            Toast.LENGTH_SHORT).show();
                }
                cancelInProgress();
                refreshData();
            } else if (intent.getAction().equals(Constants.P2P.P2P_READY)) {
                P2PHandler.getInstance().getDefenceStates(callId, password, MainApplication.GWELL_LOCALAREAIP);
                isReceveHeader = false;
                isShake = false;
                iv_last.setClickable(true);
                iv_next.setClickable(true);
                pView.sendStartBrod();
            } else if (intent.getAction().equals(Constants.P2P.P2P_ACCEPT)) {
                //" + "2");
                int[] type = intent.getIntArrayExtra("type");
                P2PView.type = type[0];
                P2PView.scale = type[1];
                int Heigh = 0;
                if (P2PView.type == 1 && P2PView.scale == 0) {
                    Heigh = screenWidth * 3 / 4;
                    setIsLand(true);
                } else {
                    Heigh = screenWidth * 9 / 16;
                    setIsLand(false);
                }

            } else if (intent.getAction().equals(Constants.P2P.ACK_RET_CHECK_PASSWORD)) {
                finish();
            } else if (intent.getAction().equals(Constants.P2P.P2P_REJECT)) {
                //" + "3");
                String error = intent.getStringExtra("error");
                int code = intent.getIntExtra("code", 9);
                showError(error, code);
                isShake = false;
                iv_last.setClickable(true);
                iv_next.setClickable(true);
            } else if (intent.getAction().equals(Constants.P2P.RET_GET_REMOTE_DEFENCE)) {
                //设置设防返回广播
                //" + "4");
//                String ids = intent.getStringExtra("contactId");
//                if (!ids.equals("") && ids.equals(callId)) {
//                    defenceState = intent.getIntExtra("state", -1);
//                    changeDefence(defenceState);
//                }
//                if (defence_state != null) defence_state.setVisibility(ImageView.VISIBLE);
            } else if (intent.getAction().equals(Constants.P2P.RET_SET_REMOTE_DEFENCE)) {
                //" + "5");
//                int result = intent.getIntExtra("state", -1);
//                if (result == 0) {
//                    if (defenceState == Constants.DefenceState.DEFENCE_STATE_ON) {
//                        defenceState = Constants.DefenceState.DEFENCE_STATE_OFF;
//                    } else {
//                        defenceState = Constants.DefenceState.DEFENCE_STATE_ON;
//                    }
//                    changeDefence(defenceState);
//                }
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                //" + "6");
                String error = intent.getStringExtra("error");
                showError(error, 0);
            } else if (intent.getAction().equals(
                    Constants.Action.MONITOR_NEWDEVICEALARMING)) {

                Log.e("警报", "跳转");
                finish();
                Intent alarm = new Intent();
                alarm.setClass(mContext, AlarmPictrueActivity.class);
                alarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                alarm.putExtra("deviceid", callId);
                startActivity(alarm);
                Log.e("警报", "跳转2");

            } else if (intent.getAction().equals(Constants.P2P.RET_P2PDISPLAY)) {
                Log.e("monitor", "RET_P2PDISPLAY");
                connectSenconde = true;
                if (!isReceveHeader) {
                    hindRlProTxError();
                    pView.updateScreenOrientation();
                    isReceveHeader = true;
                }
            } else if (intent.getAction().equals(
                    Constants.P2P.DELETE_BINDALARM_ID)) {
                int result = intent.getIntExtra("deleteResult", 1);
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (result == 0) {
                    // 删除成功
                    T.showShort(mContext, R.string.device_set_tip_success);
                } else if (result == -1) {
                    // 不支持
                    T.showShort(mContext, R.string.device_not_support);
                } else {
                    // 失败
                }
            } else if (intent.getAction().equals(
                    Constants.P2P.ACK_GET_REMOTE_DEFENCE)) {
                //" + "7");
                String contactId = intent.getStringExtra("contactId");
                int result = intent.getIntExtra("result", -1);
                if (contactId.equals(callId)) {
                    if (result == Constants.P2P_SET.ACK_RESULT.ACK_INSUFFICIENT_PERMISSIONS) {
                        isPermission = false;
                    }
                }

            }
            /*****************摄像头播放的时候自动改变高度*****************************/
            if (intent.getAction().equals(Constants.P2P.ACK_RET_GET_DEFENCE_STATES)) {
                defenceState = intent.getIntExtra("state", -1);
                changeDefence(defenceState);
            } else if (intent.getAction().equals(Constants.P2P.P2P_READY)) {

                Log.e("monitor", "P2P_READY" + "callId=" + callId);
                P2PHandler.getInstance().getDefenceStates(callId, password, MainApplication.GWELL_LOCALAREAIP);
                isReceveHeader = false;
                isShake = false;
                iv_last.setClickable(true);
                iv_next.setClickable(true);
                P2PConnect.setMonitorId(callId);
            } else if (intent.getAction().equals(Constants.P2P.P2P_ACCEPT)) {

                int[] type = intent.getIntArrayExtra("type");
                P2PView.type = type[0];
                P2PView.scale = type[1];
                int Heigh = 0;
                if (P2PView.type == 1 && P2PView.scale == 0) {
                    Heigh = screenWidth * 3 / 4;
                    setIsLand(true);
                } else {
                    Heigh = screenWidth * 9 / 16;
                    setIsLand(false);
                }
                if (ScrrenOrientation == Configuration.ORIENTATION_PORTRAIT) {

                    LinearLayout.LayoutParams parames = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    parames.height = Heigh;
                    r_p2pview.setLayoutParams(parames);
                }
                P2PHandler.getInstance().openAudioAndStartPlaying(1);
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
    }

    ;

    /*
        @Override
        public void onDestroy() {
            super.onDestroy();
            if (itemMenu != null) {
                itemMenu.dismiss();
                itemMenu = null;
            }
            defaultHandler.removeCallbacksAndMessages(null);
            defaultHandler = null;
            unregisterReceiver(defaultReceiver);
            destroyActivity();
        }*/
    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyActivity();
    }

    private void destroyActivity() {
        if (menuWindow != null) {
            menuWindow.dismiss();
            menuWindow = null;
        }
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    mCurrentVolume, 0);
        }
        if (isRegFilter) {
            unregisterReceiver(defaultReceiver);
            isRegFilter = false;
        }
        if (defaultReceiver != null) {
            unregisterReceiver(defaultReceiver);
            defaultReceiver = null;
        }
        if (mContact != null) {
            if (!StringUtils.isEmpty(groupDevice.getBipc()) && !"0".equals(groupDevice.getBipc())) {
//                P2PConnect.setMonitorId("");// 设置在监控的ID为空
//                SettingListener.setMonitorID("");
                if (sensorListener != null) {
                    sensorManager.unregisterListener(sensorListener);
                }

                Intent refreshContans = new Intent();
                refreshContans.setAction(Constants.Action.REFRESH_CONTANTS);
                sendBroadcast(refreshContans);
            }
        }

    }

    private List<DeviceInfo> getDeviceInfo() {
        return this.deviceInfos;
    }


    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        mContext = this;
        initView();
        initViewEvent();
        initRegisterReceiver();
        initData();
        if (mContact != null) {
            initPlayCamera();
            btn_play.setVisibility(View.VISIBLE);
        }
    }

    public void edit(View view) {
        //宏才，修改组名字
//        if(VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
          if(MainApplication.app.getAppGlobalConfig().isShowNewAddGroup()){
            if(inputNameParent.getVisibility()==View.GONE){
                devices_ListView.setVisibility(View.GONE);
                inputNameParent.setVisibility(View.VISIBLE);
                group_edit_save.setText(getString(R.string.save));
                String name = title.getText().toString();
                //填充到EditText中的名字不包含组名称
                if(name.length()>1){
                    name = name.substring(0,name.length());
                }else{
                    name = "";
                }
                name_edit.setText(name);
                name_edit.setSelection(name_edit.getText().toString().length());
                name_edit.setFocusable(true);
                name_edit.setFocusableInTouchMode(true);
                name_edit.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
            }else{
                showInProgress(getString(R.string.loading), false, true);
                //提交修改的结果
                JavaThreadPool.getInstance().excute(new Runnable() {
                    @Override
                    public void run() {
//                        zhuji = DatabaseOperator.getInstance(GroupInfoActivity.this)
//                                .queryDeviceZhuJiInfo(dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, ""));
                        //替换
                        zhuji = DatabaseOperator.getInstance(GroupInfoActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
                        if (zhuji == null) {
                            return;
                        }

                        String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                        String name = title.getText().toString();
                        if(name.length()>1){
                            name = name.substring(1,2);
                        }else{
                            name = name.substring(0,1);
                        }
                        if(name_edit.getText().length()>0){
                            name = "("+name+")";
                        }
                        JSONObject pJsonObject = new JSONObject();
                        pJsonObject.put("id", groupDevice.getId());
                        pJsonObject.put("logo", groupDevice.getLogo());
                        pJsonObject.put("name",name+name_edit.getText().toString());
                        pJsonObject.put("masterid", zhuji.getMasterid());
                        JSONArray array = new JSONArray();
                        for (DeviceInfo d : deviceInfos) {
                            JSONObject o = new JSONObject();
                            o.put("id", d.getId());
                            array.add(o);
                        }
                        pJsonObject.put("ids", array);
                        String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/dg/update", pJsonObject, GroupInfoActivity.this);
                        if ("0".equals(result)) {
                            final String finalName = name;
                            defaultHandler.post(new Runnable() {

                                @Override
                                public void run() {
                                    //设置成功
                                    cancelInProgress();
                                    refreshData();
                                    //隐藏修改名字视图
                                    title.setText(finalName +name_edit.getText());
                                    if(inputNameParent.getVisibility()==View.VISIBLE){
                                        devices_ListView.setVisibility(View.VISIBLE);
                                        inputNameParent.setVisibility(View.GONE);
                                        group_edit_save.setText(getString(R.string.edit));
                                    }
                                    Toast toast = Toast.makeText(mContext,"",Toast.LENGTH_SHORT);
                                    toast.setText(getString(R.string.activity_editscene_set_success));
                                    toast.show();
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    if(imm.isActive()&&name_edit.getWindowToken()!=null){
                                        imm.hideSoftInputFromWindow(name_edit.getWindowToken(),0);
                                    }

                                }
                            });
                        }else{
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    cancelInProgress();
                                    Toast toast = Toast.makeText(mContext,"",Toast.LENGTH_SHORT);
                                    toast.setText(getString(R.string.activity_editscene_set_falid));
                                    toast.show();
                                }
                            });
                        }
                    }
                });
            }
            return  ;
        }
        menuWindow.showAtLocation(view, Gravity.TOP | Gravity.RIGHT, 0,
                Util.dip2px(getApplicationContext(), 55) + Util.getStatusBarHeight(this));

    }

    private List<CameraInfo> getCameraList() {
        List<CameraInfo> camera = new ArrayList<>();
        DeviceInfo deviceInfo = null;
//        zhuji = DatabaseOperator.getInstance(getApplicationContext()).queryDeviceZhuJiInfo(
//                DataCenterSharedPreferences.getInstance(GroupInfoActivity.this, DataCenterSharedPreferences.Constant.CONFIG)
//                        .getString(DataCenterSharedPreferences.Constant.APP_MASTERID, ""));
        //替换
        zhuji = DatabaseOperator.getInstance(getApplicationContext()).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());

        //StartSearchDevice(); 这个好像是搜索局域网内的在线摄像头。不需要
        List<DeviceInfo> dInfos = DatabaseOperator.getInstance(GroupInfoActivity.this).queryAllDeviceInfos(zhuji.getId());
        if (dInfos != null && !dInfos.isEmpty()) {
            for (DeviceInfo device : dInfos) {
                if (device.getCak().equals("surveillance")) {
                    deviceInfo = device;
                    break;

                }
            }
        }

        if (deviceInfo != null && deviceInfo.getCak().equals("surveillance")) {
            List<CameraInfo> list = (List<CameraInfo>) JSON.parseArray(deviceInfo.getIpc(), CameraInfo.class);
            camera.addAll(list);
        }
        return camera;
    }


    //宏才
    public void clearEditText(View v){
        //清空输入
        name_edit.setText("");
    }
    /**
     * 初始化视图，组件等
     */
    private void initView() {
        //宏才
        inputNameParent = findViewById(R.id.inputNameParent);
        name_edit = findViewById(R.id.name_edit);
        group_edit_save = findViewById(R.id.group_edit_save);

        menuWindow = new SelectAddPopupWindow(GroupInfoActivity.this, this, 0);
        title = (TextView) findViewById(R.id.group_title);
        back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(this);
        devices_ListView = (ListView) findViewById(R.id.dv);
        deviceInfos = new ArrayList<DeviceInfo>();
        deviceAdapter = new DeviceAdapter(this);
        devices_ListView.setAdapter(deviceAdapter);
        itemMenu = new DevicesMenuPopupWindow(GroupInfoActivity.this, this);
    }


    private void initViewEvent() {
        devices_ListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                index = position;
                operationDevice = deviceInfos.get(position);
                operationDevice.setwIndex(position);
                itemMenu.updateDeviceMenu(GroupInfoActivity.this, operationDevice, dcsp,zhuji);
                itemMenu.showAtLocation(devices_ListView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
                return true;
            }

        });
        devices_ListView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    //空閒狀態
                    case OnScrollListener.SCROLL_STATE_IDLE:
                        Log.i("lege", "空閒狀態");
                        isScrolling = false;
                        break;
                    //滑動狀態
                    case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        Log.i("lege", "滑動狀態");
                        isScrolling = true;
                        break;
                    //慣性
                    case OnScrollListener.SCROLL_STATE_FLING:
                        Log.i("lege", "慣性");
                        isScrolling = true;
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        devices_ListView.setOnItemClickListener(new OnItemClickListener() {
            private DeviceInfo device;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    device = deviceInfos.get(position);
                } catch (IndexOutOfBoundsException e) {
                    return;
                }
                if (device.getCa() != null && device.getCa().contentEquals("hwzf")) {
                    Intent mIntent = new Intent();
                    mIntent.setClass(mContext, YKDownLoadCodeActivity.class);
                    mIntent.putExtra("device", device);
                    mIntent.putExtra("group", groupDevice);
                    mIntent.putExtra("camera", mContact);
                    mIntent.putExtra("did", device.getId());
                    mIntent.putExtra("masterId", zhuji.getMasterid());
                    startActivity(mIntent);
                    return;
                }
                if (device.getControlType().equals(ControlTypeMenu.neiqian.value())) {
                    String cpackage = device.getApppackage();
                    if (Util.appIsInstalled(GroupInfoActivity.this, cpackage)) {
                        // 已经安装，打开
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setComponent(new ComponentName(cpackage.substring(0, cpackage.lastIndexOf("/")),
                                cpackage.replace("/", ".")));
                        startActivity(intent);
                    } else {
                        // 提示安装
                        Toast.makeText(GroupInfoActivity.this, getString(R.string.deviceslist_server_addnewapptips),
                                Toast.LENGTH_LONG).show();
                        String downloadString = device.getAppdownload();
                        if (downloadString.startsWith("jdmapk://")) {
                            // 内嵌apk
                            File file = new File(
                                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/jdm_app_tmp.apk");
                            int resourct = 0;
                            if (downloadString.substring(9).equals("p2pipcam_hvcipc_6_5")) {
//                                resourct = R.raw.p2pipcam_hvcipc_6_5;
                            }
                            try {
                                FileUtils.copyInputStreamToFile(getResources().openRawResource(resourct), file);
                            } catch (NotFoundException e) {
                            } catch (IOException e) {
                            }
                            Util.install(GroupInfoActivity.this, Uri.fromFile(file));
                        } else if (downloadString.startsWith("http")) {
                            // http连接，需要下载。
                        }
                    }
                } else {
                    if (device.getCa().equals("sst")) { //摄像头
                        dialog = new NormalDialog(mContext);
                        dialog.showLoadingDialog3();
                        dialog.setCanceledOnTouchOutside(false);
                        Intent intent = new Intent();
                        intent.setClass(mContext, MainActivity.class);
                        intent.putExtra("device", device);
                        Log.e("数据", device.getIpc());
                        startActivity(intent);
                    } else { //其他
                        if(DeviceInfo.CaMenu.znyx.value().equals(device.getCa())){
                            //智能药箱
                            deviceIntent = new Intent();
                            deviceIntent.setClass(mContext.getApplicationContext(), SmartMedicineMainActivity.class);
                            deviceIntent.putExtra("device", device);
                        }else if(DeviceInfo.CaMenu.xueyaji.value().equals(device.getCa())){
                            deviceIntent = new Intent();
//                            deviceIntent.setClass(mContext.getApplicationContext(), XYJMainActivity.class);
                            deviceIntent.putExtra("zhuji", zhuji);
                            deviceIntent.putExtra("device", device);
                      //      deviceIntent.putExtra("mid", mId);
                        }else if(DeviceInfo.CaMenu.tizhongceng.value().equals(device.getCa())){
                            deviceIntent = new Intent();
//                            deviceIntent.setClass(mContext.getApplicationContext(), WeightMainActivity.class);
                            deviceIntent.putExtra("zhuji", zhuji);
                            deviceIntent.putExtra("device", device);
                      //      deviceIntent.putExtra("mid", mId);
                        }else{

                            deviceIntent = new Intent();
                            deviceIntent.setClass(GroupInfoActivity.this, DeviceInfoActivity.class);
                            deviceIntent.putExtra("device", device);
                            deviceIntent.putExtra("group", groupDevice);
                            deviceIntent.putExtra("camera", mContact);
                            showInProgress(getString(R.string.loading), false, true);
                            cancelInProgress();
//                        unResterReceiver();
                        }
                        startActivity(deviceIntent);
                    }

                }
            }
        });
    }

    /**
     * 获取红外设备的指令信息
     *
     * @param did 红外设备id号
     * @param b   是否下载码
     * @return
     */
    private String getYaokanCode(long did, Boolean b) {
        JSONObject object = new JSONObject();
        object.put("did", did);
        object.put("c", b);
        String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
        String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/infr/get", object, mContext);
        return result;
    }


    private void initRegisterReceiver() {
        // 注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(Actions.REFRESH_DEVICES_LIST);
        filter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        filter.addAction(Actions.CONNECTION_FAILED_SENDFAILED);
        //摄像头
        filter.addAction(Constants.P2P.P2P_ACCEPT);
        filter.addAction(Constants.P2P.P2P_READY);
        filter.addAction(Constants.P2P.P2P_REJECT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Constants.P2P.ACK_RET_CHECK_PASSWORD);
        filter.addAction(Constants.P2P.RET_GET_REMOTE_DEFENCE);
        filter.addAction(Constants.P2P.RET_SET_REMOTE_DEFENCE);
        filter.addAction(Constants.P2P.P2P_RESOLUTION_CHANGE);
        filter.addAction(Constants.P2P.DELETE_BINDALARM_ID);
        filter.addAction(Constants.Action.MONITOR_NEWDEVICEALARMING);
        filter.addAction(Constants.P2P.RET_P2PDISPLAY);
        filter.addAction(Constants.P2P.ACK_GET_REMOTE_DEFENCE);
        filter.addAction(Constants.P2P.ACK_RET_GET_DEFENCE_STATES);
        filter.addAction(Constants.Action.ACTION_NETWORK_CHANGE);

        //指令返回
        filter.addAction(Actions.SHOW_SERVER_MESSAGE);
        filter.addAction(Actions.CONNECTION_FAILED_SENDFAILED);

        filter.addAction( Constants.P2P.P2P_MONITOR_NUMBER_CHANGE);

        registerReceiver(defaultReceiver, filter);
    }

    private void initData() {
//        zhuji = DatabaseOperator.getInstance(GroupInfoActivity.this)
//                .queryDeviceZhuJiInfo(dcsp.getString(Constant.APP_MASTERID, ""));
        //替换
        zhuji = DatabaseOperator.getInstance(GroupInfoActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());

        groupDevice = (DeviceInfo) getIntent().getSerializableExtra("device");
        mContact = (Contact) getIntent().getSerializableExtra("contact");
        title.setText(groupDevice.getName());

        JavaThreadPool.getInstance().excute(new loadAllDevicesInfo(dHandlerWhat_initsuccess));
        JavaThreadPool.getInstance().excute(new CommandKeyLoad(groupDevice));
    }

    private void refreshData() {
        JavaThreadPool.getInstance().excute(new loadAllDevicesInfo(dHandlerWhat_loadsuccess));
    }

    Map<Long, List<DeviceKeys>> map = new HashMap<>();

    @SuppressLint("NewApi")
    class DeviceAdapter extends BaseAdapter {
        /**
         * 视图内部类
         *
         * @author Administrator
         */
        class DeviceInfoView {
            private ImageView low, ioc, ioc_wendu, ioc_shidu, ioc_showright, ioc_arming, ioc_disarming, ioc_home, ioc_panic;
            private TextView mode, name, time, command, command_shidu, type, type_left, type_right;
            private MyGridView keysgGridView;
            private List<MyGridView> keysgGridViews; //正常的指令面板 每一项下有一个

            private ImageButton button;
            private CheckSwitchButton switchButton;
            private RelativeLayout device_item_layout;
            private LinearLayout cLayout, rLayout, sceneLayout;
            private FrameLayout fLayout;
            private BadgeView badgeView;
            private Map<String, KeyItemAdapter> keyItemAdapters = new HashMap<>();
            private List<List<DeviceKeys>> deviceKeyses = new ArrayList<>();
            private List<Long> indexs;

            private DeviceInfoView() {

            }

            private void showkeyList(List<DeviceKeys> ds, boolean isControl, int index) {
                if (isControl) {
//					deviceKeyses.add(ds);
                    KeyItemAdapter keyItemAdapter = new KeyItemAdapter(GroupInfoActivity.this, ds, index);
                    keyItemAdapters.put(String.valueOf(index), keyItemAdapter);
                    keysgGridView.setAdapter(keyItemAdapter);
                }
            }
        }

        LayoutInflater layoutInflater;

        private DeviceAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return deviceInfos.size();
        }

        @Override
        public Object getItem(int arg0) {
            return deviceInfos.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        /**
         * 返回一个view视图，填充gridview的item
         */
        @SuppressLint("NewApi")
        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            DeviceInfoView viewCache = null;
            if (view == null) {
                viewCache = new DeviceInfoView();
                view = layoutInflater.inflate(R.layout.activity_group_deviceslist_item, null);
                viewCache.device_item_layout = (RelativeLayout) view.findViewById(R.id.device_item_layout);
                viewCache.low = (ImageView) view.findViewById(R.id.device_low);
                viewCache.ioc = (ImageView) view.findViewById(R.id.device_logo);
                viewCache.badgeView = new BadgeView(GroupInfoActivity.this, viewCache.ioc);
                viewCache.badgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                viewCache.badgeView.setTextSize(10);
                viewCache.ioc_wendu = (ImageView) view.findViewById(R.id.wendu_img);
                viewCache.ioc_shidu = (ImageView) view.findViewById(R.id.wendu_shidu_img);
                viewCache.mode = (TextView) view.findViewById(R.id.device_mode);
                viewCache.mode.setBackground(Util.createReadBgShapeDrawable(GroupInfoActivity.this));
                viewCache.name = (TextView) view.findViewById(R.id.device_name);
                viewCache.time = (TextView) view.findViewById(R.id.last_time);
                viewCache.type = (TextView) view.findViewById(R.id.device_type);
                viewCache.type_left = (TextView) view.findViewById(R.id.device_type_left);
                viewCache.type_right = (TextView) view.findViewById(R.id.device_type_right);
                viewCache.command = (TextView) view.findViewById(R.id.last_command);
                viewCache.command_shidu = (TextView) view.findViewById(R.id.last_command_shidu);
                viewCache.switchButton = (CheckSwitchButton) view.findViewById(R.id.c_switchButton);
                viewCache.button = (ImageButton) view.findViewById(R.id.c_one_button);
                viewCache.ioc_showright = (ImageView) view.findViewById(R.id.c_img);
                viewCache.rLayout = (LinearLayout) view.findViewById(R.id.r_layout);
                viewCache.cLayout = (LinearLayout) view.findViewById(R.id.c_layout);
                viewCache.sceneLayout = (LinearLayout) view.findViewById(R.id.scene_layout);
                viewCache.ioc_arming = (ImageView) view.findViewById(R.id.scene_arming);
                viewCache.ioc_disarming = (ImageView) view.findViewById(R.id.scene_disarming);
                viewCache.ioc_home = (ImageView) view.findViewById(R.id.scene_home);
                viewCache.ioc_panic = (ImageView) view.findViewById(R.id.scene_panic);
                viewCache.keysgGridView = (MyGridView) view.findViewById(R.id.command_key);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
            List<DeviceKeys> deviceKeyses = map.get(deviceInfos.get(i).getId());

            if ("control".equals(deviceInfos.get(i).getCak())) {
                if(deviceInfos.get(i).getCa().equals("zjykq")){
                    viewCache.keysgGridView.setVisibility(View.GONE);
                }else if(deviceInfos.get(i).getCa().equals("lb")){
                    viewCache.keysgGridView.setVisibility(View.GONE);
                }else{
                    if (deviceKeyses != null && !deviceKeyses.isEmpty()) {
                        viewCache.showkeyList(deviceKeyses, "control".equals(deviceInfos.get(i).getCak()), i);
                        viewCache.keysgGridView.setVisibility(View.VISIBLE);
                    } else {
                        viewCache.keysgGridView.setVisibility(View.GONE);
                    }
                }


            } else {
                viewCache.keysgGridView.setVisibility(View.GONE);
            }
            if (deviceInfos.get(i).getCa().equals("hwzf")) {
                viewCache.keysgGridView.setVisibility(View.GONE);
            }
            initButtonEvent(viewCache, i);
            setDeviceLogoAndName(viewCache, i);
            setCommand(viewCache, i);
            setShowOrHide(viewCache, i);
            setTypeAndBackground(viewCache, i);
            setBadeNumber(viewCache, i);
            setModen(viewCache, i);
            setJKDBackground(view, viewCache, i);

            return view;
        }


        /**
         * 设置设备logo图片和名称
         *
         * @param viewCache
         * @param i
         */
        private void setDeviceLogoAndName(DeviceInfoView viewCache, int i) {
            if (ControlTypeMenu.wenduji.value().equals(deviceInfos.get(i).getControlType())) {
                // 设置图片
                if (VersionType.CHANNEL_UCTECH.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
                    try {
                        viewCache.ioc.setImageBitmap(BitmapFactory.decodeStream(
                                getAssets().open("uctech/uctech_t_" + deviceInfos.get(i).getChValue() + ".png")));
                    } catch (IOException e) {
                        Log.e("uctech", "读取图片文件错误");
                    }
                } else {
                    ImageLoader.getInstance()
                            .displayImage(dcsp.getString(Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/"
                                            + deviceInfos.get(i).getLogo(),
                                    viewCache.ioc, options, new ImageLoadingBar());
                }
                viewCache.name.setText(deviceInfos.get(i).getName() + "CH" + deviceInfos.get(i).getChValue());
            } else if (ControlTypeMenu.wenshiduji.value().equals(deviceInfos.get(i).getControlType())) {
                if (VersionType.CHANNEL_UCTECH.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
                    try {
                        viewCache.ioc.setImageBitmap(BitmapFactory.decodeStream(
                                getAssets().open("uctech/uctech_th_" + deviceInfos.get(i).getChValue() + ".png")));
                    } catch (IOException e) {
                        Log.e("uctech", "读取图片文件错误");
                    }
                } else {
                    ImageLoader.getInstance()
                            .displayImage(dcsp.getString(Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/"
                                            + deviceInfos.get(i).getLogo(),
                                    viewCache.ioc, options, new ImageLoadingBar());
                }
                viewCache.name.setText(deviceInfos.get(i).getName() + "CH" + deviceInfos.get(i).getChValue());
            } else {
                // 设置图片
                ImageLoader.getInstance().displayImage(dcsp.getString(Constant.HTTP_DATA_SERVERS, "")
                        + "/devicelogo/" + deviceInfos.get(i).getLogo(), viewCache.ioc, options, new ImageLoadingBar());
                viewCache.name.setText(deviceInfos.get(i).getName());
            }
        }

        /**
         * 设置指令和时间信息
         *
         * @param viewCache
         * @param i
         */
        private void setCommand(DeviceInfoView viewCache, int i) {
            viewCache.time.setText(formatTime(deviceInfos.get(i).getLastUpdateTime()));
            if (i != 0 && VersionType.CHANNEL_LILESI.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                viewCache.command.setVisibility(View.GONE);
                viewCache.command_shidu.setVisibility(View.GONE);
                viewCache.ioc_shidu.setVisibility(View.GONE);
                viewCache.ioc_wendu.setVisibility(View.GONE);
                return;
            }
            if ("hwzf".equals(deviceInfos.get(i).getCa())) {


                if (!TextUtils.isEmpty(deviceInfos.get(i).getEids())) {
                    JSONArray array = JSONArray.parseArray(deviceInfos.get(i).getEids());
                    if (array != null) {
                        viewCache.command.setText(array.size() + getString(R.string.deviceslist_camera_count));
                    }
                } else {
                    viewCache.command.setText(0 + getString(R.string.deviceslist_camera_count));
                }

                viewCache.command_shidu.setVisibility(View.GONE);
                viewCache.ioc_shidu.setVisibility(View.GONE);
                viewCache.ioc_wendu.setVisibility(View.GONE);
            } else if ("tzc".equals(deviceInfos.get(i).getCa())) {
                try {
                    String unitName = "";
                    long commandUnit = Long.parseLong(deviceInfos.get(i).getLastCommand().substring(0, 4));
                    double commandValue = Integer.parseInt(deviceInfos.get(i).getLastCommand().substring(4), 16) / 10.0;
                    if (commandUnit == 2) {
                        unitName = "KG";
                    }
                    viewCache.command.setText(commandValue + unitName);
                } catch (Exception e) {
                    viewCache.command.setText("error");
                }
            } else if (deviceInfos.get(i).getControlType().equals(ControlTypeMenu.wenshiduji.value())
                    | deviceInfos.get(i).getControlType().equals(ControlTypeMenu.wenduji.value())) {
                String command = deviceInfos.get(i).getLastCommand();
                if (command!=null&&command.contains("℃")) {
                    if (dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("ssd")) {
                        viewCache.command.setText(command.substring(0, command.indexOf("℃") + 1));
                    } else if (dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("hsd")) {
                        viewCache.command.setText(((float) Math
                                .round((Float.parseFloat(command.substring(0, command.indexOf("℃"))) * 1.8 + 32) * 10)
                                / 10) + "℉");
                    }
                    viewCache.ioc_wendu.setVisibility(View.VISIBLE);
                } else {
                    viewCache.ioc_wendu.setVisibility(View.GONE);
                }
                if (command!=null&&command.contains("%")) {
                    viewCache.command_shidu.setText(command.substring(command.indexOf("℃") + 1));
                    viewCache.command_shidu.setVisibility(View.VISIBLE);
                    viewCache.ioc_shidu.setVisibility(View.VISIBLE);
                } else {
                    viewCache.command_shidu.setVisibility(View.GONE);
                    viewCache.ioc_shidu.setVisibility(View.GONE);
                }
            } else if (deviceInfos.get(i).getControlType().equals(ControlTypeMenu.zhuji.value())) {
                viewCache.command.setText(zhuji.getUc() + " " + getString(R.string.deviceslist_server_totalonlineapps)
                        + "  " + (deviceInfos.size() - 1) + " " + getString(R.string.deviceslist_server_totaldevices));
                viewCache.command_shidu.setVisibility(View.GONE);
                viewCache.ioc_shidu.setVisibility(View.GONE);
                viewCache.ioc_wendu.setVisibility(View.GONE);
            } else if (deviceInfos.get(i).getControlType().contains("shangxing")) {
                viewCache.command_shidu.setVisibility(View.GONE);
                viewCache.ioc_shidu.setVisibility(View.GONE);
                viewCache.ioc_wendu.setVisibility(View.GONE);
                if (deviceInfos.get(i).getStatus() == 0) {
                    viewCache.command.setText("".equals(deviceInfos.get(i).getLastCommand())
                            ? getString(R.string.deviceslist_server_item_normal) : deviceInfos.get(i).getLastCommand());
                } else if (deviceInfos.get(i).getStatus() == 1) { // 显示正常
                    viewCache.command.setText(getString(R.string.normal));
                    viewCache.time.setText("");
                }
            } else {
                //Log.e("点击", deviceInfos.get(i).getControlType());
                viewCache.command.setText("".equals(deviceInfos.get(i).getLastCommand()) ? getString(R.string.deviceslist_server_item_normal)
                        : deviceInfos.get(i).getLastCommand());
                viewCache.command_shidu.setVisibility(View.GONE);
                viewCache.ioc_shidu.setVisibility(View.GONE);
                viewCache.ioc_wendu.setVisibility(View.GONE);

            }
        }

        private String formatTime(long time) { // 需要增加定时器晚上12点刷新一下
            String forString = "";
            if (time == 0) {
                return "";
            }
            int day = 0;
            Calendar calendar = Calendar.getInstance();
            day = calendar.get(Calendar.DAY_OF_YEAR);
            calendar.setTimeInMillis(time);
            int day_tmp = calendar.get(Calendar.DAY_OF_YEAR);
            switch (day - day_tmp) {
                case 0:
                    int m = calendar.get(Calendar.MINUTE);
                    if (m < 10) {
                        forString = calendar.get(Calendar.HOUR_OF_DAY) + ":0" + m;
                    } else {
                        forString = calendar.get(Calendar.HOUR_OF_DAY) + ":" + m;
                    }
                    break;
                case 1:
                    forString = getString(R.string.yesterday);
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    switch (calendar.get(Calendar.DAY_OF_WEEK)) {
                        case 1:
                            forString = getString(R.string.sunday);
                            break;
                        case 2:
                            forString = getString(R.string.monday);
                            break;
                        case 3:
                            forString = getString(R.string.tuesday);
                            break;
                        case 4:
                            forString = getString(R.string.wednesday);
                            break;
                        case 5:
                            forString = getString(R.string.thursday);
                            break;
                        case 6:
                            forString = getString(R.string.friday);
                            break;
                        case 7:
                            forString = getString(R.string.saturday);
                            break;
                        default:
                            break;
                    }
                    ;
                    break;
                default:
                    forString = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-"
                            + calendar.get(Calendar.DAY_OF_MONTH);
                    break;
            }
            return forString;
        }

        /**
         * 设置控件的显示也隐藏
         *
         * @param viewCache
         * @param i
         */
        private void setShowOrHide(DeviceInfoView viewCache, int i) {
            // 可控制的就和只接受的不一样
            if (deviceInfos.get(i).getCa().equals(DeviceInfo.CaMenu.menling.value())) {//门铃
//                if(VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                if(MainApplication.app.getAppGlobalConfig().isShowNewAddGroup()){
                    viewCache.switchButton.setVisibility(View.VISIBLE);
                }
                viewCache.cLayout.setVisibility(View.VISIBLE);
                viewCache.ioc_showright.setVisibility(View.VISIBLE);
                viewCache.button.setVisibility(View.GONE);
  //              viewCache.switchButton.setVisibility(View.GONE);
                if (deviceInfos.get(i).getAcceptMessage() == 0) {
                    viewCache.switchButton.setCheckedNotListener(false);
                } else {
                    viewCache.switchButton.setCheckedNotListener(true);
                }
                viewCache.command.setVisibility(View.VISIBLE);
                viewCache.time.setVisibility(View.VISIBLE);
            }else if (deviceInfos.get(i).getControlType().contains("shangxing")
                    | deviceInfos.get(i).getControlType().equals("wenshiduji")
                    | deviceInfos.get(i).getControlType().equals("wenduji")
                    | deviceInfos.get(i).getControlType().equals("tizhongcheng")
                    | deviceInfos.get(i).getControlType().contains("fangdiu")) {
                viewCache.cLayout.setVisibility(View.VISIBLE);
                viewCache.ioc_showright.setVisibility(View.VISIBLE);
                viewCache.button.setVisibility(View.GONE);
                viewCache.switchButton.setVisibility(View.GONE);
                viewCache.switchButton.changeButtonBg(GroupInfoActivity.this, R.drawable.checkswitch_bottom);
//                if(VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                if(MainApplication.app.getAppGlobalConfig().isShowNewAddGroup()){
                    if(deviceInfos.get(i).getCa().equals(DeviceInfo.CaMenu.yxj.value())||
                            deviceInfos.get(i).getCa().equals(DeviceInfo.CaMenu.ldtsq.value())){
                        viewCache.switchButton.setVisibility(View.VISIBLE);
                    }
                }
                if (deviceInfos.get(i).getAcceptMessage() == 0) {
                    viewCache.switchButton.setCheckedNotListener(false);
                } else {
                    viewCache.switchButton.setCheckedNotListener(true);
                }
                viewCache.command.setVisibility(View.VISIBLE);
                viewCache.time.setVisibility(View.VISIBLE);
            } else {
                if (deviceInfos.get(i).getControlType().equals(ControlTypeMenu.xiaxing_1.value())) {
                    viewCache.cLayout.setVisibility(View.VISIBLE);
                    viewCache.ioc_showright.setVisibility(View.VISIBLE);
                    viewCache.button.setVisibility(View.GONE);
                    viewCache.switchButton.setVisibility(View.GONE);
                    viewCache.command.setVisibility(View.VISIBLE);
                    viewCache.time.setVisibility(View.VISIBLE);
                    // 红外设备显示向右的箭头图标
                    if (deviceInfos.get(i).getCa().equals("hwzf")) {
                        viewCache.ioc_showright.setVisibility(View.VISIBLE);
                    }
                } else if (deviceInfos.get(i).getControlType().equals(ControlTypeMenu.xiaxing_2.value())) {
                    viewCache.cLayout.setVisibility(View.VISIBLE);
                    viewCache.ioc_showright.setVisibility(View.VISIBLE);
                    viewCache.button.setVisibility(View.GONE);
                    viewCache.switchButton.setVisibility(View.GONE);
                    /*viewCache.switchButton.changeButtonBg(GroupInfoActivity.this,
                            R.drawable.checkswitch_bottom_wenzi);
					viewCache.switchButton.setVisibility(View.VISIBLE);*/
                    if (deviceInfos.get(i).getDr() == 1) {
                        viewCache.switchButton.setCheckedNotListener(true);
                    } else {
                        viewCache.switchButton.setCheckedNotListener(false);
                    }
                    viewCache.command.setVisibility(View.VISIBLE);
                    viewCache.time.setVisibility(View.VISIBLE);
                } else if (deviceInfos.get(i).getControlType().contains("xiaxing")) {
                    viewCache.cLayout.setVisibility(View.VISIBLE);
                    viewCache.ioc_showright.setVisibility(View.VISIBLE);
                    viewCache.button.setVisibility(View.GONE);
                    viewCache.switchButton.setVisibility(View.GONE);
                    viewCache.command.setVisibility(View.VISIBLE);
                    viewCache.time.setVisibility(View.VISIBLE);
                } else if (ControlTypeMenu.neiqian.value().equals(deviceInfos.get(i).getControlType()) ||
                        ControlTypeMenu.group.value().equals(deviceInfos.get(i).getControlType())) {
                    viewCache.cLayout.setVisibility(View.VISIBLE);
                    viewCache.ioc_showright.setVisibility(View.VISIBLE);
                    viewCache.button.setVisibility(View.GONE);
                    viewCache.switchButton.setVisibility(View.GONE);
                    viewCache.command.setVisibility(View.GONE);
                    viewCache.time.setVisibility(View.GONE);
                } else {// 只有主机了
                    viewCache.command.setVisibility(View.VISIBLE);
                    viewCache.cLayout.setVisibility(View.GONE);
                    viewCache.time.setVisibility(View.VISIBLE);
                }
            }
            if (deviceInfos.get(i).isLowb()) {
                viewCache.low.setVisibility(View.VISIBLE);
            } else {
                viewCache.low.setVisibility(View.GONE);
            }
        }

        /**
         * 设置控件的显示背景
         *
         * @param viewCache
         * @param i
         */
        private void setTypeAndBackground(DeviceInfoView viewCache, int i) {
            if (deviceInfos.get(i).getId() == zhuji.getId()) {
                viewCache.type
                        .setText((deviceInfos.get(i).getWhere() == null ? "" : (deviceInfos.get(i).getWhere() + " "))
                                + (zhuji.isOnline() ? getString(R.string.deviceslist_server_zhuji_online)
                                : getString(R.string.deviceslist_server_zhuji_offline)));
                // 在线
                if (zhuji.isOnline()) {
                    viewCache.device_item_layout.setBackgroundResource(R.drawable.device_item_click_bg);
                } else {
                    viewCache.device_item_layout.setBackgroundColor(Color.RED);
                }
                viewCache.type_left.setVisibility(View.GONE);
                viewCache.type_right.setVisibility(View.GONE);
            } else {
                // 主机是否在线
                if (!zhuji.isOnline()) { // 不在线
                    viewCache.device_item_layout.setBackgroundResource(R.color.graysloae);
                    if (deviceInfos.get(i).getControlType().contains("xiaxing")) {
                        viewCache.button.setEnabled(false);
                        viewCache.switchButton.setEnabled(false);
                    } else {
                        viewCache.switchButton.setEnabled(true);
                    }
                } else {
                    viewCache.switchButton.setEnabled(true);
                    viewCache.button.setEnabled(true);
                    viewCache.device_item_layout.setBackgroundResource(R.drawable.device_item_click_bg);
                    if (sortType == 0) { // 智能类型
                        if (deviceInfos.get(i).getAcceptMessage() > 0) {
                            // if(dcsp.getLong(Constant.DEVICE_BG_CHANGE_TYPE,
                            // 0) ==
                            // 1){ //0为啥都没有，1为蓝色背景 2闪烁
                            // if(String.valueOf(deviceInfos.get(i).getId()).equals(old_refulsh_device_id)){
                            // viewCache.device_item_layout.setBackgroundColor(Color.BLUE);
                            // }else{
                            // viewCache.device_item_layout.setBackgroundResource(R.drawable.device_item_click_bg);
                            // }
                            // }else{
                            // viewCache.device_item_layout.setBackgroundResource(R.drawable.device_item_click_bg);
                            // }
                        } else {
                            viewCache.device_item_layout.setBackgroundResource(R.color.graysloae);
                        }
                    }
                }
                if (!StringUtils.isEmpty(deviceInfos.get(i).getWhere())
                        || !StringUtils.isEmpty(deviceInfos.get(i).getType())) {
                    viewCache.type_left.setVisibility(View.VISIBLE);
                    viewCache.type_right.setVisibility(View.VISIBLE);
                    viewCache.type.setText((deviceInfos.get(i).getWhere() == null ? "" : (deviceInfos.get(i).getWhere() + " "))
                            + deviceInfos.get(i).getType());
                } else {
                    viewCache.type_left.setVisibility(View.GONE);
                    viewCache.type_right.setVisibility(View.GONE);
                    viewCache.type.setText("");
                }
            }
        }

        /**
         * 初始化按钮点击事件
         *
         * @param viewCache
         */
        private void initButtonEvent(DeviceInfoView viewCache, final int i) {
            viewCache.button.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (Util.isFastClick()) {
                        Toast.makeText(mContext, getString(R.string.activity_devices_commandhistory_tip), Toast.LENGTH_SHORT).show();
                    } else {
                        SyncMessage message = new SyncMessage();
                        message.setCommand(CommandMenu.rq_control.value());
                        message.setDeviceid(deviceInfos.get(i).getId());
                        // 操作 这里的操作写死，需要在服务器的配置页面中配置此按钮对应的指令是什么
                        message.setSyncBytes(new byte[]{0x02});
                        SyncMessageContainer.getInstance().produceSendMessage(message);
                        operationDevice = deviceInfos.get(i);
                        operationDevice.setwIndex(i);
                    }
                }
            });
            viewCache.switchButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                    if (deviceInfos.get(i).getCak().equals("control") && Util.isFastClick()) {
                        Toast.makeText(mContext, getString(R.string.activity_devices_commandhistory_tip), Toast.LENGTH_SHORT).show();
                    } else {
                        SyncMessage message = new SyncMessage();
                        message.setCommand(CommandMenu.rq_control.value());
                        message.setDeviceid(deviceInfos.get(i).getId());
                        if (arg1) { // 开关操作
                            // 这里的操作写死，需要在服务器的配置页面中配置此按钮对应的指令是什么
                            // 开操作
                            message.setSyncBytes(new byte[]{0x01});
                        } else {
                            // 关操作
                            message.setSyncBytes(new byte[]{0x00});
                        }
                        // 点击后显示进度条
                        showInProgress(getString(R.string.operationing), false, false);
                        defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 8 * 1000);
                        SyncMessageContainer.getInstance().produceSendMessage(message);
                        operationDevice = deviceInfos.get(i);
                        operationDevice.setwIndex(i);
                    }
                }
            });
        }

        public void back(View v) {
            finish();
        }

        /**
         * 设置未读消息数
         *
         * @param viewCache
         * @param i
         */
        private void setBadeNumber(DeviceInfoView viewCache, int i) {
            if (deviceInfos.get(i).getNr() == 0) {
                viewCache.badgeView.setVisibility(View.GONE);
            } else {
                viewCache.badgeView.setText(String.valueOf(deviceInfos.get(i).getNr()));
                viewCache.badgeView.show();
            }
            if (deviceInfos.get(i).getAcceptMessage() == 3) {
                viewCache.mode.setText(getString(R.string.shefang));
                if (VersionType.CHANNEL_LILESI
                        .equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                    viewCache.mode.setVisibility(View.GONE);
                } else {
                    viewCache.mode.setVisibility(View.VISIBLE);
                }
            } else {
                viewCache.mode.setVisibility(View.GONE);
            }
        }

        /**
         * 设置主机模式
         *
         * @param viewCache
         * @param i
         */

        private void setModen(DeviceInfoView viewCache, int i) {
            if (VersionType.CHANNEL_LILESI
                    .equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                if (Constant.SCENE_NOW_CF.equals(zhuji.getScene()) && !deviceInfos.get(i).getControlType().contains("xiaxing")) { //撤防，探头不可编辑接收模式 立乐斯要求
                    viewCache.switchButton.setEnabled(false);
                } else {
                    viewCache.switchButton.setEnabled(true);
                }
            }
            viewCache.rLayout.setBackgroundColor(Color.TRANSPARENT);
            viewCache.time.setTextColor(Color.GRAY);
            viewCache.time.setVisibility(View.VISIBLE);
            viewCache.sceneLayout.setVisibility(View.GONE);
        }

        private void setJKDBackground(View view, DeviceInfoView viewCache, int i) {
            if (VersionType.CHANNEL_JKD.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                //添加圆形背景
                viewCache.ioc.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.shape_round_bg));
                view.setBackgroundColor(0xff284653);
            }
        }
    }

    class loadAllDevicesInfo implements Runnable {
        private int what;

        public loadAllDevicesInfo() {
        }

        public loadAllDevicesInfo(int what) {
            this.what = what;
        }

        @Override
        public void run() {
//            zhuji = DatabaseOperator.getInstance(GroupInfoActivity.this)
//                    .queryDeviceZhuJiInfo(dcsp.getString(Constant.APP_MASTERID, ""));
            //替换
            zhuji =DatabaseOperator.getInstance(GroupInfoActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
            if (zhuji == null) {
                return;
            }
            DeviceInfo shexiangtou = null;
            List<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();
            if (zhuji != null) {
                String ordersql = "order by d.sort desc";
                int totalNr = 0;
                Cursor cursor = DatabaseOperator.getInstance(GroupInfoActivity.this).getReadableDatabase().rawQuery(
                        "SELECT d.* FROM DEVICE_STATUSINFO d LEFT JOIN GROUP_DEVICE_RELATIOIN r ON d.id = r.did WHERE r.gid = ? " + ordersql,
                        new String[]{String.valueOf(groupDevice.getId())});
                if (cursor != null && cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        DeviceInfo deviceInfo = DatabaseOperator.getInstance(GroupInfoActivity.this)
                                .buildDeviceInfo(cursor);
                        if ("shexiangtou".equals(deviceInfo.getControlType())) {
                            shexiangtou = deviceInfo;
                            if (shexiangtou.getIpc() != null) {
                                JSONArray array = JSONArray.parseArray(shexiangtou.getIpc());
                                if (array != null) {
                                    shexiangtou.setStatus(0); //显示指令
                                    shexiangtou.setLastCommand(array.size() + getString(R.string.deviceslist_camera_count));//显示摄像头个数
                                }
                            }
                            continue;
                        }
                        deviceList.add(deviceInfo);
                    }
                    //摄像头必须放在遥控器定住的逻辑前面不然会出现崩溃
                    if (shexiangtou != null) {
                        deviceList.add(1, shexiangtou);
                    }
                    //使操作的遥控器定住
                    if (operationDevice != null
                            && operationDevice.getControlType().contains(ControlTypeMenu.xiaxing.value())) {
                        for (int k = 0; k < deviceList.size(); k++) {
                            if (operationDevice.getId() == deviceList.get(k).getId()
                                    && deviceList.size() >= operationDevice.getwIndex()
                                    && operationDevice.getwIndex() != -1) {
                                DeviceInfo opDevice = deviceList.get(k);
                                deviceList.remove(k);
                                deviceList.add(operationDevice.getwIndex(), opDevice);
                            }
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
            Message m = defaultHandler.obtainMessage(this.what);
            m.obj = deviceList;
            defaultHandler.sendMessage(m);
        }
    }

    /**
     * 删除功能
     **/
    class DeleDevice implements Runnable {
        final long uid = dcsp.getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
        final String code = dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");
        List<DeviceInfo> mDeviceInfos;
        DeviceInfo mDeviceInfo;

        public DeleDevice(DeviceInfo groupInfo) {
            this.mDeviceInfo = groupInfo;
            this.mDeviceInfos = deviceInfos;
        }

        @Override
        public void run() {
//            zhuji = DatabaseOperator.getInstance(GroupInfoActivity.this)
//                    .queryDeviceZhuJiInfo(dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, ""));
            //替换
            zhuji =DatabaseOperator.getInstance(GroupInfoActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
            if (zhuji == null) {
                return;
            }

            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("id", groupDevice.getId());
            pJsonObject.put("logo", groupDevice.getLogo());
            pJsonObject.put("name", groupDevice.getName());
            pJsonObject.put("masterid", zhuji.getMasterid());
            JSONArray array = new JSONArray();
            mDeviceInfos.remove(mDeviceInfo);
            for (DeviceInfo d : mDeviceInfos) {
                JSONObject o = new JSONObject();
                o.put("id", d.getId());
                array.add(o);
            }
            pJsonObject.put("ids", array);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/dg/update", pJsonObject, GroupInfoActivity.this);
            if ("0".equals(result)) {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        defaultHandler.removeMessages(dHandler_timeout);
                        cancelInProgress();
                        refreshData();
                    }
                });
            }
            /*String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
			pJsonObject.put("uid", uid);
			pJsonObject.put("code", code);
			pJsonObject.put("id", groupDevice.getId());
			pJsonObject.put("logo", groupDevice.getLogo());
			pJsonObject.put("name", groupDevice.getName());
			pJsonObject.put("masterid", zhuji.getMasterid());
			JSONArray array = new JSONArray();
			mDeviceInfos.remove(mDeviceInfo);
			for (DeviceInfo d : mDeviceInfos) {
				JSONObject o = new JSONObject();
				o.put("id", d.getId());
				array.add(o);
			}
			pJsonObject.put("ids", array);
			final String result = HttpRequestUtils
					.requestHttpServer(
							 server + "/jdm/service/dg/update?v="
									+ URLEncoder.encode(
									SecurityUtil.crypt(pJsonObject.toJSONString(), DataCenterSharedPreferences.Constant.KEY_HTTP)),
							GroupInfoActivity.this, defaultHandler);
			if ("0".equals(result)) {
				defaultHandler.post(new Runnable() {

					@Override
					public void run() {
						defaultHandler.removeMessages(dHandler_timeout);
						cancelInProgress();
						refreshData();
					}
				});
			} else if ("-1".equals(result)) {
				defaultHandler.removeMessages(dHandler_timeout);
				defaultHandler.post(new Runnable() {

					@Override
					public void run() {
						cancelInProgress();
						Toast.makeText(GroupInfoActivity.this, getString(R.string.net_error_programs),
								Toast.LENGTH_SHORT).show();
					}
				});
			} else if ("-2".equals(result)) {
				defaultHandler.removeMessages(dHandler_timeout);
				defaultHandler.post(new Runnable() {

					@Override
					public void run() {
						cancelInProgress();
						Toast.makeText(GroupInfoActivity.this, getString(R.string.net_error_illegal_request),
								Toast.LENGTH_SHORT).show();
					}
				});
			} else if ("-3".equals(result)) {
				defaultHandler.removeMessages(dHandler_timeout);
				defaultHandler.post(new Runnable() {

					@Override
					public void run() {
						cancelInProgress();
						Toast.makeText(GroupInfoActivity.this, getString(R.string.net_error_programs),
								Toast.LENGTH_SHORT).show();
					}
				});
			} else {
				defaultHandler.removeMessages(dHandler_timeout);
				defaultHandler.post(new Runnable() {

					@Override
					public void run() {
						cancelInProgress();
						Toast.makeText(GroupInfoActivity.this, getString(R.string.net_error_weizhi),
								Toast.LENGTH_SHORT).show();
					}
				});
			}*/
        }
    }


    @Override
    public void onClick(View v) {
        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setId(groupDevice.getId());
        groupInfo.setName(groupDevice.getName());
        groupInfo.setLogo(groupDevice.getLogo());
        switch (v.getId()) {
            /******************摄像头***********************/
            case R.id.btn_play:
                tx_wait_for_connect.setVisibility(View.VISIBLE);
                btn_play.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                initCameraCreate();
                initIpc = true;
                break;
            case R.id.iv_full_screen:
                ScrrenOrientation = Configuration.ORIENTATION_LANDSCAPE;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case R.id.defence_state:
            case R.id.iv_defence:
                setDefence();
                break;
            case R.id.close_voice:
            case R.id.iv_vioce:
                Log.e("音量", "被点击了");
                if (mIsCloseVoice) {
                    mIsCloseVoice = false;
                    close_voice.setBackgroundResource(R.drawable.m_voice_on);
                    if (mCurrentVolume == 0) {
                        mCurrentVolume = 1;
                    }
                    if (mAudioManager != null) {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                mCurrentVolume, 0);
                    }
                } else {
                    mIsCloseVoice = true;
                    close_voice.setBackgroundResource(R.drawable.m_voice_off);
                    if (mAudioManager != null) {
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
                                0);
                    }
                }
                break;
            case R.id.screenshot:
                this.captureScreen(-1);
                break;
            case R.id.iv_screenshot:
                this.captureScreen(-1);
                break;
            case R.id.hungup:
            case R.id.back_btn:
                reject();
                break;
            case R.id.choose_video_format:
                changevideoformat();
                break;
            case R.id.iv_half_screen:
                control_bottom.setVisibility(View.INVISIBLE);
                ScrrenOrientation = Configuration.ORIENTATION_PORTRAIT;
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                // 竖屏使半屏
                // if (isFullScreen) {
                // isFullScreen = false;
                // pView.halfScreen();
                // Log.e("half", "half screen++");
                // }
                break;
            case R.id.video_mode_hd:
                if (current_video_mode != P2PValue.VideoMode.VIDEO_MODE_HD) {
                    current_video_mode = P2PValue.VideoMode.VIDEO_MODE_HD;
                    P2PHandler.getInstance().setVideoMode(
                            P2PValue.VideoMode.VIDEO_MODE_HD);
                    updateVideoModeText(current_video_mode);
                }
                hideVideoFormat();
                break;
            case R.id.video_mode_sd:
                if (current_video_mode != P2PValue.VideoMode.VIDEO_MODE_SD) {
                    current_video_mode = P2PValue.VideoMode.VIDEO_MODE_SD;
                    P2PHandler.getInstance().setVideoMode(
                            P2PValue.VideoMode.VIDEO_MODE_SD);
                    updateVideoModeText(current_video_mode);
                }
                hideVideoFormat();
                break;
            case R.id.video_mode_ld:
                if (current_video_mode != P2PValue.VideoMode.VIDEO_MODE_LD) {
                    current_video_mode = P2PValue.VideoMode.VIDEO_MODE_LD;
                    P2PHandler.getInstance().setVideoMode(
                            P2PValue.VideoMode.VIDEO_MODE_LD);
                    updateVideoModeText(current_video_mode);
                }
                hideVideoFormat();
                break;
            case R.id.rl_prgError:
            case R.id.btn_refrash:
                if (btnRefrash.getVisibility() == View.VISIBLE) {
                    hideError();
                    callDevice();
                }
                break;
            case R.id.iv_next:
                switchNext();
                break;
            case R.id.iv_last:
                switchLast();
                break;
            case R.id.tv_choosee_device:
                if (isShowDeviceList) {
                    l_device_list.setVisibility(View.GONE);
                    isShowDeviceList = false;
                } else {
                    l_device_list.setVisibility(View.VISIBLE);
                    isShowDeviceList = true;
                }
                break;
            case R.id.open_door:
                openDor();
                break;
            case R.id.iv_speak:
            case R.id.send_voice:
                if (!isSpeak) {
                    speak();
                } else {
                    noSpeak();
                }
                break;
/******************摄像头***********************/

            case R.id.group_dele:
                intent = new Intent(this, EditGroupActivity.class);
                bundle.putSerializable("deviceinfos", (Serializable) deviceInfos);
                intent.putExtras(bundle);
                intent.putExtra("groupinfo", groupInfo);
                intent.putExtra("add", false);
                intent.putExtra("number", number);
                intent.putExtra("device", groupDevice);
                intent.putExtra("contact", mContact);
                intent.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
                startActivity(intent);
                menuWindow.dismiss();
                reject();
                break;
            case R.id.group_add:
                intent = new Intent(this, EditGroupActivity.class);
                bundle.putSerializable("deviceinfos", (Serializable) deviceInfos);
                intent.putExtras(bundle);
                intent.putExtra("groupinfo", groupInfo);
                intent.putExtra("device", groupDevice);
                intent.putExtra("add", true);
                intent.putExtra("device", groupDevice);
                intent.putExtra("number", number);
                intent.putExtra("contact", mContact);
                intent.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
                startActivity(intent);
                menuWindow.dismiss();
                reject();
                break;
            case R.id.back:
                //宏才
//                if(VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                if(MainApplication.app.getAppGlobalConfig().isShowNewAddGroup()){
                    if(inputNameParent.getVisibility()==View.VISIBLE){
                        devices_ListView.setVisibility(View.VISIBLE);
                        inputNameParent.setVisibility(View.GONE);
                        group_edit_save.setText(getString(R.string.edit));
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        if(imm.isActive()&&name_edit.getWindowToken()!=null){
                            imm.hideSoftInputFromWindow(name_edit.getWindowToken(),0);
                        }
                        return;
                    }
                }
                finish();
                break;
            case R.id.tv_setting:
                intent.setClass(getApplicationContext(), SettingActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_setdevice: // 设置设备
                itemMenu.dismiss();
                if (operationDevice.getCak() != null && operationDevice.getCak().contains("security") && operationDevice.getAcceptMessage() > 0) {
                    intent.setClass(getApplicationContext(), ChooseAudioSettingMode.class);
                    intent.putExtra("device", operationDevice);
                    intent.putExtra("isgroup",true);
                    startActivity(intent);

                } else {
                    intent.setClass(getApplicationContext(), DeviceSetActivity.class);
                    intent.putExtra("device", operationDevice);
                    startActivity(intent);
                }
                break;
            case R.id.btn_accept_auto_strongshow: // 强力提醒模式
                itemMenu.dismiss();
                SyncMessageContainer.getInstance().sendMessageToServer(CommandMenu.rq_controlRemind, CodeMenu.zero,
                        operationDevice.getId(), new byte[]{0x03});
                break;
            case R.id.btn_accept_autoshow: // 自动提醒
                itemMenu.dismiss();
                SyncMessageContainer.getInstance().sendMessageToServer(CommandMenu.rq_controlRemind, CodeMenu.zero,
                        operationDevice.getId(), new byte[]{0x02});
                break;
            case R.id.btn_acceptnotshow: // 接收消息不提醒
                itemMenu.dismiss();
                SyncMessageContainer.getInstance().sendMessageToServer(CommandMenu.rq_controlRemind, CodeMenu.zero,
                        operationDevice.getId(), new byte[]{0x01});
                break;
            case R.id.btn_notaccept: // 关操作
                itemMenu.dismiss();
                SyncMessageContainer.getInstance().sendMessageToServer(CommandMenu.rq_controlRemind, CodeMenu.zero,
                        operationDevice.getId(), new byte[]{0x00});
                break;
            case R.id.btn_deldevice:
                itemMenu.dismiss();
                showInProgress(getString(R.string.ongoing), false, true);
                defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 8 * 1000);
//                if(VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                  if(MainApplication.app.getAppGlobalConfig().isShowNewAddGroup()){
                    JavaThreadPool.getInstance().excute(new Runnable() {
                        @Override
                        public void run() {
                            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                            JSONObject object = new JSONObject();
                            object.put("id", operationDevice.getId());
                            String result = HttpRequestUtils.requestoOkHttpPost(server+"/jdm/s3/d/del", object, mContext);
                            if(result.equals("0")){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(mContext,"",Toast.LENGTH_SHORT);
                                        toast.setText(getString(R.string.activity_editscene_set_success));
                                        toast.show();
                                        defaultHandler.removeMessages(dHandler_timeout);
                                        cancelInProgress();
                                        refreshData();
                                    }
                                });
                            }else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast toast = Toast.makeText(mContext,"",Toast.LENGTH_SHORT);
                                        toast.setText(getString(R.string.activity_editscene_set_falid));
                                        toast.show();
                                        defaultHandler.removeMessages(dHandler_timeout);
                                        cancelInProgress();
                                    }
                                });
                            }
                        }
                    });
                }else{
                   // showInProgress(getString(R.string.ongoing), false, true);
                    JavaThreadPool.getInstance().excute(new DeleDevice(deviceInfos.get(index)));
                  //  defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 8 * 1000);
                }
                break;
            case R.id.tantou_chufa_setting:
                //探头触发主机设定
                itemMenu.dismiss();
                intent.setClass(mContext,HongCaiTantouSettingActivity.class);
                intent.putExtra("device_id",operationDevice.getId());
                startActivity(intent);
                break;
            default:
                Toast.makeText(GroupInfoActivity.this, getString(R.string.deviceslist_server_leftmenu_unknownbutton),
                        Toast.LENGTH_SHORT).show();
                break;
        }
    }

    /**
     * 请求数据子线程
     */
    class BindingCameraLoad implements Runnable {
        private long uid;
        private String code;
        private String bIpc;

        public BindingCameraLoad(String bIpc) {
            this.bIpc = bIpc;
        }

        @Override
        public void run() {
            uid = DataCenterSharedPreferences.getInstance(mContext, DataCenterSharedPreferences.Constant.CONFIG)
                    .getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
            code = DataCenterSharedPreferences
                    .getInstance(mContext, DataCenterSharedPreferences.Constant.CONFIG).getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");

            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", bIpc);
//			String result = HttpRequestUtils.requestHttpServer(
//					 server + "/jdm/service/ipcs/getIPC?v="
//							+ URLEncoder.encode(SecurityUtil.crypt(object.toJSONString(), Constant.KEY_HTTP)),
//					GroupInfoActivity.this, defaultHandler);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/ipcs/getIPC", object, GroupInfoActivity.this);
            if (result != null && result.length() > 4) {

                //List<JSONObject> commands = new ArrayList<JSONObject>();
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Contact c = new Contact();
                c.contactId = resultJson.getString("iid");
                c.contactName = resultJson.getString("iname");
                c.contactPassword = resultJson.getString("ipassword");
                try {
                    c.ipadressAddress = InetAddress.getByName("192.168.1.1");
                } catch (UnknownHostException e) {
                    //
                    e.printStackTrace();
                }


                Message message = new Message();
                message.what = dHandler_initContast;
                message.obj = c;
                defaultHandler.sendMessage(message);

            } else {
                //设备不存在的时候跳转，但是不显示视屏
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
//                        unResterReceiver();
                        startActivity(deviceIntent);
                    }
                });
            }
        }
    }

    class KeyItemAdapter extends BaseAdapter {
        /**
         * 视图内部类
         *
         * @author Administrator
         */
        class DeviceInfoView {
            ImageView keybg;
            TextView keyname;
        }

        LayoutInflater layoutInflater;
        public List<DeviceKeys> keys;
        private int pi; //设备的第几项

        public KeyItemAdapter(Context context, List<DeviceKeys> keys, int index) {
            this.keys = keys;
            pi = index;
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return keys.size();
        }

        @Override
        public Object getItem(int position) {
            return keys.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            DeviceInfoView viewCache = new DeviceInfoView();
            if (view == null) {
                view = layoutInflater.inflate(R.layout.activity_history_key_item, null);
                viewCache.keybg = (ImageView) view.findViewById(R.id.dinfo_keybg);
                viewCache.keyname = (TextView) view.findViewById(R.id.dinfo_keyname);
                viewCache.keybg.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                    if(isScrolling) return;
                        if (Util.isFastClick()) {
                            Toast.makeText(GroupInfoActivity.this, getString(R.string.activity_devices_commandhistory_tip), Toast.LENGTH_SHORT).show();
                        } else {
                            deviceInfo = deviceInfos.get(pi);
                            defaultHandler.sendEmptyMessageDelayed(dHandler_key_timeout, 8000);
                            showInProgress(getString(R.string.ongoing), false, true);

                            SyncMessage message1 = new SyncMessage();
                            message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
                            message1.setDeviceid(deviceInfo.getId());
                            // 操作
                            message1.setSyncBytes(new byte[]{(byte) keys.get(position).getKeySort()});
                            SyncMessageContainer.getInstance().produceSendMessage(message1);
                            Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                            AudioManager mAudioMgr = (AudioManager) getApplicationContext().getSystemService(getApplicationContext().AUDIO_SERVICE);
                            boolean isRing = (null == RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION));
                            if (key == 1) {
                                vibrator.vibrate(new long[]{0, 200}, -1);
                            } else if (key == 2) {
                                if (isRing)
                                    mAudioMgr.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT);
                            } else if (key == 3) {
                                vibrator.vibrate(new long[]{0, 200}, -1);
                                if (isRing)
                                    mAudioMgr.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT);
                            } else {
                                if (vibrator != null) {
                                    vibrator.cancel();
                                }

                            }
                        }
                    }
                });
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
            viewCache.keyname.setText(keys.get(position).getKeyName());
            if (!StringUtils.isEmpty(keys.get(position).getKeyIco())) {
                viewCache.keybg.setImageResource(
                        getResources().getIdentifier(keys.get(position).getKeyIco(), "drawable", getBaseContext().getPackageName()));
            } else {
                viewCache.keybg.setBackgroundResource(R.drawable.device_item_one_button_bg);
            }
            return view;
        }

    }

    class CommandKeyLoad implements Runnable {
        DeviceInfo deviceInfo;

        public CommandKeyLoad(DeviceInfo deviceInfo) {
            this.deviceInfo = deviceInfo;
        }

        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", deviceInfo.getId());
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/dg/getgdkey", object, GroupInfoActivity.this);
            if (result != null && result.length() > 4) {
                JSONArray array = JSON.parseArray(result);
                if (array != null && !array.isEmpty()) {
                    for (int j = 0; j < array.size(); j++) {
                        JSONObject jsonObject = array.getJSONObject(j);
                        long did = jsonObject.getLongValue("did");
                        DatabaseOperator.getInstance(GroupInfoActivity.this).delDeviceKeysById(did);
                        if (jsonObject.get("keys") != null) {
                            JSONArray mKeys = jsonObject.getJSONArray("keys");
                            List<DeviceKeys> mapKeys = new ArrayList<>();
                            if (!mKeys.isEmpty()) {
                                for (int k = 0; k < mKeys.size(); k++) {
                                    JSONObject keyJsonObject = mKeys.getJSONObject(k);
                                    DeviceKeys key = new DeviceKeys();
                                    key.setKeySort(keyJsonObject.getIntValue("s"));
                                    key.setKeyName(keyJsonObject.getString("n"));
                                    key.setKeyIco(keyJsonObject.getString("i"));
                                    key.setKeyWhere(keyJsonObject.getIntValue("w"));
                                    key.setDeviceId(keyJsonObject.getLongValue("id"));
                                    DatabaseOperator.getInstance(GroupInfoActivity.this).insertOrUpdateDeviceKeys(key, did);
                                    mapKeys.add(key);
                                }
                            }
                            map.put(did, mapKeys);
                        }
                    }
                }
                Message m = defaultHandler.obtainMessage(dHandler_devicekeys);
                defaultHandler.sendMessage(m);
            } else if (!StringUtils.isEmpty(result)) {
                defaultHandler.sendEmptyMessage(dHandler_devicekeys);
            }

        }
    }

    /********************
     * 摄像头部分
     *********************/
    private TextView users;
    boolean initIpc = false;
    private final int dHandler_camera_0 = 1000, dHandler_camera_1 = 1001;
    private LinearLayout l_control;
    private RelativeLayout control_bottom;
    private LinearLayout control_top;
    private Button choose_video_format, btn_play;
    private TextView video_mode_hd, video_mode_sd, video_mode_ld;
    private ImageView close_voice, send_voice, iv_half_screen, hungup, screenshot, defence_state;
    private LinearLayout layout_voice_state;
    private ImageView voice_state;
    private ImageView iv_last, iv_next;
    private LinearLayout l_device_list;
    private int callType = 3;
    private boolean isReject = false;
    private boolean isRegFilter = false;
    private int ScrrenOrientation;
    private int window_width, window_height;
    private String callId = "1234567", password;
    private int connectType;
    private int defenceState = -1;//布防状态
    private boolean mIsCloseVoice = false;
    private int mCurrentVolume, mMaxVolume;
    private AudioManager mAudioManager;
    private boolean isSurpportOpenDoor = false;
    private boolean isShowVideo = false;
    private boolean isSpeak = false; //是否在对讲
    private int current_video_mode;
    private int screenWidth;
    private int screenHeigh;
    private Contact mContact;
    private String[] ipcList;
    // 刷新监控部分
    private RelativeLayout rlPrgTxError;
    private TextView txError, tx_wait_for_connect;
    private Button btnRefrash;
    private ProgressBar progressBar;
    private HeaderView ivHeader;
    private int number;
    private int currentNumber = 0;
    private boolean isShowDeviceList = false;
    List<TextView> devicelist = new ArrayList<TextView>();
    // 摇手机切换ipc
    Vibrator vibrator;
    SensorManager sensorManager;
    Sensor sensor;
    SensorEventListener sensorListener;
    private boolean isShake = true;
    private long lastUpdateTime;
    private float lastX;
    private float lastY;
    private float lastZ;
    private static final int UPTATE_INTERVAL_TIME = 70;
    private static final int SPEED_SHRESHOLD = 2000;
    private boolean isReceveHeader = false;
    boolean isPermission = true;
    private View vLineHD;
    private boolean connectSenconde = false;
    private int pushAlarmType;
    private boolean isCustomCmdAlarm = false;
    private RelativeLayout r_p2pview;

    public void initSpeark(int deviceType, boolean isOpenDor) {
        if (deviceType != P2PValue.DeviceType.DOORBELL && !isOpenDor) {
            send_voice.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent event) {
                    //
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.e("时间：", event.getEventTime() + "");
                            hideVideoFormat();
                            layout_voice_state
                                    .setVisibility(RelativeLayout.VISIBLE);

                            send_voice
                                    .setBackgroundResource(R.drawable.ic_send_audio);
                            setMute(false);
                            return true;
                        case MotionEvent.ACTION_UP:
                            layout_voice_state.setVisibility(RelativeLayout.GONE);
                            send_voice
                                    .setBackgroundResource(R.drawable.ic_send_audio);
                            setMute(true);
                            return true;
                        case MotionEvent.ACTION_CANCEL:
                            layout_voice_state.setVisibility(RelativeLayout.GONE);
                            send_voice
                                    .setBackgroundResource(R.drawable.ic_send_audio);
                            setMute(true);
                            return true;
                    }
                    return false;
                }
            });
        } else if (deviceType == P2PValue.DeviceType.DOORBELL && !isOpenDor) {
            isFirstMute = false;
            send_voice.setOnTouchListener(null);
            send_voice.setOnClickListener(this);
        } else if (isOpenDor) {
            send_voice.setOnTouchListener(null);
            control_bottom.setVisibility(View.VISIBLE);
            // 开始监控时没有声音，暂时这样
            send_voice.setOnClickListener(this);
            isFirstMute = true;
            // speak();
        }
    }

    private void setHeaderImage() {
        ivHeader.updateImage(callId, true, 1);
    }

    /**
     * 刷新IPC和NPC布局异同
     */
    private void frushLayout(int contactType) {
        if (contactType == P2PValue.DeviceType.IPC) {
            video_mode_hd.setVisibility(View.VISIBLE);
            vLineHD.setVisibility(View.VISIBLE);
        } else if (contactType == P2PValue.DeviceType.NPC) {
            video_mode_hd.setVisibility(View.GONE);
            vLineHD.setVisibility(View.GONE);
        }
    }


    public void changeDefence(int defencestate) {
//        if (defenceState == -1) return;
//        if (defencestate == Constants.DefenceState.DEFENCE_STATE_OFF) {
//            defence_state.setImageResource(R.drawable.deployment);
//        } else {
//
//            defence_state.setImageResource(R.drawable.disarm);
//        }
    }

    boolean isStartActivity = false;

    @Override
    protected void onPause() {
        super.onPause();
        if (isRegFilter) {
            unregisterReceiver(defaultReceiver);
            isRegFilter = false;
        }
        reject();
    }

    /**
     * 隐藏过度页
     */
    private void hindRlProTxError() {
        rlPrgTxError.setVisibility(View.GONE);
        control_bottom.setVisibility(View.VISIBLE);
    }

    private void showRlProTxError() {
        ObjectAnimator anima = ObjectAnimator.ofFloat(rlPrgTxError, "alpha",
                0f, 1.0f);
        control_bottom.setVisibility(View.GONE);
        rlPrgTxError.setVisibility(View.VISIBLE);
        anima.setDuration(500).start();
        anima.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                //
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
    }

    /*******************
     * 设置布防
     ************************/
    public void setDefence() {
//        if (!isPermission) {
//            T.showShort(mContext, R.string.insufficient_permissions);
//            return;
//        }
//        if (defenceState == Constants.DefenceState.DEFENCE_STATE_ON) {
//            P2PHandler.getInstance().setRemoteDefence(mContact.getContactId(),
//                    password,
//                    Constants.P2P_SET.REMOTE_DEFENCE_SET.ALARM_SWITCH_OFF);
//        } else if (defenceState == Constants.DefenceState.DEFENCE_STATE_OFF) {
//            P2PHandler.getInstance().setRemoteDefence(mContact.getContactId(),
//                    password,
//                    Constants.P2P_SET.REMOTE_DEFENCE_SET.ALARM_SWITCH_ON);
//        }

    }

    private void callDevice() {
        P2PConnect.setCurrent_state(P2PConnect.P2P_STATE_CALLING);
        P2PConnect.setCurrent_call_id(callId);
        String push_mesg = NpcCommon.mThreeNum
                + ":"
                + mContext.getResources()
                .getString(R.string.p2p_call_push_mesg);
        if (connectType == Constants.ConnectType.RTSPCONNECT) {
            callType = 3;
            String ipAddress = "";
            String ipFlag = "";
            if (mContact.ipadressAddress != null) {
                ipAddress = mContact.ipadressAddress.getHostAddress();
                ipFlag = ipAddress.substring(ipAddress.lastIndexOf(".") + 1,
                        ipAddress.length());
            } else {

            }
            P2PHandler.getInstance().call(NpcCommon.mThreeNum, "0", true,
                    Constants.P2P_TYPE.P2P_TYPE_MONITOR, "1", "1", push_mesg,
                    AppConfig.VideoMode, mContact.contactId, MainApplication.GWELL_LOCALAREAIP);
        } else if (connectType == Constants.ConnectType.P2PCONNECT) {
            callType = 1;
            String ipAdress = FList.getInstance().getCompleteIPAddress(
                    mContact.contactId);
            P2PHandler.getInstance().call(NpcCommon.mThreeNum, password, true,
                    Constants.P2P_TYPE.P2P_TYPE_MONITOR, callId, ipAdress,
                    push_mesg, AppConfig.VideoMode, mContact.contactId, MainApplication.GWELL_LOCALAREAIP);
        }
    }

    private void playReady() {
        Intent ready = new Intent();
        ready.setAction(Constants.P2P.P2P_READY);
        this.sendBroadcast(ready);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        //
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ScrrenOrientation = Configuration.ORIENTATION_LANDSCAPE;
            l_control.setVisibility(View.GONE);
            int height = (int) getResources().getDimension(
                    R.dimen.p2p_monitor_bar_height);
            control_bottom.setVisibility(View.VISIBLE);
            pView.fullScreen();
            isFullScreen = true;
        } else {
            ScrrenOrientation = Configuration.ORIENTATION_PORTRAIT;
            l_control.setVisibility(View.VISIBLE);
            control_bottom.setVisibility(View.INVISIBLE);
            control_top.setVisibility(View.GONE);
            if (isFullScreen) {
                isFullScreen = false;
                pView.halfScreen();
                Log.e("half", "half screen--");
            }
        }
    }

    private void updateVideoModeText(int mode) {
        if (mode == P2PValue.VideoMode.VIDEO_MODE_HD) {
            video_mode_hd.setTextColor(mContext.getResources().getColor(
                    R.color.blue));
            video_mode_sd.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            video_mode_ld.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            choose_video_format.setText(R.string.video_mode_hd);
        } else if (mode == P2PValue.VideoMode.VIDEO_MODE_SD) {
            video_mode_hd.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            video_mode_sd.setTextColor(mContext.getResources().getColor(
                    R.color.blue));
            video_mode_ld.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            choose_video_format.setText(R.string.video_mode_sd);
        } else if (mode == P2PValue.VideoMode.VIDEO_MODE_LD) {
            video_mode_hd.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            video_mode_sd.setTextColor(mContext.getResources().getColor(
                    R.color.white));
            video_mode_ld.setTextColor(mContext.getResources().getColor(
                    R.color.blue));
            choose_video_format.setText(R.string.video_mode_ld);
        }
    }

    @Override
    protected void onP2PViewSingleTap() {
        //
        changeControl();
    }

    private List<String> pictrues = null;

    @Override
    protected void onCaptureScreenResult(boolean isSuccess, int prePoint) {
        //
        if (isSuccess) {
            // Capture success
            T.showShort(mContext, R.string.capture_success);
            pictrues = Utils.getScreenShotImagePath(callId, 1);
            if (pictrues.size() <= 0) {
                return;
            }
            Utils.saveImgToGallery(pictrues.get(0));
        } else {
            T.showShort(mContext, R.string.capture_failed);
        }
    }

    @Override
    public int getActivityInfo() {
        //
        return Constants.ActivityInfo.ACTIVITY_APMONITORACTIVITY;
    }

    @Override
    protected void onGoBack() {
        //
        // MainApplication.app.showNotification();
    }

    @Override
    protected void onGoFront() {
        //
        // MainApplication.app.hideNotification();
    }

    @Override
    protected void onExit() {
        //
        // MainApplication.app.hideNotification();
    }

    @Override
    public void onBackPressed() {
//        if(Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
          if(MainApplication.app.getAppGlobalConfig().isShowNewAddGroup()){
            if(inputNameParent.getVisibility()==View.VISIBLE){
                devices_ListView.setVisibility(View.VISIBLE);
                inputNameParent.setVisibility(View.GONE);
                group_edit_save.setText(getString(R.string.edit));
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm.isActive()&&name_edit.getWindowToken()!=null){
                    imm.hideSoftInputFromWindow(name_edit.getWindowToken(),0);
                }
                return ;
            }
        }
        reject();
        super.onBackPressed();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        //
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            mCurrentVolume++;
            if (mCurrentVolume > mMaxVolume) {
                mCurrentVolume = mMaxVolume;
            }

            if (mCurrentVolume != 0) {
                mIsCloseVoice = false;
                if (close_voice!=null) {
                    close_voice.setBackgroundResource(R.drawable.m_voice_on);
                }
            }
            return false;
        } else if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mCurrentVolume--;
            if (mCurrentVolume < 0) {
                mCurrentVolume = 0;
            }

            if (mCurrentVolume == 0) {
                mIsCloseVoice = true;
                if (close_voice!=null) {
                    close_voice.setBackgroundResource(R.drawable.m_voice_off);
                }
            }

            return false;
        }

        return super.dispatchKeyEvent(event);
    }


    // 设置成对话状态
    private void speak() {
        hideVideoFormat();
        layout_voice_state.setVisibility(RelativeLayout.VISIBLE);
        send_voice.setBackgroundResource(R.drawable.ic_send_audio);
        // iv_speak.setBackgroundResource(R.drawable.portrait_speak_p);
        setMute(false);
        isSpeak = true;
        Log.e("leleSpeak", "speak--" + isSpeak);
    }

    private void noSpeak() {
        send_voice.setBackgroundResource(R.drawable.ic_send_audio);
        // layout_voice_state.setVisibility(RelativeLayout.GONE);
        setMute(true);
        isSpeak = false;
        mHandler.postDelayed(mrunnable, 500);
        Log.e("leleSpeak", "no speak--" + isSpeak);
    }

    private boolean isFirstMute = true;
    Runnable mrunnable = new Runnable() {

        @Override
        public void run() {
            //
            if (isFirstMute) {
                Log.e("leleSpeak", "mrunnable--");
                send_voice.performClick();
                isFirstMute = false;
            }
        }
    };

    public void stopSpeak() {
        send_voice.setBackgroundResource(R.drawable.ic_send_audio);
        layout_voice_state.setVisibility(RelativeLayout.GONE);
        setMute(true);
        isSpeak = false;
    }

    /**
     * 开门
     */
    private void openDor() {
        NormalDialog dialog = new NormalDialog(mContext, mContext
                .getResources().getString(R.string.open_door), mContext
                .getResources().getString(R.string.confirm_open_door), mContext
                .getResources().getString(R.string.yes), mContext
                .getResources().getString(R.string.no));
        dialog.setOnButtonOkListener(new NormalDialog.OnButtonOkListener() {

            @Override
            public void onClick() {
                if (isCustomCmdAlarm == true) {
                    String cmd = "IPC1anerfa:unlock";
                    P2PHandler.getInstance().sendCustomCmd(callId, password,
                            cmd, MainApplication.GWELL_LOCALAREAIP);
                } else {
                    P2PHandler.getInstance().setGPIO1_0(callId, password, MainApplication.GWELL_LOCALAREAIP);
                }
            }
        });
        dialog.showDialog();
    }

	/*Handler sHandler = new Handler() {
        public void handleMessage(Message msg) {
			switchConnect();
		};
	};*/

    /**
     * 展示连接错误
     *
     * @param error
     */
    public void showError(String error, int code) {
        if (!connectSenconde && code != 9) {
            callDevice();
            connectSenconde = true;
            return;
        }
        progressBar.setVisibility(View.GONE);
        tx_wait_for_connect.setVisibility(View.GONE);
        txError.setVisibility(View.VISIBLE);
        btnRefrash.setVisibility(View.VISIBLE);
        txError.setText(error);
    }

    /**
     * 隐藏连接错误
     */
    private void hideError() {
        progressBar.setVisibility(View.VISIBLE);
        tx_wait_for_connect.setText(getResources().getString(
                R.string.waite_for_linke));
        tx_wait_for_connect.setVisibility(View.VISIBLE);
        txError.setVisibility(View.GONE);
        btnRefrash.setVisibility(View.GONE);
    }

    /**
     * 切换连接
     */
    private void switchConnect() {
        progressBar.setVisibility(View.VISIBLE);
        tx_wait_for_connect.setText(getResources().getString(
                R.string.switch_connect));
        tx_wait_for_connect.setVisibility(View.VISIBLE);
        txError.setVisibility(View.GONE);
        btnRefrash.setVisibility(View.GONE);
        // iv_full_screen.setVisibility(View.INVISIBLE);
        showRlProTxError();
        Log.e("switchConnect", "switchConnect");
    }

    public void reject() {
        Log.e("点击", "返回键被点击了");
        if (!isReject) {
            isReject = true;
            P2PHandler.getInstance().finish();
            disconnectDooranerfa();
            finish();
        }
    }

    public void rejects() {
        if (!isReject) {
            isReject = true;
            P2PHandler.getInstance().finish();
            disconnectDooranerfa();
        }
    }

    public void readyCallDevice() {
        if (connectType == Constants.ConnectType.P2PCONNECT) {
            P2PHandler.getInstance().openAudioAndStartPlaying(1);
            P2PHandler.getInstance().getDefenceStates(callId, password, MainApplication.GWELL_LOCALAREAIP);
        } else {
            P2PHandler.getInstance().openAudioAndStartPlaying(1);
            callId = "1";
            password = "0";
            P2PHandler.getInstance().getDefenceStates(callId, password, MainApplication.GWELL_LOCALAREAIP);
        }

    }

    private long exitTime = 0;

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK
//                && event.getAction() == KeyEvent.ACTION_DOWN) {
//            if ((System.currentTimeMillis() - exitTime) > 2000 && initIpc) {
//                T.showShort(this, R.string.press_again_monitor);
//                exitTime = System.currentTimeMillis();
//            } else {
//                reject();
//            }
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }


    public void changevideoformat() {
        if (control_top.getVisibility() == View.VISIBLE) {
            Animation anim2 = AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_out);
            anim2.setDuration(100);
            control_top.startAnimation(anim2);
            control_top.setVisibility(RelativeLayout.GONE);
            isShowVideo = false;
        } else {
            Animation anim2 = AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_in);
            anim2.setDuration(100);
            control_top.setVisibility(RelativeLayout.VISIBLE);
            control_top.startAnimation(anim2);
            isShowVideo = true;
        }
    }

    public void hideVideoFormat() {
        if (control_top.getVisibility() == RelativeLayout.VISIBLE) {
            Animation anim2 = AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_out);
            anim2.setDuration(100);
            control_top.startAnimation(anim2);
            control_top.setVisibility(RelativeLayout.GONE);
            isShowVideo = false;
        }
    }

    public void changeControl() {
        if (isSpeak) {// 对讲过程中不可消失
            return;
        }
        if (ScrrenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            return;
        }
        Log.e("changeControl", "changeControl");
        if (control_bottom.getVisibility() == RelativeLayout.VISIBLE) {
            Log.e("changeControl", "changeControl--VISIBLE");
            Animation anim2 = AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_out);
            anim2.setDuration(100);
            control_bottom.startAnimation(anim2);
            anim2.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation arg0) {
                    //
                    hideVideoFormat();
                    choose_video_format.setClickable(false);
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                    //

                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    //
                    hideVideoFormat();
                    control_bottom.setVisibility(RelativeLayout.INVISIBLE);
                    choose_video_format
                            .setBackgroundResource(R.drawable.sd_backgroud);
                    choose_video_format.setClickable(true);
                }
            });

        } else {
            Log.e("changeControl", "changeControl--INVISIBLE");
            control_bottom.setVisibility(RelativeLayout.VISIBLE);
            control_bottom.bringToFront();
            Animation anim2 = AnimationUtils.loadAnimation(this,
                    android.R.anim.fade_in);
            anim2.setDuration(100);
            control_bottom.startAnimation(anim2);
            anim2.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation arg0) {
                    //
                    hideVideoFormat();
                    choose_video_format.setClickable(false);
                }

                @Override
                public void onAnimationRepeat(Animation arg0) {
                    //

                }

                @Override
                public void onAnimationEnd(Animation arg0) {
                    //
                    hideVideoFormat();
                    choose_video_format.setClickable(true);
                }
            });
        }
    }

    /**
     * 新报警信息
     */
    NormalDialog dialogs;
    String contactidTemp = "";

    private void NewMessageDialog(String Meassage, final String contacid,
                                  boolean isSurportdelete) {
        if (dialogs != null) {
            if (dialogs.isShowing()) {
                dialogs.dismiss();
            }
        }
        dialogs = new NormalDialog(mContext);
        dialogs.setContentStr(Meassage);
        dialogs.setbtnStr1(R.string.check);
        dialogs.setbtnStr2(R.string.cancel);
        dialogs.setbtnStr3(R.string.clear_bundealarmid);
        dialogs.showAlarmDialog(isSurportdelete, contacid);
        dialogs.setOnAlarmClickListner(AlarmClickListner);
        contactidTemp = contacid;
    }

    /**
     * 监控对话框单击回调
     */
    private NormalDialog.OnAlarmClickListner AlarmClickListner = new NormalDialog.OnAlarmClickListner() {

        @Override
        public void onOkClick(String alarmID, boolean isSurportDelete,
                              Dialog dialog) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            // 查看新监控--挂断当前监控，再次呼叫另一个监控
            seeMonitor(alarmID);
        }

        @Override
        public void onDeleteClick(String alarmID, boolean isSurportDelete,
                                  Dialog dialog) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            DeleteDevice(alarmID);
        }

        @Override
        public void onCancelClick(String alarmID, boolean isSurportDelete,
                                  Dialog dialog) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    };

    // 解绑确认弹框
    private void DeleteDevice(final String alarmId) {
        dialogs = new NormalDialog(mContext, mContext.getResources().getString(
                R.string.clear_bundealarmid), mContext.getResources()
                .getString(R.string.clear_bundealarmid_tips), mContext
                .getResources().getString(R.string.sure), mContext
                .getResources().getString(R.string.cancel));
        dialogs.setOnButtonOkListener(new NormalDialog.OnButtonOkListener() {

            @Override
            public void onClick() {
                P2PHandler.getInstance().DeleteDeviceAlarmId(
                        String.valueOf(alarmId), MainApplication.GWELL_LOCALAREAIP);
                dialog.dismiss();
                ShowLoading();
            }
        });
        dialog.showDialog();
    }

    private void ShowLoading() {
        dialog = new NormalDialog(mContext);
        dialog.showLoadingDialog();
    }

    private void seeMonitor(String contactId) {
        number = 1;
        final Contact contact = FList.getInstance().isContact(contactId);
        if (null != contact) {
            P2PHandler.getInstance().reject();
            switchConnect();
            changeDeviceListTextColor();
            callId = contact.contactId;
            password = contact.contactPassword;
            if (isSpeak) {
                stopSpeak();
            }
            setHeaderImage();
            if (pushAlarmType == P2PValue.AlarmType.ALARM_TYPE_DOORBELL_PUSH) {
                initSpeark(contact.contactType, true);
            } else {
                initSpeark(contact.contactType, false);
            }
            connectDooranerfa();
            callDevice();
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            frushLayout(P2PValue.DeviceType.IPC);
        } else {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            Log.i("dxsmonitor", contactId);
            createPassDialog(contactId);
        }
    }

    private Dialog passworddialog;

    void createPassDialog(String id) {
        passworddialog = new MyInputPassDialog(mContext,
                Utils.getStringByResouceID(R.string.check), id, listener);
        passworddialog.show();
    }

    private MyInputPassDialog.OnCustomDialogListener listener = new MyInputPassDialog.OnCustomDialogListener() {

        @Override
        public void check(final String password, final String id) {
            if (password.trim().equals("")) {
                T.showShort(mContext, R.string.input_monitor_pwd);
                return;
            }

            if (password.length() > 30 || password.charAt(0) == '0') {
                T.showShort(mContext, R.string.device_password_invalid);
                return;
            }

            P2PConnect.vReject(9, "");
            new Thread() {
                @Override
                public void run() {
                    while (true) {
                        if (P2PConnect.getCurrent_state() == P2PConnect.P2P_STATE_NONE) {
                            Message msg = new Message();
                            String pwd = P2PHandler.getInstance()
                                    .EntryPassword(password);
                            String[] data = new String[]{id, pwd,
                                    String.valueOf(pushAlarmType)};
                            msg.what = 1;
                            msg.obj = data;
                            handler.sendMessage(msg);
                            break;
                        }
                        Utils.sleepThread(500);
                    }
                }
            }.start();

        }
    };
    Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            //
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            if (msg.what == 0) {
                Contact contact = (Contact) msg.obj;
                Intent monitor = new Intent(mContext, ApMonitorActivity.class);
                monitor.putExtra("contact", contact);
                monitor.putExtra("connectType",
                        Constants.ConnectType.P2PCONNECT);
                startActivity(monitor);

                // Intent monitor = new Intent();
                // monitor.setClass(mContext, CallActivity.class);
                // monitor.putExtra("callId", contact.contactId);
                // monitor.putExtra("password", contact.contactPassword);
                // monitor.putExtra("isOutCall", true);
                // monitor.putExtra("contactType", P2PValue.DeviceType.NPC);
                // monitor.putExtra("type",
                // Constants.P2P_TYPE.P2P_TYPE_MONITOR);

                // if (Integer.parseInt(data[2]) ==
                // P2PValue.DeviceType.DOORBELL) {
                // monitor.putExtra("isSurpportOpenDoor", true);
                // }
                // startActivity(monitor);
                // finish();
            } else if (msg.what == 1) {
                if (passworddialog != null && passworddialog.isShowing()) {
                    passworddialog.dismiss();
                }
                String[] data = (String[]) msg.obj;
                // Contact contact=new Contact();
                // contact.contactId=data[0];
                // contact.contactName=data[0];
                // contact.contactPassword=data[1];
                // contact.contactType=P2PValue.DeviceType.IPC;
                // Intent monitor=new Intent(mContext,ApMonitorActivity.class);
                // monitor.putExtra("contact", contact);
                // monitor.putExtra("connectType",
                // Constants.ConnectType.P2PCONNECT);
                // startActivity(monitor);
                // finish();
                P2PHandler.getInstance().reject();
                switchConnect();
                changeDeviceListTextColor();
                callId = data[0];
                password = data[1];
                if (isSpeak) {
                    stopSpeak();
                }
                setHeaderImage();
                if (pushAlarmType == P2PValue.AlarmType.ALARM_TYPE_DOORBELL_PUSH) {
                    initSpeark(P2PValue.DeviceType.DOORBELL, true);
                    Log.e("leleMonitor", "switch doorbell push");
                } else {
                    initSpeark(P2PValue.DeviceType.IPC, false);
                    Log.e("leleMonitor", "switch---");
                }
                connectDooranerfa();
                callDevice();
                frushLayout(P2PValue.DeviceType.IPC);

            }
            // Intent monitor = new Intent();
            // monitor.setClass(mContext, CallActivity.class);
            // monitor.putExtra("callId", data[0]);
            // monitor.putExtra("password", data[1]);
            // monitor.putExtra("isOutCall", true);
            // monitor.putExtra("contactType", Integer.parseInt(data[2]));
            // monitor.putExtra("type", Constants.P2P_TYPE.P2P_TYPE_MONITOR);
            // if (Integer.parseInt(data[2]) == P2PValue.DeviceType.DOORBELL) {
            // monitor.putExtra("isSurpportOpenDoor", true);
            // }
            return false;
        }
    });

    @Override
    public void onHomePressed() {
        //
        super.onHomePressed();
        reject();
    }

    @Override
    protected void onResume() {
        super.onResume();
        key = dcsp.getInt(Constant.BTN_CONTROLSTYLE, 0);
//        NotificationUtil.cancelNotification(getApplicationContext(), Constant.NOTIFICATIONID);
        if (initSuccess) {
            operationDevice = null;
//			refreshData();
        }
        if (itemMenu.isShowing()) {
            itemMenu.dismiss();
        }
        /**摄像头部分**/

        if (mContact != null) {
            readyCallDevice();
            initp2pView();
        }

        /**摄像头部分**/
    }

    public void getScreenWithHeigh() {
        DisplayMetrics dm = new DisplayMetrics();
        // 获取屏幕信息
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeigh = dm.heightPixels;
    }

    /*
     * 初始化P2pview
     */
    public void initp2pView() {
        initP2PView(7, P2PView.LAYOUTTYPE_TOGGEDER);//7是设备类型(技威定义的)
        WindowManager manager = getWindowManager();
        window_width = manager.getDefaultDisplay().getWidth();
        window_height = manager.getDefaultDisplay().getHeight();
        this.initScaleView(this, window_width, window_height);
        setMute(true);
    }

    public void initIpcDeviceList() {
        for (int i = 0; i < number; i++) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.item_device, null);
            final TextView tv_deviceId = (TextView) view
                    .findViewById(R.id.tv_deviceId);
            tv_deviceId.setText(ipcList[i]);
            if (i == 0) {
                tv_deviceId.setTextColor(getResources().getColor(R.color.blue));
            } else {
                tv_deviceId
                        .setTextColor(getResources().getColor(R.color.white));
            }
            devicelist.add(tv_deviceId);
            l_device_list.addView(view);
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    //
                    Message msg = new Message();
                    msg.what = 9;
                    defaultHandler.sendMessage(msg);
                }
            });
        }
    }


    public void changeDeviceListTextColor() {
        for (int i = 0; i < devicelist.size(); i++) {
            if (i == currentNumber) {
                devicelist.get(i).setTextColor(
                        getResources().getColor(R.color.blue));
                devicelist.get(i).setClickable(false);
            } else {
                devicelist.get(i).setTextColor(
                        getResources().getColor(R.color.white));
                devicelist.get(i).setClickable(true);
            }
        }

    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public void switchNext() {
        if (currentNumber < number - 1) {
            currentNumber = currentNumber + 1;
        } else {
            currentNumber = 0;
        }
        P2PHandler.getInstance().reject();
        switchConnect();
        changeDeviceListTextColor();
        callId = ipcList[currentNumber];
        setHeaderImage();
        iv_next.setClickable(false);
    }

    public void switchLast() {
        if (currentNumber > 0) {
            currentNumber = currentNumber - 1;
        } else {
            currentNumber = number - 1;
        }
        P2PHandler.getInstance().reject();
        switchConnect();
        changeDeviceListTextColor();
        callId = ipcList[currentNumber];
        setHeaderImage();
        callDevice();
        iv_last.setClickable(false);
    }

    public void connectDooranerfa() {
        if (isCustomCmdAlarm == true) {
            String cmd_connect = "IPC1anerfa:connect";
            P2PHandler.getInstance().sendCustomCmd(callId, password,
                    cmd_connect, MainApplication.GWELL_LOCALAREAIP);
        }
    }

    public void disconnectDooranerfa() {
        if (isCustomCmdAlarm == true) {
            String cmd_disconnect = "IPC1anerfa:disconnect";
            P2PHandler.getInstance().sendCustomCmd(callId, password,
                    cmd_disconnect, MainApplication.GWELL_LOCALAREAIP);
        }
    }

    public void initPlayCamera() {
        pView = (P2PView) findViewById(R.id.p2pview);
        pView.setVisibility(View.VISIBLE);
        P2PView.type = 0;
        users = (TextView) findViewById(R.id.users);
        users.setText(getString(R.string.monitor_number)+P2PConnect.getNumber());
        pictrues = Utils.getScreenShotImagePath(callId, 1);
        l_control = (LinearLayout) findViewById(R.id.l_control);
        btn_play = (Button) findViewById(R.id.btn_play);
        control_bottom = (RelativeLayout) findViewById(R.id.control_bottom);
        control_top = (LinearLayout) findViewById(R.id.control_top);
        video_mode_hd = (TextView) findViewById(R.id.video_mode_hd);
        video_mode_sd = (TextView) findViewById(R.id.video_mode_sd);
        video_mode_ld = (TextView) findViewById(R.id.video_mode_ld);
        vLineHD = findViewById(R.id.v_line_hd);
        choose_video_format = (Button) findViewById(R.id.choose_video_format);
        close_voice = (ImageView) findViewById(R.id.close_voice);
        send_voice = (ImageView) findViewById(R.id.send_voice);
        layout_voice_state = (LinearLayout) findViewById(R.id.layout_voice_state);
        iv_half_screen = (ImageView) findViewById(R.id.iv_half_screen);
        hungup = (ImageView) findViewById(R.id.hungup);
        screenshot = (ImageView) findViewById(R.id.screenshot);
//        defence_state = (ImageView) findViewById(R.id.defence_state);
        r_p2pview = (RelativeLayout) findViewById(R.id.r_p2pview);
        r_p2pview.setVisibility(View.VISIBLE);
        voice_state = (ImageView) findViewById(R.id.voice_state);
        iv_last = (ImageView) findViewById(R.id.iv_last);
        iv_next = (ImageView) findViewById(R.id.iv_next);
        l_device_list = (LinearLayout) findViewById(R.id.l_device_list);
// 刷新监控
        rlPrgTxError = (RelativeLayout) findViewById(R.id.rl_prgError);
        txError = (TextView) findViewById(R.id.tx_monitor_error);
        btnRefrash = (Button) findViewById(R.id.btn_refrash);
        progressBar = (ProgressBar) findViewById(R.id.prg_monitor);
        tx_wait_for_connect = (TextView) findViewById(R.id.tx_wait_for_connect);
        ivHeader = (HeaderView) findViewById(R.id.hv_header);
        rlPrgTxError.setOnClickListener(this);
        btnRefrash.setOnClickListener(this);
        // 更新头像
        setHeaderImage();
        btn_play.setOnClickListener(this);
        choose_video_format.setOnClickListener(this);
        close_voice.setOnClickListener(this);
        send_voice.setOnClickListener(this);
        iv_half_screen.setOnClickListener(this);
        hungup.setOnClickListener(this);
        screenshot.setOnClickListener(this);
        video_mode_hd.setOnClickListener(this);
        video_mode_sd.setOnClickListener(this);
        video_mode_ld.setOnClickListener(this);
//        defence_state.setOnClickListener(this);
        iv_last.setOnClickListener(this);
        iv_next.setOnClickListener(this);
    }

    public void initcComponent() {
        frushLayout(mContact.contactType);

        final AnimationDrawable anim = (AnimationDrawable) voice_state
                .getDrawable();
        ViewTreeObserver.OnPreDrawListener opdl = new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                anim.start();
                return true;
            }

        };
        voice_state.getViewTreeObserver().addOnPreDrawListener(opdl);
        if (mContact.contactType == P2PValue.DeviceType.NPC) {
            current_video_mode = P2PValue.VideoMode.VIDEO_MODE_LD;
        } else {
            current_video_mode = P2PConnect.getMode();
        }

        updateVideoModeText(current_video_mode);
        if (mContact.contactType != P2PValue.DeviceType.DOORBELL
                && !isSurpportOpenDoor) {
            send_voice.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent event) {
                    int time = 0;
                    //
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            time++;
                            Log.e("时间：", event.getDownTime() + "");
                            hideVideoFormat();
                            layout_voice_state
                                    .setVisibility(RelativeLayout.VISIBLE);

                            send_voice
                                    .setBackgroundResource(R.drawable.ic_send_audio);
                            setMute(false);
                            return true;
                        case MotionEvent.ACTION_UP:
                            layout_voice_state.setVisibility(RelativeLayout.GONE);
                            send_voice
                                    .setBackgroundResource(R.drawable.ic_send_audio);
                            setMute(true);
                            return true;
                        case MotionEvent.ACTION_CANCEL:
                            layout_voice_state.setVisibility(RelativeLayout.GONE);
                            send_voice
                                    .setBackgroundResource(R.drawable.ic_send_audio);
                            setMute(true);
                            return true;
                    }
                    return false;
                }
            });
        } else if (mContact.contactType == P2PValue.DeviceType.DOORBELL
                && !isSurpportOpenDoor) {
            isFirstMute = false;
            send_voice.setOnClickListener(this);
        } else if (isSurpportOpenDoor) {
            Log.e("leleTest", "isSurpportOpenDoor=" + isSurpportOpenDoor);
            // 开始监控时没有声音，暂时这样
            send_voice.setOnClickListener(this);
            // speak();
            // speak();
            // send_voice.performClick();
            // speak();
        }
        initIpcDeviceList();
    }

    /**
     * 初始化摄像头
     */
    private void initCameraCreate() {

        if (mContact.contactType == P2PValue.DeviceType.IPC) {
            setIsLand(false);
        } else {
            setIsLand(true);
        }
        ipcList = getIntent().getStringArrayExtra("ipcList");
        number = getIntent().getIntExtra("number", -1);
        connectType = getIntent().getIntExtra("connectType",
                Constants.ConnectType.P2PCONNECT);
        isSurpportOpenDoor = getIntent().getBooleanExtra("isSurpportOpenDoor",
                false);
        isCustomCmdAlarm = getIntent().getBooleanExtra("isCustomCmdAlarm",
                false);
        callId = mContact.contactId;
        if (number > 0) {
            callId = ipcList[0];
        }
        password = mContact.contactPassword;
        P2PConnect.setMonitorId(callId);// 设置在监控的ID
//        P2PHandler.getInstance().p2pInit(mContext, new P2PListener(), new SettingListener());
        getScreenWithHeigh();
        callDevice();
        initcComponent();
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        }
        mCurrentVolume = mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        mMaxVolume = mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        ScrrenOrientation = Configuration.ORIENTATION_PORTRAIT;
        vibrator = (Vibrator) mContext
                .getSystemService(mContext.VIBRATOR_SERVICE);
    }

    /**********************
     * 摄像头部分结束
     *******************************/

    // 显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow).cacheInMemory(true).cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
            .resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
            // .displayer(new RoundedBitmapDisplayer(20))//是否设置为圆角，弧度为多少
            .displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
            .build();
}
