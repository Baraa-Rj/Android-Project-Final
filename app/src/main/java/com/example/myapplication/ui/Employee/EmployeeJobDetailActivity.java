package com.example.myapplication.ui.Employee;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityEmployeeJobDetailBinding;
import com.example.myapplication.ui.employee.viewmodel.EmployeeViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EmployeeJobDetailActivity extends AppCompatActivity {
    private static final String TAG = "EmployeeJobDetail";

    // Intent extras
    public static final String EXTRA_BOOKING_ID = "booking_id";
    public static final String EXTRA_SERVICE_NAME = "service_name";
    public static final String EXTRA_CAR_MODEL = "car_model";
    public static final String EXTRA_SCHEDULED_TIME = "scheduled_time";
    public static final String EXTRA_LOCATION = "location";
    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_CUSTOMER_NAME = "customer_name";
    public static final String EXTRA_CUSTOMER_PHONE = "customer_phone";
    public static final String EXTRA_NOTES = "notes";

    private ActivityEmployeeJobDetailBinding binding;
    private EmployeeViewModel viewModel;

    private int bookingId;
    private String status;
    private String location;
    private String customerPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmployeeJobDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(EmployeeViewModel.class);

        loadBookingData();
        setupToolbar();
        setupClickListeners();
        setupObservers();
        updateUIBasedOnStatus();
    }

    private void loadBookingData() {
        bookingId = getIntent().getIntExtra(EXTRA_BOOKING_ID, -1);
        String serviceName = getIntent().getStringExtra(EXTRA_SERVICE_NAME);
        String carModel = getIntent().getStringExtra(EXTRA_CAR_MODEL);
        String scheduledTime = getIntent().getStringExtra(EXTRA_SCHEDULED_TIME);
        location = getIntent().getStringExtra(EXTRA_LOCATION);
        status = getIntent().getStringExtra(EXTRA_STATUS);
        String customerName = getIntent().getStringExtra(EXTRA_CUSTOMER_NAME);
        customerPhone = getIntent().getStringExtra(EXTRA_CUSTOMER_PHONE);
        String notes = getIntent().getStringExtra(EXTRA_NOTES);

        // Set data to views
        binding.serviceNameText.setText(serviceName != null ? serviceName : "N/A");
        binding.carModelText.setText(carModel != null ? carModel : "N/A");
        binding.scheduledTimeText.setText(formatDateTime(scheduledTime));
        binding.locationText.setText(location != null ? location : "N/A");
        binding.customerNameText.setText(customerName != null ? customerName : "N/A");
        binding.customerPhoneText.setText(customerPhone != null ? customerPhone : "N/A");

        // Handle notes
        if (notes != null && !notes.trim().isEmpty()) {
            binding.notesText.setText(notes);
            binding.notesText.setVisibility(View.VISIBLE);
            binding.notesLabel.setVisibility(View.VISIBLE);
        } else {
            binding.notesText.setVisibility(View.GONE);
            binding.notesLabel.setVisibility(View.GONE);
        }

        // Set status chip
        binding.statusChip.setText(capitalizeStatus(status));
        binding.statusChip.setBackgroundColor(getStatusColor(status));
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Job #" + bookingId);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        binding.startJobButton.setOnClickListener(v -> updateStatus("IN_PROGRESS"));
        binding.completeJobButton.setOnClickListener(v -> showCompleteJobDialog());
        binding.callCustomerButton.setOnClickListener(v -> callCustomer());
        binding.navigateButton.setOnClickListener(v -> navigateToLocation());
    }

    private void setupObservers() {
        viewModel.getStatusUpdatedLiveData().observe(this, updated -> {
            if (updated != null && updated) {
                Toast.makeText(this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                finish(); // Return to jobs list
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                String errorMessage = error.hasGeneralError() ?
                        error.getGeneralError() : "Failed to update status";
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getLoadingLiveData().observe(this, isLoading -> {
            if (isLoading != null) {
                showLoading(isLoading);
            }
        });
    }

    private void updateUIBasedOnStatus() {
        if ("ASSIGNED".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status)) {
            binding.startJobButton.setVisibility(View.VISIBLE);
            binding.completeJobButton.setVisibility(View.GONE);
        } else if ("IN_PROGRESS".equalsIgnoreCase(status)) {
            binding.startJobButton.setVisibility(View.GONE);
            binding.completeJobButton.setVisibility(View.VISIBLE);
        } else {
            binding.startJobButton.setVisibility(View.GONE);
            binding.completeJobButton.setVisibility(View.GONE);
        }
    }

    private void updateStatus(String newStatus) {
        new AlertDialog.Builder(this)
                .setTitle("Update Status")
                .setMessage("Are you sure you want to start this job?")
                .setPositiveButton("Yes", (dialog, which) ->
                        viewModel.updateBookingStatus(bookingId, newStatus))
                .setNegativeButton("No", null)
                .show();
    }

    private void showCompleteJobDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_complete_job, null);
        TextInputEditText notesEditText = dialogView.findViewById(R.id.completionNotesEditText);

        new AlertDialog.Builder(this)
                .setTitle("Complete Job")
                .setView(dialogView)
                .setPositiveButton("Complete", (dialog, which) -> {
                    // In a real app, you'd send these notes to the API
                    viewModel.updateBookingStatus(bookingId, "COMPLETED");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void callCustomer() {
        if (customerPhone != null && !customerPhone.isEmpty()) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + customerPhone));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Customer phone not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToLocation() {
        if (location != null && !location.isEmpty()) {
            // Open Google Maps with the location
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(location));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                // Fallback to browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(location)));
                startActivity(browserIntent);
            }
        } else {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        binding.startJobButton.setEnabled(!show);
        binding.completeJobButton.setEnabled(!show);
    }

    private String formatDateTime(String isoDateTime) {
        if (isoDateTime == null || isoDateTime.isEmpty()) {
            return "N/A";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.US);
            return outputFormat.format(inputFormat.parse(isoDateTime));
        } catch (Exception e) {
            return isoDateTime;
        }
    }

    private String capitalizeStatus(String status) {
        if (status == null || status.isEmpty()) {
            return "Unknown";
        }
        return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase().replace("_", " ");
    }

    private int getStatusColor(String status) {
        if (status == null) {
            return getResources().getColor(R.color.text_secondary);
        }

        switch (status.toUpperCase()) {
            case "PENDING":
            case "ASSIGNED":
                return getResources().getColor(R.color.status_assigned);
            case "IN_PROGRESS":
                return getResources().getColor(R.color.status_in_progress);
            case "COMPLETED":
                return getResources().getColor(R.color.status_completed);
            default:
                return getResources().getColor(R.color.text_secondary);
        }
    }
}