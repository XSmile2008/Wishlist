package com.company.wishlist.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.company.wishlist.R;
import com.company.wishlist.adapter.FriendListAdapter;
import com.company.wishlist.adapter.WishListPageViewAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vladstarikov on 23.01.16.
 */
public class TabbedWishListFragment extends DebugFragment {

    WishListPageViewAdapter wishListPageViewAdapter;

    @Bind(R.id.tab_layout) TabLayout tabLayout;
    @Bind(R.id.view_pager) ViewPager viewPager;

    public static TabbedWishListFragment newInstance(String friendId) {
        Bundle args = new Bundle();
        args.putString(FriendListAdapter.FRIEND_ID, friendId);
        TabbedWishListFragment fragment = new TabbedWishListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tabed_wish_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        wishListPageViewAdapter = new WishListPageViewAdapter(getContext(), getArguments().getString(FriendListAdapter.FRIEND_ID));
        viewPager.setAdapter(wishListPageViewAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        for (Fragment fragment : wishListPageViewAdapter.getFragments()) {
            transaction.remove(fragment);
        }
        transaction.commit();
    }
}
