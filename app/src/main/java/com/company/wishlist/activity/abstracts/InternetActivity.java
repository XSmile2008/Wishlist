package com.company.wishlist.activity.abstracts;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.company.wishlist.R;

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
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

}
