package com.codebase.locationgpstest.utils.core;


import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;

public class ResourceChecker {

        /* FIELDS */

        private Activity activity;
        private Pass pass;
        private List<Resource> resourcesList;

        /* TYPES */

        public enum Resource {
                NETWORK, GPS, BLUETOOTH
        }

        public static abstract class Pass {
                public abstract void pass();
        }

        /* API */

        public ResourceChecker(Activity activity) {
                this.activity = activity;
        }

        public void check(Resource... resources) {
                resourcesList = Arrays.asList(resources);
                if (resourcesList.contains(Resource.GPS) && !isGPSActivated(activity)) {
                        new AlertDialog.Builder(activity).setMessage("GPS required.").setCancelable(false).setPositiveButton("GPS", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                      ResourceChecker.openGPSSettings(activity);
                                }
                        }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        activity.finish();
                                }
                        }).create().show();
                } else if (resourcesList.contains(Resource.NETWORK) && !isNetworkActivated(activity)) {
                        new AlertDialog.Builder(activity).setMessage("Network required.").setCancelable(false).setPositiveButton("3G", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                        Intent intent = new Intent(Intent.ACTION_MAIN);
                                        intent.setClassName("com.android.phone", "com.android.phone.Settings");
                                        activity.startActivity(intent);
                                }
                        }).setNeutralButton("WiFi", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                        activity.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                                }
                        }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        activity.finish();
                                }
                        }).create().show();
                } else if (resourcesList.contains(Resource.BLUETOOTH) && !isBluetoothActivated(activity)) {
                        new AlertDialog.Builder(activity).setMessage("Bluetooth required.").setCancelable(false).setPositiveButton("Bluetooth",
                                        new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                        activity.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                                                }
                                        }).setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        activity.finish();
                                }
                        }).create().show();
                }else{
                	if(pass!=null)
                   pass.pass();
                }
        }

        public ResourceChecker pass(Pass pass) {
                this.pass = pass;
                return this;
        }

        /* PRIVATE */

        public  static boolean isGPSActivated(Context context) {
        	
        		
                return ((LocationManager) context.getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
        }

        public static boolean isBluetoothActivated(Context context) {
                return BluetoothAdapter.getDefaultAdapter().isEnabled();
        }

        public static boolean isNetworkActivated(Context context) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
        }
        
        public static void turnGPSOn(Context context)
    	{
    	     Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
    	     intent.putExtra("enabled", true);
    	     context.sendBroadcast(intent);

    	    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
    	    if(!provider.contains("gps")){ //if gps is disabled
    	        final Intent poke = new Intent();
    	        poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider"); 
    	        poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
    	        poke.setData(Uri.parse("3")); 
    	        context.sendBroadcast(poke);


    	    }
    	}
    	// automatic turn off the gps
    	public static void turnGPSOff(Context context)
    	{
    	    String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
    	    if(provider.contains("gps")){ //if gps is enabled
    	        final Intent poke = new Intent();
    	        poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
    	        poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
    	        poke.setData(Uri.parse("3")); 
    	        poke.putExtra("enabled", false);
    	        context.sendBroadcast(poke);
    	    }
    	}
    	
    	public static void openGPSSettings(Activity activity)
    	{
    		 activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    	}
        
}