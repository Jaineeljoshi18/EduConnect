package com.example.project;

public class BlogPost {
    private String PostId;
    private String heading;
    private String authorName;
    private String genre;
    private String date;
    private String content;
    private int likeCount;
    private int commentCount;
    private int saveCount;
    private String uid;


    private boolean isLiked;
    private boolean isSaved;
    private String imageUrl;

    public BlogPost() {

    }

    public BlogPost(String postId, String heading, String authorName, String date, String genre,
                    String content, int likeCount, int commentCount, int saveCount, String imageUrl) {
        this.PostId = postId;
        this.heading = heading;
        this.authorName = authorName;
        this.genre = genre;
        this.date = date;
        this.imageUrl = imageUrl;
        this.content = content;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.saveCount = saveCount;
        this.isLiked = false;
        this.isSaved = false;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPostId() {
        return PostId;
    }

    public void setPostId(String postId) {
        PostId = postId;
    }

    public String getHeading() {
        return heading;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getGenre() {
        return genre;
    }

    public String getDate() {
        return date;
    }

    public String getContent() {
        return content;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public int getSaveCount() {
        return saveCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public String getId() {
        return PostId;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void setSaveCount(int saveCount) {
        this.saveCount = saveCount;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
