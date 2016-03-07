package com.company.wishlist.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import com.bumptech.glide.Glide;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.company.wishlist.R;
import com.company.wishlist.activity.WishEditActivity;
import com.company.wishlist.fragment.WishListFragment;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.interfaces.IWishItemAdapter;
import com.company.wishlist.model.Reservation;
import com.company.wishlist.model.Wish;
import com.company.wishlist.model.WishList;
import com.company.wishlist.util.CloudinaryUtil;
import com.company.wishlist.util.CropCircleTransformation;
import com.company.wishlist.util.AuthUtils;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vladstarikov on 08.01.16.
 */
public class WishListAdapter extends RecyclerView.Adapter<WishListAdapter.Holder> implements IOnFriendSelectedListener, IWishItemAdapter {

    private static String LOG_TAG = WishListAdapter.class.getSimpleName();
    private static int NOT_FIND = -1;//TODO:

    private Context context;
    private View rootView;
    private int mode;//WISH_LIST_MODE or GIFT_LIST_MODE

    private WishEventListener listenersWish = new WishEventListener();
    private List<Query> queriesWish = new ArrayList<>();

    private Map<String, WishList> wishLists = new HashMap<>();
    private List<Wish> wishes = new ArrayList<>();
    private Wish wishBackUp;
    private String friendId;

    private SwipeLayout swipedItem;

    /**
     * @param context context that will be used in this adapter
     * @param mode mode of this list. May be WISH_LIST_MODE or GIFT_LIST_MODE
     */
    public WishListAdapter(Context context, View rootView, int mode) {
        this.context = context;
        this.rootView = rootView;
        this.mode = mode;
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

    class WishComparator implements java.util.Comparator<Wish> {

        public int graduateByReservation(Wish wish) {
            if (wish.getReservation() == null) return 1;
            else if (wish.getReservation().getByUser().equals(AuthUtils.getCurrentUser().getId())) return 2;
            else return 0;
        }

        @Override
        public int compare(Wish left, Wish right) {
            int gradLeft = graduateByReservation(left);
            int gradRight = graduateByReservation(right);
            if (gradLeft < gradRight) return -1;
            else if (gradLeft > gradRight) return 1;
            else return right.getTitle().toLowerCase().compareTo(left.getTitle().toLowerCase());//TODO: may be use Collator
        }

    }

    private int findPositionForWish(Wish wish) {
        WishComparator comparator = new WishComparator();
        for (int i = 0; i < wishes.size(); i++) {
            if (comparator.compare(wish, wishes.get(i)) >= 0) return i;
        }
        return wishes.size();
    }

    private int findWishIndexById(String id) {
        for (int i = 0; i < wishes.size(); i++) {
            if (wishes.get(i).getId().equals(id)) return i;
        }
        return NOT_FIND;
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
                    wishes.get(position).reserve(AuthUtils.getCurrentUser().getId(), dialog.getSelectedDay().getDateInMillis());
                    Toast.makeText(context, "item " + position + " reserved", Toast.LENGTH_SHORT).show();
                }
            });
            reservedDateDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "DATE_PICKER");//TODO: check casting
        }
    }

    @Override
    public void removeWish(int position) {
        wishBackUp = wishes.get(position);
        wishBackUp.softRemove();
        Snackbar.make(rootView, context.getString(R.string.message_wish_removed, position), Snackbar.LENGTH_LONG)
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
        notifyDataSetChanged();
        wishBackUp.softRestore();
    }

    @Override
    public void onFriendSelected(String id) {
        Log.d(LOG_TAG, "onFriendSelected(" + id + ")");
        this.friendId = id;
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
                                    if (!wishList.getOwner().equals(AuthUtils.getCurrentUser().getId())) {
                                        wishLists.put(wishList.getId(), wishList);
                                        getWishes(wishList.getId());
                                    }
                                    break;
                                case WishListFragment.GIFT_LIST_MODE:
                                    if (wishList.getOwner().equals(AuthUtils.getCurrentUser().getId())) {
                                        wishLists.put(wishList.getId(), wishList);
                                        getWishes(wishList.getId());
                                    }
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
            wish.setId(dataSnapshot.getKey());
            wish.setReservation(dataSnapshot.child("reservation").getValue(Reservation.class));
            if (!wish.isRemoved() &&
                    (wishLists.get(wish.getWishListId()).getOwner().equals(friendId) ||
                            wish.isReserved() ||
                            mode == WishListFragment.GIFT_LIST_MODE)) {
                wishes.add(findPositionForWish(wish), wish);
                notifyDataSetChanged();
            }
            Log.d(LOG_TAG, "Wish.onChildAdded()" + wish.toString());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String prevKey) {
            Wish wish = dataSnapshot.getValue(Wish.class);
            wish.setId(dataSnapshot.getKey());
            int index = findWishIndexById(wish.getId());
            if (!wish.isRemoved() &&
                    (wishLists.get(wish.getWishListId()).getOwner().equals(friendId) ||
                            wish.isReserved() ||
                            mode == WishListFragment.GIFT_LIST_MODE)) {
                wish.setReservation(dataSnapshot.child("reservation").getValue(Reservation.class));
                if (index != NOT_FIND) {//change
                    wishes.remove(index);
                    wishes.add(findPositionForWish(wish), wish);
                } else {//soft-restore
                    wishes.add(findPositionForWish(wish), wish);
                }
                notifyDataSetChanged();
            } else if (index != NOT_FIND) {//soft-remove
                wishes.remove(index);
                notifyDataSetChanged();
            }
            Log.d(LOG_TAG, "Wish.onChildChanged()" + wish.toString());
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            int index = findWishIndexById(dataSnapshot.getKey());
            if (index != NOT_FIND) {
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
        @Bind(R.id.card_view) CardView cardView;
        @Bind(R.id.image_view) ImageView imageView;
        @Bind(R.id.text_view_title) TextView textViewTitle;
        @Bind(R.id.text_view_comment) TextView textViewComment;
        @Bind(R.id.text_view_status) TextView textViewStatus;

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
                    Log.d("swipe", "onOpen");
                    if (layout.getDragEdge() == SwipeLayout.DragEdge.Left) {
                        removeWish(getAdapterPosition());
                    } else {
//                        swipeLayout.setSwipeEnabled(false);//TODO
//                        swipeLayout.setOnTouchListener(new View.OnTouchListener() {
//                            @Override
//                            public boolean onTouch(View v, MotionEvent event) {
//                                if (event.getAction() == MotionEvent.ACTION_UP) {
//                                    swipeLayout.setSwipeEnabled(true);
//                                    swipeLayout.setOnTouchListener(null);
//                                    Log.d("swipe", "unlocked");
//                                }
//                                return true;
//                            }
//                        });
                    }
                }

                @Override
                public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {
                    //Log.d("swipe", "onUpdate");
                    float alpha = Math.abs((float) leftOffset / (float) bottomViewReserve.getWidth());//TODO:
                    layout.getCurrentBottomView().setAlpha(alpha);
                }

                @Override
                public void onStartOpen(SwipeLayout layout) {
                    Log.e("swipe", "onStartOpen");
                    if (swipedItem != null && !swipeLayout.equals(swipedItem)) swipedItem.close();
                    swipedItem = swipeLayout;
                    if (layout.getDragEdge() == SwipeLayout.DragEdge.Right)
                        textViewReserve.setText(wishes.get(getAdapterPosition()).isReserved() ? R.string.action_unreserve : R.string.action_reserve);
                }

                @Override
                public void onClose(SwipeLayout layout) {
                    Log.d("swipe", "onClose");
                    if (swipeLayout.equals(swipedItem)) swipedItem = null;
                }

            });
        }

        public void onBind(Wish wish) {
            Log.e(LOG_TAG, ".Holder.OnBind()");
            if (wish.getPicture() == null) {
                imageView.setImageResource(R.drawable.gift_icon);//TODO: default circle image
            } else {
                Log.d(LOG_TAG, ".Holder.LoadImage: " + wish.getPicture());
                Glide.with(context)
                        .load(CloudinaryUtil.getInstance().url().generate(wish.getPicture()))
                        .bitmapTransform(new CropCircleTransformation(Glide.get(context).getBitmapPool()))
                        .into(imageView);
            }
            textViewTitle.setText(wish.getTitle());
            textViewComment.setText(wish.getComment());

            if (wish.isReserved()) {
                if (wish.getReservation().getByUser().equals(AuthUtils.getCurrentUser().getId())) {
                    textViewStatus.setText("Reserved by me");//TODO:
                    swipeLayout.setRightSwipeEnabled(true);
                } else {
                    // by another user
                    textViewStatus.setText("Reserved");//TODO: purpose right only Reserved, cause we don't know who present the gift, it should be secret for all
                    swipeLayout.setRightSwipeEnabled(false);
                }
                textViewStatus.setTextColor(Color.RED);
            } else {
                textViewStatus.setText("");
                swipeLayout.setRightSwipeEnabled(true);
            }
        }

        @OnClick({R.id.card_view, R.id.bottom_view_reserve})
        public void onClick(View v) {
            if (swipedItem != null) swipedItem.close();
            swipedItem = null;
            switch (v.getId()) {
                case R.id.card_view:
                    Intent intent = new Intent(context, WishEditActivity.class)
                            .setAction(WishEditActivity.ACTION_EDIT)
                            .putExtra("Wish", wishes.get(getAdapterPosition()));
                    context.startActivity(intent);
                    break;
                case R.id.bottom_view_reserve:
                    reserveWish(getAdapterPosition());
                    break;
            }
        }

    }

}
