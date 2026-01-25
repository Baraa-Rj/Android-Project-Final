package com.example.myapplication.ui.employee;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class EmployeeActivity extends AppCompatActivity {
    private static final String TAG = "EmployeeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee);

        setupBottomNavigation();

        // Load default fragment on first launch
        if (savedInstanceState == null) {
            loadFragment(new EmployeeHomeFragment());
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_employee_home) {
                selectedFragment = new EmployeeHomeFragment();
            } else if (itemId == R.id.nav_employee_bookings) {
                selectedFragment = new EmployeeBookingsFragment();
            } else if (itemId == R.id.nav_employee_team) {
                selectedFragment = new EmployeeTeamFragment();
            } else if (itemId == R.id.nav_employee_profile) {
                selectedFragment = new EmployeeProfileFragment();
            }

            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
            return true;
        }
        return false;
    }
}
