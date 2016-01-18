package com.company.wishlist.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.company.wishlist.fragment.FragmentWishList;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.util.FirebaseUtil;

/**
 * Created by v.odahovskiy on 11.01.2016.
 */
public class WishListPageViewAdapter extends FragmentStatePagerAdapter implements IOnFriendSelectedListener{

    FirebaseUtil firebaseUtil;

    public static final String[] tabs = {"Wish list", "Gift list"};
    private boolean isOwner;

    public WishListPageViewAdapter(Context context, FragmentManager fm) {
        super(fm);
        firebaseUtil = new FirebaseUtil(context);
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
        return tabs.length - (isOwner ? 1 : 0);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }

    @Override
    public void onFriendSelected(String id) {
        if (firebaseUtil.getCurrentUser().getId().equals(id)) {
            isOwner = true;
        } else {
            isOwner = false;
        }
    }
}