package com.example.gymcloud;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
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
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");

        initViews();

        loginButton.setOnClickListener(v -> handleLogin());
        signupButton.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }

    private void initViews() {
        emailEditText = findViewById(R.id.login_email_edit_text);
        passwordEditText = findViewById(R.id.login_password_edit_text);
        emailLayout = findViewById(R.id.login_email_layout);
        passwordLayout = findViewById(R.id.login_password_layout);
        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.login_signup_button);
    }

    private void handleLogin() {
        clearErrors();

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!validateInput(email, password)) return;

        authenticateUser(email, password);
    }

    private boolean validateInput(String email, String password) {
        boolean isValid = true;

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        emailLayout.setError(null);
        passwordLayout.setError(null);
    }

    private void authenticateUser(String email, String password) {
        loginButton.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            fetchUserData(user.getUid());
                        }

                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Authentication failed: Invalid credentials.",
                                Toast.LENGTH_SHORT).show();
                        loginButton.setEnabled(true);
                    }
                });
    }

    /** Fetch name + email + role from Realtime Database **/
    private void fetchUserData(String userId) {

        dbRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()) {
                    Toast.makeText(LoginActivity.this,
                            "User data not found. Contact support.",
                            Toast.LENGTH_LONG).show();
                    mAuth.signOut();
                    loginButton.setEnabled(true);
                    return;
                }

                // Read user data
                String fullName = snapshot.child("fullName").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String role = snapshot.child("role").getValue(String.class);

                redirectToCorrectProfile(fullName, email, role, userId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this,
                        "Failed to fetch user data.",
                        Toast.LENGTH_SHORT).show();
                loginButton.setEnabled(true);
            }
        });
    }

    /** Redirect + putExtras **/
    private void redirectToCorrectProfile(String fullName, String email, String role, String userId) {

        Intent intent;

        if ("Admin".equals(role)) {
            intent = new Intent(LoginActivity.this, AdminProfileActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, MemberProfileActivity.class);
        }

        // Send data to next activity
        intent.putExtra("fullName", fullName);
        intent.putExtra("email", email);
        intent.putExtra("role", role);
        intent.putExtra("userId", userId);

        // Clear backstack
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }
}
