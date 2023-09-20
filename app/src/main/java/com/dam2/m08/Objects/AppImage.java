package com.dam2.m08.Objects;

import android.graphics.Bitmap;
import com.dam2.m08.Utils;
import com.google.android.gms.maps.model.LatLng;

import java.time.LocalDateTime;

public class AppImage {
    private String id;
    private Bitmap image;
    private Bitmap thumbnail;
    private LatLng location;
    private LocalDateTime date;
    private String user;

    public AppImage() {}

    public AppImage(Bitmap image, double latitude, double longitude, LocalDateTime date, String user) {
        this.image = image;
        thumbnail = Utils.getThumbnail(image);
        location = new LatLng(latitude, longitude);
        this.date = date;
        this.user = user;
    }

    public AppImage(String id, Bitmap image, /*Bitmap thumbnail,*/ double latitude, double longitude, LocalDateTime date, String user) {
        this.id = id;
        this.image = image;
        this.thumbnail = Utils.getThumbnail(image);//thumbnail;
        location = new LatLng(latitude, longitude);
        this.date = date;
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Bitmap getImage() {
        return image;
    }

    public byte[] getImageBytes() {
        return Utils.getByteArray(image);
    }

    public String getImageString() {
        return Utils.getString(image);
    }

    public void setImage(Bitmap image) {
        this.image = image;
        thumbnail = Utils.getThumbnail(image);
    }

    public void setImage(String image) {
        this.image = Utils.getBitmap(image);
        thumbnail = Utils.getThumbnail(this.image);
    }

    public void setImage(byte[] image) {
        this.image = Utils.getBitmap(image);
        thumbnail = Utils.getThumbnail(this.image);
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public byte[] getThumbnailBytes() {
        return Utils.getByteArray(thumbnail);
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setLocation(double latitude, double longitude) {
        this.location = new LatLng(latitude, longitude);
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
