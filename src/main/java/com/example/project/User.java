package com.example.project;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String name;
    private String email;
    private Integer totalPosts;
    private List<String> savedBlogIds;

    public User() {

        this.savedBlogIds = new ArrayList<>();
        this.totalPosts = 0;
    }

    public User(String userId, String name, String email, Integer totalPosts) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.totalPosts = totalPosts != null ? totalPosts : 0;
        this.savedBlogIds = new ArrayList<>();
    }

    public User(String userId, String name, String email, Integer totalPosts, List<String> savedBlogIds) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.totalPosts = totalPosts != null ? totalPosts : 0; // Handle null
        this.savedBlogIds = savedBlogIds != null ? savedBlogIds : new ArrayList<>();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(Integer totalPosts) {
        this.totalPosts = totalPosts;
    }

    public List<String> getSavedBlogIds() {
        return savedBlogIds;
    }

    public void setSavedBlogIds(List<String> savedBlogIds) {
        this.savedBlogIds = savedBlogIds != null ? savedBlogIds : new ArrayList<>();
    }

    // Method to add a blog post ID to savedBlogIds
    public void addSavedBlog(String postId, DatabaseReference userRef) {
        if (postId == null) {
            Log.e("User", "Cannot add null postId to saved blogs.");
            return;
        }
        if (!savedBlogIds.contains(postId)) {
            savedBlogIds.add(postId);
            userRef.child("savedBlogs").child(postId).setValue(true);
        }
    }

    public void removeSavedBlog(String postId, DatabaseReference userRef) {
        if (postId != null && savedBlogIds.remove(postId)) {
            userRef.child("savedBlogs").child(postId).removeValue();
        }
    }

    public boolean isBlogSaved(String postId) {
        return savedBlogIds.contains(postId);
    }
}
