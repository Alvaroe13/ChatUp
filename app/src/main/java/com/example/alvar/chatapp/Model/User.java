package com.example.alvar.chatapp.Model;

public class User {

    private String name;
    private String email;
    private String password;
    private String status;
    private String image;
    private String imageThumbnail;


    public User(String name, String email, String password, String status, String image, String imageThumbnail) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.status = status;
        this.image = image;
        this.imageThumbnail = imageThumbnail;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getStatus() {
        return status;
    }

    public String getImage() {
        return image;
    }

    public String getImageThumbnail() {
        return imageThumbnail;
    }
}
