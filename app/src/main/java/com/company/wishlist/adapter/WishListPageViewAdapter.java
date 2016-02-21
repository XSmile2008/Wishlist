package com.company.wishlist.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;

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

    public WishListPageViewAdapter(Context context, String friendId) {
        super(((AppCompatActivity) context).getSupportFragmentManager());
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

    public List<Fragment> getFragments() {
        return fragments;
    }
}