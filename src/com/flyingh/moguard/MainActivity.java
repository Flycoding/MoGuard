package com.flyingh.moguard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingh.moguard.util.Const;
import com.flyingh.vo.Feature;

public class MainActivity extends Activity {
	private static final String SECURITY_FEATURE_NAME = "security_feature_name";
	private GridView gridView;
	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		sp = getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE);
		gridView = (GridView) findViewById(R.id.grid_view);
		gridView.setAdapter(new BaseAdapter() {
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView view = (TextView) View.inflate(MainActivity.this, R.layout.grid_view_item, null);
				view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, Feature.getIconId(position), 0, 0);
				view.setText(sp.contains(SECURITY_FEATURE_NAME) && Feature.SECURITY.ordinal() == position ? sp.getString(SECURITY_FEATURE_NAME, null)
						: getItem(position));
				view.setTextColor(Color.BLUE);
				return view;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public String getItem(int position) {
				return getResources().getString(Feature.getFeatureNameId(position));
			}

			@Override
			public int getCount() {
				return Feature.values().length;
			}
		});
		gridView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, final View view, int position, long id) {
				if (Feature.SECURITY.ordinal() != position) {
					return false;
				}
				final EditText newFeatureNameEditText = (EditText) View.inflate(MainActivity.this, R.layout.change_security_feature_name, null);
				new AlertDialog.Builder(MainActivity.this).setIcon(R.drawable.ic_launcher).setTitle(R.string.change_the_feature_s_name_)
						.setView(newFeatureNameEditText).setPositiveButton(R.string.ok, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								String newFeatureName = newFeatureNameEditText.getText().toString().trim();
								if (TextUtils.isEmpty(newFeatureName)) {
									Toast.makeText(MainActivity.this, R.string.the_feature_name_should_not_be_empty_, Toast.LENGTH_SHORT).show();
									return;
								}
								TextView textView = (TextView) view;
								textView.setText(newFeatureName);
								sp.edit().putString(SECURITY_FEATURE_NAME, newFeatureName).commit();
							}
						}).setNegativeButton(R.string.cancel, null).show();
				return false;
			}
		});
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startActivity(new Intent(MainActivity.this, Feature.getActivityClass(position)));
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
