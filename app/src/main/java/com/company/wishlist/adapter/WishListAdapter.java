package com.company.wishlist.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.company.wishlist.R;
import com.company.wishlist.activity.WishEditActivity;
import com.company.wishlist.fragment.WishListFragment;
import com.company.wishlist.interfaces.IOnFriendSelectedListener;
import com.company.wishlist.model.Reservation;
import com.company.wishlist.model.Wish;
import com.company.wishlist.model.WishList;
import com.company.wishlist.util.AuthUtils;
import com.company.wishlist.util.CloudinaryUtil;
import com.company.wishlist.util.DateUtil;
import com.company.wishlist.util.social.twitter.TwitterUtils;

import com.company.wishlist.util.social.share.SocialSharing;
import com.company.wishlist.util.social.share.impl.FacebookSharing;
import com.company.wishlist.util.social.share.impl.TwitterSharing;
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
    private static int NOT_FIND = -1;

    private Context context;
    private View rootView;
    private int mode;//WISH_LIST_MODE or GIFT_LIST_MODE

    private WishEventListener listenersWish = new WishEventListener();
    private List<Query> queriesWish = new ArrayList<>();

    private Map<String, WishList> wishLists = new HashMap<>();
    private Sections sections = new Sections();
    private Wish wishBackUp;

    private String friendId;

    private SwipeLayout swipedItem;

    /**
     * @param context context that will be used in this adapter
     * @param mode    mode of this list. May be WISH_LIST_MODE or GIFT_LIST_MODE
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
        int layout = viewType == VIEW_TYPE_HEADER ? R.layout.section : R.layout.wish_list_item;
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    }

    @Override
    public void onBindHeaderViewHolder(Holder holder, int section) {
        ((TextView) holder.itemView).setText(sections.get(section).getTitle());

    }

    @Override
    public void onBindViewHolder(Holder holder, int section, int relativePosition, int absolutePosition) {
        holder.onBind(section, relativePosition);
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
            reservedDateDialog.setThemeDark(false);
            reservedDateDialog.setDateRange(DateUtil.getToday(), null);
            reservedDateDialog.setOnDateSetListener(new CalendarDatePickerDialogFragment.OnDateSetListener() {
                @Override
                public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                    wish.reserve(AuthUtils.getCurrentUser().getId(), dialog.getSelectedDay().getDateInMillis());
                    Toast.makeText(context, "item " + wish.getTitle() + " reserved", Toast.LENGTH_SHORT).show();
                }
            });
            reservedDateDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "DATE_PICKER");
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
        wishBackUp.softRestore();
    }

    /*
     * IWishItemAdapter end
     *^^^^^^^^^^^^^^^^^^^^^*/

    /*vvvvvvvvvvvvvvvvvvvvvvv
     * Firebase query start
     */

    @Override
    public void onFriendSelected(String friendId) {
        Log.d(LOG_TAG, "onFriendSelected(" + friendId + ")");
        this.friendId = friendId;
        sections.clearSections();
        for (Query query : queriesWish)
            query.removeEventListener(listenersWish);//remove all unused listeners
        getWishLists(friendId);
        notifyDataSetChanged();
    }

    /**
     * Query that get all wishLists that addressed to forUser,
     * and depending at wishList type sort it by owner
     *
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
     *
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
                Pair<Integer, Integer> pos = sections.putWish(wish);
                notifyItemInserted(sections.getAbsolutePosition(pos));
            }
            Log.d(LOG_TAG, "Wish.onChildAdded()" + wish.toString());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String prevKey) {
            Wish wish = dataSnapshot.getValue(Wish.class);
            wish.setId(dataSnapshot.getKey());
            if (!wish.isRemoved() &&
                    (wishLists.get(wish.getWishListId()).getOwner().equals(friendId) ||
                            wish.isReserved() ||
                            mode == WishListFragment.GIFT_LIST_MODE)) {
                wish.setReservation(dataSnapshot.child("reservation").getValue(Reservation.class));
                Pair<Integer, Integer> oldPos = sections.findWish(wish);
                if (oldPos != null) {//change
                    int absoluteOld = sections.getAbsolutePosition(oldPos);
                    Pair<Integer, Integer> pos = sections.putWish(wish);
                    int absoluteNew = sections.getAbsolutePosition(pos);
                    notifyItemMoved(absoluteOld, absoluteNew);
                    notifyItemChanged(absoluteNew);
                } else {//soft-restore
                    Pair<Integer, Integer> pos = sections.putWish(wish);
                    notifyItemInserted(sections.getAbsolutePosition(pos));
                }
            } else {//soft-remove
                Pair<Integer, Integer> pos = sections.removeWish(wish);
                if (pos != null) notifyItemRemoved(sections.getAbsolutePosition(pos));
            }
            Log.d(LOG_TAG, "Wish.onChildChanged()" + wish.toString());
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Wish wish = dataSnapshot.getValue(Wish.class);
            wish.setId(dataSnapshot.getKey());
            Pair<Integer, Integer> oldPos = sections.removeWish(wish);
            if (oldPos != null) {
                notifyItemRemoved(sections.getAbsolutePosition(oldPos));
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

    //TODO: write doc for this class
    public class Sections extends ArrayList<Section> {

        public void clearSections() {
            for (Section section : this) {
                section.clear();
            }
        }

        public int getDestSection(Wish wish) {
            if (wish.getReservation() == null) return 1;
            else if (!wish.getReservation().getByUser().equals(AuthUtils.getCurrentUser().getId()))
                return 2;
            else return 0;
        }

        public int getAbsolutePosition(Pair<Integer, Integer> relativePos) {
            return getAbsolutePosition(relativePos.first, relativePos.second);
        }

        public int getAbsolutePosition(int section, int relativePosition) {
            int offset = 0;
            for (int i = 0; i < section; i++) {
                offset += this.get(i).size() + 1;//add previous section size plus one for section header
            }
            return offset + relativePosition + 1;
        }

        public Pair<Integer, Integer> getRelativePosition(int absolutePosition) {
            int offset = 0;
            int section;
            for (section = 0; section < this.size(); section++) {
                if (offset + this.get(section).size() + 1 > absolutePosition) break;
                offset += this.get(section).size() + 1;
            }
            return new Pair<>(section, absolutePosition - offset - 1);
        }

        public Pair<Integer, Integer> findWish(Wish wish) {
            for (int section = 0; section < this.size(); section++) {
                int relativePosition = this.get(section).findWish(wish);
                if (relativePosition != NOT_FIND) {
                    return new Pair<>(section, relativePosition);
                }
            }
            return null;
        }

        public Pair<Integer, Integer> putWish(Wish wish) {
            Pair<Integer, Integer> oldPos = findWish(wish);
            if (oldPos != null) {
                this.get(oldPos.first).remove(oldPos.second.intValue());//remove wish from old pos
            }
            int section = getDestSection(wish);
            int relativePosition = this.get(section).putWish(wish);
            return new Pair<>(section, relativePosition);
        }

        public Pair<Integer, Integer> removeWish(Wish wish) {
            for (int section = 0; section < this.size(); section++) {
                int relativePosition = this.get(section).findWish(wish);
                if (relativePosition != NOT_FIND) {
                    this.get(section).remove(relativePosition);
                    return new Pair<>(section, relativePosition);
                }
            }
            return null;
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

        public int findWish(Wish wish) {
            for (int i = 0; i < this.size(); i++) {
                if (this.get(i).getId().equals(wish.getId())) return i;
            }
            return NOT_FIND;
        }

        public int putWish(Wish wish) {
            for (int i = 0; i < this.size(); i++) {
                if (wish.getTitle().compareTo(this.get(i).getTitle()) <= 0) {
                    this.add(i, wish);
                    return i;
                }
            }
            super.add(wish);
            return this.size() - 1;
        }

    }

    /*
     * Sections end
     *^^^^^^^^^^^^^^^^^^^*/

    /*vvvvvvvvvvvvvvvvvvvvv
     * Holders start
     */

    public class Holder extends RecyclerView.ViewHolder {

        @Bind(R.id.swipe_layout)
        SwipeLayout swipeLayout;

        //CardView
        @Bind(R.id.card_view)
        CardView cardView;
        @Bind(R.id.image_view)
        ImageView imageView;
        @Bind(R.id.text_view_title)
        TextView textViewTitle;
        @Bind(R.id.text_view_comment)
        TextView textViewComment;
        @Bind(R.id.text_view_status)
        TextView textViewStatus;

        //Background
        @Bind(R.id.bottom_view_remove)
        ViewGroup bottomViewRemove;
        @Bind(R.id.bottom_view_reserve)
        ViewGroup bottomViewReserve;
        @Bind(R.id.button_reserve)
        Button buttonReserve;
        @Bind(R.id.button_share)
        Button buttonShare;

        private SocialSharing socialSharing;

        public Holder(View itemView) {
            super(itemView);
            if (itemView.getId() != R.id.swipe_layout) return;
            ButterKnife.bind(this, itemView);

            socialSharing = new SocialSharing(context);
            swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
            swipeLayout.addDrag(SwipeLayout.DragEdge.Right, bottomViewReserve);
            if (mode == WishListFragment.GIFT_LIST_MODE)
                swipeLayout.addDrag(SwipeLayout.DragEdge.Left, bottomViewRemove);
            swipeLayout.addSwipeListener(new SimpleSwipeListener() {
                @Override
                public void onOpen(SwipeLayout layout) {
//                    Log.d("swipe", "onOpen");
                    if (layout.getDragEdge() == SwipeLayout.DragEdge.Left) {
                        Pair<Integer, Integer> pos = sections.getRelativePosition(getAdapterPosition());
                        removeWish(pos.first, pos.second);
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
//                    Log.d("swipe", "onUpdate");
                    float alpha = Math.abs((float) leftOffset / (float) bottomViewReserve.getWidth());//TODO:
                    layout.getCurrentBottomView().setAlpha(alpha);
                }

                @Override
                public void onStartOpen(SwipeLayout layout) {
//                    Log.e("swipe", "onStartOpen");
                    if (swipedItem != null && !swipeLayout.equals(swipedItem)) {
                        swipedItem.close();
                    }
                    swipedItem = swipeLayout;
                    if (layout.getDragEdge() == SwipeLayout.DragEdge.Right) {
                        Pair<Integer, Integer> pos = sections.getRelativePosition(getAdapterPosition());
                        buttonReserve.setText(sections.get(pos.first).get(pos.second).isReserved() ? R.string.action_unreserve : R.string.action_reserve);
                    }
                }

                @Override
                public void onClose(SwipeLayout layout) {
//                    Log.d("swipe", "onClose");
                    if (swipeLayout.equals(swipedItem)) swipedItem = null;
                }

            });
        }

        public void onBind(int section, int relativePosition) {

            Wish wish = sections.get(section).get(relativePosition);

            CloudinaryUtil.loadThumb(context, imageView, wish.getPicture(), R.drawable.gift_icon, true);

            textViewTitle.setText(wish.getTitle());
            textViewComment.setText(wish.getComment());

            if (wish.isReserved()) {
                if (wish.getReservation().getByUser().equals(AuthUtils.getCurrentUser().getId())) {
                    textViewStatus.setText(R.string.reserved_by_me);
                    swipeLayout.setRightSwipeEnabled(true);
                } else {// by another user
                    textViewStatus.setText(R.string.reserved);//TODO: if wish list mode == MY_WISH_LIST enable swipe and enable swipe
                    swipeLayout.setRightSwipeEnabled(false);
                }
            } else {
                textViewStatus.setText("");
                swipeLayout.setRightSwipeEnabled(true);
            }
        }

        @OnClick({R.id.card_view, R.id.button_reserve, R.id.button_share})
        public void onClick(View v) {
            Pair<Integer, Integer> pos = sections.getRelativePosition(getAdapterPosition());
            switch (v.getId()) {
                case R.id.card_view:
                    closeSwipeMenu();
                    String test = getAdapterPosition() + " -> " + sections.get(pos.first).get(pos.second).getTitle() + " @" + pos.first + ", " + pos.second;
                    Toast.makeText(context, test, Toast.LENGTH_SHORT).show();
                    Wish wish = sections.get(pos.first).get(pos.second);
                    WishList wishList = wishLists.get(wish.getWishListId());
                    Intent intent = new Intent(context, WishEditActivity.class)
                            .putExtra(Wish.class.getSimpleName(), wish)
                            .putExtra(WishList.class.getSimpleName(), wishList)
                            .setAction(wishList.getOwner().equals(AuthUtils.getCurrentUser().getId())
                                    ? WishEditActivity.ACTION_EDIT
                                    : WishEditActivity.ACTION_READ);
                    context.startActivity(intent);
                    break;
                case R.id.button_reserve:
                    closeSwipeMenu();
                    reserveWish(pos.first, pos.second);
                    break;
                case R.id.button_share:
                    final String message = context.getString(R.string.message_default_tweet_wish, sections.get(pos.first).get(pos.second).getTitle());
                    socialSharing.setMessage(message);
                    PopupMenu popup = new PopupMenu(context, v);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Toast.makeText(context, getAdapterPosition() + ": " + item, Toast.LENGTH_SHORT).show();
                            switch (item.getItemId()) {
                                case R.id.action_facebook:
                                    socialSharing.setShareStrategy(new FacebookSharing());
                                    break;
                                case R.id.action_twitter:
                                    socialSharing.setShareStrategy(new TwitterSharing());
                                    break;
                            }
                            socialSharing.share();
                            closeSwipeMenu();
                            return false;
                        }
                    });
                    popup.inflate(R.menu.menu_social_share);
                    popup.getMenu().findItem(R.id.action_twitter).setEnabled(TwitterUtils.isConnected());
                    popup.show();
                    break;
            }
        }

        private void closeSwipeMenu() {
            if (swipedItem != null) swipedItem.close();
            swipedItem = null;//required
        }

    }

    /*
     * Holders end
     *^^^^^^^^^^^^^^^^^^^*/

}
