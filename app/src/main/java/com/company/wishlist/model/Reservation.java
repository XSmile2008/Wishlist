package com.company.wishlist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by v.odahovskiy on 16.01.2016.
 */
public class Reservation implements Serializable{

    String byUser;
    String forDate;//TODO: use long value?

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

    @JsonIgnore
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (byUser != null) map.put("byUser", byUser);
        if (forDate != null) map.put("forDate", forDate);
        return map;
    }

}