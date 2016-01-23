package com.company.wishlist.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;

import com.company.wishlist.fragment.WishListFragment;

/**
 * Created by v.odahovskiy on 11.01.2016.
 */
public class WishListPageViewAdapter extends FragmentStatePagerAdapter {

    private String LOG_TAG = getClass().getSimpleName();

    private static final String[] tabs = {WishListFragment.WISH_LIST_MODE, WishListFragment.GIFT_LIST_MODE};

    private String friendId;

    public WishListPageViewAdapter(Context context, String friendId) {
        super(((AppCompatActivity) context).getSupportFragmentManager());
        this.friendId = friendId;
    }

    @Override
    public Fragment getItem(int position) {
        return WishListFragment.newInstance(tabs[position], friendId);
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