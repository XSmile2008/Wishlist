package com.company.wishlist.view;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.wishlist.R;
import com.company.wishlist.util.social.share.SocialSharing;
import com.company.wishlist.util.social.share.impl.FacebookSharing;
import com.company.wishlist.util.social.share.impl.TwitterSharing;
import com.company.wishlist.util.social.twitter.TwitterUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by vladstarikov on 24.03.16.
 */
public class BottomSheetShareDialog extends BottomSheetDialog {

    private SocialSharing mSocialSharing;
    private Menu mMenu;
    private final String mMessage;

    public BottomSheetShareDialog(Context context, String message) {
        super(context);
        setContentView(R.layout.dialog_share);
        this.mMessage = message;
        this.mSocialSharing = new SocialSharing(context);
        this.mMenu = new MenuBuilder(context);
        ((AppCompatActivity) context).getMenuInflater().inflate(R.menu.menu_social_share, mMenu);
        if (!TwitterUtils.isConnected()) {//TODO: enable/disable other share methods, make more elegant solution
            mMenu.removeItem(R.id.action_twitter);
        }

        ShareDialogAdapter adapter = new ShareDialogAdapter();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridAutofitLayoutManager(context, (int) context.getResources().getDimension(R.dimen.image_size_large)));
        recyclerView.setAdapter(adapter);
    }

    public class ShareDialogAdapter extends RecyclerView.Adapter<ShareDialogAdapter.Holder> {

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_share, parent, false));
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            holder.imageViewIcon.setImageDrawable(mMenu.getItem(position).getIcon());
            holder.textViewTitle.setText(mMenu.getItem(position).getTitle());
        }

        @Override
        public int getItemCount() {
            return mMenu.size();
        }

        public class Holder extends RecyclerView.ViewHolder {

            @BindView(R.id.image_view_icon) ImageView imageViewIcon;
            @BindView(R.id.text_view_title) TextView textViewTitle;

            public Holder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (mMenu.getItem(getAdapterPosition()).getItemId()) {
                            case R.id.action_facebook:
                                mSocialSharing.setShareStrategy(new FacebookSharing());
                                break;
                            case R.id.action_twitter:
                                mSocialSharing.setShareStrategy(new TwitterSharing());
                                break;
                        }
                        mSocialSharing.setMessage(new String(mMessage));//TODO: fix this bug
                        mSocialSharing.share();
                        hide();
                    }
                });
            }

        }

    }

}
