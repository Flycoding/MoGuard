package com.flyingh.moguard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingh.vo.AppLock;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AppLockActivity extends Activity implements LoaderCallbacks<Cursor> {
	private static final String TAG = "AppLockActivity";

	private static final int LOAD_ID = 0;
	private static final int CODE_ADD_APP_LOCK = 0;
	private ListView lockedAppListView;
	private CursorAdapter adapter;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_lock);
		lockedAppListView = (ListView) findViewById(R.id.lockedAppListView);
		initAdapter();
		lockedAppListView.setAdapter(adapter);
		lockedAppListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursor = (Cursor) adapter.getItem(position);
				final String packageName = cursor.getString(cursor.getColumnIndex(AppLock.PACKAGE_NAME));
				try {
					PackageManager pm = getPackageManager();
					ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
					String label = applicationInfo.loadLabel(pm).toString();
					Drawable icon = applicationInfo.loadIcon(pm);
					new AlertDialog.Builder(AppLockActivity.this).setIcon(icon).setTitle(R.string.confirm)
							.setMessage(getString(R.string.are_you_sure_to_delete_) + label)
							.setPositiveButton(R.string.delete, new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									getContentResolver()
											.delete(AppLock.DELETE_CONTENT_URI, AppLock.PACKAGE_NAME + "=?", new String[] { packageName });
									Toast.makeText(AppLockActivity.this, R.string.delete_success, Toast.LENGTH_SHORT).show();
									getLoaderManager().restartLoader(LOAD_ID, null, AppLockActivity.this);
								}
							}).setNegativeButton(R.string.cancel, null).show();
				} catch (NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}

		});
		getLoaderManager().initLoader(LOAD_ID, null, this);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private CursorAdapter initAdapter() {
		return adapter = new CursorAdapter(this, null, 0) {

			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				return View.inflate(context, R.layout.locked_app_item, null);
			}

			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				if (cursor == null) {
					return;
				}
				TextView lockedAppTextView = (TextView) view;
				try {
					ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(
							cursor.getString(cursor.getColumnIndex(AppLock.PACKAGE_NAME)), PackageManager.GET_UNINSTALLED_PACKAGES);
					lockedAppTextView.setText(applicationInfo.loadLabel(getPackageManager()));
					Drawable divideLineDrawable = getResources().getDrawable(R.drawable.divide_line);
					lockedAppTextView.setCompoundDrawablesWithIntrinsicBounds(applicationInfo.loadIcon(getPackageManager()),
							cursor.getPosition() == 0 ? divideLineDrawable : null, null, divideLineDrawable);
				} catch (NameNotFoundException e) {
					Log.i(TAG, e.getMessage());
					throw new RuntimeException(e);
				}

			}
		};
	}

	public void add(View view) {
		startActivityForResult(new Intent(this, AddAppLockActivity.class), CODE_ADD_APP_LOCK);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == CODE_ADD_APP_LOCK) {
				getLoaderManager().restartLoader(LOAD_ID, null, this);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.app_lock, menu);
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
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, AppLock.QUERY_CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

}
