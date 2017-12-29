package lightup_key.bluetooth.android.iconnexys.bluetooth_lightup_key.intents;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import lightup_key.bluetooth.android.iconnexys.bluetooth_lightup_key.MainActivity;
import lightup_key.bluetooth.android.iconnexys.bluetooth_lightup_key.R;

public class ProximityIntentReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 1000;
    private static final String PROXIMITY_CHANNEL = "proximity_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(getClass().getSimpleName(), "Received intent: " + intent.toString());

        String key = LocationManager.KEY_PROXIMITY_ENTERING;
        Boolean entering = intent.getBooleanExtra(key, false);
        Log.d(getClass().getSimpleName(), entering? "entering":"exiting");
        StringBuilder message = new StringBuilder("You are near your point of interest\n");
        message.append("Action: ").append(intent.getAction()).append("\n");
        message.append("Type: ").append(intent.getType()).append("\n");
        message.append("DateString: ").append(intent.getDataString()).append("\n");
        message.append("Alert: ").append(entering? "entering":"exiting").append("\n");
        createNotification(context, message.toString());
    }

    private NotificationManager notificationManager;

    public void createNotification(Context context, String message) {
        final int NOTIFY_ID = 1002;

        // There are hardcoding only for show it's just strings
        String name = "my_package_channel";
        String id = "my_package_channel_1"; // The user-visible name of the channel.
        String description = "my_package_first_channel"; // The user-visible description of the channel.

        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        if (notificationManager == null) {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = notificationManager.getNotificationChannel(id);
                if (mChannel == null) {
                    mChannel = new NotificationChannel(id, name, importance);
                    mChannel.setDescription(description);
                    mChannel.enableVibration(true);
                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    notificationManager.createNotificationChannel(mChannel);
                }
                builder = new NotificationCompat.Builder(context, id);

                intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                builder.setContentTitle(message)  // required
                        .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
                        .setContentText(context.getString(R.string.app_name))  // required
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setTicker(message)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            } else {
                builder = new NotificationCompat.Builder(context);
                intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

                builder.setContentTitle(message)                           // required
                        .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
                        .setContentText(context.getString(R.string.app_name))  // required
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setTicker(message)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setPriority(Notification.PRIORITY_HIGH);
            } // else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            Notification notification = builder.build();
            notificationManager.notify(NOTIFY_ID, notification);
        } else {
            Log.e(getClass().getSimpleName(), "Notification manager not found");
        }
    }
}