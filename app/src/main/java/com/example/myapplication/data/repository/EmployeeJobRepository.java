package com.example.myapplication.data.repository;


import android.content.Context;

import com.example.myapplication.data.api.ApiService;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.models.Employee;
import com.example.myapplication.data.models.EmployeeBooking;
import com.example.myapplication.data.models.EmployeeStats;
import com.example.myapplication.data.models.Booking;
import com.example.myapplication.data.models.StatusUpdateRequest;

import java.util.List;

import retrofit2.Call;

public class EmployeeJobRepository {
    private final ApiService apiService;

    public EmployeeJobRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
    }

    public Call<List<EmployeeBooking>> getEmployeeBookings(String status) {
        return apiService.getEmployeeBookings(status);
    }

    public Call<Booking> updateBookingStatus(int bookingId, String status) {
        StatusUpdateRequest request = new StatusUpdateRequest(status);
        return apiService.updateBookingStatus(bookingId, request);
    }

    public Call<Employee> getEmployeeProfile() {
        return apiService.getEmployeeProfile();
    }

    public Call<Employee> updateEmployeeStatus(String status) {
        StatusUpdateRequest request = new StatusUpdateRequest(status);
        return apiService.updateEmployeeStatus(request);
    }

    public Call<EmployeeStats> getEmployeeStats() {
        return apiService.getEmployeeStats();
    }
}
