package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Explore extends AppCompatActivity {

    ImageView home, create, profile;
    FirebaseUser user;
    FirebaseAuth mAuth;

    private RecyclerView recyclerView;
    private blog_adapter blogAdapter;
    private List<BlogPost> blogPosts;       // List for displayed posts
    private List<BlogPost> allBlogPosts;    // Full list for filtering
    private DatabaseReference blogDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_explore);

        recyclerView = findViewById(R.id.recyclerView);
        home = findViewById(R.id.home);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        create = findViewById(R.id.create);
        profile = findViewById(R.id.profile);

        // Set LayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize lists
        blogPosts = new ArrayList<>();
        allBlogPosts = new ArrayList<>();

        // Initialize Database Reference
        blogDatabaseRef = FirebaseDatabase.getInstance().getReference("BlogPosts");

        // Load blog posts from Firebase
        loadBlogPosts();

        // Initialize BlogAdapter and set it to RecyclerView
        blogAdapter = new blog_adapter(this, blogPosts);
        recyclerView.setAdapter(blogAdapter);

        // Set up SearchView for filtering
        SearchView searchView = findViewById(R.id.search_blog);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterPosts(query);  // Filter posts on submit
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPosts(newText);  // Filter posts as text changes
                return true;
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), dashboard.class);
                startActivity(intent);
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddPostActivity.class);
                startActivity(intent);
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Profile.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void signOut() {
        mAuth.signOut();
        Toast.makeText(getApplicationContext(), "Logout successful", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), login.class));
        finish();
    }

    private void loadBlogPosts() {
        blogDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allBlogPosts.clear();  // Clear existing posts
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    BlogPost blogPost = postSnapshot.getValue(BlogPost.class);
                    if (blogPost != null) {
                        allBlogPosts.add(blogPost);  // Add each post to the list
                    }
                }

                // Sort by likes in descending order
                Collections.sort(allBlogPosts, new Comparator<BlogPost>() {
                    @Override
                    public int compare(BlogPost post1, BlogPost post2) {
                        return Integer.compare(post2.getLikeCount(), post1.getLikeCount());
                    }
                });

                // Update blogPosts with sorted list and refresh RecyclerView
                blogPosts.clear();
                blogPosts.addAll(allBlogPosts);
                blogAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Explore.this, "Failed to load blog posts: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterPosts(String query) {
        blogPosts.clear();
        for (BlogPost post : allBlogPosts) {
            // Check if the heading, author name, or genre contains the query string (case insensitive)
            if (post.getHeading().toLowerCase().contains(query.toLowerCase()) ||
                    post.getAuthorName().toLowerCase().contains(query.toLowerCase()) ||
                    post.getGenre().toLowerCase().contains(query.toLowerCase())) {  // Added genre check
                blogPosts.add(post);  // Add posts that match query
            }
        }
        blogAdapter.notifyDataSetChanged();  // Refresh RecyclerView
    }
}
