package com.flyingh.moguard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingh.engine.UpdateInfoService;
import com.flyingh.vo.UpdateInfo;

public class SplashActivity extends Activity {
	private static final String APK_MIME_TYPE = "application/vnd.android.package-archive";
	private static final String TAG = "SplashActivity";
	private TextView versionNameTextView;
	private Handler handler = new Handler();
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		AlphaAnimation animation = new AlphaAnimation(0, 1);
		animation.setDuration(2000);
		findViewById(R.id.linear_layout).startAnimation(animation);
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage(getString(R.string.downloading_));
		versionNameTextView = (TextView) findViewById(R.id.version_name);
		try {
			final PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionNameTextView.setText(packageInfo.versionName);
			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					handler.post(new Runnable() {

						@Override
						public void run() {
							ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
							NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
							if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
								Toast.makeText(SplashActivity.this, "Network is not available...", Toast.LENGTH_SHORT).show();
								enter();
								return;
							}
							new Thread(new CheckUpdateRunnable(packageInfo.versionCode)).start();
						}
					});
				}
			}, 2000);
		} catch (NameNotFoundException e) {
			Log.i(TAG, e.getMessage());
		}
	}

	class CheckUpdateRunnable implements Runnable {
		private int currentVersionCode;

		public CheckUpdateRunnable(int currentVersionCode) {
			super();
			this.currentVersionCode = currentVersionCode;
		}

		@Override
		public void run() {

			try {
				final UpdateInfo info = new UpdateInfoService(SplashActivity.this).getUpdateInfo();
				if (needUpdate(info.getVersion())) {
					Log.i(TAG, "need update!");
					handler.post(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(SplashActivity.this, "need update", Toast.LENGTH_SHORT).show();
							new AlertDialog.Builder(SplashActivity.this).setIcon(R.drawable.ic_launcher).setTitle("confirm to update?")
									.setMessage(info.getDescription()).setCancelable(false).setPositiveButton("OK", new OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											Log.i(TAG, "start to download the apk...");
											new DownloadAsyncTask().execute(info.getUrl());
										}
									}).setNegativeButton("Cancel", new OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											enter();
										}

									}).show();
						}
					});
				} else {
					Log.i(TAG, "no need to update,enter!");
					enter();
				}
			} catch (Exception e) {
				Log.i(TAG, e.getMessage());
				handler.post(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(SplashActivity.this, R.string.get_update_information_failed, Toast.LENGTH_SHORT).show();
						enter();
					}
				});
			}

		}

		private boolean needUpdate(String version) {
			return version.compareTo(String.valueOf(currentVersionCode)) > 0;
		}

	}

	class DownloadAsyncTask extends AsyncTask<String, Integer, File> {

		@Override
		protected void onPreExecute() {
			progressDialog.show();
			progressDialog.setCancelable(false);
			progressDialog.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					switch (event.getAction()) {
					case KeyEvent.ACTION_UP:
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							new AlertDialog.Builder(SplashActivity.this).setIcon(R.drawable.ic_launcher).setTitle("Cancel?")
									.setMessage("Are you sure to cancel the download?").setPositiveButton("Confirm", new OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											cancel(true);
										}
									}).setNegativeButton("Cancel", null).show();

						}
						break;

					default:
						break;
					}
					return false;
				}
			});
		}

		@Override
		protected File doInBackground(String... params) {
			try {
				String path = params[0];
				URL url = new URL(path);
				URLConnection conn = url.openConnection();
				int contentLength = conn.getContentLength();
				InputStream is = url.openStream();
				if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
					File file = new File(Environment.getExternalStorageDirectory(), getFileName(path));
					FileOutputStream fos = new FileOutputStream(file);
					byte[] buf = new byte[1024];
					int len = 0;
					while ((len = is.read(buf)) != -1) {
						if (isCancelled()) {
							file.delete();
							break;
						}
						fos.write(buf, 0, len);
						publishProgress(contentLength, len);
					}
					fos.close();
					return file;
				}
			} catch (Exception e) {
				Log.i(TAG, e.getMessage());
			}
			return null;
		}

		private String getFileName(String path) {
			return path.substring(path.lastIndexOf("/") + 1);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			progressDialog.setMax(values[0]);
			progressDialog.setProgress(progressDialog.getProgress() + values[1]);
		}

		@SuppressLint("NewApi")
		@Override
		protected void onCancelled(File result) {
			super.onCancelled(result);
			progressDialog.dismiss();
			Toast.makeText(SplashActivity.this, R.string.canceled, Toast.LENGTH_SHORT).show();
			enter();
		}

		@Override
		protected void onPostExecute(File result) {
			Log.i(TAG, result.toString());
			progressDialog.dismiss();
			install(result);
		}

	}

	public void enter() {
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}

	private void install(File result) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(result), APK_MIME_TYPE);
		finish();
		startActivity(intent);
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
