package com.company.wishlist.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.company.wishlist.fragment.FragmentWishList;

/**
 * Created by v.odahovskiy on 11.01.2016.
 */
public class WishListPageViewAdapter extends FragmentStatePagerAdapter {

    public static final String[] tabs = {"Wish list", "Gift list"};

    public WishListPageViewAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        Bundle args = new Bundle();
        args.putString("mode", tabs[position]);
        Fragment fragment = new FragmentWishList();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return tabs.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }
}