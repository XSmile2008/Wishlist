package com.company.wishlist.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.company.wishlist.fragment.FragmentWishList;

/**
 * Created by v.odahovskiy on 11.01.2016.
 */
public class WishListPageViewAdapter extends FragmentStatePagerAdapter {

    public WishListPageViewAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return new FragmentWishList();
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Tab " + position;
    }
}