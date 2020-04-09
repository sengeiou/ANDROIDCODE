package com.smartism.znzk.domain;

import java.io.Serializable;

public class CommandInfo implements Serializable{
	private static final long serialVersionUID = 11;

	private long d_id;
	private String command;
	private long ctime;
	private String ctype;
	private long mId;
	private long gId;
	private int special;//特定指令字段



	public int getSpecial() {
		return special;
	}

	public void setSpecial(int special) {
		this.special = special;
	}

	public long getgId() {
		return gId;
	}

	public void setgId(long gId) {
		this.gId = gId;
	}

	public long getmId() {
		return mId;
	}

	public void setmId(long mId) {
		this.mId = mId;
	}

	public enum SpecialEnum{
		doorbell(3); //门铃
		private int value;
		private SpecialEnum(int value) {
			this.value = value;
		}
		public int value() {
			return value;
		}
	}

	public enum CommandTypeEnum{
		user(1),
		weight(2),
		temperature(3), //温度
		humidity(4),  //湿度
		pm25(5),
		bloodpressure(6),//血压/高
		heartrate(7),//心率
		//		weight(8), 经纬度预留
//		weight(9),
//		weight(10),
//		weight(11),
		methanol(12),
		co(13),  //一氧化碳
		lel(14),  //可燃气体
		formaldehyde(15), //甲醛
		simStatus(17), //SIM卡状态
		warnStatus(19), //联网方式
		powerStatus(20), //电源状态
		bloodsuggar(36), //血糖
		bloodpressureh(37),//血压/低
		battery(39),//电池电量
		liquidMargin(48),//驱蚊液的液量，液量
		commandsendtime(49),//
		addLockNumber(0x2E),//新增开锁编号
		delLockNumber(0x2F),//删除开锁编号
		reqLockNumberAuthorization(0x38),//请求开始编号授权
		authorizationLockNumber(0x39),//授权钥匙
		setWifiSSID(0x3A),//从机设备设置wifi SSID
		setWifiPassword(0x3B),//从机设备设置wifi password
		setPictureCommSlaveId(0x3C),//从机设备设置接收拍照的ID
		voltage(0x42),//从机设备的电压值
		setZhujiAlarmTime(0x6a),//主机报警时长
		requestAddUser(0x7E),//请求新增用户授权
		dSetJdqWorkTime(0x9A),//继电器工作时长设置 单位S 致利德拨号器
		dSetDpTypeChufa(0x9B),//触发方式 00：常闭，01，常开
		dSetBohaoType(0x9C),//拨号模式选择  00xx：拨打对应的报警防区的号码，01xx：轮拨所有号码  xx表示轮播次数
		dSetChufaSignal(0xA1),//0一个信号  1持续信号 ZLD
		dSetArmingDianPing(0xA2);//zld 00低电平布防  1高电平布防
		private int value;
		private CommandTypeEnum(int value) {
			this.value = value;
		}
		public String value() {
			return String.valueOf(value);
		}
	}

	public long getD_id() {
		return d_id;
	}

	public void setD_id(long d_id) {
		this.d_id = d_id;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public long getCtime() {
		return ctime;
	}

	public void setCtime(long ctime) {
		this.ctime = ctime;
	}

	public String getCtype() {
		return ctype;
	}

	public void setCtype(String ctype) {
		this.ctype = ctype;
	}

	@Override
	public String toString() {
		return "cType:"+ctype+",command:"+command;
	}
}