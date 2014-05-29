package com.flyingh.moguard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import com.flyingh.dao.BlacklistDao;
import com.flyingh.vo.Feature;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class BlacklistActivity extends Activity implements LoaderCallbacks<Cursor> {
	private static final String TAG = "BlacklistActivity";

	private static final String COLUMN_NUMBER = "number";
	private static final int LOADER_ID = 0;
	private ListView listView;
	private SimpleCursorAdapter adapter;
	private LoaderManager loaderManager;
	private BlacklistDao dao;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blacklist);
		dao = new BlacklistDao(this);
		listView = (ListView) findViewById(R.id.listView);
		adapter = new SimpleCursorAdapter(this, R.layout.blacklist_item, null, new String[] { COLUMN_NUMBER }, new int[] { R.id.number }, 0);
		listView.setAdapter(adapter);
		registerForContextMenu(listView);
		loaderManager = getLoaderManager();
		loaderManager.initLoader(LOADER_ID, null, this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.blacklist_item_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		Log.i(TAG, "id:" + menuInfo.id);
		Log.i(TAG, "position:" + menuInfo.position);
		Cursor cursor = (Cursor) adapter.getItem(menuInfo.position);
		final String number = cursor.getString(cursor.getColumnIndex(COLUMN_NUMBER));
		switch (item.getItemId()) {
		case R.id.updateMenuItem:
			doUpdate(number);
			break;
		case R.id.deleteMenuItem:
			doDelete(number);
			break;
		default:
			break;
		}
		return false;
	}

	private void doDelete(final String number) {
		dao.delete(number);
		loaderManager.restartLoader(LOADER_ID, null, this);
	}

	private void doUpdate(final String number) {
		final EditText editText = new EditText(this);
		editText.setInputType(InputType.TYPE_CLASS_PHONE);
		editText.setText(number);
		editText.setSelectAllOnFocus(true);
		new AlertDialog.Builder(this).setTitle(R.string.update_the_number).setMessage(R.string.input_the_new_number).setView(editText)
				.setCancelable(false).setPositiveButton(R.string.update, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String newNumber = editText.getText().toString().trim();
						if (TextUtils.equals(number, newNumber)) {
							return;
						}
						if (dao.update(number, newNumber)) {
							loaderManager.restartLoader(LOADER_ID, null, BlacklistActivity.this);
							Toast.makeText(getApplicationContext(), R.string.update_success, Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(BlacklistActivity.this, R.string.update_fail_the_number_may_already_exist, Toast.LENGTH_SHORT).show();
						}
					}
				}).setNegativeButton(R.string.cancel, null).show();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(BlacklistActivity.this, Uri.parse("content://com.flyingh.moguard.blacklistprovider/all"), null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

	public void add(View view) {
		final EditText editText = new EditText(this);
		editText.setTextColor(Color.RED);
		editText.setInputType(InputType.TYPE_CLASS_PHONE);
		new AlertDialog.Builder(this).setIcon(Feature.PHONE_GUARD.getIconId()).setTitle(R.string.add_a_number_to_blacklist)
				.setMessage(R.string.please_input_a_number_to_block).setView(editText).setPositiveButton(R.string.add, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String number = editText.getText().toString().trim();
						if (TextUtils.isEmpty(number)) {
							Toast.makeText(getApplicationContext(), R.string.the_number_should_not_be_empty, Toast.LENGTH_SHORT).show();
							return;
						}
						if (dao.add(number)) {
							Toast.makeText(getApplicationContext(), R.string.add_success, Toast.LENGTH_SHORT).show();
							loaderManager.restartLoader(LOADER_ID, null, BlacklistActivity.this);
						} else {
							Toast.makeText(getApplicationContext(), R.string.add_fail_the_number_may_already_exists, Toast.LENGTH_SHORT).show();
						}
					}
				}).setNegativeButton(R.string.cancel, null).show();
	}

}
