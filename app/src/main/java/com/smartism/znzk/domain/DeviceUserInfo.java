package com.smartism.znzk.domain;

import java.io.Serializable;

public class DeviceUserInfo implements Serializable {
		private String name;
		private boolean online;
		private long id;
		private int admin;
		private boolean flag ;
	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public int getAdmin() {
		return admin;
	}

	public void setAdmin(int admin) {
		this.admin = admin;
	}

	private String logo;

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public boolean getOnline() {
			return online;
		}
		public void setOnline(boolean online) {
			this.online = online;
		}
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		@Override
		public String toString() {
			return "DeviceUserInfo [name=" + name + ", online=" + online
					+ ", id=" + id + "]";
		}

}
