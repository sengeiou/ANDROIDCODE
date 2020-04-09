package com.smartism.znzk.domain;

import java.io.Serializable;

public class SceneInfo implements Serializable {
	private long id;
	private String name="";//防止空指针异常
	private int type;
	private String icon;
	private int status;
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	@Override
	public String toString() {
		return "SceneInfo [id=" + id + ", name=" + name + ", type=" + type
				+ ", icon=" + icon + ", status=" + status + "]";
	}
	
	
}
