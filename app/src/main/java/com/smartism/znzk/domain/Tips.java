package com.smartism.znzk.domain;

import java.io.Serializable;

public class Tips implements Serializable {

	private String c;//操作指令
	private String e;//指令名称
	private boolean flag = false;

	public Tips() {
	}

	public Tips(String c, String e, boolean flag) {
		this.c = c;
		this.e = e;
		this.flag = flag;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public String getC() {
		return c;
	}
	public void setC(String c) {
		this.c = c;
	}
	public String getE() {
		return e;
	}
	public void setE(String e) {
		this.e = e;
	}
	@Override
	public String toString() {
		return "Tips [c=" + c + ", e=" + e + "]";
	}
	

}
