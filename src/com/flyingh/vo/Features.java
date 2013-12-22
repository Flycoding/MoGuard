package com.flyingh.vo;

import com.flyingh.moguard.SecurityActivity;

public enum Features {
	SECURITY(0, SecurityActivity.class);
	private int position;
	private Class<?> activityClass;

	private Features(int position, Class<?> activityClass) {
		this.position = position;
		this.activityClass = activityClass;
	}

	public int getPosition() {
		return position;
	}

	public Class<?> getActivityClass() {
		return activityClass;
	}

}
