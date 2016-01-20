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
import com.company.wishlist.bean.EditWishBean;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.model.Wish;
import com.company.wishlist.util.FirebaseUtil;
import com.company.wishlist.util.LocalStorage;
import com.company.wishlist.util.Utilities;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vladstarikov on 08.01.16.
 */
public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.Holder> implements IOnFriendSelectedListener{

    private String LOG_TAG = getClass().getSimpleName();

    private Context context;
    private List<Wish> wishes;
    private boolean isOwner = true;
    private int selectedItem = -1;

    public WishListAdapter(Context context) {
        this.context = context;
        this.wishes = new ArrayList<>();
        getWishes();
    }

    private void getWishes() {
        FirebaseUtil firebaseUtil = new FirebaseUtil(context);
        Firebase firebaseRoot = firebaseUtil.getFirebaseRoot();
        firebaseRoot.child(FirebaseUtil.WISH_TABLE).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevKey) {
                Wish wish = dataSnapshot.getValue(Wish.class);
                wish.setId(dataSnapshot.getKey());
                wishes.add(wish);
                notifyDataSetChanged();
                Log.d("wishes.onChildAdded()", wish.toString());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevKey) {
                Wish wish = dataSnapshot.getValue(Wish.class);
                wish.setId(dataSnapshot.getKey());
                wishes.set(findWishIndexById(wish.getId()), wish);
                Log.d("wishes.onChildChanged()", wish.toString());
                notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                wishes.remove(findWishIndexById(dataSnapshot.getKey()));
                notifyDataSetChanged();
                Log.d("wishes.onChildRemoved()", dataSnapshot.getValue(Wish.class).toString());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevKey) {
                Log.d("wishes.onChildMoved()", dataSnapshot.getValue(Wish.class).toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("wishes.onChanceled()", firebaseError.toString());
            }
        });
    }

    //TODO: will be replaced with HashMap or write sort, etc...
    @Deprecated
    int findWishIndexById(String id) {
        for (int i = 0; i < wishes.size(); i++) {
            if (wishes.get(i).getId().equals(id)) return i;
        }
        return -1;
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

    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public static final int NORMAl_MODE = 0;
        public static final int DETAIL_MODE = 1;
        public static final int EDIT_MODE = 2;

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
            if (!isOwner) {
                imageButtonEdit.setVisibility(View.GONE);
                imageButtonDelete.setVisibility(View.GONE);
            }
        }

        public void setMode(int mode) {
            layoutFooter.setVisibility(mode == DETAIL_MODE || mode == EDIT_MODE ? View.VISIBLE : View.GONE);
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
            Toast.makeText(context, "item " + getAdapterPosition() + " deleted", Toast.LENGTH_SHORT).show();
        }

    }

}
