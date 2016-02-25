package com.company.wishlist.adapter;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import com.company.wishlist.interfaces.IWishItemAdapter;

/**
 * Created by vladstarikov on 24.02.16.
 */
public class WishItemTouchHelper extends ItemTouchHelper.Callback {

    IWishItemAdapter wishItemAdapter;
    View rootView;

    public WishItemTouchHelper(IWishItemAdapter wishItemAdapter, View rootView) {
        this.wishItemAdapter = wishItemAdapter;
        this.rootView = rootView;
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
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        Log.d("onyan", "onyan");
        switch (direction) {
            case ItemTouchHelper.END:
                wishItemAdapter.reserveWish(viewHolder.getAdapterPosition());
                break;
            case ItemTouchHelper.START:
                Snackbar.make(rootView, "nyan", Snackbar.LENGTH_SHORT)
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
