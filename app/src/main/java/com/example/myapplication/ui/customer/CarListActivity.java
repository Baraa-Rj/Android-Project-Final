package com.example.myapplication.ui.customer;

import android.os.Bundle;
import android.util.Log;
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
import com.example.myapplication.ui.customer.adapter.CarAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

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
        apiService.getCars().enqueue(new Callback<List<Car>>() {
            @Override
            public void onResponse(@NonNull Call<List<Car>> call, Response<List<Car>> response) {
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
        apiService.deleteCar(car.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
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
                Toast.makeText(CarListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showAddCarDialog() {
        Toast.makeText(this, "Add car coming soon!", Toast.LENGTH_SHORT).show();
    }
}
