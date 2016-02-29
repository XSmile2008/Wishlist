package com.company.wishlist.model;

/**
 * Created by v.odahovskiy on 16.01.2016.
 */
public class Reservation {

    String byUser;
    String forDate;

    public Reservation(){}

    public Reservation(String byUser, long forDateInMillis) {
        this.byUser = byUser;
        this.forDate = String.valueOf(forDateInMillis);
    }

    public String getByUser() {
        return byUser;
    }

    public void setByUser(String byUser) {
        this.byUser = byUser;
    }

    public String getForDate() {
        return forDate;
    }

    public void setForDate(String forDate) {
        this.forDate = forDate;
    }
}