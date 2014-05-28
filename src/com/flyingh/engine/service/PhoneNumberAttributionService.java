package com.flyingh.engine.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.TextView;

import com.flyingh.engine.QueryNumberService;
import com.flyingh.moguard.AdvancedToolsActivity;
import com.flyingh.moguard.util.Const;

public class PhoneNumberAttributionService extends Service {

	private TelephonyManager telephonyManager;
	private PhoneStateListener listener;
	private SharedPreferences sp;
	private WindowManager windowManager;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		sp = getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE);
		listener = new PhoneStateListener() {
			private TextView view;

			@Override
			public void onCallStateChanged(int state, String incomingNumber) {
				super.onCallStateChanged(state, incomingNumber);
				switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:
					removeView();
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					String result = handleResult(QueryNumberService.query(PhoneNumberAttributionService.this, incomingNumber));
					if (TextUtils.isEmpty(result)) {
						return;
					}
					addView(result);
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:
					removeView();
					break;
				default:
					break;
				}
			}

			private void addView(String result) {
				view = new TextView(PhoneNumberAttributionService.this);
				view.setGravity(Gravity.CENTER);
				view.setText(result);
				view.setTextColor(Color.YELLOW);
				view.setBackgroundResource(sp.getInt(Const.PHONE_NUMBER_ATTRIBUTION_BACKGROUND,
						AdvancedToolsActivity.DEFAULT_SELECT_BACKGROUND_DRAWABLE_RES_ID));
				windowManager.addView(view, getParams());
			}

			private void removeView() {
				if (view != null) {
					windowManager.removeView(view);
					view = null;
				}
			}

			private LayoutParams getParams() {
				final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
				params.height = WindowManager.LayoutParams.WRAP_CONTENT;
				params.width = WindowManager.LayoutParams.WRAP_CONTENT;
				params.format = PixelFormat.TRANSLUCENT;
				// params.windowAnimations = com.android.internal.R.style.Animation_Toast;
				params.type = WindowManager.LayoutParams.TYPE_TOAST;
				params.setTitle("Toast");
				params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
				params.x = sp.getInt(Const.LAST_ATTRIBUTION_POSITION_X, 0);
				params.y = sp.getInt(Const.LAST_ATTRIBUTION_POSITION_Y, 0);
				params.width = sp.getInt(Const.PHONE_NUMBER_ATTRIBUTION_WIDTH, WindowManager.LayoutParams.WRAP_CONTENT);
				params.height = sp.getInt(Const.PHONE_NUMBER_ATTRIBUTION_HEIGHT, WindowManager.LayoutParams.WRAP_CONTENT);

				return params;
			}
		};
		telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		super.onCreate();
	}

	protected String handleResult(String queryResult) {
		if (queryResult == null) {
			return null;
		}
		return showDefailInfo() ? queryResult : queryResult.split("\\n")[1];
	}

	private boolean showDefailInfo() {
		return sp.getBoolean(Const.SHOW_DEFAIL_INFO, false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		telephonyManager.listen(listener, PhoneStateListener.LISTEN_NONE);
	}

}
