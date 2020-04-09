package com.smartism.znzk.domain;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by win7 on 2017/6/24.
 */

public class SmartLockInfo implements Parcelable {
//    id	记录id
//    number	编号
//    type	类型 0指纹  1卡片 2密码 3钥匙 4用户 5遥控器 6人脸 7掌纹  8虹膜
//    lname	昵称
//    permission	权限(0临时权限 1正常权限 2暂停使用 3授权次数 4授权时长)
//    perResidueDegree	剩余开锁次数
//    perStartTime	授权开始时间 年后两位月日时
//    perEndTime	授权结束时间 年后两位月日时
//    roleId	角色ID
//    roleName	角色名称
//    roleKey	角色标示
//    appId	用户ID
//    appName	用户昵称
//    updateTime	最后更新时间
//    createTime	创建时间

    private long id;
    private String number;//昵称
    private int type;
    private String lname;
    private int permission;
    private int perResidueDegree;
    private String perStartTime;
    private String perEndTime;
    private long roleId;
    private String roleName;
    private String roleKey;
    private long appId;
    private String appName;
    private long updateTime;
    private long createTime;
    private String conPassword;


    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getConPassword() {
        return conPassword;
    }

    public void setConPassword(String conPassword) {
        this.conPassword = conPassword;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public int getPerResidueDegree() {
        return perResidueDegree;
    }

    public void setPerResidueDegree(int perResidueDegree) {
        this.perResidueDegree = perResidueDegree;
    }

    public String getPerStartTime() {
        return perStartTime;
    }

    public void setPerStartTime(String perStartTime) {
        this.perStartTime = perStartTime;
    }

    public String getPerEndTime() {
        return perEndTime;
    }

    public void setPerEndTime(String perEndTime) {
        this.perEndTime = perEndTime;
    }

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public SmartLockInfo(){}

    protected SmartLockInfo(Parcel in) {
        id = in.readLong();
        number = in.readString();
        type = in.readInt();
        lname = in.readString();
        permission = in.readInt();
        perResidueDegree = in.readInt();
        perStartTime = in.readString();
        perEndTime = in.readString();
        roleId = in.readLong();
        roleName = in.readString();
        roleKey = in.readString();
        appId = in.readLong();
        appName = in.readString();
        updateTime = in.readLong();
        createTime = in.readLong();
        conPassword = in.readString();
    }

    public static final Creator<SmartLockInfo> CREATOR = new Creator<SmartLockInfo>() {
        @Override
        public SmartLockInfo createFromParcel(Parcel in) {
            return new SmartLockInfo(in);
        }

        @Override
        public SmartLockInfo[] newArray(int size) {
            return new SmartLockInfo[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeLong(id);
        dest.writeString(number);
        dest.writeInt(type);
        dest.writeString(lname);
        dest.writeInt(permission);
        dest.writeInt(perResidueDegree);
        dest.writeString(perStartTime);
        dest.writeString(perEndTime);
        dest.writeLong(roleId);
        dest.writeString(roleName);
        dest.writeString(roleKey);
        dest.writeLong(appId);
        dest.writeString(appName);
        dest.writeLong(updateTime);
        dest.writeLong(createTime);
        dest.writeString(conPassword);
    }

    @Override
    public String toString() {
        return "roleId:"+roleId
                +",roleKey:"+roleKey
                +",roleName:"+roleName;
    }
}
