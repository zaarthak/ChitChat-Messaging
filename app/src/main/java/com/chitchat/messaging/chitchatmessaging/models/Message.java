package com.chitchat.messaging.chitchatmessaging.models;

public class Message {

    public String content;
    public String type;
    public String timeStamp;
    public String readStatus;

    public Message() {

    }

    public Message(String content, String type, String timeStamp, String readStatus) {

        this.content = content;
        this.type = type;
        this.timeStamp = timeStamp;
        this.readStatus = readStatus;
    }
}
