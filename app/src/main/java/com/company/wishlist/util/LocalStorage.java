package com.company.wishlist.util;

import com.company.wishlist.model.Wish;

/**
 * Created by v.odahovskiy on 19.01.2016.
 */
public class LocalStorage {

    private static LocalStorage instance;

    private Wish wish;

    public static synchronized LocalStorage getInstance() {
        if (instance == null) {
            instance = new LocalStorage();
        }
        return instance;
    }

    public static void setInstance(LocalStorage instance) {
        LocalStorage.instance = instance;
    }

    public Wish getWish() {
        return wish;
    }

    public void setWish(Wish wish) {
        this.wish = wish;
    }
}
