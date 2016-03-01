package com.company.wishlist.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.company.wishlist.R;
import com.company.wishlist.activity.abstracts.InternetActivity;
import com.company.wishlist.adapter.PinterestGridViewAdapter;
import com.company.wishlist.util.pinterest.PinterestUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ImageGridActivity extends InternetActivity {

    public static final String QUERY = "com.company.wishlist.activity.QUERY";
    public static final String RESULT_DATA = "com.company.wishlist.activity.RESULT_DATA";


    @Bind(R.id.filterGridListBtn)
    ImageButton imageButton;

    @Bind(R.id.queryImageGridList)
    EditText editText;

    PinterestGridViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_grid);
        ButterKnife.bind(this);

        //Setup ActionBar
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("");

        String query = getIntent().getStringExtra(QUERY);
        editText.setText(query);

        final GridView gridView = (GridView) findViewById(R.id.imageGridView);
        final Context app = this;

        adapter = new PinterestGridViewAdapter(app, new ArrayList<String>());
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent intent = new Intent();
                intent.putExtra(RESULT_DATA, String.valueOf(adapter.getItem(position)));
                setResult(RESULT_OK, intent);
                finish();

            }
        });

        getPictures(query);
    }

    private void getPictures(String query) {
        PinterestUtil.getImagesAsLinks(new PinterestUtil.PinterestOnLoadEvent() {
            @Override
            public void onSuccess(final List<String> urls) {
                adapter.addAll(urls);
            }
        }, query);
    }

    @OnClick(R.id.filterGridListBtn)
    public void search(View view){
        String query = editText.getText().toString().trim();
        if (StringUtils.isEmpty(query)) {
            editText.setError("Should be not empty!");
        }else {
            getPictures(query);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return false;
        }
        return super.onOptionsItemSelected(item);
    }
}

