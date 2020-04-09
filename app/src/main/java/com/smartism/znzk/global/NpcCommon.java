package com.smartism.znzk.global;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NpcCommon {
	public static enum NETWORK_TYPE {
		NETWORK_2GOR3G, NETWORK_WIFI,
	};

	private static Boolean mIsNetWorkAvailable = false;
	public static NETWORK_TYPE mNetWorkType = NETWORK_TYPE.NETWORK_WIFI;

	public static String mThreeNum="0517401";

	public static void setNetWorkState(boolean state) {
		mIsNetWorkAvailable = state;
	}

	public static boolean getNetWorkState() {
		return mIsNetWorkAvailable;
	}

	public static boolean verifyNetwork(Context context) {
		boolean isNetworkActive = false;
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null) {
			if (activeNetInfo.isConnected()) {
				setNetWorkState(true);
				isNetworkActive = true;
			} else {
				setNetWorkState(false);
				isNetworkActive = false;
			}

			if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
				mNetWorkType = NETWORK_TYPE.NETWORK_WIFI;
			} else {
				mNetWorkType = NETWORK_TYPE.NETWORK_2GOR3G;
			}
		} else {
			setNetWorkState(false);
			isNetworkActive = false;
		}

		return isNetworkActive;
	}

}
