package com.flyingh.moguard;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.flyingh.moguard.util.FileUtils;
import com.flyingh.moguard.util.StringUtils;

public class KillVirusActivity extends Activity {
	private static final String TAG = "KillVirusActivity";
	private static final String COLUMN_DESC = "desc";
	private static final String COLUMN_NAME = "name";
	private static final String ANTIVIRUS_DB_NAME = "antivirus.db";
	private AnimationDrawable animationDrawable;
	private ImageView imageView;
	private ListView listView;
	private TextView progressTextView;
	private ProgressBar progressBar;
	private ArrayAdapter<String> adapter;
	private final LinkedList<String> scannedPackages = new LinkedList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kill_virus);
		animationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.kill_virus);
		imageView = (ImageView) findViewById(R.id.imageView);
		imageView.setImageDrawable(animationDrawable);
		listView = (ListView) findViewById(R.id.listView);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView textView = (TextView) super.getView(position, convertView, parent);
				textView.setTextSize(15);
				textView.setTextColor(Color.GREEN);
				return textView;
			}
		};
		listView.setAdapter(adapter);
		progressTextView = (TextView) findViewById(R.id.progressTextView);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		progressBar.setIndeterminate(false);
	}

	public void scan(View view) {
		new AsyncTask<Void, String, List<String>>() {
			@Override
			protected void onPreExecute() {
				imageView.setClickable(false);
				animationDrawable.start();
				List<PackageInfo> packageInfos = getPackageManager().getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
				progressBar.setMax(packageInfos.size());
			}

			@Override
			protected List<String> doInBackground(Void... params) {
				List<String> result = new ArrayList<String>();
				copyFileIfNeed();
				List<PackageInfo> packageInfos = getPackageManager().getInstalledPackages(
						PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_SIGNATURES);
				SQLiteDatabase db = SQLiteDatabase.openDatabase(getFilesDir() + "/" + ANTIVIRUS_DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
				for (PackageInfo packageInfo : packageInfos) {
					publishProgress(packageInfo.packageName);
					String md5 = StringUtils.md5(packageInfo.signatures[0].toCharsString());
					Cursor cursor = db.rawQuery("select * from datable where md5=?", new String[] { md5 });
					if (cursor.moveToFirst()) {
						result.add(packageInfo.packageName + ":" + cursor.getString(cursor.getColumnIndex(COLUMN_NAME)) + "\n"
								+ cursor.getString(cursor.getColumnIndex(COLUMN_DESC)));
					}
					cursor.close();
					SystemClock.sleep(300);
				}
				result.add("com.flyingh.haha:virus9527\nthis is a virus");
				result.add("com.flyingh.haha:virus9527\nthis is a virus");
				return result;
			}

			@Override
			protected void onProgressUpdate(String... values) {
				progressTextView.setText("Scanning " + values[0]);
				progressBar.incrementProgressBy(1);
				scannedPackages.addFirst(values[0]);
				changeData(scannedPackages);
			}

			private void copyFileIfNeed() {
				try {
					openFileInput(ANTIVIRUS_DB_NAME);
				} catch (FileNotFoundException e) {
					try {
						FileUtils.copy(getAssets().open(ANTIVIRUS_DB_NAME), openFileOutput(ANTIVIRUS_DB_NAME, Context.MODE_PRIVATE));
					} catch (Exception e1) {
						Log.i(TAG, e1.getMessage());
						throw new RuntimeException(e1);
					}
				}
			}

			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@Override
			protected void onPostExecute(java.util.List<String> result) {
				imageView.setClickable(true);
				animationDrawable.stop();
				progressBar.setProgress(0);
				scannedPackages.clear();
				if (result.isEmpty()) {
					adapter.clear();
					progressTextView.setText(R.string.no_virus_found);
				} else {
					progressTextView.setText(getString(R.string.found_) + result.size()
							+ (result.size() == 1 ? getString(R.string._virus) : getString(R.string._viruses)));
					changeData(result);
				}
			}

			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			private void changeData(java.util.List<String> result) {
				adapter.setNotifyOnChange(false);
				adapter.clear();
				adapter.addAll(result);
				adapter.notifyDataSetChanged();
			}
		}.execute();
	}
}
