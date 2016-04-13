package com.company.wishlist.model;

import com.company.wishlist.util.AuthUtils;
import com.company.wishlist.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.Firebase;

import java.util.Date;

public class Notification {

    @JsonIgnore private String id;
    private String owner;
    private String reservationDate;

    public Notification() {}

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

    public String getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(String reservationDate) {
        this.reservationDate = reservationDate;
    }

    //TODO: refactoring
    @JsonIgnore
    public String create(Wish wish, Firebase.CompletionListener listener) {
        Firebase ref = getFirebaseRef();
        this.id = wish.getId();
        this.owner = AuthUtils.getCurrentUser().getId();
        this.reservationDate = wish.getReservation().getForDate();
        ref.child(this.id).setValue(this, listener);
        ref.child(this.id).keepSynced(true);
        return this.id;
    }

    @JsonIgnore
    public void remove(Firebase.CompletionListener listener) {
        getFirebaseRef().child(this.id).removeValue(listener);
    }

    @JsonIgnore
    public static Firebase getFirebaseRef() {
        return FirebaseRoot.get().child(Notification.class.getSimpleName());
    }

    @JsonIgnore
    public boolean isTimeToNotify() {
        return DateUtil.isToday(Long.valueOf(this.reservationDate));
    }

}
