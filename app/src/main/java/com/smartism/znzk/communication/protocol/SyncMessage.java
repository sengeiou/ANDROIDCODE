package com.smartism.znzk.communication.protocol;

public class SyncMessage {
	private int totalLength = 0;
	private int command = 0;
	private int code = 0;
	private long deviceid = 0;
	private byte[] syncBytes = new byte[] {};

	public enum CommandMenu {
		//app发往server端指令开始
		rq_keepalive(-1), //心跳包
		rp_keepalive(-8001),//心跳包返回
		rq_login(100), // 连接建立，登录
		rp_login(8100), // 登录返回 
		rq_control(101), // 控制指令
		rp_control(8101), //控制指令返回
		rq_pdByHand(102), // 手动配对
		rp_pdByHand(8102),  //手动配对返回
		rq_pdByHand_onlyControl(103), // 手动配对仅控制类型确认
		rp_pdByHand_onlyControl(8103), // 手动配对仅控制类型确认返回
		rq_pdByAuto(104), // 自动配对 退出由deviceid控制1 进入 0退出
		rp_pdByAuto(8104), // 自动配对返回
		rq_checkpudate(105), // 主机固件更新检查
		rp_checkpudate(8105), // 主机固件更新检查回应
		rq_pudate(106), // 主机固件更新
		rp_pudate(8106), // 主机固件更新回应
		rq_refresh(107), // 刷新列表
		rp_refresh(8107), // 刷新列表回应
		rq_szhuji(108), // 搜索主机
		rp_szhuji(8108), // 搜索主机回应
		rq_pfactory(109), // 通知主机进入或者退出工厂模式
		rp_pfactory(8109), // 通知回应
		rq_pdByHandE(110), // 退出手动配对
		rp_pdByHandE(8110), // 退出配对回应
		rq_pStudy(111), // 通知主机进入学习模式
		rp_pStudy(8111), // 通知回应
		rq_pStudyE(112), // 通知主机退出学习模式
		rp_pStudyE(8112), // 通知退出回应
		rq_developertc(113), //进入开发者消息透传
		rp_developertc(8113), //开发者页面数据回应
		rq_controlRemind(114), // 控制提醒模式
		rp_controlRemind(8114), //控制提醒模式返回
		rq_controlConfirm(115), // 控制确认
		rp_controlConfirm(8115), //控制确认返回
		//end
		//server端发往app端指令开始
		ac_newMessage(100), //正常状态上报
		su_newMessage(9100), //正常状态上报回应
		ac_zhujiOnlineChange(101),  //主机在线状态变更
		ac_changeIP(102),  //主机和app不在同一个服务器,更换IP
		ac_refresh(103),   //刷新列表指令
		ac_kickoff(104),    //被踢下线
		ac_factory(105),    //学习模式反馈
		ac_tipchange(106),    //探头消息提醒状态变更
		ac_batterychange(107),    //探头电量变更
		ac_alarm(108),    //手机触发panic报警
		su_alarm(9108); //手机触发panic报警 回应
		//end
		private int value;

		private CommandMenu(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}
	}

	public enum CodeMenu {
		// rq 前缀为请求的code request,  rp 前缀为返回请求的code response
		zero(0),
		rp_login_sessionout(1), //登录失效
		rp_login_outofday(2), //登录已过期
		rp_login_errorappinfo(3), //appid 和 appsecret错误
		rp_refresh_nocomdata(10),//有数据数据未压缩
		rp_refresh_nodata(1),
		rp_szhuji_nozhuji(1),
		rp_szhuji_needauthorization(2),
		rp_pdByHand_success(1),
		rp_pdByHand_nozhuji(2),
		rp_pdByHand_notype(3),
		rp_pdByHand_zhujioffline(4),
		rp_pdByHand_needjihuo(5),
		rp_pdByHand_servererror(6),
		rp_pdByHand_drepeat(7),
		rp_pdByHand_dinotherhub(8),
		rp_pdByHand_onlyControl_timeout(1),
		rp_pdByHand_onlyControl_sunottozj(2),
		rq_pdByAuto_into(1),
		rq_pdByAuto_quit(0),
		rp_pdByAuto_success(1),//收到此包也是结束同rp_pdByHand_success
		rp_pdByAuto_nozhuji(2),
		rp_pdByAuto_notype(3),
		rp_pdByAuto_zhujioffline(4),
		rp_pdByAuto_addone(5),
		rp_control_devicenotexist(-2),
		rp_control_commandnotexist(-3),
		rp_control_deviceoffline(-4),
		rp_control_timeout(-5),
		rp_control_needconfirm(-6),
		rp_control_verifyerror(-7),
		rp_checkpudate_nonew(0),
		rp_checkpudate_havenew(1),
		rp_checkpudate_doffline(2),
		rp_checkpudate_verror(3),//固件版本错误
		rp_pupdate_into(0),
		rp_pupdate_success(1),
		rp_pupdate_failed(2),
		rp_pupdate_progress(3),
		rp_pfactory_exit(0),
		rp_pfactory_into(1),
		rp_pfactory_exit_activation(2),
		rp_pfactory_into_activation(3),
		rq_pfactory_exit(0),
		rq_pfactory_into(1),
		rq_pfactory_exit_activation(2),
		rq_pfactory_into_activation(3),
		rp_pfactory_into_type_have(11),
		rp_pfactory_into_type_nothave(12),
		rq_pStudy_into(0),
		rq_pStudyE(0),
		rq_pStudyE_finish(1),
		rp_pStudy_into(0),
		rp_pStudy_command(1),
		rp_pStudyE(0),
		rp_pStudyE_finish(1),
		rq_developertc_a(1),
		rq_developertc_n(0),
		rq_developertc_c(2),
		rp_developertc_a(1),
		rp_developertc_n(0),
		rp_developertc_c(2),
		ac_factory_1_1_1(111),
		ac_factory_1_2_1(121),
		ac_factory_1_3_1(131),
		ac_factory_1_1_0(110),
		ac_factory_1_2_0(120),
		ac_factory_1_3_0(130),
		ac_factory_2_1_1(211),
		ac_factory_2_2_1(221),
		ac_factory_2_3_1(231),
		ac_factory_2_1_0(210),
		ac_factory_2_2_0(220),
		ac_factory_2_3_0(230);
		private int value;

		private CodeMenu(int value) {
			this.value = value;
		}

		public int value() {
			return value;
		}
	}

	public SyncMessage() {
		totalLength = 0;
		command = 0;
		deviceid = 0;
		syncBytes = new byte[] {};
	}
	
	public SyncMessage(CommandMenu command) {
		totalLength = 0;
		this.command = command.value();
		deviceid = 0;
		syncBytes = new byte[] {};
	}

	public int getTotalLength() {
		return totalLength;
	}

	public void setTotalLength(int totalLength) {
		this.totalLength = totalLength;
	}

	public int getCommand() {
		return command;
	}

	public void setCommand(int appid) {
		this.command = appid;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public byte[] getSyncBytes() {
		return syncBytes;
	}

	public void setSyncBytes(byte[] syncBytes) {
		this.syncBytes = syncBytes;
	}

	public long getDeviceid() {
		return deviceid;
	}

	public void setDeviceid(long deviceid) {
		this.deviceid = deviceid;
	}
	
}