package com.company.wishlist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

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
 * Created by v.odahovskiy on 10.01.2016.
 */
public class FirebaseFragment extends Fragment implements Firebase.AuthResultHandler {


    public static String PATH_NUM_SAVED = "numSaved";
    public static String PATH_LAST_USER = "lastUser";
    public static String PATH_LAST_SAVED = "lastSaved";
    public static String PATH_DATE_SAVED = "dateSaved";

    /**
     * Callback interface through which the fragment will report the
     * task's progress and results back to the Activity.
     */
    public interface Callbacks {
        void onAuthenticated(AuthData authData);

        void onAuthenticationError(FirebaseError firebaseError);

        void onMissingConnection();
    }

    public static final String TAG_FIREBASE_FRAGMENT = "TAG_FIREBASE_FRAGMENT";

    private Callbacks mCallbacks;
    private Context mContext;
    private Firebase mFirebase;
    private AuthData mAuthdata;
    private User mUser;
    private String mUserId;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallbacks = (Callbacks) activity;
        mContext = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);
        Firebase.setAndroidContext(getActivity().getApplicationContext());
        mFirebase = new Firebase(getString(R.string.firebase_url));
        gettingData();
    }

    public void reloadData(){
        gettingData();
    }

    private void gettingData() {
        mAuthdata = mFirebase.getAuth();

        if (isAuthenticated()) {
            if (((InternetActivity)getContext()).isConnected()) {
                saveUserInFirebase(mAuthdata);
            }else {
                mCallbacks.onMissingConnection();
            }
        }
    }

    public void authenticate(String provider, String token) {
        mFirebase.authWithOAuthToken(provider, token, this);
    }

    private void saveUserInFirebase(AuthData authData) {
        String id = authData.getProviderData().get("id").toString();
        String displayName = authData.getProviderData().get("displayName").toString();
        String provider = authData.getProvider();
        List<User> friends = getUserFriendList();
        try {
            mUser = User.getFromJSON(new FacebookProfileData().execute().get());
            mUser.setProvider(provider);
            mFirebase.child("users").child(id).child("first_name").setValue(mUser.getFirstName());
            mFirebase.child("users").child(id).child("last_name").setValue(mUser.getLastName());
            mFirebase.child("users").child(id).child("gender").setValue(mUser.getGender());
            mFirebase.child("users").child(id).child("birthday").setValue(mUser.getBirthday());
        } catch (InterruptedException | ExecutionException e) {
            mUser = new User(id, displayName, provider);
        }
        mUserId = mUser.getId();
        //mUser.setFriends(friends);


        mFirebase.child("users").child(id).child("provider").setValue(provider);
        mFirebase.child("users").child(id).child("displayName").setValue(displayName);
        mFirebase.child("users").child(id).child("friends").setValue(friends);

     //   new Firebase(mFirebase + "/wishes/" + getUser().id).keepSynced(true);
    }

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
        mAuthdata = authData;
        saveUserInFirebase(authData);
        mCallbacks.onAuthenticated(authData);
    }

    @Override
    public void onAuthenticationError(FirebaseError firebaseError) {
        mCallbacks.onAuthenticationError(firebaseError);
    }


    /**
     * Adding a bookmark to Firebase
     */
    /*public void addBookmark(final PrivateBookmark bookmark) {
        if (!Utilities.isConnected(mContext)) {
            mCallbacks.onMissingConnection();
            return;
        }
        Firebase privateBookmarkPath =
                mFirebase.child(PATH_PRIVATE_BOOKMARKS).child(mUserId).child(bookmark.getKey());

        privateBookmarkPath.setValue(bookmark);
        privateBookmarkPath.child(PATH_DATE_SAVED).setValue(ServerValue.TIMESTAMP);
        privateBookmarkPath.child(PATH_DATE_SAVED).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {
                bookmark.setDateSaved((long) dataSnapshot.getValue());
                mFirebase.child(FirebaseFragment.PATH_PUBLIC_BOOKMARKS).child(bookmark.getKey())
                        .runTransaction(new NewBookmarkTransaction(bookmark));

                for (String tag : bookmark.getTags()) {
                    addTag(tag, bookmark);
                }
            }

            @Override public void onCancelled(FirebaseError firebaseError) {

            }
        });

    }
*/
    /**
     * Updating a bookmark
     */
    /*public void updateBookmark(PrivateBookmark bookmarkOld, PrivateBookmark bookmarkNew) {
        if (!Utilities.isConnected(mContext)) {
            mCallbacks.onMissingConnection();
            return;
        }
        mFirebase.child(PATH_PRIVATE_BOOKMARKS).child(mUserId).child(bookmarkNew.getKey()).setValue(bookmarkNew);

        for (String tag : bookmarkOld.getTags()) {
            if (!bookmarkNew.isTaggedWith(tag)) {
                removeTag(tag, bookmarkNew);
            }
        }

        for (String tag : bookmarkNew.getTags()) {
            if (!bookmarkOld.isTaggedWith(tag)) {
                addTag(tag, bookmarkNew);
            }
        }
    }*/


    /**
     * Removing a bookmark from Firebase
     */
    /*public void removeBookmark(PrivateBookmark bookmark) {
        if (!Utilities.isConnected(mContext)) {
            mCallbacks.onMissingConnection();
            return;
        }
        mFirebase.child(PATH_PRIVATE_BOOKMARKS).child(mUserId).child(bookmark.getKey()).removeValue();

        for (String tag : bookmark.getTags()) {
            removeTag(tag, bookmark);
        }

        mFirebase.child(PATH_PUBLIC_BOOKMARKS).child(bookmark.getKey())
                .runTransaction(removePublicBookmarkTransaction);
    }*/


    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public Firebase getFirebase() {
        return mFirebase;
    }

    public AuthData getAuthdata() {
        return mAuthdata;
    }

    public boolean isAuthenticated() {
        return mAuthdata != null;
    }

    public User getUser() {
        return mUser;
    }
}

