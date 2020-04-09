package com.smartism.znzk.activity.device.add;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.domain.HeaterShadowInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;

/**
 * AP 配网，录入wifi信息
 */
public class AddZhujiByApCollectWifiActivity extends MZBaseActivity implements View.OnClickListener{


    private String ssid;
    private int net_id ;
    private EditText tv_ssid ,edit_pwd;
    private Button next;
    private TextView mChangeWifiTv ;


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
        setTitle(getString(R.string.add_zhuji_by_ap_name)); //设置标题
        tv_ssid = findViewById(R.id.tv_ssid);
        edit_pwd = findViewById(R.id.edit_pwd);
        next = findViewById(R.id.next);
        mChangeWifiTv = findViewById(R.id.change_wifi_tv);

        mChangeWifiTv.setOnClickListener(this);
        next.setOnClickListener(this);


        //设置View的状态
        tv_ssid.setEnabled(false);
    }

    //显示退出菜单项
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.exit_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.exit_menu_item:
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (Actions.VersionType.CHANNEL_QYJUNHONG.equals(MainApplication.app.getAppGlobalConfig().getVersion())
                        ||Actions.VersionType.CHANNEL_HCTZ.equals(MainApplication.app.getAppGlobalConfig().getVersion())) {
                    intent.setClass(getApplicationContext(), AddZhujiActivity.class);
                } else {
                    intent.setClass(getApplicationContext(), AddZhujiWayChooseActivity.class);
                }
                startActivity(intent);
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
    public int setLayoutId() {
        return R.layout.activity_add_zhuji_by_ap_collect_wifi;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.next:
                if (TextUtils.isEmpty(tv_ssid.getText().toString())) {
                    Toast.makeText(this, getString(R.string.login_tip_password_wifi), Toast.LENGTH_SHORT).show();
                    return;
                }
                //密码是可以为空的
//                if (TextUtils.isEmpty(edit_pwd.getText().toString())) {
//                    Toast.makeText(this, getString(R.string.input_password), Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                if(edit_pwd.getText().toString().length()<8){
//                    Toast.makeText(this, getString(R.string.add_zhuji_by_ap_invaildate_password), Toast.LENGTH_SHORT).show();
//                    return ;
//                }
                saveWifiInfo(tv_ssid.getText().toString(),edit_pwd.getText().toString());//保存一下Wifi账号和密码
                intent.putExtra("ssid",handleSsid(tv_ssid.getText().toString()));
                intent.putExtra("net_id",net_id);
                intent.putExtra("password",edit_pwd.getText().toString());
                intent.setClass(getApplicationContext(),AddZhujiByAPActivity.class);
                intent.putExtra(HeaterShadowInfo.type, getIntent().getStringExtra(HeaterShadowInfo.type));
                intent.putExtra("flags",1);
                startActivity(intent);
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


}
