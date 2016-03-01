package com.company.wishlist.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.company.wishlist.R;

import org.jinstagram.entity.common.Images;

import java.util.List;

/**
 * Created by v.odahovskiy on 20.01.2016.
 */
public class PinterestGridViewAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater layoutInflater;
    private List<String> mItems;

    public PinterestGridViewAdapter(Context context, List<String> imageUrls) {
        mContext = context;
        layoutInflater = LayoutInflater.from(context);
        mItems = imageUrls;
    }

    public void addAll(List<String> items){
        if (null != items && items.size() > 0){
            mItems.clear();
            mItems.addAll(items);
            notifyDataSetChanged();
        }
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

        String item = mItems.get(position);
        Glide.with(mContext)
                .load(item)
                .into(viewHolder.ivIcon);
        return convertView;
    }

    private static class ViewHolder {
        ImageView ivIcon;
    }
}