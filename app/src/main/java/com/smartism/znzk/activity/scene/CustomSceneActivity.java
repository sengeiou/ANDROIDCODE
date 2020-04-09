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
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.Tips;
import com.smartism.znzk.domain.XiaXingInfo;
import com.smartism.znzk.domain.scene.AddSceneInfo;
import com.smartism.znzk.domain.scene.DeviceTipsInfo;
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

public class CustomSceneActivity extends SceneBaseActivity implements View.OnClickListener, BaseRecyslerAdapter.RecyclerItemClickListener, BaseRecyslerAdapter.RecyclerFootListener {

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
            }
            return false;
        }
    };
    private Handler handler = new WeakRefHandler(mCallback);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_scene);
        initView();
        initEvent();
        initDate();
        initRecycle();
    }

    private void initDate() {
        cltList = new ArrayList<>();
        List<RecyclerItemBean> cList = getCltList();
        if (cList != null && !cList.isEmpty()) {
            cltList.addAll(cList);
        }
        if (isEdit) {
            et_scene_name.setText(resultStr.getName());
            if (resultStr.getType() != 0){
                et_scene_name.setKeyListener(null);
            }
        }
    }

    private void initEvent() {
        back_btn.setOnClickListener(this);
        if (!edit) return;
        custom_scene_add.setOnClickListener(this);
        right_menu.setOnClickListener(this);
    }

    private void initView() {
        custom_scene_add = (RelativeLayout) findViewById(R.id.custom_scene_add);
        recycler = (RecyclerView) findViewById(R.id.recycle_clt);
        et_scene_name = (EditText) findViewById(R.id.et_scene_name);
        right_menu = (ImageView) findViewById(R.id.right_menu);
        back_btn = (ImageView) findViewById(R.id.back_btn);
        if (!edit) {
            right_menu.setVisibility(View.GONE);
            findViewById(R.id.img_scene_add).setVisibility(View.GONE);
            et_scene_name.setFocusable(false);
        }
    }

    /**
     * 初始化已经选择的设备列表
     */
    public void initRecycle() {
        //控制设备初始化
        cltAdapter = new DevicesAdapter(cltList, 1);
        cltAdapter.setRecyclerItemLongClickListener(new BaseRecyslerAdapter.RecyclerItemLongClickListener() {
            @Override
            public boolean onRecycleItemLongClick(View view, int position) {
                return true;
            }
        });
        cltAdapter.setRecyclerItemClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);//创建默认线性LinearLayoutManager
        recycler.setLayoutManager(layoutManager);  //设置布局管理器
        recycler.setItemAnimator(new DefaultItemAnimator()); //设置Item增加、移除动画
        recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recycler.setAdapter(cltAdapter);
        View footView = LayoutInflater.from(this).inflate(R.layout.layout_scene_foot, null, false);
        tv_foot = (TextView) footView.findViewById(R.id.tv_foot);
        img_foot = (ImageView) footView.findViewById(R.id.icon_foot);
        cltAdapter.setFootView(footView);
        cltAdapter.setRecyclerFootListener(this);
        cltAdapter.setListSize(2);//默认显示两项，剩下的需要展开
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.custom_scene_add:
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
                info.setType(0);
                info.setName(name);
                info.setCycleTime("");
                info.setTimers("");
                info.setDevices(cltList);
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
                        Log.e("DeviceTipsInfo", ((DeviceTipsInfo) cltList.get(position).getT()).getDeviceInfo().getTip() + "-" + ((DeviceTipsInfo) cltList.get(position).getT()).getDeviceInfo().getName());
                        if (cltAdapter.getListSize() > 0 && position < cltAdapter.getListSize()) {
                            cltAdapter.notifyItemChanged(position);
                        }
                    } else {
                        List<Tips> list = (List<Tips>) data.getSerializableExtra("tipslist");
                        if (list != null && !list.isEmpty()) {
                            ((DeviceTipsInfo) cltList.get(position).getT()).getTips().clear();
                            ((DeviceTipsInfo) cltList.get(position).getT()).getTips().addAll(list);
                            if (cltAdapter.getListSize() > 0 && position < cltAdapter.getListSize()) {//不显示部分不能刷新列表UI
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
                        cltList.addAll(list);
                        for (int i = 0; i < list.size(); i++) {
                            loadTips(cltList.get(i), i);//设置设备指令
                        }
                    }
                    changeCltFootState();
                    cltAdapter.notifyDataSetChanged();
                    break;
            }
        }
        cltItemBean = null;
    }


    @Override
    public void onRecycleItemClick(View view, int position) {
        cltItemBean = cltList.get(position);
        DeviceTipsInfo dtInfo = (DeviceTipsInfo) cltList.get(position).getT();
        DeviceInfo info = dtInfo.getDeviceInfo();
        ((DeviceTipsInfo) cltList.get(position).getT()).getDeviceInfo().setFlag(!info.isFlag());
        Intent intent = new Intent();
        intent.setClass(this, ChooseTipsActivity.class);
        intent.putExtra("isClt", true);
        intent.putExtra("flag", 2);
        intent.putExtra("recyclerItemBean", (Serializable) cltList.get(position));
        intent.putExtra("tipslist", (Serializable) dtInfo.getTips());
        startActivityForResult(intent, 10);
    }


    public void loadTips(RecyclerItemBean itemBean, final int position) {
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
                                .getInstance(CustomSceneActivity.this, DataCenterSharedPreferences.Constant.CONFIG);
                        String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                        JSONObject pJsonObject = new JSONObject();
                        pJsonObject.put("did", did);

                        final String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/dkeycomms", pJsonObject, CustomSceneActivity.this);
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
//                tips.setC(DeviceInfo.CaMenu.ybq.value().equals(deviceInfo.getCa()) ? 2 + "" : 3 + "");
                tips.setC(3 + "");
            }
            if (deviceInfo.getTipName() != null) {
                if (deviceInfo.getTipName()
                        .equals(getString(R.string.activity_scene_item_home))) {
                    tips.setC(0 + "");
                } else if (deviceInfo.getTipName()
                        .equals(getString(R.string.activity_scene_item_outside))) {
                    tips.setC(3 + "");
//                    tips.setC(DeviceInfo.CaMenu.ybq.value().equals(deviceInfo.getCa()) ? 2 + "" : 3 + "");
                } else if (deviceInfo.getTipName()
                        .equals(getString(R.string.devices_list_menu_dialog_jsbts))) {
                    tips.setC(1 + "");
                } else if (deviceInfo.getTipName()
                        .equals(getString(R.string.ybq_chart_name))) {
                    tips.setC(2 + "");
                } else {
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
}
