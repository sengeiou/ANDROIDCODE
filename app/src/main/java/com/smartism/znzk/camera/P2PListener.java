package com.smartism.znzk.camera;
import android.content.Intent;
import android.util.Log;

import com.p2p.core.P2PInterface.IP2P;
import com.p2p.core.P2PValue;
import com.smartism.znzk.R;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.camera.SharedPreferencesManager;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.util.camera.MusicManger;

/**
 * Created by dxs on 2016/6/13.
 * p2p监控时的回调接口
 */
public class P2PListener implements IP2P {
	/**
	 * 被设备呼叫时的回调
	 * @param isOutCall
	 * @param threeNumber ID号
	 * @param type
	 */
	@Override
	public void vCalling(boolean isOutCall, String threeNumber, int type) {
		if (isOutCall) {
			P2PConnect.vCalling(true, type);
		} else {
			int c_muteState = SharedPreferencesManager.getInstance()
					.getCMuteState(MainApplication.app);
			if (c_muteState == 1) {
				MusicManger.getInstance().playCommingMusic();
			}

			int c_vibrateState = SharedPreferencesManager.getInstance()
					.getCVibrateState(MainApplication.app);
			if (c_vibrateState == 1) {
				MusicManger.getInstance().Vibrate();
			}

			P2PConnect.setCurrent_call_id(threeNumber);

			P2PConnect.vCalling(false, type);
		}
		Log.e("P2PListener---vCalling","isOutCall:"+isOutCall+"--"+"threeNumber:"+threeNumber+"--"+"type:"+type);
	}

	/**
	 * 设备端挂断时的回调
	 * @param deviceId 设备挂断
	 * @param reason_code 挂断原因
	 */
	@Override
	public void vReject(String deviceId, int reason_code, int exCode1, int exCode2) {
		String reason = "";
		switch (reason_code) {
			case 0:
				reason = MainApplication.app.getResources().getString(R.string.pw_incrrect);
				break;
			case 1:
				reason = MainApplication.app.getResources().getString(R.string.busy);
				break;
			case 2:
				reason = MainApplication.app.getResources().getString(R.string.none);
				break;
			case 3:
				reason = MainApplication.app.getResources().getString(R.string.id_disabled);
				break;
			case 4:
				reason = MainApplication.app.getResources().getString(R.string.id_overdate);
				break;
			case 5:
				reason = MainApplication.app.getResources().getString(R.string.id_inactived);
				break;
			case 6:
				reason = MainApplication.app.getResources().getString(R.string.offline);
				break;
			case 7:
				reason = MainApplication.app.getResources().getString(R.string.powerdown);
				break;
			case 8:
				reason = MainApplication.app.getResources().getString(R.string.nohelper);
				break;
			case 9:
				reason = MainApplication.app.getResources().getString(R.string.hungup);
				break;
			case 10:
				reason = MainApplication.app.getResources().getString(R.string.timeout);
				break;
			case 11:
				reason = MainApplication.app.getResources().getString(R.string.no_body);
				break;
			case 12:
				reason = MainApplication.app.getResources()
						.getString(R.string.internal_error);
				break;
			case 13:
				reason = MainApplication.app.getResources().getString(R.string.conn_fail);
				break;
			case 14:
				reason = MainApplication.app.getResources().getString(R.string.not_support);
				break;
			case 15:
				reason = MainApplication.app.getResources().getString(R.string.rtsp_not_frame);
				break;
			default:
				reason = MainApplication.app.getResources().getString(R.string.conn_fail)+"("+reason_code+")";
				break;

		}
		P2PConnect.vReject(reason_code,reason);

		Intent intent = new Intent();
		intent.setAction(Constants.P2P.P2P_REJECT);
		intent.putExtra("reason_code", reason_code);
		MainApplication.app.sendBroadcast(intent);
	}

	/**
	 * 设备端接听时的回调,参数需要传给P2PView
	 * @param type 视频宽高比例参数
	 * @param state 视频宽高比例参数
	 */
	@Override
	public void vAccept(int type, int state) {
		Intent accept = new Intent();
		accept.setAction(Constants.P2P.P2P_ACCEPT);
		accept.putExtra("type", new int[]{type, state});
		MainApplication.app.sendBroadcast(accept);
//		P2PConnect.vAccept(type, state);
	}

	/**
	 * 准备好传输音视频,一次监控只会被调用一次
	 */
	@Override
	public void vConnectReady() {
		Intent intent = new Intent();
		intent.setAction(Constants.P2P.P2P_READY);
		MainApplication.app.sendBroadcast(intent);
//		P2PConnect.vConnectReady();
	}

	@Override
	public void vAllarming(String srcId, int type, boolean isSupportExternAlarm, int iGroup, int iItem, boolean isSurpportDelete) {
		//老版设备报警时的回调,可忽略,新版报警回调见下面方法,应用不在前台或者后台时不会响应
		P2PConnect.vAllarming(Integer.parseInt(srcId), type,
				isSupportExternAlarm, iGroup, iItem, isSurpportDelete);
	}

	@Override
	public void vChangeVideoMask(int state) {
		Intent i = new Intent(Constants.P2P.P2P_CHANGE_IMAGE_TRANSFER);
		i.putExtra("state", state);
		MainApplication.app.sendBroadcast(i);

	}

	@Override
	public void vRetPlayBackPos(int length, int currentPos) {
		//回放进度回调  length是总时长 currentPos当前播放时间点
		Intent i = new Intent();
		i.setAction(Constants.P2P.PLAYBACK_CHANGE_SEEK);
		i.putExtra("max", length);
		i.putExtra("current", currentPos);
		MainApplication.app.sendBroadcast(i);
	}

	@Override
	public void vRetPlayBackStatus(int state) {
		//回放状态回调
		Intent i = new Intent();
		i.setAction(Constants.P2P.PLAYBACK_CHANGE_STATE);
		i.putExtra("state", state);
		MainApplication.app.sendBroadcast(i);
	}

	@Override
	public void vGXNotifyFlag(int flag) {

	}

	/**
	 * 设备视频解析出来的宽高,可根据宽来判断清晰度
	 * 1280 1920 高清  640 标清  320  流畅
	 * @param iWidth 宽
	 * @param iHeight 高
	 */
	@Override
	public void vRetPlaySize(int iWidth, int iHeight, boolean b) {
		Log.e("p2p", "vRetPlaySize:" + iWidth + "-" + iHeight);
		Intent i = new Intent();
		i.setAction(Constants.P2P.P2P_RESOLUTION_CHANGE);
		if (iWidth == 1280 || iWidth == 1920) {
			P2PConnect.setMode(P2PValue.VideoMode.VIDEO_MODE_HD);
			i.putExtra("mode", P2PValue.VideoMode.VIDEO_MODE_HD);
		} else if (iWidth == 640) {
			P2PConnect.setMode(P2PValue.VideoMode.VIDEO_MODE_SD);
			i.putExtra("mode", P2PValue.VideoMode.VIDEO_MODE_SD);
		} else if (iWidth == 320) {
			P2PConnect.setMode(P2PValue.VideoMode.VIDEO_MODE_LD);
			i.putExtra("mode", P2PValue.VideoMode.VIDEO_MODE_LD);
		}
		MainApplication.app.sendBroadcast(i);
		String method = new Exception().getStackTrace()[0].getMethodName();
		Log.e("P2PListener", method);
	}

	/**
	 * 当前观看人数,人数改变时回调一次
	 * @param iNumber 人数
	 * @param data
	 */
	@Override
	public void vRetPlayNumber(int iNumber, int[] data) {
		Log.e("my", "vRetPlayNumber:" + iNumber);
		P2PConnect.setNumber(iNumber);
		Intent i = new Intent();
		i.setAction(Constants.P2P.P2P_MONITOR_NUMBER_CHANGE);
		i.putExtra("number", iNumber);
		MainApplication.app.sendBroadcast(i);
	}

	/**
	 * 设备传过来的,解码前的音视频数据,已停止回调
	 * @param AudioBuffer 音频数据
	 * @param AudioLen 音频长度
	 * @param AudioFrames 音频帧
	 * @param AudioPTS 音频PTS
	 * @param VideoBuffer 视频数据
	 * @param VideoLen 视频长度
	 * @param VideoPTS 视频PTS
	 */
	@Override
	public void vRecvAudioVideoData(byte[] AudioBuffer, int AudioLen, int AudioFrames, long AudioPTS, byte[] VideoBuffer, int VideoLen, long VideoPTS) {

	}

	/**
	 * 新版报警回调,注意查看硬件接口说明
	 * @param srcId 报警设备ID
	 * @param type 报警类型
	 * @param option 功能参数
	 * @param iGroup 防区
	 * @param iItem 通道
	 * @param imagecounts 图片数量 以下功能需要设备支持,支持情况会在option反应
	 * @param imagePath 图片在设备端的路径
	 * @param alarmCapDir 报警路径
	 * @param VideoPath 报警视频路径 暂不支持
	 * @param sensorName 传感器名字 需要设备支持
	 * @param deviceType 设备类型 高16位是子类型 低16位是主类型
	 */
	@Override
	public void vAllarmingWitghTime(String srcId, int type, int option, int iGroup, int iItem, int imagecounts, String imagePath, String alarmCapDir, String VideoPath, String sensorName, int deviceType) {
		P2PConnect.vAllarmingWithPath(srcId, type, option, iGroup, iItem, imagecounts,imagePath, alarmCapDir, VideoPath);

	}

	/**
	 * 新的系统消息
	 * @param iSystemMessageType 消息类型
	 * @param iSystemMessageIndex 消息索引标记
	 */
	@Override
	public void vRetNewSystemMessage(int iSystemMessageType, int iSystemMessageIndex) {

	}

	@Override
	public void vRetRTSPNotify(int arg2, String msg) {
		//暂时无用
	}

	/**
	 * 底层视屏渲染通知消息出口,监控时会被回调多次
	 * @param what 消息主标记 为10时是视屏渲染到屏幕
	 * @param iDesID
	 * @param arg1
	 * @param arg2
	 * @param msgStr
	 */
	@Override
	public void vRetPostFromeNative(int what, int iDesID, int arg1, int arg2, String msgStr) {
		if(what==10){
			//视频渲染标记
			Intent i = new Intent();
			i.setAction(Constants.P2P.RET_P2PDISPLAY);
			MainApplication.app.sendBroadcast(i);
		}
	}

	/**
	 * 这个方法用于拦截是否是TF卡录像状态的推送
	 * @param cmd 命令头
	 * @param option 操作
	 * @param data 数据
	 */
	@Override
	public void vRetUserData(byte cmd, byte option, int[] data) {

	}
}
