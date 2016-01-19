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
import android.widget.Toast;

import com.company.wishlist.R;
import com.company.wishlist.adapter.WishListAdapter;
import com.company.wishlist.adapter.WishListPageViewAdapter;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.model.WishList;
import com.company.wishlist.util.FirebaseUtil;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;

/**
 * Created by vladstarikov on 07.01.16.
 */
public class FragmentWishList extends DebugFragment implements IOnFriendSelectedListener{

    public static final String FRIEND_ID = "FRIEND_ID";

    WishListAdapter adapter;
    RecyclerView recyclerView;
    FirebaseUtil firebaseUtil;
    private String friendId;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseUtil = new FirebaseUtil(getContext());

        Bundle bundle = getArguments();
        String mode = bundle.getString("mode");
        friendId = bundle.getString(FRIEND_ID, null);
        String currentUserId = firebaseUtil.getCurrentUser().getId();
    }

    @Override
    public void onFriendSelected(String id) {
        Log.d(this.getClass().getSimpleName(), ".onFriendSelected(" + id + ")");
        ((IOnFriendSelectedListener) recyclerView.getAdapter()).onFriendSelected(id);
    }
}
