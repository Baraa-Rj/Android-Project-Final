package com.example.myapplication.ui.manager;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.models.Team;
import com.example.myapplication.data.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamDetailActivity extends AppCompatActivity {
    private TextView teamNameText;
    private TextView teamDescriptionText;
    private RecyclerView membersRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private FloatingActionButton fabAddMember;

    private TeamMemberAdapter adapter;
    private Team team;
    private List<User> allUsers = new ArrayList<>();
    private List<User> currentMembers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_detail);

        // Get team data from intent
        int teamId = getIntent().getIntExtra("team_id", -1);
        String teamName = getIntent().getStringExtra("team_name");
        String teamDescription = getIntent().getStringExtra("team_description");

        if (teamId == -1) {
            Toast.makeText(this, "Invalid team", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        team = new Team();
        team.setId(teamId);
        team.setName(teamName);
        team.setDescription(teamDescription);

        // Initialize views
        teamNameText = findViewById(R.id.teamNameText);
        teamDescriptionText = findViewById(R.id.teamDescriptionText);
        membersRecyclerView = findViewById(R.id.membersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateText = findViewById(R.id.emptyStateText);
        fabAddMember = findViewById(R.id.fabAddMember);

        // Set team info
        teamNameText.setText(teamName);
        teamDescriptionText.setText(teamDescription != null && !teamDescription.isEmpty()
                ? teamDescription
                : "No description");

        // Setup RecyclerView
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TeamMemberAdapter(this::onRemoveMember);
        membersRecyclerView.setAdapter(adapter);

        // FAB click listener
        fabAddMember.setOnClickListener(v -> showAddMemberDialog());

        // Load data
        loadAllUsers();
        loadTeamMembers();
    }

    private void loadAllUsers() {
        RetrofitClient.getApiService(this).getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allUsers = response.body();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(TeamDetailActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTeamMembers() {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(this).getTeamMembers(team.getId()).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentMembers = response.body();
                    adapter.setMembers(currentMembers);

                    if (currentMembers.isEmpty()) {
                        membersRecyclerView.setVisibility(View.GONE);
                        emptyStateText.setVisibility(View.VISIBLE);
                    } else {
                        membersRecyclerView.setVisibility(View.VISIBLE);
                        emptyStateText.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(TeamDetailActivity.this, "Failed to load team members", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TeamDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddMemberDialog() {
        // Filter out users who are already members and only show employees
        List<User> availableUsers = new ArrayList<>();
        for (User user : allUsers) {
            boolean isAlreadyMember = false;
            for (User member : currentMembers) {
                if (member.getId() == user.getId()) {
                    isAlreadyMember = true;
                    break;
                }
            }
            // Only show employees who are not already members
            if (!isAlreadyMember && user.getRole() == User.Role.EMPLOYEE) {
                availableUsers.add(user);
            }
        }

        if (availableUsers.isEmpty()) {
            Toast.makeText(this, "No available employees to add", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] userNames = new String[availableUsers.size()];
        for (int i = 0; i < availableUsers.size(); i++) {
            userNames[i] = availableUsers.get(i).getName() + " (" + availableUsers.get(i).getEmail() + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("Add Member to Team")
                .setItems(userNames, (dialog, which) -> {
                    User selectedUser = availableUsers.get(which);
                    addMemberToTeam(selectedUser);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addMemberToTeam(User user) {
        progressBar.setVisibility(View.VISIBLE);

        // Create request body with user_id
        Map<String, Integer> requestBody = new HashMap<>();
        requestBody.put("user_id", user.getId());

        RetrofitClient.getApiService(this).addTeamMember(team.getId(), requestBody).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(TeamDetailActivity.this, "Member added successfully", Toast.LENGTH_SHORT).show();
                    loadTeamMembers();
                } else {
                    Toast.makeText(TeamDetailActivity.this, "Failed to add member", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TeamDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onRemoveMember(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Member")
                .setMessage("Remove " + user.getName() + " from this team?")
                .setPositiveButton("Remove", (dialog, which) -> removeMemberFromTeam(user))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeMemberFromTeam(User user) {
        progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getApiService(this).removeTeamMember(team.getId(), user.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(TeamDetailActivity.this, "Member removed successfully", Toast.LENGTH_SHORT).show();
                    loadTeamMembers();
                } else {
                    Toast.makeText(TeamDetailActivity.this, "Failed to remove member", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(TeamDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
