package com.flyingh.vo;

import android.net.Uri;

public class AppLock {

	public static final String TABLE_NAME = "applock";
	public static final String _ID = "_id";
	public static final String PACKAGE_NAME = "package_name";
	public static final String LABEL = "label";
	public static final String DEFAULT_SORT_ORDER = "label COLLATE LOCALIZED ASC";

	public static final Uri CONTENT_URI = Uri.parse("content://com.flyingh.moguard.APP_LOCK_PROVIDER");
	public static final Uri INSERT_CONTENT_URI = Uri.parse("content://com.flyingh.moguard.APP_LOCK_PROVIDER/insert");
	public static final Uri UPDATE_CONTENT_URI = Uri.parse("content://com.flyingh.moguard.APP_LOCK_PROVIDER/update");
	public static final Uri DELETE_CONTENT_URI = Uri.parse("content://com.flyingh.moguard.APP_LOCK_PROVIDER/delete");
	public static final Uri QUERY_CONTENT_URI = Uri.parse("content://com.flyingh.moguard.APP_LOCK_PROVIDER/query");
}
