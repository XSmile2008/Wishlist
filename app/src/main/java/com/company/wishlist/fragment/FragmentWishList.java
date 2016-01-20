package com.company.wishlist.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.company.wishlist.R;
import com.company.wishlist.activity.WishEditActivity;
import com.company.wishlist.adapter.WishListAdapter;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.util.FirebaseUtil;
import com.github.clans.fab.FloatingActionMenu;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vladstarikov on 07.01.16.
 */
public class FragmentWishList extends DebugFragment implements IOnFriendSelectedListener{

    public static final String WISH_LIST_MODE = "Wish list";
    public static final String GIFT_LIST_MODE = "Gift list";
    public static final String FRIEND_ID = "FRIEND_ID";

    private WishListAdapter adapter;
    private FirebaseUtil firebaseUtil;

    @Bind(R.id.floating_action_menu) FloatingActionMenu mFab;
    @Bind(R.id.coordinatorlayout) CoordinatorLayout mCoordinatorLayout;

    public static FragmentWishList newInstance(String mode) {
        FragmentWishList fragment = new FragmentWishList();
        Bundle args = new Bundle();
        args.putString("mode", mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseUtil = new FirebaseUtil(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wish_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        //mFab = (FloatingActionMenu) view.findViewById(R.id.floating_action_menu);
        Bundle bundle = getArguments();
        String mode = bundle.getString("mode");
        if (mode.equals(WISH_LIST_MODE)) {
            mFab.setVisibility(View.GONE);
        }
        //String friendId = bundle.getString(FRIEND_ID, null);
        adapter = new WishListAdapter(getContext(), mode, firebaseUtil.getCurrentUser().getId());//TODO:
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onFriendSelected(String id) {
        adapter.onFriendSelected(id);
    }

    @OnClick({R.id.floating_action_button_add, R.id.floating_action_button_choose})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.floating_action_button_add:
                mFab.toggle(true);
                Intent intent = new Intent(getContext(), WishEditActivity.class);
                intent.setAction(WishEditActivity.ACTION_CREATE);
                startActivity(intent);
                return;
            case R.id.floating_action_button_choose:
                Snackbar.make(mCoordinatorLayout, "action button close", Snackbar.LENGTH_LONG).show();
                mFab.close(true);
                break;
        }
    }

}
