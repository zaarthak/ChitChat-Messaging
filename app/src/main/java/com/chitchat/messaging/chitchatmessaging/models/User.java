package com.chitchat.messaging.chitchatmessaging.models;

/**
 * POJO class to store current user login details
 *
 * @author Sarthak Grover
 */

public class User {

    public String username;
    public String email;
    public String status;
    public String image;
    public String thumb_image;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email, String status, String image, String thumb_image) {

        this.username = username;
        this.email = email;
        this.status = status;
        this.image = image;
        this.thumb_image = thumb_image;
    }
}
