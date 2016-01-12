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

    public interface IFirebaseConnection {
        void onAuthenticated(AuthData authData);
        void onAuthenticationError(FirebaseError firebaseError);
        void onMissingConnection();
    }

    private Context context;
    private IFirebaseConnection iConnection;//interface for interact util with activity for connection
    private Firebase firebase;
    private AuthData authData;
    private User user;

    public FirebaseUtil(Context context) {
        this.context = context;
        this.iConnection = (IFirebaseConnection) context;
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
        List<User> friends = getUserFriendList();
        try {
            user = User.getFromJSON(new FacebookProfileData().execute().get());
            user.setProvider(provider);
            firebase.child("users").child(id).child("first_name").setValue(user.getFirstName());
            firebase.child("users").child(id).child("last_name").setValue(user.getLastName());
            firebase.child("users").child(id).child("gender").setValue(user.getGender().name());
            firebase.child("users").child(id).child("birthday").setValue(user.getBirthday());
        } catch (InterruptedException | ExecutionException e) {
            user = new User(id, displayName, provider);
        }
        user.setFriends(friends);


        firebase.child("users").child(id).child("provider").setValue(provider);
        firebase.child("users").child(id).child("displayName").setValue(displayName);
        firebase.child("users").child(id).child("friends").setValue(friends);

        //   new Firebase(mFirebase + "/wishes/" + getUser().id).keepSynced(true);
    }


    //todo it will in facebook util class
    private List<User> getUserFriendList() {
        List<User> result = new ArrayList<User>();
        try {
            JSONArray friends = new FacebookMyFriendList().execute().get();
            for (int i = 0; i < friends.length(); i++) {
                JSONObject jsonObject = friends.getJSONObject(i);
                User user = new User();
                user.setId(jsonObject.getString("id"));
                user.setDisplayName(jsonObject.getString("name"));
                result.add(user);
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void onAuthenticated(AuthData authData) {
        this.authData = authData;
        saveUserInFirebase(authData);
        iConnection.onAuthenticated(authData);
    }

    @Override
    public void onAuthenticationError(FirebaseError firebaseError) {
        iConnection.onAuthenticationError(firebaseError);
    }


    public void refresh() {
        authData = firebase.getAuth();

        if (isAuthenticated()) {
            if (((InternetActivity) context).isConnected()) {
                saveUserInFirebase(authData);
            } else {
                iConnection.onMissingConnection();
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
