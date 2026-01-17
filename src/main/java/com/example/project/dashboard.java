package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

public class dashboard extends AppCompatActivity {

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    // UI elements
    private ImageView iv1, explore, create,profile;

    // RecyclerView and Adapter for displaying blog posts
    private RecyclerView recyclerView;
    private blog_adapter blogAdapter;
    private List<BlogPost> blogPostList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        profile = findViewById(R.id.profile);


        // Initialize Firebase
        initializeFirebase();

        // Initialize UI components
        initUIComponents();

        // Set up RecyclerView
        setupRecyclerView();

        // Load blog posts from Firebase
        fetchBlogPostsFromFirebase();

        // Set up button click listeners
        setupButtonClickListeners();


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

    private void initUIComponents() {
        iv1 = findViewById(R.id.iv1);
        explore = findViewById(R.id.explore);
        create = findViewById(R.id.create);
        recyclerView = findViewById(R.id.recyclerView);
    }

    private void setupRecyclerView() {
        blogPostList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        blogAdapter = new blog_adapter(this, blogPostList);
        recyclerView.setAdapter(blogAdapter);
    }

    private void fetchBlogPostsFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BlogPosts");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                blogPostList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    BlogPost blogPost = ds.getValue(BlogPost.class);
                    if (blogPost != null) {
                        blogPostList.add(blogPost);
                    }
                }
                // Notify the adapter about data changes
                blogAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(dashboard.this, "Failed to load data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupButtonClickListeners() {

        explore.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Explore.class);
            startActivity(intent);
        });
        create.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), AddPostActivity.class);
            startActivity(intent);
        });
        profile.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Profile.class);
            startActivity(intent);
        });

    }



}
