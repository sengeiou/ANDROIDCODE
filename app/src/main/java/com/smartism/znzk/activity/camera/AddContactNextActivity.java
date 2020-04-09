package com.smartism.znzk.activity.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.p2p.core.utils.MyUtils;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.db.camera.DataManager;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.ImageUtils;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.widget.NormalDialog;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunSupport;
import com.smartism.znzk.xiongmai.lib.funsdk.support.config.ModifyPassword;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;

import java.io.File;
import java.util.List;

import static com.smartism.znzk.util.DataCenterSharedPreferences.Constant.SECURITY_SETTING_PWD;

/**
 * 完成摄像头添加，输入名称、密码完成添加
 *
 * @author update by 王建 2016年08月19日
 */
public class AddContactNextActivity extends BaseActivity implements OnClickListener {
    private static final int RESULT_GETIMG_FROM_CAMERA = 0x11;
    private static final int RESULT_GETIMG_FROM_GALLERY = 0x12;
    private static final int RESULT_CUT_IMAGE = 0x13;
    private TextView mSave, tMesg;
    private ImageView mBack;
    private ZhujiInfo info;
    Context mContext;
    EditText contactName, contactPwd;
    LinearLayout layout_device_pwd;
    TextView contactId;
    NormalDialog dialog;
    Contact mSaveContact;
    RelativeLayout modify_header;
    LinearLayout layout_create_pwd;
    EditText createPwd1, createPwd2;
    private Bitmap tempHead;
    boolean isSave = false;
    boolean isCreatePassword = false; //是否需要设置初始密码
    String input_name, input_pwd, input_create_pwd1, input_create_pwd2;
    boolean isRegFilter;
    String ipFlag;
    boolean isfactory; //连接模式
    String userPassword;
    String device;
    private String result;
    private int k;
    private int i;//3、技威，5、v380,9、雄迈
    private int isCameraList;//判断是否是返回摄像头列表还是返回设备列表 0、返回设备列表，1、返回摄像头列表
    private boolean isContactFrag = false;
    private boolean isMainList = false;//是否是主机列表
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact_next);
        isMainList = getIntent().getBooleanExtra("isMainList", false);
        mSaveContact = (Contact) getIntent().getSerializableExtra("contact");
        isCreatePassword = getIntent().getBooleanExtra("isCreatePassword", false);
        isfactory = getIntent().getBooleanExtra("isfactory", false);
        ipFlag = getIntent().getStringExtra("ipFlag");
        device = getIntent().getStringExtra("v380");
        i = getIntent().getIntExtra("int", 0);
        isCameraList = getIntent().getIntExtra("isCameraList", 0);

        mContext = this;
//        info = DatabaseOperator.getInstance(getApplicationContext()).queryDeviceZhuJiInfo(
//                DataCenterSharedPreferences.getInstance(mContext, Constant.CONFIG)
//                        .getString(Constant.APP_MASTERID, ""));
        //替换
        info = DatabaseOperator.getInstance(getApplicationContext()).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
        initCompent();
        regFilter();
    }

    public void initCompent() {
        tMesg = (TextView) findViewById(R.id.tv_pwd_message);
        contactId = (TextView) findViewById(R.id.contactId);
        contactName = (EditText) findViewById(R.id.contactName);
        contactPwd = (EditText) findViewById(R.id.contactPwd);
        contactPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        layout_device_pwd = (LinearLayout) findViewById(R.id.layout_device_pwd);
        layout_create_pwd = (LinearLayout) findViewById(R.id.layout_create_pwd);
        createPwd1 = (EditText) findViewById(R.id.createPwd1);
        createPwd2 = (EditText) findViewById(R.id.createPwd2);
        createPwd1.setTransformationMethod(PasswordTransformationMethod.getInstance());
        createPwd2.setTransformationMethod(PasswordTransformationMethod.getInstance());
        mBack = (ImageView) findViewById(R.id.back_btn);
        mSave = (TextView) findViewById(R.id.save);
        modify_header = (RelativeLayout) findViewById(R.id.modify_header);
        contactName.setText("Cam" + mSaveContact.contactId);
        if (isCreatePassword) {
            createPwd1.requestFocus();
            layout_create_pwd.setVisibility(RelativeLayout.VISIBLE);
            layout_device_pwd.setVisibility(RelativeLayout.GONE);
            tMesg.setVisibility(View.GONE);
        } else {
            contactPwd.requestFocus();
            layout_create_pwd.setVisibility(RelativeLayout.GONE);
            tMesg.setVisibility(View.VISIBLE);
            if (mSaveContact.contactType != P2PValue.DeviceType.PHONE) {
                layout_device_pwd.setVisibility(RelativeLayout.VISIBLE);
            } else {
                layout_device_pwd.setVisibility(RelativeLayout.GONE);
            }
        }

        if(i==9){
            //雄迈,隐藏密码提示
            tMesg.setVisibility(View.GONE);
        }
        contactId.setText(mSaveContact.contactId);
        modify_header.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mSave.setOnClickListener(this);
    }

    public void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.P2P.ACK_RET_SET_INIT_PASSWORD);
        filter.addAction(Constants.P2P.RET_SET_INIT_PASSWORD);
        filter.addAction(Constants.Action.SESSION_ID_ERROR);
        filter.addAction(Constants.Action.ACTION_SWITCH_USER);
        filter.addAction(Constants.Action.RECEIVE_MSG);
        filter.addAction(Constants.Action.ACTIVITY_OPENCAMERA);
        filter.addAction(Constants.Action.ACTIVITY_ADDCAMERA);
        mContext.registerReceiver(mReceiver, filter);
        isRegFilter = true;
    }

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    cancelInProgress();
                    Intent adapterIntent = new Intent();
                    adapterIntent.setAction(Constants.Action.ADAPTER_NOTIFY_LISTLIVE);
                    mContext.sendBroadcast(adapterIntent);

                    if (i == 3) {
                        Intent createPwdSuccess = new Intent();
                        createPwdSuccess.setAction(Constants.Action.ACTIVITY_FINISH);
                        mContext.sendBroadcast(createPwdSuccess);
                        Intent intent = new Intent();
                        SyncMessageContainer.getInstance().produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                        finish();
                        return true;
                    } else {
                        //添加技威摄像头
                        Intent createPwdSuccess = new Intent();
                        createPwdSuccess.setAction(Constants.Action.ACTIVITY_FINISH);
                        mContext.sendBroadcast(createPwdSuccess);
//                        Intent in = new Intent();
//                        in.setClass(mContext, MainActivity.class);
//                        startActivity(in);
                    }
                    finish();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(Constants.P2P.RET_SET_INIT_PASSWORD)) {
                int result = intent.getIntExtra("result", -1);
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
                if (result == Constants.P2P_SET.INIT_PASSWORD_SET.SETTING_SUCCESS) {
                    Contact contact = FList.getInstance().isContact(mSaveContact.contactId);
                    if (null != contact) {
                        contact.contactName = input_name;
                        contact.contactPassword = P2PHandler.getInstance().EntryPassword(input_create_pwd1);
                        contact.userPassword = userPassword;
                        FList.getInstance().update(contact);

                        mSaveContact = contact;//不加这句 addDevice 方法会取mSaveContact中的数据，昵称和密码会错误
                    } else {
                        mSaveContact.contactName = input_name;
                        mSaveContact.contactPassword = P2PHandler.getInstance().EntryPassword(input_create_pwd1);
                        mSaveContact.userPassword = userPassword;
                        FList.getInstance().insert(mSaveContact);
                    }
                    FList.getInstance().updateLocalDeviceFlag(mSaveContact.contactId,
                            Constants.DeviceFlag.ALREADY_SET_PASSWORD);
                    isSave = true;
                    FList.getInstance().updateLocalDeviceWithLocalFriends();
                    sendSuccessBroadcast();
                    addDevice(1);
                } else if (result == Constants.P2P_SET.INIT_PASSWORD_SET.ALREADY_EXIST_PASSWORD) {
                    Intent createPwdSuccess = new Intent();
                    createPwdSuccess.setAction(Constants.Action.UPDATE_DEVICE_FALG);
                    createPwdSuccess.putExtra("threeNum", mSaveContact.contactId);
                    mContext.sendBroadcast(createPwdSuccess);
                    T.showShort(mContext, R.string.already_init_passwd);
                    finish();
                } else {
                    T.showShort(mContext, R.string.operator_error);
                }
            } else if (intent.getAction().equals(Constants.P2P.ACK_RET_SET_INIT_PASSWORD)) {
                int result = intent.getIntExtra("result", -1);
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
                if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                    T.showShort(mContext, R.string.password_error);
                } else if (result == Constants.P2P_SET.ACK_RESULT.ACK_NET_ERROR) {
                    T.showShort(mContext, R.string.net_error_operator_fault);
                }
            }
        }
    };

    private void addDevice(final int showSuccess) {
        showInProgress("",true,false);
        final String c = "jiwei";
        final String id = mSaveContact.contactId;
        final String n = mSaveContact.contactName;
        final String p = mSaveContact.contactPassword;

        JavaThreadPool.getInstance().excute(new Runnable() {
            @Override
            public void run() {
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(AddContactNextActivity.this,
                        Constant.CONFIG);
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                if (!isMainList) {
                    if(i!=9){
                        //雄迈暂时添加到主机列表
                        pJsonObject.put("did", info.getId());
                    }
                }
                if(i==9){
                    //雄迈摄像头
                   pJsonObject.put("c","xiongmai");
                }else{
                    pJsonObject.put("c", c);//摄像头品牌
                }
                pJsonObject.put("id", id);//摄像头序列号
                pJsonObject.put("n", n);//摄像头名称
                pJsonObject.put("p", p);//摄像头密码
                String http =  server + "/jdm/s3/ipcs/add";
                String result = HttpRequestUtils.requestoOkHttpPost(http, pJsonObject, dcsp);

                // -1参数为空 -2校验失败 -10服务器不存在
                if ("0".equals(result)) {
                    if(i!=9){
                        //清空推送用户
                        P2PHandler.getInstance().setBindAlarmId(mSaveContact.contactId,mSaveContact.getContactPassword(),0,new String[]{}, MainApplication.GWELL_LOCALAREAIP);
                    }
                    if(i==9){
                        //雄迈摄像头保存到本地
                        DataCenterSharedPreferences.getInstance(getApplicationContext()
                                , DataCenterSharedPreferences.Constant.XM_CONFIG).putString(id+ SECURITY_SETTING_PWD,p).commit();
                    }
                    defaultHandler.sendMessage(defaultHandler.obtainMessage(1, showSuccess, 0));
                } else {
                    cancelInProgress();
                    defaultHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            T.showShort(mContext, R.string.addfailed);
                        }
                    });
                }

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_GETIMG_FROM_CAMERA) {
            try {
                Bundle extras = data.getExtras();
                tempHead = (Bitmap) extras.get("data");
                ImageUtils.saveImg(tempHead, Constants.Image.USER_HEADER_PATH,
                        Constants.Image.USER_HEADER_TEMP_FILE_NAME);

                Intent cutImage = new Intent(mContext, CutImageActivity.class);
                cutImage.putExtra("contact", mSaveContact);
                startActivityForResult(cutImage, RESULT_CUT_IMAGE);
            } catch (NullPointerException e) {
            }
        } else if (requestCode == RESULT_GETIMG_FROM_GALLERY) {

            try {
                Uri uri = data.getData();
                tempHead = ImageUtils.getBitmap(ImageUtils.getAbsPath(mContext, uri),
                        Constants.USER_HEADER_WIDTH_HEIGHT, Constants.USER_HEADER_WIDTH_HEIGHT);
                ImageUtils.saveImg(tempHead, Constants.Image.USER_HEADER_PATH,
                        Constants.Image.USER_HEADER_TEMP_FILE_NAME);

                Intent cutImage = new Intent(mContext, CutImageActivity.class);
                cutImage.putExtra("contact", mSaveContact);
                startActivityForResult(cutImage, RESULT_CUT_IMAGE);

            } catch (NullPointerException e) {
            }
        } else if (requestCode == RESULT_CUT_IMAGE) {
            try {
                if (resultCode == 1) {
                    Intent refreshContans = new Intent();
                    refreshContans.setAction(Constants.Action.REFRESH_CONTANTS);
                    refreshContans.putExtra("contact", mSaveContact);
                    mContext.sendBroadcast(refreshContans);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_btn:
                this.finish();
                break;
            case R.id.save:
                //志诚版本不能添加技威,只能添加雄迈
                if(Actions.VersionType.CHANNEL_ZHISHANG.equals(MainApplication.app.getAppGlobalConfig().getVersion())&&i!=9){
                    ToastTools.short_Toast(this,"暂不支持此类型摄像头");
                    return ;
                }
                save();
                break;
            case R.id.modify_header:
                break;
            default:
                break;
        }
    }

    public void destroyTempHead() {
        if (tempHead != null && !tempHead.isRecycled()) {
            tempHead.recycle();
            tempHead = null;
        }
    }

    void save() {
        input_name = contactName.getText().toString();//设备序列号
        input_pwd = contactPwd.getText().toString();//设备密码
        input_create_pwd1 = createPwd1.getText().toString();
        input_create_pwd2 = createPwd2.getText().toString();
        if (input_name != null && input_name.trim().equals("")) {
            T.showShort(mContext, R.string.input_contact_name);
            return;
        }
        if (isCreatePassword) {

            if (null == input_create_pwd1 || "".equals(input_create_pwd1)) {
                T.showShort(this, R.string.inputpassword);
                return;
            }
            if (input_create_pwd1.charAt(0) == '0' || input_create_pwd1.length() > 30) {
                T.showShort(this, R.string.device_password_invalid);
            }

            if (null == input_create_pwd2 || "".equals(input_create_pwd2)) {
                T.showShort(this, R.string.reinputpassword);
                return;
            }
            if (!input_create_pwd1.equals(input_create_pwd2)) {
                T.showShort(this, R.string.differentpassword);
                return;
            }

            if (null == dialog) {
                dialog = new NormalDialog(this, this.getResources().getString(R.string.verification), "", "", "");
                dialog.setStyle(NormalDialog.DIALOG_STYLE_LOADING);
            }
            if (null != ipFlag && !ipFlag.equals("") && MyUtils.isNumeric(ipFlag)) {
                userPassword = input_create_pwd1;

                Log.e("ip最后一位：", ipFlag);
                P2PHandler.getInstance().setInitPassword(ipFlag,
                        P2PHandler.getInstance().EntryPassword(input_create_pwd1), input_create_pwd1, input_create_pwd1, MainApplication.GWELL_LOCALAREAIP);

            } else {
                T.showShort(mContext, "IP没有找到");
            }
            dialog.showDialog();

        } else {//摄像头自带初始密码，输入摄像头初始密码
            if (input_pwd == null || input_pwd.trim().equals("")) {
                T.showShort(this, R.string.input_password);
                return;
            }
            if (mSaveContact.contactType != P2PValue.DeviceType.PHONE) {
                if (input_pwd != null && !input_pwd.trim().equals("")) {
                    if (input_pwd.length() > 30 || input_pwd.charAt(0) == '0') {
                        T.showShort(mContext, R.string.device_password_invalid);
                        return;
                    }
                }
            }
            if (!isfactory) {
                List<Contact> lists = DataManager.findContactByActiveUser(mContext, NpcCommon.mThreeNum);
                for (Contact c : lists) {
                    if (c.contactName.equals(input_name)) {
                        T.showShort(mContext, R.string.device_name_exist);
                        return;
                    }
                    if (c.contactId.equals(mSaveContact.contactId)) {
                        T.showShort(mContext, R.string.contact_already_exist);
                        return;
                    }
                }
                mSaveContact.contactName = input_name;
                mSaveContact.userPassword = input_pwd;
                input_pwd = P2PHandler.getInstance().EntryPassword(input_pwd);
                mSaveContact.contactPassword = input_pwd;
                //保存已连接的摄像头设备
                FList.getInstance().insert(mSaveContact);
                FList.getInstance().updateLocalDeviceWithLocalFriends();
                isSave = true;
                sendSuccessBroadcast();
                finish();
            } else {
                Contact contact = FList.getInstance().isContact(mSaveContact.contactId);
                if (null != contact) {
                    contact.contactName = input_name;
                    contact.userPassword = input_pwd;
                    input_pwd = P2PHandler.getInstance().EntryPassword(input_pwd);
                    contact.contactPassword = input_pwd;

                    FList.getInstance().update(contact);

                    mSaveContact = contact; //不加这句 addDevice 方法会取mSaveContact中的数据，昵称和密码会错误
                } else {
                    mSaveContact.contactName = input_name;
                    mSaveContact.userPassword = input_pwd;
                    input_pwd = P2PHandler.getInstance().EntryPassword(input_pwd);
                    mSaveContact.contactPassword = input_pwd;
                    mSaveContact.setIsfactory(true);
                    FList.getInstance().insert(mSaveContact);
                }
                FList.getInstance().updateLocalDeviceWithLocalFriends();
                isSave = true;
                sendSuccessBroadcast();
                addDevice(0);
            }
        }
    }

    public void sendSuccessBroadcast() {
        Intent refreshContans = new Intent();
        refreshContans.setAction(Constants.Action.REFRESH_CONTANTS);
        refreshContans.putExtra("contact", mSaveContact);
        mContext.sendBroadcast(refreshContans);

        Intent createPwdSuccess = new Intent();
        createPwdSuccess.setAction(Constants.Action.UPDATE_DEVICE_FALG);
        createPwdSuccess.putExtra("threeNum", mSaveContact.contactId);
        mContext.sendBroadcast(createPwdSuccess);

        Intent add_success = new Intent();
        add_success.setAction(Constants.Action.ADD_CONTACT_SUCCESS);
        add_success.putExtra("contact", mSaveContact);
        mContext.sendBroadcast(add_success);

        Intent refreshNearlyTell = new Intent();
        refreshNearlyTell.setAction(Constants.Action.ACTION_REFRESH_NEARLY_TELL);
        mContext.sendBroadcast(refreshNearlyTell);
        T.showShort(mContext, R.string.add_success);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyTempHead();

        if (isCreatePassword) {
            Contact contact = FList.getInstance().isContact(mSaveContact.contactId);
            if (!isSave && null == contact) {
                File file = new File(
                        Constants.Image.USER_HEADER_PATH + NpcCommon.mThreeNum + "/" + mSaveContact.contactId);
                Utils.deleteFile(file);
            }
        } else {
            if (!isSave) {
                File file = new File(
                        Constants.Image.USER_HEADER_PATH + NpcCommon.mThreeNum + "/" + mSaveContact.contactId);
                Utils.deleteFile(file);
            }
        }

        if (isRegFilter) {
            mContext.unregisterReceiver(mReceiver);
            isRegFilter = false;
        }
    }

    @Override
    public int getActivityInfo() {
        return Constants.ActivityInfo.ACTIVITY_ADDCONTACTNEXTACTIVITY;
    }

    @Override
    protected int onPreFinshByLoginAnother() {
        return 0;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (isCameraList == 1) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.putExtra("isNotCamera", false);
                startActivity(intent);
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
