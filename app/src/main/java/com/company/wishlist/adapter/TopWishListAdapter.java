package com.company.wishlist.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.company.wishlist.R;
import com.company.wishlist.activity.TopWishActivity;
import com.company.wishlist.activity.WishEditActivity;
import com.company.wishlist.fragment.WishListFragment;
import com.company.wishlist.model.Wish;
import com.company.wishlist.util.CloudinaryUtil;
import com.company.wishlist.util.CropCircleTransformation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class TopWishListAdapter extends RecyclerView.Adapter<TopWishListAdapter.Holder> {

    private Context context;
    private Set<Wish> wishes;
    private String wishListId;

    public TopWishListAdapter(Context context, String wishListId) {
        this.context = context;
        this.wishes = new HashSet<>();
        this.wishListId = wishListId;
    }

    private Wish getByIndex(int index) {
        return (Wish) wishes.toArray()[index];//TODO: optimize this
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.top_wish_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.onBind(getByIndex(position));
    }

    @Override
    public int getItemCount() {
        return wishes.size();
    }

    public void addAll(List<Wish> wishes) {
        this.wishes.clear();
        this.wishes.addAll(wishes);
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
            if (wish.getPicture() == null) {
                imageView.setImageResource(R.drawable.gift_icon);//TODO: default circle image
            } else {
                Glide.with(context)
                        .load(CloudinaryUtil.getInstance().url().generate(wish.getPicture()))
                        .bitmapTransform(new CropCircleTransformation(Glide.get(context).getBitmapPool()))
                        .into(imageView);
            }
            textViewTitle.setText(wish.getTitle());
            textViewComment.setText(wish.getComment());
        }

        @OnClick(R.id.layout_header)
        public void onClick() {
            Intent intent = new Intent(context, WishEditActivity.class)
                    .setAction(WishEditActivity.ACTION_TAKE_FROM_TOP)
                    .putExtra(WishListFragment.WISH_LIST_ID, wishListId)
                    .putExtra(Wish.class.getSimpleName(), getByIndex(getAdapterPosition()))
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(intent);
            ((TopWishActivity)context).finish();
        }

    }

}