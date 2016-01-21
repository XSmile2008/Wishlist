package com.company.wishlist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by vladstarikov on 07.01.16.
 */
public class WishList {

    @JsonIgnore String id;
    String owner;
    String forUser;

    public WishList(){}

    public WishList(String id, String owner, String forUser) {
        this.id = id;
        this.owner = owner;
        this.forUser = forUser;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getForUser() {
        return forUser;
    }

    public void setForUser(String forUser) {
        this.forUser = forUser;
    }
}
