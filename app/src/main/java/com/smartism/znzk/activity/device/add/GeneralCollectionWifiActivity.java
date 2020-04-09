package com.smartism.znzk.activity.device.add;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;
/*
* 主要想打造一个通用的Wifi手机页面
* title ：标题名
* need_exit:是否需要退出按钮  true需要
* exit_activity :退出按钮返回到的页面
* next_activity: 下一步按钮到达的页面
* */
public class GeneralCollectionWifiActivity extends MZBaseActivity implements View.OnClickListener {


    private String ssid;
    private int net_id ;
    private EditText tv_ssid ,edit_pwd;
    private Button next;
    private TextView mChangeWifiTv ;
    private String mTitle = "";//标题
    private Class mNextActivityClass ; //下一个跳转到的Activity的类名
    private boolean mNeedExit = false ; //是否需要退出按钮
    private Class mExitActivityClass ; //退出按钮跳转的Activity类名


    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                boolean isConnect = networkInfo.isConnected();
                if(isConnect){
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    String ssid = wifiManager.getConnectionInfo().getSSID() ;
                    net_id = wifiManager.getConnectionInfo().getNetworkId() ;
                    tv_ssid.setText(ssid.replaceAll("\"",""));//这里去掉了Wifi前后面的引号
                    //这里需要判断一下WiFi是否保存过，如果保存过，则直接显示密码
                    edit_pwd.setText(getWifiPassword(tv_ssid.getText().toString()));
                }
                edit_pwd.setSelection(edit_pwd.getText().toString().length());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            mNeedExit = getIntent().getBooleanExtra("need_exit",false);
            mTitle = getIntent().getStringExtra("title");
            mExitActivityClass  = (Class) getIntent().getSerializableExtra("exit_activity");
            mNextActivityClass = (Class) getIntent().getSerializableExtra("next_activity");
        }else{
            mNeedExit = savedInstanceState.getBoolean("need_exit",false);
            mTitle = savedInstanceState.getString("title");
            mExitActivityClass = (Class) savedInstanceState.getSerializable("exit_activity");
            mNextActivityClass = (Class) savedInstanceState.getSerializable("next_activity");
        }
        setTitle(mTitle==null?"":mTitle); //设置标题
        tv_ssid = findViewById(R.id.tv_ssid);
        edit_pwd = findViewById(R.id.edit_pwd);
        next = findViewById(R.id.next);
        mChangeWifiTv = findViewById(R.id.change_wifi_tv);

        mChangeWifiTv.setOnClickListener(this);
        next.setOnClickListener(this);


        //设置View的状态
        tv_ssid.setEnabled(false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("need_exit",mNeedExit);
        outState.putString("title",mTitle);
        outState.putSerializable("exit_activity",mExitActivityClass);
        outState.putSerializable("next_activity",mNextActivityClass);
        super.onSaveInstanceState(outState);
    }

    //显示退出菜单项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //需要退出按钮才加载退出菜单
        if(mNeedExit){
            getMenuInflater().inflate(R.menu.exit_menu,menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.exit_menu_item:
                if(mExitActivityClass!=null){
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setClass(getApplicationContext(), mExitActivityClass);
                    if(intent.resolveActivity(getPackageManager())!=null){
                        //存在Activity在进行跳转
                        startActivity(intent);
                    }
                }
                finish();
                return true ;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mReceiver,intentFilter);
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount()>0){
            getSupportFragmentManager().popBackStack();
            return ;
        }else{
            if(NavUtils.getParentActivityIntent(this)!=null){
                NavUtils.navigateUpFromSameTask(this);
            }
        }
        super.onBackPressed();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mReceiver!=null){
            mContext.unregisterReceiver(mReceiver);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.next:
                if (TextUtils.isEmpty(tv_ssid.getText().toString())) {
                    ToastUtil.shortMessage(getString(R.string.login_tip_password_wifi));
                    return;
                }
                if (TextUtils.isEmpty(edit_pwd.getText().toString())) {
                    ToastUtil.shortMessage(getString(R.string.input_password));
                    return;
                }
                if(edit_pwd.getText().toString().length()<8){
                    ToastUtil.shortMessage(getString(R.string.add_zhuji_by_ap_invaildate_password));
                    return ;
                }
                saveWifiInfo(tv_ssid.getText().toString(),edit_pwd.getText().toString());//保存一下Wifi账号和密码
                if(mNextActivityClass!=null){
                    intent.putExtra("ssid",handleSsid(tv_ssid.getText().toString()));
                    intent.putExtra("net_id",net_id);
                    intent.putExtra("password",edit_pwd.getText().toString());
                    intent.setClass(getApplicationContext(),mNextActivityClass);
                    if(intent.resolveActivity(getPackageManager())!=null){
                        startActivity(intent);
                    }
                }
                break ;
            case R.id.change_wifi_tv:
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
                    intent.setClassName("com.android.settings","com.android.settings.Settings$WifiSettingsActivity");
                }else{
                    intent.setClassName("com.android.settings","com.android.settings.wifi.WifiSettings");
                }
                startActivity(intent);
                break ;
        }
    }

    //需要去掉Wifi名前面的双引号
    private String getWifiPassword(String wifiName){
        String password = DataCenterSharedPreferences.getInstance(getApplicationContext(), DataCenterSharedPreferences.Constant.CONFIG).getString("wifi" + wifiName, "");
        return password ;
    }

    private boolean saveWifiInfo(String ssid,String password){
        return  DataCenterSharedPreferences.getInstance(getApplicationContext(),DataCenterSharedPreferences.Constant.CONFIG).putString("wifi"+ssid,password).commit();
    }
    //如果Wifi名含有\或者,那么在前面添加一个反斜杠
    private  static String handleSsid(String wifiName){
        String result = wifiName.replaceAll("\\\\","\\\\\\\\");
        result =  result.replaceAll(",","\\\\,");
        return  result;
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_general_collection_wifi;
    }
}
