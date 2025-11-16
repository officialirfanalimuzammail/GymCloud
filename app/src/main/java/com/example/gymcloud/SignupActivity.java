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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SignupActivity extends AppCompatActivity {
    // ... (rest of the class variables and methods remain the same) ...
    private TextInputEditText nameEditText, emailEditText, passwordEditText;
    private TextInputLayout nameLayout, emailLayout, passwordLayout, roleLayout;
    private AutoCompleteTextView roleDropdown;
    private MaterialButton signUpButton, loginButton;
    private FirebaseAuth mAuth;
    private static final int MIN_PASSWORD_LENGTH = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();
        initViews();
        setupRoleDropdown();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSignUp();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
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
        signUpButton = findViewById(R.id.signup_button);
        loginButton = findViewById(R.id.signup_login_button);
    }

    private void setupRoleDropdown() {
        List<String> roles = Arrays.asList("Member", "Admin");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles);
        roleDropdown.setAdapter(adapter);
        roleDropdown.setText(roles.get(0), false);
    }

    private void handleSignUp() {
        clearErrors();
        String fullName = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String selectedRole = roleDropdown.getText().toString();

        if (!validateInput(fullName, email, password, selectedRole)) {
            return;
        }
        registerUser(fullName, email, password, selectedRole);
    }

    private boolean validateInput(String fullName, String email, String password, String role) {
        // ... (Validation logic remains the same) ...
        boolean isValid = true;
        if (TextUtils.isEmpty(fullName)) { nameLayout.setError("Full name is required"); isValid = false; }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailLayout.setError("Enter a valid email address"); isValid = false; }
        if (TextUtils.isEmpty(password) || password.length() < MIN_PASSWORD_LENGTH) { passwordLayout.setError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters"); isValid = false; }
        if (TextUtils.isEmpty(role) || (!role.equals("Member") && !role.equals("Admin"))) { roleLayout.setError("Please select a valid role"); isValid = false; }
        return isValid;
    }

    private void clearErrors() {
        nameLayout.setError(null); emailLayout.setError(null);
        passwordLayout.setError(null); roleLayout.setError(null);
    }

    // New Firebase Registration Logic
    private void registerUser(final String fullName, final String email, String password, final String role) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignupActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();

                            // Store additional user data (name and role) in Firestore or Realtime DB
                            saveUserInFirebase(user.getUid(), email,fullName, role);

                            // ---  NAVIGATION LOGIC ---
                            Intent intent;
                            if (role.equals("Member")) {
                                // Use .equals() for String comparison in Java
                                intent = new Intent(SignupActivity.this, MemberProfileActivity.class);
                            } else {
                                intent = new Intent(SignupActivity.this, AdminProfileActivity.class);
                            }
                            startActivity(intent);
                            finish(); // Close the signup activity

                        } else {
                            Toast.makeText(SignupActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Placeholder function to save additional user details (name and role) to a database.
     */
    private void saveUserInFirebase(String userId, String email,String fullName, String role) {

        DatabaseReference usersRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId);

        HashMap<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("role", role);
        userData.put("userId", userId);

        usersRef.setValue(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User data saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
