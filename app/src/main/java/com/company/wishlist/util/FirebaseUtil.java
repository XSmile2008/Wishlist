package com.company.wishlist.util;

import android.content.Context;

import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.InternetActivity;
import com.company.wishlist.model.User;
import com.company.wishlist.model.Wish;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

/**
 * Created by v.odahovskiy on 12.01.2016.
 */
public class FirebaseUtil implements Firebase.AuthResultHandler {

    public static final String USER_TABLE = User.class.getSimpleName();
    public static final String WISH_TABLE = Wish.class.getSimpleName();

    //interface for interact util with activity for connection
    public interface IFirebaseConnection {
        void onAuthenticated(AuthData authData);

        void onAuthenticationError(FirebaseError firebaseError);

        void onMissingConnection();
    }

    private Context context;//is IFirebaseConnection so you cant cast it
    private Firebase firebaseRoot;
    private AuthData authData;
    private User user;

    public FirebaseUtil(Context context) {
        this.context = context;
        Firebase.setAndroidContext(context);
        firebaseRoot = new Firebase(context.getString(R.string.firebase_url));
        refreshAuth();
    }

    public void auth(String provider, String token) {
        firebaseRoot.authWithOAuthToken(provider, token, this);
    }

    public void unauth() {
        firebaseRoot.unauth();
    }

    private void saveUserInFirebase(AuthData authData) {
        user = FacebookUserBuilder.build(authData);
        firebaseRoot.child(USER_TABLE).child(authData.getUid()).setValue(user);
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

    public void refreshAuth() {
        authData = firebaseRoot.getAuth();

        if (isAuthenticated()) {
            if (((InternetActivity) context).isConnected()) {
                saveUserInFirebase(authData);
            } else {
                ((IFirebaseConnection) context).onMissingConnection();
            }
        }
    }

    private boolean isTokenExpired() {
        return (authData == null || Utilities.isExpired(authData.getExpires()));
    }

    public boolean isDisconnected() {
        return !isAuthenticated() || isTokenExpired();
    }

    public User getCurrentUser() {
        return user;
    }

    public boolean isAuthenticated() {
        return null != authData;
    }

    //TODO: leave for monday
    //TODO: This method will be called once at all project, maybe will be better move this to commitChanges()?
    public void save(Wish wish) {
        if (wish.getId() == null || wish.getId().isEmpty()) {
            firebaseRoot.child(WISH_TABLE).push().setValue(wish);//Automatically gen new key on DB side
        } else {
            firebaseRoot.child(WISH_TABLE).child(wish.getId()).setValue(wish);
        }
    }

    //TODO: This method will be called once at all project, maybe will be better move this to deleteWish()?
    public void remove(String id, Class<?> clazz) {
        firebaseRoot.child(clazz.getSimpleName()).child(id).removeValue();
    }

    public Firebase getFirebaseRoot() {
        return firebaseRoot;
    }

    public void getWishes(String wishlist_id) {

    }
}
