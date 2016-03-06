package com.company.wishlist.activity;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.AuthActivity;
import com.company.wishlist.model.Wish;
import com.company.wishlist.model.WishList;
import com.facebook.Profile;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * Created by v.odahovskiy on 15.01.2016.
 */
public class SettingsActivity extends AuthActivity {

    private static final String REMOVE_WISH_PREFS_KEY = "remove_wish";

    //TODO: add query that delete all soft-removed wishes for this user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            try {
                if (preference.getKey().equals(REMOVE_WISH_PREFS_KEY)) {
                    new AlertDialog.Builder(getActivity())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Message")
                            .setMessage("After push Yes your removed wishes will be deleted.")
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    removeUserWishes();
                                }

                            })
                            .setNegativeButton(R.string.no, null)
                            .show();
                }
            }catch (Exception ex){}
            finally {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
        }

        //todo may be put into wish.class
        private void removeUserWishes() {
            WishList.getFirebaseRef()
                    .orderByChild("owner")
                    .equalTo(Profile.getCurrentProfile().getId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            boolean endIterate = false;
                            for (DataSnapshot wishListDS : dataSnapshot.getChildren()) {
                                Wish.getFirebaseRef()
                                        .orderByChild("wishListId")
                                        .equalTo(wishListDS.getKey())
                                        .addChildEventListener(new ChildEventListener() {

                                            @Override
                                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                                Wish wish = dataSnapshot.getValue(Wish.class);
                                                wish.setId(dataSnapshot.getKey());
                                                if (wish.isRemoved()) {
                                                    wish.remove(null);
                                                }
                                            }

                                            @Override
                                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                            }

                                            @Override
                                            public void onChildRemoved(DataSnapshot dataSnapshot) {
                                            }

                                            @Override
                                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                                            }

                                            @Override
                                            public void onCancelled(FirebaseError firebaseError) {
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            Log.d("wish_list.onCanceled()", firebaseError.toString());
                        }
                    });

        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private AppCompatDelegate mDelegate;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    @NonNull
    @Override
    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    @Override
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

   /* private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }*/
}
