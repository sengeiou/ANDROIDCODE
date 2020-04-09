package com.smartism.znzk.activity.device.add;


import android.content.*;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.domain.HeaterShadowInfo;
import com.smartism.znzk.udputil.UseUDPSendAndReceive;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;


public class AddZhujiByApSendActivity extends MZBaseActivity implements UseUDPSendAndReceive.OnUseUDPSendAndReceiveListener, View.OnClickListener{

    private String ssid;
    private int net_id ;
    EditText tv_ssid ,edit_pwd;
    Button next;


    private String dest_ip = "255.255.255.255";
    private int dest_post = 5000;
    private final int SEND_OUTTIME = 0x64;


    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SEND_OUTTIME:
                    ToastTools.short_Toast(getApplicationContext(),getString(R.string.connect_wifi_timeout));
                    break ;
            }
        }
    };

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)){
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                boolean isConnect = networkInfo.isConnected();
                next.setEnabled(false);
                next.setBackgroundResource(R.color.gray);
                if(isConnect){
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    String ssid = wifiManager.getConnectionInfo().getSSID() ;
                    if(ssid.contains("AP_CONNECT_")){
                        next.setEnabled(true);
                        next.setBackgroundResource(R.drawable.zhzj_dialog_info_btn);
                    }
                }
            }
        }
    };



    UseUDPSendAndReceive mTask ;


    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mReceiver,intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mReceiver!=null){
            mContext.unregisterReceiver(mReceiver);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null){
            ssid = getIntent().getStringExtra("ssid");
            net_id = getIntent().getIntExtra("net_id",net_id);
        }else{
            ssid = savedInstanceState.getString("ssid");
            net_id  = savedInstanceState.getInt("net_id");
        }
        setTitle(getString(R.string.connect));
        tv_ssid = findViewById(R.id.tv_ssid);
        edit_pwd = findViewById(R.id.edit_pwd);
        next = findViewById(R.id.next);

        if(ssid!=null){
            ssid = ssid.replaceAll("\"","");
            tv_ssid.setText(ssid);
            tv_ssid.setSelection(ssid.length());
        }

        mDialog.getDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(mTask!=null){
                    mTask.cancel(true);
                }
            }
        });
        next.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("ssid",ssid);
        outState.putInt("net_id",net_id);
        super.onSaveInstanceState(outState);
    }

    @Override
    public int setLayoutId() {
        return R.layout.activity_add_zhuji_by_ap_send_layout;
    }

    Intent intent ;
    @Override
    public void receiveResult(String result) {
        mHandler.removeMessages(SEND_OUTTIME);
        if(result!=null&&result.contains("CWJAP:OK")){
            //配置成功
            String master_id = result.split(":")[2];
            intent = new Intent();
            intent.setClass(this,AddZhujiByApConnectActivity.class);
            intent.putExtra(HeaterShadowInfo.type, getIntent().getStringExtra(HeaterShadowInfo.type));
            intent.putExtra("master_id",master_id);
            //连接网络，主要是防止用户对Wifi设置为不自动连接
            if(net_id>0) {
                WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                manager.enableNetwork(net_id, true);//连接设备上次的连接过的Wifi
            }
            startActivity(intent);
        }else{
            //设置失败
            ToastTools.short_Toast(this,getString(R.string.activity_editscene_set_falid));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.next:
                if(TextUtils.isEmpty(tv_ssid.getText().toString())){
                    Toast.makeText(this,getString(R.string.login_tip_password_wifi),Toast.LENGTH_SHORT).show();
                    return ;
                }
                if(TextUtils.isEmpty(edit_pwd.getText().toString())){
                    Toast.makeText(this,getString(R.string.input_password),Toast.LENGTH_SHORT).show();
                    return ;
                }
                mTask = new UseUDPSendAndReceive(this,dest_ip,dest_post);
                mHandler.sendEmptyMessageDelayed(SEND_OUTTIME,20*1000);
                StringBuilder sendData = new StringBuilder("AT+CWJAP=");
                sendData.append("\"")
                        .append(tv_ssid.getText().toString())
                        .append("\"")
                        .append(",")
                        .append("\"")
                        .append(edit_pwd.getText().toString())
                        .append("\"");
                mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,sendData.toString());
                break;
        }
    }
}
