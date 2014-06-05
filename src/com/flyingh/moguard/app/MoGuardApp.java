package com.flyingh.moguard.app;

import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;

import com.flyingh.moguard.receiver.ScreenOffReceiver;
import com.flyingh.vo.Process;

public class MoGuardApp extends Application {
	public Process process;

	@Override
	public void onCreate() {
		super.onCreate();
		registerReceiver(new ScreenOffReceiver(), new IntentFilter(Intent.ACTION_SCREEN_OFF));
	}
}
