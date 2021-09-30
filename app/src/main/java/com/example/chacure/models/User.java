package com.example.chacure.models;

public class User {

    private String id;
    private String email;
    private String userName;
    private String imageProfile;
    private String infoUser;
    private boolean online;
    private long lastConect;
    private long timestamp;

    public User(){}

    public User(String id, String email, String userName, String imageProfile, String infoUser, boolean online, long lastConect, long timestamp) {
        this.id = id;
        this.email = email;
        this.userName = userName;
        this.imageProfile = imageProfile;
        this.infoUser = infoUser;
        this.online = online;
        this.lastConect = lastConect;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getImageProfile() {
        return imageProfile;
    }

    public void setImageProfile(String imageProfile) {
        this.imageProfile = imageProfile;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getInfoUser() {
        return infoUser;
    }

    public void setInfoUser(String infoUser) {
        this.infoUser = infoUser;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public long getLastConect() {
        return lastConect;
    }

    public void setLastConect(long lastConect) {
        this.lastConect = lastConect;
    }
}
