package com.company.wishlist.util.social;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.share.Sharer;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;

/**
 * Created by root on 17.03.2016.
 */
public class SocialShareUtils implements SocialShare {

    private static volatile SocialShareUtils instance;

    private SocialShareUtils(){}

    public static SocialShareUtils ref(){
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
                        if (response.getError() == null){
                            callback.success();
                        }else {
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
}
