package com.company.wishlist.adapter;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.company.wishlist.R;
import com.company.wishlist.util.social.pinterest.PinterestUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by v.odahovskiy on 20.01.2016.
 */
public class ImageSearchAdapter extends RecyclerView.Adapter<ImageSearchAdapter.Holder> {

    private Context mContext;
    private IOnPictureSelectedListener mListener;
    private List<String> mItems = new ArrayList<>();

    public ImageSearchAdapter(Context context, IOnPictureSelectedListener listener) {
        this.mContext = context;
        this.mListener = listener;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_search, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Glide.with(mContext)
                .load(mItems.get(position))
                .centerCrop()
                .into(holder.ivIcon);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public List<String> getItems() {
        return mItems;
    }

    public void setItems(List<String> items) {
        this.mItems = items;
    }

    public class Holder extends RecyclerView.ViewHolder {

        @Bind(R.id.ivIcon) ImageView ivIcon;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.ivIcon)
        public void onClick() {
            mListener.onPictureSelected(PinterestUtil.getOriginal(mItems.get(getAdapterPosition())));
        }

    }

    public interface IOnPictureSelectedListener {

        void onPictureSelected(String url);

    }

}