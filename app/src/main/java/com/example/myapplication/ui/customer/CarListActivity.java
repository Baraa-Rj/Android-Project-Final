package com.example.myapplication.ui.customer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.data.models.Car;
import com.example.myapplication.data.models.CarRequest;
import com.example.myapplication.data.models.ValidationError;
import com.example.myapplication.ui.customer.adapter.CarAdapter;
import com.example.myapplication.ui.customer.viewmodel.CarListViewModel;
import com.example.myapplication.utils.TokenManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CarListActivity extends AppCompatActivity implements CarAdapter.onCarDeleteListener {
    private static final String TAG = "CarListActivity";
    private ListView carsListView;
    private LinearLayout emptyState;
    private FloatingActionButton fabAddCar;
    private Toolbar toolbar;

    private CarAdapter carAdapter;
    private final List<Car> carList = new ArrayList<>();
    private CarListViewModel viewModel;
    private TokenManager tokenManager;
    private AlertDialog currentDialog;

    // Dialog field references for error handling
    private TextInputLayout modelInputLayout;
    private TextInputLayout plateNumberInputLayout;
    private TextInputLayout colorInputLayout;
    private TextInputLayout yearInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);
        initViews();
        setupToolbar();
        setupAdapter();
        setupListeners();
        setupObservers();
        viewModel.loadCars();
    }

    private void setupObservers() {
        viewModel.getCarsLiveData().observe(this, cars -> {
            if (cars != null) {
                carList.clear();
                carList.addAll(cars);
                carAdapter.updateCars(carList);
                updateEmptyState();
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                handleValidationError(error);
            }
        });

        viewModel.getCarAddedLiveData().observe(this, added -> {
            if (added != null && added) {
                Toast.makeText(this, "Car added successfully", Toast.LENGTH_SHORT).show();
                if (currentDialog != null) {
                    currentDialog.dismiss();
                    currentDialog = null;
                }
            }
        });

        viewModel.getCarDeletedLiveData().observe(this, deleted -> {
            if (deleted != null && deleted) {
                Toast.makeText(this, "Car deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyState() {
        if (carList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            carsListView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            carsListView.setVisibility(View.VISIBLE);
        }
    }


    private void initViews() {
        carsListView = findViewById(R.id.carsListView);
        emptyState = findViewById(R.id.emptyState);
        fabAddCar = findViewById(R.id.fabAddCar);
        toolbar = findViewById(R.id.toolbar);
        viewModel = new ViewModelProvider(this).get(CarListViewModel.class);
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

    private void setupAdapter() {
        carAdapter = new CarAdapter(this, carList, this);
        carsListView.setAdapter(carAdapter);
    }

    private void setupListeners() {
        fabAddCar.setOnClickListener(v -> showAddCarDialog());

    }

    @Override
    public void onCarDelete(Car car, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Car")
                .setMessage("Are you sure you want to delete " + car.getModel() + "?")
                .setPositiveButton("Delete", (dialog, which) -> deleteCar(car, position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCar(Car car, int position) {
        viewModel.deleteCar(car.getId());
    }

    private void showAddCarDialog() {
        // Inflate dialog view ONCE
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_car, null);

        // Get references to TextInputLayouts for error handling
        modelInputLayout = dialogView.findViewById(R.id.modelInputLayout);
        plateNumberInputLayout = dialogView.findViewById(R.id.plateNumberInputLayout);
        colorInputLayout = dialogView.findViewById(R.id.colorInputLayout);
        yearInputLayout = dialogView.findViewById(R.id.yearInputLayout);

        // Get references from the SAME inflated view
        TextInputEditText modelEditText = dialogView.findViewById(R.id.modelEditText);
        TextInputEditText plateNumberEditText = dialogView.findViewById(R.id.plateNumberEditText);
        TextInputEditText colorEditText = dialogView.findViewById(R.id.colorEditText);
        TextInputEditText yearEditText = dialogView.findViewById(R.id.yearEditText);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);
        MaterialButton addButton = dialogView.findViewById(R.id.addButton);

        // Clear any previous errors
        clearFieldErrors();

        // Create dialog with the inflated view
        currentDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        cancelButton.setOnClickListener(v -> {
            if (currentDialog != null) {
                currentDialog.dismiss();
                currentDialog = null;
            }
        });

        addButton.setOnClickListener(v -> {
            // Clear previous errors
            clearFieldErrors();

            String model = Objects.requireNonNull(modelEditText.getText()).toString().trim();
            String plateNumber = Objects.requireNonNull(plateNumberEditText.getText()).toString().trim();
            String color = Objects.requireNonNull(colorEditText.getText()).toString().trim();
            String yearStr = Objects.requireNonNull(yearEditText.getText()).toString().trim();

            // Client-side validation
            if (model.isEmpty()) {
                modelInputLayout.setError("Model is required");
                modelInputLayout.setErrorEnabled(true);
                return;
            }
            if (plateNumber.isEmpty()) {
                plateNumberInputLayout.setError("Plate number is required");
                plateNumberInputLayout.setErrorEnabled(true);
                return;
            }
            if (color.isEmpty()) {
                colorInputLayout.setError("Color is required");
                colorInputLayout.setErrorEnabled(true);
                return;
            }

            // Parse year (optional)
            Integer year = null;
            if (!yearStr.isEmpty()) {
                try {
                    year = Integer.parseInt(yearStr);
                } catch (NumberFormatException e) {
                    yearInputLayout.setError("Invalid year");
                    yearInputLayout.setErrorEnabled(true);
                    return;
                }
            }

            // Use CarRequest (not Car) for API
            int userId = tokenManager.getUserId();
            CarRequest carRequest = new CarRequest(userId, model, plateNumber, color, year);
            viewModel.addCar(carRequest);
        });

        currentDialog.show();
    }

    private void handleValidationError(ValidationError error) {
        // Clear all previous errors
        clearFieldErrors();

        // Show field-specific errors
        if (error.hasFieldErrors()) {
            for (String field : error.getAllFieldErrors().keySet()) {
                String errorMessage = error.getFieldError(field);
                showFieldError(field, errorMessage);
            }
        }

        // Show general error as toast
        if (error.hasGeneralError()) {
            Toast.makeText(this, error.getGeneralError(), Toast.LENGTH_LONG).show();
        }
        // If only field errors, show a generic toast
        else if (error.hasFieldErrors()) {
            Toast.makeText(this, "Please correct the highlighted fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFieldError(String field, String errorMessage) {
        switch (field) {
            case "model":
                if (modelInputLayout != null) {
                    modelInputLayout.setError(errorMessage);
                    modelInputLayout.setErrorEnabled(true);
                }
                break;
            case "plate_number":
            case "plateNumber":
                if (plateNumberInputLayout != null) {
                    plateNumberInputLayout.setError(errorMessage);
                    plateNumberInputLayout.setErrorEnabled(true);
                }
                break;
            case "color":
                if (colorInputLayout != null) {
                    colorInputLayout.setError(errorMessage);
                    colorInputLayout.setErrorEnabled(true);
                }
                break;
            case "year":
                if (yearInputLayout != null) {
                    yearInputLayout.setError(errorMessage);
                    yearInputLayout.setErrorEnabled(true);
                }
                break;
            default:
                // Unknown field, show in toast
                Toast.makeText(this, field + ": " + errorMessage, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void clearFieldErrors() {
        if (modelInputLayout != null) {
            modelInputLayout.setError(null);
            modelInputLayout.setErrorEnabled(false);
        }
        if (plateNumberInputLayout != null) {
            plateNumberInputLayout.setError(null);
            plateNumberInputLayout.setErrorEnabled(false);
        }
        if (colorInputLayout != null) {
            colorInputLayout.setError(null);
            colorInputLayout.setErrorEnabled(false);
        }
        if (yearInputLayout != null) {
            yearInputLayout.setError(null);
            yearInputLayout.setErrorEnabled(false);
        }
    }
}
