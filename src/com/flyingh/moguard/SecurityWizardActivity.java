package com.flyingh.moguard;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.flyingh.moguard.receiver.SecurityDeviceAdminReceiver;
import com.flyingh.moguard.util.Const;

public class SecurityWizardActivity extends Activity {
	private static final int CONTACT = 0;
	private int currentProgress = 1;
	private SharedPreferences sp;
	private EditText boundPhoneNumberEditText;
	private TextView startStatusTextView;
	private CheckBox startOrNotCheckBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.security_wizard);
		sp = getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE);
	}

	public void startOrNot(View view) {
		CheckBox checkBox = (CheckBox) view;
		startStatusTextView.setText(checkBox.isChecked() ? R.string.started : R.string.not_started);
		sp.edit().putBoolean(Const.STATUS_STARTED, checkBox.isChecked()).commit();
	}

	public void set(View view) {
		if (startOrNotCheckBox.isChecked()) {
			finishSetting();
		} else {
			new AlertDialog.Builder(this).setTitle("Confirm?").setMessage("Are you sure not to start?")
					.setPositiveButton("OK", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							finishSetting();
						}
					}).setNegativeButton("Cancel", null).show();
		}
	}

	private void finishSetting() {
		sp.edit().putBoolean(Const.SECURITY_WIZARD_USED, true).commit();
		DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
		ComponentName componentName = new ComponentName(this, SecurityDeviceAdminReceiver.class);
		if (!dpm.isAdminActive(componentName)) {
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
			startActivity(intent);
		}
		finish();
	}

	public void selectContact(View view) {
		startActivityForResult(new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI), CONTACT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case CONTACT:
			String contactId = data.getData().getLastPathSegment();
			Cursor cursor = getContentResolver().query(Phone.CONTENT_URI, new String[] { Phone.DATA }, Phone.CONTACT_ID + "=?",
					new String[] { contactId }, null);
			if (cursor.moveToNext()) {
				String phone = cursor.getString(cursor.getColumnIndex(Phone.DATA));
				boundPhoneNumberEditText.setText(phone);
				sp.edit().putString(Const.BOUND_PHONE_NUMBER, phone).commit();
			}
			cursor.close();
			break;

		default:
			break;
		}
	}

	public void bindOrUnBind(View view) {
		CheckBox checkBox = (CheckBox) view;
		TextView bindTextView = (TextView) findViewById(R.id.bind_text_view);
		bindTextView.setText(checkBox.isChecked() ? R.string.sim_bound : R.string.sim_unbound);
		Editor editor = sp.edit();
		if (checkBox.isChecked()) {
			TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			editor.putString(Const.SIM_SERIAL_NUMBER, telephonyManager.getSimSerialNumber());
		} else {
			editor.remove(Const.SIM_SERIAL_NUMBER);
		}
		editor.putBoolean(Const.IS_SIM_SERIAL_NUMBER_BOUND, checkBox.isChecked()).commit();
	}

	public void previous(View view) {
		--currentProgress;
		changeContentView();
	}

	public void next(View view) {
		++currentProgress;
		changeContentView();
	}

	private void changeContentView() {
		switch (currentProgress) {
		case 1:
			setContentView(R.layout.security_wizard);
			break;
		case 2:
			setContentView(R.layout.security_wizard2);
			TextView bindTextView = (TextView) findViewById(R.id.bind_text_view);
			CheckBox bindCheckBox = (CheckBox) findViewById(R.id.bind_check_box);
			if (sp.getBoolean(Const.IS_SIM_SERIAL_NUMBER_BOUND, false)) {
				bindTextView.setText(R.string.sim_bound);
				bindCheckBox.setChecked(true);
			}
			break;
		case 3:
			setContentView(R.layout.security_wizard3);
			boundPhoneNumberEditText = (EditText) findViewById(R.id.bound_phone_number);
			boundPhoneNumberEditText.setText(sp.getString(Const.BOUND_PHONE_NUMBER, null));
			break;
		case 4:
			setContentView(R.layout.security_wizard4);
			startStatusTextView = (TextView) findViewById(R.id.start_status);
			boolean isStarted = sp.getBoolean(Const.STATUS_STARTED, false);
			startStatusTextView.setText(isStarted ? R.string.started : R.string.not_started);
			startOrNotCheckBox = (CheckBox) findViewById(R.id.start_or_not_checkbox);
			startOrNotCheckBox.setChecked(isStarted);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.security_wizard, menu);
		return true;
	}

}
