package com.company.wishlist.util.social.facebook;

import com.company.wishlist.model.User;
import com.facebook.FacebookRequestError;

import java.util.List;

/**
 * Created by root on 21.03.2016.
 */
public interface FacebookFriendCallback {
    void onSuccess(List<User> friends);
    void onError(FacebookRequestError error);
}
