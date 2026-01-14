package com.example.myapplication.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.ui.auth.LoginFragment;
import com.example.myapplication.utils.TokenManager;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private TextView welcomeTextView;
    private Button logoutButton;
    private TextView userEmailTextView;
    private TokenManager tokenManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: Fragment attached to activity");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Fragment is being created");
        // Initialize TokenManager early
        tokenManager = TokenManager.getInstance(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Creating fragment view");
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initializeViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: View hierarchy created");
        setupListeners();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Fragment is becoming visible");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Fragment is now interactive");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Fragment is losing focus");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Fragment is no longer visible");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Fragment view is being destroyed");
        // Clean up view references to prevent memory leaks
        welcomeTextView = null;
        logoutButton = null;
        userEmailTextView = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Fragment is being destroyed");
        // Clean up TokenManager reference
        tokenManager = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: Fragment detached from activity");
    }

    private void initializeViews(View view) {
        welcomeTextView = view.findViewById(R.id.welcomeTextView);
        logoutButton = view.findViewById(R.id.logoutButton);
        userEmailTextView = view.findViewById(R.id.userEmailTextView);

        String userEmail = tokenManager.getUserEmail();
        if (userEmail != null) {
            userEmailTextView.setText(userEmail);
            Log.d(TAG, "initializeViews: Displaying email for user: " + userEmail);
        } else {
            Log.w(TAG, "initializeViews: No user email found in token");
        }
    }

    private void setupListeners() {
        logoutButton.setOnClickListener(v -> {
            Log.d(TAG, "Logout button clicked");
            tokenManager.clearToken();
            Log.d(TAG, "Token cleared, navigating to LoginFragment");
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        });
    }
}
