package com.company.wishlist.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.company.wishlist.R;
import com.company.wishlist.adapter.WishListPageViewAdapter;
import com.company.wishlist.model.User;
import com.company.wishlist.view.CustomViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by vladstarikov on 23.01.16.
 */
public class TabbedWishListFragment extends Fragment {

    @BindView(R.id.tab_layout) TabLayout mTabLayout;
    @BindView(R.id.view_pager) CustomViewPager mViewPager;

    public static TabbedWishListFragment newInstance(User friend) {
        Bundle args = new Bundle();
        args.putSerializable(User.class.getSimpleName(), friend);
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
        User user = (User) getArguments().getSerializable(User.class.getSimpleName());
        mViewPager.setAdapter(new WishListPageViewAdapter(getChildFragmentManager(), user));
        mViewPager.setSwiping(false);
        mTabLayout.setupWithViewPager(mViewPager);
    }

}
