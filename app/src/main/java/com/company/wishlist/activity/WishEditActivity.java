package com.company.wishlist.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.codetroopers.betterpickers.calendardatepicker.MonthAdapter;
import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.DebugActivity;
import com.company.wishlist.bean.EditWishBean;
import com.company.wishlist.fragment.WishListFragment;
import com.company.wishlist.model.Wish;
import com.company.wishlist.util.AuthUtils;
import com.company.wishlist.util.CloudinaryUtil;
import com.company.wishlist.util.ConnectionUtil;
import com.company.wishlist.util.DateUtil;
import com.company.wishlist.util.DialogUtil;
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
    public static final String ACTION_EDIT = "com.company.wishlist.ACTION_EDIT";
    public static final String ACTION_CREATE = "com.company.wishlist.ACTION_CREATE";
    public static final String ACTION_TAKE_FROM_TOP = "com.company.wishlist.ACTION_TAKE_FROM_TOP";

    @Bind(R.id.image_view)
    ImageView imageView;

    @Bind(R.id.edit_text_title)
    @NotEmpty
    @Length(min = 1)
    EditText editTextTitle;

    @Bind(R.id.edit_text_comment)
    @NotEmpty
    @Length(min = 2)
    EditText editTextComment;

    @Bind(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;

    private EditWishBean editWishBean;//TODO: test if edit wish bean contains correct data after screen rotate
    private Validator validator;
    private CalendarDatePickerDialogFragment reservedDateDialog;

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
        validator = new Validator(this);
        validator.setValidationListener(this);

        //Init reserve date picker
        reservedDateDialog = new CalendarDatePickerDialogFragment();
        reservedDateDialog.setFirstDayOfWeek(Calendar.MONDAY);
        reservedDateDialog.setRetainInstance(true);
        reservedDateDialog.setThemeDark(true);
        reservedDateDialog.setDateRange(DateUtil.getToday(), null);

        //Init bean
        switch (getIntent().getAction()) {
            case ACTION_CREATE:
                editWishBean = new EditWishBean(new Wish());
                editWishBean.setWishListId(getIntent().getStringExtra(WishListFragment.WISH_LIST_ID));
                break;
            case ACTION_EDIT:
                editWishBean = new EditWishBean((Wish) getIntent().getSerializableExtra(Wish.class.getSimpleName()));
                break;
            case ACTION_TAKE_FROM_TOP:
                editWishBean = new EditWishBean((Wish) getIntent().getSerializableExtra(Wish.class.getSimpleName()));
                editWishBean.setId(null);
                editWishBean.setWishListId(getIntent().getStringExtra(WishListFragment.WISH_LIST_ID));
                break;
        }

        //Init view
        editTextTitle.setText(editWishBean.getTitle());
        editTextComment.setText(editWishBean.getComment());
        CloudinaryUtil.loadThumb(this, imageView, editWishBean.getPicture(), R.drawable.gift_icon, true);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wish_edit, menu);
        if (getIntent().getAction().equals(ACTION_CREATE)) {
            menu.findItem(R.id.action_delete).setVisible(false);
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
                validator.validate();
                return false;
            case R.id.action_reserve:
                reserveWish();
                return false;
            case R.id.action_delete:
                deleteWish();
                return false;
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
        if (!editWishBean.isReserved()) {
            reservedDateDialog.setOnDateSetListener(new CalendarDatePickerDialogFragment.OnDateSetListener() {
                @Override
                public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                    editWishBean.reserve(AuthUtils.getCurrentUser().getId(), dialog.getSelectedDay().getDateInMillis());
                    Toast.makeText(getApplicationContext(), "wish " + editWishBean.getTitle() + " reserved", Toast.LENGTH_SHORT).show();
                }
            });
            reservedDateDialog.show(getSupportFragmentManager(), DATE_DIALOG);
        } else {
            DialogUtil.alertShow(getString(R.string.app_name), getString(R.string.message_unreserve), this, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    editWishBean.unreserve();
                    Toast.makeText(getApplicationContext(), "wish " + editWishBean.getTitle() + " unreserved", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteWish() {
        DialogUtil.alertShow(getString(R.string.app_name), getString(R.string.message_remove_wish_dialog), this, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                editWishBean.softRemove();
                Toast.makeText(getApplicationContext(), "wish " + editWishBean.getTitle() + " deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        //TODO: need to show SnackBar on main activity, like after swipe to remove, may be start this activity for result and return something like "removed" flag and than show SnackBar?
    }

    private void commitChanges() {
        if (editWishBean.isPictureChanged() && editWishBean.getOriginalWish().getPicture() != null) {
            CloudinaryUtil.destroy(editWishBean.getOriginalWish().getPicture());//destroy old image on cloud
        }
        editWishBean.setComment(editTextComment.getText().toString());
        editWishBean.setTitle(editTextTitle.getText().toString());
        if (getIntent().getAction().equals(ACTION_CREATE) || getIntent().getAction().equals(ACTION_TAKE_FROM_TOP)) {
            editWishBean.push();
        } else if (getIntent().getAction().equals(ACTION_EDIT)) {
            Wish.getFirebaseRef().child(editWishBean.getId()).updateChildren(editWishBean.toMap());
        }
        Toast.makeText(this, editWishBean.getTitle(), Toast.LENGTH_SHORT).show();
        finish();
    }

    private void discardChanges() {
        if (editWishBean.isPictureChanged()) {
            CloudinaryUtil.destroy(editWishBean.getPicture());//destroy new image on cloud
        }
    }

    /*
     * Database changes logic section end
     *^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^*/

    @OnClick({R.id.search_images_btn, R.id.gallery_image_btn})
    public void showImagesDialog(View view) {
        if (ConnectionUtil.isConnected()) {
            switch (view.getId()) {
                case R.id.search_images_btn:
                    String query = editTextTitle.getText().toString().trim();
                    if (query.isEmpty()) {
                        Snackbar.make(coordinatorLayout, "Wish title should be not empty!", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Intent intent = new Intent(this, ImageSearchActivity.class);
                        intent.putExtra(ImageSearchActivity.QUERY, editTextTitle.getText().toString());
                        startActivityForResult(intent, RESULT_IMAGE_SELECT);
                    }
                    break;
                case R.id.gallery_image_btn:
                    startActivityForResult(Intent.createChooser(new Intent(Intent.ACTION_GET_CONTENT)
                            .setType("image/*"), getString(R.string.choose_image)), RESULT_LOAD_IMAGE);
                    break;
            }
        } else {
            Snackbar.make(coordinatorLayout, getResources().getString(R.string.no_connection), Snackbar.LENGTH_LONG).show();
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
                        editWishBean.setPicture((String) imgInfo.get("public_id"));
                        CloudinaryUtil.loadThumb(getApplicationContext(), imageView, editWishBean.getPicture(), R.drawable.gift_icon, true);
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
                Snackbar.make(coordinatorLayout, getResources().getString(R.string.no_connection), Snackbar.LENGTH_LONG).show();
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
