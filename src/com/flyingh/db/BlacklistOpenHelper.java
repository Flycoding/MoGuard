package com.flyingh.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BlacklistOpenHelper extends SQLiteOpenHelper {

	public static final String TABLE_NAME = "blacklist";

	public BlacklistOpenHelper(Context context) {
		super(context, "blacklist.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table blacklist(_id integer primary key autoincrement,number varchar(11))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table blacklist");
		onCreate(db);
	}

}
