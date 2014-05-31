package com.flyingh.moguard;

import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.flyingh.engine.AppService;
import com.flyingh.vo.App;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AppManagerActivity extends Activity implements LoaderCallbacks<List<App>> {
	private ListView listView;
	private LinearLayout progressLinearLayout;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_manager);
		listView = (ListView) findViewById(R.id.appsListView);
		progressLinearLayout = (LinearLayout) findViewById(R.id.progressLinearLayout);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_manager, menu);
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

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public Loader<List<App>> onCreateLoader(int id, Bundle args) {
		return new AsyncTaskLoader<List<App>>(this) {
			private List<App> result;

			@Override
			protected void onStartLoading() {
				if (result != null) {
					deliverResult(result);
				}
				if (takeContentChanged() || result == null) {
					forceLoad();
				}
			}

			@Override
			public List<App> loadInBackground() {
				return result = AppService.loadApps(AppManagerActivity.this);
			}
		};
	}

	class ViewHolder {
		ImageView iconImageView;
		TextView labelTextView;
		TextView totalSizeTextView;
	}

	@Override
	public void onLoadFinished(Loader<List<App>> loader, final List<App> data) {
		listView.setAdapter(new BaseAdapter() {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				App app = getItem(position);
				View view = getView(convertView);
				ViewHolder viewHolder = (ViewHolder) view.getTag();
				viewHolder.iconImageView.setImageDrawable(app.getIcon());
				viewHolder.labelTextView.setText(app.getLabel());
				viewHolder.totalSizeTextView.setText(app.getTotalSize());
				return view;
			}

			private View getView(View convertView) {
				if (convertView != null) {
					return convertView;
				}
				View view = View.inflate(AppManagerActivity.this, R.layout.app_item, null);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.iconImageView = (ImageView) view.findViewById(R.id.icon);
				viewHolder.labelTextView = (TextView) view.findViewById(R.id.label);
				viewHolder.totalSizeTextView = (TextView) view.findViewById(R.id.totalSize);
				view.setTag(viewHolder);
				return view;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public App getItem(int position) {
				return data.get(position);
			}

			@Override
			public int getCount() {
				return data.size();
			}
		});
		progressLinearLayout.setVisibility(View.GONE);
	}

	@Override
	public void onLoaderReset(Loader<List<App>> loader) {
	}

}
