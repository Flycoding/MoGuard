package com.flyingh.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.flyingh.db.BlacklistOpenHelper;

public class BlacklistDao {
	private final BlacklistOpenHelper helper;

	public BlacklistDao(Context context) {
		super();
		helper = new BlacklistOpenHelper(context);
	}

	public boolean add(String number) {
		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			if (isExists(number)) {
				return false;
			}
			db.execSQL("insert into blacklist(number) values(?)", new String[] { number });
			return true;
		} finally {
			db.close();
		}
	}

	public boolean isExists(String number) {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from blacklist where number=?", new String[] { number });
		try {
			return cursor.moveToFirst();
		} finally {
			cursor.close();
		}
	}

	public void delete(String number) {
		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			db.execSQL("delete from blacklist where number=?", new String[] { number });
		} finally {
			db.close();
		}
	}

	public boolean update(String oldNumber, String newNumber) {
		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			if (isExists(newNumber)) {
				return false;
			}
			db.execSQL("update blacklist set number=? where number=?", new String[] { newNumber, oldNumber });
			return true;
		} finally {
			db.close();
		}
	}

	public Cursor queryAll() {
		return helper.getReadableDatabase().rawQuery("select * from blacklist", null);
	}

}
