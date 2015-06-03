package com.codebase;

import android.app.Activity;
import android.os.Bundle;
import com.nineoldandroids.animation.*;
import com.facebook.FacebookSdk;
import com.codebase.locationgpstest.utils.LocationUpdateNotifier;
import com.codebase.locationgpstest.utils.LocationUpdateNotifier.LocationUpdateNotifierListner;
import com.codebase.locationgpstest.utils.LocationUpdater;
import com.codebase.locationgpstest.utils.LocationUpdater.LocationFoundListner;
import com.codebase.locationgpstest.utils.core.ResourceChecker;

public class MainActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);


    }
}
