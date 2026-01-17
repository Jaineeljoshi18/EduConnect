package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class login extends AppCompatActivity {

    Button loginBut;
    TextView title, subTitle, EmailL, passwordL, forgotPasswordT, orTxt, tv4, createAccount;
    EditText EmailF, passwordF;
    private FirebaseAuth mAuth;
    private ProgressBar progress;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth != null ? mAuth.getCurrentUser() : null;
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), dashboard.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth == null) {
            Toast.makeText(this, "Firebase initialization failed.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initializing UI components
        loginBut = findViewById(R.id.loginBut);
        title = findViewById(R.id.title);
        subTitle = findViewById(R.id.subTitle);
        EmailL = findViewById(R.id.EmailL);
        EmailF = findViewById(R.id.EmailF);
        passwordL = findViewById(R.id.passwordL);
        passwordF = findViewById(R.id.passwordF);
        forgotPasswordT = findViewById(R.id.forgotPasswordT);
        tv4 = findViewById(R.id.tv4);
        createAccount = findViewById(R.id.createAccount);

        progress = findViewById(R.id.progress);

        createAccount.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Signup.class);
            startActivity(intent);
        });

        forgotPasswordT.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), forgetPassword.class);
            startActivity(intent);
        });

        loginBut.setOnClickListener(view -> {
            progress.setVisibility(View.VISIBLE);
            String email = EmailF.getText().toString().trim();
            String password = passwordF.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                progress.setVisibility(View.GONE);
                Toast.makeText(login.this, "Enter Email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                progress.setVisibility(View.GONE);
                Toast.makeText(login.this, "Enter Password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progress.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                Toast.makeText(login.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), dashboard.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });

        // Applying system window insets to the main layout view
        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }

    private void displayToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}

