package com.smartism.znzk.activity.scene;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.adapter.recycleradapter.BaseRecyslerAdapter;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.adapter.scene.ChooseTipsAdapter;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.Tips;
import com.smartism.znzk.domain.XiaXingInfo;
import com.smartism.znzk.domain.scene.DeviceTipsInfo;
import com.smartism.znzk.util.Actions;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.util.HttpRequestUtils;
import com.smartism.znzk.util.JavaThreadPool;
import com.smartism.znzk.util.WeakRefHandler;
import com.smartism.znzk.view.DividerItemDecoration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChooseTipsActivity extends ActivityParentActivity implements BaseRecyslerAdapter.RecyclerItemClickListener, View.OnClickListener {
    private RecyclerView recycler;

    private ChooseTipsAdapter mAdapter;
    private List<RecyclerItemBean> list;
    private List<Tips> tipsList;
    private RecyclerItemBean recyclerItemBean;
    private DeviceInfo deviceInfo;
    private int index = -1; //单选列表当前选中项

    private Button sure;
    private ImageView back_btn;
    private boolean isClt;
    private int type;
    private TextView title_context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_tips);
        recyclerItemBean = (RecyclerItemBean) getIntent().getSerializableExtra("recyclerItemBean");
        tipsList = (List<Tips>) getIntent().getSerializableExtra("tipslist");
        isClt = getIntent().getBooleanExtra("isClt", false);
        type = getIntent().getIntExtra("type", 0);
        initView();
        initRecycle();
        initData();
        initEvent();
    }

    private void initEvent() {
        sure.setOnClickListener(this);
        back_btn.setOnClickListener(this);
    }

    private void initData() {
        DeviceTipsInfo dtinfo = (DeviceTipsInfo) recyclerItemBean.getT();
        deviceInfo = dtinfo.getDeviceInfo();

        if (isClt) {
            if (deviceInfo.getCak() != null && deviceInfo.getCak().equals("control")) { // 控制类型的分类
                getData();
            } else if (deviceInfo.getCak() != null && deviceInfo.getCak().equals("security")) {
                initSecurityItem();
                title_context.setText(getString(R.string.activity_slectscene_title_tips));
            }
        } else {
            if (deviceInfo.getCak() != null && deviceInfo.getCak().equals("control")) { // 触发类型的分类
                getData();
            } else if (deviceInfo.getCak() != null && deviceInfo.getCak().equals("security")) {
                getTip();
            }
        }

    }

    /**
     * 初始化security的
     * 属于单选 index是选中的下标
     */
    public void initSecurityItem() {
        List<String> item = new ArrayList<String>();
        item.add(getString(R.string.activity_scene_item_home));
        if (DeviceInfo.CaMenu.ybq.value().equals(deviceInfo.getCa())) {
            item.add(getString(R.string.ybq_chart_name));

        } else {
            item.add(getString(R.string.devices_list_menu_dialog_jsbts));

            item.add(getString(R.string.activity_scene_item_inhome));
        }
        item.add(getString(R.string.activity_scene_item_outside));

        if (deviceInfo.getTip() == null) {
            list.add(new RecyclerItemBean(new Tips(3 + "", getString(R.string.devices_list_menu_dialog_jsbts), true), 0));

        } else {
            if (DeviceInfo.CaMenu.ybq.value().equals(deviceInfo.getCa())) {


                for (int i = 0; i < item.size(); i++) {
                    int j = 0;
                    String name = item.get(i);
                    if (i != 0)
                        j = i + 1;

                    if (deviceInfo.getTip().equals(String.valueOf(j))) {
                        list.add(new RecyclerItemBean(new Tips(j + "", name, true), 0));
                        index = i;
                    } else {
                        list.add(new RecyclerItemBean(new Tips(j + "", name, false), 0));
                    }
                }

                if (index == -1) {
                    index = 0;
                    ((Tips) list.get(0).getT()).setFlag(true);
                }
            } else {
                for (int i = 0; i < item.size(); i++) {
                    String name = item.get(i);
                    if (deviceInfo.getTip().equals(String.valueOf(i))) {
                        list.add(new RecyclerItemBean(new Tips(i + "", name, true), 0));
                        index = i;
                    } else {
                        list.add(new RecyclerItemBean(new Tips(i + "", name, false), 0));
                    }

                }
                if (index == -1) {
                    index = 0;
                    ((Tips) list.get(0).getT()).setFlag(true);
                }
            }
        }
    }

    private void initView() {
        title_context = (TextView) findViewById(R.id.title_context);
        recycler = (RecyclerView) findViewById(R.id.recycle_tips);
        sure = (Button) findViewById(R.id.sure);
        back_btn = (ImageView) findViewById(R.id.back_btn);
    }

    private void initRecycle() {
        list = new ArrayList<>();
        mAdapter = new ChooseTipsAdapter(list);
        mAdapter.setRecyclerItemClickListener(this);
        //创建默认线性LinearLayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);  //设置布局管理器
        recycler.setItemAnimator(new DefaultItemAnimator()); //设置Item增加、移除动画
        recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recycler.setAdapter(mAdapter);
    }

    @Override
    public void onRecycleItemClick(View view, int position) {

        if ((MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_AIERFUDE)
                || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion())) && DeviceInfo.CaMenu.zhinengsuo.value().equals(deviceInfo.getCa())) {
            Tips tips = (Tips) list.get(position).getT();
            ((Tips) list.get(position).getT()).setFlag(true);
            mAdapter.notifyItemChanged(position);
        } else if (deviceInfo.getCak() != null && deviceInfo.getCak().equals("security")) {
            //触发类实现单选模式
            if (list.size() == 1) {
                ((Tips) list.get(0).getT()).setFlag(!((Tips) list.get(0).getT()).isFlag());
                mAdapter.notifyItemChanged(index);
            } else {
                if (position == index) return;
                ((Tips) list.get(position).getT()).setFlag(true);
                mAdapter.notifyItemChanged(position);
                ((Tips) list.get(index).getT()).setFlag(false);
                mAdapter.notifyItemChanged(index);
                index = position;
            }
        } else {
            //多选指令
            Tips tips = (Tips) list.get(position).getT();
            ((Tips) list.get(position).getT()).setFlag(!tips.isFlag());
            mAdapter.notifyItemChanged(position);
        }
    }

    public boolean getTipsFlag(Tips tips) {
        for (Tips t : tipsList) {
            if (tips.getC().equals(t.getC())) {
                return true;
            }
        }
        return false;
    }

    public void getTip() {
        final long did = deviceInfo.getId();
        final String l = Locale.getDefault().getLanguage() + "-" + Locale.getDefault().getCountry();
        JavaThreadPool.getInstance().excute(new Runnable() {

            @Override
            public void run() {
                DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(ChooseTipsActivity.this,
                        DataCenterSharedPreferences.Constant.CONFIG);
                String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                JSONObject pJsonObject = new JSONObject();
                pJsonObject.put("did", did);
                final String result = HttpRequestUtils.requestoOkHttpPost( server
                        + "/jdm/s3/d/dcomms", pJsonObject, ChooseTipsActivity.this);
                mHandler.post(new Runnable() {

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
                            index = 0;
                            for (int i = 0; i < mTip.size(); i++) {
                                Tips tips = mTip.get(i);
                                if (tipsList == null || tipsList.isEmpty()) {
                                    if (deviceInfo.getTip().equals(tips.getC())) {
                                        tips.setFlag(true);
                                    }
                                    list.add(new RecyclerItemBean(tips, 0));
                                }
                            }
                        }
                        if (list.size() > 0) {
                            mAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(ChooseTipsActivity.this, getString(R.string.device_not_getdata),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

        });

    }

    public void getData() {
        if ((MainApplication.app.getAppGlobalConfig().getVersion().equals(Actions.VersionType.CHANNEL_AIERFUDE)
                || Actions.VersionType.CHANNEL_UHOME.equals(MainApplication.app.getAppGlobalConfig().getVersion()))&& DeviceInfo.CaMenu.zhinengsuo.value().equals(deviceInfo.getCa())) {
            Tips tips = new Tips("002D", getString(R.string.scene_unlock), true);
//            tips.setFlag(getTipsFlag(tips));
            tips.setFlag(true);//锁只有一个指令开锁所以默认选中
            list.clear();
            list.add(new RecyclerItemBean(tips, 0, true));
            mAdapter.notifyDataSetChanged();
        } else {
            final long did = deviceInfo.getId();
            JavaThreadPool.getInstance().excute(new Runnable() {

                @Override
                public void run() {
                    DataCenterSharedPreferences dcsp = DataCenterSharedPreferences.getInstance(ChooseTipsActivity.this,
                            DataCenterSharedPreferences.Constant.CONFIG);
                    String server = dcsp.getString(DataCenterSharedPreferences.Constant.HTTP_DATA_SERVERS, "");
                    JSONObject pJsonObject = new JSONObject();
                    pJsonObject.put("did", did);
                    final String result = HttpRequestUtils.requestoOkHttpPost( server + "/jdm/s3/d/dkeycomms", pJsonObject, ChooseTipsActivity.this);

                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            cancelInProgress();
                            String o = null;
                            try {
                                o = result;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            List<XiaXingInfo> xiaXing = JSON.parseArray(o, XiaXingInfo.class);
                            if (xiaXing != null && xiaXing.size() > 0) {
                                list.clear();
                                for (int i = 0; i < xiaXing.size(); i++) {
                                    Tips tips = new Tips(xiaXing.get(i).getS(), xiaXing.get(i).getN(), false);
                                    tips.setFlag(getTipsFlag(tips));
                                    list.add(new RecyclerItemBean(tips, 0));
                                }
                            }
                            if (list.size() > 0) {
                                mAdapter.notifyDataSetChanged();
                            } else {
                                if (deviceInfo.getCak() != null && deviceInfo.getCak().equals("control")) {
                                    initSecurityItem();
                                } else {
                                    Toast.makeText(ChooseTipsActivity.this, getString(R.string.device_not_getdata),
                                            Toast.LENGTH_LONG).show();
                                }
                            }

                        }
                    });


                }
            });

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sure:
                if (deviceInfo.getCak() != null && deviceInfo.getCak().equals("security")) {
                    Intent intent = new Intent();
                    Tips tips = (Tips) list.get(index).getT();
                    intent.putExtra("tips", tips);
                    setResult(21, intent);
                    finish();
                } else {
                    List<Tips> tipsList = new ArrayList<>();
                    for (RecyclerItemBean bean : list) {
                        Tips tips = (Tips) bean.getT();
                        if (tips.isFlag()) {
                            tipsList.add(tips);
                        }
                    }
                    Intent intent = new Intent();
                    intent.putExtra("tipslist", (Serializable) tipsList);
                    setResult(21, intent);
                    finish();
                }
                break;
            case R.id.back_btn:
                finish();
                break;
        }
    }
}
