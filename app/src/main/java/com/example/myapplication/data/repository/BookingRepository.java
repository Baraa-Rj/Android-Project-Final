package com.example.myapplication.data.repository;

import android.content.Context;
import com.example.myapplication.data.api.ApiService;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.models.Booking;
import com.example.myapplication.data.models.BookingRequest;
import com.example.myapplication.data.models.RescheduleRequest;
import java.util.List;
import retrofit2.Call;

public class BookingRepository {
    private final ApiService apiService;

    public BookingRepository(Context context) {
        this.apiService = RetrofitClient.getApiService(context);
    }

    public Call<List<Booking>> getBookings() {
        return apiService.getBookings();
    }

    public Call<Booking> createBooking(BookingRequest bookingRequest) {
        return apiService.createBooking(bookingRequest);
    }

    public Call<Void> cancelBooking(int bookingId) {
        return apiService.cancelBooking(bookingId);
    }

    public Call<Booking> rescheduleBooking(int bookingId, RescheduleRequest request) {
        return apiService.rescheduleBooking(bookingId, request);
    }

    public Call<Booking> updateBookingStatus(int bookingId, String status) {
        return apiService.updateBookingStatus(bookingId, status);
    }
}
