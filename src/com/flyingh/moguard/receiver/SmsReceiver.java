package com.flyingh.moguard.receiver;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import com.flyingh.moguard.R;
import com.flyingh.moguard.util.Const;
import com.flyingh.moguard.util.LocationUtil;

public class SmsReceiver extends BroadcastReceiver {
	private static final String TAG = "SmsReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Object[] pdus = (Object[]) intent.getExtras().get("pdus");
		for (Object pdu : pdus) {
			SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
			String messageBody = message.getMessageBody();
			switch (messageBody) {
			case Const.REQUEST_LOCATION_CODE:
				requestLocation(context);
				abortBroadcast();
				break;
			case Const.REQUEST_LOCK_CODE:
				requestLock(context);
				abortBroadcast();
				break;
			case Const.REQUEST_WIPE_CODE:
				requestWipe(context);
				abortBroadcast();
				break;
			case Const.REQUEST_ALARM_CODE:
				requestAlarm(context);
				abortBroadcast();
				break;
			default:
				break;
			}
			return;
		}
	}

	private void requestAlarm(Context context) {
		MediaPlayer player = MediaPlayer.create(context, R.raw.hay);
		player.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				mp.release();
				mp = null;
			}
		});
		player.setVolume(1, 1);
		player.start();

	}

	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private void requestWipe(Context context) {
		DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
	}

	private void requestLock(Context context) {
		DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		dpm.resetPassword(Const.DEFAULT_LOCK_PASSWORD, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
		dpm.lockNow();
	}

	private void requestLocation(Context context) {
		String location = LocationUtil.getLocation(context);
		if (TextUtils.isEmpty(location)) {
			return;
		}
		Log.i(TAG, location);
		String boundPhoneNumber = context.getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE).getString(
				Const.BOUND_PHONE_NUMBER, "5556");
		if (TextUtils.isEmpty(boundPhoneNumber)) {
			return;
		}
		SmsManager.getDefault().sendTextMessage(boundPhoneNumber, null, location, null, null);
	}

}
