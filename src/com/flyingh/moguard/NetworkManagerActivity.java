package com.flyingh.moguard;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.TrafficStats;
import android.os.Bundle;
import android.text.format.Formatter;
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
	private TextView mobileTrafficTextView;
	private TextView wifiTrafficTextView;
	private ScheduledExecutorService executorService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_network_manager);
		pm = getPackageManager();
		mobileTrafficTextView = (TextView) findViewById(R.id.mobileTrafficTextView);
		wifiTrafficTextView = (TextView) findViewById(R.id.wifiTrafficTextView);
		setTrafficInfo();
		listView = (ListView) findViewById(R.id.listView);
		listView.addHeaderView(View.inflate(this, R.layout.network_manager_title, null));
		resolveInfos = getData();
		initAdapter();
		listView.setAdapter(adapter);
		executorService = Executors.newScheduledThreadPool(1);
		executorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setTrafficInfo();
						resolveInfos = getData();
						adapter.notifyDataSetChanged();
					}
				});
			}
		}, 0, 1, TimeUnit.SECONDS);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		executorService.shutdown();
	}

	private void setTrafficInfo() {
		long mobileTxBytes = TrafficStats.getMobileTxBytes();
		long mobileRxBytes = TrafficStats.getMobileRxBytes();
		long totalTxBytes = TrafficStats.getTotalTxBytes();
		long totalRxBytes = TrafficStats.getTotalRxBytes();
		long totalMobileBytess = mobileTxBytes + mobileRxBytes;
		mobileTrafficTextView.setText("2/3G:" + Formatter.formatFileSize(this, totalMobileBytess));
		wifiTrafficTextView.setText("WIFI:" + Formatter.formatFileSize(this, totalTxBytes + totalRxBytes - totalMobileBytess));
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
				long uidTxBytes = TrafficStats.getUidTxBytes(uid);
				viewHolder.txTextView.setText(Formatter.formatFileSize(NetworkManagerActivity.this, uidTxBytes != -1 ? uidTxBytes : 0));
				long uidRxBytes = TrafficStats.getUidRxBytes(uid);
				viewHolder.rxTextView.setText(Formatter.formatFileSize(NetworkManagerActivity.this, uidRxBytes != -1 ? uidRxBytes : 0));
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
}
