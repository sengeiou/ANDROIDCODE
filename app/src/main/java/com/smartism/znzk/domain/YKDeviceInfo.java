package com.smartism.znzk.domain;

import java.io.Serializable;

/**
 * Created by win7 on 2017/3/17.
 */

public class YKDeviceInfo implements Serializable {
    private int imageId;
    private String brand;
    private String type;
    private  String name;
    private long eid ;//红外eid
    private int status;//最新指令 状态 0 on 1 0ff

    public int gettId() {
        return tId;
    }

    public void settId(int tId) {
        this.tId = tId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    private int tId;//设备类型ID

    public long getEid() {
        return eid;
    }

    public void setEid(long eid) {
        this.eid = eid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String codeId;//本地存储码库的组成键

    public String getCodeId() {
        return codeId;
    }

    public void setCodeId(String codeId) {
        this.codeId = codeId;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}