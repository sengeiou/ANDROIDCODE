package com.smartism.znzk.global;

import com.smartism.znzk.application.MainApplication;
import com.smartism.znzk.db.camera.SharedPreferencesManager;
import com.smartism.znzk.domain.camera.Account;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;



public class AutoStartReceiver extends BroadcastReceiver {
	private static final String action_boot = "android.intent.action.BOOT_COMPLETED";
	Context mContext;
	public static boolean isMainActivityStart;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.e("my", "************************************************");
		mContext = context;

		if (intent.getAction().equals(action_boot)) {
			Account activeUser = AccountPersist.getInstance()
					.getActiveAccountInfo(context);

			if (null != activeUser && null != activeUser.three_number
					&& !"".equals(activeUser.three_number)) {
				if (SharedPreferencesManager.getInstance().getIsAutoStart(
						context, activeUser.three_number)) {
					Log.e("my", "execute");
					isMainActivityStart = false;
					NpcCommon.mThreeNum = activeUser.three_number;

					Intent service = new Intent(MainApplication.MAIN_SERVICE_START);
					context.startService(service);
					//MainApplication.app.showNotification();
				}

			}

		}
		Log.e("my", "************************************************");
	}
}
