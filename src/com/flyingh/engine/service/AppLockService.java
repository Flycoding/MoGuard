package com.flyingh.engine.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.flyingh.engine.AppService;
import com.flyingh.engine.ILockService;
import com.flyingh.moguard.LockScreenActivity;
import com.flyingh.vo.AppLock;

public class AppLockService extends Service {
	public static final String EXTRA_PACKAGE_NAME = "packageName";
	private static final String TAG = "AppLockService";
	private Set<String> lockedPackageNames;
	private final Set<String> tmpNotLockedPackageNames = new HashSet<>();

	@Override
	public IBinder onBind(Intent intent) {
		return new AppLockBinder();
	}

	class AppLockBinder extends Binder implements ILockService {

		@Override
		public void unlock(String packageName) {
			tmpNotLockedPackageNames.add(packageName);
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		lockedPackageNames = AppService.loadLockedPackageNames(this);
		getContentResolver().registerContentObserver(AppLock.CONTENT_URI, true, new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				lockedPackageNames = AppService.loadLockedPackageNames(AppLockService.this);
			}
		});
		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				Log.i(TAG, "here");
				String packageName = getTopPackageName();
				KeyguardManager keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
				if (keyguardManager.inKeyguardRestrictedInputMode()) {
					tmpNotLockedPackageNames.clear();
				}
				if (lockedPackageNames.contains(packageName) && !tmpNotLockedPackageNames.contains(packageName)) {
					Intent intent = new Intent(AppLockService.this, LockScreenActivity.class);
					intent.putExtra(EXTRA_PACKAGE_NAME, packageName);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
			}

			private String getTopPackageName() {
				ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
				List<RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);
				RunningTaskInfo runningTaskInfo = runningTasks.get(0);
				return runningTaskInfo.topActivity.getPackageName();
			}
		}, 0, 500, TimeUnit.MILLISECONDS);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}
