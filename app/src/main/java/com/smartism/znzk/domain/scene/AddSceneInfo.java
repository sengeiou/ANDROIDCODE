package com.smartism.znzk.domain.scene;

import com.smartism.znzk.adapter.recycleradapter.RecyclerItemBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 封装添加场景要上传的数据
 * Created by Administrator on 2017/6/6.
 */

public class AddSceneInfo implements Serializable {
    public List<RecyclerItemBean> devices = new ArrayList<>();
    public List<RecyclerItemBean> triggerDeviceInfos = new ArrayList<>();
    public long id;
    public String name;
    public int type = 0;
    public String cycleTime;
    public String timers;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<RecyclerItemBean> getDevices() {
        return devices;
    }

    public void setDevices(List<RecyclerItemBean> devices) {
        this.devices = devices;
    }

    public List<RecyclerItemBean> getTriggerDeviceInfos() {
        return triggerDeviceInfos;
    }

    public void setTriggerDeviceInfos(List<RecyclerItemBean> triggerDeviceInfos) {
        this.triggerDeviceInfos = triggerDeviceInfos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getCycleTime() {
        return cycleTime;
    }

    public void setCycleTime(String cycleTime) {
        this.cycleTime = cycleTime;
    }

    public String getTimers() {
        return timers;
    }

    public void setTimers(String timers) {
        this.timers = timers;
    }
}
