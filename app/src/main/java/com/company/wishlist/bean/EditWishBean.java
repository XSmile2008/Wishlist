package com.company.wishlist.bean;

import com.company.wishlist.model.Wish;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by v.odahovskiy on 19.01.2016.
 */
public class EditWishBean extends Wish{

    @JsonIgnore private Wish wish;

    public EditWishBean() {}

    public EditWishBean(Wish wish){
        this.wish = wish;
        this.setId(wish.getId());
        this.setPicture(wish.getPicture());
        this.setTitle(wish.getTitle());
        this.setComment(wish.getComment());
        this.setWishListId(wish.getWishListId());
        this.setReserved(wish.getReserved());
    }

    @JsonIgnore
    public Map<String, Object> getMapToUpdate() {
        Map<String, Object> result = new HashMap<>();
        if (isDifferent(getPicture(), wish.getPicture())) {
            result.put("picture", getPicture());
        }
        if (isDifferent(getTitle(), wish.getTitle())) {
            result.put("title", getTitle());
        }
        if (isDifferent(getComment(), wish.getComment())) {
            result.put("comment", getComment());
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