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

public class Saved extends AppCompatActivity {

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


        myBlogsTextView = findViewById(R.id.my_blogs);
        savedTextView = findViewById(R.id.saved);
        recyclerView = findViewById(R.id.recyclerView);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        posts = findViewById(R.id.posts);
        home = findViewById(R.id.home);
        iv1 = findViewById(R.id.iv1);
        explore = findViewById(R.id.explore);
        create = findViewById(R.id.create);

        profile = findViewById(R.id.profile);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        blogPosts = new ArrayList<>();
        blogAdapter = new blog_adapter(this, blogPosts);
        recyclerView.setAdapter(blogAdapter);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String currentUserId = user.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
            loadUserData();  // Load user data which includes email
        } else {
            Log.e("ProfileError", "User is not logged in.");
        }

        savedTextView.setTextColor(Color.parseColor("#FF6F3C"));
        savedTextView.setBackgroundColor(Color.parseColor("#FFDBCE"));

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

                    loadSavedBlogs();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("LoadUserDataError", "Failed to load user data: " + error.getMessage());
            }
        });
    }

    private void loadSavedBlogs() {
        blogPosts.clear();
        userRef.child("savedBlogs").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot savedBlogSnapshot : snapshot.getChildren()) {
                        String savedPostId = savedBlogSnapshot.getKey();

                        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("BlogPosts").child(savedPostId);

                        postRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot postSnapshot) {
                                BlogPost blogPost = postSnapshot.getValue(BlogPost.class);
                                if (blogPost != null) {
                                    blogPosts.add(blogPost); // Add each blog post to the list
                                    blogAdapter.notifyDataSetChanged(); // Update adapter
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Log.e("LoadSavedBlogError", "Failed to load blog: " + error.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("LoadSavedBlogsError", "Failed to load saved blogs: " + error.getMessage());
            }
        });
    }

    private void setClickListeners() {
        iv1.setOnClickListener(view -> signOut());
        myBlogsTextView.setOnClickListener(v -> {
            myBlogsTextView.setBackgroundColor(Color.parseColor("#FFDBCE"));
            myBlogsTextView.setTextColor(Color.parseColor("#FF6F3C"));
            savedTextView.setTextColor(Color.BLACK);
            savedTextView.setBackgroundColor(Color.WHITE);
            Intent intent = new Intent(getApplicationContext(), MyBlogs.class);
            startActivity(intent);
        });

        savedTextView.setOnClickListener(v -> {
            savedTextView.setBackgroundColor(Color.parseColor("#FFDBCE"));
            savedTextView.setTextColor(Color.parseColor("#FF6F3C"));
            myBlogsTextView.setTextColor(Color.BLACK);
            myBlogsTextView.setBackgroundColor(Color.WHITE);

        });


        home.setOnClickListener(v -> {
            startActivity(new Intent(Saved.this, dashboard.class));
            finish();
        });

        explore.setOnClickListener(v -> {
            startActivity(new Intent(Saved.this, Explore.class));
            finish();
        });

        create.setOnClickListener(v -> {
            startActivity(new Intent(Saved.this, AddPostActivity.class));
            finish();
        });

        profile.setOnClickListener(v -> {

        });

    }
    private void signOut() {
        mAuth.signOut();
        Toast.makeText(getApplicationContext(), "Logout successful", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), login.class));
        finish();
    }
}
