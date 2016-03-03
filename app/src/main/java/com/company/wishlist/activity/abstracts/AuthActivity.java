package com.company.wishlist.activity.abstracts;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.company.wishlist.R;
import com.company.wishlist.activity.LoginActivity;
import com.company.wishlist.util.FirebaseUtil;

public abstract class AuthActivity extends InternetActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAuth();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuth();
    }

    public void checkAuth() {
        if (!isAuth() && this.getClass() != AuthActivity.class) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
        }
    }

    public boolean isAuth() {
        return new FirebaseUtil(this).isAuthenticated();
    }
}
