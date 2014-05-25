package com.flyingh.moguard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;
import com.flyingh.moguard.util.Const;

public class QueryPhoneNumberActivity extends Activity {
	protected static final Integer ERROR = -1;
	private ProgressDialog dialog;
	private EditText phoneNumberEditText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_query_phone_number);
		if (!isAddressDbExists()) {
			downloadDb();
		}
		phoneNumberEditText = (EditText) findViewById(R.id.phoneNumberEditText);
	}

	private void downloadDb() {
		new AsyncTask<String, Integer, Void>() {
			@Override
			protected void onPreExecute() {
				dialog = new ProgressDialog(QueryPhoneNumberActivity.this);
				dialog.setTitle("Downloading...");
				dialog.setMessage("Current progress:");
				dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				dialog.setIndeterminate(false);
				dialog.show();
				if (!isNetworkAvailable()) {
					Toast.makeText(QueryPhoneNumberActivity.this, "network is not available", Toast.LENGTH_SHORT)
							.show();
					cancel(true);
					finish();
				}
			}

			private boolean isNetworkAvailable() {
				ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
				return activeNetworkInfo.isConnected();
			}

			@Override
			protected Void doInBackground(String... params) {
				try {
					if (isCancelled()) {
						return null;
					}
					URL url = new URL(params[0]);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(2000);
					conn.setRequestMethod("GET");
					if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
						int contentLength = conn.getContentLength();
						dialog.setMax(contentLength);// safe
						save(conn.getInputStream());
					}
				} catch (Exception e) {
					e.printStackTrace();
					publishProgress(ERROR);
				}
				return null;
			}

			private void save(InputStream is) throws FileNotFoundException, IOException {
				FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory(),
						Const.ADDRESS_DB_NAME));
				try {
					int len = 0;
					byte[] buffer = new byte[1024 * 16];
					while ((len = is.read(buffer)) != -1) {
						publishProgress(len);
						fos.write(buffer, 0, len);
					}
				} finally {
					fos.close();
				}
			}

			@Override
			protected void onProgressUpdate(Integer... values) {
				if (values[0] == ERROR) {
					Toast.makeText(QueryPhoneNumberActivity.this, "download failed", Toast.LENGTH_LONG).show();
					cancel(true);
					finish();
				} else {
					dialog.setProgress(dialog.getProgress() + values[0]);
				}
			}

			@Override
			protected void onPostExecute(Void result) {
				dialog.dismiss();
			}

			@Override
			protected void onCancelled() {
				dialog.dismiss();
			}
		}.execute(getString(R.string.address_db_url));
	}

	private boolean isAddressDbExists() {
		return new File(Environment.getExternalStorageDirectory(), Const.ADDRESS_DB_NAME).exists();
	}

	public void query(View view) {
		String phoneNumber = phoneNumberEditText.getText().toString().trim();
		if (TextUtils.isEmpty(phoneNumber) || phoneNumber.length() < Const.MIN_PHONE_NUMBER_PREFIX) {
			Toast.makeText(this, R.string.the_phone_number_s_length_should_not_less_than_ + Const.MIN_PHONE_NUMBER_PREFIX,
					Toast.LENGTH_SHORT).show();
			phoneNumberEditText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.query_phone_number, menu);
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
