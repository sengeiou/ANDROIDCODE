package com.smartism.znzk.util.camera;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;


import com.p2p.core.P2PValue;
import com.p2p.shake.ShakeManager;
import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.global.AppConfig;
import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.FList;
import com.smartism.znzk.application.MainApplication;


import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class WifiUtils {
	private final static int WIFICIPHER_WEP = 0;
	private final static int WIFICIPHER_WPA = 1;
	private final static int WIFICIPHER_NOPASS = 3;
	private final static int WIFICIPHER_INVALID = 4;

	private static WifiUtils wifiutils = null;
	private WifiManager wifiManager;
	private Context context;
	private Handler mHandler;
	private static String APTag = AppConfig.Relese.APTAG;
	private WifiLock wifiLock;

	// 构造函数
	private WifiUtils(WifiManager wifiManager) {
		this.wifiManager = wifiManager;
	}

	private WifiUtils(Context context) {
		this.context = context;
		this.wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
	}

	public static WifiUtils getInstance() {
		if (wifiutils == null) {
			wifiutils = new WifiUtils(MainApplication.app);
		}
		if (wifiutils.wifiManager == null) {
			wifiutils.wifiManager = (WifiManager) MainApplication.app
					.getSystemService(Context.WIFI_SERVICE);
		}
		return wifiutils;
	}

	/**
	 * 创建WiFi锁
	 * 
	 * @param lockName
	 * @return
	 */
	public WifiLock CreatWifiLock(String lockName) {
		wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY,
				lockName);
		return wifiLock;
	}

	public void wifiLock() {
		if (wifiLock == null) {
			CreatWifiLock(APTag);
		}
		wifiLock.acquire();
	}

	public void wifiUnlock() {
		if (wifiLock != null && wifiLock.isHeld()) {
			wifiLock.release();
		}
	}

	public void setHandler(Handler mHandler) {
		this.mHandler = mHandler;
	}

	public void getAPmodeDevice() {
		// 获取当前可用wifi
		wifiManager.startScan();
		List<ScanResult> wifiList = wifiManager.getScanResults();
		String name;
		if (wifiList != null && wifiList.size() > 0) {
			for (int i = 0; i < wifiList.size(); i++) {
				name = wifiList.get(i).SSID;
				if (isApDevice(name)) {
					if (null != mHandler) {
						Message msg = new Message();
						msg.what = ShakeManager.HANDLE_ID_ONEAPDEVICE;
						Bundle bundle = new Bundle();
						try {
							bundle.putSerializable("address",
									InetAddress.getByName("192.168.1.1"));
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						bundle.putString("id",
								name.substring(AppConfig.Relese.APTAG.length()));
						bundle.putString("name", name);
						bundle.putInt("flag", Constants.DeviceFlag.AP_MODE);
						bundle.putInt("type", P2PValue.DeviceType.IPC);
						msg.setData(bundle);
						mHandler.sendMessage(msg);
					}
				}
			}
		}
		if (mHandler != null) {
			Message msg = new Message();
			msg.what = ShakeManager.HANDLE_ID_APDEVICE_END;
			mHandler.sendMessage(msg);
		}
	}

	public static boolean isApDevice(String name) {

		return name.startsWith(APTag);
	}

	public boolean getIsOpen() {
		return wifiManager.isWifiEnabled();
	}

	public void connectWifi(String SSID, String Password, int Type) {
		if (wifiManager != null) {
			if (getIsOpen()) {
				ConnectHandler(SSID, Password, Type);
				Log.e("dxswifi", "getIsOpen()==true");
			} else {
				// 打开WIFI
				Log.e("dxswifi", "getIsOpen()==false");
				if (OpenWifi()) {
					Log.e("dxswifi", "OpenWifi()==true");
					ConnectHandler(SSID, Password, Type);
				} else {
					// 打开Wifi失败
					Log.e("dxswifi", "OpenWifi()==false");
				}
			}
		} else {
			Log.e("dxswifi", "wifiManager==null");
		}
	}

	// 打开wifi功能
	public boolean OpenWifi() {
		boolean bRet = true;
		if (!wifiManager.isWifiEnabled()) {
			bRet = wifiManager.setWifiEnabled(true);
		}
		return bRet;
	}

	public boolean ConnectHandler(String SSID, String Password, int Type) {
		WifiConfiguration wifi = this.IsExsits(SSID);
		if (wifi != null) {
			boolean isremove=wifiManager.removeNetwork(wifi.networkId);
			Log.e("leleWifi", "isremove="+isremove);
		}

		WifiConfiguration wifiConfig = this
				.CreateWifiInfo(SSID, Password, Type);
		if (wifiConfig == null)
			return false;

		int netID = wifiManager.addNetwork(wifiConfig);
		Log.e("leleWifi", "netID="+netID);
		connectWifi(netID);
		return true;
	}

	/** 链接指定wifi **/
	public boolean connectWifi(int netId) {
		return wifiManager.enableNetwork(netId, true);
	}

	// 查看以前是否也配置过这个网络
	public WifiConfiguration IsExsits(String SSID) {
		if (SSID == null || wifiManager == null) {
			return null;
		}
		List<WifiConfiguration> existingConfigs = wifiManager
				.getConfiguredNetworks();
		if (existingConfigs == null) {
			return null;
		}
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"")
					|| existingConfig.SSID.equals(SSID)) {
				return existingConfig;
			}
		}
		return null;
	}

	public WifiConfiguration CreateWifiInfo(String SSID, String Password,
			int Type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";
		if (Type == WIFICIPHER_NOPASS) {
//			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//			config.wepTxKeyIndex = 0;
		}
		if (Type == WIFICIPHER_WEP) {
//			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + Password + "\"";
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == WIFICIPHER_WPA) {
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			if (Password == null || Password.equals("")) {
				config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
				// config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.NONE);
			} else {
				config.preSharedKey = "\"" + Password + "\"";
				config.allowedGroupCiphers
						.set(WifiConfiguration.GroupCipher.TKIP);
				config.allowedKeyManagement
						.set(WifiConfiguration.KeyMgmt.WPA_PSK);
				config.allowedPairwiseCiphers
						.set(WifiConfiguration.PairwiseCipher.TKIP);
				// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
				config.allowedGroupCiphers
						.set(WifiConfiguration.GroupCipher.CCMP);
				config.allowedPairwiseCiphers
						.set(WifiConfiguration.PairwiseCipher.CCMP);
			}
			config.status = WifiConfiguration.Status.ENABLED;
		} else {
			return null;
		}
		return config;
	}

	// 检查指定的wifi是否存在列表中
	public boolean isScanExist(String SSid) {
		
		List<ScanResult> lists = getLists();
		if (lists != null && lists.size() > 0) {

			for (int i = 0; i < lists.size(); i++) {

				if (lists.get(i).SSID.equals(SSid)) {

					return true;
				}
			}

		}
		return false;
	}

	public List<ScanResult> getLists() {

		wifiManager.startScan();
		List<ScanResult> lists = wifiManager.getScanResults();
		return lists;
	}

	public boolean isConnectWifi(String SSID) {
		if (wifiManager == null) {
			return false;
		}
		Log.e("dxswifi", "wifiinfo-->"
				+ wifiManager.getConnectionInfo().toString());
		String connectSSID = wifiManager.getConnectionInfo().getSSID();
		int workId = wifiManager.getConnectionInfo().getNetworkId();
		if (TextUtils.isEmpty(connectSSID) || workId == -1) {
			return false;
		}
		return connectSSID.equals("\"" + SSID + "\"")
				|| connectSSID.equals(SSID);
	}
	
	public boolean isConnectAP(){
		String wifiName=WifiUtils.getInstance().getConnectWifiName();
		Log.e("wifi name", wifiName);
		if(wifiName.length()>0&&wifiName.charAt(0)=='"'){
			wifiName=wifiName.substring(1, wifiName.length()-1);
		
		}
		return wifiName.startsWith(AppConfig.Relese.APTAG);
		
	}
	
	public void SetWiFiEnAble(boolean enable){
		if(wifiManager!=null){
			wifiManager.setWifiEnabled(enable);
		}
	}
	
	public void DisConnectWifi(String ssid){
		if (isConnectWifi(ssid)) {
			wifiManager.disconnect();
		}
	}

	/**
	 * 断开某个WiFi，并忘记网络
	 * 
	 * @param ssid
	 */
	public void disConnectWifi(String ssid) {
		if (isConnectWifi(ssid)) {
			wifiManager.disconnect();
		}
		WifiConfiguration wifi = this.IsExsits(ssid);
		if (wifi != null) {
			wifiManager.removeNetwork(wifi.networkId);
		}
	}
	public String getConnectWifiName(){
		  WifiManager wifiManager = (WifiManager)MainApplication.app.getSystemService(Context.WIFI_SERVICE); 
          WifiInfo wifiInfo = wifiManager.getConnectionInfo(); 
          return wifiInfo.getSSID(); 
		
	}
	public void isApDevice(){
		String wifiName=getConnectWifiName();
		if(wifiName.length()>0&&wifiName.charAt(0)=='"'){
			wifiName=wifiName.substring(1, wifiName.length()-1);
		}
		int length=AppConfig.Relese.APTAG.length();
		if(wifiName.startsWith(AppConfig.Relese.APTAG)){
//			String contactId=wifiName.substring(length,wifiName.length());
//			FList.getInstance().gainIsApMode(contactId);
		}else{
			FList.getInstance().setIsConnectApWifi(false);
		}
	}
}
