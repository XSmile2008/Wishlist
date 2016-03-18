package com.company.wishlist.activity.abstracts;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.company.wishlist.R;
import com.company.wishlist.util.ConnectionUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by vladstarikov on 11.01.16.
 */
public class InternetActivity extends DebugActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.getClass() == InternetActivity.class) {
            setContentView(R.layout.activity_internet);

            View view = findViewById(R.id.retry_internet_conn);
            if (view != null) view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ConnectionUtil.isConnected()) {
                        try {
                            Log.d("nyan", "Connection established, running " + getIntent().getStringExtra("class"));
                            Intent intent = new Intent(getApplicationContext(), Class.forName(getIntent().getStringExtra("class")))
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else {
            checkConnection();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkConnection();
    }

    public void checkConnection() {
        if (!ConnectionUtil.isConnected() && this.getClass() != InternetActivity.class) {
            startActivity(new Intent(this, InternetActivity.class)
                    .putExtra("class", this.getClass().getName())
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }
    }

}
