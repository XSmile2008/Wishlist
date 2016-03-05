package com.company.wishlist.activity.abstracts;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.company.wishlist.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * Created by vladstarikov on 11.01.16.
 */
public class InternetActivity extends DebugActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkConnection();
        if (this.getClass() == InternetActivity.class) {
            setContentView(R.layout.activity_internet);
            findViewById(R.id.retry_internet_conn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isConnected()) {
                        try {
                            Log.d("nyan", "Connection established, running " + getIntent().getStringExtra("class"));
                            Intent intent = new Intent(getApplicationContext(), Class.forName(getIntent().getStringExtra("class")))
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        //intent.putExtra(BaseActivity.RELOAD_DATA, true);
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkConnection();
    }

    public void checkConnection() {
        if (!isConnected() && this.getClass() != InternetActivity.class) {
            startActivity(new Intent(this, InternetActivity.class)
                    .putExtra("class", this.getClass().getName())
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        // boolean result = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        class CheckInternet extends AsyncTask<Void, Void, Boolean> {

            @Override
            protected Boolean doInBackground(Void... params) {
                boolean result = false;
                if (activeNetwork != null && activeNetwork.isConnected()) {
                    try {
                        URL url = new URL("http://www.google.com");
                        HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                        urlc.setConnectTimeout(3000);
                        urlc.connect();
                        if (urlc.getResponseCode() == 200) {
                            result = true;
                        }
                    } catch (IOException ignored) {}
                }
                return result;
            }
        }

        try {
            return new CheckInternet().execute().get();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

}
