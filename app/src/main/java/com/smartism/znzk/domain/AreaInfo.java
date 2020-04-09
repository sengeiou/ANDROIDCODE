package com.smartism.znzk.domain;

/**
 * Created by Administrator on 2017/2/21.
 */

public class AreaInfo {
    private long id;//主机id
    private String areaName;
    private long areaParentId;
    private String areaRegion;

    public int getAreaLevel() {
        return areaLevel;
    }

    public void setAreaLevel(int areaLevel) {
        this.areaLevel = areaLevel;
    }

    private int areaLevel ;

    @Override
    public String toString() {
        return "AreaInfo{" +
                "id=" + id +
                ", areaName='" + areaName + '\'' +
                ", areaParentId='" + areaParentId + '\'' +
                ", areaRegion='" + areaRegion + '\'' +
                ",areaLevel='"+areaLevel+'\''+
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public long getAreaParentId() {
        return areaParentId;
    }

    public void setAreaParentId(long areaParentId) {
        this.areaParentId = areaParentId;
    }

    public String getAreaRegion() {
        return areaRegion;
    }

    public void setAreaRegion(String areaRegion) {
        this.areaRegion = areaRegion;
    }
}
