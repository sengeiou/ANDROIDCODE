package com.smartism.znzk.activity.scene;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.activity.device.ZhujiListFragment;
import com.smartism.znzk.adapter.recycleradapter.BaseRecyslerAdapter;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.adapter.scene.ChooseDeviceAdapter;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.Tips;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.scene.DeviceTipsInfo;
import com.smartism.znzk.util.DataCenterSharedPreferences;
import com.smartism.znzk.view.DividerItemDecoration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChooseDeviceActivity extends ActivityParentActivity implements View.OnClickListener, BaseRecyslerAdapter.RecyclerItemClickListener {
    private RecyclerView recycler;
    private ChooseDeviceAdapter mAdapter;
    private List<RecyclerItemBean> defList;//本身已经存在的设备列表
    private List<RecyclerItemBean> deviceLis;//除了本身存在的所有设备
    private List<RecyclerItemBean> chooseList;//选中的设备
    private ZhujiInfo zhuji;
    private Intent intent;
    private int flag;//用于标识所选择的设备类型  0、 所有  1、触发设备  2、控制设备
    private boolean isClt;
    private Button sure;
    private ImageView back_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_device);
        intent = getIntent();
//        zhuji = DatabaseOperator.getInstance(ChooseDeviceActivity.this).queryDeviceZhuJiInfo(dcsp.getString(DataCenterSharedPreferences.Constant.APP_MASTERID, ""));
        //替换
        zhuji = DatabaseOperator.getInstance(ChooseDeviceActivity.this).queryDeviceZhuJiInfo(ZhujiListFragment.getMasterId());
        initView();
        initDate();
        initEvent();
    }

    private void initEvent() {
        sure.setOnClickListener(this);
        back_btn.setOnClickListener(this);
    }

    private void initDate() {
        defList = new ArrayList<>();
        deviceLis = new ArrayList<>();
        chooseList = new ArrayList<>();
        flag = intent.getIntExtra("flag", 0);
        isClt = intent.getBooleanExtra("isClt", false);
        List<RecyclerItemBean> list = (List<RecyclerItemBean>) intent.getSerializableExtra("deviceInfoses");
        if (list != null && !list.isEmpty()) defList.addAll(list);
        initRecycle();
        initDeviceList();
    }

    /**
     * c初始化Recycler 设备列表
     */
    private void initRecycle() {
        mAdapter = new ChooseDeviceAdapter(deviceLis);
        mAdapter.setRecyclerItemClickListener(this);//item点击监听
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);//创建默认线性LinearLayoutManager
        recycler.setLayoutManager(layoutManager);  //设置布局管理器
        recycler.setItemAnimator(new DefaultItemAnimator()); //设置Item增加、移除动画
        recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        recycler.setAdapter(mAdapter);
    }

    private void initView() {
        recycler = (RecyclerView) findViewById(R.id.recycle_device);
        sure = (Button) findViewById(R.id.sure);
        back_btn = (ImageView) findViewById(R.id.back_btn);
    }

    @Override
    public void onRecycleItemClick(View view, int position) {
        DeviceTipsInfo dtInfo = (DeviceTipsInfo) deviceLis.get(position).getT();
        DeviceInfo info = dtInfo.getDeviceInfo();
        ((DeviceTipsInfo) deviceLis.get(position).getT()).getDeviceInfo().setFlag(!info.isFlag());
        mAdapter.notifyItemChanged(position);
    }

    /**
     * 初始化数据源
     */
    public void initDeviceList() {
        switch (flag) {
            case 0:
                //所有设备
                getAllDeviceList(zhuji);
                break;
            case 1:
                //触发设备
                getTriDeviceList(zhuji);
                break;
            case 2:
                //控制设备
                getCltDeviceList(zhuji);
                break;
        }
        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    /**
     * 获所有发设备
     *
     * @param info
     * @return
     */
    public void getAllDeviceList(ZhujiInfo info) {
        if (info != null) {
            Cursor cursor = DatabaseOperator.getInstance(ChooseDeviceActivity.this).getWritableDatabase()
                    .rawQuery("select * from DEVICE_STATUSINFO where zj_id = ? order by sort desc", new String[]{String.valueOf(zhuji.getId())});
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    DeviceInfo deviceInfo = DatabaseOperator.getInstance(getApplicationContext()).buildDeviceInfo(cursor);
                    if (DeviceInfo.CakMenu.security.value().equals(deviceInfo.getCak())) {
                        //触发设备只显示安防类别
                        deviceLis.add(new RecyclerItemBean(new DeviceTipsInfo(deviceInfo, new ArrayList<Tips>()), 0));
                    }
                }
                cursor.close();
            }
        }
    }

    /**
     * 获取触发设备
     *
     * @param info
     * @return
     */
    public void getTriDeviceList(ZhujiInfo info) {
        if (info != null) {
            Cursor cursor = DatabaseOperator.getInstance(ChooseDeviceActivity.this).getWritableDatabase()
                    .rawQuery("select * from DEVICE_STATUSINFO where zj_id = ? order by sort desc", new String[]{String.valueOf(zhuji.getId())});
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    DeviceInfo deviceInfo = DatabaseOperator.getInstance(getApplicationContext()).buildDeviceInfo(cursor);
                    if ( (DeviceInfo.CakMenu.security.value().equals(deviceInfo.getCak()) && !(DeviceInfo.CaMenu.ybq.value()).equals(deviceInfo.getCa()) ) ||  DeviceInfo.CaMenu.zhinengsuo.value().equals(deviceInfo.getCa())) {
                        //触发设备只显示安防类别 迎宾器不显示
                        boolean flag = setDeviceFlag(deviceInfo);
                        deviceInfo.setFlag(flag);
                        deviceLis.add(new RecyclerItemBean(new DeviceTipsInfo(deviceInfo, new ArrayList<Tips>()), 0));
                    }
                }
                cursor.close();
            }
        }
    }

    /**
     * 获取控制设备
     *
     * @param info
     * @return
     */
    public void getCltDeviceList(ZhujiInfo info) {
        if (info != null) {
            Cursor cursor = DatabaseOperator.getInstance(ChooseDeviceActivity.this).getWritableDatabase()
                    .rawQuery("select * from DEVICE_STATUSINFO where zj_id = ? order by sort desc", new String[]{String.valueOf(zhuji.getId())});
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    DeviceInfo deviceInfo = DatabaseOperator.getInstance(getApplicationContext()).buildDeviceInfo(cursor);
                    if (deviceInfo.getControlType().equals(DeviceInfo.ControlTypeMenu.zhuji.value())
                            || DeviceInfo.CaMenu.hongwaizhuanfaqi.value().equals(deviceInfo.getCak())
                            || DeviceInfo.CakMenu.detection.value().equals(deviceInfo.getCak())
                            || DeviceInfo.CakMenu.health.value().equals(deviceInfo.getCak())
                            || DeviceInfo.CakMenu.surveillance.value().equals(deviceInfo.getCak())
                            || DeviceInfo.CaMenu.zhujiControl.value().equals(deviceInfo.getCa())
                            || DeviceInfo.CaMenu.zhinengsuo.value().equals(deviceInfo.getCa())
                            || DeviceInfo.CaMenu.hongwaizhuanfaqi.value().equals(deviceInfo.getCa())
                            || DeviceInfo.CaMenu.zhujifmq.value().equals(deviceInfo.getCa())
                            || DeviceInfo.CaMenu.znyx.value().equals(deviceInfo.getCa())
                            || DeviceInfo.CaMenu.menling.value().equals(deviceInfo.getCa())
                            ||DeviceInfo.CaMenu.ldtsq.value().equals(deviceInfo.getCa())
                            ||DeviceInfo.CaMenu.yxj.value().equals(deviceInfo.getCa())
                            ||deviceInfo.isFa()) {//24小时防区设备不能被控制
                        //健康采集，智能锁，主机, ,数据采集设备,主机遥控器设备,主机内置蜂鸣器设备 都不能应用于场景的联动控制
                    } else {
                        deviceInfo.setFlag(setDeviceFlag(deviceInfo));
                        deviceLis.add(new RecyclerItemBean(new DeviceTipsInfo(deviceInfo, new ArrayList<Tips>()), 0));
                    }
                }
                cursor.close();
            }
        }
    }

    public boolean setDeviceFlag(DeviceInfo info) {
        //标记已经存在的设备后删除已经存在的设备，减少遍历的次数
        if (defList != null && !defList.isEmpty()) {
            for (RecyclerItemBean bean : defList) {
                DeviceTipsInfo dtInfo = (DeviceTipsInfo) bean.getT();
                DeviceInfo deviceInfo = dtInfo.getDeviceInfo();
                if (info.getId() == deviceInfo.getId()) {
                    defList.remove(bean);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sure:
                getChooseDeviecs();
                Intent intent = new Intent();
                intent.putExtra("cleList", (Serializable) chooseList);
                setResult(23, intent);
                finish();
                break;
            case R.id.back_btn:
                finish();
                break;
        }
    }

    public List<RecyclerItemBean> getChooseDeviecs() {
        if (chooseList == null) chooseList = new ArrayList<>();
        if (deviceLis != null && !deviceLis.isEmpty()) {
            for (RecyclerItemBean bean : deviceLis) {
                DeviceTipsInfo dtInfo = (DeviceTipsInfo) bean.getT();
                DeviceInfo deviceInfo = dtInfo.getDeviceInfo();
                if (deviceInfo.isFlag()) {
                    chooseList.add(new RecyclerItemBean(dtInfo, 0));
                }
            }
        }
        return chooseList;
    }
}
