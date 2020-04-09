package com.smartism.znzk.domain.yankan;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;

public class MatchRemoteControlResult implements Serializable {

	
	//返回条数
	 
	@Expose
	private int sm;

	
	//返回命令集
	 
	@Expose
	private List<MatchRemoteControl> rs;

	public int getSm() {
		return sm;
	}

	public void setSm(int sm) {
		this.sm = sm;
	}

	public List<MatchRemoteControl> getRs() {
		return rs;
	}

	public void setRs(List<MatchRemoteControl> rs) {
		this.rs = rs;
	}

	@Override
	public String toString() {
		return "RemoteControlResult [sm=" + sm + ", rs=" + rs + "]";
	}
}
