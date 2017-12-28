package lightup_key.bluetooth.android.iconnexys.bluetooth_lightup_key;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.location.Geofence;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import lightup_key.bluetooth.android.iconnexys.bluetooth_lightup_key.intents.ProximityIntentReceiver;

public class ProximityAlertActivity extends BaseActivity {

    private static final long MINIMUM_DISTANCECHANGE_FOR_UPDATE = 1; // in Meters
    private static final long MINIMUM_TIME_BETWEEN_UPDATE = 1000; // in Milliseconds
    private static final float ANDROID_BUILDING_RADIUS_METERS = 60.0f;
    private static final String PROXIMITY_ALERT_INTENT = "lightup_key.bluetooth.android.iconnexys.ProximityAlert";
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("##.########");

    private LocationManager locationManager;
    private GeofenceStore geofenceStore;
    private EditText latitudeEditText;
    private EditText longitudeEditText;
    private EditText pointIdEditText;
    private Button findCoordinatesButton;
    private Button savePointButton;
    private Button clearPointsButton;
    private ListView listOfPoints;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.coordinate_selection);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        if (checkLocationPermission()) {
            showText(getString(R.string.proximity_no_location_permission));
            return;
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MINIMUM_TIME_BETWEEN_UPDATE,
                MINIMUM_DISTANCECHANGE_FOR_UPDATE,
                new LocationChangeListener()
        );

        latitudeEditText = findViewById(R.id.point_latitude);
        longitudeEditText = findViewById(R.id.point_longitude);
        pointIdEditText = findViewById(R.id.point_id);
        findCoordinatesButton = findViewById(R.id.find_coordinates_button);
        savePointButton = findViewById(R.id.save_point_button);
        clearPointsButton = findViewById(R.id.clear_points_button);

        findCoordinatesButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                populateCoordinatesFromLastKnownLocation();
            }
        });

        geofenceStore = new GeofenceStore(this);
        savePointButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProximityAlertPoint();
            }
        });
        clearPointsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllGeofences();
            }
        });

        final ArrayAdapter<GeofenceEntry> adapter = new ArrayAdapter<>(this, R.layout.list_devices, geofenceStore.getGeofenceList());
        listOfPoints = findViewById(R.id.listPoints);
        listOfPoints.setAdapter(adapter);
    }


    private void clearAllGeofences() {
        showText(getString(R.string.proximity_clear_all_locations));
        geofenceStore.clearAll();
        listOfPoints.invalidateViews();
    }

    private void saveProximityAlertPoint() {
        showText(getString(R.string.proximity_save_location));

        final String geofenceId = pointIdEditText.getText().toString();
        final String latitudeText = latitudeEditText.getText().toString();
        final String longitudeText = longitudeEditText.getText().toString();
        double latitude;
        double longitude;
        try {
            latitude = Double.parseDouble(latitudeText);
            longitude = Double.parseDouble(longitudeText);
        } catch (NumberFormatException e)  {
            showText(getString(R.string.proximity_parse_error));
            return;
        }
        final GeofenceEntry geofenceEntry = new GeofenceEntry(geofenceId, latitude, longitude,
                ANDROID_BUILDING_RADIUS_METERS, Geofence.NEVER_EXPIRE, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);
        geofenceStore.setGeofence(geofenceId, geofenceEntry);
        listOfPoints.invalidateViews();
        addProximityAlert(geofenceEntry);
    }

    private void addProximityAlert(GeofenceEntry geofenceEntry) throws SecurityException {
        try {
            Intent intent = new Intent(PROXIMITY_ALERT_INTENT);
            PendingIntent pendingProximityIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            locationManager.addProximityAlert(
                    geofenceEntry.getLatitude(),
                    geofenceEntry.getLongitude(),
                    geofenceEntry.getRadius(),
                    geofenceEntry.getExpirationDuration(),
                    pendingProximityIntent // will be used to generate an Intent to fire when entry to or exit from the alert region is detected
            );
            IntentFilter filter = new IntentFilter(PROXIMITY_ALERT_INTENT);
            registerReceiver(new ProximityIntentReceiver(), filter);
        } catch (IllegalArgumentException exception)  {
            showText(exception.getMessage());
        }
    }

    private void populateCoordinatesFromLastKnownLocation() {
        showText(getString(R.string.proximity_finding_location));
        if (checkLocationPermission()) return;
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        pointIdEditText.setText(getString(R.string.proximity_name_your_location));
        if (location != null) {
            latitudeEditText.setText(NUMBER_FORMAT.format(location.getLatitude()));
            longitudeEditText.setText(NUMBER_FORMAT.format(location.getLongitude()));
        } else {
            showText(getString(R.string.proximity_location_failed));
        }
        pointIdEditText.selectAll();
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return true;
        }
        return false;
    }

    public class LocationChangeListener implements LocationListener {
        public void onLocationChanged(final Location location) {
            final StringBuilder message = new StringBuilder();
            final List<GeofenceEntry> geofenceList = geofenceStore.getGeofenceList();
            if (geofenceList.size() > 0) {
                for (GeofenceEntry geofence : geofenceList) {
                    final Location pointLocation = geofence.toLocation();
                    final float distanceInMeters = location.distanceTo(pointLocation);
                    final String distanceMessage = String.format(getString(R.string.proximity_distance_from_location), geofence.getId(), distanceInMeters);
                    message.append(distanceMessage).append("\n");
                }
                showText(message.toString());
            }
        }

        public void onStatusChanged(String s, int i, Bundle b) {
        }

        public void onProviderDisabled(String s) {
        }

        public void onProviderEnabled(String s) {
        }
    }

}
