package com.company.wishlist.util.social.share;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.company.wishlist.R;

import org.jsoup.helper.StringUtil;

/**
 * Created by odahovskiy on 20.03.2016.
 */
public class SocialSharing {

    private Context context;
    private String message;
    private ShareStrategy shareStrategy;

    public SocialSharing(Context context) {
        this.context = context;
    }

    public SocialSharing(String message, Context context) {
        this.message = message;
        this.context = context;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public ShareStrategy getShareStrategy() {
        return shareStrategy;
    }

    public void setShareStrategy(ShareStrategy shareStrategy) {
        this.shareStrategy = shareStrategy;
    }

    public void share() {
        if (null == shareStrategy) {
            throw new IlleagalVariableException("Should be implemented ShareStrategy!");
        }
        View promptsView = LayoutInflater.from(context).inflate(R.layout.dialog_tweet, null, false);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.et_tweet_message);
        userInput.setText(message);

        new AlertDialog.Builder(context)
                .setView(promptsView)
                .setCancelable(false)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setMessage(userInput.getText().toString().trim());
                        if (StringUtil.isBlank(message)) {
                            Toast.makeText(context, "Should be not empty", Toast.LENGTH_SHORT).show();
                        } else {
                            shareStrategy.share(message, callback);
                        }
                    }
                })
                .show();
    }

    private SharingCallback callback = new SharingCallback() {
        @Override
        public void success() {
            Toast.makeText(context, "Oll ok", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void failure(Throwable error) {
            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
        }
    };

}
