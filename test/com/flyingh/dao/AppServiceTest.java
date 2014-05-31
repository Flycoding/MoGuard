package com.flyingh.dao;

import java.util.List;

import android.test.AndroidTestCase;
import android.util.Log;

import com.flyingh.engine.AppService;
import com.flyingh.vo.App;

public class AppServiceTest extends AndroidTestCase {
	private static final String TAG = "AppServiceTest";

	public void testLoadApps() {
		List<App> loadApps = AppService.loadApps(getContext());
		Log.i(TAG, String.valueOf(loadApps.size()));
		for (App app : loadApps) {
			System.out.println(app);
		}
	}

}
