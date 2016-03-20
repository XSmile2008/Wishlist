package com.company.wishlist.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.firebase.client.Firebase;
import com.firebase.client.annotations.NotNull;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by v.odahovskiy on 06.01.2016.
 */
public class User implements Serializable , Comparable<User>{

    @JsonIgnore@SerializedName("id") private String id;
    @SerializedName("name") private String displayName;
    @SerializedName("first_name") private String firstName;
    @SerializedName("last_name") private String lastName;
    @SerializedName("birthday") private String birthday;
    @SerializedName("gender") private String gender;
    @SerializedName("email") private String email;

    private String provider;

    public User(){}

    public User(String id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonIgnore
    public static Firebase getFirebaseRef() {
        return FirebaseRoot.get().child(User.class.getSimpleName());
    }

    @JsonIgnore
    public String getAvatarURL() {
        return "https://graph.facebook.com/" + id + "/picture?type=large";
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

    @Override
    public int compareTo(@NotNull User another) {
        return getDisplayName().compareToIgnoreCase(another.getDisplayName());
    }
}
