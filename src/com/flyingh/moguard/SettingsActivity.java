package com.flyingh.moguard;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import com.flyingh.moguard.util.Const;
import com.flyingh.moguard.util.LocaleUtils;

public class SettingsActivity extends Activity {
	private static final int DEFAULT = 2;
	private SharedPreferences sp;
	private RadioButton chineseRadioButton;
	private RadioButton englishRadioButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		sp = getSharedPreferences(Const.CONFIG_FILE_NAME, MODE_PRIVATE);
		chineseRadioButton = (RadioButton) findViewById(R.id.chinese);
		englishRadioButton = (RadioButton) findViewById(R.id.english);
		setRadioButtonState();
	}

	private void setRadioButtonState() {
		int language = sp.getInt(Const.LANGUAGE, DEFAULT);
		if (language == DEFAULT) {
			chineseRadioButton.setChecked(false);
			englishRadioButton.setChecked(false);
		} else {
			chineseRadioButton.setChecked(language == 0);
			englishRadioButton.setChecked(language == 1);
		}
	}

	public void changeLang(View view) {
		int position = getPosition(view.getId());
		if (hasChanged(position)) {
			showConfirmDialog(position);
		}
	}

	private boolean hasChanged(int position) {
		return sp.getInt(Const.LANGUAGE, DEFAULT) != position;
	}

	private int getPosition(int checkedRadioButtonId) {
		return checkedRadioButtonId == R.id.chinese ? 0 : 1;
	}

	private void showConfirmDialog(final int position) {
		new AlertDialog.Builder(SettingsActivity.this).setTitle(R.string.confirm_).setMessage(R.string.confirm_to_change_the_display_language_)
				.setPositiveButton(R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						sp.edit().putInt(Const.LANGUAGE, position).commit();
						LocaleUtils.changeLocale(SettingsActivity.this);
						ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
						am.killBackgroundProcesses(getPackageName());
						Intent intent = new Intent(SettingsActivity.this, SplashActivity.class);
						startActivity(intent);
					}
				}).setNegativeButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						setRadioButtonState();
					}
				}).show();
	}

}
