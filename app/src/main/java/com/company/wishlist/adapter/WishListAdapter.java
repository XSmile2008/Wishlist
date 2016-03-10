package com.company.wishlist.adapter;

import android.content.Context;
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

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.company.wishlist.R;
import com.company.wishlist.fragment.WishListFragment;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.model.Reservation;
import com.company.wishlist.model.Wish;
import com.company.wishlist.model.WishList;
import com.company.wishlist.util.AuthUtils;
import com.company.wishlist.util.CloudinaryUtil;
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
public class WishListAdapter extends SectionedRecyclerViewAdapter<WishListAdapter.Holder> implements IOnFriendSelectedListener {

    private static String LOG_TAG = WishListAdapter.class.getSimpleName();
    private static int NOT_FIND = -1;//TODO:

    private Context context;
    private View rootView;
    private int mode;//WISH_LIST_MODE or GIFT_LIST_MODE

    private WishEventListener listenersWish = new WishEventListener();
    private List<Query> queriesWish = new ArrayList<>();

    private Map<String, WishList> wishLists = new HashMap<>();
    private Wish wishBackUp;
    private String friendId;

    private SwipeLayout swipedItem;

    private Sections sections = new Sections();

    /**
     * @param context context that will be used in this adapter
     * @param mode mode of this list. May be WISH_LIST_MODE or GIFT_LIST_MODE
     */
    public WishListAdapter(Context context, View rootView, int mode) {
        this.context = context;
        this.rootView = rootView;
        this.mode = mode;
        sections.add(new Section("Reserved by me"));
        sections.add(new Section("Not reserved"));
        sections.add(new Section("Reserved by another users"));
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.section, parent, false));
            case VIEW_TYPE_ITEM:
                return new HolderWish(LayoutInflater.from(parent.getContext()).inflate(R.layout.wish_list_item, parent, false));
        }
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(Holder holder, int section) {
        ((TextView) holder.itemView).setText(sections.get(section).getTitle());
    }

    @Override
    public void onBindViewHolder(Holder holder, int section, int relativePosition, int absolutePosition) {
        ((HolderWish) holder).onBind(section, relativePosition);
    }

    @Override
    public int getSectionCount() {
        return sections.size();
    }

    @Override
    public int getItemCount(int section) {
        return sections.get(section).size();
    }

    /*vvvvvvvvvvvvvvvvvvvvvvv
     * IWishItemAdapter start
     */

    public void reserveWish(int section, int relativePosition) {
        final Wish wish = sections.get(section).get(relativePosition);
        if (wish.isReserved()) {
            wish.unreserve();
            Toast.makeText(context, "wish " + wish.getTitle() + " unreserved", Toast.LENGTH_SHORT).show();
        } else {
            CalendarDatePickerDialogFragment reservedDateDialog = new CalendarDatePickerDialogFragment();
            reservedDateDialog.setFirstDayOfWeek(Calendar.MONDAY);
            reservedDateDialog.setRetainInstance(true);
            reservedDateDialog.setThemeDark(true);
            reservedDateDialog.setOnDateSetListener(new CalendarDatePickerDialogFragment.OnDateSetListener() {
                @Override
                public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                    wish.reserve(AuthUtils.getCurrentUser().getId(), dialog.getSelectedDay().getDateInMillis());
                    Toast.makeText(context, "item " + wish.getTitle() + " reserved", Toast.LENGTH_SHORT).show();
                }
            });
            reservedDateDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "DATE_PICKER");//TODO: check casting
        }
    }

    public void removeWish(int section, int relativePosition) {
        wishBackUp = sections.get(section).get(relativePosition);
        wishBackUp.softRemove();
        Snackbar.make(rootView, context.getString(R.string.message_wish_removed, wishBackUp.getTitle()), Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        restoreWish();
                    }
                })
                .show();
    }

    public void restoreWish() {
        Toast.makeText(context, "restore", Toast.LENGTH_SHORT).show();
        notifyDataSetChanged();
        wishBackUp.softRestore();
    }

    /*
     * IWishItemAdapter end
     *^^^^^^^^^^^^^^^^^^^^^*/

    /*vvvvvvvvvvvvvvvvvvvvvvv
     * Firebase query start
     */

    @Override
    public void onFriendSelected(String id) {
        Log.d(LOG_TAG, "onFriendSelected(" + id + ")");
        this.friendId = id;
        sections.clearSections();
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
                int dest = sections.getDestSection(wish);
                sections.get(dest).add(wish);
                notifyItemInserted(sections.absoluteIndexById(wish.getId()));//TODO: test
            }
            Log.d(LOG_TAG, "Wish.onChildAdded()" + wish.toString());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String prevKey) {
            Wish wish = dataSnapshot.getValue(Wish.class);
            wish.setId(dataSnapshot.getKey());
            int index = sections.absoluteIndexById(wish.getId());
            if (!wish.isRemoved() &&
                    (wishLists.get(wish.getWishListId()).getOwner().equals(friendId) ||
                            wish.isReserved() ||
                            mode == WishListFragment.GIFT_LIST_MODE)) {
                wish.setReservation(dataSnapshot.child("reservation").getValue(Reservation.class));
                if (index != NOT_FIND) {//change
                    sections.removeById(wish.getId());
                    int dest = sections.getDestSection(wish);
                    sections.get(dest).add(wish);
                    notifyItemMoved(index, sections.absoluteIndexById(wish.getId()));//TODO: test
                } else {//soft-restore
                    int dest = sections.getDestSection(wish);
                    sections.get(dest).add(wish);
                    notifyItemInserted(sections.absoluteIndexById(wish.getId()));//TODO: test
//                    notifyDataSetChanged();
                }
            } else if (index != NOT_FIND) {//soft-remove
                sections.removeById(wish.getId());
                notifyItemRemoved(index);
            }
            Log.d(LOG_TAG, "Wish.onChildChanged()" + wish.toString());
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            int index = sections.removeById(dataSnapshot.getKey());
            if (index != NOT_FIND) {
                notifyItemRemoved(index);//TODO: check it!
                //notifyDataSetChanged();
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
    /*
     * Firebase query end
     *^^^^^^^^^^^^^^^^^^^^^*/

    /*vvvvvvvvvvvvvvvvvvvvv
     * Sections start
     */

    public class Sections extends ArrayList<Section> {

        public void clearSections() {
            for (Section section : this) {
                section.clear();
            }
        }

        public int getDestSection(Wish wish) {//TODO: rename
            if (wish.getReservation() == null) return 1;
            else if (wish.getReservation().getByUser().equals(AuthUtils.getCurrentUser().getId())) return 2;
            else return 0;
        }

        public int sectionIndexById(String id) {
            for (int i = 0; i < this.size(); i++) {
                if (this.get(i).indexById(id) != NOT_FIND) return i;
            }
            return NOT_FIND;
        }

        public int absoluteIndexById(String id) {
            int absoluteIndex = 0;
            for (Section section : this) {
                int sectionIndex = section.indexById(id);
                if (sectionIndex != NOT_FIND) return absoluteIndex + sectionIndex;
                absoluteIndex += section.size();
            }
            return NOT_FIND;
        }

        public int removeById(String id) {
            int absoluteIndex = 0;
            for (Section section : this) {
                int sectionIndex = section.indexById(id);
                if (sectionIndex != NOT_FIND) {
                    section.remove(sectionIndex);
                    return absoluteIndex + sectionIndex;
                }
                absoluteIndex += section.size();
            }
            return NOT_FIND;
        }

    }

    public class Section extends ArrayList<Wish> {

        private String title;

        public Section(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int indexById(String id) {
            for (int i = 0; i < this.size(); i++) {
                if (this.get(i).getId().equals(id)) return i;
            }
            return NOT_FIND;
        }

        @Override
        public boolean add(Wish wish) {
            for (int i = 0; i < this.size(); i++) {
                if (wish.getTitle().compareTo(this.get(i).getTitle()) <= 0) {
                    this.add(i, wish);
                    return true;
                }
            }
            return super.add(wish);//TODO: check
        }

    }

    /*
     * Sections end
     *^^^^^^^^^^^^^^^^^^^*/

    /*vvvvvvvvvvvvvvvvvvvvv
     * Holders start
     */

    public class Holder extends RecyclerView.ViewHolder {

        public Holder(View itemView) {
            super(itemView);
        }

    }

    public class HolderWish extends Holder {

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

        int section;
        int reletivePosition;

        public HolderWish(View itemView) {
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
                        removeWish(section, reletivePosition);
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
                        textViewReserve.setText(sections.get(section).get(reletivePosition).isReserved() ? R.string.action_unreserve : R.string.action_reserve);
                }

                @Override
                public void onClose(SwipeLayout layout) {
                    Log.d("swipe", "onClose");
                    if (swipeLayout.equals(swipedItem)) swipedItem = null;
                }

            });
        }

        public void onBind(int section, int reletivePosition) {
            this.section = section;
            this.reletivePosition = reletivePosition;
            Wish wish = sections.get(section).get(reletivePosition);

            CloudinaryUtil.loadCircleThumb(context, imageView, wish.getPicture(), R.drawable.gift_icon);

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
//            swipedItem = null;//TODO: remove
            switch (v.getId()) {
                case R.id.card_view:
                    Toast.makeText(context, String.valueOf(getAdapterPosition()) + " -> " + sections.get(section).get(reletivePosition).getTitle(), Toast.LENGTH_LONG).show();
//                    Intent intent = new Intent(context, WishEditActivity.class)
//                            .setAction(WishEditActivity.ACTION_EDIT)
//                            .putExtra("Wish", wishes.get(getAdapterPosition()));
//                    context.startActivity(intent);
                    break;
                case R.id.bottom_view_reserve:
                    reserveWish(this.section, this.reletivePosition);
                    break;
            }
        }

    }

    /*
     * Holders end
     *^^^^^^^^^^^^^^^^^^^*/

}
