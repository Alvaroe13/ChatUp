package com.example.alvar.chatapp;

import android.app.Application;

import com.example.alvar.chatapp.Model.User;

public class UserClient extends Application {

    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}