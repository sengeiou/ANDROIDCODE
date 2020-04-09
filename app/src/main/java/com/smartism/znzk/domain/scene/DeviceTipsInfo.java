package com.smartism.znzk.domain.scene;

import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.Tips;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/5.
 */

public class DeviceTipsInfo implements Serializable {

    private DeviceInfo deviceInfo;
    private List<Tips> tips = new ArrayList<>();

    public DeviceTipsInfo(DeviceInfo deviceInfo, List<Tips> tips) {
        this.deviceInfo = deviceInfo;
        this.tips = tips;
    }

    public DeviceTipsInfo() {
    }

    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(DeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public List<Tips> getTips() {
        if (tips == null)
            tips = new ArrayList<>();
        return tips;
    }

    public void setTips(List<Tips> tips) {
        this.tips = tips;
    }

    @Override
    public String toString() {
        return "DeviceTipsInfo{" +
                "deviceInfo=" + deviceInfo.toString() +
                ", tips=" + tips.size() +
                '}';
    }
}
