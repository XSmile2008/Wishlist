package com.company.wishlist.adapter;

import android.content.Context;
import android.content.DialogInterface;
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
import com.company.wishlist.model.WishList;
import com.company.wishlist.util.DialogUtil;
import com.company.wishlist.util.FirebaseUtil;
import com.company.wishlist.util.LocalStorage;
import com.company.wishlist.util.Utilities;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vladstarikov on 08.01.16.
 */
public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.Holder> implements IOnFriendSelectedListener {

    private String LOG_TAG = getClass().getSimpleName();

    private Context context;
    private List<Wish> wishes;
    private int selectedItem = -1;
    private int mode;
    private FirebaseUtil firebaseUtil;
    private WishEventListener listenersWish;
    private List<Query> queriesWish = new ArrayList<>();


    public WishListAdapter(Context context, int mode) {
        this.context = context;
        this.mode = mode;
        this.wishes = new ArrayList<>();
        this.firebaseUtil = new FirebaseUtil(context);
        this.listenersWish = new WishEventListener();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.wish_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        if (wishes.get(position).getPicture() == null) {
            holder.imageView.setImageResource(R.drawable.gift_icon);
        } else {
            holder.imageView.setImageBitmap(Utilities.decodeThumbnail(wishes.get(position).getPicture()));
        }
        holder.textViewTitle.setText(wishes.get(position).getTitle());
        holder.textViewComment.setText(wishes.get(position).getComment());
        holder.setMode((selectedItem == position) ? Holder.DETAIL_MODE : Holder.NORMAl_MODE);
    }

    @Override
    public int getItemCount() {
        return wishes.size();
    }

    @Override
    public void onFriendSelected(String id) {
        Log.d(LOG_TAG, "onFriendSelected(" + id + ")");
        wishes.clear();
        for (Query query : queriesWish) query.removeEventListener(listenersWish);//remove all unused listeners
        getWishLists(id);
        notifyDataSetChanged();
    }

    private void getWishLists(final String forUser) {
        Log.e(LOG_TAG, "GET wishes for user " + forUser);
        firebaseUtil.getFirebaseRoot()
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
                                    if (!wishList.getOwner().equals(firebaseUtil.getCurrentUser().getId()))
                                        getWishes(wishList.getId());
                                    break;
                                case WishListFragment.GIFT_LIST_MODE:
                                    if (wishList.getOwner().equals(firebaseUtil.getCurrentUser().getId()))
                                        getWishes(wishList.getId());
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.d("wish_list.onChanceled()", firebaseError.toString());
                    }
                });
    }

    private void getWishes(final String wishListId) {
        Log.d(LOG_TAG, "punyan =" + wishListId);
        Query query = firebaseUtil.getFirebaseRoot()
                .child(FirebaseUtil.WISH_TABLE)
                .orderByChild("wishListId")
                .equalTo(wishListId);
        query.addChildEventListener(listenersWish);
        queriesWish.add(query);
    }

    @Deprecated
    int findWishIndexById(String id) {
        for (int i = 0; i < wishes.size(); i++) {
            if (wishes.get(i).getId().equals(id))
                return i;
        }
        return -1;
    }

    private class WishEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String prevKey) {
            Wish wish = dataSnapshot.getValue(Wish.class);
            wish.setId(dataSnapshot.getKey());
            wishes.add(wish);
            notifyDataSetChanged();
            Log.d(LOG_TAG, "Wish.onChildAdded()" + wish.toString());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String prevKey) {
            Wish wish = dataSnapshot.getValue(Wish.class);
            wish.setId(dataSnapshot.getKey());
            wishes.set(findWishIndexById(wish.getId()), wish);
            Log.d(LOG_TAG, "Wish.onChildChanged()" + wish.toString());
            notifyDataSetChanged();
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Wish wish = dataSnapshot.getValue(Wish.class);
            wish.setId(dataSnapshot.getKey());
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

        public static final int NORMAl_MODE = 0;
        public static final int DETAIL_MODE = 1;

        //Header
        @Bind(R.id.layout_header) ViewGroup layoutHeader;
        @Bind(R.id.image_view) ImageView imageView;
        @Bind(R.id.text_view_title) TextView textViewTitle;
        @Bind(R.id.text_view_comment) TextView textViewComment;

        //Footer
        @Bind(R.id.layout_footer) ViewGroup layoutFooter;
        @Bind(R.id.image_button_close) ImageButton imageButtonClose;
        @Bind(R.id.image_button_reserve) ImageButton imageButtonReserve;
        @Bind(R.id.image_button_edit) ImageButton imageButtonEdit;
        @Bind(R.id.image_button_delete) ImageButton imageButtonDelete;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
            if (mode == WishListFragment.WISH_LIST_MODE) {
                imageButtonEdit.setVisibility(View.GONE);
                imageButtonDelete.setVisibility(View.GONE);
            }
        }

        public void setMode(int mode) {
            layoutFooter.setVisibility(mode == DETAIL_MODE ? View.VISIBLE : View.GONE);
            textViewComment.setSingleLine(mode == NORMAl_MODE);
        }

        @Override
        public void onClick(View v) {
            int selectedItemOld = selectedItem;
            selectedItem = getAdapterPosition();
            if (selectedItem != selectedItemOld) {
                notifyItemChanged(selectedItemOld);
                notifyItemChanged(getAdapterPosition());
            }
        }

        @OnClick(R.id.image_button_close)
        public void onClickClose() {
            selectedItem = -1;
            notifyItemChanged(getAdapterPosition());
        }

        @OnClick(R.id.image_button_reserve)
        public void onClickReserve() {
            Toast.makeText(context, "item " + getAdapterPosition() + " reserved", Toast.LENGTH_SHORT).show();
        }

        @OnClick(R.id.image_button_edit)
        public void onClickEdit() {
            Toast.makeText(context, "item " + getAdapterPosition() + " edit", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context, WishEditActivity.class);
            LocalStorage.getInstance().setWish(wishes.get(getAdapterPosition()));
            intent.setAction(WishEditActivity.ACTION_EDIT);
            context.startActivity(intent);
        }

        @OnClick(R.id.image_button_delete)
        public void onClickDelete() {
            DialogUtil.alertShow(context.getString(R.string.app_name), context.getString(R.string.remove_wish_dialog_text), context, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    firebaseUtil.getFirebaseRoot().child(FirebaseUtil.WISH_TABLE).child(wishes.get(getAdapterPosition()).getId()).removeValue();
                    //notifyDataSetChanged();
                }
            });
            Toast.makeText(context, "item " + getAdapterPosition() + " deleted", Toast.LENGTH_SHORT).show();
        }

    }

}
