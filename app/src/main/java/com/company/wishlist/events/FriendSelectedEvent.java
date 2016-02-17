package com.company.wishlist.events;

/**
 * Created by vladstarikov on 17.02.16.
 */
public class FriendSelectedEvent {
    private String friendId;

    public FriendSelectedEvent(String friendId) {
        this.friendId = friendId;
    }

    public String getFriendId() {
        return friendId;
    }
}
