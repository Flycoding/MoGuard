package com.flyingh.moguard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.ContentValues;
import android.content.Loader;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingh.engine.AppService;
import com.flyingh.moguard.AddAppLockActivity.UnlockedAppBaseAdapter.ViewHolder;
import com.flyingh.vo.App;
import com.flyingh.vo.AppLock;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AddAppLockActivity extends Activity implements LoaderCallbacks<List<App>> {
	private static final String TAG = "AddAppLockActivity";
	private static final int LOAD_ID = 0;
	private ListView unlockedAppListView;
	private UnlockedAppBaseAdapter adapter;
	private final Set<App> selectedApps = new HashSet<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_app_lock);
		unlockedAppListView = (ListView) findViewById(R.id.unlockedAppListView);
		adapter = new UnlockedAppBaseAdapter();
		unlockedAppListView.setAdapter(adapter);
		unlockedAppListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.i(TAG, "position:" + position);
				App app = (App) unlockedAppListView.getItemAtPosition(position);
				if (selectedApps.contains(app)) {
					selectedApps.remove(app);
				} else {
					selectedApps.add(app);
				}
				ViewHolder viewHolder = (ViewHolder) view.getTag();
				viewHolder.appLockCheckBox.setChecked(selectedApps.contains(app));
			}

		});
		getLoaderManager().initLoader(LOAD_ID, null, this);
	}

	class UnlockedAppBaseAdapter extends BaseAdapter {
		private List<App> apps = new ArrayList<>();

		public void changeData(List<App> apps) {
			this.apps = apps;
			notifyDataSetChanged();
		}

		public void clear() {
			apps.clear();
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return apps.size();
		}

		@Override
		public App getItem(int position) {
			return apps.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		class ViewHolder {
			ImageView iconImageView;
			TextView labelTextView;
			CheckBox appLockCheckBox;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			App app = getItem(position);
			View view = getView(convertView);
			ViewHolder viewHolder = (ViewHolder) view.getTag();
			viewHolder.iconImageView.setImageDrawable(app.getIcon());
			viewHolder.labelTextView.setText(app.getLabel());
			viewHolder.appLockCheckBox.setChecked(selectedApps.contains(app));
			return view;
		}

		private View getView(View convertView) {
			if (convertView != null) {
				return convertView;
			}
			View view = View.inflate(AddAppLockActivity.this, R.layout.unlocked_app_item, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.iconImageView = (ImageView) view.findViewById(R.id.iconImageView);
			viewHolder.labelTextView = (TextView) view.findViewById(R.id.labelTextView);
			viewHolder.appLockCheckBox = (CheckBox) view.findViewById(R.id.appLockCheckBox);
			view.setTag(viewHolder);
			return view;
		}

	}

	public void add(View view) {
		if (selectedApps.isEmpty()) {
			Toast.makeText(this, R.string.the_selected_app_is_empty, Toast.LENGTH_SHORT).show();
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
		for (App app : selectedApps) {
			ContentValues values = new ContentValues();
			values.put(AppLock.PACKAGE_NAME, app.getPackageName());
			values.put(AppLock.LABEL, app.getLabel());
			getContentResolver().insert(AppLock.INSERT_CONTENT_URI, values);
		}
		Toast.makeText(this, R.string.add_success, Toast.LENGTH_SHORT).show();
		setResult(RESULT_OK);
		finish();
	}

	public void cancel(View view) {
		finish();
	}

	@Override
	public Loader<List<App>> onCreateLoader(int id, Bundle args) {
		return new AsyncTaskLoader<List<App>>(this) {
			private List<App> result;

			@Override
			protected void onStartLoading() {
				super.onStartLoading();
				if (result != null) {
					deliverResult(result);
				}
				if (takeContentChanged() || result == null) {
					forceLoad();
				}
			}

			@Override
			public List<App> loadInBackground() {
				return result = AppService.loadUnlockedApps(AddAppLockActivity.this);
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<List<App>> loader, List<App> data) {
		adapter.changeData(data);
	}

	@Override
	public void onLoaderReset(Loader<List<App>> loader) {
		adapter.clear();
	}
}
