package com.smartism.znzk.activity.device;

import android.Manifest;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.p2p.core.P2PView;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentMonitorActivity;
import com.smartism.znzk.activity.camera.AlarmPictrueActivity;
import com.smartism.znzk.activity.camera.ApMonitorActivity;
import com.smartism.znzk.activity.common.SendSmsActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.camera.P2PConnect;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceInfo.ControlTypeMenu;
import com.smartism.znzk.domain.DeviceKeys;
import com.smartism.znzk.domain.HistoryCommandInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.global.AppConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.Actions.VersionType;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.NativeUtils;
import com.smartism.znzk.util.NetworkUtils;
import com.smartism.znzk.util.PacketUtil;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.view.MyGridView;
import com.smartism.znzk.view.SelectAddPopupWindow;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.widget.HeaderView;
import com.smartism.znzk.widget.MyInputPassDialog;
import com.smartism.znzk.widget.NormalDialog;
import com.smartism.znzk.xiongmai.fragment.XMFragment;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 设备信息，摄像头绑定，和摄像头播放，按钮编辑，按钮点击控制，按钮长按1S控制一次，历史记录
 *
 * @author 2016年08月24日 by 王建
 */
public class DeviceInfoActivity extends ActivityParentMonitorActivity implements OnClickListener{
    public static final String TAG = DeviceInfoActivity.class.getSimpleName();
    private DeviceInfo deviceInfo;
    private ListView commandListView;
    private Button footerView_button;
    private TextView tv_menu, tv_title;
    private CommandAdapter commandAdapter;
    private List<JSONObject> commandList;
    private View footerView, headview;
    private boolean isEditKey = false;
    private int totalSize = 0;
    private KeyItemAdapter keyItemAdapter;
    private EditKeyItemAdapter editItemAdapter;
    private LinearLayout zhuji_info_ll;//信息面板
    private MyGridView keysgGridView; //正常的指令面板,编辑时用的gridview
    private RelativeLayout AFpanel; //安防面板
    private List<CommandKey> keys, showKeys;//一个是所有的按键，一个是显示用的按键(有些需要合并开关键)，showKeys是可以不要的，只要屏蔽按钮编辑功能即可
    public static int key;//判断按键是震动 1、有声 2、无声0
    SelectAddPopupWindow menuWindow;// 自定义的弹出框类 右边菜单
    // 锁存发送 1S发送一次，按住2秒后启动
    private boolean suocun = false,
            isClick = false,
            isShowCamera = false,//是否显示摄像头播放控件
            isShowSuo = false;//是否是锁
    private RelativeLayout rl_suo, rl_dinfo;
    private LinearLayout deviceinfo_head, ll_command_info;
    //////******摄像头相关******////
    private boolean initIpc = false;
    private int dHandler_camera_0 = 1000, dHandler_camera_1 = 1001,  dHandler_daojishi = 20;
    private boolean isSupportState = false;

    private List<HistoryCommandInfo> historyCommandInfos;
    private List<CommandInfo> commandInfos; //设备的指令，状态集合

    private ZhujiInfo zhuji  ;
    private TextView mMalfunctionTv,temperatureTv;
    private ImageView mDeviceManagerIv;

    //智力得
    private ImageView mSendSmsImg ;

    private ImageView bgKeyView ; //易迅格按钮背景改变


    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1) {
                if (vkey == null) {
                    Toast.makeText(DeviceInfoActivity.this, getString(R.string.initfailed),
                            Toast.LENGTH_LONG).show();
                }
                iv_annima.clearAnimation();
                iv_annima.setVisibility(View.GONE);
                iv_suo.setImageResource(R.drawable.zss_unlock);

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmyy");//12:26:14:45
                String data = dateFormat.format(new Date());
                int id = Integer.parseInt(vkey, 16);
                String suo = String.valueOf(Long.parseLong(String.valueOf(new NativeUtils().getSecrct(id, Integer.parseInt(data)))));

                for (int i = 0; i < 10 - suo.length(); i++) {
                    suo = "0" + suo;
                }
                final String finalSuo = suo;

                AlertDialog builder = new AlertDialog.Builder(mContext, R.style.AppTheme_Dialog_Alert)
                        .setTitle(getString(R.string.jujiangsuo_title))
                        .setMessage(suo + "\n\n" + getString(R.string.jujiangsuo_msg))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                iv_suo.setImageResource(R.drawable.zss_lock);
                            }
                        })
                        .setNegativeButton(getString(R.string.deviceinfo_activity_copy), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create();
                builder.show();
                builder.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        manager.setText(finalSuo);
                        Toast.makeText(mContext, getString(R.string.deviceinfo_activity_copy_ok), Toast.LENGTH_SHORT).show();
                        return;
                    }
                });


            }
            if (msg.what == 5) {
                cancelInProgress();
                T.showShort(mContext, R.string.timeout);
            }
            if (msg.what == 6) {
                cancelInProgress();
                tv_menu.setText(getString(R.string.action_settings));
                List<DeviceKeys> deviceKeyses = DatabaseOperator.getInstance(DeviceInfoActivity.this).findDeviceKeysByDeviceId(deviceInfo.getId());
                if (deviceKeyses != null && !deviceKeyses.isEmpty()) {
                    keys.clear();
                    for (CommandKey commandKey : initKeys(deviceKeyses)) {
                        keys.add(commandKey);
                    }
                    keysSupportStateHeBin();
                }
                keyItemAdapter.notifyDataSetChanged();
                headview.setVisibility(View.VISIBLE);
                if (isShowCamera) {
                    if(r_p2pview!=null){
                        r_p2pview.setVisibility(View.VISIBLE);
                    }
                }
                //是否是智能锁
                if (isShowSuo) {
                    rl_suo.setVisibility(View.VISIBLE);
                }
            }
            if (msg.what == 7) {
                cancelInProgress();
                T.showShort(mContext, R.string.timeout);
            }
            if (msg.what == 8) {
                isClick = false;
            }
            if (msg.what == 9) { // 获取数据成功
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
            }
            if (msg.what == 10) { // 获取数据成功
                cancelInProgress();
                if (isShowCamera && keys != null && !keys.isEmpty()) {
                    //绑定摄像头情况下，有按键的全部不显示历史记录
                        if(!(deviceInfo.getCa()!=null&&
                                (deviceInfo.getCa().equals(DeviceInfo.CaMenu.zhujijzm.value())||deviceInfo.getCa().equals(DeviceInfo.CaMenu.nbyg.value())
                                        ||DeviceInfo.CaMenu.nbrqbjq.value().equals(deviceInfo.getCa())))){
                            if(commandListView.getHeaderViewsCount()>0){
                                commandListView.removeFooterView(footerView);
                                commandList.clear();
                                historyCommandInfos.clear();
                                commandAdapter.notifyDataSetChanged();
                                return true;
                            }
                        }
                }
                historyCommandInfos.clear();
                commandList.addAll((List<JSONObject>) msg.obj);
                int nullCount = 0 ; //空字符的数量
                for (int i = 0; i < commandList.size(); i++) {
                    HistoryCommandInfo info = new HistoryCommandInfo();
                    JSONObject object1 = commandList.get(i);
                    if(TextUtils.isEmpty(object1.getString("deviceCommand"))){
                        nullCount++;
                        continue;
                    }
                    info.setCommand(object1.getString("deviceCommand"));
                    info.setOpreator(object1.getString("send"));
                    String parms1 = "yyyy:MM:dd:HH:mm:ss";
                    Date date = object1.getDate("deviceCommandTime");
                    String hour = new SimpleDateFormat(parms1).format(date.getTime());

                    info.setDate(hour);
                    info.setDayOfWeek(getWeek(date));
                    historyCommandInfos.add(info);
                }

                commandAdapter.notifyDataSetChanged();
                if ((totalSize-nullCount)== historyCommandInfos.size()) {
                    commandListView.removeFooterView(footerView);
                }
            } else if (msg.what == 11) { // 键值获取成功
                defHandler.removeMessages(6);
                if (!isEditKey) {
                    tv_menu.setText(getString(R.string.action_settings));
                    if (msg.obj != null) {
                        keys.clear();
                        for (CommandKey commandKey : (List<CommandKey>) msg.obj) {
                            keys.add(commandKey);
                        }
                        keysSupportStateHeBin();//合并开 关 状态的按钮
                    }
                    if (keys != null && !keys.isEmpty()) {
                        //有按钮才y有走侧菜单
//                        tv_menu.setVisibility(View.VISIBLE);
                        if (isShowCamera) {
                            //绑定摄像头不显示历史列表
                            commandList.clear();
                            historyCommandInfos.clear();
                            commandAdapter.notifyDataSetChanged();
                        }
                    }
                    if (isShowCamera && DeviceInfo.CaMenu.zhinengsuo.value().equals(deviceInfo.getCa())) {
//                        if (isShowCamera) tv_menu.setVisibility(View.VISIBLE);
                    }

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    // 隐藏软键盘
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                    gridView.setVisibility(View.GONE);
                    if (DeviceInfo.CaMenu.zhuji.value().equals(deviceInfo.getCa())){ //主机不显示下行控制指令(催泪弹等主机控制指令)
                        keysgGridView.setVisibility(View.GONE);
                    }else{
                        keysgGridView.setVisibility(View.VISIBLE);
                    }


                }
                keyItemAdapter.notifyDataSetChanged();
            } else if (msg.what == 12) { // 锁存&发射
                if (suocun || msg.arg2 == 1||msg.arg2==3) {
                    if (msg.arg2 == 1) {
                        defHandler.removeMessages(12);
                        return true;
                    }
                    SyncMessage message1 = new SyncMessage();
                    message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
                    message1.setDeviceid(deviceInfo.getId());
                    // 操作
                    if (isSupportState){//支持状态回传
                        CommandKey key = (CommandKey) msg.obj;
                        if (key!=null){
                            if (key.getSort()%2==0){//偶数按了，减一
                                message1.setSyncBytes(new byte[]{(byte) (key.getSort()-1)});
                            }else{
                                message1.setSyncBytes(new byte[]{(byte) (key.getSort()+1)});
                            }
                        }
                    }else{
                        message1.setSyncBytes(new byte[]{(byte) keys.get(msg.arg1).getSort()});
                    }
                    SyncMessageContainer.getInstance().produceSendMessage(message1);
                    isClick = true;//区别推送下来的消息
                    if (suocun) {
                        defHandler.sendMessageDelayed(defHandler.obtainMessage(12, msg.arg1, 0,msg.obj), 1000);
                    }
                }
            } else if (msg.what == dHandler_camera_0) {
                Log.e("dxswifi", "rtsp失败");
                showError("connect error", 0);
                P2PHandler.getInstance().reject();
            } else if (msg.what == dHandler_camera_1) {
                Log.e("dxswifi", "rtsp成功");
                rlPrgTxError.setVisibility(View.GONE);
                P2PConnect.setCurrent_state(2);
                playReady();
                mContact.apModeState = Constants.APmodeState.LINK;
            }else if(msg.what == dHandler_daojishi){
                if (controlConfrimAlert!=null && controlConfrimAlert.isShowing()){
                    LinearLayout loAlertButtons = (LinearLayout) controlConfrimAlert.getContentContainer().findViewById(R.id.loAlertButtons);
                    TextView textView = (TextView) loAlertButtons.getChildAt(2).findViewById(R.id.tvAlert); //获取到按钮
                    textView.setText(getString(R.string.sure)+"("+msg.arg1+")");
                    if (msg.arg1 <= 0){
                        textView.setClickable(false);
                        textView.setTextColor(getResources().getColor(R.color.gray));
                    }else {
                        defHandler.sendMessageDelayed(defHandler.obtainMessage(dHandler_daojishi, msg.arg1 - 1, 0), 1000);
                    }
                }else{
                    defHandler.removeMessages(dHandler_daojishi);
                }
            }
            return false;
        }
    };
    private Handler defHandler = new WeakRefHandler(mCallback);

    /**
     * 将支持状态回传的设备或者类型或者种类进行按钮合并
     */
    private void keysSupportStateHeBin(){
        showKeys.clear();
        showKeys.addAll(keys);
        if(!VersionType.CHANNEL_WANGDUODUO.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
            if (!keys.isEmpty() && keys.get(0).isSstate()) {//按键合并 按键支持状态回传
                isSupportState = keys.get(0).isSstate();
                for (int i = 0; i < showKeys.size(); i++) {
                    boolean isChangeVisibility = false;//是否有状态
                    if (commandInfos != null && !commandInfos.isEmpty()) {
                        for (CommandInfo c : commandInfos) {
                            if ("95".equals(c.getCtype())) {//开关状态
                                isChangeVisibility = true;
                                int wei = showKeys.get(i).getSort() % 2 == 0 ? showKeys.get(i).getSort() / 2 : showKeys.get(i).getSort() / 2 + 1;
                                if (showKeys.size() == 2) {
                                    wei = 1;
                                }
                                String comStr = PacketUtil.int2HexString(Integer.parseInt(c.getCommand()), 8);
                                if (Integer.parseInt(comStr.substring(comStr.length() - wei, comStr.length() - wei + 1)) % 2 == 1) {//当前键为开状态
                                    if (showKeys.get(i).getSort() % 2 == 0) { //关按键
                                        showKeys.remove(i);
                                        i--;
                                    }
                                } else { //关状态
                                    if (showKeys.get(i).getSort() % 2 != 0) { //开按键
                                        showKeys.remove(i);
                                        i--;
                                    }
                                }
                            }
                        }
                    }
                    if (!isChangeVisibility) {
                        if (showKeys.get(i).getSort() % 2 != 0) { //偶数关 奇数开 默认关闭状态
                            showKeys.remove(i);
                            i--;
                        }
                    }
                }
            }
        }
    }

    public static String getWeek(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        String week = sdf.format(date);
        return week;
    }

    boolean isCreatP2P = false;

    String cameraPaiZi = null;//摄像头牌子标识
    //雄迈摄像头相关
    FrameLayout mXMFrameParent ;//雄迈摄像头父布局




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);


        //获取摄像头牌子
        cameraPaiZi = getIntent().getStringExtra("cameraPaiZi");//获取摄像头牌子
        mContext = this;
        isCreatP2P = true;
        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        mContact = (Contact) getIntent().getSerializableExtra("contact");
        zhujiInfo = (ZhujiInfo) getIntent().getSerializableExtra("zhuji");

        if (!StringUtils.isEmpty(deviceInfo.getBipc()) && !"0".equals(deviceInfo.getBipc())) {
            //确认是否绑定了摄像头
            if (mContact != null) {
                isShowCamera = true;
            }
        }
        // 初始化右边菜单
        menuWindow = new SelectAddPopupWindow(DeviceInfoActivity.this, this, 1, isShowCamera, deviceInfo);
        if (!com.smartism.znzk.util.StringUtils.isEmpty(MainApplication.app.getAppGlobalConfig().getAPPID())
                ||!com.smartism.znzk.util.StringUtils.isEmpty(MainApplication.app.getAppGlobalConfig().getXMAPPKey())) {
            setContentView(R.layout.activity_device_info);
        } else {
            setContentView(R.layout.activity_device_info_nop2pview);
        }
        initView();
        regFilter();
        //巨将的智能锁
        if (VersionType.CHANNEL_JUJIANG.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                && DeviceInfo.CaMenu.zhinengsuo.value().equals(deviceInfo.getCa())) {
            showInProgress(getString(R.string.loading), false, true);
            JavaThreadPool.getInstance().excute(new LoadKey());
            if (isShowCamera) {//智能锁在绑定摄像头的时候，还显示listview、的h话会出现布局上的问题
                ll_command_info.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 2));
                ll_command_info.setVisibility(View.INVISIBLE);
            }
        }
        historyCommandInfos = new ArrayList<>();
        keys = new ArrayList<>();
        showKeys = new ArrayList<>();
        commandAdapter = new CommandAdapter(DeviceInfoActivity.this);
        //从本地获取按键并刷新显示
        List<DeviceKeys> deviceKeyses = DatabaseOperator.getInstance(DeviceInfoActivity.this).findDeviceKeysByDeviceId(deviceInfo.getId());
        if (deviceKeyses != null && !deviceKeyses.isEmpty()) {
            keys.clear();
            for (CommandKey commandKey : initKeys(deviceKeyses)) {
                keys.add(commandKey);
                Log.e("CommandKey", "key:" + commandKey.toString());
            }
            keysSupportStateHeBin();
        } else {

        }
        commandInfos = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());

        headview.setVisibility(View.VISIBLE);

        keyItemAdapter = new KeyItemAdapter(DeviceInfoActivity.this, showKeys);
        keysgGridView.setAdapter(keyItemAdapter);


        commandListView.addFooterView(footerView);

        if (!StringUtils.isEmpty(deviceInfo.getBipc()) && !"0".equals(deviceInfo.getBipc()) && deviceInfo.getCak().contains(DeviceInfo.CakMenu.control.value())){//下行操控设备绑定了摄像机时，不显示加载更多
            commandListView.removeFooterView(footerView);
        }

        commandListView.addHeaderView(headview);
        commandList = new ArrayList<JSONObject>();


        tv_menu.setOnClickListener(this);
//        JSONObject oo = new JSONObject();
//        oo.put("deviceCommandTime", getString(R.string.history_head_time));
//        oo.put("deviceCommand", getString(R.string.history_head_content));
//        oo.put("deviceOperator", getString(R.string.history_head_deviceOperator));
//        commandList.add(oo);
        JavaThreadPool.getInstance().excute(new CommandLoad(0, 20));
        commandListView.setAdapter(commandAdapter);

        //根据设备类型判断是否显示曲线图标
//        if ("wsd".equals(deviceInfo.getCa()) || "xty".equals(deviceInfo.getCa()) || (DeviceInfo.CaMenu.wenduji.value()).equals(deviceInfo.getCa())) {
        if ("wsd".equals(deviceInfo.getCa()) || "wd".equals(deviceInfo.getCa()) || DeviceInfo.CaMenu.xuetangyi.value().equals(deviceInfo.getCa())) {
            findViewById(R.id.command_history_linechart).setVisibility(View.VISIBLE);
        }


        if (DeviceInfo.CakMenu.control.value().equals(deviceInfo.getCak())
                && !DeviceInfo.CaMenu.zhujiControl.value().equals(deviceInfo.getCa())
                && !DeviceInfo.CaMenu.laba.value().equals(deviceInfo.getCa())) {
            if (!isShowCamera && DeviceInfo.CaMenu.zhinengsuo.value().equals(deviceInfo.getCa())) {
                //智能锁在没有绑定摄像头的情况下是不显示右侧菜单
                tv_menu.setVisibility(View.GONE);
            } else {
                tv_menu.setVisibility(View.VISIBLE);
            }
        }

        footerView_button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 加载更多按钮点击
                JavaThreadPool.getInstance().excute(new CommandLoad(commandList.size(), 20));
            }
        });
        if (VersionType.CHANNEL_JUJIANG.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                && DeviceInfo.CaMenu.zhinengsuo.value().equals(deviceInfo.getCa())) {
            isShowSuo = true;
            rl_suo.setVisibility(View.VISIBLE);
            rl_dinfo.setVisibility(View.GONE);
            initDeviceInfo();
        } else {
            rl_dinfo.setVisibility(View.VISIBLE);
            rl_suo.setVisibility(View.GONE);
            initDeviceLaytouInfo();
        }

//		判断是否是绑定的来决定显示摄像头
        if (isShowCamera) {
            //进行录音权限申请
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.RECORD_AUDIO},0xf1);
            }

            if ("control".equals(deviceInfo.getCak())) {
                commandList.clear();
            }
            historyCommandInfos.clear();
            commandAdapter.notifyDataSetChanged();
//            if (footerView != null) commandList.remove(footerView);

            if(CameraInfo.CEnum.jiwei.value().equals(cameraPaiZi)){
                //技威牌子
                initIpc = true;
                initPlayCamera();
            }else if(CameraInfo.CEnum.xiongmai.value().equals(cameraPaiZi)){
                //雄迈
                if(savedInstanceState==null){
                    mXMFrameParent = findViewById(R.id.xiongmaiParent);
                    mXMFrameParent.setVisibility(View.VISIBLE);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    CameraInfo cameraInfo = new CameraInfo();
                     cameraInfo.setId(mContact.contactId );
                     cameraInfo.setN(mContact.getContactName());
                     cameraInfo.setP( mContact.userPassword);
                     cameraInfo.setC("xiongmai");
                    XMFragment xmFragment = XMFragment.newInstance(cameraInfo);
                    fragmentTransaction.add(R.id.xiongmaiParent,xmFragment);
                    fragmentTransaction.commit();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT);
                            lp.height = getResources().getDisplayMetrics().widthPixels * 9/16;
                            mXMFrameParent.setLayoutParams(lp);
                        }
                    });
                }
            }

        }

        //昱川不要卷闸门控制器控制按键
        if(VersionType.CHANNEL_HZYCZN.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            if(deviceInfo!=null&&deviceInfo.getCa()!=null&&deviceInfo.getCa().equals("jzmkzq")){
                commandListView.removeHeaderView(headview);
            }
        }

        //埃利恩隐藏控制按键
        if(zhujiInfo!=null&&zhujiInfo.getMasterid()!=null&&zhujiInfo.getMasterid().contains("FF3B")
                &&deviceInfo.getCa()!=null&&deviceInfo.getCa().equals(DeviceInfo.CaMenu.laba.value())){
            commandListView.removeHeaderView(headview);
        }





        initZhilide();

        initNbYg(); //nb烟感

        initWifiymq();//Wifiymq
    }

    private void initNbYg(){
        //nb烟感显示故障状态文字
        if(deviceInfo!=null&&(deviceInfo.getCa()!=null&&(deviceInfo.getCa().equals(DeviceInfo.CaMenu.nbyg.value())
                ||DeviceInfo.CaMenu.nbrqbjq.value().equals(deviceInfo.getCa())))){
            mDeviceManagerIv.setVisibility(View.VISIBLE);
            mMalfunctionTv.setVisibility(View.VISIBLE);
            mMalfunctionTv.setText(getString(R.string.malfunction_status_tip_tv,getString(R.string.zss_blow_normal)));

            loadNbYgInfo();
        }
    }

    private TextView drugStatusTv ;
    //Wifi烟雾器
    private void initWifiymq(){
        if(deviceInfo!=null&&(deviceInfo.getCa()!=null&&deviceInfo.getCa().equals(DeviceInfo.CaMenu.wifiymq.value()))) {
            mAnBaForceAlertTip = new AlertView(getString(R.string.remind_msg), getString(R.string.abbq_ges_notice_force_tip),
                    null,
                    new String[]{getString(R.string.ready_guide_msg13)}, null,
                    mContext, AlertView.Style.Alert,
                    new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, final int position) {
                            if (position != -1) {
                                Log.d(TAG, "点击我知道了");
                            }
                        }
                    });
            mAnBaForceAlertTip.setCancelable(false);

            drugStatusTv = findViewById(R.id.drugStatusTv);
            drugStatusTv.setText(getString(R.string.deviceinfo_activity_wifiymq_drugstatus,getString(R.string.deviceinfo_activity_wifiymq_drugsfull)));//默认状态
            drugStatusTv.setVisibility(View.VISIBLE);
            mDeviceManagerIv.setVisibility(View.VISIBLE);

            loadWifiymq();
        }
    }

    private void loadWifiymq(){

        new LoadZhujiAndDeviceTask().queryAllCommandInfo(deviceInfo.getZj_id(), new LoadZhujiAndDeviceTask.ILoadResult<List<CommandInfo>>() {
            @Override
            public void loadResult(List<CommandInfo> result) {
                if(result!=null){
                    for(CommandInfo commandInfo:result){
                        if(commandInfo.getCtype().equals("168")){
                            String command = commandInfo.getCommand() ;
                            if(command.equals("0")){
                                //药品缺少
                                drugStatusTv.setText(getString(R.string.deviceinfo_activity_wifiymq_drugstatus,getString(R.string.deviceinfo_activiyt_wifiymq_drugslack)));
                            }else if(command.equals("1")){
                                //药品充足
                                drugStatusTv.setText(getString(R.string.deviceinfo_activity_wifiymq_drugstatus,getString(R.string.deviceinfo_activity_wifiymq_drugsfull)));
                            }
                            break ;
                        }
                    }
                }
            }
        });
    }

    private ZhujiInfo zhujiInfo  ;
    private void loadNbYgInfo(){
        /*异步加载CommandInfo信息
            ILoadResult作为局部变量很可能被内存回收
        */
        if(zhujiInfo==null){
            return ;
        }
        //检查烟感是否在线
        if(!zhujiInfo.isOnline()){
            mMalfunctionTv.setText(getString(R.string.malfunction_status_tip_tv,
                    getResources().getString(R.string.deviceslisy_device_offine)));
        }else{
            new LoadZhujiAndDeviceTask().queryAllCommandInfo(zhujiInfo.getId(), (List<CommandInfo> infos) -> {
                //检查温度
                if(infos!=null){
                    for(CommandInfo commandInfo : infos){
                        if(CommandInfo.CommandTypeEnum.temperature.value().equals(commandInfo.getCtype())){
                            temperatureTv.setVisibility(View.VISIBLE);
                            temperatureTv.setText(getString(R.string.malfunction_status_temperature_tv, commandInfo.getCommand()));
                            break;
                        }
                    }
                }

                //检查低电
                boolean hasDt39 = false ;
                if(infos!=null){
                    for(CommandInfo commandInfo : infos){
                        if("39".equals(commandInfo.getCtype())){
                            if(!TextUtils.isDigitsOnly(commandInfo.getCommand())){
                                break ;
                            }
                            int lowValue = Integer.parseInt(commandInfo.getCommand());
                            if(lowValue<=20){
                                hasDt39 = true ;
                                mMalfunctionTv.setText(getString(R.string.malfunction_status_tip_tv,
                                        getResources().getString(R.string.deviceslist_zhuji_battery_low)));
                            }
                            break;
                        }
                    }
                }

                if(!hasDt39){
                    //没有dt=39
                    if (zhujiInfo.getBatteryStatus() == 1) { //低电
                        mMalfunctionTv.setText(getString(R.string.malfunction_status_tip_tv,
                                getResources().getString(R.string.deviceslist_zhuji_battery_low)));
                    }else {
                        //dt150进行判断
                        if(infos!=null){
                            for(CommandInfo commandInfo:infos){
                                if(commandInfo.getCtype().equals("150")){
                                    if(commandInfo.getCommand().equals("0")){
                                        //显示正常
                                        mMalfunctionTv.setText(getString(R.string.malfunction_status_tip_tv,getString(R.string.zss_blow_normal)));
                                    }else{
                                        mMalfunctionTv.setText(getString(R.string.malfunction_status_tip_tv,getString(R.string.malfunction_exception)));
                                    }
                                    break ;
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    private void initZhilide(){
        if(!VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
            return ;
        }
        mSendSmsImg = findViewById(R.id.send_sms_img);
        mSendSmsImg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), SendSmsActivity.class);
                    intent.putExtra("zhuji_id",deviceInfo.getZj_id());
                    startActivity(intent);
            }
        });

        if(deviceInfo.getCa().equals(DeviceInfo.CaMenu.jdq.value())){
            mSendSmsImg.setVisibility(View.VISIBLE);
        }

    }

    private void initView() {
        mMalfunctionTv = findViewById(R.id.malfunction_status_tv);
        temperatureTv = findViewById(R.id.temperatureTv);
        mDeviceManagerIv = findViewById(R.id.device_manager_img);
        tv_menu = (TextView) findViewById(R.id.menu_tv);
        tv_title = (TextView) findViewById(R.id.tv_title);
        ll_command_info = (LinearLayout) findViewById(R.id.ll_command_info);
        gridView = (GridView) findViewById(R.id.gd_command_key);
        rl_suo = (RelativeLayout) findViewById(R.id.rl_deviceinfo_suo);
        rl_dinfo = (RelativeLayout) findViewById(R.id.dinfo_layout);
        zhuji_info_ll = (LinearLayout) findViewById(R.id.zhuji_info_ll);
        if (VersionType.CHANNEL_JUJIANG.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                && DeviceInfo.CaMenu.zhinengsuo.value().equals(deviceInfo.getCa())) {
            iv_suo = (ImageView) findViewById(R.id.iv_suo);
            iv_annima = (ImageView) findViewById(R.id.iv_an);
            iv_suo.setOnClickListener(this);
            iv_annima.setOnClickListener(this);
        }
        footerView = LayoutInflater.from(DeviceInfoActivity.this).inflate(R.layout.list_foot_loadmore, null);
        headview = LayoutInflater.from(DeviceInfoActivity.this).inflate(R.layout.listview_head, null);
        commandListView = (ListView) findViewById(R.id.command_list);
//        title = (Button) findViewById(R.id.command_history_title);
        keysgGridView = (MyGridView) headview.findViewById(R.id.command_key);

        footerView_button = (Button) footerView.findViewById(R.id.load_more);
        tv_title.setText(deviceInfo.getName());
        headview.setVisibility(View.GONE);

        mDeviceManagerIv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceInfoActivity.this, DeviceDetailActivity.class);
                intent.putExtra("device",deviceInfo);
                startActivity(intent);
            }
        });
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
                            hideVideoFormat();
                            layout_voice_state
                                    .setVisibility(RelativeLayout.VISIBLE);

                            send_voice
                                    .setBackgroundResource(R.drawable.ic_send_audio_p);
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
            // 开始监控时没有声音，暂时这样
            send_voice.setOnClickListener(this);
            // speak();
            // speak();
            // send_voice.performClick();
            // speak();
        }
        initIpcDeviceList();
    }

    public void initPlayCamera() {
        pView = (P2PView) findViewById(R.id.p2pview);
        P2PView.type = 0;
        users = (TextView) findViewById(R.id.users);
        users.setText(getString(R.string.monitor_number) + P2PConnect.getNumber());
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
//        P2PConnect.setMonitorId(callId);// 设置在监控的ID
//        SettingListener.setMonitorID(callId);// 设置在监控的ID
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
        regP2pFilter();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        destroyActivity();
        defHandler.removeCallbacksAndMessages(null);
    }

    public void destroyActivity() {
        if (menuWindow != null) {
            menuWindow.dismiss();
            menuWindow = null;
        }
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    mCurrentVolume, 0);
        }
        if (isRegFilter) {
            mContext.unregisterReceiver(mP2pReceiver);
            isRegFilter = false;
        }
        mContext.unregisterReceiver(mReceiver);
        if (!StringUtils.isEmpty(deviceInfo.getBipc()) && !"0".equals(deviceInfo.getBipc())) {
            if (mContact == null) return;
            if (sensorListener != null) {
                sensorManager.unregisterListener(sensorListener);
            }

            Intent refreshContans = new Intent();
            refreshContans.setAction(Constants.Action.REFRESH_CONTANTS);
            sendBroadcast(refreshContans);
        }
    }

    private void saveModify() {
        final long did = deviceInfo.getId();
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", did);
                JSONArray p = new JSONArray();
                for (CommandKey key : keys) {
                    JSONObject o = new JSONObject();
                    o.put("id", key.getId());
                    o.put("n", key.getName());
                    o.put("i", key.getIoc());
                    o.put("w", key.getWhere());
                    p.add(o);

                }
                pJsonObject.put("keys", p);
//				final String result = HttpRequestUtils
//						.requestHttpServer(
//								 server + "/jdm/service/dkeyupdate?v="
//										+ URLEncoder.encode(
//										SecurityUtil.crypt(pJsonObject.toJSONString(), Constant.KEY_HTTP)),
//								DeviceInfoActivity.this, defHandler);
                final String result = HttpRequestUtils
                        .requestoOkHttpPost(server + "/jdm/s3/d/dkeyupdate", pJsonObject, DeviceInfoActivity.this);
                if ("0".equals(result)) {
                    defHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(DeviceInfoActivity.this,
                                    getString(R.string.activity_editscene_modify_success), Toast.LENGTH_LONG).show();
                            defHandler.sendEmptyMessage(11);
                        }
                    });
                }
            }
        });
    }

    public List<CommandKey> initKeys(List<DeviceKeys> deviceKeyses) {
        List<CommandKey> commandKeys = new ArrayList<>();
        for (DeviceKeys deviceKeys : deviceKeyses) {
            CommandKey commandKey = new CommandKey();
            commandKey.setId(deviceKeys.getDeviceId());
            commandKey.setName(deviceKeys.getKeyName());
            commandKey.setIoc(deviceKeys.getKeyIco());
            commandKey.setSort(deviceKeys.getKeySort());
            commandKey.setWhere(deviceKeys.getKeyWhere());
            commandKeys.add(commandKey);
        }
        return commandKeys;
    }

    private void initKeys() {
        headview.setVisibility(View.VISIBLE);
        keyItemAdapter.notifyDataSetChanged();
    }

    //	public static void setListViewHeightBasedOnChildren(GridView listView,BaseAdapter listAdapter) {
//		if (listAdapter == null) {
//			return;
//		}
//		// 固定列宽，有多少列
//		int col = 4;// listView.getNumColumns();
//		int totalHeight = 0;
//		int itemHeight = 0;
//		// i每次加4，相当于listAdapter.getCount()小于等于4时 循环一次，计算一次item的高度，
//		// listAdapter.getCount()小于等于8时计算两次高度相加
//		for (int i = 0; i < listAdapter.getCount(); i += col) {
//			// 获取listview的每一个item
//			View listItem = listAdapter.getView(i, null, listView);
//			listItem.measure(0, 0);
//			// 获取item的高度和
//			itemHeight = listItem.getMeasuredHeight();
//			totalHeight += itemHeight;
//
//		}
//		totalHeight += itemHeight/2;
//		// 获取listview的布局参数
//		ViewGroup.LayoutParams params = listView.getLayoutParams();
//		// 设置高度
//		params.height = totalHeight;
//		// 设置参数
//		listView.setLayoutParams(params);
//	}
    private ImageView iv_annima, iv_suo;

    /**
     * 设置锁头的布局
     */
    private void initDeviceInfo() {
        TextView name = (TextView) findViewById(R.id.d_sname);
        TextView where = (TextView) findViewById(R.id.d_swhere);
        TextView type = (TextView) findViewById(R.id.d_stype);
        name.setText(deviceInfo.getName());
        where.setText(deviceInfo.getWhere());
        type.setText(deviceInfo.getType());
    }

    private void initDeviceLaytouInfo() {
        ImageView logo = (ImageView) findViewById(R.id.device_logo);
        TextView name = (TextView) findViewById(R.id.d_name);
        TextView where = (TextView) findViewById(R.id.d_where);
        TextView type = (TextView) findViewById(R.id.d_type);
        where.setText(deviceInfo.getWhere());
        type.setText(deviceInfo.getType());
        if (ControlTypeMenu.wenduji.value().equals(deviceInfo.getControlType())) {
            // 设置图片
            if (VersionType.CHANNEL_UCTECH.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
                try {
                    logo.setImageBitmap(BitmapFactory
                            .decodeStream(getAssets().open("uctech/uctech_t_" + deviceInfo.getChValue() + ".png")));
                } catch (IOException e) {
                    Log.e("uctech", "读取图片文件错误");
                }
            } else {
                ImageLoader.getInstance().displayImage(dcsp.getString(Constant.HTTP_DATA_SERVERS, "")
                        + "/devicelogo/" + deviceInfo.getLogo(), logo, new ImageLoadingBar());
            }
            name.setText(deviceInfo.getName() + "CH" + deviceInfo.getChValue());
        } else if (ControlTypeMenu.wenshiduji.value().equals(deviceInfo.getControlType())) {
            if (VersionType.CHANNEL_UCTECH.equals(((MainApplication) getApplication()).getAppGlobalConfig().getVersion())) {
                try {
                    logo.setImageBitmap(BitmapFactory
                            .decodeStream(getAssets().open("uctech/uctech_th_" + deviceInfo.getChValue() + ".png")));
                } catch (IOException e) {
                    Log.e("uctech", "读取图片文件错误");
                }
            } else {
                ImageLoader.getInstance().displayImage(dcsp.getString(Constant.HTTP_DATA_SERVERS, "")
                        + "/devicelogo/" + deviceInfo.getLogo(), logo, new ImageLoadingBar());
            }
            name.setText(deviceInfo.getName() + "CH" + deviceInfo.getChValue());
        } else {
            // 设置图片
            ImageLoader.getInstance().displayImage(dcsp.getString(Constant.HTTP_DATA_SERVERS, "") + "/devicelogo/" + deviceInfo.getLogo(),
                    logo, new ImageLoadingBar());
            name.setText(deviceInfo.getName());
        }
        if (/*deviceInfo.getControlType().contains("xiaxing") && */!"zjykq".equals(deviceInfo.getCa()) ) { //主机遥控器需要屏蔽原有按键
            JavaThreadPool.getInstance().excute(new CommandKeyLoad());
        }
        if (MainApplication.app.getAppGlobalConfig().isShowAFpanel()) {
            if (DeviceInfo.CaMenu.zhujiControl.value().equals(deviceInfo.getCa())) {
                AFpanel = (RelativeLayout) findViewById(R.id.anfang_panel);
                AFpanel.setVisibility(View.VISIBLE);
            }
        }
        key = dcsp.getInt(Constant.BTN_CONTROLSTYLE, 0);
    }

    public void lineChart(View v) {
        Intent intent = new Intent();
//        if ("wsd".equals(deviceInfo.getCa())) {
//            intent.setClass(DeviceInfoActivity.this, WSDChartActivity.class);
//        } else if ("xty".equals(deviceInfo.getCa())) {
//            intent.setClass(DeviceInfoActivity.this, XTYChartActivity.class);
//        } else if (DeviceInfo.CaMenu.wenduji.value().equals(deviceInfo.getCa())) {
//            intent.setClass(DeviceInfoActivity.this, WSDChartActivity.class);
//        }
        intent.putExtra("device", deviceInfo);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!StringUtils.isEmpty(deviceInfo.getBipc()) && !"0".equals(deviceInfo.getBipc())) {
            reject();
            finish();
        }
    }

    public void back(View v) {
//        toGroupInfoActivity();
        reject();
        finish();
    }



//    public void toGroupInfoActivity() {
//        Contact contact = (Contact) getIntent().getSerializableExtra("camera");
//        DeviceInfo groupDevice = (DeviceInfo) getIntent().getSerializableExtra("group");
//        if (groupDevice != null) {
//            Intent intent = new Intent();
//            intent.setClass(DeviceInfoActivity.this, GroupInfoActivity.class);
//            intent.putExtra("device", groupDevice);
//            intent.putExtra("contact", contact);
//            intent.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
//            startActivity(intent);
//        }
//    }

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

            DeviceInfoView viewCache = new DeviceInfoView();
            if (view == null) {
        //       view = layoutInflater.inflate(R.layout.activity_device_command_history_list_item, null);
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
                viewCache = (DeviceInfoView) view.getTag();
            }

            HistoryCommandInfo commandInfo = historyCommandInfos.get(i);

            String[] array = commandInfo.getDate().split(":");
            if (i != 0) {
                String[] array1 = historyCommandInfos.get(i - 1).getDate().split(":");

//                viewCache.tv_day.setVisibility((!array1[2].equals(array[2])) ? View.VISIBLE : View.GONE);
//                viewCache.tv_month.setVisibility((!array1[2].equals(array[2])) ? View.VISIBLE : View.GONE);
//                viewCache.tv_xingqi.setVisibility((!array1[2].equals(array[2])) ? View.VISIBLE : View.GONE);
//
//                viewCache.iv_circle_hover.setVisibility((!array1[2].equals(array[2])) ? View.VISIBLE : View.GONE);
//                viewCache.iv_circle.setVisibility((array1[2].equals(array[2])) ? View.VISIBLE : View.GONE);
//
//
//                viewCache.tv_time.setTextColor((!array1[2].equals(array[2])) ? getResources().getColor(R.color.zhzj_default) : getResources().getColor(R.color.black));
//                viewCache.tv_oper.setTextColor((!array1[2].equals(array[2])) ? getResources().getColor(R.color.zhzj_default) : getResources().getColor(R.color.black));
//                viewCache.tv_command.setTextColor((!array1[2].equals(array[2])) ? getResources().getColor(R.color.zhzj_default) : getResources().getColor(R.color.black));
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
//                if (i == historyCommandInfos.size() - 1) {
//                    viewCache.gray_line.setVisibility(View.INVISIBLE);
//                }
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
//                viewCache.gray_line_top.setVisibility(View.GONE);
            }


//            if (i== historyCommandInfos.size()-1){
//                viewCache.gray_line.setVisibility(View.GONE);
//                viewCache.gray_line_top.setVisibility(View.VISIBLE);
//            }
            viewCache.tv_xingqi.setText(commandInfo.getDayOfWeek());
            viewCache.tv_month.setText(array[1]);
            viewCache.tv_day.setText(array[2]);
            viewCache.tv_time.setText(array[3] + ":" + array[4]+":"+array[5]);
            viewCache.tv_command.setText(commandInfo.getCommand() != null ? commandInfo.getCommand() : "");
            viewCache.tv_oper.setText(commandInfo.getOpreator() != null ? commandInfo.getOpreator() : "");


//            if (i != 0) {
//                String opreator = commandList.get(i).getString("send");
//
//                viewCache.operator.setText(commandList.get(i).getString("send"));
//                try {
//                    viewCache.time.setText(new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date(commandList.get(i).getDate("deviceCommandTime").getTime())));
//                } catch (Exception e) {
//                    viewCache.time.setText(commandList.get(i).getString("deviceCommandTime"));
//                }
//                String command = commandList.get(i).getString("deviceCommand");
            String command = commandInfo.getCommand();
            if (DeviceInfo.CaMenu.tizhongceng.value().equals(deviceInfo.getCa())) {
                try {
                    String unitName = "";
                    long commandUnit = Long.parseLong(command.substring(0, 4));
                    double commandValue = Integer.parseInt(command.substring(4), 16) / 10.0;
                    if (commandUnit == 2) {
                        unitName = "KG";
                    }
                    viewCache.tv_command.setText(commandValue + unitName);
                } catch (Exception e) {
                    viewCache.tv_command.setText("error");
                }
            } else if (DeviceInfo.CaMenu.wenduji.value().equals(deviceInfo.getCa()) ||DeviceInfo.CaMenu.wenshiduji.value().equals(deviceInfo.getCa())){
                if (dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("ssd")) {
                    viewCache.tv_command.setText(command);
                } else if (dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("hsd")) {
                    if (command.contains("℃")) {
                        viewCache.tv_command.setText(((float) Math
                                .round((Float.parseFloat(command.substring(0, command.indexOf("℃"))) * 1.8 + 32) * 10)
                                / 10) + "℉" + command.substring(command.indexOf("℃") + 1));
                    } else {
                        viewCache.tv_command.setText(command);
                    }
                }
            }
//            }
//            else {
//                viewCache.time.setText(commandList.get(i).getString("deviceCommandTime"));
//                viewCache.command.setText(commandList.get(i).getString("deviceCommand"));
//                viewCache.operator.setText(commandList.get(i).getString("deviceOperator"));
//            }
            if (!deviceInfo.getCak().equals("control")) {
                viewCache.tv_oper.setVisibility(View.GONE);
            }

            return view;

//            DeviceInfoView viewCache = new DeviceInfoView();
//            if (view == null) {
////                view = layoutInflater.inflate(R.layout.activity_device_command_history_list_item, null);
//                view = layoutInflater.inflate(R.layout.activity_zhzj_device_command_item, null);
//                viewCache.time = (TextView) view.findViewById(R.id.last_time);
//                viewCache.command = (TextView) view.findViewById(R.id.last_command);
//                viewCache.operator = (TextView) view.findViewById(R.id.last_operator);
//                view.setTag(viewCache);
//            } else {
//                viewCache = (DeviceInfoView) view.getTag();
//            }
//            if (i != 0) {
//                String opreator = commandList.get(i).getString("send");
//
//                viewCache.operator.setText(commandList.get(i).getString("send"));
//                try {
//                    viewCache.time.setText(new SimpleDateFormat("MM-dd HH:mm:ss").format(new Date(commandList.get(i).getDate("deviceCommandTime").getTime())));
//                } catch (Exception e) {
//                    viewCache.time.setText(commandList.get(i).getString("deviceCommandTime"));
//                }
//                String command = commandList.get(i).getString("deviceCommand");
//                if ("tzc".equals(deviceInfo.getCa())) {
//                    try {
//                        String unitName = "";
//                        long commandUnit = Long.parseLong(command.substring(0, 4));
//                        double commandValue = Integer.parseInt(command.substring(4), 16) / 10.0;
//                        if (commandUnit == 2) {
//                            unitName = "KG";
//                        }
//                        viewCache.command.setText(commandValue + unitName);
//                    } catch (Exception e) {
//                        viewCache.command.setText("error");
//                    }
//                } else {
//                    if (dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("ssd")) {
//                        viewCache.command.setText(command);
//                    } else if (dcsp.getString(Constant.SHOW_TEMPERATURE_UNIT, "ssd").equals("hsd")) {
//                        if (command.contains("℃")) {
//                            viewCache.command.setText(((float) Math
//                                    .round((Float.parseFloat(command.substring(0, command.indexOf("℃"))) * 1.8 + 32) * 10)
//                                    / 10) + "℉" + command.substring(command.indexOf("℃") + 1));
//                        } else {
//                            viewCache.command.setText(command);
//                        }
//                    }
//                }
//            } else {
//                viewCache.time.setText(commandList.get(i).getString("deviceCommandTime"));
//                viewCache.command.setText(commandList.get(i).getString("deviceCommand"));
//                viewCache.operator.setText(commandList.get(i).getString("deviceOperator"));
//            }
//            if (!deviceInfo.getCak().equals("control")) {
//                viewCache.operator.setVisibility(View.GONE);
//            }
//            return view;
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
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", deviceInfo.getId());
            object.put("start", this.start);
            object.put("size", this.size);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/hm", object,
                    DeviceInfoActivity.this);
            Log.e(TAG, "start" + start + ";size" + size);
            if ("-3".equals(result)) {
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceInfoActivity.this, getString(R.string.history_response_nodevice),
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
                DatabaseOperator.getInstance(DeviceInfoActivity.this).getWritableDatabase().update(
                        "DEVICE_STATUSINFO", values, "id = ?", new String[]{String.valueOf(deviceInfo.getId())});
                totalSize = resultJson.getIntValue("allCount");
                Message m = defHandler.obtainMessage(10);
                m.obj = commands;
                defHandler.sendMessage(m);
            }
        }
    }

    class CommandKeyLoad implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", deviceInfo.getId());
//			object.put("uid", dcsp.getLong(Constant.LOGIN_APPID, 0));
//			object.put("code", dcsp.getString(Constant.LOGIN_CODE, ""));

//			String result = HttpRequestUtils.requestHttpServer(
//					 server + "/jdm/service/dkeycommands?v="
//							+ URLEncoder.encode(SecurityUtil.crypt(object.toJSONString(), Constant.KEY_HTTP)),
//					DeviceInfoActivity.this, defHandler);
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/dkeycomms", object, DeviceInfoActivity.this);
            if (result != null && result.length() > 4) {
                try {
                    result = result;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                List<CommandKey> list = new ArrayList<>();
                keys.clear();
                JSONArray array = JSON.parseArray(result);
                DatabaseOperator.getInstance(DeviceInfoActivity.this).delDeviceKeysById(deviceInfo.getId());
                if (array != null && !array.isEmpty()) {
                    for (int j = 0; j < array.size(); j++) {
                        CommandKey key = new CommandKey();
                        key.setSort(array.getJSONObject(j).getIntValue("s"));
                        key.setName(array.getJSONObject(j).getString("n"));
                        key.setIoc(array.getJSONObject(j).getString("i"));
                        key.setWhere(array.getJSONObject(j).getIntValue("w"));
                        key.setId(array.getJSONObject(j).getLongValue("id"));
                        key.setSstate(array.getJSONObject(j).getBooleanValue("ss"));
                        Log.e(" CommandKey", "key1:" + key.toString());
                        DeviceKeys dkey = new DeviceKeys();
                        dkey.setKeySort(array.getJSONObject(j).getIntValue("s"));
                        dkey.setKeyName(array.getJSONObject(j).getString("n"));
                        dkey.setKeyIco(array.getJSONObject(j).getString("i"));
                        dkey.setKeyWhere(array.getJSONObject(j).getIntValue("w"));
                        dkey.setDeviceId(array.getJSONObject(j).getLongValue("id"));
                        dkey.setKeySState(array.getJSONObject(j).getBooleanValue("ss"));
                        Log.e(" CommandKey", "key2:" + dkey.toString());
                        DatabaseOperator.getInstance(DeviceInfoActivity.this).insertOrUpdateDeviceKeys(dkey, deviceInfo.getId());
                        list.add(key);
                    }
                }
                Message m = defHandler.obtainMessage(11);
                m.obj = list;
                defHandler.sendMessage(m);
            }

        }
    }

    class CommandKey implements Serializable {
        private long id;
        private int sort;
        private String name;
        private String ioc;
        private int where;
        private boolean sstate;

        public boolean isSstate() {
            return sstate;
        }

        public void setSstate(boolean sstate) {
            this.sstate = sstate;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public int getSort() {
            return sort;
        }

        public void setSort(int sort) {
            this.sort = sort;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIoc() {
            return ioc;
        }

        public void setIoc(String ioc) {
            this.ioc = ioc;
        }

        public int getWhere() {
            return where;
        }

        public void setWhere(int where) {
            this.where = where;
        }

        @Override
        public String toString() {
            return "CommandKey [id=" + id + ", sort=" + sort + ", name=" + name + ", ioc=" + ioc + ", where=" + where
                    + "]";
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

        public KeyItemAdapter(Context context, List<CommandKey> commandKeys) {
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return showKeys.size();
        }

        @Override
        public Object getItem(int position) {
            return showKeys.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            DeviceInfoView viewCache = new DeviceInfoView();
            if (view == null) {
                view = layoutInflater.inflate(R.layout.activity_history_key_item, null, false);
                viewCache.keybg = (ImageView) view.findViewById(R.id.dinfo_keybg);
                viewCache.keyname = (TextView) view.findViewById(R.id.dinfo_keyname);
                if(VersionType.CHANNEL_YIXUNGE.equals(MainApplication.app.getAppGlobalConfig().getVersion())){
                    viewCache.keyname.setVisibility(View.GONE);
                }
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
            if (getCount() >= position + 1) {
                if (isSupportState) {//开关面板按键合并
                    viewCache.keyname.setText(showKeys.get(position).getName());
                    if (showKeys.get(position).getSort() % 2 == 0) {
                        viewCache.keybg.setImageResource(R.drawable.device_item_one_button_bg);
                    } else {
                        viewCache.keybg.setImageResource(R.drawable.device_item_on_button_bg);
                    }
                } else {
                    viewCache.keyname.setText(showKeys.get(position).getName());
                    if (!StringUtils.isEmpty(showKeys.get(position).getIoc())) {
                        viewCache.keybg.setImageResource(
                                getResources().getIdentifier(showKeys.get(position).getIoc(), "drawable", getBaseContext().getPackageName()));
                    } else {
                        viewCache.keybg.setImageResource(R.drawable.device_item_one_button_bg);
                    }
                }
//                viewCache.keyname.setText(keys.get(position).getName());
//                if (!StringUtils.isEmpty(keys.get(position).getIoc())) {
//                    if (viewCache.keybg.getBackground() != null&&keys.get(position).getIoc()!= null){
//                        viewCache.keybg.getBackground().setAlpha(Color.TRANSPARENT);
//                    }
//                    viewCache.keybg.setImageResource(
//                            getResources().getIdentifier(keys.get(position).getIoc(), "drawable", getBaseContext().getPackageName()));
//                } else {
//                    viewCache.keybg.setBackgroundResource(R.drawable.device_item_one_button_bg);
//                }

//                if (!StringUtils.isEmpty(keys.get(position).getIoc())) {
//                        if (keys.get(position).getIoc()!= null)
//                    if (viewCache.keybg.getBackground() != null) {
//                        viewCache.keybg.getBackground().setAlpha(Color.TRANSPARENT);
//                    }
//                    viewCache.keybg.setImageResource(
//                            getResources().getIdentifier(keys.get(position).getIoc(), "drawable", getBaseContext().getPackageName()));
//                } else {
//                    viewCache.keybg.setBackgroundResource(R.drawable.device_item_one_button_bg);
//                }
            }

            viewCache.keybg.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    //解决锁长按不发码问题，ListView和GridView进行了事件拦截，下发子View为ACTION_CANCEL事件,不让他拦截
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    commandListView.requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            if (Util.isFastClick()) {
                                Toast.makeText(DeviceInfoActivity.this, getString(R.string.activity_devices_commandhistory_tip), Toast.LENGTH_SHORT).show();
                            } else {
                                //wifiymq处理逻辑
                                if((deviceInfo!=null&&DeviceInfo.CaMenu.wifiymq.value().equals(deviceInfo.getCa()))){
                                   break  ;
                                }
//								defHandler.sendEmptyMessageDelayed(8,1000);
//								isClick = true;//区别推送下来的消息
                                v.setPressed(true);
                                suocun = true;
                                defHandler.sendMessage(defHandler.obtainMessage(12, position, 0,showKeys.get(position)));
                                Vibrator vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
                                AudioManager mAudioMgr = (AudioManager) getApplicationContext().getSystemService(getApplicationContext().AUDIO_SERVICE);
                                if (key == 1) {
                                    vibrator.vibrate(new long[]{0, 200}, -1);
                                } else if (key == 2) {
                                    mAudioMgr.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT);
                                } else if (key == 3) {
                                    vibrator.vibrate(new long[]{0, 200}, -1);
                                    mAudioMgr.playSoundEffect(AudioManager.FX_FOCUS_NAVIGATION_LEFT);
                                } else {
                                    if (vibrator != null) {
                                        vibrator.cancel();
                                    }

                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            //wifiymq处理逻辑
                            if((deviceInfo!=null&&DeviceInfo.CaMenu.wifiymq.value().equals(deviceInfo.getCa()))){
                                bgKeyView  = (ImageView) v;
                                wifiymqDriveAway(deviceInfo.getZj_id());
                                break  ;
                            }
//                            if (isClick) {
//                                defHandler.sendEmptyMessageDelayed(7, 12000);
//                                showInProgress(getString(R.string.ongoing), false, true);
////                                isClick = false;
//                            }
//							break;
                            v.setPressed(false);
                            suocun = false;
                            defHandler.sendMessage(defHandler.obtainMessage(12, position, 1));
                            notifyDataSetChanged();
                            break;
                        case MotionEvent.ACTION_CANCEL:
//                            v.setPressed(false);
//                            suocun = false;
//                            defHandler.sendMessage(defHandler.obtainMessage(12, position, 1));
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
            return view;
        }

    }

    class EditKeyItemAdapter extends BaseAdapter {
        /**
         * 视图内部类
         *
         * @author Administrator
         */
        class DeviceInfoView {
            Spinner edit_keybg;
            EditText edit_keyname;

        }

        LayoutInflater layoutInflater;
        int pocusIndex = -1;

        public EditKeyItemAdapter(Context context) {
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

            view = layoutInflater.inflate(R.layout.activity_history_edit_key_item, null);
            viewCache.edit_keybg = (Spinner) view.findViewById(R.id.edit_keybg);
            viewCache.edit_keyname = (EditText) view.findViewById(R.id.edit_keyname);

            viewCache.edit_keyname.setText(keys.get(position).getName());
            final List<String> resId = new ArrayList<String>();
            resId.add("checkbutton_circular");
            resId.add("checkbutton_rectangle");
            resId.add("checkbutton_square");
            resId.add("checkbutton_triangle");
            for (int i = 0; i < resId.size(); i++) {
                if (keys.get(position).getIoc() != null && keys.get(position).getIoc().equals(resId.get(i))) {
                    String temp = resId.get(0);
                    resId.set(0, resId.get(i));
                    resId.set(i, temp);

                }
            }
            MyAdapter mAdapter = new MyAdapter(resId);
            final EditText et = viewCache.edit_keyname;
            viewCache.edit_keybg.setAdapter(mAdapter);

            viewCache.edit_keybg.setOnItemSelectedListener(new OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    keys.get(position).setIoc(resId.get(pos));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            viewCache.edit_keyname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        pocusIndex = position;
                    }
                }
            });
            if (pocusIndex == position) {
                viewCache.edit_keyname.requestFocus();
                Log.e("wxb", "setOnFocusChangeListener----->" + pocusIndex);

            }
            viewCache.edit_keyname.addTextChangedListener(new TextWatcher() {
                private CharSequence temp;
                private int editStart;
                private int editEnd;
                private int maxLen = 8;

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    temp = s;
                }

                @Override
                public void afterTextChanged(Editable s) {
                    editStart = et.getSelectionStart();
                    editEnd = et.getSelectionEnd();
                    Log.i("gongbiao1", "" + editStart);
                    if (calculateLength(s.toString()) > maxLen) {
                        s.delete(editStart - 1, editEnd);
                        int tempSelection = editStart;
                        et.setText(s);
                        et.setSelection(tempSelection);
                    }

                    keys.get(position).setName(s.toString());
                }

                private int calculateLength(String etstring) {
                    char[] ch = etstring.toCharArray();

                    int varlength = 0;
                    for (int i = 0; i < ch.length; i++) {
                        if ((ch[i] >= 0x2E80 && ch[i] <= 0xFE4F) || (ch[i] >= 0xA13F && ch[i] <= 0xAA40)
                                || ch[i] >= 0x80) { // 中文字符范围0x4e00 0x9fbb
                            varlength = varlength + 2;
                        } else {
                            varlength++;
                        }
                    }
                    // 这里也可以使用getBytes,更准确嘛
                    // varlength =
                    // etstring.getBytes(CharSet.forName(GBK)).lenght;//
                    // 编码根据自己的需求，注意u8中文占3个字节...
                    return varlength;
                }
            });
            return view;
        }

    }

    class MyAdapter extends BaseAdapter {
        private List<String> resId;

        public MyAdapter(List<String> resId) {
            this.resId = resId;
        }

        @Override
        public int getCount() {
            return resId.size();
        }

        @Override
        public Object getItem(int position) {
            return resId.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SpinnerImage spImg = new SpinnerImage();

            if (convertView == null) {
                convertView = View.inflate(DeviceInfoActivity.this, R.layout.spinner_image_item, null);
                spImg.iv_spinner = (ImageView) convertView.findViewById(R.id.iv_spinner);
                convertView.setTag(spImg);
            } else {
                spImg = (SpinnerImage) convertView.getTag();
            }
            spImg.iv_spinner.setImageResource(
                    getResources().getIdentifier(resId.get(position), "drawable", getBaseContext().getPackageName()));

            return convertView;

        }

        class SpinnerImage {
            ImageView iv_spinner;

        }

    }

    /**********
     * 安防面板控制区域
     *******/
    public void setDisarmingModel(View v) {
        SyncMessage message1 = new SyncMessage();
        message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
        message1.setDeviceid(deviceInfo.getId());
        // 操作
        message1.setSyncBytes(new byte[]{(byte) 2});
        SyncMessageContainer.getInstance().produceSendMessage(message1);
    }

    public void setArmingModel(View v) {
        SyncMessage message1 = new SyncMessage();
        message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
        message1.setDeviceid(deviceInfo.getId());
        // 操作
        message1.setSyncBytes(new byte[]{(byte) 1});
        SyncMessageContainer.getInstance().produceSendMessage(message1);
    }

    public void setHomeModel(View v) {
        SyncMessage message1 = new SyncMessage();
        message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
        message1.setDeviceid(deviceInfo.getId());
        // 操作
        message1.setSyncBytes(new byte[]{(byte) 3});
        SyncMessageContainer.getInstance().produceSendMessage(message1);
    }

    public void setPanicModel(View v) {
        SyncMessage message1 = new SyncMessage();
        message1.setCommand(SyncMessage.CommandMenu.rq_control.value());
        message1.setDeviceid(deviceInfo.getId());
        // 操作
        message1.setSyncBytes(new byte[]{(byte) 4});
        SyncMessageContainer.getInstance().produceSendMessage(message1);
    }
    /******安防面板控制结束*********/
    /********************
     * 摄像头部分
     *********************/
    private TextView users;
    private RelativeLayout control_bottom;
    private Button choose_video_format, btn_play;
    private TextView video_mode_hd, video_mode_sd, video_mode_ld;
    private ImageView close_voice, send_voice, iv_half_screen, hungup, screenshot;
    private RelativeLayout r_p2pview, deviceinfo_suo;
    private ImageView voice_state;
    private ImageView iv_last, iv_next;
    private LinearLayout l_device_list, l_control, control_top, layout_voice_state, deviceinfo_info;
    private Contact mContact;
    private int callType = 3;
    public Context mContext;
    private boolean isReject = false;
    private boolean isRegFilter = false;
    private int ScrrenOrientation;
    private int window_width, window_height;
    private String callId = "1234567", password;
    private int connectType;
    private int defenceState = -1;//布防状态
    private boolean mIsCloseVoice = false;
    private int mCurrentVolume, mMaxVolume;
    public AudioManager mAudioManager;
    private boolean isSurpportOpenDoor = false;
    private boolean isShowVideo = false;
    private boolean isSpeak = false; //是否在对讲
    private int current_video_mode;
    private int screenWidth;
    private int screenHeigh;
    // 刷新监控部分
    private RelativeLayout rlPrgTxError;
    private TextView txError, tx_wait_for_connect;
    private Button btnRefrash;
    private ProgressBar progressBar;
    private HeaderView ivHeader;
    private String alarm_id = "";
    private String[] ipcList;
    private int number;
    private int currentNumber = 0;
    private boolean isShowDeviceList = false;
    List<TextView> devicelist = new ArrayList<TextView>();
    // 摇手机切换ipc
    private Vibrator vibrator;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener sensorListener;
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

    public void initSpeark(int deviceType, boolean isOpenDor) {
        if (deviceType != P2PValue.DeviceType.DOORBELL && !isOpenDor) {
            send_voice.setOnTouchListener(new OnTouchListener() {

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
                                    .setBackgroundResource(R.drawable.ic_send_audio_p);
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

    public void regFilter() {
        IntentFilter filter = new IntentFilter();
        //指令返回
        filter.addAction(Actions.SHOW_SERVER_MESSAGE);
        filter.addAction(Actions.CONNECTION_FAILED_SENDFAILED);
        filter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        filter.addAction(Constants.P2P.P2P_MONITOR_NUMBER_CHANGE);
        filter.addAction(Actions.CONTROL_BACK_MESSAGE);
        mContext.registerReceiver(mReceiver, filter);
    }

    public void regP2pFilter() {
        IntentFilter filter = new IntentFilter();
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
        mContext.registerReceiver(mP2pReceiver, filter);
        isRegFilter = true;
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {

            if (intent.getAction().equals(Actions.ACCETP_ONEDEVICE_MESSAGE)) {
                if(DeviceInfo.CaMenu.nbyg.value().equals(deviceInfo.getCa())
                        ||DeviceInfo.CaMenu.nbrqbjq.value().equals(deviceInfo.getCa())
                        ||DeviceInfo.CaMenu.wifiymq.value().equals(deviceInfo.getCa())){
                    loadNbYgInfo();//刷新一下nb烟感信息
                    loadWifiymq();//刷新一下烟雾器信息
                }
                String deviceId = null ;
                //昱川的卷闸门主机
                if((VersionType.CHANNEL_HZYCZN.equals(MainApplication.app.getAppGlobalConfig().getVersion())&&deviceInfo.getCa().equals(DeviceInfo.CaMenu.zhujijzm.value()))
                        ||DeviceInfo.CaMenu.nbyg.value().equals(deviceInfo.getCa())
                        ||DeviceInfo.CaMenu.nbrqbjq.value().equals(deviceInfo.getCa())
                        ||DeviceInfo.CaMenu.wifiymq.value().equals(deviceInfo.getCa())){
                    deviceId = intent.getStringExtra("zhuji_id");
                }else{
                    deviceId = intent.getStringExtra("device_id");
                }

                if (deviceId != null && deviceId.equals(String.valueOf(deviceInfo.getId()))) {
                    //返回指令操作成功
                    defHandler.removeMessages(7);
                    String data = (String) intent.getSerializableExtra("zhuji_info");
                    cancelInProgress();
                    if (isClick) {
                        ToastUtil.shortMessage(getString(R.string.rq_control_sendsuccess));
                    }
                    isClick = false;

                    /***收到服务器推送了，需要刷新按键显示状态**/
                    commandInfos = DatabaseOperator.getInstance(mContext).queryAllCommands(deviceInfo.getId());
                    keysSupportStateHeBin();
                    /*
                    * 由于长按ListView或者GridView的子View时，父View会进行事件拦截，下面解决了拦截，
                    * 但是这里由于通知数据改变倒置重新布局，因此会收到子View也会收到ACTION_CANCEL事件，
                    * 为了避免这种情况下的ACTION_CANCEL事件，决定不在这里通知数据改变，在子View收到UP事件后进行。
                    * */
                   // keyItemAdapter.notifyDataSetChanged();
                }
            } else if (intent.getAction().equals(Actions.SHOW_SERVER_MESSAGE)) {
                //返回指令操作失败
                defHandler.removeMessages(7);
                cancelInProgress();
                if (!isClick) return;
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(intent.getStringExtra("message"));
                } catch (Exception e) {
                    Log.w("DevicesList", "获取服务器返回消息，转换为json对象失败，用原始值处理");
                }
                if (resultJson != null) {
                    switch (resultJson.getIntValue("Code")) {
                        case 4:
                            ToastUtil.shortMessage(getString(R.string.tips_4));
                          //  Toast.makeText(DeviceInfoActivity.this, getString(R.string.tips_4), Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            ToastUtil.shortMessage(getString(R.string.tips_5));
                          //  Toast.makeText(DeviceInfoActivity.this, getString(R.string.tips_5), Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            ToastUtil.shortMessage(getString(R.string.tips_6));
                         //   Toast.makeText(DeviceInfoActivity.this, getString(R.string.tips_6), Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            ToastUtil.shortMessage(getString(R.string.tips_7));
                           // Toast.makeText(DeviceInfoActivity.this, getString(R.string.tips_7), Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            ToastUtil.shortMessage(getString(R.string.tips_8));
                         //   Toast.makeText(DeviceInfoActivity.this, getString(R.string.tips_8), Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            ToastUtil.shortMessage("Unknown Info");
                           // Toast.makeText(DeviceInfoActivity.this, "Unknown Info", Toast.LENGTH_SHORT).show();
                            break;
                    }

                } else {
                    Toast.makeText(DeviceInfoActivity.this, intent.getStringExtra("message"), Toast.LENGTH_SHORT)
                            .show();

                }
            } else if (Actions.CONNECTION_FAILED_SENDFAILED.equals(intent.getAction())) { //服务器未连接
                defHandler.removeMessages(7);
                cancelInProgress();
                Toast.makeText(DeviceInfoActivity.this, getString(R.string.rq_control_sendfailed),
                        Toast.LENGTH_SHORT).show();
            }else if (Actions.CONTROL_BACK_MESSAGE.equals(intent.getAction())) { // 控制返回
                //这里采用isResumed方法来判断Fragment是否出于resume状态
                defHandler.removeMessages(7);
                cancelInProgress();
                if (intent.getIntExtra("code",0) == SyncMessage.CodeMenu.rp_control_needconfirm.value()) { //需要授权
                    final String keyStr = intent.getStringExtra("data_info");
                    if(controlConfirmFlag){
                        controlConfirmFlag = false ;
                        defHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                defHandler.removeCallbacks(this);
                                if(controlConfrimAlert!=null&&controlConfrimAlert.isShowing()){
                                    return ;
                                }
                                cancelInProgress();
                                controlConfrimAlert = new AlertView(getString(R.string.warning), getString(R.string.deviceinfo_activity_wifiymq_send_tip), getString(R.string.cancel),
                                        new String[]{getString(R.string.sure) + "(20)"}, null, mContext, AlertView.Style.Alert,
                                        new OnItemClickListener() {
                                            @Override
                                            public void onItemClick(Object o, final int position) {
                                                if (position != -1) {
                                                    if(bgKeyView!=null){
                                                        bgKeyView.setImageResource(R.drawable.yixunge_regfog);
                                                    }
                                                    yixungeControl(keyStr);
                                                }
                                            }
                                        });
                                LinearLayout loAlertButtons = (LinearLayout) controlConfrimAlert.getContentContainer().findViewById(R.id.loAlertButtons);
                                TextView textView = (TextView) loAlertButtons.getChildAt(2).findViewById(R.id.tvAlert); //获取到按钮
                                textView.setTextColor(getResources().getColor(R.color.red));
                                if(controlConfrimAlert!=null&&!controlConfrimAlert.isShowing()){

                                    controlConfrimAlert.show();
                                }
                                defHandler.sendMessageDelayed(defHandler.obtainMessage(dHandler_daojishi, 19, 0), 1000);//改变倒计时
                            }
                        },1000);
                    }
                }else if(intent.getIntExtra("code",0) == SyncMessage.CodeMenu.rp_control_verifyerror.value()){ //密码校验失败
                    //弹出提示框
                    cancelInProgress();//隐藏进度条
                    //ViewGroup频繁的remove会导致空指针异常.
                    if(mAnBaForceAlertTip!=null&&!mAnBaForceAlertTip.isShowing()){
                        defHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(mAnBaForceAlertTip!=null&&!mAnBaForceAlertTip.isShowing()){
                                    mAnBaForceAlertTip.show();
                                }
                            }
                        },300);
                    }

                }
            }
        }
    };
    BroadcastReceiver mP2pReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {

            if (intent.getAction().equals(
                    Constants.P2P.P2P_MONITOR_NUMBER_CHANGE)) {
                int number = intent.getIntExtra("number", -1);
                if (number != -1) {
                    if (users == null)
                        users = (TextView) findViewById(R.id.users);
                    users.setText(getResources().getString(
                            R.string.monitor_number)
                            + " " + P2PConnect.getNumber());
                }
            } else if (intent.getAction().equals(Constants.P2P.P2P_READY)) {
                P2PHandler.getInstance().getDefenceStates(callId, password,MainApplication.GWELL_LOCALAREAIP);
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
                if (StringUtils.isEmpty(deviceInfo.getBipc()) || "0".equals(deviceInfo.getBipc()))
                    return;
                String error = intent.getStringExtra("error");
                int code = intent.getIntExtra("code", 9);
                showError(error, code);
                isShake = false;
                iv_last.setClickable(true);
                iv_next.setClickable(true);
            } else if (intent.getAction().equals(Constants.P2P.RET_GET_REMOTE_DEFENCE)) {
                //" + "4");
                String ids = intent.getStringExtra("contactId");
                if (!ids.equals("") && ids.equals(callId)) {
                    defenceState = intent.getIntExtra("state", -1);
//                    changeDefence(defenceState);
                }
//                defence_state.setVisibility(ImageView.VISIBLE);
            } else if (intent.getAction().equals(Constants.P2P.RET_SET_REMOTE_DEFENCE)) {
                if (StringUtils.isEmpty(deviceInfo.getBipc()) || "0".equals(deviceInfo.getBipc()))
                    return;
                int result = intent.getIntExtra("state", -1);
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
//                changeDefence(defenceState);
            } else if (intent.getAction().equals(Constants.P2P.P2P_READY)) {

                Log.e("monitor", "P2P_READY" + "callId=" + callId);
                P2PHandler.getInstance().getDefenceStates(callId, password,MainApplication.GWELL_LOCALAREAIP);
                isReceveHeader = false;
                isShake = false;
                iv_last.setClickable(true);
                iv_next.setClickable(true);
                pView.sendStartBrod();
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
                    if(r_p2pview!=null){
                        r_p2pview.setLayoutParams(parames);
                    }
                }
            }
        }
    };

//    public void changeDefence(int defencestate) {
//        if (defenceState == -1) return;
//        if (defencestate == Constants.DefenceState.DEFENCE_STATE_OFF) {
//            defence_state.setImageResource(R.drawable.deployment);
//        } else {
//
//            defence_state.setImageResource(R.drawable.disarm);
//        }
//    }

    /**
     * 隐藏过度页
     */
    private void hindRlProTxError() {
        rlPrgTxError.setVisibility(View.GONE);
        control_bottom.setVisibility(View.VISIBLE);
    }

    private void yixungeControl(String keyStr){
        //发送控制
        showInProgress(getString(R.string.cld_send_ing));
        SyncMessage message = new SyncMessage();
        message.setCommand(SyncMessage.CommandMenu.rq_controlConfirm.value());
        message.setDeviceid(deviceInfo.getZj_id());
        // 操作 这里的操作写死，需要在服务器的配置页面中配置此按钮对应的指令是什么
        message.setSyncBytes(keyStr.getBytes());
        SyncMessageContainer.getInstance().produceSendMessage(message);
        defHandler.sendEmptyMessageDelayed(7, 10000);//10秒超时
        controlConfrimAlert.dismiss();
    }

    private void showRlProTxError() {
        ObjectAnimator anima = ObjectAnimator.ofFloat(rlPrgTxError, "alpha",
                0f, 1.0f);
        control_bottom.setVisibility(View.GONE);
        rlPrgTxError.setVisibility(View.VISIBLE);
        anima.setDuration(500).start();
        anima.addListener(new AnimatorListener() {

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
        if (!isPermission) {
            T.showShort(mContext, R.string.insufficient_permissions);
            return;
        }
        if (defenceState == Constants.DefenceState.DEFENCE_STATE_ON) {
            P2PHandler.getInstance().setRemoteDefence(mContact.getContactId(),
                    password,
                    Constants.P2P_SET.REMOTE_DEFENCE_SET.ALARM_SWITCH_OFF,MainApplication.GWELL_LOCALAREAIP);
        } else if (defenceState == Constants.DefenceState.DEFENCE_STATE_OFF) {
            P2PHandler.getInstance().setRemoteDefence(mContact.getContactId(),
                    password,
                    Constants.P2P_SET.REMOTE_DEFENCE_SET.ALARM_SWITCH_ON,MainApplication.GWELL_LOCALAREAIP);
        }
    }

    public void callDevice() {
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
                    AppConfig.VideoMode, mContact.contactId,MainApplication.GWELL_LOCALAREAIP);
        } else if (connectType == Constants.ConnectType.P2PCONNECT) {
            callType = 1;
            String ipAdress = FList.getInstance().getCompleteIPAddress(
                    mContact.contactId);
            P2PHandler.getInstance().call(NpcCommon.mThreeNum, password, true,
                    Constants.P2P_TYPE.P2P_TYPE_MONITOR, callId, ipAdress,
                    push_mesg, AppConfig.VideoMode, mContact.contactId,MainApplication.GWELL_LOCALAREAIP);
        }
    }

    private void playReady() {
        Intent ready = new Intent();
        ready.setAction(Constants.P2P.P2P_READY);
        this.sendBroadcast(ready);
    }

    public void setControlButtomHeight(int height) {
        LinearLayout.LayoutParams control_bottom_parames = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        control_bottom_parames.height = height;
        control_bottom.setLayoutParams(control_bottom_parames);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
//        //
//        super.onConfigurationChanged(newConfig);
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            ScrrenOrientation = Configuration.ORIENTATION_LANDSCAPE;
//            l_control.setVisibility(View.GONE);
//            int height = (int) getResources().getDimension(
//                    R.dimen.p2p_monitor_bar_height);
//            control_bottom.setVisibility(View.VISIBLE);
//            pView.fullScreen();
//            isFullScreen = true;
//        } else {
//            ScrrenOrientation = Configuration.ORIENTATION_PORTRAIT;
//            l_control.setVisibility(View.VISIBLE);
//            control_bottom.setVisibility(View.INVISIBLE);
//            control_top.setVisibility(View.GONE);
//            if (isFullScreen) {
//                isFullScreen = false;
//                pView.halfScreen();
//                Log.e("half", "half screen--");
//            }
//        }
//    }
        super.onConfigurationChanged(newConfig);
        if(CameraInfo.CEnum.jiwei.value().equals(cameraPaiZi)){
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ScrrenOrientation = Configuration.ORIENTATION_LANDSCAPE;
//            layout_title.setVisibility(View.GONE);
                l_control.setVisibility(View.GONE);
//            rl_control.setVisibility(View.GONE);
//            viewpager.setVisibility(View.GONE);
                // 设置control_bottom的高度
                int height = (int) getResources().getDimension(
                        R.dimen.p2p_monitor_bar_height);
                setControlButtomHeight(height);
                control_bottom.setVisibility(View.VISIBLE);
                // setIsLand(true);
                pView.fullScreen();
                isFullScreen = true;
                LinearLayout.LayoutParams parames = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                r_p2pview.setLayoutParams(parames);
            } else {
                ScrrenOrientation = Configuration.ORIENTATION_PORTRAIT;
//            layout_title.setVisibility(View.VISIBLE);
                l_control.setVisibility(View.VISIBLE);
//            rl_control.setVisibility(View.VISIBLE);
//            viewpager.setVisibility(View.VISIBLE);
                // 设置control_bottom的高度等于0
                setControlButtomHeight(0);
                control_bottom.setVisibility(View.INVISIBLE);
                control_top.setVisibility(View.GONE);
                // setIsLand(false);
                if (isFullScreen) {
                    isFullScreen = false;
                    pView.halfScreen();
//				Log.e("half", "half screen--");
                }
                if (P2PView.type == 1) {
                    if (P2PView.scale == 0) {
                        int Heigh = screenWidth * 3 / 4;
                        LinearLayout.LayoutParams parames = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        parames.height = Heigh;
                        r_p2pview.setLayoutParams(parames);
                    } else {
                        int Heigh = screenWidth * 9 / 16;
                        LinearLayout.LayoutParams parames = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        parames.height = Heigh;
                        r_p2pview.setLayoutParams(parames);
                    }
                } else {
                    if (mContact.contactType == P2PValue.DeviceType.NPC) {
                        int Heigh = screenWidth * 3 / 4;
                        LinearLayout.LayoutParams parames = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        parames.height = Heigh;
                        r_p2pview.setLayoutParams(parames);
                    } else {
                        int Heigh = screenWidth * 9 / 16;
                        LinearLayout.LayoutParams parames = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        parames.height = Heigh;
                        r_p2pview.setLayoutParams(parames);
                    }
                }
            }
        }

    }

    public void updateVideoModeText(int mode) {
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

    /*
         * 初始化P2pview
         */
//    public void initp2pView() {
//        this.initP2PView(mContact.contactType);
//        WindowManager manager = getWindowManager();
//        window_width = manager.getDefaultDisplay().getWidth();
//        window_height = manager.getDefaultDisplay().getHeight();
//        this.initScaleView(this, window_width, window_height);
//        setMute(true);
//    }
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
        reject();
        super.onBackPressed();
    }

    /*@Override
    public void onDestroy() {
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    mCurrentVolume, 0);
        }
        if (isRegFilter) {
            mContext.unregisterReceiver(mReceiver);
            isRegFilter = false;
        }
        P2PConnect.setPlaying(false);
        P2PConnect.setMonitorId("");// 设置在监控的ID为空
        SettingListener.setMonitorID("");
        if (sensorListener != null) {
            sensorManager.unregisterListener(sensorListener);
        }
        *//*if (!activity_stack
                .containsKey(Constants.ActivityInfo.ACTIVITY_MAINACTIVITY)) {
			Intent i = new Intent(this, MainActivity.class);
			this.startActivity(i);
		}*//*
        Intent refreshContans = new Intent();
		refreshContans.setAction(Constants.Action.REFRESH_CONTANTS);
		mContext.sendBroadcast(refreshContans);


		super.onDestroy();
	}
*/
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
                close_voice.setBackgroundResource(R.drawable.m_voice_on);
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
                if (close_voice != null) {
                    close_voice.setBackgroundResource(R.drawable.m_voice_off);
                }
            }

            return false;
        }

        return super.dispatchKeyEvent(event);
    }

    GridView gridView;

    @Override
    public void onClick(View v) {
        //
        switch (v.getId()) {
            //锁头图标的点击事件
            case R.id.iv_suo:
                iv_annima.setVisibility(View.VISIBLE);
                Animation operatingAnim = AnimationUtils.loadAnimation(mContext, R.anim.tip);
                LinearInterpolator lin = new LinearInterpolator();
                operatingAnim.setInterpolator(lin);
                if (operatingAnim != null) {
                    iv_annima.startAnimation(operatingAnim);
                }
                defHandler.sendEmptyMessageDelayed(1, 2000);

                break;
            case R.id.menu_tv:
                if (tv_menu.getText().toString().equals(getString(R.string.action_settings))) {
                    menuWindow.showAtLocation(v, Gravity.TOP | Gravity.RIGHT, 0,
                            Util.dip2px(getApplicationContext(), 55) + Util.getStatusBarHeight(this));
                } else {
                    if (isEditKey) {
                        if (!NetworkUtils.CheckNetwork(mContext)) {
                            Toast.makeText(mContext.getApplicationContext(), getString(R.string.net_error_nonet), Toast.LENGTH_SHORT).show();
                            return; //没网的时候返回，不可操作
                        }
                        showInProgress(getString(R.string.operationing), false, true);
                        isEditKey = false;
                        saveModify();
                        if (isShowCamera) {
                            if(r_p2pview!=null){
                                r_p2pview.setVisibility(View.VISIBLE);
                            }
                        }
                        //是否是智能锁
                        if (isShowSuo) {
                            rl_suo.setVisibility(View.VISIBLE);
                        }
                    }

                }
                break;
            case R.id.pop_edit:
                menuWindow.dismiss();
                isEditKey = true;
                tv_menu.setText(getString(R.string.save));
                if (editItemAdapter == null) {
                    editItemAdapter = new EditKeyItemAdapter(this);
                    gridView.setAdapter(editItemAdapter);
                } else {
                    editItemAdapter.notifyDataSetChanged();
                }
                keysgGridView.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);
                if (isShowCamera) {
                    if(r_p2pview!=null){
                        r_p2pview.setVisibility(View.GONE);
                    }
                }
                //是否是智能锁
                if (isShowSuo) {
                    rl_suo.setVisibility(View.GONE);
                }
                break;
            case R.id.pop_shock:
                key = 1;
                dcsp.putInt(Constant.BTN_CONTROLSTYLE, key).commit();
                menuWindow.dismiss();
                break;
            case R.id.pop_voiced:
                key = 2;
                dcsp.putInt(Constant.BTN_CONTROLSTYLE, key).commit();
                menuWindow.dismiss();
                break;
            case R.id.pop_silent:
                key = 0;
                dcsp.putInt(Constant.BTN_CONTROLSTYLE, key).commit();
                menuWindow.dismiss();
                break;
            case R.id.pop_voice_and_shock:
                key = 3;
                dcsp.putInt(Constant.BTN_CONTROLSTYLE, key).commit();
                menuWindow.dismiss();
                break;
            case R.id.pop_history:
                menuWindow.dismiss();
                Intent intent = new Intent();
                intent.setClass(DeviceInfoActivity.this, DeviceInfoHistoryActivity.class);
                intent.putExtra("device", deviceInfo);
                intent.putExtra("group", (DeviceInfo) getIntent().getSerializableExtra("group"));
                intent.putExtra("camera", (Contact) getIntent().getSerializableExtra("camera"));
                intent.putExtra("contact", mContact);

                intent.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
                startActivity(intent);
                //退出前先处理视频
//				reject();
//				finish();
                break;
            /******************摄像头***********************/
            /*区分雄迈与技威*/
            case R.id.btn_play:
                if(CameraInfo.CEnum.jiwei.value().equals(cameraPaiZi)){
                    //技威
                    tx_wait_for_connect.setVisibility(View.VISIBLE);
                    btn_play.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    initCameraCreate();
                    initIpc = true;
                    initp2pView();
                }else if(CameraInfo.CEnum.xiongmai.value().equals(cameraPaiZi)){
                    //雄迈

                }
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
            default:
                break;
        }
    }

    // 设置成对话状态
    private void speak() {
        hideVideoFormat();
        layout_voice_state.setVisibility(RelativeLayout.VISIBLE);
        send_voice.setBackgroundResource(R.drawable.ic_send_audio_p);
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
                            cmd,MainApplication.GWELL_LOCALAREAIP);
                } else {
                    P2PHandler.getInstance().setGPIO1_0(callId, password,MainApplication.GWELL_LOCALAREAIP);
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
        if (StringUtils.isEmpty(deviceInfo.getBipc()) || "0".equals(deviceInfo.getBipc())) return;
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
            if (!com.smartism.znzk.util.StringUtils.isEmpty(MainApplication.app.getAppGlobalConfig().getAPPID())) {
                P2PHandler.getInstance().finish();
                disconnectDooranerfa();
            }
        }
    }

    public void readyCallDevice() {
        if (connectType == Constants.ConnectType.P2PCONNECT) {
            P2PHandler.getInstance().openAudioAndStartPlaying(1);
            P2PHandler.getInstance().getDefenceStates(callId, password,MainApplication.GWELL_LOCALAREAIP);
        } else {
            P2PHandler.getInstance().openAudioAndStartPlaying(1);
            callId = "1";
            password = "0";
            P2PHandler.getInstance().getDefenceStates(callId, password,MainApplication.GWELL_LOCALAREAIP);
        }

    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN && initIpc) {

            if ((System.currentTimeMillis() - exitTime) > 2000) {
                T.showShort(this, R.string.press_again_monitor);
                exitTime = System.currentTimeMillis();
            } else {
//                toGroupInfoActivity();

                reject();
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void changevideoformat() {
        if (control_top.getVisibility() == RelativeLayout.VISIBLE) {
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
    NormalDialog dialog;
    String contactidTemp = "";

    private void NewMessageDialog(String Meassage, final String contacid,
                                  boolean isSurportdelete) {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
        dialog = new NormalDialog(mContext);
        dialog.setContentStr(Meassage);
        dialog.setbtnStr1(R.string.check);
        dialog.setbtnStr2(R.string.cancel);
        dialog.setbtnStr3(R.string.clear_bundealarmid);
        dialog.showAlarmDialog(isSurportdelete, contacid);
        dialog.setOnAlarmClickListner(AlarmClickListner);
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
        dialog = new NormalDialog(mContext, mContext.getResources().getString(
                R.string.clear_bundealarmid), mContext.getResources()
                .getString(R.string.clear_bundealarmid_tips), mContext
                .getResources().getString(R.string.sure), mContext
                .getResources().getString(R.string.cancel));
        dialog.setOnButtonOkListener(new NormalDialog.OnButtonOkListener() {

            @Override
            public void onClick() {
                P2PHandler.getInstance().DeleteDeviceAlarmId(
                        String.valueOf(alarmId),MainApplication.GWELL_LOCALAREAIP);
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

    boolean isStartPlay = false;

    @Override
    protected void onResume() {
        super.onResume();

        if (isCreatP2P) {
            isCreatP2P = false;
        }
        if (!StringUtils.isEmpty(deviceInfo.getBipc()) && !"0".equals(deviceInfo.getBipc())) {
            //判断是否是技威
            if(!CameraInfo.CEnum.jiwei.value().equals(cameraPaiZi)) return;
            if (mContact == null) return;
            readyCallDevice();
            initp2pView();
        }

        //易迅格
        if(keyItemAdapter!=null){
            keyItemAdapter.notifyDataSetChanged();
        }

        showMoreDeviceAttributes();
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

    public void showMoreDeviceAttributes(){
        if (!CollectionsUtils.isEmpty(commandInfos)){
            for (CommandInfo commandInfo : commandInfos){
                if (CommandInfo.CommandTypeEnum.temperature.value().equalsIgnoreCase(commandInfo.getCtype())){

                }
            }
        }
    }

    public void initIpcDeviceList() {
        // if(number<0){
        // number=0;
        // }
        /*LayoutParams p = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		p.height = dip2px(mContext, 40 * number);
		l_device_list.setLayoutParams(p);*/
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
                    defHandler.sendMessage(msg);
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
                    cmd_connect,MainApplication.GWELL_LOCALAREAIP);
        }
    }

    public void disconnectDooranerfa() {
        if (isCustomCmdAlarm == true) {
            String cmd_disconnect = "IPC1anerfa:disconnect";
            P2PHandler.getInstance().sendCustomCmd(callId, password,
                    cmd_disconnect,MainApplication.GWELL_LOCALAREAIP);
        }
    }

    private AlertView controlConfrimAlert;//控制确认提示框
    private EditText etName; //催泪密码
    private AlertView mAlertViewExt;//催泪密码
    private  InputMethodManager imm  ;
    private AlertView mAnBaForceAlertTip ; //安霸禁用提示框
    private boolean controlConfirmFlag = false ;
    private void wifiymqDriveAway(long zhujiId){

        if(zhujiInfo==null){
            return ;
        }
        new LoadZhujiAndDeviceTask().queryAllCommandInfo(zhujiId, new LoadZhujiAndDeviceTask.ILoadResult<List<CommandInfo>>() {
            @Override
            public void loadResult(List<CommandInfo> result) {
                String commandPwds = null ;
                if(result!=null){
                    for(CommandInfo commandInfo :result){
                        if(commandInfo.getCtype().equals("pwd_control")){
                            commandPwds = commandInfo.getCommand();
                            break ;
                        }
                    }
                }


                if (TextUtils.isEmpty(commandPwds)) {//未设置密码
                    new AlertView(getString(R.string.activity_weight_notice), zhujiInfo.isAdmin() ? getString(R.string.abbq_ges_pwd_nomianji) : getString(R.string.abbq_ges_pwd_nomianjinoadmin), getString(R.string.sure),
                            null, null, DeviceInfoActivity.this, AlertView.Style.Alert, null).show();
                } else{
                    final String finalCommandPwds = commandPwds;
                    mAlertViewExt = new AlertView(null, getString(R.string.abbq_cld_pwd_title), getString(R.string.cancel), null, new String[]{getString(R.string.sure)}, mContext, AlertView.Style.Alert, new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int position) {
                            if (position != -1){ //确认密码
                                if (etName.getText().length() != 6){
                                    Toast.makeText(mContext,R.string.abbq_update_cld_pwd_length,Toast.LENGTH_SHORT).show();
                                }else if (etName.getText().toString().equals(finalCommandPwds)) {
                                    // 操作
                                    controlConfirmFlag = true ;
                                    showInProgress(getString(R.string.operationing));
                                    defHandler.sendMessage(defHandler.obtainMessage(12, position, 3,showKeys.get(position)));
                                }else{
                                    Toast.makeText(mContext,R.string.password_error,Toast.LENGTH_SHORT).show();
                                }
                            }
                            //关闭软键盘
                            imm.hideSoftInputFromWindow(etName.getWindowToken(), 0);
                            //恢复位置
                            mAlertViewExt.setMarginBottom(0);
                        }
                    });
                    ViewGroup extView = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.alert_password_form, null);
                    etName = (EditText) extView.findViewById(R.id.etName);
                    CheckBox cbLaws = (CheckBox) extView.findViewById(R.id.cbLaws);
                    etName.setText("");
                    etName.setHint(getString(R.string.abbq_cld_pwd_title));
                    etName.setInputType(InputType.TYPE_CLASS_NUMBER
                            | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                    cbLaws.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            String psw = etName.getText().toString();

                            if (isChecked) {
                                etName.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
                            } else {
                                etName.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                            }
                            etName.setSelectAllOnFocus(true);
                            etName.setSelection(psw.length());
                        }
                    });
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

            }
        });


    }

    private String vkey;

    class LoadKey implements Runnable {

        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", deviceInfo.getId());
            JSONArray array = new JSONArray();
//        for ()
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("vkey", "id_jj_zns");
            array.add(jsonObject);
            object.put("vkeys", array);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/d/p/list", object,
                    DeviceInfoActivity.this);
            if ("-3".equals(result)) {
                cancelInProgress();
                defHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(DeviceInfoActivity.this, getString(R.string.history_response_nodevice),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if (result.length() > 4) {
                cancelInProgress();
                JSONArray resultJson = null;
                try {
                    resultJson = JSONArray.parseArray(result);
                } catch (Exception e) {
                    return;
                }


                if (resultJson != null && !resultJson.isEmpty()) {
                    vkey = (String) resultJson.getJSONObject(0).get("value");
                }
            }
        }
    }

    /**********************技威摄像头部分结束*******************************/
}
