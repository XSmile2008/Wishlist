package com.company.wishlist.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;

import com.company.wishlist.activity.abstracts.DebugActivity;
import com.company.wishlist.fragment.FragmentWishList;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.util.FirebaseUtil;

/**
 * Created by v.odahovskiy on 11.01.2016.
 */
public class WishListPageViewAdapter extends FragmentStatePagerAdapter {

    private String LOG_TAG = getClass().getSimpleName();

    private static final String[] tabs = {FragmentWishList.WISH_LIST_MODE, FragmentWishList.GIFT_LIST_MODE};

    private String friendId;

    public WishListPageViewAdapter(Context context) {
        super(((AppCompatActivity) context).getSupportFragmentManager());
        //FirebaseUtil firebaseUtil = new FirebaseUtil(context);
        //this.friendId = firebaseUtil.getCurrentUser().getId();
    }

    @Override
    public Fragment getItem(int position) {
        return FragmentWishList.newInstance(tabs[position], friendId);
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