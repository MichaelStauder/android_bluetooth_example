package lightup_key.bluetooth.android.iconnexys.bluetooth_lightup_key;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import static android.content.ContentValues.TAG;

public class GeofenceTransitionsIntentService extends IntentService {

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public GeofenceTransitionsIntentService(String name) {
        super(name);
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Errorcode: " + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        switch (geofencingEvent.getGeofenceTransition())  {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
            // Get the geofence id triggered. Note that only one geofence can be triggered at a
            // time in this example, but in some cases you might want to consider the full list
            // of geofences triggered.
            String triggeredGeoFenceId = geofencingEvent.getTriggeringGeofences().get(0).getRequestId();
            showText("Enter geofence: " + triggeredGeoFenceId);
            break;
            case Geofence.GEOFENCE_TRANSITION_EXIT :
                showText("Exiting geofence");
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                showText("Dwell geofence");
                break;
            default:
                showText("Invalid transition type: "+ geofencingEvent.getGeofenceTransition());
                break;
        }
    }

    private void showText(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}