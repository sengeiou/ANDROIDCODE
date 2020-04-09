package com.smartism.znzk.zhicheng.activities;


import android.app.KeyguardManager;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.media.SoundPool;
import android.os.*;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.MainActivity;
import com.smartism.znzk.activity.alert.AlertMessageActivity;
import com.smartism.znzk.activity.camera.ApMonitorActivity;
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
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.Account;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.global.AccountPersist;
import com.smartism.znzk.global.AppConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.util.*;
import com.smartism.znzk.view.DialogView;
import com.smartism.znzk.view.ProgressCycleView;
import com.smartism.znzk.xiongmai.activities.XiongMaiDisplayCameraActivity;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ZCAlertMessageActivity extends ActivityParentActivity implements View.OnClickListener {

    private static final int AUDIO_TIME = 60 * 1000; //播放声音持续时间
    private long deviceid = 0;
    private String from = ""; //手机对手机的触发者
    private ImageView deviceLogo;
    private TextView commandTime, command;
    private DeviceInfo deviceInfo;
    private ZhujiInfo zhuji;
    private PowerManager.WakeLock mWakelock;
    //播放声音的声音池

    private boolean isBind = false;

    ImageView zhicheng_look_camera_btn,zhicheng_ignore_btn;
    Button ignore_no_door,look_no_door,lift_no_door ;
    RelativeLayout zhicheng_no_door;
    RelativeLayout mZhiChengLinearLayout ;
    FrameLayout long_press_layout_parent ;




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

    @Override
    public void onClick(View v) {
        Intent temp = new Intent();
        temp.putExtra("zhuji", zhuji);
        temp.putExtra("device", deviceInfo);
        switch (v.getId()){
            //显示的布局是打电话按钮，说明可能是wifi锁、智能锁、或者门铃-志诚
            case R.id.zhicheng_look_camera_btn:
                //绿色的电话按钮,分为wifi锁、智能锁、普通门铃
                if(judgeDeviceOrZhuji(deviceInfo,zhuji)){
                    //主机，可能是wifi锁或者智能锁
                    if(zhuji.getCa().equals(DeviceInfo.CaMenu.wifizns.value())){
                        //wifi锁
                        temp.setClass(ZCAlertMessageActivity.this, WifiLockMainActivity.class);
                    }else if(zhuji.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())){
                        //智能锁
                        temp.setClass(ZCAlertMessageActivity.this, LockMainActivity.class);
                    }
                }else{
                    //主机下的设备，可能是智能锁、普通门铃
                    if(deviceInfo.getCa()!=null&&deviceInfo.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())){
                        temp.setClass(ZCAlertMessageActivity.this, LockMainActivity.class);
                    }else if(deviceInfo.getCa()!=null&&deviceInfo.getCa().equals(DeviceInfo.CaMenu.menling.value())){
                        CameraInfo cameraInfo  = getDeviceBindCamera(deviceInfo);
                        if(cameraInfo!=null){
                            Contact contact = new Contact();
                            contact.contactId = cameraInfo.getId();
                            temp.putExtra("deviceInfo",deviceInfo);
                            temp.putExtra("contact",contact);
                            temp.setClass(ZCAlertMessageActivity.this,XiongMaiDisplayCameraActivity.class);
                        }else{
                            finish();
                            return ;
                        }
                    }
                }
                startActivity(temp);
                finish();
                return;
            case R.id.zhicheng_ignore_btn:
                //红色的电话按钮,不管是wifi锁还是网关锁还是普通门铃直接挂断
                finish();
                return;
        }

        //到这里来了，说明显示的布局不是电话布局，一定不是门铃
        if(judgeDeviceOrZhuji(deviceInfo,zhuji)){
            //主机
            switch (v.getId()){
                case R.id.ignore_zhicheng_btn:
                    finish();
                    return;
                case R.id.release_zhicheng_btn:
                   if(zhuji.getCa().equals(DeviceInfo.CaMenu.zhuji.value())){
                        SyncMessage message = new SyncMessage();
                        message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                        message.setDeviceid(zhuji.getId());
                        message.setSyncBytes(new byte[]{102});
                        SyncMessageContainer.getInstance().produceSendMessage(message);

                    }
                    finish();
                    return ;
                case R.id.look_zhicheng_btn:
                    //主机
                    if(zhuji.getCa().equals(DeviceInfo.CaMenu.wifizns.value())){
                        //wifi锁
                        temp.setClass(ZCAlertMessageActivity.this,WifiLockMainActivity.class);
                    }else if(zhuji.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())){
                        temp.setClass(ZCAlertMessageActivity.this,LockMainActivity.class);
                    }else if(zhuji.getCa().equals(DeviceInfo.CaMenu.zhuji.value())){
                        //启动应用
                        Intent startPrg = new Intent();
                        startPrg.setAction(Intent.ACTION_MAIN);
                        startPrg.addCategory(Intent.CATEGORY_LAUNCHER);
                        startPrg.setClass(mContext,MainActivity.class);
                        startActivity(startPrg);
                        finish();
                        return ;
                    }
                    startActivity(temp);
                    finish();
                    return ;
            }
        }else{
         //主机下设备
            switch (v.getId()){
                case R.id.ignore_zhicheng_btn:
                    finish();
                    return ;
                case R.id.release_zhicheng_btn:
                    if (!deviceInfo.isFa()) {//当设备不是24小时防区的则撤防，否则只需要让主机不要叫了即可
                        mContext.showInProgress(getString(R.string.loading), false, false);
                        JavaThreadPool.getInstance().excute(new ZCAlertMessageActivity.TriggerScene(0));
                    }else{
                        SyncMessage message = new SyncMessage();
                        message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                        message.setDeviceid(zhuji.getId());
                        message.setSyncBytes(new byte[]{102});
                        SyncMessageContainer.getInstance().produceSendMessage(message);
                        finish();
                    }
/*                    if(deviceInfo.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())){
                        finish();
                    }else{
                        if (!deviceInfo.isFa()) {//当设备不是24小时防区的则撤防，否则只需要让主机不要叫了即可
                            mContext.showInProgress(getString(R.string.loading), false, false);
                            JavaThreadPool.getInstance().excute(new ZCAlertMessageActivity.TriggerScene(0));
                        }else{
                            SyncMessage message = new SyncMessage();
                            message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                            message.setDeviceid(zhuji.getId());
                            message.setSyncBytes(new byte[]{102});
                            SyncMessageContainer.getInstance().produceSendMessage(message);
                            finish();
                        }
                    }*/
                    return ;
                case R.id.look_zhicheng_btn:
                    if(deviceInfo.getCa().equals(DeviceInfo.CaMenu.zhinengsuo.value())){
                        temp.setClass(ZCAlertMessageActivity.this,LockMainActivity.class);
                    }else{
                        CameraInfo cameraInfo  = getDeviceBindCamera(deviceInfo);
                        if(cameraInfo!=null){
                            Contact contact = new Contact();
                            contact.contactId = cameraInfo.getId();
                            temp.putExtra("deviceInfo",deviceInfo);
                            temp.putExtra("contact",contact);
                            temp.setClass(ZCAlertMessageActivity.this,XiongMaiDisplayCameraActivity.class);
                        }else{
                            finish();
                            return;
                        }
                    }
                    startActivity(temp);
                    finish();
                    return ;
            }
        }
    }

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
                    .requestoOkHttpPost(
                             server + "/jdm/s3/scenes/trigger", o, mContext);
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

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1: //加载设备数据完成
                    setDeviceInfoToPage();
                    if (tm.getCallState() != TelephonyManager.CALL_STATE_RINGING) {
                        intent = new Intent(getApplicationContext(), AudioTipsService.class);
                        intent.putExtra("devId", deviceid + "");
                        isBind = bindService(intent, conn, Context.BIND_AUTO_CREATE);
                        defaultHandler.sendEmptyMessageDelayed(3, AUDIO_TIME);
                    }
                    //响铃时间1分钟
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
                    //响铃时间1分钟
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /*MainApplication.app.setAlertMessageActivity(this);*/
        deviceid = getIntent().getLongExtra("deviceid", 0);//此ID可能为主机ID 也可能为设备ID
        from = getIntent().getStringExtra("from");//panic触发者
        setContentView(R.layout.activity_alertmessage);
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(new ZCAlertMessageActivity.MyPhoneCallListener(), PhoneStateListener.LISTEN_CALL_STATE);
        deviceInfos = new ArrayList<>();
        //判断屏幕是否锁屏
        KeyguardManager km =
                (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        if (km.inKeyguardRestrictedInputMode()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        } else {
        }
        initViews();
        getZhujiCamera();//获取作为主机的雄迈摄像头
        setDeviceInfoAndZhuji(deviceid,1);
    }

    void initViews(){
        long_press_layout_parent  = findViewById(R.id.long_press_layout_parent);
        deviceLogo = (ImageView) findViewById(R.id.am_devicelogo);
        commandTime = (TextView) findViewById(R.id.am_commandtime);
        command = (TextView) findViewById(R.id.am_command);
        mZhiChengLinearLayout = findViewById(R.id.zhicheng_parent);
        zhicheng_no_door = findViewById(R.id.zhicheng_no_door);
        ignore_no_door = findViewById(R.id.ignore_zhicheng_btn);
        look_no_door = findViewById(R.id.look_zhicheng_btn);
        lift_no_door = findViewById(R.id.release_zhicheng_btn);
        View tempView = findViewById(R.id.center_view);
        zhicheng_ignore_btn  = findViewById(R.id.zhicheng_ignore_btn);
        zhicheng_look_camera_btn = findViewById(R.id.zhicheng_look_camera_btn);
        ViewGroup.LayoutParams lp = tempView.getLayoutParams();
        lp.width = getResources().getDisplayMetrics().widthPixels/3;
        tempView.setLayoutParams(lp);
        look_no_door.setOnClickListener(this);
        lift_no_door.setOnClickListener(this);
        ignore_no_door.setOnClickListener(this);
        zhicheng_look_camera_btn.setOnClickListener(this);
        zhicheng_ignore_btn.setOnClickListener(this);

        long_press_layout_parent.setVisibility(View.GONE);
    }



    @Override
    protected void onNewIntent(Intent intent) {//再次调用此页面触发
        super.onNewIntent(intent);
        deviceid = intent.getLongExtra("deviceid", 0);//此ID可能为主机ID 也可能为设备ID
        from = intent.getStringExtra("from");//panic触发者
        setDeviceInfoAndZhuji(deviceid,4);
    }

    //设置deviceInfo和zhuji对象
    void setDeviceInfoAndZhuji(long deviceId,int what){
        deviceInfo = DatabaseOperator.getInstance(ZCAlertMessageActivity.this).queryDeviceInfo(deviceId);
        if (deviceInfo != null) {
            zhuji = DatabaseOperator.getInstance(ZCAlertMessageActivity.this).queryDeviceZhuJiInfo(deviceInfo.getZj_id());
        } else {
            zhuji = DatabaseOperator.getInstance(ZCAlertMessageActivity.this).queryDeviceZhuJiInfo(deviceId);
        }

        if(deviceInfo==null){
            deviceInfo = Util.getZhujiDevice(zhuji);
        }
        //布局的显示
        if(judgeDeviceOrZhuji(deviceInfo,zhuji)){
            CommandInfo commandInfo = DatabaseOperator.getInstance().queryLastCommand(zhuji.getId());
            //表示是主机
            if(commandInfo!=null&&(CommandInfo.SpecialEnum.doorbell.value() ==commandInfo .getSpecial()||
                    zhuji.getCa().equals(DeviceInfo.CaMenu.menling.value()))){
                mZhiChengLinearLayout.setVisibility(View.VISIBLE);
                zhicheng_no_door.setVisibility(View.GONE);
            }else{
                mZhiChengLinearLayout.setVisibility(View.GONE);
                zhicheng_no_door.setVisibility(View.VISIBLE);
            }
        }else{
            //主机下的设备
            if(deviceInfo.getCa().equals(DeviceInfo.CaMenu.menling.value())
                    ||CommandInfo.SpecialEnum.doorbell.value() == DatabaseOperator.getInstance().queryLastCommand(deviceInfo.getId()).getSpecial()){
                mZhiChengLinearLayout.setVisibility(View.VISIBLE);
                zhicheng_no_door.setVisibility(View.GONE);
            }else{
                mZhiChengLinearLayout.setVisibility(View.GONE);
                zhicheng_no_door.setVisibility(View.VISIBLE);
            }
        }

        Message m = defaultHandler.obtainMessage(what);
        defaultHandler.sendMessage(m);
    }

    List<CameraInfo> zhujiCameraInfo = new ArrayList<>() ; //保存作为主机的摄像头
    //获取作为主机的雄迈摄像头
    void getZhujiCamera(){
        zhujiCameraInfo.clear();
        List<ZhujiInfo> infos = DatabaseOperator.getInstance(mContext).queryAllZhuJiInfos();
        for(ZhujiInfo zhujiInfo :infos){
            if(zhujiInfo.getCameraInfo().getC()==null){
                continue;
            }
            if(zhujiInfo.getCa().equals(DeviceInfo.CaMenu.ipcamera.value())
                    &&(zhujiInfo.getCameraInfo().getC().equals(CameraInfo.CEnum.xiongmai.value()))){
                if(!zhujiCameraInfo.contains(zhujiInfo.getCameraInfo())){
                    zhujiCameraInfo.add(zhujiInfo.getCameraInfo());
                }
            }
        }
    }

    //获取设备下绑定的雄迈摄像头
    CameraInfo getDeviceBindCamera(DeviceInfo deviceInfo){
        for (CameraInfo cs : zhujiCameraInfo) {
            if (cs.getIpcid() == Long.parseLong(deviceInfo.getBipc())) {
                return cs;
            }
        }
        return null;
    }

    //判断是主机还是设备
    boolean judgeDeviceOrZhuji(DeviceInfo deviceInfo , ZhujiInfo zhujiInfo){
            if(deviceInfo==null||zhujiInfo==null){
                throw new NullPointerException("参数不可以为空");
            }else{
                if(deviceInfo.getId()==zhujiInfo.getId()){
                    //表示是主机
                    return true ;
                }
                return false ;
            }
    }

    @Override
    protected void onResume() {
        boolean showClock = dcsp.getBoolean(DataCenterSharedPreferences.Constant.IS_LOOKS, false);
        dcsp.putBoolean(DataCenterSharedPreferences.Constant.IS_LOOKS, false).commit();
        super.onResume();
        dcsp.putBoolean(DataCenterSharedPreferences.Constant.IS_LOOKS, showClock).commit();
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



    /**
     * 设置设备logo图片和名称
     *
     * @param
     */
    private void setDeviceInfoToPage() {
        //每一次重新设置显示logo时，deviceLogo进行背景清空
        deviceLogo.setBackgroundDrawable(null);
        if (!judgeDeviceOrZhuji(deviceInfo,zhuji)) {
            if (deviceInfo.getLastUpdateTime() > 0) {
                commandTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(deviceInfo.getLastUpdateTime())));
            }
            if (DeviceInfo.ControlTypeMenu.wenduji.value().equals(deviceInfo.getControlType())) {
                //设置图片
                if (Actions.VersionType.CHANNEL_UCTECH.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
                    try {
                        deviceLogo.setImageBitmap(BitmapFactory.decodeStream(getAssets().open("uctech/uctech_t_" + deviceInfo.getChValue() + ".png")));
                    } catch (IOException e) {
//						Log.e("uctech", "读取图片文件错误");
                    }
                } else {
                    ImageLoader.getInstance().displayImage( dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/" + deviceInfo.getLogo(), deviceLogo, options, new ImageLoadingBar());
                }
                command.setText(zhuji.getName() + ":" + deviceInfo.getName()+"CH" + deviceInfo.getChValue() + deviceInfo.getLastCommand());
            } else if (DeviceInfo.ControlTypeMenu.wenshiduji.value().equals(deviceInfo.getControlType())) {
                if (Actions.VersionType.CHANNEL_UCTECH.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
                    try {
                        deviceLogo.setImageBitmap(BitmapFactory.decodeStream(getAssets().open("uctech/uctech_th_" + deviceInfo.getChValue() + ".png")));
                    } catch (IOException e) {
//						Log.e("uctech", "读取图片文件错误");
                    }
                } else {
                    ImageLoader.getInstance().displayImage( dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/" + deviceInfo.getLogo(), deviceLogo, options, new ImageLoadingBar());
                }
                command.setText(zhuji.getName() + ":" + deviceInfo.getName()+ "CH" + deviceInfo.getChValue() + deviceInfo.getLastCommand());
            } else {
                //设置图片
                ImageLoader.getInstance().displayImage( dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/" + deviceInfo.getLogo(), deviceLogo, options, new ImageLoadingBar());
                if(TextUtils.isEmpty(deviceInfo.getWhere())){
                    command.setText(zhuji.getName() + ":" + deviceInfo.getName()+ deviceInfo.getLastCommand());
                }else{
                    command.setText(zhuji.getName() + ":" + deviceInfo.getName()+"("+deviceInfo.getWhere()+")"+ deviceInfo.getLastCommand());
                }
//                command.setText(zhuji.getName() + ":" + deviceInfo.getName()+"("+deviceInfo.getWhere()+")"+ deviceInfo.getLastCommand());
            }

        } else {
            if (zhuji != null) {
                CommandInfo cmmInfo= DatabaseOperator.getInstance().queryLastCommand(zhuji.getId());
                if((zhuji.getCa().equals(DeviceInfo.CaMenu.menling.value())|| (cmmInfo != null && CommandInfo.SpecialEnum.doorbell.value() == cmmInfo.getSpecial()))){
                    deviceLogo.setBackgroundColor(getResources().getColor(R.color.main_color));
                    deviceLogo.setImageResource(R.drawable.alarm_doorbelling);
                    ((AnimationDrawable) deviceLogo.getDrawable()).start();
                    command.setText(zhuji.getName() + ":" + cmmInfo.getCommand());
                }else {
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
