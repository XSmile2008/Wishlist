package com.company.wishlist.util.social.share;

/**
 * Created by odahovskiy on 20.03.2016.
 */
public interface SharingCallback {
    void success();

    void failure(Throwable error);
}
