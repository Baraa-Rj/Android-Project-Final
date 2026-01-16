package com.example.myapplication.ui.customer;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.R;
import com.example.myapplication.data.api.ApiService;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.models.Car;
import com.example.myapplication.data.models.CarRequest;
import com.example.myapplication.ui.customer.adapter.CarAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarListActivity extends AppCompatActivity implements CarAdapter.onCarDeleteListener {
    private static final String TAG = "CarListActivity";
    private ListView carsListView;
    private LinearLayout emptyState;
    private FloatingActionButton fabAddCar;
    private Toolbar toolbar;

    private CarAdapter carAdapter;
    private final List<Car> carList = new ArrayList<>();
    private ApiService apiService;

    // Track active calls for cleanup
    private Call<List<Car>> loadCarsCall;
    private Call<Void> deleteCarCall;
    private Call<Car> addCarCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_list);
        initViews();
        setupToolbar();
        setupAdapter();
        setupListeners();
        loadCars();
    }

    private void loadCars() {
        loadCarsCall = apiService.getCars();
        loadCarsCall.enqueue(new Callback<List<Car>>() {
            @Override
            public void onResponse(@NonNull Call<List<Car>> call, @NonNull Response<List<Car>> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    carList.clear();
                    carList.addAll(response.body());
                    carAdapter.updateCars(carList);
                    updateEmptyState();
                    Log.d(TAG, "Loaded " + carList.size() + " cars");
                } else {
                    Log.e(TAG, "Failed to load cars: " + response.code());
                    Toast.makeText(CarListActivity.this, "Failed to load cars", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Car>> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Log.e(TAG, "Error loading cars: " + t.getMessage());
                Toast.makeText(CarListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        apiService = RetrofitClient.getApiService(this);
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
        deleteCarCall = apiService.deleteCar(car.getId());
        deleteCarCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful()) {
                    carAdapter.removeCar(position);
                    updateEmptyState();
                    Toast.makeText(CarListActivity.this, "Car deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(CarListActivity.this, "Failed to delete car", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(CarListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddCarDialog() {
        // Inflate dialog view ONCE
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_car, null);

        // Get references from the SAME inflated view
        TextInputEditText modelEditText = dialogView.findViewById(R.id.modelEditText);
        TextInputEditText plateNumberEditText = dialogView.findViewById(R.id.plateNumberEditText);
        TextInputEditText colorEditText = dialogView.findViewById(R.id.colorEditText);
        TextInputEditText yearEditText = dialogView.findViewById(R.id.yearEditText);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);
        MaterialButton addButton = dialogView.findViewById(R.id.addButton);

        // Create dialog with the inflated view
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        addButton.setOnClickListener(v -> {
            String model = Objects.requireNonNull(modelEditText.getText()).toString().trim();
            String plateNumber = Objects.requireNonNull(plateNumberEditText.getText()).toString().trim();
            String color = Objects.requireNonNull(colorEditText.getText()).toString().trim();
            String yearStr = Objects.requireNonNull(yearEditText.getText()).toString().trim();

            // Validation
            if (model.isEmpty()) {
                modelEditText.setError("Model is required");
                return;
            }
            if (plateNumber.isEmpty()) {
                plateNumberEditText.setError("Plate number is required");
                return;
            }
            if (color.isEmpty()) {
                colorEditText.setError("Color is required");
                return;
            }

            // Parse year (optional)
            Integer year = null;
            if (!yearStr.isEmpty()) {
                try {
                    year = Integer.parseInt(yearStr);
                } catch (NumberFormatException e) {
                    yearEditText.setError("Invalid year");
                    return;
                }
            }

            // Use CarRequest (not Car) for API
            CarRequest carRequest = new CarRequest(model, plateNumber, color, year);
            addCar(carRequest, dialog);
        });

        dialog.show();
    }

    private void addCar(CarRequest carRequest, AlertDialog dialog) {
        addCarCall = apiService.addCar(carRequest);
        addCarCall.enqueue(new Callback<Car>() {
            @Override
            public void onResponse(@NonNull Call<Car> call, @NonNull Response<Car> response) {
                if (isFinishing() || isDestroyed()) return;
                if (response.isSuccessful() && response.body() != null) {
                    carList.add(response.body());
                    carAdapter.updateCars(carList);
                    updateEmptyState();
                    dialog.dismiss();
                    Toast.makeText(CarListActivity.this, "Car added successfully", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Car added: " + response.body().getModel());
                } else {
                    Toast.makeText(CarListActivity.this, "Failed to add car", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to add car: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Car> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed()) return;
                Toast.makeText(CarListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error adding car: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel pending Retrofit calls to prevent memory leaks
        if (loadCarsCall != null) loadCarsCall.cancel();
        if (deleteCarCall != null) deleteCarCall.cancel();
        if (addCarCall != null) addCarCall.cancel();
    }
}
