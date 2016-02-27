package com.company.wishlist.model;

import com.company.wishlist.util.FirebaseUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.Firebase;
import com.firebase.client.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vladstarikov on 07.01.16.
 */
public class Wish implements Serializable{

    @JsonIgnore String id;
    String wishListId;
    String title;
    String comment;
    String picture;
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

    /**
     * push new item to database and create unique ID
     * @return generated id for this Wish
     */
    @JsonIgnore
    public String push() {
        Firebase wishTable = new Firebase(FirebaseUtil.FIREBASE_URL).child(FirebaseUtil.WISH_TABLE);
        String id = wishTable.push().getKey();//TODO: may be will be better if set generated id to this.id
        wishTable.child(id).setValue(this);
        return id;
    }

    /**
     * Hard remove this item form database
     */
    @JsonIgnore
    public void remove() {
        new Firebase(FirebaseUtil.FIREBASE_URL).child(FirebaseUtil.WISH_TABLE).child(id).removeValue();
    }

    /**
     * reserve this wish in database
     * @param userId - user thar reserve this wish
     * @param dateInMillis - reservation date
     */
    @JsonIgnore
    public void reserve(String userId, long dateInMillis) {
        Firebase wishTable = new Firebase(FirebaseUtil.FIREBASE_URL).child(FirebaseUtil.WISH_TABLE);
        wishTable.child(this.id).child("reserved").setValue(new Reserved(userId, dateInMillis));
    }

    @JsonIgnore
    public void unreserve() {//TODO: may be add CompletionListener
        Firebase wishTable = new Firebase(FirebaseUtil.FIREBASE_URL).child(FirebaseUtil.WISH_TABLE);
        wishTable.child(id).child("reserved").removeValue();
    }

    @JsonIgnore
    public boolean isReserved() {
        return null != reserved;
    }

    @JsonIgnore
    public Map<String, Object> toMap() {
        Map<String, Object> hashMap = new HashMap<>();
        if (wishListId != null) hashMap.put("wishListId", wishListId);
        if (title != null) hashMap.put("title", title);
        if (comment != null) hashMap.put("comment", comment);
        if (picture != null) hashMap.put("picture", comment);
        if (received != null) hashMap.put("received", received);
        if (reserved != null) hashMap.put("reserved", reserved);
        return hashMap;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": id = " + id + ", title = " + title + ", comment = " + comment + ", received = " + received + ", reserved = " + reserved;
    }

}
