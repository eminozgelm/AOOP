package com.example.demo;

import java.util.ArrayList;
import java.util.List;

public class UserManager {
    // Method to search for users
    public List<User> searchUsers(List<User> usersList, String query, User searcher) {
        List<User> searchResults = new ArrayList<>();

        for (User user : usersList) {
            // Check if the user matches the search query and is not hidden
            if (userMatchesQuery(user, query) && !user.isHidden()) {
                // If the searcher is friends with the user or the user is the searcher themselves, include them in the search results
                if (areFriends(user, searcher) || user.getUsername().equals(searcher.getUsername())) {
                    searchResults.add(user);
                }
            }
        }

        return searchResults;
    }

    // Method to check if two users are friends
    private boolean areFriends(User user1, User user2) {
        ArrayList<Integer> user1Friends = user1.getFriendsArray();
        ArrayList<Integer> user2Friends= user2.getFriendsArray();

        for (int id1 : user1Friends) {
            for (int id2 : user2Friends) {
                if (id1 == id2) {
                    return true; // Users are friends
                }
            }
        }

        return false; // Users are not friends
    }

    // Method to check if a user matches the search query
    private boolean userMatchesQuery(User user, String query) {
        return user.getUsername().toLowerCase().contains(query.toLowerCase());
    }
}
