package com.company.wishlist.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
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
import com.company.wishlist.util.DateUtil;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
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

    private Timer timer;
    private Map<String, Notification> notifications = new HashMap<>();
    private NotificationManager manager;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO: check if activity relaunch this service

        Log.d(LOG_TAG, "onStartCommand()");
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        com.company.wishlist.model.Notification
                .getFirebaseRef()
                .orderByChild("owner")
                .equalTo(AuthUtils.getCurrentUser().getId())
                .addChildEventListener(new NotificationListener());

        int minutes = Integer.valueOf(PreferenceManager
                .getDefaultSharedPreferences(getBaseContext())
                .getString(getString(R.string.settings_notification_repeat_key), "1"));

        Log.d(LOG_TAG, "repeat = " + minutes);

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
        }, 1000 * 60 * minutes, 1000 * 60 * minutes);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        for (Notification notification : notifications.values()) {
            manager.cancel(notification.getId().hashCode());
        }
        super.onDestroy();
    }

    /**
     * Called on timer
     */
    public void onRun() {
        Log.d(LOG_TAG, "onRun()");
        for (Map.Entry<String, Notification> entry : notifications.entrySet()) {
            if (isTimeToNotify(Long.parseLong(entry.getValue().getReservationDate()))) {
                Wish.getFirebaseRef()
                        .orderByKey()
                        .equalTo(entry.getKey())
                        .addListenerForSingleValueEvent(new WishListener());
            }
        }
    }

    public boolean isTimeToNotify(long date) {
        int days = Integer.valueOf(PreferenceManager
                .getDefaultSharedPreferences(getBaseContext())
                .getString(getString(R.string.settings_notification_start_before), "7"));

        Calendar curr = Calendar.getInstance();
        curr.setTime(new Date());

        Calendar notification = Calendar.getInstance();
        notification.setTime(new Date(date));
        notification.add(Calendar.DAY_OF_MONTH, -days);

        return curr.after(notification);
    }

    private void buildAndNotify(Wish wish) {
        buildAndNotify(wish, null);
    }

    private void buildAndNotify(Wish wish, Bitmap largeIcon) {
        Log.d(LOG_TAG, "buildAndNotify()" + largeIcon);

        //PendingIntent start app
        Intent intentStartApp = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendIntentApp = PendingIntent.getActivity(this, 0, intentStartApp, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setGroup("wishes")
                .setSmallIcon(R.drawable.ic_stat_gift)
                .setContentTitle(this.getString(R.string.notification_service_title))
                .setContentText(this.getString(R.string.notification_reserve_text, wish.getTitle()))
                .setContentIntent(pendIntentApp)
                .setAutoCancel(true);

        if (wish.hasPicture() && Build.VERSION.SDK_INT >= 23) {
            if (largeIcon == null) new BitmapLoader().execute(wish);
            else builder.setLargeIcon(largeIcon);
        }

        manager.notify(wish.getId().hashCode(), builder.build());
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
                    buildAndNotify(wish);
                    Log.d(LOG_TAG, wish.toString());
                }
            }
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }

    }

    private class BitmapLoader extends AsyncTask<Wish, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Wish... params) {
            String imageURL = CloudinaryUtil.getThumbURl(params[0].getPicture(), 200, 200);
            Bitmap bitmap = null;
            try {
                bitmap = Glide.with(getApplicationContext())
                        .load(imageURL)
                        .asBitmap()
                        .into(200, 200)
                        .get();
//                bitmap = CropCircleTransformation.transform(bitmap);//TODO:
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            buildAndNotify(params[0], bitmap);
            return bitmap;
        }

    }

}