package com.company.wishlist.model;

/**
 * Created by vladstarikov on 07.01.16.
 */
public class WishList {

    int id;
    long owner;
    long forUser;

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
