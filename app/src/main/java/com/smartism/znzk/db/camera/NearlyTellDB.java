package com.smartism.znzk.db.camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

public class NearlyTellDB {
	public static final String TABLE_NAME = "nearly_tell";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_ID_DATA_TYPE = "integer PRIMARY KEY AUTOINCREMENT";

	public static final String COLUMN_TELL_ID = "tellId";
	public static final String COLUMN_TELL_ID_DATA_TYPE = "varchar";

	public static final String COLUMN_TELL_TYPE = "tellType";
	public static final String COLUMN_TELL_TYPE_DATA_TYPE = "varchar";

	public static final String COLUMN_TELL_STATE = "tellState";
	public static final String COLUMN_TELL_STATE_DATA_TYPE = "varchar";

	public static final String COLUMN_TELL_TIME = "tellTime";
	public static final String COLUMN_TELL_TIME_DATA_TYPE = "varchar";

	public static final String COLUMN_ACTIVE_USER = "activeUser";
	public static final String COLUMN_ACTIVE_USER_DATA_TYPE = "varchar";

	private SQLiteDatabase mDBStore;

	public NearlyTellDB(SQLiteDatabase store) {
		mDBStore = store;
	}

	public static String getDeleteTableSQLString() {
		return SqlHelper.formDeleteTableSqlString(TABLE_NAME);
	}

	public static String getCreateTableString() {
		HashMap<String, String> columnNameAndType = new HashMap<String, String>();
		columnNameAndType.put(COLUMN_ID, COLUMN_ID_DATA_TYPE);
		columnNameAndType.put(COLUMN_TELL_ID, COLUMN_TELL_ID_DATA_TYPE);
		columnNameAndType.put(COLUMN_TELL_STATE, COLUMN_TELL_STATE_DATA_TYPE);
		columnNameAndType.put(COLUMN_TELL_TYPE, COLUMN_TELL_TYPE_DATA_TYPE);
		columnNameAndType.put(COLUMN_TELL_TIME, COLUMN_TELL_TIME_DATA_TYPE);
		columnNameAndType.put(COLUMN_ACTIVE_USER, COLUMN_ACTIVE_USER_DATA_TYPE);
		String mSQLCreateWeiboInfoTable = SqlHelper.formCreateTableSqlString(
				TABLE_NAME, columnNameAndType);
		return mSQLCreateWeiboInfoTable;
	}

	public long insert(NearlyTell nearlyTell) {
		long isResut = -1;
		if (nearlyTell != null) {
			ContentValues values = new ContentValues();
			values.put(COLUMN_TELL_ID, nearlyTell.tellId);
			values.put(COLUMN_TELL_STATE, nearlyTell.tellState);
			values.put(COLUMN_TELL_TYPE, nearlyTell.tellType);
			values.put(COLUMN_TELL_TIME, nearlyTell.tellTime);
			values.put(COLUMN_ACTIVE_USER, nearlyTell.activeUser);
			try {
				isResut = mDBStore.insertOrThrow(TABLE_NAME, null, values);
			} catch (SQLiteConstraintException e) {
				e.printStackTrace();
			}
		}
		return isResut;
	}

	public List<NearlyTell> findByActiveUserId(String activeUserId) {
		List<NearlyTell> lists = new ArrayList<NearlyTell>();
		Cursor cursor = null;
		cursor = mDBStore.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ COLUMN_ACTIVE_USER + "=?", new String[] { activeUserId });
		if (cursor != null) {
			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
				String tellId = cursor.getString(cursor
						.getColumnIndex(COLUMN_TELL_ID));
				int tellType = cursor.getInt(cursor
						.getColumnIndex(COLUMN_TELL_TYPE));
				int tellState = cursor.getInt(cursor
						.getColumnIndex(COLUMN_TELL_STATE));
				String tellTime = cursor.getString(cursor
						.getColumnIndex(COLUMN_TELL_TIME));
				String activeUser = cursor.getString(cursor
						.getColumnIndex(COLUMN_ACTIVE_USER));
				NearlyTell data = new NearlyTell();
				data.id = id;
				data.tellId = tellId;
				data.tellState = tellState;
				data.tellType = tellType;
				data.tellTime = tellTime;
				data.activeUser = activeUser;
				lists.add(data);
			}
			cursor.close();
		}
		return lists;
	}

	public List<NearlyTell> findByActiveUserIdAndTellId(String activeUserId,
			String tell) {
		List<NearlyTell> lists = new ArrayList<NearlyTell>();
		Cursor cursor = null;
		cursor = mDBStore.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ COLUMN_ACTIVE_USER + "=? AND " + COLUMN_TELL_ID + "=?",
				new String[] { activeUserId, tell });
		if (cursor != null) {
			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
				String tellId = cursor.getString(cursor
						.getColumnIndex(COLUMN_TELL_ID));
				int tellType = cursor.getInt(cursor
						.getColumnIndex(COLUMN_TELL_TYPE));
				int tellState = cursor.getInt(cursor
						.getColumnIndex(COLUMN_TELL_STATE));
				String tellTime = cursor.getString(cursor
						.getColumnIndex(COLUMN_TELL_TIME));
				String activeUser = cursor.getString(cursor
						.getColumnIndex(COLUMN_ACTIVE_USER));
				NearlyTell data = new NearlyTell();
				data.id = id;
				data.tellId = tellId;
				data.tellState = tellState;
				data.tellType = tellType;
				data.tellTime = tellTime;
				data.activeUser = activeUser;
				lists.add(data);
			}
			cursor.close();
		}
		return lists;
	}

	public int deleteByActiveUserId(String activeUserId) {
		return mDBStore.delete(TABLE_NAME, COLUMN_ACTIVE_USER + "=?",
				new String[] { activeUserId });
	}

	public int deleteById(int id) {
		return mDBStore.delete(TABLE_NAME, COLUMN_ID + "=?",
				new String[] { String.valueOf(id) });
	}

	public int deleteByTellId(String tellId) {
		return mDBStore.delete(TABLE_NAME, COLUMN_TELL_ID + "=?",
				new String[] { tellId });
	}
}
