package com.smartism.znzk.camera;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.p2p.core.P2PValue;
import com.smartism.znzk.activity.camera.AlarmPictrueActivity;
import com.smartism.znzk.activity.camera.DoorBellNewActivity;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.DatabaseOperator;
import com.smartism.znzk.db.camera.AlarmMask;
import com.smartism.znzk.db.camera.AlarmRecord;
import com.smartism.znzk.db.camera.Contact;
import com.smartism.znzk.db.camera.DataManager;
import com.smartism.znzk.db.camera.SharedPreferencesManager;
import com.smartism.znzk.domain.DeviceInfo;
import com.smartism.znzk.domain.ZhujiInfo;
import com.smartism.znzk.domain.camera.CameraInfo;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.NpcCommon;
import com.smartism.znzk.util.camera.MusicManger;
import com.smartism.znzk.util.camera.T;

import java.util.ArrayList;
import java.util.List;

public class P2PConnect {
	/**
	 * 请务必使用application
	 * @param context
	 */
	public P2PConnect(Context context) {
		this.mContext = context;
	}

	static String TAG = "P2PConnect";
	public static final int P2P_STATE_NONE = 0;
	public static final int P2P_STATE_CALLING = 1;
	public static final int P2P_STATE_READY = 2;
	public static final int P2P_STATE_ALARM = 4;
	private static int current_state = P2P_STATE_NONE;
	private static String current_call_id = "0";
	private static int currentDeviceType;
	private static boolean isAlarming = false;
	private static Contact playingContact = null; //正在播放的摄像头是哪个，同一个摄像头的移动侦测是需要过滤的
	private static boolean isAlarm = false;
	private static int mode = P2PValue.VideoMode.VIDEO_MODE_SD;
	private static int number = 1;
	private static Context mContext;
	private static boolean isPlayBack;
	public static boolean isDoorbell = false;
	private static String monitorId = "";
	private static String doorbellId = "";

	public static String getDoorbellId() {
		return doorbellId;
	}

	public static void setDoorbellId(String doorbellId) {
		P2PConnect.doorbellId = doorbellId;
	}

	public static void setDoorbell(boolean doorbell) {
		isDoorbell = doorbell;
	}

	public static void setMonitorId(String monitordeviceid) {
		Log.e("setMonitorId", monitordeviceid);
		monitorId = monitordeviceid;
	}

	public static int getCurrent_state() {
		return current_state;
	}

	public static void setCurrent_state(int current_state) {
		P2PConnect.current_state = current_state;
		switch (current_state) {
		case P2P_STATE_NONE:
			Log.e(TAG, "P2P_STATE_NONE");
			break;
		case P2P_STATE_CALLING:
			Log.e(TAG, "P2P_STATE_CALLING");
			break;
		case P2P_STATE_READY:
			Log.e(TAG, "P2P_STATE_READY");
			break;
		}
	}

	public static int getMode() {
		return mode;
	}

	public static void setMode(int mode) {
		P2PConnect.mode = mode;
	}

	public static int getNumber() {
		return number;
	}

	public static void setNumber(int number) {
		P2PConnect.number = number;
	}

	public static String getCurrent_call_id() {

		return current_call_id;
	}

	public static void setCurrent_call_id(String current_call_id) {
		P2PConnect.current_call_id = current_call_id;
	}

	public static void setCurrentDeviceType(int type) {
		P2PConnect.currentDeviceType = type;
	}

	public static int getCurrentDeviceType() {
		return currentDeviceType;
	}

    public static Contact getPlayingContact() {
        return playingContact;
    }

    public static void setPlayingContact(Contact playingContact) {
        P2PConnect.playingContact = playingContact;
    }

    public static void setAlarm(boolean isAlarm) {
		P2PConnect.isAlarm = isAlarm;
	}

	public static boolean isPlayBack() {
		return isPlayBack;
	}

	public static void setPlayBack(boolean isPlayBack) {
		P2PConnect.isPlayBack = isPlayBack;
	}

	public static synchronized void vCalling(boolean isOutCall, int type) {
		// TODO Auto-generated method stub
		Log.e(TAG, "vCalling:" + current_call_id);
		P2PConnect.setCurrentDeviceType(type);
		if (!isOutCall && current_state == P2P_STATE_NONE) {
			P2PConnect.setCurrent_state(P2P_STATE_CALLING);

			/*
			 * Intent call = new Intent();
			 * call.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			 * call.setClass(mContext, CallActivity.class);
			 * call.putExtra("callId", current_call_id); call.putExtra("type",
			 * Constants.P2P_TYPE.P2P_TYPE_CALL); mContext.startActivity(call);
			 */
		}

		switch (current_state) {
			case P2P_STATE_NONE:
				Log.e(TAG, "P2P_STATE_NONE");
				break;
			case P2P_STATE_CALLING:
				Log.e(TAG, "P2P_STATE_CALLING");
				break;
			case P2P_STATE_READY:
				Log.e(TAG, "P2P_STATE_READY");
				break;
		}
	}

	public static synchronized void vReject(String msg) {
		vReject(9, msg);
	}

	/**
	 * 挂断
	 *
	 * @param resoncode 挂断代号
	 * @param msg
	 *            挂断代号对应字符提示
	 */
	public static synchronized void vReject(int resoncode, String msg) {
		// TODO Auto-generated method stub
		Log.e(TAG, "vReject:" + msg);
		if (!msg.equals("")) {
			T.showShort(mContext, msg);
		}
		try {
			P2PConnect.setCurrent_state(P2P_STATE_NONE);

			P2PConnect.setMode(P2PValue.VideoMode.VIDEO_MODE_SD);
			P2PConnect.setNumber(1);

			MusicManger.getInstance().stop();
			MusicManger.getInstance().stopVibrate();

			Intent refreshContans = new Intent();
			refreshContans
					.setAction(Constants.Action.ACTION_REFRESH_NEARLY_TELL);
			MainApplication.app.sendBroadcast(refreshContans);

			Intent reject = new Intent();
			reject.setAction(Constants.P2P.P2P_REJECT);
			reject.putExtra("error", msg);
			reject.putExtra("code", resoncode);
			mContext.sendBroadcast(reject);
		} catch (Exception e) {
			Log.e(TAG, "vReject:error");
		}
		Log.e(TAG, "vReject:end");
	}

	public static synchronized void vAccept(int type, int state) {
		// TODO Auto-generated method stub
		Log.e(TAG, "vAccept");
		MusicManger.getInstance().stop();
		MusicManger.getInstance().stopVibrate();

		Intent accept = new Intent();
		accept.setAction(Constants.P2P.P2P_ACCEPT);
		accept.putExtra("type", new int[] { type, state });
		mContext.sendBroadcast(accept);
	}

	public static synchronized void vConnectReady() {
		// TODO Auto-generated method stub
		Log.e(TAG, "vConnectReady");
		if (current_state != P2P_STATE_READY) {
			P2PConnect.setCurrent_state(P2P_STATE_READY);
			Intent ready = new Intent();
			ready.setAction(Constants.P2P.P2P_READY);
			mContext.sendBroadcast(ready);
		}
	}

	public static synchronized void vAllarming(int id, int type,
			boolean isSupport, int group, int item, boolean isSupportDelete) {
		// TODO Auto-generated method stub
		 Log.e("myvAllarming...", "vAllarming:" + isAlarming + " " + id + " " + type);
		// if (type == P2PValue.AlarmType.RECORD_FAILED_ALARM) {
		// return;
		// }
		// AlarmRecord alarmRecord = new AlarmRecord();
		// alarmRecord.alarmTime = String.valueOf(System.currentTimeMillis());
		// alarmRecord.deviceId = String.valueOf(id);
		// alarmRecord.alarmType = type;
		// alarmRecord.activeUser = NpcCommon.mThreeNum;
		// if ((type == P2PValue.AlarmType.EXTERNAL_ALARM || type ==
		// P2PValue.AlarmType.LOW_VOL_ALARM)
		// && isSupport) {
		// alarmRecord.group = group;
		// alarmRecord.item = item;
		// } else {
		// alarmRecord.group = -1;
		// alarmRecord.item = -1;
		// }
		// DataManager.insertAlarmRecord(mContext, alarmRecord);
		// Intent i = new Intent();
		// i.setAction(Constants.Action.REFRESH_ALARM_RECORD);
		// MainApplication.app.sendBroadcast(i);
		// if (null == NpcCommon.mThreeNum || "".equals(NpcCommon.mThreeNum)) {
		// return;
		// }
		// // 屏蔽的设备不接受报警推送
		// // List<AlarmMask> list =
		// DataManager.findAlarmMaskByActiveUser(mContext,
		// // NpcCommon.mThreeNum);
		// // for (AlarmMask alarmMask : list) {
		// // if (id == Integer.parseInt(alarmMask.deviceId)) {
		// // return;
		// // }
		// // }
		// if (current_state == P2P_STATE_CALLING
		// && Integer.parseInt(current_call_id) == id) {
		// return;
		// }
		// if (current_state == P2P_STATE_READY
		// && Integer.parseInt(current_call_id) == id) {
		// return;
		// }
		// if (type != P2PValue.AlarmType.DEFENCE
		// && type != P2PValue.AlarmType.NO_DEFENCE) {
		// long time = SharedPreferencesManager.getInstance()
		// .getIgnoreAlarmTime(mContext);
		// int time_interval = SharedPreferencesManager.getInstance()
		// .getAlarmTimeInterval(mContext);
		// if ((System.currentTimeMillis() - time) < (1000 * time_interval)) {
		// return;
		// }
		// }
		// if (!isPlaying) {
		// if (type == P2PValue.AlarmType.ALARM_TYPE_DOORBELL_PUSH) {
		// Intent it = new Intent();
		// it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// it.setClass(P2PConnect.mContext, DoorBellNewActivity.class);
		// it.putExtra("contactId", String.valueOf(id));
		// MainApplication.app.startActivity(it);
		// return;
		// }
		// // if (isAlarm == true) {
		// // Intent it = new Intent();
		// // it.setAction(Constants.Action.CHANGE_ALARM_MESSAGE);
		// // it.putExtra("alarm_id", id);
		// // it.putExtra("alarm_type", type);
		// // it.putExtra("isSupport", isSupport);
		// // it.putExtra("group", group);
		// // it.putExtra("item", item);
		// // it.putExtra("isSupportDelete", isSupportDelete);
		// // MainApplication.app.sendBroadcast(it);
		// // } else {
		// // isAlarm = true;
		// // Intent alarm = new Intent();
		// // alarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// // alarm.setClass(mContext, AlarmActivity.class);
		// // alarm.putExtra("alarm_id", id);
		// // alarm.putExtra("alarm_type", type);
		// // alarm.putExtra("isSupport", isSupport);
		// // alarm.putExtra("group", group);
		// // alarm.putExtra("item", item);
		// // alarm.putExtra("isSupportDelete", isSupportDelete);
		// // MainApplication.app.startActivity(alarm);
		// // }
		// if(isDoorbell==true&&doorbellId.equals(String.valueOf(id))&&type==P2PValue.AlarmType.MOTION_DECT_ALARM){
		// return;
		// }
		// Intent alarm = new Intent();
		// alarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// alarm.setClass(mContext, AlarmActivity.class);
		// alarm.putExtra("alarm_id", id);
		// alarm.putExtra("alarm_type", type);
		// alarm.putExtra("isSupport", isSupport);
		// alarm.putExtra("group", group);
		// alarm.putExtra("item", item);
		// alarm.putExtra("isSupportDelete", isSupportDelete);
		// MainApplication.app.startActivity(alarm);
		// } else {// 正在监控
		// if (!monitorId.equals(id)) {
		// // 监控页面弹窗
		// Intent k = new Intent();
		// k.setAction(Constants.Action.MONITOR_NEWDEVICEALARMING);
		// k.putExtra("messagetype", 1);
		// k.putExtra("alarm_id", String.valueOf(id));
		// k.putExtra("alarm_type", type);
		// k.putExtra("isSupport", isSupport);
		// k.putExtra("group", group);
		// k.putExtra("item", item);
		// k.putExtra("isSupportDelete", isSupportDelete);
		// MainApplication.app.sendBroadcast(k);
		// }
		// }

	}

	public static synchronized void vEndAllarm() {
		// TODO Auto-generated method stub
		isAlarming = false;
	}

	public static synchronized void vAllarmingWithPath(String id, int type,
			int option, int group, int item, int counts, String times,
			String picture, String video) {
		Log.e("vAllarmingWithPath", ".....vAllarmingWithPath....");
		if (type == P2PValue.AlarmType.RECORD_FAILED_ALARM) {
			return;
		}
		AlarmRecord alarmRecord = new AlarmRecord();
		alarmRecord.alarmTime = String.valueOf(System.currentTimeMillis());
		alarmRecord.deviceId = String.valueOf(id);
		alarmRecord.alarmType = type;
		alarmRecord.activeUser = NpcCommon.mThreeNum;
		if ((type == P2PValue.AlarmType.EXTERNAL_ALARM || type == P2PValue.AlarmType.LOW_VOL_ALARM)) {
			alarmRecord.group = group;
			alarmRecord.item = item;
		} else {
			alarmRecord.group = -1;
			alarmRecord.item = -1;
		}
		DataManager.insertAlarmRecord(mContext, alarmRecord);
		Intent i = new Intent();
		i.setAction(Constants.Action.REFRESH_ALARM_RECORD);
		MainApplication.app.sendBroadcast(i);
		if (null == NpcCommon.mThreeNum || "".equals(NpcCommon.mThreeNum)) {
			return;
		}

		List<AlarmMask> list = DataManager.findAlarmMaskByActiveUser(mContext,
				NpcCommon.mThreeNum);
		for (AlarmMask alarmMask : list) {
			if (id.equals(alarmMask.deviceId)) {
				return;
			}
		}
		if (current_state == P2P_STATE_CALLING
				&& id.equals(current_call_id)) {
			return;
		}
		if (current_state == P2P_STATE_READY
				&& id.equals(current_call_id)) {
			return;
		}
		if (type != P2PValue.AlarmType.DEFENCE
				&& type != P2PValue.AlarmType.NO_DEFENCE) {
			long time = SharedPreferencesManager.getInstance()
					.getIgnoreAlarmTime(mContext);
			int time_interval = SharedPreferencesManager.getInstance()
					.getAlarmTimeInterval(mContext);
			if ((System.currentTimeMillis() - time) < (1000 * time_interval)) {
				return;
			}
		}
		Log.e("报警", type + ".........");
		if (type == P2PValue.AlarmType.RECORD_FAILED_ALARM) {
			return;
		}
		if (playingContact==null) {
			if (type == P2PValue.AlarmType.ALARM_TYPE_DOORBELL_PUSH) {
				if (!isDoorbell) {
					Intent it = new Intent();
					it.setClass(P2PConnect.mContext, DoorBellNewActivity.class);
					it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
					it.putExtra("contactId", String.valueOf(id));
					mContext.startActivity(it);
				}
				return;
			}
		}
			if (isDoorbell == true && doorbellId.equals(String.valueOf(id))
					&& type == P2PValue.AlarmType.MOTION_DECT_ALARM) {
				return;
			}
			/*final Contact c = DataManager.findContactByActiveUserAndContactId(
					mContext, NpcCommon.mThreeNum, alarmRecord.deviceId);*/
			/*Log.e("报警", alarmRecord.deviceId + ":" + c.contactId + ":"
					+ c.contactPassword);*/
		if (playingContact==null && isHaveCamera(alarmRecord)) {
			Intent alarm = new Intent();
			alarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			alarm.setClass(mContext, AlarmPictrueActivity.class);
			alarm.putExtra("alarm_id", Integer.parseInt(id));
			alarm.putExtra("alarm_type", type);
			alarm.putExtra("deviceid", alarmRecord.deviceId);
			alarm.putExtra("group", group);
			alarm.putExtra("item", item);
			alarm.putExtra("imageCounts", counts);
			alarm.putExtra("picture", picture);
			alarm.putExtra("alarmTime", alarmRecord.alarmTime);
			String runningActivity = "";
			MainApplication.app.startActivity(alarm);

			try {
				ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
				runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();

			} catch (Exception e) {

			}
			Log.e("报警", id + ".........");
			if (runningActivity != null && !runningActivity.contains("AlarmPictrueActivity")) {

			}
		}
	}
	//判断本地是否有摄像头
	private static boolean isHaveCamera(AlarmRecord alarmRecord) {
		DeviceInfo device = null;
		List<ZhujiInfo> zhujiInfos = DatabaseOperator.getInstance(mContext.getApplicationContext()).queryAllZhuJiInfos();

		List<CameraInfo> camera = new ArrayList<>();
		if (zhujiInfos != null) {
			for (ZhujiInfo info : zhujiInfos) {

				if(DeviceInfo.CakMenu.surveillance.value().equals(info.getCak())){ //主机就是摄像头
					return true;
				}
				//获取主机下的设备，找到监控设备
				List<DeviceInfo> dInfos = DatabaseOperator.getInstance(mContext).queryAllDeviceInfos(info.getId());
				if (dInfos != null && !dInfos.isEmpty()) {
					for (DeviceInfo deviceInfo : dInfos) {
						if (deviceInfo.getCak().equals("surveillance")) {
							device = deviceInfo;
							break;
						}
					}
				}

				// 从服务器上获取到监控设备下的所有设备
				if (device != null && device.getCak().equals("surveillance")) {
					List<CameraInfo> ca = new ArrayList<>();
					ca = (List<CameraInfo>) JSON.parseArray(device.getIpc(), CameraInfo.class);
					if (ca != null && !ca.isEmpty()) {
						camera.addAll(ca);
					}
				}
			}
		}
		//根据id找到报警设备（有多条相同设备信息时只去第一条 注意：一个监控设备可以被多个主机添加（名称不一样，但是密码一样））
		if (camera != null && !camera.isEmpty()) {
			for (CameraInfo cameraInfo : camera) {
				if (alarmRecord.deviceId.equals(cameraInfo.getId())) {
					return true;
				}
			}
		}
		return false;
	}
}
