package com.flyingh.moguard;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.flyingh.moguard.util.Const;

public class TaskManagerSettingsActivity extends Activity {

	private TextView showDisplayModeTextView;
	private TextView showCleanModeTextView;
	private SharedPreferences sp;
	private CheckBox displayModeCheckBox;
	private CheckBox cleanModeCheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_manager_settings);
		sp = getSharedPreferences(Const.CONFIG_FILE_NAME, MODE_PRIVATE);
		showDisplayModeTextView = (TextView) findViewById(R.id.showDisplayModeTextView);
		showDisplayModeTextView.setText(getDisplayModeInfo(sp.getBoolean(Const.SHOW_SYSTEM_APP, true)));
		displayModeCheckBox = (CheckBox) findViewById(R.id.displayModeCheckBox);
		displayModeCheckBox.setChecked(sp.getBoolean(Const.SHOW_SYSTEM_APP, true));
		showCleanModeTextView = (TextView) findViewById(R.id.showCleanModeTextView);
		showCleanModeTextView.setText(getCleanModeInfo(sp.getBoolean(Const.CLEAN_WHEN_LOCK, false)));
		cleanModeCheckBox = (CheckBox) findViewById(R.id.cleanModeCheckBox);
		cleanModeCheckBox.setChecked(sp.getBoolean(Const.CLEAN_WHEN_LOCK, false));
	}

	public void showSystemAppOrNot(View view) {
		CheckBox checkBox = (CheckBox) view;
		boolean checked = checkBox.isChecked();
		showDisplayModeTextView.setText(getDisplayModeInfo(checked));
		sp.edit().putBoolean(Const.SHOW_SYSTEM_APP, checked).commit();
	}

	private int getDisplayModeInfo(boolean checked) {
		return checked ? R.string.show_user_system_app : R.string.show_user_app_only;
	}

	public void cleanWhenLockedOrNot(View view) {
		CheckBox checkBox = (CheckBox) view;
		boolean checked = checkBox.isChecked();
		showCleanModeTextView.setText(getCleanModeInfo(checked));
		sp.edit().putBoolean(Const.CLEAN_WHEN_LOCK, checked).commit();
	}

	private int getCleanModeInfo(boolean checked) {
		return checked ? R.string.clean_when_lock_screen : R.string.don_t_clean_when_lock_screen;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.task_manager_settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
