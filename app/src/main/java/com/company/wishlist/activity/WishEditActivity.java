package com.company.wishlist.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.InternetActivity;
import com.company.wishlist.bean.EditWishBean;
import com.company.wishlist.model.Reserved;
import com.company.wishlist.model.Wish;
import com.company.wishlist.util.CropCircleTransformation;
import com.company.wishlist.util.DialogUtil;
import com.company.wishlist.util.FirebaseUtil;
import com.company.wishlist.util.LocalStorage;
import com.company.wishlist.util.Utilities;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vladstarikov on 15.01.16.
 */
public class WishEditActivity extends InternetActivity implements Validator.ValidationListener, CalendarDatePickerDialogFragment.OnDateSetListener {

    private static int RESULT_LOAD_IMAGE = 1;
    private static String DATE_FIALOG = "DATE_PICKER";
    public static String ACTION_EDIT = "com.company.wishlist.ACTION_EDIT";
    public static String ACTION_CREATE = "com.company.wishlist.ACTION_CREATE";

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

    private FirebaseUtil firebaseUtil;
    private EditWishBean editWishBean;
    private Validator validator;
    private CalendarDatePickerDialogFragment reservedDateDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish_edit);
        ButterKnife.bind(this);

        this.firebaseUtil = new FirebaseUtil(this);

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

        initWishEdit();
        initView();
    }

    public void initWishEdit() {
        if (action().equals(ACTION_CREATE)) {
            editWishBean = new EditWishBean(new Wish());
        }else if (action().equals(ACTION_EDIT)) {
            editWishBean = new EditWishBean(LocalStorage.getInstance().getWish());
        }else {
            finish();//todo may be init new object
        }
    }

    private String action() {
        return getIntent().getAction();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wish_edit, menu);
        Bundle args = getIntent().getExtras();
        if (args == null || !args.containsKey("wish")) {
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
                LocalStorage.getInstance().setWish(null);
                finish();
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

    private void initView() {
        editTextTitle.setText(editWishBean.getTitle());
        editTextComment.setText(editWishBean.getComment());
        if (!Utilities.isBlank(editWishBean.getPicture())) {
            imageView.setImageBitmap(Utilities.decodeThumbnail(editWishBean.getPicture()));
        } else {
            imageView.setImageResource(R.drawable.gift_icon);
        }
    }

    /**
     * Change logic
     */
    private void reserveWish() {
        if (!editWishBean.isReserved()) {
            reservedDateDialog.show(getSupportFragmentManager(), DATE_FIALOG);
        } else {
            DialogUtil.alertShow(getString(R.string.app_name), getString(R.string.unreserve), this, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    firebaseUtil.getFirebaseRoot().child(FirebaseUtil.WISH_TABLE).child(editWishBean.getId())
                            .child("reserved").removeValue(new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            Toast.makeText(getApplicationContext(), "wish " + editWishBean.getTitle() + " unreserved", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private void deleteWish() {
        DialogUtil.alertShow(getString(R.string.app_name), getString(R.string.remove_wish_dialog_text), this, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                firebaseUtil.remove(editWishBean.getId(), Wish.class);
                Toast.makeText(getApplicationContext(), "wish " + editWishBean.getTitle() + " deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void commitChanges() {
        fillWishFields();
        save();
        Toast.makeText(this, editWishBean.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void save() {
        if (action().equals(ACTION_CREATE)){
            firebaseUtil.getFirebaseRoot()
                    .child(FirebaseUtil.WISH_TABLE)
                    .child(editWishBean.getId())
                    .setValue(editWishBean);
        }
        if (action().equals(ACTION_EDIT)) {
            firebaseUtil.getFirebaseRoot()
                    .child(FirebaseUtil.WISH_TABLE)
                    .child(editWishBean.getId())
                    .updateChildren(editWishBean.getMapToUpdate());
        }
    }

    private void fillWishFields() {
        editWishBean.setComment(editTextComment.getText().toString());
        editWishBean.setTitle(editTextTitle.getText().toString());
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
            imageView.buildDrawingCache();
            editWishBean.setPicture(Utilities.encodeThumbnail(imageView.getDrawingCache()));
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
        final Reserved reserved = new Reserved(firebaseUtil.getCurrentUser().getId(), reservationDate);
        firebaseUtil.getFirebaseRoot()
                .child(FirebaseUtil.WISH_TABLE)
                .child(editWishBean.getId())
                .child("reserved").setValue(reserved);
    }
}
