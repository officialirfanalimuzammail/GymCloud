package com.example.gymcloud;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Delayed auto-login after 1.5 seconds
        new Handler().postDelayed(this::checkAutoLogin, 1500);
    }

    private void checkAutoLogin() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean remember = prefs.getBoolean("remember", false);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (remember && user != null) {
            // User remembered → go directly to profile
            startActivity(new Intent(SplashActivity.this, AdminProfileActivity.class));
        } else {
            // Not remembered → open login
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
        finish();
    }
}
