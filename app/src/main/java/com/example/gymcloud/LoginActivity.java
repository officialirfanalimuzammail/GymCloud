package com.example.gymcloud;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText;
    private TextInputLayout emailLayout, passwordLayout;
    private MaterialButton loginButton, signupButton;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        initViews();
        setupListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.login_email_edit_text);
        passwordEditText = findViewById(R.id.login_password_edit_text);

        emailLayout = findViewById(R.id.login_email_layout);
        passwordLayout = findViewById(R.id.login_password_layout);

        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.login_signup_button);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> handleLogin());
        signupButton.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }

    private void handleLogin() {
        clearErrors();

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!validateInput(email, password)) return;

        authenticateUser(email, password);
    }

    private boolean validateInput(String email, String password) {
        boolean valid = true;

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email");
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            valid = false;
        }

        return valid;
    }

    private void clearErrors() {
        emailLayout.setError(null);
        passwordLayout.setError(null);
    }

    private void authenticateUser(String email, String password) {
        loginButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Toast.makeText(this,
                                "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        loginButton.setEnabled(true);
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        fetchUserData(user.getUid());
                    }
                });
    }

    /** Fetch full user profile from Firebase **/
    private void fetchUserData(String uid) {

        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    Toast.makeText(LoginActivity.this,
                            "User not found in database.",
                            Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                    loginButton.setEnabled(true);
                    return;
                }

                // Base fields
                String fullName = snapshot.child("fullName").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String role = snapshot.child("role").getValue(String.class);

                if (role == null) role = "Guest"; // fail-safe

                // For Members only
                String gymId = snapshot.child("gymId").getValue(String.class);
                String gymName = snapshot.child("gymName").getValue(String.class);

                redirectToCorrectScreen(uid, fullName, email, role, gymId, gymName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this,
                        "Failed to load user data.",
                        Toast.LENGTH_SHORT).show();
                loginButton.setEnabled(true);
            }
        });
    }

    /** Redirect user based on role + pass all extras **/
    private void redirectToCorrectScreen(String uid, String fullName, String email,
                                         String role, String gymId, String gymName) {

        Intent intent;

        switch (role) {

            case "Admin":
                intent = new Intent(this, AdminProfileActivity.class);
                break;

            case "Member":
                intent = new Intent(this, MemberProfileActivity.class);
                intent.putExtra("gymId", gymId);
                intent.putExtra("gymName", gymName);
                break;

            default: // Guest or Unknown
                intent = new Intent(this, GuestDashboardActivity.class);
                break;
        }

        // Common extras
        intent.putExtra("userId", uid);
        intent.putExtra("fullName", fullName);
        intent.putExtra("email", email);
        intent.putExtra("role", role);

        // Clear backstack
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }
}
