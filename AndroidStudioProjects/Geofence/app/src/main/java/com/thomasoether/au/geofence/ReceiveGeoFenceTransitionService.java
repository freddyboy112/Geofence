package com.thomasoether.au.geofence;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class ReceiveGeoFenceTransitionService extends IntentService {

    // Notification channel ID, needed for API 26 and higher
    public final static String CHANNEL_ID = "default";



    public ReceiveGeoFenceTransitionService() {
        super("ReceiveGeoFenceTransitionService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);

        if (event.hasError()) {
            // TODO: Handle error
        } else {
            int transition = event.getGeofenceTransition();

            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Bundle extras = intent.getExtras();

                // Create notification intent
                Intent notificationIntent = new Intent(this, DistanceToLocationActivity.class);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                notificationIntent.putExtra("lat", extras.getDouble("lat"));
                notificationIntent.putExtra("long", extras.getDouble("long"));

                PendingIntent contentIntent = PendingIntent.getActivity(this.getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification notification = new NotificationCompat.Builder(this.getApplicationContext(), CHANNEL_ID)
                        .setContentTitle("You are near " + extras.getString("location"))
                        .setContentText(extras.getString("message"))
                        .setTicker("You're near the Hovedbanegaard. Do you want to see how to get to the CS building?")
                        .setContentIntent(contentIntent)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .build();

                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                Log.d(MainActivity.TAG, "Notification created");

                NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
                manager.notify(1, notification);

                Log.d(MainActivity.TAG, "Notified!");

            } else {
                // TODO: Handle invalid transition
            }
        }
    }

}