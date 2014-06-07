package com.flyingh.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.flyingh.vo.CommonContact;
import com.flyingh.vo.CommonContactType;

public class CommonContactService {
	private static final String COMMONNUM_DB = "commonnum.db";
	private static final String TAG = "CommonContactService";

	public static Map<CommonContactType, List<CommonContact>> group(Context context) {
		Map<CommonContactType, List<CommonContact>> map = new LinkedHashMap<>();
		AssetManager assets = context.getAssets();
		try {
			File file = new File(Environment.getExternalStorageDirectory(), COMMONNUM_DB);
			if (!file.exists()) {
				copy(assets.open(COMMONNUM_DB), new FileOutputStream(file));
			}
			SQLiteDatabase db = SQLiteDatabase.openDatabase(Environment.getExternalStorageDirectory() + "/" + COMMONNUM_DB, null,
					SQLiteDatabase.OPEN_READONLY);
			Cursor cursor = db.rawQuery("SELECT * FROM classlist ORDER BY idx ASC", null);
			while (cursor.moveToNext()) {
				ArrayList<CommonContact> list = new ArrayList<CommonContact>();
				map.put(convert(cursor), list);
				Cursor subCursor = db.rawQuery("SELECT * FROM table" + (cursor.getPosition() + 1) + " ORDER BY _id", null);
				while (subCursor.moveToNext()) {
					list.add(convertToContact(subCursor));
				}
				subCursor.close();
			}
		} catch (IOException e) {
			Log.i(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
		return map;
	}

	private static CommonContact convertToContact(Cursor subCursor) {
		String id = subCursor.getString(subCursor.getColumnIndex("_id"));
		String name = subCursor.getString(subCursor.getColumnIndex("name"));
		String number = subCursor.getString(subCursor.getColumnIndex("number"));
		return new CommonContact(id, name, number);
	}

	private static CommonContactType convert(Cursor cursor) {
		String id = cursor.getString(cursor.getColumnIndex(CommonContactType._ID));
		String name = cursor.getString(cursor.getColumnIndex(CommonContactType.NAME));
		return new CommonContactType(id, name);
	}

	private static void copy(InputStream is, OutputStream os) throws IOException {
		byte[] buffer = new byte[1024 * 16];
		int len = -1;
		while ((len = is.read(buffer)) != -1) {
			os.write(buffer, 0, len);
		}
		os.close();
		is.close();
	}
}
