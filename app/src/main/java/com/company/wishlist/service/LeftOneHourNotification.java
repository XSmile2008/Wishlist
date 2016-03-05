package com.company.wishlist.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;

import com.company.wishlist.model.Notification;
import com.company.wishlist.model.Wish;

public class LeftOneHourNotification extends IntentService {

    public static final String NOTIFICATION_ID = "com.company.wishlist.service.notification.id";
    public static final String ANDROID_NOTIFICATION_ID = "com.company.wishlist.service.android.notification.id";
    private static int NOTIFICATION_MISSED = -99999;


    public LeftOneHourNotification() {
        super("UpdateProcessService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String id = intent.getExtras().getString(NOTIFICATION_ID, null);
        if (null != id) {
            Notification.getFirebaseRef().child(id).setValue(null);
        }

        Integer notificationId = intent.getExtras().getInt(ANDROID_NOTIFICATION_ID, NOTIFICATION_MISSED);
        if (notificationId != NOTIFICATION_MISSED) {
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(notificationId);
        }
    }
}