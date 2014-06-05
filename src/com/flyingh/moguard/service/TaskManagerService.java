package com.flyingh.moguard.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.text.format.Formatter;
import android.widget.RemoteViews;

import com.flyingh.moguard.R;
import com.flyingh.moguard.receiver.TaskManagerWidgetProvider;

public class TaskManagerService extends Service {

	private ActivityManager am;
	private ScheduledExecutorService executorService;

	@Override
	public void onCreate() {
		super.onCreate();
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		executorService = Executors.newScheduledThreadPool(1);
		executorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				RemoteViews views = new RemoteViews(getPackageName(), R.layout.layout_task_manager_widget);
				views.setTextViewText(R.id.processCountTextView, getProcessCount());
				views.setTextViewText(R.id.memoryTextView, getAvailableMemory());
				views.setOnClickPendingIntent(R.id.cleanButton, PendingIntent.getService(TaskManagerService.this, 0, new Intent(
						TaskManagerService.this, KillBackgroundProcessesService.class), PendingIntent.FLAG_UPDATE_CURRENT));
				AppWidgetManager.getInstance(TaskManagerService.this).updateAppWidget(
						new ComponentName(TaskManagerService.this, TaskManagerWidgetProvider.class), views);

			}
		}, 0, 1, TimeUnit.SECONDS);
	}

	protected CharSequence getAvailableMemory() {
		MemoryInfo outInfo = new MemoryInfo();
		am.getMemoryInfo(outInfo);
		return Formatter.formatFileSize(this, outInfo.availMem);
	}

	protected CharSequence getProcessCount() {
		return String.valueOf(am.getRunningAppProcesses().size());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		executorService.shutdown();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
