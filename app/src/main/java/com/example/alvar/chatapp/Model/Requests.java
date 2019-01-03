package com.example.alvar.chatapp.Model;

public class Requests {

    private String name, imageThumbnail;

    public Requests(){
    }


    public Requests(String name, String imageThumbnail) {
        this.name = name;
        this.imageThumbnail = imageThumbnail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageThumbnail() {
        return imageThumbnail;
    }

    public void setImageThumbnail(String imageThumbnail) {
        this.imageThumbnail = imageThumbnail;
    }

}
