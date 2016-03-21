package com.company.wishlist.util.social.share.impl;

import com.company.wishlist.util.social.twitter.TwitterUtils;
import com.company.wishlist.util.social.share.ShareStrategy;
import com.company.wishlist.util.social.share.SharingCallback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;

/**
 * Created by root on 20.03.2016.
 */
public class TwitterSharing implements ShareStrategy {
    @Override
    public void share(String message) {
        TwitterUtils.share(message, null);
    }

    @Override
    public void share(String message, final SharingCallback callback) {
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
    }
}
