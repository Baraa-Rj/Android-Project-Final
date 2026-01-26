package com.example.myapplication.ui.customer.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.data.models.Booking;
import com.example.myapplication.data.models.BookingRequest;
import com.example.myapplication.data.models.ValidationError;
import com.example.myapplication.data.repository.BookingRepository;
import com.example.myapplication.utils.ValidationErrorParser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingViewModel extends AndroidViewModel {
    private static final String TAG = "BookingViewModel";

    private final BookingRepository bookingRepository;
    private final MutableLiveData<List<Booking>> bookingsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Booking> bookingCreatedLiveData = new MutableLiveData<>();
    private final MutableLiveData<ValidationError> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> bookingCancelledLiveData = new MutableLiveData<>();

    // Track ongoing calls for cleanup
    private Call<List<Booking>> loadBookingsCall;
    private Call<Booking> createBookingCall;
    private Call<Void> cancelBookingCall;

    public BookingViewModel(@NonNull Application application) {
        super(application);
        this.bookingRepository = new BookingRepository(application);
        Log.d(TAG, "BookingViewModel initialized");
    }

    // Package-private constructor for testing
    BookingViewModel(@NonNull Application application, BookingRepository bookingRepository) {
        super(application);
        this.bookingRepository = bookingRepository;
    }

    public LiveData<List<Booking>> getBookingsLiveData() {
        return bookingsLiveData;
    }

    public LiveData<Booking> getBookingCreatedLiveData() {
        return bookingCreatedLiveData;
    }

    public LiveData<ValidationError> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public LiveData<Boolean> getBookingCancelledLiveData() {
        return bookingCancelledLiveData;
    }

    public void loadBookings() {
        Log.d(TAG, "Loading bookings...");
        loadingLiveData.setValue(true);

        loadBookingsCall = bookingRepository.getBookings();
        loadBookingsCall.enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<List<Booking>> call, @NonNull Response<List<Booking>> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> allBookings = response.body();

                    // Filter to show only upcoming/unfinished bookings (exclude completed and cancelled)
                    List<Booking> upcomingBookings = new ArrayList<>();
                    for (Booking booking : allBookings) {
                        String status = booking.getStatus();
                        if (status != null &&
                            !status.equalsIgnoreCase("completed") &&
                            !status.equalsIgnoreCase("cancelled")) {
                            upcomingBookings.add(booking);
                        }
                    }

                    bookingsLiveData.setValue(upcomingBookings);
                    Log.d(TAG, "Loaded " + upcomingBookings.size() + " upcoming bookings (filtered from " + allBookings.size() + " total)");
                } else {
                    Log.e(TAG, "Failed to load bookings: " + response.code());
                    ValidationError validationError = ValidationErrorParser.parseError(response);
                    errorLiveData.setValue(validationError);
                }
                loadBookingsCall = null;
            }

            @Override
            public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
                loadingLiveData.setValue(false);
                Log.e(TAG, "Error loading bookings: " + t.getMessage());
                ValidationError validationError = new ValidationError();
                validationError.setGeneralError("Connection failed. Please check your internet connection.");
                errorLiveData.setValue(validationError);
                loadBookingsCall = null;
            }
        });
    }

    public void createBooking(BookingRequest bookingRequest) {
        Log.d(TAG, "Creating booking for service ID: " + bookingRequest.getServiceId());
        loadingLiveData.setValue(true);

        createBookingCall = bookingRepository.createBooking(bookingRequest);
        createBookingCall.enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(@NonNull Call<Booking> call, @NonNull Response<Booking> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    Booking booking = response.body();
                    Log.d(TAG, "Booking created successfully with ID: " + booking.getId());
                    bookingCreatedLiveData.setValue(booking);
                } else {
                    Log.e(TAG, "Failed to create booking: " + response.code());
                    ValidationError validationError = ValidationErrorParser.parseError(response);
                    errorLiveData.setValue(validationError);
                }
                createBookingCall = null;
            }

            @Override
            public void onFailure(@NonNull Call<Booking> call, @NonNull Throwable t) {
                loadingLiveData.setValue(false);
                Log.e(TAG, "Error creating booking: " + t.getMessage());
                ValidationError validationError = new ValidationError();
                validationError.setGeneralError("Connection failed. Please check your internet connection.");
                errorLiveData.setValue(validationError);
                createBookingCall = null;
            }
        });
    }

    public void cancelBooking(int bookingId) {
        Log.d(TAG, "Cancelling booking ID: " + bookingId);
        loadingLiveData.setValue(true);

        cancelBookingCall = bookingRepository.cancelBooking(bookingId);
        cancelBookingCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful()) {
                    Log.d(TAG, "Booking cancelled successfully");
                    bookingCancelledLiveData.setValue(true);
                    // Reload bookings to get updated list
                    loadBookings();
                } else {
                    Log.e(TAG, "Failed to cancel booking: " + response.code());
                    ValidationError validationError = ValidationErrorParser.parseError(response);
                    errorLiveData.setValue(validationError);
                }
                cancelBookingCall = null;
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                loadingLiveData.setValue(false);
                Log.e(TAG, "Error cancelling booking: " + t.getMessage());
                ValidationError validationError = new ValidationError();
                validationError.setGeneralError("Connection failed. Please check your internet connection.");
                errorLiveData.setValue(validationError);
                cancelBookingCall = null;
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "ViewModel is being cleared");

        // Cancel any ongoing network calls
        if (loadBookingsCall != null && !loadBookingsCall.isCanceled()) {
            loadBookingsCall.cancel();
            loadBookingsCall = null;
        }
        if (createBookingCall != null && !createBookingCall.isCanceled()) {
            createBookingCall.cancel();
            createBookingCall = null;
        }
        if (cancelBookingCall != null && !cancelBookingCall.isCanceled()) {
            cancelBookingCall.cancel();
            cancelBookingCall = null;
        }
    }
}
