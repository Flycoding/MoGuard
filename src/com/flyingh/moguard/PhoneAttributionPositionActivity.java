package com.flyingh.moguard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import com.flyingh.moguard.util.Const;

public class PhoneAttributionPositionActivity extends Activity {
	private static final String TAG = "PhoneAttributionPositionActivity";

	private TextView positionTextView;
	private TextView hintTextView;
	private SharedPreferences sp;
	int startX = 0;
	int startY = 0;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = getSharedPreferences(Const.CONFIG_FILE_NAME, MODE_PRIVATE);
		setContentView(R.layout.activity_phone_attribution_position);
		Point outSize = new Point();

		getWindowManager().getDefaultDisplay().getSize(outSize);
		final int height = outSize.y;
		Log.i(TAG, "height:" + height);
		positionTextView = (TextView) findViewById(R.id.positionTextView);
		hintTextView = (TextView) findViewById(R.id.hintTextView);
		positionTextView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					break;
				case MotionEvent.ACTION_MOVE:
					int dx = (int) (event.getRawX() - startX);
					int dy = (int) (event.getRawY() - startY);
					positionTextView.layout(positionTextView.getLeft() + dx, positionTextView.getTop() + dy,
							positionTextView.getRight() + dx, positionTextView.getBottom() + dy);
					if (event.getRawY() < height / 2) {
						hintTextView.layout(hintTextView.getLeft(), 600, hintTextView.getRight(), 650);
					} else {
						hintTextView.layout(hintTextView.getLeft(), 350, hintTextView.getRight(), 400);
					}
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					break;
				case MotionEvent.ACTION_UP:
					sp.edit().putInt(Const.LAST_ATTRIBUTION_POSITION_X, (int) event.getRawX())
							.putInt(Const.LAST_ATTRIBUTION_POSITION_Y, (int) event.getRawY()).commit();
					break;
				default:
					break;
				}
				return true;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.phone_attribution_position, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
