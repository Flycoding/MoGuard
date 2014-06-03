package com.flyingh.vo;

import com.flyingh.moguard.AdvancedToolsActivity;
import com.flyingh.moguard.AppManagerActivity;
import com.flyingh.moguard.BlacklistActivity;
import com.flyingh.moguard.R;
import com.flyingh.moguard.SecurityActivity;
import com.flyingh.moguard.TaskManagerActivity;

public enum Feature {
	//@formatter:off
	SECURITY(R.drawable.security, R.string.security, SecurityActivity.class),
	PHONE_GUARD (R.drawable.callmsgsafe2, R.string.phone_guard, BlacklistActivity.class),
	APPS(R.drawable.app3, R.string.apps, AppManagerActivity.class),
	TASK_MANAGER(R.drawable.taskmanager4, R.string.task_manager, TaskManagerActivity.class),
	KILL_VIRUS(R.drawable.trojan5, R.string.kill_virus, null),
	NETWORK_MANAGER(R.drawable.netmanager6, R.string.network_manager, null),
	SYSTEM_OPTIMIZE(R.drawable.sysoptimize7, R.string.system_optimize, null),
	ADVANCED_TOOLS(R.drawable.atools8, R.string.advanced_tools, AdvancedToolsActivity.class),
	SETTINGS(R.drawable.settings9, R.string.settings, null);
	//@formatter:on
	private int iconId;
	private int featureNameId;
	private Class<?> activityClass;

	private Feature(int iconId, int featureNameId, Class<?> activityClass) {
		this.iconId = iconId;
		this.featureNameId = featureNameId;
		this.activityClass = activityClass;
	}

	public Class<?> getActivityClass() {
		return activityClass;
	}

	public int getIconId() {
		return iconId;
	}

	public int getFeatureNameId() {
		return featureNameId;
	}

	public static final Feature getFeature(int position) {
		return values()[position];
	}

	public static final int getIconId(int position) {
		return getFeature(position).iconId;
	}

	public static final int getFeatureNameId(int position) {
		return getFeature(position).featureNameId;
	}

	public static Class<?> getActivityClass(int position) {
		return getFeature(position).activityClass;
	}

}
