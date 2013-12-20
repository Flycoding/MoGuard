package com.flyingh.moguard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class MainActivity extends Activity {
	private GridView gridView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		gridView = (GridView) findViewById(R.id.grid_view);
		final String[] features = getResources().getStringArray(R.array.features);
		final int[] iconIds = { R.drawable.security, R.drawable.callmsgsafe2, R.drawable.app3, R.drawable.taskmanager4, R.drawable.trojan5,
				R.drawable.netmanager6, R.drawable.sysoptimize7, R.drawable.atools8, R.drawable.settings9 };
		gridView.setAdapter(new BaseAdapter() {
			@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView view = (TextView) View.inflate(MainActivity.this, R.layout.grid_view_item, null);
				view.setCompoundDrawablesRelativeWithIntrinsicBounds(0, iconIds[position], 0, 0);
				view.setText(features[position]);
				return view;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public String getItem(int position) {
				return features[position];
			}

			@Override
			public int getCount() {
				return features.length;
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
