//package com.smartism.znzk.util.yaokan;
//
//import android.os.Looper;
//
//public class YkanSDKManager {
//
//	private static String TAG = YkanSDKManager.class.getSimpleName();
//
//
//	private String appId = "";
//	private String deviceId = "";
//	private boolean initFinished = false;
//	
//	public static YkanSDKManager yKanSDKManager;
//
//	private YkanSDKManager(String appID,String deviceId){
//		this.appId = appID;
//		this.deviceId = deviceId;
//	}
//
//	public static YkanSDKManager init( String appID,String deviceId) {
//		if( yKanSDKManager == null ){
//			yKanSDKManager = new YkanSDKManager(appID,deviceId);
//		}
//		return yKanSDKManager;
//	}
//	public static YkanSDKManager getInstance() {
//		if( yKanSDKManager != null ){
//			return yKanSDKManager;
//		}else{
//			if (Looper.myLooper() ==null){
//				Looper.prepare();
//			}
//			//Logger.e(TAG, "没有调用  YkanSDKManager.init(Context  ctx,String appID)方法，请先执行");
//			return null;
//		}
//	}
//
//	/**
//	 * 获取AppId
//	 * @return
//	 */
//	public String getAppId() {
//		return appId;
//	}
//
//	/**
//	 * 获取设备ID
//	 * @return
//	 */
//	public String getDeviceId() {
//		return deviceId;
//	}
//
//	/**
//	 *判断是否已经完成
//	 * @return
//	 */
//	public boolean isInitFinished() {
//		return initFinished;
//	}
//
//	public void setLogger(boolean b) {
//		//Logger.mLogGrade = b;
//	}
//
//}
