package com.flyingh.moguard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingh.moguard.service.AppLockService;
import com.flyingh.moguard.service.PhoneNumberAttributionService;
import com.flyingh.moguard.util.Const;

public class AdvancedToolsActivity extends Activity {

	private static final String COUNT = "count";

	private static final String TAG = "AdvancedToolsActivity";

	private static final String BACKUP_SMS_FILE_NAME = "smses.xml";
	private static final String SMS = "sms";
	private static final String SMSES = "smses";
	private static final String _ID = "_id";
	private static final String BODY = "body";
	private static final String TYPE = "type";
	private static final String DATE = "date";
	private static final String ADDRESS = "address";
	private static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
	public static final int DEFAULT_SELECT_BACKGROUND_ITEM_INDEX = 1;
	public static final int DEFAULT_SELECT_BACKGROUND_DRAWABLE_RES_ID = R.drawable.call_locate_orange;

	private TextView serviceStatusTextView;
	private CheckBox startOrNotCheckBox;
	private TextView showInfoTextView;
	private CheckBox showDetailOrNotCheckBox;
	private SharedPreferences sp;
	private int height;
	private int width;
	private ProgressDialog progressDialog;

	private TextView lockServiceStatusTextView;

	private CheckBox lockServiceCheckBox;

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

		lockServiceStatusTextView = (TextView) findViewById(R.id.lockServiceStatusTextView);
		boolean appLockServiceStarted = sp.getBoolean(Const.APP_LOCK_SERVICE_STARTED, false);
		lockServiceStatusTextView.setText(appLockServiceStarted ? R.string.service_started : R.string.service_not_starts);
		lockServiceCheckBox = (CheckBox) findViewById(R.id.lockServiceCheckBox);
		lockServiceCheckBox.setChecked(appLockServiceStarted);
	}

	public void startAppLock(View view) {
		startActivity(new Intent(this, AppLockActivity.class));
	}

	public void startAppLockServiceOrNot(View view) {
		CheckBox appLockStatusCheckBox = (CheckBox) view;
		lockServiceStatusTextView.setText(appLockStatusCheckBox.isChecked() ? R.string.service_started : R.string.service_not_starts);
		Intent service = new Intent(this, AppLockService.class);
		if (appLockStatusCheckBox.isChecked()) {
			startService(service);
			sp.edit().putBoolean(Const.APP_LOCK_SERVICE_STARTED, true).commit();
		} else {
			stopService(service);
			sp.edit().putBoolean(Const.APP_LOCK_SERVICE_STARTED, false).commit();
		}
	}

	public void backupSms(View view) {
		new AsyncTask<Void, Integer, Void>() {
			@Override
			protected void onPreExecute() {
				progressDialog = new ProgressDialog(AdvancedToolsActivity.this);
				progressDialog.setTitle(getString(R.string.backuping_));
				progressDialog.setMessage(getString(R.string.current_progress_));
				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.show();
			}

			@Override
			protected Void doInBackground(Void... params) {
				Cursor cursor = getContentResolver().query(SMS_CONTENT_URI, new String[] { _ID, ADDRESS, DATE, TYPE, BODY }, null, null, null);
				progressDialog.setMax(cursor.getCount());
				XmlSerializer serializer = Xml.newSerializer();
				try {
					serializer.setOutput(new FileOutputStream(new File(Environment.getExternalStorageDirectory(), BACKUP_SMS_FILE_NAME)), "UTF-8");
					serializer.startDocument("UTF-8", true);
					serializer.startTag(null, SMSES);
					serializer.startTag(null, COUNT);
					serializer.text(String.valueOf(cursor.getCount()));
					serializer.endTag(null, COUNT);
					while (cursor.moveToNext()) {
						try {
							serializer.startTag(null, SMS);
							serializer.startTag(null, ADDRESS);
							serializer.text(cursor.getString(cursor.getColumnIndex(ADDRESS)));
							serializer.endTag(null, ADDRESS);

							serializer.startTag(null, DATE);
							serializer.text(cursor.getString(cursor.getColumnIndex(DATE)));
							serializer.endTag(null, DATE);

							serializer.startTag(null, TYPE);
							serializer.text(cursor.getString(cursor.getColumnIndex(TYPE)));
							serializer.endTag(null, TYPE);

							serializer.startTag(null, BODY);
							serializer.text(cursor.getString(cursor.getColumnIndex(BODY)));
							serializer.endTag(null, BODY);
							serializer.endTag(null, SMS);
							progressDialog.incrementProgressBy(1);
						} catch (Exception e) {
							Log.i(TAG, e.getMessage());
							Log.i(TAG, "_ID:" + cursor.getString(cursor.getColumnIndex(_ID)));
						}
					}
					serializer.endTag(null, SMSES);
					serializer.endDocument();
					cursor.close();
				} catch (IllegalArgumentException | IllegalStateException | IOException e) {
					Log.i(TAG, e.getMessage());
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				Toast.makeText(getApplicationContext(), R.string.backup_success, Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
				progressDialog = null;
			}
		}.execute();
	}

	public void restoreSms(View view) {
		new AsyncTask<Void, Integer, Void>() {
			@Override
			protected void onPreExecute() {
				progressDialog = new ProgressDialog(AdvancedToolsActivity.this);
				progressDialog.setTitle(getString(R.string.restoring_));
				progressDialog.setMessage(getString(R.string.current_progress_));
				progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				progressDialog.show();
			}

			@Override
			protected Void doInBackground(Void... params) {
				XmlPullParser parser = Xml.newPullParser();
				try {
					parser.setInput(new FileInputStream(new File(Environment.getExternalStorageDirectory(), BACKUP_SMS_FILE_NAME)), "UTF-8");
					int eventType = parser.getEventType();
					ContentResolver contentResolver = getContentResolver();
					ContentValues values = new ContentValues();
					while (eventType != XmlPullParser.END_DOCUMENT) {
						switch (eventType) {
						case XmlPullParser.START_TAG:
							if (COUNT.equals(parser.getName())) {
								progressDialog.setMax(Integer.valueOf(parser.nextText()));
							} else if (ADDRESS.equals(parser.getName())) {
								values.put(ADDRESS, parser.nextText());
							} else if (TYPE.equals(parser.getName())) {
								values.put(TYPE, parser.nextText());
							} else if (DATE.equals(parser.getName())) {
								values.put(DATE, parser.nextText());
							} else if (BODY.equals(parser.getName())) {
								values.put(BODY, parser.nextText());
							}
							break;
						case XmlPullParser.END_TAG:
							if (SMS.equals(parser.getName())) {
								Cursor cursor = contentResolver.query(SMS_CONTENT_URI, new String[] { "count(1)" }, buildSelection(),
										buildSelectionArgs(values), null);
								if (cursor.moveToFirst() && cursor.getInt(0) == 0) {
									contentResolver.insert(SMS_CONTENT_URI, values);
								}
								progressDialog.incrementProgressBy(1);
								values.clear();
								cursor.close();
							}
							break;
						default:
							break;
						}
						eventType = parser.next();
					}
				} catch (XmlPullParserException | NumberFormatException | IOException e) {
					Log.i(TAG, e.getMessage());
				}
				return null;
			}

			private String buildSelection() {
				return new StringBuilder().append(ADDRESS).append("=? and ").append(DATE).append("=? and ").append(TYPE).append("=? and ")
						.append(BODY).append("=?").toString();
			}

			private String[] buildSelectionArgs(ContentValues values) {
				String[] result = new String[values.size()];
				result[0] = values.getAsString(ADDRESS);
				result[1] = values.getAsString(DATE);
				result[2] = values.getAsString(TYPE);
				result[3] = values.getAsString(BODY);
				return result;
			}

			@Override
			protected void onPostExecute(Void result) {
				Toast.makeText(getApplicationContext(), R.string.restore_success, Toast.LENGTH_SHORT).show();
				progressDialog.dismiss();
				progressDialog = null;
			}

		}.execute();

	}

	public void queryCommonNumbers(View view) {
		startActivity(new Intent(this, CommonNumberActivity.class));
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
