package com.company.wishlist.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.company.wishlist.R;
import com.company.wishlist.activity.WishEditActivity;
import com.company.wishlist.adapter.FriendListAdapter;
import com.company.wishlist.adapter.WishListAdapter;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.model.WishList;
import com.company.wishlist.util.FirebaseUtil;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.github.clans.fab.FloatingActionMenu;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vladstarikov on 07.01.16.
 */
public class WishListFragment extends DebugFragment implements IOnFriendSelectedListener{

    public static final String MY_WISH_LIST_MODE = "My wish list";//TODO
    public static final String WISH_LIST_MODE = "Wish list";
    public static final String GIFT_LIST_MODE = "Gift list";
    public static final String WISH_LIST_ID = "WISH_LIST_ID";
    public static final String MODE = "mode";

    private WishListAdapter adapter;
    private FirebaseUtil firebaseUtil;
    private String wishListId;
    private OnFriendSelectedReceiver receiver;

    @Bind(R.id.fab_menu) FloatingActionMenu mFab;

    public static WishListFragment newInstance(String mode, String friendId) {
        WishListFragment fragment = new WishListFragment();
        Bundle args = new Bundle();
        args.putString(MODE, mode);
        args.putString(FriendListAdapter.FRIEND_ID, friendId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseUtil = new FirebaseUtil(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FriendListAdapter.ON_FRIEND_SELECTED);
        receiver = new OnFriendSelectedReceiver();
        getContext().registerReceiver(receiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(receiver);
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
        Bundle bundle = getArguments();
        String mode = bundle.getString(MODE);
        String friendId = bundle.getString(FriendListAdapter.FRIEND_ID);
        if (mode.equals(WISH_LIST_MODE)) {
            mFab.setVisibility(View.GONE);
        }
        adapter = new WishListAdapter(getContext(), mode, friendId);//TODO:
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        onFriendSelected(friendId);
    }

    @OnClick({R.id.fab_add, R.id.fab_choose})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add:
                mFab.toggle(true);
                Intent intent = new Intent(getContext(), WishEditActivity.class)
                        .setAction(WishEditActivity.ACTION_CREATE)
                        .putExtra(WISH_LIST_ID, wishListId);
                startActivity(intent);
                return;
            case R.id.fab_choose:
                mFab.close(true);
                break;
        }
    }

    @Override
    public void onFriendSelected(final String friendId) {
        firebaseUtil.getFirebaseRoot().child(FirebaseUtil.WISH_LIST_TABLE)
                .orderByChild("forUser")
                .equalTo(friendId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(LOG_TAG, ".onDataChange(" + dataSnapshot + ")");
                        for (DataSnapshot wishListDS : dataSnapshot.getChildren()) {
                            if (wishListDS.getValue(WishList.class).getOwner().equals(firebaseUtil.getCurrentUser().getId())) {
                                wishListId = wishListDS.getKey();
                                adapter.onFriendSelected(friendId);
                                return;
                            }
                        }
                        WishList wishList = new WishList(dataSnapshot.getRef().push().getKey(), firebaseUtil.getCurrentUser().getId(), friendId);
                        dataSnapshot.getRef().child(wishList.getId()).setValue(wishList);
                        wishListId = wishList.getId();
                        adapter.onFriendSelected(friendId);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e(LOG_TAG, firebaseError.toString());
                    }
                });
    }

    public class OnFriendSelectedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String friendId = intent.getStringExtra(FriendListAdapter.FRIEND_ID);
            onFriendSelected(friendId);
        }
    }

}