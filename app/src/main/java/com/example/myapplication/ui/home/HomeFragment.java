package com.example.myapplication.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.data.local.TokenManager;
import com.example.myapplication.ui.auth.LoginFragment;

public class HomeFragment extends Fragment {
    private TextView welcomeTextView;
    private Button logoutButton;
    private TextView userEmailTextView;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initializeViews(view);
        setupListeners();
        return view;
    }

    public void initializeViews(View view) {
        welcomeTextView = view.findViewById(R.id.welcomeTextView);
        logoutButton = view.findViewById(R.id.logoutButton);
        userEmailTextView = view.findViewById(R.id.userEmailTextView);
        tokenManager = TokenManager.getInstance(getContext());

        String userEmail = tokenManager.getEmail();
        if (userEmail != null) {
            userEmailTextView.setText(userEmail);
        }
    }

    public void setupListeners() {
        logoutButton.setOnClickListener(v -> {
            tokenManager.clearToken();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        });
    }
}
