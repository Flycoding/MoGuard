package com.flyingh.moguard;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;

public class SelectContactActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.select_contact, menu);
		return true;
	}

}
