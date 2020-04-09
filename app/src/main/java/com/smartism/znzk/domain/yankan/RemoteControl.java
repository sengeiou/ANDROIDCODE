package com.smartism.znzk.domain.yankan;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RemoteControl implements Serializable {

	// /**
	// *
	// */
	// private static final long serialVersionUID = 1L;
	//
	// /**
	// * 遥控器ID
	// */
	// @Expose
	// private String rid;
	//
	// /**
	// * 遥控设备厂牌中文件名称
	// */
	// @Expose
	// @SerializedName("name_zh")
	// private String nameZh;
	//
	// /**
	// * 遥控设备厂牌英文件名称
	// */
	// @Expose
	// @SerializedName("name_en")
	// private String nameEn;
	//
	// /**
	// * 被遥控设备类型
	// */
	// @Expose
	// @SerializedName("be_rc_type")
	// private int beRCtype;
	//
	// /**
	// * 被遥控设备型号
	// */
	// @Expose
	// @SerializedName("be_rmodel")
	// private String beRmodel;
	//
	// /**
	// * 遥控器型号
	// */
	// @Expose
	// private String rmodel;
	//
	// /**
	// * 自定义设备描述
	// */
	// @Expose
	// private String rdesc;
	//
	// /**
	// * 排序
	// */
	// @Expose
	// @SerializedName("order_no")
	// private String orderNo;
	//
	// /**
	// * 遥控器命令
	// */
	// @Expose
	// @SerializedName("rc_command")
	// private HashMap<String,String> rcCommand;
	//
	// public String getRid() {
	// return rid;
	// }
	//
	// public void setRid(String rid) {
	// this.rid = rid;
	// }
	//
	// public String getNameZh() {
	// return nameZh;
	// }
	//
	// public void setNameZh(String nameZh) {
	// this.nameZh = nameZh;
	// }
	//
	// public String getNameEn() {
	// return nameEn;
	// }
	//
	// public void setNameEn(String nameEn) {
	// this.nameEn = nameEn;
	// }
	//
	// public int getBeRCtype() {
	// return beRCtype;
	// }
	//
	// public void setBeRCtype(int beRCtype) {
	// this.beRCtype = beRCtype;
	// }
	//
	// public String getBeRmodel() {
	// return beRmodel;
	// }
	//
	// public void setBeRmodel(String beRmodel) {
	// this.beRmodel = beRmodel;
	// }
	//
	// public String getRmodel() {
	// return rmodel;
	// }
	//
	// public void setRmodel(String rmodel) {
	// this.rmodel = rmodel;
	// }
	//
	// public String getRdesc() {
	// return rdesc;
	// }
	//
	// public void setRdesc(String rdesc) {
	// this.rdesc = rdesc;
	// }
	//
	// public String getOrderNo() {
	// return orderNo;
	// }
	//
	// public void setOrderNo(String orderNo) {
	// this.orderNo = orderNo;
	// }
	//
	// public HashMap<String, String> getRcCommand() {
	// return rcCommand;
	// }
	//
	// public void setRcCommand(HashMap<String, String> rcCommand) {
	// this.rcCommand = rcCommand;
	// }
	//
	// @Override
	// public String toString() {
	// return "RemoteControl [rid=" + rid + ", nameZh=" + nameZh + ", nameEn="
	// + nameEn + ", beRCtype=" + beRCtype + ", beRmodel=" + beRmodel
	// + ", rmodel=" + rmodel + ", rdesc=" + rdesc + ", orderNo="
	// + orderNo + ", rcCommand=" + rcCommand + "]";
	// }
	// ===============================================================
	// private static final long serialVersionUID = 1L;
	// // 遥控器ID
	// @Expose
	// private String rid;
	// // 遥控设备厂牌中文件名称
	// @Expose
	// @SerializedName("name")
	// private String name;
	// // 设备类型
	// @Expose
	// private int t;
	// // 被遥控设备类型
	// @Expose
	// @SerializedName("be_rc_type")
	// private int beRCtype;
	// // 被遥控设备型号
	// @Expose
	// @SerializedName("be_rmodel")
	// private String beRmodel;
	// // 遥控器型号
	// @Expose
	// private String rmodel;
	// // 自定义设备描述
	// @Expose
	// private String rdesc;
	// // 排序
	// @Expose
	// @SerializedName("order_no")
	// private String orderNo;
	// // 遥控器命令
	// @Expose
	// @SerializedName("rc_command")
	// private HashMap<String, String> rcCommand;
	//
	// public String getRmodel() {
	// return rmodel;
	// }
	//
	// public int getT() {
	// return t;
	// }
	//
	// public void setT(int t) {
	// this.t = t;
	// }
	//
	// public String getRid() {
	// return rid;
	// }
	//
	// public void setRid(String rid) {
	// this.rid = rid;
	// }
	//
	// public String getName() {
	// return name;
	// }
	//
	// public void setName(String name) {
	// this.name = name;
	// }
	//
	// public int getBeRCtype() {
	// return beRCtype;
	// }
	//
	// public void setBeRCtype(int beRCtype) {
	// this.beRCtype = beRCtype;
	// }
	//
	// public String getBeRmodel() {
	// return beRmodel;
	// }
	//
	// public void setBeRmodel(String beRmodel) {
	// this.beRmodel = beRmodel;
	// }
	//
	// public String getRdesc() {
	// return rdesc;
	// }
	//
	// public void setRdesc(String rdesc) {
	// this.rdesc = rdesc;
	// }
	//
	// public String getOrderNo() {
	// return orderNo;
	// }
	//
	// public void setOrderNo(String orderNo) {
	// this.orderNo = orderNo;
	// }
	//
	// public HashMap<String, String> getRcCommand() {
	// return rcCommand;
	// }
	//
	// public void setRcCommand(HashMap<String, String> rcCommand) {
	// this.rcCommand = rcCommand;
	// }
	//
	// public static long getSerialversionuid() {
	// return serialVersionUID;
	// }
	//
	// public void setRmodel(String rmodel) {
	// this.rmodel = rmodel;
	// }
	//
	// @Override
	// public String toString() {
	// return "RemoteControl [rid=" + rid + ", name=" + name + ", t=" + t + ",
	// beRCtype=" + beRCtype + ", beRmodel="
	// + beRmodel + ", rmodel=" + rmodel + ", rdesc=" + rdesc + ", orderNo=" +
	// orderNo + ", rcCommand="
	// + rcCommand + "]";
	// }
	// ===============================================================

	private static final long serialVersionUID = 1L;
	// 遥控器ID
	@Expose
	private String rid;
	// 遥控设备厂牌中文件名称
	@Expose
	private String name;
	// 设备类型
	@Expose
	private int t;
	// 被遥控型号
	@Expose
	private String be_rmodel;

	// 遥控器型号
	@Expose
	private String rmodel;

	// 自定义设备描述
	@Expose
	private String rdesc;
	// 排序号
	@Expose
	private int order_no;

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getT() {
		return t;
	}

	public void setT(int t) {
		this.t = t;
	}

	public String getBe_rmodel() {
		return be_rmodel;
	}

	public void setBe_rmodel(String be_rmodel) {
		this.be_rmodel = be_rmodel;
	}

	public String getRmodel() {
		return rmodel;
	}

	public void setRmodel(String rmodel) {
		this.rmodel = rmodel;
	}

	public String getRdesc() {
		return rdesc;
	}

	public void setRdesc(String rdesc) {
		this.rdesc = rdesc;
	}

	public int getOrder_no() {
		return order_no;
	}

	public void setOrder_no(int order_no) {
		this.order_no = order_no;
	}

	public HashMap<String, String> getRcCommand() {
		return null;
	}

//	public void setRcCommand(HashMap<String, String> rcCommand) {
//		this.rcCommand = rcCommand;
//	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	// 遥控器命令
	// @Expose
	//private HashMap<String, String> rcCommand;
	@Expose
	ArrayList<key> rc_command; // 遥控器命令，采用json格式返回

	class key{
		String kn;	 //国际化键显示名
		String src;	 //原始码
	}
}
