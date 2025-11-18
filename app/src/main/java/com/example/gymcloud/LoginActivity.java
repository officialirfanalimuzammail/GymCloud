package com.example.gymcloud;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.CheckBox;
import android.widget.TextView;
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
    private CheckBox rememberMeCheck;
    private TextView forgotPasswordText;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        initViews();
        loadSavedLogin();
        setupListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.login_email_edit_text);
        passwordEditText = findViewById(R.id.login_password_edit_text);
        forgotPasswordText = findViewById(R.id.login_forgot_password);
        emailLayout = findViewById(R.id.login_email_layout);
        passwordLayout = findViewById(R.id.login_password_layout);

        loginButton = findViewById(R.id.login_button);
        signupButton = findViewById(R.id.login_signup_button);
        rememberMeCheck = findViewById(R.id.checkbox_remember_me);
    }

    private void loadSavedLogin() {
        String savedEmail = prefs.getString("email", "");
        String savedPass = prefs.getString("password", "");
        boolean savedCheck = prefs.getBoolean("remember", false);

        emailEditText.setText(savedEmail);
        passwordEditText.setText(savedPass);
        rememberMeCheck.setChecked(savedCheck);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> handleLogin());
        signupButton.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
        forgotPasswordText.setOnClickListener(v -> handleForgotPassword());
    }

    private void handleForgotPassword() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter your email first");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                "Password reset email sent!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
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

                    /** Save login if user checked Remember Me */
                    if (rememberMeCheck.isChecked()) {
                        prefs.edit()
                                .putString("email", email)
                                .putString("password", password)
                                .putBoolean("remember", true)
                                .apply();
                    } else {
                        prefs.edit().clear().apply();
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        fetchUserData(user.getUid());
                    }
                });
    }

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

                String fullName = snapshot.child("fullName").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String phone = snapshot.child("phone").getValue(String.class);
                String role = snapshot.child("role").getValue(String.class);

                String gymId = snapshot.child("gymId").getValue(String.class);
                String gymName = snapshot.child("gymName").getValue(String.class);

                redirectToCorrectScreen(uid, fullName, email, phone, role, gymId, gymName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this,
                        "Failed to load user data.", Toast.LENGTH_SHORT).show();
                loginButton.setEnabled(true);
            }
        });
    }

    private void redirectToCorrectScreen(String uid, String fullName, String email,
                                         String phone, String role,
                                         String gymId, String gymName) {

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

            default:
                intent = new Intent(this, GuestDashboardActivity.class);
                break;
        }

        intent.putExtra("userId", uid);
        intent.putExtra("fullName", fullName);
        intent.putExtra("email", email);
        intent.putExtra("phone", phone);
        intent.putExtra("role", role);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
