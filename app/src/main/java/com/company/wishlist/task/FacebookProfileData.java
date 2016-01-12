package com.company.wishlist.task;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.company.wishlist.model.User;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.gson.Gson;

import org.json.JSONObject;

/**
 * Created by v.odahovskiy on 06.01.2016.
 */
public class FacebookProfileData extends AsyncTask<Void, Void, User> {

    private final String TAG = FacebookProfileData.class.getSimpleName();

    @Override
    protected User doInBackground(Void... params) {
        final User[] user = {null};
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d(TAG, "FB profile data response successful loaded...");
                        user[0] = new Gson().fromJson(object.toString(), User.class);
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,last_name,first_name,email,gender, birthday");
        request.setParameters(parameters);
        request.executeAndWait();
        return user[0];
    }
}
