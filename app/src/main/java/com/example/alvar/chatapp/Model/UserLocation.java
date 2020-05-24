package com.example.alvar.chatapp.Model;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class UserLocation  {

    private User user;
    private GeoPoint geo_point;
    private @ServerTimestamp Date timeStamp;

    public UserLocation() {
    }

    public UserLocation(User user, GeoPoint geo_point, Date timeStamp) {
        this.user = user;
        this.geo_point = geo_point;
        this.timeStamp = timeStamp;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public GeoPoint getGeo_point() {
        return geo_point;
    }

    public void setGeo_point(GeoPoint geo_point) {
        this.geo_point = geo_point;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

}
