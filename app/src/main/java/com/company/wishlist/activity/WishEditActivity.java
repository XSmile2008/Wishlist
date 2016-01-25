package com.company.wishlist.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.InternetActivity;
import com.company.wishlist.adapter.InstaGridViewAdapter;
import com.company.wishlist.bean.EditWishBean;
import com.company.wishlist.fragment.WishListFragment;
import com.company.wishlist.model.Reserved;
import com.company.wishlist.model.Wish;
import com.company.wishlist.util.CropCircleTransformation;
import com.company.wishlist.util.DialogUtil;
import com.company.wishlist.util.FirebaseUtil;
import com.company.wishlist.util.LocalStorage;
import com.company.wishlist.util.Utilities;
import com.company.wishlist.util.social.InstagramUtil;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.GridHolder;
import com.orhanobut.dialogplus.OnItemClickListener;

import org.jinstagram.entity.common.Images;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vladstarikov on 15.01.16.
 */
public class WishEditActivity extends InternetActivity implements Validator.ValidationListener, CalendarDatePickerDialogFragment.OnDateSetListener {

    private static int RESULT_LOAD_IMAGE = 1;
    private static String DATE_DIALOG = "DATE_PICKER";
    public static String ACTION_EDIT = "com.company.wishlist.ACTION_EDIT";
    public static String ACTION_CREATE = "com.company.wishlist.ACTION_CREATE";
    public static String ACTION_TAKE_FROM_TOP = "com.company.wishlist.ACTION_TAKE_FROM_TOP";

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

    @Bind(R.id.insta_images_btn)
    ImageButton instaImgBtn;

    @Bind(R.id.insta_layout)
    LinearLayout instaLayout;

    @Bind(R.id.insta_text)
    TextView instaText;


    private FirebaseUtil firebaseUtil;
    private EditWishBean editWishBean;
    private Validator validator;
    private CalendarDatePickerDialogFragment reservedDateDialog;
    private Firebase wishesRef;

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
        actionBar.setTitle(getIntent().getAction() == ACTION_CREATE ? "New wish" : "Edit wish");

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

    public void initWishEdit() {
        if (getIntent().getAction().equals(ACTION_CREATE)) {//TODO: use firebase push, not random UUID, ID must set on firebase side
            editWishBean = new EditWishBean(new Wish());
            editWishBean.setWishListId(getIntent().getStringExtra(WishListFragment.WISH_LIST_ID));//TODO:
        } else if (getIntent().getAction().equals(ACTION_EDIT)) {
            editWishBean = new EditWishBean(LocalStorage.getInstance().getWish());
        } else if(getIntent().getAction().equals(ACTION_TAKE_FROM_TOP)) {
            editWishBean = new EditWishBean(LocalStorage.getInstance().getWish());
            editWishBean.setId(null);
            editWishBean.setWishListId(getIntent().getStringExtra(WishListFragment.WISH_LIST_ID));//TODO:
        }

        wishesRef = firebaseUtil.getFirebaseRoot().child(FirebaseUtil.WISH_TABLE);
    }

    private void initView() {
        editTextTitle.setText(editWishBean.getTitle());
        editTextComment.setText(editWishBean.getComment());
        if (!Utilities.isBlank(editWishBean.getPicture())) {
            //Glide.with(this).load(Utilities.decodeThumbnail(editWishBean.getPicture())).into(imageView);

            imageView.setImageBitmap(Utilities.decodeThumbnail(editWishBean.getPicture()));//TODO: CropCircleTransformation
        } else {
            imageView.setImageResource(R.drawable.gift_icon);
        }
    }

    /**
     * Change logic
     */
    private void reserveWish() {
        if (!editWishBean.isReserved()) {
            reservedDateDialog.show(getSupportFragmentManager(), DATE_DIALOG);
        } else {
            DialogUtil.alertShow(getString(R.string.app_name), getString(R.string.unreserve), this, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    wishesRef.child(editWishBean.getId()).child("reserved").removeValue(new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            Toast.makeText(getApplicationContext(), "wish " + editWishBean.getTitle() + " unreserved", Toast.LENGTH_SHORT).show();
                            editWishBean.setReserved(null);
                        }
                    });
                }
            });
        }
    }

    private void deleteWish() {
        DialogUtil.alertShow(getString(R.string.app_name), getString(R.string.remove_wish_dialog_text), this, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                wishesRef.child(editWishBean.getId()).removeValue();
                Toast.makeText(getApplicationContext(), "wish " + editWishBean.getTitle() + " deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void commitChanges() {
        editWishBean.setComment(editTextComment.getText().toString());
        editWishBean.setTitle(editTextTitle.getText().toString());
        if (getIntent().getAction().equals(ACTION_CREATE) || getIntent().getAction().equals(ACTION_TAKE_FROM_TOP)) {
            wishesRef.child(wishesRef.push().getKey()).setValue(editWishBean);
        } else if (getIntent().getAction().equals(ACTION_EDIT)) {
            wishesRef.child(editWishBean.getId()).updateChildren(editWishBean.getMapToUpdate());
        }
        Toast.makeText(this, editWishBean.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.insta_images_btn)
    public void showInstaImagesDialog(View view) {
        String[] tags = editTextTitle.getText().toString().split("\\s+");

        DialogPlus dialog = DialogPlus.newDialog(this)
                .setAdapter(new InstaGridViewAdapter(this, InstagramUtil.getInstance().getPicturesByTag(tags)))
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                        Images image = (Images) item;
                        Glide.with(getApplicationContext())
                                .load(image.getLowResolution().getImageUrl())
                                .bitmapTransform(new CropCircleTransformation(Glide.get(getApplicationContext()).getBitmapPool()))
                                .into(imageView);
                        editWishBean.setPicture(Utilities.encodeThumbnail(Utilities.getBitmapFromURL(image.getLowResolution().getImageUrl())));
                        dialog.dismiss();

                    }
                })
                .setCancelable(true)
                .setContentHolder(new GridHolder(4))
                .setGravity(Gravity.BOTTOM)
                .setExpanded(true)  // This will enable the expand feature, (similar to android L share dialog)
                .create();
        dialog.show();
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
            try {
                Uri selectedImageUri = data.getData();
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), selectedImageUri);
                editWishBean.setPicture(Utilities.encodeThumbnail(bitmap));
                Glide.with(this)
                        .load(selectedImageUri)
                        .bitmapTransform(new CropCircleTransformation(Glide.get(this).getBitmapPool()))
                        .into(imageView);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
        wishesRef.child(editWishBean.getId()).child("reserved").setValue(reserved, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                editWishBean.setReserved(null);
                Toast.makeText(getApplicationContext(), "wish " + editWishBean.getTitle() + " reserved", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
