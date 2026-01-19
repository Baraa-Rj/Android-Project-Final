package com.example.myapplication.ui.customer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityCustomerBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private ActivityCustomerBinding binding;
    private Fragment homeFragment;
    private Fragment servicesFragment;
    private Fragment bookingsFragment;
    private Fragment profileFragment;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCustomerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initViews();
        initFragments();
        setupBottomNavigation();

    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
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
                .add(binding.fragmentContainer.getId(), profileFragment, "profile").hide(profileFragment)
                .add(binding.fragmentContainer.getId(), bookingsFragment, "bookings").hide(bookingsFragment)
                .add(binding.fragmentContainer.getId(), servicesFragment, "service").hide(servicesFragment)
                .add(binding.fragmentContainer.getId(), homeFragment, "home").commit();
    }

    private void switchFragment(Fragment newFragment) {
        getSupportFragmentManager().beginTransaction().hide(activeFragment).show(newFragment).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit();
        activeFragment = newFragment;
    }

    private void initViews() {
        // View binding handles view initialization
    }

    /**
     * Navigate to Services tab (called from HomeFragment)
     */
    public void navigateToServices() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_services);
    }

    /**
     * Navigate to Bookings tab (called from HomeFragment)
     */
    public void navigateToBookings() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_bookings);
    }
}
