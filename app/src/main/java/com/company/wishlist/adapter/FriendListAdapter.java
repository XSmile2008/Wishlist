package com.company.wishlist.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.company.wishlist.R;
import com.company.wishlist.events.FriendSelectedEvent;
import com.company.wishlist.model.User;
import com.company.wishlist.view.CropCircleTransformation;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by v.odahovskiy on 08.01.2016.
 */
public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.Holder> {

    private Context mContext;
    private List<User> mFriends;

    public FriendListAdapter(Context context, List<User> friends) {
        this.mContext = context;
        setFriends(friends);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend_list, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        Glide.with(mContext)
                .load(mFriends.get(position).getAvatarURL())
                .placeholder(R.drawable.ic_account_circle_80dp)
                .bitmapTransform(new CropCircleTransformation(Glide.get(mContext).getBitmapPool()))
                .into(holder.imageViewAvatar);
        holder.textViewTitle.setText(mFriends.get(position).getDisplayName());
    }

    @Override
    public int getItemCount() {
        return mFriends.size();
    }

    public void setFriends(List<User> friends) {
        if (null == friends) {
            friends = new ArrayList<>();
        }
        this.mFriends = friends;
        Collections.sort(friends);
        notifyDataSetChanged();
    }

    public class Holder extends RecyclerView.ViewHolder {

        @BindView(R.id.friend_avatar_iv)
        ImageView imageViewAvatar;
        @BindView(R.id.friend_name_tv)
        TextView textViewTitle;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user = mFriends.get(getAdapterPosition());
                    EventBus.getDefault().post(new FriendSelectedEvent(user));
                }
            });
        }
    }

}
