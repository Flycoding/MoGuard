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
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.flyingh.engine.QueryNumberService;
import com.flyingh.moguard.util.Const;

public class QueryPhoneNumberActivity extends Activity {
	protected static final Integer ERROR = -1;
	private ProgressDialog dialog;
	private EditText queryParamEditText;
	private TextView resultTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_query_phone_number);
		if (!isAddressDbExists()) {
			downloadDb();
		}
		queryParamEditText = (EditText) findViewById(R.id.queryParamEditText);
		resultTextView = (TextView) findViewById(R.id.resultTextView);
	}

	private void downloadDb() {
		new AsyncTask<String, Integer, Void>() {
			@Override
			protected void onPreExecute() {
				dialog = new ProgressDialog(QueryPhoneNumberActivity.this);
				dialog.setTitle(getString(R.string.downloading_));
				dialog.setMessage(getString(R.string.current_progress_));
				dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				dialog.setIndeterminate(false);
				dialog.show();
				if (!isNetworkAvailable()) {
					Toast.makeText(QueryPhoneNumberActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
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
				FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), Const.ADDRESS_DB_NAME));
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
					Toast.makeText(QueryPhoneNumberActivity.this, R.string.download_failed, Toast.LENGTH_LONG).show();
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
		String queryParam = queryParamEditText.getText().toString().trim();
		if (TextUtils.isEmpty(queryParam)) {
			Toast.makeText(this, R.string.the_phone_number_should_not_be_empty, Toast.LENGTH_SHORT).show();
			queryParamEditText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
			return;
		}
		String result = QueryNumberService.query(this, queryParam);
		resultTextView.setText(TextUtils.isEmpty(result) ? getString(R.string.no_result) : result);
	}

}
