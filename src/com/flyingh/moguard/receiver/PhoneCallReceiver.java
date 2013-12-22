package com.flyingh.moguard.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.flyingh.moguard.SecurityActivity;
import com.flyingh.moguard.util.Const;

public class PhoneCallReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String resultData = getResultData();
		SharedPreferences sp = context.getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE);
		if (sp.getString(Const.PHONE_CALL_NUMBER_TO_START_SECURITY, Const.DEFAULT_PHONE_NUMBER_TO_START_SECURITY).equals(resultData)) {
			Intent securityIntent = new Intent(context, SecurityActivity.class);
			securityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(securityIntent);
			setResultData(null);
		}
	}

}
