package com.company.wishlist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.Firebase;

import java.io.Serializable;

/**
 * Created by vladstarikov on 07.01.16.
 */
public class WishList implements Serializable {

    @JsonIgnore String id;
    String owner;
    String forUser;

    public WishList(){}

    public WishList(String owner, String forUser) {
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

    @JsonIgnore
    public static Firebase getFirebaseRef() {
        return FirebaseRoot.get().child(WishList.class.getSimpleName());
    }

    @JsonIgnore
    public String push() {
        this.id = push(null);
        return this.id;
    }

    @JsonIgnore
    public String push(Firebase.CompletionListener listener) {
        Firebase wishListTable = getFirebaseRef();
        this.id = wishListTable.push().getKey();
        wishListTable.child(this.id).setValue(this, listener);
        return this.id;
    }

}
