package com.flyingh.moguard.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import com.flyingh.db.AppLockOpenHelper;
import com.flyingh.vo.AppLock;

public class AppLockProvider extends ContentProvider {
	private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int INSERT = 0;
	private static final int UPDATE = 1;
	private static final int DELETE = 2;
	private static final int QUERY = 3;

	private AppLockOpenHelper helper;
	static {
		MATCHER.addURI("com.flyingh.moguard.APP_LOCK_PROVIDER", "insert", INSERT);
		MATCHER.addURI("com.flyingh.moguard.APP_LOCK_PROVIDER", "update", UPDATE);
		MATCHER.addURI("com.flyingh.moguard.APP_LOCK_PROVIDER", "delete", DELETE);
		MATCHER.addURI("com.flyingh.moguard.APP_LOCK_PROVIDER", "query", QUERY);
	}

	@Override
	public boolean onCreate() {
		helper = new AppLockOpenHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		check(uri, QUERY);
		SQLiteDatabase db = helper.getReadableDatabase();
		if (TextUtils.isEmpty(sortOrder)) {
			sortOrder = AppLock.DEFAULT_SORT_ORDER;
		}
		return db.query(AppLock.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
	}

	private void check(Uri uri, int code) {
		if (MATCHER.match(uri) != code) {
			throw new IllegalArgumentException("wrong Uri:" + uri.toString());
		}
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		check(uri, INSERT);
		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			return ContentUris.withAppendedId(uri, db.insert(AppLock.TABLE_NAME, null, values));
		} finally {
			db.close();
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		check(uri, DELETE);
		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			return db.delete(AppLock.TABLE_NAME, selection, selectionArgs);
		} finally {
			db.close();
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		check(uri, UPDATE);
		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			return db.update(AppLock.TABLE_NAME, values, selection, selectionArgs);
		} finally {
			db.close();
		}
	}

}
