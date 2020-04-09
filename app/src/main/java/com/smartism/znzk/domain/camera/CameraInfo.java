package com.smartism.znzk.domain.camera;

import com.p2p.core.P2PHandler;
import com.smartism.znzk.util.StringUtils;

import java.io.Serializable;

public class CameraInfo implements Serializable{
	private long ipcid;
	private String c;
	private String id;
	private String n;
	private String p;//存放的是原始密码值，但是操作时需要用到转换后的密码，在get方法中转换

	public String getZjName() {
		return zjName;
	}

	public void setZjName(String zjName) {
		this.zjName = zjName;
	}

	private String zjName ; //保存摄像头作为主机时的名字
	public String getC() {
		return c;
	}
	public void setC(String c) {
		this.c = c;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getN() {
		return n;
	}
	public void setN(String n) {
		this.n = n;
	}
	public String getP() {
		return !StringUtils.isEmpty(p)?P2PHandler.getInstance().EntryPassword(p):p;
	}
	public String getOriginalP() {
		return p;
	}
	public void setP(String p) {
		this.p = p;
	}

	public long getIpcid() {
		return ipcid;
	}

	public void setIpcid(long ipcid) {
		this.ipcid = ipcid;
	}

	public enum CEnum {
		xiongmai("xiongmai"),
		jiwei("jiwei"),
		hoshoo("hoshoo");


		private String value;

		private CEnum(String value) {
			this.value = value;
		}

		public String value() {
			return value;
		}
	}
	@Override
	public String toString() {
		return "CameraInfo [c=" + c + ", id=" + id + ", n=" + n + ", p=" + p
				+ ",ipcid="+ipcid+"]";
	}
	
	
}
