package com.company.wishlist.bean;

import android.support.annotation.Nullable;

import com.company.wishlist.model.Wish;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by v.odahovskiy on 19.01.2016.
 */
public class EditWishBean extends Wish {

    @JsonIgnore private Wish wish;

    public EditWishBean() {}

    public EditWishBean(@Nullable Wish wish) {
        this.wish = wish;
        if (wish != null) {
            this.setId(wish.getId());
            this.setPicture(wish.getPicture());
            this.setTitle(wish.getTitle());
            this.setComment(wish.getComment());
            this.setWishListId(wish.getWishListId());
            this.setReservation(wish.getReservation());
            this.setIsRemoved(wish.getIsRemoved());
        }
    }

    @JsonIgnore
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (isDifferent(getPicture(), wish.getPicture())) {
            map.put("picture", getPicture());
        }
        if (isDifferent(getTitle(), wish.getTitle())) {
            map.put("title", getTitle());
        }
        if (isDifferent(getComment(), wish.getComment())) {
            map.put("comment", getComment());
        }
        if (this.getReservation() != this.getOriginalWish().getReservation()) {
            if (getReservation() == null) map.put("reservation", null);
            else map.put("reservation", getReservation().toMap());
        }
        return map;
    }

    @JsonIgnore
    public Wish getOriginalWish() {
        return wish;
    }

    @JsonIgnore
    public boolean isPictureChanged() {
        return this.getPicture() != null && (wish.getPicture() == null || !this.getPicture().equals(wish.getPicture()));
    }

    /**
     * Check if field of edit bean equals for initial state
     *
     * @param value       current value
     * @param parentValue initial value
     * @return true if fields has different values
     */
    private boolean isDifferent(String value, String parentValue) {
        return (null != value && !value.equals(parentValue));
    }

}
