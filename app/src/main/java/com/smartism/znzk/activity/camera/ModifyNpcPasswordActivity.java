package com.smartism.znzk.activity.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.widget.HeaderView;
import com.smartism.znzk.widget.NormalDialog;

public class ModifyNpcPasswordActivity extends BaseActivity implements OnClickListener {
    Context mContext;
    Contact mContact;
    ImageView mBack;
    TextView mSave;
    EditText old_pwd, new_pwd, re_new_pwd;
    TextView tv_weak_password;
    TextView tv_title;
    HeaderView header_img;
    Button viewDeviceVersionBtn;
    private TextView contactName;
    NormalDialog dialog;
    private ZhujiInfo info;
    private String result;
    String idOrIp = "";
    private boolean isRegFilter = false;
    String password_old, password_new, password_re_new;
    boolean isWeakPwd = false;
    String userPassword;
    boolean isModifyNvrPwd = false;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.e("修改到服务器", "成功");
                    mContact.contactPassword = password_new;
                    mContact.userPassword = userPassword;
                    FList.getInstance().update(mContact);
                    Intent refreshContans = new Intent();
                    refreshContans.setAction(Constants.Action.REFRESH_CONTANTS);
                    refreshContans.putExtra("contact", mContact);
                    mContext.sendBroadcast(refreshContans);
                    defaultHandler.removeCallbacks(runnable);

                    T.showShort(mContext, R.string.device_set_tip_success);
                    finish();
                    Log.e("修改到数据库", "成功");
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
        setContentView(R.layout.modify_npc_pwd);

        mContact = (Contact) getIntent().getSerializableExtra("contact");
        isWeakPwd = getIntent().getBooleanExtra("isWeakPwd", false);
        isModifyNvrPwd = getIntent().getBooleanExtra("isModifyNvrPwd", false);
        mContext = this;
        if (mContact != null) {
            idOrIp = mContact.contactId;
            if (mContact.ipadressAddress != null && !mContact.ipadressAddress.equals("")) {
                String mark = mContact.ipadressAddress.getHostAddress();
                idOrIp = mark.substring(mark.lastIndexOf(".") + 1,
                        mark.length());
            }
        }
        initCompent();
        DeviceInfo deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("deviceInfo");
        if (deviceInfo!=null && DeviceInfo.ControlTypeMenu.zhuji.value().equals(deviceInfo.getControlType())){
            info = DatabaseOperator.getInstance(getApplicationContext()).queryDeviceZhuJiInfo(deviceInfo.getId());
        }else {
//            info = DatabaseOperator.getInstance(getApplicationContext())
//                    .queryDeviceZhuJiInfo(DataCenterSharedPreferences
//                            .getInstance(MainApplication.app, Constant.CONFIG)
//                            .getString(Constant.APP_MASTERID, ""));
            //替换
            info = DatabaseOperator.getInstance(getApplicationContext()).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
        }
        regFilter();
    }

    public void initCompent() {
        mBack = (ImageView) findViewById(R.id.back_btn);
        mSave = (TextView) findViewById(R.id.save);
        old_pwd = (EditText) findViewById(R.id.old_pwd);
        new_pwd = (EditText) findViewById(R.id.new_pwd);
        re_new_pwd = (EditText) findViewById(R.id.re_new_pwd);
        tv_title = (TextView) findViewById(R.id.tv_title);
        contactName = (TextView) findViewById(R.id.contactName);
        header_img = (HeaderView) findViewById(R.id.header_img);
        viewDeviceVersionBtn = (Button) findViewById(R.id.viewDeviceVersionBtn);
        if (mContact != null) {
            contactName.setText(mContact.contactName);
            header_img.updateImage(mContact.contactId, false);
            if (mContact.contactType == P2PValue.DeviceType.NPC) {
                viewDeviceVersionBtn.setVisibility(RelativeLayout.GONE);
            } else {
                viewDeviceVersionBtn.setVisibility(RelativeLayout.VISIBLE);
            }
        }
        old_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        new_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
        re_new_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());

        tv_weak_password = (TextView) findViewById(R.id.tv_weak_password);
        if (isWeakPwd) {
            tv_weak_password.setText(getResources().getString(R.string.weak_password));
            // tv_weak_password.setVisibility(TextView.VISIBLE);
        } else {
            tv_weak_password.setText(getResources().getString(R.string.new_device_password));
            // tv_weak_password.setVisibility(TextView.GONE);
        }
        if (isModifyNvrPwd) {
            tv_title.setText(getResources().getString(R.string.modify_nvr_pwd));
        } else {
            tv_title.setText(getResources().getString(R.string.modify_device_password));
        }
        mBack.setOnClickListener(this);
        mSave.setOnClickListener(this);
        viewDeviceVersionBtn.setOnClickListener(this);
    }

    public void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.P2P.ACK_RET_SET_DEVICE_PASSWORD);
        filter.addAction(Constants.P2P.RET_SET_DEVICE_PASSWORD);
        filter.addAction(Constants.P2P.RET_DEVICE_NOT_SUPPORT);
        filter.addAction(Constants.P2P.ACK_RET_GET_DEVICE_INFO);
        filter.addAction(Constants.P2P.RET_GET_DEVICE_INFO);
        mContext.registerReceiver(mReceiver, filter);
        isRegFilter = true;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (intent.getAction().equals(
                    Constants.P2P.RET_GET_DEVICE_INFO)) {
                int result = intent.getIntExtra("result", -1);
                String cur_version = intent.getStringExtra("cur_version");
                int iUbootVersion = intent.getIntExtra("iUbootVersion", 0);
                int iKernelVersion = intent.getIntExtra("iKernelVersion", 0);
                int iRootfsVersion = intent.getIntExtra("iRootfsVersion", 0);
                if (null != dialog) {
                    dialog.dismiss();
                    dialog = null;
                }

                if (isCancelCheck) {
                    return;
                }

                NormalDialog deviceInfo = new NormalDialog(mContext);
                deviceInfo.showDeviceInfoDialog(cur_version,
                        String.valueOf(iUbootVersion),
                        String.valueOf(iKernelVersion),
                        String.valueOf(iRootfsVersion));
            } else if (intent.getAction().equals(
                    Constants.P2P.ACK_RET_GET_DEVICE_INFO)) {
                int result = intent.getIntExtra("result", -1);
                if (result == Constants.P2P_SET.ACK_RESULT.ACK_PWD_ERROR) {
                    if (null != dialog) {
                        dialog.dismiss();
                        dialog = null;
                    }
                    T.showShort(mContext, R.string.password_error);
                }
            } else if (intent.getAction().equals(Constants.P2P.ACK_RET_SET_DEVICE_PASSWORD)) {
                if (dialog != null) {
                    dialog.dismiss();
                    dialog = null;
                }
                modify();
            }
        }
    };
    boolean isCancelCheck = false;

    @Override
    public void onClick(View view) {
        // TODO Auto-generated method stub
        switch (view.getId()) {
            case R.id.viewDeviceVersionBtn:
                if (null != dialog && dialog.isShowing()) {
                    Log.e("my", "isShowing");
                    return;
                }
                dialog = new NormalDialog(mContext);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface arg0) {
                        // TODO Auto-generated method stub
                        isCancelCheck = true;
                    }

                });
                dialog.setTitle(mContext.getResources().getString(
                        R.string.device_info));
                dialog.showLoadingDialog2();
                dialog.setCanceledOnTouchOutside(false);
                isCancelCheck = false;
                P2PHandler.getInstance().getDeviceVersion(idOrIp,
                        mContact.contactPassword, MainApplication.GWELL_LOCALAREAIP);
                break;
            case R.id.back_btn:
                finish();
                break;
            case R.id.save:
                password_old = old_pwd.getText().toString();
                password_new = new_pwd.getText().toString();
                Log.e("2qweqeqe", mContact.contactId + "---" + mContact.contactPassword);
                password_re_new = re_new_pwd.getText().toString();
                if ("".equals(password_old.trim())) {
                    T.showShort(mContext, R.string.input_old_device_pwd);
                    return;
                }
                if (!mContact.contactPassword.equals(P2PHandler.getInstance().EntryPassword(password_old))) {
                    T.showShort(mContext, R.string.old_pwd_error);
                    return;
                }

                if ("".equals(password_new.trim())) {
                    T.showShort(mContext, R.string.input_new_device_pwd);
                    return;
                }

                String reg = "^(\\d+[A-Za-z]+[A-Za-z0-9]*)|([A-Za-z]+\\d+[A-Za-z0-9]*)$";
                if (password_new.length() < 6 || password_new.length() > 10 || password_new.charAt(0) == '0') {
                    T.showShort(mContext, R.string.device_password_invalid);
                    return;
                }
                if (!password_new.matches(reg)) {
                    T.showShort(mContext, R.string.device_password_invalid);
                    return;
                }
                if ("".equals(password_re_new.trim())) {
                    T.showShort(mContext, R.string.input_re_new_device_pwd);
                    return;
                }

                if (!password_re_new.equals(password_new)) {
                    T.showShort(mContext, R.string.pwd_inconsistence);
                    return;
                }

                if (null == dialog) {
                    dialog = new NormalDialog(this, this.getResources().getString(R.string.verification), "", "", "");
                    dialog.setStyle(NormalDialog.DIALOG_STYLE_LOADING);
                }
                dialog.showDialog();
                defaultHandler.postDelayed(runnable, 6 * 1000);
                userPassword = password_new;
                password_old = P2PHandler.getInstance().EntryPassword(password_old);
                password_new = P2PHandler.getInstance().EntryPassword(password_new);
                P2PHandler.getInstance().setDevicePassword(mContact.contactId, password_old, password_new, MainApplication.GWELL_LOCALAREAIP);
                break;
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (dialog != null) {
                dialog.dismiss();
                dialog = null;
            }
            T.showShort(mContext, R.string.operator_error);
        }
    };

    private void modify() {
        final long did = info.getId();
        final String c = "jiwei";
        final String id = mContact.contactId;
        final String n = mContact.contactName;
        final String p = userPassword;
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences
                        .getInstance(ModifyNpcPasswordActivity.this, Constant.CONFIG);
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", did);
                pJsonObject.put("c", c);
                pJsonObject.put("id", id);
                pJsonObject.put("n", n);
                pJsonObject.put("p", p);

                String http =  server + "/jdm/s3/ipcs/add";

                String result = HttpRequestUtils.requestoOkHttpPost(http, pJsonObject, dcsp);

                // -1参数为空 -2校验失败 -10服务器不存在
                if ("0".equals(result)) {
                    defaultHandler.sendEmptyMessage(1);
                } else {
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
    public void onDestroy() {
        super.onDestroy();
        dialog = null;
        if (isRegFilter) {
            mContext.unregisterReceiver(mReceiver);
            isRegFilter = false;
        }
    }

    @Override
    public int getActivityInfo() {
        // TODO Auto-generated method stub
        return Constants.ActivityInfo.ACTIVITY_MODIFNPCPASSWORDACTIVITY;
    }

    @Override
    protected int onPreFinshByLoginAnother() {
        return 0;
    }
}
