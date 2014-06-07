package com.flyingh.moguard;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.TrafficStats;
import android.os.Bundle;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class NetworkManagerActivity extends Activity {
	private ListView listView;
	private List<ResolveInfo> resolveInfos;
	private BaseAdapter adapter;
	private PackageManager pm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network_manager);
		pm = getPackageManager();
		listView = (ListView) findViewById(R.id.listView);
		listView.addHeaderView(View.inflate(this, R.layout.network_manager_title, null));
		resolveInfos = getData();
		initAdapter();
		listView.setAdapter(adapter);
	}

	private List<ResolveInfo> getData() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		return pm.queryIntentActivities(intent, 0);
	}

	private void initAdapter() {
		adapter = new BaseAdapter() {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				ResolveInfo resolveInfo = getItem(position);
				View view = getView(convertView);
				ViewHolder viewHolder = (ViewHolder) view.getTag();
				viewHolder.iconImageView.setImageDrawable(resolveInfo.loadIcon(pm));
				viewHolder.labelTextView.setText(resolveInfo.loadLabel(pm));
				int uid = resolveInfo.activityInfo.applicationInfo.uid;
				viewHolder.txTextView.setText(Formatter.formatFileSize(NetworkManagerActivity.this, TrafficStats.getUidTxBytes(uid)));
				viewHolder.rxTextView.setText(Formatter.formatFileSize(NetworkManagerActivity.this, TrafficStats.getUidRxBytes(uid)));
				return view;
			}

			class ViewHolder {
				ImageView iconImageView;
				TextView labelTextView;
				TextView txTextView;
				TextView rxTextView;
			}

			private View getView(View convertView) {
				if (convertView != null) {
					return convertView;
				}
				View view = View.inflate(NetworkManagerActivity.this, R.layout.network_manager_item, null);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.iconImageView = (ImageView) view.findViewById(R.id.iconImageView);
				viewHolder.labelTextView = (TextView) view.findViewById(R.id.labelTextView);
				viewHolder.txTextView = (TextView) view.findViewById(R.id.txTextView);
				viewHolder.rxTextView = (TextView) view.findViewById(R.id.rxTextView);
				view.setTag(viewHolder);
				return view;
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public ResolveInfo getItem(int position) {
				return resolveInfos.get(position);
			}

			@Override
			public int getCount() {
				return resolveInfos.size();
			}
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.network, menu);
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

}
