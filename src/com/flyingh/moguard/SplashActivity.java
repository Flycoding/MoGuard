package com.flyingh.moguard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.xmlpull.v1.XmlPullParserException;

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
import android.view.animation.AlphaAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingh.engine.UpdateInfoService;
import com.flyingh.moguard.util.LocaleUtils;
import com.flyingh.vo.UpdateInfo;

public class SplashActivity extends Activity {
	private static final String APK_MIME_TYPE = "application/vnd.android.package-archive";
	private static final String TAG = "SplashActivity";
	private TextView versionNameTextView;
	private final Handler handler = new Handler();
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		LocaleUtils.changeLocale(this);
		startAnimation();
		try {
			final PackageInfo packageInfo = getPackageInfo();
			initVersionNameTextView(packageInfo.versionName);
			checkUpdateDelayed(packageInfo.versionCode, 2000);
		} catch (NameNotFoundException e) {
			Log.i(TAG, e.getMessage());
		}
	}

	private void checkUpdateDelayed(final int versionCode, long delayMillis) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!isNetworkConnected()) {
					showNetworkNotAvailableToast();
					enterMainUI();
					return;
				}
				checkUpdate(versionCode);
			}
		}, delayMillis);
	}

	private void checkUpdate(final int versionCode) {
		new Thread(new CheckUpdateRunnable(versionCode)).start();
	}

	private void initVersionNameTextView(String versionName) {
		versionNameTextView = (TextView) findViewById(R.id.version_name);
		versionNameTextView.setText(versionName);
	}

	private PackageInfo getPackageInfo() throws NameNotFoundException {
		return getPackageManager().getPackageInfo(getPackageName(), 0);
	}

	private void initProgressDialog() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage(getString(R.string.downloading_));
		progressDialog.setCancelable(false);
	}

	private void startAnimation() {
		AlphaAnimation animation = new AlphaAnimation(0, 1);
		animation.setDuration(2000);
		findViewById(R.id.linear_layout).startAnimation(animation);
	}

	class CheckUpdateRunnable implements Runnable {
		private final int currentVersionCode;

		public CheckUpdateRunnable(int currentVersionCode) {
			super();
			this.currentVersionCode = currentVersionCode;
		}

		@Override
		public void run() {
			try {
				final UpdateInfo info = getUpdateInfo();
				if (hasUpdate(info.getVersion())) {
					handler.post(new Runnable() {
						@Override
						public void run() {
							showHasUpdateToast();
							showHasUpdateAlertDialog(info);
						}

					});
				} else {
					enterMainUI();
				}
			} catch (Exception e) {
				handler.post(new Runnable() {

					@Override
					public void run() {
						showUpdateFailedToast();
						enterMainUI();
					}
				});
			}

		}

		private UpdateInfo getUpdateInfo() throws IOException, XmlPullParserException {
			return new UpdateInfoService(SplashActivity.this).getUpdateInfo();
		}

		private boolean hasUpdate(String version) {
			return version.compareTo(String.valueOf(currentVersionCode)) > 0;
		}

	}

	private void showHasUpdateToast() {
		Toast.makeText(SplashActivity.this, R.string.has_update, Toast.LENGTH_SHORT).show();
	}

	class DownloadAsyncTask extends AsyncTask<String, Integer, File> {

		@Override
		protected void onPreExecute() {
			initProgressDialog();
			setOnKeyListener();
			progressDialog.show();
		}

		private void setOnKeyListener() {
			progressDialog.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					switch (event.getAction()) {
					case KeyEvent.ACTION_UP:
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							showConfirmCancelDownloadingDialog();
						}
						break;
					default:
						break;
					}
					return false;
				}

			});
		}

		private void showConfirmCancelDownloadingDialog() {
			new AlertDialog.Builder(SplashActivity.this).setIcon(R.drawable.ic_launcher).setTitle(R.string.cancel_)
					.setMessage(R.string.are_you_sure_to_cancel_the_download_).setPositiveButton(R.string.confirm, new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							cancel(true);
						}
					}).setNegativeButton(R.string.cancel, null).show();
		}

		@Override
		protected File doInBackground(String... params) {
			try {
				String path = params[0];
				URL url = new URL(path);
				URLConnection conn = url.openConnection();
				progressDialog.setMax(conn.getContentLength());
				InputStream is = url.openStream();
				if (isMediaMounted()) {
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
						progressDialog.incrementProgressBy(len);
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
			return path.substring(path.lastIndexOf(File.separator) + 1);
		}

		@SuppressLint("NewApi")
		@Override
		protected void onCancelled(File result) {
			super.onCancelled(result);
			progressDialog.dismiss();
			showCanceledToast();
			enterMainUI();
		}

		private void showCanceledToast() {
			Toast.makeText(SplashActivity.this, R.string.canceled, Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onPostExecute(File result) {
			Log.i(TAG, result.toString());
			progressDialog.dismiss();
			install(result);
		}

	}

	public void enterMainUI() {
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}

	private void install(File result) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(result), APK_MIME_TYPE);
		finish();
		startActivity(intent);
	}

	private boolean isNetworkConnected() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private void showNetworkNotAvailableToast() {
		Toast.makeText(this, R.string.network_is_not_available_, Toast.LENGTH_SHORT).show();
	}

	private void showHasUpdateAlertDialog(final UpdateInfo info) {
		new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher).setTitle(R.string.confirm_to_update_).setMessage(info.getDescription())
				.setCancelable(false).setPositiveButton(R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						download(info.getUrl());
					}
				}).setNegativeButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						enterMainUI();
					}

				}).show();
	}

	private void showUpdateFailedToast() {
		Toast.makeText(this, R.string.get_update_information_failed, Toast.LENGTH_SHORT).show();
	}

	private void download(String url) {
		new DownloadAsyncTask().execute(url);
	}

	private boolean isMediaMounted() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}

}
