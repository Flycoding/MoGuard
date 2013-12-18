package com.flyingh.moguard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingh.engine.UpdateInfoService;
import com.flyingh.vo.UpdateInfo;

public class SplashActivity extends Activity {
	private static final String TAG = "SplashActivity";
	private TextView versionNameTextView;
	private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		versionNameTextView = (TextView) findViewById(R.id.version_name);
		try {
			final PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionNameTextView.setText(packageInfo.versionName);
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						final UpdateInfo info = new UpdateInfoService(SplashActivity.this).getUpdateInfo();
						if (needUpdate(info.getVersion(), packageInfo.versionCode)) {
							Log.i(TAG, "need update!");
							handler.post(new Runnable() {

								@Override
								public void run() {
									Toast.makeText(SplashActivity.this, "need update", Toast.LENGTH_SHORT).show();
									new AlertDialog.Builder(SplashActivity.this).setIcon(R.drawable.ic_launcher).setTitle("confirm to update?")
											.setMessage(info.getDescription()).setCancelable(false).setPositiveButton("OK", new OnClickListener() {

												@Override
												public void onClick(DialogInterface dialog, int which) {
													Log.i(TAG, "to download the apk...");
												}
											}).setNegativeButton("Cancel", new OnClickListener() {

												@Override
												public void onClick(DialogInterface dialog, int which) {
													enter();
												}

											}).show();
								}
							});
						} else {
							Log.i(TAG, "no need to update,enter!");
						}
					} catch (Exception e) {
						Log.i(TAG, e.getMessage());
						handler.post(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(SplashActivity.this, R.string.get_update_information_failed, Toast.LENGTH_SHORT).show();
							}
						});
					}
				}

				private boolean needUpdate(String version, int versionCode) {
					return version.compareTo(String.valueOf(versionCode)) > 0;
				}
			}).start();
		} catch (NameNotFoundException e) {
			Log.i(TAG, e.getMessage());
		}
	}

	public void enter() {
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
