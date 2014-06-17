package com.flyingh.moguard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingh.engine.AppService;
import com.flyingh.moguard.util.FileUtils;
import com.flyingh.vo.App;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SystemOptimizeActivity extends ListActivity implements LoaderCallbacks<List<App>> {
	private static final String CLEARPATH_DB_NAME = "clearpath.db";
	private static final String TAG = "SystemOptimizeActivity";
	private static final int LOAD_ID = 0;
	private ArrayAdapter<App> adapter;
	private LinearLayout progressLinearLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_system_optimize);
		progressLinearLayout = (LinearLayout) findViewById(R.id.progressLinearLayout);
		initAdapter();
		setListAdapter(adapter);
		getLoaderManager().initLoader(LOAD_ID, null, this);
	}

	private void initAdapter() {
		adapter = new ArrayAdapter<App>(this, R.layout.system_optimize_item) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				App app = getItem(position);
				View view = getView(convertView);
				ViewHolder viewHolder = (ViewHolder) view.getTag();
				viewHolder.iconImageView.setImageDrawable(app.getIcon());
				PackageStats packageStats = app.getPackageStats();
				if (packageStats != null) {
					viewHolder.codeSizeTextView.setText(Formatter.formatFileSize(SystemOptimizeActivity.this, packageStats.codeSize));
					viewHolder.dataSizeTextView.setText(Formatter.formatFileSize(SystemOptimizeActivity.this, packageStats.dataSize));
					viewHolder.cacheSizeTextView.setText(Formatter.formatFileSize(SystemOptimizeActivity.this, packageStats.cacheSize));
				} else {
					viewHolder.codeSizeTextView.setText(null);
					viewHolder.dataSizeTextView.setText(null);
					viewHolder.cacheSizeTextView.setText(null);
				}
				return view;
			}

			private View getView(View convertView) {
				if (convertView != null) {
					return convertView;
				}
				View view = View.inflate(SystemOptimizeActivity.this, R.layout.system_optimize_item, null);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.iconImageView = (ImageView) view.findViewById(R.id.iconImageView);
				viewHolder.codeSizeTextView = (TextView) view.findViewById(R.id.codeSizeTextView);
				viewHolder.dataSizeTextView = (TextView) view.findViewById(R.id.dataSizeTextView);
				viewHolder.cacheSizeTextView = (TextView) view.findViewById(R.id.cacheSizeTextView);
				view.setTag(viewHolder);
				return view;
			}

			class ViewHolder {
				ImageView iconImageView;
				TextView codeSizeTextView;
				TextView dataSizeTextView;
				TextView cacheSizeTextView;
			}
		};
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent();
		intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		intent.setData(Uri.parse("package:" + adapter.getItem(position).getPackageName()));
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.system_optimize, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.clear_external_cache) {
			clearExternalCache();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void clearExternalCache() {
		new AsyncTask<Void, Integer, Long>() {
			private ProgressDialog dialog;

			@Override
			protected void onPreExecute() {
				copyFileIfNeed();
				showDialog();
			}

			private void copyFileIfNeed() {
				try {
					openFileInput(CLEARPATH_DB_NAME);
				} catch (FileNotFoundException e) {
					try {
						FileUtils.copy(getAssets().open(CLEARPATH_DB_NAME), openFileOutput(CLEARPATH_DB_NAME, Context.MODE_PRIVATE));
					} catch (IOException e1) {
						Log.i(TAG, e1.getMessage());
						throw new RuntimeException(e1);
					}
				}
			}

			private void showDialog() {
				dialog = new ProgressDialog(SystemOptimizeActivity.this);
				dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				dialog.setTitle("Clear external cache");
				dialog.setMessage("Status");
				dialog.setIcon(R.drawable.android);
				dialog.show();
			}

			@Override
			protected Long doInBackground(Void... params) {
				SQLiteDatabase db = SQLiteDatabase.openDatabase(getFilesDir() + "/" + CLEARPATH_DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
				List<PackageInfo> installedPackages = getPackageManager().getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
				dialog.setProgress(installedPackages.size());
				long result = 0;
				for (PackageInfo packageInfo : installedPackages) {
					Cursor cursor = db.query("softdetail",
							new String[] { "_id", "softChinesename", "softEnglishname", "apkname", "filepath", "stype" }, "apkname=?",
							new String[] { packageInfo.packageName }, null, null, null);
					if (cursor.moveToFirst()) {
						final String softEnglishName = cursor.getString(cursor.getColumnIndex("softEnglishname"));
						String filePath = cursor.getString(cursor.getColumnIndex("filepath"));
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								dialog.setMessage("clear " + softEnglishName);
							}
						});
						result += delete(new File(filePath));
						SystemClock.sleep(100);
						dialog.incrementProgressBy(1);
					}
				}
				return result;
			}

			private long delete(File file) {
				long result = 0;
				if (file.isDirectory()) {
					for (File f : file.listFiles()) {
						if (f == null) {
							continue;
						}
						result += delete(f);
					}
				}
				result += file.length();
				file.delete();
				return result;
			}

			@Override
			protected void onPostExecute(Long result) {
				dialog.dismiss();
				Toast.makeText(getApplicationContext(), "clear success:" + Formatter.formatFileSize(SystemOptimizeActivity.this, result),
						Toast.LENGTH_LONG).show();
			}

		}.execute();
	}

	@Override
	public Loader<List<App>> onCreateLoader(int id, Bundle args) {
		progressLinearLayout.setVisibility(View.VISIBLE);
		return new AsyncTaskLoader<List<App>>(this) {
			private List<App> apps;

			@Override
			protected void onStartLoading() {
				if (apps != null) {
					deliverResult(apps);
				}
				if (takeContentChanged() || apps == null) {
					forceLoad();
				}
			}

			@Override
			public List<App> loadInBackground() {
				return apps = AppService.loadApps(SystemOptimizeActivity.this);
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<List<App>> loader, List<App> data) {
		adapter.setNotifyOnChange(false);
		adapter.clear();
		adapter.addAll(data);
		adapter.notifyDataSetChanged();
		progressLinearLayout.setVisibility(View.GONE);
	}

	@Override
	public void onLoaderReset(Loader<List<App>> loader) {
		adapter.clear();
	}

}
