package com.flyingh.moguard;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.flyingh.moguard.util.Const;
import com.flyingh.moguard.util.StringUtils;

public class SecurityActivity extends Activity {
	private static final String TAG = "SecurityActivity";
	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE);
		if (hasSecurityPasswordSetted()) {
			Log.i(TAG, "enter password to enter");
			showInputSecurityPasswordDialog();
		} else {
			Log.i(TAG, "set security password");
			showSetSecurityPasswordDialog();
		}
	}

	private boolean hasWizardUsed() {
		return sp.getBoolean(Const.SECURITY_WIZARD_USED, false);
	}

	private void showInputSecurityPasswordDialog() {
		final Dialog dialog = new Dialog(this, R.style.SecurityDialogTheme);
		final View view = View.inflate(this, R.layout.input_security_password_dialog_view, null);
		dialog.setContentView(view);
		Button cancelInputButton = (Button) view.findViewById(R.id.cancel_input_password);
		cancelInputButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				finish();
			}
		});
		Button confirmInputButton = (Button) view.findViewById(R.id.confirm_input_password);
		confirmInputButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText passwordEditText = (EditText) view.findViewById(R.id.input_password);
				String password = passwordEditText.getText().toString().trim();
				if (!StringUtils.equals(StringUtils.md5(password), sp.getString(Const.SECURITY_PASSWORD, null))) {
					Toast.makeText(SecurityActivity.this, R.string.password_is_wrong, Toast.LENGTH_SHORT).show();
					return;
				}
				dialog.dismiss();
				if (hasWizardUsed()) {
					Log.i(TAG, "wizard used!");
					// TODO:
				} else {
					Log.i(TAG, "wizard not used!");
					finish();
					startActivity(new Intent(SecurityActivity.this, SecurityWizardActivity.class));
				}
			}
		});
		dialog.setCancelable(false);
		dialog.show();
	}

	private void showSetSecurityPasswordDialog() {
		final Dialog dialog = new Dialog(this, R.style.SecurityDialogTheme);
		final View view = View.inflate(this, R.layout.set_security_password_dialog_view, null);
		dialog.setContentView(view);
		Button confirmButton = (Button) view.findViewById(R.id.confirm_set_password);
		confirmButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText passwordEditText = (EditText) view.findViewById(R.id.password);
				EditText repeatPasswordEditText = (EditText) view.findViewById(R.id.repeat_password);
				String password = passwordEditText.getText().toString().trim();
				String repeatPassword = repeatPasswordEditText.getText().toString().trim();
				if (TextUtils.isEmpty(password) || TextUtils.isEmpty(repeatPassword)) {
					Toast.makeText(SecurityActivity.this, R.string.the_password_or_the_repeat_password_should_not_be_empty, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				if (!StringUtils.equals(password, repeatPassword)) {
					Toast.makeText(SecurityActivity.this, R.string.the_password_and_the_repeat_password_are_not_the_same, Toast.LENGTH_SHORT).show();
					return;
				}
				sp.edit().putString(Const.SECURITY_PASSWORD, StringUtils.md5(password)).commit();
				dialog.dismiss();
				finish();
			}
		});
		Button cancelButton = (Button) view.findViewById(R.id.cancel_set_password);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
				finish();
			}
		});
		dialog.setCancelable(false);
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
