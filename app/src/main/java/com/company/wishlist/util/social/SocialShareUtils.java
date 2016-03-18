package com.company.wishlist.util.social;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.company.wishlist.R;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.share.Sharer;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;

import org.jsoup.helper.StringUtil;

/**
 * Created by root on 17.03.2016.
 */
public class SocialShareUtils implements SocialShare {

    private static volatile SocialShareUtils instance;

    private SocialShareUtils() {
    }

    public static SocialShareUtils ref() {
        if (null == instance) {
            instance = new SocialShareUtils();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void share(String message, Social social, final Callback callback) {
        if (null == callback) {
            throw new IllegalArgumentException("Should implement Callback for SocialShareUtils");
        }
        switch (social) {
            case FACEBOOK:
                FacebookUtils.share(message, new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response.getError() == null) {
                            callback.success();
                        } else {
                            callback.failure(response.getError().getException());
                        }
                    }
                });
                break;
            case TWITTER:
                TwitterUtils.share(message, new com.twitter.sdk.android.core.Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> result) {
                        callback.success();
                    }

                    @Override
                    public void failure(TwitterException e) {
                        callback.failure(e);
                    }
                });
                break;
        }
    }

    public static void showShareDialog(String message, final Context context, final SocialShare.Social social) {
        View promptsView = LayoutInflater.from(context).inflate(R.layout.share_tweet_dialog, null, false);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.et_tweet_message);
        userInput.setText(message);

        new AlertDialog.Builder(context)
                .setView(promptsView)
                .setCancelable(false)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String message = userInput.getText().toString().trim();
                        if (StringUtil.isBlank(message)) {
                            Toast.makeText(context, "Should be not empty", Toast.LENGTH_SHORT).show();
                        } else {
                            SocialShareUtils.ref().share(message, social, new SocialShare.Callback() {
                                @Override
                                public void success() {
                                    Toast.makeText(context, "Message shared successful", Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void failure(Throwable error) {
                                    Toast.makeText(context, "Problem with sharing message.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                })
                .show();
    }
}
