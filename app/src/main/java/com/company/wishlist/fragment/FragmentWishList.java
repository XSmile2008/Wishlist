package com.company.wishlist.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.company.wishlist.R;
import com.company.wishlist.adapter.WishListAdapter;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;

/**
 * Created by vladstarikov on 07.01.16.
 */
public class FragmentWishList extends DebugFragment implements IOnFriendSelectedListener{

    WishListAdapter adapter;
    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wish_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter = new WishListAdapter(getContext());
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onFriendSelected(String id) {
        Log.d(LOG_TAG, "onFriendSelected(" + id + ")");
    }

    public static FragmentWishList newInstance(String mode) {
        FragmentWishList fragment = new FragmentWishList();
        Bundle args = new Bundle();
        args.putString("mode", mode);
        fragment.setArguments(args);
        return fragment;
    }

}
