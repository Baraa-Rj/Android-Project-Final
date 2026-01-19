package com.example.myapplication.ui.customer.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.data.models.Car;
import com.example.myapplication.data.models.CarRequest;
import com.example.myapplication.data.models.ValidationError;
import com.example.myapplication.data.repository.CarRepository;
import com.example.myapplication.utils.ValidationErrorParser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarListViewModel extends AndroidViewModel {
    private static final String TAG = "CarListViewModel";

    private final CarRepository carRepository;
    private final MutableLiveData<List<Car>> carsLiveData = new MutableLiveData<>();
    private final MutableLiveData<ValidationError> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> carAddedLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> carDeletedLiveData = new MutableLiveData<>();

    // Track ongoing calls for cleanup
    private Call<List<Car>> loadCarsCall;
    private Call<Car> addCarCall;
    private Call<Void> deleteCarCall;

    public CarListViewModel(@NonNull Application application) {
        super(application);
        this.carRepository = new CarRepository(application);
        Log.d(TAG, "CarListViewModel initialized");
    }

    // Package-private constructor for testing
    CarListViewModel(@NonNull Application application, CarRepository carRepository) {
        super(application);
        this.carRepository = carRepository;
    }

    public LiveData<List<Car>> getCarsLiveData() {
        return carsLiveData;
    }

    public LiveData<ValidationError> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public LiveData<Boolean> getCarAddedLiveData() {
        return carAddedLiveData;
    }

    public LiveData<Boolean> getCarDeletedLiveData() {
        return carDeletedLiveData;
    }

    public void loadCars() {
        Log.d(TAG, "Loading cars...");
        loadingLiveData.setValue(true);

        loadCarsCall = carRepository.getCars();
        loadCarsCall.enqueue(new Callback<List<Car>>() {
            @Override
            public void onResponse(@NonNull Call<List<Car>> call, @NonNull Response<List<Car>> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Car> cars = response.body();
                    carsLiveData.setValue(cars);
                    Log.d(TAG, "Loaded " + cars.size() + " cars");
                } else {
                    Log.e(TAG, "Failed to load cars: " + response.code());
                    ValidationError validationError = ValidationErrorParser.parseError(response);
                    errorLiveData.setValue(validationError);
                }
                loadCarsCall = null;
            }

            @Override
            public void onFailure(@NonNull Call<List<Car>> call, @NonNull Throwable t) {
                loadingLiveData.setValue(false);
                Log.e(TAG, "Error loading cars: " + t.getMessage());
                ValidationError validationError = new ValidationError();
                validationError.setGeneralError("Connection failed. Please check your internet connection.");
                errorLiveData.setValue(validationError);
                loadCarsCall = null;
            }
        });
    }

    public void addCar(CarRequest carRequest) {
        Log.d(TAG, "Adding car: " + carRequest.getModel());
        loadingLiveData.setValue(true);

        addCarCall = carRepository.addCar(carRequest);
        addCarCall.enqueue(new Callback<Car>() {
            @Override
            public void onResponse(@NonNull Call<Car> call, @NonNull Response<Car> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Car added successfully");
                    carAddedLiveData.setValue(true);
                    // Reload cars to get updated list
                    loadCars();
                } else {
                    Log.e(TAG, "Failed to add car: " + response.code());
                    ValidationError validationError = ValidationErrorParser.parseError(response);
                    errorLiveData.setValue(validationError);
                }
                addCarCall = null;
            }

            @Override
            public void onFailure(@NonNull Call<Car> call, @NonNull Throwable t) {
                loadingLiveData.setValue(false);
                Log.e(TAG, "Error adding car: " + t.getMessage());
                ValidationError validationError = new ValidationError();
                validationError.setGeneralError("Connection failed. Please check your internet connection.");
                errorLiveData.setValue(validationError);
                addCarCall = null;
            }
        });
    }

    public void deleteCar(int carId) {
        Log.d(TAG, "Deleting car ID: " + carId);
        loadingLiveData.setValue(true);

        deleteCarCall = carRepository.deleteCar(carId);
        deleteCarCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful()) {
                    Log.d(TAG, "Car deleted successfully");
                    carDeletedLiveData.setValue(true);
                    // Reload cars to get updated list
                    loadCars();
                } else {
                    Log.e(TAG, "Failed to delete car: " + response.code());
                    ValidationError validationError = ValidationErrorParser.parseError(response);
                    errorLiveData.setValue(validationError);
                }
                deleteCarCall = null;
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                loadingLiveData.setValue(false);
                Log.e(TAG, "Error deleting car: " + t.getMessage());
                ValidationError validationError = new ValidationError();
                validationError.setGeneralError("Connection failed. Please check your internet connection.");
                errorLiveData.setValue(validationError);
                deleteCarCall = null;
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "ViewModel is being cleared");

        // Cancel any ongoing network calls
        if (loadCarsCall != null && !loadCarsCall.isCanceled()) {
            loadCarsCall.cancel();
            loadCarsCall = null;
        }
        if (addCarCall != null && !addCarCall.isCanceled()) {
            addCarCall.cancel();
            addCarCall = null;
        }
        if (deleteCarCall != null && !deleteCarCall.isCanceled()) {
            deleteCarCall.cancel();
            deleteCarCall = null;
        }
    }
}
