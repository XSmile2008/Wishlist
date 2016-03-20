package com.company.wishlist.util.social.share.impl;

import com.company.wishlist.util.social.FacebookUtils;
import com.company.wishlist.util.social.share.ShareStrategy;
import com.company.wishlist.util.social.share.SharingCallback;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;


public class FacebookSharing implements ShareStrategy {
    @Override
    public void share(String message) {
        FacebookUtils.share(message, null);
    }

    @Override
    public void share(String message, final SharingCallback callback) {
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
    }
}
