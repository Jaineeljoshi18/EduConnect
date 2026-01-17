package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity {

    private TextView heading, authorNameView, date, genre, content;
    private ImageView ivBack, post_image_view;
    private RecyclerView comments; // RecyclerView for comments
    private EditText commentText;
    private Button commentSubmit;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference commentReference;
    private DatabaseReference postReference; // Reference for the post
    private FirebaseDatabase firebaseDatabase;

    private List<Comment> listComment; // List to hold comments
    private CommentsAdapter commentAdapter; // Adapter for RecyclerView

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        FirebaseApp.initializeApp(this);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        post_image_view= findViewById(R.id.post_image_view);

        // Initialize views
        heading = findViewById(R.id.heading);
        authorNameView = findViewById(R.id.tv_name);
        date = findViewById(R.id.date);
        genre = findViewById(R.id.genre);
        content = findViewById(R.id.post);
        ivBack = findViewById(R.id.go_back);
        comments = findViewById(R.id.comments); // RecyclerView for comments
        commentText = findViewById(R.id.commentText);
        commentSubmit = findViewById(R.id.comment_submit);

        boolean scrollToComments = getIntent().getBooleanExtra("scrollToComments", false);
        if (scrollToComments) {
            scrollToCommentSection();
        }

        // Retrieve post details from Intent
        retrievePostDetails();

        // Initialize RecyclerView for comments
        loadComments();

        // Submit comment button listener
        setupSubmitCommentListener();

        // Back button listener
        ivBack.setOnClickListener(view -> finish());
    }

    private void retrievePostDetails() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String authorName = intent.getStringExtra("authorName");
        String genreStr = intent.getStringExtra("genre");
        String contentStr = intent.getStringExtra("content");
        String postId = intent.getStringExtra("PostId");
        String imageUrl = intent.getStringExtra("imageUrl");
        String dateStr = intent.getStringExtra("date");

        if (title != null && authorName != null && genreStr != null && contentStr != null && postId != null) {
            heading.setText(title);
            authorNameView.setText(authorName);
            genre.setText(genreStr);
            content.setText(contentStr);
            date.setText(dateStr);

            // Set the commentReference with the correct postId
            commentReference = firebaseDatabase.getReference("Comment").child(postId);
            postReference = firebaseDatabase.getReference("BlogPosts").child(postId); // Initialize postReference

            // Load the image only if the URL is available
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).into(post_image_view);
            } else {
                // Optionally, set a placeholder or hide the ImageView if no image is available
                post_image_view.setVisibility(View.GONE);
            }
        } else {
            Toast.makeText(this, "Error retrieving post details.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadComments() {
        // Set up RecyclerView properties
        comments.setLayoutManager(new LinearLayoutManager(this));

        if (commentReference != null) {
            commentReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    listComment = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Comment comment = snapshot.getValue(Comment.class);
                        if (comment != null) {
                            comment.setCommentId(snapshot.getKey());  // Set commentId with Firebase key
                            listComment.add(comment); // Add comment to the list
                        }
                    }

                    // Pass the current user's ID to the adapter
                    commentAdapter = new CommentsAdapter(PostDetailActivity.this, listComment, firebaseUser.getUid());
                    comments.setAdapter(commentAdapter); // Set adapter to RecyclerView
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("PostDetailActivity", "Failed to load comments: " + error.getMessage());
                }
            });
        }
    }

    private void setupSubmitCommentListener() {
        commentSubmit.setOnClickListener(view -> {
            String commentContent = commentText.getText().toString().trim();
            if (commentContent.isEmpty()) {
                showMessage("Comment cannot be empty");
                return;
            }

            if (firebaseUser == null) {
                showMessage("You must be logged in to add a comment.");
                return;
            }

            String uid = firebaseUser.getUid();
            String readerName = firebaseUser.getDisplayName();
            Comment comment = new Comment(commentContent, uid, readerName);

            commentReference.push().setValue(comment)
                    .addOnSuccessListener(unused -> {
                        showMessage("Comment Added");
                        commentText.setText("");
                        incrementCommentCount(postReference); // Increment comment count
                    })
                    .addOnFailureListener(e -> showMessage("Failed to add Comment: " + e.getMessage()));
        });
    }

    public void onDeleteClicked(Comment comment) {
        if (firebaseUser != null && firebaseUser.getUid().equals(comment.getUid())) {  // Check if the current user is the comment author
            commentReference.child(comment.getCommentId()).removeValue()
                    .addOnSuccessListener(unused -> {
                        showMessage("Comment deleted successfully");
                        decrementCommentCount(postReference);
                    })
                    .addOnFailureListener(e -> showMessage("Failed to delete comment: " + e.getMessage()));
        } else {
            showMessage("You can only delete your own comments.");
        }
    }

    private void scrollToCommentSection() {
        ScrollView scrollView = findViewById(R.id.scrollView); // Replace with your ScrollView ID
        View commentSection = findViewById(R.id.commentSection); // Replace with the comment section ID

        scrollView.post(() -> scrollView.smoothScrollTo(0, commentSection.getTop()));
    }

    private void incrementCommentCount(DatabaseReference postRef) {
        postRef.child("commentCount").setValue(ServerValue.increment(1));
    }

    private void decrementCommentCount(DatabaseReference postRef) {
        postRef.child("commentCount").setValue(ServerValue.increment(-1));
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
