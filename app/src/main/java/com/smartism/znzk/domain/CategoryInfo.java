package com.smartism.znzk.domain;

import java.io.Serializable;

public class CategoryInfo implements Serializable{
	private static final long serialVersionUID = 1L;
	private long id;
	private long p_id;
	private int netType;
	private String name;
	private String ioc;
	private String ckey;
	private String chakey;
	private String remark;

	public enum NetTypeEnum{
		wifiOrLan(0), //自行上网 wifi  lan  nb 等
		rf(1),//RF上网
		zigbee(2),//zibgee
		cable(3);//有线 指有线防区
		private int value;
		private NetTypeEnum(int value) {
			this.value = value;
		}
		public int value() {
			return value;
		}
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getP_id() {
		return p_id;
	}
	public void setP_id(long p_id) {
		this.p_id = p_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIoc() {
		return ioc;
	}
	public void setIoc(String ioc) {
		this.ioc = ioc;
	}
	public String getCkey() {
		return ckey;
	}
	public void setCkey(String ckey) {
		this.ckey = ckey;
	}
	public String getChakey() {
		return chakey;
	}
	public void setChakey(String chakey) {
		this.chakey = chakey;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}

	public int getNetType() {
		return netType;
	}

	public void setNetType(int netType) {
		this.netType = netType;
	}
}
