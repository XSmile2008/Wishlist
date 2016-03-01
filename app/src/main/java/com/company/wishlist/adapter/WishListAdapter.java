package com.company.wishlist.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.company.wishlist.R;
import com.company.wishlist.activity.WishEditActivity;
import com.company.wishlist.fragment.WishListFragment;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.interfaces.IWishItemAdapter;
import com.company.wishlist.model.Reservation;
import com.company.wishlist.model.Wish;
import com.company.wishlist.model.WishList;
import com.company.wishlist.util.FirebaseUtil;
import com.company.wishlist.util.LocalStorage;
import com.company.wishlist.util.Utilities;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vladstarikov on 08.01.16.
 */
public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.Holder> implements IOnFriendSelectedListener, IWishItemAdapter {

    private String LOG_TAG = getClass().getSimpleName();

    private Context context;
    private View rootView;
    private int mode;//WISH_LIST_MODE or GIFT_LIST_MODE

    private WishEventListener listenersWish;
    private List<Query> queriesWish = new ArrayList<>();

    private List<Wish> wishes;

    private Wish wishBackUp;
    private int wishBackUpPos;//TODO: check it

    /**
     * @param context context that will be used in this adapter
     * @param mode mode of this list. May be WISH_LIST_MODE or GIFT_LIST_MODE
     */
    public WishListAdapter(Context context, View rootView, int mode) {
        this.context = context;
        this.rootView = rootView;
        this.mode = mode;
        this.wishes = new ArrayList<>();
        this.listenersWish = new WishEventListener();
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.wish_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        holder.onBind(wishes.get(position));
    }

    @Override
    public int getItemCount() {
        return wishes.size();
    }

    @Deprecated
    private int findWishIndexById(String id) {
        for (int i = 0; i < wishes.size(); i++) {
            if (wishes.get(i).getId().equals(id)) return i;
        }
        return -1;
    }

    @Override
    public void reserveWish(final int position) {
        if (wishes.get(position).isReserved()) {
            wishes.get(position).unreserve();
            Toast.makeText(context, "item " + position + " unreserved", Toast.LENGTH_SHORT).show();
        } else {
            CalendarDatePickerDialogFragment reservedDateDialog = new CalendarDatePickerDialogFragment();
            reservedDateDialog.setFirstDayOfWeek(Calendar.MONDAY);
            reservedDateDialog.setRetainInstance(true);
            reservedDateDialog.setThemeDark(true);
            reservedDateDialog.setOnDateSetListener(new CalendarDatePickerDialogFragment.OnDateSetListener() {
                @Override
                public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                    wishes.get(position).reserve(FirebaseUtil.getCurrentUser().getId(), dialog.getSelectedDay().getDateInMillis());
                    Toast.makeText(context, "item " + position + " reserved", Toast.LENGTH_SHORT).show();
                }
            });
            reservedDateDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "DATE_PICKER");//TODO: check casting
        }
    }

    @Override
    public void removeWish(int position) {
        Toast.makeText(context, "item " + position + " removed", Toast.LENGTH_SHORT).show();
        wishBackUpPos = position;
        wishBackUp = wishes.get(position);
        wishBackUp.softRemove();
        Snackbar.make(rootView, R.string.message_wish_removed, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        restoreWish();
                    }
                })
                .show();
    }

    @Override
    public void restoreWish() {
        Toast.makeText(context, "restore", Toast.LENGTH_SHORT).show();
        wishes.add(wishBackUpPos, wishBackUp);
        notifyDataSetChanged();
        wishBackUp.softRestore();
    }

    @Override
    public void onFriendSelected(String id) {
        Log.d(LOG_TAG, "onFriendSelected(" + id + ")");
        wishes.clear();
        for (Query query : queriesWish) query.removeEventListener(listenersWish);//remove all unused listeners
        getWishLists(id);
        notifyDataSetChanged();
    }

    /**
     * Query that get all wishLists that addressed to forUser,
     * and depending at wishList type sort it by owner
     * @param forUser - userId
     */
    private void getWishLists(final String forUser) {
        Log.e(LOG_TAG, "GET wishes for user " + forUser);
        WishList.getFirebaseRef()
                .orderByChild("forUser")
                .equalTo(forUser)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(LOG_TAG, "WishList.onChildAdded" + dataSnapshot);
                        for (DataSnapshot wishListDS : dataSnapshot.getChildren()) {
                            WishList wishList = wishListDS.getValue(WishList.class);
                            wishList.setId(wishListDS.getKey());
                            switch (mode) {
                                case WishListFragment.WISH_LIST_MODE:
                                    if (!wishList.getOwner().equals(FirebaseUtil.getCurrentUser().getId()))
                                        getWishes(wishList.getId());
                                    break;
                                case WishListFragment.GIFT_LIST_MODE:
                                    if (wishList.getOwner().equals(FirebaseUtil.getCurrentUser().getId()))
                                        getWishes(wishList.getId());
                                    break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.d("wish_list.onCanceled()", firebaseError.toString());
                    }
                });
    }

    /**
     * Query that getting wishes from wish list that have same wishListId
     * @param wishListId - id of wishList from that we getting wishes
     */
    private void getWishes(final String wishListId) {
        Log.d(LOG_TAG, "punyan =" + wishListId);
        Query query = Wish.getFirebaseRef()
                .orderByChild("wishListId")
                .equalTo(wishListId);
        query.addChildEventListener(listenersWish);
        queriesWish.add(query);
    }

    private class WishEventListener implements ChildEventListener {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String prevKey) {
            Wish wish = dataSnapshot.getValue(Wish.class);
            if (!wish.isRemoved()) {
                wish.setReservation(dataSnapshot.child("reservation").getValue(Reservation.class));
                wish.setId(dataSnapshot.getKey());
                wishes.add(wish);
                notifyDataSetChanged();
            }
            Log.d(LOG_TAG, "Wish.onChildAdded()" + wish.toString());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String prevKey) {
            Wish wish = dataSnapshot.getValue(Wish.class);
            wish.setId(dataSnapshot.getKey());
            int index = findWishIndexById(wish.getId());
            if (!wish.isRemoved()) {
                wish.setReservation(dataSnapshot.child("reservation").getValue(Reservation.class));
                if (index != -1) wishes.set(index, wish);
                else wishes.add(wish);
                notifyItemChanged(index);
            } else if (index != -1) {
                wishes.remove(index);
                notifyDataSetChanged();
            }
            Log.d(LOG_TAG, "Wish.onChildChanged()" + wish.toString());
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            int index = findWishIndexById(dataSnapshot.getKey());
            if (index != -1) {
                wishes.remove(index);
                notifyDataSetChanged();
            }
            Log.d(LOG_TAG, "Wish.onChildRemoved()" + dataSnapshot.getValue(Wish.class).toString());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String prevKey) {
            Log.d(LOG_TAG, "Wish.onChildMoved()" + dataSnapshot.getValue(Wish.class).toString());
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            Log.d(LOG_TAG, "Wish.onCanceled()" + firebaseError.toString());
        }

    }

    public class Holder extends RecyclerView.ViewHolder {

        @Bind(R.id.swipe_layout) SwipeLayout swipeLayout;

        //CardView
        @Bind(R.id.image_view) ImageView imageView;
        @Bind(R.id.text_view_title) TextView textViewTitle;
        @Bind(R.id.text_view_comment) TextView textViewComment;

        //Background
        @Bind(R.id.bottom_view_remove) ViewGroup bottomViewRemove;
        @Bind(R.id.bottom_view_reserve) ViewGroup bottomViewReserve;
        @Bind(R.id.text_view_reserve) TextView textViewReserve;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
            swipeLayout.addDrag(SwipeLayout.DragEdge.Right, bottomViewReserve);
            if (mode == WishListFragment.GIFT_LIST_MODE) swipeLayout.addDrag(SwipeLayout.DragEdge.Left, bottomViewRemove);
            swipeLayout.addSwipeListener(new SimpleSwipeListener() {
                @Override
                public void onOpen(SwipeLayout layout) {
                    if (layout.getDragEdge() == SwipeLayout.DragEdge.Left) removeWish(getAdapterPosition());
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                    float alpha = Math.abs((float) leftOffset / (float) bottomViewReserve.getWidth());//TODO:
                    layout.getCurrentBottomView().setAlpha(alpha);
                }

                @Override
                public void onStartOpen(SwipeLayout layout) {
                    if (layout.getDragEdge() == SwipeLayout.DragEdge.Right)
                        textViewReserve.setText(wishes.get(getAdapterPosition()).isReserved() ? R.string.action_unreserve : R.string.action_reserve);
                }
            });
        }

        public void onBind(Wish wish) {
            if (wish.getPicture() == null) imageView.setImageResource(R.drawable.gift_icon);//TODO: circle image
            else imageView.setImageBitmap(Utilities.decodeThumbnail(wish.getPicture()));
            textViewTitle.setText(wish.getTitle());
            textViewComment.setText(wish.getComment());
        }

        @OnClick({R.id.card_view, R.id.bottom_view_reserve})
        public void onClick(View v) {
            swipeLayout.close();
            switch (v.getId()) {
                case R.id.card_view:
                    Toast.makeText(context, "item " + getAdapterPosition() + " edit", Toast.LENGTH_SHORT).show();
                    LocalStorage.getInstance().setWish(wishes.get(getAdapterPosition()));
                    Intent intent = new Intent(context, WishEditActivity.class);
                    intent.setAction(WishEditActivity.ACTION_EDIT);
                    context.startActivity(intent);
                    break;
                case R.id.bottom_view_reserve:
                    reserveWish(getAdapterPosition());
                    break;
            }
        }

    }

}
