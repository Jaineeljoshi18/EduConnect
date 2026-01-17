package com.example.project;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyBlogs extends AppCompatActivity {

    private TextView myBlogsTextView, savedTextView, name, email, posts;
    private ImageView home, explore, create, profile,iv1;
    private RecyclerView recyclerView;
    private blog_adapter blogAdapter;
    private List<BlogPost> blogPosts;
    private DatabaseReference userRef;
    private FirebaseUser user;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize views
        myBlogsTextView = findViewById(R.id.my_blogs);
        savedTextView = findViewById(R.id.saved);
        recyclerView = findViewById(R.id.recyclerView);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        posts = findViewById(R.id.posts);
        home = findViewById(R.id.home);
        explore = findViewById(R.id.explore);
        create = findViewById(R.id.create);

        iv1 = findViewById(R.id.iv1);
        profile = findViewById(R.id.profile);

        // Set up RecyclerView and adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        blogPosts = new ArrayList<>();
        blogAdapter = new blog_adapter(this, blogPosts);
        recyclerView.setAdapter(blogAdapter);

        // Get current user and load data
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            loadUserData();  // Load user data which includes email
        } else {
            Log.e("ProfileError", "User is not logged in.");
        }

        myBlogsTextView.setTextColor(Color.parseColor("#FF6F3C")); // Orange color
        myBlogsTextView.setBackgroundColor(Color.parseColor("#FFDBCE")); // Orange color
        // Set click listeners for navigation
        setClickListeners();
        initializeFirebase();
    }

    private void initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this);
        } catch (Exception e) {
            Log.e("Firebase", "Firebase initialization error: " + e.getMessage());
        }
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }


    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userName = snapshot.child("name").getValue(String.class);
                    String userEmail = snapshot.child("email").getValue(String.class);
                    Integer totalPosts = snapshot.child("totalPosts").getValue(Integer.class);

                    name.setText(userName);
                    email.setText(userEmail);
                    posts.setText(String.valueOf(totalPosts != null ? totalPosts : 0));

                    // After loading user data, load the user's blogs
                    loadUserBlogs(userEmail);  // Pass the user email to load blogs
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("LoadUserDataError", "Failed to load user data: " + error.getMessage());
            }
        });
    }

    private void loadUserBlogs(String userEmail) {
        DatabaseReference blogsRef = FirebaseDatabase.getInstance().getReference("BlogPosts");

        // Load blogs by email
        blogsRef.orderByChild("uEmail").equalTo(userEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        blogPosts.clear();  // Clear existing posts
                        for (DataSnapshot blogSnapshot : snapshot.getChildren()) {
                            BlogPost blogPost = blogSnapshot.getValue(BlogPost.class);
                            if (blogPost != null) {
                                blogPosts.add(blogPost);
                            }
                        }
                        blogAdapter.notifyDataSetChanged();  // Notify the adapter to refresh the list
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("LoadUserBlogsError", "Failed to load user blogs: " + error.getMessage());
                    }
                });
    }

    private void setClickListeners() {
        iv1.setOnClickListener(view -> signOut());
        myBlogsTextView.setOnClickListener(v ->
        {
            myBlogsTextView.setBackgroundColor(Color.parseColor("#FFDBCE"));
            myBlogsTextView.setTextColor(Color.parseColor("#FF6F3C")); // Set to orange
            savedTextView.setTextColor(Color.BLACK);
            savedTextView.setBackgroundColor(Color.WHITE);
            loadUserBlogs(email.getText().toString().trim());
        });

        savedTextView.setOnClickListener(v -> {
            savedTextView.setBackgroundColor(Color.parseColor("#FFDBCE"));
            savedTextView.setTextColor(Color.parseColor("#FF6F3C")); // Set to orange
            myBlogsTextView.setTextColor(Color.BLACK);
            myBlogsTextView.setBackgroundColor(Color.WHITE);
            Intent intent = new Intent(getApplicationContext(), Saved.class);
            startActivity(intent);
        });

        // Bottom navigation listeners
        home.setOnClickListener(v -> {
            startActivity(new Intent(MyBlogs.this, dashboard.class));
            finish();
        });

        explore.setOnClickListener(v -> {
            startActivity(new Intent(MyBlogs.this, Explore.class));
            finish();
        });

        create.setOnClickListener(v -> {
            startActivity(new Intent(MyBlogs.this, AddPostActivity.class));
            finish();
        });

        profile.setOnClickListener(v -> {
            // Stay on profile page
        });
    }
    private void signOut() {
        mAuth.signOut();
        Toast.makeText(getApplicationContext(), "Logout successful", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), login.class));
        finish();
    }
}
