package com.flyingh.moguard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
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

	public static final int DEFAULT_SELECT_BACKGROUND_ITEM_INDEX = 1;
	public static final int DEFAULT_SELECT_BACKGROUND_DRAWABLE_RES_ID = R.drawable.call_locate_orange;

	private TextView serviceStatusTextView;
	private CheckBox startOrNotCheckBox;
	private TextView showInfoTextView;
	private CheckBox showDetailOrNotCheckBox;
	private SharedPreferences sp;
	private int height;
	private int width;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_advanced_tools);
		sp = getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE);

		Point outSize = new Point();
		getWindowManager().getDefaultDisplay().getSize(outSize);
		width = outSize.x;
		height = outSize.y;

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
				sp.edit().putBoolean(Const.SHOW_DEFAIL_INFO, isChecked).putInt(Const.PHONE_NUMBER_ATTRIBUTION_WIDTH, isChecked ? width : width / 3)
						.putInt(Const.PHONE_NUMBER_ATTRIBUTION_HEIGHT, isChecked ? height / 4 : height / 8).commit();
				showInfoTextView.setText(isChecked ? R.string.show_detail_information : R.string.show_short_information);
				showDetailOrNotCheckBox.setChecked(isChecked);
			}
		});
		if (sp.getBoolean(Const.SHOW_DEFAIL_INFO, false)) {
			showInfoTextView.setText(R.string.show_detail_information);
			showDetailOrNotCheckBox.setChecked(true);
		}
	}

	public void selectPhoneAttributionStyle(View view) {
		String[] items = new String[] { getString(R.string.gray), getString(R.string.orange), getString(R.string.green), getString(R.string.blue),
				getString(R.string.white) };
		final int[] drawableIds = { R.drawable.call_locate_gray, R.drawable.call_locate_orange, R.drawable.call_locate_green,
				R.drawable.call_locate_blue, R.drawable.call_locate_white };
		new AlertDialog.Builder(this)
				.setTitle(R.string.select_phone_number_attribution_style_)
				.setSingleChoiceItems(items, sp.getInt(Const.PHONE_NUMBER_ATTRIBUTION_BACKGROUND_INDEX, DEFAULT_SELECT_BACKGROUND_ITEM_INDEX),
						new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								sp.edit().putInt(Const.PHONE_NUMBER_ATTRIBUTION_BACKGROUND_INDEX, which)
										.putInt(Const.PHONE_NUMBER_ATTRIBUTION_BACKGROUND, drawableIds[which]).commit();
								dialog.dismiss();
							}
						}).show();
	}

	public void setPhoneAttributionPosition(View view) {
		startActivity(new Intent(this, PhoneAttributionPositionActivity.class));
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
