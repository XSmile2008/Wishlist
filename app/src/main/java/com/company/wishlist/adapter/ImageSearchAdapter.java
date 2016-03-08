package com.company.wishlist.adapter;

import android.app.ProgressDialog;
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

    public ImageSearchAdapter(Context context, IOnPictureSelectedListener listener, String query) {
        this.mContext = context;
        this.mListener = listener;
        loadPictures(query);
    }

    public ImageSearchAdapter(Context context, IOnPictureSelectedListener listener, List<String> urls) {
        this.mContext = context;
        this.mListener = listener;
        this.mItems = urls;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.image_search_item, parent, false));
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

    public void loadPictures(String query) {
        final ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle(R.string.app_name);
        progressDialog.setMessage(mContext.getResources().getString(R.string.message_loading_pints_dialog));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
        PinterestUtil.getImagesAsLinks(new PinterestUtil.IOnDoneListener() {
            @Override
            public void onDone(final List<String> urls) {
                if (null != urls && urls.size() > 0) {
                    new Handler(mContext.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Collections.shuffle(urls);
                            mItems = urls;
                            notifyDataSetChanged();
                            progressDialog.dismiss();
                        }
                    });
                }
            }
        }, query);
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