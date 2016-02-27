package com.company.wishlist.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.company.wishlist.R;
import com.company.wishlist.activity.WishEditActivity;
import com.company.wishlist.fragment.WishListFragment;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.interfaces.IWishItemAdapter;
import com.company.wishlist.model.Reserved;
import com.company.wishlist.model.Wish;
import com.company.wishlist.model.WishList;
import com.company.wishlist.util.FirebaseUtil;
import com.company.wishlist.util.LocalStorage;
import com.company.wishlist.util.Utilities;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vladstarikov on 08.01.16.
 */
public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.Holder> implements IOnFriendSelectedListener, IWishItemAdapter {

    private String LOG_TAG = getClass().getSimpleName();

    private Context context;
    private List<Wish> wishes;
    private int selectedItem = -1;//TODO: remove
    private int mode;//WISH_LIST_MODE or GIFT_LIST_MODE
    private WishEventListener listenersWish;
    private List<Query> queriesWish = new ArrayList<>();
    private Wish wishBackUp;

    /**
     * @param context context that will be used in this adapter
     * @param mode mode of this list. May be WISH_LIST_MODE or GIFT_LIST_MODE
     */
    public WishListAdapter(Context context, int mode) {
        this.context = context;
        this.mode = mode;
        this.wishes = new ArrayList<>();
        this.listenersWish = new WishEventListener();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.wish_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.onBind(wishes.get(position));
    }

    @Override
    public int getItemCount() {
        return wishes.size();
    }

    @Deprecated
    private int findWishIndexById(String id) {
        for (int i = 0; i < wishes.size(); i++) {
            if (wishes.get(i).getId().equals(id)) return i;
        }
        return -1;
    }

    @Override
    public void removeWish(int position) {
        Toast.makeText(context, "item " + position + " removed", Toast.LENGTH_SHORT).show();
        wishBackUp = wishes.get(position);
        wishes.get(position).remove();
    }

    @Override
    public void reserveWish(int position) {
        Toast.makeText(context, "item " + position + " reserved", Toast.LENGTH_SHORT).show();
        if (wishes.get(position).isReserved()) wishes.get(position).unreserve();
        else {
            wishes.get(position).reserve(FirebaseUtil.getCurrentUser().getId(), 1L);
        }
    }

    @Override
    public void restoreWish() {
        Toast.makeText(context, "restore", Toast.LENGTH_SHORT).show();
        wishBackUp.push();
    }

    @Override
    public void onFriendSelected(String id) {
        Log.d(LOG_TAG, "onFriendSelected(" + id + ")");
        wishes.clear();
        for (Query query : queriesWish) query.removeEventListener(listenersWish);//remove all unused listeners
        getWishLists(id);
        notifyDataSetChanged();
    }

    /**
     * Query that get all wishLists that addressed to forUser,
     * and depending at wishList type sort it by owner
     * @param forUser - userId
     */
    private void getWishLists(final String forUser) {
        Log.e(LOG_TAG, "GET wishes for user " + forUser);
        new Firebase(FirebaseUtil.FIREBASE_URL)
                .child(FirebaseUtil.WISH_LIST_TABLE)
                .orderByChild("forUser")
                .equalTo(forUser)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(LOG_TAG, "WishList.onChildAdded" + dataSnapshot);
                        for (DataSnapshot wishListDS : dataSnapshot.getChildren()) {
                            WishList wishList = wishListDS.getValue(WishList.class);
                            wishList.setId(wishListDS.getKey());
                            switch (mode) {
                                case WishListFragment.WISH_LIST_MODE:
                                    if (!wishList.getOwner().equals(FirebaseUtil.getCurrentUser().getId()))
                                        getWishes(wishList.getId());
                                    break;
                                case WishListFragment.GIFT_LIST_MODE:
                                    if (wishList.getOwner().equals(FirebaseUtil.getCurrentUser().getId()))
                                        getWishes(wishList.getId());
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.d("wish_list.onCanceled()", firebaseError.toString());
                    }
                });
    }

    /**
     * Query that getting wishes from wish list that have same wishListId
     * @param wishListId - id of wishList from that we getting wishes
     */
    private void getWishes(final String wishListId) {
        Log.d(LOG_TAG, "punyan =" + wishListId);
        Query query = new Firebase(FirebaseUtil.FIREBASE_URL)
                .child(FirebaseUtil.WISH_TABLE)
                .orderByChild("wishListId")
                .equalTo(wishListId);
        query.addChildEventListener(listenersWish);
        queriesWish.add(query);
    }

    private class WishEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String prevKey) {
            Wish wish = dataSnapshot.getValue(Wish.class);
            wish.setReserved(dataSnapshot.child("reserved").getValue(Reserved.class));
            wish.setId(dataSnapshot.getKey());
            wishes.add(wish);
            notifyDataSetChanged();
            Log.d(LOG_TAG, "Wish.onChildAdded()" + wish.toString());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String prevKey) {
            Wish wish = dataSnapshot.getValue(Wish.class);
            wish.setReserved(dataSnapshot.child("reserved").getValue(Reserved.class));
            wish.setId(dataSnapshot.getKey());
            wishes.set(findWishIndexById(wish.getId()), wish);
            Log.d(LOG_TAG, "Wish.onChildChanged()" + wish.toString());
            notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            wishes.remove(findWishIndexById(dataSnapshot.getKey()));
            notifyDataSetChanged();
            Log.d(LOG_TAG, "Wish.onChildRemoved()" + dataSnapshot.getValue(Wish.class).toString());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String prevKey) {
            Log.d(LOG_TAG, "Wish.onChildMoved()" + dataSnapshot.getValue(Wish.class).toString());
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            Log.d(LOG_TAG, "Wish.onChanceled()" + firebaseError.toString());
        }

    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //CardView
        @Bind(R.id.card_view) CardView cardView;
        @Bind(R.id.image_view) ImageView imageView;
        @Bind(R.id.text_view_title) TextView textViewTitle;
        @Bind(R.id.text_view_comment) TextView textViewComment;

        //Background
        @Bind(R.id.background) ViewGroup background;
        @Bind(R.id.image_view_action) ImageView imageViewAction;
        @Bind(R.id.text_view_action) TextView textViewAction;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        public void onBind(Wish wish) {
            if (wish.getPicture() == null) imageView.setImageResource(R.drawable.gift_icon);//TODO: circle image
            else imageView.setImageBitmap(Utilities.decodeThumbnail(wish.getPicture()));
            textViewTitle.setText(wish.getTitle());
            textViewComment.setText(wish.getComment());
        }

        @Override
        public void onClick(View v) {
            //TODO: remove this
            int selectedItemOld = selectedItem;
            selectedItem = getAdapterPosition();
            if (selectedItem != selectedItemOld) {
                notifyItemChanged(selectedItemOld);
                notifyItemChanged(getAdapterPosition());
            }

            Toast.makeText(context, "item " + getAdapterPosition() + " edit", Toast.LENGTH_SHORT).show();
            LocalStorage.getInstance().setWish(wishes.get(getAdapterPosition()));
            Intent intent = new Intent(context, WishEditActivity.class);
            intent.setAction(WishEditActivity.ACTION_EDIT);
            context.startActivity(intent);
        }

    }

}
