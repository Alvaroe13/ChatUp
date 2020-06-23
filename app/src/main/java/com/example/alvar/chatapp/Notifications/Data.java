package com.example.alvar.chatapp.Notifications;

public class Data {

    private String recipientUserID;
    private String message;
    private String senderUsername;
    private String senderID;
    private String messageID;
    private String senderPhoto;

    public Data() {
    }

    public Data(String recipientUserID, String message, String senderUsername, String senderID, String messageID, String senderPhoto)  {
        this.recipientUserID = recipientUserID;
        this.message = message;
        this.senderUsername = senderUsername;
        this.senderID = senderID;
        this.messageID = messageID;
        this.senderPhoto = senderPhoto;
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

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getSenderPhoto() {
        return senderPhoto;
    }

    public void setSenderPhoto(String senderPhoto) {
        this.senderPhoto = senderPhoto;
    }
}
