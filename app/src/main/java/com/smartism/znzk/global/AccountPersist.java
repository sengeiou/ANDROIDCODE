package com.smartism.znzk.global;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.smartism.znzk.domain.camera.Account;
import com.smartism.znzk.util.camera.MusicManger;



//save account data
public class AccountPersist {
	public static final String ACCOUNT_SHARED_PREFERENCE = "account_shared";
	public static AccountPersist manager = null;

	private AccountPersist() {
	}

	public synchronized static AccountPersist getInstance() {
		if (null == manager) {
			synchronized (MusicManger.class) {
				if (null == manager) {
					manager = new AccountPersist();
				}
			}
		}
		return manager;
	}

	public static String mACCOUNTPWD = "account_pwd";
	public static final String ACC_INFO_3C = "account_info_3c";
	public static final String ACC_INFO_PHONE = "account_info_phone";
	public static final String ACC_INFO_EMAIL = "account_info_email";
	public static final String ACC_INFO_SESSION_ID = "account_info_session_id";
	public static final String ACC_INFO_SESSION_ID2 = "account_info_session_id2";
	public static final String ACC_INFO_COUNTRY_CODE = "account_info_country_code";
	public static final String CODE1 = "code1";
	public static final String CODE2 = "code2";
	public static final String ACTIVE = "active";

	public void setActiveAccount(Context context, Account account) {

		SharedPreferences preference = context.getSharedPreferences(
				ACCOUNT_SHARED_PREFERENCE, context.MODE_PRIVATE);
		Editor editor = preference.edit();
		editor.putString(ACC_INFO_3C, account.three_number);
		editor.putString(ACC_INFO_PHONE, account.phone);
		editor.putString(ACC_INFO_EMAIL, account.email);
		editor.putString(ACC_INFO_SESSION_ID, String.valueOf(account.sessionId));
		editor.putString(ACC_INFO_SESSION_ID2, String.valueOf(account.sessionId2));
		editor.putString(CODE1, String.valueOf(account.rCode1));
		editor.putString(CODE2, String.valueOf(account.rCode2));
		editor.putString(ACC_INFO_COUNTRY_CODE, account.countryCode);
		editor.putString(mACCOUNTPWD, account.password);
		editor.commit();
	}

	public void delActiveAccount(Context context) {

		SharedPreferences preference = context.getSharedPreferences(
				ACCOUNT_SHARED_PREFERENCE, context.MODE_PRIVATE);
		Editor editor = preference.edit();
		editor.remove(ACC_INFO_3C);
		editor.remove(ACC_INFO_PHONE);
		editor.remove(ACC_INFO_EMAIL);
		editor.remove(ACC_INFO_SESSION_ID);
		editor.remove(ACC_INFO_SESSION_ID2);
		editor.remove(CODE1);
		editor.remove(CODE2);
		editor.remove(ACC_INFO_COUNTRY_CODE);
		editor.remove(mACCOUNTPWD);
		editor.commit();
	}

	public Account getActiveAccountInfo(Context context) {
		SharedPreferences preference = context.getSharedPreferences(
				ACCOUNT_SHARED_PREFERENCE, context.MODE_PRIVATE);
		Editor editor = preference.edit();
		String three_number = preference.getString(ACC_INFO_3C, "");
		String phone = preference.getString(ACC_INFO_PHONE, "");
		String email = preference.getString(ACC_INFO_EMAIL, "");
		int sessionId = Integer.parseInt(preference.getString(ACC_INFO_SESSION_ID, "0"));
		int sessionId2 = Integer.parseInt(preference.getString(ACC_INFO_SESSION_ID2, "0"));
		int code1 = Integer.parseInt(preference.getString(CODE1, "0"));
		int code2 = Integer.parseInt(preference.getString(CODE2, "0"));
		String countryCode = preference.getString(ACC_INFO_COUNTRY_CODE, "");
		String password = preference.getString(mACCOUNTPWD, "");
		editor.commit();
		if (three_number.equals("")) {
			return null;
		} else {
			return new Account(three_number, email, phone, sessionId,sessionId2, code1,
					code2, countryCode,password);
		}
	}

}
