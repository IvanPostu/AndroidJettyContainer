package com.ipostu.androidjettycontainer.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.ipostu.androidjettycontainer.R;

public class MyForegroundService extends Service {

    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final String ACTION_START = "ACTION_START";
    private static final String ACTION_STOP = "ACTION_STOP";
    private static final int NOTIFICATION_ID = 1;

    private Thread taskThread;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification("Service is stopped", false));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                startServiceTask();
                updateNotification("Service is running", true);
            } else if (ACTION_STOP.equals(action)) {
                stopServiceTask();
                updateNotification("Service is stopped", false);
            }
        }
        return START_STICKY;
    }

    private void startServiceTask() {
        taskThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Thread currentThread = Thread.currentThread();

                while (!currentThread.isInterrupted()) {
                    try {
                        Thread.sleep(5000);
                        Log.d(null, String.format("Service task %s is running...",
                                currentThread.getName()));
                    } catch (InterruptedException e) {
                        if (!currentThread.isInterrupted()) {
                            currentThread.interrupt();
                        }
                        Log.d(null, String.format("Service task %s is interrupted",
                                currentThread.getName()), e);
                    }
                }
            }
        });
        taskThread.start();
    }

    private void stopServiceTask() {
        if (taskThread != null) {
            taskThread.interrupt();
            taskThread = null;
        }
    }

    private void updateNotification(String message, boolean isRunning) {
        Notification notification = createNotification(message, isRunning);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    private Notification createNotification(String message, boolean isRunning) {
        Intent startIntent = new Intent(this, MyForegroundService.class);
        startIntent.setAction(ACTION_START);
        PendingIntent startPendingIntent = PendingIntent.getService(this, 0, startIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(this, MyForegroundService.class);
        stopIntent.setAction(ACTION_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(message)
                .setSmallIcon(R.drawable.icon)
                .addAction(new NotificationCompat.Action(R.drawable.icon, "Start", startPendingIntent))
                .addAction(new NotificationCompat.Action(R.drawable.icon, "Stop", stopPendingIntent))
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
