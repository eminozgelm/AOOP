package com.example.demo;

public class Group {
    private String groupName;
    private int[] usersArray;
    private int groupID;

    private int[] groupAdmins;
    public Group(String groupName, int[] usersArray, int[] groupAdmins) {
        this.groupName = groupName;
        this.usersArray = usersArray;
        this.groupAdmins = groupAdmins;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int[] getUsersArray() {
        return usersArray;
    }

    public void setUsersArray(int[] usersArray) {
        this.usersArray = usersArray;
    }

    public int[] getGroupAdmins() {
        return groupAdmins;
    }

    public void setGroupAdmins(int[] groupAdmins) {
        this.groupAdmins = groupAdmins;
    }

    public void setGroupID(int groupID) {
        this.groupID = groupID;
    }

    public int getGroupID() {
        return groupID;
    }
}
