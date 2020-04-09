package com.smartism.znzk.db.camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ContactDB {
	public static final String TABLE_NAME = "contact";
	public static final String COLUMN_ID = "id";
	public static final String COLUMN_ID_DATA_TYPE = "integer PRIMARY KEY AUTOINCREMENT";

	public static final String COLUMN_CONTACT_NAME = "contactName";
	public static final String COLUMN_CONTACT_NAME_DATA_TYPE = "varchar";

	public static final String COLUMN_CONTACT_ID = "contactId";
	public static final String COLUMN_CONTACT_ID_DATA_TYPE = "varchar";

	public static final String COLUMN_CONTACT_PASSWORD = "contactPassword";
	public static final String COLUMN_CONTACT_PASSWORD_DATA_TYPE = "varchar";

	public static final String COLUMN_CONTACT_TYPE = "contactType";
	public static final String COLUMN_CONTACT_TYPE_DATA_TYPE = "integer";

	public static final String COLUMN_MESSAGE_COUNT = "messageCount";
	public static final String COLUMN_MESSAGE_COUNT_DATA_TYPE = "integer";

	public static final String COLUMN_ACTIVE_USER = "activeUser";
	public static final String COLUMN_ACTIVE_USER_DATA_TYPE = "varchar";
	
	public static final String COLUMN_ACTIVE_USERPWD = "userPwd";
	public static final String COLUMN_ACTIVE_USERPWD_DATA_TYPE = "varchar"; 

	private SQLiteDatabase myDatabase ;

	public ContactDB(SQLiteDatabase myDatabase) {
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
		columnNameAndType.put(COLUMN_CONTACT_TYPE,
				COLUMN_CONTACT_TYPE_DATA_TYPE);
		columnNameAndType.put(COLUMN_MESSAGE_COUNT,
				COLUMN_MESSAGE_COUNT_DATA_TYPE);
		columnNameAndType.put(COLUMN_ACTIVE_USER, COLUMN_ACTIVE_USER_DATA_TYPE);
		columnNameAndType.put(COLUMN_ACTIVE_USERPWD, COLUMN_ACTIVE_USERPWD_DATA_TYPE);
		String mSQLCreateWeiboInfoTable = SqlHelper.formCreateTableSqlString(
				TABLE_NAME, columnNameAndType);
		return mSQLCreateWeiboInfoTable;
	}

	public long insert(Contact contact) {
		long resultId = 0;
		if (contact != null) {
			ContentValues values = new ContentValues();
			values.put(COLUMN_CONTACT_NAME, contact.contactName);
			values.put(COLUMN_CONTACT_ID, contact.contactId);
			values.put(COLUMN_CONTACT_PASSWORD, contact.contactPassword);
			values.put(COLUMN_CONTACT_TYPE, contact.contactType);
			values.put(COLUMN_MESSAGE_COUNT, contact.messageCount);
			values.put(COLUMN_ACTIVE_USER, contact.activeUser);
			values.put(COLUMN_ACTIVE_USERPWD, contact.userPassword);
			try {
				resultId = myDatabase.insertOrThrow(TABLE_NAME, null, values);
			} catch (SQLiteConstraintException e) {
				e.printStackTrace();
			}
		}
		return resultId;
	}

	public void update(Contact contact) {

		ContentValues values = new ContentValues();
		values.put(COLUMN_CONTACT_NAME, contact.contactName);
		values.put(COLUMN_CONTACT_ID, contact.contactId);
		values.put(COLUMN_CONTACT_PASSWORD, contact.contactPassword);
		values.put(COLUMN_CONTACT_TYPE, contact.contactType);
		values.put(COLUMN_MESSAGE_COUNT, contact.messageCount);
		values.put(COLUMN_ACTIVE_USER, contact.activeUser);
		values.put(COLUMN_ACTIVE_USERPWD, contact.userPassword);
		try {
			myDatabase.update(TABLE_NAME, values, COLUMN_ACTIVE_USER
					+ "=? AND " + COLUMN_CONTACT_ID + "=?", new String[] {
					contact.activeUser, contact.contactId });
		} catch (SQLiteConstraintException e) {
			e.printStackTrace();
		}

	}

	public List<Contact> findByActiveUserId(String activeUserId) {
		List<Contact> lists = new ArrayList<Contact>();
		Cursor cursor = null;
		Log.d("leleText", "activeUserId="+activeUserId);
		cursor = myDatabase.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ COLUMN_ACTIVE_USER + "=?" + " order by " + COLUMN_CONTACT_ID,
				new String[] { activeUserId });
		if (cursor != null) {
			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
				String contactName = cursor.getString(cursor
						.getColumnIndex(COLUMN_CONTACT_NAME));
				String contactId = cursor.getString(cursor
						.getColumnIndex(COLUMN_CONTACT_ID));
				if (contactId.charAt(0) == '0') {
					continue;
				}
				String contactPassword = cursor.getString(cursor
						.getColumnIndex(COLUMN_CONTACT_PASSWORD));
				int contactType = cursor.getInt(cursor
						.getColumnIndex(COLUMN_CONTACT_TYPE));
				int messageCount = cursor.getInt(cursor
						.getColumnIndex(COLUMN_MESSAGE_COUNT));
				String activeUser = cursor.getString(cursor
						.getColumnIndex(COLUMN_ACTIVE_USER));
				String userpwd = cursor.getString(cursor
						.getColumnIndex(COLUMN_ACTIVE_USERPWD));
				Contact data = new Contact();
				data.id = id;
				data.contactName = contactName;
				data.contactId = contactId;
				data.contactPassword = contactPassword;
				data.contactType = contactType;
				data.messageCount = messageCount;
				data.activeUser = activeUser;
				data.userPassword=userpwd;
				lists.add(data);
				Log.e("dxsdoorbell", data.contactId + "type-db-->"
						+ data.contactType);
			}
			cursor.close();
		}
		return lists;
	}

	public List<Contact> findByActiveUserIdAndContactId(String activeUserId,
			String ContactId) {
		List<Contact> lists = new ArrayList<Contact>();
		Cursor cursor = null;
		cursor = myDatabase.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE "
				+ COLUMN_ACTIVE_USER + "=? AND " + COLUMN_CONTACT_ID + "=?",
				new String[] { activeUserId, ContactId });
		if (cursor != null) {
			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
				String contactName = cursor.getString(cursor
						.getColumnIndex(COLUMN_CONTACT_NAME));
				String contactId = cursor.getString(cursor
						.getColumnIndex(COLUMN_CONTACT_ID));
				String contactPassword = cursor.getString(cursor
						.getColumnIndex(COLUMN_CONTACT_PASSWORD));
				int contactType = cursor.getInt(cursor
						.getColumnIndex(COLUMN_CONTACT_TYPE));
				int messageCount = cursor.getInt(cursor
						.getColumnIndex(COLUMN_MESSAGE_COUNT));
				String activeUser = cursor.getString(cursor
						.getColumnIndex(COLUMN_ACTIVE_USER));
				String userpwd = cursor.getString(cursor
						.getColumnIndex(COLUMN_ACTIVE_USERPWD));
				Contact data = new Contact();
				data.id = id;
				data.contactName = contactName;
				data.contactId = contactId;
				data.contactPassword = contactPassword;
				data.contactType = contactType;
				data.messageCount = messageCount;
				data.activeUser = activeUser;
				data.userPassword=userpwd;
				lists.add(data);
			}
			cursor.close();
		}
		return lists;
	}

	public int deleteById(int id) {
		return myDatabase.delete(TABLE_NAME, COLUMN_ID + "=?",
				new String[] { String.valueOf(id) });
	}

	public int deleteByActiveUserIdAndContactId(String activeUserId,
			String contactId) {
		return myDatabase.delete(TABLE_NAME, COLUMN_ACTIVE_USER + "=?"
				+ " AND " + COLUMN_CONTACT_ID + "=?", new String[] {
				activeUserId, contactId });
	}
}
