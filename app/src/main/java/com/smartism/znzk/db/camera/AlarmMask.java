package com.smartism.znzk.db.camera;

import java.io.Serializable;

public class AlarmMask implements Serializable {
	// id
	public int id;
	// 当前登录用户
	public String activeUser;
	// 报警屏蔽ID
	public String deviceId;
}
