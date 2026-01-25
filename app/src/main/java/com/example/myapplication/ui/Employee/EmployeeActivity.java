package com.example.myapplication.ui.Employee;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityEmployeeBinding;

import com.example.myapplication.R;

public class EmployeeActivity extends AppCompatActivity {
    private ActivityEmployeeBinding binding;
    private Fragment jobsFragment;
    private Fragment profileFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmployeeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initFragments();
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_jobs) {
                switchFragment(jobsFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                switchFragment(profileFragment);
                return true;
            }

            return false;
        });
    }

    private void initFragments() {
        jobsFragment = new EmployeeJobsFragment();
        profileFragment = new EmployeeProfileFragment();
        activeFragment = jobsFragment;

        getSupportFragmentManager().beginTransaction()
                .add(binding.fragmentContainer.getId(), profileFragment, "profile").hide(profileFragment)
                .add(binding.fragmentContainer.getId(), jobsFragment, "jobs").commit();
    }

    private void switchFragment(Fragment newFragment) {
        getSupportFragmentManager().beginTransaction()
                .hide(activeFragment)
                .show(newFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
        activeFragment = newFragment;
    }
}