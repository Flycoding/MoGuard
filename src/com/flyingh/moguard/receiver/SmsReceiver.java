package com.flyingh.moguard.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

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
			if (!Const.REQUEST_LOCATION_CODE.equalsIgnoreCase(messageBody)) {
				return;
			}
			String location = LocationUtil.getLocation(context);
			if (TextUtils.isEmpty(location)) {
				return;
			}
			Log.i(TAG, location);
			String boundPhoneNumber = context.getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE).getString(Const.BOUND_PHONE_NUMBER,
					"5556");
			if (TextUtils.isEmpty(boundPhoneNumber)) {
				return;
			}
			SmsManager.getDefault().sendTextMessage(boundPhoneNumber, null, location, null, null);
		}
	}

}
