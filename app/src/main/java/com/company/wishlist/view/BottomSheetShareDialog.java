package com.company.wishlist.view;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.company.wishlist.R;
import com.company.wishlist.util.social.share.ShareStrategy;

import java.util.List;

import butterknife.Bind;

/**
 * Created by vladstarikov on 24.03.16.
 */
public class BottomSheetShareDialog extends BottomSheetDialog {

    public BottomSheetShareDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_share);
        ShareDialogAdapter adapter = new ShareDialogAdapter(null);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
    }

    protected BottomSheetShareDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    public class ShareDialogAdapter extends RecyclerView.Adapter<ShareDialogAdapter.Holder> {

        List<String> shareStrategies;

        public ShareDialogAdapter(List<String> shareStrategies) {
            this.shareStrategies = shareStrategies;
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_share_item, parent, false));
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
//            holder.imageViewIcon.setImageResource(R.drawable.facebook);
        }

        @Override
        public int getItemCount() {
            return 0;
        }

        public class Holder extends RecyclerView.ViewHolder {

            @Bind(R.id.image_view_icon) ImageView imageViewIcon;
            @Bind(R.id.text_view_title) TextView textViewTitle;

            public Holder(View itemView) {
                super(itemView);
            }

        }

    }

}
