package com.codebase.locationgpstest.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

public class LocationUpdateNotifier extends BroadcastReceiver {

	public static String BROADCAST_LOCATION_UPDATER_STATE = "BROADCAST_LOCATION_UPDATER_STATE";
	public static String NOTIFY_LOCATION_UPDATER_AVAILABLE = "BROADCAST_LOCATION_UPDATER_AVAILABLE";
	
	public interface LocationUpdateNotifierListner {

		public void onLocationUpdaterStateChange(boolean state);
	}

	public LocationUpdateNotifierListner locationListner;

	@Override
	public void onReceive(Context arg0, Intent intent) {
		// TODO Auto-generated method stub

		if (intent.getAction().equals(
				LocationUpdateNotifier.BROADCAST_LOCATION_UPDATER_STATE)) {
			Bundle bundle = intent.getExtras();

			boolean locationUpdaterAvailable = bundle
					.getBoolean(LocationUpdateNotifier.NOTIFY_LOCATION_UPDATER_AVAILABLE);
			if (locationListner != null) {
				if (locationUpdaterAvailable) {
					locationListner.onLocationUpdaterStateChange(true);
				} else {
					locationListner.onLocationUpdaterStateChange(false);
				}

			}

		}
	}
	public void onLocationUpdateNotifier(LocationUpdateNotifierListner locationUpdateListner)
	{
		this.locationListner = locationUpdateListner;
	}
	public void registerLocationUpdater(Context context)
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(LocationUpdateNotifier.BROADCAST_LOCATION_UPDATER_STATE);
		context.registerReceiver(this, filter);
	}
	
	public void unregisterLocationUpdater(Context context)
	{
		context.unregisterReceiver(this);
	}
	public static void notifyLocationAvailable(Context context) {
		Intent intent = new Intent();
		intent.setAction(BROADCAST_LOCATION_UPDATER_STATE);
		intent.putExtra(NOTIFY_LOCATION_UPDATER_AVAILABLE, true);
		context.sendBroadcast(intent);
	}

	public static void notifyLocationUnAvailable(Context context) {
		Intent intent = new Intent();
		intent.setAction(BROADCAST_LOCATION_UPDATER_STATE);
		intent.putExtra(NOTIFY_LOCATION_UPDATER_AVAILABLE, false);
		context.sendBroadcast(intent);
	}
}
