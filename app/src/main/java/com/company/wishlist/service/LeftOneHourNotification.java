package com.company.wishlist.service;

import android.app.IntentService;
import android.content.Intent;

public class LeftOneHourNotification extends IntentService {

    public LeftOneHourNotification() {
        super("UpdateProcessService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
      //
        //set confirm true to wish notification or remove
    }
}