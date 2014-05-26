package com.flyingh.engine;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.flyingh.moguard.R;
import com.flyingh.moguard.util.Const;

public class QueryNumberService {

	private static final String TABLE_NAME = "info";
	private static final String COLUMN_MOBILEPREFIX = "mobileprefix";
	private static final String COLUMN_AREA = "area";
	private static final String COLUMN_CITY = "city";
	private static final String COLUMN_CARDTYPE = "cardtype";

	public static String query(Context context, String queryParam) {
		if (isPhoneNumber(queryParam)) {
			return queryPhoneNumber(context, queryParam);
		} else if (isAreaNumber(queryParam)) {
			return queryAreaNumber(context, queryParam);
		}
		return null;
	}

	private static String queryAreaNumber(Context context, String queryParam) {
		SQLiteDatabase db = openDbReadOnly();
		Cursor cursor = db.query(TABLE_NAME, new String[] { COLUMN_CITY }, COLUMN_AREA + "=?",
				new String[] { queryParam }, null, null, null);
		StringBuilder sb = new StringBuilder();
		if (cursor.moveToFirst()) {
			sb.append(context.getString(R.string.city_)).append(cursor.getString(cursor.getColumnIndex(COLUMN_CITY)));
		}
		cursor.close();
		db.close();
		return sb.toString();
	}

	private static String queryPhoneNumber(Context context, String queryParam) {
		SQLiteDatabase db = openDbReadOnly();
		Cursor cursor = db.query(TABLE_NAME, new String[] { COLUMN_AREA, COLUMN_CITY, COLUMN_CARDTYPE },
				COLUMN_MOBILEPREFIX + "=?", new String[] { queryParam.substring(0, Const.MIN_PHONE_NUMBER_PREFIX) },
				null, null, null);
		StringBuilder sb = new StringBuilder();
		if (cursor.moveToFirst()) {
			sb.append(context.getString(R.string.area_)).append(cursor.getString(cursor.getColumnIndex(COLUMN_AREA)))
					.append("\n");
			sb.append(context.getString(R.string.city_)).append(cursor.getString(cursor.getColumnIndex(COLUMN_CITY)))
					.append("\n");
			sb.append(context.getString(R.string.cardtype_)).append(
					cursor.getString(cursor.getColumnIndex(COLUMN_CARDTYPE)));
		}
		cursor.close();
		db.close();
		return sb.toString();
	}

	private static SQLiteDatabase openDbReadOnly() {
		return SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + "/" + Const.ADDRESS_DB_NAME,
				null, SQLiteDatabase.OPEN_READONLY);
	}

	private static boolean isAreaNumber(String queryParam) {
		return queryParam.matches("\\d{3,4}");
	}

	private static boolean isPhoneNumber(String queryParam) {
		return queryParam.matches("1[34578]\\d{5,9}");
	}

}
