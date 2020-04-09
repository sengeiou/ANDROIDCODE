package com.smartism.znzk.db.camera;

public class SysMessage implements Comparable {
	// id
	public int id;
	// 消息内容(中文)
	public String msg;
	// 消息内容(英文)
	public String msg_en;
	// 接收时间
	public String msg_time;
	// 当前登录用户
	public String activeUser;
	// 消息类型
	public int msgType;
	// 消息状态
	public int msgState;

	// 状态：未读
	public static final int MESSAGE_STATE_NO_READ = 0;
	// 状态已读
	public static final int MESSAGE_STATE_READED = 1;
	// 类型：管理员发送
	public static final int MESSAGE_TYPE_ADMIN = 2;

	// 排序
	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		Message msg = (Message) o;
		if (msg.id > this.id) {
			return 1;
		} else if (msg.id < this.id) {
			return -1;
		} else {
			return 0;
		}
	}
}
