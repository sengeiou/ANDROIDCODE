package com.smartism.znzk.domain.yankan;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviceType implements Serializable {
	// /**
	// *
	// */
	// private static final long serialVersionUID = 1L;
	//
	// /**
	// * 设备id
	// */
	// @Expose
	// private int tid ;
	//
	// /**
	// * 设备型号中文名称
	// */
	// @Expose
	// @SerializedName("name_zh")
	// private String nameZh ;
	//
	// /**
	// * 设备型号英文名称
	// */
	// @Expose
	// @SerializedName("name_en")
	// private String nameEn ;
	//
	// public int getTid() {
	// return tid;
	// }
	//
	// public void setTid(int tid) {
	// this.tid = tid;
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
	// @Override
	// public String toString() {
	// return "DeviceType [tid=" + tid + ", nameZh=" + nameZh + ", nameEn="
	// + nameEn + "]";
	// }

	/**
	 * 设备类型
	 */
	@Expose
	private int t;

	public int getTid() {
		return t;
	}

	public void setTid(int tid) {
		this.t = t;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 设备型号中文名称
	 */
	@Expose
	@SerializedName("name")
	private String name;

	@Override
	public String toString() {
		return "DeviceType [tid=" + t + ", name=" + name + "]";
	}
}
