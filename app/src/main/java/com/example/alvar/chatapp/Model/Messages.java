package com.example.alvar.chatapp.Model;

public class Messages {

    private String message;
    private String sender;
    private String type;


    public Messages() {
    }

    public Messages(String message, String sender, String type) {
        this.message = message;
        this.sender = sender;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
