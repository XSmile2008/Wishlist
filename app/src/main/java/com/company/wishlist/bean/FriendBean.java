package com.company.wishlist.bean;

import com.company.wishlist.model.User;
import com.company.wishlist.task.FacebookProfileData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by v.odahovskiy on 08.01.2016.
 */
public class FriendBean {

    private String id;
    private String name;
    private String imageUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public static FriendBean getFromJSON(JSONObject object) {
        FriendBean user = new FriendBean();

        try {
            user.setId(object.getString(FacebookProfileData.ID));
            user.setName(object.getString(FacebookProfileData.NAME));
        } catch (JSONException e) {
            user = null;
        }

        return user;
    }
}
