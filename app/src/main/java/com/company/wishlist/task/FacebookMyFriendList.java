package com.company.wishlist.task;

import android.os.AsyncTask;

import com.company.wishlist.model.User;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by v.odahovskiy on 08.01.2016.
 */
public class FacebookMyFriendList extends AsyncTask<Void, Void, List<User>> {
    //TODO: move this to MainActivity, it is not called anywhere else
    @Override
    protected List<User> doInBackground(Void... params) {
        final List<User> result = new ArrayList<>();
        GraphRequest request = GraphRequest.newMyFriendsRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONArrayCallback() {
                    @Override
                    public void onCompleted(JSONArray objects, GraphResponse response) {
                        List<User> users = new Gson().fromJson(objects.toString(), new TypeToken<List<User>>() {}.getType());
                        result.addAll(users);
                    }
                });
        request.executeAndWait();
        return result;
    }
}
