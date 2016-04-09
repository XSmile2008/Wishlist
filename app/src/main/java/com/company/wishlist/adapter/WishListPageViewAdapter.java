package com.company.wishlist.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.company.wishlist.fragment.WishListFragment;
import com.company.wishlist.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by v.odahovskiy on 11.01.2016.
 */
public class WishListPageViewAdapter extends FragmentStatePagerAdapter {

    private static final String[] TABS = {"Wish list", "Gift list"};

    private List<Fragment> mFragments = new ArrayList<>(TABS.length);

    public WishListPageViewAdapter(FragmentManager fm, User friend) {
        super(fm);
        mFragments.add(WishListFragment.newInstance(WishListFragment.WISH_LIST_MODE, friend));
        mFragments.add(WishListFragment.newInstance(WishListFragment.GIFT_LIST_MODE, friend));
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return TABS.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TABS[position];
    }

}