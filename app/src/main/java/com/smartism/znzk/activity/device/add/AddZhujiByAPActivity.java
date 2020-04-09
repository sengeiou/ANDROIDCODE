package com.smartism.znzk.activity.device.add;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.lsemtmf.genersdk.tools.commen.ToastTools;
import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.domain.HeaterShadowInfo;
import com.smartism.znzk.fragment.APFragmentFirstTip;
import com.smartism.znzk.fragment.APFragmentSecondTip;
import com.smartism.znzk.udputil.UseUDPSendAndReceive;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.widget.customview.WithTextProgressDialog;
import com.smartism.znzk.zhicheng.activities.MZBaseActivity;

//AP配网
public class AddZhujiByAPActivity extends MZBaseActivity implements APFragmentFirstTip.OnAPFragmentFisrtTipListener
        ,APFragmentSecondTip.OnAPFragmentTwoTipListener,APFragmentSecondTip.NewOnApFragmentTwoTipListener, UseUDPSendAndReceive.OnUseUDPSendAndReceiveListener {

    private final static  String LOG_DEBUG = "AddZhujiByAPActivity";
    FragmentManager mFragmentManager ;
    private String mSsid ,mWifiPassword;
    private int mNetId ;
    private String dest_ip = "255.255.255.255";
    private int dest_post = 5000;
    private final int SEND_OUTTIME = 0x64;
    private int mCurrentDislpayFragment = -1; //0 APFragmentFirstTip 1APFragmentSecondTip   本来是两个连续的Fragment切换 IOS改了，拆散了，不想重新创建页面，

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SEND_OUTTIME:
                    if(mTask!=null){
                        mTask.cancel(true);//中断线程
                    }
                    ToastTools.short_Toast(getApplicationContext(),getString(R.string.connect_wifi_timeout));
                    break ;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.add_zhuji_by_ap_name)); //设置标题
        if(savedInstanceState==null){
            mSsid = getIntent().getStringExtra("ssid");
            mNetId = getIntent().getIntExtra("net_id",-1);
            mWifiPassword = getIntent().getStringExtra("password");
            mCurrentDislpayFragment = getIntent().getIntExtra("flags",-1);
            mFragmentManager = getSupportFragmentManager() ;
            if(mCurrentDislpayFragment==0){
                FragmentTransaction transaction = mFragmentManager.beginTransaction() ;
                transaction.add(R.id.fragment_contrainer,APFragmentFirstTip.getInstance(""));
                transaction.commit();
            }else if(mCurrentDislpayFragment==1){
                FragmentTransaction transaction = mFragmentManager.beginTransaction() ;
                transaction.add(R.id.fragment_contrainer,APFragmentSecondTip.getInstance(""));
                transaction.commit();
            }
        }else{
            mSsid = savedInstanceState.getString("ssid");
            mNetId = savedInstanceState.getInt("net_id");
            mWifiPassword = savedInstanceState.getString("password");
        }

        initSelfProgress();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("ssid",mSsid);
        outState.putInt("net_id",mNetId);
        outState.putString("password",mWifiPassword);
        super.onSaveInstanceState(outState);
    }

    WithTextProgressDialog mProgressView ;
    private void initSelfProgress(){
        mProgressView = new WithTextProgressDialog(this);
        mProgressView.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(mTask!=null){
                    mTask.cancel(true);
                }
            }
        });
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
    public void showProgress(String text) {
        mProgressView.setText(text);
        if(!mProgressView.isShowing()){
            mProgressView.show();
        }
    }

    @Override
    public void hideProgress() {
        if(mProgressView.isShowing()){
            mProgressView.dismiss();
        }
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
    public int setLayoutId() {
        return R.layout.activity_add_zhuji_by_ap_layout;
    }

    @Override
    public void apFirstFragmentNext() {

        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),AddZhujiByApCollectWifiActivity.class);
        intent.putExtra(HeaterShadowInfo.type, getIntent().getStringExtra(HeaterShadowInfo.type));
        startActivity(intent);
/*
        IOS又改咯，拆散了Fragment
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in_center,R.anim.fade_out_center,R.anim.fade_in_center,R.anim.fade_out_center);
        transaction.replace(R.id.fragment_contrainer, APFragmentSecondTip.getInstance(""));
        transaction.addToBackStack(null);
        transaction.commit();*/
    }

    @Override
    public void apSecondFragmentNext(String ssid,int netId) {
        Intent intent = new Intent();
        intent.putExtra("ssid",ssid);
        intent.putExtra("net_id",netId);
        Log.d(LOG_DEBUG,"ssid:"+ssid+"-net_id"+netId);
        intent.setClass(this,AddZhujiByApSendActivity.class);
        intent.putExtra(HeaterShadowInfo.type, getIntent().getStringExtra(HeaterShadowInfo.type));
        startActivity(intent);
    }

    UseUDPSendAndReceive mTask ;
    @Override
    public void newApSecondFragmentNext() {
/*        if(mTask!=null){
            hideProgress();
            mTask.cancel(true);
        }
        mTask = new UseUDPSendAndReceive(this,dest_ip,dest_post);
        mHandler.sendEmptyMessageDelayed(SEND_OUTTIME,25*1000);
        mTask.setProgressText(getString(R.string.add_zhuji_by_ap_send_wifiinfo_todevice));
        StringBuilder sendData = new StringBuilder("AT+CWJAP=");
        sendData.append("\"")
                .append(mSsid)
                .append("\"")
                .append(",")
                .append("\"")
                .append(mWifiPassword)
                .append("\"");
        mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,sendData.toString());*/

        Intent intent = new Intent();
        intent.setClass(getApplicationContext(),AddZhujiByApConnectActivity.class);
        intent.putExtra("net_id",mNetId);
        intent.putExtra("ssid",mSsid);
        intent.putExtra("password",mWifiPassword);
        intent.putExtra(HeaterShadowInfo.type, getIntent().getStringExtra(HeaterShadowInfo.type));
        startActivity(intent);
    }

    private Intent intent ;
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
            if(mNetId>=0) {
                WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                manager.enableNetwork(mNetId, true);//连接设备上次的连接过的Wifi
            }
            startActivity(intent);
        }else{
            //设置失败
            ToastTools.short_Toast(this,getString(R.string.activity_editscene_set_falid));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
