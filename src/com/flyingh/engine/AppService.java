package com.flyingh.engine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.util.Log;

import com.flyingh.moguard.AppManagerActivity.DisplayMode;
import com.flyingh.moguard.AppManagerActivity.OrderMode;
import com.flyingh.moguard.util.Const;
import com.flyingh.vo.App;
import com.flyingh.vo.AppLock;

public class AppService {
	private static final String TAG = "AppService";

	public static List<App> loadUnlockedApps(Context context) {
		Set<App> set = new HashSet<>();
		PackageManager pm = context.getPackageManager();
		List<ApplicationInfo> installedApplications = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		for (ApplicationInfo appInfo : installedApplications) {
			set.add(new App.Builder().icon(appInfo.loadIcon(pm)).label(appInfo.loadLabel(pm).toString()).packageName(appInfo.packageName).build());
		}
		set.removeAll(toApp(context.getContentResolver().query(AppLock.QUERY_CONTENT_URI, null, null, null, null)));
		ArrayList<App> result = new ArrayList<>(set);
		Collections.sort(result);
		return result;
	}

	private static List<App> toApp(Cursor cursor) {
		List<App> apps = new ArrayList<>();
		while (cursor.moveToNext()) {
			String packageName = cursor.getString(cursor.getColumnIndex(AppLock.PACKAGE_NAME));
			apps.add(new App.Builder().packageName(packageName).build());
		}
		return apps;
	}

	public static List<App> loadApps(final Context context) {
		sp = context.getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE);
		int appModeIndex = sp.getInt(Const.APP_DISPLAY_MODE, 0);
		final PackageManager packageManager = context.getPackageManager();
		List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		final List<App> apps = new ArrayList<>(installedApplications.size());
		for (final ApplicationInfo applicationInfo : installedApplications) {
			Drawable icon = applicationInfo.loadIcon(packageManager);
			String label = applicationInfo.loadLabel(packageManager).toString();
			String packageName = applicationInfo.packageName;
			if (DisplayMode.USER.ordinal() == appModeIndex && isSystemApp(applicationInfo) || DisplayMode.SYSTEM.ordinal() == appModeIndex
					&& !isSystemApp(applicationInfo)) {
				continue;
			}
			final App app = new App.Builder().icon(icon).label(label).packageName(packageName).isSystemApp(isSystemApp(applicationInfo)).build();
			apps.add(app);
			try {
				Method method = packageManager.getClass().getDeclaredMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
				method.setAccessible(true);
				method.invoke(packageManager, applicationInfo.packageName, new IPackageStatsObserver.Stub() {

					@Override
					public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
						long totalSizeLong = getTotalSizeLong(pStats);
						app.setTotalSizeLong(totalSizeLong);
						app.setTotalSize(Formatter.formatFileSize(context, totalSizeLong));
					}

					@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					private long getTotalSizeLong(PackageStats pStats) {
						long totalSizeLong = 0;
						totalSizeLong += pStats.cacheSize;
						totalSizeLong += pStats.codeSize;
						totalSizeLong += pStats.dataSize;
						totalSizeLong += pStats.externalCacheSize;
						totalSizeLong += pStats.externalCodeSize;
						totalSizeLong += pStats.externalDataSize;
						totalSizeLong += pStats.externalMediaSize;
						totalSizeLong += pStats.externalObbSize;
						return totalSizeLong;
					}

				});
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
				Log.i(TAG, e.getMessage());
				throw new RuntimeException(e);
			}
		}
		Collections.sort(apps, getComparator());
		return apps;
	}

	private static Comparator<App> getComparator() {
		String orderMode = sp.getString(Const.APP_ORDER_MODE, OrderMode.ORDER_BY_NAME.name());
		boolean orderMenuClicked = sp.getBoolean(Const.ORDER_MENU_CLICKED, false);
		sp.edit().remove(Const.ORDER_MENU_CLICKED).commit();
		if (orderByName(orderMode)) {
			return orderMenuClicked ? orderByName = Collections.reverseOrder(orderByName) : orderByName;
		} else if (orderBySize(orderMode)) {
			return orderMenuClicked ? orderBySize = Collections.reverseOrder(orderBySize) : orderBySize;
		}
		return null;
	}

	private static boolean orderBySize(String orderMode) {
		return OrderMode.ORDER_BY_SIZE.name().equals(orderMode);
	}

	private static boolean orderByName(String orderMode) {
		return OrderMode.ORDER_BY_NAME.name().equals(orderMode);
	}

	private static Comparator<App> orderByName = new Comparator<App>() {

		@Override
		public int compare(App lhs, App rhs) {
			return Collator.getInstance().compare(lhs.getLabel(), rhs.getLabel());
		}
	};
	private static Comparator<App> orderBySize = new Comparator<App>() {

		@Override
		public int compare(App lhs, App rhs) {
			long lhsSize = lhs.getTotalSizeLong();
			long rhsSize = rhs.getTotalSizeLong();
			return lhsSize < rhsSize ? -1 : lhsSize == rhsSize ? 0 : 1;
		}
	};
	private static SharedPreferences sp;

	public static boolean isSystemApp(ApplicationInfo applicationInfo) {
		return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
	}

}
