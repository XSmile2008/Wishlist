package com.company.wishlist.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.company.wishlist.R;
import com.company.wishlist.util.IntentUtil;
import com.company.wishlist.util.Utilities;

public class NoInternetConnectionActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet_connection);
        ((Button) findViewById(R.id.retry_internet_conn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isConnected(getApplicationContext())){
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.putExtra(BaseActivity.RELOAD_DATA, true);
                    startActivity(i);
                    finish();
                }
            }
        });
    }
}
