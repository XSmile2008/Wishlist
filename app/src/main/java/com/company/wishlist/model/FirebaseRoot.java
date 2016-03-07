package com.company.wishlist.model;

import com.firebase.client.Firebase;

/**
 * Created by vladstarikov on 07.03.16.
 */
public class FirebaseRoot {

    private static final String FIREBASE_URL = "https://appwishlist.firebaseio.com";

    private static volatile Firebase firebaseRoot = new Firebase(FIREBASE_URL);

    public static Firebase get() {
        return firebaseRoot;
    }

}
