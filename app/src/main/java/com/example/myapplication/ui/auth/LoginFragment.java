package com.example.myapplication.ui.auth;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.myapplication.R;
import com.example.myapplication.ui.customer.CustomerActivity;
import com.google.android.material.textfield.TextInputEditText;
import android.content.Intent;

public class LoginFragment extends Fragment {
    private static final String TAG = "LoginFragment";
    private static final String KEY_EMAIL = "key_email";
    private static final String KEY_PASSWORD = "key_password";

    private AuthViewModel authViewModel;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private Button loginButton;
    private TextView registerTextView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: Fragment attached to activity");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Fragment is being created");
        // Initialize ViewModel early to survive configuration changes
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Creating fragment view");
        View view = inflater.inflate(R.layout.fragment_auth_login, container, false);
        initializeUIElements(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: View hierarchy created");

        setupClickListeners();
        observeViewModel();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());
        registerTextView.setOnClickListener(v -> navigateToRegister());
    }

    private void observeViewModel() {
       authViewModel.getAuthResponseLiveData().observe(getViewLifecycleOwner(), authResponse -> {
    if (authResponse != null) {
        Log.d(TAG, "Login successful for user: " + authResponse.getEmail());
        Toast.makeText(getContext(), "Login Successful!", Toast.LENGTH_SHORT).show();

        Intent intent;
        String role = authResponse.getRole();
        
        if ("employee".equalsIgnoreCase(role)) {
            intent = new Intent(requireContext(), com.example.myapplication.ui.employee.EmployeeActivity.class);
        } else if ("manager".equalsIgnoreCase(role)) {
            intent = new Intent(requireContext(), com.example.myapplication.ui.manager.ManagerActivity.class);
        } else {
            intent = new Intent(requireContext(), CustomerActivity.class);
        }
        
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

       if (getActivity() != null) {
            getActivity().finish();
        }
    }
});

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
        emailEditText = null;
        passwordEditText = null;
        loginButton = null;
        registerTextView = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Fragment is being destroyed");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: Fragment detached from activity");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: Saving fragment state");

        // Save input fields to survive configuration changes
        if (emailEditText != null && emailEditText.getText() != null) {
            outState.putString(KEY_EMAIL, emailEditText.getText().toString());
        }
        if (passwordEditText != null && passwordEditText.getText() != null) {
            outState.putString(KEY_PASSWORD, passwordEditText.getText().toString());
        }
    }

    private void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d(TAG, "restoreInstanceState: Restoring fragment state");

        String savedEmail = savedInstanceState.getString(KEY_EMAIL);
        String savedPassword = savedInstanceState.getString(KEY_PASSWORD);

        if (savedEmail != null && emailEditText != null) {
            emailEditText.setText(savedEmail);
        }
        if (savedPassword != null && passwordEditText != null) {
            passwordEditText.setText(savedPassword);
        }
    }

    public void initializeUIElements(View view) {
        emailEditText = view.findViewById(R.id.emailEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        loginButton = view.findViewById(R.id.loginButton);
        registerTextView = view.findViewById(R.id.registerLink);
    }

    private void attemptLogin() {
        // Clear previous errors
        emailEditText.setError(null);
        passwordEditText.setError(null);

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Client-side validation
        boolean hasError = false;

        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            hasError = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Please enter a valid email address");
            hasError = true;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            hasError = true;
        }

        if (hasError) {
            return;
        }

        authViewModel.login(email, password);
    }

    private void navigateToRegister() {
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new RegisterFragment())
                .addToBackStack(null)
                .commit();
    }
}
