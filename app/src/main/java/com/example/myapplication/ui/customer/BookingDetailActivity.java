package com.example.myapplication.ui.customer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.myapplication.data.api.VolleyClient;
import com.example.myapplication.data.models.Booking;
import com.example.myapplication.databinding.ActivityBookingDetailBinding;
import com.example.myapplication.utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

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

    // Constants
    private static final String BASE_URL = "http://10.0.2.2:8000/api/bookings/";
    private static final String REQUEST_TAG = "BookingDetailActivity";

    // View Binding
    private ActivityBookingDetailBinding binding;

    // Volley
    private RequestQueue requestQueue;
    private TokenManager tokenManager;

    // Data
    private int bookingId;
    private String status;
    private String currentScheduledTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestQueue = VolleyClient.getInstance(this).getRequestQueue();
        tokenManager = new TokenManager(this);

        initViews();
        loadBookingData();
        setupToolbar();
        updateUIBasedOnStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(REQUEST_TAG);
        }
    }

    private void initViews() {
        binding.cancelButton.setOnClickListener(v -> handleCancelBooking());
        binding.rescheduleButton.setOnClickListener(v -> handleRescheduleBooking());
    }

    private void loadBookingData() {
        bookingId = getIntent().getIntExtra(EXTRA_BOOKING_ID, -1);
        String serviceName = getIntent().getStringExtra(EXTRA_SERVICE_NAME);
        String carModel = getIntent().getStringExtra(EXTRA_CAR_MODEL);
        currentScheduledTime = getIntent().getStringExtra(EXTRA_SCHEDULED_TIME);
        String location = getIntent().getStringExtra(EXTRA_LOCATION);
        status = getIntent().getStringExtra(EXTRA_STATUS);
        double totalPrice = getIntent().getDoubleExtra(EXTRA_TOTAL_PRICE, 0.0);
        String notes = getIntent().getStringExtra(EXTRA_NOTES);

        // Set data to views
        binding.serviceNameText.setText(serviceName != null ? serviceName : "N/A");
        binding.carModelText.setText(carModel != null ? carModel : "N/A");
        binding.scheduledTimeText.setText(formatDateTime(currentScheduledTime));
        binding.locationText.setText(location != null ? location : "N/A");
        binding.priceText.setText(formatPrice(totalPrice));

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
            getSupportActionBar().setTitle("Booking #" + bookingId);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void updateUIBasedOnStatus() {
        // Only show cancel/reschedule buttons for PENDING bookings
        if ("PENDING".equalsIgnoreCase(status)) {
            binding.cancelButton.setVisibility(View.VISIBLE);
            binding.rescheduleButton.setVisibility(View.VISIBLE);
        } else {
            binding.cancelButton.setVisibility(View.GONE);
            binding.rescheduleButton.setVisibility(View.GONE);
        }
    }

    private void handleCancelBooking() {
        new AlertDialog.Builder(this)
            .setTitle("Cancel Booking")
            .setMessage("Are you sure you want to cancel this booking? This action cannot be undone.")
            .setPositiveButton("Yes, Cancel", (dialog, which) -> performCancelBooking())
            .setNegativeButton("No", null)
            .show();
    }

    private void performCancelBooking() {
        String url = BASE_URL + bookingId;

        StringRequest request = new StringRequest(
            Request.Method.DELETE, url,
            response -> {
                Toast.makeText(this, "Booking cancelled successfully", Toast.LENGTH_SHORT).show();
                finish(); // Return to bookings list
            },
            error -> {
                String errorMsg = "Failed to cancel booking";
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    try {
                        String errorBody = new String(error.networkResponse.data);
                        JSONObject json = new JSONObject(errorBody);
                        if (json.has("detail")) {
                            errorMsg = json.getString("detail");
                        }
                    } catch (Exception e) {
                        // Use default error message
                    }
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = tokenManager.getToken();
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        request.setTag(REQUEST_TAG);
        requestQueue.add(request);
    }

    private void handleRescheduleBooking() {
        // Parse current scheduled time to set as default
        Calendar currentCalendar = Calendar.getInstance();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date currentDate = sdf.parse(currentScheduledTime);
            if (currentDate != null) {
                currentCalendar.setTime(currentDate);
            }
        } catch (Exception e) {
            // Use current time as fallback
        }

        // Show date picker
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select New Date")
            .setSelection(currentCalendar.getTimeInMillis())
            .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar selectedCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            selectedCalendar.setTimeInMillis(selection);

            // Show time picker after date is selected
            showTimePicker(selectedCalendar, currentCalendar.get(Calendar.HOUR_OF_DAY), currentCalendar.get(Calendar.MINUTE));
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    private void showTimePicker(Calendar selectedDate, int currentHour, int currentMinute) {
        MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(currentHour)
            .setMinute(currentMinute)
            .setTitleText("Select New Time")
            .build();

        timePicker.addOnPositiveButtonClickListener(v -> {
            // Combine date and time
            Calendar finalDateTime = Calendar.getInstance();
            finalDateTime.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR));
            finalDateTime.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH));
            finalDateTime.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH));
            finalDateTime.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            finalDateTime.set(Calendar.MINUTE, timePicker.getMinute());
            finalDateTime.set(Calendar.SECOND, 0);

            // Format to ISO 8601
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            String newScheduledTime = isoFormat.format(finalDateTime.getTime());

            performReschedule(newScheduledTime);
        });

        timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
    }

    private void performReschedule(String newScheduledTime) {
        String url = BASE_URL + bookingId + "/reschedule";

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("scheduled_time", newScheduledTime);
        } catch (JSONException e) {
            Toast.makeText(this, "Failed to create request", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.PUT, url, requestBody,
            response -> {
                try {
                    // Update current scheduled time
                    currentScheduledTime = response.getString("scheduled_time");
                    binding.scheduledTimeText.setText(formatDateTime(currentScheduledTime));
                    Toast.makeText(this, "Booking rescheduled successfully", Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    Toast.makeText(this, "Booking rescheduled successfully", Toast.LENGTH_SHORT).show();
                }
            },
            error -> {
                String errorMsg = "Failed to reschedule booking";
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    try {
                        String errorBody = new String(error.networkResponse.data);
                        JSONObject json = new JSONObject(errorBody);
                        if (json.has("detail")) {
                            errorMsg = json.getString("detail");
                        }
                    } catch (Exception e) {
                        // Use default error message
                    }
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
            }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = tokenManager.getToken();
                if (token != null) {
                    headers.put("Authorization", "Bearer " + token);
                }
                headers.put("Accept", "application/json");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        request.setTag(REQUEST_TAG);
        requestQueue.add(request);
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
