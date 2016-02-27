package com.company.wishlist.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.company.wishlist.R;
import com.company.wishlist.activity.WishEditActivity;
import com.company.wishlist.fragment.WishListFragment;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.model.Wish;
import com.company.wishlist.util.FirebaseUtil;
import com.company.wishlist.util.LocalStorage;
import com.company.wishlist.util.Utilities;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vladstarikov on 08.01.16.
 */
public class TopWishListAdapter extends RecyclerView.Adapter<TopWishListAdapter.Holder> {

    private Context context;
    private List<Wish> wishes;
    private String wishListId;

    public TopWishListAdapter(Context context, String wishListId) {
        this.context = context;
        this.wishes = new ArrayList<>();
        this.wishListId = wishListId;
        getWishes();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.top_wish_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.onBind(wishes.get(position));
    }

    @Override
    public int getItemCount() {
        return wishes.size();
    }

    private void getWishes() {
        Firebase firebaseRoot = new Firebase(FirebaseUtil.FIREBASE_URL);
        //todo write nice query to get random wishes
        firebaseRoot.child(FirebaseUtil.WISH_TABLE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    wishes.add(postSnapshot.getValue(Wish.class));
                }
                Collections.shuffle(wishes);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {}
        });
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder {

        @Bind(R.id.image_view) ImageView imageView;
        @Bind(R.id.text_view_title) TextView textViewTitle;
        @Bind(R.id.text_view_comment) TextView textViewComment;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void onBind(Wish wish) {
            if (wish.getPicture() == null) imageView.setImageResource(R.drawable.gift_icon);//TODO: circle image
            else imageView.setImageBitmap(Utilities.decodeThumbnail(wish.getPicture()));
            textViewTitle.setText(wish.getTitle());
            textViewComment.setText(wish.getComment());
        }

        @OnClick(R.id.image_button_add)
        public void onClickEdit() {

            Intent intent = new Intent(context, WishEditActivity.class)
                    .setAction(WishEditActivity.ACTION_TAKE_FROM_TOP)
                    .putExtra(WishListFragment.WISH_LIST_ID, wishListId);

            LocalStorage.getInstance().setWish(wishes.get(getAdapterPosition()));

            context.startActivity(intent);
        }


    }

}