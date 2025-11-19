package com.example.gymcloud;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MemberProfileActivity extends AppCompatActivity {

    TextView memberName, memberEmail;

    // Titles
    TextView titleFeeDetails, titlePersonalGrowth, titleAttendance, titleWorkout, titleFeeHistory;

    // Sections
    LinearLayout sectionFeeDetails, sectionPersonalGrowth, sectionAttendance, sectionWorkout, sectionFeeHistory;

    @SuppressLint({"SetTextI18n", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_member_profile);

        // ================
        //  Basic Profile
        // ================
        memberName = findViewById(R.id.memberName);
        memberEmail = findViewById(R.id.memberEmail);

        String fullName = getIntent().getStringExtra("fullName");
        String email = getIntent().getStringExtra("email");

        if (fullName == null) fullName = "N/A";
        if (email == null) email = "N/A";

        memberName.setText("Welcome : " + fullName);
        memberEmail.setText("Phone : " + email);   // You said you're receiving phone number in "email"


        // ======================
        //   FIND COLLAPSIBLE IDs
        // ======================

        // Titles
        titleFeeDetails = findViewById(R.id.titleFeeDetails);
        titlePersonalGrowth = findViewById(R.id.titlePersonalGrowth);
        titleAttendance = findViewById(R.id.titleAttendance);
        titleWorkout = findViewById(R.id.titleWorkout);
        titleFeeHistory = findViewById(R.id.titleFeeHistory);

        // Sections
        sectionFeeDetails = findViewById(R.id.sectionFeeDetails);
        sectionPersonalGrowth = findViewById(R.id.sectionPersonalGrowth);
        sectionAttendance = findViewById(R.id.sectionAttendance);
        sectionWorkout = findViewById(R.id.sectionWorkout);
        sectionFeeHistory = findViewById(R.id.sectionFeeHistory);


        // ======================
        //   CLICK LISTENERS
        // ======================

        titleFeeDetails.setOnClickListener(v ->
                toggleSection(sectionFeeDetails));

        titlePersonalGrowth.setOnClickListener(v ->
                toggleSection(sectionPersonalGrowth));

        titleAttendance.setOnClickListener(v ->
                toggleSection(sectionAttendance));

        titleWorkout.setOnClickListener(v ->
                toggleSection(sectionWorkout));

        titleFeeHistory.setOnClickListener(v ->
                toggleSection(sectionFeeHistory));
    }


    // ============================
    //   TOGGLE COLLAPSIBLE VIEWS
    // ============================

    private void toggleSection(View section) {
        if (section.getVisibility() == View.VISIBLE) {
            section.setVisibility(View.GONE);
        } else {
            section.setVisibility(View.VISIBLE);
        }
    }
}
