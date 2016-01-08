package com.company.wishlist.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.wishlist.R;
import com.company.wishlist.model.Wish;

import java.util.List;

/**
 * Created by vladstarikov on 08.01.16.
 */
public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.Holder> {

    List<Wish> wishes;

    public WishListAdapter(List<Wish> wishes) {
        this.wishes = wishes;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.wish_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.textViewTitle.setText(wishes.get(position).getTitle());
        holder.textViewComment.setText(wishes.get(position).getComment());
    }

    @Override
    public int getItemCount() {
        return wishes.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textViewTitle;
        TextView textViewComment;

        public Holder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            textViewTitle = (TextView) itemView.findViewById(R.id.text_view_title);
            textViewComment = (TextView) itemView.findViewById(R.id.text_view_comment);
        }
    }
}
