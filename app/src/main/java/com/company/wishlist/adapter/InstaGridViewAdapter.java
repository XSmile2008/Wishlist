package com.company.wishlist.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.company.wishlist.R;

import org.jinstagram.entity.common.Images;

import java.util.List;

/**
 * Created by v.odahovskiy on 20.01.2016.
 */
public class InstaGridViewAdapter extends BaseAdapter {
    private Context mContext;
    private List<Images> mItems;

    public InstaGridViewAdapter(Context context, List<Images> items) {
        mContext = context;
        mItems = items;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.insta_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Images item = mItems.get(position);
        Glide.with(mContext)
                .load(item.getThumbnail().getImageUrl())
                .into(viewHolder.ivIcon);
        return convertView;
    }

    private static class ViewHolder {
        ImageView ivIcon;
    }
}