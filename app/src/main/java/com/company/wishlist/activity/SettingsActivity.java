package com.company.wishlist.activity;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.AuthActivity;
import com.company.wishlist.model.Wish;
import com.company.wishlist.model.WishList;
import com.company.wishlist.service.NotificationService;
import com.company.wishlist.util.AuthUtils;
import com.company.wishlist.util.DialogUtil;
import com.company.wishlist.util.social.TwitterUtils;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

/**
 * Created by v.odahovskiy on 15.01.2016.
 */
public class SettingsActivity extends AuthActivity {

    static TwitterLoginButton twitterLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        Preference twitterPreference;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            twitterLoginButton = new TwitterLoginButton(getActivity());

            twitterPreference = (Preference) getPreferenceManager()
                    .findPreference(getString(R.string.twitter_button_key));

            twitterPreference.setSummary(TwitterUtils.userName());

            twitterLoginButton.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    twitterPreference.setSummary(String.format("%s", result.data.getUserName()));
                }

                @Override
                public void failure(TwitterException e) {

                }
            });
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, final Preference preference) {
            String key = preference.getKey();
            try {
                if (key.equals(getString(R.string.clear_wishes_key)))
                    clearWishesAction();
                else if (key.equals(getString(R.string.notification_enabled_key))) {
                    notificationEnableAction(preference);
                } else if (key.equals(getString(R.string.twitter_button_key))) {
                    twitterAuthAction();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                return super.onPreferenceTreeClick(preferenceScreen, preference);
            }
        }

        private void twitterAuthAction() {
            if (!TwitterUtils.isConnected()) {
                twitterLoginButton.performClick();
            } else {
                DialogUtil.alertShow("Logout from Twitter", "Are you sure?", getActivity(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TwitterUtils.logout();
                        twitterPreference.setSummary(getString(R.string.twitter_not_connected));
                    }
                });
            }
        }

        private void notificationEnableAction(Preference preference) {
            boolean enabled = preference.getSharedPreferences().getBoolean(getString(R.string.notification_enabled_key), false);
            if (enabled) {
                getActivity().startService(new Intent(getActivity(), NotificationService.class));
            } else {
                getActivity().stopService(new Intent(getActivity(), NotificationService.class));
            }
        }

        private void clearWishesAction() {
            new AlertDialog.Builder(getActivity())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Message")
                    .setMessage("After push Yes your removed wishes will be deleted.")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Wish.clearAllSoftRemovedForUser(AuthUtils.getCurrentUser().getId());
                        }

                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
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
