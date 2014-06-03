package com.flyingh.vo;

public class Process implements Comparable<Process> {
	private App app;
	private int pid;
	private long memory;

	public Process() {
		super();
	}

	public Process(App app, int pid, long memory) {
		super();
		this.app = app;
		this.pid = pid;
		this.memory = memory;
	}

	public App getApp() {
		return app;
	}

	public void setApp(App app) {
		this.app = app;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public long getMemory() {
		return memory;
	}

	public void setMemory(long memory) {
		this.memory = memory;
	}

	@Override
	public String toString() {
		return "Process [app=" + app + ", pid=" + pid + ", memory=" + memory + "]";
	}

	@Override
	public int compareTo(Process another) {
		return memory < another.memory ? 1 : memory == another.memory ? 0 : -1;
	}
}
