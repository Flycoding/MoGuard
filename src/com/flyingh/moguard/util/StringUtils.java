package com.flyingh.moguard.util;

import java.security.MessageDigest;

import android.util.Base64;
import android.util.Log;

public class StringUtils {
	private static final String TAG = "StringUtils";

	public static boolean equals(String password, String repeatPassword) {
		return password == null ? repeatPassword == null : password.equals(repeatPassword);
	}

	public static String md5(String password) {
		try {
			return Base64.encodeToString(MessageDigest.getInstance("MD5").digest(password.getBytes("UTF-8")), Base64.DEFAULT);
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
		}
		return null;
	}

}
