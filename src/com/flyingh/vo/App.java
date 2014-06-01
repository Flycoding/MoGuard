package com.flyingh.vo;

import java.text.Collator;

import android.graphics.drawable.Drawable;

public class App implements Comparable<App> {
	private Drawable icon;
	private String label;
	private String totalSize;
	private String packageName;
	private boolean isSystemApp;

	private long totalSizeLong;

	public static class Builder {
		private Drawable icon;
		private String label;
		private String totalSize;
		private String packageName;
		private boolean isSystemApp;

		public Builder icon(Drawable icon) {
			this.icon = icon;
			return this;
		}

		public Builder label(String label) {
			this.label = label;
			return this;
		}

		public Builder totalSize(String totalSize) {
			this.totalSize = totalSize;
			return this;
		}

		public Builder packageName(String packageName) {
			this.packageName = packageName;
			return this;
		}

		public Builder isSystemApp(boolean isSystemApp) {
			this.isSystemApp = isSystemApp;
			return this;
		}

		public App build() {
			return new App(icon, label, totalSize, packageName, isSystemApp);
		}

	}

	private App() {
		super();
	}

	private App(Drawable icon, String label, String totalSize, String packageName, boolean isSystemApp) {
		super();
		this.icon = icon;
		this.totalSize = totalSize;
		this.packageName = packageName;
		setLabel(label);
		this.isSystemApp = isSystemApp;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = (label != null ? label : packageName);
	}

	public String getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(String totalSize) {
		this.totalSize = totalSize;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public boolean isSystemApp() {
		return isSystemApp;
	}

	public void setSystemApp(boolean isSystemApp) {
		this.isSystemApp = isSystemApp;
	}

	public void setTotalSizeLong(long totalSizeLong) {
		this.totalSizeLong = totalSizeLong;
	}

	public long getTotalSizeLong() {
		return totalSizeLong;
	}

	@Override
	public String toString() {
		return "App [icon=" + icon + ", label=" + label + ", totalSize=" + totalSize + ", packageName=" + packageName + ", isSystemApp="
				+ isSystemApp + "]";
	}

	@Override
	public int compareTo(App another) {
		return Collator.getInstance().compare(label, another.label);
	}

}
