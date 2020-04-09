package com.smartism.znzk.activity.camera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.p2p.core.P2PHandler;
import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.camera.P2PConnect;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.camera.RecordFile;
import com.smartism.znzk.fragment.RecordFilesFrag;
import com.smartism.znzk.global.AppConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.global.NpcCommon;

public class RecorFilesActivity extends BaseActivity {
    private Contact contact;
    private RecordFilesFrag filesFrag;
    RecordFile recordFile = null;
    String device;
    int device_id;
    int callType = 3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contact = (Contact) getIntent().getSerializableExtra("contact");
        device = getIntent().getStringExtra("device");
        device_id = getIntent().getIntExtra("deviceid", 0);
        setContentView(R.layout.activity_recor_files);
        fragmentManager = getSupportFragmentManager();
        Log.e(" P2PConnect.getCurrent_state()", "P2PConnect"+P2PConnect.getCurrent_state());
//        callDevice();
        String push_mesg = NpcCommon.mThreeNum;
        P2PHandler.getInstance().call(NpcCommon.mThreeNum, "0", true,
                Constants.P2P_TYPE.P2P_TYPE_MONITOR, "1", "1", push_mesg,
                AppConfig.VideoMode, contact.contactId, MainApplication.GWELL_LOCALAREAIP);
        Bundle args = new Bundle();
        args.putSerializable("contact", contact);
        args.putSerializable("file", recordFile);
        args.putInt("deviceid", device_id);
        args.putString("device", device);
        args.putBoolean("isEnforce", true);
        filesFrag = new RecordFilesFrag();
        filesFrag.setArguments(args);
        initView();
    }
    int connectType = Constants.ConnectType.P2PCONNECT;
    public void callDevice() {
        P2PConnect.setCurrent_state(P2PConnect.P2P_STATE_CALLING);
        P2PConnect.setCurrent_call_id(contact.contactId);

        String push_mesg = NpcCommon.mThreeNum
                + ":"
                + this.getResources()
                .getString(R.string.p2p_call_push_mesg);
        Log.e("dxsTest", "NpcCommon.mThreeNum-->" + NpcCommon.mThreeNum
                + "mContact.contactId-->" + contact.contactId
                + "connectType-->" + contact + "AppConfig.VideoMode-->"
                + AppConfig.VideoMode);
        if (connectType == Constants.ConnectType.RTSPCONNECT) {
            callType = 3;
            String ipAddress = "";
            String ipFlag = "";
            if (contact.ipadressAddress != null) {
                ipAddress = contact.ipadressAddress.getHostAddress();
                ipFlag = ipAddress.substring(ipAddress.lastIndexOf(".") + 1,
                        ipAddress.length());
            } else {

            }
            P2PHandler.getInstance().call(NpcCommon.mThreeNum, "0", true,
                    Constants.P2P_TYPE.P2P_TYPE_MONITOR, "1", "1", push_mesg,
                    AppConfig.VideoMode, contact.contactId, MainApplication.GWELL_LOCALAREAIP);
            // P2PHandler.getInstance().RTSPConnect(NpcCommon.mThreeNum,
            // mContact.contactPassword, true, callType, mContact.contactId,
            // ipFlag, push_mesg, ipAddress,AppConfig.VideoMode,rtspHandler);
        } else if (connectType == Constants.ConnectType.P2PCONNECT) {
            callType = 1;
            String ipAdress = FList.getInstance().getCompleteIPAddress(
                    contact.contactId);
            P2PHandler.getInstance().call(NpcCommon.mThreeNum, contact.contactPassword, true,
                    Constants.P2P_TYPE.P2P_TYPE_MONITOR, contact.contactId, ipAdress,
                    push_mesg, AppConfig.VideoMode, contact.contactId, MainApplication.GWELL_LOCALAREAIP);
        }
    }
    public void back(View v) {
        Intent monitor = new Intent();
        monitor.setClass(RecorFilesActivity.this, ApMonitorActivity.class);
        monitor.putExtra("contact", contact);
        monitor.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
        this.startActivity(monitor);
        finish();
    }
    FragmentManager fragmentManager;
    private void initView() {

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.recor_files_main, filesFrag);
        fragmentTransaction.commit();
    }

    @Override
    public int getActivityInfo() {
        return 0;
    }

    @Override
    protected int onPreFinshByLoginAnother() {
        return 0;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { //监控/拦截/屏蔽返回键
//            Intent monitor = new Intent();
//            monitor.setClass(this, ApMonitorActivity.class);
//            monitor.putExtra("contact", contact);
//            monitor.putExtra("connectType", Constants.ConnectType.P2PCONNECT);
//            startActivity(monitor);
            finish();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_MENU) {
            //监控/拦截菜单键
        } else if (keyCode == KeyEvent.KEYCODE_HOME) {
            //由于Home键为系统键，此处不能捕获，需要重写onAttachedToWindow()
        }
        return super.onKeyDown(keyCode, event);
    }
}
