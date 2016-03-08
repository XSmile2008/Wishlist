package com.company.wishlist.events;

import com.company.wishlist.model.User;

/**
 * Created by vladstarikov on 17.02.16.
 */
public class FriendSelectedEvent {
    private User friend;

    public FriendSelectedEvent(User friend) {
        this.friend = friend;
    }

    public User getFriend() {
        return friend;
    }
}
