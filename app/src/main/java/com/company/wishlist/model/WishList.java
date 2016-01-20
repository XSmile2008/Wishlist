package com.company.wishlist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vladstarikov on 07.01.16.
 */
public class WishList {

    @JsonIgnore String id;
    @SerializedName("owner") String owner;
    @SerializedName("for_user") String forUser;

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
