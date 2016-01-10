package com.company.wishlist.adapter;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.company.wishlist.R;
import com.company.wishlist.model.Wish;

import java.util.List;

/**
 * Created by vladstarikov on 08.01.16.
 */
public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.Holder> {

    Context context;
    List<Wish> wishes;

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
    }

    @Override
    public int getItemCount() {
        return wishes.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textViewTitle;
        TextView textViewComment;
        ImageButton imageButtonOptions;

        public Holder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            textViewTitle = (TextView) itemView.findViewById(R.id.text_view_title);
            textViewComment = (TextView) itemView.findViewById(R.id.text_view_comment);
            imageButtonOptions = (ImageButton) itemView.findViewById(R.id.image_button_options);
            imageButtonOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(context, v);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Toast.makeText(context, getAdapterPosition() + ": " + item, Toast.LENGTH_SHORT).show();
                            switch (item.getItemId()) {
                                case R.id.action_reserve:
                                    break;
                                case R.id.action_edit:
                                    break;
                                case R.id.action_delete:
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.inflate(R.menu.menu_wish_list_item);
                    popup.show();
                }
            });
        }
    }

}
