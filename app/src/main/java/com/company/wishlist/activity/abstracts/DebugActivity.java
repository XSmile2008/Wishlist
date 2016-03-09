package com.company.wishlist.activity.abstracts;

import android.util.Log;

import com.akexorcist.localizationactivity.LocalizationActivity;

/**
 * Created by vladstarikov on 07.01.16.
 */
public abstract class DebugActivity extends LocalizationActivity{

    String LOG_TAG = this.getClass().getSimpleName();


    @Override
    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(LOG_TAG, "onRestart()");
    }
}
