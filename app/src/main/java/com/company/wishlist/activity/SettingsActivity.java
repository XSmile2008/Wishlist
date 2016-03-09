package com.company.wishlist.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
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
import com.company.wishlist.util.AuthUtils;
import com.company.wishlist.util.LanguageHelper;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Locale;

/**
 * Created by v.odahovskiy on 15.01.2016.
 */
public class SettingsActivity extends AuthActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String REMOVE_WISH_PREFS_KEY = "remove_wish";

    private static Context context;
    //TODO: add query that destroy all soft-removed wishes for this user

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        context = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.lang_key))) {
            String language = sharedPreferences.getString(key, "en");
            //persistValue(sharedPreferences, key, language);
            setLanguage(language);
            /*LanguageHelper.setUpLnaguage(language);
            ((SettingsActivity) context).recreate();*/
        }
    }

    private void persistValue(SharedPreferences sharedPreferences, String key, String language) {
        SharedPreferences.Editor es = sharedPreferences.edit();
        es.putString(key, language);
        es.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager
                .getDefaultSharedPreferences(getBaseContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager
                .getDefaultSharedPreferences(getBaseContext()).registerOnSharedPreferenceChangeListener(this);
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
                            .setTitle(getResources().getString(R.string.settings_dialog_message))
                            .setMessage(getResources().getString(R.string.settings_clear_dialog_message))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    removeUserWishes();
                                }

                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
        }

        //todo may be put into wish.class
        private void removeUserWishes() {
            WishList.getFirebaseRef()
                    .orderByChild("owner")
                    .equalTo(AuthUtils.getCurrentUser().getId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
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
}
