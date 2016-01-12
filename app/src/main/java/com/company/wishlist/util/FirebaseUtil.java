package com.company.wishlist.util;

import android.content.Context;

import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.InternetActivity;
import com.company.wishlist.model.User;
import com.company.wishlist.task.FacebookMyFriendList;
import com.company.wishlist.task.FacebookProfileData;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by v.odahovskiy on 12.01.2016.
 */
public class FirebaseUtil implements Firebase.AuthResultHandler {

    //interface for interact util with activity for connection
    public interface IFirebaseConnection {
        void onAuthenticated(AuthData authData);
        void onAuthenticationError(FirebaseError firebaseError);
        void onMissingConnection();
    }

    private Context context;//is IFirebaseConnection so you cant cast it
    private Firebase firebase;
    private AuthData authData;
    private User user;

    public FirebaseUtil(Context context) {
        this.context = context;
        Firebase.setAndroidContext(context);
        firebase = new Firebase(context.getString(R.string.firebase_url));
        refresh();
    }

    public void authenticate(String provider, String token) {
        firebase.authWithOAuthToken(provider, token, this);
    }

    private void saveUserInFirebase(AuthData authData) {
        String id = authData.getProviderData().get("id").toString();
        String displayName = authData.getProviderData().get("displayName").toString();
        String provider = authData.getProvider();
        try {
            user = new FacebookProfileData().execute().get();
            user.setProvider(provider);
            firebase.child("users").child(id).child("first_name").setValue(user.getFirstName());
            firebase.child("users").child(id).child("last_name").setValue(user.getLastName());
            firebase.child("users").child(id).child("gender").setValue(user.getGender());
            firebase.child("users").child(id).child("birthday").setValue(user.getBirthday());
        } catch (InterruptedException | ExecutionException e) {
            user = new User(id, displayName, provider);
        }

        firebase.child("users").child(id).child("provider").setValue(provider);
        firebase.child("users").child(id).child("displayName").setValue(displayName);
    }

    @Override
    public void onAuthenticated(AuthData authData) {
        this.authData = authData;
        saveUserInFirebase(authData);
        ((IFirebaseConnection) context).onAuthenticated(authData);
    }

    @Override
    public void onAuthenticationError(FirebaseError firebaseError) {
        ((IFirebaseConnection) context).onAuthenticationError(firebaseError);
    }


    public void refresh() {
        authData = firebase.getAuth();

        if (isAuthenticated()) {
            if (((InternetActivity) context).isConnected()) {
                saveUserInFirebase(authData);
            } else {
                ((IFirebaseConnection) context).onMissingConnection();
            }
        }
    }

    public User getCurrentUser() {
        return user;
    }

    public boolean isAuthenticated() {
        return null != authData;
    }

    public Firebase getFirebase() {
        return firebase;
    }

    public AuthData getAuthdata() {
        return authData;
    }

}
