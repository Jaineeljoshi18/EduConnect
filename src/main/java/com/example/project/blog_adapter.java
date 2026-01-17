package com.example.project;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class blog_adapter extends RecyclerView.Adapter<blog_adapter.BlogViewHolder> {

    private Context context;
    private List<BlogPost> blogPosts;
    private FirebaseUser user;


    public blog_adapter(Context context, List<BlogPost> blogPosts) {
        this.context = context;
        this.blogPosts = blogPosts;
        this.user = FirebaseAuth.getInstance().getCurrentUser();

    }

    @NonNull
    @Override
    public BlogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.blog_item, parent, false);
        return new BlogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogViewHolder holder, int position) {
        BlogPost blogPost = blogPosts.get(position);


        if (user != null && user.getUid().equals(blogPost.getUid())) {
            holder.deleteButton.setVisibility(View.VISIBLE);
            Log.d("DeleteButton", "Visible for post: " + blogPost.getId());
        } else {
            holder.deleteButton.setVisibility(View.GONE);
            Log.d("DeleteButton", "Not visible for post: " + blogPost.getId());
        }

        holder.heading.setText(blogPost.getHeading());
        holder.authorName.setText(blogPost.getAuthorName());
        holder.genre.setText(blogPost.getGenre());
        holder.content.setText(blogPost.getContent());
        holder.likeCount.setText(String.valueOf(blogPost.getLikeCount()));
        holder.saveCount.setText(String.valueOf(blogPost.getSaveCount()));
        holder.date.setText(blogPost.getDate());

        holder.profile.setImageResource(R.drawable.img_2);



        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("BlogPosts").child(blogPost.getId());
        if (user != null) {
            DatabaseReference likedByRef = postRef.child("likedBy").child(user.getUid());
            likedByRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isLiked = snapshot.exists();
                    blogPost.setLiked(isLiked);
                    holder.likeButton.setImageResource(isLiked ? R.drawable.liked : R.drawable.like);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("LikeError", "Failed to check like status: " + error.getMessage());
                }
            });

            DatabaseReference savedByRef = postRef.child("savedBy").child(user.getUid());
            savedByRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean isSaved = snapshot.exists();
                    blogPost.setSaved(isSaved);
                    holder.saveButton.setImageResource(isSaved ? R.drawable.saved : R.drawable.save);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("SaveError", "Failed to check save status: " + error.getMessage());
                }
            });
        }

        postRef.child("commentCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long commentCount = snapshot.getValue(Long.class);
                holder.commentCount.setText(String.valueOf(commentCount != null ? commentCount : 0));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CommentCountError", "Failed to load comment count: " + error.getMessage());
            }
        });

        holder.likeButton.setOnClickListener(v -> {
            if (user == null) {
                Toast.makeText(context, "Please log in to like posts", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference likedByRef = postRef.child("likedBy").child(user.getUid());

            likedByRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        likedByRef.removeValue();
                        blogPost.setLiked(false);
                        blogPost.setLikeCount(blogPost.getLikeCount() - 1);
                        postRef.child("likeCount").setValue(blogPost.getLikeCount());
                        holder.likeButton.setImageResource(R.drawable.like);
                    } else {
                        likedByRef.setValue(true);
                        blogPost.setLiked(true);
                        blogPost.setLikeCount(blogPost.getLikeCount() + 1);
                        postRef.child("likeCount").setValue(blogPost.getLikeCount());
                        holder.likeButton.setImageResource(R.drawable.liked);
                    }
                    holder.likeCount.setText(String.valueOf(blogPost.getLikeCount()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("LikeError", "Failed to update like status: " + error.getMessage());
                }
            });
        });

        holder.saveButton.setOnClickListener(v -> {
            if (user == null) {
                Toast.makeText(context, "Please log in to save posts", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference userSavedBlogsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("savedBlogs")
                    .child(blogPost.getId()); // Reference to save the blog ID

            DatabaseReference savedByRef = postRef.child("savedBy").child(user.getUid());

            savedByRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        savedByRef.removeValue();
                        userSavedBlogsRef.removeValue();
                        blogPost.setSaved(false);
                        blogPost.setSaveCount(blogPost.getSaveCount() - 1);
                        postRef.child("saveCount").setValue(blogPost.getSaveCount());
                        holder.saveButton.setImageResource(R.drawable.save);
                    } else {
                        savedByRef.setValue(true);
                        userSavedBlogsRef.setValue(true);
                        blogPost.setSaved(true);
                        blogPost.setSaveCount(blogPost.getSaveCount() + 1);
                        postRef.child("saveCount").setValue(blogPost.getSaveCount());
                        holder.saveButton.setImageResource(R.drawable.saved);
                    }
                    holder.saveCount.setText(String.valueOf(blogPost.getSaveCount()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("SaveError", "Failed to update save status: " + error.getMessage());
                }
            });
        });

        holder.see_more.setOnClickListener(view -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("title", blogPost.getHeading());
            intent.putExtra("content", blogPost.getContent());
            intent.putExtra("authorName", blogPost.getAuthorName());
            intent.putExtra("genre", blogPost.getGenre());
            intent.putExtra("PostId", blogPost.getId());
            intent.putExtra("imageUrl", blogPost.getImageUrl());
            intent.putExtra("date", blogPost.getDate());
            intent.putExtra("scrollToComments", false);
            context.startActivity(intent);
        });

        holder.commentButton.setOnClickListener(view -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("title", blogPost.getHeading());
            intent.putExtra("content", blogPost.getContent());
            intent.putExtra("authorName", blogPost.getAuthorName());
            intent.putExtra("genre", blogPost.getGenre());
            intent.putExtra("PostId", blogPost.getId());
            intent.putExtra("imageUrl", blogPost.getImageUrl());
            intent.putExtra("date", blogPost.getDate());
            intent.putExtra("scrollToComments", true);
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (user != null && user.getUid().equals(blogPost.getUid())) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Blog Post")
                        .setMessage("Are you sure you want to delete this post?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            deleteBlog(blogPost, position);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            } else {
                Toast.makeText(context, "You can only delete your own posts.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteBlog(BlogPost blogPost, int position) {
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("BlogPosts").child(blogPost.getId());
        postRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(blogPost.getUid());
                userRef.child("totalPosts").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Long totalPosts = snapshot.getValue(Long.class);
                        if (totalPosts != null && totalPosts > 0) {
                            userRef.child("totalPosts").setValue(totalPosts - 1).addOnCompleteListener(updateTask -> {
                                if (updateTask.isSuccessful()) {
                                    Log.d("TotalPostsUpdate", "Total posts count decremented successfully.");
                                } else {
                                    Log.e("TotalPostsUpdateError", "Failed to decrement total posts count.");
                                }
                            });
                        } else {
                            Log.d("TotalPostsUpdate", "Total posts count is already zero or missing.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("TotalPostsError", "Failed to access total posts count: " + error.getMessage());
                    }
                });

                blogPosts.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Blog deleted successfully.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete blog.", Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    public int getItemCount() {
        return blogPosts != null ? blogPosts.size() : 0;
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        public ImageView profile;
        TextView heading, authorName, genre, date, content, likeCount, commentCount, saveCount, see_more;
        ImageButton likeButton, commentButton, saveButton, deleteButton;
        CardView cardView;

        public BlogViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            profile = itemView.findViewById(R.id.iv_image);
            heading = itemView.findViewById(R.id.heading);
            authorName = itemView.findViewById(R.id.tv_name);
            genre = itemView.findViewById(R.id.genre);
            date = itemView.findViewById(R.id.date);
            content = itemView.findViewById(R.id.post);
            likeCount = itemView.findViewById(R.id.likeCount);
            commentCount = itemView.findViewById(R.id.commentCount);
            saveCount = itemView.findViewById(R.id.saveCount);
            likeButton = itemView.findViewById(R.id.like);
            commentButton = itemView.findViewById(R.id.comm);
            saveButton = itemView.findViewById(R.id.save);
            see_more = itemView.findViewById(R.id.see_more);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

}

