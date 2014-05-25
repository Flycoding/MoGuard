package com.flyingh.moguard.util;

import java.math.BigInteger;
import java.security.MessageDigest;

import android.util.Log;

public class StringUtils {
	private static final String TAG = "StringUtils";

	public static boolean equals(String password, String repeatPassword) {
		return password == null ? repeatPassword == null : password.equals(repeatPassword);
	}

	public static String md5(String password) {
		try {
			return String.format("%032x", new BigInteger(1, MessageDigest.getInstance("MD5").digest(password.getBytes())));
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
		}
		return null;
	}

}
