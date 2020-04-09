package com.smartism.znzk.domain.camera;

import com.p2p.core.P2PHandler;

import java.io.Serializable;

public class CameraBindInfo implements Serializable{
	private long ipcid;
	private String c;
	private String id;
	private String n;
	private String p;//存放的是原始密码值，但是操作时需要用到转换后的密码，在get方法中转换
    private boolean initSuccess;//是否初始化成功
	private boolean checked;//是否是选择状态
	private boolean support433Alarm = true;//是否支持433报警
	private boolean progressing = true;//是否在执行中
	public String getContactPassword(){
		return P2PHandler.getInstance().EntryPassword(p);
	}
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
		return p!=null?P2PHandler.getInstance().EntryPassword(p):p;
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

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public boolean isSupport433Alarm() {
		return support433Alarm;
	}

	public void setSupport433Alarm(boolean support433Alarm) {
		this.support433Alarm = support433Alarm;
	}

	public boolean isProgressing() {
		return progressing;
	}

	public void setProgressing(boolean progressing) {
		this.progressing = progressing;
	}

    public boolean isInitSuccess() {
        return initSuccess;
    }

    public void setInitSuccess(boolean initSuccess) {
        this.initSuccess = initSuccess;
    }

    @Override
	public String toString() {
		return "CameraInfo [c=" + c + ", id=" + id + ", n=" + n + ", p=" + p
				+ "]";
	}
	
	
}
