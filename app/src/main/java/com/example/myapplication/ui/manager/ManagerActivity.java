package com.example.myapplication.ui.manager;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class ManagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        TextView welcomeText = findViewById(R.id.welcomeText);
        welcomeText.setText("Welcome Manager!");
    }
}
