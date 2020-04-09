package com.smartism.znzk.db.camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

public class MessageDB {
	public static final String TABLE_NAME = "message";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_ID_DATA_TYPE = "integer PRIMARY KEY AUTOINCREMENT";
	public static final String COLUMN_FROMID = "fromId";
	public static final String COLUMN_FROMID_DATA_TYPE = "varchar";
	public static final String COLUMN_TOID = "toId";
	public static final String COLUMN_TOID_DATA_TYPE = "varchar";
	public static final String COLUMN_MSG = "msg";
	public static final String COLUMN_MSG_DATA_TYPE = "varchar";
	public static final String COLUMN_MSGTIME = "msgTime";
	public static final String COLUMN_MSGTIME_DATA_TYPE = "varchar";
	public static final String COLUMN_ACTIVE_USER = "active_user";
	public static final String COLUMN_ACTIVE_USER_DATA_TYPE = "varchar";
	public static final String COLUMN_MSG_STATE = "msg_state";
	public static final String COLUMN_MSG_STATE_DATA_TYPE = "varchar";
	public static final String COLUMN_MSG_FLAG = "msg_flag";
	public static final String COLUMN_MSG_FLAG_DATA_TYPE = "varchar";
	private SQLiteDatabase myDatabase;

	public MessageDB(SQLiteDatabase myDatabase) {
		this.myDatabase = myDatabase;
	}

	public static String getDeleteTableSQLString() {
		return SqlHelper.formDeleteTableSqlString(TABLE_NAME);
	}

	public static String getCreateTableString() {
		HashMap<String, String> columnNameAndType = new HashMap<String, String>();
		columnNameAndType.put(COLUMN_ID, COLUMN_ID_DATA_TYPE);
		columnNameAndType.put(COLUMN_FROMID, COLUMN_FROMID_DATA_TYPE);
		columnNameAndType.put(COLUMN_TOID, COLUMN_TOID_DATA_TYPE);
		columnNameAndType.put(COLUMN_MSG, COLUMN_MSG_DATA_TYPE);
		columnNameAndType.put(COLUMN_MSGTIME, COLUMN_MSGTIME_DATA_TYPE);
		columnNameAndType.put(COLUMN_ACTIVE_USER, COLUMN_ACTIVE_USER_DATA_TYPE);
		columnNameAndType.put(COLUMN_MSG_STATE, COLUMN_MSG_STATE_DATA_TYPE);
		columnNameAndType.put(COLUMN_MSG_FLAG, COLUMN_MSG_FLAG_DATA_TYPE);
		String mSQLCreateWeiboInfoTable = SqlHelper.formCreateTableSqlString(
				TABLE_NAME, columnNameAndType);
		return mSQLCreateWeiboInfoTable;
	}

	public long insert(Message msg) {
		long isResut = -1;
		if (msg != null) {
			ContentValues values = new ContentValues();
			values.put(COLUMN_FROMID, msg.fromId);
			values.put(COLUMN_TOID, msg.toId);
			values.put(COLUMN_MSG, msg.msg);
			values.put(COLUMN_MSGTIME, msg.msgTime);
			values.put(COLUMN_ACTIVE_USER, msg.activeUser);
			values.put(COLUMN_MSG_STATE, msg.msgState);
			values.put(COLUMN_MSG_FLAG, msg.msgFlag);
			try {
				isResut = myDatabase.insertOrThrow(TABLE_NAME, null, values);
			} catch (SQLiteConstraintException e) {
				e.printStackTrace();
			}
		}
		return isResut;
	}

	public void delete(int msgId) {
		myDatabase.delete(TABLE_NAME, COLUMN_ID + "=?",
				new String[] { String.valueOf(msgId) });
	}

	public void deleteByActiveUserAndChatId(String activeUserId, String chatId) {
		myDatabase.delete(TABLE_NAME, COLUMN_ACTIVE_USER + "=? AND "
				+ COLUMN_TOID + "=?", new String[] { activeUserId, chatId });
		myDatabase.delete(TABLE_NAME, COLUMN_ACTIVE_USER + "=? AND "
				+ COLUMN_FROMID + "=?", new String[] { activeUserId, chatId });
	}

	public void update(Message msg) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_FROMID, msg.fromId);
		values.put(COLUMN_TOID, msg.toId);
		values.put(COLUMN_MSG, msg.msg);
		values.put(COLUMN_MSGTIME, msg.msgTime);
		values.put(COLUMN_ACTIVE_USER, msg.activeUser);
		values.put(COLUMN_MSG_STATE, msg.msgState);
		values.put(COLUMN_MSG_FLAG, msg.msgFlag);
		try {// mDBStore.insertOrThrow(TABLE_NAME, null, newInfoValues);
			myDatabase.update(TABLE_NAME, values, COLUMN_ID + "=?",
					new String[] { String.valueOf(msg.id) });
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}

	}

	public void updateStateByFlag(String msgFlag, String msgState) {
		ContentValues values = new ContentValues();
		values.put(COLUMN_MSG_STATE, msgState);
		values.put(COLUMN_MSG_FLAG, "-1");
		try {
			myDatabase.update(TABLE_NAME, values, COLUMN_MSG_FLAG + "=?",
					new String[] { msgFlag });
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}
	}

	public List<Message> findMessageByActiveUserAndChatId(String activeUserId,
			String chatId) {
		List<Message> lists = new ArrayList<Message>();
		if (chatId.equals(activeUserId)) {
			return lists;
		}
		Cursor cursor = null;
		cursor = myDatabase.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ COLUMN_ACTIVE_USER + "=? AND (" + COLUMN_FROMID + "=? OR "
				+ COLUMN_TOID + "=?)", new String[] { activeUserId, chatId,
				chatId });

		if (cursor != null) {
			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
				String fromId = cursor.getString(cursor
						.getColumnIndex(COLUMN_FROMID));
				String toId = cursor.getString(cursor
						.getColumnIndex(COLUMN_TOID));
				String msg = cursor
						.getString(cursor.getColumnIndex(COLUMN_MSG));
				String msgTime = cursor.getString(cursor
						.getColumnIndex(COLUMN_MSGTIME));
				String activeUser = cursor.getString(cursor
						.getColumnIndex(COLUMN_ACTIVE_USER));
				String msgState = cursor.getString(cursor
						.getColumnIndex(COLUMN_MSG_STATE));
				String msgFlag = cursor.getString(cursor
						.getColumnIndex(COLUMN_MSG_FLAG));
				Message data = new Message();
				data.id = id;
				data.fromId = fromId;
				data.toId = toId;
				data.msg = msg;
				data.msgTime = msgTime;
				data.activeUser = activeUser;
				data.msgState = msgState;
				data.msgFlag = msgFlag;
				lists.add(data);
			}
			cursor.close();
		}
		return lists;
	}

}
