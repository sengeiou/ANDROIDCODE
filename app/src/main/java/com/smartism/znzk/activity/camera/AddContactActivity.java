package com.smartism.znzk.activity.camera;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.macrovideo.sdk.tools.DeviceScanner;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.DeviceMainActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.db.camera.DataManager;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.domain.camera.LocalDevice;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.global.VList;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunSupport;
import com.smartism.znzk.xiongmai.lib.funsdk.support.OnFunDeviceListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.OnFunDeviceOptListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevStatus;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevType;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.smartism.znzk.util.DataCenterSharedPreferences.Constant.SECURITY_SETTING_PWD;

/**
 * 手动添加摄像头
 *
 * @author 王建 update 2016年08月05日
 */
public class AddContactActivity extends BaseActivity implements OnClickListener, OnFunDeviceListener {
    private TextView mNext;
    private ImageView mBack;
    private ZhujiInfo info;
    private Button search;
    private RelativeLayout rl_id, rl_name, rl_pwd, rl_search, rl_ensrue;
    private int i;
    private int isCameraList;//判断是否是返回摄像头列表还是返回设备列表 0、返回设备列表，1、返回摄像头列表
    Context mContext;
    EditText contactId;
    Contact mContact;
    Button ensure;
    EditText input_device_id, input_device_name, input_device_password;
    Contact saveContact = new Contact();
    private String result;
    private boolean isRegFilter = false;
    private boolean isCamera = false;
    private boolean isMainList = false;//是否是主机列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        i = getIntent().getIntExtra("int", 0);//获取摄像头类型
        isCameraList = getIntent().getIntExtra("isCameraList", 0);
        isMainList = getIntent().getBooleanExtra("isMainList", false);
        List<LocalDevice> localDevices = FList.getInstance()
                .getLocalDevices();
        mContact = (Contact) getIntent().getSerializableExtra("contact");
        mContext = this;
        initCompent();
//        info = DatabaseOperator.getInstance(getApplicationContext())
//                .queryDeviceZhuJiInfo(DataCenterSharedPreferences
//                        .getInstance(MainApplication.app, Constant.CONFIG)
//                        .getString(Constant.APP_MASTERID, ""));
        //替换
        info = DatabaseOperator.getInstance(getApplicationContext()).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
        if (info == null){
            info = new ZhujiInfo();
        }
        String path = Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera";
        File dirFile = new File(path);
        if (dirFile.exists()) {
        }

        //雄迈设置监听器
        FunSupport.getInstance().registerOnFunDeviceListener(this);
        //查询所有的主机
        new LoadZhujiAndDeviceTask().queryZhujiInfos(new LoadZhujiAndDeviceTask.ILoadResult<List<ZhujiInfo>>() {
            @Override
            public void loadResult(List<ZhujiInfo> result) {
                mZhujiInfos = result ;
            }
        });

        if(!MainApplication.app.getAppGlobalConfig().isShowAddXMCamera()){
            //不支持雄迈，设置只能输入数字id
            input_device_id.setInputType(InputType.TYPE_CLASS_NUMBER);
        }else{
            if(i==9&&mContact!=null){
               // rl_pwd.setVisibility(View.GONE);
                input_device_id.setText(mContact.contactId);
                input_device_id.setEnabled(false);
            }
        }
    }
    List<ZhujiInfo> mZhujiInfos ;

    @Override
    public void onDeviceListChanged() {

    }

    @Override
    public void onDeviceStatusChanged(FunDevice funDevice) {
        if (funDevice.devStatus == FunDevStatus.STATUS_ONLINE) {
            // 如果设备在线,是个真的设备，添加进去
            addDevice(null);
        }else{
            cancelInProgress();
            ToastTools.short_Toast(this, getResources().getString(R.string.camera_off));
        }
    }

    @Override
    public void onDeviceAddedSuccess() {

    }

    @Override
    public void onDeviceAddedFailed(Integer errCode) {

    }

    @Override
    public void onDeviceRemovedSuccess() {

    }

    @Override
    public void onDeviceRemovedFailed(Integer errCode) {

    }

    @Override
    public void onAPDeviceListChanged() {

    }

    @Override
    public void onLanDeviceListChanged() {

    }



    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    cancelInProgress();
                    T.showShort(mContext, R.string.time_out);
                    break;
                case 1:
                    //完成服务器添加，再将数据保存在本地
                    defaultHandler.removeMessages(10);
                    if(i!=9) {
                        FList.getInstance().insert(saveContact);
                        FList.getInstance().updateLocalDeviceWithLocalFriends();
                    }
                    T.showShort(mContext, R.string.add_success);
                    //解决
//				sendSuccessBroadcast();
                    String flag = "";
                    if (i == 3) {
                        flag = "jiwei";
                        Intent createPwdSuccess = new Intent();
                        createPwdSuccess.setAction(Constants.Action.RADAR_SET_WIFI_SUCCESS);
                        mContext.sendBroadcast(createPwdSuccess);
//                        Intent intent = new Intent();
//                        if (isCameraList == 1) {
//                            intent.setClass(mContext, MainActivity.class);
//                        } else {
//                            intent.setClass(mContext, DeviceMainActivity.class);
//                            intent.putExtra("isNotCamera", false);    //跳转之后会提示更新，为了避免从摄像头列表跳转弹出，添加一个判断
//                        }
//                        startActivity(intent);
                        SyncMessageContainer.getInstance().produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                        finish();
                        return true;
                    }else if(i==9){
                        Intent intents = new Intent();
                        intents.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intents.setClass(mContext, DeviceMainActivity.class);
                        startActivity(intents);
                        SyncMessageContainer.getInstance().produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                        finish();
                    } else {
                        flag = "v380";
                        Intent createPwdSuccess = new Intent();
                        createPwdSuccess.setAction(Constants.Action.ACTIVITY_FINISH);
                        mContext.sendBroadcast(createPwdSuccess);
                        Intent intents = new Intent();
                        intents.setClass(mContext, MainActivity.class);
                        startActivity(intents);
                    }

		/*		this.postDelayed(new Runnable() {
                    @Override
					public void run() {
						Intent intent = new Intent();
						intent.setClass(mContext, MainActivity.class);
						intent.putExtra("device", deviceInfo);
						startActivity(intent);

					}
				},2*1000);*/
                    finish();

                    break;
                case 0:
                    com.macrovideo.sdk.custom.DeviceInfo info = (com.macrovideo.sdk.custom.DeviceInfo) msg.obj;
                    Log.e("摄像头", info.getnDevID() + "");
                    addDevice(info);
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    public void initCompent() {
  //      contactId = (EditText) findViewById(R.id.input_device_id);
        search = (Button) findViewById(R.id.search);
        mBack = (ImageView) findViewById(R.id.back_btn);
        mNext = (TextView) findViewById(R.id.next);
        ensure = (Button) findViewById(R.id.bt_ensure);
        input_device_id = (EditText) findViewById(R.id.input_device_id);
        input_device_name = (EditText) findViewById(R.id.input_contact_name);
        input_device_password = (EditText) findViewById(R.id.input_contact_pwd);
        rl_id = (RelativeLayout) findViewById(R.id.rl_id);
        rl_name = (RelativeLayout) findViewById(R.id.rl_name);
        rl_pwd = (RelativeLayout) findViewById(R.id.rl_pwd);
        rl_search = (RelativeLayout) findViewById(R.id.rl_search);
        rl_ensrue = (RelativeLayout) findViewById(R.id.rl_ensrue);
        input_device_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        mBack.setOnClickListener(this);
        mNext.setOnClickListener(this);
        ensure.setOnClickListener(this);
        search.setOnClickListener(this);
//        if (i == 5) {
//            rl_search.setVisibility(View.VISIBLE);
//            rl_id.setVisibility(View.GONE);
//            rl_name.setVisibility(View.GONE);
//            rl_pwd.setVisibility(View.GONE);
//            rl_ensrue.setVisibility(View.GONE);
//        } else if (i == 3) {
//            rl_search.setVisibility(View.GONE);
//            rl_id.setVisibility(View.VISIBLE);
//            rl_name.setVisibility(View.VISIBLE);
//            rl_pwd.setVisibility(View.VISIBLE);
//            rl_ensrue.setVisibility(View.VISIBLE);
//        }

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.back_btn:
                this.finish();
                break;
            case R.id.next:
                next();
                break;
            case R.id.bt_ensure:
                i = judgeXiongMaiAndJiwei();
                if(i==9){
                    nextXM();
                }else{
                    next();
                }
                break;
            case R.id.search:
//			Log.e("摄像头", "准备连接");
                StartSearchDevice();
                break;
            default:
                break;
        }
    }
    // 开始设备搜索
    public boolean StartSearchDevice() {
        Log.e("摄像头", "准备连接1");
        closeMulticast();
        openMulticast();
        new DeviceSearchThread(1).start();
        return true;

    }

    public void closeMulticast() {
        if (multicastLock != null) {
            multicastLock.release();
            multicastLock = null;
        }

    }

    public void openMulticast() {
        try {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            multicastLock = wifiManager.createMulticastLock("multicast");
            multicastLock.acquire();
        } catch (Exception e) {
            // @@System.out.println("openMulticast error");//add for test
            multicastLock = null;
        }
    }

    MulticastLock multicastLock = null;
    DeviceInfo deviceInfo = null;

    // 设备搜索线程
    public class DeviceSearchThread extends Thread {

        static final int MAX_DATA_PACKET_LENGTH = 128;

        private byte buffer[] = new byte[MAX_DATA_PACKET_LENGTH];

        private int nTreadSearchID = 0;

        public DeviceSearchThread(int nSearchID) {
            nTreadSearchID = nSearchID;

        }

        public void run() {
            Log.e("摄像头", "准备连接2");
            System.out.println("DeviceSearchThread: run ");// add for test
            com.macrovideo.sdk.custom.DeviceInfo info = null;
            ArrayList<com.macrovideo.sdk.custom.DeviceInfo> resultList = DeviceScanner.getDeviceListFromLan();
            if (resultList != null && resultList.size() > 0) {
                for (int i = 0; i < resultList.size(); i++) {
                    info = resultList.get(i);
                    VList.getInstance().insert(info);
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = info;
                    defaultHandler.sendMessage(msg);
                }
                Intent in = new Intent();
                in.setClass(mContext, DeviceMainActivity.class);
                // in.putExtra("v380", "v380");
                startActivity(in);
                finish();
            }

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeMulticast();
        if(MainApplication.app.getAppGlobalConfig().isShowAddXMCamera()){
            FunSupport.getInstance().removeOnFunDeviceListener(this);
        }
    }

    public void next() {
        String input_id = input_device_id.getText().toString();
        String input_name = input_device_name.getText().toString();
        String input_pwd = input_device_password.getText().toString();
        if (input_id != null && input_id.trim().equals("")) {
            T.showShort(mContext, R.string.input_contact_id);
            return;
        }
        if (input_id.charAt(0) == '0' || input_id.length() > 9 || !Utils.isNumeric(input_id)) {
            T.show(mContext, R.string.device_id_invalid, Toast.LENGTH_SHORT);
            return;
        }

        List<DeviceInfo> deviceInfos = DatabaseOperator.getInstance(AddContactActivity.this).queryAllDeviceInfos(info.getId());
        for (DeviceInfo d : deviceInfos) {
            if (d.getCak().equals("surveillance")) {
                deviceInfo = d;
                break;
            }
        }
        List<CameraInfo> camera = null;
        if (deviceInfo != null && deviceInfo.getCak().equals("surveillance")) {
            camera = (List<CameraInfo>) JSON.parseArray(deviceInfo.getIpc(), CameraInfo.class);
        }
        if (null != FList.getInstance().isContact(input_id)) {
            if (camera != null && camera.size() > 0) {
                for (int i = 0; i < camera.size(); i++) {
                    if (camera.get(i).getId().equals(input_id)) {
                        T.showShort(mContext, R.string.contact_already_exist);
                        return;
                    }
                }

            }


        }
        int type;
        if (input_id.charAt(0) == '0') {
            type = P2PValue.DeviceType.PHONE;
        } else {
            type = P2PValue.DeviceType.UNKNOWN;
        }
        if (input_name != null && input_name.trim().equals("")) {

            T.showShort(mContext, R.string.input_contact_name);
            return;
        }
        saveContact.contactId = input_id;
        saveContact.contactType = type;
        saveContact.activeUser = NpcCommon.mThreeNum;
        saveContact.messageCount = 0;
        List<Contact> lists = DataManager.findContactByActiveUser(mContext, NpcCommon.mThreeNum);

        for (Contact c : lists) {
            if (c.contactName.equals(input_name)) {
                if (camera != null && camera.size() > 0) {
                    for (int i = 0; i < camera.size(); i++) {
                        if (camera.get(i).getN().equals(input_name)) {
                            T.showShort(mContext, R.string.device_name_exist);
                            return;
                        }
                    }
                }
            }

        }
        if (input_pwd == null || input_pwd.trim().equals("")) {
            T.showShort(this, R.string.input_password);
            return;
            // input_pwd = "";
        }
        if (saveContact.contactType != P2PValue.DeviceType.PHONE) {
            if (input_pwd != null && !input_pwd.trim().equals("")) {
                if (input_pwd.charAt(0) == '0' || input_pwd.length() > 30) {
                    T.showShort(mContext, R.string.device_password_invalid);
                    return;
                }
            }
        }

        List<Contact> contactlist = DataManager.findContactByActiveUser(mContext, NpcCommon.mThreeNum);
        for (Contact contact : contactlist) {
            if (contact.contactId.equals(saveContact.contactId)) {
                if (camera != null && camera.size() > 0) {
                    for (int i = 0; i < camera.size(); i++) {
                        if (camera.get(i).getId().equals(input_id)) {
                            T.showShort(mContext, R.string.contact_already_exist);
                            return;
                        }
                    }
                }

            }
        }

        saveContact.contactName = input_name;
        saveContact.userPassword = input_pwd;
        String pwd = P2PHandler.getInstance().EntryPassword(input_pwd);
        saveContact.contactPassword = pwd;
        String[] contactIds = new String[]{input_id};
        P2PHandler.getInstance().getFriendStatus(contactIds);
        P2PHandler.getInstance().checkPassword(input_id, pwd,MainApplication.GWELL_LOCALAREAIP);
        P2PHandler.getInstance().getDefenceStates(input_id, pwd,MainApplication.GWELL_LOCALAREAIP);
        P2PHandler.getInstance().checkDeviceUpdate(input_id, pwd,MainApplication.GWELL_LOCALAREAIP);
        if (saveContact.defenceState == Constants.DefenceState.DEFENCE_STATE_WARNING_PWD) {
            T.showShort(mContext, R.string.pw_incrrect);
            return;
        }
        addDevice(null);
    }

    private void nextXM(){
        String input_id = input_device_id.getText().toString();
        String input_name = input_device_name.getText().toString();
        String input_pwd = input_device_password.getText().toString();
        if (input_id != null && input_id.trim().equals("")) {
            T.showShort(mContext, R.string.input_contact_id);
            return;
        }
        if (input_name != null && input_name.trim().equals("")) {

            T.showShort(mContext, R.string.input_contact_name);
            return;
        }
        if(rl_pwd.getVisibility()==View.VISIBLE){
          if (input_pwd == null || input_pwd.trim().equals("")) {
             T.showShort(this, R.string.input_password);
             return;
          }
          if(input_pwd.length()<6){
              ToastUtil.shortMessage(getString(R.string.register_tip_password_length));
              return ;
          }
        }

        boolean isExists = false ;
        if(mZhujiInfos==null||mZhujiInfos.size()==0){
            isExists = false ;
        }else{
            for(ZhujiInfo info : mZhujiInfos){
                if(info.getCameraInfo()!=null&&input_id.equals(info.getCameraInfo().getId())){
                    isExists = true ;
                    break ;
                }
            }
        }

        if(isExists){
            T.showShort(mContext, R.string.contact_already_exist);
            return ;
        }
        showInProgress(getString(R.string.loading), false, true);
        //查询设备是否在线，不再线不添加
        requestDeviceStatus(input_id);
    }

    private int judgeXiongMaiAndJiwei(){
        String input_id = input_device_id.getText().toString();
        if (input_id != null && input_id.trim().equals("")) {
            T.showShort(mContext, R.string.input_contact_id);
            return -1;
        }
        if(TextUtils.isDigitsOnly(input_id)){
            return 3; //技威
        }else{
            return 9;//雄迈
        }
    }


    private void addDevice(com.macrovideo.sdk.custom.DeviceInfo device) {
        String c1 = null, id1 = null, n1 = null, p1 = null;
        final long did = info.getId();
        if (i == 5) {
            c1 = "v380";
            id1 = device.getnDevID() + "";
            n1 = device.getStrName();
            p1 = device.getStrPassword();
        } else if(i==9){
            c1 = "xiongmai";
            id1 = input_device_id.getText().toString();
            n1 = input_device_name.getText().toString();
            p1 =  input_device_password.getText().toString();
        }else{
            c1 = "jiwei";
            id1 = saveContact.contactId;
            n1 = saveContact.contactName;
            p1 = saveContact.contactPassword;
        }
        final String c = c1;
        final String id = id1;
        final String n = n1;
        final String p = p1;
        defaultHandler.sendEmptyMessageDelayed(10,10*1000);
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(AddContactActivity.this,
                        Constant.CONFIG);
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                if (!isMainList&&i!=9) {
                    pJsonObject.put("did", did);
                }
                pJsonObject.put("c", c);
                pJsonObject.put("id", id);
                pJsonObject.put("n", n);
                pJsonObject.put("p", p);
                String http =  server + "/jdm/s3/ipcs/add";
                String result = HttpRequestUtils.requestoOkHttpPost(http, pJsonObject, dcsp);
                // -1参数为空 -2校验失败 -10服务器不存在
                if ("0".equals(result)) {
                    //清空推送用户
                    cancelInProgress();
                    if(i!=9){
                        P2PHandler.getInstance().setBindAlarmId(id,p,0,new String[]{},MainApplication.GWELL_LOCALAREAIP);
                    }
                    if(i==9){
                        //雄迈摄像头保存到本地
                        DataCenterSharedPreferences.getInstance(getApplicationContext()
                                , DataCenterSharedPreferences.Constant.XM_CONFIG).putString(id+ SECURITY_SETTING_PWD,p).commit();
                    }
                    defaultHandler.sendEmptyMessage(1);
                } else {
                    defaultHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            cancelInProgress();
                            defaultHandler.removeMessages(10);
                            T.showShort(mContext, R.string.addfailed);
                        }
                    });
                }

            }
        });
    }

    // 设备登录
    private void requestDeviceStatus(String devSn) {
        FunSupport.getInstance().requestDeviceStatus(FunDevType.EE_DEV_NORMAL_MONITOR ,devSn);
    }

    @Override
    public int getActivityInfo() {
        // TODO Auto-generated method stub
        return Constants.ActivityInfo.ACTIVITY_ADDCONTACTACTIVITY;
    }

    @Override
    protected int onPreFinshByLoginAnother() {
        return 0;
    }

    public void sendSuccessBroadcast() {
        Intent refreshContans = new Intent();
        refreshContans.setAction(Constants.Action.REFRESH_CONTANTS);
        refreshContans.putExtra("contact", saveContact);
        mContext.sendBroadcast(refreshContans);

        Intent createPwdSuccess = new Intent();
        createPwdSuccess.setAction(Constants.Action.UPDATE_DEVICE_FALG);
        createPwdSuccess.putExtra("threeNum", saveContact.contactId);
        mContext.sendBroadcast(createPwdSuccess);

        Intent add_success = new Intent();
        add_success.setAction(Constants.Action.ADD_CONTACT_SUCCESS);
        add_success.putExtra("contact", saveContact);
        mContext.sendBroadcast(add_success);

        Intent refreshNearlyTell = new Intent();
        refreshNearlyTell.setAction(Constants.Action.ACTION_REFRESH_NEARLY_TELL);
        mContext.sendBroadcast(refreshNearlyTell);
        T.showShort(mContext, R.string.add_success);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
