/*
 * Copyright (C) 2014 The Android Open Source Project
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
package lightup_key.bluetooth.android.iconnexys.bluetooth_lightup_key;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Storage for geofence values, implemented in SharedPreferences.
 */
public class GeofenceStore {

    // Keys for flattened geofences stored in SharedPreferences.
    private static final String KEY_PREFIX = "lightup_key.bluetooth.android.iconnexys.bluetooth_lightup_key.KEY";
    private static final String KEY_ID = KEY_PREFIX +"_ID";
    private static final String KEY_LATITUDE = KEY_PREFIX +"_LATITUDE";
    private static final String KEY_LONGITUDE = KEY_PREFIX +"_LONGITUDE";
    private static final String KEY_RADIUS = KEY_PREFIX +"_RADIUS";
    private static final String KEY_EXPIRATION_DURATION = KEY_PREFIX + "_EXPIRATION_DURATION";
    private static final String KEY_TRANSITION_TYPE = KEY_PREFIX + "_TRANSITION_TYPE";

    // Invalid values, used to test geofence storage when retrieving geofences.
    private static final long INVALID_LONG_VALUE = -999l;
    private static final float INVALID_FLOAT_VALUE = -999.0f;
    private static final int INVALID_INT_VALUE = -999;

    // The SharedPreferences object in which geofences are stored.
    private final SharedPreferences sharedPreferences;
    // The name of the SharedPreferences.
    private static final String SHARED_PREFERENCES = "SharedPreferences";
    // Internal List of Geofence objects. In a real app, these might be provided by an API based on
    // locations within the user's proximity.
    private List<GeofenceEntry> geofenceList;

    /**
     * Create the SharedPreferences storage with private access only.
     */
    public GeofenceStore(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        geofenceList = populateList(sharedPreferences);
    }

    @NonNull
    private List<GeofenceEntry> populateList(SharedPreferences sharedPreferences) {
        List<GeofenceEntry> list = new ArrayList<>();
        for (String key: sharedPreferences.getAll().keySet())  {
            Log.d(getClass().getSimpleName(), "Shared preference contains key: " + key);
            if (key.endsWith(KEY_ID)) {
                Log.d(getClass().getSimpleName(), "Found geofence id: " + key);
                GeofenceEntry geofence = getGeofenceEntry(sharedPreferences.getString(key, null));
                if (geofence != null) {
                    list.add(geofence);
                }
            }
        };
        return list;
    }

    /**
     * Returns a stored geofence by its id, or returns null if it's not found.
     * @param id The ID of a stored geofence.
     * @return A GeofenceEntry defined by its center and radius, or null if the ID is invalid.
     */
    public GeofenceEntry getGeofenceEntry(String id) {
        if (id != null)  {
            // Get the latitude for the geofence identified by id, or INVALID_FLOAT_VALUE if it doesn't
            // exist (similarly for the other values that follow).
            double latitude = sharedPreferences.getFloat(getGeofenceFieldKey(id, KEY_LATITUDE), INVALID_FLOAT_VALUE);
            double longitude = sharedPreferences.getFloat(getGeofenceFieldKey(id, KEY_LONGITUDE), INVALID_FLOAT_VALUE);
            float radius = sharedPreferences.getFloat(getGeofenceFieldKey(id, KEY_RADIUS), INVALID_FLOAT_VALUE);
            long expirationDuration = sharedPreferences.getLong(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION), INVALID_LONG_VALUE);
            int transitionType = sharedPreferences.getInt(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE), INVALID_INT_VALUE);
            // If none of the values is incorrect, return the object.
            if (latitude != INVALID_FLOAT_VALUE
                    && longitude != INVALID_FLOAT_VALUE
                    && radius != INVALID_FLOAT_VALUE
                    && expirationDuration != INVALID_LONG_VALUE
                    && transitionType != INVALID_INT_VALUE) {
                return new GeofenceEntry(id, latitude, longitude, radius, expirationDuration, transitionType);
            }
            // Otherwise, return null.
        }
        return null;
    }

    /**
     * Save a geofence.
     * @param geofence The GeofenceEntry with the values you want to save in SharedPreferences.
     */
    public void setGeofence(String id, GeofenceEntry geofence) {
        // Get a SharedPreferences editor instance. Among other things, SharedPreferences
        // ensures that updates are atomic and non-concurrent.
        SharedPreferences.Editor prefs = sharedPreferences.edit();
        // Write the Geofence values to SharedPreferences.
        prefs.putString(getGeofenceFieldKey(id, KEY_ID), id);
        prefs.putFloat(getGeofenceFieldKey(id, KEY_LATITUDE), (float) geofence.getLatitude());
        prefs.putFloat(getGeofenceFieldKey(id, KEY_LONGITUDE), (float) geofence.getLongitude());
        prefs.putFloat(getGeofenceFieldKey(id, KEY_RADIUS), geofence.getRadius());
        prefs.putLong(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION), geofence.getExpirationDuration());
        prefs.putInt(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE), geofence.getTransitionType());
        // Commit the changes.
        prefs.apply();
        geofenceList.add(geofence);
    }

    /**
     * Remove a flattened geofence object from storage by removing all of its keys.
     */
    public void clearGeofence(String id) {
        SharedPreferences.Editor prefs = sharedPreferences.edit();
        prefs.remove(getGeofenceFieldKey(id, KEY_LATITUDE));
        prefs.remove(getGeofenceFieldKey(id, KEY_LONGITUDE));
        prefs.remove(getGeofenceFieldKey(id, KEY_RADIUS));
        prefs.remove(getGeofenceFieldKey(id, KEY_EXPIRATION_DURATION));
        prefs.remove(getGeofenceFieldKey(id, KEY_TRANSITION_TYPE));
        prefs.apply();
    }

    /**
     * Given a Geofence object's ID and the name of a field (for example, KEY_LATITUDE), return
     * the key name of the object's values in SharedPreferences.
     * @param id The ID of a Geofence object.
     * @param fieldName The field represented by the key.
     * @return The full key name of a value in SharedPreferences.
     */
    private String getGeofenceFieldKey(String id, String fieldName) {
        return KEY_PREFIX + "_" + id + "_" + fieldName;
    }

    public List<GeofenceEntry> getGeofenceList() {
        return geofenceList;
    }

    public void clearAll() {
        clearAllSharedPreferences();
        geofenceList.clear();
    }

    public void clearAllSharedPreferences()  {
        SharedPreferences.Editor prefs = sharedPreferences.edit();
        for (String key: sharedPreferences.getAll().keySet())  {
            if (key.startsWith(KEY_PREFIX)) {
                prefs.remove(key);
            }
        }
        prefs.apply();
    }
}