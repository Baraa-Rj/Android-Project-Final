package com.example.myapplication.ui.employee;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class EmployeeActivity extends AppCompatActivity {
    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        welcomeTextView = findViewById(R.id.welcomeTextView);
        welcomeTextView.setText("Welcome, Employee!");
    }
}