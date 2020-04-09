package com.smartism.znzk.db.camera;

import java.io.Serializable;
import java.sql.Timestamp;

public class NearlyTell implements Serializable, Comparable {
	// id
	public int id;
	// 通话者ID
	public String tellId;
	// 通话类型
	public int tellType;
	// 通话状态
	public int tellState;
	// 通话时间
	public String tellTime;
	// 当前登录用户
	public String activeUser;
	// 显示计数，不存数据库
	public int count;

	// 未接听
	public static final int TELL_STATE_CALL_IN_REJECT = 0;
	// 接听成功
	public static final int TELL_STATE_CALL_IN_ACCEPT = 1;
	// 未拨通或被挂断
	public static final int TELL_STATE_CALL_OUT_REJECT = 2;
	// 拨通成功
	public static final int TELL_STATE_CALL_OUT_ACCEPT = 3;

	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		NearlyTell nearlyTell = (NearlyTell) o;
		Timestamp user1Time = new Timestamp(Long.parseLong(this.tellTime));
		Timestamp user2Time = new Timestamp(Long.parseLong(nearlyTell.tellTime));
		if (user1Time.after(user2Time)) {
			return -1;
		} else if (!user1Time.after(user2Time)) {
			return 1;
		} else {
			return 0;
		}
	}
}
