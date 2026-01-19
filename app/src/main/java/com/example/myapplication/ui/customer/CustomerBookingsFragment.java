package com.example.myapplication.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.api.ApiService;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.models.Booking;
import com.example.myapplication.ui.customer.adapter.BookingAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerBookingsFragment extends Fragment implements BookingAdapter.OnBookingClickListener {

    private RecyclerView bookingsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;

    private BookingAdapter bookingAdapter;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customer_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupRecyclerView();
        loadBookings();
    }

    private void initViews(View view) {
        bookingsRecyclerView = view.findViewById(R.id.bookingsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);
        apiService = RetrofitClient.getApiService(requireContext());
    }

    private void setupRecyclerView() {
        bookingAdapter = new BookingAdapter(this);
        bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bookingsRecyclerView.setAdapter(bookingAdapter);
    }

    private void loadBookings() {
        showLoading(true);

        apiService.getBookings().enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<List<Booking>> call, @NonNull Response<List<Booking>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> bookings = response.body();
                    if (bookings.isEmpty()) {
                        showEmpty(true);
                    } else {
                        showEmpty(false);
                        bookingAdapter.setBookings(bookings);
                    }
                } else {
                    showError("Failed to load bookings");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Booking>> call, @NonNull Throwable t) {
                showLoading(false);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        bookingsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean show) {
        emptyText.setVisibility(show ? View.VISIBLE : View.GONE);
        bookingsRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookingClick(Booking booking) {
        Intent intent = new Intent(requireContext(), BookingDetailActivity.class);
        intent.putExtra(BookingDetailActivity.EXTRA_BOOKING_ID, booking.getId());
        intent.putExtra(BookingDetailActivity.EXTRA_SERVICE_NAME, booking.getServiceName());
        intent.putExtra(BookingDetailActivity.EXTRA_CAR_MODEL, booking.getCarModel());
        intent.putExtra(BookingDetailActivity.EXTRA_SCHEDULED_TIME, booking.getScheduledTime());
        intent.putExtra(BookingDetailActivity.EXTRA_LOCATION, booking.getLocation());
        intent.putExtra(BookingDetailActivity.EXTRA_STATUS, booking.getStatus());
        intent.putExtra(BookingDetailActivity.EXTRA_TOTAL_PRICE, booking.getTotalPrice());
        intent.putExtra(BookingDetailActivity.EXTRA_NOTES, booking.getNotes());
        startActivity(intent);
    }

    // Refresh bookings when fragment becomes visible again
    @Override
    public void onResume() {
        super.onResume();
        loadBookings();
    }
}
