package com.example.myapplication.ui.Employee;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.myapplication.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.Employee.viewmodel.EmployeeJobViewModel;
import com.example.myapplication.ui.employee.viewmodel.EmployeeViewModel;
import com.example.myapplication.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;
/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EmployeeProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EmployeeProfileFragment extends Fragment {


    private TextView userName, userEmail, userPhone;
    private TextView totalJobsText, completedTodayText, ratingText;
    private MaterialButton logoutButton;

    private TokenManager tokenManager;
    private EmployeeJobViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = TokenManager.getInstance(requireContext());
        viewModel = new ViewModelProvider(this).get(EmployeeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employee_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupClickListeners();
        setupObservers();

        // Load profile data
        viewModel.loadEmployeeProfile();
        viewModel.loadEmployeeStats();
    }

    private void initViews(View view) {
        userName = view.findViewById(R.id.userName);
        userEmail = view.findViewById(R.id.userEmail);
        userPhone = view.findViewById(R.id.userPhone);
        totalJobsText = view.findViewById(R.id.totalJobsText);
        completedTodayText = view.findViewById(R.id.completedTodayText);
        ratingText = view.findViewById(R.id.ratingText);
        logoutButton = view.findViewById(R.id.logoutButton);

        // Set initial email from token
        String email = tokenManager.getUserEmail();
        if (email != null) {
            userEmail.setText(email);
        }
    }

    private void setupClickListeners() {
        logoutButton.setOnClickListener(v -> logout());
    }

    private void setupObservers() {
        viewModel.getEmployeeLiveData().observe(getViewLifecycleOwner(), employee -> {
            if (employee != null) {
                userName.setText(employee.getName());
                userEmail.setText(employee.getEmail());
                userPhone.setText(employee.getPhone());

                if (employee.getRating() != null) {
                    ratingText.setText(String.format(Locale.US, "%.1f ⭐", employee.getRating()));
                }
            }
        });

        viewModel.getStatsLiveData().observe(getViewLifecycleOwner(), stats -> {
            if (stats != null) {
                totalJobsText.setText(String.valueOf(stats.getTotalJobs()));
                completedTodayText.setText(String.valueOf(stats.getCompletedToday()));
            }
        });
    }

    private void logout() {
        tokenManager.clearToken();

        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}