package com.flyingh.moguard;

import android.app.Activity;
import android.content.Context;
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

import com.flyingh.moguard.util.Const;

public class SecurityWizardActivity extends Activity {
	private static final int CONTACT = 0;
	private int currentProgress = 1;
	private SharedPreferences sp;
	private EditText boundPhoneNumberEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.security_wizard);
		sp = getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE);
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
		bindTextView.setText(checkBox.isChecked() ? R.string.bound : R.string.unbound);
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
				bindTextView.setText(R.string.bound);
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
