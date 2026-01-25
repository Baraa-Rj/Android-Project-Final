package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.ui.auth.LoginFragment;
import com.example.myapplication.ui.customer.CustomerActivity;
import com.example.myapplication.utils.TokenManager;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private TokenManager tokenManager;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Activity is being created");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);

        // Only initialize fragment on first creation, not on configuration changes
        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: First creation, loading initial fragment");
            loadInitialFragment();
        } else {
            Log.d(TAG, "onCreate: Restoring from saved state");
        }
    }

    private void loadInitialFragment() {
        if (tokenManager.getToken() != null) {
            // User is logged in, navigate based on role
            String role = tokenManager.getUserRole();
            Log.d(TAG, "loadInitialFragment: User is logged in with role: " + role);

            Intent intent;
            if ("employee".equalsIgnoreCase(role)) {
                intent = new Intent(this, com.example.myapplication.ui.employee.EmployeeActivity.class);
            } else if ("manager".equalsIgnoreCase(role)) {
                intent = new Intent(this, com.example.myapplication.ui.manager.ManagerActivity.class);
            } else {
                intent = new Intent(this, CustomerActivity.class);
            }

            startActivity(intent);
            finish();
        } else {
            // User is not logged in, show LoginFragment
            Log.d(TAG, "loadInitialFragment: User is not logged in, loading LoginFragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(binding.fragmentContainer.getId(), new LoginFragment())
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Activity is becoming visible");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Activity is now in foreground and interactive");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Activity is losing focus");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Activity is no longer visible");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Activity is being destroyed");
        // Clean up any resources if needed
        tokenManager = null;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: Saving instance state");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState: Restoring instance state");
        // Restore any saved state here if needed
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart: Activity is restarting after being stopped");
    }
}