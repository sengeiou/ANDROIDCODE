package com.smartism.znzk.activity.scene;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.communication.protocol.SyncMessage;
import com.smartism.znzk.communication.protocol.SyncMessageContainer;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.FoundInfo;
import com.smartism.znzk.domain.SceneInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.LogUtil;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.MyGridView;
import com.smartism.znzk.view.pickerview.OptionsPickerView;
import com.smartism.znzk.view.pickerview.OptionsPickerView.OnOptionsSelectListener;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SceneActivity extends ActivityParentActivity implements View.OnClickListener {
    public static final int SecuritySceneType_Normal = 0;
    public static final int SecuritySceneType_Time = 1;
    public static final int SecuritySceneType_Trigger = 2;
    public static final int SecuritySceneType_Home = 3;
    public static final int SecuritySceneType_Arming = 4;
    public static final int SecuritySceneType_DesArming = 5;
    private LinearLayout ll_custom, ll_timimg, ll_linkage;
    private TextView currentScene;
    private MyGridView sceneGridView, timeGridView, triggerGridView, securityGridView;
    private SceneItemAdapter sceneItemAdapter;
    private TimeItemAdapter timeItemAdapter;
    private TriggerItemAdapter triggerItemAdapter;
    private SecurityItemAdapter securityItemAdapter;
    private List<SceneInfo> items, timeItems, triggerItems, securityItems;
    private String result;
    private SwipeRefreshLayout refresh;
    public static String test;
    private ZhujiInfo zhuji;
    private FoundInfo resultStr;
    private ArrayList<String> pressOperation;
    private OptionsPickerView<String> pressOptions;
    private int pos = 0;
    private List<SceneInfo> sceneInfos = new ArrayList<SceneInfo>();
    private ImageView right_menu;
    private ImageView back_btn, iv_timing_arming_diarming;
    public static final int TIME_OUT = 100;
    private LinearLayout ll_top;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    cancelInProgress();
                    items.clear();
                    timeItems.clear();
                    triggerItems.clear();
                    securityItems.clear();
                    List<SceneInfo> infos = (List<SceneInfo>) msg.obj;
                    for (int i = 0; i < infos.size(); i++) {
                        Log.e("场景", infos.get(i).getName() + "...." + infos.get(i).getType() + "...." + infos.get(i).toString());
                        if (infos.get(i).getType() == SecuritySceneType_Normal) {
                            items.add(infos.get(i));
                        } else if (infos.get(i).getType() == SecuritySceneType_Time) {
                            timeItems.add(infos.get(i));
                        } else if (infos.get(i).getType() == SecuritySceneType_Trigger) {
                            triggerItems.add(infos.get(i));
                        } else if (infos.get(i).getType() == SecuritySceneType_Home) {//home
                            securityItems.add(infos.get(i));
                        } else if (infos.get(i).getType() == SecuritySceneType_Arming) {//arming
                            securityItems.add(0, infos.get(i));
                        }
                    }
                    for (int i = 0; i < infos.size(); i++) { //放在下一个执行,如果放在上面可能会崩溃。
                        if (infos.get(i).getType() == SecuritySceneType_DesArming) {//desarming
                            securityItems.add(1, infos.get(i)); //设置到下标1的位置。需要单独执行,否则会崩溃
                        }
                    }
                    if (items.size() > 0) {
                        ll_custom.setVisibility(View.VISIBLE);
                    } else {
                        ll_custom.setVisibility(View.GONE);
                    }
                    if (timeItems.size() > 0) {
                        ll_timimg.setVisibility(View.VISIBLE);
                    } else {
                        ll_timimg.setVisibility(View.GONE);
                    }
                    if (triggerItems.size() > 0) {
                        ll_linkage.setVisibility(View.VISIBLE);
                    } else {
                        ll_linkage.setVisibility(View.GONE);
                    }
                    setNowSceneName();
                    triggerItemAdapter.notifyDataSetChanged();
                    timeItemAdapter.notifyDataSetChanged();
                    sceneItemAdapter.notifyDataSetChanged();
                    securityItemAdapter.notifyDataSetChanged();
                    break;
                case 2:
                    cancelInProgress();
                    if (msg.arg1 == -3) {
                        zhuji.setScene(Constant.SCENE_NOW_HOME);
                        DatabaseOperator.getInstance(SceneActivity.this).insertOrUpdateZhujiInfo(zhuji);
                        SyncMessageContainer.getInstance()
                                .produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                        initSceneList();
                    } else if (msg.arg1 == -1) {
                        zhuji.setScene(Constant.SCENE_NOW_SF);
                        DatabaseOperator.getInstance(SceneActivity.this).insertOrUpdateZhujiInfo(zhuji);
                        SyncMessageContainer.getInstance()
                                .produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                        initSceneList();
                    } else if (msg.arg1 == 0) {
                        zhuji.setScene(Constant.SCENE_NOW_CF);
                        DatabaseOperator.getInstance(SceneActivity.this).insertOrUpdateZhujiInfo(zhuji);
                        SyncMessageContainer.getInstance()
                                .produceSendMessage(new SyncMessage(SyncMessage.CommandMenu.rq_refresh));
                        initSceneList();
                    }
                    break;
                case 10:
                    if (result == null || "".equals(result)) {
                        cancelInProgress();
                        Toast.makeText(SceneActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    resultStr = JSON.parseObject(result, FoundInfo.class);
                    resultStr.setTip(1);
                    Intent intent = new Intent();
                    intent.putExtra("result", resultStr);
                    switch (resultStr.getType()) {
                        case 0:
                            intent.setClass(getApplicationContext(), CustomSceneActivity.class);
                            break;
                        case 1:
                            intent.setClass(getApplicationContext(), TimingSceneActivity.class);
                            break;
                        case 2:
                            intent.setClass(getApplicationContext(), LinkageSceneActivity.class);
                            break;
                        default:
                            intent.setClass(getApplicationContext(), CustomSceneActivity.class);
                            break;
                    }
                    startActivity(intent);
                    break;
                case TIME_OUT:
                    cancelInProgress();
                    Toast.makeText(mContext, getString(R.string.time_out), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private Handler defaultHandler = new WeakRefHandler(mCallback);
    private ImageView mSmsControlImg ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene);
        initZhuji();
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initZhuji();
        initSceneList();

        jiayuSetting();
    }
    private void jiayuSetting(){
        if(!ZhujiListFragment.getMasterId().contains("FF20")){
            return ;
        }
        mSmsControlImg = findViewById(R.id.sms_control_scene);
        mSmsControlImg.setVisibility(View.VISIBLE);
        mSmsControlImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),SecenControlBySmsActivity.class);
                intent.putExtra("zhuji_id",zhuji.getId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        defaultHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void initView() {
        right_menu = (ImageView) findViewById(R.id.right_menu);
        back_btn = (ImageView) findViewById(R.id.back_btn);
        iv_timing_arming_diarming = (ImageView) findViewById(R.id.iv_timing_arming_diarming);

        ll_custom = (LinearLayout) findViewById(R.id.ll_custom);
        ll_timimg = (LinearLayout) findViewById(R.id.ll_timimg);
        ll_linkage = (LinearLayout) findViewById(R.id.ll_linkage);
        ll_top = (LinearLayout) findViewById(R.id.ll_top);
        ll_top.setFocusable(true);
        ll_top.setFocusableInTouchMode(true);
        ll_top.requestFocus();


        right_menu.setOnClickListener(this);
        back_btn.setOnClickListener(this);
        iv_timing_arming_diarming.setOnClickListener(this);

        currentScene = (TextView) findViewById(R.id.activity_currentscene);
        refresh = (SwipeRefreshLayout) findViewById(R.id.refresh_ly);
        refresh.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                initSceneList();
                setNowSceneName();
                // Toast.makeText(getApplicationContext(),
                // currentScene.getText().toString(), 1).show();
                //场景列表
                sceneItemAdapter.notifyDataSetChanged();
                //定时场景
                timeItemAdapter.notifyDataSetChanged();

                triggerItemAdapter.notifyDataSetChanged();
                securityItemAdapter.notifyDataSetChanged();
                refresh.setRefreshing(false);
            }
        });
        // Toast.makeText(getApplicationContext(),test, 1).show();
        //长按显示的地步菜单
        pressOperation = new ArrayList<String>();
        pressOperation.add(getString(R.string.activity_scene_modify));
        pressOperation.add(getString(R.string.activity_scene_enable));
        pressOperation.add(getString(R.string.activity_scene_disable));
        pressOperation.add(getString(R.string.activity_scene_del));
        pressOptions = new OptionsPickerView<String>(this);
        pressOptions.setPicker(pressOperation);
        pressOptions.setTitle(getString(R.string.activity_editscene_need_operation));
        pressOptions.setCyclic(false);
        pressOptions.setOnoptionsSelectListener(new OnOptionsSelectListener() {

            @Override
            public void onOptionsSelect(int options1, int option2, int options3) {
                if (pressOperation.get(options1).equals(getString(R.string.activity_scene_del))) {
                    DeletSene();

                } else if (pressOperation.get(options1).equals(getString(R.string.activity_scene_modify))) {
                    FoundScene();

                } else if (pressOperation.get(options1).equals(getString(R.string.activity_scene_enable))) {
                    enable(1);
                } else {
                    enable(0);
                }
            }

        });
        items = new ArrayList<SceneInfo>();
        timeItems = new ArrayList<SceneInfo>();
        triggerItems = new ArrayList<SceneInfo>();
        securityItems = new ArrayList<SceneInfo>();

        sceneGridView = (MyGridView) findViewById(R.id.activity_scene_gridview);
        timeGridView = (MyGridView) findViewById(R.id.activity_time_gridview);
        triggerGridView = (MyGridView) findViewById(R.id.activity_trigger_gridview);
        securityGridView = (MyGridView) findViewById(R.id.activity_security_gridview);

        securityItemAdapter = new SecurityItemAdapter(getApplicationContext());
        securityGridView.setAdapter(securityItemAdapter);
        securityGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showInProgress(getString(R.string.operationing), false, true);
                if (position == 0) { // 设防
                    JavaThreadPool.getInstance().excute(new TriggerScene(-1));
                } else if (position == 1) { // 撤防
                    JavaThreadPool.getInstance().excute(new TriggerScene(0));
                } else if (position == 2) { // 在家
                    JavaThreadPool.getInstance().excute(new TriggerScene(-3)); // -2已经被占用
                }
            }
        });
        if (zhuji!=null && "FF25".equals(zhuji.getCompanyPrefix())) {//主机为大屏序列号端 将启用在家可编辑
            securityGridView.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 2) {
                        pos = position;
                        sceneInfos = securityItems;
                        FoundScene();
                    } else {
                        Toast.makeText(SceneActivity.this, getString(R.string.activity_scene_notedit), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
        }
        triggerItemAdapter = new TriggerItemAdapter(getApplicationContext());
        triggerGridView.setAdapter(triggerItemAdapter);
        triggerGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                }
            }
        });
        triggerGridView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                pressOptions.show();
                pos = position;
                sceneInfos = triggerItems;
//                }
                return true;
            }
        });


        timeItemAdapter = new TimeItemAdapter(SceneActivity.this);
        timeGridView.setAdapter(timeItemAdapter);
        timeGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
        timeGridView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                pressOptions.show();
                pos = position;
                sceneInfos = timeItems;
                return true;
            }
        });
        sceneItemAdapter = new SceneItemAdapter(SceneActivity.this);
        sceneGridView.setAdapter(sceneItemAdapter);
        sceneGridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                triggerScene(items.get(position));
            }
        });
        sceneGridView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                pressOptions.show();
                pos = position;
                sceneInfos = items;
                return true;
            }
        });

        //埃利恩
        if(zhuji.getMasterid().contains("FF3B")){
            right_menu.setVisibility(View.GONE);
        }
    }

    public void triggerScene(final SceneInfo info) {
        final long cid = info.getId();
        final long did = zhuji.getId();
        final long uid = dcsp.getLong(Constant.LOGIN_APPID, 0);
        final String code = dcsp.getString(Constant.LOGIN_CODE, "");
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", did);
                pJsonObject.put("id", cid);
                String result = HttpRequestUtils
                        .requestoOkHttpPost(
                                 server + "/jdm/s3/scenes/trigger", pJsonObject, SceneActivity.this);

                // 0成功设置 -1参数为空 -2校验失败 -3id不存在
                if ("0".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(SceneActivity.this, getString(R.string.activity_editscene_set_success),
                                    Toast.LENGTH_LONG).show();
                            currentScene.setText(info.getName());
                            zhuji.setScene(info.getName());
                            triggerItemAdapter.notifyDataSetChanged();
                            sceneItemAdapter.notifyDataSetChanged();
                            timeItemAdapter.notifyDataSetChanged();
                            securityItemAdapter.notifyDataSetChanged();

                        }
                    });
                } else if ("-1".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(SceneActivity.this, getString(R.string.register_tip_empty),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-2".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(SceneActivity.this, getString(R.string.device_check_failure),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-3".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(SceneActivity.this, getString(R.string.activity_editscene_id_empty),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-4".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(SceneActivity.this, getString(R.string.activity_editscene_isdisable),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

            }
        });
    }

    public void FoundScene() {
        final long cid = sceneInfos.get(pos).getId();
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("id", cid);
                result = HttpRequestUtils
                        .requestoOkHttpPost( server + "/jdm/s3/scenes/get", pJsonObject, SceneActivity.this);
                if ("-3".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(SceneActivity.this, getString(R.string.device_not_getdata),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if (result != null && result.length() > 3) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            defaultHandler.sendEmptyMessage(10);
                        }
                    });
                }
            }
        });

    }

    public void DeletSene() {
        final long uid = dcsp.getLong(Constant.LOGIN_APPID, 0);
        final String code = dcsp.getString(Constant.LOGIN_CODE, "");
        final long cid = sceneInfos.get(pos).getId();
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {

                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("id", cid);

                final String result = HttpRequestUtils
                        .requestoOkHttpPost(
                                 server + "/jdm/s3/scenes/del", pJsonObject, SceneActivity.this);
                if ("0".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            sceneInfos.remove(pos);
                            sceneItemAdapter.notifyDataSetChanged();
                            timeItemAdapter.notifyDataSetChanged();
                            triggerItemAdapter.notifyDataSetChanged();
                            Toast.makeText(SceneActivity.this, getString(R.string.device_del_success),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else if ("-4".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(SceneActivity.this, getString(R.string.activity_editscene_not),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    public void enable(int s) {
        final long cid = sceneInfos.get(pos).getId();
        final int status = s;
        showInProgress(getString(R.string.loading), false, true);
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(SceneActivity.this,
                        Constant.CONFIG);
                String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("id", cid);
                pJsonObject.put("s", status);

                final String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/scenes/status", pJsonObject, SceneActivity.this);
                if ("0".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            if (status == 1) {
                                Toast.makeText(SceneActivity.this, getString(R.string.activity_editscene_enable),
                                        Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(SceneActivity.this, getString(R.string.activity_editscene_disable),
                                        Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                } else if ("-3".equals(result)) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(SceneActivity.this, getString(R.string.activity_editscene_s_erro),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void initZhuji() {
//        showInProgress(getString(R.string.loading), false, true);
//        zhuji = DatabaseOperator.getInstance(SceneActivity.this)
//                .queryDeviceZhuJiInfo(dcsp.getString(Constant.APP_MASTERID, ""));
        //替换
        zhuji = DatabaseOperator.getInstance(SceneActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
//        initSceneList();
    }

    private void initSceneList() {
        showInProgress(getString(R.string.loading), false, true);
        defaultHandler.sendEmptyMessageDelayed(TIME_OUT, 12 * 1000);
        JavaThreadPool.getInstance().excute(new SceneLoad());
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.iv_timing_arming_diarming:
                intent.setClass(this, ScenceArmingDisarmingActivity.class);
                intent.putExtra("securityItems", (Serializable) securityItems);
                startActivity(intent);
                break;
            case R.id.right_menu:
                intent.setClass(getApplicationContext(), SelectSceneTypeActivity.class);
                intent.putExtra("securityItems", (Serializable) securityItems);
                startActivity(intent);
                break;
            case R.id.back_btn:
                finish();
                break;
        }
    }

    class SceneItemAdapter extends BaseAdapter {
        /**
         * 视图内部类
         *
         * @author Administrator
         */
        class DeviceInfoView {
            ImageView ioc;
            TextView name;
        }

        LayoutInflater layoutInflater;

        public SceneItemAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            DeviceInfoView viewCache = new DeviceInfoView();
            if (view == null) {
                view = layoutInflater.inflate(R.layout.activity_scene_item, null);
                viewCache.ioc = (ImageView) view.findViewById(R.id.scene_item_img);
                viewCache.name = (TextView) view.findViewById(R.id.scene_item_txt);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
            viewCache.name.setText(items.get(position).getName());
            if (items.get(position).getName()!=null&&items.get(position).getName().equals(currentScene.getText().toString())
                    && items.get(position).getType() == 0) { // 自动类型
                viewCache.ioc.setImageResource(R.drawable.zhzj_cj_zidingyi_hover);
            } else {
                viewCache.ioc.setImageResource(R.drawable.zhzj_cj_zidingyi);
            }

            return view;
        }

    }

    class TimeItemAdapter extends BaseAdapter {
        /**
         * 视图内部类
         *
         * @author Administrator
         */
        class DeviceInfoView {
            ImageView ioc;
            TextView name;
        }

        LayoutInflater layoutInflater;

        public TimeItemAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
            return timeItems.size();
        }

        @Override
        public Object getItem(int position) {
            return timeItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            DeviceInfoView viewCache = new DeviceInfoView();
            if (view == null) {
                view = layoutInflater.inflate(R.layout.activity_scene_item, null);
                viewCache.ioc = (ImageView) view.findViewById(R.id.scene_item_img);
                viewCache.name = (TextView) view.findViewById(R.id.scene_item_txt);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
            viewCache.name.setText(timeItems.get(position).getName());
            if (timeItems.get(position).getName().equals(currentScene.getText().toString())
                    && timeItems.get(position).getType() == 1) { // 自动类型
                viewCache.ioc.setImageResource(R.drawable.zhzj_cj_dingshii_hover);
            } else {
                viewCache.ioc.setImageResource(R.drawable.zhzj_cj_dingshi);
            }
            return view;
        }

    }

    class TriggerItemAdapter extends BaseAdapter {
        /**
         * 视图内部类
         *
         * @author Administrator
         */
        class DeviceInfoView {
            ImageView ioc;
            TextView name;
        }

        LayoutInflater layoutInflater;

        public TriggerItemAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
            return triggerItems.size();
        }

        @Override
        public Object getItem(int position) {
            return triggerItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            DeviceInfoView viewCache = new DeviceInfoView();
            if (view == null) {
                view = layoutInflater.inflate(R.layout.activity_scene_item, null);
                viewCache.ioc = (ImageView) view.findViewById(R.id.scene_item_img);
                viewCache.name = (TextView) view.findViewById(R.id.scene_item_txt);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
            viewCache.name.setTextColor(Color.BLACK);
            viewCache.name.setText(triggerItems.get(position).getName());
            if (triggerItems.get(position).getName().equals(currentScene.getText().toString())
                    && triggerItems.get(position).getType() == 2) { // 自动类型
                viewCache.ioc.setImageResource(R.drawable.zhzj_cj_liandongi_hover);
            } else {
                viewCache.ioc.setImageResource(R.drawable.zhzj_cj_liandong);
            }
            return view;
        }

    }

    class SecurityItemAdapter extends BaseAdapter {
        /**
         * 视图内部类
         *
         * @author Administrator
         */
        class DeviceInfoView {
            ImageView ioc;
            TextView name;
        }

        LayoutInflater layoutInflater;

        public SecurityItemAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
//            Log.e("场景", securityItems.toString() + "");
            return securityItems.size();
        }

        @Override
        public Object getItem(int position) {
            return securityItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            DeviceInfoView viewCache = new DeviceInfoView();
            if (view == null) {
                view = layoutInflater.inflate(R.layout.activity_scene_item, null);
                viewCache.ioc = (ImageView) view.findViewById(R.id.scene_item_img);
                viewCache.name = (TextView) view.findViewById(R.id.scene_item_txt);
                view.setTag(viewCache);
            } else {
                viewCache = (DeviceInfoView) view.getTag();
            }
            viewCache.name.setTextColor(Color.BLACK);
            viewCache.name.setText(securityItems.get(position).getName());
            if (position == 0 && zhuji!=null && Constant.SCENE_NOW_SF.equals(zhuji.getScene())) {
                viewCache.ioc.setImageResource(R.drawable.zhzj_sy_shefang_hover);
            } else if (position == 0) {
                viewCache.ioc.setImageResource(R.drawable.zhzj_sy_shefang);
            }
            if (position == 1 && zhuji!=null && Constant.SCENE_NOW_CF.equals(zhuji.getScene())) {
                viewCache.ioc.setImageResource(R.drawable.zhzj_sy_chefang_hover);
            } else if (position == 1) {
                viewCache.ioc.setImageResource(R.drawable.zhzj_sy_chefang);
            }
            if (position == 2 && zhuji!=null && Constant.SCENE_NOW_HOME.equals(zhuji.getScene())){
                viewCache.ioc.setImageResource(R.drawable.zhzj_sy_athome_hover);
            } else if (position == 2) {
                viewCache.ioc.setImageResource(R.drawable.zhzj_sy_athome);
            }
            return view;
        }
    }

    class SceneLoad implements Runnable {
        @Override
        public void run() {
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
//            String mId = dcsp.getString(Constant.APP_MASTERID, "");
            //替换
            String mId = ZhujiListFragment.getMasterId();
            JSONObject pJsonObject = new JSONObject();
            pJsonObject.put("m", mId);
            String result = HttpRequestUtils
                    .requestoOkHttpPost( server + "/jdm/s3/scenes/all", pJsonObject, SceneActivity.this);
            List<SceneInfo> sceneInfos = new ArrayList<SceneInfo>();
            Log.e("all scene:", result + "");
            if (!StringUtils.isEmpty(result) && result.length() > 2) {
                defaultHandler.removeMessages(TIME_OUT);
                JSONArray ll = null;
                try {
                    ll = JSON.parseArray(result);
                } catch (Exception e) {
                    LogUtil.e(getApplicationContext(), TAG, "解密错误：：", e);
                }
                if (ll == null) {
                    defaultHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            Toast.makeText(SceneActivity.this, getString(R.string.device_set_tip_responseerr), Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }
                for (int j = 0; j < ll.size(); j++) {
                    SceneInfo info = new SceneInfo();
                    JSONObject o = (JSONObject) ll.get(j);
                    info.setId(o.getLongValue("id"));
                    info.setName(o.getString("name")==null?"":o.getString("name"));
                    info.setType(o.getIntValue("type"));
                    sceneInfos.add(info);
                }
                Message m = defaultHandler.obtainMessage(1);
                m.obj = sceneInfos;
                defaultHandler.sendMessage(m);
            } else {
                defaultHandler.removeMessages(TIME_OUT);
                defaultHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(SceneActivity.this, getString(R.string.net_error_requestfailed), Toast.LENGTH_LONG).show();
                        return;

                    }
                });
            }
        }
    }

    /**
     * 结束进度条
     */
    public void cancelInProgress() {
        synchronized (this) {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
                mProgressCycleView = null;
                // mProgressBar = null;
            }
        }
    }

    class TriggerScene implements Runnable {
        private int sId;

        public TriggerScene() {
        }

        public TriggerScene(int sId) {
            this.sId = sId;
        }

        @Override
        public void run() {

            JSONObject o = new JSONObject();
            o.put("id", sId);
            o.put("did", zhuji.getId());
//			o.put("uid", dcsp.getLong(Constant.LOGIN_APPID, 0));
//			o.put("code", dcsp.getString(Constant.LOGIN_CODE, ""));
            String server = dcsp.getString(Constant.HTTP_DATA_SERVERS, "");
            String result = HttpRequestUtils
                    .requestoOkHttpPost(
                             server + "/jdm/s3/scenes/trigger", o, SceneActivity.this);
//			String result = HttpRequestUtils.requestHttpServer(
//					 server + "/jdm/service/scenes/trigger?v="
//							+ URLEncoder.encode(SecurityUtil.crypt(o.toJSONString(), Constant.KEY_HTTP)),
//					SceneActivity.this, defaultHandler);
            if ("0".equals(result)) {
                Message m = defaultHandler.obtainMessage(2);
                m.arg1 = sId;
                defaultHandler.sendMessage(m);
            } else {
                defaultHandler.post(new Runnable() {
                    public void run() {
                        cancelInProgress();
                        Toast.makeText(SceneActivity.this, getString(R.string.net_error_operationfailed),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }


    private void setNowSceneName() {
        if (zhuji != null) {
            if (Constant.SCENE_NOW_SF.equals(zhuji.getScene())) {
                currentScene.setText(getString(R.string.activity_scene_item_outside));
            } else if (Constant.SCENE_NOW_CF.equals(zhuji.getScene())) {
                currentScene.setText(getString(R.string.activity_scene_item_home));
            } else if (Constant.SCENE_NOW_HOME.equals(zhuji.getScene())) {
                currentScene.setText(getString(R.string.activity_scene_item_inhome));
            } else if ("".equals(zhuji.getScene())) {
                currentScene.setText(getString(R.string.activity_scene_item_no));
            } else {
                currentScene.setText(zhuji.getScene());
            }
        }
    }
}
