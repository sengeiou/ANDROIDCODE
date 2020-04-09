package com.smartism.znzk.domain;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smartism.znzk.R;
import com.smartism.znzk.domain.camera.CameraInfo;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZhujiInfo implements Serializable {
    private static final long serialVersionUID = 11;

    private String relationId;//关联关系ID
    private String type;//设备类型
    private String name;//主机名称
    private int logoResource;
    private String mac;//mac地址

    private boolean isChecked;//是否选中，临时变量

    private long id;//主机id
    private String where;//主机放置地点
    private String logo;//主机logo
    private String brandName;//主机品牌名称
    private boolean online;//主机是否在线
    private String masterid;//主机masterid
    private long cid;//厂商id
    private long gid;//组ID
    private boolean admin;//是否admin
    private String scene="";//场景
    private int uc;//用户数
    private int updateStatus; //更新状态
    private int powerStatus; //电源状态 0市电 1电池
    private int batteryStatus; //电池状态 0正常 1底电
    private int batterySurplus = -1; //电池剩余电量 百分比 -1不支持
    private int simStatus; //sim卡状态  0无卡 1有卡 2信号差
    private int wanType;//联网方式 0有线 1wifi 2gsm
    private int gsm; //类型0非GSM类型  1为GSM类型
    private String ca;//分类标示
    private String cak;//分类广义表示(行为分组)
    private String scenet; //智能场景类型
    private long bipc; //绑定摄像头id
    private CameraInfo cameraInfo = new CameraInfo();
    private String dt; //设备类型
    private String dtid; //
    private boolean la;//是否支持本地报警优先

    private String country;//所在国家
    private int usercount;//人数

    private int ac;//支持的报警中心类型 0不支持 1艾礼安
    private boolean ex;//是否是体验主机
    private boolean op;//是否是运营
    private long stime;//所在国家
    private long ipcid; //数据库id

    private String rolek ="";//用户角色

    public String getRolek() {
        return rolek;
    }

    public void setRolek(String rolek) {
        this.rolek = rolek;
    }

    public void setIpcid(long ipcid) {
        this.ipcid = ipcid;
    }

    public long getIpcid() {

        return ipcid;
    }
    //    private String c; //绑定摄像头id
//    private String id; //绑定摄像头id
//    private String n; //绑定摄像头id
//    private String p; //绑定摄像头id

    private int nr;//未读消息数
    private int devices;//设备数

    private long lastCommandTime;//子设备或者自己中最新的指令时间

    private List<CommandInfo> dCommands = new ArrayList<>(1);
    private Map<String, String> setInfos = new HashMap<>(1);

    public class CtrDeviceType{
        public static final String INSECTICIDE = "insecticide";
        public static final String AIRCARE = "aircare";

        public static final String INSECTICIDE_SINGLE_REFILL = "insecticide_single_refill";
        public static final String AIRCARE_DOUBLE_REFILL = "aircare_double_refill";
        public static final String AIRCARE_SINGLE_REFILL = "aircare_single_refill";

        public static final String INSECTICIDE_SINGLE_GROUP = "insecticide_single_group";
        public static final String AIRCARE_SINGLE_GROUP = "aircare_single_group";
    }

    public enum GNSetNameMenu{
        supportAddDeviceByType("gn1"),  //是否支持按类型添加
        supportAddDeviceByAny("gn2"),  //是否支持任意探头添加
        freeSmsCall("gn3"),  //短信电话套餐是否免费  1免费
        supportClearDevices("gn4"), //是否支持添加上来弹出清空从机操作 0不支持 空和非0支持
        showTypeInDeviceList("gn5"), //主机列表设备是否显示类型 空和0默认显示  1不显示
        supportAddDeviceByHistory("gn6"),//是否支持历史记录恢复 空和0默认支持  1不支持
        supportIpcLinkage("gn7"), //是否支持摄像头联动报警(开启需要摄像头支持) 默认和非1支持， 1不支持
        supportHomeEdit("gn8"), //是否支持在家可编辑 默认和非1不支持，1支持
        supportCable("gn9"), //是否支持APP添加有线防区 默认和非1不支持，1支持 - 有线防区入口从右上角菜单改到设备列表，参数控制删除菜单。
        batteryMargin("gn10"),//是否支持App本地设置报警时长，1表示支持
        supportSwitchGSM("gn11"),//是否支持启用GSM报警开关 默认和非1支持，1不支持
        supportGSMTypeWay("gn12"),//是否支持GSM号码单独设置sms/call 默认和非1不支持(也就是同时支持)，1支持
        supportWifiSet("gn13"),//是否支持APP设置主机WIFI名称和密码  默认和非1不支持，1支持
        supportLangSet("gn14"),//是否支持APP设置主机语言  默认和非1不支持，1支持
        supportTimeZoneSet("gn15"),//是否支持APP设置主机时区 默认和非1不支持，1支持
        supportRefreshInit("gn16"),//是否支持APP主动同步主机配件信息 默认和非1不支持，1支持
        cableCount("gn17"),//是否支持APP主动同步主机配件信息 默认和非1不支持，1支持
        delayArming("gn18"),//是否支持延时布防 默认和非1不支持，1支持
        supportLevel("gn19"),//是否支持有线防区触发模式设置 默认和非1不支持，1支持 - 高电平触发还是低电平触发
        supportDevicePwd("gn20"),//是否支持主机密码设置 默认和非1不支持，1支持
        alarmCenterReadOnly("gn21"),//报警中心账号是否只读 默认和非1不是，1是
        supportAddDevice("gn22"),//是否支持添加设备入口，优先级高于权限  默认和非1支持，1不支持
        supportDelDevice("gn23"),//是否支持删除设备入口，优先级高于权限  默认和非1支持，1不支持
        supportAlarmPhone("gn24"),//是否支持网络报警短信电话设置 默认和非1支持，1不支持
        delayAlarming("gn25");//延时报警 默认和非1不支持，1支持
        private String value;
        private GNSetNameMenu(String value) {
            this.value = value;
        }
        public String value() {
            return value;
        }
    }

    public String getCompanyPrefix(){
        return masterid.substring(0,4);
    }

    public int getDevices() {
        return devices;
    }

    public void setDevices(int devices) {
        this.devices = devices;
    }

    public int getNr() {
        return nr;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getUsercount() {
        return usercount;
    }

    public void setUsercount(int usercount) {
        this.usercount = usercount;
    }

    public boolean isEx() {
        return ex;
    }

    public void setEx(boolean ex) {
        this.ex = ex;
    }

    public boolean isOp() {
        return op;
    }

    public void setOp(boolean op) {
        this.op = op;
    }

    public long getStime() {
        return stime;
    }

    public void setStime(long stime) {
        this.stime = stime;
    }

    public String getDt() {
        return dt;
    }

    public void setDt(String dt) {
        this.dt = dt;
    }

    public String getDtid() {
        return dtid;
    }

    public void setDtid(String dtid) {
        this.dtid = dtid;
    }

    public long getBipc() {
        return bipc;
    }

    public void setBipc(long bipc) {
        this.bipc = bipc;
    }

    public CameraInfo getCameraInfo() {
        return cameraInfo;
    }

    public void setCameraInfo(CameraInfo cameraInfo) {
        this.cameraInfo = cameraInfo;
    }

    public int getStatusCall() {
        return statusCall;
    }

    public void setStatusCall(int statusCall) {
        this.statusCall = statusCall;
    }

    private int statusCall;//电话报警状态
    private int statusSMS;//短信报警状态


    public String getScenet() {
        return scenet;
    }

    public void setScenet(String scenet) {
        this.scenet = scenet;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getMasterid() {
        return masterid;
    }

    public void setMasterid(String masterid) {
        this.masterid = masterid;
    }

    public long getCid() {
        return cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getScene() {
        return scene;
    }

    public void setScene(String scene) {
        this.scene = scene;
    }

    public int getUc() {
        return uc;
    }

    public void setUc(int uc) {
        this.uc = uc;
    }

    public int getUpdateStatus() {
        return updateStatus;
    }

    public void setUpdateStatus(int status) {
        this.updateStatus = status;
    }

    public String getWhere() {
        return where;
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public int getGsm() {
        return gsm;
    }

    public void setGsm(int gsm) {
        this.gsm = gsm;
    }

    public int getPowerStatus() {
        return powerStatus;
    }

    public void setPowerStatus(int powerStatus) {
        this.powerStatus = powerStatus;
    }

    public int getBatteryStatus() {
        return batteryStatus;
    }

    public void setBatteryStatus(int batteryStatus) {
        this.batteryStatus = batteryStatus;
    }

    public int getSimStatus() {
        return simStatus;
    }

    public void setSimStatus(int simStatus) {
        this.simStatus = simStatus;
    }

    public int getWanType() {
        return wanType;
    }

    public void setWanType(int wanType) {
        this.wanType = wanType;
    }

    public String getCa() {
        return ca;
    }

    public void setCa(String ca) {
        this.ca = ca;
    }

    public String getCak() {
        return cak;
    }

    public void setCak(String cak) {
        this.cak = cak;
    }


    public int getStatusSMS() {
        return statusSMS;
    }

    public void setStatusSMS(int statusSMS) {
        this.statusSMS = statusSMS;
    }

    public int getAc() {
        return ac;
    }

    public void setAc(int ac) {
        this.ac = ac;
    }

    public boolean isLa() {
        return la;
    }

    public void setLa(boolean la) {
        this.la = la;
    }

    public long getLastCommandTime() {
        return lastCommandTime;
    }

    public void setLastCommandTime(long lastCommandTime) {
        this.lastCommandTime = lastCommandTime;
    }

    public String getBrandName() {
        return brandName;
    }

    public int getBatterySurplus() {
        return batterySurplus;
    }

    public void setBatterySurplus(int batterySurplus) {
        this.batterySurplus = batterySurplus;
    }

    public String getBrandNameText(){
        if (!StringUtils.isEmpty(brandName)){
            JSONArray array = JSONArray.parseArray(brandName);
            if (!array.isEmpty()){
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < array.size(); i++) {
                    JSONObject o = array.getJSONObject(i);
                    builder.append("<font color = '");
                    builder.append(o.getString("c"));
                    builder.append("'>");
                    builder.append(o.getString("t"));
                    builder.append("</font>");
                }
                return builder.toString();
            }
        }
        return "";
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public long getGid() {
        return gid;
    }

    public void setGid(long gid) {
        this.gid = gid;
    }

    public List<CommandInfo> getdCommands() {
        return dCommands;
    }

    public Map<String, String> getSetInfos() {
        return setInfos;
    }

    public void setSetInfos(Map<String, String> setInfos) {
        this.setInfos = setInfos;
    }

    public void setdCommands(List<CommandInfo> dCommands) {
        this.dCommands = dCommands;
    }

    public String getRelationId() {
        return relationId;
    }

    public void setRelationId(String relationId) {
        this.relationId = relationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        switch (type){
            case CtrDeviceType.INSECTICIDE_SINGLE_REFILL:
                logoResource = R.drawable.insecticide_single_refill;
                break;
            case CtrDeviceType.AIRCARE_DOUBLE_REFILL:
                logoResource = R.drawable.aircare_double_refill;
                break;
            case CtrDeviceType.AIRCARE_SINGLE_GROUP:
                logoResource = R.drawable.aircare_single_group;
                break;
            case CtrDeviceType.INSECTICIDE_SINGLE_GROUP:
                logoResource = R.drawable.insecticide_single_group;
                break;
            case CtrDeviceType.AIRCARE_SINGLE_REFILL:
            default:
                logoResource = R.drawable.aircare_single_refill;
                break;
        }
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getLogoResource() {
        return logoResource;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public String toString() {
        return "ZhujiInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", where='" + where + '\'' +
                ", logo='" + logo + '\'' +
                ", online=" + online +
                ", masterid='" + masterid + '\'' +
                ", cid=" + cid +
                ", admin=" + admin +
                ", scene='" + scene + '\'' +
                ", uc=" + uc +
                ", updateStatus=" + updateStatus +
                ", powerStatus=" + powerStatus +
                ", batteryStatus=" + batteryStatus +
                ", simStatus=" + simStatus +
                ", wanType=" + wanType +
                ", gsm=" + gsm +
                ", ca='" + ca + '\'' +
                ", cak='" + cak + '\'' +
                ", scenet='" + scenet + '\'' +
                ", bipc=" + bipc +
                ",ipcid="+ipcid+
                ", cameraInfo=" + cameraInfo +
                ", dt='" + dt + '\'' +
                ", dtid='" + dtid + '\'' +
                ", statusCall=" + statusCall +
                ", statusSMS=" + statusSMS +
                ", ac=" + ac +
                '}';
    }
}