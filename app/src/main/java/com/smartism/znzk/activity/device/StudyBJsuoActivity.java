package com.smartism.znzk.activity.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.DeviceUserInfo;
import com.smartism.znzk.domain.SmartLockInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.StringUtils;
import com.smartism.znzk.util.ToastUtil;
import com.smartism.znzk.util.WeakRefHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StudyBJsuoActivity extends ActivityParentActivity implements View.OnClickListener {
    private final int dHandler_timeout = 1, dHandler_keytype = 2, getdHandler_loadkeysuccess = 3;
    private LinearLayout layout_progress, layout_finishstudy;
    private TextView layout_progress_text;
    private EditText edit_keyname;
    private Button button_finishstudy, button_againrequest;
    private DeviceInfo deviceInfo;
    //    private JSONObject keyInfo;
    private String studyKeyId; //正在学习的钥匙ID
    private ZhujiInfo zhujiInfo;
    private RadioButton key_nomal, key_authority;
    private ListView key_list, user_list;
    private KeyRoleAdapter roleAdapter;
    private UsersAdapter usersAdapter;
    private List<KeyRole> roleList = new ArrayList<>();
    private List<SmartLockInfo> lockInfos;
    private KeyRole keyRole = null;//当前选中钥匙
    private List<DeviceUserInfo> userInfos = new ArrayList<DeviceUserInfo>();
    private List<DeviceUserInfo> orignUserInfos;
    private DeviceUserInfo adminInfo;//管理员用户
    private DeviceUserInfo userInfo = null; //当前选中用户
    private DeviceUserInfo updateInfo = null; //当前选中用
    private SmartLockInfo smartLockInfo;
    private List<DeviceUserInfo> operateUserInfos;
    private boolean isAdminKeyExsit;
    private long adminRoleId;

    private KeyRole adminKeyRole;
    private List<KeyRole> noAdminKeyRoles;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case getdHandler_loadkeysuccess:
                    lockInfos.clear();
                    lockInfos.addAll((Collection<? extends SmartLockInfo>) msg.obj);
                    break;
                case dHandler_timeout: //超时
                    defaultHandler.removeMessages(dHandler_timeout);
                    mContext.cancelInProgress();
                    layout_progress_text.setText(getString(R.string.timeout));
                    button_againrequest.setVisibility(View.VISIBLE);
                    break;
                case dHandler_keytype:
                    List<KeyRole> roles = (List<KeyRole>) msg.obj;
                    roleList.clear();
                    roleList.addAll(roles);
                    adminKeyRole = new KeyRole();
                    noAdminKeyRoles = new ArrayList<>();
                    //DeviceUserInfo [name=lock_num_admin, id=1, key=管理员钥匙,flag=false]
                    for (int i = 0; i < roleList.size(); i++) {
                        if (roleList.get(i).getName().equals(DeviceInfo.RoleKey.admin.value())) {
                            adminKeyRole = roleList.get(i);
//                            adminKeyRole.setFlag(true);
                        } else {
                            noAdminKeyRoles.add(roleList.get(i));
                        }
                    }


                    for (int i = 0; i < lockInfos.size(); i++) {
                        SmartLockInfo lockInfo = lockInfos.get(i);
                        if (lockInfo.getRoleKey() != null && lockInfo.getRoleKey().equals(DeviceInfo.RoleKey.admin.value())) {
                            isAdminKeyExsit = true;
                            adminRoleId = lockInfo.getRoleId();
                        }
                    }
                    if (smartLockInfo == null) {
                        if (isAdminKeyExsit) {
                            roleList.remove(adminKeyRole);
                        }
                    } else {
                        if (isAdminKeyExsit && smartLockInfo.getRoleId() != adminRoleId) {//编辑对象为非管理员钥匙  管理员钥匙不显示
                            roleList.remove(adminKeyRole);
                        }
                        //选中当前钥匙
                        for (int i = 0; i < roleList.size(); i++) {
                            if (roleList.get(i).getName().equals(smartLockInfo.getRoleKey())) {
                                keyRole = roleList.get(i);
                                roleList.get(i).setFlag(true);
                            }
                        }
                    }
                    JavaThreadPool.getInstance().excute(new LoadUsers(zhujiInfo.getId(), 10));
                    roleAdapter.notifyDataSetChanged();
                    break;
                case 10:
                    cancelInProgress();
                    userInfos.clear();
                    userInfos.addAll((Collection<? extends DeviceUserInfo>) msg.obj);
//                        lockInfos 钥匙列表

                    if (smartLockInfo == null) {
                        if (isAdminKeyExsit) {
                            userInfos.remove(adminInfo);
                        }
                        for (int i = 0; i < userInfos.size(); i++) {
                            DeviceUserInfo info = userInfos.get(i);
                            for (int j = 0; j < lockInfos.size(); j++) {
                                if (info.getId() == (lockInfos.get(j).getAppId())) {//只要钥匙列表里的钥匙有用户 要去掉(当前编辑的用户钥匙选中)
                                    userInfos.remove(info);
                                    noAdminInfos.remove(info);
                                    i--;
                                }
                            }
                        }
                    } else {

                        if ((smartLockInfo.getRoleKey() != null) && !smartLockInfo.getRoleKey().equals(DeviceInfo.RoleKey.admin.value())) {//编辑对象为非管理员钥匙  管理员钥匙不显示
                            userInfos.remove(adminInfo);
                            DeviceUserInfo needRemove = null;
                            for (int i = 0; i < userInfos.size(); i++) {
                                DeviceUserInfo info = userInfos.get(i);
                                for (int j = 0; j < lockInfos.size(); j++) {
                                    if (info.getId() == (lockInfos.get(j).getAppId())) {//只要钥匙列表里的钥匙有用户 要去掉(当前编辑的用户钥匙选中)
                                        if (smartLockInfo.getAppId() != 0 && smartLockInfo.getAppId() == info.getId()) {
                                            info.setFlag(true);
                                            userInfo = info;
                                            updateInfo = userInfo;
                                        } else {
                                            userInfos.remove(info);
                                            i--;
                                            noAdminInfos.remove(info);
                                        }
                                    }
                                }
                            }

                        } else {
                            for (int i = 0; i < userInfos.size(); i++) {
                                DeviceUserInfo info = userInfos.get(i);
                                for (int j = 0; j < lockInfos.size(); j++) {
                                    if (info.getId() == (lockInfos.get(j).getAppId())) {//只要钥匙列表里的钥匙有用户 要去掉(当前编辑的用户钥匙选中)
//                                        userInfos.remove(info);
                                        noAdminInfos.remove(info);
                                    }
                                }
                            }
                            userInfos.clear();
                            if (smartLockInfo.getAppId() != 0) {
                                userInfo = adminInfo;
                                adminInfo.setFlag(true);
                            }
                            userInfos.add(adminInfo);
                        }

                    }
                    usersAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);

    private BroadcastReceiver defaultReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context arg0, Intent intent) {
            if (Actions.REFRESH_DEVICES_LIST.equals(intent.getAction())) { // 数据刷新完成广播
            } else if (Actions.ACCETP_ONEDEVICE_MESSAGE.equals(intent.getAction())) { // 某一个设备的推送广播
                if (!StringUtils.isEmpty(intent.getStringExtra("device_id")) && deviceInfo.getId() == Long.parseLong(intent.getStringExtra("device_id"))) {
                    String data = (String) intent.getSerializableExtra("device_info");
                    if (data != null) {
                        JSONObject object = JSONObject.parseObject(data);
                        if ("2".equals(object.getString("sort"))) {
                            //发送成功等待回应
//                            Toast.makeText(mContext, getString(R.string.rq_control_sendsuccess),
//                                    Toast.LENGTH_SHORT).show();
//                            mContext.cancelInProgress();
//                            defaultHandler.removeMessages(dHandler_timeout);
                        } else if (object.containsKey("dt") && object.getIntValue("dt") == 61) {//61表示从机是否进入学习模式
                            defaultHandler.removeMessages(dHandler_timeout);
                            mContext.cancelInProgress();
//                            if (object.getIntValue("deviceCommand") == 1) {
                            layout_progress_text.setText(getString(R.string.activity_beijingsuo_intomodel));
                            defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 30 * 1000);
//                            } else {
//                                layout_progress_text.setText(getString(R.string.activity_beijingsuo_instudymodelfailed));
//                            }
                        } else if (object.containsKey("dt") && object.getIntValue("dt") == 46) {//46表示// 新增钥匙
                            defaultHandler.removeMessages(dHandler_timeout);
                            String deviceCommand = object.getString("deviceCommand");
                            if(deviceCommand.equals("-1")){
                                layout_progress_text.setText(getString(R.string.activity_beijingsuo_key_exits));
                            }else{
                                JavaThreadPool.getInstance().excute(new KeyRoleLoad());
                                studyKeyId = object.getString("deviceCommand");
                                layout_progress.setVisibility(View.GONE);
                                layout_finishstudy.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            } else if (Actions.CONNECTION_FAILED_SENDFAILED.equals(intent.getAction())) { // 发送失败
                mContext.cancelInProgress();
                Toast.makeText(mContext, getString(R.string.rq_control_sendfailed),
                        Toast.LENGTH_SHORT).show();
                defaultHandler.removeMessages(dHandler_timeout);
            } else if (Actions.SHOW_SERVER_MESSAGE.equals(intent.getAction())) { // 显示服务器信息
                defaultHandler.removeMessages(dHandler_timeout);
                mContext.cancelInProgress();
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(intent.getStringExtra("message"));
                } catch (Exception e) {
                    Log.w("DevicesList", "获取服务器返回消息，转换为json对象失败，用原始值处理");
                }
                if (resultJson != null) {
                    switch (resultJson.getIntValue("Code")) {
                        case 4:
                            Toast.makeText(mContext, getString(R.string.tips_4), Toast.LENGTH_SHORT).show();
                            break;
                        case 5:
                            Toast.makeText(mContext, getString(R.string.tips_5), Toast.LENGTH_SHORT).show();
                            break;
                        case 6:
                            Toast.makeText(mContext, getString(R.string.tips_6), Toast.LENGTH_SHORT).show();
                            break;
                        case 7:
                            Toast.makeText(mContext, getString(R.string.tips_7), Toast.LENGTH_SHORT).show();
                            break;
                        case 8:
                            Toast.makeText(mContext, getString(R.string.tips_8), Toast.LENGTH_SHORT).show();
                            break;

                        default:
                            Toast.makeText(mContext, "Unknown Info", Toast.LENGTH_SHORT).show();
                            break;
                    }

                } else {
                    Toast.makeText(mContext, intent.getStringExtra("message"), Toast.LENGTH_SHORT)
                            .show();

                }
            }
        }
    };
    private int totalSize;
    private List<DeviceUserInfo> noAdminInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_bjsuo);
        initData();
        initRegisterReceiver();
        initView();
        initListview();
    }

    /**
     * 选择改变选中图标
     *
     * @param position
     */
    public void changeKeySelectStatu(int position) {

        for (int i = 0; i < roleList.size(); i++) {
            roleList.get(i).setFlag(false);
        }
//        for (int i = 0; i < userInfos.size(); i++) {
//            userInfos.get(i).setFlag(false);
//        }
        keyRole = roleList.get(position);
        keyRole.setFlag(true);
        if (keyRole.getName().equals(DeviceInfo.RoleKey.admin.value())) {
            userInfos.clear();
            userInfos.add(adminInfo);
            adminInfo.setFlag(true);//默认选中
            userInfo = adminInfo;//当前角色管理员钥匙
        } else {
//            for (DeviceUserInfo info : operateUserInfos) {
//                if (info.getId() == adminInfo.getId()) {
//                    operateUserInfos.remove(info);
//                }
//            }
            userInfos.clear();
            if (smartLockInfo != null && smartLockInfo.getAppId() != 0 && updateInfo != null) {//代表编辑用户
                userInfo = updateInfo;
                userInfos.addAll(noAdminInfos);
            } else {
//                if (updateInfo!=null){
//                }
                userInfo = updateInfo;
//                if (userInfo != null && noAdminInfos != null) {
//                    for (int i = 0; i < noAdminInfos.size(); i++) {
//                        if (userInfo.getId() == noAdminInfos.get(i).getId()) {
//                            noAdminInfos.get(i).setFlag(false);
//                        }
//                    }
//                }
                userInfos.addAll(noAdminInfos);
            }
//            userInfo = null;//当前角色不是管理员  解决点了一下管理员再点其他角色 会自动分配

        }
        roleAdapter.notifyDataSetChanged();
        usersAdapter.notifyDataSetChanged();
        Log.e("TAG_!!!", "List_item:" + roleList.get(position).toString());
    }

    /**
     * 选择改变选中图标
     *
     * @param position
     */
    public void changeUserSelectStatu(int position) {
        userInfo = null;
        for (int i = 0; i < userInfos.size(); i++) {
            if (i != position) {
                userInfos.get(i).setFlag(false);
            }
        }
        userInfos.get(position).setFlag(!userInfos.get(position).isFlag());
        if (userInfos.get(position).isFlag()) {

            userInfo = userInfos.get(position);
            updateInfo = userInfo;
        } else {
            updateInfo = null;
        }
        usersAdapter.notifyDataSetChanged();
    }

    private void initListview() {
        roleAdapter = new KeyRoleAdapter();
        key_list.setAdapter(roleAdapter);
        key_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (roleList.get(position).isFlag()) return;
//                JavaThreadPool.getInstance().excute(new LoadUsers(zhujiInfo.getId(), 10));
                changeKeySelectStatu(position);
//                userInfos.clear();
            }
        });
        usersAdapter = new UsersAdapter();
        user_list.setAdapter(usersAdapter);
        user_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                changeUserSelectStatu(position);
            }
        });
    }

    private void initView() {
        key_list = (ListView) findViewById(R.id.key_list);
        user_list = (ListView) findViewById(R.id.user_list);
        layout_progress_text = (TextView) findViewById(R.id.layout_progress_text);
        layout_progress_text.setText(getString(R.string.activity_beijingsuo_instudymodel));
        layout_progress = (LinearLayout) findViewById(R.id.layout_progress);
        layout_finishstudy = (LinearLayout) findViewById(R.id.layout_finishstudy);
        button_finishstudy = (Button) findViewById(R.id.button_finishstudy);
        button_finishstudy.setOnClickListener(this);
        button_againrequest = (Button) findViewById(R.id.study_againrequest);
        button_againrequest.setOnClickListener(this);
        edit_keyname = (EditText) findViewById(R.id.edit_keyname);
        if (smartLockInfo != null) {
            JavaThreadPool.getInstance().excute(new KeyRoleLoad());
            layout_progress.setVisibility(View.GONE);
            layout_finishstudy.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.title_study)).setText(getString(R.string.activity_beijingsuo_updatekeytitle));
            edit_keyname.setText(smartLockInfo.getLname());
        } else {
            layout_progress.setVisibility(View.VISIBLE);
            layout_finishstudy.setVisibility(View.GONE);
            insertKeyStudyModel();
        }
        if (smartLockInfo != null) {
            button_finishstudy.setText(getString(R.string.activity_beijingsuo_submitupdate));
        } else {
            button_finishstudy.setText(getString(R.string.activity_beijingsuo_finishstudy));
        }
    }

    public boolean isHaveKey(DeviceUserInfo userInfo) {
        for (int i = 0; i < lockInfos.size(); i++) {
            if (lockInfos.get(i).getAppId() == userInfo.getId())
                return true;
        }
        return false;
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        View v = getCurrentFocus();
//        if ((v != null && (v instanceof EditText))) {
//            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//        }
//        return super.onTouchEvent(event);
//    }

    public boolean isShouldHideInput(View v, MotionEvent ev) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
            if (ev.getX() > left && ev.getX() < right && ev.getY() > top && ev.getY() < bottom) {
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private void HideSoftInput(IBinder token) {
        if (token != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (isShouldHideInput(view, ev)) {
                HideSoftInput(view.getWindowToken());
                view.clearFocus();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void initData() {
        orignUserInfos = new ArrayList<>();

        deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        zhujiInfo = DatabaseOperator.getInstance().queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
//        String keyinfo = getIntent().getStringExtra("keyinfo");
        smartLockInfo = (SmartLockInfo) getIntent().getParcelableExtra("keyinfo");
        lockInfos = getIntent().getParcelableArrayListExtra("keyinfos");
        if (lockInfos == null)
            lockInfos = new ArrayList<>();
//        initKeyList();
    }

    /**
     * 初始化钥匙列表
     */
    private void initKeyList() {
        JavaThreadPool.getInstance().excute(new LoadAllKeysInfo());
    }

    private class LoadAllKeysInfo implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", deviceInfo.getId());
            object.put("start", 0);
            object.put("size", 100);
            String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/dln/list", object, StudyBJsuoActivity.this);

            if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(StudyBJsuoActivity.this, getString(R.string.history_response_nodevice),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (result.length() > 4) {

                List<JSONObject> commands = new ArrayList<JSONObject>();
                JSONObject resultJson = null;
                try {
                    resultJson = JSON.parseObject(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                    return;
                }
                JSONArray array = resultJson.getJSONArray("result");

                resultJson = JSON.parseObject(result);

                totalSize = resultJson.getIntValue("total");


                List<SmartLockInfo> list;
                if (array == null || array.size() == 0) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                        }
                    });
                    return;
                }
                Log.e(TAG, array.toJSONString());
                list = JSON.parseArray(array.toJSONString(), SmartLockInfo.class);


                Message m = defaultHandler.obtainMessage(getdHandler_loadkeysuccess);
                m.obj = list;
                defaultHandler.sendMessage(m);
            }
        }
    }

    /**
     * 注册广播
     */
    private void initRegisterReceiver() {
        IntentFilter receiverFilter = new IntentFilter();
        receiverFilter.addAction(Actions.REFRESH_DEVICES_LIST);
        receiverFilter.addAction(Actions.ACCETP_ONEDEVICE_MESSAGE);
        receiverFilter.addAction(Actions.CONNECTION_FAILED_SENDFAILED);
        receiverFilter.addAction(Actions.SHOW_SERVER_MESSAGE);
        this.registerReceiver(defaultReceiver, receiverFilter);
    }

    /**
     * 进入锁学习模式，发送学习指令
     */
    private void insertKeyStudyModel() {
        layout_progress_text.setText(getString(R.string.activity_beijingsuo_instudymodel));
        button_againrequest.setVisibility(View.GONE);
        defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 15 * 1000);
        SyncMessage message = new SyncMessage();
        message.setCommand(SyncMessage.CommandMenu.rq_control.value());
        message.setDeviceid(deviceInfo.getId());
        // 操作 这里的操作写死，需要在服务器的配置页面中配置此按钮对应的指令是什么 1指令是进入学习模式,0退出学习模式
        message.setSyncBytes(new byte[]{0x01});
        SyncMessageContainer.getInstance().produceSendMessage(message);
    }

    public void back(View v) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (defaultReceiver != null) {
            mContext.unregisterReceiver(defaultReceiver);
        }
        defaultHandler.removeMessages(dHandler_timeout);

        //判断是否处于学习模式
        if(layout_progress.getVisibility()==View.VISIBLE){
            //退出学习
            SyncMessage message = new SyncMessage();
            message.setCommand(SyncMessage.CommandMenu.rq_control.value());
            message.setDeviceid(deviceInfo.getId());
            // 操作 这里的操作写死，需要在服务器的配置页面中配置此按钮对应的指令是什么 1指令是进入学习模式,0退出学习模式
            message.setSyncBytes(new byte[]{0x0});
            SyncMessageContainer.getInstance().produceSendMessage(message);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_finishstudy:
                int permission = 0;
                if ("".equals(edit_keyname.getText().toString())) {
                    Toast.makeText(StudyBJsuoActivity.this, getString(R.string.activity_beijingsuo_namemust), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (keyRole == null || keyRole.getId() == 0) {
                    Toast.makeText(StudyBJsuoActivity.this, getString(R.string.activity_beijingsuo_key_type), Toast.LENGTH_SHORT).show();
                    return;
                }
                defaultHandler.sendEmptyMessageDelayed(dHandler_timeout, 12 * 1000);
                showInProgress(getString(R.string.operationing), false, false);
                JavaThreadPool.getInstance().excute(new FinishKeyStudy(edit_keyname.getText().toString(), permission));
                break;
            case R.id.study_againrequest:
                insertKeyStudyModel();
                break;
        }
    }

    private class FinishKeyStudy implements Runnable {
        String name;
        int permission;

        public FinishKeyStudy(String name, int permission) {
            this.name = name;
            this.permission = permission;
        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", deviceInfo.getId());
            object.put("number", studyKeyId);
            object.put("type", 3);//钥匙
            object.put("lname", name);
            if (keyRole.getName().equals("lock_num_temp")) {
                object.put("permission", 0);
            } else {
                object.put("permission", 1);
            }
            object.put("roleId", keyRole.getId());
            object.put("appId", userInfo == null ? "0" : userInfo.getId());
            String urlproperties = "/jdm/s3/dln/add";
            if (smartLockInfo != null) {
                urlproperties = "/jdm/s3/dln/update";
                object.put("vid", smartLockInfo.getId());
                object.put("nname", name);
            }
            String result = HttpRequestUtils.requestoOkHttpPost( server + urlproperties, object, StudyBJsuoActivity.this);

            if (result != null && result.equals("0")) {
                defaultHandler.removeMessages(dHandler_timeout);
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(StudyBJsuoActivity.this, getString(R.string.success), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } else if (result != null && result.equals("-3")) {//主机不在线
                defaultHandler.removeMessages(dHandler_timeout);
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(StudyBJsuoActivity.this, getString(R.string.activity_zhuji_not),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (result != null && result.equals("-4")) {//添加失败
                defaultHandler.removeMessages(dHandler_timeout);
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(StudyBJsuoActivity.this, getString(R.string.activity_beijingsuo_study_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                defaultHandler.removeMessages(dHandler_timeout);
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(StudyBJsuoActivity.this, getString(R.string.net_error_weizhi),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    class KeyRole {
        private long id;
        private String name;//admin
        private String key;
        private boolean flag = false;

        public boolean isFlag() {
            return flag;
        }

        public void setFlag(boolean flag) {
            this.flag = flag;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return "DeviceUserInfo [name=" + name + ", id=" + id
                    + ", key=" + key + ",flag=" + flag + "]";
        }
    }

    private class KeyRoleLoad implements Runnable {

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("id", deviceInfo.getId());
            String urlproperties = "/jdm/s3/dln/role/list";
            String result = HttpRequestUtils.requestoOkHttpPost( server + urlproperties, object, StudyBJsuoActivity.this);
            if (result != null && result.contains("[")) {
                Log.e(TAG, "roleList:" + result.toString());
                List<KeyRole> roles = new ArrayList<>();
                JSONArray jsonArray = JSON.parseArray(result);
                for (int j = 0; j < jsonArray.size(); j++) {
                    KeyRole keyRole = new KeyRole();
                    JSONObject jsonObject = jsonArray.getJSONObject(j);
                    keyRole.setId(jsonObject.getLong("id"));
                    keyRole.setName(jsonObject.getString("key"));
                    keyRole.setKey(jsonObject.getString("name"));
                    roles.add(keyRole);
                }
                Message message = new Message();
                message.what = dHandler_keytype;
                message.obj = roles;
                defaultHandler.sendMessage(message);
            } else if (result != null && result.equals("-3")) {//主机不在线
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(StudyBJsuoActivity.this, getString(R.string.activity_zhuji_not), Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (result != null && result.equals("-4")) {//添加失败
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(StudyBJsuoActivity.this, getString(R.string.update_failed), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(StudyBJsuoActivity.this, getString(R.string.net_error_weizhi), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    class KeyRoleAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return roleList.size();
        }

        @Override
        public Object getItem(int position) {
            return roleList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MViewHolder holder = null;
            if (convertView == null) {
                holder = new MViewHolder();
                convertView = LayoutInflater.from(StudyBJsuoActivity.this).inflate(R.layout.activity_study_bjsuo_item, parent, false);
                holder.name = (TextView) convertView.findViewById(R.id.item_name);
                holder.select = (CheckBox) convertView.findViewById(R.id.select);
                convertView.setTag(holder);
            } else {
                holder = (MViewHolder) convertView.getTag();
            }
            holder.name.setText(roleList.get(position).getKey());
            holder.select.setChecked(roleList.get(position).isFlag());
            return convertView;
        }

        class MViewHolder {
            private TextView name;
            private CheckBox select;
        }
    }


    class UsersAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return userInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return userInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MViewHolder holder = null;
            if (convertView == null) {
                holder = new MViewHolder();
                convertView = LayoutInflater.from(StudyBJsuoActivity.this).inflate(R.layout.activity_study_bjsuo_item, parent, false);
                holder.name = (TextView) convertView.findViewById(R.id.item_name);
                holder.select = (CheckBox) convertView.findViewById(R.id.select);
                convertView.setTag(holder);
            } else {
                holder = (MViewHolder) convertView.getTag();
            }

            holder.name.setText(userInfos.get(position).getName());
            holder.select.setChecked(userInfos.get(position).isFlag());
            return convertView;
        }

        class MViewHolder {
            private TextView name;
            private CheckBox select;
        }
    }


    class LoadUsers implements Runnable {
        private long did;
        private int what;

        public LoadUsers(long did, int what) {
            this.did = did;
            this.what = what;
        }

        @Override
        public void run() {
            DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(StudyBJsuoActivity.this,
                    DataCenterSharedPreferences.Constant.CONFIG);
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("did", did);
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/u/all", pJsonObject, StudyBJsuoActivity.this);
            if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(StudyBJsuoActivity.this, getString(R.string.device_set_tip_nodevice),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if ("-5".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(StudyBJsuoActivity.this, getString(R.string.device_not_getdata),
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else if (!TextUtils.isEmpty(result) && result.length() > 4) {
                Log.e(TAG, "userInfos: " + result.toString());
                adminInfo = new DeviceUserInfo();
                noAdminInfos = new ArrayList<>();
                List<DeviceUserInfo> lists = JSON.parseArray(result, DeviceUserInfo.class);
                if (lists.size() > 0) {
                    for (DeviceUserInfo info : lists) {
                        if (info.getAdmin() == 1) {
                            adminInfo = info;
                        } else {
                            noAdminInfos.add(info);
                        }
                    }
                }
                Message message = Message.obtain();
                message.obj = lists;
                message.what = 10;
                defaultHandler.sendMessage(message);
            }

        }
    }
}
