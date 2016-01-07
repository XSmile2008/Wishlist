package com.company.wishlist.model;

/**
 * Created by vladstarikov on 07.01.16.
 */
public class Wish {

    int id;
    int wishListID;
    String title;
    String comment;
    String picture; //URL

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
}
