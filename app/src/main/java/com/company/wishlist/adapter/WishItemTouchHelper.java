package com.company.wishlist.adapter;

import android.graphics.Canvas;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.company.wishlist.R;
import com.company.wishlist.interfaces.IWishItemAdapter;

/**
 * Created by vladstarikov on 24.02.16.
 */
public class WishItemTouchHelper extends ItemTouchHelper.Callback {

    private final float alphaCoefficient = 0.33f;

    private IWishItemAdapter wishItemAdapter;
    private View rootView;//need for snackBar

    public WishItemTouchHelper(RecyclerView recyclerView) {
        this.wishItemAdapter = (IWishItemAdapter) recyclerView.getAdapter();
        this.rootView = recyclerView;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.END) | makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.START);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        WishListAdapter.Holder wishHolder = (WishListAdapter.Holder) viewHolder;
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            int margin = viewHolder.itemView.getContext().getResources().getDimensionPixelSize(R.dimen.default_margin_large);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            layoutParams.setMargins(margin, margin, margin, margin);
            if (dX > 0) {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                wishHolder.imageViewAction.setImageResource(R.drawable.ic_favorite_red_600_24dp);
                wishHolder.textViewAction.setText(R.string.action_reserve_text);
            } else {
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                wishHolder.imageViewAction.setImageResource(R.drawable.ic_delete_grey_600_24dp);
                wishHolder.textViewAction.setText(R.string.action_remove_text);
            }
            wishHolder.background.setLayoutParams(layoutParams);

            // Fade out the view as it is swiped out of the parent's bounds
            double size = viewHolder.itemView.getWidth() * alphaCoefficient;
            float alpha = (float) (Math.abs(dX) / size);
            wishHolder.background.setAlpha(alpha);
            wishHolder.cardView.setTranslationX(dX);
            Log.e("onChildDraw", String.valueOf(wishHolder.cardView.getTranslationX()));
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        CardView cardView = ((WishListAdapter.Holder) viewHolder).cardView;
        Log.e("onSwiped", String.valueOf(cardView.getTranslationX()));
        cardView.setTranslationX(0);
        Log.e("onSwiped2", String.valueOf(cardView.getTranslationX()));
        switch (direction) {
            case ItemTouchHelper.END:
                wishItemAdapter.reserveWish(viewHolder.getAdapterPosition());
                break;
            case ItemTouchHelper.START:
                Snackbar.make(rootView, "Wish removed", Snackbar.LENGTH_LONG)
                        .setAction("dismiss", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                wishItemAdapter.restoreWish();
                            }
                        }).show();
                wishItemAdapter.removeWish(viewHolder.getAdapterPosition());
                break;
        }
    }



}
