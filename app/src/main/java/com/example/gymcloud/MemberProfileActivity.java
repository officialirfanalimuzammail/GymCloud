package com.example.gymcloud;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MemberProfileActivity extends AppCompatActivity {

    TextView memberName;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_member_profile);

        // Initialize view
        memberName = findViewById(R.id.memberName);

        // Receive data from Intent
        String fullName = getIntent().getStringExtra("fullName");
        String email = getIntent().getStringExtra("email");
        String role = getIntent().getStringExtra("role");
        String selectedGym = getIntent().getStringExtra("selectedGym");


        // Null-safe default values
        if (fullName == null) fullName = "N/A";
        if (email == null) email = "N/A";
        if (role == null) role = "N/A";
        if (selectedGym == null) selectedGym = "N/A";


        // Set profile text
        memberName.setText(
                "Name: " + fullName +
                        "\nEmail: " + email +
                        "\nRole: " + role +
                        "\nSelected Gym: " + selectedGym
        );
    }
}
