package com.example.alvar.chatapp.Model;

public class Messages {

    private String message;
    private String senderID;
    private String type;
    private String receiverID;
    private String messageDate;
    private String messageTime;
    private Boolean seen;


    public Messages() {
    }


    public Messages(String message, String senderID, String receiverID, String type, String messageDate ,String messageTime ,Boolean seen) {
        this.message = message;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.messageDate = messageDate;
        this.messageTime = messageTime;
        this.type = type;
        this.seen = seen;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
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

    public String getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(String receiverID) {
        this.receiverID = receiverID;
    }

    public String getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(String messageDate) {
        this.messageDate = messageDate;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(String messageTime) {
        this.messageTime = messageTime;
    }
}
