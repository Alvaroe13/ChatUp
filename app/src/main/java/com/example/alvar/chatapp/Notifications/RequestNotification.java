package com.example.alvar.chatapp.Notifications;

public class RequestNotification {

    private String title;
    private String message;


    public RequestNotification(String title, String message ) {
        this.message = message;
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
