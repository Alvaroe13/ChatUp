package com.example.alvar.chatapp.Model;

public class Contacts {

    private String name, status, image, imageThumbnail;

    public Contacts(){

    }

    public Contacts(String name, String status, String image, String imageThumbnail) {
        this.name = name;
        this.status = status;
        this.image = image;
        this.imageThumbnail = imageThumbnail;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageThumbnail() {
        return imageThumbnail;
    }

    public void setImageThumbnail(String imageThumbnail) {
        this.imageThumbnail = imageThumbnail;
    }


}
