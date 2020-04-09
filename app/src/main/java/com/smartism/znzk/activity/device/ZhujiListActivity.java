package com.smartism.znzk.activity.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.FragmentParentActivity;
import com.smartism.znzk.activity.device.add.AddZhujiActivity;
import com.smartism.znzk.activity.user.LoginActivity;
import com.smartism.znzk.adapter.ZhujiListAdapter;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.communication.service.CoreService;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.Util;
import com.smartism.znzk.util.WeakRefHandler;

import java.util.ArrayList;
import java.util.List;

public class ZhujiListActivity extends FragmentParentActivity implements ZhujiListAdapter.ZhujiListAdapterOnclick {
    private ListView zj_ListView;
    private List<ZhujiInfo> zhujiList;
    private ZhujiListAdapter adapter;
    private ZhujiListActivity mContext;
    private ImageView add;
    private Button back_btn;
    private boolean isToDeviceMian;//如果是剛進來，回退的是結束掉，而不是跳轉到設備列表
    private final static int
            dHandler_scenes = 1,
            dHandlerWhat_loadsuccess = 2;
    private FrameLayout frameLayout;
    private ZhujiListFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhuji_list);
        isToDeviceMian = getIntent().getBooleanExtra("isToDeviceMian", false);
        mContext = this;
        initView();
        initRegisterReceiver();
        initData();
        initEvent();
        initfragment();
    }
    private void initfragment() {
        fragment = new ZhujiListFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_zhuji_list, fragment);
        fragmentTransaction.commit();
    }
    public void initView() {
        add = (ImageView) findViewById(R.id.device_main_menu);
        zj_ListView = (ListView) findViewById(R.id.zhujilist_listview);
        back_btn = (Button) findViewById(R.id.back_btn);
    }

    Button login;
    ZhujiInfo zhuji;

    public void initData() {
        login = (Button) findViewById(R.id.login);
        if (mContext.dcsp != null) {
//            String masterID = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, "");
//            zhuji = DatabaseOperator.getInstance(mContext)
//                    .queryDeviceZhuJiInfo(masterID);
            //替换
            zhuji = DatabaseOperator.getInstance(getApplicationContext()).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
        }
        if (zhuji == null) {
            zhujiList = DatabaseOperator.getInstance(mContext).queryAllZhuJiInfos();
            if (!zhujiList.isEmpty()) {
                zhuji = zhujiList.get(0);
//                mContext.dcsp.putString(DataCenterSharedPreferences.Constant.APP_MASTERID, zhuji.getMasterid()).commit();
                //替换
                ZhujiListFragment.setMasterId(zhuji.getMasterid());
            }
        }
        zhujiList = DatabaseOperator.getInstance(mContext.getApplicationContext()).queryAllZhuJiInfos();
        if (zhujiList == null) zhujiList = new ArrayList<>();
//        adapter = new ZhujiListAdapter(zhujiList, this);
        zj_ListView.setAdapter(adapter);
    }

    public void initEvent() {
        adapter.setZhujiListAdapterOnclick(this);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isToDeviceMian) {
                    Intent intent = new Intent();
                    intent.setClass(getApplication(), DeviceMainActivity.class);
                    intent.putExtra("isNotCamera", false);
                    startActivity(intent);
                }
                finish();
            }
        });
        zj_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
//                mContext.dcsp.putString(DataCenterSharedPreferences.Constant.APP_MASTERID, zhujiList.get(position).getMasterid()).commit();
                //替换
                ZhujiListFragment.setMasterId(zhujiList.get(position).getMasterid());
                Intent intent = new Intent();
                intent.setClass(getApplication(), DeviceMainActivity.class);
                intent.putExtra("isNotCamera", false);
                startActivity(intent);
                finish();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.setClass(getApplicationContext(), AddZhujiActivity.class);
                startActivity(intent);
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    public void logout() {
        JavaThreadPool.getInstance().excute(new ExitLogin());
        // 跳转到登录页面，并且需要清空activity栈
        Intent refreshContans = new Intent();
        refreshContans.setAction(Constants.Action.ACTIVITY_FINISH);
        sendBroadcast(refreshContans);
        Intent loginIntent = new Intent();
        loginIntent.setClass(this, CoreService.class);
        stopService(loginIntent);
        loginIntent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        loginIntent.setClass(this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private class ExitLogin implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            String result = HttpRequestUtils
                    .requestoOkHttpPost(server + "/jdm/s3/u/logout", null, mContext);
            Util.clearLoginInfo(getApplicationContext(), dcsp); //清空用户登录信息
        }
    }

    @Override
    public void OnItemImgClickListener(int position, View view, int viewId) {
        ZhujiInfo zhujiInfo = zhujiList.get(position);
        //主机安防按钮点击回调
        if (MainApplication.app.getAppGlobalConfig().isClickDeviceItem() && !zhujiInfo.isOnline())
            return;//主机离线，设备列表不可点击
        switch (viewId) {
            case R.id.scene_arming:
                // 设防按钮点击
                mContext.showInProgress(getString(R.string.operationing), false, true);
                JavaThreadPool.getInstance().excute(new TriggerScenes(-1, zhujiInfo, position));
                break;
            case R.id.scene_disarming:
                // 撤防按钮点击
                mContext.showInProgress(getString(R.string.operationing), false, true);
                JavaThreadPool.getInstance().excute(new TriggerScenes(0, zhujiInfo, position));
                break;
            case R.id.scene_home:
                // 在家按钮点击
                mContext.showInProgress(getString(R.string.operationing), false, true);
                JavaThreadPool.getInstance().excute(new TriggerScenes(-3, zhujiInfo, position));
                break;
        }
    }


    /**
     * 更新数据
     */

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Actions.REFRESH_DEVICES_LIST.equals(intent.getAction())) { // 数据刷新完成广播
                List<ZhujiInfo> zhujis = DatabaseOperator.getInstance(mContext.getApplicationContext()).queryAllZhuJiInfos();
                if (zhujis != null) {
                    zhujiList.clear();
                    zhujiList.addAll(zhujis);
                }
                adapter.notifyDataSetChanged();
            }
        }
    };

    boolean isRegist = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null && isRegist) {
            isRegist = false;
            unregisterReceiver(receiver);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isRegist) {
            initRegisterReceiver();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void back(View v) {
        finish();
    }

    /**
     * 注册广播
     */
    private void initRegisterReceiver() {
        if (isRegist) return;
        isRegist = true;
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Actions.REFRESH_DEVICES_LIST);
        receiverFilter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        receiverFilter.addAction(Actions.CONNECTION_FAILED);
        receiverFilter.addAction(Actions.CONNECTION_ING);
        receiverFilter.addAction(Actions.CONNECTION_NONET);
        receiverFilter.addAction(Actions.CONNECTION_SUCCESS);
        receiverFilter.addAction(Actions.CONNECTION_FAILED_SENDFAILED);
        receiverFilter.addAction(Actions.SHOW_SERVER_MESSAGE);
        receiverFilter.addAction(Actions.ZHUJI_CHECKUPDATE);
        receiverFilter.addAction(Actions.ZHUJI_UPDATE);
        receiverFilter.addAction(Constants.Action.ACTION_NETWORK_CHANGE);
        mContext.registerReceiver(receiver, receiverFilter);

    }


    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case dHandler_scenes: // 场景操作完成
                    mContext.cancelInProgress();
                    int sId = msg.arg1;
                    int position =msg.arg2;
                    zhujiList.get(position).setScene(String.valueOf(sId));
                    adapter.notifyDataSetChanged();
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    class TriggerScenes implements Runnable {
        private int sId;
        private int position;
        private ZhujiInfo zhuji;

        public TriggerScenes() {
        }

        public TriggerScenes(int sId, ZhujiInfo zhuji, int position) {
            this.sId = sId;
            this.zhuji = zhuji;
            this.position = position;
        }

        @Override
        public void run() {

            JSONObject o = new JSONObject();
            o.put("id", sId);
            o.put("did", zhuji.getId());
            String server = mContext.dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            String result = HttpRequestUtils
                    .requestoOkHttpPost(server + "/jdm/s3/scenes/trigger", o, mContext);
            if ("0".equals(result)) {
                Message m = defaultHandler.obtainMessage(dHandler_scenes);
                m.arg1 = sId;
                m.arg2 = position;
                defaultHandler.sendMessage(m);
            } else {
                mContext.cancelInProgress();
                defaultHandler.post(new Runnable() {
                    public void run() {
                        mContext.cancelInProgress();
                        Toast.makeText(mContext, getString(R.string.net_error_operationfailed),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            //这里重写返回键
            if (isToDeviceMian) {
                Intent intent = new Intent();
                intent.setClass(getApplication(), DeviceMainActivity.class);
                intent.putExtra("isNotCamera", false);
                startActivity(intent);
            }
            finish();
            return true;
        }
        return false;
    }

}
