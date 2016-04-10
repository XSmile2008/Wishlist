package com.company.wishlist.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.DebugActivity;
import com.company.wishlist.bean.EditWishBean;
import com.company.wishlist.model.Reservation;
import com.company.wishlist.model.Wish;
import com.company.wishlist.model.WishList;
import com.company.wishlist.util.AuthUtils;
import com.company.wishlist.util.CloudinaryUtil;
import com.company.wishlist.util.ConnectionUtil;
import com.company.wishlist.util.DateUtil;
import com.company.wishlist.view.BottomSheetShareDialog;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vladstarikov on 15.01.16.
 */
public class WishEditActivity extends DebugActivity implements Validator.ValidationListener {

    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_IMAGE_SELECT = 2;
    private static final String DATE_DIALOG = "DATE_PICKER";
    public static final String ACTION_READ = "com.company.wishlist.ACTION_READ";
    public static final String ACTION_EDIT = "com.company.wishlist.ACTION_EDIT";
    public static final String ACTION_CREATE = "com.company.wishlist.ACTION_CREATE";
    public static final String ACTION_TAKE_FROM_TOP = "com.company.wishlist.ACTION_TAKE_FROM_TOP";

    @Bind(R.id.image_view)
    ImageView mImageView;

    @Bind(R.id.edit_text_title)
    @NotEmpty
    @Length(min = 1)
    EditText mEditTextTitle;

    @Bind(R.id.edit_text_comment)
    @NotEmpty
    @Length(min = 2)
    EditText mEditTextComment;

    @Bind(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

    @Bind(R.id.fab)
    FloatingActionButton mFab;

    private Menu mMenu;

    private EditWishBean mEditWishBean;//TODO: test if edit wish bean contains correct data after screen rotate
    private Validator mValidator;
    private CalendarDatePickerDialogFragment mReservedDateDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish_edit);
        ButterKnife.bind(this);

        //Setup ActionBar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(getIntent().getAction().equals(ACTION_CREATE) ? "New wish" : "Edit wish");

        //Setup validator
        mValidator = new Validator(this);
        mValidator.setValidationListener(this);

        //Init reserve date picker
        mReservedDateDialog = new CalendarDatePickerDialogFragment();
        mReservedDateDialog.setFirstDayOfWeek(Calendar.MONDAY);
        mReservedDateDialog.setRetainInstance(true);
        mReservedDateDialog.setDateRange(DateUtil.getToday(), null);

        //Get wish and wishlist
        Wish wish = (Wish) getIntent().getSerializableExtra(Wish.class.getSimpleName());
        WishList wishList = (WishList) getIntent().getSerializableExtra(WishList.class.getSimpleName());
        mEditWishBean = new EditWishBean(wish);

        //Init fields and image
        mEditTextTitle.setText(mEditWishBean.getTitle());
        mEditTextComment.setText(mEditWishBean.getComment());
        if (mEditWishBean.hasPicture()) {//TODO: load optimized image
            Glide.with(this)
                    .load(CloudinaryUtil.getInstance().url().generate(mEditWishBean.getPicture()))
                    .crossFade()
                    .into(mImageView);
        } else {
            mImageView.setImageResource(R.drawable.wish_header);
        }

        //Init bean
        switch (getIntent().getAction()) {
            case ACTION_CREATE:
                mEditWishBean.setWishListId(wishList.getId());
                break;
            case ACTION_READ:
                mFab.setVisibility(View.GONE);
                ActionMode.Callback callback = new ActionMode.Callback() {//TODO: temporally will be like this, ask a question on next lesson
                    @Override
                    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                        menu.removeItem(android.R.id.cut);
                        menu.removeItem(android.R.id.paste);
                        return true;
                    }

                    @Override
                    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                        return false;
                    }

                    @Override
                    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                        return false;
                    }

                    @Override
                    public void onDestroyActionMode(ActionMode mode) {

                    }

                };
                mEditTextTitle.setInputType(InputType.TYPE_NULL);
                mEditTextTitle.setTextIsSelectable(true);
                mEditTextTitle.setCustomSelectionActionModeCallback(callback);
                mEditTextComment.setInputType(InputType.TYPE_NULL);
                mEditTextComment.setTextIsSelectable(true);
                mEditTextComment.setMinLines(8);
                mEditTextComment.setCustomSelectionActionModeCallback(callback);
                break;
            case ACTION_TAKE_FROM_TOP:
                mEditWishBean.setId(null);
                mEditWishBean.setWishListId(wishList.getId());
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.mMenu = menu;
        getMenuInflater().inflate(R.menu.menu_wish_edit, menu);
        menu.findItem(R.id.action_delete).setVisible(getIntent().getAction().equals(ACTION_EDIT));
        if (mEditWishBean.isReserved() && !mEditWishBean.getReservation().getByUser().equals(AuthUtils.getCurrentUser().getId())) {
            menu.findItem(R.id.action_reserve).setVisible(false);
        } else {
            menu.findItem(R.id.action_reserve).setIcon(mEditWishBean.isReserved()
                    ? R.drawable.ic_favorite_white_24dp
                    : R.drawable.ic_favorite_border_white_24dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return false;
            case R.id.action_done:
                mValidator.validate();
                return false;
            case R.id.action_reserve:
                reserveWish();
                return false;
            case R.id.action_delete:
                deleteWish();
                return false;
            case R.id.action_share:
                final String message = getString(R.string.message_default_tweet_wish, mEditWishBean.getTitle());
                BottomSheetDialog bottomSheetDialog = new BottomSheetShareDialog(this, message);
                bottomSheetDialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        discardChanges();
    }

    /*vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
     * Database changes logic section
     */

    private void reserveWish() {
        if (!mEditWishBean.isReserved()) {
            mReservedDateDialog.setOnDateSetListener(new CalendarDatePickerDialogFragment.OnDateSetListener() {
                @Override
                public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                    mEditWishBean.setReservation(new Reservation(AuthUtils.getCurrentUser().getId(), dialog.getSelectedDay().getDateInMillis()));
                    mMenu.findItem(R.id.action_reserve).setIcon(R.drawable.ic_favorite_white_24dp);
                    Toast.makeText(getApplicationContext(), "wish " + mEditWishBean.getTitle() + " reserved", Toast.LENGTH_SHORT).show();
                }
            });
            mReservedDateDialog.show(getSupportFragmentManager(), DATE_DIALOG);
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.message_unreserve)
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            mEditWishBean.setReservation(null);
                            mMenu.findItem(R.id.action_reserve).setIcon(R.drawable.ic_favorite_border_white_24dp);
                            Toast.makeText(getApplicationContext(), "wish " + mEditWishBean.getTitle() + " unreserved", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();
        }
    }

    private void deleteWish() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage(R.string.message_remove_wish_dialog)
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mEditWishBean.softRemove();
                        Toast.makeText(getApplicationContext(), "wish " + mEditWishBean.getTitle() + " deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .show();
    }

    private void commitChanges() {
        if (!getIntent().getAction().equals(ACTION_READ)) {
            if (mEditWishBean.getOriginal() != null && mEditWishBean.getOriginal().hasPicture() && mEditWishBean.isPictureChanged()) {
                CloudinaryUtil.destroy(mEditWishBean.getOriginal().getPicture());//destroy old image on cloud
            }
            mEditWishBean.setComment(mEditTextComment.getText().toString().trim());
            mEditWishBean.setTitle(mEditTextTitle.getText().toString().trim());
        }
        if (getIntent().getAction().equals(ACTION_CREATE) || getIntent().getAction().equals(ACTION_TAKE_FROM_TOP)) {
            mEditWishBean.push();
        } else  {
            Wish.getFirebaseRef().child(mEditWishBean.getId()).updateChildren(mEditWishBean.toMap());
        }
        Toast.makeText(this, mEditWishBean.getTitle(), Toast.LENGTH_SHORT).show();
        finish();
    }

    private void discardChanges() {
        if (mEditWishBean.isPictureChanged()) {
            CloudinaryUtil.destroy(mEditWishBean.getPicture());//destroy new image on cloud
        }
    }

    /*
     * Database changes logic section end
     *^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^*/

    @OnClick({R.id.fab})
    public void showImagesDialog(View view) {
        if (ConnectionUtil.isConnected()) {
            String query = mEditTextTitle.getText().toString().trim();
            if (query.isEmpty()) {
                Snackbar.make(mCoordinatorLayout, "Wish title should be not empty!", Snackbar.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, ImageSearchActivity.class);
                intent.putExtra(ImageSearchActivity.QUERY, mEditTextTitle.getText().toString());
                startActivityForResult(intent, RESULT_IMAGE_SELECT);
            }
//            //TODO: move loading image from storage to ImageSearchActivity
//            startActivityForResult(Intent.createChooser(new Intent(Intent.ACTION_GET_CONTENT)
//                    .setType("image/*"), getString(R.string.choose_image)), RESULT_LOAD_IMAGE);
        } else {
            Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.no_connection), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * called when user choose image from device
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            if (ConnectionUtil.isConnected()) {
                CloudinaryUtil.IOnDoneListener listener = new CloudinaryUtil.IOnDoneListener() {
                    @Override
                    public void onDone(final Map<String, Object> imgInfo) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mEditWishBean.isPictureChanged() && mEditWishBean.hasPicture()) {
                                    CloudinaryUtil.destroy(mEditWishBean.getPicture());//if user pick image second time destroy old
                                }
                                mEditWishBean.setPicture((String) imgInfo.get("public_id"));
                                Glide.with(getApplicationContext()) //TODO: load optimized image
                                        .load(CloudinaryUtil.getInstance().url().generate(mEditWishBean.getPicture()))
                                        .crossFade()
                                        .into(mImageView);
                            }
                        });
                    }
                };
                try {
                    if (requestCode == RESULT_LOAD_IMAGE) {
                        Uri selectedImageUri = data.getData();
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        CloudinaryUtil.upload(inputStream, listener);
                    } else if (requestCode == RESULT_IMAGE_SELECT) {
                        final String url = data.getStringExtra(ImageSearchActivity.RESULT_DATA).trim();
                        if (url.isEmpty()) throw new IOException("Something went wrong...");
                        CloudinaryUtil.upload(url, listener);
                    }
                } catch (IOException e) {
                    Snackbar.make(findViewById(R.id.coordinator_layout), e.getMessage(), Snackbar.LENGTH_SHORT);
                }
            } else {
                Snackbar.make(mCoordinatorLayout, getResources().getString(R.string.no_connection), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /*vvvvvvvvvvvvvvvvvvv
     * Validation section
     */

    @Override
    public void onValidationSucceeded() {
        commitChanges();
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
    /*
     * Validation section end
     *^^^^^^^^^^^^^^^^^^^^^^^*/
}
