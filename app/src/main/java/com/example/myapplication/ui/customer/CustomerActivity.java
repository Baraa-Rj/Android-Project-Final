package com.example.myapplication.ui.customer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private Fragment homeFragment;
    private Fragment servicesFragment;
    private Fragment bookingsFragment;
    private Fragment profileFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);
        initViews();
        initFragments();
        setupBottomNavigation();

    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                switchFragment(homeFragment);
                return true;
            } else if (itemId == R.id.nav_services) {
                switchFragment(servicesFragment);
                return true;
            } else if (itemId == R.id.nav_bookings) {
                switchFragment(bookingsFragment);
                return true;
            } else if (itemId == R.id.nav_profile) {
                switchFragment(profileFragment);
                return true;
            }

            return false;
        });
    }

    private void initFragments() {
        homeFragment = new CustomerHomeFragment();
        servicesFragment = new CustomerServicesFragment();
        bookingsFragment = new CustomerBookingsFragment();
        profileFragment = new CustomerProfileFragment();
        activeFragment = homeFragment;

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, profileFragment, "profile").hide(profileFragment)
                .add(R.id.fragment_container, bookingsFragment, "bookings").hide(bookingsFragment)
                .add(R.id.fragment_container, servicesFragment, "service").hide(servicesFragment)
                .add(R.id.fragment_container, homeFragment, "home").commit();
    }

    private void switchFragment(Fragment newFragment) {
        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(newFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        activeFragment = newFragment;
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }
}
