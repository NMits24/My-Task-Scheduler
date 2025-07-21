package com.example.groupassignment_group8;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String soundUriString = intent.getStringExtra("sound_uri");
        String appPackageName = intent.getStringExtra("app_package");
        double latitude = intent.getDoubleExtra("latitude", -1);
        double longitude = intent.getDoubleExtra("longitude", -1);

        // Start the full-screen AlarmActivity directly
        Intent fullScreenIntent = new Intent(context, AlarmActivity.class);
        fullScreenIntent.putExtra("title", title);
        fullScreenIntent.putExtra("description", description);
        fullScreenIntent.putExtra("sound_uri", soundUriString);
        fullScreenIntent.putExtra("app_package", appPackageName);
        fullScreenIntent.putExtra("latitude", latitude);
        fullScreenIntent.putExtra("longitude", longitude);
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(fullScreenIntent);

        // Also post a notification as a fallback
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannelNotification(title, description, soundUriString);
        notificationHelper.getManager().notify(1, nb.build());
    }
}
