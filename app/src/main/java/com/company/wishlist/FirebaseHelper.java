package com.company.wishlist;

import android.content.Context;

import com.company.wishlist.model.User;
import com.firebase.client.Firebase;

import java.util.List;

/**
 * Created by vladstarikov on 12.01.16.
 */
public class FirebaseHelper {

    public final String FIREBASE_APP_URL = "https://appwishlist.firebaseio.com";

    Context context;

    Firebase rootRef;

    public FirebaseHelper(Context context) {
        this.context = context;
        this.rootRef = new Firebase(context.getString(R.string.firebase_url));
    }

    public void addUser(User user) {
        rootRef.child(User.class.toString().toLowerCase() + "s2").setValue(user);
    }


}
