package com.smartism.znzk.domain;

import java.io.Serializable;

public class ZhujiGroupInfo implements Serializable{
	/**
	 *  主机分组
	 */
	private static final long serialVersionUID = 11;
	private long id;
	private String name;
	private String logo;
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
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}

}