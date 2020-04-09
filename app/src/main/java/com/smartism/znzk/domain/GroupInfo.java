package com.smartism.znzk.domain;

import java.io.Serializable;

public class GroupInfo implements Serializable{
	/**
	 *  设备分组
	 */
	private static final long serialVersionUID = 11;
	private long id;
	private long zj_id;
	private String name;
	private String logo;
	private String bipc; //群组绑定的摄像头 json格式
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getZj_id() {
		return zj_id;
	}
	public void setZj_id(long zj_id) {
		this.zj_id = zj_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getBipc() {
		return bipc;
	}
	public void setBipc(String bipc) {
		this.bipc = bipc;
	}
	
	
}