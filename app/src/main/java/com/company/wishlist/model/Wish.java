package com.company.wishlist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.annotations.Nullable;

import java.io.Serializable;

/**
 * Created by vladstarikov on 07.01.16.
 */
public class Wish implements Serializable{

    @JsonIgnore String id;
    String wishListId;
    String title;
    String comment;
    String picture; //URL
    @Nullable Boolean received;
    @Nullable Reserved reserved;

    public Wish(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWishListId() {
        return wishListId;
    }

    public void setWishListId(String wishListId) {
        this.wishListId = wishListId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Boolean getReceived() {
        return received;
    }

    public void setReceived(Boolean received) {
        this.received = received;
    }

    public Reserved getReserved() {
        return reserved;
    }

    public void setReserved(Reserved reserved) {
        this.reserved = reserved;
    }

    public void reserve(String userId, long dateInMillis) {
        this.reserved = new Reserved(userId, dateInMillis);
    }

    @JsonIgnore
    public boolean isReserved() {
        return null != reserved;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": id = " + id + ", title = " + title + ", comment = " + comment;
    }
}
