package com.example.myapplication.ui.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.data.models.Booking;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class BookingDetailActivity extends AppCompatActivity {
    private static final String TAG = "BookingDetailActivity";

    // Intent extras keys
    public static final String EXTRA_BOOKING_ID = "booking_id";
    public static final String EXTRA_SERVICE_NAME = "service_name";
    public static final String EXTRA_CAR_MODEL = "car_model";
    public static final String EXTRA_SCHEDULED_TIME = "scheduled_time";
    public static final String EXTRA_LOCATION = "location";
    public static final String EXTRA_STATUS = "status";
    public static final String EXTRA_TOTAL_PRICE = "total_price";
    public static final String EXTRA_NOTES = "notes";

    // Views
    private Toolbar toolbar;
    private TextView statusChip;
    private TextView serviceNameText;
    private TextView carModelText;
    private TextView scheduledTimeText;
    private TextView locationText;
    private TextView priceText;
    private TextView notesText;
    private TextView notesLabel;
    private MaterialButton cancelButton;
    private MaterialButton rescheduleButton;

    // Data
    private int bookingId;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        initViews();
        loadBookingData();
        setupToolbar();
        updateUIBasedOnStatus();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        statusChip = findViewById(R.id.statusChip);
        serviceNameText = findViewById(R.id.serviceNameText);
        carModelText = findViewById(R.id.carModelText);
        scheduledTimeText = findViewById(R.id.scheduledTimeText);
        locationText = findViewById(R.id.locationText);
        priceText = findViewById(R.id.priceText);
        notesText = findViewById(R.id.notesText);
        notesLabel = findViewById(R.id.notesLabel);
        cancelButton = findViewById(R.id.cancelButton);
        rescheduleButton = findViewById(R.id.rescheduleButton);

        cancelButton.setOnClickListener(v -> handleCancelBooking());
        rescheduleButton.setOnClickListener(v -> handleRescheduleBooking());
    }

    private void loadBookingData() {
        bookingId = getIntent().getIntExtra(EXTRA_BOOKING_ID, -1);
        String serviceName = getIntent().getStringExtra(EXTRA_SERVICE_NAME);
        String carModel = getIntent().getStringExtra(EXTRA_CAR_MODEL);
        String scheduledTime = getIntent().getStringExtra(EXTRA_SCHEDULED_TIME);
        String location = getIntent().getStringExtra(EXTRA_LOCATION);
        status = getIntent().getStringExtra(EXTRA_STATUS);
        double totalPrice = getIntent().getDoubleExtra(EXTRA_TOTAL_PRICE, 0.0);
        String notes = getIntent().getStringExtra(EXTRA_NOTES);

        // Set data to views
        serviceNameText.setText(serviceName != null ? serviceName : "N/A");
        carModelText.setText(carModel != null ? carModel : "N/A");
        scheduledTimeText.setText(formatDateTime(scheduledTime));
        locationText.setText(location != null ? location : "N/A");
        priceText.setText(formatPrice(totalPrice));

        // Handle notes
        if (notes != null && !notes.trim().isEmpty()) {
            notesText.setText(notes);
            notesText.setVisibility(View.VISIBLE);
            notesLabel.setVisibility(View.VISIBLE);
        } else {
            notesText.setVisibility(View.GONE);
            notesLabel.setVisibility(View.GONE);
        }

        // Set status chip
        statusChip.setText(capitalizeStatus(status));
        statusChip.setBackgroundColor(getStatusColor(status));
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Booking #" + bookingId);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void updateUIBasedOnStatus() {
        // Only show cancel/reschedule buttons for PENDING bookings
        if ("PENDING".equalsIgnoreCase(status)) {
            cancelButton.setVisibility(View.VISIBLE);
            rescheduleButton.setVisibility(View.VISIBLE);
        } else {
            cancelButton.setVisibility(View.GONE);
            rescheduleButton.setVisibility(View.GONE);
        }
    }

    private void handleCancelBooking() {
        // TODO: Implement cancel booking API call
        Toast.makeText(this, "Cancel booking functionality will be implemented", Toast.LENGTH_SHORT).show();
    }

    private void handleRescheduleBooking() {
        // TODO: Implement reschedule booking functionality
        Toast.makeText(this, "Reschedule booking functionality will be implemented", Toast.LENGTH_SHORT).show();
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
            // Fallback: try simpler format without 'T' separator
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.US);
                return outputFormat.format(inputFormat.parse(isoDateTime));
            } catch (Exception ex) {
                return isoDateTime; // Return as-is if parsing fails
            }
        }
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.US);
        return formatter.format(price);
    }

    private String capitalizeStatus(String status) {
        if (status == null || status.isEmpty()) {
            return "Unknown";
        }
        return status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
    }

    private int getStatusColor(String status) {
        if (status == null) {
            return getResources().getColor(R.color.text_secondary);
        }

        switch (status.toUpperCase()) {
            case "PENDING":
                return getResources().getColor(R.color.status_pending);
            case "ASSIGNED":
                return getResources().getColor(R.color.status_assigned);
            case "IN_PROGRESS":
                return getResources().getColor(R.color.status_in_progress);
            case "COMPLETED":
                return getResources().getColor(R.color.status_completed);
            case "CANCELLED":
                return getResources().getColor(R.color.status_cancelled);
            default:
                return getResources().getColor(R.color.text_secondary);
        }
    }
}
