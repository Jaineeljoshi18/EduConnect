package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

public class forgetPassword extends AppCompatActivity {

    FirebaseAuth mAuth;
    EditText EmailE;
    Button forBut;
    String email;
    ImageView iv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_password);

        mAuth = FirebaseAuth.getInstance();
        EmailE= findViewById(R.id.EmailE);
        forBut = findViewById(R.id.forBut);
        iv2 = findViewById(R.id.iv2);

        iv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), login.class);
                startActivity(intent);
            }
        });

        forBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void validateData() {

        email = EmailE.getText().toString();
        if(email.isEmpty()){
            EmailE.setError("Required");
        }else{
            forgetPass();
        }
    }

    private void forgetPass() {
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(forgetPassword.this, "Check your Email", Toast.LENGTH_SHORT).show();
                    Intent intent5 = new Intent(forgetPassword.this, login.class);
                    startActivity(intent5);
                    finish();
                }
                else{
                    Toast.makeText(forgetPassword.this, "Error: "+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}