package com.flyingh.moguard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.flyingh.engine.service.PhoneNumberAttributionService;
import com.flyingh.moguard.util.Const;

public class AdvancedToolsActivity extends Activity {

	private TextView serviceStatusTextView;
	private CheckBox startOrNotCheckBox;
	private TextView showInfoTextView;
	private CheckBox showDetailOrNotCheckBox;
	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_advanced_tools);
		sp = getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE);
		serviceStatusTextView = (TextView) findViewById(R.id.serviceStatusTextView);
		startOrNotCheckBox = (CheckBox) findViewById(R.id.startOrNotCheckBox);
		startOrNotCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				sp.edit().putBoolean(Const.PHONE_NUMBER_ATTRIBUTION_SERVICE_STARTED, isChecked).commit();
				serviceStatusTextView.setText(isChecked ? R.string.service_started : R.string.service_not_starts);
				startOrNotCheckBox.setChecked(isChecked);
				Intent service = new Intent(AdvancedToolsActivity.this, PhoneNumberAttributionService.class);
				if (isChecked) {
					startService(service);
				} else {
					stopService(service);
				}
			}
		});
		if (sp.getBoolean(Const.PHONE_NUMBER_ATTRIBUTION_SERVICE_STARTED, false)) {
			serviceStatusTextView.setText(R.string.service_started);
			startOrNotCheckBox.setChecked(true);
		}
		showInfoTextView = (TextView) findViewById(R.id.showInfoTextView);
		showDetailOrNotCheckBox = (CheckBox) findViewById(R.id.showDetailOrNotCheckBox);
		showDetailOrNotCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				sp.edit().putBoolean(Const.SHOW_DEFAIL_INFO, isChecked).commit();
				showInfoTextView
						.setText(isChecked ? R.string.show_detail_information : R.string.show_short_information);
				showDetailOrNotCheckBox.setChecked(isChecked);
			}
		});
		if (sp.getBoolean(Const.SHOW_DEFAIL_INFO, false)) {
			showInfoTextView.setText(R.string.show_detail_information);
			showDetailOrNotCheckBox.setChecked(true);
		}
	}

	public void queryPhoneNumber(View view) {
		startActivity(new Intent(this, QueryPhoneNumberActivity.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.advanced_tools, menu);
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
