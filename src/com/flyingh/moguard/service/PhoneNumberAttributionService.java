package com.flyingh.moguard.service;

import java.lang.reflect.Method;
import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog.Calls;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.internal.telephony.ITelephony;
import com.flyingh.dao.BlacklistDao;
import com.flyingh.engine.QueryNumberService;
import com.flyingh.moguard.AdvancedToolsActivity;
import com.flyingh.moguard.BlacklistActivity;
import com.flyingh.moguard.util.Const;
import com.flyingh.vo.Feature;

public class PhoneNumberAttributionService extends Service {
	public static final String INCOMING_NUMBER = "incoming_number";

	private static final String TAG = "PhoneNumberAttributionService";

	private TelephonyManager telephonyManager;
	private PhoneStateListener listener;
	private SharedPreferences sp;
	private WindowManager windowManager;
	private BlacklistDao dao;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		sp = getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE);
		dao = new BlacklistDao(this);
		listener = new PhoneStateListener() {
			private TextView view;
			private long startRingingTime;
			private long endRingingTime;

			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			@Override
			public void onCallStateChanged(int state, final String incomingNumber) {
				super.onCallStateChanged(state, incomingNumber);
				switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:
					endRingingTime = System.currentTimeMillis();
					Log.i(TAG, "endRingingTime=" + endRingingTime);
					boolean isRingOneTimeCall = startRingingTime > 0 && endRingingTime > startRingingTime && endRingingTime - startRingingTime < 5000;
					Log.i(TAG, "endRingingTime - startRingingTime=" + (endRingingTime - startRingingTime));
					if (isRingOneTimeCall) {
						if (isInBlacklist(incomingNumber)) {
							Log.i(TAG, "isInBlacklist");
							showBlockBlacklistNumberNotification(incomingNumber);
						} else {
							showNotification(incomingNumber);
						}
					}
					startRingingTime = 0;
					removeView();
					break;
				case TelephonyManager.CALL_STATE_RINGING:
					startRingingTime = System.currentTimeMillis();
					Log.i(TAG, "startRingingTime=" + startRingingTime);
					if (isInBlacklist(incomingNumber)) {
						endCall();
						getContentResolver().registerContentObserver(Calls.CONTENT_URI, true, new ContentObserver(new Handler()) {
							@Override
							public void onChange(boolean selfChange) {
								Cursor cursor = getContentResolver().query(Calls.CONTENT_URI, null, Calls.NUMBER + "=?",
										new String[] { incomingNumber }, null);
								if (cursor.moveToFirst()) {
									getContentResolver().delete(Calls.CONTENT_URI, Calls._ID + "=?",
											new String[] { cursor.getString(cursor.getColumnIndex(Calls._ID)) });
									getContentResolver().unregisterContentObserver(this);
								}
							}
						});
						return;
					}
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

			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			private void showBlockBlacklistNumberNotification(final String incomingNumber) {
				Notification notification = new Notification.Builder(PhoneNumberAttributionService.this).setTicker("block a number")
						.setContentTitle("block a number in blacklist").setContentText(incomingNumber).setSubText("calls block")
						.setContentInfo(String.format(DateUtils.isToday(System.currentTimeMillis()) ? "%tT" : "%1$tF %1$tT", Calendar.getInstance()))
						.setAutoCancel(true).setDefaults(Notification.DEFAULT_LIGHTS)
						.setLargeIcon(BitmapFactory.decodeResource(getResources(), Feature.PHONE_GUARD.getIconId()))
						.setSmallIcon(android.R.drawable.star_on).setWhen(System.currentTimeMillis()).build();
				NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				notificationManager.notify(1, notification);
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

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	protected void showNotification(String incomingNumber) {
		Intent intent = new Intent(this, BlacklistActivity.class);
		intent.putExtra(INCOMING_NUMBER, incomingNumber);
		Notification notification = new Notification.Builder(this).setTicker("block a call").setContentTitle("catch a call ringing one time")
				.setContentText(incomingNumber).setSubText("click to add to blacklist")
				.setContentInfo(String.format(DateUtils.isToday(System.currentTimeMillis()) ? "%tT" : "%1$tF %1$tT", Calendar.getInstance()))
				.setAutoCancel(true).setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT))
				.setDefaults(Notification.DEFAULT_LIGHTS).setLargeIcon(BitmapFactory.decodeResource(getResources(), Feature.PHONE_GUARD.getIconId()))
				.setSmallIcon(android.R.drawable.star_on).setWhen(System.currentTimeMillis()).build();
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(0, notification);
	}

	protected void endCall() {
		try {
			Method getServiceMethod = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
			getServiceMethod.setAccessible(true);
			IBinder binder = (IBinder) getServiceMethod.invoke(null, TELEPHONY_SERVICE);
			ITelephony.Stub.asInterface(binder).endCall();
		} catch (Exception e) {
			Log.i(TAG, "here:" + e);
			Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
			sendOrderedBroadcast(intent, "android.permission.CALL_PRIVILEGES");
		}
	}

	protected boolean isInBlacklist(String incomingNumber) {
		return dao.isExists(incomingNumber);
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
