package com.smartism.znzk.db.camera;

import com.p2p.core.P2PHandler;
import com.p2p.core.P2PValue;
import com.smartism.znzk.global.AppConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.camera.WifiUtils;

import java.io.Serializable;
import java.net.InetAddress;

public class Contact implements Serializable, Comparable {
	// id
	public int id;
	// 联系人名称
	public String contactName="";
	// 联系人ID
	public String contactId;
	// 联系人监控密码 注意：不是登陆密码，只有当联系人类型为设备才有
	public String contactPassword="0";
	// 联系人类型
	public int contactType;
	// 此联系人发来多少条未读消息
	public int messageCount;
	// 当前登录的用户
	public String activeUser="";
	// 在线状态 不保存数据库
	public int onLineState = Constants.DeviceState.OFFLINE;
	// 布放状态不保存数据库
	public int defenceState = Constants.DefenceState.DEFENCE_STATE_LOADING;
	// 记录是否是点击获取布放状态 不保存数据库
	public boolean isClickGetDefenceState = false;
	// 联系人标记 不保存数据库
	public int contactFlag;
	// ip地址
	public InetAddress ipadressAddress;
    // 用户输入的密码
	public String userPassword="";
    //是否设备有更新
	public int Update=Constants.P2P_SET.DEVICE_UPDATE.UNKNOWN;
    //当前版本
	public String cur_version="";
	//可更新到的版本
	public String up_version="";
    //有木有rtsp标记
	public int rtspflag=0;
	public boolean isfactory;
	// 按在线状态排序
	public String wifiPassword="12345678";
    //AP模式下的wifi密码
	public int mode=P2PValue.DeviceMode.GERNERY_MODE;
	public int apModeState=Constants.APmodeState.UNLINK;
	public boolean isConnectApWifi=false;
	@Override
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		Contact o = (Contact) arg0;
		if (o.onLineState > this.onLineState) {
			return 1;
		} else if (o.onLineState < this.onLineState) {
			return -1;
		} else {
			return 0;
		}
	}
	/**
	 * 获取设备IP最后一段
	 * @return 空返回""
	 */
	public String getIpMark(){
		if(ipadressAddress!=null){
			String mark=ipadressAddress.getHostAddress();
			return mark.substring(mark.lastIndexOf(".")+1,mark.length());
		}
		return "";
	}
	// 为什么这要返回IP地址的最后一个 ？
	public String getContactId(){
		String ip=getIpMark();
		if(ip.equals("")){
			return contactId;
		}
		return ip;
	}

	public String getContactPassword() {
		return P2PHandler.getInstance().EntryPassword(contactPassword);
	}

	public void setContactPassword(String contactPassword) {
		this.contactPassword = contactPassword;
	}


	public void setApModeState(int state){
		this.apModeState=state;
	}
	
	public int getApModeState(){
		if(contactType==P2PValue.DeviceType.IPC&&mode==P2PValue.DeviceMode.AP_MODE){
			 String wifiName=AppConfig.Relese.APTAG+contactId;
			if(WifiUtils.getInstance().isConnectWifi(AppConfig.Relese.APTAG+contactId)){
				apModeState=Constants.APmodeState.LINK;
			}else{
				if(WifiUtils.getInstance().isScanExist(wifiName)){
					//处于AP模式
					apModeState=Constants.APmodeState.UNLINK;
				}else{
					return apModeState;
				}
			}
		}
			return apModeState;
	}
	
	public String getAPName(){
		return AppConfig.Relese.APTAG+contactId;
	}
	/**
	 * 获取显示名称
	 * @return 
	 */
	public String getContactName(){
		if(contactName!=null&&contactName.length()>0){
			return contactName;
		}else{
			return contactId;
		}
	}
	
	public boolean isIsfactory() {
		return isfactory;
	}
	public void setIsfactory(boolean isfactory) {
		this.isfactory = isfactory;
	}
	@Override
	public String toString() {
		return "Contact [id=" + id + ", contactName=" + contactName
				+ ", contactId=" + contactId + ", contactPassword="
				+ contactPassword + ", contactType=" + contactType
				+ ", messageCount=" + messageCount + ", activeUser="
				+ activeUser + ", onLineState=" + onLineState
				+ ", defenceState=" + defenceState
				+ ", isClickGetDefenceState=" + isClickGetDefenceState
				+ ", contactFlag=" + contactFlag + ", ipadressAddress="
				+ ipadressAddress + ", userPassword=" + userPassword
				+ ", Update=" + Update + ", cur_version=" + cur_version
				+ ", up_version=" + up_version + ", rtspflag=" + rtspflag
				+ ", wifiPassword=" + wifiPassword + ", mode=" + mode
				+ ", apModeState=" + apModeState + ", isConnectApWifi="
				+ isConnectApWifi + "]";
	}
	
	
}
