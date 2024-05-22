package com.example.demo;

public abstract class Post {
    public abstract void display();
    public String text;
    public int groupID;
    public int ownerID;

    public Post(){
        this.ownerID = ownerID;
        this.text = text;

    }
    }


class RegularPost extends Post {
    public RegularPost(int ownerID, String text, int groupID) {
        this.ownerID = ownerID;
        this.text = text;
        this.groupID = -1;
    }
    @Override
    public void display() {
        System.out.println("Displaying a Regular Post");
    }
}

class GroupPost extends Post {

    public GroupPost(int ownerID, String text, int groupID) {
        this.ownerID = ownerID;
        this.text = text;
        this.groupID = groupID;
    }

    @Override
    public void display() {
        System.out.println("Displaying a Group Post");
    }
    public int getGroupID() {
        return this.groupID;
    }
    public void setGroupID(String groupName) {
        this.groupID = groupID;
    }
    public int getOwnerId() {
        return ownerID;
    }

    public void setOwnerId(int ownerId) {
        this.ownerID = ownerId;
    }

    public String getText(){
        return this.text;
    }

}
