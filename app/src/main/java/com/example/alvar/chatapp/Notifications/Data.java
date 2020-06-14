package com.example.alvar.chatapp.Notifications;

public class Data {

    private String recipientUserID;
    private String message;
    private String title;
    private String senderID;

    public Data() {
    }

    public Data(String recipientUserID, String message, String title, String senderID)  {
        this.recipientUserID = recipientUserID;
        this.message = message;
        this.title = title;
        this.senderID = senderID;
    }

    public String getRecipientUserID() {
        return recipientUserID;
    }

    public void setRecipientUserID(String recipientUserID) {
        this.recipientUserID = recipientUserID;
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

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

}
