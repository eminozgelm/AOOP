package com.example.demo;

public class User {
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String[] profileInfos;
    private int[] friendsArray;
    private int isHidden;

    // Private constructor to enforce the use of builder
    private User(UserBuilder builder) {
        this.username = builder.username;
        this.password = builder.password;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.emailAddress = builder.emailAddress;
        this.profileInfos = builder.profileInfos;
        this.friendsArray = builder.friendsArray;
        this.isHidden = builder.isHidden;
    }

    public int getIsHidden() {
        return isHidden;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public void setProfileInfos(String[] profileInfos) {
        this.profileInfos = profileInfos;
    }

    public void setFriendsArray(int[] friendsArray) {
        this.friendsArray = friendsArray;
    }

    public void setIsHidden(int isHidden) {
        this.isHidden = isHidden;
    }

    public User(String username, String password, String firstName, String lastName, String[] profileInfos, int[] friendsArray) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.profileInfos = profileInfos;
        this.friendsArray = friendsArray;
    }
    // Getters for all fields (can also have setters if needed)

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String[] getProfileInfos() {
        return profileInfos;
    }

    public int[] getFriendsArray() {
        return friendsArray;
    }

    public int isHidden() {
        return isHidden;
    }

    public void setEmailAdress(String email) {
        this.emailAddress = email;
    }

    public void setHidden() {
        this.isHidden = 0;
    }

    // Builder class
    public static class UserBuilder {
        // Required fields
        private String username;
        private String password;

        // Optional fields with default values
        private String firstName = "";
        private String lastName = "";
        private String emailAddress = "";
        private String[] profileInfos = {};
        private int[] friendsArray = {};
        private int isHidden = 0;

        // Constructor with required fields
        public UserBuilder(String username, String password) {
            this.username = username;
            this.password = password;
        }

        // Setter methods for optional fields
        public UserBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserBuilder emailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public UserBuilder profileInfos(String[] profileInfos) {
            this.profileInfos = profileInfos;
            return this;
        }

        public UserBuilder friendsArray(int[] friendsArray) {
            this.friendsArray = friendsArray;
            return this;
        }

        public UserBuilder isHidden(int isHidden) {
            this.isHidden = isHidden;
            return this;
        }

        // Build method to create User object
        public User build() {
            return new User(this);
        }
    }
}
