package com.smartism.znzk.activity.scene;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceTimerInfo;
import com.smartism.znzk.domain.FoundInfo;
import com.smartism.znzk.domain.SceneInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.CollectionsUtils;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.camera.T;
import com.smartism.znzk.view.SwitchButton.SwitchButton;
import com.smartism.znzk.view.alertview.AlertView;
import com.smartism.znzk.view.alertview.OnItemClickListener;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by JP on 2017-12-16
 * 设备定时列表
 */

public class ScenceArmingDisarmingActivity extends ActivityParentActivity implements View.OnClickListener {
    public static final String TAG = ScenceArmingDisarmingActivity.class.getSimpleName();
    private ImageView btn_add;
    private TextView tv_morn;
    private Context mContext;
    private String deviceId;
    private ListView mListView;
    //    private List<DeviceTimerInfo> timerInfos;
    private MyAdapter adapter;
    private SortTime sortTime;
    private boolean status = false;
    private int itemPosition = -1;
    private buttomMenuPopupWindow popupWindow;
    private RelativeLayout relativeLayout;
    private View headerView;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    mHandler.removeMessages(10);
                    cancelInProgress();
                    List<FoundInfo> sceneInfos = (List<FoundInfo>) msg.obj;
                    sceneList.clear();
                    sceneList.addAll(sceneInfos);
                    adapter.notifyDataSetChanged();
                    break;
                case 10:
                    cancelInProgress();
                    T.showShort(mContext, R.string.timeout);
                    break;
            }
            return false;
        }
    };
    private Handler mHandler = new WeakRefHandler(mCallback);

    private List<DeviceTimerInfo.DeviceControlInfo> infos;
    private ZhujiInfo zhuji;
    private List<FoundInfo> sceneList;
    private List<SceneInfo> securityItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arming_disarming_scence);
        mContext = this;
//        zhuji = DatabaseOperator.getInstance(ScenceArmingDisarmingActivity.this)
//                .queryDeviceZhuJiInfo(dcsp.getString(Constant.APP_MASTERID, ""));
        zhuji = DatabaseOperator.getInstance(ScenceArmingDisarmingActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
        initView();
        initData();
    }

    private void initData() {
        infos = new ArrayList<>();
        sortTime = new SortTime();
        tv_morn.setOnClickListener(this);
        showInProgress(getString(R.string.loading), false, true);
        mHandler.sendEmptyMessageDelayed(15, 10 * 1000);
        JavaThreadPool.getInstance().excute(new SceneLoad());
//        showInProgress(getString(R.string.loading), false, true);
    }

    public void enable(int s, final SwitchButton btnSwitch, final int position) {
        final int status = s;
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(ScenceArmingDisarmingActivity.this,
                        DataCenterSharedPreferences.Constant.CONFIG);
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("id", sceneList.get(position).getId());
                pJsonObject.put("s", status);
                final String result = HttpRequestUtils.requestoOkHttpPost(server + "/jdm/s3/scenes/status", pJsonObject, ScenceArmingDisarmingActivity.this);
                if ("0".equals(result)) {
                    JavaThreadPool.getInstance().excute(new SceneLoad());
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            if (status == 1) {
                                Toast.makeText(ScenceArmingDisarmingActivity.this, getString(R.string.activity_editscene_enable),
                                        Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(ScenceArmingDisarmingActivity.this, getString(R.string.activity_editscene_disable),
                                        Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                } else if ("-3".equals(result)) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            btnSwitch.setChecked(status == 1 ? false : true);
                            Toast.makeText(ScenceArmingDisarmingActivity.this, getString(R.string.activity_editscene_s_erro),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            btnSwitch.setChecked(status == 1 ? false : true);
                            Toast.makeText(ScenceArmingDisarmingActivity.this, getString(R.string.net_error_requestfailed),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private String[] times;

    private void initView() {
        sceneList = new ArrayList<>();
//        securityItems = new ArrayList<>();
        securityItems = (List<SceneInfo>) getIntent().getSerializableExtra("securityItems");
        times = new String[]{
                getString(R.string.monday),
                getString(R.string.tuesday),
                getString(R.string.wednesday),
                getString(R.string.thursday),
                getString(R.string.friday),
                getString(R.string.saturday),
                getString(R.string.sunday),
                getString(R.string.everyday)};
//        headerView = LayoutInflater.from(this).inflate(R.layout.activity_djkzq_item_header, null);

//        TextView textView = (TextView) headerView.findViewById(R.id.tv_status);
//        textView.setText("类型");
        relativeLayout = (RelativeLayout) findViewById(R.id.ll_layout);
        tv_morn = (TextView) findViewById(R.id.text_morn);
//        switch_morn = (SwitchButton) findViewById(switch_morn);
        btn_add = (ImageView) findViewById(R.id.btn_add);
        btn_add.setOnClickListener(this);
        adapter = new MyAdapter();
        mListView = (ListView) findViewById(R.id.lv_clock);
//        mListView.addHeaderView(headerView, null, false);

        mListView.setAdapter(adapter);
        mListView.setEmptyView(findViewById(R.id.tv_empty));
        popupWindow = new buttomMenuPopupWindow(this, this);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//                itemPosition = position - 1;
                itemPosition = position;
                popupWindow.updateDeviceMenu(mContext);
                popupWindow.showAtLocation(relativeLayout, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
                return true;
            }
        });
    }

    public class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return sceneList.size();
        }

        @Override
        public Object getItem(int position) {
            return sceneList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int arg0, View view, ViewGroup parent) {
            MyHolder myHolder = null;
            if (view == null) {
                myHolder = new MyHolder();
                view = LayoutInflater.from(ScenceArmingDisarmingActivity.this).inflate(R.layout.activity_djkzq_item, null);
                myHolder.tv = (TextView) view.findViewById(R.id.tv_tel);
                myHolder.tv_status = (TextView) view.findViewById(R.id.tv_status);
                myHolder.tv_date = (TextView) view.findViewById(R.id.tv_date);
                myHolder.btn_switch = (SwitchButton) view.findViewById(R.id.btn_switch);
                view.setTag(myHolder);
            } else {
                myHolder = (MyHolder) view.getTag();
            }
            int time = 0, hour = 0, min = 0;
            String date = "";
            FoundInfo foundInfo = sceneList.get(arg0);


            if (!CollectionsUtils.isEmpty(foundInfo.getTriggerInfos())) {
                FoundInfo.TriggerInfosEntity triggerInfosEntity = foundInfo.getTriggerInfos().get(0);
                time = triggerInfosEntity.getTime();
                date = triggerInfosEntity.getCycle();
                hour = time / 60;
                min = time - hour * 60;

                String hour1 = (hour < 10) ? "0" + hour : String.valueOf(hour);
                String min1 = (min < 10) ? "0" + min : String.valueOf(min);

                myHolder.tv.setText(hour1 + ":" + min1);
            }
            Log.e(TAG, "getCycle :" + date);
            if (!CollectionsUtils.isEmpty(foundInfo.getControlInfos())) {
                FoundInfo.ControlInfosEntity controlInfosEntity = foundInfo.getControlInfos().get(0);
                if (!CollectionsUtils.isEmpty(securityItems)) {
                    for (SceneInfo sceneInfo : securityItems) {
                        if (controlInfosEntity.getDeviceId().equals(String.valueOf(sceneInfo.getId())))
                            myHolder.tv_status.setText(sceneInfo.getName());
                    }
                }
            }
            if (!TextUtils.isEmpty(date)) {
                if (date.equals("1111111")) {
                    myHolder.tv_date.setText(getString(R.string.everyday));
                } else {
                    initCycle(myHolder.tv_date, date);
                }
            }
            setTiming(myHolder.btn_switch, arg0);
            return view;
        }

        //根据传回的code解析出重复的日期
        public void initCycle(TextView tv_date, String cycle) {
            char[] codes = cycle.toCharArray();
            StringBuffer sb = new StringBuffer();
            if (codes != null && codes.length > 1) {
                for (int i = 0; i < codes.length; i++) {
                    if (String.valueOf(codes[i]).equals("1")) {
                        sb.append(times[i]);
                        sb.append(",");
                    }
                }
                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                tv_date.setText(sb.toString());
            }
        }

        private void setTiming(final SwitchButton btn_switch, final int arg0) {

            btn_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    itemPosition = arg0 - 1;
                    enable(isChecked ? 1 : 0, btn_switch, arg0);
                }
            });
            btn_switch.setCheckedImmediatelyNoEvent(sceneList.get(arg0).getStatus() == 0 ? false : true);
        }

        class MyHolder {
            public TextView tv, tv_status, tv_date;
            public SwitchButton btn_switch;
        }
    }


    public class SortTime implements Comparator {

        @Override
        public int compare(Object lhs, Object rhs) {
            return ((DeviceTimerInfo) lhs).getTime() - ((DeviceTimerInfo) rhs).getTime();
        }
    }


    public void back(View v) {
        finish();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            showInProgress(getString(R.string.loading), false, true);
            mHandler.sendEmptyMessageDelayed(15, 10 * 1000);
            JavaThreadPool.getInstance().excute(new SceneLoad());
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.text_morn:
                break;
            case R.id.btn_add:
                if (CollectionsUtils.isEmpty(securityItems))
                    return;
                intent.putExtra("securityItems", (Serializable) securityItems);
                intent.setClass(this, ScenceArmingDisarmingEditActicity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.btn_deldevice:
                popupWindow.dismiss();
                new AlertView(getString(R.string.deviceslist_server_leftmenu_deltitle),
                        getString(R.string.qwq_clock),
                        getString(R.string.deviceslist_server_leftmenu_delcancel),
                        new String[]{getString(R.string.deviceslist_server_leftmenu_delbutton)}, null,
                        mContext, AlertView.Style.Alert,
                        new OnItemClickListener() {

                            @Override
                            public void onItemClick(Object o, final int position) {
                                if (position != -1) {
                                    showInProgress(getString(R.string.deviceslist_server_leftmenu_deltips), false, true);
                                    JavaThreadPool.getInstance().excute(new Runnable() {

                                        @Override
                                        public void run() {

                                            String server = dcsp.getString(
                                                    DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                                            JSONObject object = new JSONObject();
                                            object.put("id", sceneList.get(itemPosition).getId());
                                            server = server + "/jdm/s3/scenes/del";
                                            String result = HttpRequestUtils.requestoOkHttpPost(server, object, ScenceArmingDisarmingActivity.this);
                                            // -1参数为空，0删除成功
                                            if (result != null && result.equals("0")) {
                                                mHandler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        cancelInProgress();
                                                        Toast.makeText(ScenceArmingDisarmingActivity.this, getString(R.string.device_del_success), Toast.LENGTH_SHORT).show();
                                                        sceneList.remove(itemPosition);
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                });

                                            }
                                        }
                                    });
                                }
                            }
                        }).show();
                break;
            case R.id.btn_setdevice:
                popupWindow.dismiss();
                if (CollectionsUtils.isEmpty(securityItems)) {
                    return;
                }
                Intent intent2 = new Intent(this, ScenceArmingDisarmingEditActicity.class);
                intent2.putExtra("securityItems", (Serializable) securityItems);
                intent2.putExtra("foundInfo", sceneList.get(itemPosition));
                startActivityForResult(intent2, 1);
                break;
        }

    }

    /**
     * 底部菜单，长按定时项出现编辑
     */
    public class buttomMenuPopupWindow extends PopupWindow {

        private View mMenuView;
        private TextView btn_deldevice, btn_setdevice;

        public buttomMenuPopupWindow(Context context, View.OnClickListener itemsOnClick) {
            super(context);
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mMenuView = inflater.inflate(R.layout.djkzq_item_menu, null);
            btn_deldevice = (TextView) mMenuView.findViewById(R.id.btn_deldevice);
            btn_setdevice = (TextView) mMenuView.findViewById(R.id.btn_setdevice);

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

        }


        public void updateDeviceMenu(Context context) {
            btn_setdevice.setText(context.getResources().getString(R.string.check));
            btn_deldevice.setText(context.getResources().getString(R.string.zss_item_del));
        }

    }

    class SceneLoad implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
//            String mId = dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, "");
            //替换
            String mId = ZhujiListFragment.getMasterId() ;
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("m", mId);
//            pJsonObject.put("type", 6);
            final String result = HttpRequestUtils
                    .requestoOkHttpPost(server + "/jdm/s3/scenes/all", pJsonObject, ScenceArmingDisarmingActivity.this);
            List<FoundInfo> sceneInfos = new ArrayList<FoundInfo>();
            Log.e(TAG, "scenceLoad :" + result + "");
            if (!StringUtils.isEmpty(result) && result.startsWith("[")) {
                JSONArray ll = null;
                try {
                    ll = JSON.parseArray(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                }
                if (ll == null) {
                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(ScenceArmingDisarmingActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                if (CollectionsUtils.isEmpty(securityItems)) {
                    securityItems = new ArrayList<>();
                    for (int j = 0; j < ll.size(); j++) {
                        SceneInfo info = new SceneInfo();
                        if (((JSONObject) ll.get(j)).getIntValue("type") == 4) {
                            info.setId(((JSONObject) ll.get(j)).getLongValue("id"));
                            info.setName(((JSONObject) ll.get(j)).getString("name"));
                            info.setType(((JSONObject) ll.get(j)).getIntValue("type"));
                            securityItems.add(info);
                        } else if (((JSONObject) ll.get(j)).getIntValue("type") == 5) {
                            info.setId(((JSONObject) ll.get(j)).getLongValue("id"));
                            info.setName(((JSONObject) ll.get(j)).getString("name"));
                            info.setType(((JSONObject) ll.get(j)).getIntValue("type"));
                            securityItems.add(info);
                        } else if (((JSONObject) ll.get(j)).getIntValue("type") == 3) {
                            info.setId(((JSONObject) ll.get(j)).getLongValue("id"));
                            info.setName(((JSONObject) ll.get(j)).getString("name"));
                            info.setType(((JSONObject) ll.get(j)).getIntValue("type"));
                            securityItems.add(info);
                        }
                    }
                }
                for (int j = 0; j < ll.size(); j++) {
                    JSONObject jsonObject = (JSONObject) ll.get(j);
                    FoundInfo foundInfo = JSON.parseObject(jsonObject.toString(), FoundInfo.class);
                    if (foundInfo != null && !CollectionsUtils.isEmpty(foundInfo.getTriggerInfos()) && !CollectionsUtils.isEmpty(foundInfo.getControlInfos()))
                        sceneInfos.add(foundInfo);
                }
                Message m = mHandler.obtainMessage(2);
                m.obj = sceneInfos;
                mHandler.sendMessage(m);
            } else {
                mHandler.removeMessages(10);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(ScenceArmingDisarmingActivity.this, getString(R.string.net_error), Toast.LENGTH_LONG).show();
                        return;

                    }
                });
            }

        }
    }

}
