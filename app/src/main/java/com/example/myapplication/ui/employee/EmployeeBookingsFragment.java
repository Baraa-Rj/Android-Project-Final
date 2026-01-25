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
import com.example.myapplication.ui.employee.viewmodel.EmployeeBookingViewModel;

public class EmployeeBookingsFragment extends Fragment {
    private static final String TAG = "EmployeeBookingsFragment";

    private EmployeeBookingViewModel viewModel;
    private EmployeeBookingAdapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employee_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerView = view.findViewById(R.id.bookingsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EmployeeBookingAdapter(this::onBookingClick);
        recyclerView.setAdapter(adapter);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(EmployeeBookingViewModel.class);

        // Observe LiveData
        observeViewModel();

        // Load bookings
        viewModel.loadBookings();
    }

    private void observeViewModel() {
        // Observe bookings - show ALL team bookings (not filtered by date)
        viewModel.getBookingsLiveData().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings != null) {
                adapter.setBookings(bookings);

                // Show/hide empty state
                if (bookings.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyStateText.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyStateText.setVisibility(View.GONE);
                }
            }
        });

        // Observe loading state
        viewModel.getLoadingLiveData().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observe errors
        viewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onBookingClick(Booking booking) {
        // Show dialog to update booking status
        showStatusUpdateDialog(booking);
    }

    private void showStatusUpdateDialog(Booking booking) {
        String currentStatus = booking.getStatus();
        String[] statusOptions;

        // Determine available status transitions
        if ("pending".equalsIgnoreCase(currentStatus) || "assigned".equalsIgnoreCase(currentStatus)) {
            statusOptions = new String[] { "Start Work (In Progress)" };
        } else if ("in_progress".equalsIgnoreCase(currentStatus)) {
            statusOptions = new String[] { "Mark as Completed" };
        } else {
            // Already completed or cancelled - no action
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
