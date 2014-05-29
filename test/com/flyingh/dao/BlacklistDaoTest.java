package com.flyingh.dao;

import android.database.Cursor;
import android.test.AndroidTestCase;

public class BlacklistDaoTest extends AndroidTestCase {
	public void testAdd() {
		BlacklistDao dao = new BlacklistDao(getContext());
		for (int i = 0; i < 10; i++) {
			dao.add("10" + i);
		}
	}

	public void testDelete() {
		BlacklistDao dao = new BlacklistDao(getContext());
		dao.delete("15812345679");
	}

	public void testUpdate() {
		BlacklistDao dao = new BlacklistDao(getContext());
		dao.update("15812345678", "15812345688");
	}

	public void testQueryAll() {
		BlacklistDao dao = new BlacklistDao(getContext());
		Cursor cursor = dao.queryAll();
		while (cursor.moveToNext()) {
			System.out.println(cursor.getString(cursor.getColumnIndex("number")));
		}
	}

}
