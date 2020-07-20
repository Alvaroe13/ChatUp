package com.example.alvar.chatapp.Notifications;

public class RequestNotification {

    private String title;
    private String message;
    private String contactID;


    public RequestNotification(String title, String message, String contactID ) {
        this.message = message;
        this.title = title;
        this.contactID = contactID;
    }

    public String getContactID() {
        return contactID;
    }

    public void setContactID(String contactID) {
        this.contactID = contactID;
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
