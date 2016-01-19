package com.company.wishlist.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by v.odahovskiy on 16.01.2016.
 */
public class Reserved {

    @SerializedName("by_user") String byUser;
    @SerializedName("for_date") String forDate;

    public Reserved(){}

    public Reserved(String byUser, String forDate) {
        this.byUser = byUser;
        this.forDate = forDate;
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