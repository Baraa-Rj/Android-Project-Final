package com.example.myapplication.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

public class CustomerProfileFragment extends Fragment {

    private TextView userName;
    private TextView userEmail;
    private LinearLayout myCarsItem;
    private LinearLayout walletItem;
    private MaterialButton logoutButton;

    private TokenManager tokenManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = TokenManager.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupUserInfo();
        setupClickListeners();
    }

    private void initViews(View view) {
        userName = view.findViewById(R.id.userName);
        userEmail = view.findViewById(R.id.userEmail);
        myCarsItem = view.findViewById(R.id.myCarsItem);
        walletItem = view.findViewById(R.id.walletItem);
        logoutButton = view.findViewById(R.id.logoutButton);
    }

    private void setupUserInfo() {
        String email = tokenManager.getUserEmail();
        if (email != null) {
            userEmail.setText(email);
            // Extract name from email (before @)
            String name = email.split("@")[0];
            userName.setText(capitalizeFirst(name));
        }
    }

    private void setupClickListeners() {
        myCarsItem.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CarListActivity.class);
            startActivity(intent);
        });

        walletItem.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), WalletActivity.class);
            startActivity(intent);
        });

        // Notifications feature removed - requires backend support
        // TODO: Implement notifications when backend API is ready

        logoutButton.setOnClickListener(v -> logout());
    }

    private void logout() {
        // Clear token
        tokenManager.clearToken();

        // Navigate to MainActivity (login screen)
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finish current activity
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
