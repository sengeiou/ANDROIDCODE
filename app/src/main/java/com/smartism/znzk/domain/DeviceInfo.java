package com.smartism.znzk.domain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceInfo implements Serializable{
	private static final long serialVersionUID = 11;
	private long id;
	private long zj_id;
	private String relationId;
	private String mac;
	private String tip;//设备触发的指令
	private int typed;//触发场景类型
	private String  tipName;//指令名称
	private String name;
	private String logo;
	private long lastUpdateTime;
	private String lastCommand;
	private int lastCommandSpecial;//最后指令的特殊含义
	private int acceptMessage; //接收服务器信息提醒等级
	private String type; //设备类型
	private long typeid; //设备类型id
	private String where; //设备放置地点
	private String controlType;   //设备控制类型
	private String appdownload; //app下载位置
	private String apppackage; //app启动包+首个activity，格式为"packageName/activity"
	private String chValue; //温湿度计的ch值
	private int sort;  //排序字段
	private int status; //状态
	private int nr; //未读消息数
	private int nt; //联网类型1 有线 0 rf
	private String ca=""; //分类key
	private String cak; //分类广义标识key
	private int dr;  //触发的键值
	private boolean lowb; //是否低电
	private boolean fa; //是否是永久防区设备
	private boolean flag;//在首页 此属性表示是否是管理员
	private String ipc;
	private String bipc; //非摄像头设备绑定的摄像头
	private int gsm;//主机数据
	private String dtype;//数据类型，用于devicesListActivity展示用
	private long mid;//体重数据、血压数据等的成员ID
	private long vid;//体重数据等数据的数据库ID
	private int wIndex = -1; //在集合中的位置
	private String slaveId;
	private String masterId;
	private String eids;
	public List<Tips> tipsList = new ArrayList<>();
	private String mc;//设备制造厂家标识 用于区分进入不同的 同类型设备页面

	private List<CommandInfo> dCommands;

	private int adsUt;
	private String adsUrl;

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public List<Tips> getTipsList() {
		return tipsList;
	}

	public void setTipsList(List<Tips> tipsList) {
		this.tipsList = tipsList;
	}

	public String getEids() {
		return eids;
	}

	public void setEids(String eids) {
		this.eids = eids;
	}

	public String getSlaveId() {
		return slaveId;
	}

	public void setSlaveId(String slaveId) {
		this.slaveId = slaveId;
	}

	//首页主机特殊用途字段
	private int powerStatus;//电源状态(主机)

	public int getPowerStatus() {
		return powerStatus;
	}

	public void setPowerStatus(int powerStatus) {
		this.powerStatus = powerStatus;
	}

    public int getLastCommandSpecial() {
        return lastCommandSpecial;
    }

    public void setLastCommandSpecial(int lastCommandSpecial) {
        this.lastCommandSpecial = lastCommandSpecial;
    }

	public enum CakMenu {
		security("security"), //安防类
		detection("detection"),//数据检测、采集类
		control("control"),//控制类型
		surveillance("surveillance"),//监控类
		zhuji("zhuji"),//主机
		group("group"),//分组
		health("health");//健康采集类
		private String value;
		private CakMenu(String value) {
			this.value = value;
		}
		public String value() {
			return value;
		}
	}

	/**
	 * 详细的分类信息 - 不是全部 详见接口文档 - 请用到的童鞋继续补充
	 * @author 王建
	 *
	 */
	public enum CaMenu {
		bohaoqi("bohaoqi"),
		jdq("jdq"),//继电器
		nbyg("nbyg"),//nb烟感
		wifizns("wifizns"),//wifi设备 智能锁
		zhuji("zhuji"),//主机
		zhujijzm("jzmzj"),//主机-卷闸门
		menling("ml"),//门铃
		menci("mc"), //门磁
		hongwai("hw"),//红外
		yangan("yg"),//烟感
		wenshiduji("wsd"),//温湿度
		wenduji("wd"),//温度计
		cazuo("cz"),//遥控插座
		rangqibaojing("rq"),//燃气报警器
		zhendongtance("zd"),//震动探测器
		jinji("jj"),//紧急呼叫器
		zhujiControl("zjykq"),//主机遥控器
		zhujifmq("zhuji_fmq"),//主机内置蜂鸣器
		hongwaizhuanfaqi("hwzf"),//红外转发
		laba("lb"),//喇叭
		ipcamera("sst"),//摄像头
		xueyaji("yyj"),//血压计
		xuetangyi("xty"),//血糖仪
		tizhongceng("tzc"),//体重秤
		quwenqi("qwq"),//驱蚊器
		zhinengsuo("zns"),//智能锁
		maoyan("my"),//猫眼
		znyx("znyx"), //智能药箱
		kgmb("kgmb"),//开关面板，触摸面板
		djkzq("djkzq"),
		ybq("ybq"),
		yxj("yxj"),
		ldtsq("ldtsq"),
		rqzj("rqzj"),
		wifirqbjq("wifirqbjq"),
		nbrqbjq("nbrqbjq"),
		fangqu("fq"),
		wifiymq("wifiymq");
		private String value;
		private CaMenu(String value) {
			this.value = value;
		}
		public String value() {
			return value;
		}
	}

	/**
	 * 类型生产厂家枚举 用于区分同类型设备 不同定制化
	 */
	public enum TypeCompanyMenu{
		zhihuimao("zhihuimao"),//智慧猫的基础版本锁
		xhouse("xhouse"),//巨将锁
		efud("efud"),//中山锁
		zhicheng("zhicheng"), //志城锁
		zhichengwifi("zhichengwifi"), //志城锁 WIFI
		zhouligong("zhouligong"), //周立功 双向
		hongtai("hongtai"), //宏泰 双向
		jieaolihua("jieaolihua"); //捷奥利华 公寓
		private String value;
		private TypeCompanyMenu(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	}

	public enum RoleKey {
		admin("lock_num_admin"),//管理员权限
		partner("lock_num_partner"),//爱人权限 不能授权和注册钥匙
		old("lock_num_old"),//老人权限 可以看到开锁记录，可以接收开锁推送
		baby("lock_num_baby"),//小孩权限 小孩不能接收拍照，不能接收正常开锁，只能接收非法开锁
		guest("lock_num_guest"),//客人权限 同小孩
		temp("lock_num_temp");//临时权限
		private String value;

		public String value() {
			return value;
		}

		RoleKey(String value) {
			this.value = value;
		}

	}


	public enum ControlTypeMenu{
		shangxing("shangxing"),
		shangxing_1("shangxing_1"),
		shangxing_2("shangxing_2"),
		shangxing_3("shangxing_3"),
		shangxing_4("shangxing_4"),
		shangxing_5("shangxing_5"),
		xiaxing("xiaxing"),
		xiaxing_1("xiaxing_1"),
		xiaxing_2("xiaxing_2"),
		xiaxing_3("xiaxing_3"),
		xiaxing_4("xiaxing_4"),
		xiaxing_5("xiaxing_5"),
		xiaxing_6("xiaxing_6"),
		xiaxing_7("xiaxing_7"),
		xiaxing_8("xiaxing_8"),
		xiaxing_9("xiaxing_9"),
		xiaxing_10("xiaxing_10"),
		xiaxing_11("xiaxing_11"),
		xiaxing_12("xiaxing_12"),
		xiaxing_13("xiaxing_13"),
		xiaxing_14("xiaxing_14"),
		xiaxing_15("xiaxing_15"),
		xiaxing_16("xiaxing_16"),
		fangdiu_1("fangdiu_1"),
		wenshiduji("wenshiduji"),
		wenduji("wenduji"),
		tizhongcheng("tizhongcheng"),
		zhuji("zhuji"),
		group("group"),
		adsinfo("adsinfo"),
		neiqian("neiqian"),
		ipcamera("shexiangtou"),
		qita("qita");
		private String value;
		private ControlTypeMenu(String value) {
			this.value = value;
		}
		public String value() {
			return value;
		}
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getLastUpdateTime() {
		return lastUpdateTime;
	}
	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	public String getLastCommand() {
		return lastCommand;
	}
	public void setLastCommand(String lastCommand) {
		this.lastCommand = lastCommand;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getAcceptMessage() {
		return acceptMessage;
	}
	public void setAcceptMessage(int acceptMessage) {
		this.acceptMessage = acceptMessage;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getControlType() {
		return controlType;
	}
	
	
	public boolean isFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	public void setControlType(String controlType) {
		this.controlType = controlType;
	}
	public int getSort() {
		return sort;
	}
	public void setSort(int sort) {
		this.sort = sort;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getWhere() {
		return where;
	}
	public void setWhere(String where) {
		this.where = where;
	}
	public String getAppdownload() {
		return appdownload;
	}
	public void setAppdownload(String appdownload) {
		this.appdownload = appdownload;
	}
	public String getApppackage() {
		return apppackage;
	}
	public void setApppackage(String apppackage) {
		this.apppackage = apppackage;
	}
	public String getChValue() {
		return chValue;
	}
	public void setChValue(String chValue) {
		this.chValue = chValue;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public long getTypeid() {
		return typeid;
	}
	public void setTypeid(long typeid) {
		this.typeid = typeid;
	}
	public int getNr() {
		return nr;
	}
	public void setNr(int nr) {
		this.nr = nr;
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
	public int getDr() {
		return dr;
	}
	public void setDr(int dr) {
		this.dr = dr;
	}
	public boolean isLowb() {
		return lowb;
	}
	public void setLowb(boolean lowb) {
		this.lowb = lowb;
	}
	public int getwIndex() {
		return wIndex;
	}
	public void setwIndex(int wIndex) {
		this.wIndex = wIndex;
	}
	
	public String getTip() {
		return tip;
	}
	public void setTip(String tip) {
		this.tip = tip;
	}
	
	public String getTipName() {
		return tipName;
	}
	public void setTipName(String tipName) {
		this.tipName = tipName;
	}
	
	public int getTyped() {
		return typed;
	}
	public void setTyped(int typed) {
		this.typed = typed;
	}
	//重写equals方法，set的contains方法会用此方法对比
	@Override
	public boolean equals(Object o) {
		return this.id == ((DeviceInfo)o).id;
	}
	@Override
	public int hashCode() {
		return 111111; //配合equals方法使用
	}
	public DeviceInfo deepClone() {
		DeviceInfo info = null;
		    try {
		  ByteArrayOutputStream baos = new ByteArrayOutputStream();
		  ObjectOutputStream oos = new ObjectOutputStream(baos);
		  oos.writeObject(this);
		  oos.close();
		 
		  ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		  ObjectInputStream bis = new ObjectInputStream(bais);
		  info = (DeviceInfo)bis.readObject();
		 } catch (IOException e) {
		  e.printStackTrace();
		 } catch (ClassNotFoundException e) {
		  e.printStackTrace();
		 }
		    return info;
		}
	public long getZj_id() {
		return zj_id;
	}
	public void setZj_id(long zj_id) {
		this.zj_id = zj_id;
	}
	public int getGsm() {
		return gsm;
	}
	public void setGsm(int gsm) {
		this.gsm = gsm;
	}
	public String getBipc() {
		return bipc;
	}
	public void setBipc(String bipc) {
		this.bipc = bipc;
	}

	public String getDtype() {
		return dtype;
	}

	public void setDtype(String dtype) {
		this.dtype = dtype;
	}

	public long getMid() {
		return mid;
	}

	public void setMid(long mid) {
		this.mid = mid;
	}

	public long getVid() {
		return vid;
	}

	public void setVid(long vid) {
		this.vid = vid;
	}
	public String getIpc() {
		return ipc;
	}
	public void setIpc(String ipc) {
		this.ipc = ipc;
	}

	public boolean isFa() {
		return fa;
	}

	public void setFa(boolean fa) {
		this.fa = fa;
	}

	public String getMasterId() {
		return masterId;
	}

	public void setMasterId(String masterId) {
		this.masterId = masterId;
	}

	public int getAdsUt() {
		return adsUt;
	}

	public void setAdsUt(int adsUt) {
		this.adsUt = adsUt;
	}

	public String getAdsUrl() {
		return adsUrl;
	}

	public void setAdsUrl(String adsUrl) {
		this.adsUrl = adsUrl;
	}

	public String getMc() {
		return mc;
	}

	public void setMc(String mc) {
		this.mc = mc;
	}

	public List<CommandInfo> getdCommands() {
		return dCommands;
	}

	public void setdCommands(List<CommandInfo> dCommands) {
		this.dCommands = dCommands;
	}

	public int getNt() {
		return nt;
	}

	public void setNt(int nt) {
		this.nt = nt;
	}

	public String getRelationId() {
		return relationId;
	}

	public void setRelationId(String relationId) {
		this.relationId = relationId;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	@Override
	public String toString() {
		return "DeviceInfo{" +
				"id=" + id +
				", zj_id=" + zj_id +
				", tip='" + tip + '\'' +
				", typed=" + typed +
				", tipName='" + tipName + '\'' +
				", name='" + name + '\'' +
				", logo='" + logo + '\'' +
				", lastUpdateTime=" + lastUpdateTime +
				", lastCommand='" + lastCommand + '\'' +
				", lastCommandSpecial=" + lastCommandSpecial +
				", acceptMessage=" + acceptMessage +
				", type='" + type + '\'' +
				", typeid=" + typeid +
				", where='" + where + '\'' +
				", controlType='" + controlType + '\'' +
				", appdownload='" + appdownload + '\'' +
				", apppackage='" + apppackage + '\'' +
				", chValue='" + chValue + '\'' +
				", sort=" + sort +
				", status=" + status +
				", nr=" + nr +
				", nt=" + nt +
				", ca='" + ca + '\'' +
				", cak='" + cak + '\'' +
				", dr=" + dr +
				", lowb=" + lowb +
				", fa=" + fa +
				", flag=" + flag +
				", ipc='" + ipc + '\'' +
				", bipc='" + bipc + '\'' +
				", gsm=" + gsm +
				", dtype='" + dtype + '\'' +
				", mid=" + mid +
				", vid=" + vid +
				", wIndex=" + wIndex +
				", slaveId='" + slaveId + '\'' +
				", masterId='" + masterId + '\'' +
				", eids='" + eids + '\'' +
				", tipsList=" + tipsList +
				", mc='" + mc + '\'' +
				", dCommands=" + dCommands +
				", adsUt=" + adsUt +
				", adsUrl='" + adsUrl + '\'' +
				", powerStatus=" + powerStatus +
				'}';
	}
}