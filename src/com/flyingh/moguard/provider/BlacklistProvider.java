package com.flyingh.moguard.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.flyingh.db.BlacklistOpenHelper;

public class BlacklistProvider extends ContentProvider {
	private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int ALL = 2;
	static {
		MATCHER.addURI("com.flyingh.moguard.blacklistprovider", "all", ALL);
	}
	private BlacklistOpenHelper helper;

	@Override
	public boolean onCreate() {
		helper = new BlacklistOpenHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		switch (MATCHER.match(uri)) {
		case ALL:
			return helper.getReadableDatabase().query(BlacklistOpenHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
		default:
			break;
		}
		return null;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			return ContentUris.withAppendedId(uri, db.insert(BlacklistOpenHelper.TABLE_NAME, null, values));
		} finally {
			db.close();
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			return db.delete(BlacklistOpenHelper.TABLE_NAME, selection, selectionArgs);
		} finally {
			db.close();
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			return db.update(BlacklistOpenHelper.TABLE_NAME, values, selection, selectionArgs);
		} finally {
			db.close();
		}
	}

}
