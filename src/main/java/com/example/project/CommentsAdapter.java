package com.example.project;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private Context mContext;
    private List<Comment> mData;
    private String currentUserId;  // Current user ID

    public CommentsAdapter(Context mContext, List<Comment> mData, String currentUserId) {
        this.mContext = mContext;
        this.mData = mData;
        this.currentUserId = currentUserId;  // Initialize current user ID
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new CommentViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = mData.get(position);
        holder.tv_name.setText(comment.getUname());
        holder.tv_content.setText(comment.getContent());
        holder.tv_date.setText(timestampToString((Long) comment.getTimestamp()));

        // Show delete button only if the current user is the author of the comment
        if (comment.getUid().equals(currentUserId)) {
            holder.delete_comment.setVisibility(View.VISIBLE);
        } else {
            holder.delete_comment.setVisibility(View.GONE);
        }

        // Set delete button click listener
        holder.delete_comment.setOnClickListener(view -> {
            if (comment.getUid().equals(currentUserId)) { // Additional safety check
                if (mContext instanceof PostDetailActivity) {
                    ((PostDetailActivity) mContext).onDeleteClicked(comment);
                }
            } else {
                Toast.makeText(mContext, "You can only delete your own comments.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_content, tv_date;
        ImageView delete_comment;

        public CommentViewHolder(View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.comment_username);
            tv_content = itemView.findViewById(R.id.comment_content);
            tv_date = itemView.findViewById(R.id.comment_date);
            delete_comment = itemView.findViewById(R.id.delete);
        }
    }

    private String timestampToString(long time) {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        return DateFormat.format("dd/MM", calendar).toString();
    }
}
