package com.example.myapplication.ui.Employee.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.data.models.Employee;
import com.example.myapplication.data.models.EmployeeBooking;
import com.example.myapplication.data.models.EmployeeStats;
import com.example.myapplication.data.models.Booking;
import com.example.myapplication.data.models.ValidationError;
import com.example.myapplication.data.repository.EmployeeJobRepository;
import com.example.myapplication.utils.ValidationErrorParser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeJobViewModel extends AndroidViewModel {
    private static final String TAG = "EmployeeViewModel";

    private final EmployeeJobRepository employeeRepository;
    private final MutableLiveData<List<EmployeeBooking>> bookingsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Employee> employeeLiveData = new MutableLiveData<>();
    private final MutableLiveData<EmployeeStats> statsLiveData = new MutableLiveData<>();
    private final MutableLiveData<ValidationError> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> statusUpdatedLiveData = new MutableLiveData<>();

    private Call<List<EmployeeBooking>> loadBookingsCall;
    private Call<Booking> updateStatusCall;
    private Call<Employee> profileCall;
    private Call<EmployeeStats> statsCall;

    public EmployeeJobViewModel(@NonNull Application application) {
        super(application);
        this.employeeRepository = new EmployeeJobRepository(application);
        Log.d(TAG, "EmployeeViewModel initialized");
    }

    public LiveData<List<EmployeeBooking>> getBookingsLiveData() { return bookingsLiveData; }
    public LiveData<Employee> getEmployeeLiveData() { return employeeLiveData; }
    public LiveData<EmployeeStats> getStatsLiveData() { return statsLiveData; }
    public LiveData<ValidationError> getErrorLiveData() { return errorLiveData; }
    public LiveData<Boolean> getLoadingLiveData() { return loadingLiveData; }
    public LiveData<Boolean> getStatusUpdatedLiveData() { return statusUpdatedLiveData; }

    public void loadBookings(String status) {
        Log.d(TAG, "Loading bookings with status: " + status);
        loadingLiveData.setValue(true);

        loadBookingsCall = employeeRepository.getEmployeeBookings(status);
        loadBookingsCall.enqueue(new Callback<List<EmployeeBooking>>() {
            @Override
            public void onResponse(@NonNull Call<List<EmployeeBooking>> call, @NonNull Response<List<EmployeeBooking>> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    bookingsLiveData.setValue(response.body());
                    Log.d(TAG, "Loaded " + response.body().size() + " bookings");
                } else {
                    ValidationError error = ValidationErrorParser.parseError(response);
                    errorLiveData.setValue(error);
                }
                loadBookingsCall = null;
            }

            @Override
            public void onFailure(@NonNull Call<List<EmployeeBooking>> call, @NonNull Throwable t) {
                loadingLiveData.setValue(false);
                ValidationError error = new ValidationError();
                error.setGeneralError("Connection failed. Please check your internet connection.");
                errorLiveData.setValue(error);
                loadBookingsCall = null;
            }
        });
    }

    public void updateBookingStatus(int bookingId, String status) {
        Log.d(TAG, "Updating booking " + bookingId + " to status: " + status);
        loadingLiveData.setValue(true);

        updateStatusCall = employeeRepository.updateBookingStatus(bookingId, status);
        updateStatusCall.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(@NonNull Call<Booking> call, @NonNull Response<Booking> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful()) {
                    statusUpdatedLiveData.setValue(true);
                    Log.d(TAG, "Status updated successfully");
                } else {
                    ValidationError error = ValidationErrorParser.parseError(response);
                    errorLiveData.setValue(error);
                }
                updateStatusCall = null;
            }

            @Override
            public void onFailure(@NonNull Call<Booking> call, @NonNull Throwable t) {
                loadingLiveData.setValue(false);
                ValidationError error = new ValidationError();
                error.setGeneralError("Connection failed. Please check your internet connection.");
                errorLiveData.setValue(error);
                updateStatusCall = null;
            }
        });
    }

    public void loadEmployeeProfile() {
        Log.d(TAG, "Loading employee profile...");
        loadingLiveData.setValue(true);

        profileCall = employeeRepository.getEmployeeProfile();
        profileCall.enqueue(new Callback<Employee>() {
            @Override
            public void onResponse(@NonNull Call<Employee> call, @NonNull Response<Employee> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    employeeLiveData.setValue(response.body());
                } else {
                    ValidationError error = ValidationErrorParser.parseError(response);
                    errorLiveData.setValue(error);
                }
                profileCall = null;
            }

            @Override
            public void onFailure(@NonNull Call<Employee> call, @NonNull Throwable t) {
                loadingLiveData.setValue(false);
                ValidationError error = new ValidationError();
                error.setGeneralError("Connection failed.");
                errorLiveData.setValue(error);
                profileCall = null;
            }
        });
    }

    public void loadEmployeeStats() {
        Log.d(TAG, "Loading employee stats...");

        statsCall = employeeRepository.getEmployeeStats();
        statsCall.enqueue(new Callback<EmployeeStats>() {
            @Override
            public void onResponse(@NonNull Call<EmployeeStats> call, @NonNull Response<EmployeeStats> response) {
                if (response.isSuccessful() && response.body() != null) {
                    statsLiveData.setValue(response.body());
                }
                statsCall = null;
            }

            @Override
            public void onFailure(@NonNull Call<EmployeeStats> call, @NonNull Throwable t) {
                statsCall = null;
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (loadBookingsCall != null) loadBookingsCall.cancel();
        if (updateStatusCall != null) updateStatusCall.cancel();
        if (profileCall != null) profileCall.cancel();
        if (statsCall != null) statsCall.cancel();
    }
}
