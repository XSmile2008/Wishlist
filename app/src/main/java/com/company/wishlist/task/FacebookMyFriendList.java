package com.company.wishlist.task;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by v.odahovskiy on 08.01.2016.
 */
public class FacebookMyFriendList extends AsyncTask<Void, Void, JSONArray> {

    private final String TAG = FacebookMyFriendList.class.getSimpleName();


    @Override
    protected JSONArray doInBackground(Void... params) {
        AccessToken token = AccessToken.getCurrentAccessToken();
        final JSONArray[] result = {new JSONArray()};

        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            result[0] = response.getJSONObject().getJSONArray("data");
                        } catch (JSONException e) {
                            Log.d(TAG, e.getMessage());
                        }
                        Log.d(TAG, response.getJSONObject().toString());
                    }
                }
        );

        Bundle parameters = new Bundle();
        parameters.putString("data", "id,name");
        request.setParameters(parameters);
        request.executeAndWait();

        return result[0];
    }
}
