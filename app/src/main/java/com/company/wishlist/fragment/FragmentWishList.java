package com.company.wishlist.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.company.wishlist.R;
import com.company.wishlist.adapter.WishListAdapter;
import com.company.wishlist.model.Wish;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vladstarikov on 07.01.16.
 */
public class FragmentWishList extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wish_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WishListAdapter adapter = new WishListAdapter(createTestData());
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    @java.lang.Deprecated
    private List<Wish> createTestData() {
        List<Wish> list = new ArrayList<>();
        while (list.size() < 11) {
            Wish wish = new Wish();
            wish.setTitle("Title" + list.size());
            wish.setComment("This is comment for wish#" + list.size());
            list.add(wish);
        }
        return list;
    }
}
