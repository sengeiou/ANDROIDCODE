package com.smartism.znzk.activity.alert;

import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.camera.ApMonitorActivity;
import com.smartism.znzk.activity.device.BeijingSuoActivity;
import com.smartism.znzk.activity.smartlock.LockMainActivity;
import com.smartism.znzk.activity.smartlock.WifiLockMainActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.communication.service.AudioTipsService;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceInfo.ControlTypeMenu;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.Account;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.global.AccountPersist;
import com.smartism.znzk.global.AppConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.Actions.VersionType;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.DialogView;
import com.smartism.znzk.view.ProgressCycleView;
import com.smartism.znzk.xiongmai.activities.XiongMaiDisplayCameraActivity;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 强力提醒用的页面
 *
 * @author Administrator
 */
public class AlertMessageActivity extends ActivityParentActivity {
    private static final int AUDIO_TIME = 60 * 1000; //播放声音持续时间
    private long deviceid = 0;
    private String from = ""; //手机对手机的触发者
    private ImageView deviceLogo, deblocking_btn;
    private ProgressCycleView deblocking_bg;
    private TextView commandTime, command;
    private DeviceInfo deviceInfo;
    private ZhujiInfo zhuji;
    private WakeLock mWakelock;
    //播放声音的声音池
    private SoundPool soundPool = null; //未用
    private int progress_int = 0;
    private boolean progress_advance = true; //进度条前进 true前进  false后退
    private Timer progress_control = null;
    private TimerTask progress_task = null;
    private boolean isBind = false;
    private final int dHandler_loadsuccess = 5;
    private Contact mCotact;
    private String name;
    private TextView tv_notice;

    CameraInfo seeCamera;//需要查看的摄像头
    //志诚要求
    FrameLayout long_press_layout;
    ImageView zhicheng_look_camera_btn, zhicheng_ignore_btn;
    boolean isIgnore = false; //记录是否点击了忽略按钮
    Button ignore_no_door, look_no_door, lift_no_door;
    RelativeLayout zhicheng_no_door;


    //显示图片的配置
    DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.loading)
            .showImageOnFail(R.drawable.sorrow)
            .cacheInMemory(false)
            .cacheOnDisc(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        }
    };
    private long sendId;


    public class MyPhoneCallListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            switch (state) {
                case TelephonyManager.CALL_STATE_OFFHOOK:                   //电话通话的状态
//                    Toast.makeText(Main.this, "正在通话...", Toast.LENGTH_SHORT).show();
                    break;

                case TelephonyManager.CALL_STATE_RINGING:                   //电话响铃的状态
//                    Toast.makeText(Main.this, incomingNumber, Toast.LENGTH_SHORT).show();
                    if (conn != null && isBind == true) {
                        unbindService(conn);
                        isBind = false;
                    }
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    private TelephonyManager tm;
    private Intent intent;
    private List<DeviceInfo> deviceInfos;
    private boolean isFmqOpen;
    private DeviceInfo mXiongMaiCamareInfo;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0://获取到绑定的摄像头信息
//                    mCotact = (Contact) msg.obj;
                    Intent monitor = (Intent) msg.obj;
                    startActivity(monitor);
                    break;
                case 1: //加载设备数据完成
                    setDeviceInfoToPage();
                    if (tm.getCallState() != TelephonyManager.CALL_STATE_RINGING) {
                        intent = new Intent(getApplicationContext(), AudioTipsService.class);
                        intent.putExtra("devId", deviceid + "");
                        isBind = bindService(intent, conn, Context.BIND_AUTO_CREATE);
                        defaultHandler.sendEmptyMessageDelayed(3, AUDIO_TIME);
                    }

                    if (Actions.VersionType.CHANNEL_ZHICHENG.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                        if (zhuji != null && zhuji.getCa().equals(DeviceInfo.CaMenu.wifizns.value())) {
                            CommandInfo commandInfos = DatabaseOperator.getInstance().queryLastCommand(zhuji.getId());
                            long_press_layout.setVisibility(View.GONE);
                            if (commandInfos != null && CommandInfo.SpecialEnum.doorbell.value() == commandInfos.getSpecial()) {
                                mZhiChengLinearLayout.setVisibility(View.VISIBLE);
                            } else {
                                zhicheng_no_door.setVisibility(View.VISIBLE);
                            }
                        } else if (deviceInfo != null && deviceInfo.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())) {
                            CommandInfo commandInfos = DatabaseOperator.getInstance().queryLastCommand(deviceInfo.getId());
                            long_press_layout.setVisibility(View.GONE);
                            if (commandInfos != null && CommandInfo.SpecialEnum.doorbell.value() == commandInfos.getSpecial()) {
                                mZhiChengLinearLayout.setVisibility(View.VISIBLE);
                            } else {
                                zhicheng_no_door.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    //响铃时间1分钟
                    break;
                case 2:
                    if (progress_advance) {
                        deblocking_bg.setVisibility(View.VISIBLE);
                        progress_int += 1;
                        if (progress_int >= 100) {
                            if (progress_control != null) {
                                progress_control.cancel();
                                progress_control = null;
                            }
                            if (deviceInfo != null) {
                                if (deviceInfo.getCak().contains(DeviceInfo.CakMenu.security.value())) {
                                    intent = new Intent(Actions.DEVICE_STATUS_NORMAL);
                                    intent.putExtra("devicdid", deviceid);
                                    sendBroadcast(intent);
                                }

                                String toCamera = null;
                                if (!StringUtils.isEmpty(deviceInfo.getBipc()) && !"0".equals(deviceInfo.getBipc())) {
                                    toCamera = getString(R.string.lookover);
                                    CameraInfo c = null;
                                    List<CameraInfo> cameraInfos = DatabaseOperator.getInstance(mContext.getApplicationContext()).queryAllCameras(zhuji);
                                    //雄迈摄像头单独作为一个主机
                                    List<ZhujiInfo> infos = DatabaseOperator.getInstance(mContext).queryAllZhuJiInfos();
                                    for (ZhujiInfo zhujiInfo : infos) {
                                        if (zhujiInfo.getCameraInfo().getC() == null) {
                                            continue;
                                        }
                                        if (zhujiInfo.getCa().equals(DeviceInfo.CaMenu.ipcamera.value())
                                                && (zhujiInfo.getCameraInfo().getC().equals(CameraInfo.CEnum.xiongmai.value()))) {
                                            if (!cameraInfos.contains(zhujiInfo.getCameraInfo())) {
                                                cameraInfos.add(zhujiInfo.getCameraInfo());
                                            }
                                        }
                                    }
                                    if (!cameraInfos.isEmpty()) {
                                        for (CameraInfo cs : cameraInfos) {
                                            if (cs.getIpcid() == Long.parseLong(deviceInfo.getBipc())) {
                                                if (cs.getC().equals(CameraInfo.CEnum.xiongmai.value())) {
                                                    //雄迈，保存一下，后面需要用于跳转到摄像头页面
                                                    for (ZhujiInfo zhujiInfo : infos) {
                                                        if (zhujiInfo.getCameraInfo().getC() == null
                                                                || !zhujiInfo.getCa().equals(DeviceInfo.CaMenu.ipcamera.value())) {
                                                            continue;
                                                        }
                                                        if (zhujiInfo.getCameraInfo().getIpcid() == Long.parseLong(deviceInfo.getBipc())) {
                                                            //找到了
                                                            mXiongMaiCamareInfo = Util.getZhujiDevice(zhujiInfo);
                                                        }
                                                    }
                                                }
                                                c = cs;
                                                Contact contact = new Contact();
                                                contact.contactId = c.getId();
                                                contact.contactName = c.getN();
                                                contact.contactPassword = c.getP();
                                                contact.userPassword = c.getOriginalP();
                                                mCotact = contact;
                                                seeCamera = cs;
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (mCotact == null) toCamera = null;
                                if ((deviceInfo.getCak().contains("security")
                                        || (DeviceInfo.CaMenu.zhujiControl.value().equals(deviceInfo.getCa())))) {
                                    CharSequence positiveText = getString(R.string.lift);
                                    //宏才文字显示红色
                                    if (judgeHCTC()) {
                                        SpannableString spannableString = new SpannableString(getString(R.string.no_defence));
                                        ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.RED);
                                        spannableString.setSpan(colorSpan, 0, getString(R.string.no_defence).length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                                        positiveText = spannableString;
                                    }
                                    //这里
                                    DialogView dialogView = new DialogView(AlertMessageActivity.this, false,
                                            getString(R.string.activity_alertmessage_jctip),
                                            judgeHCTC() ? getString(R.string.hongcai_tantou_activity_alertmessage_jcmessage) : getString(R.string.activity_alertmessage_jcmessage),
                                            getString(R.string.ignore),
                                            toCamera,
                                            positiveText, new DialogView.DialogViewItemListener() {
                                        @Override
                                        public void onItemListener(DialogView dialogView, View view, int index) {
                                            switch (index) {
                                                case 0:
                                                    /**巨将适用，直接下发给主机**/
                                                    if (VersionType.CHANNEL_JUJIANG.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                                                        SyncMessage message1 = new SyncMessage();
                                                        message1.setCommand(SyncMessage.CommandMenu.rq_controlRemind.value());
                                                        message1.setDeviceid(deviceid);
                                                        message1.setSyncBytes(new byte[]{0x10});
                                                        SyncMessageContainer.getInstance()
                                                                .produceSendMessage(message1);
                                                    }
                                                    dialogView.dismiss();
                                                    finish();
                                                    break;
                                                case 1:
                                                    Intent monitor = new Intent();
                                                    monitor.setAction(Constants.Action.EXITE_AP_MODE);
                                                    sendBroadcast(monitor);
                                                    monitor = new Intent();
                                                    if (seeCamera != null) {
                                                        if (seeCamera.getC().equals(CameraInfo.CEnum.xiongmai.value())) {
                                                            monitor.setClass(AlertMessageActivity.this, XiongMaiDisplayCameraActivity.class);
                                                            monitor.putExtra("deviceInfo", mXiongMaiCamareInfo);
                                                            monitor.putExtra("contact", mCotact);
                                                            startActivity(monitor);
                                                        } else if (seeCamera.getC().equals(CameraInfo.CEnum.jiwei.value())) {
                                                            monitor.setClass(AlertMessageActivity.this, ApMonitorActivity.class);
                                                            monitor.putExtra("flag", true);
                                                            monitor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                            monitor.putExtra("contact", mCotact);
                                                            monitor.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
                                                            verifyIPCLogin(monitor);
                                                        }
                                                    }
//                                                startActivity(monitor);
                                                    dialogView.dismiss();
                                                    finish();
                                                    break;
                                                case 2:
                                                    if(zhuji!=null&&zhuji.getMasterid().contains("FF3B")){
                                                        //艾立恩主机,不撤防，只让主机不叫
                                                        SyncMessage message = new SyncMessage();
                                                        message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                                                        message.setDeviceid(zhuji.getId());
                                                        message.setSyncBytes(new byte[]{102});
                                                        SyncMessageContainer.getInstance().produceSendMessage(message);
                                                        finish();
                                                    }else{
                                                        if (!deviceInfo.isFa()) {//当设备不是24小时防区的则撤防，否则只需要让主机不要叫了即可
                                                            mContext.showInProgress(getString(R.string.loading), false, false);
                                                            JavaThreadPool.getInstance().excute(new TriggerScene(0));
                                                        } else {
                                                            if (zhuji != null) {
                                                                SyncMessage message = new SyncMessage();
                                                                message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                                                                message.setDeviceid(zhuji.getId());
                                                                message.setSyncBytes(new byte[]{102});
                                                                SyncMessageContainer.getInstance().produceSendMessage(message);
                                                                finish();
                                                            }
                                                        }
                                                    }

//                                                    message1 = new SyncMessage();
//                                                    message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
//                                                    message1.setDeviceid(deviceid);
//                                                    message1.setSyncBytes(new byte[]{0x00});
//                                                    SyncMessageContainer.getInstance()
//                                                            .produceSendMessage(message1);
//                                                    refreshData();
                                                    dialogView.dismiss();
                                                    break;
                                            }

                                        }

                                    });
                                    dialogView.setOnKeyListener(new DialogInterface.OnKeyListener() {
                                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                            return true;
                                        }
                                    });
                                    dialogView.show();
                                } else if (DeviceInfo.CaMenu.menling.value().equals(deviceInfo.getCa()) && toCamera != null) { //探头和主机遥控器之外的特殊处理
                                    DialogView dialogView = new DialogView(AlertMessageActivity.this, false,
                                            getString(R.string.activity_alertmessage_tip),
                                            getString(R.string.activity_alertmessage_choose),
                                            getString(R.string.ignore),
                                            null,
                                            toCamera, new DialogView.DialogViewItemListener() {
                                        @Override
                                        public void onItemListener(DialogView dialogView, View view, int index) {
                                            switch (index) {
                                                case 0:
                                                    dialogView.dismiss();
                                                    finish();
                                                    break;
                                                case 1:
                                                    //other按键触发
                                                    dialogView.dismiss();
                                                    break;
                                                case 2:
                                                    Intent monitor = new Intent();
                                                    if (seeCamera != null) {
                                                        if (seeCamera.getC().equals(CameraInfo.CEnum.xiongmai.value())) {
                                                            monitor.setClass(AlertMessageActivity.this, XiongMaiDisplayCameraActivity.class);
                                                            monitor.putExtra("deviceInfo", mXiongMaiCamareInfo);
                                                            monitor.putExtra("contact", mCotact);
                                                            startActivity(monitor);
                                                        } else if (seeCamera.getC().equals(CameraInfo.CEnum.jiwei.value())) {
                                                            monitor.setClass(AlertMessageActivity.this, ApMonitorActivity.class);
                                                            monitor.putExtra("flag", true);
                                                            monitor.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                            monitor.putExtra("contact", mCotact);
                                                            monitor.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
                                                            verifyIPCLogin(monitor);
                                                        }
                                                    }
                                                    dialogView.dismiss();
                                                    finish();
                                                    break;
                                            }

                                        }

                                    });
                                    dialogView.setOnKeyListener(new DialogInterface.OnKeyListener() {
                                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                            return true;
                                        }
                                    });
                                    dialogView.show();
                                } else if (DeviceInfo.CaMenu.zhinengsuo.value().equals(deviceInfo.getCa())) {
                                    if (VersionType.CHANNEL_ZHICHENG.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                                        if (!isIgnore) {
                                            //点击查看按钮，不管有没有绑定摄像头，直接跳转
                                            Intent temp = new Intent();
                                            temp.setClass(AlertMessageActivity.this, LockMainActivity.class);
                                            temp.putExtra("zhuji", zhuji);
                                            temp.putExtra("device", deviceInfo);
                                            startActivity(temp);
                                        }
                                        finish();
                                    } else if (VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                                            && !DeviceInfo.CaMenu.zhinengsuo.value().equals(deviceInfo.getCa())) {
                                        finish();
                                    } else {
                                        if (seeCamera != null) {
                                            DialogView dialogView = new DialogView(AlertMessageActivity.this, false,
                                                    getString(R.string.activity_alertmessage_jctip),
                                                    getString(R.string.activity_alertmessage_jcmessage),
                                                    getString(R.string.ignore),
                                                    getString(R.string.lookover),
                                                    getString(R.string.lift), new DialogView.DialogViewItemListener() {
                                                @Override
                                                public void onItemListener(DialogView dialogView, View view, int index) {
                                                    switch (index) {
                                                        case 0:
                                                            dialogView.dismiss();
                                                            finish();
                                                            break;
                                                        case 2:
                                                            if (!deviceInfo.isFa()) {//当设备不是24小时防区的则撤防，否则只需要让主机不要叫了即可
                                                                mContext.showInProgress(getString(R.string.loading), false, false);
                                                                JavaThreadPool.getInstance().excute(new TriggerScene(0));
                                                            } else {
                                                                if (zhuji != null) {
                                                                    SyncMessage message = new SyncMessage();
                                                                    message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                                                                    message.setDeviceid(zhuji.getId());
                                                                    message.setSyncBytes(new byte[]{102});
                                                                    SyncMessageContainer.getInstance().produceSendMessage(message);
                                                                    finish();
                                                                }
                                                            }
                                                            dialogView.dismiss();
                                                            break;
                                                        case 1:
                                                            Intent monitor = new Intent();
                                                            monitor.setAction(Constants.Action.EXITE_AP_MODE);
                                                            sendBroadcast(monitor);
                                                            if (VersionType.CHANNEL_RUNLONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                                                                monitor.putExtra("suo_alarm", true); //锁端非法开锁报警
                                                                monitor.setClass(AlertMessageActivity.this, BeijingSuoActivity.class);
                                                            } else {
                                                                monitor.setClass(AlertMessageActivity.this, LockMainActivity.class);
                                                            }
                                                            monitor.putExtra("zhuji", zhuji);
                                                            monitor.putExtra("device", deviceInfo);
                                                            startActivity(monitor);
                                                            dialogView.dismiss();
                                                            finish();
                                                            break;
                                                    }

                                                }
                                            });
                                            dialogView.show();
                                        } else {
                                            DialogView dialogView = new DialogView(AlertMessageActivity.this, false,
                                                    getString(R.string.activity_alertmessage_jctip),
                                                    getString(R.string.activity_alertmessage_jcmessage),
                                                    getString(R.string.ignore),
                                                    null,
                                                    getString(R.string.lift), new DialogView.DialogViewItemListener() {
                                                @Override
                                                public void onItemListener(DialogView dialogView, View view, int index) {
                                                    switch (index) {
                                                        case 0:
                                                            dialogView.dismiss();
                                                            finish();
                                                            break;
                                                        case 2:
                                                            if (!deviceInfo.isFa()) {//当设备不是24小时防区的则撤防，否则只需要让主机不要叫了即可
                                                                mContext.showInProgress(getString(R.string.loading), false, false);
                                                                JavaThreadPool.getInstance().excute(new TriggerScene(0));
                                                            } else {
                                                                if (zhuji != null) {
                                                                    SyncMessage message = new SyncMessage();
                                                                    message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                                                                    message.setDeviceid(zhuji.getId());
                                                                    message.setSyncBytes(new byte[]{102});
                                                                    SyncMessageContainer.getInstance().produceSendMessage(message);
                                                                    finish();
                                                                }
                                                            }
                                                            dialogView.dismiss();
                                                            break;
                                                    }

                                                }
                                            });
                                            dialogView.show();
                                        }
                                    }

                                } else {
                                    finish();
                                }
                            } else if (zhuji != null) {//设备为空，主机不为空时 是主机的报警信息，直接解除并关闭此页面,不一定是报警，可能是wifi锁门铃
                                //紧急
                                CommandInfo commandInfos = DatabaseOperator.getInstance().queryLastCommand(zhuji.getId());
//                                if((zhuji.getCa().equals(DeviceInfo.CaMenu.menling.value())|| (commandInfos != null && CommandInfo.SpecialEnum.doorbell.value() == commandInfos.getSpecial())))
                                if (zhuji.getCa().equals(DeviceInfo.CaMenu.zhuji.value())) {
                                    CharSequence message = getString(R.string.activity_alertmessage_jcmessage);
                                    //宏才要求显示信息字体一行大一行小
                                    if (judgeHCTC()) {
                                        String temp = getString(R.string.hongcai_eem_activity_alertmessage_firstmessage);//后面加了空格
                                        SpannableStringBuilder ssb = new SpannableStringBuilder(temp);
                                        ssb.setSpan(new AbsoluteSizeSpan((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()))
                                                , temp.length() - 1, temp.length(), SpannableStringBuilder.SPAN_EXCLUSIVE_INCLUSIVE);
                                        ssb.insert(temp.length(), getString(R.string.hongcai_eem_activity_alertmessage_lastmessage));
                                        message = ssb;
                                    }
                                    DialogView dialogView = new DialogView(AlertMessageActivity.this, false,
                                            getString(R.string.activity_alertmessage_jctip),
                                            message,
                                            judgeHCTC() ? getString(R.string.hongcai_zhixi_tip) : getString(R.string.ignore),
                                            null,
                                            judgeHCTC() ? getString(R.string.hongcai_zhongyaoalarm) : getString(R.string.lift), new DialogView.DialogViewItemListener() {
                                        @Override
                                        public void onItemListener(DialogView dialogView, View view, int index) {
                                            switch (index) {
                                                case 0:
                                                    finish();
                                                    break;
                                                case 1:
                                                    break;
                                                case 2:
                                                    if ("FF1F".equals(zhuji.getMasterid().substring(0, 4)) || "FF1E".equals(zhuji.getMasterid().substring(0, 4))) {//宏泰手机触发紧急，让主机撤防。
                                                        mContext.showInProgress(getString(R.string.loading), false, false);
                                                        JavaThreadPool.getInstance().excute(new TriggerScene(0));
                                                    } else {
                                                        SyncMessage message = new SyncMessage();
                                                        message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                                                        message.setDeviceid(zhuji.getId());
                                                        message.setSyncBytes(new byte[]{102});
                                                        SyncMessageContainer.getInstance().produceSendMessage(message);
                                                        finish();
                                                    }
                                                    break;
                                            }
                                        }
                                    });
                                    dialogView.setOnKeyListener(new DialogInterface.OnKeyListener() {
                                        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                                            return true;
                                        }
                                    });
                                    dialogView.show();
                                } else if (zhuji.getCa().equals(DeviceInfo.CaMenu.wifizns.value())) {
                                    if (VersionType.CHANNEL_ZHICHENG.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                                        if (!isIgnore) {
                                            //点击查看按钮，不管有没有绑定摄像头，直接跳转
                                            Intent temp = new Intent();
                                            temp.setAction(Constants.Action.EXITE_AP_MODE);
                                            sendBroadcast(temp);
                                            temp.setClass(AlertMessageActivity.this, WifiLockMainActivity.class);
                                            temp.putExtra("zhuji", zhuji);
                                            temp.putExtra("device", deviceInfo);
                                            startActivity(temp);
                                        }
                                        finish();
                                    } else {
                                        //wifi锁 ，门铃是3，密码冻结0
                                        String bIpc = String.valueOf(zhuji.getBipc());
                                        if (!TextUtils.isEmpty(bIpc) && !bIpc.equals("0")) {
                                            //说明绑定了摄像头,门铃的话不需要显示解除按钮，报警的话需要显示解除按钮
                                            if (commandInfos.getSpecial() == 0) {
                                                //密码冻结
                                                DialogView dialogView = new DialogView(AlertMessageActivity.this, false,
                                                        getString(R.string.activity_alertmessage_jctip),
                                                        getString(R.string.activity_alertmessage_jcmessage),
                                                        getString(R.string.ignore),
                                                        getString(R.string.lookover),
                                                        getString(R.string.lift), new DialogView.DialogViewItemListener() {
                                                    @Override
                                                    public void onItemListener(DialogView dialogView, View view, int index) {
                                                        switch (index) {
                                                            case 0:
                                                            case 2:
                                                                finish();
                                                                break;
                                                            case 1:
                                                                Intent monitor = new Intent();
                                                                monitor.setAction(Constants.Action.EXITE_AP_MODE);
                                                                sendBroadcast(monitor);
                                                                monitor.setClass(AlertMessageActivity.this, WifiLockMainActivity.class);
                                                                monitor.putExtra("zhuji", zhuji);
                                                                monitor.putExtra("device", Util.getZhujiDevice(zhuji));
                                                                startActivity(monitor);
                                                                finish();
                                                                break;
                                                        }

                                                    }
                                                });
                                                dialogView.show();
                                            } else if (commandInfos.getSpecial() == 3) {
                                                //门铃
                                                DialogView dialogView = new DialogView(AlertMessageActivity.this, false,
                                                        getString(R.string.activity_alertmessage_jctip),
                                                        getString(R.string.activity_alertmessage_jcmessage),
                                                        getString(R.string.ignore),
                                                        getString(R.string.lookover),
                                                        null, new DialogView.DialogViewItemListener() {
                                                    @Override
                                                    public void onItemListener(DialogView dialogView, View view, int index) {
                                                        switch (index) {
                                                            case 0:
                                                            case 2:
                                                                finish();
                                                                break;
                                                            case 1:
                                                                Intent monitor = new Intent();
                                                                monitor.setAction(Constants.Action.EXITE_AP_MODE);
                                                                sendBroadcast(monitor);
                                                                monitor.setClass(AlertMessageActivity.this, WifiLockMainActivity.class);
                                                                monitor.putExtra("zhuji", zhuji);
                                                                monitor.putExtra("device", Util.getZhujiDevice(zhuji));
                                                                startActivity(monitor);
                                                                finish();
                                                                break;
                                                        }

                                                    }
                                                });
                                                dialogView.show();
                                            }

                                        } else {
                                            //没有绑定摄像头，门铃不显示任何按钮，报警显示按钮
                                            if (commandInfos.getSpecial() == 0) {
                                                //密码冻结
                                                DialogView dialogView = new DialogView(AlertMessageActivity.this, false,
                                                        getString(R.string.activity_alertmessage_jctip),
                                                        getString(R.string.activity_alertmessage_jcmessage),
                                                        getString(R.string.ignore),
                                                        null,
                                                        getString(R.string.lift), new DialogView.DialogViewItemListener() {
                                                    @Override
                                                    public void onItemListener(DialogView dialogView, View view, int index) {
                                                        switch (index) {
                                                            case 0:
                                                            case 2:
                                                                finish();
                                                                break;
                                                        }

                                                    }
                                                });
                                                dialogView.show();
                                            } else {
                                                finish();
                                            }
                                        }
                                    }
                                } else if (zhuji.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())) {
                                    String bIpc = String.valueOf(zhuji.getBipc());
                                    if (!TextUtils.isEmpty(bIpc) && !bIpc.equals("0")) {
                                        //绑定摄像头
                                        DialogView dialogView = new DialogView(AlertMessageActivity.this, false,
                                                getString(R.string.activity_alertmessage_jctip),
                                                getString(R.string.activity_alertmessage_jcmessage),
                                                getString(R.string.ignore),
                                                getString(R.string.lookover),
                                                getString(R.string.lift), new DialogView.DialogViewItemListener() {
                                            @Override
                                            public void onItemListener(DialogView dialogView, View view, int index) {
                                                switch (index) {
                                                    case 0:
                                                    case 2:
                                                        finish();
                                                        break;
                                                    case 1:
                                                        Intent monitor = new Intent();
                                                        monitor.setAction(Constants.Action.EXITE_AP_MODE);
                                                        sendBroadcast(monitor);
                                                        monitor.setClass(AlertMessageActivity.this, LockMainActivity.class);
                                                        monitor.putExtra("zhuji", zhuji);
                                                        monitor.putExtra("device", deviceInfo);
                                                        startActivity(monitor);
                                                        finish();
                                                        break;
                                                }

                                            }
                                        });
                                        dialogView.show();
                                    } else {
                                        DialogView dialogView = new DialogView(AlertMessageActivity.this, false,
                                                getString(R.string.activity_alertmessage_jctip),
                                                getString(R.string.activity_alertmessage_jcmessage),
                                                getString(R.string.ignore),
                                                null,
                                                getString(R.string.lift), new DialogView.DialogViewItemListener() {
                                            @Override
                                            public void onItemListener(DialogView dialogView, View view, int index) {
                                                switch (index) {
                                                    case 0:
                                                    case 2:
                                                        finish();
                                                        break;
                                                }

                                            }
                                        });
                                        dialogView.show();
                                    }
                                } else {
                                    finish();
                                }
                            } else {
                                finish();
                            }
                        }
                        deblocking_bg.setProgress(progress_int);
                    } else {
                        progress_int -= 1;
                        deblocking_bg.setProgress(progress_int);
                        if (progress_int <= 0) {
                            deblocking_bg.setVisibility(View.GONE);
                            if (progress_control != null) {
                                progress_control.cancel();
                                progress_control = null;
                            }
                        }
                    }
                    break;
                case 3:
                    //三分钟后响铃关闭
                    if (isBind) {
                        unbindService(conn);
                        isBind = false;
                    }

                    break;
                case 4: //更新页面
                    setDeviceInfoToPage();
                    if (!isBind) {
                        setDeviceInfoToPage();
                        intent = new Intent(getApplicationContext(), AudioTipsService.class);
                        intent.putExtra("devId", deviceid + "");
                        isBind = bindService(intent, conn, Context.BIND_AUTO_CREATE);
                        defaultHandler.sendEmptyMessageDelayed(3, 60 * 1000);
                    }

                    if (Actions.VersionType.CHANNEL_ZHICHENG.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                        if (zhuji != null && zhuji.getCa().equals(DeviceInfo.CaMenu.wifizns.value())) {
                            CommandInfo commandInfos = DatabaseOperator.getInstance().queryLastCommand(zhuji.getId());
                            long_press_layout.setVisibility(View.GONE);
                            if (commandInfos != null && CommandInfo.SpecialEnum.doorbell.value() == commandInfos.getSpecial()) {
                                mZhiChengLinearLayout.setVisibility(View.VISIBLE);
                                zhicheng_no_door.setVisibility(View.GONE);
                            } else {
                                mZhiChengLinearLayout.setVisibility(View.GONE);
                                zhicheng_no_door.setVisibility(View.VISIBLE);
                            }
                        } else if (deviceInfo != null && deviceInfo.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())) {
                            CommandInfo commandInfos = DatabaseOperator.getInstance().queryLastCommand(deviceInfo.getId());
                            long_press_layout.setVisibility(View.GONE);
                            if (commandInfos != null && CommandInfo.SpecialEnum.doorbell.value() == commandInfos.getSpecial()) {
                                mZhiChengLinearLayout.setVisibility(View.VISIBLE);
                                zhicheng_no_door.setVisibility(View.GONE);
                            } else {
                                mZhiChengLinearLayout.setVisibility(View.GONE);
                                zhicheng_no_door.setVisibility(View.VISIBLE);
                            }
                        } else {
                            if (mZhiChengLinearLayout.getVisibility() == View.VISIBLE || zhicheng_no_door.getVisibility() == View.VISIBLE) {
                                mZhiChengLinearLayout.setVisibility(View.GONE);
                                zhicheng_no_door.setVisibility(View.GONE);
                                long_press_layout.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    //响铃时间1分钟
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    class TriggerScene implements Runnable {
        private int sId;


        public TriggerScene(int sId) {
            this.sId = sId;
        }

        @Override
        public void run() {
            JSONObject o = new JSONObject();
            o.put("id", sId);
            o.put("did", zhuji.getId());
            String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            String result = HttpRequestUtils
                    .requestoOkHttpPost(server + "/jdm/s3/scenes/trigger", o, mContext);
            if ("0".equals(result)) {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();

                        Toast.makeText(mContext, getString(R.string.success),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } else {
                defaultHandler.post(new Runnable() {
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.net_error_operationfailed),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }


    /**
     * 验证IP摄像头是否登录，并打开摄像头
     */
    private void verifyIPCLogin(Intent intent) {
        Account activeUser = AccountPersist.getInstance().getActiveAccountInfo(mContext);

        if (activeUser != null && !activeUser.three_number.equals("0517401")) {
            if (intent == null) return;
            NpcCommon.mThreeNum = activeUser.three_number;
//            P2PHandler.getInstance().p2pInit(mContext, new P2PListener(), new SettingListener());
            NpcCommon.verifyNetwork(mContext);
            connect();
            Log.e("log", "verifyIPCLogin");
            startActivity(intent);
            return;
        }
        //      Toast.makeText(mContext, R.string.net_error_loginoutofdayipc, Toast.LENGTH_SHORT).show();
    }

    private boolean judgeHCTC() {
        return VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion());
    }

    private void connect() {
        Intent service = new Intent(MainApplication.MAIN_SERVICE_START);
        service.setPackage(mContext.getPackageName());
        mContext.startService(service);
        if (AppConfig.DeBug.isWrightAllLog) {
            Intent log = new Intent(MainApplication.LOGCAT);
            log.setPackage(mContext.getPackageName());
            mContext.startService(log);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        MainApplication.app.setAlertMessageActivity(this);
        deviceid = getIntent().getLongExtra("deviceid", 0);//此ID可能为主机ID 也可能为设备ID
        from = getIntent().getStringExtra("from");//panic触发者
        setContentView(R.layout.activity_alertmessage);
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(new MyPhoneCallListener(), PhoneStateListener.LISTEN_CALL_STATE);
        deviceInfos = new ArrayList<>();
        //判断屏幕是否锁屏
        KeyguardManager km =
                (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        } else {
//			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//		            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//		            | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
//		            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//			setRequestedOrientation(0);
        }
        tv_notice = (TextView) findViewById(R.id.tv_notice);
        deviceLogo = (ImageView) findViewById(R.id.am_devicelogo);
        deblocking_btn = (ImageView) findViewById(R.id.am_controlbtn);
        deblocking_bg = (ProgressCycleView) findViewById(R.id.am_controlbg);
        deblocking_bg.setVisibility(View.GONE);
        deblocking_btn.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN && v.getId() == R.id.am_controlbtn) {
                    progress_advance = true;
                    if (progress_control == null) {
                        progress_control = new Timer();
                        progress_task = new TimerTask() {

                            @Override
                            public void run() {
                                defaultHandler.sendEmptyMessage(2);
                            }
                        };
                        progress_control.schedule(progress_task, 0, 10);
                    }
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP && v.getId() == R.id.am_controlbtn) {
                    progress_advance = false;
                    //Log.e("aaa", "解除报警页面是否需要手势密码");
                    //dcsp.putBoolean(Constant.IS_LOOKS, true).commit();
                    return true;
                }
                return false;
            }
        });
        commandTime = (TextView) findViewById(R.id.am_commandtime);
        command = (TextView) findViewById(R.id.am_command);
        JavaThreadPool.getInstance().excute(new loadDevicesInfo(1, deviceid));


        //志诚
        if (VersionType.CHANNEL_ZHICHENG.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
            mZhiChengLinearLayout = findViewById(R.id.zhicheng_parent);
            zhicheng_no_door = findViewById(R.id.zhicheng_no_door);
            ignore_no_door = findViewById(R.id.ignore_zhicheng_btn);
            look_no_door = findViewById(R.id.look_zhicheng_btn);
            lift_no_door = findViewById(R.id.release_zhicheng_btn);
            View tempView = findViewById(R.id.center_view);
            zhicheng_ignore_btn = findViewById(R.id.zhicheng_ignore_btn);
            zhicheng_look_camera_btn = findViewById(R.id.zhicheng_look_camera_btn);
            long_press_layout = findViewById(R.id.long_press_layout_parent);
            ViewGroup.LayoutParams lp = tempView.getLayoutParams();
            lp.width = getResources().getDisplayMetrics().widthPixels / 3;
            tempView.setLayoutParams(lp);
            View.OnClickListener temp = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getId() == R.id.zhicheng_ignore_btn || v.getId() == R.id.ignore_zhicheng_btn || v.getId() == R.id.release_zhicheng_btn) {
                        isIgnore = true;
                    }
                    progress_advance = true;
                    progress_int = 200;
                    defaultHandler.sendEmptyMessage(2);
                }
            };
            look_no_door.setOnClickListener(temp);
            lift_no_door.setOnClickListener(temp);
            ignore_no_door.setOnClickListener(temp);
            zhicheng_look_camera_btn.setOnClickListener(temp);
            zhicheng_ignore_btn.setOnClickListener(temp);
        }
    }

    RelativeLayout mZhiChengLinearLayout;

    class loadAllDevicesInfo implements Runnable {

        public loadAllDevicesInfo() {
        }


        @Override
        public void run() {
            Cursor cursor = DatabaseOperator.getInstance(AlertMessageActivity.this).getReadableDatabase().rawQuery(
                    "select * from DEVICE_STATUSINFO where zj_id = ? ",
                    new String[]{String.valueOf(zhuji.getId())});
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    DeviceInfo deviceInfo = DatabaseOperator.getInstance(AlertMessageActivity.this).buildDeviceInfo(cursor);
                    if ("zhuji_fmq".equals(deviceInfo.getCa())) {
                        if (deviceInfo.getDr() == 0) {
                            isFmqOpen = false;
                        } else {
                            isFmqOpen = true;
                            sendId = deviceInfo.getId();
                        }

                        Log.e("alertmessage_fmq: ", "" + isFmqOpen);
                        break;
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            if (!isFmqOpen) {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
                return;
            }
            SyncMessage message1 = new SyncMessage();
            message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
            message1.setDeviceid(sendId);
            message1.setSyncBytes(new byte[]{0x00});
            SyncMessageContainer.getInstance()
                    .produceSendMessage(message1);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SyncMessage message2 = new SyncMessage();
            message2.setCommand(SyncMessage.CommandMenu.rq_control.value());
            message2.setDeviceid(sendId);
            message2.setSyncBytes(new byte[]{0x01});
            SyncMessageContainer.getInstance()
                    .produceSendMessage(message2);
            defaultHandler.post(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
//            Message m = defaultHandler.obtainMessage(this.what);
//            m.obj = deviceList;
//            defaultHandler.sendMessage(m);
        }
    }

    private void refreshData() {
        JavaThreadPool.getInstance().excute(new loadAllDevicesInfo());
    }


    @Override
    protected void onNewIntent(Intent intent) {//再次调用此页面触发
        super.onNewIntent(intent);
        deviceid = intent.getLongExtra("deviceid", 0);//此ID可能为主机ID 也可能为设备ID
        from = intent.getStringExtra("from");//panic触发者
        JavaThreadPool.getInstance().excute(new loadDevicesInfo(4, deviceid));
    }

    @Override
    protected void onResume() {
        boolean showClock = dcsp.getBoolean(Constant.IS_LOOKS, false);
        dcsp.putBoolean(Constant.IS_LOOKS, false).commit();
        super.onResume();
        dcsp.putBoolean(Constant.IS_LOOKS, showClock).commit();
        acquireWakeLock();
    }


    private void acquireWakeLock() {
        if (mWakelock == null) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());
            mWakelock.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseWakeLock();
    }

    private void releaseWakeLock() {
        if (mWakelock != null && mWakelock.isHeld()) {
            mWakelock.release();
            mWakelock = null;
        }
    }

    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        defaultHandler.removeCallbacksAndMessages(null);
        defaultHandler = null;
        if (isBind) {
            unbindService(conn);
            isBind = false;
        }
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //屏蔽所有按钮
        return true;
    }

    class loadDevicesInfo implements Runnable {
        private int what;
        private long did;

        public loadDevicesInfo() {
        }

        public loadDevicesInfo(int what, long deviceid) {
            this.what = what;
            this.did = deviceid;
        }

        @Override
        public void run() {
            deviceInfo = DatabaseOperator.getInstance(AlertMessageActivity.this).queryDeviceInfo(did);
            if (deviceInfo != null) {
                if (!StringUtils.isEmpty(deviceInfo.getBipc()) && !"0".equals(deviceInfo.getBipc())) {
                    JavaThreadPool.getInstance().excute(new BindingCameraLoad(deviceInfo.getBipc(), 0));
                }
                zhuji = DatabaseOperator.getInstance(AlertMessageActivity.this).queryDeviceZhuJiInfo(deviceInfo.getZj_id());
            } else {
                zhuji = DatabaseOperator.getInstance(AlertMessageActivity.this).queryDeviceZhuJiInfo(did);
            }
            Message m = defaultHandler.obtainMessage(this.what);
            defaultHandler.sendMessage(m);
        }
    }


    /**
     * 请求数据子线程
     */
    class BindingCameraLoad implements Runnable {
        private long uid;
        private String code;
        private String bIpc;
        private int what;

        public BindingCameraLoad(String bIpc, int what) {
            this.bIpc = bIpc;
            this.what = what;
        }

        @Override
        public void run() {
            CameraInfo c = null;
            List<CameraInfo> cameraInfos = DatabaseOperator.getInstance(mContext.getApplicationContext()).queryAllCameras(zhuji);
            if (!cameraInfos.isEmpty()) {
                for (CameraInfo cs : cameraInfos) {
                    if (cs.getIpcid() == Long.parseLong(bIpc)) {
                        c = cs;
                        break;
                    }
                }
            }
            if (c != null) {
                Contact contact = new Contact();
                contact.contactId = c.getId();
                contact.contactName = c.getN();
                contact.contactPassword = c.getP();
                contact.userPassword = c.getOriginalP();
                try {
                    contact.ipadressAddress = InetAddress.getByName("192.168.1.1");
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                mCotact = contact;
//                Message message = new Message();
//                message.what = this.what;
//                message.obj = contact;
//                defaultHandler.sendMessage(message);
            }
        }

    }


    /**
     * 设置设备logo图片和名称
     *
     * @param
     */
    private void setDeviceInfoToPage() {
        //每一次重新设置显示logo时，deviceLogo进行背景清空
        deviceLogo.setBackgroundDrawable(null);
        if (deviceInfo != null) {
            if (deviceInfo.getLastUpdateTime() > 0) {
                commandTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(deviceInfo.getLastUpdateTime())));
            }
            if (ControlTypeMenu.wenduji.value().equals(deviceInfo.getControlType())) {
                //设置图片
                if (VersionType.CHANNEL_UCTECH.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
                    try {
                        deviceLogo.setImageBitmap(BitmapFactory.decodeStream(getAssets().open("uctech/uctech_t_" + deviceInfo.getChValue() + ".png")));
                    } catch (IOException e) {
//						Log.e("uctech", "读取图片文件错误");
                    }
                } else {
                    ImageLoader.getInstance().displayImage(dcsp.getString(Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/" + deviceInfo.getLogo(), deviceLogo, options, new ImageLoadingBar());
                }
                command.setText(zhuji.getName() + ":" + deviceInfo.getName() + "CH" + deviceInfo.getChValue() + deviceInfo.getLastCommand());
            } else if (ControlTypeMenu.wenshiduji.value().equals(deviceInfo.getControlType())) {
                if (VersionType.CHANNEL_UCTECH.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
                    try {
                        deviceLogo.setImageBitmap(BitmapFactory.decodeStream(getAssets().open("uctech/uctech_th_" + deviceInfo.getChValue() + ".png")));
                    } catch (IOException e) {
//						Log.e("uctech", "读取图片文件错误");
                    }
                } else {
                    ImageLoader.getInstance().displayImage(dcsp.getString(Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/" + deviceInfo.getLogo(), deviceLogo, options, new ImageLoadingBar());
                }
                command.setText(zhuji.getName() + ":" + deviceInfo.getName() + "CH" + deviceInfo.getChValue() + deviceInfo.getLastCommand());
            } else {
                //设置图片
                ImageLoader.getInstance().displayImage( dcsp.getString(Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/" + deviceInfo.getLogo(), deviceLogo, options, new ImageLoadingBar());
                if (TextUtils.isEmpty(deviceInfo.getWhere())) {
                    command.setText(zhuji.getName() + ":" + deviceInfo.getName() + deviceInfo.getLastCommand());
                } else {
                    command.setText(zhuji.getName() + ":" + deviceInfo.getName() + "(" + deviceInfo.getWhere() + ")" + deviceInfo.getLastCommand());
                }
//                command.setText(zhuji.getName() + ":" + deviceInfo.getName()+"("+deviceInfo.getWhere()+")"+ deviceInfo.getLastCommand());
            }
            if (deviceInfo.getCa().equals(DeviceInfo.CaMenu.menling.value())) {
                tv_notice.setText(getString(R.string.close_notice));
            }
        } else {
            if (zhuji != null) {
                CommandInfo cmmInfo = DatabaseOperator.getInstance().queryLastCommand(zhuji.getId());
                if ((zhuji.getCa().equals(DeviceInfo.CaMenu.menling.value()) || (cmmInfo != null && CommandInfo.SpecialEnum.doorbell.value() == cmmInfo.getSpecial()))) {
                    deviceLogo.setBackgroundColor(getResources().getColor(R.color.main_color));
                    deviceLogo.setImageResource(R.drawable.alarm_doorbelling);
                    ((AnimationDrawable) deviceLogo.getDrawable()).start();
                    command.setText(zhuji.getName() + ":" + cmmInfo.getCommand());
                } else {
                    deviceLogo.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_alert));
                    command.setText(zhuji.getName() + ":" + getString(R.string.activity_alertmessage_jqif) + "  " + (from == null ? "" : from + getString(R.string.activity_alertmessage_help)));
                    if (from == null) {
                        List<CommandInfo> commandInfos = DatabaseOperator.getInstance(mContext).queryAllCommands(zhuji.getId());
                        if (!CollectionsUtils.isEmpty(commandInfos)) {
                            for (CommandInfo c : commandInfos) {
                                if ("0".equals(c.getCtype())) {//主机有特殊报警指令发上来了，需要发送解除主机的鸣叫
                                    command.setText(zhuji.getName() + ":" + c.getCommand());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
