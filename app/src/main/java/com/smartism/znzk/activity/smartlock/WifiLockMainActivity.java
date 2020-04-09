package com.smartism.znzk.activity.smartlock;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.camera.PlayBaseActivity;
import com.smartism.znzk.activity.device.PerminssonTransActivity;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.HistoryCommandInfo;
import com.smartism.znzk.domain.SmartLockInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.xiongmai.fragment.XMFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.smartism.znzk.util.Actions.ACCETP_ONEDEVICE_MESSAGE;


/**
 * 2018年08月03日
 * create by 王建
 * 深圳市智慧猫软件技术有限公司 版权所有
 *
 */

public class WifiLockMainActivity extends PlayBaseActivity implements View.OnClickListener, OnItemClickListener {

    private final String  TAG = WifiLockMainActivity.class.getSimpleName();
    private LinearLayout ll_sbxx, ll_ks, ll_lsmm, ll_his,ll_number,ll_userhead;
    private TextView tv_status;
    private ImageView iv_dy_status, iv_share;
    private ListView lv_user,commandListView; //用户，历史记录集合
    private LockAdapter adapter;//用户记录adapter
    private List<SmartLockInfo> lockInfos;//用户记录集合
    private CommandAdapter commandAdapter;//历史记录adapter
    private List<HistoryCommandInfo> historyCommandInfos;//历史记录集合
    private View footerView;
    private Button footerView_button;
    private Context mContext;
    private SmartLockInfo lockInfo;
    private int itemPosition = -1;
    private JJLockMenuPopupWindow popupWindow;
    private LinearLayout linearlayout;
    private int totalSize = 0;

    private AlertView mAlertViewExt;
    private InputMethodManager imm;
    private EditText etName;
    private ZhujiInfo zhuji;
    private String lockId;
    private int inputType;
    private boolean isSend;
    private CheckBox cbLaws;
    private TextView tv_title;

    private AlertView permissonView;

    /******页面功能动态配置项******/
    private boolean config_show_numberList = false; //是否显示用户列表
    private boolean config_show_history = true;//是否显示历史记录


    //摄像头部分
    CameraInfo mCameraInfo = null;//当前wifi锁绑定的摄像头对象
    final int GETBIPC_MESSAGE = 0x89;
    DeviceInfo operationDevice = null ;
    boolean isNormal  =true ;
    FrameLayout mXMFrameLayout  ;
    boolean isCameraShow = false ;//记录是否有摄像头要显示
    LinearLayout rl_op_bottom ; //底部说话布局
    ImageView iv_vioce ; //静音按钮
    ImageView wifi_lock_iv_screenshot  ;//视频截图按钮
    ImageView wifi_lock_iv_speak ; //语音对讲按钮
    ImageView other_ll_ks ;//底部开锁按钮

    int type =-1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wifisuo);
        if(savedInstanceState!=null){
            isNormal = false;
        }
        mContext = this;

        operationDevice = (DeviceInfo) getIntent().getSerializableExtra("device");
        zhuji = (ZhujiInfo) getIntent().getSerializableExtra("zhuji");
        String bIpc = null ;
        if(operationDevice!=null){
            bIpc = operationDevice.getBipc() ;
        }else{
            bIpc = String.valueOf(zhuji.getBipc());
        }

     /*   //查询设备添加过的摄像头
       if (deviceInfo != null && deviceInfo.getBipc() != null && !"0".equals(deviceInfo.getBipc())) {
            JavaThreadPool.getInstance().excute(new BindingCameraLoad(deviceInfo.getBipc()));
        }*/
        initView();
        initData();
        initEvent();
        sendCommandIfNeed();//发送开锁指令，如果需要

        //摄像头部分
        //查找摄像头是否存在
        commandListView.setVisibility(View.INVISIBLE);//隐藏历史记录列表
        //从后台获取绑定的摄像头信息
       Log.v(getClass().getSimpleName(),bIpc);
        if(bIpc!=null&&!bIpc.equals("0")){
            //录音权限申请,又绑定摄像头进行权限申请
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},0x78);
            }
            //获取摄像头id,获取到id后，再来决定查看当前设备是否添加了当前摄像头
            mHandler.sendEmptyMessageDelayed(10,10*1000);//10秒后取消进度条
            showInProgress(getString(R.string.loading), false, true);
            JavaThreadPool.getInstance().excute(new BindingCameraLoad(bIpc));
        }
    }


    CommandInfo mCommandInfo = null; //保存wifi锁的密码
    //检查wifi锁是设置了密码
    private boolean checkWifiZNSHasPwd(){
        List<CommandInfo> commandInfos = DatabaseOperator.getInstance().queryAllCommands(zhuji.getId());
        for(CommandInfo commandInfo :commandInfos){
            if(commandInfo.getCtype().equals("pwd_control")){
                mCommandInfo = commandInfo ;
                return true ;
            }
        }
        return false ;
    }

    //从应用的SQLite数据库中去获取绑定摄像头信息,前提要求用户没有删除相应摄像头才可以播放,由于雄迈目前添加时只作为主机，因此只管主机是列表下的摄像头
    private void findBindCameraFromLocal(CameraInfo cameraInfo){
        List<ZhujiInfo> infos = DatabaseOperator.getInstance(this).queryAllZhuJiInfos();
        for(ZhujiInfo zhujiInfo :infos){
            if(zhujiInfo.getCa().equals(DeviceInfo.CaMenu.ipcamera.value())){
                if(zhujiInfo.getCameraInfo().getIpcid()==cameraInfo.getIpcid()){
                    isCameraShow  = true ;
                    mCameraInfo = zhujiInfo.getCameraInfo();
                }
            }
        }
    }


    /**
     * 发送指令需要打开手势密码，而打开手势密码是通过activityResult来传递已经验证过手势密码的，而带了摄像头播放页面需要在不可见时finish，
     * 重写了父类的finish方法，使本页面能够被父类重启和执行未执行的操作。
     */
    private void sendCommandIfNeed(){
        if (getIntent().getIntExtra("requestCode",0) == 3 && getIntent().getIntExtra("resultCode",0) == RESULT_OK) {
            Toast.makeText(this, getString(R.string.jujiangsuo_init_optioning), Toast.LENGTH_LONG).show();
            initLockStatus();
        }else if (getIntent().getIntExtra("requestCode",0) == 2 && getIntent().getIntExtra("resultCode",0) == RESULT_OK) {
//            showInProgress(getString(R.string.operationing), false, true);
            Toast.makeText(this, getString(R.string.jujiangsuo_ks_optioning), Toast.LENGTH_LONG).show();
            SyncMessage message = new SyncMessage();
            message.setCommand(SyncMessage.CommandMenu.rq_control.value());
            message.setDeviceid(zhuji.getId());
            message.setSyncBytes(new byte[]{0x02});
            SyncMessageContainer.getInstance().produceSendMessage(message);
            mHandler.sendEmptyMessageDelayed(11, 15 * 1000);
        }
    }
    private void initEvent() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACCETP_ONEDEVICE_MESSAGE);
        filter.addAction(Actions.REFRESH_DEVICES_LIST);
        filter.addAction(Actions.CONNECTION_FAILED_SENDFAILED);//控制指令发送失败发送该广播
        filter.addAction(Actions.CONTROL_BACK_MESSAGE);//控制指令授权码发送该广播
        registerReceiver(receiver, filter);
        lv_user.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, View view, final int position, long id) {
//                if (position == 0) {
//                    return false;
//                }
//                    parent.getChildAt(position).setBackgroundResource(R.color.oldlace);
//                if (zhuji.isAdmin()) {
                    itemPosition = position;
                    popupWindow.updateDeviceMenu(mContext);
                    popupWindow.showAtLocation(linearlayout, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
//                } else {
//                    Toast.makeText(mContext, getString(R.string.net_error_nopermission), Toast.LENGTH_SHORT).show();
//                }
//                    popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//                        @Override
//                        public void onDismiss() {
//                            parent.getChildAt(position).setBackgroundResource(R.color.white);
//                        }
//                    });
//                changeBackGround(0.7f);
                return true;
            }
        });
        //用户点击事件 现在没有信息
//        if (!isFamalily && zhuji.isAdmin()) {
//            lv_user.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    Intent deviceIntent = new Intent();
//                    deviceIntent.setClass(mContext.getApplicationContext(), JJLockPerssionActivity.class);
//                    deviceIntent.putExtra("device", deviceInfo);
//                    deviceIntent.putExtra("lockinfo", lockInfos.get(position));
//                    startActivity(deviceIntent);
//                }
//            });
//        }
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (Actions.ACCETP_ONEDEVICE_MESSAGE.equals(intent.getAction())) {
                String deviceId = intent.getStringExtra("zhuji_id");
                if (deviceId != null && deviceId.equals(String.valueOf(zhuji.getId()))) {
                    //更新主机状态
                    zhuji = DatabaseOperator.getInstance().queryDeviceZhuJiInfo(zhuji.getId());
                    String data = (String) intent.getSerializableExtra("zhuji_info");
                    if (data != null) {
                        JSONObject object1 = JSONObject.parseObject(data);
                        if (object1 != null && CommandInfo.CommandTypeEnum.requestAddUser.value().equals(object1.getString("dt"))
                                && !TextUtils.isEmpty(object1.getString("deviceCommand")) && !"0".equals(object1.getString("deviceCommand"))) {
//                            if (zhuji.isAdmin()) {
                                showWIFILockRequestUser();
//                            }
                        }else if(object1.getString("dt").equals("pwd_control")){
                            Log.d(TAG,object1.toJSONString());
                        }else if (object1.containsKey("sort") && object1.getString("sort").equals("2")) { //2 键 开锁键
                            if (mHandler.hasMessages(11)){
                                mHandler.removeMessages(11);
                                cancelInProgress();
                                Toast.makeText(WifiLockMainActivity.this, getString(R.string.jjsuo_ks_success), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }else if (Actions.REFRESH_DEVICES_LIST.equals(intent.getAction())) {
                //刷新列表

            }else if(Actions.CONTROL_BACK_MESSAGE.equals(intent.getAction())){
                mHandler.removeMessages(11);
                cancelInProgress();
                final int code = intent.getIntExtra("code",-1);
                if(code==SyncMessage.CodeMenu.rp_control_needconfirm.value()){
                    final String keyStr = intent.getStringExtra("data_info");
                    //发送了控制指令授权码，显示再一次确认提示框
                    mTimer = new Timer();
                    View view = getLayoutInflater().inflate(R.layout.unlock_notice_layout,mXMFrameLayout,false);
                    mConfirmAlert = new AlertDialog.Builder(mContext).setView(view).setCancelable(false)
                            .create();
                    TextView title = view.findViewById(R.id.pwd_title_tv);
                    TextView content = view.findViewById(R.id.pwd_content_tv);
                    title.setVisibility(View.VISIBLE);
                    title.setText(getString(R.string.unlock_notice_layout_title));
                    content.setText(getString(R.string.unlock_notice_layout_message));
                    TextView confirm_btn = view.findViewById(R.id.pwd_confirm_btn);
                    TextView cancel_btn = view.findViewById(R.id.pwd_cancel_btn);

                    confirm_btn.setText(getString(R.string.unlock_notice_ensure));
                    cancel_btn.setText(getString(R.string.permission_cancel));
                    cancel_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mTimer.cancel();
                            //没必要判断是否显示
                            if(mConfirmAlert.isShowing()){
                                mConfirmAlert.dismiss();
                            }
                            ToastTools.short_Toast(mContext,getString(R.string.oay_result_cancel));
                        }
                    });
                    confirm_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mConfirmAlert.dismiss();
                            mTimer.cancel();
                            //回应开锁验证码
                            showInProgress(getString(R.string.ongoing));
                            SyncMessage message = new SyncMessage();
                            message.setCommand(SyncMessage.CommandMenu.rq_controlConfirm.value());
                            message.setDeviceid(zhuji.getId());
                            message.setSyncBytes(keyStr.getBytes());
                            SyncMessageContainer.getInstance().produceSendMessage(message);
                            mHandler.sendEmptyMessageDelayed(11, 15000);//15秒超时
                        }
                    });
                    CustomTask task = new CustomTask(confirm_btn);
                    mTimer.schedule(task,0,1000);
                    mConfirmAlert.show();
                }
            }else if(Actions.CONNECTION_FAILED_SENDFAILED.equals(intent.getAction())){
                //控制指令发送失败
                mHandler.removeMessages(11);
                cancelInProgress();
                Toast toast = Toast.makeText(mContext,null,Toast.LENGTH_SHORT);
                toast.setText(getString(R.string.EE_AS_PHONE_CODE4));
                toast.show();
            }
        }
    };

    private class AuthorseKey implements Runnable {
        int author = 0;
        long deviceId;
        String key = "";

        public AuthorseKey(int author, long deviceId,String key) {
            this.author = author;
            this.deviceId = deviceId;
            this.key = key;
        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", deviceId);
            JSONArray array = new JSONArray();
            JSONObject o = new JSONObject();
            o.put("vkey", key);
            o.put("value", author);
            array.add(o);
            object.put("vkeys", array);
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/p/set", object, WifiLockMainActivity.this);
            if (result != null && result.equals("0")) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.success),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.operator_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public void updateHeaderImage() {
        setIvHeader(R.drawable.jjiang_suo_banner);
    }

    private void initData() {
        lockInfo = new SmartLockInfo();
        tv_title.setText(!TextUtils.isEmpty(zhuji.getName()) ? zhuji.getName() : getString(R.string.jjsuo_suo_name));
        iv_dy_status.setImageResource(zhuji.getBatteryStatus() == 0 ? R.drawable.jjiang_suo_dyzc : R.drawable.jjiang_suo_dydd);
        tv_status.setText(zhuji.getBatteryStatus() == 0 ? getString(R.string.jjsuo_dy_normal) : getString(R.string.jjsuo_dy_unormal));
    }

    @Override
    protected void onResume() {
        super.onResume();
        initUserOrHistoryInfos();
    }

    private void initUserOrHistoryInfos() {
        tv_title.setText(!TextUtils.isEmpty(zhuji.getName()) ? zhuji.getName() : getString(R.string.jjsuo_suo_name));
        if (config_show_numberList) {
            JavaThreadPool.getInstance().excute(new UsersLoad(0, 100));
        }
        if (config_show_history) {
            historyCommandInfos.clear();
            JavaThreadPool.getInstance().excute(new CommandLoad(0, 20));
        }
        checkRequestUser();
    }

    private void checkRequestUser(){
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                List<CommandInfo> commandInfos = DatabaseOperator.getInstance().queryAllCommands(zhuji.getId());
                if (!CollectionsUtils.isEmpty(commandInfos)){
                    for (CommandInfo commandInfo: commandInfos) {
                        if (CommandInfo.CommandTypeEnum.requestAddUser.value().equals(commandInfo.getCtype()) && !"0".equals(commandInfo.getCommand()) && commandInfo.getCtime() + 60000 > System.currentTimeMillis()){
                            showWIFILockRequestUser();
                            break;
                        }
                    }
                }
            }
        });
    }

    private void showWIFILockRequestUser(){
        try {
            if (permissonView == null || !permissonView.isShowing()) {
                permissonView = new AlertView(getString(R.string.activity_beijingsuo_reqkeyauthtitle), getString(R.string.jjsuo_request_adduser),
                        null, new String[]{getString(R.string.activity_beijingsuo_reqkeyauth), getString(R.string.activity_beijingsuo_notauth)
                        , getString(R.string.cancel)}, null,
                        mContext, AlertView.Style.Alert,
                        new com.smartism.znzk.view.alertview.OnItemClickListener() {

                            @Override
                            public void onItemClick(Object o, int position) {
                                if (position == 0) {
                                    showInProgress(mContext.getString(R.string.operationing), false, true);
                                    //开始授权
                                    JavaThreadPool.getInstance().excute(new AuthorseKey(100, zhuji.getId(),CommandInfo.CommandTypeEnum.requestAddUser.value()));
                                } else if (position == 1) {
                                    //取消授权
                                    showInProgress(mContext.getString(R.string.operationing), false, true);
                                    JavaThreadPool.getInstance().excute(new AuthorseKey(0, zhuji.getId(),CommandInfo.CommandTypeEnum.requestAddUser.value()));
                                }
                            }
                        });
                permissonView.show();
            }
        } catch (Exception ex) {
            //防止key不是json崩溃
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    public class JJLockMenuPopupWindow extends PopupWindow {

        private View mMenuView;
        private Button btn_deldevice, btn_setdevice, btn_setpassword, btn_setpause,btn_update;

        public JJLockMenuPopupWindow(Context context, View.OnClickListener itemsOnClick) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mMenuView = inflater.inflate(R.layout.suo_user_item_menu, null);
            btn_deldevice = (Button) mMenuView.findViewById(R.id.btn_deldevice);
            btn_setdevice = (Button) mMenuView.findViewById(R.id.btn_setdevice);
            btn_setpassword = (Button) mMenuView.findViewById(R.id.btn_setpassword);
            btn_setpause = (Button) mMenuView.findViewById(R.id.btn_setpause);
            btn_update = (Button) mMenuView.findViewById(R.id.btn_update);

            btn_deldevice.setOnClickListener(itemsOnClick);
            btn_setdevice.setOnClickListener(itemsOnClick);
            btn_setpassword.setOnClickListener(itemsOnClick);
            btn_setpause.setOnClickListener(itemsOnClick);
            btn_update.setOnClickListener(itemsOnClick);
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
//            this.setOnDismissListener(new OnDismissListener() {
//                @Override
//                public void onDismiss() {
//                    changeBackGround(1.0f);
//                }
//            });

        }


        public void updateDeviceMenu(Context context) {
            btn_setdevice.setText(context.getResources().getString(R.string.jjsuo_cancle_pass));
            btn_deldevice.setText(context.getResources().getString(R.string.zss_item_del));
        }

    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch(msg.what){
                case 1:
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    lockInfos.clear();
                    List<SmartLockInfo> list;
                    JSONArray array;
                    cancelInProgress();
                    array = (JSONArray) msg.obj;
                    if (array == null || array.size() == 0) {
//                        Toast.makeText(mContext, "无数据", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    Log.e(TAG, array.toJSONString());
                    list = JSON.parseArray(array.toJSONString(), SmartLockInfo.class);
                    lockInfos.add(new SmartLockInfo());
                    lockInfos.addAll(list);
//                    Log.e("user_lock", lockInfos.toString());
//                    myAdapter = new MedicineAdapter(beanList,mContext);
                    adapter.notifyDataSetChanged();

                    break;
                case 2:
                    cancelInProgress();
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    JSONArray arr = (JSONArray) msg.obj;
                    for (Object o : arr) {
                        JSONObject ob = (JSONObject) o;
                        if (ob.containsKey("id_jj_zns")) {
                            lockId = ob.getString("id_jj_zns");
                        }
                    }
                    break;
                case 10:
                    cancelInProgress();
                    Toast.makeText(mContext, getString(R.string.time_out), Toast.LENGTH_SHORT).show();
                    break;
                case 11:
                    cancelInProgress();
                    Toast.makeText(mContext, getString(R.string.jjsuo_ks_timeout), Toast.LENGTH_SHORT).show();
                    break;
                case 12:
                    cancelInProgress();
                    List<JSONObject> commandList = (List<JSONObject>) msg.obj;
                    for (int i = 0; i < commandList.size(); i++) {
                        HistoryCommandInfo info = new HistoryCommandInfo();
                        JSONObject object1 = commandList.get(i);
                        String temp = object1.getString("send");
                        //device信息的历史记录
                        if(temp.contains("device")){
                            temp = "";
                        }
                        info.setOpreator(temp);
                        info.setCommand(object1.getString("deviceCommand"));
                        String parms1 = "yyyy:MM:dd:HH:mm:ss";
                        Date date = object1.getDate("deviceCommandTime");
                        String hour = new SimpleDateFormat(parms1).format(date.getTime());
                        info.setDate(hour);
                        info.setDayOfWeek(getWeek(date));
                        historyCommandInfos.add(info);
                    }
                    commandAdapter.notifyDataSetChanged();
                    if (totalSize == historyCommandInfos.size()) {
                        commandListView.removeFooterView(footerView);
                    }
                    break;
                case GETBIPC_MESSAGE:
                    //向服务器查询摄像头信息,结果
                    cancelInProgress();
                    mHandler.removeMessages(10);

                    //显示摄像头布局
                    if(mCameraInfo!=null){
                        //需要显示摄像头
                        if(mCameraInfo.getC().equals(CameraInfo.CEnum.xiongmai.value())){
                            //雄迈
                            if(isNormal){
                                mXMFrameLayout.setVisibility(View.VISIBLE);
                                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                                XMFragment xmFragment = XMFragment.newInstance(mCameraInfo);
                                fragmentTransaction.add(R.id.wifi_xiongmai_parant,xmFragment);
                                fragmentTransaction.commitAllowingStateLoss();
                                initXiongMaiEvent(xmFragment);//初始化雄迈相关按钮事件处理
                            }
                        }else if(mCameraInfo.getC().equals(CameraInfo.CEnum.jiwei.value())){
                            //技威望
                            ToastTools.short_Toast(getApplicationContext(),"暂不支持该类摄像头,敬请期待");
                        }
                    }
                    break;
            }
            return true;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    public String getWeek(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        String week = sdf.format(date);
        return week;
    }

    private void initLockStatus() {
//        mHandler.sendEmptyMessageDelayed(10, 15 * 1000);
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject object = new JSONObject();
                object.put("did", zhuji.getId());
                String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/dln/init", object, WifiLockMainActivity.this);

                if ("0".equals(result)) {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
//                            cancelInProgress();
                            Toast.makeText(WifiLockMainActivity.this, getString(R.string.jujiangsuo_init_success),
                                    Toast.LENGTH_LONG).show();
                            initUserOrHistoryInfos();
                        }
                    });
                } else if ("-3".equals(result)) {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(WifiLockMainActivity.this, getString(R.string.history_response_nodevice),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-4".equals(result)) {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(WifiLockMainActivity.this, getString(R.string.operator_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-5".equals(result)) {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(WifiLockMainActivity.this, getString(R.string.net_error_operationfailed),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(WifiLockMainActivity.this, getString(R.string.jujiangsuo_init_error),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    class CommandAdapter extends BaseAdapter {
        LayoutInflater layoutInflater;

        public CommandAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return historyCommandInfos.size();
        }

        @Override
        public Object getItem(int arg0) {
            return historyCommandInfos.get(arg0);
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

            CommandAdapter.DeviceInfoView viewCache = new CommandAdapter.DeviceInfoView();
            if (view == null) {
//                view = layoutInflater.inflate(R.layout.activity_device_command_history_list_item, null);
                view = layoutInflater.inflate(R.layout.activity_zhzj_device_command_item, null);

                viewCache.tv_month = (TextView) view.findViewById(R.id.tv_month);
                viewCache.tv_day = (TextView) view.findViewById(R.id.tv_day);
                viewCache.tv_xingqi = (TextView) view.findViewById(R.id.tv_xingqi);
                viewCache.tv_time = (TextView) view.findViewById(R.id.tv_time);
                viewCache.tv_oper = (TextView) view.findViewById(R.id.tv_oper);
                viewCache.iv_circle_hover = (ImageView) view.findViewById(R.id.iv_circle_hover);
                viewCache.iv_circle = (ImageView) view.findViewById(R.id.iv_circle);
                viewCache.gray_line = (View) view.findViewById(R.id.gray_line);
//                viewCache.gray_line_top = (View) view.findViewById(R.id.gray_line_top);
                viewCache.tv_command = (TextView) view.findViewById(R.id.tv_command);
                view.setTag(viewCache);

            } else {
                viewCache = (CommandAdapter.DeviceInfoView) view.getTag();
            }

            HistoryCommandInfo commandInfo = historyCommandInfos.get(i);

            String[] array = commandInfo.getDate().split(":");
            if (i != 0) {
                String[] array1 = historyCommandInfos.get(i - 1).getDate().split(":");
                if (!array1[2].equals(array[2])) {
                    viewCache.tv_day.setVisibility(View.VISIBLE);
                    viewCache.tv_month.setVisibility(View.VISIBLE);
                    viewCache.tv_xingqi.setVisibility(View.VISIBLE);
                    viewCache.iv_circle_hover.setVisibility(View.VISIBLE);
                    viewCache.iv_circle.setVisibility(View.GONE);
                    viewCache.tv_time.setTextColor(getResources().getColor(R.color.zhzj_default));
                    viewCache.tv_oper.setTextColor(getResources().getColor(R.color.zhzj_default));
                    viewCache.tv_command.setTextColor(getResources().getColor(R.color.zhzj_default));
                } else {
                    viewCache.iv_circle_hover.setVisibility(View.GONE);
                    viewCache.iv_circle.setVisibility(View.VISIBLE);
                    viewCache.tv_day.setVisibility(View.GONE);
                    viewCache.tv_month.setVisibility(View.GONE);
                    viewCache.tv_xingqi.setVisibility(View.GONE);
                    viewCache.tv_time.setTextColor(getResources().getColor(R.color.black));
                    viewCache.tv_oper.setTextColor(getResources().getColor(R.color.black));
                    viewCache.tv_command.setTextColor(getResources().getColor(R.color.black));
                }
            } else {
                viewCache.tv_day.setVisibility(View.VISIBLE);
                viewCache.tv_month.setVisibility(View.VISIBLE);
                viewCache.tv_xingqi.setVisibility(View.VISIBLE);
                viewCache.iv_circle_hover.setVisibility(View.VISIBLE);
                viewCache.iv_circle.setVisibility(View.GONE);
                viewCache.tv_time.setTextColor(getResources().getColor(R.color.zhzj_default));
                viewCache.tv_oper.setTextColor(getResources().getColor(R.color.zhzj_default));
                viewCache.tv_command.setTextColor(getResources().getColor(R.color.zhzj_default));
            }
            String[] array1;
            try {
                array1 = historyCommandInfos.get(i + 1).getDate().split(":");
                if (!array1[2].equals(array[2])) {
                    viewCache.gray_line.setVisibility(View.GONE);
                } else {
                    viewCache.gray_line.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                viewCache.gray_line.setVisibility(View.GONE);
            }
            viewCache.tv_xingqi.setText(commandInfo.getDayOfWeek());
            viewCache.tv_month.setText(array[1]);
            viewCache.tv_day.setText(array[2]);
            viewCache.tv_time.setText(array[3] + ":" + array[4] + ":" + array[5]);
            viewCache.tv_command.setText(commandInfo.getCommand() != null ? commandInfo.getCommand() : "");
            viewCache.tv_oper.setText(commandInfo.getOpreator() != null ? commandInfo.getOpreator() : "");

            return view;
        }

        class DeviceInfoView {
            TextView time, command, operator;
            public TextView tv_day, tv_month, tv_xingqi, tv_time, tv_oper, tv_command;
            //            public RelativeLayout rl_day;
//            public LinearLayout ll_time;
            public ImageView iv_circle_hover, iv_circle;
            private View gray_line;
//            ,gray_line_top;
        }
    }

    //获取历史记录
    class CommandLoad implements Runnable {
        private int start, size;

        public CommandLoad(int start, int size) {
            this.size = size;
            this.start = start;
        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", zhuji.getId());
            object.put("start", this.start);
            object.put("size", this.size);
            String result = HttpRequestUtils.requestoOkHttpPost(
                     server + "/jdm/s3/d/hm", object,
                    WifiLockMainActivity.this);
            Log.e(TAG, "start" + start + ";size" + size);
            if ("-3".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(WifiLockMainActivity.this, getString(R.string.history_response_nodevice),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if (result.length() > 4) {
                List<JSONObject> commands = new ArrayList<JSONObject>();
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                    return;
                }

                JSONArray array = resultJson.getJSONArray("result");

                if (array != null && !array.isEmpty()) {
                    for (int j = 0; j < array.size(); j++) {
                        commands.add(array.getJSONObject(j));
                    }
                }
                // 请求成功了，需要刷新数据到页面，也需要清除此设备的历史未读记录
                ContentValues values = new ContentValues();
                values.put("nr", 0); // 未读消息数
                DatabaseOperator.getInstance(WifiLockMainActivity.this).getWritableDatabase().update(
                        "DEVICE_STATUSINFO", values, "id = ?", new String[]{String.valueOf(zhuji.getId())});

                totalSize = resultJson.getIntValue("allCount");
                Message m = mHandler.obtainMessage(12);
                m.obj = commands;
                mHandler.sendMessage(m);
            }
        }
    }

    class UsersLoad implements Runnable {
        private int start, size;

        public UsersLoad(int start, int size) {
            this.size = size;
            this.start = start;
        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
//            object.put("id", 74235915769217024l);
            object.put("id", zhuji.getId());
            object.put("start", this.start);
            object.put("size", this.size);
            String result = HttpRequestUtils.requestoOkHttpPost(
                     server + "/jdm/s3/dln/list", object,
                    WifiLockMainActivity.this);
            if ("-3".equals(result)) {
                if (mHandler.hasMessages(10)) {
                    mHandler.removeMessages(10);
                }
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(WifiLockMainActivity.this, getString(R.string.history_response_nodevice),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if (result != null && result.length() > 4) {

                JSONObject resultJson = null;
//                try {
//                    resultJson = JSON.parseObject(SecurityUtil.decryptHexStringToString(result, DataCenterSharedPreferences.Constant.KEY_HTTP));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                resultJson = JSON.parseObject(result);

                JSONArray array = resultJson.getJSONArray("result");

                totalSize = resultJson.getIntValue("total");

                Message m = mHandler.obtainMessage(1);
                m.obj = array;
                mHandler.sendMessage(m);
            }
        }
    }


    //雄迈摄像头事件处理方法
    private void initXiongMaiEvent(final XMFragment xm){
        View.OnClickListener onClickListener =(View.OnClickListener) xm;
        iv_vioce.setOnClickListener(onClickListener);
        wifi_lock_iv_screenshot.setOnClickListener(onClickListener);
        wifi_lock_iv_speak.setOnTouchListener((View.OnTouchListener)xm);
    }

    //静音按钮点击后的图标更换
    public void handleIVoice(boolean isClose){
        if(isClose){
            //为true表示静音
            iv_vioce.setImageResource(R.drawable.zhzj_sxt_jingyin);
        }else{
            //
            iv_vioce.setImageResource(R.drawable.zhzj_sxt_shengyin);
        }
    }


    private void initView() {

        //摄像头部分
        mXMFrameLayout = findViewById(R.id.wifi_xiongmai_parant);
        rl_op_bottom = findViewById(R.id.bottom_camera_btn);//底部说话布局
        iv_vioce = findViewById(R.id.wifi_lock_iv_vioce);//静音按钮
        wifi_lock_iv_screenshot = findViewById(R.id.wifi_lock_iv_screenshot);//截图按钮
        wifi_lock_iv_speak  =findViewById(R.id.wifi_lock_iv_speak);//语音说话按钮
        other_ll_ks = findViewById(R.id.other_ll_ks);
        other_ll_ks.setOnClickListener(this);

        //实现没有绑定摄像头时提示功能
        wifi_lock_iv_speak.setOnClickListener(WifiLockMainActivity.this);//如果有摄像头，雄迈会覆盖事件处理器
        wifi_lock_iv_screenshot.setOnClickListener(WifiLockMainActivity.this);
        iv_vioce.setOnClickListener(WifiLockMainActivity.this);




        iv_dy_status = (ImageView) findViewById(R.id.iv_dy_status);
        tv_status = (TextView) findViewById(R.id.tv_status);
        tv_title = (TextView) findViewById(R.id.tv_title);
        ll_ks = (LinearLayout) findViewById(R.id.ll_ks);
        linearlayout = (LinearLayout) findViewById(R.id.ll_main);
        ll_sbxx = (LinearLayout) findViewById(R.id.ll_sbxx);
        ll_lsmm = (LinearLayout) findViewById(R.id.ll_lsmm);
        ll_his = (LinearLayout) findViewById(R.id.ll_his);
        ll_number = (LinearLayout) findViewById(R.id.ll_number);

        lv_user = (ListView) findViewById(R.id.lv_user);
        ll_userhead = (LinearLayout) findViewById(R.id.layout_zhuji_userlist);
        iv_share = (ImageView) findViewById(R.id.iv_share);

        if (config_show_numberList){
            lv_user.setVisibility(View.VISIBLE);
            ll_his.setVisibility(View.VISIBLE);
            ll_userhead.setVisibility(View.VISIBLE);
        }
        commandListView = (ListView) findViewById(R.id.command_list);
        if (config_show_history){
            commandListView.setVisibility(View.VISIBLE);
            ll_number.setVisibility(View.VISIBLE);
        }
        footerView = LayoutInflater.from(WifiLockMainActivity.this).inflate(R.layout.list_foot_loadmore, null);
        footerView_button = (Button) footerView.findViewById(R.id.load_more);
        footerView_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 加载更多按钮点击
                JavaThreadPool.getInstance().excute(new CommandLoad(historyCommandInfos.size(), 20));
            }
        });
        iv_share.setOnClickListener(this);
        ll_sbxx.setOnClickListener(this);
        ll_ks.setOnClickListener(this);
        ll_lsmm.setOnClickListener(this);
        ll_his.setOnClickListener(this);
        ll_number.setOnClickListener(this);
/*
        if (zhuji.isOnline()){
            *//*
            * 通过onTouchEvent方法得知，
            * 当View不可用时，onTouchEvent方法会直接根据是否可以点击或者长按返回，此时点击事件无法响应
            * *//*
            ll_ks.setEnabled(true);
            other_ll_ks.setEnabled(true);
        }else{
            other_ll_ks.setEnabled(false);
            ll_ks.setEnabled(false);
        }*/

        lockInfos = new ArrayList<>();
        historyCommandInfos = new ArrayList<>();
        popupWindow = new JJLockMenuPopupWindow(this, this);
        adapter = new LockAdapter();
        lv_user.setAdapter(adapter);
        commandAdapter = new CommandAdapter(mContext);
        commandListView.addFooterView(footerView);
        commandListView.setAdapter(commandAdapter);
//        commandListView.addFooterView(footerView);
    }

    class LockAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return lockInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return lockInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyHolder holder;
            if (convertView == null) {
                holder = new MyHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.activity_jjsuo_user_item, parent, false);
                holder.iv_circle = (ImageView) convertView.findViewById(R.id.iv_logo);
                holder.iv_right = (ImageView) convertView.findViewById(R.id.iv_right);
                holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                holder.tv_number = (TextView) convertView.findViewById(R.id.tv_number);
                holder.tv_type = (TextView) convertView.findViewById(R.id.tv_type);
                holder.tv_command = (TextView) convertView.findViewById(R.id.tv_command);
                convertView.setTag(holder);
            } else {
                holder = (MyHolder) convertView.getTag();
            }
            SmartLockInfo lockInfo = lockInfos.get(position);
            if (lockInfo.getId() != 0){
                holder.iv_circle.setVisibility(View.VISIBLE);
                holder.iv_right.setVisibility(View.VISIBLE);
                holder.tv_name.setText(lockInfo.getLname());
                if (lockInfo.getPermission() == 1) {
                    holder.tv_command.setText(getString(R.string.normal));
                } else if (lockInfo.getPermission() == 2) {
                    holder.tv_command.setText(getString(R.string.jjsuo_suo_pause));
                } else if (lockInfo.getPermission() == 3) {
                    holder.tv_command.setText(lockInfo.getPerResidueDegree() + getString(R.string.jjsuo_info_count));
                } else if (lockInfo.getPermission() == 4) {
                    holder.tv_command.setText(showTime(lockInfo));
                } else {
                    holder.tv_command.setText("");
                }
                holder.tv_number.setText(lockInfo.getNumber());
                switch (lockInfo.getType()){
                    case 0:
                        holder.tv_type.setText(getString(R.string.jjsuo_user_type_0));
                        break;
                    case 1:
                        holder.tv_type.setText(getString(R.string.jjsuo_user_type_1));
                        break;
                    case 2:
                        holder.tv_type.setText(getString(R.string.jjsuo_user_type_2));
                        break;
                    case 3:
                        holder.tv_type.setText(getString(R.string.jjsuo_user_type_3));
                        break;
                    case 4:
                        holder.tv_type.setText(getString(R.string.jjsuo_user_type_4));
                        break;
                    case 5:
                        holder.tv_type.setText(getString(R.string.jjsuo_user_type_5));
                    case 6:
                        holder.tv_type.setText(getString(R.string.jjsuo_user_type_6));
                        break;
                    case 7:
                        holder.tv_type.setText(getString(R.string.jjsuo_user_type_7));
                        break;
                    case 8:
                        holder.tv_type.setText(getString(R.string.jjsuo_user_type_8));
                        break;
                    default :
                        holder.tv_type.setText(getString(R.string.jjsuo_user_type_un));
                        break;
                }
            }else{
                holder.iv_circle.setVisibility(View.INVISIBLE);
                holder.iv_right.setVisibility(View.INVISIBLE);
                if (lockInfo.getPermission() == 0){
                    holder.tv_command.setText("");
                }
                holder.tv_number.setText(R.string.jjsuo_user_number);
                holder.tv_type.setText(R.string.jjsuo_user_type);
                holder.tv_name.setText(R.string.jjsuo_user_lname);
            }


            return convertView;
        }

        class MyHolder {
            ImageView iv_circle,iv_right;
            TextView tv_name, tv_command,tv_number,tv_type;
        }
    }

    private String getTime(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm");
        return format.format(date);
    }

    private String showTime(SmartLockInfo lockInfo) {
        if (lockInfo.getPerStartTime() == null || lockInfo.getPerEndTime() == null) {
            return "";
        }
        String result = "";
        Date dateStart;
        Date dateEnd;
        lockInfo.getPerStartTime();
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHH");
        try {
            dateStart = format.parse(lockInfo.getPerStartTime());
            dateEnd = format.parse(lockInfo.getPerEndTime());
            result = getTime(dateStart) + "  -  " + getTime(dateEnd);
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("lockMainTimeError", "error");
        }
        return result;
    }


    public void back(View v) {
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void updateLockInfo(final String pramas) {
        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject object = new JSONObject();

                object.put("did", zhuji.getId());

                object.put("permission", lockInfos.get(itemPosition).getPermission());

                if (lockInfos.get(itemPosition).getPermission() == 3) {
                    try {
                        object.put("pertotal", lockInfos.get(itemPosition).getPerResidueDegree());
                    } catch (Exception e) {
                        Log.e("permisson;", "解析失败");
                    }

                } else if (lockInfos.get(itemPosition).getPermission() == 4) {
                    object.put("perstart", lockInfos.get(itemPosition).getPerStartTime());
                    object.put("perend", lockInfos.get(itemPosition).getPerEndTime());
                }


//                object.put("number", lockInfos.get(itemPosition).getNumber());
                object.put("nname", lockInfos.get(itemPosition).getLname());
                object.put("conpassword", pramas);
                object.put("vid", lockInfos.get(itemPosition).getId());

                String result;
                result = HttpRequestUtils
                        .requestoOkHttpPost(
                                 server + "/jdm/s3/dln/update", object, WifiLockMainActivity.this);

                Log.e(TAG, object.toJSONString());
                if ("0".equals(result)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            //device_set_tip_success
                            cancelInProgress();
                            if (mHandler.hasMessages(10)) {
                                mHandler.removeMessages(10);
                            }
                            if (pramas.equals("")) {
                                Toast.makeText(mContext, getString(R.string.jjsuo_pass_cancle_notice),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(mContext, getString(R.string.jjsuo_pass_succ),
                                        Toast.LENGTH_LONG).show();
                            }
                            initUserOrHistoryInfos();
//                        sendBroadcast(new Intent(Actions.ACCETP_REFRESH_MEDICINE_INFO));
                        }
                    });
                } else if ("-4".equals(result)) {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.net_error_requestfailed),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-5".equals(result)) {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.jjsuo_set_pass_other),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-6".equals(result)) {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(WifiLockMainActivity.this, getString(R.string.net_error_operationfailed), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    if (mHandler.hasMessages(10)) {
                        mHandler.removeMessages(10);
                    }
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(mContext, getString(R.string.register_tip_empty),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }

    private void closeKeyboard() {
        //关闭软键盘
        imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
        //恢复位置
        mAlertViewExt.setMarginBottom(0);
    }


    /*
    * Timer在一个线程内周期性执行某个线程执行体
    * */
    Timer mTimer ;
    AlertDialog mConfirmAlert ;

    class CustomTask extends  TimerTask{

        TextView confirmText ;
        int i  = 20 ;
        public CustomTask(TextView confirmText){
            super();
            this.confirmText = confirmText ;
        }
        @Override
        public void run() {
            i--;
           if(i>0){
               //20s倒计时
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       confirmText.setText(getString(R.string.unlock_notice_ensure)+"("+i+"s)");
                   }
               });
           }else{
               //超过20s关闭,对话框
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       if(mConfirmAlert.isShowing()){
                           mConfirmAlert.dismiss();
                       }
                       Toast toast = Toast.makeText(mContext,null,Toast.LENGTH_SHORT);
                       toast.setText(getString(R.string.unlock_time_passed));
                       toast.show();
                       Log.d(TAG,"开锁20s时间到了");
                   }
               });
               mTimer.cancel();//取消
           }
        }
    }
    @Override
    public void onItemClick(Object o, int position) {
        closeKeyboard();
        //判断是否是拓展窗口View，而且点击的是非取消按钮
        if (o == mAlertViewExt && position != AlertView.CANCELPOSITION) {
            if(type==3){
                String pwd = etName.getText().toString();
                if(!TextUtils.isEmpty(pwd)){
                    if(pwd.length()==6){
                        //进行开锁密码验证
                        if(mCommandInfo.getCommand().equals(pwd)){
                            //密码验证成功,发送控制指令，会发送会验证码，需要再一次确认
                            //发送开锁指令
                            showInProgress(getString(R.string.jujiangsuo_ks_optioning), false, true);
                            SyncMessage message = new SyncMessage();
                            message.setCommand(SyncMessage.CommandMenu.rq_control.value());
                            message.setDeviceid(zhuji.getId());
                            message.setSyncBytes(new byte[]{0x02});
                            SyncMessageContainer.getInstance().produceSendMessage(message);
                            mHandler.sendEmptyMessageDelayed(11, 8 * 1000);
                        }else{
                            //密码错误,提示重新输入
                            Toast toast = Toast.makeText(mContext,null,Toast.LENGTH_SHORT);
                            toast.setText(getString(R.string.pw_incrrect));
                            toast.show();
                        }
                    }else{
                        ToastTools.short_Toast(mContext,getString(R.string.abbq_cld_pwd_title));
                    }
                }else{
                    ToastTools.short_Toast(mContext,getString(R.string.input_password));
                }
            }else{
                if (etName.getText().toString().length() != 8) {
                    Toast.makeText(mContext, getString(R.string.jjsuo_set_pass_length), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (etName.getText().toString().equals(lockInfos.get(itemPosition).getConPassword())) {
                    Toast.makeText(mContext, getString(R.string.jjsuo_set_pass), Toast.LENGTH_SHORT).show();
                    return;
                }
                String name = etName.getText().toString();
//            mHandler.sendEmptyMessageDelayed(10, 15 * 1000);
                showInProgress(getString(R.string.device_set_tip_inupdate), false, true);
                updateLockInfo(name);
            }
            return;
        }
    }

    @Override
    public void onClick(View v) {
        Intent deviceIntent;
        switch (v.getId()) {
            case R.id.iv_share:
                deviceIntent = new Intent();
                deviceIntent.putExtra("device", Util.getZhujiDevice(zhuji));
                deviceIntent.setClass(this, PerminssonTransActivity.class);
                startActivity(deviceIntent);
                break;
            case R.id.btn_play:
//                if (deviceInfo.getBipc() == null || deviceInfo.getBipc().equals("0")) {
//                    Toast.makeText(mContext, getString(R.string.jjsuo_bind_carera), Toast.LENGTH_SHORT).show();
//                    return;
//                }
                break;
            case R.id.btn_setpause:

                break;
            case R.id.btn_deldevice:
                popupWindow.dismiss();
                new AlertView(getString(R.string.deviceslist_server_leftmenu_deltitle),
                        getString(R.string.weight_del_user),
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
                                            JSONArray array = new JSONArray();
                                            JSONObject object1 = new JSONObject();
                                            object1.put("vid", lockInfos.get(itemPosition).getId());
                                            array.add(object1);
                                            object.put("vids", array);
                                            object.put("did", zhuji.getId());
                                            server = server + "/jdm/s3/dln/del";
                                            String result = HttpRequestUtils.requestoOkHttpPost( server, object, WifiLockMainActivity.this);
                                            // -1参数为空，0删除成功
                                            if (result != null && result.equals("0")) {
                                                mHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        cancelInProgress();
                                                        Toast.makeText(WifiLockMainActivity.this, getString(R.string.device_del_success), Toast.LENGTH_SHORT).show();
                                                        lockInfos.remove(itemPosition);
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                });

                                            } else if ("-5".equals(result)) {
                                                mHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        cancelInProgress();
                                                        Toast.makeText(WifiLockMainActivity.this, getString(R.string.net_error_operationfailed), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            } else {
                                                mHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        cancelInProgress();
                                                        Toast.makeText(WifiLockMainActivity.this, getString(R.string.net_error_failed), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        }).show();
                break;
            case R.id.btn_update:
                popupWindow.dismiss();
                LayoutInflater factory = LayoutInflater.from(WifiLockMainActivity.this);//提示框
                final View view = factory.inflate(R.layout.zss_edit_box, null);//这里必须是final的
                final EditText edit = (EditText) view.findViewById(R.id.et_nicheng);//获得输入框对象
                new AlertDialog.Builder(mContext, R.style.AppTheme_Dialog_Alert)
                        .setTitle(getString(R.string.jjsuo_user_unametip))//提示框标题
                        .setView(view)
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.sure),//提示框的两个按钮
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        if (TextUtils.isEmpty(edit.getText().toString().trim())) {
                                            Toast.makeText(WifiLockMainActivity.this, getString(R.string.jjsuo_user_uname_empty), Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        showInProgress(getString(R.string.loading));
                                        JavaThreadPool.getInstance().excute(new Runnable() {
                                            @Override
                                            public void run() {
                                                String server = dcsp.getString(
                                                        DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                                                JSONObject object1 = new JSONObject();
                                                object1.put("did",zhuji.getId());
                                                object1.put("vid", lockInfos.get(itemPosition).getId());
                                                object1.put("nname", edit.getText().toString().trim());
                                                server = server + "/jdm/s3/dln/update";
                                                String result = HttpRequestUtils.requestoOkHttpPost( server, object1, WifiLockMainActivity.this);
                                                // -1参数为空，0更新成功
                                                if (result != null && result.equals("0")) {
                                                    mHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            cancelInProgress();
                                                            Toast.makeText(WifiLockMainActivity.this, getString(R.string.device_set_tip_success), Toast.LENGTH_SHORT).show();
                                                            lockInfos.get(itemPosition).setLname(edit.getText().toString().trim());
                                                            adapter.notifyDataSetChanged();
                                                        }
                                                    });

                                                }else{
                                                    mHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            cancelInProgress();
                                                            Toast.makeText(WifiLockMainActivity.this, getString(R.string.update_failed), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                })
                        .setNegativeButton(getString(R.string.cancel), null).create().show();
                break;

            case R.id.ll_sbxx:
                    deviceIntent = new Intent();
                    deviceIntent.setClass(mContext.getApplicationContext(), LockInfoActivity.class);
                    deviceIntent.putExtra("device", Util.getZhujiDevice(zhuji));
                    deviceIntent.putExtra("zhuji", zhuji);
//                deviceIntent.putExtra("group", groupDevice);
                    deviceIntent.putExtra("lockId", lockId);
                    startActivity(deviceIntent);
                break;
            case R.id.other_ll_ks:
            case R.id.ll_ks:
                if (zhuji.isOnline()) {
                    //检查是否有密码
                    if(checkWifiZNSHasPwd()){
                        //有密码,提示输入密码后开锁
                        Log.d(TAG,"设置了密码，输入密码后开锁");
                        type = 3;
                        inputPwd(type);
                    }else{
                        //没有密码,提示设置密码
                        if(zhuji.isAdmin()){
                            //跳转设置密码
                            final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                            View tempView = getLayoutInflater().inflate(R.layout.unlock_notice_layout,mXMFrameLayout,false);
                            TextView cancel = tempView.findViewById(R.id.pwd_cancel_btn);
                            TextView confirm = tempView.findViewById(R.id.pwd_confirm_btn);
                            TextView content = tempView.findViewById(R.id.pwd_content_tv);
                            content.setText(getString(R.string.zhicheng_kaisuotishi));
                            confirm.setText(getString(R.string.confirm));
                            alertDialog.setView(tempView);
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialog.dismiss();
                                }
                            });
                            confirm.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    alertDialog.dismiss();
                                    Intent intent = new Intent(WifiLockMainActivity.this,LockInfoActivity.class);
                                    intent.putExtra("device", Util.getZhujiDevice(zhuji));
                                    intent.putExtra("zhuji", zhuji);
                                    intent.putExtra("lockId", lockId);
                                    startActivity(intent);
                                }
                            });
                            alertDialog.show();
                        }else{
                            //提醒叫管理员设置密码
                            Toast toast = Toast.makeText(this,null,Toast.LENGTH_LONG);
                            toast.setText(getString(R.string.contact_admin_set_password));
                            toast.show();
                        }
                    }
//                    alertView = new AlertView(getString(R.string.activity_weight_notice),
//                                    "远程开锁提醒",
//                                    getString(R.string.deviceslist_server_leftmenu_delcancel),
//                                    new String[]{getString(R.string.confirm)}, null,
//                                    mContext, AlertView.Style.Alert,
//                                    new OnItemClickListener() {
//
//                                        @Override
//                                        public void onItemClick(Object o, final int position) {
//                                            if (position != -1) {
//                                                //开锁确认
//                                                showInProgress(getString(R.string.jujiangsuo_ks_optioning), false, true);
//                                                SyncMessage message = new SyncMessage();
//                                                message.setCommand(SyncMessage.CommandMenu.rq_control.value());
//                                                message.setDeviceid(zhuji.getId());
//                                                message.setSyncBytes(new byte[]{0x02});
//                                                SyncMessageContainer.getInstance().produceSendMessage(message);
//                                                mHandler.sendEmptyMessageDelayed(11, 8 * 1000);
//                                            }
//                                        }
//                                    });
//                    alertView.show();
                }else{
                    String temp = getResources().getString(R.string.jujiangsuo_init_zjoffline);
                   Toast toast =  Toast.makeText(this,null,Toast.LENGTH_SHORT);
                   toast.setText(temp);
                   toast.show();
                }
                break;
            case R.id.ll_lsmm:
                deviceIntent = new Intent();
                deviceIntent.setClass(this,LockPasswordActivity.class);
                deviceIntent.putExtra("lockId", lockId);
                deviceIntent.putExtra("device", Util.getZhujiDevice(zhuji));
                startActivity(deviceIntent);
                break;
            case R.id.ll_his:
                /*deviceIntent = new Intent();
                deviceIntent.setClass(this, DeviceCommandHistoryActivity.class);
                deviceIntent.putExtra("device", Util.getZhujiDevice(zhuji));
                startActivity(deviceIntent);*/
                if(commandListView.getVisibility()==View.INVISIBLE){
                    commandListView.setVisibility(View.VISIBLE);
                    //开锁按钮不见了，显示顶部开锁按钮
                    ll_ks.setVisibility(View.VISIBLE);
                    rl_op_bottom.setVisibility(View.INVISIBLE);
                }else{
                    //开锁按钮出来了，隐藏底部开锁按钮
                    ll_ks.setVisibility(View.GONE);
                    commandListView.setVisibility(View.INVISIBLE);
                    rl_op_bottom.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.ll_number:
                    Intent intent1 = new Intent(this, LockUserListActivity.class);
                    intent1.putExtra("device", Util.getZhujiDevice(zhuji));
                    startActivity(intent1);
                break;
            case R.id.wifi_lock_iv_screenshot:
                if(!isCameraShow){
                    //没有绑定可以播放的摄像头
                    ToastTools.short_Toast(this,getResources().getString(R.string.jjsuo_bind_carera));
                    break;
                }
                break;
            case R.id.wifi_lock_iv_speak:
                if(!isCameraShow){
                    //没有绑定可以播放的摄像头
                    ToastTools.short_Toast(this,getResources().getString(R.string.jjsuo_bind_carera));
                    break;
                }
                break;
            case R.id.wifi_lock_iv_vioce:
                if(!isCameraShow){
                    //没有绑定可以播放的摄像头
                    ToastTools.short_Toast(this,getResources().getString(R.string.jjsuo_bind_carera));
                    break ;
                }
                break;

        }
        super.onClick(v);
    }


    private void inputPwd(int type) {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        String title = "";
        String content = "";
        if(type==3){
            title = getString(R.string.jujiangsuo_title);
            content = getString(R.string.inputpassword);
        }
        //拓展窗口
        mAlertViewExt = new AlertView(null, title, getString(R.string.cancel), null, new String[]{getString(R.string.compele)},
                this, AlertView.Style.Alert, this);
        ViewGroup extView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.activity_addzhuji_alertext_form, null);
        etName = (EditText) extView.findViewById(R.id.etName);

        etName.setText(title);
        etName.setHint(content);

        if(type==3){
            etName.setText("");
            etName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});//最大输入6位
            etName.setInputType(InputType.TYPE_CLASS_NUMBER);//只能输入数字
            etName.setTransformationMethod(PasswordTransformationMethod.getInstance());//设置为密文
        }

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


    private AlertView alertView;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 请求数据子线程,查询绑定的摄像头
     */
    class BindingCameraLoad implements Runnable {
        private long uid;
        private String code;
        private String bIpc;

        public BindingCameraLoad(String bIpc) {
            this.bIpc = bIpc;
            uid = DataCenterSharedPreferences.getInstance(mContext, DataCenterSharedPreferences.Constant.CONFIG)
                    .getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
            code = DataCenterSharedPreferences
                    .getInstance(mContext, DataCenterSharedPreferences.Constant.CONFIG).getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");


        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", bIpc);
            Log.e("wxb", "object---->" + uid + ":" + code + ":" + bIpc);
//            String result = HttpRequestUtils.requestHttpServer(
//                     server + "/jdm/service/ipcs/getIPC?v="
//                            + URLEncoder.encode(SecurityUtil.crypt(object.toJSONString(), DataCenterSharedPreferences.Constant.KEY_HTTP)),
//                    CameraListActivity.this, defaultHandler);


            String result=HttpRequestUtils.requestoOkHttpPost(  server + "/jdm/s3/ipcs/getIPC",object,WifiLockMainActivity.this);
            // -1参数为空，-2设备不存在
            if ("-3".equals(result)) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
//                        Toast.makeText(CameraListActivity.this, getString(R.string.history_response_nodevice),
//                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if (result!=null && result.length() > 4) {

                //List<JSONObject> commands = new ArrayList<JSONObject>();
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.e("wxb", "result---->" + resultJson);
                Contact c = new Contact();
                c.contactId = resultJson.getString("iid");
                c.contactName = resultJson.getString("iname");
                c.contactPassword = resultJson.getString("ipassword");
                CameraInfo cameraInfo  = new CameraInfo();
                cameraInfo.setId(resultJson.getString("iid"));
                cameraInfo.setC(resultJson.getString("ibrand"));
                cameraInfo.setIpcid(resultJson.getLong("id"));
                findBindCameraFromLocal(cameraInfo);//查找本地数据库是否存在相应设备头
                Message message = new Message();
                message.what = GETBIPC_MESSAGE;
                message.obj = c;
                mHandler.sendMessage(message);
            }
        }
    }
}
