package com.smartism.znzk.db.camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.smartism.znzk.util.camera.Utils;



import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

public class APContactDB {
	public static final String TABLE_NAME = "apcontact";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_ID_DATA_TYPE = "integer PRIMARY KEY AUTOINCREMENT";

	public static final String COLUMN_CONTACT_NAME = "APnickName";
	public static final String COLUMN_CONTACT_NAME_DATA_TYPE = "varchar";

	public static final String COLUMN_CONTACT_ID = "APcontactName";
	public static final String COLUMN_CONTACT_ID_DATA_TYPE = "varchar";

	public static final String COLUMN_CONTACT_PASSWORD = "APcontactPwd";
	public static final String COLUMN_CONTACT_PASSWORD_DATA_TYPE = "blob";

	public static final String COLUMN_ACTIVE_USER = "activeUser";
	public static final String COLUMN_ACTIVE_USER_DATA_TYPE = "varchar";

	public static final String ActiviUser = "0517401";

	private SQLiteDatabase myDatabase;

	public APContactDB(SQLiteDatabase myDatabase) {
		this.myDatabase = myDatabase;
	}

	public static String getDeleteTableSQLString() {
		return SqlHelper.formDeleteTableSqlString(TABLE_NAME);
	}

	public static String getCreateTableString() {
		HashMap<String, String> columnNameAndType = new HashMap<String, String>();
		columnNameAndType.put(COLUMN_ID, COLUMN_ID_DATA_TYPE);
		columnNameAndType.put(COLUMN_CONTACT_NAME,
				COLUMN_CONTACT_NAME_DATA_TYPE);
		columnNameAndType.put(COLUMN_CONTACT_ID, COLUMN_CONTACT_ID_DATA_TYPE);
		columnNameAndType.put(COLUMN_CONTACT_PASSWORD,
				COLUMN_CONTACT_PASSWORD_DATA_TYPE);
		columnNameAndType.put(COLUMN_ACTIVE_USER, COLUMN_ACTIVE_USER_DATA_TYPE);
		String mSQLCreateWeiboInfoTable = SqlHelper.formCreateTableSqlString(
				TABLE_NAME, columnNameAndType);
		return mSQLCreateWeiboInfoTable;
	}

	/**
	 * 增加AP设备
	 * 
	 * @param apcontact
	 * @return
	 */
	public long insert(APContact apcontact) {
		long resultId = 0;
		if (apcontact != null) {
			ContentValues values = new ContentValues();
			values.put(COLUMN_CONTACT_NAME, apcontact.nickName);
			values.put(COLUMN_CONTACT_ID, apcontact.contactId);
			values.put(COLUMN_CONTACT_PASSWORD,
					Utils.desCrypto(apcontact.Pwd.getBytes(), "12345678"));
			values.put(COLUMN_ACTIVE_USER, ActiviUser);
			try {
				resultId = myDatabase.insertOrThrow(TABLE_NAME, null, values);
			} catch (SQLiteConstraintException e) {
				e.printStackTrace();
			}
		}
		return resultId;
	}

	/**
	 * 更新AP设备
	 * 
	 * @param apcontact
	 */
	public void update(APContact apcontact) {

		ContentValues values = new ContentValues();
		values.put(COLUMN_CONTACT_NAME, apcontact.nickName);
		values.put(COLUMN_CONTACT_ID, apcontact.contactId);
		values.put(COLUMN_CONTACT_PASSWORD,
				Utils.desCrypto(apcontact.Pwd.getBytes(), "12345678"));
		values.put(COLUMN_ACTIVE_USER, ActiviUser);
		try {
			myDatabase.update(TABLE_NAME, values, COLUMN_ACTIVE_USER
					+ "=? AND " + COLUMN_CONTACT_ID + "=?", new String[] {
					ActiviUser, apcontact.contactId });
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 查找AP设备
	 * 
	 * @param activiUser
	 * @param contactId
	 * @return
	 */
	public APContact findAPContatctByAPname(String activiUser, String apname) {
		APContact apcontact = null;
		Cursor cursor = null;
		cursor = myDatabase.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ COLUMN_ACTIVE_USER + "=? AND " + COLUMN_CONTACT_ID + "=?",
				new String[] { ActiviUser, apname });
		if (cursor != null) {
			while (cursor.moveToNext()) {
				apcontact = new APContact();
				int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
				String contactName = cursor.getString(cursor
						.getColumnIndex(COLUMN_CONTACT_NAME));
				String contactId = cursor.getString(cursor
						.getColumnIndex(COLUMN_CONTACT_ID));
				byte[] contactPassword = cursor.getBlob(cursor
						.getColumnIndex(COLUMN_CONTACT_PASSWORD));
				String activeUser = cursor.getString(cursor
						.getColumnIndex(COLUMN_ACTIVE_USER));
				apcontact.id = id;
				apcontact.nickName = contactName;
				apcontact.contactId = contactId;
				try {
					apcontact.Pwd = Utils.decrypt(contactPassword, "12345678");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				apcontact.activeUser = ActiviUser;
			}
			cursor.close();

		}
		return apcontact;
	}

	/**
	 * 删除AP设备
	 * 
	 * @param activeUserId
	 * @param contactId
	 * @return
	 */
	public int deleteByActiveUserIdAndContactId(String activeUserId,
			String contactId) {
		return myDatabase.delete(TABLE_NAME, COLUMN_ACTIVE_USER + "=?"
				+ " AND " + COLUMN_CONTACT_ID + "=?", new String[] {
				ActiviUser, contactId });
	}
}
