package com.example.alvar.chatapp.Model;

public class Messages {

    private String message;
    private String senderID;
    private String type;
    private String receiverID;
    private String messageDate;
    private String messageTime;
    private String nameFile;
    private String messageID;
    private Boolean seen;


    public Messages() {
    }

    public Messages(String message, String senderID, String type, String receiverID, String messageDate, String messageTime, String nameFile, String messageID, Boolean seen) {
        this.message = message;
        this.senderID = senderID;
        this.type = type;
        this.receiverID = receiverID;
        this.messageDate = messageDate;
        this.messageTime = messageTime;
        this.nameFile = nameFile;
        this.messageID = messageID;
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

    public String getNameFile() {
        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public Boolean getSeen() {
        return seen;
    }

    public void setSeen(Boolean seen) {
        this.seen = seen;
    }
}
