package com.company.wishlist.util.social.share;

/**
 * Created by odahovskiy on 17.03.2016.
 */
public interface ShareStrategy {
    void share(String message);
    void share(String message, SharingCallback callback);
}
