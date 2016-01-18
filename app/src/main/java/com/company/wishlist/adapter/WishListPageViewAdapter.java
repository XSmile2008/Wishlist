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
import android.view.ViewGroup;

import com.company.wishlist.fragment.FragmentWishList;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.util.FirebaseUtil;

/**
 * Created by v.odahovskiy on 11.01.2016.
 */
public class WishListPageViewAdapter extends FragmentStatePagerAdapter implements IOnFriendSelectedListener{

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
        Bundle args = new Bundle();
        args.putString("mode", tabs[position]);
        Fragment fragment = new FragmentWishList();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return isOwner ? 1 : 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabs[position];
    }

    /*@Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        ((Fragment)object).getFragmentManager().beginTransaction().remove((Fragment)object).commit();
    }*/

    @Override
    public void onFriendSelected(String id) {
        if (firebaseUtil.getCurrentUser().getId().equals(id)) {
            isOwner = true;
        } else {
            isOwner = false;
        }
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        tabLayout.setTabsFromPagerAdapter(this);
    }
}