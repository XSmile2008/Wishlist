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
    @Nullable
    Reservation reservation;

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

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    /**
     * push new item to database and create unique ID
     * @return generated id for this Wish
     */
    @JsonIgnore
    public String push() {
        return this.push(null);
    }

    /**
     * push new item to database and create unique ID
     * @param listener onCompleteListener
     * @return generated id for this Wish
     */
    @JsonIgnore
    public String push(Firebase.CompletionListener listener) {
        Firebase wishTable = new Firebase(FirebaseUtil.FIREBASE_URL).child(FirebaseUtil.WISH_TABLE);
        String id = wishTable.push().getKey();//TODO: may be will be better if set generated id to this.id
        if (reservation == null) {
            wishTable.child(id).setValue(this, listener);
        } else {
            wishTable.child(id).setValue(this);
            wishTable.child(id).child("reservation").setValue(reservation, listener);
        }
        return id;
    }//TODO: test if this also pushed nested fields like Reservation

    /**
     * Hard remove this item form database
     */
    @JsonIgnore
    public void remove() {
        this.remove(null);
    }

    /**
     * Hard remove this item form database
     * @param listener onCompleteListener
     */
    @JsonIgnore
    public void remove(Firebase.CompletionListener listener) {
        new Firebase(FirebaseUtil.FIREBASE_URL).child(FirebaseUtil.WISH_TABLE).child(id).removeValue(listener);
    }

    /**
     * Reserve this wish in database
     * @param userId user thar reserve this wish
     * @param date reservation date
     */
    @JsonIgnore
    public void reserve(String userId, long date) {
        this.reserve(userId, date, null);
    }

    /**
     * Reserve this wish in database
     * @param userId user thar reserve this wish
     * @param date reservation date
     * @param listener onCompleteListener
     */
    @JsonIgnore
    public void reserve(String userId, long date, Firebase.CompletionListener listener) {
        Firebase wishTable = new Firebase(FirebaseUtil.FIREBASE_URL).child(FirebaseUtil.WISH_TABLE);
        wishTable.child(this.id).child("reservation").setValue(new Reservation(userId, date), listener);
    }

    /**
     * Unreserve this wish in database
     */
    @JsonIgnore
    public void unreserve() {
        this.unreserve(null);
    }

    /**
     * Unreserve this wish in database
     * @param listener onCompleteListener
     */
    @JsonIgnore
    public void unreserve(Firebase.CompletionListener listener) {
        Firebase wishTable = new Firebase(FirebaseUtil.FIREBASE_URL).child(FirebaseUtil.WISH_TABLE);
        wishTable.child(id).child("reservation").removeValue(listener);
    }

    @JsonIgnore
    public boolean isReserved() {
        return null != reservation;
    }

    @JsonIgnore
    public Map<String, Object> toMap() {
        Map<String, Object> hashMap = new HashMap<>();
        if (wishListId != null) hashMap.put("wishListId", wishListId);
        if (title != null) hashMap.put("title", title);
        if (comment != null) hashMap.put("comment", comment);
        if (picture != null) hashMap.put("picture", comment);
        if (received != null) hashMap.put("received", received);
        if (reservation != null) hashMap.put("reservation", reservation);
        return hashMap;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": id = " + id + ", title = " + title + ", comment = " + comment + ", received = " + received + ", reservation = " + reservation;
    }

}
