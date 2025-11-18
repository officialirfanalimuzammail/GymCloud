package com.example.gymcloud;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class AdminProfileActivity extends AppCompatActivity {

    private TextView txtGymName, txtAdminName, txtTotalMembers;
    private MaterialButton btnShowAllMembers, btnFeeDefaulters, btnSubmitFee, btnEditProfile, btnLogout;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef, gymRef, membersRef;

    private String adminId;
    private String gymId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        adminId = mAuth.getCurrentUser().getUid();

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(adminId);
        gymRef = FirebaseDatabase.getInstance().getReference("Gyms");
        membersRef = FirebaseDatabase.getInstance().getReference("Members");

        initViews();
        loadAdminData();
        setupButtons();
    }

    private void initViews() {
        txtGymName = findViewById(R.id.txtGymName);
        txtAdminName = findViewById(R.id.txtAdminName);
        txtTotalMembers = findViewById(R.id.txtTotalMembers);

        btnShowAllMembers = findViewById(R.id.btnShowAllMembers);
        btnFeeDefaulters = findViewById(R.id.btnFeeDefaulters);
        btnSubmitFee = findViewById(R.id.btnSubmitFee);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void loadAdminData() {

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    Toast.makeText(AdminProfileActivity.this, "Profile not found!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String fullName = snapshot.child("fullName").getValue(String.class);
                gymId = snapshot.child("gymId").getValue(String.class);

                txtAdminName.setText(fullName != null ? fullName : "Admin");

                if (gymId != null && !gymId.isEmpty()) {
                    loadGymInfo(gymId);
                    loadMembersCount(gymId);
                } else {
                    txtGymName.setText("Gym not found");
                    txtTotalMembers.setText("Total Members: 0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadGymInfo(String gymId) {

        gymRef.child(gymId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String gymName = snapshot.child("name").getValue(String.class);
                txtGymName.setText(gymName != null ? gymName : "Gym");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // ✔ FIXED: Matches your Firebase structure (Members → gymId → memberId : true)
    private void loadMembersCount(String gymId) {

        membersRef.child(gymId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        long count = snapshot.getChildrenCount();
                        txtTotalMembers.setText("Total Members: " + count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void setupButtons() {

        btnShowAllMembers.setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            i.putExtra("gymId", gymId);
            startActivity(i);
        });

        btnFeeDefaulters.setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            i.putExtra("gymId", gymId);
            startActivity(i);
        });

        btnSubmitFee.setOnClickListener(v -> {
            Intent i = new Intent(this, LoginActivity.class);
            i.putExtra("gymId", gymId);
            startActivity(i);
        });

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
