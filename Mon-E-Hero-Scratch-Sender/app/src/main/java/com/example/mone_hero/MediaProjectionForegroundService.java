package com.example.mone_hero;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

public class MediaProjectionForegroundService extends Service {

    private static final String CHANNEL_ID = "MediaProjectionForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        // Définir le type de service en premier plan requis pour la projection des médias
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "MediaProjectionForegroundService", NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("MediaProjectionForegroundService")
                    .setContentText("Foreground service for media projection")
                    .setSmallIcon(R.drawable.ic_notification_image)
                    .build();

            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
