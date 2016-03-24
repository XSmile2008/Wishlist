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

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by vladstarikov on 24.03.16.
 */
public class BottomSheetShareDialog extends BottomSheetDialog {

    SocialSharing socialSharing;
    Menu menu;
    final String message;

    public BottomSheetShareDialog(Context context, String message) {
        super(context);
        setContentView(R.layout.dialog_share);
        this.message = message;
        this.socialSharing = new SocialSharing(context);
        this.menu = new MenuBuilder(context);
        ((AppCompatActivity) context).getMenuInflater().inflate(R.menu.menu_social_share, menu);

        ShareDialogAdapter adapter = new ShareDialogAdapter();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridAutofitLayoutManager(context, (int) context.getResources().getDimension(R.dimen.image_size_large)));
        recyclerView.setAdapter(adapter);
    }

    public class ShareDialogAdapter extends RecyclerView.Adapter<ShareDialogAdapter.Holder> {

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_share_item, parent, false));
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            holder.imageViewIcon.setImageDrawable(menu.getItem(position).getIcon());
            holder.textViewTitle.setText(menu.getItem(position).getTitle());
        }

        @Override
        public int getItemCount() {
            return menu.size();
        }

        public class Holder extends RecyclerView.ViewHolder {

            @Bind(R.id.image_view_icon) ImageView imageViewIcon;
            @Bind(R.id.text_view_title) TextView textViewTitle;

            public Holder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (menu.getItem(getAdapterPosition()).getItemId()) {
                            case R.id.action_facebook:
                                socialSharing.setShareStrategy(new FacebookSharing());
                                break;
                            case R.id.action_twitter:
                                socialSharing.setShareStrategy(new TwitterSharing());
                                break;
                        }
                        socialSharing.setMessage(new String(message));//TODO: fix this bug
                        socialSharing.share();
                        hide();
                    }
                });
            }

        }

    }

}
