package com.smartism.znzk.domain;

import java.io.Serializable;

public class AppUserInfo implements Serializable {
	private Long id;
	private String account;
	private String name;
	private String mobile;
	private String email;
	private String logo;
	private String role;
	private String code;
	private Long parent_id;  //父设备ID
	private String parent_account; //父设备账号
	private String parent_name;   //父设备名称
	/**
	 * 三种设备类型，双向类(首发都可以)，单向类(只能接受设备信息)，遥控类(只能控制设备，设备不上报信息)
	 * @author Administrator
	 *
	 */
	public enum typeEnum{
		Shang_Xia,Shang,Xia  //三种类型
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public Long getParent_id() {
		return parent_id;
	}
	public void setParent_id(Long parent_id) {
		this.parent_id = parent_id;
	}
	public String getParent_account() {
		return parent_account;
	}
	public void setParent_account(String parent_account) {
		this.parent_account = parent_account;
	}
	public String getParent_name() {
		return parent_name;
	}
	public void setParent_name(String parent_name) {
		this.parent_name = parent_name;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}