package com.company.wishlist.util;

import com.firebase.client.Firebase;

/**
 * Created by v.odahovskiy on 12.01.2016.
 */
public class FirebaseUtils  {

    private static  String FIREBASE_URL = "https://appwishlist.firebaseio.com";

    private static volatile Firebase firebaseRoot = new Firebase(FIREBASE_URL);

    public static Firebase get(){
        return firebaseRoot;
    }
}
