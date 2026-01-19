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
import com.example.myapplication.ui.customer.adapter.CarAdapter;
import com.example.myapplication.ui.customer.viewmodel.CarListViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

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
    private AlertDialog currentDialog;

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
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
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

        // Get references from the SAME inflated view
        TextInputEditText modelEditText = dialogView.findViewById(R.id.modelEditText);
        TextInputEditText plateNumberEditText = dialogView.findViewById(R.id.plateNumberEditText);
        TextInputEditText colorEditText = dialogView.findViewById(R.id.colorEditText);
        TextInputEditText yearEditText = dialogView.findViewById(R.id.yearEditText);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);
        MaterialButton addButton = dialogView.findViewById(R.id.addButton);

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
            viewModel.addCar(carRequest);
        });

        currentDialog.show();
    }
}
