package com.smartism.znzk.domain.camera;




import java.io.Serializable;
import java.util.Arrays;

import com.smartism.znzk.R;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.camera.Utils;

/**
 * Created by dxs on 2016/1/18.
 */
public class Sensor implements Serializable,Cloneable {
    public static int[] modes=new int[]{R.string.music_0,R.string.music_1,R.string.music_2,R.string.music_3,R.string.music_4,R.string.music_5,R.string.music_6,R.string.music_7};
    public static final int SENSORCATEGORY_NORMAL=0;
    public static final int SENSORCATEGORY_SPECIAL=1;
    public static final int NameLen=16;
    private int SensorCategory=0;//是否是特殊传感器
    private byte SensorType=0;//传感器类型
    private byte[] SensorData=new byte[7];
    private byte[] SensorName=new byte[NameLen];
    private byte isNameChanged=0;

    private byte LampState=0;//插座状态 0正在查询,1打开成功,2关闭成功,3查询到开,4查询到关

    public Sensor() {
    }

    /**
     * 传感器构造
     * @param sensorCategory
     * @param data
     * @param type
     */
    public Sensor(int sensorCategory,byte[] data,byte type){
        this.SensorCategory=sensorCategory;
        if(SensorCategory==SENSORCATEGORY_NORMAL){
            if(type==0){
                System.arraycopy(data,0,SensorData,0,4);
                System.arraycopy(data,4,SensorName,0,SensorName.length);
            }else{
                System.arraycopy(data,0,SensorData,0,7);
                System.arraycopy(data,7,SensorName,0,SensorName.length);
            }
            SensorType=SensorData[0];
            isNameChanged=data[data.length-1];
        }else{
            SensorData[0]=data[0];
            System.arraycopy(data,1,SensorData,4,data.length-1);
            SensorType=SensorData[0];
        }

    }

    public Sensor(int sensorCategory,byte[] data){
        this.SensorCategory=sensorCategory;
        System.arraycopy(data,3,SensorData,0,7);
        System.arraycopy(data, 10, SensorName, 0, SensorName.length);
        SensorType=SensorData[0];
        isNameChanged=data[data.length-2];//是否改过名字
        LampState=data[data.length-1];
    }

    public int getSensorCategory() {
        return SensorCategory;
    }

    public void setSensorCategory(int sensorCategory) {
        SensorCategory = sensorCategory;
    }

    public byte getSensorType() {
        return SensorType;
    }

    public void setSensorType(byte sensorType) {
        SensorType = sensorType;
    }

    public String getName(){
        return new String(SensorName).trim();
    }

    public byte getLampState() {
        return LampState;
    }

    public void setLampState(byte lampState) {
        LampState = lampState;
    }

    /**
     * 获得传感器特征码
     * @return
     */
    public byte[] getSensorData() {
        if(SensorCategory==SENSORCATEGORY_NORMAL){
            return SensorData;
        }else{
            byte[] specialData=new byte[4];
            specialData[0]=SensorData[0];
            System.arraycopy(SensorData,4,specialData,1,specialData.length-1);
            return specialData;
        }

    }

    public byte getIsNameChanged() {
        return isNameChanged;
    }

    public void setIsNameChanged(byte isNameChanged) {
        this.isNameChanged = isNameChanged;
    }

    /**
     * 获取开关状态,遥控不可获取
     * @return
     */
//    public boolean getSensorSwitch(){
//        if(SensorCategory==SENSORCATEGORY_NORMAL&&SensorType== Constants.SensorType.TYPE_REMOTE_CONTROLLER) return false;
//        int[] home=Utils.getByteBinnery(SensorData[4],false);
//        int[] out=Utils.getByteBinnery(SensorData[5],false);
//        int[] sleep=Utils.getByteBinnery(SensorData[6],false);
//        return (home[0]|out[0]|sleep[0])==1;
//    }
    /**
     * 获取传感器开关
     */
    public boolean getSensorSwitch(){
    	if (SensorCategory == SENSORCATEGORY_NORMAL && SensorType == Constants.SensorType.TYPE_REMOTE_CONTROLLER)
            return false;
        int[] home = Utils.getByteBinnery(SensorData[4], false);
        int[] out = Utils.getByteBinnery(SensorData[5], false);
        int[] sleep = Utils.getByteBinnery(SensorData[6], false);
        return (home[0] | out[0] | sleep[0]) == 1;
    }

    /**
     * 获取模式下某个开关
     * @param mode
     * @param type
     * @return
     */
    public boolean getSensorStateAtMode(int mode,int type){
        int[] temp=Utils.getByteBinnery(SensorData[4+mode],true);
        return temp[type]==1;
    }

    /**
     * 设置传感器开关
     * @param isEnable
     */
    public void setSensorSwitch(boolean isEnable){
        if(isEnable){
            SensorData[4]=Utils.ChangeBitTrue(SensorData[4],7);
            SensorData[5]=Utils.ChangeBitTrue(SensorData[5],7);
            SensorData[6]=Utils.ChangeBitTrue(SensorData[6],7);
        }else{
            SensorData[4]=Utils.ChangeByteFalse(SensorData[4], 7);
            SensorData[5]=Utils.ChangeByteFalse(SensorData[5], 7);
            SensorData[6]=Utils.ChangeByteFalse(SensorData[6],7);
        }
    }

    /**
     * 设置某种模式下某个项目的开关
     * @param mode 0 home 1 out 2 sleeep
     * @param type
     * @param isEnable
     */
    public void setSensorStateAtMode(int mode,int type,boolean isEnable){
        if(isEnable){
            SensorData[4+mode]=Utils.ChangeBitTrue(SensorData[4+mode],type);
        }else{
            SensorData[4+mode]=Utils.ChangeByteFalse(SensorData[4+mode],type);
        }
    }

    /**
     * 获取传感器特征码
     * @return 传感器特征码
     */
    public byte[] getSensorSignature(){
        byte[] Signature=new byte[4];
        System.arraycopy(SensorData,0,Signature,0,Signature.length);
        return Signature;
    }

    public void setName(String name){
        byte[] namebyte=name.getBytes();
        if(namebyte.length<=NameLen){
            Arrays.fill(SensorName, (byte) 0);
            System.arraycopy(namebyte,0,SensorName,0,namebyte.length);
        }
    }

    /**
     * 更新传感器数据
     * @param data 新数据
     * @param offset 偏移值
     */
    public void upDataSensorData(byte[] data,int offset){
        if(SensorCategory==SENSORCATEGORY_NORMAL){
            System.arraycopy(data,offset,SensorData,0,SensorData.length);
        }else{
            SensorData[0]=data[offset];
            System.arraycopy(data,1+offset,SensorData,4,3);
            SensorType=SensorData[0];
        }

    }

    /**
     * 获取某种模式下的所有信息
     * @param mode
     * @return
     */
    public byte getSensorModeInfo(int mode){
        return SensorData[mode+4];
    }

    /**
     * 设置模式下的信息新值
     * @param mode
     * @param modeinfo
     */
    public void setSensorModeInfo(int mode,byte modeinfo){
        SensorData[mode+4]=modeinfo;
    }
    
    /**
     * 是否控制类传感器
     *
     * @return
     */
    public boolean isControlSensor() {
        if (SensorCategory == SENSORCATEGORY_NORMAL && (SensorType == Constants.SensorType.TYPE_JACK
                || SensorType == Constants.SensorType.TYPE_LIGHT
                || SensorType == Constants.SensorType.TYPE_CURTAIN
                || SensorType == Constants.SensorType.TYPE_WARNING_SPEAKER)) {
            return true;
        }
        return false;
    }

    public Object clone() {
        Sensor o = null;
        try {
            o = (Sensor) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return o;
    }
}
