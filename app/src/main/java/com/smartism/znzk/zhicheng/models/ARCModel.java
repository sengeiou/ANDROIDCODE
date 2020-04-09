package com.smartism.znzk.zhicheng.models;

import android.os.Parcel;
import android.os.Parcelable;

//某一个品牌下的空调遥控器型号种类
public class ARCModel implements Parcelable {


    private int brandType = 1 ; //空调
    private String rcName = "";//遥控器名字
    private String kfId  ; //遥控器型号id
    private int parentId  ; //某一个空调牌子的id
    private String parentName = "";//空调品牌名
    private String pinyin ;
    private String firstTwoLetters ="";

    public String getFirstTwoLetters() {
        return firstTwoLetters;
    }

    public void setFirstTwoLetters(String firstTwoLetters) {
        this.firstTwoLetters = firstTwoLetters;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getLocalServereid() {
        return localServereid;
    }

    public void setLocalServereid(String localServereid) {
        this.localServereid = localServereid;
    }

    private String localServereid = "";



    public int getBrandType() {
        return brandType;
    }

    public void setBrandType(int brandType) {
        this.brandType = brandType;
    }


    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }



    public String getRcName() {
        return rcName;
    }

    public void setRcName(String rcName) {
        this.rcName = rcName;
    }

    public String getKfId() {
        return kfId;
    }

    public void setKfId(String kfId) {
        this.kfId = kfId;
    }

    public int getParentId() {
        return parentId;
    }

    public ARCModel(){
    }

    public void setParentId(int parentId){
        this.parentId = parentId;
    }



    private  ARCModel(Parcel in){
        this.rcName = in.readString();
        this.kfId = in.readString() ;
        this.parentId = in.readInt();
        this.parentName = in.readString();
        this.brandType = in.readInt();
        this.localServereid = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(rcName);
        dest.writeString(kfId);
        dest.writeInt(parentId);
        dest.writeString(parentName);
        dest.writeInt(brandType);
        dest.writeString(localServereid);
    }

    public static final Parcelable.Creator<ARCModel> CREATOR =
            new Parcelable.Creator<ARCModel>(){

                @Override
                public ARCModel createFromParcel(Parcel source) {
                    return new ARCModel(source);
                }

                @Override
                public ARCModel[] newArray(int size) {
                    return new ARCModel[0];
                }
            };

    @Override
    public String toString() {
        return "brandName:"+parentName+
                ",kfid:"+kfId+",rcName:"+rcName;
    }
}
