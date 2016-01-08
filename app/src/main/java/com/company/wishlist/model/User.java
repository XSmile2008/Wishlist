package com.company.wishlist.model;

import com.company.wishlist.task.FacebookProfileData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by v.odahovskiy on 06.01.2016.
 */
public class User {

    public enum Gender{male, female}

    private String id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String birthday;
    private Gender gender;

    private List<User> friends = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = Gender.valueOf(gender);
    }

    public static User getFromJSON(JSONObject object) {
        User user = new User();

        try {
            user.setId(object.getString(FacebookProfileData.ID));
            user.setFirstName(object.getString(FacebookProfileData.FIRST_NAME));
            user.setLastName(object.getString(FacebookProfileData.LAST_NAME));
            user.setFullName(object.getString(FacebookProfileData.NAME));
            user.setBirthday(object.getString(FacebookProfileData.BIRTHDAY));
            user.setGender(object.getString(FacebookProfileData.GENDER));
        } catch (JSONException e) {
            user = null;
        }

        return user;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", birthday='" + birthday + '\'' +
                ", gender='" + gender + '\'' +
                '}';
    }
}
