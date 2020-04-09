package com.smartism.znzk.activity.camera;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.jwkj.soundwave.SoundWaveManager;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.device.add.Add8266WifiActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.hipermission.HiPermission;
import com.smartism.znzk.hipermission.PermissionCallback;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.util.camera.Utils;
import com.smartism.znzk.view.zbarscan.ScanCaptureActivity;
import com.smartism.znzk.widget.NormalDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 雷达添加
 *
 * @author 2016年08月05日 update by 王建
 */
public class RadarAddActivity extends BaseActivity implements OnClickListener {
    private Context mContext;
    String ssid;
    int type;
    int mLocalIp;
    Button bt_next;
    TextView tv_ssid, add_camera;
    EditText edit_pwd;
    ImageView back_btn;
    boolean bool1, bool2, bool3, bool4;
    private byte mAuthMode;
    private byte AuthModeAutoSwitch = 2;
    private byte AuthModeOpen = 0;
    private byte AuthModeShared = 1;
    private byte AuthModeWPA = 3;
    private byte AuthModeWPA1PSKWPA2PSK = 9;
    private byte AuthModeWPA1WPA2 = 8;
    private byte AuthModeWPA2 = 6;
    private byte AuthModeWPA2PSK = 7;
    private byte AuthModeWPANone = 5;
    private byte AuthModeWPAPSK = 4;
    boolean isRegFilter = false;
    private LinearLayout rlPwd;
    private boolean isWifiOpen = false;
    private int i;
    private int isCameraList;
    boolean isPlaintextpassword = false;
    private ImageView seePwd;
    private RelativeLayout rlSeePw, title;
    private ImageView scanner;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        mContext = this;

        boolean isSuccess = SoundWaveManager.init(this);//初始化声波配置
        //i = getIntent().getIntExtra("int", 0);
        if (MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_JUJIANG)) {
            i = getIntent().getIntExtra("int", 0);
            isCameraList = getIntent().getIntExtra("isCameraList", 0);
        } else {
            i = getIntent().getIntExtra("int", 0);
            isCameraList = getIntent().getIntExtra("isCameraList", 0);

        }
        setContentView(R.layout.activity_radar_add);
        initComponent();
        regFilter();

        initLoadBrand();

        if(MainApplication.app.getAppGlobalConfig().isShowAddJWCamera()){
            add_camera.setVisibility(View.VISIBLE);
        }else{
            add_camera.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isOpenLocationService()) {
            currentWifi();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void initComponent() {
        add_camera = (TextView) findViewById(R.id.add_camera);
        tv_ssid = (TextView) findViewById(R.id.tv_ssid);
        edit_pwd = (EditText) findViewById(R.id.edit_pwd);
        back_btn = (ImageView) findViewById(R.id.back_btn);
        bt_next = (Button) findViewById(R.id.next);
        rlPwd = (LinearLayout) findViewById(R.id.layout_pwd);
        seePwd = (ImageView) findViewById(R.id.btn_see_password);
        rlSeePw = (RelativeLayout) findViewById(R.id.rl_see_password);
        title = (RelativeLayout) findViewById(R.id.layout_title);
        scanner = (ImageView) findViewById(R.id.scanner);
        scanner.setOnClickListener(this);
        rlSeePw.setOnClickListener(this);
        bt_next.setOnClickListener(this);
        back_btn.setOnClickListener(this);
        add_camera.setOnClickListener(this);
        if (MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_JUJIANG)) {
            //巨将添加逻辑—》配网完成后扫描二维码
            title.setVisibility(View.VISIBLE);
        }
    }

    public void regFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.Action.RADAR_SET_WIFI_FAILED);   //wifi设置失败
        filter.addAction(Constants.Action.RADAR_SET_WIFI_SUCCESS);    //wifi设置成功
        registerReceiver(br, filter);
        isRegFilter = true;

    }

    BroadcastReceiver br = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(
                    Constants.Action.RADAR_SET_WIFI_FAILED)) {
                //没有连接时，弹出提示框
                NormalDialog dialog = new NormalDialog(mContext);
                dialog.setOnButtonCancelListener(new NormalDialog.OnButtonCancelListener() {
                    @Override
                    public void onClick() {
                        // TODO Auto-generated method stub
                        finish();
                    }
                });
                dialog.showConnectFail();
            } else if (intent.getAction().equals(
                    Constants.Action.RADAR_SET_WIFI_SUCCESS)) {
                //设置wifi成功，退出当前
                finish();
            }
        }
    };

    public void currentWifi() {
        //获取wifi管理对象
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!manager.isWifiEnabled())
            return;
        //获取本机已经连接的wifi的信息
        WifiInfo info = manager.getConnectionInfo();
        //信号强度
        ssid = info.getSSID();
        //ip地址
        mLocalIp = info.getIpAddress();
        Log.e("ssid", ssid);
        List<ScanResult> datas = new ArrayList<ScanResult>();
        if (!manager.isWifiEnabled())
            return;
        manager.startScan();
        //获取wifi的所有集合
        datas = manager.getScanResults();
        if (ssid == null) {
            return;
        }
        if (ssid.equals("")) {
            return;
        }
        int a = ssid.charAt(0);
        if (a == 34) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        if (!ssid.equals("<unknown ssid>") && !ssid.equals("0x")) {
            tv_ssid.setText(ssid);
            if ("".equals(edit_pwd.getText().toString())) {
                edit_pwd.setText(DataCenterSharedPreferences.getInstance(mContext, DataCenterSharedPreferences.Constant.CONFIG).getString("wifi" + ssid, ""));
            }
        }
        for (int i = 0; i < datas.size(); i++) {
            ScanResult result = datas.get(i);
            if (!result.SSID.equals(ssid)) {
                continue;
            }
            if (Utils.isWifiOpen(result)) {
                type = 0;
                isWifiOpen = true;
                rlPwd.setVisibility(View.GONE);
            } else {
                type = 1;
                isWifiOpen = false;
                rlPwd.setVisibility(View.VISIBLE);
            }
            bool1 = result.capabilities.contains("WPA-PSK");
            bool2 = result.capabilities.contains("WPA2-PSK");
            bool3 = result.capabilities.contains("WPA-EAP");
            bool4 = result.capabilities.contains("WPA2-EAP");
            if (result.capabilities.contains("WEP")) {
                this.mAuthMode = this.AuthModeOpen;
            }
            if ((bool1) && (bool2)) {
                mAuthMode = AuthModeWPA1PSKWPA2PSK;
            } else if (bool2) {
                this.mAuthMode = this.AuthModeWPA2PSK;
            } else if (bool1) {
                this.mAuthMode = this.AuthModeWPAPSK;
            } else if ((bool3) && (bool4)) {
                this.mAuthMode = this.AuthModeWPA1WPA2;
            } else if (bool4) {
                this.mAuthMode = this.AuthModeWPA2;
            } else {
                if (!bool3)
                    break;
                this.mAuthMode = this.AuthModeWPA;
            }

        }

    }

    @Override
    public int getActivityInfo() {
        // TODO Auto-generated method stub
        return Constants.ActivityInfo.ACTIVITY_RARDARADDACTIVITY;
    }

    @Override
    protected int onPreFinshByLoginAnother() {
        return 0;
    }

    BottomSheetDialog mBottomSheetDialog ;
    private  void initLoadBrand(){
        mBottomSheetDialog = new BottomSheetDialog(this);
        View view_group =getLayoutInflater().inflate(R.layout.xm_jiwei_by_sn_add_layout,null);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,getResources().getDisplayMetrics().heightPixels/4);
        view_group.setLayoutParams(lp);
        View xm_tv = view_group.findViewById(R.id.xiongmai_tv);
        View other_tv = view_group.findViewById(R.id.other_tv);
        mBottomSheetDialog.setContentView(view_group);
        ViewGroup viewGroup = (ViewGroup) view_group.getParent();
        viewGroup.setBackgroundColor(Color.parseColor("#00000000"));

        View.OnClickListener listener = new  View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(RadarAddActivity.this, AddContactActivity.class);
                switch (v.getId()){
                    case R.id.xiongmai_tv:
                        intent.putExtra("int", 9);
                        break ;
                    case R.id.other_tv:
                        intent.putExtra("int", i);
                        intent.putExtra("isCameraList", isCameraList);
                        intent.putExtra("isMainList",getIntent().getBooleanExtra("isMainList",false));
                        break ;
                }
                startActivity(intent);
                mBottomSheetDialog.dismiss();
            }

        };
        other_tv.setOnClickListener(listener);
        xm_tv.setOnClickListener(listener);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.add_camera:
                intent.setClass(RadarAddActivity.this, AddContactActivity.class);
                intent.putExtra("int", i);
                intent.putExtra("isCameraList", isCameraList);
                intent.putExtra("isMainList",getIntent().getBooleanExtra("isMainList",false));
                startActivity(intent);
                break;
            case R.id.next:
                InputMethodManager manager = (InputMethodManager) getSystemService(mContext.INPUT_METHOD_SERVICE);
                if (manager != null) {
                    manager.hideSoftInputFromWindow(edit_pwd.getWindowToken(), 0);
                }
                String wifiPwd = edit_pwd.getText().toString();
                if (ssid == null || ssid.equals("")) {
                    T.showShort(mContext, R.string.please_choose_wireless);
                    return;
                }
                if (ssid.equals("<unknown ssid>")) {
                    T.showShort(mContext, R.string.please_choose_wireless);
                    return;
                }
                if (!isWifiOpen) {
                    if (null == wifiPwd || wifiPwd.length() <= 0
                            && (type == 1 || type == 2)) {
                        T.showShort(mContext, R.string.please_input_wifi_password);
                        return;
                    }
                }
                DataCenterSharedPreferences.getInstance(mContext, DataCenterSharedPreferences.Constant.CONFIG).putString("wifi"+ssid,wifiPwd).commit();
                // 设置好wifi以后，跳转到等待界面
                Intent device_network = new Intent();
                // 设置好wifi以后，跳转到等待界面
//			if (MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_JUJIANG)){
//				device_network.setClass(mContext, ReadyGuideActivity.class);
//			}else {
//				device_network.setClass(mContext, AddWaitActicity.class);
//			}
                device_network.setClass(mContext, ReadyGuideActivity.class);
                device_network.putExtra("isMainList", getIntent().getBooleanExtra("isMainList", false));
                device_network.putExtra("ssidname", ssid);
                device_network.putExtra("wifiPwd", wifiPwd);
                device_network.putExtra("type", mAuthMode);
                device_network.putExtra("LocalIp", mLocalIp);
                device_network.putExtra("isNeedSendWifi", true);
                device_network.putExtra("int", i);
                device_network.putExtra("isCameraList", isCameraList);
                startActivity(device_network);
                finish();
                break;
            case R.id.back_btn:
                finish();
                break;
            case R.id.scanner:
                intent.setClass(RadarAddActivity.this, ScanCaptureActivity.class);
                intent.putExtra("int", i);
                intent.putExtra("isCameraList", isCameraList);
                startActivity(intent);
                finish();
                break;
            case R.id.rl_see_password:
                if (isPlaintextpassword) {
                    isPlaintextpassword = false;
                    seePwd.setImageResource(R.drawable.zhzj_eye_c);
                    edit_pwd.setTransformationMethod(PasswordTransformationMethod
                            .getInstance());

                } else {
                    isPlaintextpassword = true;
                    seePwd.setImageResource(R.drawable.zhzj_eye_o);
                    edit_pwd.setTransformationMethod(HideReturnsTransformationMethod
                            .getInstance());
                }
                edit_pwd.setSelection(edit_pwd.getText().length());
                // case R.id.choose_wifi:
                // Intent wifi = new Intent(mContext, WifiListActivity.class);
                // startActivity(wifi);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
//        SoundWaveManager.onDestroy(this); 会崩溃，demo中也无destory
        super.onDestroy();
        if (isRegFilter == true) {
            unregisterReceiver(br);
            isRegFilter = false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    protected boolean isOpenLocationService(){
        //Android6.0以上扫描Wifi需要定位功能,引导用户打开定位服务开关
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (HiPermission.checkPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)){
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
                                RadarAddActivity.this.finish();
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
                                currentWifi();
                            }
                        });
            }
        }
        return true;
    }
}
