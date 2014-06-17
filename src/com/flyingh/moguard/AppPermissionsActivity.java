package com.flyingh.moguard;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.flyingh.moguard.app.MoGuardApp;

public class AppPermissionsActivity extends Activity {
	private static final String TAG = "AppPermissionsActivity";

	private TextView appNameTextView;
	private TextView packageNameTextView;
	private ScrollView scrollView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_permissions);
		appNameTextView = (TextView) findViewById(R.id.appNameTextView);
		packageNameTextView = (TextView) findViewById(R.id.packageNameTextView);
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		showUI();
	}

	private void showUI() {
		try {
			MoGuardApp application = (MoGuardApp) getApplication();
			appNameTextView.setText(application.process.getApp().getLabel());
			packageNameTextView.setText(application.process.getApp().getPackageName());

			Class<?> cls = Class.forName("android.widget.AppSecurityPermissions");
			Constructor<?> constructor = cls.getDeclaredConstructor(Context.class, String.class);
			constructor.setAccessible(true);
			Object newInstance = constructor.newInstance(this, application.process.getApp().getPackageName());
			Method method = cls.getDeclaredMethod("getPermissionsView");
			method.setAccessible(true);
			View view = (View) method.invoke(newInstance);
			scrollView.addView(view);
			application.process = null;
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			throw new RuntimeException(e);
		}
	}

}
