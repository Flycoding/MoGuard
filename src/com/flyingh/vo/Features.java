package com.flyingh.vo;

import com.flyingh.moguard.R;
import com.flyingh.moguard.SecurityActivity;

public enum Features {
	//@formatter:off
	SECURITY(0, R.drawable.security, R.string.security, SecurityActivity.class),
	PHONE_GUARD (1, R.drawable.callmsgsafe2, R.string.phone_guard, null),
	APPS(2, R.drawable.app3, R.string.apps, null),
	TASK_MANAGER(3, R.drawable.taskmanager4, R.string.task_manager, null),
	KILL_VIRUS(4, R.drawable.trojan5, R.string.kill_virus, null),
	NETWORK_MANAGER(5, R.drawable.netmanager6, R.string.network_manager, null),
	SYSTEM_OPTIMIZE(6, R.drawable.sysoptimize7, R.string.system_optimize, null),
	ADVANCED_TOOLS(7, R.drawable.atools8, R.string.advanced_tools, null),
	SETTINGS(8, R.drawable.settings9, R.string.settings, null);
	//@formatter:on
	private int position;
	private int iconId;
	private int featureNameId;
	private Class<?> activityClass;

	private Features(int position, int iconId, int featureNameId, Class<?> activityClass) {
		this.position = position;
		this.iconId = iconId;
		this.featureNameId = featureNameId;
		this.activityClass = activityClass;
	}

	public int getPosition() {
		return position;
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

	public static final int getIconId(int position) {
		return values()[position].iconId;
	}

	public static final int getFeatureNameId(int position) {
		return values()[position].featureNameId;
	}

}
