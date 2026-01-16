package com.example.myapplication.ui.customer;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.data.api.ApiService;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.models.Booking;
import com.example.myapplication.data.models.BookingRequest;
import com.example.myapplication.data.models.Car;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.example.myapplication.utils.TokenManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingActivity extends AppCompatActivity {
    private static final String TAG = "BookingActivity";

    // Intent extras keys
    public static final String EXTRA_SERVICE_ID = "service_id";
    public static final String EXTRA_SERVICE_NAME = "service_name";
    public static final String EXTRA_SERVICE_DESCRIPTION = "service_description";
    public static final String EXTRA_SERVICE_PRICE = "service_price";
    public static final String EXTRA_SERVICE_DURATION = "service_duration";

    // Views
    private Toolbar toolbar;
    private TextView serviceName;
    private TextView serviceDescription;
    private TextView servicePrice;
    private TextView serviceDuration;
    private Spinner carSpinner;
    private TextView noCarsWarning;
    private MaterialButton dateButton;
    private MaterialButton timeButton;
    private TextInputEditText locationEditText;
    private TextInputEditText notesEditText;
    private MaterialButton confirmButton;
    private FrameLayout loadingOverlay;

    // Data
    private ApiService apiService;
    private TokenManager tokenManager;
    private List<Car> carList = new ArrayList<>();
    private int serviceId;
    private double serviceServicePrice;
    private Calendar selectedDateTime = Calendar.getInstance();
    private boolean dateSelected = false;
    private boolean timeSelected = false;

    // Track active calls for cleanup
    private Call<List<Car>> loadCarsCall;
    private Call<Booking> createBookingCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        initViews();
        setupToolbar();
        loadServiceData();
        loadUserCars();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        serviceName = findViewById(R.id.serviceName);
        serviceDescription = findViewById(R.id.serviceDescription);
        servicePrice = findViewById(R.id.servicePrice);
        serviceDuration = findViewById(R.id.serviceDuration);
        carSpinner = findViewById(R.id.carSpinner);
        noCarsWarning = findViewById(R.id.noCarsWarning);
        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);
        locationEditText = findViewById(R.id.locationEditText);
        notesEditText = findViewById(R.id.notesEditText);
        confirmButton = findViewById(R.id.confirmButton);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        apiService = RetrofitClient.getApiService(this);
        tokenManager = TokenManager.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadServiceData() {
        // Get service data from intent
        serviceId = getIntent().getIntExtra(EXTRA_SERVICE_ID, -1);
        String name = getIntent().getStringExtra(EXTRA_SERVICE_NAME);
        String description = getIntent().getStringExtra(EXTRA_SERVICE_DESCRIPTION);
        serviceServicePrice = getIntent().getDoubleExtra(EXTRA_SERVICE_PRICE, 0.0);
        int duration = getIntent().getIntExtra(EXTRA_SERVICE_DURATION, 0);

        if (serviceId == -1) {
            Toast.makeText(this, "Invalid service", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display service info
        serviceName.setText(name != null ? name : "Unknown Service");
        serviceDescription.setText(description != null ? description : "");
        servicePrice.setText(String.format(Locale.US, "$%.2f", serviceServicePrice));
        serviceDuration.setText(String.format(Locale.US, "~%d min", duration));
    }

    private void loadUserCars() {
        loadCarsCall = apiService.getCars();
        loadCarsCall.enqueue(new Callback<List<Car>>() {
            @Override
            public void onResponse(@NonNull Call<List<Car>> call, @NonNull Response<List<Car>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    carList.clear();
                    carList.addAll(response.body());
                    setupCarSpinner();
                } else {
                    Log.e(TAG, "Failed to load cars: " + response.code());
                    showNoCarsWarning();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Car>> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Log.e(TAG, "Error loading cars: " + t.getMessage());
                showNoCarsWarning();
            }
        });
    }

    private void setupCarSpinner() {
        if (carList.isEmpty()) {
            showNoCarsWarning();
            return;
        }

        noCarsWarning.setVisibility(View.GONE);
        carSpinner.setVisibility(View.VISIBLE);

        // Create display list with car model and plate number
        List<String> carDisplayList = new ArrayList<>();
        for (Car car : carList) {
            String display = car.getModel();
            if (car.getYear() != null) {
                display = car.getYear() + " " + display;
            }
            display += " (" + car.getPlateNumber() + ")";
            carDisplayList.add(display);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                carDisplayList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        carSpinner.setAdapter(adapter);
    }

    private void showNoCarsWarning() {
        carSpinner.setVisibility(View.GONE);
        noCarsWarning.setVisibility(View.VISIBLE);
        confirmButton.setEnabled(false);
    }

    private void setupClickListeners() {
        dateButton.setOnClickListener(v -> showDatePicker());
        timeButton.setOnClickListener(v -> showTimePicker());
        confirmButton.setOnClickListener(v -> validateAndBook());
    }

    private void showDatePicker() {
        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.DAY_OF_MONTH, 1); // Minimum is tomorrow

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    dateSelected = true;
                    updateDateButton();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
        dialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    // Restrict to business hours (8 AM - 6 PM)
                    if (hourOfDay < 8 || hourOfDay >= 18) {
                        Toast.makeText(this, "Please select a time between 8 AM and 6 PM", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    timeSelected = true;
                    updateTimeButton();
                },
                9, // Default 9 AM
                0,
                false
        );
        dialog.show();
    }

    private void updateDateButton() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        dateButton.setText(dateFormat.format(selectedDateTime.getTime()));
    }

    private void updateTimeButton() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        timeButton.setText(timeFormat.format(selectedDateTime.getTime()));
    }

    private void validateAndBook() {
        // Validate car selection
        if (carList.isEmpty()) {
            Toast.makeText(this, "Please add a car first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate date
        if (!dateSelected) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate time
        if (!timeSelected) {
            Toast.makeText(this, "Please select a time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate location
        String location = locationEditText.getText() != null ?
                locationEditText.getText().toString().trim() : "";
        if (location.isEmpty()) {
            locationEditText.setError("Location is required");
            return;
        }

        // Get selected car
        int selectedCarPosition = carSpinner.getSelectedItemPosition();
        if (selectedCarPosition < 0 || selectedCarPosition >= carList.size()) {
            Toast.makeText(this, "Please select a car", Toast.LENGTH_SHORT).show();
            return;
        }

        Car selectedCar = carList.get(selectedCarPosition);
        String notes = notesEditText.getText() != null ?
                notesEditText.getText().toString().trim() : null;

        // Format scheduled time for API (ISO 8601 format)
        SimpleDateFormat apiDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        String scheduledTime = apiDateFormat.format(selectedDateTime.getTime());

        // Create booking request
        BookingRequest bookingRequest = new BookingRequest(
                tokenManager.getUserId(),
                selectedCar.getId(),
                serviceId,
                location,
                scheduledTime,
                serviceServicePrice,
                notes
        );

        createBooking(bookingRequest);
    }

    private void createBooking(BookingRequest bookingRequest) {
        showLoading(true);

        createBookingCall = apiService.createBooking(bookingRequest);
        createBookingCall.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(@NonNull Call<Booking> call, @NonNull Response<Booking> response) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Booking created: " + response.body().getId());
                    Toast.makeText(BookingActivity.this, "Booking confirmed!", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Log.e(TAG, "Failed to create booking: " + response.code());
                    Toast.makeText(BookingActivity.this, "Failed to create booking. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Booking> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                showLoading(false);
                Log.e(TAG, "Error creating booking: " + t.getMessage());
                Toast.makeText(BookingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        confirmButton.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel pending Retrofit calls to prevent memory leaks
        if (loadCarsCall != null) loadCarsCall.cancel();
        if (createBookingCall != null) createBookingCall.cancel();
    }
}
