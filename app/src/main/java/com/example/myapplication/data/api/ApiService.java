package com.example.myapplication.data.api;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.DELETE;

import com.example.myapplication.data.models.Service;
import com.example.myapplication.data.models.User;
import com.example.myapplication.data.models.LoginRequest;
import com.example.myapplication.data.models.RegisterRequest;
import com.example.myapplication.data.models.AuthResponse;
import com.example.myapplication.data.models.Car;
import com.example.myapplication.data.models.CarRequest;
import com.example.myapplication.data.models.Booking;
import com.example.myapplication.data.models.BookingRequest;
import com.example.myapplication.data.models.RescheduleRequest;

public interface ApiService {
    @POST("/api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @POST("/api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest registerRequest);

    @GET("/api/auth/me")
    Call<User> getCurrentUser();

    @GET("/api/cars")
    Call<List<Car>> getCars();

    @POST("/api/cars")
    Call<Car> addCar(@Body CarRequest car);

    @PUT("/api/cars/{id}")
    Call<Car> updateCar(@Path("id") int id, @Body CarRequest car);

    @DELETE("/api/cars/{id}")
    Call<Void> deleteCar(@Path("id") int id);

    @GET("api/services")
    Call<List<Service>> getServices();

    // Bookings
    @GET("api/bookings")
    Call<List<Booking>> getBookings();

    @POST("api/bookings")
    Call<Booking> createBooking(@Body BookingRequest bookingRequest);

    @DELETE("api/bookings/{id}")
    Call<Void> cancelBooking(@Path("id") int id);

    @PUT("api/bookings/{id}/reschedule")
    Call<Booking> rescheduleBooking(@Path("id") int id, @Body RescheduleRequest request);

    @PATCH("api/bookings/{id}/status")
    Call<Booking> updateBookingStatus(@Path("id") int id, @Query("status") String status);
}
