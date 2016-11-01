/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

/**
 * A class that handles everything about location.
 */
public class LocationManager {
    private static final String TAG = "LocationManager";

    private Context mContext;
    private Listener mListener;
    private android.location.LocationManager mLocationManager;
    private boolean mRecordLocation;

    LocationListener [] mLocationListeners = new LocationListener[] {
            new LocationListener(android.location.LocationManager.GPS_PROVIDER),
            new LocationListener(android.location.LocationManager.NETWORK_PROVIDER)
    };

    public interface Listener {
        public void showGpsOnScreenIndicator(boolean hasSignal);
        public void hideGpsOnScreenIndicator();
   }

    public LocationManager(Context context) {
        mContext = context;
    }

    public LocationManager(Context context, Listener listener) {
        mContext = context;
        mListener = listener;
    }

    public Location getCurrentLocation() {
        // go in best to worst order
        Log.d(TAG, "getCurrentLocation mLocationListeners size:" + mLocationListeners.length);
        for (int i = 0; i < mLocationListeners.length; i++) {
            Location l = mLocationListeners[i].current();
            Log.d(TAG, "getCurrentLocation Location l= " + l);
            if (l != null) return l;
        }
        Log.d(TAG, "getCurrentLocation No location received yet.");
        return null;
    }

    public void recordLocation(boolean recordLocation) {
        Log.d(TAG, "recordLocation =" + recordLocation);
        if (recordLocation) {
            startReceivingLocationUpdates();
        } else {
            stopReceivingLocationUpdates();
        }
    }

    private void startReceivingLocationUpdates() {
        if (mLocationManager == null) {
            mLocationManager = (android.location.LocationManager)
                    mContext.getSystemService(Context.LOCATION_SERVICE);
        }
        Log.d(TAG, "startReceivingLocationUpdates mLocationManager=" + mLocationManager);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String bestProvider = mLocationManager.getBestProvider(criteria, true);
        if (bestProvider == null) {
            Log.d(TAG, "no provider avaiable!");
            return;
        }
        Location location = mLocationManager.getLastKnownLocation(bestProvider);
        Log.d(TAG, "startReceivingLocationUpdates location =" + location);
        if (location != null) {
            mLocationListeners[0].onLocationChanged(location);
        }

        if (mLocationManager != null) {
            try {
                mLocationManager.requestLocationUpdates(
                        android.location.LocationManager.NETWORK_PROVIDER,
                        1000,
                        0F,
                        mLocationListeners[1]);
            } catch (SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "provider does not exist " + ex.getMessage());
            }
            try {
                Log.d(TAG, "startReceivingLocationUpdates requestLocationUpdates will");
                mLocationManager.requestLocationUpdates(
                        android.location.LocationManager.GPS_PROVIDER,
                        1000,
                        1,
                        mLocationListeners[0]);
                Log.d(TAG, "startReceivingLocationUpdates requestLocationUpdates end");
                if (mListener != null) mListener.showGpsOnScreenIndicator(false);
            } catch (SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "provider does not exist " + ex.getMessage());
            }
            Log.d(TAG, "startReceivingLocationUpdates");
        }
    }

    private void stopReceivingLocationUpdates() {
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    Log.d(TAG, "stopReceivingLocationUpdates i = " + i);
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
            Log.d(TAG, "stopReceivingLocationUpdates mLocationManager not null");
        }
        Log.d(TAG, "stopReceivingLocationUpdates!");
        if (mListener != null) mListener.hideGpsOnScreenIndicator();
    }

    private class LocationListener
            implements android.location.LocationListener {
        Location mLastLocation;
        boolean mValid = false;
        String mProvider;

        public LocationListener(String provider) {
            mProvider = provider;
            mLastLocation = new Location(mProvider);
        }

        @Override
        public void onLocationChanged(Location newLocation) {
            Log.d(TAG, "tLatitude:" + newLocation.getLatitude() + ",Longitude:" + newLocation.getLongitude());
            if (newLocation.getLatitude() == 0.0
                    && newLocation.getLongitude() == 0.0) {
                // Hack to filter out 0.0,0.0 locations
                return;
            }
            // If GPS is available before start camera, we won't get status
            // update so update GPS indicator when we receive data.
            if (mListener != null && mRecordLocation &&
                    android.location.LocationManager.GPS_PROVIDER.equals(mProvider)) {
                mListener.showGpsOnScreenIndicator(true);
            }
            if (!mValid) {
                Log.d(TAG, "Got first location.");
            }
            Log.d(TAG, "set(newLocation) will");
            mLastLocation.set(newLocation);
            Log.d(TAG, "set(newLocation) end");
            mValid = true;
            Log.d(TAG, "mValid=" + mValid);
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            mValid = false;
            Log.d(TAG, "onProviderDisabled mValid=" + mValid);
        }

        @Override
        public void onStatusChanged(
                String provider, int status, Bundle extras) {
            switch(status) {
                case LocationProvider.OUT_OF_SERVICE:
                case LocationProvider.TEMPORARILY_UNAVAILABLE: {
                    mValid = false;
                    if (mListener != null && mRecordLocation &&
                            android.location.LocationManager.GPS_PROVIDER.equals(provider)) {
                        mListener.showGpsOnScreenIndicator(false);
                    }
                    break;
                }
            }
            Log.d(TAG, "onStatusChanged mValid=" + mValid);
        }

        public Location current() {
            Log.d(TAG, "Location current() mValid=" + mValid);
            return mLastLocation;
        }
    }
}
