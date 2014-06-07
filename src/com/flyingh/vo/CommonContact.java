package com.flyingh.vo;

public class CommonContact {
	public static final String _ID = "_id";
	public static final String NAME = "name";
	public static final String NUMBER = "number";
	private String id;
	private String name;
	private String number;

	public CommonContact() {
		super();
	}

	public CommonContact(String id, String name, String number) {
		super();
		this.id = id;
		this.name = name;
		this.number = number;
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

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	@Override
	public String toString() {
		return "CommonContact [id=" + id + ", name=" + name + ", number=" + number + "]";
	}

}
