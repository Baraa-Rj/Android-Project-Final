package com.example.myapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import com.example.myapplication.data.local.TokenManager;
import com.example.myapplication.ui.home.HomeFragment;
import com.example.myapplication.ui.auth.LoginFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Check if user is already logged in
        TokenManager tokenManager = TokenManager.getInstance(this);
        Fragment initialFragment;

        if (tokenManager.getToken() != null) {
            initialFragment = new HomeFragment();
        } else {
            initialFragment = new LoginFragment();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, initialFragment)
                    .commit();
        }
    }
}