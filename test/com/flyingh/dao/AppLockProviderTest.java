package com.flyingh.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import com.flyingh.vo.AppLock;

public class AppLockProviderTest extends AndroidTestCase {
	public void testInsert() {
		ContentValues values = new ContentValues();
		values.put(AppLock.PACKAGE_NAME, "com.flyingh.moguard");
		getContext().getContentResolver().insert(AppLock.INSERT_CONTENT_URI, values);
	}

	public void testUpdate() {
		ContentValues values = new ContentValues();
		values.put(AppLock.PACKAGE_NAME, "com.flyingh.app");
		getContext().getContentResolver().update(AppLock.UPDATE_CONTENT_URI, values, AppLock.PACKAGE_NAME + "=?",
				new String[] { "com.flyingh.hello" });
	}

	public void testQuery() {
		Cursor cursor = getContext().getContentResolver().query(AppLock.QUERY_CONTENT_URI, null, AppLock.PACKAGE_NAME + "=?",
				new String[] { "com.flyingh.app" }, null);
		if (cursor.moveToFirst()) {
			System.out.println(cursor.getString(cursor.getColumnIndex(AppLock._ID)));
			System.out.println(cursor.getString(cursor.getColumnIndex(AppLock.PACKAGE_NAME)));
		}
	}

	public void testDelete() {
		int number = getContext().getContentResolver().delete(AppLock.DELETE_CONTENT_URI, null, null);
		System.out.println(number);
	}

}
