package com.example.demo;

public class PostFactory {
    public static Post getPost(int ownerID,String text,int groupID){
        Post post;
        if(groupID != -1){
            post = new GroupPost(ownerID,text,groupID);
        }
        else {
            post = new RegularPost(ownerID,text,-1);
        }
        return post;
    }
}