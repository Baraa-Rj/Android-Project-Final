package com.example.myapplication.ui.auth;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
import com.google.android.material.textfield.TextInputEditText;
import com.example.myapplication.ui.home.HomeFragment;

public class RegisterFragment extends Fragment {
    private static final String TAG = "RegisterFragment";
    private static final String KEY_NAME = "key_name";
    private static final String KEY_EMAIL = "key_email";
    private static final String KEY_PHONE = "key_phone";
    private static final String KEY_PASSWORD = "key_password";

    private AuthViewModel authViewModel;
    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText phoneEditText;
    private TextInputEditText passwordEditText;
    private Button registerButton;
    private TextView loginTextView;

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
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        initializeUIElements(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: View hierarchy created");

        setupClickListeners();
        observeViewModel();

        // Restore saved state if available
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }
    }

    private void setupClickListeners() {
        registerButton.setOnClickListener(v -> attemptRegister());
        loginTextView.setOnClickListener(v -> navigateToLogin());
    }

    private void observeViewModel() {
        authViewModel.getAuthResponseLiveData().observe(getViewLifecycleOwner(), authResponse -> {
            if (authResponse != null) {
                Log.d(TAG, "Registration successful for user: " + authResponse.getEmail());
                Toast.makeText(getContext(), "Registration Successful!", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        });

        authViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Log.e(TAG, "Registration error: " + errorMessage);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            // Disable button during loading to prevent multiple submissions
            registerButton.setEnabled(!isLoading);
            Log.d(TAG, "Loading state: " + isLoading);
        });
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
        nameEditText = null;
        emailEditText = null;
        phoneEditText = null;
        passwordEditText = null;
        registerButton = null;
        loginTextView = null;
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
        if (nameEditText != null && nameEditText.getText() != null) {
            outState.putString(KEY_NAME, nameEditText.getText().toString());
        }
        if (emailEditText != null && emailEditText.getText() != null) {
            outState.putString(KEY_EMAIL, emailEditText.getText().toString());
        }
        if (phoneEditText != null && phoneEditText.getText() != null) {
            outState.putString(KEY_PHONE, phoneEditText.getText().toString());
        }
        if (passwordEditText != null && passwordEditText.getText() != null) {
            outState.putString(KEY_PASSWORD, passwordEditText.getText().toString());
        }
    }

    private void restoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d(TAG, "restoreInstanceState: Restoring fragment state");

        String savedName = savedInstanceState.getString(KEY_NAME);
        String savedEmail = savedInstanceState.getString(KEY_EMAIL);
        String savedPhone = savedInstanceState.getString(KEY_PHONE);
        String savedPassword = savedInstanceState.getString(KEY_PASSWORD);

        if (savedName != null && nameEditText != null) {
            nameEditText.setText(savedName);
        }
        if (savedEmail != null && emailEditText != null) {
            emailEditText.setText(savedEmail);
        }
        if (savedPhone != null && phoneEditText != null) {
            phoneEditText.setText(savedPhone);
        }
        if (savedPassword != null && passwordEditText != null) {
            passwordEditText.setText(savedPassword);
        }
    }

    private void initializeUIElements(View view) {
        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        phoneEditText = view.findViewById(R.id.phoneEditText);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        registerButton = view.findViewById(R.id.registerButton);
        loginTextView = view.findViewById(R.id.loginLink);
    }

    private void attemptRegister() {
        // Clear previous errors
        nameEditText.setError(null);
        emailEditText.setError(null);
        phoneEditText.setError(null);
        passwordEditText.setError(null);

        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Client-side validation
        boolean hasError = false;

        if (name.isEmpty()) {
            nameEditText.setError("Name is required");
            hasError = true;
        } else if (name.length() < 2) {
            nameEditText.setError("Name must be at least 2 characters");
            hasError = true;
        }

        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            hasError = true;
        }

        if (phone.isEmpty()) {
            phoneEditText.setError("Phone is required");
            hasError = true;
        } else if (phone.length() < 10) {
            phoneEditText.setError("Phone must be at least 10 digits");
            hasError = true;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            hasError = true;
        } else if (password.length() < 8) {
            passwordEditText.setError("Password must be at least 8 characters");
            hasError = true;
        } else if (!password.matches(".*[a-zA-Z].*")) {
            passwordEditText.setError("Password must contain at least one letter");
            hasError = true;
        } else if (!password.matches(".*\\d.*")) {
            passwordEditText.setError("Password must contain at least one number");
            hasError = true;
        }

        if (hasError) {
            return;
        }

        authViewModel.register(name, email, phone, password);
    }

    private void navigateToLogin() {
        getParentFragmentManager().popBackStack();
    }
}