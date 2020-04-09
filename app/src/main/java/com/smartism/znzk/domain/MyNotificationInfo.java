package com.smartism.znzk.domain;

import android.content.Context;
import android.graphics.Bitmap;

import com.smartism.znzk.util.DataCenterSharedPreferences.Constant;

public class MyNotificationInfo{
	private int id = Constant.NOTIFICATIONID; //通知的ID
	private Context context;
	private String contentTitle; //通知标题
	private String contentText;  //通知内容
	private String ticker; //向上翻滚的消息
	private int icon = 0;     //消息在未展开时的图标
	private Bitmap bigIcon; //消息在展开时的图标
	private boolean silence = false; //是否静默
	private boolean vibrator = true;//是否震动
	private int nr = 0;    //未读消息总数
	private boolean isOngo = false; //是否进行中的通知
	private long device_id=0;
	private int special = -1 ; //智能药箱提醒吃药时间到了

	public int getSpecial() {
		return special;
	}

	public void setSpecial(int special) {
		this.special = special;
	}

	public long getDevice_id() {
		return device_id;
	}

	public void setDevice_id(long device_id) {
		this.device_id = device_id;
	}

	public MyNotificationInfo() {
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Context getContext() {
		return context;
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public String getContentTitle() {
		return contentTitle;
	}
	public void setContentTitle(String contentTitle) {
		this.contentTitle = contentTitle;
	}
	public String getContentText() {
		return contentText;
	}
	public void setContentText(String contentText) {
		this.contentText = contentText;
	}
	public String getTicker() {
		return ticker;
	}
	public void setTicker(String ticker) {
		this.ticker = ticker;
	}
	public int getIcon() {
		return icon;
	}
	public void setIcon(int icon) {
		this.icon = icon;
	}
	public Bitmap getBigIcon() {
		return bigIcon;
	}
	public void setBigIcon(Bitmap bigIcon) {
		this.bigIcon = bigIcon;
	}
	public boolean isSilence() {
		return silence;
	}
	public void setSilence(boolean silence) {
		this.silence = silence;
	}
	public int getNr() {
		return nr;
	}
	public void setNr(int nr) {
		this.nr = nr;
	}
	public boolean isOngo() {
		return isOngo;
	}
	public void setOngo(boolean isOngo) {
		this.isOngo = isOngo;
	}

	public boolean isVibrator() {
		return vibrator;
	}

	public void setVibrator(boolean vibrator) {
		this.vibrator = vibrator;
	}

	@Override
	public String toString() {
		return "MyNotificationInfo{" +
				"id=" + id +
				", context=" + context +
				", contentTitle='" + contentTitle + '\'' +
				", contentText='" + contentText + '\'' +
				", ticker='" + ticker + '\'' +
				", icon=" + icon +
				", bigIcon=" + bigIcon +
				", silence=" + silence +
				", vibrator=" + vibrator +
				", nr=" + nr +
				", isOngo=" + isOngo +
				'}';
	}
}
