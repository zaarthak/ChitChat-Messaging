package com.chitchat.messaging.chitchatmessaging.models;

/**
 * POJO class to store user details
 *
 * @author Sarthak Grover
 */

public class User {

    public String username;
    public String email;
    public long phone;
    public String status;
    public String image;
    public String thumb_image;
    public String deviceToken;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String status, String image, String thumb_image, String deviceToken) {
        // Constructor without 'phone' field for google sign-in, since google default_profile_pic_drawable details does not contain user phone number.
        this.username = username;
        this.email = email;
        this.status = status;
        this.image = image;
        this.thumb_image = thumb_image;
        this.deviceToken = deviceToken;
    }

    public User(String username, String email, long phone, String status, String image, String thumb_image, String deviceToken) {

        this.username = username;
        this.email = email;
        this.phone = phone;
        this.status = status;
        this.image = image;
        this.thumb_image = thumb_image;
        this.deviceToken = deviceToken;
    }
}
