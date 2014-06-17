package com.flyingh.moguard;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.flyingh.moguard.util.Const;

public class PhoneAttributionPositionActivity extends Activity {
	private static final String TAG = "PhoneAttributionPositionActivity";

	private ImageView imageView;
	private TextView hintTextView;
	private SharedPreferences sp;
	int startX = 0;
	int startY = 0;

	private int height;
	private int width;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = getSharedPreferences(Const.CONFIG_FILE_NAME, MODE_PRIVATE);
		setContentView(R.layout.activity_phone_attribution_position);
		Point outSize = new Point();
		getWindowManager().getDefaultDisplay().getSize(outSize);
		width = outSize.x;
		height = outSize.y;
		Log.i(TAG, "height:" + height);
		Log.i(TAG, "width:" + width);
		imageView = (ImageView) findViewById(R.id.imageView);
		hintTextView = (TextView) findViewById(R.id.hintTextView);
		imageView.setOnTouchListener(new OnTouchListener() {

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
					imageView.layout(imageView.getLeft() + dx, imageView.getTop() + dy, imageView.getRight() + dx, imageView.getBottom() + dy);
					updateHintLayout(event);
					startX = (int) event.getRawX();
					startY = (int) event.getRawY();
					Log.i(TAG, "newX:" + startX + ",newY:" + startY);
					break;
				case MotionEvent.ACTION_UP:
					sp.edit().putInt(Const.LAST_ATTRIBUTION_POSITION_X, imageView.getLeft())
							.putInt(Const.LAST_ATTRIBUTION_POSITION_Y, imageView.getTop()).putInt(Const.LAST_HINT_POSITION_X, hintTextView.getLeft())
							.putInt(Const.LAST_HINT_POSITION_Y, hintTextView.getTop()).commit();
					break;
				default:
					break;
				}
				return true;
			}

		});
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private int getHeight() {
		Point outSize = new Point();
		getWindowManager().getDefaultDisplay().getSize(outSize);
		return outSize.y;
	}

	private void updateHintLayout(MotionEvent event) {
		if (event.getRawY() < height / 2) {
			hintTextView.layout(hintTextView.getLeft(), 600, hintTextView.getRight(), 650);
		} else {
			hintTextView.layout(hintTextView.getLeft(), 350, hintTextView.getRight(), 400);
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	protected void onResume() {
		super.onResume();
		LayoutParams imageViewParams = (LayoutParams) imageView.getLayoutParams();
		imageViewParams.leftMargin = sp.getInt(Const.LAST_ATTRIBUTION_POSITION_X, 0);
		Log.i(TAG, "leftMargin:" + imageViewParams.leftMargin);
		imageViewParams.topMargin = sp.getInt(Const.LAST_ATTRIBUTION_POSITION_Y, 0);
		Log.i(TAG, "topMargin:" + imageViewParams.topMargin);
		if (isShowingDetailInfo()) {
			imageViewParams.width = width;
			imageViewParams.height = height / 4;
		} else {
			imageViewParams.width = width / 3;
			imageViewParams.height = height / 8;
		}
		LayoutParams hintTextViewParams = (LayoutParams) hintTextView.getLayoutParams();
		hintTextViewParams.leftMargin = sp.getInt(Const.LAST_HINT_POSITION_X, 0);
		hintTextViewParams.topMargin = sp.getInt(Const.LAST_HINT_POSITION_Y, 200);
		imageView.setLayoutParams(imageViewParams);
		imageView.setBackgroundResource(sp.getInt(Const.PHONE_NUMBER_ATTRIBUTION_BACKGROUND, R.drawable.call_locate_orange));
		hintTextView.setLayoutParams(hintTextViewParams);
		sp.edit().putInt(Const.PHONE_NUMBER_ATTRIBUTION_WIDTH, imageView.getWidth())
				.putInt(Const.PHONE_NUMBER_ATTRIBUTION_HEIGHT, imageView.getHeight()).commit();
	}

	private boolean isShowingDetailInfo() {
		return sp.getBoolean(Const.SHOW_DEFAIL_INFO, false);
	}

}
