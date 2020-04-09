package com.smartism.znzk.global;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.macrovideo.sdk.custom.DeviceInfo;


public class VList {
    private static VList manager = null;
    // 局域网内搜索到的全部的设备
    private static List<DeviceInfo> allLocalDevices = new ArrayList<DeviceInfo>();

    public VList() {
        manager = this;
    }

    public static VList getInstance() {
        if (manager == null) {
            manager = new VList();
        }
        return manager;
    }

    public void insert(DeviceInfo info) {
        if (allLocalDevices.size() == 0) {
            allLocalDevices.add(info);
        } else {
            for (int i = 0; i < allLocalDevices.size(); i++) {
                if (allLocalDevices.get(i).getnDevID() != info.getnDevID()) {
                    allLocalDevices.add(info);
                }

            }

        }
        Log.i("摄像头", "本地摄像头列表大小：" + allLocalDevices.size());
    }

    public List<DeviceInfo> getDevice() {
        return allLocalDevices;
    }

    public DeviceInfo findById(int id) {
        for (int i = 0; i < allLocalDevices.size(); i++) {
            if (allLocalDevices.get(i).getnDevID() == id) {
                return allLocalDevices.get(i);
            }
        }

        return null;
    }


}
