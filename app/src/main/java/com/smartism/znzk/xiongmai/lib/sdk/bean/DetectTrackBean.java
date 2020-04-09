package com.smartism.znzk.xiongmai.lib.sdk.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by hws on 2018-06-28.
 * 人形跟随
 * 2019年09月28日 增加拷贝功能
 */

public class DetectTrackBean implements Cloneable{
    public static final String JSON_NAME = "Detect.DetectTrack";
    @JSONField(name = "Enable")
    private int enable;
    @JSONField(name = "Sensitivity")
    private int sensitivity;
    @JSONField(name = "ReturnTime")
    private int returnTime;//单位 秒

    @Override
    public DetectTrackBean clone() {//无对象成员变量 用浅拷贝即可
        try {
            return (DetectTrackBean) super.clone();
        }catch (CloneNotSupportedException ex){
            System.out.println("DetectTrackBean doesn't implement Cloneable");
        }
        return this;
    }

    public int getEnable() {
        return enable;
    }

    public void setEnable(int enable) {
        this.enable = enable;
    }

    public int getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(int sensitivity) {
        this.sensitivity = sensitivity;
    }

    public int getReturnTime() {
        return returnTime;
    }

    public void setReturnTime(int returnTime) {
        this.returnTime = returnTime;
    }
}
