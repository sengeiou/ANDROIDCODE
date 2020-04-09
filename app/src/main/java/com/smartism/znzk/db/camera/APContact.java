package com.smartism.znzk.db.camera;

import java.io.Serializable;

import com.smartism.znzk.global.AppConfig;



public class APContact implements Serializable {
	// id
	public int id;
	// AP热点名称
	public String APname;
	// 别名
	public String nickName;
	// ap密码
	public String Pwd;
	public String activeUser;
	public String contactId;

	public APContact() {
		super();
	}

	public APContact(String aPname, String nickName, String pwd,
			String activeUser) {
		super();
		APname = aPname;
		this.nickName = nickName;
		Pwd = pwd;
		this.activeUser = activeUser;
	}

	public APContact(String contactId, String aPname, String nickName,
			String pwd, String activeUser) {
		super();
		this.contactId = contactId;
		APname = aPname;
		this.nickName = nickName;
		Pwd = pwd;
		this.activeUser = activeUser;
	}
	
	public String getApName(){
		return AppConfig.Relese.APTAG+contactId;
	}

	@Override
	public String toString() {
		return "APContact [APname=" + APname + ", nickName=" + nickName
				+ ", Pwd=" + Pwd + ", activeUser=" + activeUser + "]";
	}

}
