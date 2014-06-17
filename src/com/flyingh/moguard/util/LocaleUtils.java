package com.flyingh.moguard.util;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

public class LocaleUtils {
	public static final int DEFAULT = 2;

	public static void changeLocale(Context context) {
		Resources resources = context.getResources();
		Configuration configuration = resources.getConfiguration();
		configuration.locale = getLocale(context);
		resources.updateConfiguration(configuration, resources.getDisplayMetrics());
	}

	private static Locale getLocale(Context context) {
		SharedPreferences sp = context.getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE);
		int language = sp.getInt(Const.LANGUAGE, DEFAULT);
		return language == 0 ? Locale.CHINESE : language == 1 ? Locale.ENGLISH : Locale.getDefault();
	}
}
