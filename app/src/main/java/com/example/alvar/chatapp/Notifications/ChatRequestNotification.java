package com.example.alvar.chatapp.Notifications;

public class ChatRequestNotification {

    private RequestNotification data;
    private String to;

    public ChatRequestNotification(RequestNotification data, String to) {
        this.data = data;
        this.to = to;
    }

}
