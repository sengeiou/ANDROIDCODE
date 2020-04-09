package com.smartism.znzk.domain.yankan;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;

public class BrandResult implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 总的条数
	 */
	@Expose
	private int sm;
	
	/**
	 * 所有品牌列表
	 */
	@Expose
	private List<Brand> rs ;

	public int getSm() {
		return sm;
	}

	public void setSm(int sm) {
		this.sm = sm;
	}

	public List<Brand> getRs() {
		return rs;
	}

	public void setRs(List<Brand> rs) {
		this.rs = rs;
	}

	@Override
	public String toString() {
		return "BrandResult [sm=" + sm + ", rs=" + rs + "]";
	}
	
}
