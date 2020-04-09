package com.smartism.znzk.activity.scene;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smartism.znzk.R;
import com.smartism.znzk.activity.ActivityParentActivity;
import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;
import com.smartism.znzk.adapter.scene.DevicesAdapter;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.FoundInfo;
import com.smartism.znzk.domain.Tips;
import com.smartism.znzk.domain.scene.DeviceTipsInfo;
import com.smartism.znzk.util.CollectionsUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by Administrator on 2017/6/13.
 */

public class SceneBaseActivity extends ActivityParentActivity {
    public int showLines = 2;//设备列表显的条数
    public ImageView right_menu;
    public ImageView back_btn;
    public EditText et_scene_name;
    //控制列表
    public TextView tv_foot;
    public ImageView img_foot;
    //触发列表
    public TextView tv_foot_t;
    public ImageView img_foot_t;
    //控制设备
    public RelativeLayout custom_scene_add;
    public RecyclerView recycler;
    public DevicesAdapter cltAdapter, touAdapter;
    public RecyclerItemBean cltItemBean; //点击item进入选择指令的对象
    public RecyclerItemBean touchItemBean;//点击item进入选择指令的对象
    public FoundInfo resultStr = null;
    public boolean edit = true;
    public List<DeviceInfo> deviceInfos;
    public boolean isEdit = false;//是否是编辑模式

    public List<RecyclerItemBean> touchList;//触发设备
    public List<RecyclerItemBean> cltList;//控制设备


    boolean cltFlag = true;//数据展开收起 true 收起状态
    boolean touchFlag = true;//数据展开收起 true 收起状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resultStr = (FoundInfo) getIntent().getSerializableExtra("result");
        edit = getIntent().getBooleanExtra("edit", true);
        if (resultStr != null) isEdit = true;
        deviceInfos = new ArrayList<>();
        loadDevice();
    }

    //获取所有的设备
    public void loadDevice() {
        Cursor cursor = DatabaseOperator.getInstance(SceneBaseActivity.this).getWritableDatabase()
                .rawQuery("select * from DEVICE_STATUSINFO order by sort desc", new String[]{});
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                DeviceInfo info = DatabaseOperator.getInstance(getApplicationContext()).buildDeviceInfo(cursor);
                deviceInfos.add(info);
            }
            cursor.close();

        }
    }

    //整理触发类型的设备
    public List<RecyclerItemBean> getTrriList() {
        if (resultStr == null) return new ArrayList<>();
        List<FoundInfo.TriggerInfosEntity> triggerInfos = resultStr.getTriggerInfos();
        if (triggerInfos != null && !triggerInfos.isEmpty()) {
            List<RecyclerItemBean> trriList = new ArrayList<>();
            List<DeviceInfo> devices = matchTrriDevice(triggerInfos);
            for (DeviceInfo info : devices) {
                DeviceTipsInfo dtInfo = new DeviceTipsInfo();
                dtInfo.setDeviceInfo(info.deepClone());
                trriList.add(new RecyclerItemBean(dtInfo, 0));
            }
            for (FoundInfo.TriggerInfosEntity entity : triggerInfos) {
                for (int i = 0; i < trriList.size(); i++) {
                    DeviceTipsInfo dtInfo = (DeviceTipsInfo) trriList.get(i).getT();
                    DeviceInfo deviceInfo = dtInfo.getDeviceInfo();
                    if (String.valueOf(entity.getDevice()).equals(String.valueOf(deviceInfo.getId()))) {
                        ((DeviceTipsInfo) trriList.get(i).getT()).getDeviceInfo().setTipName(entity.getCommandName());
                        ((DeviceTipsInfo) trriList.get(i).getT()).getDeviceInfo().setTip(entity.getCommand());
                    }
                }
            }
            return trriList;
        }
        return new ArrayList<>();
    }


    //整理操控类型的设备
    public List<RecyclerItemBean> getCltList() {
        if (resultStr == null) return new ArrayList<>();
        List<FoundInfo.ControlInfosEntity> controlInfos = resultStr.getControlInfos();
        if (controlInfos != null && !controlInfos.isEmpty()) {
            List<RecyclerItemBean> cltList = new ArrayList<>();
            List<DeviceInfo> devices = matchCltDevice(controlInfos);
            for (DeviceInfo info : devices) {
                DeviceTipsInfo dtInfo = new DeviceTipsInfo();
                dtInfo.setDeviceInfo(info.deepClone());
                cltList.add(new RecyclerItemBean(dtInfo, 0));
            }
            for (int i = 0; i < cltList.size(); i++) {
                DeviceTipsInfo dtInfo = (DeviceTipsInfo) cltList.get(i).getT();
                DeviceInfo deviceInfo = dtInfo.getDeviceInfo();
                for (FoundInfo.ControlInfosEntity entity : controlInfos) {
                    if (String.valueOf(entity.getDeviceId()).equals(String.valueOf(deviceInfo.getId()))) {
                        if (deviceInfo.getCak().equals("security")) {
                            ((DeviceTipsInfo) cltList.get(i).getT()).getDeviceInfo().setTipName(entity.getCommandName());
                            ((DeviceTipsInfo) cltList.get(i).getT()).getDeviceInfo().setTip(entity.getCommand());
                        } else {
                            Tips tips = new Tips();
                            tips.setE(entity.getCommandName());
                            tips.setC(entity.getCommand());
                            ((DeviceTipsInfo) cltList.get(i).getT()).getTips().add(tips);
                        }
                    }
                }
            }
            return cltList;
        }
        return new ArrayList<>();
    }


    /**
     * 匹配是否有该设备
     *
     * @return
     */
    public List<DeviceInfo> matchTrriDevice(List<FoundInfo.TriggerInfosEntity> triggerInfos) {
        if (deviceInfos == null || deviceInfos.isEmpty()) return null;
        List<DeviceInfo> itemBeanSet = new ArrayList<>();
        for (FoundInfo.TriggerInfosEntity entity : triggerInfos) {
            for (DeviceInfo info : deviceInfos) {
                if (String.valueOf(entity.getDevice()).equals(String.valueOf(info.getId()))) {
                    itemBeanSet.add(info);
                }
            }
        }
        return itemBeanSet;
    }


    /**
     * 匹配是否有该设备,并去掉重复设备
     *
     * @return
     */
    public List<DeviceInfo> matchCltDevice(List<FoundInfo.ControlInfosEntity> cltInfos) {
        if (deviceInfos == null || deviceInfos.isEmpty()) return null;
        List<DeviceInfo> itemBeanSet = new ArrayList<>();


        for (FoundInfo.ControlInfosEntity entity : cltInfos) {
            for (DeviceInfo info : deviceInfos) {
                if (String.valueOf(entity.getDeviceId()).equals(String.valueOf(info.getId()))) {
                    itemBeanSet.add(info);
                }
            }
        }

        if (!CollectionsUtils.isEmpty(itemBeanSet)) {
            itemBeanSet = removeDuplicateUser(itemBeanSet);
        }
        return itemBeanSet;
    }


    private static ArrayList<DeviceInfo> removeDuplicateUser(List<DeviceInfo> infos) {
        Set<DeviceInfo> set = new TreeSet<DeviceInfo>(new Comparator<DeviceInfo>() {
            @Override
            public int compare(DeviceInfo o1, DeviceInfo o2) {
                //字符串,则按照asicc码升序排列
                return String.valueOf(o1.getId()).compareTo(String.valueOf(o2.getId()));
            }
        });
        set.addAll(infos);
        return new ArrayList<DeviceInfo>(set);
    }


    public void changeCltFootState() {
        tv_foot.setText(getString(R.string.scene_list_open));
        img_foot.setImageResource(R.drawable.zhzj_tjcj_zhankai);
        cltAdapter.setListSize(showLines);
    }

    public void changeTouFootState() {
        tv_foot_t.setText(getString(R.string.scene_list_open));
        img_foot_t.setImageResource(R.drawable.zhzj_tjcj_zhankai);
        cltAdapter.setListSize(showLines);
    }

}
