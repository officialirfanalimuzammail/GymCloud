package com.example.gymcloud;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SignupActivity extends AppCompatActivity {

    // UI
    private TextInputEditText nameEditText, emailEditText, passwordEditText;
    private TextInputLayout nameLayout, emailLayout, passwordLayout, roleLayout, memberGymLayout;
    private AutoCompleteTextView roleDropdown, gymDropdown;
    private MaterialButton signUpButton, loginButton;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference gymRef;

    // Constants
    private static final int MIN_PASSWORD_LENGTH = 8;

    // Gym Data
    private List<String> gymNames = new ArrayList<>();
    private List<String> gymIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initViews();
        initFirebase();
        setupRoleDropdown();
        loadGyms();
        setupListeners();
    }

    private void initViews() {
        nameEditText = findViewById(R.id.signup_name_edit_text);
        emailEditText = findViewById(R.id.signup_email_edit_text);
        passwordEditText = findViewById(R.id.signup_password_edit_text);

        nameLayout = findViewById(R.id.signup_name_layout);
        emailLayout = findViewById(R.id.signup_email_layout);
        passwordLayout = findViewById(R.id.signup_password_layout);
        roleLayout = findViewById(R.id.signup_role_layout);

        roleDropdown = findViewById(R.id.signup_role_dropdown);

        memberGymLayout = findViewById(R.id.signup_select_gym_layout);
        gymDropdown = findViewById(R.id.signup_select_gym_dropdown);

        signUpButton = findViewById(R.id.signup_button);
        loginButton = findViewById(R.id.signup_login_button);
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        gymRef = FirebaseDatabase.getInstance().getReference("Gyms");
    }

    private void setupRoleDropdown() {
        List<String> roles = List.of("Guest", "Member", "Admin");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                roles
        );

        roleDropdown.setAdapter(adapter);
        roleDropdown.setText("Guest", false);

        roleDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String role = roleDropdown.getText().toString();
            if (role.equals("Member")) {
                memberGymLayout.setVisibility(View.VISIBLE);
            } else {
                memberGymLayout.setVisibility(View.GONE);
                gymDropdown.setText("");
            }
        });
    }

    private void loadGyms() {
        gymRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gymNames.clear();
                gymIds.clear();

                for (DataSnapshot gymSnap : snapshot.getChildren()) {
                    String id = gymSnap.getKey();
                    String name = gymSnap.child("name").getValue(String.class);

                    if (id != null && name != null) {
                        gymIds.add(id);
                        gymNames.add(name);
                    }
                }

                ArrayAdapter<String> gymAdapter = new ArrayAdapter<>(
                        SignupActivity.this,
                        android.R.layout.simple_list_item_1,
                        gymNames
                );
                gymDropdown.setAdapter(gymAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SignupActivity.this, "Failed to load gyms", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        signUpButton.setOnClickListener(v -> handleSignup());
        loginButton.setOnClickListener(v ->
                startActivity(new Intent(SignupActivity.this, LoginActivity.class)));
    }

    private void handleSignup() {
        clearErrors();

        String fullName = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String role = roleDropdown.getText().toString();
        String gymName = gymDropdown.getText().toString();

        if (!validateInput(fullName, email, password, role)) return;

        String gymId = "";
        if (role.equals("Member")) {
            int index = gymNames.indexOf(gymName);
            if (index >= 0) gymId = gymIds.get(index);

            if (TextUtils.isEmpty(gymId)) {
                gymDropdown.setError("Please select a gym");
                return;
            }
        }

        registerUser(fullName, email, password, role, gymId, gymName);
    }

    private boolean validateInput(String name, String email, String password, String role) {
        boolean valid = true;

        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Full name required");
            valid = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Invalid email");
            valid = false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            passwordLayout.setError("Minimum 8 characters");
            valid = false;
        }
        if (TextUtils.isEmpty(role)) {
            roleLayout.setError("Select role");
            valid = false;
        }

        return valid;
    }

    private void registerUser(String fullName, String email, String password,
                              String role, String gymId, String gymName) {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Error: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) return;

                    saveUserDetails(user.getUid(), fullName, email, role, gymId, gymName);
                });
    }

    private void saveUserDetails(String uid, String fullName, String email,
                                 String role, String gymId, String gymName) {

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid);

        HashMap<String, Object> data = new HashMap<>();
        data.put("userId", uid);
        data.put("fullName", fullName);
        data.put("email", email);
        data.put("role", role);
        data.put("createdAt", System.currentTimeMillis());

        if (role.equals("Member")) {
            data.put("gymId", gymId);
            data.put("gymName", gymName);

            // add user under gym membership node
            FirebaseDatabase.getInstance()
                    .getReference("Members")
                    .child(gymId)
                    .child(uid)
                    .setValue(true);
        }

        userRef.setValue(data)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show();
                    redirectAfterSignup(role, fullName, email, gymName);
                });
    }

    private void redirectAfterSignup(String role, String name, String email, String gymName) {
        Intent intent;

        if (role.equals("Admin")) {
            intent = new Intent(this, CreateGymActivity.class);
        } else {
            intent = new Intent(this, MemberProfileActivity.class);
            intent.putExtra("gymName", gymName);
        }

        intent.putExtra("fullName", name);
        intent.putExtra("email", email);
        intent.putExtra("role", role);
        startActivity(intent);
        finish();
    }

    private void clearErrors() {
        nameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        roleLayout.setError(null);
    }
}
