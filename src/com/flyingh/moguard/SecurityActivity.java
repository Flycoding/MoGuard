package com.flyingh.moguard;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;

import com.flyingh.moguard.util.Const;

public class SecurityActivity extends Activity {
	private static final String TAG = "SecurityActivity";
	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_security);
		sp = getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE);
		if (hasSecurityPasswordSetted()) {
			Log.i(TAG, "enter password to enter");
		} else {
			Log.i(TAG, "set security password");
			showSetSecurityPasswordDialog();
		}
	}

	private void showSetSecurityPasswordDialog() {
		Dialog dialog = new Dialog(this, R.style.SecurityDialogTheme);
		dialog.setContentView(R.layout.set_security_dialog_view);
		dialog.show();
	}

	private boolean hasSecurityPasswordSetted() {
		return sp.contains(Const.SECURITY_PASSWORD) && !TextUtils.isEmpty(sp.getString(Const.SECURITY_PASSWORD, null));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.security, menu);
		return true;
	}

}
