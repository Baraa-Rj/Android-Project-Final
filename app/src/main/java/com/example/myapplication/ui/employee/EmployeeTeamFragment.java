package com.example.myapplication.ui.employee;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.data.api.RetrofitClient;
import com.example.myapplication.data.api.ApiService;
import com.example.myapplication.data.models.Team;
import com.example.myapplication.data.models.TeamMember;
import com.example.myapplication.data.models.TeamResponse;
import com.example.myapplication.ui.employee.adapter.TeamMemberAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class EmployeeTeamFragment extends Fragment {
    private static final String TAG = "EmployeeTeamFragment";

    private CardView teamInfoCard;
    private TextView teamName;
    private TextView teamDescription;
    private TextView membersTitle;
    private RecyclerView teamMembersRecyclerView;
    private TextView emptyStateText;
    private ProgressBar progressBar;
    private TeamMemberAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_employee_team, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        loadTeamData();
    }

    private void initViews(View view) {
        teamInfoCard = view.findViewById(R.id.teamInfoCard);
        teamName = view.findViewById(R.id.teamName);
        teamDescription = view.findViewById(R.id.teamDescription);
        membersTitle = view.findViewById(R.id.membersTitle);
        teamMembersRecyclerView = view.findViewById(R.id.teamMembersRecyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new TeamMemberAdapter();
        teamMembersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        teamMembersRecyclerView.setAdapter(adapter);
    }

    private void loadTeamData() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
        teamInfoCard.setVisibility(View.GONE);
        membersTitle.setVisibility(View.GONE);
        teamMembersRecyclerView.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient(requireContext()).create(ApiService.class);
        Call<TeamResponse> call = apiService.getMyTeam();

        call.enqueue(new Callback<TeamResponse>() {
            @Override
            public void onResponse(Call<TeamResponse> call, Response<TeamResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    TeamResponse teamResponse = response.body();
                    Team team = teamResponse.getTeam();
                    List<TeamMember> members = teamResponse.getMembers();

                    if (team != null) {
                        displayTeamData(team, members);
                    } else {
                        showEmptyState();
                    }
                } else {
                    Log.e(TAG, "Failed to load team. Response code: " + response.code());
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<TeamResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Error loading team", t);
                Toast.makeText(getContext(), "Failed to load team: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void displayTeamData(Team team, List<TeamMember> members) {
        // Show team info
        teamInfoCard.setVisibility(View.VISIBLE);
        teamName.setText(team.getName());

        if (team.getDescription() != null && !team.getDescription().isEmpty()) {
            teamDescription.setText(team.getDescription());
            teamDescription.setVisibility(View.VISIBLE);
        } else {
            teamDescription.setVisibility(View.GONE);
        }

        // Show team members
        if (members != null && !members.isEmpty()) {
            membersTitle.setVisibility(View.VISIBLE);
            teamMembersRecyclerView.setVisibility(View.VISIBLE);
            adapter.setMembers(members);
            Log.d(TAG, "Displaying " + members.size() + " team members");
        } else {
            membersTitle.setVisibility(View.GONE);
            teamMembersRecyclerView.setVisibility(View.GONE);
            Log.d(TAG, "No team members to display");
        }
    }

    private void showEmptyState() {
        teamInfoCard.setVisibility(View.GONE);
        membersTitle.setVisibility(View.GONE);
        teamMembersRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
    }
}
