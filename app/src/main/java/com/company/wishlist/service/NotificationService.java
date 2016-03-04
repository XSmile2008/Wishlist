package com.company.wishlist.service;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;

import com.company.wishlist.R;
import com.company.wishlist.activity.MainActivity;
import com.company.wishlist.model.Notification;
import com.facebook.Profile;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService  extends Service {

    private Timer timer;
    private UpdateTimerTask updateTimerTask;
    private Map<String, Integer> notifications = new HashMap<>();
    private NotificationManager ntfcManager;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent intentLater = new Intent(this, LeftOneHourNotification.class);
        PendingIntent pendIntentLater = PendingIntent.getService(this, 0, intentLater, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentDontNotificate = new Intent(this, LeftOneHourNotification.class);
        PendingIntent pendIntentDontNotificate = PendingIntent.getService(this, 0, intentDontNotificate, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentStartApp = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendIntentApp = PendingIntent.getActivity(this, 0, intentStartApp, PendingIntent.FLAG_UPDATE_CURRENT);

        android.support.v4.app.NotificationCompat.Builder ntfcBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Wish reminder")
                .setContentIntent(pendIntentApp)
                .setAutoCancel(true)
                .addAction(android.R.drawable.arrow_up_float, "Later", pendIntentLater)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Ok", pendIntentDontNotificate);

        ntfcManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        timer = new Timer();
        updateTimerTask = new UpdateTimerTask(ntfcBuilder, ntfcManager);

        timer.schedule(updateTimerTask, UpdateTimerTask.TASK_DELAY, UpdateTimerTask.TASTK_REPEAT);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        for (Integer notificationId : notifications.values()) {
            ntfcManager.cancel(notificationId);
            timer.cancel();
        }
        super.onDestroy();
    }

    private class UpdateTimerTask extends TimerTask {

        public final static int TASK_DELAY = 1000; //in milliseconds
//        public final static int TASTK_REPEAT = 1000 * 60 * 15;//60 seconds
        public final static int TASTK_REPEAT = 1000 * 35; //60 seconds

        private android.support.v4.app.NotificationCompat.Builder builder;
        private NotificationManager manager;

        public UpdateTimerTask(android.support.v4.app.NotificationCompat.Builder builder, NotificationManager manager) {
            this.builder = builder;
            this.manager = manager;
        }

        @Override
        public void run() {
            //// TODO: 04.03.2016
            /**
             * I'm using owner , cause if we will used date field, we cat take many records for this date
             * need to think what way is better
             */
            Notification.getFirebaseRef().orderByChild("owner").equalTo(Profile.getCurrentProfile().getId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                Notification notification = ds.getValue(Notification.class);
                                notification.setId(ds.getKey());
                                if (notification.isToday() && !notification.getConfirm()) {
                                    builder.setContentText("Don't forget for " + notification.getWishTitle());
                                    Integer id = notifications.get(notification.getId());
                                    id = (null == id)? notifications.values().size() + 1 : id;
                                    notifications.put(notification.getId(), id);// check for duplicates
                                    manager.notify(id, builder.build());
                                }
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });
            }
        }
    }