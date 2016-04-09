package com.company.wishlist.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.wishlist.R;
import com.company.wishlist.activity.TopWishActivity;
import com.company.wishlist.activity.WishEditActivity;
import com.company.wishlist.model.Wish;
import com.company.wishlist.model.WishList;
import com.company.wishlist.util.CloudinaryUtil;
import com.company.wishlist.component.IndexedHashSet;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TopWishAdapter extends RecyclerView.Adapter<TopWishAdapter.Holder> {

    private Context mContext;
    private IndexedHashSet<Wish> mWishes;
    private WishList mWishList;

    public TopWishAdapter(Context context, WishList wishList) {
        this.mContext = context;
        this.mWishes = new IndexedHashSet<>();
        this.mWishList = wishList;
    }

    private Wish getByIndex(int index) {
        return mWishes.get(index);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_top_wish_list, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.onBind(getByIndex(position));
    }

    @Override
    public int getItemCount() {
        return mWishes.size();
    }

    public void addAll(List<Wish> wishes) {
        this.mWishes.clear();
        this.mWishes.addAll(wishes);
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
            CloudinaryUtil.loadThumb(mContext, imageView, wish.getPicture(), R.drawable.gift_icon, true);
            textViewTitle.setText(wish.getTitle());
            textViewComment.setText(wish.getComment());
        }

        @OnClick(R.id.layout_header)
        public void onClick() {
            Intent intent = new Intent(mContext, WishEditActivity.class)
                    .setAction(WishEditActivity.ACTION_TAKE_FROM_TOP)
                    .putExtra(WishList.class.getSimpleName(), mWishList)
                    .putExtra(Wish.class.getSimpleName(), getByIndex(getAdapterPosition()))
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mContext.startActivity(intent);
            ((TopWishActivity) mContext).finish();
        }

    }

}