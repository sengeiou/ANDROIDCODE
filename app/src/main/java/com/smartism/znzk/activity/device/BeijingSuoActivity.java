package com.smartism.znzk.activity.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.FragmentParentActivity;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.domain.CommandInfo;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.SmartLockInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;
import com.smartism.znzk.xiongmai.fragment.XMFragment;
import com.smartism.znzk.zhicheng.tasks.LoadZhujiAndDeviceTask;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BeijingSuoActivity extends FragmentParentActivity implements View.OnClickListener {
    private final int dHandler_timeout = 1, getdHandler_loadkeysuccess = 2;
    private ListView listView;
    private KeysAdapter keysApter;
    private TextView rMenu, title, percentage;
    private View headView;
    private ImageButton bj_head_add;
    private DeviceInfo deviceInfo;
    private ZhujiInfo zhujiInfo;
    private ArrayList<JSONObject> keysList;
    private AlertView shouquan;
    private List<SmartLockInfo> lockInfos;
    private ImageView low_vol, mTipImageView;
    private FrameLayout mCameraParentLayout;
    private boolean flag = false;
    private Contact mContact;
    private CameraInfo mCameraInfo;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case getdHandler_loadkeysuccess: // 获取数据成功
                    cancelInProgress();

                    lockInfos.clear();
                    lockInfos.addAll((List<SmartLockInfo>) msg.obj);
                    keysApter.notifyDataSetChanged();

                    if (lockInfos != null && lockInfos.size() > 0 && flag) {
                        flag = false;
                        initDeviceCommands();
                    }
//                    keysList.clear();
//                    keysList.addAll((List<JSONObject>) msg.obj);
//                    keysApter.notifyDataSetChanged();
                    break;
                case dHandler_timeout: //超时
                    defaultHandler.removeMessages(dHandler_timeout);
                    mContext.cancelInProgress();
                    Toast.makeText(mContext.getApplicationContext(), getString(R.string.timeout), Toast.LENGTH_SHORT).show();
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
                if (intent.getStringExtra("device_id") != null && deviceInfo.getId() == Long.parseLong(intent.getStringExtra("device_id"))) {
                    try {
                        DeviceInfo deviceInfo1 = DatabaseOperator.getInstance().queryDeviceInfo(deviceInfo.getId());
                        deviceInfo = deviceInfo1;
                        low_vol.setImageResource(deviceInfo1.isLowb() ? R.drawable.bjs_didianliang : R.drawable.bjs_dianliang_zhengchang);
                        String data = intent.getStringExtra("device_info");
                        if (data != null) {
                            JSONObject object = JSONObject.parseObject(data);
                            if (CommandInfo.CommandTypeEnum.reqLockNumberAuthorization.value().equals(object.getString("dt")) && !TextUtils.isEmpty(object.getString("dtv"))) {
                                if(zhujiInfo!=null&&zhujiInfo.isAdmin()){
                                    showAuthorizeConfirm(object.getString("dtv"));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
    private LinearLayout linearLayout;
    private boolean mIsSuoAlarm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beijing_suo);
        if (savedInstanceState == null) {
            mIsSuoAlarm = getIntent().getBooleanExtra("suo_alarm", false);
            deviceInfo = (DeviceInfo) getIntent().getSerializableExtra("device");
        } else {
            deviceInfo = (DeviceInfo) savedInstanceState.getSerializable("device");
            mIsSuoAlarm = savedInstanceState.getBoolean("suo_alarm", false);
        }
        initView();
        initData();
        initRegisterReceiver();

        judgeBindCamera(savedInstanceState);//判断是否有摄像头
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("suo_alarm", mIsSuoAlarm);
        outState.putSerializable("device", deviceInfo);
        super.onSaveInstanceState(outState);
    }

    private void judgeBindCamera(final Bundle savedInstanceState) {
        if (!StringUtils.isEmpty(deviceInfo.getBipc()) && !"0".equals(deviceInfo.getBipc())) {

            //说明绑定了摄像头，雄迈作为特殊的主机
            new LoadZhujiAndDeviceTask().queryZhujiInfos(new LoadZhujiAndDeviceTask.ILoadResult<List<ZhujiInfo>>() {
                @Override
                public void loadResult(List<ZhujiInfo> result) {
                    List<CameraInfo> cameraInfos = new ArrayList<>();
                    for (ZhujiInfo zhujiInfo : result) {
                        if (zhujiInfo.getCameraInfo().getC() == null) {
                            continue;
                        }
                        if (zhujiInfo.getCa().equals(DeviceInfo.CaMenu.ipcamera.value())
                                && (zhujiInfo.getCameraInfo().getC().equals(CameraInfo.CEnum.xiongmai.value()))) {
                            if (!cameraInfos.contains(zhujiInfo.getCameraInfo())) {
                                cameraInfos.add(zhujiInfo.getCameraInfo());
                            }
                        }
                    }

                    if (!cameraInfos.isEmpty()) {
                        for (CameraInfo cs : cameraInfos) {
                            if (cs.getIpcid() == Long.parseLong(deviceInfo.getBipc())) {
                                mCameraInfo = cs;
                                break;
                            }
                        }
                    }

                    if (mCameraInfo != null) {
                        mCameraParentLayout.setVisibility(View.VISIBLE);
                        mTipImageView.setVisibility(View.GONE);
                        if (savedInstanceState != null) {
                            return; //表明Activity是由于异常情况被重新创建
                        }
                        //添加摄像头显示Fragment
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        XMFragment xmFragment = XMFragment.newInstance(mCameraInfo, mIsSuoAlarm);
                        fragmentTransaction.add(R.id.camera_display_parent, xmFragment);
                        fragmentTransaction.commit();
                    }


                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initKeyList();
    }

    private void initData() {
        flag = true;
        lockInfos = new ArrayList<>();
        low_vol.setImageResource(deviceInfo.isLowb() ? R.drawable.bjs_didianliang : R.drawable.bjs_dianliang_zhengchang);
        zhujiInfo = DatabaseOperator.getInstance(this).queryDeviceZhuJiInfo(deviceInfo.getZj_id());
        if (zhujiInfo.getRolek().equals("lock_num_guest") || zhujiInfo.getRolek().equals("lock_num_temp") || zhujiInfo.getRolek().equals("lock_num_baby")) {
            //客人、小孩、临时不可查看历史纪录
            rMenu.setVisibility(View.GONE);
            listView.setVisibility(View.GONE);
        }
        title.setText(deviceInfo.getName());
        keysList = new ArrayList<>();
        keysApter = new KeysAdapter(this);
        listView.setAdapter(keysApter);
        if (zhujiInfo.isAdmin() || zhujiInfo.getRolek().equals("lock_num_admin")) {
            bj_head_add.setVisibility(View.VISIBLE);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                    new AlertView(null, null, getString(R.string.cancel), null, new String[]{getString(R.string.edit), getString(R.string.delete)},
                            mContext, AlertView.Style.ActionSheet, new OnItemClickListener() {
                        @Override
                        public void onItemClick(Object o, int p) {
                            switch (p) {
                                case 0:
                                    Intent intent = new Intent();
                                    intent.setClass(getApplication(), StudyBJsuoActivity.class);
                                    intent.putExtra("device", deviceInfo);
                                    intent.putExtra("keyinfo", lockInfos.get(position - 1));
                                    intent.putParcelableArrayListExtra("keyinfos", (ArrayList<? extends Parcelable>) lockInfos);//钥匙列表
                                    startActivity(intent);
                                    break;
                                case 1:
                                    showInProgress(getString(R.string.operationing), false, true);
                                    JavaThreadPool.getInstance().excute(new DelKey(lockInfos.get(position - 1).getId()));
                                    break;
                                default:
                                    break;
                            }
                        }
                    }).show();

                    itemPosition = position;
                    return true;
                }
            });
        }
//        initDeviceCommands();
    }

    private BJSMenuPopupWindow popupWindow;
    private int itemPosition = -1;

    public class BJSMenuPopupWindow extends PopupWindow {

        private View mMenuView;
        private Button btn_deldevice, btn_setdevice;

        public BJSMenuPopupWindow(Context context, View.OnClickListener itemsOnClick) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mMenuView = inflater.inflate(R.layout.zss_item_menu, null);
            btn_deldevice = (Button) mMenuView.findViewById(R.id.btn_deldevice);
            btn_setdevice = (Button) mMenuView.findViewById(R.id.btn_setdevice);

            btn_deldevice.setOnClickListener(itemsOnClick);
            btn_setdevice.setOnClickListener(itemsOnClick);
            //设置SelectPicPopupWindow的View
            this.setContentView(mMenuView);
            //设置SelectPicPopupWindow弹出窗体的宽
            this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            //设置SelectPicPopupWindow弹出窗体的高
            this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            //设置SelectPicPopupWindow弹出窗体可点击
            this.setFocusable(true);
            //设置SelectPicPopupWindow弹出窗体动画效果
            this.setAnimationStyle(R.style.Devices_list_menu_Animation);
            //实例化一个ColorDrawable颜色为半透明
            ColorDrawable dw = new ColorDrawable(0x00000000);
            //设置SelectPicPopupWindow弹出窗体的背景
            this.setBackgroundDrawable(dw);
            //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
            mMenuView.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {

                    int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                    int y = (int) event.getY();
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (y < height) {
                            dismiss();
                        }
                    }
                    return true;
                }
            });
            this.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss() {
                    WindowManager.LayoutParams parms = getWindow().getAttributes();
                    parms.alpha = 1.0f;
                    getWindow().setAttributes(parms);

                }
            });

        }


        public void updateDeviceMenu(Context context) {
            btn_setdevice.setText(context.getResources().getString(R.string.check));
            btn_deldevice.setText(context.getResources().getString(R.string.zss_item_del));
        }

    }

    private void initDeviceCommands() {
        if (zhujiInfo != null && !zhujiInfo.isAdmin())
            return;
        List<CommandInfo> commandInfos = DatabaseOperator.getInstance(this).queryAllCommands(deviceInfo.getId());
        if (commandInfos != null && commandInfos.size() > 0) {
            for (CommandInfo comm : commandInfos) {
                if (comm.getCtype().equals(CommandInfo.CommandTypeEnum.reqLockNumberAuthorization.value()) && !StringUtils.isEmpty(comm.getCommand())) { //有需要授权的编号并且在两小时内
                    showAuthorizeConfirm(comm.getCommand());
                }
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

    private void initView() {
        linearLayout = (LinearLayout) findViewById(R.id.activity_beijing_suo);
        low_vol = (ImageView) findViewById(R.id.low_vol);
        headView = LayoutInflater.from(this).inflate(R.layout.beijingsuo_headview, null, false);
        bj_head_add = (ImageButton) headView.findViewById(R.id.bj_head_add);
        bj_head_add.setOnClickListener(this);
        listView = (ListView) findViewById(R.id.beijingsuo_list);
        percentage = (TextView) findViewById(R.id.percentage);
        rMenu = (TextView) findViewById(R.id.menu_tv);
        title = (TextView) findViewById(R.id.title);
        listView.addHeaderView(headView);
        rMenu.setOnClickListener(this);

        mTipImageView = findViewById(R.id.tip_img);
        mCameraParentLayout = findViewById(R.id.camera_display_parent);//摄像头父布局
        //  popupWindow = new BJSMenuPopupWindow(this, this);


    }

    public void back(View v) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_tv:
                Intent intent1 = new Intent();
                intent1.setClass(this, BeiJingSuoHistoryActivity.class);
                intent1.putExtra("device", deviceInfo);
                startActivity(intent1);
                break;
            case R.id.bj_head_add:
                Intent intent = new Intent(this, StudyBJsuoActivity.class);
                intent.putExtra("device", deviceInfo);
                intent.putParcelableArrayListExtra("keyinfos", (ArrayList<? extends Parcelable>) lockInfos);
//                intent.putExtra("keyinfos", (Serializable) keysList);
                startActivity(intent);
                break;
            case R.id.btn_setdevice:
                popupWindow.dismiss();
                Intent intent2 = new Intent();
                intent2.setClass(getApplication(), StudyBJsuoActivity.class);
                intent2.putExtra("device", deviceInfo);
//                                    intent.putExtra("keyinfo", keysList.get(position - 1).toJSONString());
//                                    intent.putExtra("keyinfos", (Serializable) keysList);//钥匙列表
                intent2.putExtra("keyinfo", lockInfos.get(itemPosition - 1));
                intent2.putParcelableArrayListExtra("keyinfos", (ArrayList<? extends Parcelable>) lockInfos);//钥匙列表
                startActivity(intent2);

                break;
            case R.id.btn_deldevice:
                popupWindow.dismiss();

                showInProgress(getString(R.string.operationing), false, true);
//                                    JavaThreadPool.getInstance().excute(new DelKey(keysList.get(position - 1).getLongValue("id")));
                JavaThreadPool.getInstance().excute(new DelKey(lockInfos.get(itemPosition - 1).getId()));

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (defaultReceiver != null) {
            mContext.unregisterReceiver(defaultReceiver);
        }
    }

    /**
     * 显示授权窗口
     */
    private void showAuthorizeConfirm(final String key) {
        try {
            JSONObject object = JSON.parseObject(key);
            if (object != null) {
                if (shouquan == null || !shouquan.isShowing()) {
                    shouquan = new AlertView(getString(R.string.activity_beijingsuo_reqkeyauthtitle), object.getString("lname"),
                            null,
                            new String[]{getString(R.string.activity_beijingsuo_reqkeyauth), getString(R.string.activity_beijingsuo_notauth)}, null,
                            mContext, AlertView.Style.Alert,
                            new OnItemClickListener() {

                                @Override
                                public void onItemClick(Object o, int position) {
                                    if (position == 0) {
                                        //开始授权
                                        mContext.showInProgress(mContext.getString(R.string.operationing), false, true);
                                        JavaThreadPool.getInstance().excute(new AuthorseKey(1));
                                    } else if (position == 1) {
                                        //取消授权
                                        mContext.showInProgress(mContext.getString(R.string.operationing), false, true);
                                        JavaThreadPool.getInstance().excute(new AuthorseKey(0));
                                    }
                                }
                            });
                    shouquan.show();
                }
            }
        } catch (Exception ex) {
            //防止key不是json崩溃
        }

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
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/dln/list", object, BeijingSuoActivity.this);

            if ("-3".equals(result)) {
                defaultHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(BeijingSuoActivity.this, getString(R.string.history_response_nodevice),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else if (result.length() > 4) {
//
                JSONObject resultJson = null;
                resultJson = JSON.parseObject(result);

                JSONArray array = resultJson.getJSONArray("result");

                totalSize = resultJson.getIntValue("total");


                List<SmartLockInfo> list;
                if (array == null || array.size() == 0) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            lockInfos.clear();
                            keysApter.notifyDataSetChanged();
                        }
                    });
//                        Toast.makeText(mContext, "无数据", Toast.LENGTH_SHORT).show();
                    return;
                }
                list = JSON.parseArray(array.toJSONString(), SmartLockInfo.class);
                //排序
                Collections.sort(list, new Comparator<SmartLockInfo>() {
                    //根据roleId进行排序，但是临时钥匙和客户钥匙不允许
                    @Override
                    public int compare(SmartLockInfo o1, SmartLockInfo o2) {
                        //临时钥匙排最后,当角色未知时，未知钥匙，也排后面
                        if(o1.getRoleKey()==null || o1.getRoleKey().equals("lock_num_temp")){
                            return 1;
                        }
                        if(o2.getRoleKey()==null ||o2.getRoleKey().equals("lock_num_temp")){
                            return -1 ;
                        }
                        int result = 0 ;
                        long tmp = o1.getRoleId()-o2.getRoleId();
                        if(tmp>0){
                            result = 1 ;
                        }else if(tmp<0){
                            result=-1;
                        }
                        return result;
                    }
                });
                // 请求成功了，需要刷新数据到页面，也需要清除此设备的历史未读记录
                Message m = defaultHandler.obtainMessage(getdHandler_loadkeysuccess);
                m.obj = list;
                defaultHandler.sendMessage(m);
            }
        }
    }

    /**
     * 指纹锁钥匙列表
     */
    class KeysAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater;

        public KeysAdapter(Context context) {
            this.layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return lockInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return lockInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHande hande = null;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.item_beijingsuo_keylist, null, false);
                hande = new ViewHande(convertView);
                convertView.setTag(hande);
            } else {
                hande = (ViewHande) convertView.getTag();
            }
            hande.setValue(lockInfos.get(position));
//            hande.setValue(keysList.get(position));
            return convertView;
        }

        class ViewHande {
            private ImageView logo;
            private TextView name, type, user;

            public ViewHande(View view) {
                logo = (ImageView) view.findViewById(R.id.item_beijingsuo_logo);
                user = (TextView) view.findViewById(R.id.item_beijingsuo_user);
                name = (TextView) view.findViewById(R.id.item_beijingsuo_name);
                type = (TextView) view.findViewById(R.id.item_beijingsuo_type);
            }

            public void setValue(SmartLockInfo info) {
                user.setText(info.getAppName());
                name.setText(info.getLname());
                type.setText(info.getRoleName());
                type.setTextColor((info.getRoleKey() != null && info.getRoleKey().equals(DeviceInfo.RoleKey.temp.value())) ? getResources().getColor(R.color.runlong_temp) : getResources().getColor(R.color.black));
                logo.setImageResource((info.getRoleKey() != null && info.getRoleKey().equals(DeviceInfo.RoleKey.temp.value())) ? R.drawable.icon_linshiyaoshi : R.drawable.icon_yaoshi);

            }
        }
    }

    private class AuthorseKey implements Runnable {
        int author = 0;

        public AuthorseKey(int author) {
            this.author = author;
        }

        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            object.put("did", deviceInfo.getId());
            JSONArray array = new JSONArray();
            JSONObject o = new JSONObject();
            o.put("vkey", CommandInfo.CommandTypeEnum.authorizationLockNumber.value());
            o.put("value", author);
            array.add(o);
            object.put("vkeys", array);
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/p/set", object, BeijingSuoActivity.this);

            if (result != null && result.equals("0")) {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(BeijingSuoActivity.this, getString(R.string.success),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(BeijingSuoActivity.this, getString(R.string.operator_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private class DelKey implements Runnable {
        long id = 0;

        public DelKey(long id) {
            this.id = id;
        }

        @Override
        public void run() {
            String server = dcsp.getString(
                    DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
            JSONObject object = new JSONObject();
            JSONArray array = new JSONArray();
            JSONObject object1 = new JSONObject();
            object1.put("vid", id);
            array.add(object1);
            object.put("vids", array);
            object.put("did", deviceInfo.getId());
            String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/dln/del", object, BeijingSuoActivity.this);

            if (result != null && result.equals("0")) {
                initKeyList();
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(BeijingSuoActivity.this, getString(R.string.success),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(BeijingSuoActivity.this, getString(R.string.operator_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
