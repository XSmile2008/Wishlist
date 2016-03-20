package com.company.wishlist.model;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Wish implements Serializable {

    @JsonIgnore
    String id;
    String wishListId;
    String title;
    String comment;
    String picture;
    Reservation reservation;
    Boolean isReceived;
    Boolean isRemoved;

    public Wish() {}

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

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Boolean getIsReceived() {
        return isReceived;
    }

    public void setIsReceived(Boolean isReceived) {
        this.isReceived = isReceived;
    }

    public Boolean getIsRemoved() {
        return isRemoved;
    }

    public void setIsRemoved(Boolean isRemoved) {
        this.isRemoved = isRemoved;
    }

    @JsonIgnore
    public static Firebase getFirebaseRef() {
        return FirebaseRoot.get().child(Wish.class.getSimpleName());
    }

    /**
     * push new item to database and create unique ID
     *
     * @return generated id for this Wish
     */
    @JsonIgnore
    public String push() {
        return this.push(null);
    }

    /**
     * push new item to database and create unique ID
     *
     * @param listener onCompleteListener
     * @return generated id for this Wish
     */
    @JsonIgnore
    public String push(Firebase.CompletionListener listener) {
        Firebase wishTable = getFirebaseRef();
        this.id = wishTable.push().getKey();
        if (reservation == null) {
            wishTable.child(id).setValue(this, listener);
        } else {
            wishTable.child(id).setValue(this);
            wishTable.child(id).child("reservation").setValue(reservation, listener);
        }
        return this.id;
    }//TODO: test if this also pushed nested fields like Reservation

    /**
     * Hard remove this item form database
     *
     * @param listener onCompleteListener
     */
    @JsonIgnore
    public void remove(Firebase.CompletionListener listener) {
        getFirebaseRef().child(id).removeValue(listener);
    }

    /**
     * Soft remove this item form database
     */
    @JsonIgnore
    public void softRemove() {
        this.softRemove(null);
        removeNotification();
    }

    /**
     * Soft remove this item form database
     *
     * @param listener onCompleteListener
     */
    @JsonIgnore
    public void softRemove(Firebase.CompletionListener listener) {
        this.isRemoved = true;
        getFirebaseRef().child(id).child("isRemoved").setValue(true, listener);
    }

    /**
     * Soft remove this item form database
     */
    @JsonIgnore
    public void softRestore() {
        this.softRestore(null);
        createNotification();
    }

    private void createNotification() {
        if (isReserved()) {
            new Notification().create(null, this);
        }
    }

    /**
     * Soft remove this item form database
     *
     * @param listener onCompleteListener
     */
    @JsonIgnore
    public void softRestore(Firebase.CompletionListener listener) {
        this.isRemoved = null;
        getFirebaseRef().child(id).child("isRemoved").removeValue(listener);
    }

    /**
     * Reserve this wish in database
     *
     * @param userId user thar reserve this wish
     * @param date   reservation date
     */
    @JsonIgnore
    public void reserve(String userId, long date) {
        this.reserve(userId, date, null);
        createNotification();
    }

    /**
     * Reserve this wish in database
     *
     * @param userId   user thar reserve this wish
     * @param date     reservation date
     * @param listener onCompleteListener
     */
    @JsonIgnore
    public void reserve(String userId, long date, Firebase.CompletionListener listener) {
        this.reservation = new Reservation(userId, date); //TODO: check it
        getFirebaseRef().child(this.id).child("reservation").setValue(this.reservation, listener);
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
     *
     * @param listener onCompleteListener
     */
    @JsonIgnore
    public void unreserve(Firebase.CompletionListener listener) {
        this.reservation = null; //TODO: check it
        getFirebaseRef().child(id).child("reservation").removeValue(listener);
        removeNotification();//TODO: test it!
    }

    @JsonIgnore
    public boolean isReserved() {
        return null != reservation;
    }

    @JsonIgnore
    public boolean isRemoved() {
        return null != isRemoved;
    }

    @JsonIgnore
    private void removeNotification() {
        Notification.getFirebaseRef().orderByChild("wishId").equalTo(this.getId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Notification notification = ds.getValue(Notification.class);
                            notification.setId(ds.getKey());
                            notification.remove(new Firebase.CompletionListener() {
                                @Override
                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    Log.d(this.getClass().getSimpleName(), " Notification for this wish was removed");
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
    }

    @JsonIgnore public static void clearAllSoftRemovedForUser(String userId){
        Firebase ref = getFirebaseRef();
        if (null == ref) { throw new NullPointerException("Firebase reference should be initialized"); }
        if (null == userId){ throw new IllegalArgumentException("User id should be not null"); }
        WishList.getFirebaseRef()
                .orderByChild("owner")
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot wishListDS : dataSnapshot.getChildren()) {
                            Wish.getFirebaseRef()
                                    .orderByChild("wishListId")
                                    .equalTo(wishListDS.getKey())
                                    .addChildEventListener(new ChildEventListener() {

                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                            Wish wish = dataSnapshot.getValue(Wish.class);
                                            wish.setId(dataSnapshot.getKey());
                                            if (wish.isRemoved()) {
                                                wish.remove(null);
                                            }
                                        }

                                        @Override
                                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                                        }

                                        @Override
                                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                                        }

                                        @Override
                                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.d("wish_list.onCanceled()", firebaseError.toString());
                    }
                });
    }

    @JsonIgnore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (wishListId != null) map.put("wishListId", wishListId);
        if (title != null) map.put("title", title);
        if (comment != null) map.put("comment", comment);
        if (picture != null) map.put("picture", comment);
        if (isReceived != null) map.put("isReceived", isReceived);
        if (reservation != null) map.put("reservation", reservation);
        return map;
    }


    @Override
    public boolean equals(Object o) {
        return (o != null) && (o instanceof Wish) && (this.hashCode() == o.hashCode());
    }

    @Override
    public int hashCode() {
        int titleHash = title != null ? title.hashCode() : 0;
        int commentHash = comment != null ? comment.hashCode() : 0;
        int pictureHash = picture != null ? picture.hashCode() : 0;
        return titleHash + commentHash + pictureHash;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ": id = " + id + ", title = " + title + ", comment = " + comment + ", reservation = " + reservation + ", isReceived = " + isReceived + ", isRemoved = " + isRemoved;
    }

}
