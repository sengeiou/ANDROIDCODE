package com.smartism.znzk.domain;

import java.io.Serializable;

/**
 * Created by win7 on 2016/8/31.
 */
public class WeightUserInfo implements Serializable {
    private long userId;
    private String userName;
    private String userSex;
    private int    userHeight;
    private String userBirthday;
    private String userLogo;
    private String userObjectiveWeight;
    private long skinFid;//抱婴者ID

    public long getSkinFid() {
        return skinFid;
    }

    public void setSkinFid(long skinFid) {
        this.skinFid = skinFid;
    }

    private int odbp;//舒张压
    private int osbp;//收缩压

    public int getOdbp() {
        return odbp;
    }

    public void setOdbp(int odbp) {
        this.odbp = odbp;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getOsbp() {
        return osbp;
    }

    public void setOsbp(int osbp) {
        this.osbp = osbp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserSex() {
        return userSex;
    }

    public void setUserSex(String userSex) {
        this.userSex = userSex;
    }

    public int getUserHeight() {
        return userHeight;
    }

    public void setUserHeight(int userHeight) {
        this.userHeight = userHeight;
    }



    public String getUserBirthday() {
        return userBirthday;
    }

    public void setUserBirthday(String userBirthday) {
        this.userBirthday = userBirthday;
    }

    public String getUserLogo() {
        return userLogo;
    }

    public void setUserLogo(String userLogo) {
        this.userLogo = userLogo;
    }

    public String getUserObjectiveWeight() {
        return userObjectiveWeight;
    }

    public void setUserObjectiveWeight(String userObjectiveWeight) {
        this.userObjectiveWeight = userObjectiveWeight;
    }


    @Override
    public String toString() {
        return "WeightUserInfo [userId=" + userId + ", name=" + userName + ",userSex=" + userSex
                + ", height=" + userHeight + ", logo=" + userLogo + ",objectiveHeight="+userObjectiveWeight+",birthday="+userBirthday+"]";
    }
}
