package com.example.alvar.chatapp.Model;

public class User {

    private String name;
    private String email;
    private String password;
    private String status;
    private String image;
    private String imageThumbnail;
    private String token;
    private String userID;

    public User(){

    }

    public User(String name, String email, String password, String status, String image, String imageThumbnail, String token, String userID) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.status = status;
        this.image = image;
        this.imageThumbnail = imageThumbnail;
        this.token = token;
        this.userID = userID;
    }

    /**
     * Getters
     * @return
     */
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

    public String getToken() {
        return token;
    }

    public String getUserID() {
        return userID;
    }

    /**
     * Setters
     * @param name
     */

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setImageThumbnail(String imageThumbnail) {
        this.imageThumbnail = imageThumbnail;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
