package com.chitchat.messaging.chitchatmessaging.models;

public class Message {

    public String content;
    public String media;
    public String type;
    public String timeStamp;
    public String readStatus;

    public Message() {

    }

    public Message(String content, String media, String type, String timeStamp, String readStatus) {

        this.content = content;
        this.media = media;
        this.type = type;
        this.timeStamp = timeStamp;
        this.readStatus = readStatus;
    }
}
