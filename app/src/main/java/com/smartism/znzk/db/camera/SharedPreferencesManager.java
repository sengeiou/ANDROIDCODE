package com.smartism.znzk.db.camera;

import com.smartism.znzk.global.Constants;
import com.smartism.znzk.global.NpcCommon;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;



public class SharedPreferencesManager {
	public static final String SP_FILE_GWELL = "gwell";
	public static final String KEY_RECENTNAME = "recentName";
	public static final String KEY_RECENTPASS = "recentPass";
	public static final String KEY_RECENTCODE = "recentCode";

	public static final String KEY_RECENTNAME_EMAIL = "recentName_email";
	public static final String KEY_RECENTPASS_EMAIL = "recentPass_email";

	public static final String KEY_NAMES = "names";
	public static final String KEY_UPDATE_CHECKTIME = "update_checktime";

	public static final String KEY_C_VIBRATE_STATE = "c_vibrate_state";
	public static final String KEY_C_SYS_BELL = "c_system_bell";
	public static final String KEY_C_SD_BELL = "c_sd_bell";
	public static final String KEY_C_BELL_SELECTPOS = "c_selectpos";
	public static final String KEY_C_MUTE_STATE = "c_mute_state";
	public static final String KEY_C_BELL_TYPE = "c_bell_type";
	public static final String KEY_A_VIBRATE_STATE = "a_vibrate_state";
	public static final String KEY_A_SYS_BELL = "a_system_bell";
	public static final String KEY_A_SD_BELL = "a_sd_bell";
	public static final String KEY_A_BELL_SELECTPOS = "a_selectpos";
	public static final String KEY_A_MUTE_STATE = "a_mute_state";
	public static final String KEY_A_BELL_TYPE = "a_bell_type";
	public static final String NOTIFY_VERSION = "notify_version";

	public static final String IS_SHOW_NOTIFY = "is_show_notify";
	public static final String IS_REMEMBER_PASS = "is_remember_pass";
	public static final String IS_REMEMBER_PASS_EMAIL = "is_remember_pass_email";
	public static final String RECENT_LOGIN_TYPE = "recent_login_type";
	public static final String IGONORE_ALARM_TIME = "ignore_alarm_time";
	public static final String ALARM_TIME_INTERVAL = "alarm_time_interval";
	public static final String IS_AUTO_START = "is_auto_start";
	public static final String IS_EMAIL_SENDSELF = "is_email_sendself";
	public static final String IS_AP_ENTER = "is_ap_enter";

	public static final String LAST_AUTO_CHECK_UPDATE_TIME = "last_auto_check_update_time";

	public static final int TYPE_BELL_SYS = 0;
	public static final int TYPE_BELL_SD = 1;
	private static SharedPreferencesManager manager = null;

	private SharedPreferencesManager() {
	}

	public synchronized static SharedPreferencesManager getInstance() {
		if (null == manager) {
			synchronized (SharedPreferencesManager.class) {
				if (null == manager) {
					manager = new SharedPreferencesManager();
				}
			}
		}
		return manager;
	}

	public String getData(Context context, String fileName, String key) {
		SharedPreferences sf = context.getSharedPreferences(fileName,
				context.MODE_PRIVATE);
		return sf.getString(key, "");
	}

	public void putData(Context context, String fileName, String key,
			String value) {
		SharedPreferences sf = context.getSharedPreferences(fileName,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public int getIntData(Context context, String fileName, String key) {
		SharedPreferences sf = context.getSharedPreferences(fileName,
				context.MODE_PRIVATE);
		return sf.getInt(key, 0);
	}

	public void putIntData(Context context, String fileName, String key,
			int value) {
		SharedPreferences sf = context.getSharedPreferences(fileName,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public void putNotifyVersion(int version, Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putInt(NpcCommon.mThreeNum + NOTIFY_VERSION, version);
		editor.commit();
	}

	public int getNotifyVersion(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getInt(NpcCommon.mThreeNum + NOTIFY_VERSION, 0);
	}

	public void putLastAutoCheckUpdateTime(long time, Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putLong(NpcCommon.mThreeNum + LAST_AUTO_CHECK_UPDATE_TIME, time);
		editor.commit();
	}

	public long getLastAutoCheckUpdateTime(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getLong(NpcCommon.mThreeNum + LAST_AUTO_CHECK_UPDATE_TIME, 0);
	}

	public void putIgnoreAlarmTime(Context context, long time) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putLong(NpcCommon.mThreeNum + IGONORE_ALARM_TIME, time);
		editor.commit();
	}

	public long getIgnoreAlarmTime(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getLong(NpcCommon.mThreeNum + IGONORE_ALARM_TIME, 0);
	}

	public void putIsAutoStart(Context context, boolean bool) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putBoolean(NpcCommon.mThreeNum + IS_AUTO_START, bool);
		editor.commit();
	}

	public boolean getIsAutoStart(Context context, String threeNum) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getBoolean(threeNum + IS_AUTO_START, false);
	}

	public void putAlarmTimeInterval(Context context, int time) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putInt(NpcCommon.mThreeNum + ALARM_TIME_INTERVAL, time);
		editor.commit();
	}

	public int getAlarmTimeInterval(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getInt(NpcCommon.mThreeNum + ALARM_TIME_INTERVAL, 10);
	}

	public void putIsShowNotify(Context context, boolean bool) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putBoolean(NpcCommon.mThreeNum + IS_SHOW_NOTIFY, bool);
		editor.commit();
	}

	public boolean getIsShowNotify(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getBoolean(NpcCommon.mThreeNum + IS_SHOW_NOTIFY, true);
	}

	public void putIsRememberPass(Context context, boolean bool) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putBoolean(IS_REMEMBER_PASS, bool);
		editor.commit();
	}

	public boolean getIsRememberPass(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getBoolean(IS_REMEMBER_PASS, true);
	}

	public void putIsRememberPass_email(Context context, boolean bool) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putBoolean(IS_REMEMBER_PASS_EMAIL, bool);
		editor.commit();
	}

	public boolean getIsRememberPass_email(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getBoolean(IS_REMEMBER_PASS_EMAIL, true);
	}

	public void putRecentLoginType(Context context, int type) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putInt(RECENT_LOGIN_TYPE, type);
		editor.commit();
	}

	public int getRecentLoginType(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getInt(RECENT_LOGIN_TYPE, Constants.LoginType.PHONE);
	}

	public void putCVibrateState(int state, Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putInt(NpcCommon.mThreeNum + KEY_C_VIBRATE_STATE, state);
		editor.commit();
	}

	public int getCVibrateState(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getInt(NpcCommon.mThreeNum + KEY_C_VIBRATE_STATE, 1);
	}

	public void putAVibrateState(int state, Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putInt(NpcCommon.mThreeNum + KEY_A_VIBRATE_STATE, state);
		editor.commit();
	}

	public int getAVibrateState(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getInt(NpcCommon.mThreeNum + KEY_A_VIBRATE_STATE, 1);
	}

	public void putCSystemBellId(int bellId, Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putInt(NpcCommon.mThreeNum + KEY_C_SYS_BELL, bellId);
		editor.commit();
	}

	public int getCSystemBellId(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getInt(NpcCommon.mThreeNum + KEY_C_SYS_BELL, -1);
	}

	public void putASystemBellId(int bellId, Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putInt(NpcCommon.mThreeNum + KEY_A_SYS_BELL, bellId);
		editor.commit();
	}

	public int getASystemBellId(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getInt(NpcCommon.mThreeNum + KEY_A_SYS_BELL, -1);
	}

	public int getCSdBellId(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getInt(NpcCommon.mThreeNum + KEY_C_SD_BELL, -1);
	}

	public void putCSdBellId(int bellId, Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putInt(NpcCommon.mThreeNum + KEY_C_SD_BELL, bellId);
		editor.commit();
	}

	public int getASdBellId(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getInt(NpcCommon.mThreeNum + KEY_A_SD_BELL, -1);
	}

	public void putASdBellId(int bellId, Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putInt(NpcCommon.mThreeNum + KEY_A_SD_BELL, bellId);
		editor.commit();
	}

	public void putCBellSelectPos(int selectpos, Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putInt(NpcCommon.mThreeNum + KEY_C_BELL_SELECTPOS, selectpos);
		editor.commit();
	}

	public int getCBellSelectPos(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getInt(NpcCommon.mThreeNum + KEY_C_BELL_SELECTPOS, 0);
	}

	public void putABellSelectPos(int selectpos, Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putInt(NpcCommon.mThreeNum + KEY_A_BELL_SELECTPOS, selectpos);
		editor.commit();
	}

	public int getABellSelectPos(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getInt(NpcCommon.mThreeNum + KEY_A_BELL_SELECTPOS, 0);
	}

	public void putCMuteState(int state, Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putInt(NpcCommon.mThreeNum + KEY_C_MUTE_STATE, state);
		editor.commit();
	}

	public int getCMuteState(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getInt(NpcCommon.mThreeNum + KEY_C_MUTE_STATE, 1);
	}

	public void putAMuteState(int state, Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putInt(NpcCommon.mThreeNum + KEY_A_MUTE_STATE, state);
		editor.commit();
	}

	public int getAMuteState(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getInt(NpcCommon.mThreeNum + KEY_A_MUTE_STATE, 1);
	}

	public void putCBellType(int type, Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putInt(NpcCommon.mThreeNum + KEY_C_BELL_TYPE, type);
		editor.commit();
	}

	public int getCBellType(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getInt(NpcCommon.mThreeNum + KEY_C_BELL_TYPE, TYPE_BELL_SYS);
	}

	public void putABellType(int type, Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putInt(NpcCommon.mThreeNum + KEY_A_BELL_TYPE, type);
		editor.commit();
	}

	public int getABellType(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getInt(NpcCommon.mThreeNum + KEY_A_BELL_TYPE, TYPE_BELL_SYS);
	}

	public void putIsDoorbellBind(String doorbellId, boolean isDoorBellBind,
			Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putBoolean(doorbellId, isDoorBellBind);
		editor.commit();
	}

	public boolean getIsDoorbellBind(Context context, String doorbellId) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getBoolean(doorbellId, false);
	}

	public void putIsSendemailToSelf(Context context, boolean bool) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putBoolean(IS_EMAIL_SENDSELF, bool);
		editor.commit();
	}

	public boolean getIsSendemailToSelf(Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getBoolean(IS_EMAIL_SENDSELF, false);
	}

	public void putIsDoorBellToast(String doorbellId, boolean isDoorBellBind,
			Context context) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		Editor editor = sf.edit();
		editor.putBoolean(doorbellId, isDoorBellBind);
		editor.commit();
	}

	public boolean getIsDoorBellToast(Context context, String doorbellId) {
		SharedPreferences sf = context.getSharedPreferences(SP_FILE_GWELL,
				context.MODE_PRIVATE);
		return sf.getBoolean(doorbellId, false);
	}
}
