package com.smartism.znzk.db.camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

public class AlarmMaskDB {
	public static final String TABLE_NAME = "alarm_mask";

	public static final String COLUMN_ID = "id";
	public static final String COLUMN_ID_DATA_TYPE = "integer PRIMARY KEY AUTOINCREMENT";

	public static final String COLUMN_DEVICEID = "deviceId";
	public static final String COLUMN_DEVICEID_DATA_TYPE = "varchar";

	public static final String COLUMN_ACTIVE_USER = "activeUser";
	public static final String COLUMN_ACTIVE_USER_DATA_TYPE = "varchar";

	private SQLiteDatabase mDBStore;

	public AlarmMaskDB(SQLiteDatabase store) {
		mDBStore = store;
	}

	public static String getDeleteTableSQLString() {
		return SqlHelper.formDeleteTableSqlString(TABLE_NAME);
	}

	public static String getCreateTableString() {
		HashMap<String, String> columnNameAndType = new HashMap<String, String>();
		columnNameAndType.put(COLUMN_ID, COLUMN_ID_DATA_TYPE);
		columnNameAndType.put(COLUMN_DEVICEID, COLUMN_DEVICEID_DATA_TYPE);
		columnNameAndType.put(COLUMN_ACTIVE_USER, COLUMN_ACTIVE_USER_DATA_TYPE);
		String mSQLCreateWeiboInfoTable = SqlHelper.formCreateTableSqlString(
				TABLE_NAME, columnNameAndType);
		return mSQLCreateWeiboInfoTable;
	}

	public long insert(AlarmMask alarmMask) {
		long isResut = -1;
		if (alarmMask != null) {
			ContentValues values = new ContentValues();
			values.put(COLUMN_DEVICEID, alarmMask.deviceId);// friend.getId()
			values.put(COLUMN_ACTIVE_USER, alarmMask.activeUser);
			try {// mDBStore.insertOrThrow(TABLE_NAME, null, newInfoValues);
				isResut = mDBStore.insertOrThrow(TABLE_NAME, "", values);
			} catch (SQLiteConstraintException e) {
				e.printStackTrace();
			}
		}
		return isResut;
	}

	public List<AlarmMask> findByActiveUserId(String activeUserId) {
		List<AlarmMask> lists = new ArrayList<AlarmMask>();
		Cursor cursor = null;
		cursor = mDBStore.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ COLUMN_ACTIVE_USER + "=?", new String[] { activeUserId });
		if (cursor != null) {
			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
				String deviceId = cursor.getString(cursor
						.getColumnIndex(COLUMN_DEVICEID));
				String activeUser = cursor.getString(cursor
						.getColumnIndex(COLUMN_ACTIVE_USER));
				AlarmMask data = new AlarmMask();
				data.id = id;
				data.deviceId = deviceId;
				data.activeUser = activeUser;
				lists.add(data);
			}
			cursor.close();
		}
		return lists;
	}

	public int deleteByActiveUserAndDeviceId(String activeUserId,
			String deviceId) {
		return mDBStore.delete(TABLE_NAME, COLUMN_ACTIVE_USER + "=? AND "
				+ COLUMN_DEVICEID + "=?",
				new String[] { activeUserId, deviceId });
	}
}
