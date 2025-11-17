package com.example.gymcloud;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CreateGymActivity extends AppCompatActivity {

    TextView adminName;
    TextInputEditText inputGymName, inputGymAddress, inputGymPhone;
    Button btnSaveGym;

    FirebaseAuth mAuth;
    DatabaseReference userRef, gymRef;

    String gymId;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_gym);

        mAuth = FirebaseAuth.getInstance();

        // Input fields
        inputGymName = findViewById(R.id.inputGymName);
        inputGymAddress = findViewById(R.id.inputGymAddress);
        inputGymPhone = findViewById(R.id.inputGymPhone);
        btnSaveGym = findViewById(R.id.btnSaveGym);

        // Admin info
        adminName = findViewById(R.id.adminName);

        String uid = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        // Get data from intent
        String fullName = getIntent().getStringExtra("fullName");
        String email = getIntent().getStringExtra("email");
        String role = getIntent().getStringExtra("role");

        if (fullName == null) fullName = "N/A";
        if (email == null) email = "N/A";
        if (role == null) role = "N/A";

        adminName.setText("Name: " + fullName + "\nEmail: " + email + "\nRole: " + role);

        // Load saved gym data (if exists)
        loadGymId(uid);

        // Save button click
        btnSaveGym.setOnClickListener(v -> saveGymData(uid));
    }

    // -----------------------------------------
    // 🔥 Step 1: Load admin's gymId from Firebase
    // -----------------------------------------
    private void loadGymId(String uid) {
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists() && snapshot.child("gymId").exists()) {

                gymId = snapshot.child("gymId").getValue(String.class);

                if (gymId != null) {
                    loadGymDetails(gymId);
                }

            } else {
                // If gymId not exist, create a new one for admin
                gymId = userRef.getKey();  // or use push().getKey()
                userRef.child("gymId").setValue(gymId);
            }
        });
    }

    // -----------------------------------------
    // 🔥 Step 2: Load gym details if exist
    // -----------------------------------------
    private void loadGymDetails(String gymId) {
        gymRef = FirebaseDatabase.getInstance()
                .getReference("Gyms")
                .child(gymId);

        gymRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {

                String gName = snapshot.child("name").getValue(String.class);
                String gAddress = snapshot.child("address").getValue(String.class);
                String gPhone = snapshot.child("phone").getValue(String.class);

                inputGymName.setText(gName);
                inputGymAddress.setText(gAddress);
                inputGymPhone.setText(gPhone);
            }
        });
    }

    // -----------------------------------------
    // 🔥 Step 3: Save gym data to Firebase
    // -----------------------------------------
    private void saveGymData(String uid) {

        String name = inputGymName.getText().toString().trim();
        String address = inputGymAddress.getText().toString().trim();
        String phone = inputGymPhone.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gymId == null) {
            gymId = uid;  // fallback
        }

        gymRef = FirebaseDatabase.getInstance()
                .getReference("Gyms")
                .child(gymId);

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("address", address);
        map.put("phone", phone);

        gymRef.setValue(map).addOnSuccessListener(aVoid -> {

            // Save gymId to admin's user profile
            userRef.child("gymId").setValue(gymId);

            Toast.makeText(CreateGymActivity.this, "Gym details saved!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(CreateGymActivity.this, AdminProfileActivity.class);
            startActivity(intent);
            finish();


        }).addOnFailureListener(e ->
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}
