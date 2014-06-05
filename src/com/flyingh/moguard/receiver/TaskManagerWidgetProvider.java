package com.flyingh.moguard.receiver;

import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

import com.flyingh.moguard.service.TaskManagerService;

public class TaskManagerWidgetProvider extends AppWidgetProvider {
	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		context.startService(new Intent(context, TaskManagerService.class));
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		context.startService(new Intent(context, TaskManagerService.class));
	}
}
