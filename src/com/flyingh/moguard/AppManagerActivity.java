package com.flyingh.moguard;

import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingh.engine.AppService;
import com.flyingh.moguard.util.Const;
import com.flyingh.vo.App;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AppManagerActivity extends Activity implements LoaderCallbacks<List<App>>, OnClickListener {
	private static final int LOAD_ID = 0;
	private static final int UNINSTALL_REQUEST_CODE = 0;
	private ListView listView;
	private LinearLayout progressLinearLayout;
	private PopupWindow popupWindow;
	private SharedPreferences sp;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_manager);
		sp = getSharedPreferences(Const.CONFIG_FILE_NAME, MODE_PRIVATE);
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
		Spinner spinner = (Spinner) findViewById(R.id.appShowModeSpinner);
		spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, DisplayMode.values()));
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				sp.edit().putInt(Const.APP_DISPLAY_MODE, position).commit();
				getLoaderManager().restartLoader(LOAD_ID, null, AppManagerActivity.this);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	public static enum DisplayMode {
		USER_SYSTEM {
			@Override
			public String toString() {
				return "user&system";
			}
		},
		USER, SYSTEM;
		@Override
		public String toString() {
			return name().toLowerCase(Locale.getDefault());
		}
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
			share(v);
			break;

		default:
			break;
		}
	}

	private void share(View v) {
		int position = (int) v.getTag();
		App app = (App) listView.getItemAtPosition(position);
		Intent target = new Intent(Intent.ACTION_SEND);
		target.putExtra(Intent.EXTRA_SUBJECT, R.string.share);
		target.putExtra(Intent.EXTRA_TEXT, R.string.share_you_an_app_ + app.getLabel());
		target.setType("text/plain");
		startActivity(Intent.createChooser(target, getString(R.string.share)));
	}

	private void uninstall(View v) {
		int position = (int) v.getTag();
		App app = (App) listView.getItemAtPosition(position);
		if (app.isSystemApp()) {
			Toast.makeText(this, R.string.system_app_the_app_forbid_the_operation, Toast.LENGTH_SHORT).show();
			return;
		}
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
			Toast.makeText(this, R.string.can_t_run, Toast.LENGTH_LONG).show();
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
		switch (item.getItemId()) {
		case R.id.order_by_name:
			order(OrderMode.ORDER_BY_NAME.name());
			break;
		case R.id.order_by_size:
			order(OrderMode.ORDER_BY_SIZE.name());
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void order(String orderMode) {
		sp.edit().putString(Const.APP_ORDER_MODE, orderMode).putBoolean(Const.ORDER_MENU_CLICKED, true).commit();
		getLoaderManager().restartLoader(LOAD_ID, null, this);
	}

	public static enum OrderMode {
		ORDER_BY_NAME, ORDER_BY_SIZE;
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
					progressLinearLayout.setVisibility(View.VISIBLE);
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
				viewHolder.labelTextView.setTextColor(app.isSystemApp() ? Color.RED : Color.GREEN);
				viewHolder.totalSizeTextView.setText(app.getTotalSize());
				viewHolder.totalSizeTextView.setTextColor(app.isSystemApp() ? Color.RED : Color.GREEN);
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
