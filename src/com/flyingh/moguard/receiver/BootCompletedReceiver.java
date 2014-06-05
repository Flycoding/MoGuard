package com.flyingh.moguard.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import com.flyingh.moguard.service.PhoneNumberAttributionService;
import com.flyingh.moguard.util.Const;

public class BootCompletedReceiver extends BroadcastReceiver {
	private static final String TAG = "BootCompletedReceiver";

	private SharedPreferences sp;

	@Override
	public void onReceive(Context context, Intent intent) {
		sp = context.getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE);
		if (isStarted() && isSimBound() && hasBoundPhoneNumber()) {
			SmsManager.getDefault().sendTextMessage(sp.getString(Const.BOUND_PHONE_NUMBER, null), null,
					"the SIM card is changed", null, null);
		}
		if (isPhoneNumberAttributionStatusStarted()) {
			Log.i(TAG, "start phone number attribution service");
			context.startService(new Intent(context, PhoneNumberAttributionService.class));
		}
	}

	private boolean isPhoneNumberAttributionStatusStarted() {
		return sp.getBoolean(Const.PHONE_NUMBER_ATTRIBUTION_SERVICE_STARTED, false);
	}

	private boolean hasBoundPhoneNumber() {
		return !TextUtils.isEmpty(sp.getString(Const.BOUND_PHONE_NUMBER, null));
	}

	private boolean isSimBound() {
		return sp.getBoolean(Const.IS_SIM_SERIAL_NUMBER_BOUND, false);
	}

	private boolean isStarted() {
		return sp.getBoolean(Const.STATUS_STARTED, false);
	}

}
