package com.flyingh.vo;

public class CommonContactType {
	public static final String _ID = "idx";
	public static final String NAME = "name";

	private String id;
	private String name;

	public CommonContactType() {
		super();
	}

	public CommonContactType(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "CommonContactType [id=" + id + ", name=" + name + "]";
	}
}
