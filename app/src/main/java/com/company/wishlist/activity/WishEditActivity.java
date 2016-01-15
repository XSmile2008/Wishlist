package com.company.wishlist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.InternetActivity;
import com.company.wishlist.model.Wish;
import com.company.wishlist.util.CropCircleTransformation;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by vladstarikov on 15.01.16.
 */
public class WishEditActivity extends InternetActivity {

    @Bind(R.id.image_view) ImageView imageView;
    @Bind(R.id.edit_text_title) EditText editTextTitle;
    @Bind(R.id.edit_text_comment) EditText editTextComment;

    Wish wish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wish_edit);
        ButterKnife.bind(this);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        Bundle args = getIntent().getExtras();
        if (args != null && args.containsKey("wish")) {
            wish = (Wish) args.getSerializable("wish");
        } else {
            wish = new Wish();
        }
        initView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_wish_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home :
                onBackPressed();
                return false;
            case R.id.action_done :
                commitChanges();
                onBackPressed();
                return false;
            case R.id.action_reserve :
                reserveWish();
                return false;
            case R.id.action_delete :
                deleteWish();
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

        editTextTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                wish.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editTextComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                wish.setComment(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    public void commitChanges() {
        //write new/updated wish object to database
        Toast.makeText(this, wish.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.image_view)
    public void chooseWishImage(ImageView view) {
        Toast.makeText(this, "will be call dialog to choose image from photo library or write picture URL", Toast.LENGTH_SHORT).show();
    }

    private void reserveWish() {
        //show reservation dialog
        Toast.makeText(this, "wish " + wish.getTitle() + " reserved", Toast.LENGTH_SHORT).show();
    }

    private void deleteWish() {
        //show deletion dialog
        Toast.makeText(this, "wish " + wish.getTitle() + " deleted", Toast.LENGTH_SHORT).show();
    }
}
