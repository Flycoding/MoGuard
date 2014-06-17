package com.flyingh.moguard.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationUtil {

	public static String getLocation(final Context context) {
		final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		final SharedPreferences sp = context.getSharedPreferences(Const.CONFIG_FILE_NAME, Context.MODE_PRIVATE);
		locationManager.requestLocationUpdates(locationManager.getBestProvider(configCriteria(), true), 60000, 50, new LocationListener() {

			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onProviderDisabled(String provider) {
				locationManager.removeUpdates(this);
			}

			@Override
			public void onLocationChanged(Location location) {
				double longitude = location.getLongitude();
				double latitude = location.getLatitude();
				sp.edit().putString(Const.PREVIOUS_LOCATION, longitude + "," + latitude).commit();
			}
		});
		return sp.getString(Const.PREVIOUS_LOCATION, null);
	}

	private static Criteria configCriteria() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
		criteria.setSpeedRequired(true);
		return criteria;
	}
}
