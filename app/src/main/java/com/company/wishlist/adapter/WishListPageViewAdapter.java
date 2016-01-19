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
public class WishListPageViewAdapter extends FragmentStatePagerAdapter implements IOnFriendSelectedListener{

    private String LOG_TAG = getClass().getSimpleName();

    FirebaseUtil firebaseUtil;

    public static final String[] tabs = {"Wish list", "Gift list"};
    private boolean isOwner = true;

    FragmentManager fragmentManager;
    TabLayout tabLayout;

    public WishListPageViewAdapter(Context context, TabLayout tabLayout) {
        super(((AppCompatActivity) context).getSupportFragmentManager());
        this.fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
        this.firebaseUtil = new FirebaseUtil(context);
        this.tabLayout = tabLayout;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d(LOG_TAG, ".getItem(" + position + ")");
        return FragmentWishList.newInstance(tabs[position]);
    }

    @Override
    public int getCount() {
        return isOwner ? 1 : 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        //fragmentManager.beginTransaction().remove((Fragment)object).commit();
        Log.d(LOG_TAG, ".destroyItem()");
    }

    @Override
    public void onFriendSelected(String id) {
        isOwner = firebaseUtil.getCurrentUser().getId().equals(id);
        notifyDataSetChanged();
        for (Fragment fragment: fragmentManager.getFragments()) {
            if (fragment instanceof FragmentWishList)
                ((FragmentWishList)fragment).onFriendSelected(id);
        }
        Log.d(LOG_TAG, fragmentManager.getFragments().toString());
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        tabLayout.setTabsFromPagerAdapter(this);
    }
}