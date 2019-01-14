package com.example.alvar.chatapp.Model;

public class Messages {

    private String message, senderByID, type;
    private Boolean seen;


    public Messages() {
    }

    public Messages(String message, String senderByID, String type, Boolean seen) {
        this.message = message;
        this.senderByID = senderByID;
        this.type = type;
        this.seen = seen;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderByID() {
        return senderByID;
    }

    public void setSenderByID(String senderByID) {
        this.senderByID = senderByID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }
}
