package com.company.wishlist.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.company.wishlist.R;
import com.company.wishlist.bean.FriendBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by v.odahovskiy on 08.01.2016.
 */
public class FriendListAdapter extends BaseAdapter {
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
    public int getCount() {
        return friends.size();
    }

    @Override
    public Object getItem(int position) {
        return friends.get(position);
    }

    @Override
    public long getItemId(int position) {
        return Long.valueOf(friends.get(position).getId());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        ImageView avatar = (ImageView) convertView.findViewById(R.id.friend_avatar_iv);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.friend_name_tv);

        Glide.with(context)
                .load(friends.get(position).getImageUrl())
                .asBitmap()
                .into(avatar);
        txtTitle.setText(friends.get(position).getName());
        return convertView;
    }
}
