package com.company.wishlist.model;

import com.firebase.client.annotations.Nullable;
import com.google.gson.annotations.SerializedName;

/**
 * Created by vladstarikov on 07.01.16.
 */
public class Wish {

    @SerializedName("id") int id;
    @SerializedName("wishlist_id") int wishListID;
    @SerializedName("title") String title;
    @SerializedName("comment") String comment;
    @SerializedName("picture") String picture; //URL
    @Nullable @SerializedName("received") Boolean received;
    @Nullable @SerializedName("reserved") Reserved reserved;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public class Reserved {
        @SerializedName("by_user") String byUser;
        @SerializedName("for_date") String forDate;
    }

}
