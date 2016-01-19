package com.company.wishlist.bean;

import com.company.wishlist.model.Wish;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by v.odahovskiy on 19.01.2016.
 */
public class EditWishBean {

    @JsonIgnore private String id;
    @SerializedName("wishlist_id") int wishListID;
    @SerializedName("title") String title;
    @SerializedName("comment") String comment;
    @SerializedName("picture") String picture;
    @JsonIgnore private boolean reserved;
    @JsonIgnore private Wish wish;

    public EditWishBean() {}

    public EditWishBean(Wish wish){
        this.wish = wish;
        this.id = wish.getId();
        this.picture = wish.getPicture();
        this.title = wish.getTitle();
        this.comment = wish.getComment();
        this.wishListID = wish.getWishListID();
        this.reserved = null != wish.getReserved();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
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

    public int getWishListID() {
        return wishListID;
    }

    public void setWishListID(int wishListID) {
        this.wishListID = wishListID;
    }

    @JsonIgnore
    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    @JsonIgnore
    public Map<String, Object> getMapToUpdate() {
        Map<String, Object> result = new HashMap<>();
        if (isDifferent(picture, wish.getPicture())) {
            result.put("picture", picture);
        }
        if (isDifferent(title, wish.getTitle())) {
            result.put("title", title);
        }
        if (isDifferent(comment, wish.getComment())) {
            result.put("comment", comment);
        }
        return result;
    }

    /**
     * Check if field of edit bean equals for initial state
     * @param value current value
     * @param parentValue initial value
     * @return true if fields has different values
     */
    private boolean isDifferent(String value, String parentValue) {
        return (null != value && !value.equals(parentValue));
    }
}
