package com.smartism.znzk.activity.scene;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.adapter.recycleradapter.BaseRecyslerAdapter;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.adapter.scene.DevicesAdapter;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.Tips;
import com.smartism.znzk.domain.XiaXingInfo;
import com.smartism.znzk.domain.scene.AddSceneInfo;
import com.smartism.znzk.domain.scene.DeviceTipsInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.util.scene.AddSceneThread;
import com.smartism.znzk.util.scene.EditSceneThread;
import com.smartism.znzk.view.DividerItemDecoration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 联动场景activity
 */
public class LinkageSceneActivity extends SceneBaseActivity implements View.OnClickListener {
    private RelativeLayout custom_scene_touch, custom_scene_clt;
    private RecyclerView recycle_touch, recycle_clt;
    private TextView scene_data, scene_times;

    private Handler.Callback mCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    //初始化默认指令
                    Bundle b = msg.getData();
                    String obj = b.getString("obj");
                    String dec = b.getString("dec");
                    int pos = b.getInt("pos");
                    if (cltList != null && cltList.size() > pos) {
                        if (((DeviceTipsInfo) cltList.get(pos).getT()).getDeviceInfo().getCak() != null &&
                                "control".equals(((DeviceTipsInfo) cltList.get(pos).getT()).getDeviceInfo().getCak())) {
                            ((DeviceTipsInfo) cltList.get(pos).getT()).getTips().add(new Tips(obj, dec, false));
                        } else {
                            ((DeviceTipsInfo) cltList.get(pos).getT()).getDeviceInfo().setTip(obj);
                            ((DeviceTipsInfo) cltList.get(pos).getT()).getDeviceInfo().setTipName(null);
                        }
                        if (cltAdapter.getListSize() > 0 && pos < cltAdapter.getListSize()) {
                            cltAdapter.notifyItemChanged(pos);
                        }
                        if (pos == cltList.size() - 1) {
                            cancelInProgress();
                        }
                    }
                    break;

                case 1:
                    Bundle bl = msg.getData();
                    String objs = bl.getString("objs");
                    String decs = bl.getString("decs");
                    int p = bl.getInt("p");

                    if (touchList != null && touchList.size() > p) {
                        if (((DeviceTipsInfo) touchList.get(p).getT()).getDeviceInfo().getCak() != null &&
                                "security".equals(((DeviceTipsInfo) touchList.get(p).getT()).getDeviceInfo().getCak())) {
                            ((DeviceTipsInfo) touchList.get(p).getT()).getDeviceInfo().setTip(objs);
                            ((DeviceTipsInfo) touchList.get(p).getT()).getDeviceInfo().setTipName(null);
                        } else {
                            ((DeviceTipsInfo) touchList.get(p).getT()).getTips().add(new Tips(objs, decs, false));

                        }
                        if (touAdapter.getListSize() > 0 && p < touAdapter.getListSize()) {
                            touAdapter.notifyItemChanged(p);
                        }
                        if (p == touchList.size() - 1) {
                            cancelInProgress();
                        }
                    }
                    break;
            }
            return false;
        }
    };
    private Handler handler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linkage_scene);
        initView();
        initData();
        initRecycle();
        initEven();
        for (int i = 0; i < touchList.size(); i++) {
            getTip(touchList.get(i), i);
        }

    }

    private void initData() {
        cltList = new ArrayList<>();
        touchList = new ArrayList<>();


        //这里要后获取设备的控制指令，可以覆盖触发设备指令(触发设备指令是唯一的)
        List<RecyclerItemBean> cList = getCltList();
        if (cList != null && !cList.isEmpty()) {
            cltList.addAll(cList);
        }


        //这里要先获取触发设备，否则设备指令为触发会覆盖控制设备中同一设备的控制指令覆盖
        List<RecyclerItemBean> tList = getTrriList();
        if (tList != null && !tList.isEmpty()) {
            for (int i = 0; i < tList.size(); i++) {
                touchList.add(tList.get(i));
            }
        }





        if (isEdit) {
            et_scene_name.setText(resultStr.getName());
        }
    }

    private void initEven() {
        back_btn.setOnClickListener(this);
        if (!edit) return;
        right_menu.setOnClickListener(this);
        custom_scene_touch.setOnClickListener(this);
        custom_scene_clt.setOnClickListener(this);
        //触发设备选择操作指令
        touAdapter.setRecyclerItemClickListener(new BaseRecyslerAdapter.RecyclerItemClickListener() {
            @Override
            public void onRecycleItemClick(View view, int position) {
                touchItemBean = touchList.get(position);
                DeviceTipsInfo dtInfo = (DeviceTipsInfo) touchList.get(position).getT();
                DeviceInfo info = dtInfo.getDeviceInfo();
                ((DeviceTipsInfo) touchList.get(position).getT()).getDeviceInfo().setFlag(!info.isFlag());
                Intent intent = new Intent();
                intent.setClass(LinkageSceneActivity.this, ChooseTipsActivity.class);
                intent.putExtra("recyclerItemBean", (Serializable) touchList.get(position));
                intent.putExtra("tipslist", (Serializable) dtInfo.getTips());
                startActivityForResult(intent, 11);
            }
        });//item点击监听
        //控制设备选择操作指令
        cltAdapter.setRecyclerItemClickListener(new BaseRecyslerAdapter.RecyclerItemClickListener() {
            @Override
            public void onRecycleItemClick(View view, int position) {
                cltItemBean = cltList.get(position);
                DeviceTipsInfo dtInfo = (DeviceTipsInfo) cltList.get(position).getT();
                DeviceInfo info = dtInfo.getDeviceInfo();
                ((DeviceTipsInfo) cltList.get(position).getT()).getDeviceInfo().setFlag(!info.isFlag());
                Intent intent = new Intent();
                intent.setClass(LinkageSceneActivity.this, ChooseTipsActivity.class);
                intent.putExtra("isClt", true);
                intent.putExtra("recyclerItemBean", (Serializable) cltList.get(position));
                intent.putExtra("tipslist", (Serializable) dtInfo.getTips());
                intent.putExtra("type", 1);
                startActivityForResult(intent, 10);
            }
        });//item点击监听
    }

    private void initView() {
        et_scene_name = (EditText) findViewById(R.id.et_scene_name);
        right_menu = (ImageView) findViewById(R.id.right_menu);
        back_btn = (ImageView) findViewById(R.id.back_btn);

        custom_scene_touch = (RelativeLayout) findViewById(R.id.custom_scene_touch);
        custom_scene_clt = (RelativeLayout) findViewById(R.id.custom_scene_clt);
        recycle_touch = (RecyclerView) findViewById(R.id.recycle_touch);
        recycle_clt = (RecyclerView) findViewById(R.id.recycle_clt);
        scene_data = (TextView) findViewById(R.id.scene_date);
        scene_times = (TextView) findViewById(R.id.scene_times);

        if (!edit) {
            right_menu.setVisibility(View.GONE);
            findViewById(R.id.iv_scene_touch).setVisibility(View.GONE);
            findViewById(R.id.iv_scene_controll).setVisibility(View.GONE);
            et_scene_name.setFocusable(false);
            custom_scene_touch.setClickable(false);
            custom_scene_clt.setClickable(false);
        }
    }

    /**
     * 初始化已经选择的设备列表
     */
    public void initRecycle() {
        //触发设备初始化
        touAdapter = new DevicesAdapter(touchList, 0);
        LinearLayoutManager touLayout = new LinearLayoutManager(this);//创建默认线性LinearLayoutManager
        recycle_touch.setLayoutManager(touLayout);  //设置布局管理器
        recycle_touch.setItemAnimator(new DefaultItemAnimator()); //设置Item增加、移除动画
        recycle_touch.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recycle_touch.setAdapter(touAdapter);
        View toufootView = LayoutInflater.from(this).inflate(R.layout.layout_scene_foot, null, false);
        tv_foot_t = (TextView) toufootView.findViewById(R.id.tv_foot);
        img_foot_t = (ImageView) toufootView.findViewById(R.id.icon_foot);
        touAdapter.setFootView(toufootView);
        touAdapter.setRecyclerFootListener(new BaseRecyslerAdapter.RecyclerFootListener() {
            @Override
            public void onRecyclefootClick(View view) {
                if (touAdapter.getListSize() == showLines) {
                    tv_foot_t.setText(getString(R.string.scene_list_close));
                    img_foot_t.setImageResource(R.drawable.zhzj_cj_shouqi);
                    touAdapter.setListSize(0);
                } else {
                    tv_foot_t.setText(getString(R.string.scene_list_open));
                    img_foot_t.setImageResource(R.drawable.zhzj_tjcj_zhankai);
                    touAdapter.setListSize(showLines);
                }
                touAdapter.notifyDataSetChanged();
            }
        });
        touAdapter.setListSize(showLines);//默认显示两项，剩下的需要展开

        //控制设备初始化
        cltAdapter = new DevicesAdapter(cltList, 1);
        LinearLayoutManager cltLinear = new LinearLayoutManager(this);//创建默认线性LinearLayoutManager
        recycle_clt.setLayoutManager(cltLinear);  //设置布局管理器
        recycle_clt.setItemAnimator(new DefaultItemAnimator()); //设置Item增加、移除动画
        recycle_clt.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recycle_clt.setAdapter(cltAdapter);
        View footView = LayoutInflater.from(this).inflate(R.layout.layout_scene_foot, null, false);
        tv_foot = (TextView) footView.findViewById(R.id.tv_foot);
        img_foot = (ImageView) footView.findViewById(R.id.icon_foot);
        cltAdapter.setFootView(footView);
        cltAdapter.setRecyclerFootListener(new BaseRecyslerAdapter.RecyclerFootListener() {
            @Override
            public void onRecyclefootClick(View view) {
                if (cltAdapter.getListSize() == showLines) {
                    tv_foot.setText(getString(R.string.scene_list_close));
                    img_foot.setImageResource(R.drawable.zhzj_cj_shouqi);
                    cltAdapter.setListSize(0);
                } else {
                    tv_foot.setText(getString(R.string.scene_list_open));
                    img_foot.setImageResource(R.drawable.zhzj_tjcj_zhankai);
                    cltAdapter.setListSize(showLines);
                }
                cltAdapter.notifyDataSetChanged();
            }
        });
        cltAdapter.setListSize(showLines);//默认显示两项，剩下的需要展开
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.custom_scene_touch://添加触发设备
                intent.setClass(this, ChooseDeviceActivity.class);
                intent.putExtra("flag", 1);
                intent.putExtra("isClt", true);
                intent.putExtra("deviceInfoses", (Serializable) touchList);
                startActivityForResult(intent, 11);
                break;
            case R.id.custom_scene_clt://添加控制设备
                intent.setClass(this, ChooseDeviceActivity.class);
                intent.putExtra("flag", 2);
                intent.putExtra("deviceInfoses", (Serializable) cltList);
                startActivityForResult(intent, 10);
                break;
            case R.id.right_menu:
                String name = et_scene_name.getText().toString().trim();
                if (name == null || "".equals(name)) {
                    Toast.makeText(this, getString(R.string.activity_scene_item_name), Toast.LENGTH_SHORT).show();
                    return;
                }
                AddSceneInfo info = new AddSceneInfo();
                info.setType(2);
                info.setName(name);
                info.setCycleTime("");
                info.setTimers("");
                info.setDevices(cltList);
                info.setTriggerDeviceInfos(touchList);
                if (isEdit) {
                    info.setId(resultStr.getId());
                    mContext.showInProgress(getString(R.string.operationing));
                    JavaThreadPool.getInstance().excute(new EditSceneThread(this, info, handler));
                } else {
                    mContext.showInProgress(getString(R.string.operationing));
                    JavaThreadPool.getInstance().excute(new AddSceneThread(this, info, handler));
                }
                break;
            case R.id.back_btn:
                finish();
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && data != null) {
            //操作设备选择返回
            switch (resultCode) {
                case 20:
                    break;
                case 21:
                    //设备操作指令
                    int position = -1;
                    DeviceInfo deviceInfo = ((DeviceTipsInfo) cltItemBean.getT()).getDeviceInfo();
                    for (int i = 0; i < cltList.size(); i++) {
                        if (cltItemBean != null && cltItemBean == cltList.get(i)) {
                            position = i;
                        }
                    }
                    if (position == -1) return;
                    if (deviceInfo.getCak() != null && deviceInfo.getCak().equals("security")) {
                        Tips tips = (Tips) data.getSerializableExtra("tips");
                        ((DeviceTipsInfo) cltList.get(position).getT()).getDeviceInfo().setTip(tips.getC());
                        ((DeviceTipsInfo) cltList.get(position).getT()).getDeviceInfo().setTipName(tips.getE());
                        if (cltAdapter.getListSize() > 0 && position < cltAdapter.getListSize()) {
                            cltAdapter.notifyItemChanged(position);
                        }
                    } else {
                        List<Tips> list = (List<Tips>) data.getSerializableExtra("tipslist");
                        if (list != null && !list.isEmpty()) {
                            ((DeviceTipsInfo) cltList.get(position).getT()).getTips().clear();
                            ((DeviceTipsInfo) cltList.get(position).getT()).getTips().addAll(list);
                            if (cltAdapter.getListSize() > 0 && position < cltAdapter.getListSize()) {
                                cltAdapter.notifyItemChanged(position);
                            }
                        }
                    }
                    break;
                case 22:
                    break;
                case 23:
                    //返回选择的设备
                    List<RecyclerItemBean> list = (List<RecyclerItemBean>) data.getSerializableExtra("cleList");
                    cltList.clear();
                    if (list != null && !list.isEmpty()) {
                        for (int i = 0; i < list.size(); i++) {
                            cltList.add(list.get(i));
                            loadTips(cltList.get(i), i, requestCode);
                        }
                    }
                    changeCltFootState();
                    cltAdapter.notifyDataSetChanged();
                    break;
            }
            cltItemBean = null;
        } else if (requestCode == 11 && data != null) {
            //操作设备选择返回
            switch (resultCode) {
                case 20:
                    break;
                case 21:
                    //设备操作指令
                    int position = -1;
                    DeviceInfo deviceInfo = ((DeviceTipsInfo) touchItemBean.getT()).getDeviceInfo();
                    for (int i = 0; i < touchList.size(); i++) {
                        if (touchItemBean != null && touchItemBean == touchList.get(i)) {
                            position = i;
                        }
                    }
                    if (position == -1) return;
                    if (deviceInfo.getCak() != null && deviceInfo.getCak().equals("security")) {
                        Tips tips = (Tips) data.getSerializableExtra("tips");
                        ((DeviceTipsInfo) touchList.get(position).getT()).getDeviceInfo().setTip(tips.getC());
                        ((DeviceTipsInfo) touchList.get(position).getT()).getDeviceInfo().setTipName(tips.getE());
                        Log.e("DeviceTipsInfo", ((DeviceTipsInfo) touchList.get(position).getT()).getDeviceInfo().getTip() + "-" + ((DeviceTipsInfo) touchList.get(position).getT()).getDeviceInfo().getName());
                        if (touAdapter.getListSize() > 0 && position < touAdapter.getListSize()) {
                            touAdapter.notifyItemChanged(position);
                        }
                    } else {
                        List<Tips> list = (List<Tips>) data.getSerializableExtra("tipslist");
                        if (list != null && !list.isEmpty()) {
                            ((DeviceTipsInfo) touchList.get(position).getT()).getTips().clear();
                            ((DeviceTipsInfo) touchList.get(position).getT()).getTips().addAll(list);
                            if (touAdapter.getListSize() > 0 && position < touAdapter.getListSize()) {
                                touAdapter.notifyItemChanged(position);
                            }
                        }
                    }
                    break;
                case 22:
                    break;
                case 23:
                    //返回选择的设备
                    List<RecyclerItemBean> list = (List<RecyclerItemBean>) data.getSerializableExtra("cleList");
                    touchList.clear();
                    if (list != null && !list.isEmpty()) {
                        for (int i = 0; i < list.size(); i++) {
                            touchList.add(list.get(i));
                            loadTips(touchList.get(i), i, requestCode);
                            touAdapter.notifyItemInserted(i);
                        }
                    }
                    changeTouFootState();
                    touAdapter.notifyDataSetChanged();
                    break;
            }
            touchItemBean = null;
        }

    }

    private void loadTips(RecyclerItemBean itemBean, final int position, int requestCode) {
        if (requestCode == 11) {
            //触发设备--从服务器上获取
            getTip(itemBean, position);
        } else {
            loadCltTips(itemBean, position, requestCode);
        }
    }

    private void loadTriTips(RecyclerItemBean itemBean, final int position, int requestCode) {
        DeviceTipsInfo dtInfo = (DeviceTipsInfo) itemBean.getT();
        DeviceInfo deviceInfo = dtInfo.getDeviceInfo();
        Log.e("loadTips", deviceInfo.toString());
        if (deviceInfo.getCak() != null && deviceInfo.getCak().equals("control")) {
            if (deviceInfo.getTipName() == null) {
                final long uid = dcsp.getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
                final String code = dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");
                final long did = deviceInfo.getId();
                showInProgress(getString(R.string.loading), false, false);
                JavaThreadPool.getInstance().excute(new Runnable() {

                    @Override
                    public void run() {
                        DataCenterSharedPreferences dcsp = DataCenterSharedPreferences
                                .getInstance(LinkageSceneActivity.this, DataCenterSharedPreferences.Constant.CONFIG);
                        String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                        JSONObject pJsonObject = new JSONObject();
                        pJsonObject.put("did", did);
                        final String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/dkeycomms", pJsonObject, LinkageSceneActivity.this);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                cancelInProgress();
                                String o = null;
                                try {
                                    o = result;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                String obj = null;
                                String dec = null;
                                List<XiaXingInfo> xiaXing = JSON.parseArray(o, XiaXingInfo.class);
                                if (xiaXing != null && xiaXing.size() > 0) {
                                    obj = xiaXing.get(0).getS();
                                    dec = xiaXing.get(0).getN();
                                    Message msg = handler.obtainMessage(2);
                                    Bundle bs = new Bundle();
                                    bs.putString("obje", obj);
                                    bs.putString("dece", dec);
                                    bs.putInt("pose", position);
                                    msg.setData(bs);
                                    handler.sendMessage(msg);
                                }
                            }
                        });
                    }


                });

            }

        }
        if (deviceInfo.getCak() != null && deviceInfo.getCak().equals("security")) {
            if (deviceInfo.getTipName() == null) {
                final long uid = dcsp.getLong(DataCenterSharedPreferences.Constant.LOGIN_APPID, 0);
                final String code = dcsp.getString(DataCenterSharedPreferences.Constant.LOGIN_CODE, "");
                final long did = deviceInfo.getId();
                final String l = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
                showInProgress(getString(R.string.loading), false, false);

                JavaThreadPool.getInstance().excute(new Runnable() {

                    @Override
                    public void run() {
                        DataCenterSharedPreferences dcsp = DataCenterSharedPreferences
                                .getInstance(LinkageSceneActivity.this, DataCenterSharedPreferences.Constant.CONFIG);
                        String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                        JSONObject pJsonObject = new JSONObject();
                        pJsonObject.put("did", did);
                        final String result = HttpRequestUtils.requestoOkHttpPost( server
                                + "/jdm/s3/d/dcomms", pJsonObject, LinkageSceneActivity.this);
                        String jsonStr = "";
                        try {
                            jsonStr = result;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        final String o = jsonStr;
                        if (o != null && o.length() > 4) {
                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    cancelInProgress();
                                    String objs = null;
                                    String decs = null;
                                    List<Tips> mTip = JSON.parseArray(o, Tips.class);
                                    Message msg = handler.obtainMessage(1);
                                    if (mTip != null && !mTip.isEmpty()) {
                                        objs = mTip.get(0).getC();
                                        decs = mTip.get(0).getE();
                                    }

                                    Bundle bl = new Bundle();
                                    bl.putString("objs", objs);
                                    bl.putString("decs", decs);
                                    bl.putInt("p", position);
                                    msg.setData(bl);
                                    handler.sendMessage(msg);
                                }
                            });
                        } else {
                            cancelInProgress();
                        }
                    }
                });

            }

        }

    }

    /**
     * 获取控制设备的指令
     *
     * @param itemBean
     * @param position
     * @param code
     */
    public void loadCltTips(RecyclerItemBean itemBean, final int position, final int code) {
        DeviceTipsInfo dtInfo = (DeviceTipsInfo) itemBean.getT();
        DeviceInfo deviceInfo = dtInfo.getDeviceInfo();
        if (deviceInfo.getCak() != null && deviceInfo.getCak().equals("control")) {

            if (deviceInfo.getTipName() == null) {
                final long did = deviceInfo.getId();
                showInProgress(getString(R.string.loading), false, false);
                JavaThreadPool.getInstance().excute(new Runnable() {

                    @Override
                    public void run() {
                        DataCenterSharedPreferences dcsp = DataCenterSharedPreferences
                                .getInstance(LinkageSceneActivity.this, DataCenterSharedPreferences.Constant.CONFIG);
                        String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                        JSONObject pJsonObject = new JSONObject();
                        pJsonObject.put("did", did);

                        final String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/dkeycomms", pJsonObject, LinkageSceneActivity.this);
                        handler.post(new Runnable() {

                            @Override
                            public void run() {
                                cancelInProgress();
                                String o = null;
                                try {
                                    o = result;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                String obj = null;
                                String dec = null;
                                List<XiaXingInfo> xiaXing = JSON.parseArray(o, XiaXingInfo.class);
                                if (xiaXing != null && !xiaXing.isEmpty()) {
                                    obj = xiaXing.get(0).getS();
                                    dec = xiaXing.get(0).getN();
                                }

                                Message msg = handler.obtainMessage(0);
                                Bundle b = new Bundle();
                                b.putString("obj", obj);
                                b.putString("dec", dec);
                                b.putInt("pos", position);
                                msg.setData(b);
                                handler.sendMessage(msg);
                            }
                        });
                    }

                });

            }
        } else {
            Tips tips = new Tips();
            if (deviceInfo.getTipName() == null) {
                deviceInfo.setTipName(getString(R.string.activity_scene_item_outside));
                tips.setC(3 + "");
            }
            if (deviceInfo.getTipName() != null) {
                if (deviceInfo.getTipName()
                        .equals(getString(R.string.activity_scene_item_home))) {
                    tips.setC(0 + "");
                } else if (deviceInfo.getTipName()
                        .equals(getString(R.string.activity_scene_item_outside))) {
                    tips.setC(3 + "");
                } else if (deviceInfo.getTipName()
                        .equals(getString(R.string.devices_list_menu_dialog_jsbts))) {
                    tips.setC(1 + "");
                } else if (deviceInfo.getTipName()
                        .equals(getString(R.string.ybq_chart_name))) {
                    tips.setC(2 + "");
                }else {
                    tips.setC(2 + "");
                }

                Message msg = handler.obtainMessage(0);
                Bundle b = new Bundle();
                b.putString("obj", tips.getC());
                b.putString("dec", deviceInfo.getName());
                b.putInt("pos", position);
                msg.setData(b);
                handler.sendMessage(msg);
            }
        }
    }


    public void getTip(RecyclerItemBean itemBean, final int position) {
        DeviceTipsInfo dtInfo = (DeviceTipsInfo) itemBean.getT();
        final DeviceInfo deviceInfo = dtInfo.getDeviceInfo();
        Log.e("deviceInfo", "33========>" + deviceInfo.toString());

        if ((MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_AIERFUDE)
                || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())) && DeviceInfo.CaMenu.zhinengsuo.value().equals(deviceInfo.getCa())) {
            String obj = null;
            String dec = null;
            obj = "002D";
            dec = getString(R.string.scene_unlock);
//            Message msg = handler.obtainMessage(2);
//            Bundle bs = new Bundle();
//            bs.putString("obje", obj);
//            bs.putString("dece", dec);
//            bs.putInt("pose", position);
//            msg.setData(bs);
//            handler.sendMessage(msg);
            ((DeviceTipsInfo) touchList.get(position).getT()).getDeviceInfo().setTip(obj);
            ((DeviceTipsInfo) touchList.get(position).getT()).getDeviceInfo().setTipName(dec);
            if (touAdapter.getListSize() > 0 && position < touAdapter.getListSize()) {
                touAdapter.notifyItemChanged(position);
            }

        }else {
            final long did = deviceInfo.getId();
            final String l = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
            JavaThreadPool.getInstance().excute(new Runnable() {

                @Override
                public void run() {
                    DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(LinkageSceneActivity.this,
                            DataCenterSharedPreferences.Constant.CONFIG);
                    String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                    JSONObject pJsonObject = new JSONObject();
                    pJsonObject.put("did", did);
                    final String result = HttpRequestUtils.requestoOkHttpPost( server
                            + "/jdm/s3/d/dcomms", pJsonObject, LinkageSceneActivity.this);
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            String o = null;
                            try {
                                o = result;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            List<Tips> mTip = JSON.parseArray(o, Tips.class);
                            ArrayList<String> str = new ArrayList<String>();
                            if (mTip != null && mTip.size() > 0) {
                                Log.e("deviceInfo", "22========>" + deviceInfo.getTip());
                                if (deviceInfo.getTip() != null) {
                                    for (Tips tips : mTip) {
                                        if (tips.getC().equals(deviceInfo.getTip())) {
                                            ((DeviceTipsInfo) touchList.get(position).getT()).getDeviceInfo().setTip(tips.getC());
                                            ((DeviceTipsInfo) touchList.get(position).getT()).getDeviceInfo().setTipName(tips.getE());
                                            if (touAdapter.getListSize() > 0 && position < touAdapter.getListSize()) {
                                                touAdapter.notifyItemChanged(position);
                                            }
                                        }
                                    }
                                } else {
                                    Tips tips = mTip.get(0);
                                    ((DeviceTipsInfo) touchList.get(position).getT()).getDeviceInfo().setTip(tips.getC());
                                    ((DeviceTipsInfo) touchList.get(position).getT()).getDeviceInfo().setTipName(tips.getE());
                                    if (touAdapter.getListSize() > 0 && position < touAdapter.getListSize()) {
                                        touAdapter.notifyItemChanged(position);
                                    }
                                }

                            }

                        }
                    });
                }

            });
        }
    }
}
