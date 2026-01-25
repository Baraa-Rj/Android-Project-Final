package com.example.myapplication.ui.manager;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.models.Booking;
import com.example.myapplication.data.models.Team;
import com.example.myapplication.ui.manager.adapter.ManagerBookingAdapter;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerBookingsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private ManagerBookingAdapter adapter;
    private List<Team> teams = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_bookings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.bookingsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ManagerBookingAdapter(this::onBookingClick);
        recyclerView.setAdapter(adapter);

        loadTeams();
        loadBookings();
    }

    private void loadTeams() {
        RetrofitClient.getApiService(requireContext()).getTeams().enqueue(new Callback<List<Team>>() {
            @Override
            public void onResponse(Call<List<Team>> call, Response<List<Team>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    teams = response.body();
                }
            }

            @Override
            public void onFailure(Call<List<Team>> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load teams", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadBookings() {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(requireContext()).getBookings().enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> bookings = response.body();
                    adapter.setBookings(bookings);

                    if (bookings.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyStateText.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyStateText.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load bookings", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onBookingClick(Booking booking) {
        showTeamAssignmentDialog(booking);
    }

    private void showTeamAssignmentDialog(Booking booking) {
        if (teams.isEmpty()) {
            Toast.makeText(getContext(), "No teams available. Create a team first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] teamNames = new String[teams.size()];
        for (int i = 0; i < teams.size(); i++) {
            teamNames[i] = teams.get(i).getName();
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Assign Team to Booking")
                .setItems(teamNames, (dialog, which) -> {
                    Team selectedTeam = teams.get(which);
                    assignTeam(booking.getId(), selectedTeam.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void assignTeam(int bookingId, int teamId) {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(requireContext()).assignTeamToBooking(bookingId, teamId).enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Team assigned successfully", Toast.LENGTH_SHORT).show();
                    loadBookings();
                } else {
                    Toast.makeText(getContext(), "Failed to assign team", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
