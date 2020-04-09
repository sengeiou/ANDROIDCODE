package com.smartism.znzk.domain;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/2/21.
 */

public class OwenerInfo implements Serializable {

    private long id;//主机id
    private long userCountryId;//用户所在国家ID
    private long userAreaId;//用户所在省份ID
    private long userCityId;//用户所在城市ID
    private long userCountyId;//用户所在县ID
    private long userCommunityId ; //用户所在小区id
    private long userStreetId ; //用户所在街道id
    private String masterId;
    private String userName;
    private String userTel;
    private String userPhone;
    private String userAddress;
    private long serviceTime;
    private String userAreaInfo;

    public long getUserCommunityId() {
        return userCommunityId;
    }

    public void setUserCommunityId(long userCommunityId) {
        this.userCommunityId = userCommunityId;
    }

    public long getUserStreetId() {
        return userStreetId;
    }

    public void setUserStreetId(long userStreetId) {
        this.userStreetId = userStreetId;
    }

    @Override
    public String toString() {
        return "OwenerInfo{" +
                "id=" + id +
                ", userAreaId=" + userAreaId +
                ", userCityId=" + userCityId +
                ", userCountryId=" + userCountryId +
                ",userCountyId="+userCountyId+
                ",userCommunityId="+userCommunityId+
                ",userStreetId="+userStreetId+
                ", masterId='" + masterId + '\'' +
                ", userName='" + userName + '\'' +
                ", userTel='" + userTel + '\'' +
                ", userPhone='" + userPhone + '\'' +
                ", userAddress='" + userAddress + '\'' +
                ", serviceTime=" + serviceTime +
                ", userAreaInfo='" + userAreaInfo + '\'' +
                '}';
    }

    public long getUserCountryId() {
        return userCountryId;
    }

    public void setUserCountryId(long userCountryId) {
        this.userCountryId = userCountryId;
    }

    public long getUserCountyId() {
        return userCountyId;
    }

    public void setUserCountyId(long userCountyId) {
        this.userCountyId = userCountyId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public long getUserAreaId() {
        return userAreaId;
    }

    public void setUserAreaId(long userAreaId) {
        this.userAreaId = userAreaId;
    }

    public long getUserCityId() {
        return userCityId;
    }

    public void setUserCityId(long userCityId) {
        this.userCityId = userCityId;
    }


    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserTel() {
        return userTel;
    }

    public void setUserTel(String userTel) {
        this.userTel = userTel;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public long getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(long serviceTime) {
        this.serviceTime = serviceTime;
    }

    public String getUserAreaInfo() {
        return userAreaInfo;
    }

    public void setUserAreaInfo(String userAreaInfo) {
        this.userAreaInfo = userAreaInfo;
    }
}
