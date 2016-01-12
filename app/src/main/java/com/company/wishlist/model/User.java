package com.company.wishlist.model;

import com.company.wishlist.task.FacebookProfileData;
import com.company.wishlist.util.Utilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by v.odahovskiy on 06.01.2016.
 */
public class User {

    private String id;
    private String firstName;
    private String lastName;
    private String birthday;
    private String gender;

    private String displayName;
    private String provider;

    public User(){}

    public User(String id, String displayName, String provider) {
        this.id = id;
        this.displayName = displayName;
        this.provider = provider;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public static User getFromJSON(JSONObject object) {
        User user = new User();

        try {
            user.setId(object.getString(FacebookProfileData.ID));
            user.setFirstName(object.getString(FacebookProfileData.FIRST_NAME));
            user.setLastName(object.getString(FacebookProfileData.LAST_NAME));
            user.setDisplayName(object.getString(FacebookProfileData.NAME));
            user.setBirthday(object.getString(FacebookProfileData.BIRTHDAY));
            user.setGender(object.getString(FacebookProfileData.GENDER));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return user;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthday='" + birthday + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}
