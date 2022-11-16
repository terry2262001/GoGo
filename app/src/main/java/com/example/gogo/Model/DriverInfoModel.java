package com.example.gogo.Model;

public class DriverInfoModel {
    String imageURL;
    String bio;
    String fullname;
    String id;
    String username;

    public DriverInfoModel() {
    }

    public DriverInfoModel(String imageURL, String bio, String fullname, String id, String username) {
        this.imageURL = imageURL;
        this.bio = bio;
        this.fullname = fullname;
        this.id = id;
        this.username = username;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
