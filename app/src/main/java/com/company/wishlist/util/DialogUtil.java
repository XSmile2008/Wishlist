package com.company.wishlist.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.company.wishlist.R;
import com.company.wishlist.activity.MainActivity;
import com.company.wishlist.model.Wish;
import com.company.wishlist.util.social.TwitterUtils;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;

import org.jsoup.helper.StringUtil;

/**
 * Created by v.odahovskiy on 05.01.2016.
 */
public class DialogUtil {

    public static void alertShow(String title, String message, Context context){
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(null != title? title :"WishList");
        alertDialog.setMessage(null != message? message : "");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Close",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public static void alertShow(String title, String message, Context context, DialogInterface.OnClickListener onClickListener){
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, onClickListener)
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    public static ProgressDialog progressDialog(String title, String message, Context context){
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        return dialog;
    }

    public static void showSendTweetDialog(String message, final Context context){
        LayoutInflater li = LayoutInflater.from(context);
        View promptsView = li.inflate(R.layout.share_tweet_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.et_tweet_message);

        userInput.setText(message);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Tweet",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                String message =  userInput.getText().toString().trim();
                                if (StringUtil.isBlank(message)){
                                    Toast.makeText(context, "Should be not empty", Toast.LENGTH_SHORT).show();
                                }else {
                                    TwitterUtils.tweet(message, new Callback<Tweet>() {
                                        @Override
                                        public void success(Result<Tweet> result) {
                                            Toast.makeText(context, "Tweet shared successful", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void failure(TwitterException e) {
                                            Toast.makeText(context, "Problem with sharing tweet.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
