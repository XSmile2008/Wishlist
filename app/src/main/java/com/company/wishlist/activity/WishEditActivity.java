package com.company.wishlist.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.FirebaseActivity;
import com.company.wishlist.model.Wish;
import com.company.wishlist.util.CropCircleTransformation;
import com.company.wishlist.util.DialogUtil;
import com.company.wishlist.util.Utilities;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vladstarikov on 15.01.16.
 */
public class WishEditActivity extends FirebaseActivity implements Validator.ValidationListener, CalendarDatePickerDialogFragment.OnDateSetListener {

    private static int RESULT_LOAD_IMAGE = 1;
    private static String DATE_FIALOG = "DATE_PICKER";

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

    Wish wish;
    Validator validator;
    CalendarDatePickerDialogFragment reservedDateDialog;

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

        //Setup validator
        validator = new Validator(this);
        validator.setValidationListener(this);

        //Init reserve date picker
        reservedDateDialog = new CalendarDatePickerDialogFragment();
        reservedDateDialog.setOnDateSetListener(this);
        reservedDateDialog.setFirstDayOfWeek(Calendar.MONDAY);
        reservedDateDialog.setRetainInstance(true);
        reservedDateDialog.setThemeDark(true);

        wish = getWish();
        initView();
    }

    public Wish getWish() {
        Bundle args = getIntent().getExtras();
        return (args != null && args.containsKey("wish")) ? (Wish) args.getSerializable("wish") : new Wish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wish_edit, menu);
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
                finish();
                return false;
            case R.id.action_reserve:
                reserveWish();
                return false;
            case R.id.action_delete:
                deleteWish();
                finish();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        if (wish != null) {
            editTextTitle.setText(wish.getTitle());
            editTextComment.setText(wish.getComment());
            if (wish.getPicture() != null && !wish.getPicture().isEmpty()) {
                Glide.with(this)
                        .load(wish.getPicture())
                        .bitmapTransform(new CropCircleTransformation(Glide.get(this).getBitmapPool()))
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.gift_icon);
            }
        } else {
            imageView.setImageResource(R.drawable.gift_icon);
        }
    }

    private void reserveWish() {
        if (!wish.isWishReserved()) {
            reservedDateDialog.show(getSupportFragmentManager(), DATE_FIALOG);
        } else {
            DialogUtil.alertShow(getString(R.string.app_name), getString(R.string.unreserve), this, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    wish.setReserved(null);
                    validator.validate();
                    Toast.makeText(getApplicationContext(), "wish " + wish.getTitle() + " unreserved", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void deleteWish() {
        if (null != wish.getUUID()) {
            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.remove_wish_dialog_text))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getFirebaseUtil().remove(wish.getUUID(), Wish.class);
                            Toast.makeText(getApplicationContext(), "wish " + wish.getTitle() + " deleted", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        }
    }

    private void commitChanges() {
        fillWishFields();
        getFirebaseUtil().save(wish);
        Toast.makeText(this, wish.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void fillWishFields() {
        wish.setComment(editTextComment.getText().toString());
        wish.setTitle(editTextTitle.getText().toString());
        imageView.buildDrawingCache();
        wish.setPicture(Utilities.getEncoded64ImageStringFromBitmap(imageView.getDrawingCache()));
    }

    @OnClick(R.id.image_view)
    public void chooseWishImage(ImageView view) {
        startActivityForResult(
                Intent.createChooser(
                        new Intent(Intent.ACTION_GET_CONTENT)
                                .setType("image/*"), getString(R.string.choose_image)),
                RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImageUri = data.getData();
            Glide.with(this)
                    .load(selectedImageUri)
                    .bitmapTransform(new CropCircleTransformation(Glide.get(this).getBitmapPool()))
                    .into(imageView);
        } else {
            DialogUtil.alertShow(getString(R.string.app_name), getString(R.string.choose_image_error_text), this);
        }
    }

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

    @Override
    public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
        final long reservationDate = dialog.getSelectedDay().getDateInMillis();
        wish.reserve(getFirebaseUtil().getCurrentUser().getId(), reservationDate);
        validator.validate();
    }
}
