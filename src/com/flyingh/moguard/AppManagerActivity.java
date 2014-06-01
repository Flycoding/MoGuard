package com.flyingh.moguard;

import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingh.engine.AppService;
import com.flyingh.vo.App;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AppManagerActivity extends Activity implements LoaderCallbacks<List<App>>, OnClickListener {
	private static final int LOAD_ID = 0;
	private static final int UNINSTALL_REQUEST_CODE = 0;
	private ListView listView;
	private LinearLayout progressLinearLayout;
	private PopupWindow popupWindow;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_manager);
		listView = (ListView) findViewById(R.id.appsListView);
		progressLinearLayout = (LinearLayout) findViewById(R.id.progressLinearLayout);
		getLoaderManager().initLoader(LOAD_ID, null, this);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				showPopupWindow(view, position);
			}

		});
		listView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				resetPopupWindow();
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				resetPopupWindow();
			}
		});
	}

	private void showPopupWindow(View view, int position) {
		resetPopupWindow();
		popupWindow = new PopupWindow(initContentView(position), LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		int[] location = new int[2];
		view.getLocationInWindow(location);
		popupWindow.showAtLocation(view, Gravity.TOP | Gravity.LEFT, location[0] + 150, location[1]);
	}

	private View initContentView(int position) {
		View popupWindowView = View.inflate(this, R.layout.popup_window_item, null);
		TextView runTextView = (TextView) popupWindowView.findViewById(R.id.runTextView);
		TextView uninstallTextView = (TextView) popupWindowView.findViewById(R.id.uninstallTextView);
		TextView shareTextView = (TextView) popupWindowView.findViewById(R.id.shareTextView);
		runTextView.setOnClickListener(this);
		uninstallTextView.setOnClickListener(this);
		shareTextView.setOnClickListener(this);

		runTextView.setTag(position);
		uninstallTextView.setTag(position);
		shareTextView.setTag(position);
		return popupWindowView;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.runTextView:
			run(v);
			break;
		case R.id.uninstallTextView:
			uninstall(v);
			break;
		case R.id.shareTextView:
			break;

		default:
			break;
		}
	}

	private void uninstall(View v) {
		int position = (int) v.getTag();
		App app = (App) listView.getItemAtPosition(position);
		String packageName = app.getPackageName();
		startActivityForResult(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + packageName)), UNINSTALL_REQUEST_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == UNINSTALL_REQUEST_CODE) {
			getLoaderManager().restartLoader(LOAD_ID, null, this);
		}
	}

	private void run(View v) {
		int position = (int) v.getTag();
		App app = (App) listView.getItemAtPosition(position);
		Intent launchIntentForPackage = getPackageManager().getLaunchIntentForPackage(app.getPackageName());
		if (launchIntentForPackage == null) {
			Toast.makeText(this, "can't run", Toast.LENGTH_LONG).show();
			resetPopupWindow();
			return;
		}
		startActivity(launchIntentForPackage);
	}

	private void resetPopupWindow() {
		if (popupWindow != null) {
			popupWindow.dismiss();
			popupWindow = null;
		}
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
