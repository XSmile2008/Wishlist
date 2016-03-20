package com.company.wishlist.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.company.wishlist.R;
import com.company.wishlist.activity.MainActivity;
import com.company.wishlist.util.AuthUtils;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

//// TODO: 04.03.2016
//TODO: may be use ChildEventListener?
//TODO: make option how many days before reservation date to start send notifications
//TODO: group notifications

/**
 * I'm using owner , cause if we will used date field, we cat take many records for this date
 * need to think what way is better
 */

public class NotificationService extends Service {

    private final static String LOG_TAG = NotificationService.class.getSimpleName();

    public final static int TASK_DELAY = 1000; //in milliseconds
    public final static int TASK_REPEAT = 1000 * 60 * 60; // Repeat every hour

    private Timer timer;
    private Map<String, Integer> notifications = new HashMap<>();
    private NotificationManager manager;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        String s = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext())
                .getString(getString(R.string.settings_notification_reserve_repeat_key), null);

        int period = s != null ? 1000 * 60 * Integer.valueOf(s) : TASK_REPEAT;
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.e(LOG_TAG, "onRun");
                onRun();
            }
        }, TASK_DELAY, period);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        for (Integer notificationId : notifications.values()) {
            manager.cancel(notificationId);
            timer.cancel();
        }
        super.onDestroy();
    }

    /**
     * Called on timer
     */
    public void onRun() {
        com.company.wishlist.model.Notification
                .getFirebaseRef()
                .orderByChild("owner")
                .equalTo(AuthUtils.getCurrentUser().getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            com.company.wishlist.model.Notification notification = ds.getValue(com.company.wishlist.model.Notification.class);
                            notification.setId(ds.getKey());
                            if (notification.isToday()) {//TODO: or other period that user set in settings
                                Integer id = notifications.get(notification.getId());
                                id = (null == id) ? notifications.values().size() + 1 : id;
                                notifications.put(notification.getId(), id);// check for duplicates
                                buildAndNotify(notification, id);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    private void buildAndNotify(com.company.wishlist.model.Notification notification, Integer id) {
        //PendingIntent don't remind
        Intent intentDontRemind = new Intent(this, NotRemindNotificationAction.class);
        intentDontRemind.putExtra(NotRemindNotificationAction.NOTIFICATION_ID, notification.getId());
        intentDontRemind.putExtra(NotRemindNotificationAction.ANDROID_NOTIFICATION_ID, id);
        PendingIntent pendIntentDontRemind = PendingIntent.getService(this, 0, intentDontRemind, PendingIntent.FLAG_UPDATE_CURRENT);

        //PendingIntent start app
        Intent intentStartApp = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendIntentApp = PendingIntent.getActivity(this, 0, intentStartApp, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder ntfcBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(this.getString(R.string.notification_service_title))
                .setContentIntent(pendIntentApp)
                .setAutoCancel(true)
                .setContentText(this.getString(R.string.notification_reserve_text, notification.getWishTitle()))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, this.getString(R.string.notification_action_text), pendIntentDontRemind);

        manager.notify(id, ntfcBuilder.build());
    }

}