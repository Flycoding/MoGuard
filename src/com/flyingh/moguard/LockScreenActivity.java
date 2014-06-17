package com.flyingh.moguard;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.flyingh.engine.ILockService;
import com.flyingh.moguard.service.AppLockService;
import com.flyingh.moguard.util.Const;
import com.flyingh.moguard.util.StringUtils;

public class LockScreenActivity extends Activity {
	private static final String TAG = "LockScreenActivity";

	private TextView lockedAppTextView;
	private EditText lockPasswordEditText;
	private SharedPreferences sp;
	private String packageName;
	private ILockService lockService;

	private ServiceConnection conn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_lock_screen);
		conn = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				lockService = (ILockService) service;
			}
		};
		bindService(new Intent(this, AppLockService.class), conn, BIND_AUTO_CREATE);
		sp = getSharedPreferences(Const.CONFIG_FILE_NAME, MODE_PRIVATE);
		lockedAppTextView = (TextView) findViewById(R.id.lockedAppTextView);
		lockPasswordEditText = (EditText) findViewById(R.id.lockPasswordEditText);
		try {
			Intent intent = getIntent();
			packageName = intent.getStringExtra(AppLockService.EXTRA_PACKAGE_NAME);
			ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			lockedAppTextView.setText(applicationInfo.loadLabel(getPackageManager()));
			lockedAppTextView.setCompoundDrawablesWithIntrinsicBounds(null, applicationInfo.loadIcon(getPackageManager()), null, getResources()
					.getDrawable(R.drawable.divide_line));
		} catch (NameNotFoundException e) {
			Log.i(TAG, e.getMessage());
			throw new RuntimeException(e);

		}
	}

	public void enter(View view) {
		String password = lockPasswordEditText.getText().toString().trim();
		if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
			lockPasswordEditText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
			Toast.makeText(this, R.string.password_is_wrong, Toast.LENGTH_SHORT).show();
			return;
		}
		lockService.unlock(packageName);
		finish();
	}

	private boolean isPasswordValid(String password) {
		return sp.getString(Const.APP_LOCK_PASSWORD, StringUtils.md5(Const.DEFAULT_APP_LOCK_PASSWORD)).equals(StringUtils.md5(password));
	}

	@Override
	protected void onDestroy() {
		unbindService(conn);
		super.onDestroy();
	}

}
