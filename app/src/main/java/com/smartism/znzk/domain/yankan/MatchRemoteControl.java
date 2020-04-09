package com.smartism.znzk.domain.yankan;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MatchRemoteControl implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// 遥控器ID
	@Expose
	public String rid;

	// 遥控设备厂牌名称
	@Expose
	public String name;

	// 遥控设备类型
	@Expose
	@SerializedName("t")
	public int tId;

	// 被遥控设备型号
	@Expose
	@SerializedName("be_rmodel")
	public String beRmodel;

	// 遥控器型号
	@Expose
	public String rmodel;

	// 自定义设备描述
	@Expose
	public String rdesc;

	// 排序
	@Expose
	@SerializedName("order_no")
	public String orderNo;

	// 是否压缩
	@Expose
	public int zip;

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}

	public String getBeRmodel() {
		return beRmodel;
	}

	public void setBeRmodel(String beRmodel) {
		this.beRmodel = beRmodel;
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

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int gettId() {
		return tId;
	}

	public void settId(int tId) {
		this.tId = tId;
	}

	public int getZip() {
		return zip;
	}

	public void setZip(int zip) {
		this.zip = zip;
	}

	// ==================
	// 遥控器命令相关数据

	@Expose
	@SerializedName("rc_command")
	public Object rcCommand;

	public Object getRcCommand() {
		return rcCommand;
	}

	public void setRcCommand(Object rcCommand) {
		this.rcCommand = rcCommand;
	}
	
	
//	@Expose
//	@SerializedName("rc_command")
//	public CMD rcCommand;
//
//	public Object getRcCommand() {
//		return rcCommand;
//	}
//
//	public void setRcCommand(Object rcCommand) {
//		this.rcCommand = (CMD) rcCommand;
//	}
//	
//	
//	class CMD{
//		@Expose
//		public OBJ on;
//	}
//	
//	class OBJ{
//		public String getSrc() {
//			return src;
//		}
//		public void setSrc(String src) {
//			this.src = src;
//		}
//		@Expose
//		public String kn;
//		@Expose
//		public String src;
//		@Expose
//		public String shortCode;
//		@Expose
//		public String order;
//	}
}
