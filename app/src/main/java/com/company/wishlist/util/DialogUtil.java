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
import com.company.wishlist.util.social.SocialShare;
import com.company.wishlist.util.social.SocialShareUtils;
import com.twitter.sdk.android.core.TwitterException;

import org.jsoup.helper.StringUtil;

/**
 * Created by v.odahovskiy on 05.01.2016.
 */
public class DialogUtil {

    public static void alertShow(String title, String message, Context context) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(null != title ? title : "WishList");
        alertDialog.setMessage(null != message ? message : "");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Close",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    public static void alertShow(String title, String message, Context context, DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, onClickListener)
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    public static ProgressDialog progressDialog(String title, String message, Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        return dialog;
    }

}
