package com.example.alvar.chatapp.Notifications;

public class ResponseFCM {

    private String success;

    public ResponseFCM(){

    }

    public ResponseFCM(String success) {
        this.success = success;
    }

    public String getSuccess() {
        return success;
    }
}
