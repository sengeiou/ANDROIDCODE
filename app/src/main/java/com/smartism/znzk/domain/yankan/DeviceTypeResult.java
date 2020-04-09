package com.smartism.znzk.domain.yankan;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;

public class DeviceTypeResult implements Serializable{

	private static final long serialVersionUID = 1L;

	/**
	 * 返回条数
	 */
	@Expose
	private int sm ;
	
	/**
	 * 返回的集合数目
	 */
	@Expose
	private List<DeviceType> rs ;
	
	public int getSm() {
		return sm;
	}

	public void setSm(int sm) {
		this.sm = sm;
	}

	public List<DeviceType> getRs() {
		return rs;
	}

	public void setRs(List<DeviceType> rs) {
		this.rs = rs;
	}
	
	@Override
	public String toString() {
		return "DeviceResult [sm=" + sm + ", rs=" + rs + "]";
	}
	
}
