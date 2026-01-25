package com.example.myapplication.ui.employee;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.myapplication.data.models.Booking;
import com.example.myapplication.data.repository.BookingRepository;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeBookingViewModel extends AndroidViewModel {
    private static final String TAG = "EmployeeBookingVM";
    private final BookingRepository bookingRepository;
    private final MutableLiveData<List<Booking>> bookingsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public EmployeeBookingViewModel(@NonNull Application application) {
        super(application);
        this.bookingRepository = new BookingRepository(application);
    }

    public LiveData<List<Booking>> getBookingsLiveData() {
        return bookingsLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void loadBookings() {
        loadingLiveData.setValue(true);
        bookingRepository.getBookings().enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> bookings = response.body();
                    Log.d(TAG, "Loaded " + bookings.size() + " bookings for employee");
                    for (Booking b : bookings) {
                        Log.d(TAG, "Booking ID: " + b.getId() + ", Service: " + b.getServiceName() +
                              ", Scheduled: " + b.getScheduledTime() + ", Status: " + b.getStatus());
                    }
                    bookingsLiveData.setValue(bookings);
                } else {
                    Log.e(TAG, "Failed to load bookings. Response code: " + response.code());
                    errorLiveData.setValue("Failed to load bookings");
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Network error: " + t.getMessage());
                Log.e(TAG, "Error loading bookings", t);
            }
        });
    }

    public void updateBookingStatus(int bookingId, String status) {
        loadingLiveData.setValue(true);
        bookingRepository.updateBookingStatus(bookingId, status).enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful()) {
                    loadBookings(); // Reload bookings after status update
                } else {
                    errorLiveData.setValue("Failed to update status");
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Network error: " + t.getMessage());
                Log.e(TAG, "Error updating status", t);
            }
        });
    }
}
