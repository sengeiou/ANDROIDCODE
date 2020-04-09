package com.smartism.znzk.domain.yankan;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Brand implements Serializable {

	// /**
	// *
	// */
	// private static final long serialVersionUID = 1L;
	//
	// /**
	// * 被遥控设备品牌中文名称
	// */
	// @Expose
	// @SerializedName("name_zh")
	// private String nameZh ;
	//
	// /**
	// * 被遥控设备品牌英文名称
	// */
	// @Expose
	// @SerializedName("name_en")
	// private String nameEn ;
	//
	// /**
	// * 首字母(中文)
	// */
	// @Expose
	// private String fc;
	//
	// /**
	// * 使用频率
	// */
	// @Expose
	// private int ht;
	//
	// /**
	// * 常用品牌标识
	// */
	// @Expose
	// private int common;
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
	// public String getFc() {
	// return fc;
	// }
	//
	// public void setFc(String fc) {
	// this.fc = fc;
	// }
	//
	// public int getHt() {
	// return ht;
	// }
	//
	// public void setHt(int ht) {
	// this.ht = ht;
	// }
	//
	// public int getCommon() {
	// return common;
	// }
	//
	// public void setCommon(int common) {
	// this.common = common;
	// }
	//
	// @Override
	// public String toString() {
	// return "Brand [nameZh=" + nameZh + ", nameEn=" + nameEn + ", fc=" + fc
	// + ", ht=" + ht + ", common=" + common + "]";
	// }

	private static final long serialVersionUID = 1L;
	// 被遥控设备品牌中文名称
	@Expose
	@SerializedName("name")
	private String name;

	@Expose
	private int bid;
	// 常用品牌标识
	@Expose
	private int common;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getBid() {
		return bid;
	}

	public void setBid(int bid) {
		this.bid = bid;
	}

	public int getCommon() {
		return common;
	}

	public void setCommon(int common) {
		this.common = common;
	}

	@Override
	public String toString() {
		return "Brand [name=" + name + ", common=" + common + "]";
	}
}
