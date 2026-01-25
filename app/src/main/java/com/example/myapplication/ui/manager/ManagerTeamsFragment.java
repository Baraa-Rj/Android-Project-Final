package com.example.myapplication.ui.manager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.example.myapplication.data.models.Team;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManagerTeamsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private FloatingActionButton fabAddTeam;
    private ManagerTeamAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manager_teams, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.teamsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        fabAddTeam = view.findViewById(R.id.fabAddTeam);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ManagerTeamAdapter(this::onTeamClick);
        recyclerView.setAdapter(adapter);

        fabAddTeam.setOnClickListener(v -> showCreateTeamDialog());

        loadTeams();
    }

    private void loadTeams() {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService().getTeams().enqueue(new Callback<List<Team>>() {
            @Override
            public void onResponse(Call<List<Team>> call, Response<List<Team>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<Team> teams = response.body();
                    adapter.setTeams(teams);

                    if (teams.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyStateText.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyStateText.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load teams", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Team>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCreateTeamDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_create_team, null);
        EditText nameInput = dialogView.findViewById(R.id.teamNameInput);
        EditText descInput = dialogView.findViewById(R.id.teamDescInput);

        new AlertDialog.Builder(getContext())
                .setTitle("Create New Team")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String description = descInput.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Team name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    createTeam(name, description);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createTeam(String name, String description) {
        Team newTeam = new Team();
        newTeam.setName(name);
        newTeam.setDescription(description);

        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService().createTeam(newTeam).enqueue(new Callback<Team>() {
            @Override
            public void onResponse(Call<Team> call, Response<Team> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Team created successfully", Toast.LENGTH_SHORT).show();
                    loadTeams();
                } else {
                    Toast.makeText(getContext(), "Failed to create team", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Team> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onTeamClick(Team team) {
        Toast.makeText(getContext(), "Team: " + team.getName(), Toast.LENGTH_SHORT).show();
    }
}
