package com.smartism.znzk.xiongmai.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.xiongmai.lib.funsdk.support.FunSupport;
import com.smartism.znzk.xiongmai.lib.funsdk.support.OnFunDeviceWiFiConfigListener;
import com.smartism.znzk.xiongmai.lib.funsdk.support.models.FunDevice;
import com.smartism.znzk.xiongmai.lib.funsdk.support.utils.MyUtils;
import com.smartism.znzk.xiongmai.lib.funsdk.support.utils.StringUtils;

import java.util.List;

/*
* 主要任务，给摄像头配置快速Wifi
* 扫描周围Wifi获取当前设备连接到的Wifi信息
* 需要进行Wifi扫描适配
* */
public class NewXiongMaiQuickWifiConfig extends ActivityParentActivity implements View.OnClickListener,OnFunDeviceWiFiConfigListener {

    WifiManager mWifiManager ;
    EditText mEidtTextPwd ;  //Wifi密码控件
    ImageView mPwdIsSee ; //密码是否可见控件
    Button mCommitBtn;//开始验证按钮
    TextView mWifiName;//用户输入Wifi名
    ImageView mBackView ;//返回键
    boolean isScan = false; //是否可以扫描Wifi标志
    boolean isSeePwd = false; //密码是否可见，默认不可见
    String TAG = getClass().getSimpleName();


    BroadcastReceiver mWifiBroadcast  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean isSucess = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED,false);
            switch (action){
                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    List<ScanResult> list = mWifiManager.getScanResults();
                    if(list!=null&&list.size()>0){
                        ScanResult temp = null;
                        Log.v(TAG,"周围Wifi个数:"+list.size());
                        //扫描到了,判断用户输入的Wifi是否可用
                        for(ScanResult scanResult:list){
                            Log.v(TAG,scanResult.SSID);
                          if(scanResult.SSID.equals(mWifiName.getText().toString())){
                              temp = scanResult;
                          }
                        }
                        startQuickSetting(temp);
                    }
                    break;
            }
        }
    };

    private void startQuickSetting(ScanResult scanResult){
        try {
            if ( null == scanResult ) {
                ToastTools.short_Toast(this,"当前Wifi信息有误1");
                Log.v(TAG,"Wifi扫描结果为空,没有扫描到周围该Wifi信息");
                return;
            }
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            DhcpInfo wifiDhcp = mWifiManager.getDhcpInfo();

            if ( null == wifiInfo ) {
                ToastTools.short_Toast(this,"当前Wifi信息有误2");
                return;
            }

            String ssid = wifiInfo.getSSID().replace("\"", "");
            if ( StringUtils.isStringNULL(ssid) ) {
                ToastTools.short_Toast(this,"当前Wifi信息有误3");
                return;
            }

            int pwdType = MyUtils.getEncrypPasswordType(scanResult.capabilities);
            String wifiPwd = mEidtTextPwd.getText().toString().trim();

            if ( pwdType != 0 && StringUtils.isStringNULL(wifiPwd) ) {
                // 需要密码
                ToastTools.short_Toast(this,"当前Wifi信息有误4");
                return;
            }

            StringBuffer data = new StringBuffer();
            data.append("S:").append(ssid).append("P:").append(wifiPwd).append("T:").append(pwdType);

            String submask;
            if (wifiDhcp.netmask == 0) {
                submask = "255.255.255.0";
            } else {
                submask = MyUtils.formatIpAddress(wifiDhcp.netmask);
            }

            String mac = wifiInfo.getMacAddress();
            StringBuffer info = new StringBuffer();
            info.append("gateway:").append(MyUtils.formatIpAddress(wifiDhcp.gateway)).append(" ip:")
                    .append(MyUtils.formatIpAddress(wifiDhcp.ipAddress)).append(" submask:").append(submask)
                    .append(" dns1:").append(MyUtils.formatIpAddress(wifiDhcp.dns1)).append(" dns2:")
                    .append(MyUtils.formatIpAddress(wifiDhcp.dns2)).append(" mac:").append(mac)
                    .append(" ");

            showInProgress("正在配置中，请稍后",true,false);

            FunSupport.getInstance().startWiFiQuickConfig(ssid,
                    data.toString(), info.toString(),
                    MyUtils.formatIpAddress(wifiDhcp.gateway),
                    pwdType, 0, mac, -1);

//            FunWifiPassword.getInstance().saveWifiPassword(ssid, wifiPwd);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar_add);
        mEidtTextPwd = findViewById(R.id.edit_pwd);
        mPwdIsSee = findViewById(R.id.btn_see_password);
        mWifiName = findViewById(R.id.tv_ssid);
        mCommitBtn = findViewById(R.id.next);
        mBackView = findViewById(R.id.back_btn);

        mBackView.setOnClickListener(this);
        mCommitBtn.setOnClickListener(this);
        mPwdIsSee.setOnClickListener(this);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if(!mWifiManager.isWifiEnabled()){
            mWifiManager.setWifiEnabled(true);//打开Wifi
        }
        //设置当前Wifi名
        mWifiName.setText(mWifiManager.getConnectionInfo().getSSID().replace("\"",""));//这句很重要
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            //进行访问位置权限申请
           if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                   != PackageManager.PERMISSION_GRANTED){
               ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},99);
           }else{
               isScan = true;
           }
        }else{
            isScan = true;
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mWifiBroadcast,intentFilter);

        FunSupport.getInstance().registerOnFunDeviceWiFiConfigListener(this);//注册Wifi配置成功与否监听器
    }

    private void stopQuickSetting() {
        FunSupport.getInstance().stopWiFiQuickConfig();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mWifiBroadcast!=null){
            unregisterReceiver(mWifiBroadcast);
        }
        stopQuickSetting();//停止Wifi配置
        FunSupport.getInstance().removeOnFunDeviceWiFiConfigListener(this);//移除Wifi配置监听器
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 99:
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    isScan = true;
                }else{
                    Log.v(TAG,"不具备访问位置权限");
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.next:
                if(isScan) {
                   boolean bool =  mWifiManager.startScan();//扫描Wifi
                   if(bool){
                       Log.v(TAG,"开始扫描周围Wifi");
                       ToastTools.short_Toast(this,"开始扫描周围Wifi");
                   }else{
                       Log.v(TAG,"扫描周围Wifi信息请求失败");
                   }
                }
                break;
            case R.id.btn_see_password:
                if(!isSeePwd){
                    //设置为可见
                    mPwdIsSee.setImageResource(R.drawable.pwd_eye_gray);
                    mEidtTextPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    isSeePwd=true;
                }else{
                    //设置为不可见
                    mPwdIsSee.setImageResource(R.drawable.pwd_eye_blue);
                    mEidtTextPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    isSeePwd=false;
                }
                mEidtTextPwd.setSelection(mEidtTextPwd.getText().toString().length());//设置光标
                break;
            case R.id.back_btn:
                finish();
                break;
        }
    }

    @Override
    public void onDeviceWiFiConfigSetted(FunDevice funDevice) {
        cancelInProgress();
        if ( null != funDevice ) {
          //配置Wifi成功
            ToastTools.short_Toast(this,"给设备配置Wifi成功");
            Intent intent =new Intent(this,XiongMaiDisplayCameraActivity.class);
            startActivity(intent);
        }
    }
}
