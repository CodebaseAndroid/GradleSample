package com.codebase.locationgpstest.utils;

/*Gps tracker Algorithm

 1. Initialize
 2. Scanning
 a 	clear all handler(stop all timers) and remove updates(stop updates)
 b	check gps or wifi to use for location updates
 c	set one provider and scan and start z timer(for step d)
 d  if  a "z" amount of time reaches switch provider goto "a" and "c" not b

 e	if scan found 

 cancel all timers
 wait for another update
 start timer x(for step f)




 f. if x amount of times reaches redo from a

 */

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.codebase.locationgpstest.utils.core.ResourceChecker;

public class LocationUpdater extends Service implements LocationListener {

	public interface LocationFoundListner {

		public void onLocationUpdate(Location location);

	}

	public static String TAG_LOCATION_UPDATER = "TAG_LOCATION_UPDATER";
	public static LocationUpdater locationUpdaterSingleInstance = null;

	private Context appContext;

	private String currentLocationUpdateProvider;
	private long DELAY_IN_NEXT_SCAN = 1000 * 30 * 1;
	private long LOCATION_UPDATE_FREQUENCY = 0;

	private float LOCATION_UPDATE_MIN_DISTANCE = 10;
	private LocationFoundListner locationListener;
	private LocationManager locationManager;
	private Location oldLocation;
	private Handler locationUpdateHandler;

	private LocationUpdater(Context context) {
		// TODO Auto-generated constructor stub
		this.appContext = context;
		this.locationManager = (LocationManager) appContext
				.getSystemService(LOCATION_SERVICE);
		this.locationUpdateHandler = new Handler();
		Log.e(TAG_LOCATION_UPDATER, "locationUpdateHandler Constructor");

	}

	private boolean isLocationBeProvided(Location newLocation) {
		if (oldLocation == null) {
			oldLocation = newLocation;
			return true;
		}
		float distance = oldLocation.distanceTo(newLocation);
		if (distance >= LOCATION_UPDATE_MIN_DISTANCE)

		{
			oldLocation = newLocation;
			return true;
		}

		return false;

	}

	private String getAvailableLocationProvider() {

		currentLocationUpdateProvider = LocationManager.NETWORK_PROVIDER;
		if (this.isNetworkLocationUpdatesPossible()) {
			currentLocationUpdateProvider = LocationManager.NETWORK_PROVIDER;
		} else if (this.isGPSLocationUpdatesPossible()) {
			currentLocationUpdateProvider = LocationManager.GPS_PROVIDER;
		}
		return currentLocationUpdateProvider;
	}

	private Location getBestLocationOnUpdate(Location aLocation) {
		Location locationAvailable = getLocationFromAvailableProvider();

		if (locationAvailable == null)
			return aLocation;
		if (aLocation.getAccuracy() >= locationAvailable.getAccuracy())
			return aLocation;
		return locationAvailable;

	}

	private Location getLocationFromAvailableProvider() {
		Location lastLocation = new Location(LocationManager.NETWORK_PROVIDER);
		Location gpsLocation = null, networkLocation = null;

		gpsLocation = locationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		networkLocation = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		if (gpsLocation != null && networkLocation != null) {
			lastLocation = (gpsLocation.getAccuracy() > networkLocation
					.getAccuracy()) ? gpsLocation : networkLocation;
		} else {
			lastLocation = (gpsLocation == null) ? ((networkLocation == null) ? lastLocation
					: networkLocation)
					: gpsLocation;
		}

		return lastLocation;
	}

	private String getSwitchedProvider() {

		if (currentLocationUpdateProvider == null)
			return getAvailableLocationProvider();
		if (currentLocationUpdateProvider
				.equalsIgnoreCase(LocationManager.GPS_PROVIDER)
				&& isNetworkLocationUpdatesPossible()) {
			currentLocationUpdateProvider = LocationManager.NETWORK_PROVIDER;
		} else if (isGPSLocationUpdatesPossible()) {
			currentLocationUpdateProvider = LocationManager.GPS_PROVIDER;
		}
		return currentLocationUpdateProvider;
	}

	private boolean isGPSLocationUpdatesPossible() {
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

	}

	private boolean isNetworkLocationUpdatesPossible() {

		// getting network status
		return locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Location location2 = getBestLocationOnUpdate(location);

		if (isLocationBeProvided(location2)) {

			Log.e("GPS", "Location Found");
			locationListener.onLocationUpdate(location2);
		}
		setDelayForNextScan();
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

		Log.e(TAG_LOCATION_UPDATER, "onProviderDisabled povider is " + provider);

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

		Log.e(TAG_LOCATION_UPDATER, "onProviderEnabled povider is " + provider);

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

		Log.e(TAG_LOCATION_UPDATER, "onStatusChanged povider is " + provider);

	}

	private void setDelayForNextScan() {

		stopLocationScanning();

		locationUpdateHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Log.e(TAG_LOCATION_UPDATER, "Time set for next scan ended");
				setUpScanning(false);

			}
		}, DELAY_IN_NEXT_SCAN);
	}

	private void setUpScanning(boolean forceToAutoSwitchProviderAfterTimeout) {

		stopLocationScanning();
		Log.e(TAG_LOCATION_UPDATER, "On setUpScanning()");
		String provider = (forceToAutoSwitchProviderAfterTimeout) ? getSwitchedProvider()
				: getAvailableLocationProvider();
		Log.e(TAG_LOCATION_UPDATER, "Provider is " + provider);
		if (provider != null) {
			locationManager.requestLocationUpdates(provider,
					LOCATION_UPDATE_FREQUENCY, LOCATION_UPDATE_MIN_DISTANCE,
					LocationUpdater.this);
		}

		locationUpdateHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub

				// LocationApplication.LogError(");
				Log.e(TAG_LOCATION_UPDATER,
						"Time set for force scan ended so switching provider");

				Location newLoc = getLocationFromAvailableProvider();
				if (isLocationBeProvided(newLoc)) {
					locationListener

					.onLocationUpdate(newLoc);
				}
				LocationUpdater.this.setUpScanning(true);

			}
		}, DELAY_IN_NEXT_SCAN);

	}

	private void stopLocationScanning() {
		locationUpdateHandler.removeCallbacksAndMessages(null);
		locationManager.removeUpdates(this);

	}

	public static boolean checkForGpsWithAlert(final Activity activity,
			String message, String goToSettingsButtonTitle,
			String cancelButtonTitle) {
		String aMessage = (message == null) ? "GPS required." : message;
		String aGoToSettingsButtonTitle = (goToSettingsButtonTitle == null) ? "Enable GPS"
				: goToSettingsButtonTitle;
		String aCancelButtonTitle = (cancelButtonTitle == null) ? "Cancel"
				: cancelButtonTitle;

		if (!LocationUpdater.isGPSActivated(activity)) {
			new AlertDialog.Builder(activity)
					.setMessage(aMessage)
					.setCancelable(false)
					.setPositiveButton(aGoToSettingsButtonTitle,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									LocationUpdater.openGPSSettings(activity);
									// LocationUpdater.toastGPSStatus(activity);
								}
							})
					.setNegativeButton(aCancelButtonTitle,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();

								}
							}).create().show();
		}

		return LocationUpdater.isGPSActivated(activity);
	}

	public static LocationUpdater getSharedLocationUpdater(Context context) {
		if (locationUpdaterSingleInstance == null) {
			locationUpdaterSingleInstance = new LocationUpdater(context);
		}

		return locationUpdaterSingleInstance;
	}

	public static LocationUpdater getSharedLocationUpdater() {

		return locationUpdaterSingleInstance;
	}

	public static void getUpdate(Context context,
			LocationFoundListner aLocationListener) {
		LocationUpdater locationUpdater = LocationUpdater
				.getSharedLocationUpdater(context);
		locationUpdater.locationListener = aLocationListener;
	}

	public static boolean isGPSActivated(Context context) {
		PackageManager pm = context.getPackageManager();
		boolean hasGps = pm
				.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);

		boolean hasActivated = hasGps
				&& ((LocationManager) context
						.getSystemService(Context.LOCATION_SERVICE))
						.isProviderEnabled(LocationManager.GPS_PROVIDER);

		return hasActivated;
	}

	public static void updateGpsStatus(Context context) {
		LocationUpdater locationUpdater = LocationUpdater
				.getSharedLocationUpdater();

		locationUpdater.notifyGPSActiveCallback();

	}

	public static boolean isGpsSensorPresent(Context context,
			boolean showAlert, String message) {
		PackageManager pm = context.getPackageManager();
		boolean hasGps = pm
				.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);

		if (!hasGps && showAlert) {
			new AlertDialog.Builder(context)
					.setMessage(message)
					.setCancelable(false)

					.setNegativeButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();

								}
							}).create().show();
		}
		return hasGps;
	}

	public static void openGPSSettings(Activity activity) {
		activity.startActivity(new Intent(
				Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	}

	public static void startScanning(Context context, boolean switchProvider) {
		getSharedLocationUpdater(context).setUpScanning(switchProvider);
	}

	public static void stopScanning(Context context) {
		getSharedLocationUpdater(context).stopLocationScanning();
	}

	private static void toastGPSStatus(Context context) {
		String message = "Gps is " + ResourceChecker.isGPSActivated(context);
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();

	}

	private void notifyGPSActiveCallback() {

		if (isGPSLocationUpdatesPossible()) {
			LocationUpdateNotifier.notifyLocationAvailable(appContext);
		} else {
			LocationUpdateNotifier.notifyLocationUnAvailable(appContext);
		}

	}

	public static Location getLastKnownLocation() {
		LocationUpdater locationUpdater = LocationUpdater
				.getSharedLocationUpdater();

		return locationUpdater.getLocationFromAvailableProvider();
	}

	public String ConvertPointToLocation(Location location) {

		if (location == null)
			return "";
		if (location.getLatitude() == 0 && location.getLongitude() == 0)
			return "";
		String address = "";

		Geocoder geoCoder = new Geocoder(appContext, Locale.getDefault());
		try {

			List<Address> addresses = geoCoder.getFromLocation(
					location.getLatitude(), location.getLongitude(), 1);

			if (addresses.size() > 0) {
				for (int index = 0; index < addresses.get(0)
						.getMaxAddressLineIndex(); index++)
					address += addresses.get(0).getAddressLine(index) + " ";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return address;
	}

	public static String getLastLocationName() {
		LocationUpdater locationUpdater = LocationUpdater
				.getSharedLocationUpdater();
		if (locationUpdater == null)
			return "";
		return locationUpdater.ConvertPointToLocation(LocationUpdater
				.getLastKnownLocation());
	}

}
