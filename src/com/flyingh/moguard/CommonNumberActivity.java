package com.flyingh.moguard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.flyingh.engine.CommonContactService;
import com.flyingh.vo.CommonContact;
import com.flyingh.vo.CommonContactType;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CommonNumberActivity extends Activity implements LoaderCallbacks<Map<CommonContactType, List<CommonContact>>> {

	private static final int LOAD_ID = 0;
	private ExpandableListView expandableListView;
	private CommonNumberExpandableListAdapter adapter;
	private LinearLayout progressLinearLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_common_number);
		expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
		progressLinearLayout = (LinearLayout) findViewById(R.id.progressLinearLayout);
		adapter = new CommonNumberExpandableListAdapter();
		expandableListView.setAdapter(adapter);
		getLoaderManager().initLoader(LOAD_ID, null, this);
		expandableListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				CommonContact contact = adapter.getChild(groupPosition, childPosition);
				startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contact.getNumber())));
				return true;
			}
		});
	}

	class CommonNumberExpandableListAdapter extends BaseExpandableListAdapter {
		private Map<CommonContactType, List<CommonContact>> map = new LinkedHashMap<>();

		public void changeData(Map<CommonContactType, List<CommonContact>> map) {
			this.map = map;
			notifyDataSetChanged();
		}

		public void clear() {
			map.clear();
			notifyDataSetChanged();
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			TextView textView = (TextView) (convertView != null ? convertView : new TextView(CommonNumberActivity.this));
			textView.setText(getGroup(groupPosition).getName());
			textView.setTextColor(Color.YELLOW);
			textView.setTextSize(20);
			return textView;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public int getGroupCount() {
			return map.keySet().size();
		}

		@Override
		public CommonContactType getGroup(int groupPosition) {
			return map.keySet().toArray(new CommonContactType[map.size()])[groupPosition];
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return map.get(getGroup(groupPosition)).size();
		}

		class ViewHolder {
			TextView nameTextView;
			TextView numbertTextView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			CommonContact contact = getChild(groupPosition, childPosition);
			View view = getView(convertView);
			ViewHolder viewHolder = (ViewHolder) view.getTag();
			viewHolder.nameTextView.setText(contact.getName());
			viewHolder.numbertTextView.setText(contact.getNumber());
			return view;
		}

		private View getView(View convertView) {
			if (convertView != null) {
				return convertView;
			}
			View view = View.inflate(CommonNumberActivity.this, R.layout.common_number_child_item, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.nameTextView = (TextView) view.findViewById(R.id.nameTextView);
			viewHolder.numbertTextView = (TextView) view.findViewById(R.id.numberTextView);
			view.setTag(viewHolder);
			return view;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return Long.parseLong(groupPosition + "" + childPosition);
		}

		@Override
		public CommonContact getChild(int groupPosition, int childPosition) {
			return map.get(getGroup(groupPosition)).get(childPosition);
		}

	}

	@Override
	public Loader<Map<CommonContactType, List<CommonContact>>> onCreateLoader(int id, Bundle args) {
		progressLinearLayout.setVisibility(View.VISIBLE);
		return new AsyncTaskLoader<Map<CommonContactType, List<CommonContact>>>(this) {
			private Map<CommonContactType, List<CommonContact>> map;

			@Override
			protected void onStartLoading() {
				if (map != null) {
					deliverResult(map);
				}
				if (takeContentChanged() || map == null) {
					forceLoad();
				}
			}

			@Override
			public Map<CommonContactType, List<CommonContact>> loadInBackground() {
				return map = CommonContactService.group(CommonNumberActivity.this);
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<Map<CommonContactType, List<CommonContact>>> loader, Map<CommonContactType, List<CommonContact>> data) {
		adapter.changeData(data);
		progressLinearLayout.setVisibility(View.GONE);
	}

	@Override
	public void onLoaderReset(Loader<Map<CommonContactType, List<CommonContact>>> loader) {
		adapter.clear();
	}

}
