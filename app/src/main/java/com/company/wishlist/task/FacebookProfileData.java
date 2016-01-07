package com.company.wishlist.task;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;

import org.json.JSONObject;

/**
 * Created by v.odahovskiy on 06.01.2016.
 */
public class FacebookProfileData extends AsyncTask<Void, Void, JSONObject> {

    private final String TAG = FacebookProfileData.class.getSimpleName();

    @Override
    protected JSONObject doInBackground(Void... params) {
        AccessToken token = AccessToken.getCurrentAccessToken();
        final JSONObject[] result = {new JSONObject()};

        GraphRequest request = GraphRequest.newMeRequest(
                token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        Log.d(TAG, "FB profile data response successful loaded...");
                        result[0] = object;
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,last_name,first_name,email,gender, birthday");
        request.setParameters(parameters);
        request.executeAndWait();


        return result[0];
    }

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String BIRTHDAY = "birthday";
    public static final String GENDER = "gender";
    public static final String EMAIL = "email";
}
