package com.example.demo;

public class User {
    private String username;
    private String password;

    private String firstName;

    private String lastName;

    private String emailAdress;

    private String[] profileInfos;

    private int[] friendsArray;

    boolean isHidden;

    public User(String username, String password, String firstName, String lastName, String[] profileInfos, int[] friendsArray) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profileInfos = profileInfos;
        this.friendsArray = friendsArray;
    }

    public String getEmailAdress() {
        return emailAdress;
    }

    public void setEmailAdress(String emailAdress) {
        this.emailAdress = emailAdress;
    }


    public String[] getProfileInfos() {
        return profileInfos;
    }

    public void setProfileInfos(String[] profileInfos) {
        this.profileInfos = profileInfos;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public int[] getFriendsArray() {
        return friendsArray;
    }

    public void setFriendsArray(int[] friendsArray) {
        this.friendsArray = friendsArray;
    }
}