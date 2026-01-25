package com.example.myapplication.ui.employee;

import android.app.AlertDialog;
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
import com.example.myapplication.ui.employee.adapter.EmployeeBookingAdapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EmployeeHomeFragment extends Fragment {
    private static final String TAG = "EmployeeHomeFragment";

    private EmployeeBookingViewModel viewModel;
    private EmployeeBookingAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employee_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.bookingsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EmployeeBookingAdapter(this::onBookingClick);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(EmployeeBookingViewModel.class);

        observeViewModel();

        viewModel.loadBookings();
    }

    private void observeViewModel() {
        viewModel.getBookingsLiveData().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings != null) {
                List<Booking> upcomingBookings = filterUpcomingBookings(bookings);
                adapter.setBookings(upcomingBookings);

                if (upcomingBookings.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyStateText.setVisibility(View.VISIBLE);
                    emptyStateText.setText("No upcoming bookings");
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyStateText.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Booking> filterUpcomingBookings(List<Booking> bookings) {
        List<Booking> upcomingBookings = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = dateFormat.format(new Date());

        for (Booking booking : bookings) {
            String bookingDate = booking.getScheduledTime();
            // Show bookings scheduled for today or future dates
            if (bookingDate != null && bookingDate.compareTo(today) >= 0) {
                upcomingBookings.add(booking);
            }
        }

        return upcomingBookings;
    }

    private void onBookingClick(Booking booking) {
        showStatusUpdateDialog(booking);
    }

    private void showStatusUpdateDialog(Booking booking) {
        String currentStatus = booking.getStatus();
        String[] statusOptions;

        if ("pending".equalsIgnoreCase(currentStatus) || "assigned".equalsIgnoreCase(currentStatus)) {
            statusOptions = new String[] { "Start Work (In Progress)" };
        } else if ("in_progress".equalsIgnoreCase(currentStatus)) {
            statusOptions = new String[] { "Mark as Completed" };
        } else {
            Toast.makeText(getContext(), "Booking is already " + currentStatus, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Update Booking Status")
                .setItems(statusOptions, (dialog, which) -> {
                    String newStatus = which == 0
                            ? (currentStatus.equalsIgnoreCase("in_progress") ? "completed" : "in_progress")
                            : currentStatus;
                    viewModel.updateBookingStatus(booking.getId(), newStatus);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
