package com.chitchat.messaging.chitchatmessaging.models;

/**
 * POJO class to store message details
 *
 * @author Sarthak Grover
 */

public class Message {

    public String content;
    public String media;
    public String type;
    public String timeStamp;
    public String readStatus;

    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }

    public Message(String content, String media, String type, String timeStamp, String readStatus) {

        this.content = content;
        this.media = media;
        this.type = type;
        this.timeStamp = timeStamp;
        this.readStatus = readStatus;
    }
}
