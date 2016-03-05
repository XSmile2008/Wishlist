package com.company.wishlist.service;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;

import com.company.wishlist.R;
import com.company.wishlist.activity.MainActivity;
import com.facebook.Profile;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {

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

        ntfcManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        timer = new Timer();
        updateTimerTask = new UpdateTimerTask(ntfcManager, this);

        int val = getRepeatValueAsMinutes();
        timer.schedule(updateTimerTask, UpdateTimerTask.TASK_DELAY, val);

        return START_STICKY;
    }

    private int getRepeatValueAsMinutes() {
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String repeatMinutes = shared.getString(getString(R.string.settings_notification_reserve_repeat_key), null);

        if (null != repeatMinutes) {
            return 1000 * 60 * Integer.valueOf(repeatMinutes);
        } else return UpdateTimerTask.TASTK_REPEAT;

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
        public final static int TASTK_REPEAT = 1000 * 60 * 60; // Repeat every hour

        private NotificationManager manager;
        private Service service;

        public UpdateTimerTask(NotificationManager manager, Service service) {
            this.manager = manager;
            this.service = service;
        }

        @Override
        public void run() {
            //// TODO: 04.03.2016
            /**
             * I'm using owner , cause if we will used date field, we cat take many records for this date
             * need to think what way is better
             */
            try {
                com.company.wishlist.model.Notification.getFirebaseRef().orderByChild("owner").equalTo(Profile.getCurrentProfile().getId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                    com.company.wishlist.model.Notification notification = ds.getValue(com.company.wishlist.model.Notification.class);
                                    notification.setId(ds.getKey());
                                    if (notification.isToday()) {
                                        Integer id = notifications.get(notification.getId());
                                        id = (null == id) ? notifications.values().size() + 1 : id;
                                        notifications.put(notification.getId(), id);// check for duplicates

                                        buildAndNotify(notification, id);
                                    }
                                }
                            }

                            private void buildAndNotify(com.company.wishlist.model.Notification notification, Integer id) {
                                Intent intentDontRemind = new Intent(service, LeftOneHourNotification.class);
                                intentDontRemind.putExtra(LeftOneHourNotification.NOTIFICATION_ID, notification.getId());
                                intentDontRemind.putExtra(LeftOneHourNotification.ANDROID_NOTIFICATION_ID, id);

                                Intent intentStartApp = new Intent(service, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                PendingIntent pendIntentApp = PendingIntent.getActivity(service, 0, intentStartApp, PendingIntent.FLAG_UPDATE_CURRENT);

                                PendingIntent pendIntentDontRemind = PendingIntent.getService(service, 0, intentDontRemind, PendingIntent.FLAG_UPDATE_CURRENT);

                                android.support.v4.app.NotificationCompat.Builder ntfcBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(service)
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentTitle(service.getString(R.string.notification_service_title))
                                        .setContentIntent(pendIntentApp)
                                        .setAutoCancel(true);

                                ntfcBuilder.setContentText(service.getString(R.string.notification_reserve_text, notification.getWishTitle()));

                                ntfcBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel, service.getString(R.string.notification_action_text), pendIntentDontRemind);

                                manager.notify(id, ntfcBuilder.build());
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
            } catch (Exception ex) {
            }
        }
    }
}