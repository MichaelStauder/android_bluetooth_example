package lightup_key.bluetooth.android.iconnexys.bluetooth_lightup_key;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class ProximityIntentReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 1000;
    private static final String PROXIMITY_CHANNEL = "proximity_channel";

    @Override
    public void onReceive(Context context, Intent intent) {

        String key = LocationManager.KEY_PROXIMITY_ENTERING;
        Boolean entering = intent.getBooleanExtra(key, false);
        Log.d(getClass().getSimpleName(), entering? "entering":"exiting");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, null, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PROXIMITY_CHANNEL);
        int defaults = Notification.DEFAULT_VIBRATE;
        defaults |= Notification.DEFAULT_LIGHTS;
        Notification notification = builder.setContentIntent(pendingIntent)
                .setTicker("Proximity Alert!")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true).setContentTitle("Proximity Alert!")
                .setContentText("You are near your point of interest.")
                .setAutoCancel(true)
                .setLights(Color.WHITE, 1500,1500)
                .setDefaults(defaults)
                .build();
        if (notificationManager != null) {
            Log.d(getClass().getSimpleName(), "Send notification");
            notificationManager.notify(NOTIFICATION_ID, notification);
        } else {
            Log.d(getClass().getSimpleName(), "Notification manager is null");
        }
    }
}