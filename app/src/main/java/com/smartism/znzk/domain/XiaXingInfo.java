package com.smartism.znzk.domain;

import java.io.Serializable;

public class XiaXingInfo implements Serializable {

	private String s;
	private String n;
	private String i;
	private String w;
	private long id;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getS() {
		return s;
	}
	public void setS(String s) {
		this.s = s;
	}
	public String getN() {
		return n;
	}
	public void setN(String n) {
		this.n = n;
	}
	public String getI() {
		return i;
	}
	public void setI(String i) {
		this.i = i;
	}
	public String getW() {
		return w;
	}
	public void setW(String w) {
		this.w = w;
	}
	@Override
	public String toString() {
		return "XiaXingInfo [s=" + s + ", n=" + n + ", i=" + i + ", w=" + w
				+ "]";
	}
	
}
