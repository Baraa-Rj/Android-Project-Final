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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.models.EmployeeBooking;
import com.example.myapplication.ui.Employee.viewmodel.EmployeeJobViewModel;
import com.example.myapplication.ui.employee.adapter.EmployeeBookingAdapter;
import com.example.myapplication.ui.employee.viewmodel.EmployeeViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EmployeeJobsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EmployeeJobsFragment extends Fragment implements EmployeeBookingAdapter.OnJobClickListener {
    private RecyclerView jobsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private ChipGroup statusChipGroup;
    private Chip chipAll, chipPending, chipInProgress, chipCompleted;

    private EmployeeBookingAdapter adapter;
    private EmployeeJobViewModel viewModel;
    private String currentFilter = null; // null means all

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employee_jobs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        setupChipFilters();
        setupObservers();

        // Load all jobs initially
        viewModel.loadBookings(null);
    }

    private void initViews(View view) {
        jobsRecyclerView = view.findViewById(R.id.jobsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);
        statusChipGroup = view.findViewById(R.id.statusChipGroup);
        chipAll = view.findViewById(R.id.chipAll);
        chipPending = view.findViewById(R.id.chipPending);
        chipInProgress = view.findViewById(R.id.chipInProgress);
        chipCompleted = view.findViewById(R.id.chipCompleted);

        viewModel = new ViewModelProvider(this).get(EmployeeViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new EmployeeBookingAdapter(this);
        jobsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        jobsRecyclerView.setAdapter(adapter);
    }

    private void setupChipFilters() {
        chipAll.setOnClickListener(v -> filterJobs(null));
        chipPending.setOnClickListener(v -> filterJobs("ASSIGNED"));
        chipInProgress.setOnClickListener(v -> filterJobs("IN_PROGRESS"));
        chipCompleted.setOnClickListener(v -> filterJobs("COMPLETED"));
    }

    private void filterJobs(String status) {
        currentFilter = status;
        viewModel.loadBookings(status);
    }

    private void setupObservers() {
        viewModel.getBookingsLiveData().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings != null) {
                if (bookings.isEmpty()) {
                    showEmpty(true);
                } else {
                    showEmpty(false);
                    adapter.setBookings(bookings);
                }
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                String errorMessage = error.hasGeneralError() ?
                        error.getGeneralError() : "Failed to load jobs";
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                showLoading(isLoading);
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        jobsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean show) {
        emptyText.setVisibility(show ? View.VISIBLE : View.GONE);
        jobsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onJobClick(EmployeeBooking booking) {
        Intent intent = new Intent(requireContext(), EmployeeJobDetailActivity.class);
        intent.putExtra(EmployeeJobDetailActivity.EXTRA_BOOKING_ID, booking.getId());
        intent.putExtra(EmployeeJobDetailActivity.EXTRA_SERVICE_NAME, booking.getServiceName());
        intent.putExtra(EmployeeJobDetailActivity.EXTRA_CAR_MODEL, booking.getCarModel());
        intent.putExtra(EmployeeJobDetailActivity.EXTRA_SCHEDULED_TIME, booking.getScheduledTime());
        intent.putExtra(EmployeeJobDetailActivity.EXTRA_LOCATION, booking.getLocation());
        intent.putExtra(EmployeeJobDetailActivity.EXTRA_STATUS, booking.getStatus());
        intent.putExtra(EmployeeJobDetailActivity.EXTRA_CUSTOMER_NAME, booking.getCustomerName());
        intent.putExtra(EmployeeJobDetailActivity.EXTRA_CUSTOMER_PHONE, booking.getCustomerPhone());
        intent.putExtra(EmployeeJobDetailActivity.EXTRA_NOTES, booking.getNotes());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh jobs when returning to fragment
        viewModel.loadBookings(currentFilter);
    }
}