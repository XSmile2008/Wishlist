package com.company.wishlist.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.company.wishlist.activity.MainActivity;

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
}
