package com.flyingh.moguard;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class SecurityWizardActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.security_wizard);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.security_wizard, menu);
		return true;
	}

}
