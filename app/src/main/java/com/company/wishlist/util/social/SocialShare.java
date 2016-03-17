package com.company.wishlist.util.social;

/**
 * Created by root on 17.03.2016.
 */
public interface SocialShare {
    interface Callback{
        void success();
        void failure(Throwable error);
    }
    enum Social {FACEBOOK, TWITTER}
    void share(String message, Social social, Callback callback);
}
