package com.example.demo;

public class GroupPost {

    private String groupName;
    private int ownerId;
    private String text;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public GroupPost(String groupName, int ownerId, String text) {
        this.groupName = groupName;
        this.ownerId = ownerId;
        this.text = text;
    }



    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

