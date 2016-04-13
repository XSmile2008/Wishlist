package com.company.wishlist.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.company.wishlist.R;
import com.company.wishlist.activity.MainActivity;
import com.company.wishlist.model.Notification;
import com.company.wishlist.model.Wish;
import com.company.wishlist.util.AuthUtils;
import com.company.wishlist.util.CloudinaryUtil;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

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
    public final static int TASK_REPEAT = 1000 * 60 * 60; // Repeat every hour//TODO:

    private Timer timer;
    private Map<String, Notification> notifications = new HashMap<>();
    private NotificationManager manager;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand()");
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        com.company.wishlist.model.Notification
                .getFirebaseRef()
                .orderByChild("owner")
                .equalTo(AuthUtils.getCurrentUser().getId())
                .addChildEventListener(new NotificationListener());

        String s = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext())
                .getString(getString(R.string.settings_notification_reserve_repeat_key), null);

        int period = (s != null) ? 1000 * 60 * Integer.valueOf(s) : TASK_REPEAT;
//        int period = 1000 * 60;
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

//    @Override
//    public void onDestroy() {
//        timer.cancel();
//        for (Integer notificationId : notifications.values()) {
//            manager.cancel(notificationId);
//        }
//        super.onDestroy();
//    }

    /**
     * Called on timer
     */
    public void onRun() {
        Log.d(LOG_TAG, "onRun()");
        for (Map.Entry<String, Notification> entry : notifications.entrySet()) {
            if (entry.getValue().isTimeToNotify()) {
                Wish.getFirebaseRef()
                        .orderByKey()
                        .equalTo(entry.getKey())
                        .addListenerForSingleValueEvent(new WishListener());
            }
        }
    }

    private void buildAndNotify(Wish wish, Integer id) {
        Log.d(LOG_TAG, "buildAndNotify()");

        //PendingIntent don't remind
        Intent intentDontRemind = new Intent(this, NotRemindNotificationAction.class);
        intentDontRemind.putExtra(NotRemindNotificationAction.NOTIFICATION_ID, wish.getId());
        intentDontRemind.putExtra(NotRemindNotificationAction.ANDROID_NOTIFICATION_ID, id);
        PendingIntent pendIntentDontRemind = PendingIntent.getService(this, 0, intentDontRemind, PendingIntent.FLAG_UPDATE_CURRENT);

        //PendingIntent start app
        Intent intentStartApp = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendIntentApp = PendingIntent.getActivity(this, 0, intentStartApp, PendingIntent.FLAG_UPDATE_CURRENT);


        String imageURL = CloudinaryUtil.getThumbURl(wish.getPicture(), 200, 200);
        Bitmap bitmap = null;
//        try {
//            new BitmapLoader().execute(imageURL).get(3, TimeUnit.SECONDS);
//        } catch (InterruptedException | ExecutionException | TimeoutException e) {
//            e.printStackTrace();
//        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_gift)
                .setContentTitle(this.getString(R.string.notification_service_title))
                .setContentIntent(pendIntentApp)
                .setAutoCancel(true)
                .setContentText(this.getString(R.string.notification_reserve_text, wish.getTitle()))
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, this.getString(R.string.notification_action_text), pendIntentDontRemind);

        if (bitmap != null) builder.setLargeIcon(bitmap);

        manager.notify(id, builder.build());
    }

    private class NotificationListener implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Notification notification = dataSnapshot.getValue(Notification.class);
            notification.setId(dataSnapshot.getKey());
            notifications.put(notification.getId(), notification);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Notification notification = dataSnapshot.getValue(Notification.class);
            notification.setId(dataSnapshot.getKey());
            notifications.put(notification.getId(), notification);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            notifications.remove(dataSnapshot.getKey());
            manager.cancel(dataSnapshot.getKey().hashCode());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            Log.e(LOG_TAG, firebaseError.toString());
        }
    }

    private class WishListener implements ValueEventListener {

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                Wish wish = ds.getValue(Wish.class);
                if (!wish.isRemoved()) {
                    wish.setId(ds.getKey());
                    buildAndNotify(wish, wish.getId().hashCode());
                    Log.d(LOG_TAG, wish.toString());
                }
            }
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }

    }

    private class BitmapLoader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            try {
                bitmap = Glide.with(getApplicationContext())
                        .load(params[0])
                        .asBitmap()
                        .into(-1, -1)
                        .get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            return bitmap;
        }

    }

}