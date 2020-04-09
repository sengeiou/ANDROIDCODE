package com.smartism.znzk.db.camera;

public class AlarmRecord {
	// id
	public int id;
	// 报警设备ID
	public String deviceId;
	// 报警类型
	public int alarmType;
	// 报警时间
	public String alarmTime;
	// 当前登录用户
	public String activeUser;
	// 防区
	public int group;
	// 通道
	public int item;
	//图片存放路径
	public String alarmPictruePath="";
	
}
