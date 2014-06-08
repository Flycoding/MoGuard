package com.flyingh.moguard;

import java.util.List;

import android.annotation.TargetApi;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageStats;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.flyingh.engine.AppService;
import com.flyingh.vo.App;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SystemOptimizeActivity extends ListActivity implements LoaderCallbacks<List<App>> {
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
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		setListAdapter(adapter);
		progressLinearLayout.setVisibility(View.GONE);
	}

	@Override
	public void onLoaderReset(Loader<List<App>> loader) {
		adapter.clear();
	}

}
