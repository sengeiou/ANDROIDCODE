package com.smartism.znzk.db.camera;

public class Message implements Comparable {
	// id
	public int id;
	// 发送者ID
	public String fromId;
	// 接收者ID
	public String toId;
	// 消息内容
	public String msg;
	// 发送或接收时间
	public String msgTime;
	// 当前登录用户
	public String activeUser;
	// 消息状态
	public String msgState;
	// 消息临时标记
	public String msgFlag;

	// 排序
	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		Message msg = (Message) o;
		if (msg.id > this.id) {
			return -1;
		} else if (msg.id < this.id) {
			return 1;
		} else {
			return 0;
		}
	}
}
