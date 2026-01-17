package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Signup extends AppCompatActivity {

    TextView title, subTitle, nameLabel, usernameLabel, emailLabel, pass, log;
    EditText name_field, email_field, password_field;
    Button sign_up_button;
    ImageView penImage;
    FirebaseAuth mAuth;
    ProgressBar progress;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent2 = new Intent(getApplicationContext(), dashboard.class);
            startActivity(intent2);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        title = findViewById(R.id.title);
        log = findViewById(R.id.log);
        subTitle = findViewById(R.id.subTitle);
        penImage = findViewById(R.id.penImage);
        name_field = findViewById(R.id.name_field);
        progress = findViewById(R.id.progress);
        mAuth = FirebaseAuth.getInstance();
        email_field = findViewById(R.id.email_field);
        password_field = findViewById(R.id.password_field);
        pass = findViewById(R.id.pass);
        sign_up_button = findViewById(R.id.sign_up_button);

        log.setOnClickListener(view -> {
            Intent intent3 = new Intent(getApplicationContext(), login.class);
            startActivity(intent3);
        });

        sign_up_button.setOnClickListener(view -> {
            progress.setVisibility(View.VISIBLE);
            String email = email_field.getText().toString().trim();
            String password = password_field.getText().toString().trim();
            String name = name_field.getText().toString().trim();

            // Validations
            if (TextUtils.isEmpty(email)) {
                progress.setVisibility(View.GONE);
                Toast.makeText(Signup.this, "Enter Email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                progress.setVisibility(View.GONE);
                Toast.makeText(Signup.this, "Enter Password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(name)) {
                progress.setVisibility(View.GONE);
                Toast.makeText(Signup.this, "Enter name", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progress.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(updateTask -> {
                                            if (updateTask.isSuccessful()) {
                                                // Save user data to the database
                                                saveUserData(user.getUid(), name, email);
                                                Toast.makeText(Signup.this, "Account Created", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(getApplicationContext(), dashboard.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(Signup.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        });
    }

    private void saveUserData(String userId, String name, String email) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        HashMap<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("email", email);
        userData.put("totalPosts", 0);

        userRef.setValue(userData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Toast.makeText(Signup.this, "User data saved successfully.", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(Signup.this, "Failed to save user data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
