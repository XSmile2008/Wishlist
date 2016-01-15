package com.company.wishlist.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by vladstarikov on 07.01.16.
 */
public class WishList {

    @SerializedName("id") int id;
    @SerializedName("owner") long owner;
    @SerializedName("for_user")long forUser;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getOwner() {
        return owner;
    }

    public void setOwner(long owner) {
        this.owner = owner;
    }

    public long getForUser() {
        return forUser;
    }

    public void setForUser(long forUser) {
        this.forUser = forUser;
    }
}
