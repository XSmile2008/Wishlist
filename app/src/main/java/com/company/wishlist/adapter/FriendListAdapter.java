package com.company.wishlist.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.company.wishlist.R;
import com.company.wishlist.bean.FriendBean;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by v.odahovskiy on 08.01.2016.
 */
public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.Holder> {

    private Context context;
    private List<FriendBean> friends;

    public FriendListAdapter(Context context, List<FriendBean> friends) {
        this.context = context;
        this.friends = (null != friends) ? friends : new ArrayList<FriendBean>();
    }

    private boolean isUnique(FriendBean friendBean){
        for (FriendBean friend : friends){
            if (friend.getId().equals(friendBean.getId())){
                return true;
            }
        }
        return false;
    }

    public void addFriend(FriendBean friend) {
        if (null != friend && isUnique(friend)) {
            friends.add(friend);
            notifyDataSetChanged();
        }
    }

    public void addAll(List<FriendBean> friends) {
        this.friends.clear();
        this.friends.addAll(friends);
        notifyDataSetChanged();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Glide.with(context)
                .load(friends.get(position).getImageUrl())
                .asBitmap()
                .into(holder.imageViewAvatar);
        holder.textViewTitle.setText(friends.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public class Holder extends RecyclerView.ViewHolder {

        ImageView imageViewAvatar;
        TextView textViewTitle;

        public Holder(View itemView) {
            super(itemView);
            imageViewAvatar = (ImageView) itemView.findViewById(R.id.friend_avatar_iv);
            textViewTitle = (TextView) itemView.findViewById(R.id.friend_name_tv);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    long id = Long.parseLong(friends.get(getAdapterPosition()).getId());
                    ((IOnFriendSelectedListener) context).onFriendSelected(id);
                }
            });
        }
    }
}
