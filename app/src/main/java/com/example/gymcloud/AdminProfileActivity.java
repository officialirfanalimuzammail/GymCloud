package com.example.gymcloud;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminProfileActivity extends AppCompatActivity {
    TextView adminName;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_profile);
        String fullName = getIntent().getStringExtra("fullName");
        String email = getIntent().getStringExtra("email");
        String role = getIntent().getStringExtra("role");
        String userId = getIntent().getStringExtra("userId");
        adminName = findViewById(R.id.adminName);
        adminName.setText("Name: " + fullName + "\nEmail: " + email + "\nRole: " + role);



    }
}