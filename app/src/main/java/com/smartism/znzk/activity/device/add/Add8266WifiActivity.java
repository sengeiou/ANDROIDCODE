package com.smartism.znzk.activity.device.add;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.domain.HeaterShadowInfo;
import com.smartism.znzk.hipermission.HiPermission;
import com.smartism.znzk.hipermission.PermissionCallback;
import com.smartism.znzk.hipermission.PermissionItem;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.EspWifiAdminSimple;
import com.smartism.znzk.util.NetworkUtils;
import com.smartism.znzk.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;


public class Add8266WifiActivity extends BaseActivty implements View.OnClickListener {

    CheckBox cbLaws;
    ImageView ivBack;
    TextView tvName;
    TextView wifiSupport;
    EditText wifiSsid;

    EditText wifiPwd;
    Button btnNext;
    LinearLayout llAdd;
    TextView tvStatus;
    private String ssid;

    private String password;

    private boolean prompt = false;

    private boolean checkUpResult = true;

    private boolean isAPConfig = true;

    private EspWifiAdminSimple mWifiAdmin;
    private String apSsid;
    private final String TAG = "AddDeviceActivity";
    boolean isMainList;

    private ImageView iv_wifi_icon;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add8266_wifi);
        mWifiAdmin = new EspWifiAdminSimple(this);
        isMainList = getIntent().getBooleanExtra("isMainList", false);
        initView();
    }

    private void initView() {
        wifiSsid = (EditText) findViewById(R.id.wifi_ssid);
        cbLaws = (CheckBox) findViewById(R.id.cbLaws);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        tvName = (TextView) findViewById(R.id.tv_name);
        wifiPwd = (EditText) findViewById(R.id.wifi_pwd);
        llAdd = (LinearLayout) findViewById(R.id.ll_add);
        btnNext = (Button) findViewById(R.id.btn_next);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        iv_wifi_icon = (ImageView) findViewById(R.id.iv_wifi_icon);
        if (isMainList){
            ((TextView) findViewById(R.id.tv_name)).setText(R.string.add_devices_title);
        }
        if (Actions.VersionType.CHANNEL_ZHILIDE.equals(MainApplication.app.getAppGlobalConfig().getVersion()))
            iv_wifi_icon.setVisibility(View.GONE);
        ivBack.setOnClickListener(this);
        btnNext.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        apSsid = mWifiAdmin.getWifiConnectedSsid();
//        boolean isApSsidEmpty = TextUtils.isEmpty(apSsid);
//        btnNext.setEnabled(!isApSsidEmpty);
        if (isOpenLocationService()) {
            initConfigure();
        }
    }

    //初始化配网操作，如果SSID包含（10.10.3),则为AP配网，wifi账号可编辑
    //否则为SmartLink,获取当前ssid，wifi账号不可编辑
    private void initConfigure() {
        String strSSID = NetworkUtils.getCurentWifiSSID(mContext);
        String strIp = NetworkUtils.getCurentWifiIp(mContext);
        Log.i(TAG, "strSSID" + strSSID + ",strIp" + strIp);
        if (strIp.contains("10.10.3")) {
            isAPConfig = true;
//            tvStatus.setText("AP配网");
//            Toast.makeText(mContext, "AP模式", Toast.LENGTH_LONG).show();
            wifiSsid.setFocusableInTouchMode(true);
        } else {
            isAPConfig = false;
//            tvStatus.setText("智能配网");
            wifiSsid.setText(strSSID);//设置当前的wifi
            if ("".equals(wifiPwd.getText().toString())) {
                wifiPwd.setText(dcsp.getString("wifi" + strSSID, ""));
            }
            wifiSsid.setFocusableInTouchMode(false);
        }
        wifiPwd.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        //密码可视
        cbLaws.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String psw = wifiPwd.getText().toString();

                if (isChecked) {
                    wifiPwd.setInputType(0x90);
                } else {
                    wifiPwd.setInputType(0x81);
                }
                wifiPwd.setSelection(psw.length());
            }
        });
        if (!NetworkUtils.whetherNetWorkIsWifi(mContext)) {
            Toast.makeText(this, getString(R.string.hiflying_smartlinker_no_wifi_connectivity), Toast.LENGTH_SHORT).show();
//            tvStatus.setText("WIFI尚未打开");
            initDialog();
        }
    }

    private void initDialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new
                        Intent(Settings.ACTION_WIFI_SETTINGS)); //直接进入手机中的wifi网络设置界面
            }
        };
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(getString(R.string.hiflying_smartlinker_no_wifi_connectivity));
        dialogBuilder.setPositiveButton(getString(R.string.connect_wifi), dialogClickListener);
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_next:
                prompt = true;
                if (checkUpResult == false) {
                    checkUpResult = true;
                }
                doConfigure();
                break;
        }
    }


    private void doConfigure() {
//        initConfigure();
        ssid = wifiSsid.getText().toString().trim();
        password = wifiPwd.getText().toString().trim();

        if (!NetworkUtils.whetherNetWorkIsWifi(mContext) && prompt) {
            Toast.makeText(this, getString(R.string.hiflying_smartlinker_no_wifi_connectivity), Toast.LENGTH_SHORT).show();
            checkUpResult = false;
            prompt = false;
        }
        if (ssid.equals("") && prompt) {
            Toast.makeText(this, getString(R.string.login_tip_password_wifi), Toast.LENGTH_SHORT).show();
            checkUpResult = false;
            prompt = false;
        }
        //wifi密码不限制必填
//        if (password.equals("") && prompt) {
//            Toast.makeText(this, getString(R.string.login_tip_password_empty), Toast.LENGTH_SHORT).show();
//            checkUpResult = false;
//            prompt = false;
//        }

        if (checkUpResult) {
            dcsp.putString("wifi"+ssid,password).commit();
            //进入配网过程
            Intent intent = new Intent(Add8266WifiActivity.this, AddZhujiGuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("ssid", ssid);
            bundle.putString("password", password);
            bundle.putString("apSsid", apSsid);
            bundle.putBoolean("isMainList", isMainList);
            bundle.putString(HeaterShadowInfo.type, getIntent().getStringExtra(HeaterShadowInfo.type));
            intent.putExtras(bundle);
            startActivityForResult(intent,5);
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==5&&resultCode==11){
            setResult(resultCode);
            finish();
        }else if (requestCode == 5 && resultCode == 8) {
            setResult(resultCode);
            finish();
        }
    }

    protected boolean isOpenLocationService(){
        //Android6.0以上扫描Wifi需要定位功能,引导用户打开定位服务开关
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (HiPermission.checkPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)){
                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                //有没有打开GPS或者网络定位时，引导用户打开
                if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    //此时再判断网络定位服务是否打开,如果还没打开就引导用户打开
                    if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                        new AlertDialog.Builder(this).setTitle(getString(R.string.hiflying_smartlinker_request_location))
                                .setMessage(R.string.hiflying_smartlinker_request_location_tip)
                                .setCancelable(false)
                                .setPositiveButton(getString(R.string.action_settings), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //跳转至位置开关设置界面
                                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivity(intent);
                                    }
                                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Add8266WifiActivity.this.finish();
                            }
                        }).show();
                    }else{
                        return true;//网络定位服务打开
                    }
                }else{
                    return true;//GPS定位服务打开
                }
            }else{
                HiPermission.create(mContext)
                        .animStyle(R.style.PermissionAnimFade)//设置动画
                        .checkSinglePermission(Manifest.permission.ACCESS_FINE_LOCATION,new PermissionCallback() {
                            @Override
                            public void onClose() {

                            }

                            @Override
                            public void onFinish() {

                            }

                            @Override
                            public void onDeny(String permission, int position) {
                                ToastUtil.longMessage(getString(R.string.hiflying_smartlinker_request_location_error));
                                finish();
                            }

                            @Override
                            public void onGuarantee(String permission, int position) {
                                initConfigure();
                            }
                        });
            }
        }
        return true;
    }

}

