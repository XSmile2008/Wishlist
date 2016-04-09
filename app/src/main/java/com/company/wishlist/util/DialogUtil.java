package com.company.wishlist.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by v.odahovskiy on 05.01.2016.
 */
@Deprecated
public class DialogUtil {

    @Deprecated
    public static void alertShow(String title, String message, Context context, DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, onClickListener)
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

}
