package com.company.wishlist.model;

import com.company.wishlist.bean.EditWishBean;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.annotations.Nullable;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by vladstarikov on 07.01.16.
 */
public class Wish implements Serializable{

    @JsonIgnore String id;
    @SerializedName("wishlist_id") int wishListID;
    @SerializedName("title") String title;
    @SerializedName("comment") String comment;
    @SerializedName("picture") String picture; //URL
    @Nullable @SerializedName("received") Boolean received;
    @Nullable @SerializedName("reserved") Reserved reserved;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getWishListID() {
        return wishListID;
    }

    public void setWishListID(int wishListID) {
        this.wishListID = wishListID;
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
    public boolean isWishReserved() {
        return null != reserved;
    }

    public Wish(){
        id = UUID.randomUUID().toString();
    }

    public Wish(EditWishBean editWishBean) {
        this.id = editWishBean.getId();
        this.title = editWishBean.getTitle();
        this.comment = editWishBean.getComment();
        this.picture = editWishBean.getPicture();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": id = " + id + ", title = " + title + ", comment = " + comment;
    }
}
