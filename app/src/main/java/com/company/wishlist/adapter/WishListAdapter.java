package com.company.wishlist.adapter;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.company.wishlist.R;
import com.company.wishlist.model.Wish;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vladstarikov on 08.01.16.
 */
public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.Holder> {

    private Context context;
    private List<Wish> wishes;

    private int selectedItem = -1;

    public WishListAdapter(Context context, List<Wish> wishes) {
        this.context = context;
        this.wishes = wishes;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.wish_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.imageView.setImageResource(R.drawable.gift_icon);
        holder.textViewTitle.setText(wishes.get(position).getTitle());
        holder.textViewComment.setText(wishes.get(position).getComment());

        if (selectedItem == position) {
            holder.setMode(holder.DETAIL);
        } else {
            holder.setMode(holder.NORMAl);
        }
    }

    @Override
    public int getItemCount() {
        return wishes.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        public final int NORMAl = 0;
        public final int DETAIL = 1;
        public final int EDIT = 2;

        @Bind(R.id.image_view) ImageView imageView;
        @Bind(R.id.text_view_title) TextView textViewTitle;
        @Bind(R.id.text_view_comment) TextView textViewComment;
        @Bind(R.id.layout_normal) RelativeLayout layout_normal;
        @Bind(R.id.layout_detail) LinearLayout layout_detail;
        @Bind(R.id.layout_edit) LinearLayout layout_edit;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selectedItemOld = selectedItem;
                    selectedItem = getAdapterPosition();
                    if (selectedItem == selectedItemOld) selectedItem = -1;
                    else notifyItemChanged(selectedItemOld);
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }

        public void setMode(int mode) {
            layout_normal.setVisibility(mode == NORMAl || mode == DETAIL ? View.VISIBLE : View.GONE);
            layout_detail.setVisibility(mode == DETAIL ? View.VISIBLE : View.GONE);
            layout_edit.setVisibility(mode == EDIT ? View.VISIBLE : View.GONE);
        }
    }

}
