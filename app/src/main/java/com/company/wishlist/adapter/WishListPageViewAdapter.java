package com.company.wishlist.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.company.wishlist.fragment.WishListFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by v.odahovskiy on 11.01.2016.
 */
public class WishListPageViewAdapter extends FragmentStatePagerAdapter {

    private String LOG_TAG = getClass().getSimpleName();

    private static final String[] tabs = {"Wish list", "Gift list"};

    private List<Fragment> fragments = new ArrayList<>(tabs.length);

    public WishListPageViewAdapter(FragmentManager fm, String friendId) {
        super(fm);
        fragments.add(WishListFragment.newInstance(WishListFragment.WISH_LIST_MODE, friendId));
        fragments.add(WishListFragment.newInstance(WishListFragment.GIFT_LIST_MODE, friendId));
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
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