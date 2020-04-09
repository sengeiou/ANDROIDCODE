package com.smartism.znzk.domain.camera;

import java.io.Serializable;
import java.net.InetAddress;

import com.smartism.znzk.global.Constants;



public class LocalDevice implements Serializable {
	public String contactId;
	public String name;
	public int flag;
	public int type;
	public InetAddress address;
	public int rtspFrag ;
	public int defenceState = Constants.DefenceState.DEFENCE_STATE_LOADING;
	public int apModeState=Constants.APmodeState.UNLINK;
	public boolean isClickGetDefenceState = false;
	

	public int getRtspFrag() {
		return rtspFrag;
	}

	public void setRtspFrag(int rtspFrag) {
		this.rtspFrag = rtspFrag;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		LocalDevice obj = (LocalDevice) o;
		if (obj.contactId.equals(this.contactId)) {
			return true;
		} else {
			return false;
		}
	}

}
