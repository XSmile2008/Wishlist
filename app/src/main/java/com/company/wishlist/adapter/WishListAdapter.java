package com.company.wishlist.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.company.wishlist.R;
import com.company.wishlist.activity.WishEditActivity;
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
    private boolean isOwner = true;
    private int selectedItem = -1;

    public WishListAdapter(Context context, List<Wish> wishes) {
        this.context = context;
        this.wishes = wishes;
    }

    public WishListAdapter(Context context, List<Wish> wishes, boolean isOwner) {
        this(context, wishes);
        this.isOwner = isOwner;
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
        holder.setMode((selectedItem == position) ? Holder.DETAIL_MODE : Holder.NORMAl_MODE);
    }

    @Override
    public int getItemCount() {
        return wishes.size();
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
            Intent intent = new Intent(context, WishEditActivity.class)
                    .putExtra("wish", wishes.get(getAdapterPosition()));
            context.startActivity(intent);
        }

        @OnClick(R.id.image_button_delete)
        public void onClickDelete() {
            Toast.makeText(context, "item " + getAdapterPosition() + " deleted", Toast.LENGTH_SHORT).show();
        }

    }

}
