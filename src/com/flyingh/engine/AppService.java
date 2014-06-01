package com.flyingh.engine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.RemoteException;
import android.text.format.Formatter;
import android.util.Log;

import com.flyingh.moguard.AppManagerActivity.AppMode;
import com.flyingh.moguard.util.Const;
import com.flyingh.vo.App;

public class AppService {
	private static final String TAG = "AppService";

	public static List<App> loadApps(final Context context) {

		SharedPreferences sp = context.getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE);
		int appModeIndex = sp.getInt(Const.APP_MODE, 0);
		final PackageManager packageManager = context.getPackageManager();
		List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		final List<App> apps = new ArrayList<>(installedApplications.size());
		for (final ApplicationInfo applicationInfo : installedApplications) {
			Drawable icon = applicationInfo.loadIcon(packageManager);
			String label = applicationInfo.loadLabel(packageManager).toString();
			String packageName = applicationInfo.packageName;
			if (AppMode.USER.ordinal() == appModeIndex && isSystemApp(applicationInfo) || AppMode.SYSTEM.ordinal() == appModeIndex
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
						app.setTotalSize(getTotalSize(pStats));
					}

					@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
					private String getTotalSize(PackageStats pStats) {
						long totalSize = 0;
						totalSize += pStats.cacheSize;
						totalSize += pStats.codeSize;
						totalSize += pStats.dataSize;
						totalSize += pStats.externalCacheSize;
						totalSize += pStats.externalCodeSize;
						totalSize += pStats.externalDataSize;
						totalSize += pStats.externalMediaSize;
						totalSize += pStats.externalObbSize;
						return Formatter.formatFileSize(context, totalSize);
					}

				});
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
				Log.i(TAG, e.getMessage());
				throw new RuntimeException(e);
			}
		}
		Collections.sort(apps);
		return apps;
	}

	public static boolean isSystemApp(ApplicationInfo applicationInfo) {
		return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
	}
}
