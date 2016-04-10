package com.company.wishlist.model;

import com.company.wishlist.util.AuthUtils;
import com.company.wishlist.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.Firebase;

import java.util.Date;

public class Notification {

    public static final int NOTIFY_BEFORE_RESERVATION_DAYS = 1;

    @JsonIgnore private String id;
    private String wishId;
    private String wishTitle;
    private String reservationDate;
    private String notifyDate;
    private String owner;

    public Notification() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWishId() {
        return wishId;
    }

    public void setWishId(String wishId) {
        this.wishId = wishId;
    }

    public String getWishTitle() {
        return wishTitle;
    }

    public void setWishTitle(String wishTitle) {
        this.wishTitle = wishTitle;
    }

    public String getReservationDate() {
        return reservationDate;
    }

    public void setReservationDate(String reservationDate) {
        this.reservationDate = reservationDate;
    }

    public String getNotifyDate() {
        return notifyDate;
    }

    public void setNotifyDate(String notifyDate) {
        this.notifyDate = notifyDate;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    //TODO: refactoring
    @JsonIgnore
    public String create(Firebase.CompletionListener listener, Wish wish) {
        Firebase wishTable = getFirebaseRef();
        this.id = wishTable.push().getKey();
        this.wishId = wish.getId();
        this.wishTitle = wish.getTitle();
        this.reservationDate = wish.getReservation().getForDate();
        this.owner = AuthUtils.getCurrentUser().getId();

        long reserve = Long.valueOf(wish.getReservation().getForDate());
        long notify = DateUtil.isToday(reserve) ? reserve : DateUtil.subtractDaysFromDate(reserve, NOTIFY_BEFORE_RESERVATION_DAYS);
        this.notifyDate = String.valueOf(DateUtil.getDateWithoutTime(new Date(notify)));

        wishTable.child(this.id).setValue(this);
        wishTable.child(this.id).keepSynced(true);
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
    public boolean isToday() {
        return DateUtil.isToday(Long.valueOf(this.notifyDate));
    }

}
