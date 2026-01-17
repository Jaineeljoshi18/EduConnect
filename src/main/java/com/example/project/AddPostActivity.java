package com.example.project;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class AddPostActivity extends AppCompatActivity {

    EditText Content, Title, Genre;
    TextView upload;
    Button  submit_button;
    ImageView blog_image, home, explore,profile;
    Uri imageUrl = null;
    DatabaseReference ref;

    private static final int GALLERY_IMAGE_CODE = 100;
    ProgressDialog pd;
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Add Post");
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Title = findViewById(R.id.Title);
        Content = findViewById(R.id.Content);
        Genre = findViewById(R.id.Genre);
        home = findViewById(R.id.home);
        explore = findViewById(R.id.explore);
        upload = findViewById(R.id.upload);
        submit_button = findViewById(R.id.submit_button);
        blog_image = findViewById(R.id.blog_image);

        profile = findViewById(R.id.profile);
        pd = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference("Users");

        permission();

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), dashboard.class);
                startActivity(intent);
            }
        });
        explore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Explore.class);
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

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryPick();
            }
        });

        submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String heading = Title.getText().toString();
                String genre = Genre.getText().toString();
                String content = Content.getText().toString();

                if (TextUtils.isEmpty(heading)) {
                    Title.setError("Title is required.");
                } else if (TextUtils.isEmpty(genre)) {
                    Genre.setError("Genre is required.");
                } else if (TextUtils.isEmpty(content)) {
                    Content.setError("Description is required.");
                } else {
                    uploadData(heading, genre, content);
                }
            }
        });
    }

    private void uploadData(String title, String genre, String content) {
        pd.setMessage("Publishing post");
        pd.show();

        final String timeStamp = String.valueOf(System.currentTimeMillis());
        String filepath = "BlogPosts/" + "blogposts_" + timeStamp;

        if (blog_image.getDrawable() != null) {
            Bitmap bitmap = ((BitmapDrawable) blog_image.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] data = baos.toByteArray();

            StorageReference reference = FirebaseStorage.getInstance().getReference().child(filepath);
            reference.putBytes(data)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;

                            String downloadUri = uriTask.getResult().toString();
                            if (uriTask.isSuccessful()) {
                                storePostData(title, genre, content, timeStamp, downloadUri);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(AddPostActivity.this, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            storePostData(title, genre, content, timeStamp, null);
        }
    }

    private void storePostData(String title, String genre, String content, String timeStamp, String imageUrl) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String formattedDate = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date(Long.parseLong(timeStamp)));

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", user.getUid());
        hashMap.put("authorName", user.getDisplayName());
        hashMap.put("uEmail", user.getEmail());
        hashMap.put("postId", timeStamp);
        hashMap.put("heading", title);
        hashMap.put("genre", genre);
        hashMap.put("content", content);
        hashMap.put("date", formattedDate);
        hashMap.put("likeCount", 0);
        hashMap.put("saveCount", 0);
        hashMap.put("commentCount", 0);

        if (imageUrl != null) {
            hashMap.put("imageUrl", imageUrl);
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("BlogPosts");
        ref.child(timeStamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, "Post Published", Toast.LENGTH_SHORT).show();
                        Title.setText("");
                        Content.setText("");
                        Genre.setText("");
                        blog_image.setImageURI(null);
                        startActivity(new Intent(AddPostActivity.this, dashboard.class));

                        incrementUserPostCount(user.getUid());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPostActivity.this, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void galleryPick() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_IMAGE_CODE);
    }

    private void permission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (!report.areAllPermissionsGranted()) {
                            Toast.makeText(AddPostActivity.this, "Storage permission is required.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == GALLERY_IMAGE_CODE && data != null) {
            imageUrl = data.getData();
            blog_image.setImageURI(imageUrl);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void incrementUserPostCount(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.child("totalPosts").setValue(ServerValue.increment(1))
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {

                        Toast.makeText(AddPostActivity.this, "Failed to update total posts: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}
