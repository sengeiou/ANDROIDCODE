package com.smartism.znzk.db.camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

public class SysMessageDB {
	public static final String TABLE_NAME = "sys_message";

	public static final String COLUMN_ID = "id";
	public static final String COLUMN_ID_DATA_TYPE = "integer PRIMARY KEY AUTOINCREMENT";

	public static final String COLUMN_MSG = "msg";
	public static final String COLUMN_MSG_DATA_TYPE = "varchar";

	public static final String COLUMN_MSG_EN = "msg_en";
	public static final String COLUMN_MSG_EN_DATA_TYPE = "varchar";

	public static final String COLUMN_MSGTIME = "msgTime";
	public static final String COLUMN_MSGTIME_DATA_TYPE = "varchar";

	public static final String COLUMN_ACTIVE_USER = "active_user";
	public static final String COLUMN_ACTIVE_USER_DATA_TYPE = "varchar";

	public static final String COLUMN_MSGSTATE = "msgState";
	public static final String COLUMN_MSGSTATE_DATA_TYPE = "integer";

	public static final String COLUMN_MSGSTYPE = "msgType";
	public static final String COLUMN_MSGSTYPE_DATA_TYPE = "integer";

	private SQLiteDatabase myDatabase;

	public SysMessageDB(SQLiteDatabase myDatabase) {
		this.myDatabase = myDatabase;
	}

	public static String getDeleteTableSQLString() {
		return SqlHelper.formDeleteTableSqlString(TABLE_NAME);
	}

	public static String getCreateTableString() {
		HashMap<String, String> columnNameAndType = new HashMap<String, String>();
		columnNameAndType.put(COLUMN_ID, COLUMN_ID_DATA_TYPE);
		columnNameAndType.put(COLUMN_MSG, COLUMN_MSG_DATA_TYPE);
		columnNameAndType.put(COLUMN_MSGTIME, COLUMN_MSGTIME_DATA_TYPE);
		columnNameAndType.put(COLUMN_ACTIVE_USER, COLUMN_ACTIVE_USER_DATA_TYPE);
		columnNameAndType.put(COLUMN_MSGSTATE, COLUMN_MSGSTATE_DATA_TYPE);
		columnNameAndType.put(COLUMN_MSGSTYPE, COLUMN_MSGSTYPE_DATA_TYPE);
		columnNameAndType.put(COLUMN_MSG_EN, COLUMN_MSG_EN_DATA_TYPE);
		String mSQLCreateWeiboInfoTable = SqlHelper.formCreateTableSqlString(
				TABLE_NAME, columnNameAndType);
		return mSQLCreateWeiboInfoTable;
	}

	public long insert(SysMessage msg) {
		long isResut = -1;
		if (msg != null) {
			ContentValues values = new ContentValues();
			values.put(COLUMN_MSG, msg.msg);
			values.put(COLUMN_MSG_EN, msg.msg_en);
			values.put(COLUMN_MSGTIME, msg.msg_time);
			values.put(COLUMN_ACTIVE_USER, msg.activeUser);
			values.put(COLUMN_MSGSTATE, msg.msgState);
			values.put(COLUMN_MSGSTYPE, msg.msgType);
			try {
				isResut = myDatabase.insertOrThrow(TABLE_NAME, null, values);
			} catch (SQLiteConstraintException e) {
				e.printStackTrace();
			}
		}

		return isResut;
	}

	public List<SysMessage> findByActiveUserId(String userId) {
		List<SysMessage> lists = new ArrayList<SysMessage>();

		Cursor cursor = null;
		cursor = myDatabase.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ COLUMN_ACTIVE_USER + "=?", new String[] { userId });

		if (cursor != null) {
			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
				String msg = cursor
						.getString(cursor.getColumnIndex(COLUMN_MSG));
				String msg_en = cursor.getString(cursor
						.getColumnIndex(COLUMN_MSG_EN));
				String msg_time = cursor.getString(cursor
						.getColumnIndex(COLUMN_MSGTIME));
				String activeUser = cursor.getString(cursor
						.getColumnIndex(COLUMN_ACTIVE_USER));
				int msgState = cursor.getInt(cursor
						.getColumnIndex(COLUMN_MSGSTATE));
				int msgType = cursor.getInt(cursor
						.getColumnIndex(COLUMN_MSGSTYPE));
				SysMessage data = new SysMessage();
				data.id = id;
				data.msg = msg;
				data.msg_en = msg_en;
				data.msg_time = msg_time;
				data.activeUser = activeUser;
				data.msgState = msgState;
				data.msgType = msgType;
				lists.add(data);
			}
			cursor.close();
		}
		return lists;
	}

	public void updateSysMsgState(int id, int state) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_MSGSTATE, state);
		try {
			myDatabase.update(TABLE_NAME, values, COLUMN_ID + "=?",
					new String[] { String.valueOf(id) });
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}

	}

	public int delete(int id) {
		return myDatabase.delete(TABLE_NAME, COLUMN_ID + "=?",
				new String[] { String.valueOf(id) });
	}
}
