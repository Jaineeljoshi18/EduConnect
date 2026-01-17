package com.example.project;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class demo extends AppCompatActivity{
    // Firebase Database reference
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    // UI Elements
    private EditText editTextName, editTextAge;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        // Bind UI elements
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        btnSave = findViewById(R.id.btnSave);

        // Save data to Firebase when button is clicked
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });
    }

    // Method to save user data to Firebase
    private void saveUserData() {
        String name = editTextName.getText().toString();
        String age = editTextAge.getText().toString();

        // Check if inputs are valid
        if (!name.isEmpty() && !age.isEmpty()) {
            // Create a unique ID for each user and push data
            String userId = myRef.push().getKey();
            User user = new User(name, age);

            // Save user data under the userId node
            if (userId != null) {
                myRef.child(userId).setValue(user);
                Toast.makeText(demo.this, "Data saved!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(demo.this, "Please enter valid data.", Toast.LENGTH_SHORT).show();
        }
    }

    // User class to represent the user data
    public static class User {
        public String name;
        public String age;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(String name, String age) {
            this.name = name;
            this.age = age;
        }
    }
}
