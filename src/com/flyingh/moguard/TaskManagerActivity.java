package com.flyingh.moguard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flyingh.adapter.GroupAdapter;
import com.flyingh.adapter.GroupAdapter.Transformer;
import com.flyingh.engine.AppService;
import com.flyingh.moguard.app.MoGuardApp;
import com.flyingh.moguard.util.Const;
import com.flyingh.vo.App;
import com.flyingh.vo.Process;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TaskManagerActivity extends Activity implements LoaderCallbacks<List<Process>> {
	private static final String TAG = "TaskManagerActivity";
	private static final int LOAD_ID = 0;
	private TextView processCountTextView;
	private TextView memoryTextView;
	private ListView listView;
	private ActivityManager am;
	private Transformer<Process, Boolean> transformer;
	private GroupAdapter<Boolean, Collection<Process>, Process> adapter;
	private final Set<Integer> checkedPositions = new HashSet<>();
	private final List<Process> processes = new ArrayList<>();
	private LinearLayout progressLinearLayout;
	private SharedPreferences sp;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_manager);
		sp = getSharedPreferences(Const.CONFIG_FILE_NAME, MODE_PRIVATE);
		processCountTextView = (TextView) findViewById(R.id.processCountTextView);
		memoryTextView = (TextView) findViewById(R.id.memoryTextView);
		listView = (ListView) findViewById(R.id.listView);
		progressLinearLayout = (LinearLayout) findViewById(R.id.progressLinearLayout);
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		setProcessAndMemoryInfo();
		transformer = new Transformer<Process, Boolean>() {

			@Override
			public Boolean transform(Process e) {
				return e.getApp().isSystemApp();
			}
		};
		adapter = initAdapter(processes);
		listView.setAdapter(adapter);
		getLoaderManager().initLoader(LOAD_ID, null, this);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (isNotSelectable(position)) {
					return;
				}
				ViewHolder viewHolder = (ViewHolder) view.getTag();
				if (viewHolder == null) {
					return;
				}
				if (checkedPositions.contains(position)) {
					checkedPositions.remove(position);
				} else {
					checkedPositions.add(position);
				}
				viewHolder.checkBox.setChecked(checkedPositions.contains(position));
			}

		});
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				if (adapter.isKeyPosition(position)) {
					return true;
				}
				Process process = (Process) adapter.getItem(position);
				if (TextUtils.isEmpty(process.getApp().getPackageName())) {
					return true;
				}
				MoGuardApp application = (MoGuardApp) getApplication();
				application.process = process;
				startActivity(new Intent(getApplicationContext(), AppPermissionsActivity.class));
				return true;
			}
		});
	}

	private boolean isNotSelectable(int position) {
		Object item = adapter.getItem(position);
		if (!Process.class.isInstance(item)) {
			return false;
		}
		Process process = (Process) item;
		String packageName = process.getApp().getPackageName();
		return TextUtils.isEmpty(packageName) || TextUtils.equals(Const.PACKAGE_NAME, packageName);
	}

	private GroupAdapter<Boolean, Collection<Process>, Process> initAdapter(List<Process> processes) {
		return new GroupAdapter<Boolean, Collection<Process>, Process>(this, transformer, processes) {

			@Override
			public View newKeyView(int position, Context context, Boolean item, ViewGroup parent) {
				TextView textView = new TextView(context);
				textView.setGravity(Gravity.CENTER);
				textView.setTextSize(20);
				textView.setBackgroundColor(Color.YELLOW);
				textView.setTextColor(Color.RED);
				textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, getResources().getDrawable(R.drawable.divide_line));
				return textView;
			}

			@Override
			public void bindKeyView(View view, int position, Context context, Boolean k) {
				TextView textView = (TextView) view;
				textView.setText(k ? R.string.system_process : R.string.user_process);
			}

			@Override
			public View newValueView(int position, Context context, Process item, ViewGroup parent) {
				View view = View.inflate(context, R.layout.task_item, null);
				ViewHolder viewHolder = new ViewHolder();
				viewHolder.iconImageView = (ImageView) view.findViewById(R.id.iconImageView);
				viewHolder.labelTextView = (TextView) view.findViewById(R.id.labelTextView);
				viewHolder.memTextView = (TextView) view.findViewById(R.id.memTextView);
				viewHolder.checkBox = (CheckBox) view.findViewById(R.id.checkBox);
				view.setTag(viewHolder);
				return view;
			}

			@Override
			public void bindValueView(View view, int position, Context context, Process e) {
				ViewHolder viewHolder = (ViewHolder) view.getTag();
				viewHolder.iconImageView.setImageDrawable(e.getApp().getIcon());
				viewHolder.labelTextView.setText(e.getApp().getLabel());
				viewHolder.memTextView.setText(Formatter.formatFileSize(context, e.getMemory()));
				if (isNotSelectable(position)) {
					viewHolder.checkBox.setVisibility(View.GONE);
					viewHolder.checkBox.setChecked(false);
				} else {
					viewHolder.checkBox.setVisibility(View.VISIBLE);
					viewHolder.checkBox.setChecked(checkedPositions.contains(position));
				}
			}
		};
	}

	class ViewHolder {
		ImageView iconImageView;
		TextView labelTextView;
		TextView memTextView;
		CheckBox checkBox;
	}

	private List<Process> convert(List<RunningAppProcessInfo> runningAppProcesses) {
		List<Process> result = new ArrayList<>();
		boolean showSystemApp = sp.getBoolean(Const.SHOW_SYSTEM_APP, true);
		for (RunningAppProcessInfo runningAppProcessInfo : runningAppProcesses) {
			Log.i(TAG, runningAppProcessInfo.processName);
			int totalPrivateDirty = am.getProcessMemoryInfo(new int[] { runningAppProcessInfo.pid })[0].getTotalPrivateDirty();
			App app = AppService.getApp(this, runningAppProcessInfo.processName);
			if (showSystemApp || !app.isSystemApp()) {
				Process process = new Process(app, runningAppProcessInfo.pid, totalPrivateDirty * 1024);
				result.add(process);
			}
		}
		return result;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void setProcessAndMemoryInfo() {
		List<RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
		processCountTextView.setText(getString(R.string.current_process_) + runningAppProcesses.size());
		MemoryInfo outInfo = new MemoryInfo();
		am.getMemoryInfo(outInfo);
		memoryTextView.setText(getString(R.string.avail_total_) + Formatter.formatFileSize(this, outInfo.availMem) + "/"
				+ Formatter.formatFileSize(this, outInfo.totalMem));
	}

	public void clean(View view) {
		int killedProcessCount = 0;
		long cleanedMemory = 0;
		for (Integer checkedPosition : checkedPositions) {
			Process process = (Process) adapter.getItem(checkedPosition);
			String packageName = process.getApp().getPackageName();
			if (!TextUtils.isEmpty(packageName)) {
				am.killBackgroundProcesses(packageName);
				++killedProcessCount;
				cleanedMemory += process.getMemory();
			}
		}
		Toast toast = new Toast(this);
		TextView toastView = (TextView) View.inflate(this, R.layout.toast, null);
		toastView.setText(getString(R.string.killed_process_number_) + killedProcessCount + "\n");
		toastView.append(getString(R.string.cleaned_memory_) + Formatter.formatFileSize(this, cleanedMemory));
		toast.setView(toastView);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.show();
		checkedPositions.clear();
		setProcessAndMemoryInfo();
		getLoaderManager().restartLoader(LOAD_ID, null, this);
	}

	public void setup(View view) {
		startActivityForResult(new Intent(this, TaskManagerSettingsActivity.class), 0);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		getLoaderManager().restartLoader(LOAD_ID, null, this);
	}

	@Override
	public Loader<List<Process>> onCreateLoader(int id, Bundle args) {
		progressLinearLayout.setVisibility(View.VISIBLE);
		return new AsyncTaskLoader<List<Process>>(this) {
			private List<Process> processes;

			@Override
			protected void onStartLoading() {
				if (processes != null) {
					deliverResult(processes);
				}
				if (takeContentChanged() || processes == null) {
					forceLoad();
				}
			}

			@Override
			public List<Process> loadInBackground() {
				processes = convert(am.getRunningAppProcesses());
				Log.i(TAG, String.valueOf(processes.size()));
				return processes;
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<List<Process>> loader, List<Process> data) {
		progressLinearLayout.setVisibility(View.GONE);
		adapter.changeData(data);
	}

	@Override
	public void onLoaderReset(Loader<List<Process>> loader) {
		adapter.clear();
	}
}
