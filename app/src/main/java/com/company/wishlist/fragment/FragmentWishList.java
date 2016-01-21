package com.company.wishlist.fragment;

import android.content.Intent;
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
import com.company.wishlist.adapter.WishListAdapter;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.model.WishList;
import com.company.wishlist.util.FirebaseUtil;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
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
    public static final String WISH_LIST_ID = "WISH_LIST_ID";
    public static final String FRIEND_ID = "FRIEND_ID";
    public static final String MODE = "mode";

    private WishListAdapter adapter;
    private FirebaseUtil firebaseUtil;
    private String friendId;

    @Bind(R.id.fab_menu) FloatingActionMenu mFab;

    public static FragmentWishList newInstance(String mode, String friendId) {
        FragmentWishList fragment = new FragmentWishList();
        Bundle args = new Bundle();
        args.putString(MODE, mode);
        args.putString(FRIEND_ID, friendId);
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
        String mode = bundle.getString(MODE);
        friendId = bundle.getString(FRIEND_ID);
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


    public void onClick(View v, String wishListId) {
        switch (v.getId()) {
            case R.id.fab_add:
                mFab.toggle(true);
                Intent intent = new Intent(getContext(), WishEditActivity.class)
                        .setAction(WishEditActivity.ACTION_CREATE)
                        .putExtra(WISH_LIST_ID, wishListId);
                startActivity(intent);
                return;
            case R.id.fab_choose:
                //Snackbar.make(mCoordinatorLayout, "action button close", Snackbar.LENGTH_LONG).show();
                mFab.close(true);
                break;
        }
    }

    //TODO:  may be call this check on friend selected
    @OnClick({R.id.fab_menu, R.id.fab_add, R.id.fab_choose})
    public void initWishList(final View view) {
        final Firebase wishListTable = firebaseUtil.getFirebaseRoot().child(firebaseUtil.WISH_LIST_TABLE);
        wishListTable
                .orderByChild("forUser")
                .equalTo(friendId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(LOG_TAG, ".onDataChange(" + dataSnapshot + ")");
                        for (DataSnapshot wishListDS : dataSnapshot.getChildren()) {
                            if (wishListDS.getValue(WishList.class).getOwner().equals(firebaseUtil.getCurrentUser().getId())) {
                                onClick(view, wishListDS.getKey());
                                return;
                            }
                        }
                        WishList wishList = new WishList(wishListTable.push().getKey(), firebaseUtil.getCurrentUser().getId(), friendId);
                        wishListTable.child(wishList.getId()).setValue(wishList);
                        onClick(view, wishList.getId());
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e(LOG_TAG, firebaseError.toString());
                    }
                });
    }

}
