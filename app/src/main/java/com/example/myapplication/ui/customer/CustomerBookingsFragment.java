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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.models.Booking;
import com.example.myapplication.ui.customer.adapter.BookingAdapter;
import com.example.myapplication.ui.customer.viewmodel.BookingViewModel;

import java.util.List;

public class CustomerBookingsFragment extends Fragment implements BookingAdapter.OnBookingClickListener {

    private RecyclerView bookingsRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;

    private BookingAdapter bookingAdapter;
    private BookingViewModel viewModel;

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
        setupObservers();
        viewModel.loadBookings();
    }

    private void initViews(View view) {
        bookingsRecyclerView = view.findViewById(R.id.bookingsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyText = view.findViewById(R.id.emptyText);
        viewModel = new ViewModelProvider(this).get(BookingViewModel.class);
    }

    private void setupRecyclerView() {
        bookingAdapter = new BookingAdapter(this);
        bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bookingsRecyclerView.setAdapter(bookingAdapter);
    }

    private void setupObservers() {
        viewModel.getBookingsLiveData().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings != null) {
                if (bookings.isEmpty()) {
                    showEmpty(true);
                } else {
                    showEmpty(false);
                    bookingAdapter.setBookings(bookings);
                }
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                String errorMessage = error.hasGeneralError() ?
                    error.getGeneralError() : "Failed to load bookings";
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                showLoading(isLoading);
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
        viewModel.loadBookings();
    }
}
